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
 * $Id: HtmlUnitFacesTestCase.java,v 1.16 2006/07/31 23:05:00 rlubke Exp $
 */



package com.sun.faces.test.servlet30.systest;

import static com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX_45;
import static com.gargoylesoftware.htmlunit.BrowserVersion.INTERNET_EXPLORER;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * <p>Abstract base class for test cases utilizing HtmlUnit.</p>
 */

public abstract class HtmlUnitFacesITCase extends TestCase {

    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public HtmlUnitFacesITCase(String name) {
        super(name);
        this.testName = name;
    }


    // ------------------------------------------------------ Instance Variables

    /**
     * The <code>Log</code> instance for this class.
     */
    protected static final Logger log = Logger.getLogger(HtmlUnitFacesITCase.class.getName());

    protected String testName;

    // System property values used to configure the HTTP connection
    protected String contextPath = null;
    protected String host = null;
    protected int port = 0;
    protected Integer virtualServerPort = null;

    // The current session identifier
    protected String sessionId = null;


    // The WebClient instance for this test case
    protected WebClient client = null;

    // The URL for our test application
    protected URL domainURL = null;

    // The cookie manager
    protected CookieManager cmanager = null;

    // The last requested page
    protected HtmlPage lastpage = null;

    // Possible containers - these should be the uppercase values of the possible container values in build.properties
    protected enum Container { GLASSFISH, GLASSFISHV3PRELUDE, GLASSFISHV3, GLASSFISHV3_1, GLASSFISHV3_1_NO_CLUSTER, TOMCAT6, TOMCAT7, WLS_10_3_4_NO_CLUSTER, WLS_12_1_1_NO_CLUSTER }

    // Per-container exclusions
    protected Map<Container, Vector<String>> exclusions = null;

    // Instance numbers for clustering
    private List<Integer> instanceNumbers;

    protected boolean forceNoCluster = false;

    private Random rand = new Random();

    protected boolean isVirtualServer = false;


    // ---------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {

        BrowserVersion browserVersion;

        isVirtualServer = Boolean.parseBoolean(System.getProperty("virtual-server"));

