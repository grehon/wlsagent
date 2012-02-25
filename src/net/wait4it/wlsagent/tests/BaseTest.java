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

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class BaseTest {

	public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean) {
		String serverName;
		String serverState;
		Result result = new Result();

		try {
			serverName = connection.getAttribute(serverRuntimeMbean, "Name").toString();
			serverState = connection.getAttribute(serverRuntimeMbean, "State").toString();

		} catch (Exception e) {
			e.printStackTrace();
			result.setStatus(Status.UNKNOWN);
			result.setMessage(e.toString());
			return result;
		}
		
		result.setMessage(serverName + " is in " + serverState + " state");
		
		if (serverState.equals("RUNNING"))
			result.setStatus(Status.OK);
		else
			result.setStatus(Status.CRITICAL);
		
		return result;
	}

}
