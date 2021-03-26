/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: CompareFiles.java,v 1.1 2005/10/18 16:41:30 edburns Exp $
 */



package com.sun.faces.cactus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.List;

public class CompareFiles {

    public CompareFiles() {
    }


    public static String stripJsessionidFromLine(String line) {
        if (null == line) {
            return line;
        }
        int
            start = 0,
            end = 0;
        String result = line;

        if (-1 == (start = line.indexOf(";jsessionid="))) {
            return result;
        }

        if (-1 == (end = line.indexOf("?", start))) {
            if (-1 == (end = line.indexOf("\"", start))) {
                throw new IllegalStateException();
            }
        }
        result = stripJsessionidFromLine(line.substring(0, start) +
                                         line.substring(end));
        return result;
    }


    /**
     * This method compares the input files character by character.
     * Skips whitespaces and comparison is not case sensitive.
     */
    public static boolean filesIdentical(String newFileName, String oldFileName, List oldLinesToIgnore)
        throws IOException {

        boolean same = true;

        File newFile = new File(newFileName);
        File oldFile = new File(oldFileName);

        FileReader newFileReader = new FileReader(newFile);
        FileReader oldFileReader = new FileReader(oldFile);
        LineNumberReader newReader = new LineNumberReader(newFileReader);
        LineNumberReader oldReader = new LineNumberReader(oldFileReader);

        String newLine, oldLine;

        newLine = newReader.readLine().trim();
        oldLine = oldReader.readLine().trim();

        // if one of the lines is null, but not the other
        if (((null == newLine) && (null != oldLine)) ||
            ((null != newLine) && (null == oldLine))) {
            System.out.println("1OLD=" + oldLine);
            System.out.println("1NEW=" + newLine);
            same = false;
        }

        while (null != newLine && null != oldLine) {
            if (!newLine.equals(oldLine)) {
                if (oldLine.contains("jakarta.faces.Token")) {
                    break;
                }
                if (null != oldLinesToIgnore && oldLinesToIgnore.size() > 0) {
                    // go thru the list of oldLinesToIgnore and see if
                    // the current oldLine matches any of them.
                    Iterator ignoreLines = oldLinesToIgnore.iterator();
                    boolean foundMatch = false;
                    while (ignoreLines.hasNext()) {
                        String newTrim = ((String) ignoreLines.next()).trim();
                        if (oldLine.equals(newTrim)) {
                            foundMatch = true;
                            break;
                        }
                    }
                    // If we haven't found a match, then this mismatch is
                    // important
                    if (!foundMatch) {
                        System.out.println("2OLD=" + oldLine);
                        System.out.println("2NEW=" + newLine);
                        same = false;
                        break;
                    }
                } else {
                    newLine = stripJsessionidFromLine(newLine);
                    oldLine = stripJsessionidFromLine(oldLine);
                    if (!newLine.equals(oldLine)) {
                        System.out.println("3OLD=" + oldLine);
                        System.out.println("3NEW=" + newLine);
                        same = false;
                        break;
                    }
                }
            }

            newLine = newReader.readLine();
            oldLine = oldReader.readLine();

            // if one of the lines is null, but not the other
            if (((null == newLine) && (null != oldLine)) ||
                ((null != newLine) && (null == oldLine))) {
                System.out.println("4OLD=" + oldLine);
                System.out.println("4NEW=" + newLine);
                same = false;
                break;
            }
            if (null != newLine) {
                newLine = newLine.trim();
            }
            if (null != oldLine) {
                oldLine = oldLine.trim();
            }
        }

        newReader.close();
        oldReader.close();

        // if same is true and both files have reached eof, then
        // files are identical
        if (same == true) {
            return true;
        }
        return false;
    }
}
