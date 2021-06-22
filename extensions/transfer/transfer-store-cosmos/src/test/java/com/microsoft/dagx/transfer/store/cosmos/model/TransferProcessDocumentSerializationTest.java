/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.transfer.store.cosmos.model;

import com.microsoft.dagx.spi.types.TypeManager;
import com.microsoft.dagx.spi.types.domain.transfer.DataAddress;
import com.microsoft.dagx.spi.types.domain.transfer.DataRequest;
import com.microsoft.dagx.spi.types.domain.transfer.ResourceManifest;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransferProcessDocumentSerializationTest {

    private TypeManager typeManager;

    @BeforeEach
    void setup() {
        typeManager = new TypeManager();
        typeManager.registerTypes(TransferProcessDocument.class, TransferProcess.class);
    }

    @Test
    void testSerialization() {
        var transferProcess = TransferProcess.Builder.newInstance()
                .id("test-process")
                .type(TransferProcess.Type.CLIENT)
                .dataRequest(createDataRequest())
                .resourceManifest(createManifest())
                .build();

        var document = TransferProcessDocument.from(transferProcess, "test-part-key");

        final String s = typeManager.writeValueAsString(document);

        assertThat(s).isNotNull();
        assertThat(s).contains("\"partitionKey\":\"test-part-key\"");
        assertThat(s).contains("\"id\":\"test-process\"");
        assertThat(s).contains("\"id\":\"test-process\"");
        assertThat(s).contains("\"type\":\"CLIENT\"");
        assertThat(s).contains("\"errorDetail\":null");
        assertThat(s).contains("\"destinationType\":\"Test Address Type\"");
        assertThat(s).contains("\"keyName\":\"Test Key Name\"");
        assertThat(s).contains("\"type\":\"Test Address Type\"");

    }

    @Test
    void testDeserialization() {
        var transferProcess = TransferProcess.Builder.newInstance()
                .id("test-process")
                .type(TransferProcess.Type.CLIENT)
                .dataRequest(createDataRequest())
                .resourceManifest(createManifest())
                .build();

        var document = TransferProcessDocument.from(transferProcess, "test-part-key");
        final String json = typeManager.writeValueAsString(document);

        Object transferProcessDeserialized = typeManager.readValue(json, TransferProcessDocument.class);
        assertThat(transferProcessDeserialized).usingRecursiveComparison().isEqualTo(document);

    }

    private ResourceManifest createManifest() {
        return ResourceManifest.Builder.newInstance()
                .build();
    }

    private DataRequest createDataRequest() {
        return DataRequest.Builder.newInstance()
                .dataDestination(DataAddress.Builder.newInstance()
                        .type("Test Address Type")
                        .keyName("Test Key Name")
                        .build())
                .processId("test-process-id")
                .build();
    }
}