        String instanceNumbersStr = System.getProperty("instance.numbers");
        if (null != instanceNumbersStr && 0 < instanceNumbersStr.length() &&
	    !("${instance.numbers}".equals(instanceNumbersStr))) {
            String [] strs = instanceNumbersStr.split(",");
            List<Integer> instNums = getInstanceNumbers();
            for (String cur : strs) {
                try {
                    instNums.add(Integer.parseInt(cur));
                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        String forceNoClusterStr = System.getProperty("force.no.cluster");
        forceNoCluster = (null != forceNoClusterStr && 0 < forceNoClusterStr.length());

        contextPath = "/jsf-systest";
        host = System.getProperty("integration.serverName");
        port = Integer.parseInt(System.getProperty("integration.serverPort"));
        String browser = System.getProperty("browser");

        if ("FF3".equals(browser)) {
            browserVersion = FIREFOX_45;
        } else if ("FF2".equals(browser)) {
            browserVersion = FIREFOX_45;
        } else if ("IE6".equals(browser)) {
            browserVersion = INTERNET_EXPLORER;
        } else {
            browserVersion = INTERNET_EXPLORER;
        }
        
        String proxyHost = System.getProperty("proxyHost");
        String proxyPort = System.getProperty("proxyPort");

        client = new WebClient(browserVersion);
        cmanager = client.getCookieManager();
        
        // Add an ajax controller to synchronize all ajax calls
        client.setAjaxController(new NicelyResynchronizingAjaxController());
        domainURL = getURL("/");
        WebRequest settings = new WebRequest(domainURL);
        if (null != proxyHost && null != proxyPort) {
            settings.setProxyHost(proxyHost);
            int proxyPortInt = Integer.parseInt(proxyPort);
            settings.setProxyPort(proxyPortInt);
            ProxyConfig config = client.getOptions().getProxyConfig();
            config.setProxyHost(proxyHost);
            config.setProxyPort(proxyPortInt);
        }

        WebResponse response = client.getWebConnection().getResponse(settings);
    }

    protected WebClient getClient() {
        return client;
    }

    protected List<Integer> getInstanceNumbers() {
        if (instanceNumbers == null) {
            instanceNumbers = new ArrayList<Integer>();
        }
        
        return instanceNumbers;
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return new TestSuite(HtmlUnitFacesITCase.class);
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {

        client = null;
        domainURL = null;
        cmanager = null;

    }


    // ------------------------------------------------- Individual Test Methods


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Extract and return the result of calling <code>asText()</code>
     * on the <code>&lt;body&gt;</code> element of this page.</p>
     *
     * @param page The <code>HtmlPage</code> to process
     */
    protected String getBodyText(HtmlPage page) {

        Object body = page.getDocumentElement().getElementsByTagName("body").get(0);

        if (body != null) {
            if (body instanceof HtmlBody) {
                return (((HtmlBody) body).asText());
            }
        }

        fail("This page does not have a <body> element");
        return (null); // To satisfy the compiler

    }


    /**
     * <p>Return the page for the specified context-relative path,
     * maintaining session affinity if <code>sessionId</code> is not null.</p>
     *
     * @param path Context-relative part of the path
     */
    protected HtmlPage getPage(String path) throws Exception {

        /* Cookies seem to be maintained automatically now
        if (sessionId != null) {
            //            System.err.println("Joining   session " + sessionId);
            client.addRequestHeader("Cookie", "JSESSIONID=" + sessionId);
        }
        */
        lastpage  = (HtmlPage) client.getPage(getURL(path));
        if (sessionId == null) {
            parseSession(lastpage);
        }
        return lastpage;

    }
    
    protected HtmlPage getPageWithRetry(String path, int retries) throws Exception {
        int i = 0;
        boolean success = false;
        do {
            try {
                getPage(path);
                success = true;
            } catch (FailingHttpStatusCodeException fhse) {
                Thread.sleep(3000);
            }                
        } while (!success && ++i < retries);
        
        
        return lastpage;
    }

    protected HtmlPage getPageSticky(String path) throws Exception {

        /* Cookies seem to be maintained automatically now
        if (sessionId != null) {
            //            System.err.println("Joining   session " + sessionId);
            client.addRequestHeader("Cookie", "JSESSIONID=" + sessionId);
        }
        */
        lastpage  = (HtmlPage) client.getPage(getURLSticky(path));
        if (sessionId == null) {
            parseSession(lastpage);
        }
        return lastpage;

    }

    protected HtmlPage getPageFromInstanceN(String path, int instanceNumber) throws Exception {

        /* Cookies seem to be maintained automatically now
        if (sessionId != null) {
            //            System.err.println("Joining   session " + sessionId);
            client.addRequestHeader("Cookie", "JSESSIONID=" + sessionId);
        }
        */
        lastpage  = (HtmlPage) client.getPage(getURLFromInstanceN(path, instanceNumber));
        if (sessionId == null) {
            parseSession(lastpage);
        }
        return lastpage;

    }

    protected int getPort() {

        if (null != virtualServerPort) {
            return virtualServerPort.intValue();
        }

        int result = port;
        List<Integer> instNums = getInstanceNumbers();
        if (!instNums.isEmpty() && 
            (!forceNoCluster || isVirtualServer)) {
            int instanceNumberIndex = rand.nextInt(instNums.size());
            try {
                String num = instNums.get(instanceNumberIndex).toString() + port;
                result = Integer.parseInt(num);

            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                throw e;
            }
        }

        if (isVirtualServer) {
            virtualServerPort = (Integer) result;
        }

        return result;
    }

    protected int getFirstPort() {
        int result = port;
        List<Integer> instNums = getInstanceNumbers();
        if (!instNums.isEmpty() && !forceNoCluster) {
            int instanceNumberIndex = 0;
            try {
                String num = instNums.get(instanceNumberIndex).toString() + port;
                result = Integer.parseInt(num);

            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                throw e;
            }
        }
        return result;
    }

    protected int getNthPort(int instanceNumber) {
        int result = port;
        if (!forceNoCluster) {
            try {
                String num = instanceNumber + "" + port;
                result = Integer.parseInt(num);

            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
                throw e;
            }
        }
        return result;
    }
    /**
     * The same as {@link #getPage(String)} except this uses the specified
     * WebClient.
     *
     * @param path   context-relative path
     * @param client WebClient
     * @return an HtmlPage instance
     * @throws Exception if an error occurs
     */
    protected HtmlPage getPage(String path, WebClient client) throws Exception {
        lastpage = (HtmlPage) client.getPage(getURL(path));
        if (sessionId == null) {
            parseSession(lastpage);
        }
        return lastpage;
    }


    /**
     * <p>Return a <code>URL</code> for the specified context-relative
     * path.</p>
     *
     * @param path Context relative path
     */
    protected URL getURL(String path) throws Exception {

        StringBuffer sb = new StringBuffer("http://");
        sb.append(host);
        int myPort = getPort();
        if (myPort != 80) {
            sb.append(":");
            sb.append("" + myPort);
        }
        sb.append(contextPath);
        sb.append(path);
//        if (log.isLoggable(Level.INFO)) {
//           log.info(sb.toString());
//        }
        return (new URL(sb.toString()));

    }

    protected URL getURLSticky(String path) throws Exception {

        StringBuffer sb = new StringBuffer("http://");
        sb.append(host);
        int myPort = getFirstPort();
        if (myPort != 80) {
            sb.append(":");
            sb.append("" + myPort);
        }
        sb.append(contextPath);
        sb.append(path);
        if (log.isLoggable(Level.INFO)) {
           log.info(sb.toString());
        }
        return (new URL(sb.toString()));

    }

    protected URL getURLFromInstanceN(String path, int instanceNumber) throws Exception {

        StringBuilder sb = new StringBuilder("http://");
        sb.append(host);
        int myPort = getNthPort(instanceNumber);
        if (myPort != 80) {
            sb.append(":");
            sb.append("").append(myPort);
        }
        sb.append(contextPath);
        sb.append(path);
        if (log.isLoggable(Level.INFO)) {
           log.info(sb.toString());
        }
        return (new URL(sb.toString()));

    }


    /**
     * <p>Parse and save any session identifier from the specified page.</p>
     *
     * @param page The current page
     */
    protected void parseSession(HtmlPage page) {

        String value =
                page.getWebResponse().getResponseHeaderValue("Set-Cookie");
        if (value == null) {
            return;
        }
        int equals = value.indexOf("JSESSIONID=");
        if (equals < 0) {
            return;
        }
        value = value.substring(equals + "JSESSIONID=".length());
        int semi = value.indexOf(";");
        if (semi >= 0) {
            value = value.substring(0, semi);
        }
        sessionId = value;
        //        System.err.println("Beginning session " + sessionId);

    }


    protected boolean clearAllCookies() {
        cmanager.clearCookies();
        return true;
    }

    // Return the form with the specified "id" from the specified page
    // (HtmlPage.getFormByName() looks at "name" instead)
    protected HtmlForm getFormById(HtmlPage page, String id) {

        Iterator forms = page.getForms().iterator();
        while (forms.hasNext()) {
            HtmlForm form = (HtmlForm) forms.next();
            if (id.equals(form.getAttribute("id"))) {
                return (form);
            }
        }
        return (null);

    }
    
    protected <T> List<T> getAllElementsOfGivenClass(HtmlPage root, Class<T> matchClass) {
        return getAllElementsOfGivenClass(root.getDocumentElement(), null, matchClass);
    }


    /**
     * <p>Added to compensate for changes in the HtmlUnit 1.4 API.</p>
     *
     * @see #getAllElementsOfGivenClass(com.gargoylesoftware.htmlunit.html.HtmlElement, java.util.List, Class)
     */
    protected List getAllElementsOfGivenClass(HtmlPage root, List list, Class matchClass) {
        return getAllElementsOfGivenClass(root.getDocumentElement(), list, matchClass);
    }

    /**
     * Depth first search from root to find all children that are
     * instances of HtmlInput.  Add them to the list.
     */
    protected List getAllElementsOfGivenClass(HtmlElement root, List list, Class matchClass) {
        if (null == root) {
            return list;
        }
        if (null == list) {
            list = new ArrayList();
        }
        Iterable<DomElement> iterable = root.getChildElements();
        Iterator<DomElement> iter = iterable.iterator();
        while (iter.hasNext()) {
            getAllElementsOfGivenClass((HtmlElement) iter.next(), list, matchClass);
        }
        if (matchClass.isInstance(root)) {
            if (!list.contains(root)) {
                list.add(root);
            }
        }
        return list;
    }

    protected HtmlInput getInputContainingGivenId(HtmlPage root,
                                                  String id) {
        List list;
        int i;
        HtmlInput result = null;

        list = getAllElementsOfGivenClass(root, null, HtmlInput.class);
        for (i = 0; i < list.size(); i++) {
            result = (HtmlInput) list.get(i);
            if (-1 != result.getAttribute("id").indexOf(id)) {
                break;
            }
            result = null;
        }
        return result;

    }

    protected HtmlInput getNthInputContainingGivenId(HtmlPage root,
                                                     String id,
                                                     int whichInput) {
        List list;
        int i, hitCount = 0;
        HtmlInput result = null;

        list = getAllElementsOfGivenClass(root, null, HtmlInput.class);
        for (i = 0; i < list.size(); i++) {
            result = (HtmlInput) list.get(i);
            if (-1 != result.getAttribute("id").indexOf(id) &&
                    hitCount++ == whichInput) {
                break;
            }
            result = null;
        }
        return result;

    }

    protected HtmlInput getNthFromLastInputContainingGivenId(HtmlPage root,
                                                             String id,
                                                             int whichInput) {
        List list;
        int i, hitCount = 0;
        HtmlInput result = null;

        list = getAllElementsOfGivenClass(root, null, HtmlInput.class);
        for (i = list.size() - 1; i >= 0; i--) {
            result = (HtmlInput) list.get(i);
            if (-1 != result.getAttribute("id").indexOf(id) &&
                    hitCount++ == whichInput) {
                break;
            }
            result = null;
        }
        return result;

    }

    protected String getText(String element) {
        return ((HtmlElement)lastpage.getHtmlElementById(element)).asText();
    }

    /*
      Check that the text of the element is equal to the supplied string
     */
    protected boolean check(String element, String expected) {
        return expected.equals(getText(element));
    }

    protected void checkTrue(String element, String expected) {
        assertTrue(element+":- Expected '"+expected+"', but received '"+getText(element)+"'", check(element,expected));
    }

    @Override
    protected void runTest() throws Throwable {
        String currentContainer = System.getProperty("container");
        boolean doRunTest = true;
        if(currentContainer == null) {
//            log.warning("Test exclusions not taken into account since no container property could be found");
        } else if(exclusions != null) {
            Vector<String> excludedTests = this.exclusions.get(Container.valueOf(currentContainer.toUpperCase().replaceAll("\\.", "_")));
            if(null != excludedTests && !excludedTests.isEmpty() && excludedTests.contains(testName)) {
                log.log(Level.INFO, "Skipping execution of test ''{0}'' for container {1}", new Object[]{testName, currentContainer});
                doRunTest = false;
            } 
        }
        if (doRunTest) {
            super.runTest();
        }
    }

    /**
     * Adds an exclusion for a particular test method in the TestCase
     * @param container the container for which the exclusion should be applied
     * @param testName the name of the test method to exclude
     */
    protected void addExclusion(Container container, String testName) {

        if(exclusions == null) {
            exclusions = new HashMap<Container, Vector<String>>();
        }
        
        Vector<String> excluded = this.exclusions.get(container);
        if(excluded == null) {
            excluded = new Vector<String>();
            exclusions.put(container, excluded);
        }
        excluded.add(testName);
    }
}
