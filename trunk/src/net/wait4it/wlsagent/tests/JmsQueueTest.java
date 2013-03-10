/**
 * This file is part of Wlsagent.
 *
 * Wlsagent is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wlsagent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wlsagent. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.wait4it.wlsagent.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import net.wait4it.nagios.wlsagent.core.Result;
import net.wait4it.nagios.wlsagent.core.Status;
import net.wait4it.nagios.wlsagent.core.WLSProxy;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class JmsQueueTest extends TestUtils implements Test {

    public Result run(WLSProxy proxy, String params) {
        // Test result
        Result result = new Result();

        // Test performance data
        List<String> output = new ArrayList<String>();

        // Test specific messages
        List<String> message = new ArrayList<String>();

        // Test overall status code
        int code = 0;

        // Test thresholds
        long warning;
        long critical;

        Map<String,String> destinations = new HashMap<String,String>();
        String thresholds = "";

        // Test code for a specific queue
        int testCode = 0;

        // Message prefix
        String prefix = "JMS message count: ";

        // Performance data
        String destinationName;
        long messagesCurrentCount;
        long messagesPendingCount;

        // Parses HTTP query params
        for (String s : Arrays.asList(params.split("\\|"))) {
            destinations.put(s.split(",", 2)[0], s.split(",", 2)[1]);
        }

        try {
            ObjectName jmsRuntimeMbean = proxy.getMBean("JMSRuntime");
            ObjectName[] jmsServerRuntimeMbeans = proxy.getMBeans(jmsRuntimeMbean, "JMSServers");
            for (ObjectName jmsServerRuntime : jmsServerRuntimeMbeans) {
                ObjectName[] jmsDestinationRuntimeMbeans = proxy.getMBeans(jmsServerRuntime, "Destinations");
                for (ObjectName jmsDestinationRuntime : jmsDestinationRuntimeMbeans) {
                    destinationName = (String)proxy.getAttribute(jmsDestinationRuntime, "Name");
                    if (destinationName.split("@").length == 2) {
                        destinationName = destinationName.split("@")[1];
                    }
                    if (destinationName.split("!").length == 2) {
                        destinationName = destinationName.split("!")[1];
                    }
                    if (destinations.containsKey("*") || destinations.containsKey(destinationName)) {
                        messagesCurrentCount = (Long)proxy.getAttribute(jmsDestinationRuntime, "MessagesCurrentCount");
                        messagesPendingCount = (Long)proxy.getAttribute(jmsDestinationRuntime, "MessagesPendingCount");
                        StringBuilder out = new StringBuilder();
                        out.append("JmsQueue-").append(destinationName).append("-current=").append(messagesCurrentCount).append(" ");
                        out.append("JmsQueue-").append(destinationName).append("-pending=").append(messagesPendingCount);
                        output.add(out.toString());
                        thresholds = destinations.get("*") != null ? destinations.get("*") : destinations.get(destinationName);
                        warning = Long.parseLong(thresholds.split(",")[0]);
                        critical = Long.parseLong(thresholds.split(",")[1]);
                        testCode = checkResult(messagesCurrentCount, critical, warning);
                        if (testCode == Status.WARNING.getCode() || testCode == Status.CRITICAL.getCode()) {
                            message.add(destinationName + " (" + messagesCurrentCount + ")");
                            code = (testCode > code) ? testCode : code;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(Status.UNKNOWN);
            result.setMessage(e.toString());
            return result;
        }

        for (Status status : Status.values()) {
            if (code == status.getCode()) {
                result.setStatus(status);           
                break;
            }
        }

        result.setOutput(formatOut(output));
        result.setMessage(formatMsg(prefix, message));

        return result;
    }

}
