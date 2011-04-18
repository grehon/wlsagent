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

package net.wait4it.wlsagent.utils;

/**
 * @author Yann Lambret
 *
 */
public class Utils {

	public static Integer checkResult(Long n1, Long n2, Long critical, Long warning, Integer code) {
		if (isCritical(n1, n2, critical)) {
			code = 2;
		} else if (isWarning(n1, n2, warning) && code == 0) {
			code = 1;
		}
		return code;
	}

	public static Integer checkResult(Long n, Long critical, Long warning, Integer code) {
		if (isCritical(n, critical)) {
			code = 2;
		} else if (isWarning(n, warning) && code == 0) {
			code = 1;
		}
		return code;
	}

	private static boolean isCritical(Long n1, Long n2, Long critical) {
		Long ratio = ratio(n1, n2);
		return ratio >= critical;
	}

	private static boolean isCritical(Long n, Long critical) {
		return n >= critical;
	}

	private static boolean isWarning(Long n1, Long n2, Long warning) {
		Long ratio = ratio(n1, n2);
		return ratio >= warning;
	}

	private static boolean isWarning(Long n, Long warning) {
		return n >= warning;
	}

	private static Long ratio(Long n1, Long n2) {
		return Math.round(((n1.doubleValue())/(n2.doubleValue()))*100);
	}

}
