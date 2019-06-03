/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collection.api;

import java.util.Map;

import org.opennms.netmgt.model.ResourcePath;

import com.google.common.collect.Maps;

/**
 * <p>LatencyCollectionResource class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class LatencyCollectionResource implements CollectionResource {

    private final String m_serviceName;
    private final String m_ipAddress;
    private final String m_location;
    private final Map<AttributeGroupType, AttributeGroup> m_attributeGroups = Maps.newLinkedHashMap();
    private final String m_ifLabel;
    private final Map<String, String> m_ifInfo = Maps.newLinkedHashMap();

    /**
     * <p>Constructor for LatencyCollectionResource.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param location a {@link java.lang.String} object.
     */
    public LatencyCollectionResource(String serviceName, String ipAddress, String location, String ifLabel, Map<String, String> ifInfo) {
        m_serviceName = serviceName;
        m_ipAddress = ipAddress;
        m_location = location;
        m_ifLabel = ifLabel;
        m_ifInfo.putAll(ifInfo);
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInstance() {
        return m_ipAddress + "[" + m_serviceName + "]";
    }
    
    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return m_ipAddress;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getInterfaceLabel() {
        return m_serviceName;
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getResourceTypeName() {
        return CollectionResource.RESOURCE_TYPE_LATENCY;
    }

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean rescanNeeded() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /**
     * Adds the given attribute into the collection for this resource
     *
     * @param attr The Attribute to add
     */
    public void addAttribute(CollectionAttribute attr) {
        AttributeGroup group = getGroup(attr.getAttributeType().getGroupType());
        group.addAttribute(attr);
    }

    /**
     * Finds, or creates, and returns the AttributeGroup for the given group Type
     *
     * @param groupType a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
     * @return a {@link org.opennms.netmgt.collection.api.AttributeGroup} object.
     */
    public final AttributeGroup getGroup(AttributeGroupType groupType) {
        AttributeGroup group = m_attributeGroups.get(groupType);
        if (group == null) {
            group = new AttributeGroup(this, groupType);
            m_attributeGroups.put(groupType, group);
        }
        return group;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitResource(this);
        for (AttributeGroup group: m_attributeGroups.values()) {
            group.visit(visitor);
        }
        visitor.completeResource(this);
    }

    /**
     * <p>getOwnerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getOwnerName() {
        return m_ipAddress;
    }

    @Override
    public ResourcePath getPath() {
        if (m_location == null || "Default".equals(m_location)) {
            return ResourcePath.get(m_ipAddress);
        } else {
            return ResourcePath.get(ResourcePath.sanitize(m_location), m_ipAddress);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("%s on %s at %s", m_serviceName, m_ipAddress, m_location);
    }

    @Override
    public ResourcePath getParent() {
        return ResourcePath.get(m_ipAddress);
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

    public String getIfLabel() {
        return m_ifLabel;
    }

    public Map<String, String> getIfInfo() {
        return m_ifInfo;
    }

}
