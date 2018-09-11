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

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;

@Named
@SessionScoped
public class Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean switchit;

    private Validator<Object> validator;
    private Validator<Object> v1 = new CustomValidator1();
    private Validator<Object> v2 = new CustomValidator2();
    private Validator<Object> vret = v1;
    private Validator<Object> vnext = v2;

    private Converter<Object> converter;
    private Converter<Object> c1 = new CustomConverter1();
    private Converter<Object> c2 = new CustomConverter2();
    private Converter<Object> cret = c1;
    private Converter<Object> cnext = c2;

    public Validator<Object> getValidator() {
        if (switchit) {
            Validator<Object> tmp = vret;
            vret = vnext;
            vnext = tmp;
            switchit = false;
        }

        return vret;
    }



    public Converter<Object> getConverter() {
        if (switchit) {
            Converter<Object> tmp = cret;
            cret = cnext;
            cnext = tmp;
            switchit = false;
        }

        return cret;
    }

    // ----------------------------------------------------------- Inner Classes



    public void setValidator2(Validator<Object> val) {
        System.out.println("setValidator2() -> " + val.getClass().getName());

        if (!(val instanceof LBValidator)) {
            throw new IllegalArgumentException("Expected LBValidator, received: " + val.getClass().getName());
        }

        this.validator = val;
    }

    public Validator<Object> getValidator2() {
        return validator;
    }

    public void setConverter2(Converter<Object> con) {
        System.out.println("setConverter2() -> " + con.getClass().getName());

        if (!(con instanceof LBConverter)) {
            throw new IllegalArgumentException("Expected LBConverter, received: " + con.getClass().getName());
        }

        this.converter = con;
    }

    public Converter<Object> getConverter2() {
        return converter;
    }

    private class CustomValidator1 implements Validator<Object> {

        @Override
        public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
            switchit = true;
            context.addMessage(null, new FacesMessage("CustomValidator1 invoked"));
        }
    }

    private class CustomValidator2 implements Validator<Object> {

        @Override
        public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
            switchit = true;
            context.addMessage(null, new FacesMessage("CustomValidator2 invoked"));
        }
    }

    private class CustomConverter1 implements Converter<Object> {

        @Override
        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            switchit = true;

            context.addMessage(null, new FacesMessage("CustomConverter1 invoked"));

            return value;
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Object value) {
            switchit = true;
            return value.toString();
        }

    }

    private class CustomConverter2 implements Converter<Object> {

        @Override
        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            switchit = true;

            context.addMessage(null, new FacesMessage("CustomConverter2 invoked"));

            return value;
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Object value) {
            switchit = true;
            return value.toString();
        }
    }
}
