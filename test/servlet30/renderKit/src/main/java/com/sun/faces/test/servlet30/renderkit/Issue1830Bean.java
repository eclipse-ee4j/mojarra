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

package com.sun.faces.test.servlet30.renderkit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean(name = "issue1830Bean")
@RequestScoped
public class Issue1830Bean implements Serializable {
    /**
     * Get the repeat.
     */
    public List<List<String>> getRepeat() {
        List<List<String>> result = new ArrayList<List<String>>();
        for (int i=0; i<10; i++) {
            List<String> subList = new ArrayList<String>();
            for(int j=0; j<10; j++) {
                subList.add(Integer.toString(j));
            }
            result.add(subList);
        }
        return result;
    }
}
