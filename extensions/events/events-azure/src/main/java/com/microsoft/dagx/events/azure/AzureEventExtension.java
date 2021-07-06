/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */
package com.microsoft.dagx.events.azure;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.microsoft.dagx.spi.metadata.MetadataObservable;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.security.Vault;
import com.microsoft.dagx.spi.system.ServiceExtension;
import com.microsoft.dagx.spi.system.ServiceExtensionContext;
import com.microsoft.dagx.spi.transfer.TransferProcessObservable;

import java.util.Objects;
import java.util.Set;

public class AzureEventExtension implements ServiceExtension {


    private static final String TOPIC_NAME_SETTING = "dagx.events.topic.name";
    private static final String TOPIC_ENDPOINT_SETTING = "dagx.events.topic.endpoint";
    private Monitor monitor;

    @Override
    public Set<String> requires() {
        return Set.of("dagx:transfer-process-observable");
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();

        monitor.info("AzureEventsExtension: create event grid appender");
        registerListeners(context);

        monitor.info("Initialized Azure Events Extension");
    }


    @Override
    public void start() {
        monitor.info("Started Azure Events Extension");
    }

    @Override
    public void shutdown() {
        monitor.info("Shutdown Azure Events Extension");
    }

    private void registerListeners(ServiceExtensionContext context) {

        var vault = context.getService(Vault.class);
        var endpoint = context.getSetting(TOPIC_ENDPOINT_SETTING, null);
        var topicName = context.getSetting(TOPIC_NAME_SETTING, null);
        var publisherClient = new EventGridPublisherClientBuilder()
                .credential(new AzureKeyCredential(Objects.requireNonNull(vault.resolveSecret(topicName), "Did not find secret in vault: " + endpoint)))
                .endpoint(endpoint)
                .buildEventGridEventPublisherAsyncClient();

        final EventGridPublisher publisher = new EventGridPublisher(monitor, publisherClient);

        var processObservable = context.getService(TransferProcessObservable.class, true);
        if (processObservable != null) {
            processObservable.registerListener(publisher);
        }

        var metadataObservable = context.getService(MetadataObservable.class, true);
        if (metadataObservable != null) {
            metadataObservable.registerListener(publisher);
        }


    }
}
