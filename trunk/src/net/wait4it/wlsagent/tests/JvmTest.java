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
import javax.management.ObjectName;

import net.wait4it.nagios.wlsagent.core.Result;
import net.wait4it.nagios.wlsagent.core.Status;
import net.wait4it.nagios.wlsagent.core.WLSProxy;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class JvmTest extends TestUtils implements Test {

    public Result run(WLSProxy proxy, String params) {
        // Test result
        Result result = new Result();

        // Test overall status code
        int code = 0;

        // Test thresholds
        long warning;
        long critical;

        // Performance data
        long heapSizeMax;
        long heapSizeCurrent;
        long heapFreeCurrent;
        long heapUsedCurrent;
        double jvmProcessorLoad;

        // Parses HTTP query params
        String[] paramsArray = params.split(",");
        warning = Long.parseLong(paramsArray[1]);
        critical = Long.parseLong(paramsArray[2]);

        try {
            ObjectName jvmRuntimeMbean = proxy.getMBean("JVMRuntime");
            heapSizeMax = format((Long)proxy.getAttribute(jvmRuntimeMbean, "HeapSizeMax"));
            heapSizeCurrent = format((Long)proxy.getAttribute(jvmRuntimeMbean, "HeapSizeCurrent"));
            heapFreeCurrent = format((Long)proxy.getAttribute(jvmRuntimeMbean, "HeapFreeCurrent"));
            heapUsedCurrent = heapSizeCurrent - heapFreeCurrent;
            StringBuilder out = new StringBuilder();
            out.append("HeapSize=").append(heapSizeCurrent).append("MB;;;0;").append(heapSizeMax).append(" ");
            out.append("UsedMemory=").append(heapUsedCurrent).append("MB;;;0;").append(heapSizeMax);
            try {
                jvmProcessorLoad = (Double)proxy.getAttribute(jvmRuntimeMbean, "JvmProcessorLoad");
                out.append(" JvmProcessorLoad=").append(Math.round(jvmProcessorLoad * 100)).append("%;;;0;100");
            } catch (AttributeNotFoundException ignored) {
                // Not dealing with a JRockitRuntimeMBean
            }
            result.setOutput(out.toString());
            code = checkResult(heapUsedCurrent, heapSizeMax, critical, warning);
            if (code == Status.WARNING.getCode() || code == Status.CRITICAL.getCode())
                result.setMessage("memory used (" + heapUsedCurrent + "/" + heapSizeMax + ")");
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(Status.UNKNOWN);
            result.setMessage(e.toString());
            return result;
        }

        for (Status status : Status.values()) {
            if (code == status.getCode()) {
                result.setStatus(status);
                break;
            }
        }

        return result;
    }

}
