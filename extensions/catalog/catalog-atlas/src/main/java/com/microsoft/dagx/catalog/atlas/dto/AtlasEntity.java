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
import com.microsoft.dagx.common.collection.CollectionUtil;
import com.microsoft.dagx.common.string.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtlasEntity extends AtlasStruct implements Serializable {
    public static final String KEY_GUID = "guid";
    public static final String KEY_HOME_ID = "homeId";
    public static final String KEY_IS_PROXY = "isProxy";
    public static final String KEY_IS_INCOMPLETE = "isIncomplete";
    public static final String KEY_PROVENANCE_TYPE = "provenanceType";
    public static final String KEY_STATUS = "status";
    public static final String KEY_CREATED_BY = "createdBy";
    public static final String KEY_UPDATED_BY = "updatedBy";
    public static final String KEY_CREATE_TIME = "createTime";
    public static final String KEY_UPDATE_TIME = "updateTime";
    public static final String KEY_VERSION = "version";
    private static final long serialVersionUID = 1L;
    @JsonIgnore
    private static final AtomicLong s_nextId = new AtomicLong(System.nanoTime());
    private String guid = null;
    private String homeId = null;
    private Boolean isProxy = Boolean.FALSE;
    private Boolean isIncomplete = Boolean.FALSE;
    private Integer provenanceType = 0;
    private AtlasEntity.Status status = AtlasEntity.Status.ACTIVE;
    private String createdBy = null;
    private String updatedBy = null;
    private Date createTime = null;
    private Date updateTime = null;
    private Long version = 0L;

    private Map<String, Object> relationshipAttributes;
    private List<AtlasClassification> classifications;
    private List<AtlasTermAssignmentHeader> meanings;
    private Map<String, String> customAttributes;
    private Map<String, Map<String, Object>> businessAttributes;
    private Set<String> labels;

    public AtlasEntity() {
        this(null, null);
    }

    public AtlasEntity(String typeName) {
        this(typeName, null);
    }

    public AtlasEntity(AtlasEntityDef entityDef) {
        this(entityDef != null ? entityDef.getName() : null, null);
    }

    public AtlasEntity(String typeName, String attrName, Object attrValue) {
        super(typeName, attrName, attrValue);

        init();
    }

    public AtlasEntity(String typeName, Map<String, Object> attributes) {
        super(typeName, attributes);

        init();
    }

    public AtlasEntity(AtlasEntityHeader header) {
        super(header.getTypeName(), header.getAttributes());

        setGuid(header.getGuid());
        setStatus(header.getStatus());
        setClassifications(header.getClassifications());
        setMeanings(header.getMeanings());
    }

    public AtlasEntity(Map map) {
        super(map);

        if (map != null) {
            Object oGuid = map.get(KEY_GUID);
            Object homeId = map.get(KEY_HOME_ID);
            Object isProxy = map.get(KEY_IS_PROXY);
            Object isIncomplete = map.get(KEY_IS_INCOMPLETE);
            Object provenanceType = map.get(KEY_PROVENANCE_TYPE);
            Object status = map.get(KEY_STATUS);
            Object createdBy = map.get(KEY_CREATED_BY);
            Object updatedBy = map.get(KEY_UPDATED_BY);
            Object createTime = map.get(KEY_CREATE_TIME);
            Object updateTime = map.get(KEY_UPDATE_TIME);
            Object version = map.get(KEY_VERSION);

            if (oGuid != null) {
                setGuid(oGuid.toString());
            }

            if (homeId != null) {
                setHomeId(homeId.toString());
            }

            if (isProxy != null) {
                setIsProxy((Boolean) isProxy);
            } else {
                setIsProxy(Boolean.FALSE);
            }

            if (isIncomplete != null) {
                setIsIncomplete((Boolean) isIncomplete);
            } else {
                setIsIncomplete(Boolean.FALSE);
            }

            if (provenanceType instanceof Number) {
                setProvenanceType(((Number) version).intValue());
            }

            if (status != null) {
                setStatus(AtlasEntity.Status.valueOf(status.toString()));
            }

            if (createdBy != null) {
                setCreatedBy(createdBy.toString());
            }

            if (createTime instanceof Number) {
                setCreateTime(new Date(((Number) createTime).longValue()));
            }

            if (updatedBy != null) {
                setUpdatedBy(updatedBy.toString());
            }

            if (updateTime instanceof Number) {
                setUpdateTime(new Date(((Number) updateTime).longValue()));
            }

            if (version instanceof Number) {
                setVersion(((Number) version).longValue());
            }
        }
    }

    public AtlasEntity(AtlasEntity other) {
        super(other);

        if (other != null) {
            setGuid(other.getGuid());
            setHomeId(other.getHomeId());
            setIsProxy(other.isProxy());
            setIsIncomplete(other.getIsIncomplete());
            setProvenanceType(other.getProvenanceType());
            setStatus(other.getStatus());
            setCreatedBy(other.getCreatedBy());
            setUpdatedBy(other.getUpdatedBy());
            setCreateTime(other.getCreateTime());
            setUpdateTime(other.getUpdateTime());
            setVersion(other.getVersion());
            setClassifications(other.getClassifications());
            setRelationshipAttributes(other.getRelationshipAttributes());
            setMeanings(other.getMeanings());
            setCustomAttributes(other.getCustomAttributes());
            setBusinessAttributes(other.getBusinessAttributes());
            setLabels(other.getLabels());
        }
    }

    private static String nextInternalId() {
        return "-" + Long.toString(s_nextId.getAndIncrement());
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public Boolean isProxy() {
        return isProxy;
    }

    public void setIsProxy(Boolean isProxy) {
        this.isProxy = isProxy;
    }

    public Boolean getIsIncomplete() {
        return isIncomplete;
    }

    public void setIsIncomplete(Boolean isIncomplete) {
        this.isIncomplete = isIncomplete;
    }

    public Integer getProvenanceType() {
        return provenanceType;
    }

    public void setProvenanceType(Integer provenanceType) {
        this.provenanceType = provenanceType;
    }

    public AtlasEntity.Status getStatus() {
        return status;
    }

    public void setStatus(AtlasEntity.Status status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Map<String, Object> getRelationshipAttributes() {
        return relationshipAttributes;
    }

    public void setRelationshipAttributes(Map<String, Object> relationshipAttributes) {
        this.relationshipAttributes = relationshipAttributes;
    }

    public void setRelationshipAttribute(String name, Object value) {
        Map<String, Object> r = relationshipAttributes;

        if (r != null) {
            r.put(name, value);
        } else {
            r = new HashMap<>();
            r.put(name, value);

            relationshipAttributes = r;
        }
    }

    public Object getRelationshipAttribute(String name) {
        Map<String, Object> a = relationshipAttributes;

        return a != null ? a.get(name) : null;
    }

    public boolean hasRelationshipAttribute(String name) {
        Map<String, Object> r = relationshipAttributes;

        return r != null ? r.containsKey(name) : false;
    }

    public Map<String, String> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(Map<String, String> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public Map<String, Map<String, Object>> getBusinessAttributes() {
        return businessAttributes;
    }

    public void setBusinessAttributes(Map<String, Map<String, Object>> businessAttributes) {
        this.businessAttributes = businessAttributes;
    }

    public void setBusinessAttribute(String nsName, String nsAttrName, Object nsValue) {
        Map<String, Map<String, Object>> businessAttributes = this.businessAttributes;

        if (businessAttributes == null) {
            businessAttributes = new HashMap<>();

            this.businessAttributes = businessAttributes;
        }

        Map<String, Object> businessAttributeMap = businessAttributes.get(nsName);

        if (businessAttributeMap == null) {
            businessAttributeMap = new HashMap<>();

            businessAttributes.put(nsName, businessAttributeMap);
        }

        businessAttributeMap.put(nsAttrName, nsValue);
    }

    public Object getBusinessAttribute(String bmName, String bmAttrName) {
        Map<String, Map<String, Object>> businessAttributes = this.businessAttributes;
        Map<String, Object> businessAttributeMap = businessAttributes == null ? null : businessAttributes.get(bmName);

        return businessAttributeMap == null ? null : businessAttributeMap.get(bmAttrName);
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public List<AtlasClassification> getClassifications() {
        return classifications;
    }

    public void setClassifications(List<AtlasClassification> classifications) {
        this.classifications = classifications;
    }

    public void addClassifications(List<AtlasClassification> classifications) {
        List<AtlasClassification> c = this.classifications;

        if (c == null) {
            c = new ArrayList<>();
        }

        c.addAll(classifications);

        this.classifications = c;
    }

    public List<AtlasTermAssignmentHeader> getMeanings() {
        return meanings;
    }

    public void setMeanings(final List<AtlasTermAssignmentHeader> meanings) {
        this.meanings = meanings;
    }

    public void addMeaning(AtlasTermAssignmentHeader meaning) {
        List<AtlasTermAssignmentHeader> meanings = this.meanings;

        if (meanings == null) {
            meanings = new ArrayList<>();
        }
        meanings.add(meaning);
        setMeanings(meanings);
    }

    private void init() {
        setGuid(nextInternalId());
        setHomeId(null);
        setIsProxy(Boolean.FALSE);
        setIsIncomplete(Boolean.FALSE);
        setProvenanceType(0);
        setStatus(null);
        setCreatedBy(null);
        setUpdatedBy(null);
        setCreateTime(null);
        setUpdateTime(null);
        setClassifications(null);
        setMeanings(null);
        setCustomAttributes(null);
        setBusinessAttributes(null);
        setLabels(null);
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("AtlasEntity{");
        super.toString(sb);
        sb.append("guid='").append(guid).append('\'');
        sb.append(", homeId='").append(homeId).append('\'');
        sb.append(", isProxy='").append(isProxy).append('\'');
        sb.append(", isIncomplete=").append(isIncomplete);
        sb.append(", provenanceType=").append(provenanceType);
        sb.append(", status=").append(status);
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", updatedBy='").append(updatedBy).append('\'');
        dumpDateField(", createTime=", createTime, sb);
        dumpDateField(", updateTime=", updateTime, sb);
        sb.append(", version=").append(version);
        sb.append(", relationshipAttributes=[");
        dumpObjects(relationshipAttributes, sb);
        sb.append("]");
        sb.append(", classifications=[");
        AtlasBaseTypeDef.dumpObjects(classifications, sb);
        sb.append(']');
        sb.append(", meanings=[");
        AtlasBaseTypeDef.dumpObjects(meanings, sb);
        sb.append(']');
        sb.append(", customAttributes=[");
        dumpObjects(customAttributes, sb);
        sb.append("]");
        sb.append(", businessAttributes=[");
        dumpObjects(businessAttributes, sb);
        sb.append("]");
        sb.append(", labels=[");
        dumpObjects(labels, sb);
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

        AtlasEntity that = (AtlasEntity) o;
        return Objects.equals(guid, that.guid) &&
                Objects.equals(homeId, that.homeId) &&
                Objects.equals(isProxy, that.isProxy) &&
                Objects.equals(isIncomplete, that.isIncomplete) &&
                Objects.equals(provenanceType, that.provenanceType) &&
                status == that.status &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(version, that.version) &&
                Objects.equals(relationshipAttributes, that.relationshipAttributes) &&
                Objects.equals(customAttributes, that.customAttributes) &&
                Objects.equals(businessAttributes, that.businessAttributes) &&
                Objects.equals(labels, that.labels) &&
                Objects.equals(classifications, that.classifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), guid, homeId, isProxy, isIncomplete, provenanceType, status, createdBy, updatedBy,
                createTime, updateTime, version, relationshipAttributes, classifications, customAttributes, businessAttributes, labels);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * Status of the entity - can be active or deleted. Deleted entities are not removed from Atlas store.
     */
    public enum Status {ACTIVE, DELETED, PURGED}

    /**
     * An instance of an entity along with extended info - like hive_table, hive_database.
     */
    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtlasEntityExtInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Map<String, AtlasEntity> referredEntities;


        public AtlasEntityExtInfo() {
            setReferredEntities(null);
        }

        public AtlasEntityExtInfo(AtlasEntity referredEntity) {
            addReferredEntity(referredEntity);
        }

        public AtlasEntityExtInfo(Map<String, AtlasEntity> referredEntities) {
            setReferredEntities(referredEntities);
        }

        public AtlasEntityExtInfo(AtlasEntity.AtlasEntityExtInfo other) {
            if (other != null) {
                setReferredEntities(other.getReferredEntities());
            }
        }

        public Map<String, AtlasEntity> getReferredEntities() {
            return referredEntities;
        }

        public void setReferredEntities(Map<String, AtlasEntity> referredEntities) {
            this.referredEntities = referredEntities;
        }

        @JsonIgnore
        public final void addReferredEntity(AtlasEntity entity) {
            addReferredEntity(entity.getGuid(), entity);
        }

        @JsonIgnore
        public final void addReferredEntity(String guid, AtlasEntity entity) {
            Map<String, AtlasEntity> r = referredEntities;

            if (r == null) {
                r = new HashMap<>();

                referredEntities = r;
            }

            if (guid != null) {
                r.put(guid, entity);
            }
        }

        @JsonIgnore
        public final AtlasEntity removeReferredEntity(String guid) {
            Map<String, AtlasEntity> r = referredEntities;

            return r != null && guid != null ? r.remove(guid) : null;
        }

        @JsonIgnore
        public final AtlasEntity getReferredEntity(String guid) {
            Map<String, AtlasEntity> r = referredEntities;

            return r != null && guid != null ? r.get(guid) : null;
        }

        @JsonIgnore
        public AtlasEntity getEntity(String guid) {
            return getReferredEntity(guid);
        }

        @JsonIgnore
        public AtlasEntity removeEntity(String guid) {
            Map<String, AtlasEntity> r = referredEntities;

            return r != null && guid != null ? r.remove(guid) : null;
        }

        public void updateEntityGuid(String oldGuid, String newGuid) {
            AtlasEntity entity = getEntity(oldGuid);

            if (entity != null) {
                entity.setGuid(newGuid);

                if (removeEntity(oldGuid) != null) {
                    addReferredEntity(newGuid, entity);
                }
            }
        }

        public boolean hasEntity(String guid) {
            return getEntity(guid) != null;
        }

        public void compact() {
            // for derived classes to implement their own logic
        }

        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("AtlasEntityExtInfo{");
            sb.append("referredEntities={");
            AtlasBaseTypeDef.dumpObjects(referredEntities, sb);
            sb.append("}");
            sb.append("}");

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

            AtlasEntity.AtlasEntityExtInfo that = (AtlasEntity.AtlasEntityExtInfo) o;
            return Objects.equals(referredEntities, that.referredEntities);
        }

        @Override
        public int hashCode() {
            return Objects.hash(referredEntities);
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }

    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtlasEntityWithExtInfo extends AtlasEntity.AtlasEntityExtInfo {
        private static final long serialVersionUID = 1L;

        private AtlasEntity entity;

        public AtlasEntityWithExtInfo() {
            this(null, null);
        }

        public AtlasEntityWithExtInfo(AtlasEntity entity) {
            this(entity, null);
        }

        public AtlasEntityWithExtInfo(AtlasEntity entity, AtlasEntity.AtlasEntityExtInfo extInfo) {
            super(extInfo);

            this.entity = entity;
        }

        public AtlasEntity getEntity() {
            return entity;
        }

        public void setEntity(AtlasEntity entity) {
            this.entity = entity;
        }

        @JsonIgnore
        @Override
        public AtlasEntity getEntity(String guid) {
            AtlasEntity ret = super.getEntity(guid);

            if (ret == null && entity != null) {
                if (StringUtils.equals(guid, entity.getGuid())) {
                    ret = entity;
                }
            }

            return ret;
        }

        @JsonIgnore
        @Override
        public void compact() {
            super.compact();

            // remove 'entity' from referredEntities
            if (entity != null) {
                removeEntity(entity.getGuid());
            }
        }

        @Override
        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("AtlasEntityWithExtInfo{");
            sb.append("entity=").append(entity).append(",");
            super.toString(sb);
            sb.append("}");

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

            AtlasEntity.AtlasEntityWithExtInfo that = (AtlasEntity.AtlasEntityWithExtInfo) o;
            return Objects.equals(entity, that.entity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), entity);
        }
    }

    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtlasEntitiesWithExtInfo extends AtlasEntity.AtlasEntityExtInfo {
        private static final long serialVersionUID = 1L;

        private List<AtlasEntity> entities;

        public AtlasEntitiesWithExtInfo() {
            this(null, null);
        }

        public AtlasEntitiesWithExtInfo(AtlasEntity entity) {
            this(Arrays.asList(entity), null);
        }

        public AtlasEntitiesWithExtInfo(List<AtlasEntity> entities) {
            this(entities, null);
        }

        public AtlasEntitiesWithExtInfo(AtlasEntity.AtlasEntityWithExtInfo entity) {
            this(Arrays.asList(entity.getEntity()), entity);
        }

        public AtlasEntitiesWithExtInfo(List<AtlasEntity> entities, AtlasEntity.AtlasEntityExtInfo extInfo) {
            super(extInfo);

            this.entities = entities;
        }

        public List<AtlasEntity> getEntities() {
            return entities;
        }

        public void setEntities(List<AtlasEntity> entities) {
            this.entities = entities;
        }

        @JsonIgnore
        @Override
        public AtlasEntity getEntity(String guid) {
            AtlasEntity ret = super.getEntity(guid);

            if (ret == null && CollectionUtil.isNotEmpty(entities)) {
                for (AtlasEntity entity : entities) {
                    if (StringUtils.equals(guid, entity.getGuid())) {
                        ret = entity;

                        break;
                    }
                }
            }

            return ret;
        }

        public void addEntity(AtlasEntity entity) {
            List<AtlasEntity> entities = this.entities;

            if (entities == null) {
                entities = new ArrayList<>();

                this.entities = entities;
            }

            entities.add(entity);
        }

        public void removeEntity(AtlasEntity entity) {
            List<AtlasEntity> entities = this.entities;

            if (entity != null && entities != null) {
                entities.remove(entity);
            }
        }

        @Override
        public void compact() {
            super.compact();

            // remove 'entities' from referredEntities
            if (CollectionUtil.isNotEmpty(entities)) {
                for (AtlasEntity entity : entities) {
                    removeReferredEntity(entity.getGuid());
                }
            }
        }

        @Override
        public StringBuilder toString(StringBuilder sb) {
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append("AtlasEntitiesWithExtInfo{");
            sb.append("entities=[");
            AtlasBaseTypeDef.dumpObjects(entities, sb);
            sb.append("],");
            super.toString(sb);
            sb.append("}");

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

            AtlasEntity.AtlasEntitiesWithExtInfo that = (AtlasEntity.AtlasEntitiesWithExtInfo) o;
            return Objects.equals(entities, that.entities);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), entities);
        }
    }
}

