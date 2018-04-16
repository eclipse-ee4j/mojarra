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
 * $Id: FacesTestCase.java,v 1.1 2005/10/18 16:41:31 edburns Exp $
 */



// FacesTestCaseJsp.java

package com.sun.faces.cactus;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * This interface defines the contract between something that extends a
 * cactus TestCase class (JspTestCase or ServletTestCase) and
 * FacesTestCaseService.
 */

public interface FacesTestCase {

    public HttpServletRequest getRequest();


    public HttpServletResponse getResponse();


    public ServletConfig getConfig();


    public PageContext getPageContext();


    /**
     * @return true if the ServletResponse output should be sent to a file
     */

    public boolean sendResponseToFile();


    /**
     * @return the name of the expected output filename for this testcase.
     */

    public String getExpectedOutputFilename();


    public String[] getLinesToIgnore();


    public boolean sendWriterToFile();
    
    public void setTestRootDir(String testRootDir);
    
    public String getTestRootDir();

    public void verifyEqualsContractPositive(Object x, Object y, Object z);

} // end of interface FacesTestCase
