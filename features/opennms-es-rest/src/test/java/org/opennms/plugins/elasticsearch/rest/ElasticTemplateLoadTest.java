/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.opennms.plugins.elasticsearch.rest.template.DefaultTemplateLoader;
import org.opennms.plugins.elasticsearch.rest.template.Version;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ElasticTemplateLoadTest {

    @Test
    public void loadTemplate() throws IOException {
        DefaultTemplateLoader defaultTemplateLoader = new DefaultTemplateLoader();

        String template = defaultTemplateLoader.load(new Version(6, 4, 1),
                                                     "/eventsIndexTemplate");

        final JsonElement json = new JsonParser().parse(template);
        if (!json.isJsonObject()) {
            throw new IllegalArgumentException("Provided template is not a valid json object");
        }
        JsonObject templateJson = json.getAsJsonObject();
        assertNotNull(templateJson);
        assertFalse(templateJson.toString().isEmpty());
    }

}
