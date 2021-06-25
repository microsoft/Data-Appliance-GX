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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@JsonTypeName("dagx:transferprocessdocument")
public class TransferProcessDocument {

    private String externalId;
    @JsonUnwrapped
    private TransferProcess wrappedInstance;

    @JsonProperty
    private String partitionKey;

    @JsonProperty
    private Lease lease;

    protected TransferProcessDocument() {
        //Jackson does not yet support the combination of @JsonUnwrapped and a @JsonProperty annotation in a constructor
    }

    private TransferProcessDocument(TransferProcess wrappedInstance, String partitionKey, String externalId) {
        this.wrappedInstance = wrappedInstance;
        this.partitionKey = partitionKey;
        this.externalId = externalId;
    }

    public static TransferProcessDocument from(TransferProcess process, String partitionKey, String externalId) {
        return new TransferProcessDocument(process, partitionKey, externalId);
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

    public Lease getLease() {
        return lease;
    }

    public void acquireLease(String connectorId) {
        if (lease == null || lease.getLeasedBy().equals(connectorId)) {
            lease = new Lease(connectorId);
        } else {
            var startDate = Instant.ofEpochMilli(lease.getLeasedAt());
            var endDate = Instant.ofEpochMilli(lease.getLeasedUntil());
            throw new IllegalStateException("This document is leased by " + lease.getLeasedBy() + "on " + startDate.toString() + " and cannot be leased again until " + endDate.toString() + "!");
        }
    }

    public static class Lease {
        @JsonProperty
        private final String leasedBy;
        @JsonProperty
        private final long leasedAt;
        @JsonProperty
        private final long leasedUntil;

        private Lease(String leasedBy) {
            this(leasedBy, Instant.now().toEpochMilli(), Instant.now().plus(60, ChronoUnit.SECONDS).toEpochMilli());
        }

        public Lease(@JsonProperty("leasedBy") String leasedBy, @JsonProperty("leasedAt") long leasedAt, @JsonProperty("leasedUntil") long leasedUntil) {
            this.leasedBy = leasedBy;
            this.leasedAt = leasedAt;
            this.leasedUntil = leasedUntil;
        }

        public String getLeasedBy() {
            return leasedBy;
        }

        public long getLeasedAt() {
            return leasedAt;
        }

        public long getLeasedUntil() {
            return leasedUntil;
        }
    }
}
