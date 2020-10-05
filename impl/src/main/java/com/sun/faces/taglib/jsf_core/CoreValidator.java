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

package com.sun.faces.taglib.jsf_core;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.faces.taglib.FacesValidator;
import com.sun.faces.taglib.ValidatorInfo;

/**
 * <p>
 * A TagLibrary Validator class to allow a TLD to mandate that Faces tag must have an id if it is a child or sibling of a
 * Jakarta Standard Tag conditional or iteration tag
 * </p>
 *
 * @author Justyna Horwat
 */
public class CoreValidator extends FacesValidator {

    // *********************************************************************
    // Constants

    // *********************************************************************
    // Validation and configuration state (protected)

    private ValidatorInfo validatorInfo;
    private IdTagParserImpl idTagParser;
    private CoreTagParserImpl coreTagParser;

    // *********************************************************************
    // Constructor and lifecycle management

    /**
     * <p>
     * CoreValidator constructor
     * </p>
     */
    public CoreValidator() {
        super();
        init();
    }

    /**
     * <p>
     * Initialize state
     * </p>
     */
    @Override
    protected void init() {
        super.init();
        failed = false;
        validatorInfo = new ValidatorInfo();

        idTagParser = new IdTagParserImpl();
        idTagParser.setValidatorInfo(validatorInfo);

        coreTagParser = new CoreTagParserImpl();
        coreTagParser.setValidatorInfo(validatorInfo);

    }

    /**
     * <p>
     * Release and re-initialize state
     * </p>
     */
    @Override
    public void release() {
        super.release();
        init();
    }

    //
    // Superclass overrides.
    //

    /**
     * <p>
     * Get the validator handler
     * </p>
     */
    @Override
    protected DefaultHandler getSAXHandler() {
        if (java.beans.Beans.isDesignTime()) {
            return null;
        }
        return new CoreValidatorHandler();
    }

    /**
     * <p>
     * Create failure message from any failed validations
     * </p>
     *
     * @param prefix Tag library prefix
     * @param uri Tag library uri
     */
    @Override
    protected String getFailureMessage(String prefix, String uri) {
        // we should only get called if this Validator failed
        StringBuffer result = new StringBuffer();

        if (idTagParser.getMessage() != null) {
            result.append(idTagParser.getMessage());
        }
        if (coreTagParser.getMessage() != null) {
            result.append(coreTagParser.getMessage());
        }

        return result.toString();
    }

    // *********************************************************************
    // SAX handler

    /**
     * <p>
     * The handler that provides the base of the TLV implementation.
     * </p>
     */
    private class CoreValidatorHandler extends DefaultHandler {

        /**
         * Parse the starting element. If it is a specific Jakarta Standard Tag tag make sure that the nested Faces tags have IDs.
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
            validatorInfo.setValidator(CoreValidator.this);

            idTagParser.parseStartElement();

            if (idTagParser.hasFailed()) {
                failed = true;
            }

            coreTagParser.parseStartElement();

            if (coreTagParser.hasFailed()) {
                failed = true;
            }
        }

        /**
         * <p>
         * Parse the ending element. If it is a specific JSTL tag make sure that the nested count is decreased.
         * </p>
         *
         * @param ns Element name space.
         * @param ln Element local name.
         * @param qn Element QName.
         */
        @Override
        public void endElement(String ns, String ln, String qn) {
            validatorInfo.setNameSpace(ns);
            validatorInfo.setLocalName(ln);
            validatorInfo.setQName(qn);
            idTagParser.parseEndElement();
            coreTagParser.parseEndElement();

        }
    }
}
