/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.transfer.store.cosmos;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.microsoft.dagx.spi.transfer.store.TransferProcessStore;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcess;
import com.microsoft.dagx.transfer.store.cosmos.model.TransferProcessDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class CosmosTransferProcessStore implements TransferProcessStore {


    private final String containerName;
    private final CosmosContainer container;

    public CosmosTransferProcessStore(CosmosClient client, String cosmosDbName, String containerName) {
        this.containerName = containerName;
        var database = validateCosmos(client, cosmosDbName);
        container = database.getContainer(containerName);
    }

    @Override
    public TransferProcess find(String id) {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        //  Set populate query metrics to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true);

        container.readItem(id, new PartitionKey(id), TransferProcessDocument.class);

        return null;
    }

    @Override
    public @Nullable String processIdForTransferId(String id) {
        return null;
    }

    @Override
    public @NotNull List<TransferProcess> nextForState(int state, int max) {
        return null;
    }

    @Override
    public void create(TransferProcess process) {

    }

    @Override
    public void update(TransferProcess process) {

    }

    @Override
    public void delete(String processId) {

    }

    @Override
    public void createData(String processId, String key, Object data) {

    }

    @Override
    public void updateData(String processId, String key, Object data) {

    }

    @Override
    public void deleteData(String processId, String key) {

    }

    @Override
    public void deleteData(String processId, Set<String> keys) {

    }

    @Override
    public <T> T findData(Class<T> type, String processId, String resourceDefinitionId) {
        return null;
    }

    private CosmosDatabase validateCosmos(CosmosClient client, String databaseName) {
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        return client.getDatabase(databaseResponse.getProperties().getId());
    }
}
