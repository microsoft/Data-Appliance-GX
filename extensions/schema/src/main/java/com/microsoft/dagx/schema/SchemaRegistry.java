package com.microsoft.dagx.schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SchemaRegistry {
    public static final String FEATURE = "schema-registry";

    private final Map<String, Schema> schemas;

    public SchemaRegistry() {
        schemas = new HashMap<>();
    }

    void register(Schema schema){
        schemas.put(schema.getName(), schema);
    }
    Schema getSchema(String identifier){
        return schemas.get(identifier);
    }

    boolean hasSchema(String identifier){
        return schemas.containsKey(identifier);
    }

    public Collection<Schema> getSchemas() {
        return schemas.values();
    }
}
