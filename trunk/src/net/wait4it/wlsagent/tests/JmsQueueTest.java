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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.wait4it.wlsagent.utils.Result;
import net.wait4it.wlsagent.utils.Status;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class JmsQueueTest extends TestUtils implements Test {

    public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
        Result result = new Result();
        List<String> output = new ArrayList<String>();      
        int code = 0;

        /**
         * Specific test variables
         */
        Map<String,String> destinations = new HashMap<String,String>();
        List<String> alerts = new ArrayList<String>();
        String[] thresholdsArray;

        /**
         * Populate the HashMap with JMS destination name
         * keys and string values like 'warning;critical'
         */
        String[] paramsArray = params.split("\\|");
        for (String param : paramsArray) {
            String[] destinationsArray = param.split(";", 2);
            destinations.put(destinationsArray[0], destinationsArray[1]);
        }

        try {
            ObjectName jmsRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "JMSRuntime");
            ObjectName[] jmsServerRuntimeMbeans = (ObjectName[])connection.getAttribute(jmsRuntimeMbean, "JMSServers");
            for (ObjectName jmsServerRuntime : jmsServerRuntimeMbeans) {
                ObjectName[] jmsDestinationRuntimeMbeans = (ObjectName[])connection.getAttribute(jmsServerRuntime, "Destinations");
                for (ObjectName jmsDestinationRuntime : jmsDestinationRuntimeMbeans) {
                    String destinationName = connection.getAttribute(jmsDestinationRuntime, "Name").toString();
                    if (destinationName.split("@").length == 2)
                        destinationName = destinationName.split("@")[1];
                    if (destinationName.split("!").length == 2)
                        destinationName = destinationName.split("!")[1];
                    if (destinations.containsKey("*") || destinations.containsKey(destinationName)) {
                        long messagesCurrentCount = Long.parseLong(connection.getAttribute(jmsDestinationRuntime, "MessagesCurrentCount").toString());
                        long messagesPendingCount = Long.parseLong(connection.getAttribute(jmsDestinationRuntime, "MessagesPendingCount").toString());
                        StringBuilder out = new StringBuilder();
                        out.append("JmsQueue-").append(destinationName).append("-current=").append(messagesCurrentCount).append(" ");
                        out.append("JmsQueue-").append(destinationName).append("-pending=").append(messagesPendingCount);
                        output.add(out.toString());
                        if (destinations.containsKey("*"))
                            thresholdsArray = destinations.get("*").split(";");
                        else
                            thresholdsArray = destinations.get(destinationName).split(";");
                        long warning = Long.parseLong(thresholdsArray[0]);
                        long critical = Long.parseLong(thresholdsArray[1]);
                        int testCode = checkResult(messagesCurrentCount, critical, warning);
                        if (testCode == Status.WARNING.getCode() || testCode == Status.CRITICAL.getCode())
                            alerts.add(destinationName + " (" + messagesCurrentCount + ")");
                        if (testCode > code)
                            code = testCode;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(Status.UNKNOWN);
            result.setMessage(e.toString());
            return result;
        }

        // Set result status
        for (Status status : Status.values()) {
            if (code == status.getCode()) {
                result.setStatus(status);           
                break;
            }
        }

        // Set result message
        if (! alerts.isEmpty()) {
            Collections.sort(alerts);
            StringBuilder sb = new StringBuilder(alerts.remove(0));
            while(! alerts.isEmpty())
                sb.append(", ").append(alerts.remove(0));
            result.setMessage("JMS message count: " + sb.toString());
        }

        // Set result output
        if (! output.isEmpty()) {
            Collections.sort(output);
            StringBuilder sb = new StringBuilder(output.remove(0));
            while(! output.isEmpty())
                sb.append(" ").append(output.remove(0));
            result.setOutput(sb.toString());
        }

        return result;
    }

}
