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

package com.sun.faces.application;

import com.sun.faces.util.MessageFactory;
import com.sun.faces.util.RequestStateManager;

import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

/**
 * Helper class to aid the ConverterPropertyEditorBase in converting properties.
 *
 * @author Mike Youngstrom
 */
public class PropertyEditorHelper {

    private Application app;

    public PropertyEditorHelper(Application app) {
        this.app = app;
    }

    /**
     * Convert the <code>textValue</code> to an object of type targetClass by delegating to a converter.
     */
    public Object convertToObject(Class<?> targetClass, String textValue) {
        UIComponent component = getComponent();
        Converter converter = app.createConverter(targetClass);
        if (null == converter) {
            // PENDING(edburns): I18N
            FacesException e = new FacesException(
                    "Cannot create Converter to convert value " + textValue + " to instance of target class " + targetClass.getName() + '.');
            throw e;
        }
        FacesContext currentInstance = FacesContext.getCurrentInstance();
        try {
            return converter.getAsObject(currentInstance, component, textValue);
        } catch (ConverterException ce) {
            addConversionErrorMessage(currentInstance, component, ce);
            return null;
        }
    }

    /**
     * Convert an object of type targetClass to text by delegating to a converter obtained from the Faces application.
     */
    public String convertToString(Class<?> targetClass, Object value) {
        UIComponent component = getComponent();
        Converter converter = app.createConverter(targetClass);
        if (null == converter) {
            // PENDING(edburns): I18N
            throw new FacesException("Cannot create Converter to convert " + targetClass.getName() + " value " + value + " to string.");
        }
        FacesContext currentInstance = FacesContext.getCurrentInstance();
        try {
            return converter.getAsString(currentInstance, component, value);
        } catch (ConverterException ce) {
            addConversionErrorMessage(currentInstance, component, ce);
            return null;
        }
    }

    /**
     * Return the {@link jakarta.faces.component.UIComponent} that is currently being processed.
     *
     * @return the current component, or null.
     */
    protected UIComponent getComponent() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            return (UIComponent) RequestStateManager.get(context, RequestStateManager.TARGET_COMPONENT_ATTRIBUTE_NAME);
        }
        return null;
    }

    /**
     * Add a conversion error message in the case of a PropertyEditor based conversion error.
     *
     * @param context
     * @param component
     * @param ce
     */
    protected void addConversionErrorMessage(FacesContext context, UIComponent component, ConverterException ce) {
        String converterMessageString = null;
        FacesMessage message;
        UIInput input;
        if (component instanceof UIInput) {
            input = (UIInput) component;
            converterMessageString = input.getConverterMessage();
            input.setValid(false);
        }
        if (null != converterMessageString) {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, converterMessageString, converterMessageString);
        } else {
            message = ce.getFacesMessage();
            if (message == null) {
                message = MessageFactory.getMessage(context, UIInput.CONVERSION_MESSAGE_ID);
                if (message.getDetail() == null) {
                    message.setDetail(ce.getMessage());
                }
            }
        }
        context.addMessage(component != null ? component.getClientId(context) : null, message);
    }
}
