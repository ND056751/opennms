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

package org.opennms.smoketest.containers;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.awaitility.core.ConditionTimeoutException;
import org.opennms.smoketest.utils.OpenNMSRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.SelinuxContext;

public class OpenNMSContainer extends GenericContainer {
    public static final String DB_ALIAS = "db";
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSContainer.class);
    private static final int OPENNMS_WEB_PORT = 8980;
    private static final int OPENNMS_SSH_PORT = 8101;
    static final String ALIAS = "opennms";

    public OpenNMSContainer() {
        super("horizon");
        withExposedPorts(OPENNMS_WEB_PORT, OPENNMS_SSH_PORT)
                .withEnv("POSTGRES_HOST", DB_ALIAS)
                .withEnv("POSTGRES_PORT", Integer.toString(PostgreSQLContainer.POSTGRESQL_PORT))
                // User/pass are hardcoded in PostgreSQLContainer but are not exposed
                .withEnv("POSTGRES_USER", "test")
                .withEnv("POSTGRES_PASSWORD", "test")
                .withEnv("OPENNMS_DBNAME", "opennms")
                .withEnv("OPENNMS_DBUSER", "opennms")
                .withEnv("OPENNMS_DBPASS", "opennms")
                .withEnv("KARAF_FEATURES", "producer")
                .withEnv("JAVA_OPTS", "-Djava.security.egd=file:/dev/./urandom")
                .withClasspathResourceMapping("opennms-overlay", "/opt/opennms-overlay", BindMode.READ_ONLY, SelinuxContext.SINGLE)
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                .withCommand("-s")
                .waitingFor(new WaitForOpenNMS(this));
    }

    /**
     * @return the URL in a form consumable by containers networked with this one using the alias and internal port
     */
    public static URL getBaseUrlInternal() {
        try {
            return new URL(String.format("http://%s:%d/", ALIAS, OPENNMS_WEB_PORT));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the URL in a form consumable by the host using the mapped port
     */
    public URL getBaseUrlExternal() {
        try {
            return new URL(String.format("http://%s:%d/", getContainerIpAddress(), getMappedPort(OPENNMS_WEB_PORT)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public OpenNMSRestClient getRestClient() {
        try {
            return new OpenNMSRestClient(new URL(getBaseUrlExternal() + "opennms"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getWebPort() {
        return getMappedPort(OPENNMS_WEB_PORT);
    }

    private static class WaitForOpenNMS extends org.testcontainers.containers.wait.strategy.AbstractWaitStrategy {
        private final OpenNMSContainer openNMSContainer;

        public WaitForOpenNMS(OpenNMSContainer openNMSContainer) {
            this.openNMSContainer = Objects.requireNonNull(openNMSContainer);
        }

        @Override
        protected void waitUntilReady() {
            LOG.info("Waiting for OpenNMS...");
            final long timeoutMins = 5;
            final OpenNMSRestClient nmsRestClient = openNMSContainer.getRestClient();
            try {
                await().atMost(timeoutMins, MINUTES)
                        .pollInterval(5, SECONDS).pollDelay(0, SECONDS)
                        .ignoreExceptions()
                        .until(nmsRestClient::getDisplayVersion, notNullValue());
            } catch(ConditionTimeoutException e) {
                LOG.error("OpenNMS did not finish starting after {} minutes. Gathering logs.", timeoutMins);
                copyLogs(openNMSContainer);
                throw new RuntimeException(e);
            }
            LOG.info("OpenNMS is ready");
        }
    }

    private static void copyLogs(OpenNMSContainer container) {
        // List of known log files we expect to find in the container
        final List<String> logFiles = Arrays.asList("eventd.log",
                "jetty-server.log",
                "karaf.log",
                "manager.log",
                "web.log");
        final Path sourceLogFolder = Paths.get("/opt", "opennms", "logs");
        final Path targetLogFolder = Paths.get("target", "logs", "opennms");
        try {
            Files.createDirectories(targetLogFolder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create " + targetLogFolder, e);
        }
        for (String logFile : logFiles) {
            try {
                container.copyFileFromContainer(sourceLogFolder.resolve(logFile).toString(),
                        targetLogFolder.resolve(logFile).toString());
            } catch (Exception e) {
                // Some log files may not exist, just log a message if copying any of these fails
                LOG.info("Failed to copy log file {} from container: {}", logFile, e.getMessage());
            }

        }
    }
}
