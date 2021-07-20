/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.spi.types.domain.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

@JsonDeserialize(builder = TransferType.Builder.class)
public class TransferType {
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private String contentType = DEFAULT_CONTENT_TYPE;
    private boolean isFinite = true;

    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("isFinite")
    public boolean isFinite() {
        return isFinite;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String contentType = DEFAULT_CONTENT_TYPE;
        private boolean isFinite = true;

        private Builder() {
        }

        @JsonCreator
        public static Builder transferType() {
            return new Builder();
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder isFinite(boolean isFinite) {
            this.isFinite = isFinite;
            return this;
        }

        public TransferType build() {
            TransferType transferType = new TransferType();
            transferType.contentType = Objects.requireNonNull(contentType, "Content type cannot be null!");
            transferType.isFinite = isFinite;
            return transferType;
        }
    }
}
