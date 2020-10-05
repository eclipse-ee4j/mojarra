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

package com.sun.faces.taglib;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.faces.util.Util;

import jakarta.servlet.jsp.tagext.PageData;
import jakarta.servlet.jsp.tagext.TagLibraryValidator;
import jakarta.servlet.jsp.tagext.ValidationMessage;

/**
 * <p>
 * Base class for all faces TLVs
 * </p>
 *
 * @author Justyna Horwat
 * @author Ed Burns
 */
public abstract class FacesValidator extends TagLibraryValidator {

    // *********************************************************************
    // Constants

    // *********************************************************************
    // Validation and configuration state (protected)

    protected boolean failed; // did the page fail?

    // *********************************************************************
    // Constants

    private static final String JSF_CORE_URI = "http://java.sun.com/jsf/core";

    private static final String JSF_HTML_URI = "http://java.sun.com/jsf/html";

    private static final String JSTL_OLD_CORE_URI = "http://java.sun.com/jstl/core";

    private static final String JSTL_NEW_CORE_URI = "http://java.sun.com/jsp/jstl/core";

    // Prefix for Faces HTML tags
    protected String JSF_HTML_PRE = null;

    public String getJSF_HTML_PRE() {
        return JSF_HTML_PRE;
    }

    // Prefix for Faces CORE tags
    protected String JSF_CORE_PRE = null;

    public String getJSF_CORE_PRE() {
        return JSF_CORE_PRE;
    }

    // Prefix for Jakarta Standard Tag CORE tags
    protected String JSTL_CORE_PRE = null;

    public String getJSTL_CORE_PRE() {
        return JSTL_CORE_PRE;
    }

    // QName for Jakarta Standard Tag conditional tag
    protected String JSTL_IF_QN = ":if";

    public String getJSTL_IF_QN() {
        return JSTL_IF_QN;
    }

    // Local Name for Jakarta Standard Tag conditional tag
    protected String JSTL_IF_LN = "if";

    public String getJSTL_IF_LN() {
        return JSTL_IF_LN;
    }

    // QName for Jakarta Standard Tag conditional tag
    protected String JSTL_CHOOSE_QN = ":choose";

    public String getJSTL_CHOOSE_QN() {
        return JSTL_CHOOSE_QN;
    }

    // Local Name for Jakarta Standard Tag conditional tag
    protected String JSTL_CHOOSE_LN = "choose";

    public String getJSTL_CHOOSE_LN() {
        return JSTL_CHOOSE_LN;
    }

    // QName for Jakarta Standard Tag iterator tag
    protected String JSTL_FOREACH_QN = ":forEach";

    public String getJSTL_FOREACH_QN() {
        return JSTL_FOREACH_QN;
    }

    // Local Name for Jakarta Standard Tag iterator tag
    protected String JSTL_FOREACH_LN = "forEach";

    public String getJSTL_FOREACH_LN() {
        return JSTL_FOREACH_LN;
    }

    // QName for Jakarta Standard Tag iterator tag
    protected String JSTL_FORTOKENS_QN = ":forTokens";

    public String getJSTL_FORTOKENS_QN() {
        return JSTL_FORTOKENS_QN;
    }

    // Local Name for Jakarta Standard Tag iterator tag
    protected String JSTL_FORTOKENS_LN = "forTokens";

    public String getJSTL_FORTOKENS_LN() {
        return JSTL_FORTOKENS_LN;
    }

    // QName for Faces Form tag
    protected String JSF_FORM_QN = ":form";

    public String getJSF_FORM_QN() {
        return JSF_FORM_QN;
    }

    // Local Name for Faces Form tag
    protected String JSF_FORM_LN = "form";

    public String getJSF_FORM_LN() {
        return JSF_FORM_LN;
    }

    // QName for Faces subview tag
    protected String JSF_SUBVIEW_QN = ":subview";

    public String getJSF_SUBVIEW_QN() {
        return JSF_SUBVIEW_QN;
    }

    // Local Name for Faces subview tag
    protected String JSF_SUBVIEW_LN = "subview";

    public String getJSF_SUBVIEW_LN() {
        return JSF_SUBVIEW_LN;
    }

    // *********************************************************************
    // Constructor and lifecycle management

    public FacesValidator() {
        super();
        init();
    }

