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

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.wait4it.wlsagent.utils.Result;
import net.wait4it.wlsagent.utils.Status;

/**
 * @author Yann Lambret
 *
 */
public class ComponentTest extends TestUtils implements Test {

	private static final String MESSAGE = " component test ";

	public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
		Result result = new Result();
		StringBuilder output = new StringBuilder();
		Integer code = 0;

		/**
		 * Specific test variables
		 */
		Map<String,String> components = new HashMap<String,String>();
		ObjectName applicationRuntimeMbeans[] = null;

		/**
		 * Populate the HashMap with ContextRoot keys
		 * and string values like 'warning;critical'
		 */
		String[] paramsArray = params.split("\\|");
		for (int i = 0; i < paramsArray.length; i++) {
			String[] componentsArray = (paramsArray[i]).split(";", 2);
			components.put(componentsArray[0], componentsArray[1]);
		}

		try {
			applicationRuntimeMbeans = (ObjectName[])connection.getAttribute(serverRuntimeMbean, "ApplicationRuntimes");
			for (ObjectName applicationRuntime:applicationRuntimeMbeans) {
				ObjectName componentRuntimeMbeans[] = (ObjectName[])connection.getAttribute(applicationRuntime, "ComponentRuntimes");
				for (ObjectName componentRuntime:componentRuntimeMbeans) {
					String contextRoot = null;
					try {
						contextRoot = connection.getAttribute(componentRuntime, "ContextRoot").toString().substring(1);
					} catch (AttributeNotFoundException e) {
						/**
						 * Our component is not an instance of WebAppComponentRuntimeMBean.
						 */
						continue;
					}
					if (components.containsKey(contextRoot)) {
						Long openSessions = Long.parseLong(connection.getAttribute(componentRuntime, "OpenSessionsCurrentCount").toString());
						output.append("app-" + contextRoot + "=" + openSessions + " ");
						String[] thresholdsArray = components.get(contextRoot).split(";");
						Long warning = Long.parseLong(thresholdsArray[0]);
						Long critical = Long.parseLong(thresholdsArray[1]);
						code = checkResult(openSessions, critical, warning, code);
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
