/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.transfer.store.cosmos;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.microsoft.dagx.spi.DagxException;
import com.microsoft.dagx.spi.transfer.store.TransferProcessStore;
import com.microsoft.dagx.spi.types.TypeManager;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcess;
import com.microsoft.dagx.transfer.store.cosmos.model.TransferProcessDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CosmosTransferProcessStore implements TransferProcessStore {


    private final CosmosContainer container;
    private final CosmosQueryRequestOptions tracingOptions;
    private final TypeManager typeManager;

    public CosmosTransferProcessStore(CosmosContainer container, TypeManager typeManager) {

        this.container = container;
        this.typeManager = typeManager;
        tracingOptions = new CosmosQueryRequestOptions();
        tracingOptions.setQueryMetricsEnabled(true);
    }

    @Override
    public TransferProcess find(String id) {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        final CosmosItemResponse<Object> response = container.readItem(id, new PartitionKey(id), options, Object.class);

        var obj = response.getItem();


        return convertObject(obj).getWrappedInstance();
    }

    @Override
    public @Nullable String processIdForTransferId(String id) {
        var query = "SELECT * FROM TransferProcessDocument WHERE TransferProcessDocument.partitionKey = '" + id + "'";

        try {
            var response = container.queryItems(query, tracingOptions, Object.class);
            return response.stream()
                    .map(this::convertObject)
                    .map(pd -> pd.getWrappedInstance().getId()).findFirst().orElse(null);
        } catch (CosmosException ex) {
            throw new DagxException(ex);
        }
    }

    @Override
    public @NotNull List<TransferProcess> nextForState(int state, int max) {
        //todo: lock rows for update

        tracingOptions.setMaxBufferedItemCount(max);

        var query = "SELECT * FROM TransferProcessDocument WHERE TransferProcessDocument.state = " + state + " ORDER BY TransferProcessDocument.stateTimestamp OFFSET 0 LIMIT " + max;

        final CosmosPagedIterable<Object> processes = container.queryItems(query, tracingOptions, Object.class);

        return processes.stream()
                .map(this::convertObject)
                .map(TransferProcessDocument::getWrappedInstance)
                .collect(Collectors.toList());
    }

    @Override
    public void create(TransferProcess process) {

        Objects.requireNonNull(process.getId(), "TransferProcesses must have an ID!");
        process.transitionInitial();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        //todo: configure indexing
        var document = TransferProcessDocument.from(process, process.getDataRequest().getId());
        try {
            final var response = container.createItem(document, new PartitionKey(process.getId()), options);
            handleResponse(response);
        } catch (CosmosException cme) {
            throw new DagxException(cme);
        }
    }


    @Override
    public void update(TransferProcess process) {
        var document = TransferProcessDocument.from(process, process.getDataRequest().getId());
        try {
            final var response = container.upsertItem(document, new PartitionKey(process.getId()), new CosmosItemRequestOptions());
            handleResponse(response);
        } catch (CosmosException cme) {
            throw new DagxException(cme);
        }
    }

    @Override
    public void delete(String processId) {
        try {
            var response = container.deleteItem(processId, new PartitionKey(processId), new CosmosItemRequestOptions());
            handleResponse(response);
        } catch (CosmosException cme) {
            throw new DagxException(cme);
        }

    }

    @Override
    public void createData(String processId, String key, Object data) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void updateData(String processId, String key, Object data) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void deleteData(String processId, String key) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void deleteData(String processId, Set<String> keys) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public <T> T findData(Class<T> type, String processId, String resourceDefinitionId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void handleResponse(CosmosItemResponse<?> response) {
        final int code = response.getStatusCode();
        if (code < 200 || code >= 300) {
            throw new DagxException("Error creating TransferProcess in CosmosDB: " + code);
        }
    }

    private TransferProcessDocument convertObject(Object databaseDocument) {
        return typeManager.readValue(typeManager.writeValueAsBytes(databaseDocument), TransferProcessDocument.class);
    }
}
