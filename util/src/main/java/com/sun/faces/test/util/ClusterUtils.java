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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClusterUtils {

    /**
     * No instantiating me :)
     */
    private ClusterUtils() {
    }

    /**
     * A utility method that gives you an array of String that contain the base
     * URLs for the given test scenario.
     * 
     * @return the base URLs.
     */
    public static String[] getBaseUrls() {
        List<String> result = new ArrayList<String>();
        result.add(System.getProperty("integration.url"));
        for (int i = 1; i < 10; i++) {
            if (System.getProperty("integration.url" + i) != null
                    && !System.getProperty("integration.url" + i).trim().equals("")) {
                result.add(System.getProperty("integration.url" + i));
            }
        }
        return result.toArray(new String[0]);
    }
    
    /**
     * A utility method that scrambles the order of the base URLs.
     * 
     * @return the randomized base URLs.
     */
    public static String[] getRandomizedBaseUrls() {
        List<String> urls = Arrays.asList(getBaseUrls());
        Collections.shuffle(urls);
        return urls.toArray(new String[0]);
    }
}
