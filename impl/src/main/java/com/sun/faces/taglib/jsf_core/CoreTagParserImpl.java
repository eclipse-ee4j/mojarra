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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.xml.sax.Attributes;

import com.sun.faces.RIConstants;
import com.sun.faces.taglib.TagParser;
import com.sun.faces.taglib.ValidatorInfo;

/**
 * <p>
 * Parses the command tag attributes and verifies that the required attributes are present
 * </p>
 */
public class CoreTagParserImpl implements TagParser {

    // *********************************************************************
    // Validation and configuration state (protected)

    // PENDING(edburns): Make this localizable
    private StringBuffer failureMessages; // failureMessages
    private boolean failed;
    private ValidatorInfo validatorInfo;

    // *********************************************************************
    // Constructor and lifecycle management

    /**
     * <p>
     * CoreTagParserImpl constructor
     * </p>
     */
    public CoreTagParserImpl() {
        failed = false;
        failureMessages = new StringBuffer();
    }

    /**
     * <p>
     * Set the validator info object that has the current tag information
     * </p>
     *
     * @param validatorInfo object with current tag info
     */
    @Override
    public void setValidatorInfo(ValidatorInfo validatorInfo) {
        this.validatorInfo = validatorInfo;
    }

    /**
     * <p>
     * Get the failure message
     * </p>
     *
     * @return String Failure message
     */
    @Override
    public String getMessage() {
        return failureMessages.toString();
    }

    /**
     * <p>
     * Return false if validator conditions have not been met
     * </p>
     *
     * @return boolean false if validation conditions have not been met
     */
    @Override
    public boolean hasFailed() {
        return failed;
    }

    /**
     * <p>
     * Parse the starting element. Parcel out to appropriate handler method.
     * </p>
     */
    @Override
    public void parseStartElement() {

        String ns = validatorInfo.getNameSpace();
        String ln = validatorInfo.getLocalName();

        if (ns.equals(RIConstants.CORE_NAMESPACE)) {
            switch (ln) {
            case "valueChangeListener":
                handleListener();
                break;
            case "actionListener":
                handleListener();
                break;
            case "converter":
                handleConverter();
                break;
            case "validator":
                handleValidator();
                break;
            }
        }
    }

    /**
     * <p>
     * Parse the end element
     * </p>
     */
    @Override
    public void parseEndElement() {
        // no parsing required
    }

    // *********************************************************************
    // Private methods

    /**
     * <p>
     * Listener tags must have a "type" and/or "binding" attribute.
     * </p>
     * <p/>
     * <p>
     * PRECONDITION: qn is an actionListener or valueChangeListener
     * </p>
     */
    private void handleListener() {
        Attributes attrs = validatorInfo.getAttributes();
        String ln = validatorInfo.getLocalName();
        boolean hasType = false;
        boolean hasBinding = false;

        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getLocalName(i).equals("type")) {
                hasType = true;
            }
            if (attrs.getLocalName(i).equals("binding")) {
                hasBinding = true;
            }
        }
        if (failed = !hasBinding && !hasType) {
            Object[] obj = new Object[1];
            obj[0] = ln;
            ResourceBundle rb = ResourceBundle.getBundle(RIConstants.TLV_RESOURCE_LOCATION);
            failureMessages.append(MessageFormat.format(rb.getString("TLV_LISTENER_ERROR"), obj));
            failureMessages.append("\n");
        }
    }

    /**
     * <p>
     * Validator tag must have a "validatorId" and/or "binding" attribute.
     * </p>
     * <p/>
     * <p>
     * PRECONDITION: qn is a validator
     * </p>
     */
    private void handleValidator() {
        Attributes attrs = validatorInfo.getAttributes();
        String ln = validatorInfo.getLocalName();
        boolean hasValidatorId = false;
        boolean hasBinding = false;

        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getLocalName(i).equals("validatorId")) {
                hasValidatorId = true;
            }
            if (attrs.getLocalName(i).equals("binding")) {
                hasBinding = true;
            }
        }
        if (failed = !hasBinding && !hasValidatorId) {
            Object[] obj = new Object[1];
            obj[0] = ln;
            ResourceBundle rb = ResourceBundle.getBundle(RIConstants.TLV_RESOURCE_LOCATION);
            failureMessages.append(MessageFormat.format(rb.getString("TLV_VALIDATOR_ERROR"), obj));
            failureMessages.append("\n");
        }
    }

    /**
     * <p>
     * Converter tag must have a "converterId" and/or "binding" attribute.
     * </p>
     * <p/>
     * <p>
     * PRECONDITION: qn is a converter
     * </p>
     */
    private void handleConverter() {
        Attributes attrs = validatorInfo.getAttributes();
        String ln = validatorInfo.getLocalName();
        boolean hasConverterId = false;
        boolean hasBinding = false;

        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getLocalName(i).equals("converterId")) {
                hasConverterId = true;
            }
            if (attrs.getLocalName(i).equals("binding")) {
                hasBinding = true;
            }
        }
        if (failed = !hasBinding && !hasConverterId) {
            Object[] obj = new Object[1];
            obj[0] = ln;
            ResourceBundle rb = ResourceBundle.getBundle(RIConstants.TLV_RESOURCE_LOCATION);
            failureMessages.append(MessageFormat.format(rb.getString("TLV_CONVERTER_ERROR"), obj));
            failureMessages.append("\n");
        }
    }
}
