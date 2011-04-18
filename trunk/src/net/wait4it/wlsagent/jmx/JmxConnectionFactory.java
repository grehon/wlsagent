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

package net.wait4it.wlsagent.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

/**
 * @author Yann Lambret
 *
 */
public class JmxConnectionFactory {
	private static final String PROTOCOL = "t3";
	private static final String JNDI_ROOT = "/jndi/";
	private static final String JNDI_NAME = "weblogic.management.mbeanservers.runtime";

	public static MBeanServerConnection getInstance(Map<String,String> params) {
		HashMap<String,String> map = new HashMap<String,String>();
		JMXServiceURL url = null;
		JMXConnector connector = null;
		MBeanServerConnection connection = null;	
		map.put(Context.SECURITY_PRINCIPAL, params.get("username"));
		map.put(Context.SECURITY_CREDENTIALS, params.get("password"));
		map.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
		map.put("jmx.remote.x.request.waiting.timeout", "5000");
		try {
			url = new JMXServiceURL("service:jmx:" + PROTOCOL + "://" + params.get("hostname") + ":" + params.get("port") + JNDI_ROOT + JNDI_NAME);
			connector = JMXConnectorFactory.connect(url, map);
			connection = connector.getMBeanServerConnection();
		} catch (Exception e) {
			throw new RuntimeException("Unable to get MBeanServerConnection for JMXServiceURL " + url.toString());
		}
		return connection;
	}

}
