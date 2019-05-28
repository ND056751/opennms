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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * This class is used to help debug and develop Selenium based tests.
 *
 * To use it, run the main() method in one session, and update your test to
 * extend this class temporarily.
 *
 * @author jwhite
 */
public class OpenNMSSeleniumDebugIT extends AbstractOpenNMSSeleniumHelper {

    private final String opennmsWebUrl;
    public final RemoteWebDriver driver;

    public OpenNMSSeleniumDebugIT(String webDriverUrl, String opennmsWebUrl) {
        this.opennmsWebUrl = Objects.requireNonNull(opennmsWebUrl);
        Objects.requireNonNull(webDriverUrl);
        try {
            driver = new RemoteWebDriver(new URL(webDriverUrl), OpenNMSSeleniumIT.getFirefoxOptions());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected WebDriver getDriver() {
        return driver;
    }

    @Override
    protected String getBaseUrlInternal() {
        return OpenNMSContainer.getBaseUrlInternal().toString();
    }

    @Override
    protected String getBaseUrlExternal() {
        return opennmsWebUrl;
    }

    public static class DebugIT extends OpenNMSSeleniumIT {
        @Test
        public void canDebug() throws InterruptedException {
            System.out.printf("\n\nWeb driver is available at: %s\n", firefox.getSeleniumAddress());
            System.out.printf("OpenNMS is available at: %s\n", opennmsContainer.getBaseUrlExternal());
            Thread.sleep(TimeUnit.HOURS.toMillis(8));
        }
    }

    public static void main(String... args) {
        JUnitCore.runClasses(DebugIT.class);
    }
}
