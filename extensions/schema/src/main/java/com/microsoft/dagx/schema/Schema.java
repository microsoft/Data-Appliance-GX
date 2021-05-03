package com.microsoft.dagx.schema;

import java.util.AbstractSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public abstract class Schema {

    protected final AbstractSet<SchemaAttribute> attributes;

    protected Schema() {
        attributes = new LinkedHashSet<>();
        addAttributes();
    }

    protected abstract void addAttributes();

    /**
     * A string that uniquely identifies the schema. Can be scoped/namespaced
     */
    public abstract String getName();

    public AbstractSet<SchemaAttribute> getAttributes() {
        return attributes;
    }

    public AbstractSet<SchemaAttribute> getRequiredAttributes() {
        return getAttributes().stream().filter(SchemaAttribute::isRequired).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