    protected void init() {
        failed = false;
    }

    @Override
    public void release() {
        super.release();
        init();
    }

    /**
     * <p>
     * Subclass override. If it returns null, the subclass is telling us: do not validate.
     * </p>
     */

    protected abstract DefaultHandler getSAXHandler();

    protected abstract String getFailureMessage(String prefix, String uri);

    // *********************************************************************
    // Validation entry point

    /**
     * Validate a Jakarta Server Pages page. Return an an array of Validation Messages if a validation failure occurs. Return null on
     * success.
     *
     * @param prefix Value of directive prefix argument.
     * @param uri Value of directive uri argument.
     * @param page JspData page object.
     * @return ValidationMessage[] An array of Validation messages.
     */
    @Override
    public synchronized ValidationMessage[] validate(String prefix, String uri, PageData page) {
        ValidationMessage[] result = null;
        try {

            // get a handler
            DefaultHandler h = getSAXHandler();

            // if the subclass doesn't want validation to ocurr
            if (null == h) {
                // don't validate
                return result;
            }

            // parse the page
            SAXParserFactory f = Util.createSAXParserFactory();
            f.setNamespaceAware(true);
            f.setValidating(true);
            SAXParser p = f.newSAXParser();
            p.parse(page.getInputStream(), h);

            // on validation failure generate error message
            if (failed) {
                result = vmFromString(getFailureMessage(prefix, uri));
            } else {
                // success
                result = null;
            }

        } catch (SAXException ex) {
            result = vmFromString(ex.toString());
        } catch (ParserConfigurationException | IOException ex) {
            result = vmFromString(ex.toString());
        }
        // Make sure all resources are released
        release();
        return result;
    }

    // *********************************************************************
    // Utility functions

    /**
     * Construct a ValidationMessage[] from a single String and no ID.
     *
     * @param message Message string.
     * @return ValidationMessage[] An array of Validation Messages.
     */
    private ValidationMessage[] vmFromString(String message) {
        return new ValidationMessage[] { new ValidationMessage(null, message) };
    }

    protected void debugPrintTagData(String ns, String ln, String qn, Attributes attrs) {
        int i = 0, len = attrs.getLength();
        System.out.println("nameSpace: " + ns + " localName: " + ln + " QName: " + qn);
        for (i = 0; i < len; i++) {
            System.out.println("\tlocalName: " + attrs.getLocalName(i));
            System.out.println("\tQName: " + attrs.getQName(i));
            System.out.println("\tvalue: " + attrs.getValue(i) + "\n");
        }
    }

    /**
     * This method provides for the ability of the TLV to use whatever user defined tag lib prefix is in the page to
     * recognize tags.
     */

    protected void maybeSnagTLPrefixes(String qName, Attributes attrs) {

        if (!qName.equals("jsp:root")) {
            return;
        }
        int colon, i = 0, len = attrs.getLength();
        String prefix = null, value = null;
        for (i = 0; i < len; i++) {
            if (null != (value = attrs.getValue(i)) && null != (qName = attrs.getQName(i))) {
                if (qName.startsWith("xmlns:") && 7 <= qName.length()) {
                    prefix = qName.substring(6);
                    switch (value) {
                    case JSF_CORE_URI:
                        JSF_CORE_PRE = prefix;
                        JSF_SUBVIEW_QN = JSF_CORE_PRE + JSF_SUBVIEW_QN;
                        break;
                    case JSF_HTML_URI:
                        JSF_HTML_PRE = prefix;
                        JSF_FORM_QN = JSF_HTML_PRE + JSF_FORM_QN;
                        break;
                    case JSTL_OLD_CORE_URI:
                    case JSTL_NEW_CORE_URI:
                        JSTL_CORE_PRE = prefix;
                        JSTL_IF_QN = JSTL_CORE_PRE + JSTL_IF_QN;
                        JSTL_CHOOSE_QN = JSTL_CORE_PRE + JSTL_CHOOSE_QN;
                        JSTL_FOREACH_QN = JSTL_CORE_PRE + JSTL_FOREACH_QN;
                        JSTL_FORTOKENS_QN = JSTL_CORE_PRE + JSTL_FORTOKENS_QN;
                        break;
                    }
                }
            }
        }
    }

}
