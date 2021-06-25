/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.transfer.store.cosmos;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosStoredProcedure;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.*;
import com.microsoft.dagx.spi.DagxException;
import com.microsoft.dagx.spi.transfer.store.TransferProcessStore;
import com.microsoft.dagx.spi.types.TypeManager;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcess;
import com.microsoft.dagx.transfer.store.cosmos.model.TransferProcessDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CosmosTransferProcessStore implements TransferProcessStore {


    private final CosmosContainer container;
    private final CosmosQueryRequestOptions tracingOptions;
    private final TypeManager typeManager;
    private final String partitionKey;

    public CosmosTransferProcessStore(CosmosContainer container, TypeManager typeManager, String partitionKey) {

        this.container = container;
        this.typeManager = typeManager;
        this.partitionKey = partitionKey;
        tracingOptions = new CosmosQueryRequestOptions();
        tracingOptions.setQueryMetricsEnabled(true);
    }

    @Override
    public TransferProcess find(String id) {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        try {
            final CosmosItemResponse<Object> response = container.readItem(id, new PartitionKey(partitionKey), options, Object.class);
            var obj = response.getItem();

            return convertObject(obj).getWrappedInstance();
        } catch (NotFoundException ex) {
            return null;
        }

    }

    @Override
    public @Nullable String processIdForTransferId(String transferId) {
        var query = "SELECT * FROM TransferProcessDocument WHERE TransferProcessDocument.dataRequest.id = '" + transferId + "'";

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

        tracingOptions.setMaxBufferedItemCount(max);

        var sproc = getStoredProcedure();
        List<Object> params = Arrays.asList(state, max, getConnectorId());
        var options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(new PartitionKey(partitionKey));

        final CosmosStoredProcedureResponse response = sproc.execute(params, options);
        var rawJson = response.getResponseAsString();

        //now we need to convert to a list, convert each element in that list to json, and convert that back to a TransferProcessDocument
        var l = typeManager.readValue(rawJson, List.class);

        //noinspection unchecked
        return (List<TransferProcess>) l.stream().map(typeManager::writeValueAsString)
                .map(json -> typeManager.readValue(json.toString(), TransferProcessDocument.class))
                .map(tp -> ((TransferProcessDocument) tp).getWrappedInstance())
                .collect(Collectors.toList());

    }

    private String getConnectorId() {
        return "dagx-connector";
    }

    private CosmosStoredProcedure getStoredProcedure() {
        return container.getScripts().getStoredProcedure("nextForState");
    }


    @Override
    public void create(TransferProcess process) {

        Objects.requireNonNull(process.getId(), "TransferProcesses must have an ID!");
        process.transitionInitial();

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        //todo: configure indexing
        var document = TransferProcessDocument.from(process, partitionKey, process.getDataRequest().getId());
        try {
            final var response = container.createItem(document, new PartitionKey(partitionKey), options);
            /**/
            handleResponse(response);
        } catch (CosmosException cme) {
            throw new DagxException(cme);
        }
    }


    @Override
    public void update(TransferProcess process) {
        var document = TransferProcessDocument.from(process, partitionKey, process.getDataRequest().getId());
        try {
            final var response = container.upsertItem(document, new PartitionKey(partitionKey), new CosmosItemRequestOptions());
            handleResponse(response);
        } catch (CosmosException cme) {
            throw new DagxException(cme);
        } finally {
//            releaseLease(process.getId());
        }
    }

    @Override
    public void delete(String processId) {
        try {
            var response = container.deleteItem(processId, new PartitionKey(partitionKey), new CosmosItemRequestOptions());
            handleResponse(response);
        } catch (NotFoundException ignored) {
            //noop
        } catch (CosmosException cme) {
            throw new DagxException(cme);
        } finally {
            releaseLease(processId);
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

    @Override
    public void lock(TransferProcess process) {
        final String transferProcessId = process.getId();
//        acquireLease(transferProcessId);
    }

    @Override
    public void unlock(TransferProcess process) {
//        releaseLease(process.getId());
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

    private void acquireLease(String transferProcessId) {
//        blobStoreApi.acquireLease(leaseConfiguration.getAccountName(), leaseConfiguration.getContainerName(), transferProcessId, transferProcessId, -1);
    }

    private void releaseLease(String transferProcessId) {
//        blobStoreApi.releaseLease(leaseConfiguration.getAccountName(), leaseConfiguration.getContainerName(), transferProcessId, transferProcessId);
    }

}
