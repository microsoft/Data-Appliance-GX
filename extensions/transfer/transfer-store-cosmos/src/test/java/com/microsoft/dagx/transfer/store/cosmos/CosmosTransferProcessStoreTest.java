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
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.microsoft.dagx.spi.DagxException;
import com.microsoft.dagx.spi.types.TypeManager;
import com.microsoft.dagx.spi.types.domain.metadata.DataCatalogEntry;
import com.microsoft.dagx.spi.types.domain.metadata.DataEntry;
import com.microsoft.dagx.spi.types.domain.transfer.DataRequest;
import com.microsoft.dagx.spi.types.domain.transfer.ResourceManifest;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcess;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcessStates;
import com.microsoft.dagx.transfer.store.cosmos.model.TransferProcessDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.*;

import static com.microsoft.dagx.common.ConfigurationFunctions.propOrEnv;
import static com.microsoft.dagx.transfer.store.cosmos.TestHelper.createTransferProcess;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class CosmosTransferProcessStoreTest {

    private final static String accountName = "cosmos-itest";
    private final static String databaseName = "connector-itest";
    private final static String containerName = "CosmosTransferProcessStoreTest";
    private static CosmosContainer container;
    private static CosmosDatabase database;
    private CosmosTransferProcessStore store;
    private TypeManager typeManager;

    @BeforeAll
    static void prepareCosmosClient() {

        var key = propOrEnv("COSMOS_KEY", null);
        assertThat(key).describedAs("COSMOS_KEY cannot be null!").isNotNull();
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
        typeManager = new TypeManager();
        typeManager.registerTypes(TestHelper.DummyCatalogEntry.class, DataCatalogEntry.class, DataRequest.class, DataEntry.class);
        store = new CosmosTransferProcessStore(container, typeManager);
    }

    @Test
    void create() {
        final String id = UUID.randomUUID().toString();
        TransferProcess transferProcess = createTransferProcess(id);
        store.create(transferProcess);

        final CosmosPagedIterable<Object> documents = container.readAllItems(new PartitionKey(id), Object.class);
        assertThat(documents).hasSize(1);
        assertThat(documents).allSatisfy(obj -> {
            var doc = convert(obj);
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
        var tp = createTransferProcess(id1, TransferProcessStates.UNSAVED);

        String id2 = UUID.randomUUID().toString();
        var tp2 = createTransferProcess(id2, TransferProcessStates.UNSAVED);

        Thread.sleep(500); //make sure the third process is the youngest - should not get fetched
        String id3 = UUID.randomUUID().toString();
        var tp3 = createTransferProcess(id3, TransferProcessStates.UNSAVED);

        store.create(tp);
        store.create(tp2);
        store.create(tp3);


        final List<TransferProcess> processes = store.nextForState(TransferProcessStates.INITIAL.code(), 2);

        assertThat(processes).hasSize(2);
        //lets make sure the list only contains the 2 oldest ones
        assertThat(processes).allMatch(p -> Arrays.asList(id1, id2).contains(p.getId()))
                .noneMatch(p -> p.getId().equals(id3));
    }

    @Test
    void nextForState_noneInDesiredState() {

        String id1 = UUID.randomUUID().toString();
        var tp = createTransferProcess(id1, TransferProcessStates.UNSAVED);

        String id2 = UUID.randomUUID().toString();
        var tp2 = createTransferProcess(id2, TransferProcessStates.UNSAVED);

        String id3 = UUID.randomUUID().toString();
        var tp3 = createTransferProcess(id3, TransferProcessStates.UNSAVED);

        store.create(tp);
        store.create(tp2);
        store.create(tp3);

        final List<TransferProcess> processes = store.nextForState(TransferProcessStates.IN_PROGRESS.code(), 5);

        assertThat(processes).isEmpty();
    }

    @Test
    void nextForState_batchSizeLimits() {
        for (var i = 0; i < 5; i++) {
            var tp = createTransferProcess("process_" + i, TransferProcessStates.UNSAVED);
            store.create(tp);
        }

        var processes = store.nextForState(TransferProcessStates.INITIAL.code(), 3);
        assertThat(processes).hasSize(3);
    }

    @Test
    void find() {
        var tp = createTransferProcess("tp-id");
        store.create(tp);

        final TransferProcess dbProcess = store.find("tp-id");
        assertThat(dbProcess).isNotNull().isEqualTo(tp).usingRecursiveComparison();
    }

    @Test
    void find_notExist() {
        assertThat(store.find("not-exist")).isNull();
    }

    @Test
    void processIdForTransferId() {
        var tp = createTransferProcess("process-id");
        var transferId = tp.getDataRequest().getId();
        assertThat(transferId).isNotNull();

        store.create(tp);

        final String processId = store.processIdForTransferId(transferId);
        assertThat(processId).isEqualTo("process-id");
    }

    @Test
    void processIdForTransferId_notExist() {
        assertThat(store.processIdForTransferId("not-exist")).isNull();
    }

    @Test
    void update_exists_shouldUpdate() {
        var tp = createTransferProcess("process-id");

        store.create(tp);

        tp.transitionProvisioning(new ResourceManifest());
        store.update(tp);

        final CosmosItemResponse<Object> response = container.readItem(tp.getId(), new PartitionKey(tp.getId()), Object.class);

        final TransferProcessDocument stored = convert(response.getItem());
        assertThat(stored.getWrappedInstance()).isEqualTo(tp);
        assertThat(stored.getWrappedInstance().getState()).isEqualTo(TransferProcessStates.PROVISIONING.code());
    }

    @Test
    void update_notExist_shouldCreate() {
        var tp = createTransferProcess("process-id");

        tp.transitionInitial();
        tp.transitionProvisioning(new ResourceManifest());
        store.update(tp);

        final CosmosItemResponse<Object> response = container.readItem(tp.getId(), new PartitionKey(tp.getId()), Object.class);

        final TransferProcessDocument stored = convert(response.getItem());
        assertThat(stored.getWrappedInstance()).isEqualTo(tp);
        assertThat(stored.getWrappedInstance().getState()).isEqualTo(TransferProcessStates.PROVISIONING.code());
    }

    @Test
    void delete() {

        final String processId = "test-process-id";
        var tp = createTransferProcess(processId);

        store.create(tp);

        store.delete(processId);

        final CosmosPagedIterable<Object> objects = container.readAllItems(new PartitionKey(processId), Object.class);
        assertThat(objects).isEmpty();
    }

    @Test
    void delete_notExist() {
        store.delete("not-exist");
        //no exception should be raised
    }

    @Test
    void verifyAllNotImplemented() {
        assertThatThrownBy(() -> store.createData("pid", "key", new Object())).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> store.updateData("pid", "key", new Object())).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> store.deleteData("pid", "key")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> store.deleteData("pid", Set.of("k1", "k2"))).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> store.findData(String.class, "pid", "key")).isInstanceOf(UnsupportedOperationException.class);
    }

    private TransferProcessDocument convert(Object obj) {
        return typeManager.readValue(typeManager.writeValueAsBytes(obj), TransferProcessDocument.class);
    }

    @AfterEach
    void teardown() {
        final CosmosContainerResponse delete = container.delete();
        assertThat(delete.getStatusCode()).isGreaterThanOrEqualTo(200).isLessThan(300);
    }
}