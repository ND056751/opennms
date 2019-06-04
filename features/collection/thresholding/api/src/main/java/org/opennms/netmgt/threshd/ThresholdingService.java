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

import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * Thresholding API Service.
 */
public interface ThresholdingService {

    // TODO - should getSession throw ThresholdInitializationException??

    // TODO - JavaDoc -- TODO - finalize parameters/types

    /**
     * Create a {@link ThresholdingSession} for Thresholding on {@link CollectionSet}s.
     * 
     * @param m_nodeId
     * @param hostAddress
     * @param serviceName
     * @param m_repository
     * @param m_params
     * @return
     * @throws ThresholdInitializationException
     *             if there is an error initializing Thresholding.
     */
    ThresholdingSession createSession(int m_nodeId, String hostAddress, String serviceName, RrdRepository rrdRepository, ServiceParameters serviceParameters)
            throws ThresholdInitializationException;

}