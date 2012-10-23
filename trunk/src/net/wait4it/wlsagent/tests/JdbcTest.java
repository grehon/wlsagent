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
public class JdbcTest extends TestUtils implements Test {

    public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
        Result result = new Result();
        List<String> output = new ArrayList<String>();
        List<String> alerts = new ArrayList<String>();
        int code = 0;

        /**
         * Specific test variables
         */
        Map<String,String> datasources = new HashMap<String,String>();
        String[] thresholdsArray;

        /**
         * Populate the HashMap with datasource name keys
         * and string values like 'warning;critical'
         */
        String[] paramsArray = PIPE_PATTERN.split(params);
        for (String param : paramsArray) {
            String[] datasourcesArray = SEMICOLON_PATTERN.split(param, 2);
            datasources.put(datasourcesArray[0], datasourcesArray[1]);
        }

        try {
            ObjectName jdbcServiceRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "JDBCServiceRuntime");
            ObjectName[] jdbcDataSourceRuntimeMbeans = (ObjectName[])connection.getAttribute(jdbcServiceRuntimeMbean, "JDBCDataSourceRuntimeMBeans");
            for (ObjectName datasourceRuntime : jdbcDataSourceRuntimeMbeans) {
                String datasourceName = connection.getAttribute(datasourceRuntime, "Name").toString();
                if (datasources.containsKey("*") || datasources.containsKey(datasourceName)) {
                    long currCapacity = Long.parseLong(connection.getAttribute(datasourceRuntime, "CurrCapacity").toString());
                    long activeConnectionsCurrentCount = Long.parseLong(connection.getAttribute(datasourceRuntime, "ActiveConnectionsCurrentCount").toString());
                    long waitingForConnectionCurrentCount = Long.parseLong(connection.getAttribute(datasourceRuntime, "WaitingForConnectionCurrentCount").toString());
                    StringBuilder out = new StringBuilder();
                    out.append("jdbc-").append(datasourceName).append("-capacity=").append(currCapacity).append(" ");
                    out.append("jdbc-").append(datasourceName).append("-active=").append(activeConnectionsCurrentCount).append(" ");
                    out.append("jdbc-").append(datasourceName).append("-waiting=").append(waitingForConnectionCurrentCount);
                    output.add(out.toString());
                    if (datasources.containsKey("*"))
                        thresholdsArray = SEMICOLON_PATTERN.split(datasources.get("*"));
                    else
                        thresholdsArray = SEMICOLON_PATTERN.split(datasources.get(datasourceName));
                    long warning = Long.parseLong(thresholdsArray[0]);
                    long critical = Long.parseLong(thresholdsArray[1]);
                    int testCode = checkResult(waitingForConnectionCurrentCount, critical, warning);
                    if (testCode == Status.WARNING.getCode() || testCode == Status.CRITICAL.getCode()) 
                        alerts.add(datasourceName + " (" + waitingForConnectionCurrentCount + ")");
                    if (testCode > code)
                        code = testCode;
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
            result.setMessage("JDBC connection waiting count: " + sb.toString());
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
