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

package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.containers.MinionContainer;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.containers.PostgreSQLContainer;
import org.opennms.smoketest.utils.CommandTestUtils;
import org.opennms.smoketest.utils.OpenNMSRestClient;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;


public class RpcOverKafkaIT {
    private static final Logger LOG = LoggerFactory.getLogger(RpcOverKafkaIT.class);

    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static final KafkaContainer kafka = new KafkaContainer()
            .withNetwork(Network.SHARED);

    private static final OpenNMSContainer opennmsContainer = new OpenNMSContainer();

    private static final MinionContainer minionContainer = new MinionContainer();

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(postgreSQLContainer)
            .around(kafka)
            .around(opennmsContainer)
            .around(minionContainer);

    private static final String LOCALHOST = "127.0.0.1";

            /* kafka-rpc.properties
        # Use Kafka strategy replacing default camel
org.opennms.core.ipc.rpc.strategy=kafka
         */

    /* kafka-rpc.boot
#Disable JMS and enable Kafka
!opennms-core-ipc-rpc-jms
opennms-core-ipc-rpc-kafka
     */

    @Test
    public void verifyKafkaRpcWithIcmpServiceDetection() {
        // Add node and interface with minion location.
        addRequisition(opennmsContainer.getRestClient(), "MINION", LOCALHOST);
        await().atMost(3, MINUTES).pollInterval(15, SECONDS)
                .until(this::detectIcmpAtLocationMinion, containsString("'ICMP' WAS detected on 127.0.0.1"));
    }

    private String detectIcmpAtLocationMinion() throws Exception {
        try (final SshClient sshClient = new SshClient(opennmsContainer.getSshAddress(), "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("detect -l MINION ICMP 127.0.0.1");
            pipe.println("logout");
            await().atMost(90, SECONDS).until(sshClient.isShellClosedCallable());
            String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
            shellOutput = StringUtils.substringAfter(shellOutput, "detect -l MINION ICMP 127.0.0.1");
            LOG.info("Detect output: {}", shellOutput);
            return shellOutput;
        }
    }

    @Test
    public void verifyKafkaRpcWithJdbcServiceDetection() {
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
                .until(this::detectJdbcAtLocationMinion, containsString("'JDBC' WAS detected"));
    }

    private String detectJdbcAtLocationMinion() throws Exception {
        // Retrieve Postgres address and add form a URL
        String jdbcUrl = String.format("url=jdbc:postgresql://%s:5432/opennms", OpenNMSContainer.DB_ALIAS);
        try (final SshClient sshClient = new SshClient(opennmsContainer.getSshAddress(), "admin", "admin")) {
            // Perform JDBC service detection on Minion
            final PrintStream pipe = sshClient.openShell();
            pipe.println("detect -l MINION JDBC 127.0.0.1 " + jdbcUrl);
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
            // Sanitize the output
            String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
            shellOutput = StringUtils.substringAfter(shellOutput, "detect -l MINION JDBC");
            LOG.info("Detect output: {}", shellOutput);
            return shellOutput;
        }
    }

    public static void addRequisition(OpenNMSRestClient client, String location, String ipAddress) {
        Requisition requisition = new Requisition("foreignSource");
        List<RequisitionInterface> interfaces = new ArrayList<>();
        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr(ipAddress);
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        interfaces.add(requisitionInterface);
        RequisitionNode node = new RequisitionNode();
        node.setNodeLabel(ipAddress);
        node.setLocation(location);
        node.setInterfaces(interfaces);
        node.setForeignId("foreignId");
        requisition.insertNode(node);

        client.addOrReplaceRequisition(requisition);
        client.importRequisition("foreignSource");
    }

}
