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
 * $Id: FacesTestCaseService.java,v 1.2 2005/10/26 02:24:05 edburns Exp $
 */



// FacesTestCaseService.java

package com.sun.faces.cactus;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.ConfigureListener;
import org.apache.cactus.server.ServletContextWrapper;

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.ResponseWriter;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseId;
import junit.framework.Assert;


/**
 * Subclasses of ServletTestCase and JspTestCase use an instance of this
 * class to handle behavior specific to Faces TestCases.  You may
 * recognize this as using object compositition vs multiple inheritance.
 * <p/>
 * <p/>
 * <p/>
 * <B>Lifetime And Scope</B> <P> Same as the JspTestCase or
 * ServletTestCase instance that uses it.
 *
 * @version $Id: FacesTestCaseService.java,v 1.2 2005/10/26 02:24:05 edburns Exp $
 */

public class FacesTestCaseService extends Object {

//
// Protected Constants
//

    /**
     * Things used as names and values in the System.Properties table.
     */

    public static final String ENTER_CALLED = "enterCalled";
    public static final String EXIT_CALLED = "exitCalled";
    public static final String EMPTY = "empty";


//
// Class Variables
//

//
// Instance Variables
//

// Attribute Instance Variables

// Relationship Instance Variables

    protected FacesTestCase facesTestCase = null;

    protected Assert assertVendor = null;

    protected FacesContextFactory facesContextFactory = null;

    protected FacesContext facesContext = null;

    protected ConfigureListener configureListener = null;

    protected Lifecycle lifecycle = null;

    protected ServletContext servletContext = null;



//
// Constructors and Initializers    
//

    public FacesTestCaseService(FacesTestCase newFacesTestCase) {
        facesTestCase = newFacesTestCase;
        assertVendor = (Assert) facesTestCase;
    }


//
// Class methods
//

//
// General Methods
//

    public FacesContext getFacesContext() {
        return facesContext;
    }


    public FacesContextFactory getFacesContextFactory() {
        return facesContextFactory;
    }


