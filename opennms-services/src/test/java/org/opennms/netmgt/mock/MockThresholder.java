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

package org.opennms.netmgt.mock;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.threshd.ThresholdInitializationException;
import org.opennms.netmgt.threshd.ThresholdingVisitor;

public class MockThresholder implements ThresholdingVisitor {

    @Override
    public void visitCollectionSet(CollectionSet set) {
    }

    @Override
    public void visitResource(CollectionResource resource) {
    }

    @Override
    public void visitGroup(AttributeGroup group) {
    }

    @Override
    public void visitAttribute(CollectionAttribute attribute) {
    }

    @Override
    public void completeAttribute(CollectionAttribute attribute) {
    }

    @Override
    public void completeGroup(AttributeGroup group) {
    }

    @Override
    public void completeResource(CollectionResource resource) {
    }

    @Override
    public void completeCollectionSet(CollectionSet set) {
    }

    @Override
    public void setCounterReset(boolean counterReset) {
    }

    @Override
    public boolean hasThresholds() {
        return false;
    }

    /*    @Override
    public List<String> getScheduledOutages() {
        return Collections.emptyList();
    }*/

    @Override
    public void reload() {
        // no-op
    }

    /*
    @Override
    public void reloadScheduledOutages() throws ThresholdInitializationException {
    }
    */
    @Override
    public boolean isNodeInOutage() {
        return false;
    }

    @Override
    public Date getCollectionTimestamp() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getScheduledOutages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reloadScheduledOutages() throws ThresholdInitializationException {
        // TODO Auto-generated method stub

    }

    /*    @Override
    public Date getCollectionTimestamp() {
        return null;
    }*/
}
