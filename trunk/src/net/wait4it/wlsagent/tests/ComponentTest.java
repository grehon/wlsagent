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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.wait4it.wlsagent.utils.Result;
import net.wait4it.wlsagent.utils.Status;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class ComponentTest extends TestUtils implements Test {

	private static final String MESSAGE = " component test ";
	
	// No statistics for WLS internal components
	private static final List<String> EXCLUSIONS = new ArrayList<String>(9);

	static {
		EXCLUSIONS.add("_async");
		EXCLUSIONS.add("bea_wls_deployment_internal");
		EXCLUSIONS.add("bea_wls_cluster_internal");
		EXCLUSIONS.add("bea_wls_diagnostics");
		EXCLUSIONS.add("bea_wls_internal");
		EXCLUSIONS.add("console");
		EXCLUSIONS.add("consolehelp");
		EXCLUSIONS.add("uddi");
		EXCLUSIONS.add("uddiexplorer");
	}

    public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
		Result result = new Result();
		StringBuilder output = new StringBuilder(100);
		int code = 0;

		/**
		 * Specific test variables
		 */
		Map<String,String> components = new HashMap<String,String>(16);
		ObjectName[] applicationRuntimeMbeans;
		String[] thresholdsArray;

		/**
		 * Populate the HashMap with ContextRoot keys
		 * and string values like 'warning;critical'
		 */
		String[] paramsArray = PIPE_PATTERN.split(params);
        for (String param : paramsArray) {
            String[] componentsArray = SEMICOLON_PATTERN.split(param, 2);
            components.put(componentsArray[0], componentsArray[1]);
        }

		try {
			applicationRuntimeMbeans = (ObjectName[])connection.getAttribute(serverRuntimeMbean, "ApplicationRuntimes");
			for (ObjectName applicationRuntime : applicationRuntimeMbeans) {
				ObjectName[] componentRuntimeMbeans = (ObjectName[])connection.getAttribute(applicationRuntime, "ComponentRuntimes");
				for (ObjectName componentRuntime : componentRuntimeMbeans) {
					String contextRoot;
					try {
						contextRoot = connection.getAttribute(componentRuntime, "ContextRoot").toString();
                        // the context root may be an empty string or a single character
                        if (contextRoot.length() > 1) {
                            contextRoot = contextRoot.substring(1);
                        }
					} catch (AttributeNotFoundException ignored) {
						/**
						 * Our component is not an instance of WebAppComponentRuntimeMBean.
						 */
						continue;
					}
					if (EXCLUSIONS.contains(contextRoot))
						continue;
					if (components.containsKey("*") || components.containsKey(contextRoot)) {
						long openSessions = Long.parseLong(connection.getAttribute(componentRuntime, "OpenSessionsCurrentCount").toString());
                        output.append("app-").append(contextRoot).append("=").append(openSessions).append(" ");

						if (components.containsKey("*"))
							thresholdsArray = SEMICOLON_PATTERN.split(components.get("*"));
                        else
                            thresholdsArray = SEMICOLON_PATTERN.split(components.get(contextRoot));

						long warning = Long.parseLong(thresholdsArray[0]);
						long critical = Long.parseLong(thresholdsArray[1]);
						code = checkResult(openSessions, critical, warning, code);
                        if (code == Status.CRITICAL.getCode() || code == Status.WARNING.getCode()) {
                            result.setMessage("Open Sessions (" + contextRoot + ") = " + openSessions);
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
                if (null == result.getMessage() || result.getMessage().length() == 0) {
				    result.setMessage(status.getMessage(MESSAGE));
                }
				result.setOutput(output.toString());
                break;
			}
		}

		return result;
	}

}
