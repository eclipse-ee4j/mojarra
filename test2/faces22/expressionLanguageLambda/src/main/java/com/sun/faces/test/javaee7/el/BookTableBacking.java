/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee7.el;

import java.util.List;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named
@Dependent
public class BookTableBacking {
    
    public String getValue() {
        String result = "";
        FacesContext context = FacesContext.getCurrentInstance();
        
        @SuppressWarnings("unchecked")
        List<Book> books = context.getApplication().evaluateExpressionGet(
            context, 
            "#{library.books.stream().filter(b->b.category == 'Philosophical Fiction').toList()}",
            List.class);
        
        if (!books.isEmpty()) {
            result = books.get(0).getTitle();
        }
        
        return result;
    }

}
