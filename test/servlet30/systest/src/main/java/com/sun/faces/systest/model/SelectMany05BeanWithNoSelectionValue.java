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

package com.sun.faces.systest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author edburns
 */
public class SelectMany05BeanWithNoSelectionValue extends SelectMany05Bean {

    private List<HobbitBean> hobbitList;
    
    
    public SelectMany05BeanWithNoSelectionValue() {
        HobbitBean[] hobbits = getHobbitBeanArray();

        hobbitList = new ArrayList<HobbitBean>();
        hobbitList.addAll(Arrays.asList(hobbits));
        
    }

    @Override
    protected HobbitBean[] getHobbitBeanArray() {
        // Prepend a HobbitBean with the value of "No Selection"
        // without the quotes, to the super's hobbit bean array.
        HobbitBean [] superResult = super.getHobbitBeanArray();
        HobbitBean [] result = new HobbitBean[superResult.length + 1];
        result[0] = new HobbitBean("No Selection", "<No Selection>");
        for (int i = 1; i < result.length; i++) {
            result[i] = superResult[i-1];
        }
        
        return result;
    }
    
    public List<HobbitBean> getHobbitList() {
        return hobbitList;
    }
    

}
