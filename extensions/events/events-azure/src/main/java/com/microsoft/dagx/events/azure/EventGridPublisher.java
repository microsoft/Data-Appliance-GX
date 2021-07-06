/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.events.azure;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.microsoft.dagx.spi.metadata.MetadataListener;
import com.microsoft.dagx.spi.monitor.Monitor;
import com.microsoft.dagx.spi.transfer.TransferProcessListener;
import com.microsoft.dagx.spi.types.domain.transfer.TransferProcess;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

class EventGridPublisher implements TransferProcessListener, MetadataListener {

    private final Monitor monitor;
    private final EventGridPublisherAsyncClient<EventGridEvent> client;
    private final String eventTypeTransferprocess = "dagx/transfer/transferprocess";
    private final String eventTypeMetadata = "dagx/metadata/store";

    public EventGridPublisher(Monitor monitor, EventGridPublisherAsyncClient<EventGridEvent> client) {
        this.monitor = monitor;
        this.client = client;
    }

    @Override
    public void created(TransferProcess process) {
        if (process.getType() == TransferProcess.Type.CLIENT) {
            sendEvent("createdClient", eventTypeTransferprocess, process).subscribe(new DefaultSubscriber<>("Transfer process created"));
        } else {
            sendEvent("createdProvider", eventTypeTransferprocess, process).subscribe(new DefaultSubscriber<>("Transfer process created"));
        }
    }

    @Override
    public void completed(TransferProcess process) {
        sendEvent("completed", eventTypeTransferprocess, process).subscribe(new DefaultSubscriber<>("Transfer process completed"));
    }


    @Override
    public void deprovisioned(TransferProcess process) {
        sendEvent("deprovisioned", eventTypeTransferprocess, process).subscribe(new DefaultSubscriber<>("Transfer process resources deprovisioned"));

    }

    @Override
    public void ended(TransferProcess process) {
        sendEvent("ended", eventTypeTransferprocess, process).subscribe(new DefaultSubscriber<>("Transfer process ended"));

    }

    @Override
    public void error(TransferProcess process) {
        sendEvent("error", eventTypeTransferprocess, process).subscribe(new DefaultSubscriber<>("Transfer process errored!"));

    }

    @Override
    public void querySubmitted() {
        sendEvent("querySubmitted", eventTypeMetadata, null).subscribe(new DefaultSubscriber<>("query submitted"));
    }

    @Override
    public void searchInitiated() {
        sendEvent("searchInitiated", eventTypeMetadata, null).subscribe(new DefaultSubscriber<>("search initiated"));
    }

    @Override
    public void metadataItemAdded() {
        sendEvent("itemAdded", eventTypeMetadata, null).subscribe(new DefaultSubscriber<>("AzureEventGrid: metadata item added"));
    }

    @Override
    public void metadataItemUpdated() {
        sendEvent("itemUpdated", eventTypeMetadata, null).subscribe(new DefaultSubscriber<>("metadata item updated"));
    }

    private Mono<Void> sendEvent(String what, String where, Object payload) {
        final BinaryData data = BinaryData.fromObject(payload);
        var evt = new EventGridEvent(what, where, data, "0.1");
        return client.sendEvent(evt);
    }

    private class DefaultSubscriber<T> extends BaseSubscriber<T> {

        private final String message;

        DefaultSubscriber(String message) {
            this.message = message;
        }

        @Override
        protected void hookOnComplete() {
            monitor.info("AzureEventGrid: " + message);
        }

        @Override
        protected void hookOnError(@NotNull Throwable throwable) {
            monitor.severe("Error during event publishing", throwable);
        }
    }
}
