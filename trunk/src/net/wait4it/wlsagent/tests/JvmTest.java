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

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.wait4it.wlsagent.utils.Result;
import net.wait4it.wlsagent.utils.Status;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class JvmTest extends TestUtils implements Test {

	public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
		Result result = new Result();
		StringBuilder output = new StringBuilder(100);
		int code = 0;

		/**
		 * Specific test variables
		 */
		ObjectName jvmRuntimeMbean;
		long heapSizeMax;
		long heapSizeCurrent;
		long heapFreeCurrent;
		long heapUsedCurrent;
		long warning;
		long critical;
		double jvmProcessorLoad;

		/**
		 * Parse parameters
		 */
		String[] paramsArray = SEMICOLON_PATTERN.split(params);
		warning = Long.parseLong(paramsArray[1]);
		critical = Long.parseLong(paramsArray[2]);

		try {
			jvmRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "JVMRuntime");
			heapSizeMax = format((Long)connection.getAttribute(jvmRuntimeMbean, "HeapSizeMax"));
			heapSizeCurrent = format((Long)connection.getAttribute(jvmRuntimeMbean, "HeapSizeCurrent"));
			heapFreeCurrent = format((Long)connection.getAttribute(jvmRuntimeMbean, "HeapFreeCurrent"));
			heapUsedCurrent = heapSizeCurrent - heapFreeCurrent;
			output.append("HeapSize=").append(heapSizeCurrent).append("MB;;;0;").append(heapSizeMax).append(" ");
			output.append("UsedMemory=").append(heapUsedCurrent).append("MB;;;0;").append(heapSizeMax);

			try {
				jvmProcessorLoad = (Double)connection.getAttribute(jvmRuntimeMbean, "JvmProcessorLoad");
				output.append(" JvmProcessorLoad=").append(Math.round(jvmProcessorLoad * 100)).append("%;;;0;100");
			} catch (AttributeNotFoundException ignored) {
				/**
				 * Not dealing with a JRockitRuntimeMBean
				 */
			}

			code = checkResult(heapUsedCurrent, heapSizeMax, critical, warning, code);
			if (code == Status.CRITICAL.getCode() || code == Status.WARNING.getCode()) {
				result.setMessage("Memory used (" + heapUsedCurrent + "/" + heapSizeCurrent + ")");
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
				result.setOutput(output.toString());
				break;
			}
		}

		return result;
	}

}
