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
public class CommandTagParserImpl implements TagParser {

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
     * CommandTagParserImpl constructor
     * </p>
     */
    public CommandTagParserImpl() {
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

        if (ns.equals(RIConstants.HTML_NAMESPACE)) {
            if (ln.equals("commandButton")) {
                handleCommandButton();
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
     * set failed flag to true unless tag has a value attribute
     * </p>
     * .
     * <p/>
     * <p>
     * PRECONDITION: qn is a commandButton
     * </p>
     */
    private void handleCommandButton() {
        Attributes attrs = validatorInfo.getAttributes();
        String ln = validatorInfo.getLocalName();
        boolean hasValue = false;
        boolean hasImage = false;
        boolean hasBinding = false;

        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getLocalName(i).equals("value")) {
                hasValue = true;
            }
            if (attrs.getLocalName(i).equals("image")) {
                hasImage = true;
            }
            if (attrs.getLocalName(i).equals("binding")) {
                hasBinding = true;
            }
        }
        if (failed = !hasBinding && !(hasValue || hasImage)) {
            Object[] obj = new Object[1];
            obj[0] = ln;
            ResourceBundle rb = ResourceBundle.getBundle(RIConstants.TLV_RESOURCE_LOCATION);
            failureMessages.append(MessageFormat.format(rb.getString("TLV_COMMAND_ERROR"), obj));
            failureMessages.append("\n");
        }

    }

}
