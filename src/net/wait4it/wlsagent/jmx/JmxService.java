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

import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class JmxService {

    private static final ObjectName SERVICE;

    static {
        try {
            SERVICE = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
        } catch (MalformedObjectNameException e) {
            throw new AssertionError(e);
        }
    }

    private JmxService() {

    }

    public static MBeanServerConnection getConnection(Map<String,String> params) throws Exception {
        return JmxConnectionFactory.getInstance(params);
    }

    public static ObjectName getServerRuntime(MBeanServerConnection connection) throws Exception {
        return (ObjectName)connection.getAttribute(SERVICE, "ServerRuntime");
    }

}
