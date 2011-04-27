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
import net.wait4it.wlsagent.utils.Utils;

/**
 * @author Yann Lambret
 *
 */
public class JdbcTest extends TestUtils implements Test {

	private static final String MESSAGE = " jdbc test ";

	public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
		Result result = new Result();
		StringBuilder output = new StringBuilder();
		Integer code = 0;

		/**
		 * Specific test variables
		 */
		Map<String,String> datasources = new HashMap<String,String>();
		ObjectName jdbcServiceRuntimeMbean = null;
		ObjectName jdbcDataSourceRuntimeMbeans[] = null;

		/**
		 * Populate the HashMap with datasource name keys
		 * and string values like 'warning;critical'
		 */
		String[] paramsArray = params.split("\\|");
		for (int i = 0; i < paramsArray.length; i++) {
			String[] datasourcesArray = (paramsArray[i]).split(";", 2);
			datasources.put(datasourcesArray[0], datasourcesArray[1]);
		}

		try {
			jdbcServiceRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "JDBCServiceRuntime");
			jdbcDataSourceRuntimeMbeans = (ObjectName[])connection.getAttribute(jdbcServiceRuntimeMbean, "JDBCDataSourceRuntimeMBeans");
			for (ObjectName datasourceRuntime : jdbcDataSourceRuntimeMbeans) {
				String datasourceName = connection.getAttribute(datasourceRuntime, "Name").toString();
				if (datasources.containsKey(datasourceName)) {
					Long currCapacity = Long.parseLong(connection.getAttribute(datasourceRuntime, "CurrCapacity").toString());
					Long activeConnectionsCurrentCount = Long.parseLong(connection.getAttribute(datasourceRuntime, "ActiveConnectionsCurrentCount").toString());
					Long waitingForConnectionCurrentCount = Long.parseLong(connection.getAttribute(datasourceRuntime, "WaitingForConnectionCurrentCount").toString());
					output.append("jdbc-" + datasourceName + "-capacity" + "=" + currCapacity + " ");
					output.append("jdbc-" + datasourceName + "-active" + "=" + activeConnectionsCurrentCount + " ");
					output.append("jdbc-" + datasourceName + "-waiting" + "=" + waitingForConnectionCurrentCount + " ");
					String[] thresholdsArray = datasources.get(datasourceName).split(";");
					Long warning = Long.parseLong(thresholdsArray[0]);
					Long critical = Long.parseLong(thresholdsArray[1]);
					code = checkResult(waitingForConnectionCurrentCount, critical, warning, code);
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
