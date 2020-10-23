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

/**
 * @author Jacob Hookom
 */
public final class Path {

    public static String normalize(String path) {
        if (path.length() == 0) {
            return path;
        }
        String n = path;
        boolean abs = false;
        while (n.indexOf('\\') >= 0) {
            n = n.replace('\\', '/');
        }
        if (n.charAt(0) != '/') {
            n = '/' + n;
            abs = true;
        }
        int idx = 0;
        while (true) {
            idx = n.indexOf("%20");
            if (idx == -1) {
                break;
            }
            n = n.substring(0, idx) + " " + n.substring(idx + 3);
        }
        while (true) {
            idx = n.indexOf("/./");
            if (idx == -1) {
                break;
            }
            n = n.substring(0, idx) + n.substring(idx + 2);
        }
        if (abs) {
            n = n.substring(1);
        }
        return n;
    }

    public static String relative(String ctx, String path) {
        if (path.length() == 0) {
            return context(ctx);
        }
        String c = context(normalize(ctx));
        String p = normalize(path);
        p = c + p;

        int idx = 0;
        while (true) {
            idx = p.indexOf("/../");
            if (idx == -1) {
                break;
            }
            int s = p.lastIndexOf('/', idx - 3);
            if (s == -1) {
                break;
            }
            p = p.substring(0, s) + p.substring(idx + 3);
        }
        return p;
    }

    public static String context(String path) {
        int idx = path.lastIndexOf('/');
        if (idx == -1) {
            return "/";
        }
        return path.substring(0, idx + 1);
    }

}
