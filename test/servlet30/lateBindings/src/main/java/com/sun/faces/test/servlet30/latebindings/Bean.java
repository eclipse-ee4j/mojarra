/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.latebindings;

import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.application.FacesMessage;


public class Bean {

    private boolean switchit;


    private Validator v1 = new CustomValidator1();
    private Validator v2 = new CustomValidator2();
    private Validator vret = v1;
    private Validator vnext = v2;
    public Validator getValidator() {
        if (switchit) {
            Validator tmp = vret;
            vret = vnext;
            vnext = tmp;
            switchit = false;
        }
        return vret;
    }

    private Converter c1 = new CustomConverter1();
    private Converter c2 = new CustomConverter2();
    private Converter cret = c1;
    private Converter cnext = c2;
    public Converter getConverter() {
        if (switchit) {
            Converter tmp = cret;
            cret = cnext;
            cnext = tmp;
            switchit = false;
        }
        return cret;
    }

    // ----------------------------------------------------------- Inner Classes

    private Validator val;
    public void setValidator2(Validator val) {
        System.out.println("setValidator2() -> " + val.getClass().getName());
        if (!(val instanceof LBValidator)) {
            throw new IllegalArgumentException("Expected LBValidator, received: " + val.getClass().getName());
        }
        this.val = val;
    }

    public Validator getValidator2() {
        return val;
    }

    private Converter con;
    public void setConverter2(Converter con) {
        System.out.println("setConverter2() -> " + con.getClass().getName());
        if (!(con instanceof LBConverter)) {
            throw new IllegalArgumentException("Expected LBConverter, received: " + con.getClass().getName());
        }
        this.con = con;
    }

    public Converter getConverter2() {
        return con;
    }
    private class CustomValidator1 implements Validator {

        public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
            switchit = true;
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                          "CustomValidator1 invoked",
                                                          "CustomValidator1 invoked"));
        }
    }

    private class CustomValidator2 implements Validator {

        public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
            switchit = true;
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                          "CustomValidator2 invoked",
                                                          "CustomValidator2 invoked"));
        }
    }

    private class CustomConverter1 implements Converter {

        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            switchit = true;
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                          "CustomConverter1 invoked",
                                                          "customConverter1 invoked"));
        }

        public String getAsString(FacesContext context, UIComponent component, Object value) {
            switchit = true; 
            return value.toString();
        }

    }

    private class CustomConverter2 implements Converter {

        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            switchit = true;
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                          "CustomConverter2 invoked",
                                                          "customConverter2 invoked"));
        }

        public String getAsString(FacesContext context, UIComponent component, Object value) {
            switchit = true;
            return value.toString();
        }

    }
}
