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
import java.util.Collections;
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

    // No statistics for WLS internal components
    private static final List<String> EXCLUSIONS = Collections.unmodifiableList(Arrays.asList(
            "_async",
            "bea_wls_deployment_internal",
            "bea_wls_cluster_internal",
            "bea_wls_diagnostics",
            "bea_wls_internal",
            "console",
            "consolehelp",
            "uddi",
            "uddiexplorer"));

    public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
        Result result = new Result();
        List<String> output = new ArrayList<String>();     
        int code = 0;

        /**
         * Specific test variables
         */
        Map<String,String> components = new HashMap<String,String>();
        List<String> alerts = new ArrayList<String>();
        String[] thresholdsArray;

        /**
         * Populate the HashMap with context root keys
         * and string values like 'warning;critical'
         */
        String[] paramsArray = params.split("\\|");
        for (String param : paramsArray) {
            String[] componentsArray = param.split(";", 2);
            components.put(componentsArray[0], componentsArray[1]);
        }

        try {
            ObjectName[] applicationRuntimeMbeans = (ObjectName[])connection.getAttribute(serverRuntimeMbean, "ApplicationRuntimes");
            for (ObjectName applicationRuntime : applicationRuntimeMbeans) {
                ObjectName[] componentRuntimeMbeans = (ObjectName[])connection.getAttribute(applicationRuntime, "ComponentRuntimes");
                for (ObjectName componentRuntime : componentRuntimeMbeans) {
                    String contextRoot;
                    try {
                        contextRoot = connection.getAttribute(componentRuntime, "ContextRoot").toString();
                        // The context root may be an empty string or a single character
                        if (contextRoot.length() > 1)
                            contextRoot = contextRoot.substring(1);
                    } catch (AttributeNotFoundException ignored) {
                        // Our component is not an instance of WebAppComponentRuntimeMBean
                        continue;
                    }
                    if (EXCLUSIONS.contains(contextRoot))
                        continue;
                    if (components.containsKey("*") || components.containsKey(contextRoot)) {
                        long openSessions = Long.parseLong(connection.getAttribute(componentRuntime, "OpenSessionsCurrentCount").toString());
                        output.add("app-" + contextRoot + "=" + openSessions);
                        if (components.containsKey("*"))
                            thresholdsArray = components.get("*").split(";");
                        else
                            thresholdsArray = components.get(contextRoot).split(";");
                        long warning = Long.parseLong(thresholdsArray[0]);
                        long critical = Long.parseLong(thresholdsArray[1]);
                        int testCode = checkResult(openSessions, critical, warning);
                        if (testCode == Status.WARNING.getCode() || testCode == Status.CRITICAL.getCode())
                            alerts.add(contextRoot + " (" + openSessions + ")");
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
            result.setMessage("HTTP session count: " + sb.toString());
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
