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

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.hibernate.IfLabelDaoImpl;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * These tests were removed from {@link ThresholdingVisitorTest} because they
 * require Spring context initialization in order to have a working copy of
 * {@link IfLabelDaoImpl} which is used while storing latency information. 
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 *
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-eventUtil.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
public class ThresholdingSessionIT implements TemporaryDatabaseAware<MockDatabase> {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingSessionIT.class);

    private MockDatabase mockDb;

    @Autowired
    private JdbcTemplate m_jdbcTemplate;

    @Autowired
    private ApplicationContext m_context;

    @Autowired
    private ThresholdingService service;

    private int nodeId;

    private String ifFace;

    private String location;

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        mockDb = database;
    }

    @Before
    public void setup() {
        nodeId = 1;
        ifFace = "mockIfFace";
        location = "mockLocation";
    }
    @Test
    public void canLoadServiceContext() throws ThresholdInitializationException {
        ServiceParameters serviceParams = new ServiceParameters(new HashMap<>());
        ThresholdingSession visitor = service.createSession(nodeId, location, ifFace, null, serviceParams);
        assertNotNull("Failed  to instantiate ThresholdingVisitor", visitor);
    }

}