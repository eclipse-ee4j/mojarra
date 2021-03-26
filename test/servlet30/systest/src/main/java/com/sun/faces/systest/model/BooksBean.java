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
import java.util.Iterator;
import java.util.List;


public class BooksBean {

    private List<BookBean> books;    


    public BooksBean() {
        books = new ArrayList<BookBean>();
        books.add(new BookBean("Harry Potter and the Sorcerer's Stone",
                                "J.K. Rowling",
                                "10009001",
                                12.99));
        books.add(new BookBean("Dune",
                                "Frank Herbert",
                                "98111012",
                                15.99));
        books.add(new BookBean("The Hitchhiker's Guide to the Galaxy",
                                "Douglas Adams",
                                "11001199",
                                13.99));
    }    


    public List<BookBean> getBooks() {
        return books;
    }    
    
    public double getTotalCost() {
        double cost = 0.0;
        for (Iterator<BookBean> i = books.iterator(); i.hasNext(); ) {
            BookBean book = i.next();
            cost += (book.getQuantity() * book.getPrice());     
        }
        
        return cost;
    }      
}
