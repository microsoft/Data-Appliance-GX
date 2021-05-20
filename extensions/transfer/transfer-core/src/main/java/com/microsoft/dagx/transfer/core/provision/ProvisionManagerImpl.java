/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

package com.microsoft.dagx.transfer.core.provision;

import com.microsoft.dagx.spi.DagxException;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.transfer.provision.ProvisionManager;
import com.microsoft.dagx.spi.transfer.provision.Provisioner;
import com.microsoft.dagx.spi.transfer.store.TransferProcessStore;
import com.microsoft.dagx.spi.types.TypeManager;
import com.microsoft.dagx.spi.types.domain.transfer.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Default provision manager. Invoke {@link #start(TransferProcessStore)} to initialize an instance.
 */
public class ProvisionManagerImpl implements ProvisionManager {
    private final Vault vault;
    private final TypeManager typeManager;
    private final Monitor monitor;
    private final List<Provisioner<?, ?>> provisioners = new ArrayList<>();
    private TransferProcessStore processStore;

    public ProvisionManagerImpl(Vault vault, TypeManager typeManager, Monitor monitor) {
        this.vault = vault;
        this.typeManager = typeManager;
        this.monitor = monitor;
    }

    public void start(TransferProcessStore processStore) {
        this.processStore = processStore;
        var context = new ProvisionContextImpl(this.processStore, this::onResource, this::onDestinationResource);
        provisioners.forEach(provisioner -> provisioner.initialize(context));
    }

    @Override
    public <RD extends ResourceDefinition, PR extends ProvisionedResource> void register(Provisioner<RD, PR> provisioner) {
        provisioners.add(provisioner);
    }

    @Override
    public void provision(TransferProcess process) {
        if (process.getResourceManifest().getDefinitions().isEmpty()) {
            // no resources to provision, advance state
            process.transitionProvisioned();
            processStore.update(process);
        }
        for (ResourceDefinition definition : process.getResourceManifest().getDefinitions()) {
            Provisioner<ResourceDefinition, ?> chosenProvisioner = getProvisioner(definition);
            var status = chosenProvisioner.provision(definition);
        }
    }

    @Override
    public void deprovision(TransferProcess process) {
        for (ProvisionedResource definition : process.getProvisionedResourceSet().getResources()) {
            Provisioner<?, ProvisionedResource> chosenProvisioner = getProvisioner(definition);
            chosenProvisioner.deprovision(definition);
        }
    }

    void onDestinationResource(ProvisionedDataDestinationResource destinationResource, DestinationSecretToken secretToken) {
        var processId = destinationResource.getTransferProcessId();
        var transferProcess = processStore.find(processId);
        if (transferProcess == null) {
            processNotFound(destinationResource);
            return;
        }


        transferProcess.getDataRequest().updateDestination(destinationResource.createDataDestination());

        if (secretToken != null) {
            String keyName = destinationResource.getResourceName();
            vault.storeSecret(keyName, typeManager.writeValueAsString(secretToken));
            transferProcess.getDataRequest().getDataDestination().setKeyName(keyName);

        }

        updateProcessWithProvisionedResource(destinationResource, transferProcess);
    }

    void onResource(ProvisionedResource provisionedResource) {
        var processId = provisionedResource.getTransferProcessId();
        var transferProcess = processStore.find(processId);
        if (transferProcess == null) {
            processNotFound(provisionedResource);
            return;
        }

        updateProcessWithProvisionedResource(provisionedResource, transferProcess);
    }

    private void updateProcessWithProvisionedResource(ProvisionedResource provisionedResource, TransferProcess transferProcess) {
        transferProcess.addProvisionedResource(provisionedResource);

        if (provisionedResource.isError()) {
            var processId = transferProcess.getId();
            var resourceId = provisionedResource.getResourceDefinitionId();
            monitor.severe(format("Error provisioning resource %s for process %s: %s", resourceId, processId, provisionedResource.getErrorMessage()));
            processStore.update(transferProcess);
            return;
        }

        if (TransferProcessStates.ERROR.code() != transferProcess.getState() && transferProcess.provisioningComplete()) {
            // TODO If all resources provisioned, delete scratch data
            transferProcess.transitionProvisioned();
        }
        processStore.update(transferProcess);
    }

    private void processNotFound(ProvisionedResource provisionedResource) {
        var resourceId = provisionedResource.getResourceDefinitionId();
        var processId = provisionedResource.getTransferProcessId();
        monitor.severe(format("Error received when provisioning resource %s Process id not found for: %s", resourceId, processId));
    }

    @NotNull
    private Provisioner<ResourceDefinition, ?> getProvisioner(ResourceDefinition definition) {
        Provisioner<ResourceDefinition, ?> provisioner = null;
        for (Provisioner<?, ?> candidate : provisioners) {
            if (candidate.canProvision(definition)) {
                //noinspection unchecked
                provisioner = (Provisioner<ResourceDefinition, ?>) candidate;
                break;
            }
        }
        if (provisioner == null) {
            throw new DagxException("Unknown provision type" + definition.getClass().getName());
        }
        return provisioner;
    }

    @NotNull
    private Provisioner<?, ProvisionedResource> getProvisioner(ProvisionedResource provisionedResource) {
        Provisioner<?, ProvisionedResource> provisioner = null;
        for (Provisioner<?, ?> candidate : provisioners) {
            if (candidate.canDeprovision(provisionedResource)) {
                //noinspection unchecked
                provisioner = (Provisioner<?, ProvisionedResource>) candidate;
                break;
            }
        }
        if (provisioner == null) {
            throw new DagxException("Unknown provision type" + provisionedResource.getClass().getName());
        }
        return provisioner;
    }


}
