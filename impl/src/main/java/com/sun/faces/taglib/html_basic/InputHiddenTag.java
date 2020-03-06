/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
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

package com.sun.faces.taglib.html_basic;

import jakarta.servlet.jsp.JspException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.webapp.UIComponentELTag;

/*
 * ******* GENERATED CODE - DO NOT EDIT *******
 */

public class InputHiddenTag extends UIComponentELTag {

    // Setter Methods
    // PROPERTY: converter
    private jakarta.el.ValueExpression converter;

    public void setConverter(jakarta.el.ValueExpression converter) {
        this.converter = converter;
    }

    // PROPERTY: converterMessage
    private jakarta.el.ValueExpression converterMessage;

    public void setConverterMessage(jakarta.el.ValueExpression converterMessage) {
        this.converterMessage = converterMessage;
    }

    // PROPERTY: immediate
    private jakarta.el.ValueExpression immediate;

    public void setImmediate(jakarta.el.ValueExpression immediate) {
        this.immediate = immediate;
    }

    // PROPERTY: required
    private jakarta.el.ValueExpression required;

    public void setRequired(jakarta.el.ValueExpression required) {
        this.required = required;
    }

    // PROPERTY: requiredMessage
    private jakarta.el.ValueExpression requiredMessage;

    public void setRequiredMessage(jakarta.el.ValueExpression requiredMessage) {
        this.requiredMessage = requiredMessage;
    }

    // PROPERTY: validator
    private jakarta.el.MethodExpression validator;

    public void setValidator(jakarta.el.MethodExpression validator) {
        this.validator = validator;
    }

    // PROPERTY: validatorMessage
    private jakarta.el.ValueExpression validatorMessage;

    public void setValidatorMessage(jakarta.el.ValueExpression validatorMessage) {
        this.validatorMessage = validatorMessage;
    }

    // PROPERTY: value
    private jakarta.el.ValueExpression value;

    public void setValue(jakarta.el.ValueExpression value) {
        this.value = value;
    }

    // PROPERTY: valueChangeListener
    private jakarta.el.MethodExpression valueChangeListener;

    public void setValueChangeListener(jakarta.el.MethodExpression valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    // General Methods
    @Override
    public String getRendererType() {
        return "jakarta.faces.Hidden";
    }

    @Override
    public String getComponentType() {
        return "jakarta.faces.HtmlInputHidden";
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        jakarta.faces.component.UIInput input = null;
        try {
            input = (jakarta.faces.component.UIInput) component;
        } catch (ClassCastException cce) {
            throw new IllegalStateException(
                    "Component " + component.toString() + " not expected type.  Expected: jakarta.faces.component.UIInput.  Perhaps you're missing a tag?");
        }

        if (converter != null) {
            if (!converter.isLiteralText()) {
                input.setValueExpression("converter", converter);
            } else {
                Converter conv = FacesContext.getCurrentInstance().getApplication().createConverter(converter.getExpressionString());
                input.setConverter(conv);
            }
        }

        if (converterMessage != null) {
            input.setValueExpression("converterMessage", converterMessage);
        }
        if (immediate != null) {
            input.setValueExpression("immediate", immediate);
        }
        if (required != null) {
            input.setValueExpression("required", required);
        }
        if (requiredMessage != null) {
            input.setValueExpression("requiredMessage", requiredMessage);
        }
        if (validator != null) {
            input.addValidator(new MethodExpressionValidator(validator));
        }
        if (validatorMessage != null) {
            input.setValueExpression("validatorMessage", validatorMessage);
        }
        if (value != null) {
            input.setValueExpression("value", value);
        }
        if (valueChangeListener != null) {
            input.addValueChangeListener(new MethodExpressionValueChangeListener(valueChangeListener));
        }
    }

    // Methods From TagSupport
    @Override
    public int doStartTag() throws JspException {
        try {
            return super.doStartTag();
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new JspException(root);
        }
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            return super.doEndTag();
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            throw new JspException(root);
        }
    }

    // RELEASE
    @Override
    public void release() {
        super.release();

        // component properties
        converter = null;
        converterMessage = null;
        immediate = null;
        required = null;
        requiredMessage = null;
        validator = null;
        validatorMessage = null;
        value = null;
        valueChangeListener = null;

        // rendered attributes
    }

    public String getDebugString() {
        return "id: " + getId() + " class: " + this.getClass().getName();
    }

}
