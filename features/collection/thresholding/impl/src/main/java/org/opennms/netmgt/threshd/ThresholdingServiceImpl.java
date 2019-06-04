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

import javax.annotation.PostConstruct;

import org.opennms.core.utils.InsufficientInformationException;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Thresholding Service.
 */
@EventListener(name = "threshd")
public class ThresholdingServiceImpl implements ThresholdingService {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingServiceImpl.class);

    @Autowired
    private ThresholdingEventProxy eventProxy;

    @Autowired
    private ThresholdingSetPersister thresholdingSetPersister;

    @Autowired
    private ResourceStorageDao resourceStorageDao;

    @EventHandler(uei = EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void nodeGainedService(Event event) throws InsufficientInformationException {
        LOG.debug(event.toString());
        EventUtils.checkNodeId(event);
        // Trigger re-evaluation of Threshold Packages, re-evaluating Filters.
        ThreshdConfigFactory.getInstance().rebuildPackageIpListMap();
    }

    @EventHandler(uei = EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI)
    public void handlenodeCategoryChanged(Event event) throws InsufficientInformationException {
        LOG.debug(event.toString());
        EventUtils.checkNodeId(event);
        // Trigger re-evaluation of Threshold Packages, re-evaluating Filters.
        ThreshdConfigFactory.getInstance().rebuildPackageIpListMap();
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void daemonReload(Event event) {
        final String thresholdsDaemonName = "Threshd";
        boolean isThresholds = false;
        for (Parm parm : event.getParmCollection()) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && thresholdsDaemonName.equalsIgnoreCase(parm.getValue().getContent())) {
                isThresholds = true;
                break;
            }
        }
        if (isThresholds) {
            try {
                ThreshdConfigFactory.reload();
                ThresholdingConfigFactory.reload();
                thresholdingSetPersister.clear();
            } catch (final Exception e) {
                throw new RuntimeException("Unable to reload thresholding.", e);
            }
        }
    }

    @Override
    public ThresholdingSession createSession(int nodeId, String hostAddress, String serviceName, RrdRepository repository, ServiceParameters serviceParams)
            throws ThresholdInitializationException {
        ThresholdingSessionKey sessionKey = new ThresholdingSessionKey(nodeId, hostAddress, serviceName, serviceName, serviceName);
        // ThresholdingSet thresholdingSet = new CollectorThresholdingSet(nodeId, hostAddress, serviceName, repository, serviceParams, resourceStorageDao, eventProxy);
        ThresholdingSession session = new ThresholdingSessionImpl(this, sessionKey, resourceStorageDao, repository);
        // thresholdingSetPersister.persistSet(session, thresholdingSet);
        return session;
    }

    public ThresholdingVisitorImpl getThresholdingVistor(ThresholdingSession session) {
        ThresholdingSet thresholdingSet = thresholdingSetPersister.getThresholdingSet(session, eventProxy);
        return new ThresholdingVisitorImpl(thresholdingSet, ((ThresholdingSessionImpl) session).getResourceDao(), eventProxy);
    }

    public ThresholdingEventProxy getEventProxy() {
        return eventProxy;
    }

    public void setEventProxy(ThresholdingEventProxy eventProxy) {
        this.eventProxy = eventProxy;
    }

    public ThresholdingSetPersister getThresholdingSetPersister() {
        return thresholdingSetPersister;
    }

    public void setThresholdingSetPersister(ThresholdingSetPersister thresholdingSetPersister) {
        this.thresholdingSetPersister = thresholdingSetPersister;
    }

    // Send?
    // thresholds configuration change
    // ueiList.add(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI);

    @PostConstruct
    private void init() {
        try {
            ThreshdConfigFactory.init();
            ThresholdingConfigFactory.init();
        } catch (final Exception e) {
            throw new RuntimeException("Unable to initialize thresholding.", e);
        }
    }

}