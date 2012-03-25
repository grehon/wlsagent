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
import net.wait4it.wlsagent.tests.BaseTest;
import net.wait4it.wlsagent.utils.Option;
import net.wait4it.wlsagent.utils.Result;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 *
 */
public class WlsModule {

    private final StringBuilder header = new StringBuilder(500);
    private final StringBuilder message = new StringBuilder(500);
    private final StringBuilder output = new StringBuilder(500);
    private final BaseTest baseTest = new BaseTest();
    private String status = "OK";
    private int code = 0;

    public String run(Map<String, String> params) {
        MBeanServerConnection connection;
        ObjectName serverRuntimeMbean;
        Result result;

        try {
            connection = JmxService.getConnection(params);
            serverRuntimeMbean = JmxService.getServerRuntime(connection);
        } catch (Exception e) {
            e.printStackTrace();
            return "3|" + e;
        }

        result = baseTest.run(connection, serverRuntimeMbean);
        header.append(result.getMessage()).append(" - ");

        switch (result.getStatus()) {
        case OK:
            break;
        case CRITICAL:
            code = 2;
            status = "CRITICAL";
            break;
        case UNKNOWN:
            code = 3;
            status  = "UNKNOWN";
            break;
        }

        for (Option option : Option.values()) {
            if (params.containsKey(option.getName()))
                checkResult(option.getTest().run(connection, serverRuntimeMbean, params.get(option.getName())));
        }

        header.append("status ").append(status);

        if (! status.equals("OK") && message.length() > 0)
            header.append(" - ").append(message.toString());

        output.insert(0, header + "|");
        output.insert(0, code + "|");
        return output.toString();
    }

    private void checkResult(Result result) {
        String out = "";
        String msg = "";

        switch (result.getStatus()) {
        case OK:
            out = result.getOutput();
            break;
        case WARNING:
            if (code < 1) { code = 1; status = "WARNING"; }
            msg = result.getMessage();
            out = result.getOutput();
            break;
        case CRITICAL:
            if (code < 2) { code = 2; status = "CRITICAL"; }
            msg = result.getMessage();
            out = result.getOutput();
            break;
        case UNKNOWN:
            code = 3;
            status = "UNKNOWN";
            msg = result.getMessage();
            break;
        }

        if (msg.length() > 0) {
            if (message.length() > 0)
                message.append(" ");
            message.append(msg);
        }

        if (out.length() > 0) {
            if (output.length() > 0)
                output.append(" ");
            output.append(out);
        }
    }

}