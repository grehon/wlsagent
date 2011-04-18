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

import net.wait4it.wlsagent.tests.ComponentTest;
import net.wait4it.wlsagent.tests.JdbcTest;
import net.wait4it.wlsagent.tests.JtaTest;
import net.wait4it.wlsagent.tests.JvmTest;
import net.wait4it.wlsagent.tests.Test;
import net.wait4it.wlsagent.tests.ThreadPoolTest;

/**
 * @author Yann Lambret
 *
 */
public enum Option {

	JVM        ( "jvm", new JvmTest() ),
	JTA        ( "jta", new JtaTest() ),
	JDBC       ( "jdbc", new JdbcTest() ),
	COMPONENT  ( "component", new ComponentTest() ),
	THREADPOOL ( "thread-pool", new ThreadPoolTest() );

	private String name;
	private Test test;

	private Option(String name, Test test) {
		this.name = name;
		this.test = test;
	}

	public String getName() {
		return name;
	}

	public Test getTest() {
		return test;
	}

}
