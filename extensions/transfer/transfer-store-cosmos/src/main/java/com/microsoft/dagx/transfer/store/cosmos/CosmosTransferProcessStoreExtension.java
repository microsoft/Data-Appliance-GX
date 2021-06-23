/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.transfer.store.cosmos;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.microsoft.dagx.common.string.StringUtils;
import com.microsoft.dagx.spi.DagxException;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.system.ServiceExtension;
import com.microsoft.dagx.spi.system.ServiceExtensionContext;
import com.microsoft.dagx.spi.transfer.store.TransferProcessStore;
import com.microsoft.dagx.transfer.store.cosmos.model.TransferProcessDocument;

import java.util.ArrayList;

/**
 * Provides an in-memory implementation of the {@link com.microsoft.dagx.spi.transfer.store.TransferProcessStore} for testing.
 */
public class CosmosTransferProcessStoreExtension implements ServiceExtension {
    private final static String COSMOS_ACCOUNTNAME_SETTING = "dagx.cosmos.account.name";
    private final static String COSMOS_DBNAME_SETTING = "dagx.cosmos.database.name";
    private Monitor monitor;

    @Override
    public void initialize(ServiceExtensionContext context) {

        monitor = context.getMonitor();
        monitor.info("Initializing Cosmos Memory Transfer Process Store extension...");

        var cosmosAccountName = context.getSetting(COSMOS_ACCOUNTNAME_SETTING, null);
        if (StringUtils.isNullOrEmpty(cosmosAccountName)) {
            throw new DagxException("'" + COSMOS_ACCOUNTNAME_SETTING + "' cannot be null or empty!");
        }
        var cosmosDbName = context.getSetting(COSMOS_DBNAME_SETTING, null);
        if (StringUtils.isNullOrEmpty(cosmosDbName)) {
            throw new DagxException("'" + COSMOS_DBNAME_SETTING + "' cannot be null or empty!");
        }

        var vault = context.getService(Vault.class);
        var accountKey = vault.resolveSecret(cosmosAccountName);
        if (StringUtils.isNullOrEmpty(accountKey)) {
            throw new DagxException("No credentials found in vault for Cosmos DB '" + cosmosAccountName + "'");
        }

        final String host = "https://" + cosmosAccountName + ".documents.azure.com:443/";

        ArrayList<String> preferredRegions = new ArrayList<>();
        preferredRegions.add("West US");
        var client = new CosmosClientBuilder()
                .endpoint(host)
                .key(accountKey)
                .preferredRegions(preferredRegions)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildClient();


        var database = getDatabase(client, cosmosDbName);
        final CosmosContainerResponse response = database.createContainerIfNotExists("dagx-transferprocess", "/partitionKey");
        var container = database.getContainer(response.getProperties().getId());

        context.registerService(TransferProcessStore.class, new CosmosTransferProcessStore(container, context.getTypeManager()));

        context.getTypeManager().registerTypes(TransferProcessDocument.class);
        monitor.info("Initialized Cosmos Memory Transfer Process Store extension");

    }


    @Override
    public void start() {
        monitor.info("Started Initialized Cosmos Transfer Process Store extension");
    }

    @Override
    public void shutdown() {
        monitor.info("Shutdown Initialized Cosmos Transfer Process Store extension");
    }

    private CosmosDatabase getDatabase(CosmosClient client, String databaseName) {
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        return client.getDatabase(databaseResponse.getProperties().getId());
    }
}

