package com.microsoft.dagx.catalog.atlas.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class AtlasCustomTypeAttribute {
    /**
     * Pre-defined list of attributes that are required for a transfer that originates from Azure Blob Store
     */
    public static final List<AtlasCustomTypeAttribute> AZURE_BLOB_ATTRS = new ArrayList<>() {{
        add(new AtlasCustomTypeAttribute("account", "string", true));
        add(new AtlasCustomTypeAttribute("blobname", "string", true));
        add(new AtlasCustomTypeAttribute("container", "string", true));
        add(new AtlasCustomTypeAttribute("type", "string", true));
        add(new AtlasCustomTypeAttribute("keyName", "string", true));
    }};
    @JsonProperty
    private String name;
    @JsonProperty
    private String type;
    @JsonProperty
    private boolean required;

    public AtlasCustomTypeAttribute(String name, String type, boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public AtlasCustomTypeAttribute(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return "AttributeDefDto{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", required=" + required +
                '}';
    }
}
