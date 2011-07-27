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

package net.wait4it.wlsagent;

import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.wait4it.wlsagent.jmx.JmxService;
import net.wait4it.wlsagent.tests.ServerNameTest;
import net.wait4it.wlsagent.utils.Option;
import net.wait4it.wlsagent.utils.Result;

/**
 * @author Yann Lambret
 *
 */
public class WlsModule {

	private StringBuilder header = new StringBuilder();
	private StringBuilder message = new StringBuilder();
	private StringBuilder output = new StringBuilder();
	private ServerNameTest serverNameTest = new ServerNameTest();
	private String status = "OK";
	private Integer code = 0;

	public String run(Map<String, String> params) {
		MBeanServerConnection connection = null;
		ObjectName serverRuntimeMbean = null;

		try {
			connection = JmxService.getConnection(params);
			serverRuntimeMbean = JmxService.getServerRuntime(connection);
		} catch (Exception e) {
			return "3|" + e.getMessage();
		}

		header.append(serverNameTest.run(connection, serverRuntimeMbean) + ": ");

		for (Option option : Option.values()) {
			if (params.containsKey(option.getName())) {
				checkResult(option.getTest().run(connection, serverRuntimeMbean, params.get(option.getName())));
			}
		}

		if (status.equals("OK"))
			header.append("status " + status);
		else
			header.append("status " + status + " - " + message.toString());

		output.insert(0, header.toString() + "|");
		output.insert(0, code.toString() + "|");
		return output.toString();
	}

	private void checkResult(Result result) {
		switch (result.getStatus()) {
		case OK:
			output.append(result.getOutput());
			break;
		case WARNING:
			if (code < 1) { code = 1; status = "WARNING"; }
			message.append(result.getMessage());
			output.append(result.getOutput());
			break;
		case CRITICAL:
			if (code < 2) { code = 2; status = "CRITICAL"; }
			message.append(result.getMessage());
			output.append(result.getOutput());
			break;
		case UNKNOWN:
			code = 3;
			status = "UNKNOWN";
			message.append(result.getMessage());
			break;
		}
	}

}
