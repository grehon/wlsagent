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
 * @author Kiril Dunn
 */
public enum Status {

	OK       ( 0, "" ),
	WARNING  ( 1, "Warning alert raised by the" ),
	CRITICAL ( 2, "Critical alert raised by the" ),
	UNKNOWN  ( 3, "Unknown status for the" );

	private final int code;
	private final String message;

	private Status(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage(String message) {
		return this.message + message;
	}

}
