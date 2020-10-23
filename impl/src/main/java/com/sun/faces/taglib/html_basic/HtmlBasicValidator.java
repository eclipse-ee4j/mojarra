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

package com.sun.faces.taglib.html_basic;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.faces.taglib.FacesValidator;
import com.sun.faces.taglib.ValidatorInfo;

/**
 * <p>
 * Top level validator for the html_basic tld
 * </p>
 *
 * @author Justyna Horwat
 * @author Ed Burns
 */
public class HtmlBasicValidator extends FacesValidator {

    // *********************************************************************
    // Validation and configuration state (protected)
    private ValidatorInfo validatorInfo;
    private CommandTagParserImpl commandTagParser;

    // *********************************************************************
    // Constructor and lifecycle management

    public HtmlBasicValidator() {
        super();
        init();
    }

    @Override
    protected void init() {
        super.init();
        failed = false;
        validatorInfo = new ValidatorInfo();

        commandTagParser = new CommandTagParserImpl();
        commandTagParser.setValidatorInfo(validatorInfo);
    }

    @Override
    public void release() {
        super.release();
        init();
    }

    @Override
    protected DefaultHandler getSAXHandler() {
        // don't run the TLV if we're in designTime
        if (java.beans.Beans.isDesignTime()) {
            return null;
        }

        return new HtmlBasicValidatorHandler();
    }

    @Override
    protected String getFailureMessage(String prefix, String uri) {
        // we should only get called if this Validator failed

        StringBuffer result = new StringBuffer();

        if (commandTagParser.getMessage() != null) {
            result.append(commandTagParser.getMessage());
        }
        return result.toString();
    }

    // *********************************************************************
    // SAX handler

    /**
     * The handler that provides the base of the TLV implementation.
     */
    private class HtmlBasicValidatorHandler extends DefaultHandler {

        /**
         * Parse the starting element. Parcel out to appropriate handler method.
         *
         * @param ns Element name space.
         * @param ln Element local name.
         * @param qn Element QName.
         * @param attrs Element's Attribute list.
         */
        @Override
        public void startElement(String ns, String ln, String qn, Attributes attrs) {
            maybeSnagTLPrefixes(qn, attrs);
            validatorInfo.setNameSpace(ns);
            validatorInfo.setLocalName(ln);
            validatorInfo.setQName(qn);
            validatorInfo.setAttributes(attrs);

            commandTagParser.parseStartElement();
            if (commandTagParser.hasFailed()) {
                failed = true;
            }
        }

        /**
         * Parse the ending element. If it is a specific JSTL tag make sure that the nested count is decreased.
         *
         * @param ns Element namespace.
         * @param ln Element local name.
         * @param qn Element QName.
         */
        @Override
        public void endElement(String ns, String ln, String qn) {
        }
    }
}
