/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.pollables;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.LatencyCollectionAttribute;
import org.opennms.netmgt.collection.api.LatencyCollectionAttributeType;
import org.opennms.netmgt.collection.api.LatencyCollectionResource;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.SingleResourceCollectionSet;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.api.IfLabel;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.ThresholdingService;
import org.opennms.netmgt.threshd.ThresholdingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>LatencyStoringServiceMonitorAdaptor class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 */
public class LatencyStoringServiceMonitorAdaptor implements ServiceMonitorAdaptor {

    private static final Logger LOG = LoggerFactory.getLogger(LatencyStoringServiceMonitorAdaptor.class);

    public static final int HEARTBEAT_STEP_MULTIPLIER = 2;

    private PollerConfig m_pollerConfig;
    private Package m_pkg;
    private final PersisterFactory m_persisterFactory;

    private final ThresholdingService m_thresholdingService;

    private ThresholdingSession m_thresholdingSession;

    private final IfLabel m_ifLabelDao;

    public LatencyStoringServiceMonitorAdaptor(PollerConfig config, Package pkg, PersisterFactory persisterFactory, ThresholdingService thresholdingService, IfLabel ifLabelDao) {
        m_pollerConfig = config;
        m_pkg = pkg;
        m_persisterFactory = persisterFactory;
        m_thresholdingService = thresholdingService;
        m_ifLabelDao = ifLabelDao;
    }

    @Override
    public PollStatus handlePollResult(MonitoredService svc, Map<String, Object> parameters, PollStatus status) {
        if (!status.getProperties().isEmpty()) {
            storeResponseTime(svc, new LinkedHashMap<String, Number>(status.getProperties()), parameters);
        }
        return status;
    }

    private void storeResponseTime(MonitoredService svc, Map<String, Number> entries, Map<String,Object> parameters) {
        String rrdPath     = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
        String dsName      = ParameterMap.getKeyedString(parameters, "ds-name", PollStatus.PROPERTY_RESPONSE_TIME);
        String rrdBaseName = ParameterMap.getKeyedString(parameters, "rrd-base-name", dsName);
        String thresholds  = ParameterMap.getKeyedString(parameters, "thresholding-enabled", "false");

        if (!entries.containsKey(dsName) && entries.containsKey(PollStatus.PROPERTY_RESPONSE_TIME)) {
            entries.put(dsName, entries.get(PollStatus.PROPERTY_RESPONSE_TIME));
            entries.remove(PollStatus.PROPERTY_RESPONSE_TIME);
        }

        if (rrdPath == null) {
            LOG.debug("storeResponseTime: RRD repository not specified in parameters, latency data will not be stored.");
            return;
        }

        CollectionSet collectionSet = getCollectionSet(svc, entries, rrdBaseName);
        File rrdRepositoryRoot = new File(rrdPath);

        if (thresholds.equalsIgnoreCase("true")) {
            applyThresholds(rrdRepositoryRoot, collectionSet, thresholds, svc, thresholds, entries);
        } else {
            LOG.debug("storeResponseTime: Thresholds processing is not enabled. Check thresholding-enabled parameter on service definition");
        }

        LOG.debug("storeResponseTime: Persisting latency data for {}", svc);
        persistCollectionSet(collectionSet, rrdRepositoryRoot);
    }

    private void persistCollectionSet(CollectionSet collectionSet, File rrdRepositoryRoot) {
        RrdRepository repository = new RrdRepository();
        repository.setStep(m_pollerConfig.getStep(m_pkg));
        repository.setRraList(m_pollerConfig.getRRAList(m_pkg));
        repository.setHeartBeat(repository.getStep() * HEARTBEAT_STEP_MULTIPLIER);
        repository.setRrdBaseDir(rrdRepositoryRoot);
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        CollectionSetVisitor persister = m_persisterFactory.createPersister(params, repository, false, true, true);
        collectionSet.visit(persister);
    }

    private void applyThresholds(File repository, CollectionSet collectionSet, String rrdPath, MonitoredService service, String dsName, Map<String, Number> entries) {
        try {
            if (m_thresholdingSession == null) {
                // FIXME remove or initialize serviceparams
                // FIXME remove repository
                ServiceParameters serviceParams = new ServiceParameters(new HashMap<>());
                m_thresholdingSession = m_thresholdingService.createSession(service.getNodeId(), 
                                                                            service.getIpAddr(),
                                                                            service.getSvcName(),
                                                                            null,
                                                                            serviceParams);
            }
            m_thresholdingSession.accept(collectionSet);
        } catch (Throwable e) {
            LOG.error("Failed to threshold on {} for {} because of an exception", service, dsName, e);
        }
    }

    private CollectionSet getCollectionSet(MonitoredService service, Map<String, Number> entries, String rrdBaseName) {
        // When making calls directly to RrdUtils#createRrd() and RrdUtils#updateRrd(),
        // the behavior was as follows:
        // 1) All samples get written to response/${ipAddr}/${rrdBaseName}.rrd
        //     This happens whether or not storeByGroup is enabled.
        // 2) If multiple entries are present, the DSs are created in the same order that they
        //    appear in the map

        String ifLabel = "";
        Map<String, String> ifInfo = new HashMap<>();
        if (m_ifLabelDao != null) {
            ifLabel = m_ifLabelDao.getIfLabel(service.getNodeId(), InetAddressUtils.addr(service.getIpAddr()));
            if (ifLabel != null) {
                ifInfo.putAll(m_ifLabelDao.getInterfaceInfoFromIfLabel(service.getNodeId(), ifLabel));
            }
        }
        LatencyCollectionResource latencyResource = new LatencyCollectionResource(service.getSvcName(), service.getIpAddr(), service.getNodeLocation(), ifLabel, ifInfo);
        for (final Entry<String, Number> entry : entries.entrySet()) {
            final String ds = entry.getKey();
            final Number value = entry.getValue() != null ? entry.getValue() : Double.NaN;
            LatencyCollectionAttributeType latencyType = new LatencyCollectionAttributeType(rrdBaseName, ds);
            latencyResource.addAttribute(new LatencyCollectionAttribute(latencyResource, latencyType, ds, value.doubleValue()));
        }

        SingleResourceCollectionSet collectionSet = new SingleResourceCollectionSet(latencyResource, new Date());
        collectionSet.setStatus(CollectionStatus.SUCCEEDED);

        return collectionSet;
    }

}
