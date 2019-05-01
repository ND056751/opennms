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

import java.util.Map;

import org.opennms.netmgt.dao.api.IfLabel;

/**
 * <p>LatencyThresholdingSet class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public interface LatencyThresholdingSet extends ThresholdingSet {

    /*
     * Latency thresholds use ds-type="if"
     * Returns true if any attribute of the service is involved in any of defined thresholds.
     */
    /**
     * <p>hasThresholds</p>
     *
     * @param attributes a {@link java.util.Map} object.
     * @return a boolean.
     */
    public boolean hasThresholds(Map<String, Double> attributes);

    /*
     * Apply thresholds definitions for specified service using attributesMap as current values.
     * Thresholding Service will send events if some thresholds are triggered or rearmed.
     */
    public void applyThresholds(String svcName, Map<String, Double> attributes, IfLabel ifLabelDao);

}
