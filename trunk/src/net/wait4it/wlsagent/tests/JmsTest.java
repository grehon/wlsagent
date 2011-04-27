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

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.wait4it.wlsagent.utils.Result;
import net.wait4it.wlsagent.utils.Status;

/**
 * @author Yann Lambret
 *
 */
public class JmsTest extends TestUtils implements Test {

	private static final String MESSAGE = " jms test ";

	public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
		Result result = new Result();
		StringBuilder output = new StringBuilder();
		Integer code = 0;

		/**
		 * Specific test variables
		 */
		Map<String,String> destinations = new HashMap<String,String>();
		ObjectName jmsRuntimeMbean = null;
		ObjectName jmsServerRuntimeMbeans[] = null;

		/**
		 * Populate the HashMap with jms destinations name
		 * keys and string values like 'warning;critical'
		 */
		String[] paramsArray = params.split("\\|");
		for (int i = 0; i < paramsArray.length; i++) {
			String[] destinationsArray = (paramsArray[i]).split(";", 2);
			destinations.put(destinationsArray[0], destinationsArray[1]);
		}

		try {
			jmsRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "JMSRuntime");
			jmsServerRuntimeMbeans = (ObjectName[])connection.getAttribute(jmsRuntimeMbean, "JMSServers");
			for (ObjectName jmsServerRuntime : jmsServerRuntimeMbeans) {
				ObjectName jmsDestinationRuntimeMbeans[] = (ObjectName[])connection.getAttribute(jmsServerRuntime, "Destinations");
				for (ObjectName jmsDestinationRuntime : jmsDestinationRuntimeMbeans) {
					String destinationName = connection.getAttribute(jmsDestinationRuntime, "Name").toString().split("@")[1];
					if (destinations.containsKey(destinationName)) {
						Long messagesCurrentCount = Long.parseLong(connection.getAttribute(jmsDestinationRuntime, "MessagesCurrentCount").toString());
						Long messagesPendingCount = Long.parseLong(connection.getAttribute(jmsDestinationRuntime, "MessagesPendingCount").toString());
						output.append("jms-" + destinationName + "-current" + "=" + messagesCurrentCount + " ");
						output.append("jms-" + destinationName + "-pending" + "=" + messagesPendingCount + " ");
						String[] thresholdsArray = destinations.get(destinationName).split(";");
						Long warning = Long.parseLong(thresholdsArray[0]);
						Long critical = Long.parseLong(thresholdsArray[1]);
						code = checkResult(messagesCurrentCount, critical, warning, code);
					}
				}
			}
		} catch (Exception e) {
			result.setStatus(Status.UNKNOWN);
			result.setMessage(Status.UNKNOWN.getMessage(MESSAGE));
			return result;
		}

		for (Status status : Status.values()) {
			if (code == status.getCode()) {
				result.setStatus(status);
				result.setMessage(status.getMessage(MESSAGE));
				result.setOutput(output.toString());
			}
		}

		return result;
	}

}
