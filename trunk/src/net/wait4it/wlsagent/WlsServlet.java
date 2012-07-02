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

import java.io.IOException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Yann Lambret
 *
 */
@SuppressWarnings("serial")
public class WlsServlet extends HttpServlet {

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String,String> params = new HashMap<String,String>();
        WlsModule module = new WlsModule();

        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
            String param = e.nextElement();
            params.put(param, request.getParameter(param));
        }

        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(module.run(params));
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
