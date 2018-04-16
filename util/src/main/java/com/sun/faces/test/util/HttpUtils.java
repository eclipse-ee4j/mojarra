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

package com.sun.faces.test.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class HttpUtils {
    
    /**
     * <p>Create an HTTP request line from the following parameters.</p>
     * 
     * <code><pre>&lt;methodName&gt; + " /" + &lt;path&gt; + " HTTP/1.1\r\n"</pre></code>
     * 
     * <p>Open a socket to the specified host and port, and issue the request. 
     * Read the result into a buffer and return it as the result.  Save aside the
     * HTTP response code into the outbound argument rc, which must have 
     * at least one element.</p>
     * 
     */ 

    public static String issueHttpRequest(String methodName, int [] rc, String host, String port, String path) throws Exception {
        Integer portInt = Integer.valueOf(port);

        Socket s = new Socket(host, portInt);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        String requestLine = methodName + " /" + path + " HTTP/1.1\r\n";
        writer.write(requestLine);
        writer.write("Host: " + host + ":" + port + "\r\n");
        writer.write("User-Agent: systest-client\r\n");
        writer.write("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
        writer.write("Connection: close\r\n");
        writer.write("\r\n");
        writer.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String cur = null;
        StringBuilder builder = new StringBuilder();
        rc[0] = -1;
        while (null != (cur = reader.readLine())) {
            if (-1 == rc[0]) {
                String [] tokens = cur.split("\\s");
                rc[0] = Integer.valueOf(tokens[1]);
            }
            builder.append(cur).append("\n");
        }
        writer.close();

        
        return builder.toString();
    }
    
}
