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

import java.util.Collections;
import java.util.List;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 */
public abstract class TestUtils {

    public static int checkResult(long n1, long n2, long critical, long warning) {
        int code = 0;

        if (isCritical(n1, n2, critical)) {
            code = 2;
        } else if (isWarning(n1, n2, warning)) {
            code = 1;
        }

        return code;
    }

    public static int checkResult(long n, long critical, long warning) {
        int code = 0;

        if (isCritical(n, critical)) {
            code = 2;
        } else if (isWarning(n, warning)) {
            code = 1;
        }

        return code;
    }

    public String formatOut(List<String> list) {
        StringBuilder sb = new StringBuilder();
        if (! list.isEmpty()) {
            Collections.sort(list);
            sb.append(list.remove(0));
            while(! list.isEmpty()) {
                sb.append(" ").append(list.remove(0));
            }
        }
        return sb.toString();
    }

    public String formatMsg(String prefix, List<String> list) {
        StringBuilder sb = new StringBuilder();
        if (! list.isEmpty()) {
            Collections.sort(list);
            sb.append(prefix).append(list.remove(0));
            while(! list.isEmpty()) {
                sb.append(", ").append(list.remove(0));
            }
        }
        return sb.toString();
    }

    public static long format(Long value) {
        return value / 1024 / 1024;
    }

    private static boolean isCritical(long n1, long n2, long critical) {
        long ratio = ratio(n1, n2);
        return ratio >= critical;
    }

    private static boolean isCritical(long n, long critical) {
        return n >= critical;
    }

    private static boolean isWarning(long n1, long n2, long warning) {
        long ratio = ratio(n1, n2);
        return ratio >= warning;
    }

    private static boolean isWarning(long n, long warning) {
        return n >= warning;
    }

    private static long ratio(long n1, long n2) {
        return Math.round((double) n1 / (double) n2 * 100);
    }

}
