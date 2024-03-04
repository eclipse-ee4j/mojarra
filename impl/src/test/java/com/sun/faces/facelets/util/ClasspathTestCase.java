/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.util;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.jar.JarFile;

import org.junit.jupiter.api.Test;

public class ClasspathTestCase {

    @Test
    public void testGetAlternativeJarFile() throws Exception {
        String input = "rar:/scratch/fleme/fmwhome/AS11gR1SOA/soa/connectors/FileAdapter.rar!fileAdapter.jar!/META-INF/";

        JarFile output = Classpath.getAlternativeJarFile(input);
        assertNull(output);

        input = "/scratch/ejburns/Documents/JavaEE/workareas/i_mojarra_1869/jsf-test/JAVASERVERFACES-1869/reproducer/FileAdapter.rar!fileAdapter.jar!/META-INF/";
        output = Classpath.getAlternativeJarFile(input);
        assertNull(output);
    }
}
