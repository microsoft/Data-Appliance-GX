/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.transfer.store.cosmos;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.microsoft.dagx.spi.DagxException;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcess;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcessStates;
import com.microsoft.dagx.transfer.store.cosmos.model.TransferProcessDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.microsoft.dagx.common.ConfigurationFunctions.propOrEnv;
import static com.microsoft.dagx.transfer.store.cosmos.TestHelper.createTransferProcess;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CosmosTransferProcessStoreTest {

    private final static String accountName = "cosmos-itest";
    private final static String databaseName = "connector-itest";
    private final static String containerName = "CosmosTransferProcessStoreTest";
    private static CosmosContainer container;
    private static CosmosDatabase database;
    private CosmosTransferProcessStore store;

    @BeforeAll
    static void prepareCosmosClient() {

        var key = propOrEnv("COSMOS_KEY", () -> {
            throw new RuntimeException("No COSMOS_KEY was found!");
        });
        var client = new CosmosClientBuilder()
                .key(key)
                .preferredRegions(Collections.singletonList("westeurope"))
                .consistencyLevel(ConsistencyLevel.SESSION)
                .endpoint("https://" + accountName + ".documents.azure.com:443/")
                .buildClient();

        final CosmosDatabaseResponse response = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(response.getProperties().getId());


    }

    @BeforeEach
    void setUp() {
        final CosmosContainerResponse containerIfNotExists = database.createContainerIfNotExists(containerName, "/partitionKey");
        container = database.getContainer(containerIfNotExists.getProperties().getId());
        store = new CosmosTransferProcessStore(container);
    }

    @Test
    void create() {
        final String id = UUID.randomUUID().toString();
        TransferProcess transferProcess = createTransferProcess(id);
        store.create(transferProcess);

        final CosmosPagedIterable<TransferProcessDocument> documents = container.readAllItems(new PartitionKey(id), TransferProcessDocument.class);
        assertThat(documents).hasSize(1);
        assertThat(documents).allSatisfy(doc -> {
            assertThat(doc.getWrappedInstance()).isNotNull();
            assertThat(doc.getWrappedInstance().getId()).isEqualTo(id);
            assertThat(doc.getPartitionKey()).isEqualTo(id);
        });
    }

    @Test
    void create_processWithSameIdExists_throwsException() {
        final String id = UUID.randomUUID().toString();
        TransferProcess transferProcess = createTransferProcess(id);
        store.create(transferProcess);

        var secondProcess = createTransferProcess(id);

        assertThatThrownBy(() -> store.create(secondProcess)).isInstanceOf(DagxException.class).hasRootCauseInstanceOf(ConflictException.class);
    }

    @Test
    void nextForState() throws InterruptedException {

        String id1 = UUID.randomUUID().toString();
        var tp = createTransferProcess(id1, TransferProcessStates.PROVISIONED);

        String id2 = UUID.randomUUID().toString();
        var tp2 = createTransferProcess(id2, TransferProcessStates.PROVISIONED);

        Thread.sleep(500); //make sure the third process is the youngest - should not get fetched
        String id3 = UUID.randomUUID().toString();
        var tp3 = createTransferProcess(id3, TransferProcessStates.PROVISIONED);

        store.create(tp);
        store.create(tp2);
        store.create(tp3);


        final List<TransferProcess> processes = store.nextForState(TransferProcessStates.PROVISIONED.code(), 2);

        assertThat(processes).hasSize(2);
        //lets make sure the list only contains the 2 oldest ones
        assertThat(processes).allMatch(p -> Arrays.asList(id1, id2).contains(p.getId()))
                .noneMatch(p -> p.getId().equals(id3));
    }

    @Test
    void nextForState_noneInDesiredState() {

        String id1 = UUID.randomUUID().toString();
        var tp = createTransferProcess(id1, TransferProcessStates.PROVISIONED);

        String id2 = UUID.randomUUID().toString();
        var tp2 = createTransferProcess(id2, TransferProcessStates.PROVISIONED);

        String id3 = UUID.randomUUID().toString();
        var tp3 = createTransferProcess(id3, TransferProcessStates.PROVISIONED);

        store.create(tp);
        store.create(tp2);
        store.create(tp3);

        final List<TransferProcess> processes = store.nextForState(TransferProcessStates.IN_PROGRESS.code(), 5);

        assertThat(processes).isEmpty();
    }

    @Test
    void nextForState_batchSizeLimits() {
        for (var i = 0; i < 5; i++) {
            var tp = createTransferProcess("process_" + i, TransferProcessStates.IN_PROGRESS);
            store.create(tp);
        }

        var processes = store.nextForState(TransferProcessStates.IN_PROGRESS.code(), 3);
        assertThat(processes).hasSize(3);
    }


    @AfterEach
    void teardown() {
        final CosmosContainerResponse delete = container.delete();
        assertThat(delete.getStatusCode()).isGreaterThanOrEqualTo(200).isLessThan(300);
    }
}