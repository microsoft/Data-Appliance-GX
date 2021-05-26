/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.catalog.atlas.dto;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

/**
 * class that captures details of a struct-type.
 */
@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtlasStructDef extends AtlasBaseTypeDef implements Serializable {
    private static final long serialVersionUID = 1L;

    // do not update this list contents directly - the list might be in the middle of iteration in another thread
    // to update list contents: 1) make a copy 2) update the copy 3) assign the copy to this member
    private List<AtlasAttributeDef> attributeDefs;


    protected AtlasStructDef(TypeCategory category, String name, String description, String typeVersion, List<AtlasAttributeDef> attributeDefs, Map<String, String> options) {
        this(category, name, description, typeVersion, attributeDefs, null, options);
    }

    protected AtlasStructDef(TypeCategory category, String name, String description, String typeVersion, List<AtlasAttributeDef> attributeDefs, String serviceType, Map<String, String> options) {
        super(category, name, description, typeVersion, serviceType, options);

        setAttributeDefs(attributeDefs);
    }

    public AtlasStructDef(AtlasStructDef other) {
        super(other);

        setAttributeDefs(other != null ? other.getAttributeDefs() : null);
    }

    private static boolean hasAttribute(List<AtlasAttributeDef> attributeDefs, String attrName) {
        return findAttribute(attributeDefs, attrName) != null;
    }

    public static AtlasAttributeDef findAttribute(Collection<AtlasAttributeDef> attributeDefs, String attrName) {
        AtlasAttributeDef ret = null;

        if (!attributeDefs.isEmpty()) {
            for (AtlasAttributeDef attributeDef : attributeDefs) {
                if (attributeDef.getName().equalsIgnoreCase(attrName)) {
                    ret = attributeDef;
                    break;
                }
            }
        }

        return ret;
    }

    public List<AtlasAttributeDef> getAttributeDefs() {
        return attributeDefs;
    }

    public void setAttributeDefs(List<AtlasAttributeDef> attributeDefs) {
        if (this.attributeDefs != null && this.attributeDefs == attributeDefs) {
            return;
        }

        if (attributeDefs == null || attributeDefs.isEmpty()) {
            this.attributeDefs = new ArrayList<>();
        } else {
            // if multiple attributes with same name are present, keep only the last entry
            List<AtlasAttributeDef> tmpList = new ArrayList<>(attributeDefs.size());
            Set<String> attribNames = new HashSet<>();

            ListIterator<AtlasAttributeDef> iter = attributeDefs.listIterator(attributeDefs.size());
            while (iter.hasPrevious()) {
                AtlasAttributeDef attributeDef = iter.previous();
                String attribName = attributeDef != null ? attributeDef.getName() : null;

                if (attribName != null) {
                    attribName = attribName.toLowerCase();

                    if (!attribNames.contains(attribName)) {
                        tmpList.add(new AtlasAttributeDef(attributeDef));

                        attribNames.add(attribName);
                    }
                }
            }
            Collections.reverse(tmpList);

            this.attributeDefs = tmpList;
        }
    }

    public AtlasAttributeDef getAttribute(String attrName) {
        return findAttribute(attributeDefs, attrName);
    }

    public void addAttribute(AtlasAttributeDef attributeDef) {
        if (attributeDef == null) {
            return;
        }

        List<AtlasAttributeDef> a = attributeDefs;

        List<AtlasAttributeDef> tmpList = new ArrayList<>();
        if (!a.isEmpty()) {
            // copy existing attributes, except ones having same name as the attribute being added
            for (AtlasAttributeDef existingAttrDef : a) {
                if (!existingAttrDef.getName().equalsIgnoreCase(attributeDef.getName())) {
                    tmpList.add(existingAttrDef);
                }
            }
        }
        tmpList.add(new AtlasAttributeDef(attributeDef));

        attributeDefs = tmpList;
    }

    public void removeAttribute(String attrName) {
        List<AtlasAttributeDef> a = attributeDefs;

        if (hasAttribute(a, attrName)) {
            List<AtlasAttributeDef> tmpList = new ArrayList<>();

            // copy existing attributes, except ones having same name as the attribute being removed
            for (AtlasAttributeDef existingAttrDef : a) {
                if (!existingAttrDef.getName().equalsIgnoreCase(attrName)) {
                    tmpList.add(existingAttrDef);
                }
            }

            attributeDefs = tmpList;
        }
    }

    public boolean hasAttribute(String attrName) {
        return getAttribute(attrName) != null;
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("AtlasStructDef{");
        super.toString(sb);
        sb.append(", attributeDefs=[");
        if (!attributeDefs.isEmpty()) {
            int i = 0;
            for (AtlasAttributeDef attributeDef : attributeDefs) {
                attributeDef.toString(sb);
                if (i > 0) {
                    sb.append(", ");
                }
                i++;
            }
        }
        sb.append("]");
        sb.append('}');

        return sb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AtlasStructDef that = (AtlasStructDef) o;
        return Objects.equals(attributeDefs, that.attributeDefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attributeDefs);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * class that captures details of a struct-attribute.
     */
    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtlasAttributeDef implements Serializable {
        public static final int DEFAULT_SEARCHWEIGHT = -1;
        public static final String SEARCH_WEIGHT_ATTR_NAME = "searchWeight";
        public static final String INDEX_TYPE_ATTR_NAME = "indexType";
        public static final String ATTRDEF_OPTION_SOFT_REFERENCE = "isSoftReference";
        public static final String ATTRDEF_OPTION_APPEND_ON_PARTIAL_UPDATE = "isAppendOnPartialUpdate";
        public static final int COUNT_NOT_SET = -1;
        private static final long serialVersionUID = 1L;
        private final String STRING_TRUE = "true";
        private String name;
        private String typeName;
        private boolean isOptional;
        private Cardinality cardinality;
        private int valuesMinCount;
        private int valuesMaxCount;
        private boolean isUnique;
        private boolean isIndexable;
        private boolean includeInNotification;
        private String defaultValue;
        private String description;
        private int searchWeight = DEFAULT_SEARCHWEIGHT;
        private IndexType indexType = null;
        private List<AtlasConstraintDef> constraints;
        private Map<String, String> options;
        private String displayName;

        public AtlasAttributeDef() {
            this(null, null);
        }

        public AtlasAttributeDef(String name, String typeName) {
            this(name, typeName, DEFAULT_SEARCHWEIGHT);
        }

        public AtlasAttributeDef(String name, String typeName, int searchWeight) {
            this(name, typeName, false, Cardinality.SINGLE, searchWeight, null);
        }

        private AtlasAttributeDef(String name, String typeName, boolean isOptional, Cardinality cardinality, int searchWeight, IndexType indexType) {
            this(name, typeName, isOptional, cardinality, COUNT_NOT_SET, COUNT_NOT_SET, false, false, false, null, searchWeight, indexType);
        }

        public AtlasAttributeDef(String name, String typeName, boolean isOptional, Cardinality cardinality,
                                 int valuesMinCount, int valuesMaxCount, boolean isUnique, boolean isIndexable, boolean includeInNotification, List<AtlasConstraintDef> constraints) {
            this(name, typeName, isOptional, cardinality, valuesMinCount, valuesMaxCount, isUnique, isIndexable, includeInNotification, constraints, DEFAULT_SEARCHWEIGHT, null);
        }

        private AtlasAttributeDef(String name, String typeName, boolean isOptional, Cardinality cardinality,
                                  int valuesMinCount, int valuesMaxCount, boolean isUnique, boolean isIndexable, boolean includeInNotification, List<AtlasConstraintDef> constraints, int searchWeight, IndexType indexType) {
            this(name, typeName, isOptional, cardinality, valuesMinCount, valuesMaxCount, isUnique, isIndexable, includeInNotification, null, constraints, null, null, searchWeight, indexType);
        }

        public AtlasAttributeDef(String name, String typeName, boolean isOptional, Cardinality cardinality,
                                 int valuesMinCount, int valuesMaxCount, boolean isUnique, boolean isIndexable, boolean includeInNotification, String defaultValue,
                                 List<AtlasConstraintDef> constraints, Map<String, String> options, String description, int searchWeight, IndexType indexType) {
            setName(name);
            setTypeName(typeName);
            setIsOptional(isOptional);
            setCardinality(cardinality);
            setValuesMinCount(valuesMinCount);
            setValuesMaxCount(valuesMaxCount);
            setIsUnique(isUnique);
            setIsIndexable(isIndexable);
            setIncludeInNotification(includeInNotification);
            setDefaultValue(defaultValue);
            setConstraints(constraints);
            setOptions(options);
            setDescription(description);
            setSearchWeight(searchWeight);
            setIndexType(indexType);
        }

        public AtlasAttributeDef(AtlasAttributeDef other) {
            if (other != null) {
                setName(other.getName());
                setTypeName(other.getTypeName());
                setIsOptional(other.getIsOptional());
                setCardinality(other.getCardinality());
                setValuesMinCount(other.getValuesMinCount());
                setValuesMaxCount(other.getValuesMaxCount());
                setIsUnique(other.getIsUnique());
                setIsIndexable(other.getIsIndexable());
                setIncludeInNotification(other.getIncludeInNotification());
                setDefaultValue(other.getDefaultValue());
                setConstraints(other.getConstraints());
                setOptions(other.getOptions());
                setDescription((other.getDescription()));
                setSearchWeight(other.getSearchWeight());
                setIndexType(other.getIndexType());
                setDisplayName(other.getDisplayName());
            }
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public int getSearchWeight() {
            return searchWeight;
        }

        public void setSearchWeight(int searchWeight) {
            this.searchWeight = searchWeight;
        }

        public IndexType getIndexType() {
            return indexType;
        }

        public void setIndexType(IndexType indexType) {
            this.indexType = indexType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public boolean getIsOptional() {
            return isOptional;
        }

        public void setIsOptional(boolean optional) {
            isOptional = optional;
        }

        public Cardinality getCardinality() {
            return cardinality;
        }

        public void setCardinality(Cardinality cardinality) {
            this.cardinality = cardinality;
        }

        public int getValuesMinCount() {
            return valuesMinCount;
        }

        public void setValuesMinCount(int valuesMinCount) {
            this.valuesMinCount = valuesMinCount;
        }

        public int getValuesMaxCount() {
            return valuesMaxCount;
        }

        public void setValuesMaxCount(int valuesMaxCount) {
            this.valuesMaxCount = valuesMaxCount;
        }

        public boolean getIsUnique() {
            return isUnique;
        }

        public void setIsUnique(boolean unique) {
            isUnique = unique;
        }

        public boolean getIsIndexable() {
            return isIndexable;
        }

        public void setIsIndexable(boolean idexable) {
            isIndexable = idexable;
        }

        public boolean getIncludeInNotification() {
            return includeInNotification;
        }

        public void setIncludeInNotification(Boolean isInNotification) {
            includeInNotification = isInNotification == null ? Boolean.FALSE : isInNotification;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public List<AtlasConstraintDef> getConstraints() {
            return constraints;
        }

        public void setConstraints(List<AtlasConstraintDef> constraints) {
            if (this.constraints != null && this.constraints == constraints) {
                return;
            }

            if (constraints == null || constraints.isEmpty()) {
                this.constraints = null;
            } else {
                this.constraints = new ArrayList<>(constraints);
            }
        }

        public void addConstraint(AtlasConstraintDef constraintDef) {
            List<AtlasConstraintDef> cDefs = constraints;

            if (cDefs == null) {
                cDefs = new ArrayList<>();

                constraints = cDefs;
            }

            cDefs.add(constraintDef);
        }

        public Map<String, String> getOptions() {
            return options;
        }

        public void setOptions(Map<String, String> options) {
            if (options != null) {
                this.options = new HashMap<>(options);
            } else {
                this.options = null;
            }
        }

        @JsonIgnore
        public boolean isSoftReferenced() {
            return options != null &&
                    getOptions().containsKey(AtlasAttributeDef.ATTRDEF_OPTION_SOFT_REFERENCE) &&
                    getOptions().get(AtlasAttributeDef.ATTRDEF_OPTION_SOFT_REFERENCE).equals(STRING_TRUE);
        }

        @JsonIgnore
        public boolean isAppendOnPartialUpdate() {
            String val = getOption(AtlasAttributeDef.ATTRDEF_OPTION_APPEND_ON_PARTIAL_UPDATE);

            return val != null && Boolean.valueOf(val);
        }

        @JsonIgnore
        public void setOption(String name, String value) {
            if (options == null) {
                options = new HashMap<>();
            }

            options.put(name, value);
        }

        @JsonIgnore
        public String getOption(String name) {
            Map<String, String> option = options;

            return option != null ? option.get(name) : null;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("AtlasAttributeDef{");
            sb.append("name='").append(name).append('\'');
            sb.append(", typeName='").append(typeName).append('\'');
            sb.append(", description='").append(description).append('\'');
            sb.append(", getIsOptional=").append(isOptional);
            sb.append(", cardinality=").append(cardinality);
            sb.append(", valuesMinCount=").append(valuesMinCount);
            sb.append(", valuesMaxCount=").append(valuesMaxCount);
            sb.append(", isUnique=").append(isUnique);
            sb.append(", isIndexable=").append(isIndexable);
            sb.append(", includeInNotification=").append(includeInNotification);
            sb.append(", defaultValue=").append(defaultValue);
            sb.append(", options='").append(options).append('\'');
            sb.append(", searchWeight='").append(searchWeight).append('\'');
            sb.append(", indexType='").append(indexType).append('\'');
            sb.append(", displayName='").append(displayName).append('\'');
            sb.append(", constraints=[");
            if (!constraints.isEmpty()) {
                int i = 0;
                for (AtlasConstraintDef constraintDef : constraints) {
                    constraintDef.toString(sb);
                    if (i > 0) {
                        sb.append(", ");
                    }
                    i++;
                }
            }
            sb.append("]");
            sb.append('}');

            return sb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AtlasAttributeDef that = (AtlasAttributeDef) o;
            return isOptional == that.isOptional &&
                    valuesMinCount == that.valuesMinCount &&
                    valuesMaxCount == that.valuesMaxCount &&
                    isUnique == that.isUnique &&
                    isIndexable == that.isIndexable &&
                    includeInNotification == that.includeInNotification &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(typeName, that.typeName) &&
                    cardinality == that.cardinality &&
                    Objects.equals(defaultValue, that.defaultValue) &&
                    Objects.equals(description, that.description) &&
                    Objects.equals(constraints, that.constraints) &&
                    Objects.equals(options, that.options) &&
                    Objects.equals(searchWeight, that.searchWeight) &&
                    Objects.equals(indexType, that.indexType) &&
                    Objects.equals(displayName, that.displayName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, typeName, isOptional, cardinality, valuesMinCount, valuesMaxCount, isUnique, isIndexable, includeInNotification, defaultValue, constraints, options, description, searchWeight, indexType, displayName);
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }

        /**
         * single-valued attribute or multi-valued attribute.
         */
        public enum Cardinality {SINGLE, LIST, SET}

        public enum IndexType {DEFAULT, STRING}
    }


    /**
     * class that captures details of a constraint.
     */
    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtlasConstraintDef implements Serializable {
        public static final String CONSTRAINT_TYPE_OWNED_REF = "ownedRef";
        public static final String CONSTRAINT_TYPE_INVERSE_REF = "inverseRef";
        public static final String CONSTRAINT_PARAM_ATTRIBUTE = "attribute";
        private static final long serialVersionUID = 1L;
        private String type;   // foreignKey/mappedFromRef/valueInRange
        private Map<String, Object> params; // onDelete=cascade/refAttribute=attr2/min=0,max=23


        public AtlasConstraintDef() {
        }

        public AtlasConstraintDef(String type) {
            this(type, null);
        }

        public AtlasConstraintDef(String type, Map<String, Object> params) {
            this.type = type;

            if (params != null) {
                this.params = new HashMap<>(params);
            }
        }

        public AtlasConstraintDef(AtlasConstraintDef that) {
            if (that != null) {
                type = that.type;

                if (that.params != null) {
                    params = new HashMap<>(that.params);
                }
            }
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        @JsonIgnore
        public boolean isConstraintType(String name) {
            return type.equalsIgnoreCase(name);
        }

        @JsonIgnore
        public Object getParam(String name) {
            Map<String, Object> params = this.params;

            return params != null ? params.get(name) : null;
        }

        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("AtlasConstraintDef{");
            sb.append("type='").append(type).append('\'');
            sb.append(", params='").append(params).append('\'');
            sb.append('}');

            return sb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AtlasConstraintDef that = (AtlasConstraintDef) o;
            return Objects.equals(type, that.type) &&
                    Objects.equals(params, that.params);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, params);
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }
}
