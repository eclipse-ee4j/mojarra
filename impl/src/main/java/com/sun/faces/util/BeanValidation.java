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

package com.sun.faces.util;

import static jakarta.faces.validator.BeanValidator.VALIDATOR_FACTORY_KEY;
import static jakarta.validation.Validation.buildDefaultValidatorFactory;

import java.util.Locale;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;

/**
 * Various utility methods for use with the Jakarta Bean Validation API in Jakarta Faces.
 *
 */
public class BeanValidation {

    public static Validator getBeanValidator(FacesContext context) {
        ValidatorFactory validatorFactory = getValidatorFactory(context);

        ValidatorContext validatorContext = validatorFactory.usingContext();
        MessageInterpolator jsfMessageInterpolator = new JsfAwareMessageInterpolator(context, validatorFactory.getMessageInterpolator());
        validatorContext.messageInterpolator(jsfMessageInterpolator);

        return validatorContext.getValidator();
    }

    public static ValidatorFactory getValidatorFactory(FacesContext context) {
        ValidatorFactory validatorFactory = null;

        Object cachedObject = context.getExternalContext().getApplicationMap().get(VALIDATOR_FACTORY_KEY);

        if (cachedObject instanceof ValidatorFactory) {
            validatorFactory = (ValidatorFactory) cachedObject;
        } else {
            try {
                validatorFactory = buildDefaultValidatorFactory();
            } catch (ValidationException e) {
                throw new FacesException("Could not build a default Bean Validator factory", e);
            }

            context.getExternalContext().getApplicationMap().put(VALIDATOR_FACTORY_KEY, validatorFactory);
        }

        return validatorFactory;
    }

    private static class JsfAwareMessageInterpolator implements MessageInterpolator {

        private final FacesContext context;
        private final MessageInterpolator delegate;

        public JsfAwareMessageInterpolator(FacesContext context, MessageInterpolator delegate) {
            this.context = context;
            this.delegate = delegate;
        }

        @Override
        public String interpolate(String message, MessageInterpolator.Context context) {
            Locale locale = this.context.getViewRoot().getLocale();
            if (locale == null) {
                locale = Locale.getDefault();
            }
            return delegate.interpolate(message, context, locale);
        }

        @Override
        public String interpolate(String message, MessageInterpolator.Context context, Locale locale) {
            return delegate.interpolate(message, context, locale);
        }
    }

}
