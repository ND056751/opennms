/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.xml.event.Event;

public interface ThresholdingSet {

    boolean hasThresholds();

    /*
     * Returns true if the specified attribute is involved in any of defined thresholds for node/address/service
     * 
     * @param resourceTypeName a {@link java.lang.String} object.
     * @param attributeName a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean hasThresholds(final String resourceTypeName, final String attributeName);

    boolean isNodeInOutage();

    void reinitialize();

    void setCounterReset(boolean counterReset);

    boolean hasThresholds(CollectionAttribute attribute);

    List<Event> applyThresholds(CollectionResource resource, Map<String, CollectionAttribute> m_attributesMap, Date m_collectionTimestamp);

    @Deprecated
    boolean hasThresholds(Map<String, Double> attributes);

}
