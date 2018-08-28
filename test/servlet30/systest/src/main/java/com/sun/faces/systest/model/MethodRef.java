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

package com.sun.faces.systest.model;

import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionValueChangeListener;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpSession;

import com.sun.faces.systest.TestValueChangeListener;

public class MethodRef extends Object {

    public MethodRef() {
    }

    protected String buttonPressedOutcome = null;

    public String getButtonPressedOutcome() {
        return buttonPressedOutcome;
    }

    public void setButtonPressedOutcome(String newButtonPressedOutcome) {
        buttonPressedOutcome = newButtonPressedOutcome;
    }

    public String button1Pressed() {
        setButtonPressedOutcome("button1 was pressed");
        return null;
    }

    public String invalidateSession() {
        FacesContext fContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) fContext.getExternalContext().getSession(true);
        session.invalidate();
        return null;
    }

    public String button2Pressed() {
        setButtonPressedOutcome("button2 was pressed");
        return null;
    }

    public void button3Pressed(ActionEvent event) {
        setButtonPressedOutcome(event.getComponent().getId() + " was pressed");
    }

    protected String validateOutcome;

    public String getValidateOutcome() {
        return validateOutcome;
    }

    public void setValidateOutcome(String newValidateOutcome) {
        validateOutcome = newValidateOutcome;
    }

    public void validateInput(FacesContext context, UIComponent toValidate, Object valueObj) {
        String value = (String) valueObj;
        if (!value.equals("batman")) {
            throw new ValidatorException(new FacesMessage("You didn't enter batman", "You must enter batman"));
        }

    }

    protected String changeOutcome;

    public String getChangeOutcome() {
        return changeOutcome;
    }

    public void setChangeOutcome(String newChangeOutcome) {
        changeOutcome = newChangeOutcome;
    }

    public void valueChange(ValueChangeEvent vce) {
        vce.getComponent().getAttributes().put("onblur", vce.getNewValue().toString());
        setChangeOutcome(vce.getNewValue().toString());
    }

    public void inputFieldValueChange(ValueChangeEvent vce) {
        vce.getComponent().getAttributes().put("onblur", vce.getNewValue().toString());
    }

    protected UIInput inputField = null;

    public void setInputField(UIInput input) {
        this.inputField = input;
    }

    public UIInput getInputField() {
        if (inputField == null) {
            inputField = new UIInput();
            inputField.addValueChangeListener(new TestValueChangeListener());

            Class<?> args[] = { ValueChangeEvent.class };

            MethodExpression mb = FacesContext.getCurrentInstance().getApplication().getExpressionFactory().createMethodExpression(
                    FacesContext.getCurrentInstance().getELContext(), "#{methodRef.inputFieldValueChange}", Object.class, args);

            inputField.addValueChangeListener(new MethodExpressionValueChangeListener(mb));
        }

        return inputField;
    }

}
