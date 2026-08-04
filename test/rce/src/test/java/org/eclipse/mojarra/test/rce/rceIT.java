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

import java.io.IOException;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;

public class rceIT extends BaseIT {

    @Test
    public void testRemoteInclude() {
        open("remote.jsf");

        assertTrue(this.getPageSource().contains("Invalid path"));
    }

    @Test
    public void testSafeParam() {
        open("param.jsf?p=local.xhtml");
        assertTrue(this.getPageSource().contains("safe"));
    }

    @Test
    public void testHttpParam() {
        open("param.jsf?p=https://raw.githubusercontent.com/eclipse-ee4j/mojarra/refs/heads/4.0/test/issue5750/src/main/webapp/issue5750.xhtml");
        String source = this.getPageSource();

        assertFalse(source.contains("support \"begin\" and \"end\""));
        assertTrue(source.contains("Invalid path"));
    }

    @Test
    public void testFileParam() {
        open("param.jsf?p=file:///etc/passwd");
        String source = this.getPageSource();
        assertTrue(source.contains("Invalid path"));
    }

    @Test
    public void testPathTraversal() throws IOException {
        open("param.jsf?p=../../WEB-INF/web.xml");
        String source = this.getPageSource();
        assertTrue(source.contains("Invalid path"));
    }

    @Test
    public void testMalformedUri() throws IOException {
        open("param.jsf?p=://bad");
        String source = this.getPageSource();
        assertTrue(source.contains("Invalid path"));
    }

    @Test
    public void testAbsolutePathParam() throws IOException {
        open("param.jsf?p=/WEB-INF/web.xml");
        String source = this.getPageSource();
        assertTrue(source.contains("Invalid path"));
    }
}
