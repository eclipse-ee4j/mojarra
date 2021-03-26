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

package com.sun.faces.composite;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionListener;
import javax.faces.event.ActionEvent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.application.FacesMessage;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.faces.convert.Converter;

@ManagedBean
@RequestScoped
public class CompositeBean {

    public List<String> getTableInputValues() {
        List<String> result = new ArrayList<String>();
        result.add("a value");
        return result;
    }

    public ActionListener getActionListener() {
        return new ActionListener() {

            public void processAction(ActionEvent event)
                  throws AbortProcessingException {
                FacesContext ctx = FacesContext.getCurrentInstance();
                UIComponent source = (UIComponent) event.getSource();
                String cid = source.getClientId(ctx);
                ctx.addMessage(cid,
                               new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                "Action Invoked : " + cid,
                                                "Action Invoked : " + cid));
            }
        };

    }

    private String text = "DEFAULT VALUE";
    public String getText() {
        return text;
    }

    // for #1966
    private List<String> defaultValueList = Arrays.asList("Item 1","Item 2");

    public List<String> getDefaultValueList() {
        return this.defaultValueList;
    }

    private List<String> emptyList = Collections.emptyList();

    public List<String> getEmptyList() {
        return this.emptyList;
    }

    public Color getColor() {
        return Color.PINK;
    }
    // end #1966

    // for #1986
    private BigDecimal bigDecimalValue = null;

    public BigDecimal getBigDecimalValue() {
        return bigDecimalValue;
    }
    // end #1986

    public void setBigDecimalValue(BigDecimal bigDecimalValue) {
        this.bigDecimalValue = bigDecimalValue;
    }


    public Validator getValidator() {

        return new TestValidator();

    }


    public Converter getConverter() {

        return new TestConverter();

    }


    public String doNav() {

        return "nestingNav";

    }


    public String action() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent c = UIComponent.getCurrentComponent(ctx);
        ctx.addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Action invoked: " + c.getClientId(ctx),
                                        "Action invoked: " + c.getClientId(ctx)));
        return "";

    }


    public String action(Object arg1, Object arg2) {

        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent c = UIComponent.getCurrentComponent(ctx);
        String message = "Action invoked: "
                             + c.getClientId(ctx)
                             + ", arg1: " + arg1.toString()
                             + ", arg2: " + arg2.toString();

        ctx.addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        message,
                                        message));
        return "";

    }

    public String custom() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent c = UIComponent.getCurrentComponent(ctx);
        ctx.addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Custom action invoked: " + c.getClientId(ctx),
                                        "Custom action invoked: " + c.getClientId(ctx)));
        return "";

    }

     public String custom(Object arg1, Object arg2) {

        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent c = UIComponent.getCurrentComponent(ctx);
        String message = "Custom action invoked: "
                             + c.getClientId(ctx)
                             + ", arg1: " + arg1.toString()
                             + ", arg2: " + arg2.toString();

        ctx.addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        message,
                                        message));
        return "";

    }


    public String display(String arg) {

        return "arg: " + arg;

    }


    public void actionListener(ActionEvent ae) {

        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent c = UIComponent.getCurrentComponent(ctx);
        ctx.addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "ActionListener invoked: " + c.getClientId(ctx),
                                        "ActionListener invoked: " + c.getClientId(ctx)));
    }


    public void validate(FacesContext ctx, UIComponent c, Object o) {

        ctx.addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "validator invoked: " + c.getClientId(ctx),
                                        "validator invoked: " + c.getClientId(ctx)));

    }


    public void valueChange(ValueChangeEvent event) {

        FacesContext ctx = FacesContext.getCurrentInstance();
        UIComponent c = event.getComponent();
        ctx.addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "ValueChange invoked: " + c.getClientId(ctx),
                                        "ValueChange invoked: " + c.getClientId(ctx)));

    }

    public Format getFormat() {
        Format result = new DecimalFormat();
        return result;
    }


    public String getStringValue() {

        return "equalityCheck";

    }

    public String[] getStringValues() {
        return new String[] { "equalityCheck", "failedCheck" };
    }

    public String[] getItems() {
        return new String[] { "A", "B", "C" };
    }

    public List<Integer> getTestValues() {
        List<Integer> values = new ArrayList<Integer>(1);
        values.add(1);
        return values;
    }


    // ---------------------------------------------------------- Nested Classes


    public static class TestValidator implements Validator {

        public void validate(FacesContext context, UIComponent component, Object value)
              throws ValidatorException {

            String cid = component.getClientId(context);
            context.addMessage(cid,
                               new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                "Validator Invoked : " + cid,
                                                "Validator Invoked : " + cid));
        }
    }


    public static class TestConverter implements Converter {

        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            return value;
        }

        public String getAsString(FacesContext context, UIComponent component, Object value) {
            String cid = component.getClientId(context);
            context.addMessage(cid,
                               new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                "Converter Invoked : " + cid,
                                                "Converter Invoked : " + cid));
            return value.toString();
        }
    }

}
