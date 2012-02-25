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

import java.text.DecimalFormat;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.wait4it.wlsagent.utils.Result;
import net.wait4it.wlsagent.utils.Status;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class ThreadPoolTest extends TestUtils implements Test {

	private static final String MESSAGE = " thread pool test ";

	public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
		Result result = new Result();
		StringBuilder output = new StringBuilder(100);
		int code = 0;

		/**
		 * Specific test variables
		 */
		ObjectName threadPoolRuntimeMbean;
		long threadTotalCount;
		long threadIdleCount;
		long threadActiveCount;
		long warning;
		long critical;
		double throughput;
		DecimalFormat df = new DecimalFormat("0.00");

		/**
		 * Parse parameters
		 */
		String[] paramsArray = SEMICOLON_PATTERN.split(params);
		warning = Long.parseLong(paramsArray[1]);
		critical = Long.parseLong(paramsArray[2]);

		try {
			threadPoolRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "ThreadPoolRuntime");
			threadTotalCount = Long.parseLong(connection.getAttribute(threadPoolRuntimeMbean, "ExecuteThreadTotalCount").toString());
			threadIdleCount = Long.parseLong(connection.getAttribute(threadPoolRuntimeMbean, "ExecuteThreadIdleCount").toString());
			threadActiveCount = threadTotalCount - threadIdleCount;
			throughput = Double.parseDouble(connection.getAttribute(threadPoolRuntimeMbean, "Throughput").toString());
			output.append("ThreadPoolSize=").append(threadTotalCount).append(" ");
			output.append("ThreadActiveCount=").append(threadActiveCount).append(";;;0;").append(threadTotalCount).append(" ");
			output.append("Throughput=").append(df.format(throughput));
			code = checkResult(threadActiveCount, threadTotalCount, critical, warning, code);
			if (code == Status.CRITICAL.getCode() || code == Status.WARNING.getCode()) {
				result.setMessage("Active threads (" + threadActiveCount + "/" + threadTotalCount + ")");
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
				if (result.getMessage() == null || result.getMessage().length() == 0) {
					result.setMessage(status.getMessage(MESSAGE));
				}
				result.setOutput(output.toString());
				break;
			}
		}

		return result;
	}

}

