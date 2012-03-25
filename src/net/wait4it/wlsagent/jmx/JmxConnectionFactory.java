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
 * @author Kiril Dunn
 */
public class JmxConnectionFactory {

    private static final String JNDI_NAME = "/jndi/weblogic.management.mbeanservers.runtime";

    private JmxConnectionFactory() {
    }

    public static MBeanServerConnection getInstance(Map<String,String> params) throws Exception {
        Map<String, String> map = new HashMap<String,String>(10);
        JMXServiceURL url;
        JMXConnector connector;
        MBeanServerConnection connection;
        map.put(Context.SECURITY_PRINCIPAL, params.get("username"));
        map.put(Context.SECURITY_CREDENTIALS, params.get("password"));
        map.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
        url = new JMXServiceURL("service:jmx:t3://" + params.get("hostname") + ":" + params.get("port") + JNDI_NAME);
        connector = JMXConnectorFactory.connect(url, map);
        connection = connector.getMBeanServerConnection();
        return connection;
    }
}
