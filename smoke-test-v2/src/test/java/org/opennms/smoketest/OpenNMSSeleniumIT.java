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

package org.opennms.smoketest;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.containers.PostgreSQLContainer;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Network;

/**
 * Base class for Selenium based testing of the OpenNMS web application.
 */
public class OpenNMSSeleniumIT extends AbstractOpenNMSSeleniumHelper {

    public static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    public static final OpenNMSContainer opennmsContainer = new OpenNMSContainer();

    public static final BrowserWebDriverContainer firefox = (BrowserWebDriverContainer) new BrowserWebDriverContainer()
            .withCapabilities(new FirefoxOptions())
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, new File("target"))
            .withNetwork(Network.SHARED);

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(postgreSQLContainer)
            .around(opennmsContainer)
            .around(firefox);

    public static RemoteWebDriver driver;

    @BeforeClass
    public static void setUpClass() {
        driver = firefox.getWebDriver();
    }

    @Override
    protected WebDriver getDriver() {
        return driver;
    }

    @Override
    protected String getBaseUrlInternal() {
        return opennmsContainer.getBaseUrlInternal().toString();
    }

    @Override
    protected String getBaseUrlExternal() {
        return opennmsContainer.getBaseUrlExternal().toString();
    }
}
