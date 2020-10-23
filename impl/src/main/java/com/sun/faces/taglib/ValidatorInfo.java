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

import org.xml.sax.Attributes;

/**
 *
 *
 */
public class ValidatorInfo {

    // *********************************************************************
    // Validation and configuration state (protected)
    private String nameSpace;
    private String localName;
    private String qName;
    private Attributes attributes;
    private FacesValidator validator;
    private String prefix;
    private String uri;

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getLocalName() {
        return localName;
    }

    public void setQName(String qName) {
        this.qName = qName;
    }

    public String getQName() {
        return qName;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setValidator(FacesValidator validator) {
        this.validator = validator;
    }

    public FacesValidator getValidator() {
        return validator;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        StringBuffer mesg = new StringBuffer();

        mesg.append("\nValidatorInfo NameSpace: ");
        mesg.append(nameSpace);
        mesg.append("\nValidatorInfo LocalName: ");
        mesg.append(localName);
        mesg.append("\nValidatorInfo QName: ");
        mesg.append(qName);
        for (int i = 0; i < attributes.getLength(); i++) {
            mesg.append("\nValidatorInfo attributes.getQName(");
            mesg.append(i);
            mesg.append("): ");
            mesg.append(attributes.getQName(i));
            mesg.append("\nValidatorInfo attributes.getValue(");
            mesg.append(i);
            mesg.append("): ");
            mesg.append(attributes.getValue(i));
        }
        mesg.append("\nValidatorInfo prefix: ");
        mesg.append(prefix);
        mesg.append("\nValidatorInfo uri: ");
        mesg.append(uri);

        return mesg.toString();
    }
}
