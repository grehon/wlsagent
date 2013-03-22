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

package net.wait4it.nagios.wlsagent.tests;

import java.text.DecimalFormat;

import javax.management.ObjectName;

import weblogic.management.runtime.ExecuteThread;

import net.wait4it.nagios.wlsagent.core.Result;
import net.wait4it.nagios.wlsagent.core.Status;
import net.wait4it.nagios.wlsagent.core.WLSProxy;

/**
 * Gets statistics for the WebLogic thread pool.
 * 
 * The following metrics are available:
 * 
 *   - The thread pool current size
 *   - The active thread count
 *   - The hogging thread count
 *   - The stuck thread count
 *   - The thread pool throughput
 * 
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class ThreadPoolTest extends TestUtils implements Test {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    /**
     * WebLogic thread pool stats.
     * 
     * @param proxy   an applicative proxy for the target WLS instance
     * @param params  warning and critical thresholds
     * @return result collected data and test status
     */
    public Result run(WLSProxy proxy, String params) {
        Result result = new Result();
        int code = 0;

        // Test thresholds
        long warning;
        long critical;

        // Performance data
        ExecuteThread threadsArray[];
        int threadIdleCount = 0;
        int threadHoggingCount = 0;
        int threadStuckCount = 0;
        int threadTotalCount;
        int threadActiveCount;
        double throughput;

        // Parses HTTP query params
        String[] paramsArray = params.split(",");
        warning = Long.parseLong(paramsArray[1]);
        critical = Long.parseLong(paramsArray[2]);

        try {
            ObjectName threadPoolRuntimeMbean = proxy.getMBean("ThreadPoolRuntime");
            throughput = (Double)proxy.getAttribute(threadPoolRuntimeMbean, "Throughput");
            threadsArray = (ExecuteThread[])proxy.getAttribute(threadPoolRuntimeMbean, "ExecuteThreads");
            for (ExecuteThread thread : threadsArray) { 
                if ((Boolean)thread.isIdle()) {
                    threadIdleCount += 1;
                }
                if ((Boolean)thread.isHogger()) {
                    threadHoggingCount += 1;
                }
                if ((Boolean)thread.isStuck()) { 
                    threadStuckCount += 1;
                }
            }
            threadTotalCount = threadsArray.length;
            threadActiveCount = threadTotalCount - threadIdleCount;
            StringBuilder out = new StringBuilder();
            out.append("ThreadPoolSize=").append(threadTotalCount).append(" ");
            out.append("ThreadActiveCount=").append(threadActiveCount).append(";;;0;").append(threadTotalCount).append(" ");
            out.append("ThreadHoggingCount=").append(threadHoggingCount).append(";;;0;").append(threadTotalCount).append(" ");
            out.append("ThreadStuckCount=").append(threadStuckCount).append(";;;0;").append(threadTotalCount).append(" ");
            out.append("Throughput=").append(DF.format(throughput));
            result.setOutput(out.toString());
            code = checkResult(threadStuckCount, critical, warning);
            if (code == Status.WARNING.getCode() || code == Status.CRITICAL.getCode()) {
                result.setMessage("thread pool stuck count (" + threadStuckCount + "/" + threadTotalCount + ")");
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
                break;
            }
        }

        return result;
    }

}
