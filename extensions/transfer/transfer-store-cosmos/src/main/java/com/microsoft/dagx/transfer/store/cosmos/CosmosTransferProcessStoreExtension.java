/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.transfer.store.cosmos;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientBuilder;
import com.microsoft.dagx.common.string.StringUtils;
import com.microsoft.dagx.spi.DagxException;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.system.ServiceExtension;
import com.microsoft.dagx.spi.system.ServiceExtensionContext;
import com.microsoft.dagx.spi.transfer.store.TransferProcessStore;

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
                .credential(new AzureKeyCredential(accountKey))
                .preferredRegions(preferredRegions)
                .userAgentSuffix("CosmosDBJavaQuickstart")
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildClient();


        context.registerService(TransferProcessStore.class, new CosmosTransferProcessStore(client, cosmosDbName, "dagx-transferprocess"));

        monitor = context.getMonitor();
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

}

