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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.wait4it.wlsagent.utils.Result;
import net.wait4it.wlsagent.utils.Status;
import net.wait4it.wlsagent.utils.Utils;

/**
 * @author Yann Lambret
 *
 */
public class JtaTest extends TestUtils implements Test {

	private static final String MESSAGE = " jta test ";

	public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
		Result result = new Result();
		StringBuilder output = new StringBuilder();
		Integer code = 0;

		/**
		 * Specific test variables
		 */
		ObjectName jtaRuntimeMbean = null;
		Long activeTransactionsTotalCount = null;
		Long warning = null;
		Long critical = null;

		/**
		 * Parse parameters
		 */
		String[] paramsArray = params.split(";");
		warning = Long.parseLong(paramsArray[1]);
		critical = Long.parseLong(paramsArray[2]);

		try {
			jtaRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "JTARuntime");
			activeTransactionsTotalCount = (Long.parseLong(connection.getAttribute(jtaRuntimeMbean, "ActiveTransactionsTotalCount").toString()));
			output.append("ActiveTransactions=" + activeTransactionsTotalCount + " ");
			code = checkResult(activeTransactionsTotalCount, critical, warning, code);
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
