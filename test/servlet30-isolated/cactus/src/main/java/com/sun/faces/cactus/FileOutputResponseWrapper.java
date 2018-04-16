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
 * $Id: FileOutputResponseWrapper.java,v 1.1 2005/10/18 16:41:32 edburns Exp $
 */



// FileOutputResponseWrapper.java

package com.sun.faces.cactus;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The sole purpose of <B>FileOutputResponseWrapper</B> is to wrap an
 * ServletResponse and change its writer object  so that
 * output can be directed to a file.  <P>
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 * @version $Id: FileOutputResponseWrapper.java,v 1.1 2005/10/18 16:41:32 edburns Exp $
 */

public class FileOutputResponseWrapper extends HttpServletResponseWrapper {

//
// Protected Constants
//

//
// Class Variables
//

//
// Instance Variables
//
    protected PrintWriter out = null;
    public static String FACES_RESPONSE_FILENAME = "FacesResponse.txt";

// Attribute Instance Variables


// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public FileOutputResponseWrapper(HttpServletResponse toWrap, 
            String testRootDir) {
        super(toWrap);
        try {
            FileOutputResponseWriter.initializeFacesResponseRoot(testRootDir);
            File file = new File(FACES_RESPONSE_FILENAME);
            FileOutputStream fs = new FileOutputStream(file);
            out = new PrintWriter(fs);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public void flushBuffer() throws IOException {
        out.flush();
        out.close();
    }


//
// Class methods
//

//
// Methods from ServletResponse 
//

    public PrintWriter getWriter() {
        return out;
    }


} // end of class FileOutputResponseWrapper


