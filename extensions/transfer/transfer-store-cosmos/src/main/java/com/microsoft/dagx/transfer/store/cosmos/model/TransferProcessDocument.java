/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.transfer.store.cosmos.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcess;

@JsonTypeName("dagx:transferprocessdocument")
public class TransferProcessDocument {

    private String externalId;
    @JsonUnwrapped
    private TransferProcess wrappedInstance;

    @JsonProperty
    private String partitionKey;

    protected TransferProcessDocument() {
        //Jackson does not yet support the combination of @JsonUnwrapped and a @JsonProperty annotation in a constructor
    }

    private TransferProcessDocument(TransferProcess wrappedInstance, String partitionKey, String externalId) {
        this.wrappedInstance = wrappedInstance;
        this.partitionKey = partitionKey;
        this.externalId = externalId;
    }

    public static TransferProcessDocument from(TransferProcess process, String externalId) {
        return new TransferProcessDocument(process, process.getId(), externalId);
    }


    public String getPartitionKey() {
        return partitionKey;
    }

    public TransferProcess getWrappedInstance() {
        return wrappedInstance;
    }

    public String getExternalId() {
        return externalId;
    }
}
