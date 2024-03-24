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

package com.sun.faces.xml;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

public class ParseXMLTestCase {

    List<File> list = new ArrayList<>();
    private final static String xmlDir = "/conf/share";

    // ------------------------------------------------------------ Test Methods
    /**
     * Added for issue 904.
     */
    @Test
    public void testParseXML() throws Exception {

        String curDir = System.getProperty("user.dir");
        File baseDir = new File(curDir);
        System.out.println("current dir = " + curDir);
        System.out.flush();
        visitAllDirsAndFiles(new File(baseDir, xmlDir));
        for (Object file : list) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(true);
                SAXParser saxParser = factory.newSAXParser();

                System.out.println("XML file to be parsed : file://" + file.toString());
                System.out.flush();
                saxParser.parse(new InputSource(new FileInputStream(file.toString())), new XHTMLResolvingHandler());
                System.out.println("parsing complete.");
                System.out.flush();
            } catch (Exception e) {
                System.out.println("Parse error for " + file.toString() + " " + e.toString());
                System.out.flush();
                fail();
            }
        }

    }

    // Process all files and directories under dir
    private void visitAllDirsAndFiles(File dir) {

        if (dir.isFile()) {
            if (isXML(dir)) {
                list.add(dir);
            }
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(new File(dir, children[i]));
            }
        }
    }

    private boolean isXML(File file) {
        String name = file.getName();
        return name.endsWith(".xml");
    }

}
