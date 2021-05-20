/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

package com.microsoft.dagx.transfer.provision.azure;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.transfer.provision.ProvisionContext;
import com.microsoft.dagx.spi.transfer.provision.Provisioner;
import com.microsoft.dagx.spi.transfer.response.ResponseStatus;
import com.microsoft.dagx.spi.types.domain.transfer.ProvisionedResource;
import com.microsoft.dagx.spi.types.domain.transfer.ResourceDefinition;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import java.time.OffsetDateTime;

/**
 *
 */
public class ObjectStorageProvisioner implements Provisioner<ObjectStorageResourceDefinition, ObjectContainerProvisionedResource> {
    private final RetryPolicy<Object> retryPolicy;
    private final Monitor monitor;
    private final Vault vault;
    private ProvisionContext context;

    public ObjectStorageProvisioner(RetryPolicy<Object> retryPolicy, Monitor monitor, Vault vault) {
        this.retryPolicy = retryPolicy;
        this.monitor = monitor;
        this.vault = vault;
    }

    @Override
    public void initialize(ProvisionContext context) {
        this.context = context;
    }

    @Override
    public boolean canProvision(ResourceDefinition resourceDefinition) {
        return resourceDefinition instanceof ObjectStorageResourceDefinition;
    }

    @Override
    public boolean canDeprovision(ProvisionedResource resourceDefinition) {
        return resourceDefinition instanceof ObjectContainerProvisionedResource;
    }

    @Override
    public ResponseStatus provision(ObjectStorageResourceDefinition resourceDefinition) {
        String containerName = resourceDefinition.getContainerName();
        String accountName = resourceDefinition.getAccountName();

        monitor.info("Azure Storage Container request submitted: " + containerName);

        final String key = vault.resolveSecret(accountName + "-key1");

        if (key == null) {
            monitor.severe("No Object Storage credential found in vault!");
            return ResponseStatus.FATAL_ERROR;
        }
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, key);


        BlobServiceClient blobContainerClient = new BlobServiceClientBuilder()
                .credential(credential)
                .endpoint("https://" + accountName + ".blob.core.windows.net")
                .buildClient();

        //create the container
        var containerClient = blobContainerClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            Failsafe.with(retryPolicy).run(containerClient::create);
            monitor.debug("ObjectStorageProvisioner: created a new container " + containerName);
        } else {
            monitor.debug("ObjectStorageProvisioner: re-use existing container " + containerName);
        }

        BlobContainerSasPermission permissions = BlobContainerSasPermission.parse("w");
        OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(1);
        BlobServiceSasSignatureValues vals = new BlobServiceSasSignatureValues(expiryTime, permissions);
        monitor.debug("ObjectStorageProvisioner: obtained temporary SAS token (write-only)");

        // the "?" is actually important, otherwise downstream transfer tools like nifi might complain
        String writeOnlySas = "?" + Failsafe.with(retryPolicy).get(() -> containerClient.generateSas(vals));

        var resource = ObjectContainerProvisionedResource.Builder.newInstance()
                .id(containerName)
                .accountName(accountName)
                .containerName(containerName)
                .resourceDefinitionId(resourceDefinition.getId())
                .transferProcessId(resourceDefinition.getTransferProcessId()).build();

        var secretToken = new AzureSasToken(writeOnlySas, expiryTime.toInstant().toEpochMilli());

        context.callback(resource, secretToken);


        ////// TODO: REMOVE!!!
//        blobContainerClient.deleteBlobContainer(containerName);
        ////// END  DEBUG!!!

        return ResponseStatus.OK;
    }

    @Override
    public ResponseStatus deprovision(ObjectContainerProvisionedResource provisionedResource) {
        return ResponseStatus.OK;
    }
}
