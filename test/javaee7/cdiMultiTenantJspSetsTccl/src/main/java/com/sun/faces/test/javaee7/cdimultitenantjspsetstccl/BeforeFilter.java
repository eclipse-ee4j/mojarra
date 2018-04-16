/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.test.javaee7.cdimultitenantjspsetstccl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContextFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class BeforeFilter implements Filter {
    
    private FilterConfig filterConfig = null;
    
    public BeforeFilter() {
    }    
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        
        Thread thread = Thread.currentThread();
        ClassLoader tccl = thread.getContextClassLoader();
        ClassLoader tcclp1 = new URLClassLoader(new URL[0], tccl); 
        thread.setContextClassLoader(tcclp1);
        
        final boolean weldIsTCCLReplacementResilient = true;
        
        if (weldIsTCCLReplacementResilient) {
            try {
                chain.doFilter(request, response);
            } catch (Exception t) {
                HttpServletResponse resp = (HttpServletResponse) response;
                PrintWriter pw = resp.getWriter();
                try {
                    pw.print("<html><body><p id=\"result\">FAILURE</p>");
                    int indentLevel = 0;
                    String indent;
                    Throwable cause = t;
                    do {
                        StringBuilder indentBuilder = new StringBuilder();
                        for (int i = 0; i < indentLevel; i++) {
                            indentBuilder.append("&nbsp;&nbsp;");
                        }
                        indent = indentBuilder.toString();
                        pw.print("<p>" + indent + " Exception: " + cause.getClass().getName() + "</p>");
                        pw.print("<p>" + indent + " Exception Message: " + cause.getLocalizedMessage() + "</p>");
                        pw.print("<code><pre>");
                        cause.printStackTrace(pw);
                        pw.print("</pre></code>");
                    } while (null != (cause = cause.getCause()));
                    pw.print("</body></html>");
                    resp.setStatus(200);
                    pw.close();
                } catch (Exception e) {
                }
            } finally {
                thread.setContextClassLoader(tccl);
            }
        } else {
        }
        
    }

    public void destroy() {        
    }

    public void init(FilterConfig filterConfig) {        
        this.filterConfig = filterConfig;
    }
    
}
