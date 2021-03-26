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

package com.sun.faces.test.servlet30.systest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;

public class HttpMethodITCase extends HtmlUnitFacesITCase {

    public HttpMethodITCase(String name) {
        super(name);
    }

    public static Test suite() {
        return (new TestSuite(HttpMethodITCase.class));
    }

    static final String interweaving = "/faces/interweaving01.jsp";
    static final String interweavingRegEx = "(?s).*Begin\\s*test\\s*jsp include without verbatim\\s*interweaving\\s*works\\s*well!!\\s*End\\s*test\\s*jsp include without verbatim.*";

    static final String repeat = "/faces/facelets/uirepeat.xhtml";
    static final String repeatRegEx = "(?s).*ListFlavor is chocolate.*";

    public void testPositive() throws Exception {
        int[] rc = new int[1];
        // Ensure the GET request works as expected
        assertTrue(issueHttpRequest("GET", rc, interweaving).matches(interweavingRegEx));
        assertEquals(HttpURLConnection.HTTP_OK, rc[0]);

        // Ensure the POST request works as expected
        assertTrue(issueHttpRequest("POST", rc, interweaving).matches(interweavingRegEx));
        assertEquals(HttpURLConnection.HTTP_OK, rc[0]);

        // Ensure the PUT request works as expected
        assertTrue(issueHttpRequest("PUT", rc, repeat).matches(repeatRegEx));
        assertEquals(HttpURLConnection.HTTP_OK, rc[0]);

        // Ensure the DELETE request works as expected
        assertTrue(issueHttpRequest("DELETE", rc, repeat).matches(repeatRegEx));
        assertEquals(HttpURLConnection.HTTP_OK, rc[0]);

        // Ensure the HEAD request works as expected
        String result = issueHttpRequest("HEAD", rc, repeat);
        String[] tokens = result.split("[\\r\\n][\\r\\n]");
        assertTrue(1 == tokens.length);
        assertEquals(HttpURLConnection.HTTP_OK, rc[0]);

        // Ensure the OPTIONS  request works as expected
        result = issueHttpRequest("OPTIONS", rc, repeat);
        tokens = result.split("[\\r\\n][\\r\\n]");
        assertTrue(1 == tokens.length || "0".equals(tokens[1]));
        assertEquals(HttpURLConnection.HTTP_OK, rc[0]);

        // Ensure the GETBOGUSALLOWED request *does* work, because
        // we configured it in web.xml
        assertTrue(issueHttpRequest("GETBOGUSALLOWED", rc, repeat).matches(repeatRegEx));
        assertEquals(HttpURLConnection.HTTP_OK, rc[0]);

    }

    public void testNegative() throws Exception {
        int[] rc = new int[1];

        // Ensure the GET22 request does not work
        assertFalse("Bogus HTTP method was accepted by server.  Fail.",
                issueHttpRequest("GET22", rc, interweaving).matches(interweavingRegEx));
        assertFalse("Bogus HTTP method returned HTTP_OK status.  Fail.", HttpURLConnection.HTTP_OK == rc[0]);
    }

    private String issueHttpRequest(String methodName, int[] rc, String path) throws Exception {

        URL url = getURL(path);
        Socket s = new Socket(url.getHost(), url.getPort());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        String requestLine = methodName + " /" + contextPath + path + " HTTP/1.1\r\n";
        writer.write(requestLine);
        writer.write("Host: " + url.getHost() + ":" + url.getPort() + "\r\n");
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
                String[] tokens = cur.split("\\s");
                rc[0] = Integer.valueOf(tokens[1]);
            }
            builder.append(cur).append("\n");
        }
        writer.close();

        return builder.toString();
    }

}
