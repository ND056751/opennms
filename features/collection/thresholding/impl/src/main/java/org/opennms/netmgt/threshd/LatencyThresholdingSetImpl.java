/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.LatencyCollectionAttribute;
import org.opennms.netmgt.collection.api.LatencyCollectionAttributeType;
import org.opennms.netmgt.collection.api.LatencyCollectionResource;
import org.opennms.netmgt.dao.api.IfLabel;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>LatencyThresholdingSet class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class LatencyThresholdingSetImpl extends ThresholdingSetImpl {

    private String m_location;

    /**
     * <p>Constructor for LatencyThresholdingSet.</p>
     *
     * @param nodeId a int.
     * @param hostAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param repository a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     * @param eventProxy 
     * @param interval a long.
     * @throws ThresholdInitializationException 
     */
    public LatencyThresholdingSetImpl(int nodeId, String hostAddress, String serviceName, String location, RrdRepository repository, ResourceStorageDao resourceStorageDao, ThresholdingEventProxy eventProxy)
            throws ThresholdInitializationException {
        super(nodeId, hostAddress, serviceName, repository, resourceStorageDao, eventProxy);
        m_location = location;
    }

    public void applyThresholds(String svcName, Map<String, Double> attributes, IfLabel ifLabelDao) {
        String ifLabel = "";
        Map<String, String> ifInfo = new HashMap<>();
        if (ifLabelDao != null) {
            ifLabel = ifLabelDao.getIfLabel(m_nodeId, InetAddressUtils.addr(m_hostAddress));
            if (ifLabel != null) {
                ifInfo = ifLabelDao.getInterfaceInfoFromIfLabel(m_nodeId, ifLabel);
            }
        }
        LatencyCollectionResource latencyResource = new LatencyCollectionResource(svcName, m_hostAddress, m_location, ifLabel, ifInfo);
        LatencyCollectionAttributeType latencyType = new LatencyCollectionAttributeType();
        Map<String, CollectionAttribute> attributesMap = new HashMap<String, CollectionAttribute>();
        for (final Entry<String, Double> entry : attributes.entrySet()) {
            final String ds = entry.getKey();
            attributesMap.put(ds, new LatencyCollectionAttribute(latencyResource, latencyType, ds, entry.getValue()));
        }
        //The timestamp is irrelevant; latency is never a COUNTER (which is the only reason the date is used).  
        //Yes, we have to know a little too much about the implementation details of CollectionResourceWrapper to say that, but
        // we have little choice
        CollectionResourceWrapper resourceWrapper = new CollectionResourceWrapper(new Date(), m_nodeId, m_hostAddress, m_serviceName, m_repository, latencyResource, attributesMap,
                                                                                  m_resourceStorageDao);
        List<Event> events = applyThresholds(resourceWrapper, attributesMap);
        // TODO move sending events into applyThresholds()
        for (Event event : events) {
            m_eventProxy.sendEvent(event);
        }
    }

}
