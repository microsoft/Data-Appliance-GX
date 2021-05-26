/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

package com.microsoft.dagx.catalog.atlas.metadata;

import com.microsoft.dagx.spi.types.domain.metadata.DataCatalog;
import com.microsoft.dagx.spi.types.domain.transfer.DataAddress;

import java.util.Map;
import java.util.stream.Collectors;

public class AtlasDataCatalog implements DataCatalog {

    private static final String ATLAS_PROPERTY_KEYNAME = "keyName";
    private static final String ATLAS_PROPERTY_TYPE = "type";
    private final AtlasApi atlasApi;

    public AtlasDataCatalog(AtlasApi atlasApi) {
        this.atlasApi = atlasApi;
    }

    @Override
    public DataAddress getPropertiesForEntity(String id) {
        var entity = atlasApi.getEntityById(id);

        if (entity != null) {
            return DataAddress.Builder.newInstance()
                    .keyName(entity.getEntity().getAttribute(ATLAS_PROPERTY_KEYNAME).toString())
                    .type(entity.getEntity().getAttribute(ATLAS_PROPERTY_TYPE).toString())
                    .properties(convert(entity.getEntity().getAttributes()))
                    .build();
        }

        return null;
    }

    private Map<String, String> convert(Map<String, Object> attributes) {
        return attributes.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().toString()));
    }
}
