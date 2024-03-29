package com.microsoft.dagx.transfer.demo.protocols.spi.object;

import com.microsoft.dagx.transfer.demo.protocols.common.DataDestination;

import java.util.concurrent.CompletableFuture;

/**
 * Implements in-memory, simulated object storage.
 *
 * An {@link ObjectStorageObserver} can be registered using {@link #register(ObjectStorageObserver)} to record operations or introduce failures for testing.
 */
public interface ObjectStorage {

    /**
     * Registers an observer
     */
    void register(ObjectStorageObserver observer);

    /**
     * Asynchronously provisions an object container, returning its destination information.
     *
     * @param containerName the container name
     */
    CompletableFuture<DataDestination> provision(String containerName);

    /**
     * De-provisions an object container
     *
     * @param containerName the container name
     */
    boolean deprovision(String containerName);

    /**
     * Stores an object.
     *
     * @param containerName the container name
     * @param objectKey the key associated with the object
     * @param token the temporary access token to store the object
     * @param data the object contents
     * @return if the object was successfully stored
     */
    boolean store(String containerName, String objectKey, String token, byte[] data);


}
