/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

package com.microsoft.dagx.catalog.atlas.dataseed;

import com.microsoft.dagx.catalog.atlas.metadata.AtlasApi;
import com.microsoft.dagx.schema.SchemaRegistry;
import com.microsoft.dagx.spi.DagxException;
import org.apache.atlas.model.typedef.AtlasTypesDef;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AtlasDataSeeder {
    private final AtlasApi atlasApi;
    private final SchemaRegistry schemaRegistry;

    public AtlasDataSeeder(AtlasApi atlasApi, SchemaRegistry schemaRegistry) {

        this.atlasApi = atlasApi;
        this.schemaRegistry = schemaRegistry;
    }

    public String[] createClassifications() {
        var mapper = new ObjectMapper();
        try {
            Map<String, List<String>> classifications = mapper.readValue(getClass().getClassLoader().getResourceAsStream("classifications.json"), Map.class);
            String[] classificationNames = classifications.keySet().stream().flatMap(key -> classifications.get(key).stream()).toArray(String[]::new);
            atlasApi.createClassifications(classificationNames);
            return classificationNames;

        } catch (IOException e) {
            throw new DagxException(e);
        }

    }

    public List<AtlasTypesDef> createTypedefs() {
        List<AtlasTypesDef> entityTypes = new ArrayList<>();

        for (var schema : schemaRegistry.getSchemas()) {
            String sanitizedName = schema.getName().replace(":", "_");
            entityTypes.add(atlasApi.createCustomTypes(sanitizedName, Set.of("DataSet"), new ArrayList<>(schema.getAttributes())));
        }
        return entityTypes;
    }

    public List<String> createEntities() {
        try {
            ArrayList<String> entityGuids = new ArrayList<>();

            entityGuids.add(atlasApi.createEntity(AzureBlobFileEntityBuilder.ENTITY_TYPE_NAME, AzureBlobFileEntityBuilder.newInstance()
                    .withAccount("dagxblobstoreitest")
                    .withBlobname("testimage.jpg")
                    .withContainer("testcontainer")
                    .withKeyName("dagxblobstoreitest-key1")
                    .withDescription("this is a second entity")
                    .build()));
            return entityGuids;

        } catch (Exception e) {
            throw new DagxException(e);
        }
    }

    public void deleteEntities(List<String> guids) {
        if (guids != null && !guids.isEmpty()) {
            atlasApi.deleteEntities(guids);
        }
    }

    public void deleteClassifications(String... classificationNames) {
        if (classificationNames != null) {
            atlasApi.deleteClassification(classificationNames);
        }
    }

    public void deleteEntityTypes(List<AtlasTypesDef> entityTypes) {
        if (entityTypes != null && !entityTypes.isEmpty()) {
            atlasApi.deleteType(entityTypes);
        }
    }


}
