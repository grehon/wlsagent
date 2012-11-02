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
public class JtaTest extends TestUtils implements Test {

    public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
        Result result = new Result();
        StringBuilder output = new StringBuilder();
        int code = 0;

        /**
         * Parse parameters
         */
        String[] paramsArray = SEMICOLON_PATTERN.split(params);
        long warning = Long.parseLong(paramsArray[1]);
        long critical = Long.parseLong(paramsArray[2]);

        try {
            ObjectName jtaRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "JTARuntime");
            long activeTransactionsTotalCount = Long.parseLong(connection.getAttribute(jtaRuntimeMbean, "ActiveTransactionsTotalCount").toString());
            output.append("ActiveTransactions=").append(activeTransactionsTotalCount);
            code = checkResult(activeTransactionsTotalCount, critical, warning);
            if (code == Status.WARNING.getCode() || code == Status.CRITICAL.getCode()) {
                result.setMessage("transaction active count (" + activeTransactionsTotalCount + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(Status.UNKNOWN);
            result.setMessage(e.toString());
            return result;
        }

        // Set result status and output
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