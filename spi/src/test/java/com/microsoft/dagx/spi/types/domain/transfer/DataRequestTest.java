/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

package com.microsoft.dagx.spi.types.domain.transfer;

import com.microsoft.dagx.spi.types.domain.metadata.DataEntry;
import com.microsoft.dagx.spi.types.domain.metadata.GenericDataCatalogEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 *
 */
class DataRequestTest {

    @Test
    void verifyNoDestination() {
        String id = UUID.randomUUID().toString();
        DataEntry entry = DataEntry.Builder.newInstance().catalogEntry(GenericDataCatalogEntry.Builder.newInstance().build()).build();

        Assertions.assertThrows(IllegalArgumentException.class, () -> DataRequest.Builder.newInstance().id(id).dataEntry(entry).build());
    }

}
