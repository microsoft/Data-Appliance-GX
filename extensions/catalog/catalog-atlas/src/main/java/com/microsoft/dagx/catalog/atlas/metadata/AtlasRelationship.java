/*
 * Copyright (c) Microsoft Corporation.
 *  All rights reserved.
 *
 */

package com.microsoft.dagx.catalog.atlas.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.microsoft.dagx.catalog.atlas.dto.AtlasClassification;
import com.microsoft.dagx.catalog.atlas.dto.AtlasEntityHeader;
import com.microsoft.dagx.catalog.atlas.dto.AtlasRelationshipDef;
import com.microsoft.dagx.catalog.atlas.dto.AtlasStruct;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AtlasRelationship extends AtlasStruct implements Serializable {
    public static final String KEY_GUID = "guid";
    public static final String KEY_HOME_ID = "homeId";
    public static final String KEY_PROVENANCE_TYPE = "provenanceType";
    public static final String KEY_STATUS = "status";
    public static final String KEY_CREATED_BY = "createdBy";
    public static final String KEY_UPDATED_BY = "updatedBy";
    public static final String KEY_CREATE_TIME = "createTime";
    public static final String KEY_UPDATE_TIME = "updateTime";
    public static final String KEY_VERSION = "version";
    public static final String KEY_END1 = "end1";
    public static final String KEY_END2 = "end2";
    public static final String KEY_LABEL = "label";
    public static final String KEY_PROPAGATE_TAGS = "propagateTags";
    public static final String KEY_BLOCKED_PROPAGATED_CLASSIFICATIONS = "blockedPropagatedClassifications";
    public static final String KEY_PROPAGATED_CLASSIFICATIONS = "propagatedClassifications";
    private static final long serialVersionUID = 1L;
    @JsonIgnore
    private static final AtomicLong s_nextId = new AtomicLong(System.nanoTime());
    private String guid = null;
    private String homeId = null;
    private Integer provenanceType = null;
    private AtlasObjectId end1 = null;
    private AtlasObjectId end2 = null;
    private String label = null;
    private AtlasRelationshipDef.PropagateTags propagateTags = AtlasRelationshipDef.PropagateTags.NONE;
    private AtlasRelationship.Status status = AtlasRelationship.Status.ACTIVE;
    private String createdBy = null;
    private String updatedBy = null;
    private Date createTime = null;
    private Date updateTime = null;
    private Long version = 0L;
    private Set<AtlasClassification> propagatedClassifications;
    private Set<AtlasClassification> blockedPropagatedClassifications;

    public AtlasRelationship() {
        super();

        init();
    }

    public AtlasRelationship(String typeName) {
        this(typeName, null);
    }

    public AtlasRelationship(String typeName, Map<String, Object> attributes) {
        super(typeName, attributes);

        init();
    }

    public AtlasRelationship(String typeName, AtlasObjectId end1, AtlasObjectId end2) {
        super(typeName);

        init(nextInternalId(), null, 0, end1, end2, null, null, null, null, null, null, null, 0L, null, null);
    }

    public AtlasRelationship(String typeName, AtlasObjectId end1, AtlasObjectId end2, Map<String, Object> attributes) {
        super(typeName, attributes);

        init(nextInternalId(), null, 0, end1, end2, null, null, null, null, null, null, null, 0L, null, null);
    }

    public AtlasRelationship(String typeName, String attrName, Object attrValue) {
        super(typeName, attrName, attrValue);

        init();
    }

    public AtlasRelationship(AtlasRelationshipDef relationshipDef) {
        this(relationshipDef != null ? relationshipDef.getName() : null);
    }

    public AtlasRelationship(AtlasRelationship other) {
        super(other);

        if (other != null) {
            init(other.guid, other.homeId, other.provenanceType, other.end1, other.end2, other.label, other.propagateTags, other.status, other.createdBy, other.updatedBy,
                    other.createTime, other.updateTime, other.version, other.propagatedClassifications, other.blockedPropagatedClassifications);
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

    public Integer getProvenanceType() {
        return provenanceType;
    }

    public void setProvenanceType(Integer provenanceType) {
        this.provenanceType = provenanceType;
    }

    public AtlasRelationship.Status getStatus() {
        return status;
    }

    public void setStatus(AtlasRelationship.Status status) {
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

    public AtlasObjectId getEnd1() {
        return end1;
    }

    public void setEnd1(AtlasObjectId end1) {
        this.end1 = end1;
    }

    public AtlasObjectId getEnd2() {
        return end2;
    }

    public void setEnd2(AtlasObjectId end2) {
        this.end2 = end2;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public AtlasRelationshipDef.PropagateTags getPropagateTags() {
        return propagateTags;
    }

    public void setPropagateTags(AtlasRelationshipDef.PropagateTags propagateTags) {
        this.propagateTags = propagateTags;
    }

    public Set<AtlasClassification> getPropagatedClassifications() {
        return propagatedClassifications;
    }

    public void setPropagatedClassifications(Set<AtlasClassification> propagatedClassifications) {
        this.propagatedClassifications = propagatedClassifications;
    }

    public Set<AtlasClassification> getBlockedPropagatedClassifications() {
        return blockedPropagatedClassifications;
    }

    public void setBlockedPropagatedClassifications(Set<AtlasClassification> blockedPropagatedClassifications) {
        this.blockedPropagatedClassifications = blockedPropagatedClassifications;
    }

    private void init() {
        init(nextInternalId(), null, 0, null, null, null, null, null, null, null, null, null, 0L, null, null);
    }

    private void init(String guid, String homeId, Integer provenanceType, AtlasObjectId end1, AtlasObjectId end2, String label, AtlasRelationshipDef.PropagateTags propagateTags,
                      AtlasRelationship.Status status, String createdBy, String updatedBy, Date createTime, Date updateTime, Long version,
                      Set<AtlasClassification> propagatedClassifications, Set<AtlasClassification> blockedPropagatedClassifications) {
        setGuid(guid);
        setHomeId(homeId);
        setProvenanceType(provenanceType);
        setEnd1(end1);
        setEnd2(end2);
        setLabel(label);
        setPropagateTags(propagateTags);
        setStatus(status);
        setCreatedBy(createdBy);
        setUpdatedBy(updatedBy);
        setCreateTime(createTime);
        setUpdateTime(updateTime);
        setVersion(version);
        setPropagatedClassifications(propagatedClassifications);
        setBlockedPropagatedClassifications(blockedPropagatedClassifications);
    }

    @Override
    public StringBuilder toString(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("AtlasRelationship{");
        super.toString(sb);
        sb.append("guid='").append(guid).append('\'');
        sb.append(", homeId='").append(homeId).append('\'');
        sb.append(", provenanceType=").append(provenanceType);
        sb.append(", end1=").append(end1);
        sb.append(", end2=").append(end2);
        sb.append(", label='").append(label).append('\'');
        sb.append(", propagateTags=").append(propagateTags);
        sb.append(", status=").append(status);
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", updatedBy='").append(updatedBy).append('\'');
        dumpDateField(", createTime=", createTime, sb);
        dumpDateField(", updateTime=", updateTime, sb);
        sb.append(", version=").append(version);
        sb.append(", propagatedClassifications=[");
        dumpObjects(propagatedClassifications, sb);
        sb.append("]");
        sb.append(", blockedPropagatedClassifications=[");
        dumpObjects(blockedPropagatedClassifications, sb);
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

        AtlasRelationship that = (AtlasRelationship) o;
        return Objects.equals(guid, that.guid) &&
                Objects.equals(homeId, that.homeId) &&
                Objects.equals(provenanceType, that.provenanceType) &&
                Objects.equals(end1, that.end1) &&
                Objects.equals(end2, that.end2) &&
                Objects.equals(label, that.label) &&
                propagateTags == that.propagateTags &&
                status == that.status &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(version, that.version) &&
                Objects.equals(propagatedClassifications, that.propagatedClassifications) &&
                Objects.equals(blockedPropagatedClassifications, that.blockedPropagatedClassifications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), guid, homeId, provenanceType, end1, end2, label, propagateTags, status, createdBy, updatedBy,
                createTime, updateTime, version, propagatedClassifications, blockedPropagatedClassifications);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public enum Status {ACTIVE, DELETED}

    @JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AtlasRelationshipWithExtInfo implements Serializable {
        private AtlasRelationship relationship;
        private Map<String, AtlasEntityHeader> referredEntities;

        public AtlasRelationshipWithExtInfo() {
        }

        public AtlasRelationshipWithExtInfo(AtlasRelationship relationship) {
            setRelationship(relationship);
        }

        public AtlasRelationship getRelationship() {
            return relationship;
        }

        public void setRelationship(AtlasRelationship relationship) {
            this.relationship = relationship;
        }

        public Map<String, AtlasEntityHeader> getReferredEntities() {
            return referredEntities;
        }

        public void setReferredEntities(Map<String, AtlasEntityHeader> referredEntities) {
            this.referredEntities = referredEntities;
        }

        public boolean referredEntitiesContains(String guid) {
            return (referredEntities != null) ? referredEntities.containsKey(guid) : false;
        }

        @JsonIgnore
        public final void addReferredEntity(String guid, AtlasEntityHeader entityHeader) {
            Map<String, AtlasEntityHeader> r = referredEntities;

            if (r == null) {
                r = new HashMap<>();

                referredEntities = r;
            }

            if (guid != null) {
                r.put(guid, entityHeader);
            }
        }

        @JsonIgnore
        public final AtlasEntityHeader removeReferredEntity(String guid) {
            Map<String, AtlasEntityHeader> r = referredEntities;

            return r != null && guid != null ? r.remove(guid) : null;
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
            AtlasRelationship.AtlasRelationshipWithExtInfo that = (AtlasRelationship.AtlasRelationshipWithExtInfo) o;

            return Objects.equals(relationship, that.relationship) &&
                    Objects.equals(referredEntities, that.referredEntities);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), relationship, referredEntities);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("AtlasRelationshipWithExtInfo{");
            sb.append("relationship=").append(relationship);
            sb.append(", referredEntities=").append(referredEntities);
            sb.append('}');

            return sb.toString();
        }
    }
}