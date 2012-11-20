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

import weblogic.management.runtime.ExecuteThread;

import net.wait4it.wlsagent.utils.Result;
import net.wait4it.wlsagent.utils.Status;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public class ThreadPoolTest extends TestUtils implements Test {

    public Result run(MBeanServerConnection connection, ObjectName serverRuntimeMbean, String params) {
        Result result = new Result();
        StringBuilder output = new StringBuilder();
        int code = 0;

        /**
         * Specific test variables
         */
        DecimalFormat df = new DecimalFormat("0.00");
        int threadIdleCount = 0;
        int threadHoggingCount = 0;
        int threadStuckCount = 0;

        /**
         * Parse parameters
         */
        String[] paramsArray = params.split(";");
        long warning = Long.parseLong(paramsArray[1]);
        long critical = Long.parseLong(paramsArray[2]);

        try {
            ObjectName threadPoolRuntimeMbean = (ObjectName)connection.getAttribute(serverRuntimeMbean, "ThreadPoolRuntime");
            double throughput = Double.parseDouble(connection.getAttribute(threadPoolRuntimeMbean, "Throughput").toString());
            ExecuteThread threadsArray[] = (ExecuteThread[])connection.getAttribute(threadPoolRuntimeMbean, "ExecuteThreads");
            for (ExecuteThread thread : threadsArray) { 
                if ((Boolean)thread.isIdle())
                    threadIdleCount += 1;
                if ((Boolean)thread.isHogger())
                    threadHoggingCount += 1;
                if ((Boolean)thread.isStuck()) 
                    threadStuckCount += 1;
            }
            int threadTotalCount = threadsArray.length;
            int threadActiveCount = threadTotalCount - threadIdleCount;
            output.append("ThreadPoolSize=").append(threadTotalCount).append(" ");
            output.append("ThreadActiveCount=").append(threadActiveCount).append(";;;0;").append(threadTotalCount).append(" ");
            output.append("ThreadHoggingCount=").append(threadHoggingCount).append(";;;0;").append(threadTotalCount).append(" ");
            output.append("ThreadStuckCount=").append(threadStuckCount).append(";;;0;").append(threadTotalCount).append(" ");
            output.append("Throughput=").append(df.format(throughput));
            code = checkResult(threadStuckCount, critical, warning);
            if (code == Status.WARNING.getCode() || code == Status.CRITICAL.getCode())
                result.setMessage("thread pool stuck count (" + threadStuckCount + "/" + threadTotalCount + ")");
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
