/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

package com.microsoft.dagx.transfer.provision.aws.s3;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.microsoft.dagx.schema.aws.S3BucketSchema;
import com.microsoft.dagx.spi.types.domain.transfer.DataAddress;
import com.microsoft.dagx.spi.types.domain.transfer.ProvisionedDataDestinationResource;


/**
 * A provisioned S3 bucket and credentials associated with a transfer process.
 */
@JsonDeserialize(builder = S3BucketProvisionedResource.Builder.class)
@JsonTypeName("dagx:s3bucketprovisionedresource")
public class S3BucketProvisionedResource extends ProvisionedDataDestinationResource {
    @JsonProperty
    private String region;

    @JsonProperty
    private String bucketName;

    private S3BucketProvisionedResource() {
    }

    public String getRegion() {
        return region;
    }

    public String getBucketName() {
        return bucketName;
    }

    @Override
    public DataAddress createDataDestination() {
        return DataAddress.Builder.newInstance().property(S3BucketSchema.REGION, region)
                .type(S3BucketSchema.TYPE)
                .property(S3BucketSchema.BUCKET_NAME, bucketName)
                .keyName("s3-temp-" + bucketName)
                .build();
    }

    @Override
    public String getResourceName() {
        return bucketName;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends ProvisionedDataDestinationResource.Builder<S3BucketProvisionedResource, Builder> {

        private Builder() {
            super(new S3BucketProvisionedResource());
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder();
        }

        public Builder region(String region) {
            provisionedResource.region = region;
            return this;
        }

        public Builder bucketName(String bucketName) {
            provisionedResource.bucketName = bucketName;
            return this;
        }
    }
}