    public void setUp() {
        FacesContext oldContext = FacesContext.getCurrentInstance();
        if (null != oldContext) {
            oldContext.release();
        }
        this.servletContext = facesTestCase.getConfig().getServletContext();

        HttpServletResponse response = null;
        TestingUtil.setUnitTestModeEnabled(true);

        // Since we run using tomcat's deploy targets, we must obtain the
        // absolute path to where we are to write our output files.
        String testRootDir =
              this.servletContext.getInitParameter(
                    "testRootDir");

        assert (null != testRootDir);
        facesTestCase.setTestRootDir(testRootDir);

        this.configureListener = new ConfigureListener();
        ServletContextEvent e = new ServletContextEvent(this.servletContext);

//        // make sure this gets called once per ServletContext instance.
//        if (null == (servletContext.getAttribute(FacesServlet.CONFIG_FILES_ATTR))) {
//            System.out.println("INVOKING CONFIGLISTENER");
//            configureListener.contextDestroyed(e);
//            FactoryFinder.releaseFactories();
            configureListener.contextInitialized(e);
//            FacesContext.getCurrentInstance().release();
//        }

        initFacesContext();

        if (facesTestCase.sendWriterToFile()) {
            ResponseWriter responseWriter =
                  new FileOutputResponseWriter(testRootDir);
            facesContext.setResponseWriter(responseWriter);
        }

        com.sun.faces.cactus.TestBean testBean = new com.sun.faces.cactus.TestBean();
        facesContext.getExternalContext().getSessionMap().put("TestBean",
                                                              testBean);

        UIViewRoot root = getFacesContext().getApplication().getViewHandler()
              .createView(facesContext, null);
        root.setViewId("/viewId");
        facesContext.setViewRoot(root);

        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);

    }


    public void tearDown() {
        // make sure this gets called!
        if (this.servletContext != null) {
            this.servletContext
                  .removeAttribute(RIConstants.HTML_BASIC_RENDER_KIT);
        }

        ServletContextEvent e =
              new ServletContextEvent(this.servletContext);
        configureListener.contextDestroyed(e);

        // make sure session is not null. It will null in case release
        // was invoked.
        try {
            if (facesContext.getExternalContext() != null) {
                if (facesContext.getExternalContext().getSession(true) !=
                    null) {
                    facesContext.getExternalContext().getSessionMap().remove(
                          "TestBean");
                }
            }
        } catch (IllegalStateException ie) {
        }
        this.servletContext = null;
    }


    public boolean verifyExpectedOutput() {
        boolean result = false;
        CompareFiles cf = new CompareFiles();
        String errorMessage = null;
        String outputFileName = null;
        String correctFileName = null;

        // If this testcase doesn't participate in file comparison
        if (!facesTestCase.sendResponseToFile() &&
            (!facesTestCase.sendWriterToFile()) &&
            (null == facesTestCase.getExpectedOutputFilename())) {
            return true;
        }

        if (facesTestCase.sendResponseToFile()) {
            outputFileName = FileOutputResponseWrapper.FACES_RESPONSE_FILENAME;
        } else {
            outputFileName = FileOutputResponseWriter.RESPONSE_WRITER_FILENAME;
        }
        correctFileName = FileOutputResponseWriter.FACES_RESPONSE_ROOT +
                          facesTestCase.getExpectedOutputFilename();

        errorMessage = "File Comparison failed: diff -u " + outputFileName +
                       " " +
                       correctFileName;

        ArrayList ignoreList = null;
        String[] ignore = null;

        if (null != (ignore = facesTestCase.getLinesToIgnore())) {
            ignoreList = new ArrayList();
            for (int i = 0; i < ignore.length; i++) {
                ignoreList.add(ignore[i]);
            }
        }

        try {
            result =
                  cf.filesIdentical(outputFileName,
                                    correctFileName,
                                    ignoreList);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        if (!result) {
            System.out.println(errorMessage);
        }
        System.out.println("VERIFY:" + result);
        return result;
    }

    /**
     * <p>verify that the objects passed as paremeters obey the general contract
     * of equals(), as listed in Item 7 of Bloch's Effective Java.
     */
    public void verifyEqualsContractPositive(Object x, Object y, Object z) {

        Assert.assertTrue(x.equals(x));
        Assert.assertTrue(x.equals(y) && y.equals(x));
        Assert.assertTrue(x.equals(y) && y.equals(z) && x.equals(z));
        Assert.assertTrue(x.equals(y));
        Assert.assertTrue(x.equals(y));
        Assert.assertTrue(x.equals(y));
        Assert.assertTrue(y.equals(z));
        Assert.assertTrue(y.equals(z));
        Assert.assertTrue(y.equals(z));
        Assert.assertFalse(x.equals(null));
        Assert.assertFalse(y.equals(null));
        Assert.assertFalse(z.equals(null));
    }


    /**
     * This utiity method searches the specified file (line by line) for
     * the occurence of the specified string.
     */
    public boolean verifyExpectedStringInOutput(String str) {
        boolean exists = false;
        String outputFileName = null;

        // If this testcase doesn't deal with output file(s)
        if (!facesTestCase.sendResponseToFile() &&
            (!facesTestCase.sendWriterToFile()) &&
            (null == facesTestCase.getExpectedOutputFilename())) {
            return true;
        }

        if (facesTestCase.sendResponseToFile()) {
            outputFileName = FileOutputResponseWrapper.FACES_RESPONSE_FILENAME;
        } else {
            outputFileName = FileOutputResponseWriter.RESPONSE_WRITER_FILENAME;
        }

        try {
            File fileToCheck = new File(outputFileName);
            FileReader fileReader = new FileReader(fileToCheck);
            LineNumberReader lineReader = new LineNumberReader(fileReader);
            String line = lineReader.readLine().trim();

            while (null != line) {
                if (line.indexOf(str) >= 0) {
                    exists = true;
                    break;
                }
                line = lineReader.readLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return exists;
    }


    public boolean isMember(String toTest, String[] set) {
        int
              len = set.length,
              i = 0;
        for (i = 0; i < len; i++) {
            if (set[i].equals(toTest)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @return true iff every element in subset is present in the iterator.
     */

    public boolean isSubset(String[] subset, Iterator superset) {
        int i, len = subset.length;
        boolean[] hits = new boolean[len];
        String cur = null;
        for (i = 0; i < len; i++) {
            hits[i] = false;
        }


        // for each element in the superset, go through the entire subset,
        // marking our "hits" array if there is a match.
        while (superset.hasNext()) {
            cur = (String) superset.next();
            for (i = 0; i < len; i++) {
                if (cur.equals(subset[i])) {
                    hits[i] = true;
                }
            }
        }

        // if any of the hits array is false, return false;
        for (i = 0; i < len; i++) {
            if (!hits[i]) {
                return false;
            }
        }
        return true;
    }


    /**
     * <p>This method allows comparing the attribute sets of two
     * HttpServletRequests for equality.</p>
     *
     * @return true if every attribute in in request1 has an analog in
     *         request2, with the same value as in request1, and the converse is
     *         true as well.
     */


    public boolean requestsHaveSameAttributeSet(HttpServletRequest request1,
                                                HttpServletRequest request2) {
        Enumeration attrNames = request1.getAttributeNames();
        String curAttr;
        Object valA, valB;

        // make sure every name/value pair in request1 is the same
        // name/value pare in request2.

        while (attrNames.hasMoreElements()) {
            curAttr = (String) attrNames.nextElement();
            valA = request1.getAttribute(curAttr);
            valB = request2.getAttribute(curAttr);
            if (null != valA && null != valB) {
                // if any of the values are not equal, return false
                if (!valA.equals(valB)) {
                    return false;
                }
            } else if (null != valA || null != valB) {
                // one of the values is null, therefore, not equal, 
                // return false
                return false;
            }
        }

        // make sure every name/value pair in request2 is the same
        // name/value pare in request1.
        attrNames = request2.getAttributeNames();

        while (attrNames.hasMoreElements()) {
            curAttr = (String) attrNames.nextElement();
            valA = request2.getAttribute(curAttr);
            valB = request1.getAttribute(curAttr);
            if (null != valA && null != valB) {
                // if any of the values are not equal, return false
                if (!valA.equals(valB)) {
                    return false;
                }
            } else if (null != valA || null != valB) {
                // one of the values is null, therefore, not equal, 
                // return false
                return false;
            }
        }
        return true;
    }


    public void loadFromInitParam(String paramValue) {
        final String paramVal = paramValue;

        // clear out the attr that was set in the servletcontext attr set.
        this.servletContext.removeAttribute(
              FacesServlet.CONFIG_FILES_ATTR);
        ApplicationAssociate.clearInstance(
              FacesContext.getCurrentInstance().getExternalContext());
        // clear out the renderKit factory
        FactoryFinder.releaseFactories();

        // work around a bug in cactus where calling
        // config.setInitParameter() doesn't cause
        // servletContext.getInitParameter() to relfect the call.

        ServletContextWrapper sc =
              new ServletContextWrapper(this.servletContext) {
                  public String getInitParameter(String theName) {
                      if (null != theName &&
                          theName.equals(FacesServlet.CONFIG_FILES_ATTR)) {
                          return paramVal;
                      }
                      return super.getInitParameter(theName);
                  }

                  public String getContextPath() {
                      return "/test";
                  }
              };

        ServletContextEvent e =
              new ServletContextEvent(sc);
        ApplicationAssociate.setCurrentInstance(null);
        configureListener.contextDestroyed(new ServletContextEvent(this.servletContext));
        servletContext = sc;

        configureListener = new ConfigureListener();
        configureListener.contextInitialized(e);

        initFacesContext();
    }

    public Object wrapRequestToHideParameters() {
        Object oldRequest =
              getFacesContext().getExternalContext().getRequest();

        HttpServletRequest wrapper =
              new HttpServletRequestWrapper((HttpServletRequest) oldRequest) {
                  public java.util.Enumeration getParameterNames() {
                      return new java.util.Enumeration() {
                          public boolean hasMoreElements() {
                              return (false);
                          }

                          public Object nextElement() {
                              return new java.util.NoSuchElementException();
                          }
                      };
                  }
              };
        getFacesContext().getExternalContext().setRequest(wrapper);
        return oldRequest;
    }

    public void unwrapRequestToShowParameters(Object oldRequest) {
        getFacesContext().getExternalContext().setRequest(oldRequest);
    }

    public void setModificationTime(String relativePath, long modTime) {
        String basePath = System.getProperty("explodedWarDir");
        if (null == basePath || 0 == basePath.length()) {
            throw new IllegalArgumentException("SystemProperty \'\'explodedWarDir\'\' not set or set to empty value.  Please set this value to be the fully qualified local file path to the exploded test war.");
        }
        File toMod = new File(basePath, relativePath);
        toMod.setLastModified(modTime);
    }

    private void initFacesContext() {
        HttpServletResponse response;
        if (facesTestCase.sendResponseToFile()) {
            response =
                  new FileOutputResponseWrapper(facesTestCase.getResponse(),
                                                facesTestCase.getTestRootDir());
        } else {
            response = facesTestCase.getResponse();
        }
        LifecycleFactory factory = (LifecycleFactory)
              FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        assert (null != factory);
        lifecycle = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        assert (null != lifecycle);

        facesContextFactory = (FacesContextFactory)
              FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        facesContext =
              facesContextFactory.getFacesContext(this.servletContext,
                                                  facesTestCase.getRequest(),
                                                  response, lifecycle);
    }


} // end of class FacesTestCaseService
