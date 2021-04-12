package com.microsoft.dagx.transfer.nifi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.microsoft.dagx.spi.DagxException;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.transfer.flow.DataFlowInitiateResponse;
import com.microsoft.dagx.spi.transfer.response.ResponseStatus;
import com.microsoft.dagx.spi.types.TypeManager;
import com.microsoft.dagx.spi.types.domain.metadata.DataEntry;
import com.microsoft.dagx.spi.types.domain.metadata.DataEntryExtensions;
import com.microsoft.dagx.spi.types.domain.metadata.GenericDataEntryExtensions;
import com.microsoft.dagx.spi.types.domain.transfer.DataRequest;
import com.microsoft.dagx.transfer.nifi.api.NifiApiClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

class NifiDataFlowControllerTest {

    private NifiDataFlowController controller;
    private Vault vault;
    private static TypeManager typeManager;
    private String storageAccount;
    private String containerName;
    private static String storageAccountKey;
    private static NifiApiClient client;

    @BeforeAll
    public static void prepare() throws Exception {

        //todo: spin up dockerized nifi
        typeManager = new TypeManager();
        typeManager.getMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var f = Thread.currentThread().getContextClassLoader().getResource("TwoClouds.xml");
        var file = new File(Objects.requireNonNull(f).toURI());
        client = new NifiApiClient("http://localhost:8080", typeManager);
        String processGroup = "root";
        try {
            var templateId = client.uploadTemplate(processGroup, file);
            client.instantiateTemplate(templateId);
        } catch (DagxException ignored) {
        } finally {
            var controllerService = client.getControllerServices(processGroup).get(0);
            var controllerServiceId = controllerService.id;
            var version = controllerService.revision.version;
            client.startControllerService(controllerServiceId, version);
            client.startProcessGroup(processGroup);
        }
        //todo: spin up Azure Container Storage
        storageAccountKey = "p8xcDBKin2DzgEIpS0vOFGNxjeLyVsKcpon/fRCI/Qailuw7Jlp2WxM2dk/UB6RX9SKxR7HBMLLoUQpUdT/VCw==";
    }

    @BeforeEach
    void setUp() {

        storageAccount = "nififlowtest";
        containerName = "nifi-transfer";

        Monitor monitor = new Monitor() {
        };
        NifiTransferManagerConfiguration config = NifiTransferManagerConfiguration.Builder.newInstance().url("http://localhost:8888")
                .build();
        typeManager.registerTypes(DataRequest.class);

        vault = createMock(Vault.class);
        expect(vault.resolveSecret(NifiDataFlowController.NIFI_CREDENTIALS)).andReturn("Basic dGVzdHVzZXJAZ2FpYXguY29tOmdYcHdkIzIwMiE=");
        replay(vault);
        controller = new NifiDataFlowController(config, typeManager, monitor, vault);
    }

    @Test
    void initiateFlow() {
        var initialSize = client.getBulletinBoard().bulletins.size();
        var ext = GenericDataEntryExtensions.Builder.newInstance().property("type", "AzureStorage")
                .property("account", storageAccount)
                .property("container", containerName)
                .property("blobname", "sportster48.jpg")
                .property("key", storageAccountKey)
                .build();

        String id = UUID.randomUUID().toString();
        DataEntry<DataEntryExtensions> entry = DataEntry.Builder.newInstance().id(id).extensions(ext).build();

        DataRequest dataRequest = DataRequest.Builder.newInstance()
                .id(id)
                .dataEntry(entry)
                .dataTarget(AzureStorageTarget.Builder.newInstance()
                        .account(storageAccount)
                        .container(containerName)
                        .blobName("bike_very_new.jpg")
                        .key(storageAccountKey)
                        .build())
                .build();

        //act
        DataFlowInitiateResponse response = controller.initiateFlow(dataRequest);

        //assert
        assertEquals(ResponseStatus.OK, response.getStatus());
        //todo: verify that the "bike_vey_new.jpg" is actually in the storage
    }

    @Test
    void initiateFlow_sourceNotFound() {
        var bulletinSize = client.getBulletinBoard().bulletins.size();

        var ext = GenericDataEntryExtensions.Builder.newInstance().property("type", "AzureStorage")
                .property("account", storageAccount)
                .property("container", containerName)
                .property("blobname", "notexist.png")
                .property("key", storageAccountKey)
                .build();

        String id = UUID.randomUUID().toString();
        DataEntry<DataEntryExtensions> entry = DataEntry.Builder.newInstance().id(id).extensions(ext).build();

        DataRequest dataRequest = DataRequest.Builder.newInstance()
                .id(id)
                .dataEntry(entry)
                .dataTarget(AzureStorageTarget.Builder.newInstance()
                        .account(storageAccount)
                        .container(containerName)
                        .blobName("will_not_succeed.jpg")
                        .key(storageAccountKey)
                        .build())
                .build();

        //act
        DataFlowInitiateResponse response = controller.initiateFlow(dataRequest);

        //assert
        assertEquals(ResponseStatus.OK, response.getStatus());

        // will fail the test if bulletinSize has not increased by 1 within 5 seconds
        assertTimeoutPreemptively(Duration.ofMillis(5000), () -> {
            while (bulletinSize + 1 != client.getBulletinBoard().bulletins.size()) {
                Thread.sleep(10);
            }
        });

        assertEquals(bulletinSize + 1, client.getBulletinBoard().bulletins.size());
        //todo: verify that the "bike_vey_new.jpg" is actually in the storage
    }

    @Test
    void initiateFlow_noDestinationDefined() {
        String id = UUID.randomUUID().toString();
        DataEntry<DataEntryExtensions> entry = DataEntry.Builder.newInstance().extensions(GenericDataEntryExtensions.Builder.newInstance().build()).build();

        DataRequest dataRequest = DataRequest.Builder.newInstance()
                .id(id)
                .dataEntry(entry)
                .build();

        var e = assertThrows(DagxException.class, () -> controller.initiateFlow(dataRequest));
        assertEquals(IllegalArgumentException.class, e.getCause().getClass());
    }

    @Test
    void initiateFlow_noCredsFoundInVault() {
        String id = UUID.randomUUID().toString();
        DataEntry<DataEntryExtensions> entry = DataEntry.Builder.newInstance().extensions(GenericDataEntryExtensions.Builder.newInstance().build()).build();

        DataRequest dataRequest = DataRequest.Builder.newInstance()
                .id(id)
                .dataTarget(() -> "TestType")
                .dataEntry(entry)
                .build();

        reset(vault);
        expect(vault.resolveSecret(NifiDataFlowController.NIFI_CREDENTIALS)).andReturn(null);
        replay(vault);

        assertThrows(DagxException.class, () -> controller.initiateFlow(dataRequest), "No NiFi credentials found in Vault!");
    }

    @AfterAll
    public static void winddown() {
        //todo: kill Azure Container Storage

        //todo: kill dockerized nifi
    }
}
