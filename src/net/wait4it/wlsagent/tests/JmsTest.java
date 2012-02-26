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

import java.util.*;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.wait4it.wlsagent.utils.Result;
import net.wait4it.wlsagent.utils.Status;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class JmsTest extends TestUtils implements Test {

	public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
		Result result = new Result();
		List<String> output = new ArrayList<String>(5);
		List<String> alerts = new ArrayList<String>(5);
		int code = 0;

		/**
		 * Specific test variables
		 */
		Map<String,String> destinations = new HashMap<String,String>(16);
		ObjectName jmsRuntimeMbean;
		ObjectName[] jmsServerRuntimeMbeans;

		/**
		 * Populate the HashMap with jms destinations name
		 * keys and string values like 'warning;critical'
		 */
		String[] paramsArray = PIPE_PATTERN.split(params);
		for (String param : paramsArray) {
			String[] destinationsArray = SEMICOLON_PATTERN.split(param, 2);
			destinations.put(destinationsArray[0], destinationsArray[1]);
		}

		try {
			jmsRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "JMSRuntime");
			jmsServerRuntimeMbeans = (ObjectName[])connection.getAttribute(jmsRuntimeMbean, "JMSServers");
			for (ObjectName jmsServerRuntime : jmsServerRuntimeMbeans) {
				ObjectName[] jmsDestinationRuntimeMbeans = (ObjectName[])connection.getAttribute(jmsServerRuntime, "Destinations");
				for (ObjectName jmsDestinationRuntime : jmsDestinationRuntimeMbeans) {
					String destinationName = AT_PATTERN.split(connection.getAttribute(jmsDestinationRuntime, "Name").toString())[1];
					if (destinations.containsKey(destinationName)) {
						long messagesCurrentCount = Long.parseLong(connection.getAttribute(jmsDestinationRuntime, "MessagesCurrentCount").toString());
						long messagesPendingCount = Long.parseLong(connection.getAttribute(jmsDestinationRuntime, "MessagesPendingCount").toString());

						StringBuilder out = new StringBuilder(256);
						out.append("jms-").append(destinationName).append("-current" + "=").append(messagesCurrentCount).append(' ');
						out.append("jms-").append(destinationName).append("-pending" + "=").append(messagesPendingCount);
						output.add(out.toString());

						String[] thresholdsArray = SEMICOLON_PATTERN.split(destinations.get(destinationName));
						long warning = Long.parseLong(thresholdsArray[0]);
						long critical = Long.parseLong(thresholdsArray[1]);
						code = checkResult(messagesCurrentCount, critical, warning, code);
						if (code == Status.CRITICAL.getCode() || code == Status.WARNING.getCode()) {
							alerts.add(destinationName + " (" + messagesCurrentCount + ")");
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

		if (! alerts.isEmpty()) {
			Collections.sort(alerts);
			StringBuilder sb = new StringBuilder(alerts.remove(0));
			while(! alerts.isEmpty())
				sb.append(", ").append(alerts.remove(0));
			result.setMessage("JMS message count: " + sb.toString());
		}

		for (Status status : Status.values()) {
			if (code == status.getCode()) {
				result.setStatus(status);
				Collections.sort(output);
				StringBuilder out = new StringBuilder(256);
				for (String o : output) {
					if (out.length() > 0) {
						out.append(' ');
					}
					out.append(o);
				}
				result.setOutput(out.toString());
				break;
			}
		}

		return result;
	}

}
