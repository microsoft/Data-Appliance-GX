package com.microsoft.dagx.catalog.atlas.dataseed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.dagx.catalog.atlas.metadata.TypeAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class AtlasTypeDefDto {
    @JsonProperty
    private String typeName;
    @JsonProperty
    private Set<String> superTypeNames;
    @JsonProperty
    private List<TypeAttribute> attributes = new ArrayList<>();

    public String getTypeKeyName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Set<String> getSuperTypeNames() {
        return superTypeNames;
    }

    public void setSuperTypeNames(Set<String> superTypeNames) {
        this.superTypeNames = superTypeNames;
    }

    public List<TypeAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<TypeAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "AtlasTypeDefDto{" +
                "typeName='" + typeName + '\'' +
                ", superTypeNames=" + superTypeNames +
                ", attributes=" + attributes +
                '}';
    }

}
