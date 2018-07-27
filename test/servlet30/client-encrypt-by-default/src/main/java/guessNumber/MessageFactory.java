/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package guessNumber;

import static javax.faces.FactoryFinder.APPLICATION_FACTORY;
import static javax.faces.application.FacesMessage.FACES_MESSAGES;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ValueExpression;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class MessageFactory extends Object {

    /**
     * This version of getMessage() is used in the RI for localizing RI specific messages.
     */

    public static FacesMessage getMessage(String messageId, Object params[]) {
        Locale locale = null;
        FacesContext context = FacesContext.getCurrentInstance();

        // context.getViewRoot() may not have been initialized at this point.
        if (context != null && context.getViewRoot() != null) {
            locale = context.getViewRoot().getLocale();
            if (locale == null) {
                locale = Locale.getDefault();
            }
        } else {
            locale = Locale.getDefault();
        }

        return getMessage(locale, messageId, params);
    }

    public static FacesMessage getMessage(Locale locale, String messageId, Object params[]) {
        String summary = null, detail = null, bundleName = null;
        ResourceBundle bundle = null;

        // see if we have a user-provided bundle
        if ((bundleName = getApplication().getMessageBundle()) != null) {
            if (null != (bundle = ResourceBundle.getBundle(bundleName, locale, getCurrentLoader(bundleName)))) {
                // see if we have a hit
                try {
                    summary = bundle.getString(messageId);
                    detail = bundle.getString(messageId + "_detail");
                } catch (MissingResourceException e) {
                }
            }
        }

        // We couldn't find a summary in the user-provided bundle
        if (summary == null) {
            // see if we have a summary in the app provided bundle
            bundle = ResourceBundle.getBundle(FACES_MESSAGES, locale, getCurrentLoader(bundleName));
            if (bundle == null) {
                throw new NullPointerException();
            }

            // See if we have a hit
            try {
                summary = bundle.getString(messageId);
                detail = bundle.getString(messageId + "_detail");
            } catch (MissingResourceException e) {
            }
        }

        // We couldn't find a summary anywhere! Return null
        if (summary == null) {
            return null;
        }

        if (null == summary || null == bundle) {
            throw new NullPointerException(" summary " + summary + " bundle " + bundle);
        }

        // At this point, we have a summary and a bundle.
        return new BindingFacesMessage(locale, summary, detail, params);
    }

    //
    // Methods from MessageFactory
    //
    public static FacesMessage getMessage(FacesContext context, String messageId) {
        return getMessage(context, messageId, null);
    }

    public static FacesMessage getMessage(FacesContext context, String messageId, Object params[]) {
        if (context == null || messageId == null) {
            throw new NullPointerException(" context " + context + " messageId " + messageId);
        }

        Locale locale = null;

        // viewRoot may not have been initialized at this point.
        if (context != null && context.getViewRoot() != null) {
            locale = context.getViewRoot().getLocale();
        } else {
            locale = Locale.getDefault();
        }

        if (null == locale) {
            throw new NullPointerException(" locale " + locale);
        }

        FacesMessage message = getMessage(locale, messageId, params);
        if (message != null) {
            return message;
        }
        locale = Locale.getDefault();

        return (getMessage(locale, messageId, params));
    }

    public static FacesMessage getMessage(FacesContext context, String messageId, Object param0) {
        return getMessage(context, messageId, new Object[] { param0 });
    }

    public static FacesMessage getMessage(FacesContext context, String messageId, Object param0, Object param1) {
        return getMessage(context, messageId, new Object[] { param0, param1 });
    }

    public static FacesMessage getMessage(FacesContext context, String messageId, Object param0, Object param1, Object param2) {
        return getMessage(context, messageId, new Object[] { param0, param1, param2 });
    }

    public static FacesMessage getMessage(FacesContext context, String messageId, Object param0, Object param1, Object param2, Object param3) {
        return getMessage(context, messageId, new Object[] { param0, param1, param2, param3 });
    }

    // Gets the "label" property from the component.
    public static Object getLabel(FacesContext context, UIComponent component) {
        Object o = component.getAttributes().get("label");
        if (o == null) {
            o = component.getValueExpression("label");
        }

        // Use the "clientId" if there was no label specified.
        if (o == null) {
            o = component.getClientId(context);
        }

        return o;
    }

    protected static Application getApplication() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            return (FacesContext.getCurrentInstance().getApplication());
        }
        ApplicationFactory afactory = (ApplicationFactory) FactoryFinder.getFactory(APPLICATION_FACTORY);

        return afactory.getApplication();
    }

    protected static ClassLoader getCurrentLoader(Object fallbackClass) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }

    /**
     * This class overrides FacesMessage to provide the evaluation of binding expressions in addition to
     * Strings. It is often the case, that a binding expression may reference a localized property value
     * that would be used as a substitution parameter in the message. For example:
     * <code>#{bundle.userLabel}</code> "bundle" may not be available until the page is rendered. The
     * "late" binding evaluation in <code>getSummary</code> and <code>getDetail</code> allow the
     * expression to be evaluated when that property is available.
     */
    static class BindingFacesMessage extends FacesMessage {
        private static final long serialVersionUID = 1L;

        BindingFacesMessage(Locale locale, String messageFormat, String detailMessageFormat,
                // array of parameters, both Strings and ValueExpressions
                Object[] parameters) {

            super(messageFormat, detailMessageFormat);
            this.locale = locale;
            this.parameters = parameters;
            if (parameters != null) {
                resolvedParameters = new Object[parameters.length];
            }
        }

        @Override
        public String getSummary() {
            String pattern = super.getSummary();
            resolveBindings();
            return getFormattedString(pattern, resolvedParameters);
        }

        @Override
        public String getDetail() {
            String pattern = super.getDetail();
            resolveBindings();
            return getFormattedString(pattern, resolvedParameters);
        }

        private void resolveBindings() {
            FacesContext context = null;
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    Object o = parameters[i];
                    if (o instanceof ValueExpression) {
                        if (context == null) {
                            context = FacesContext.getCurrentInstance();
                        }
                        o = ((ValueExpression) o).getValue(context.getELContext());
                    }
                    // to avoid 'null' appearing in message
                    if (o == null) {
                        o = "";
                    }
                    resolvedParameters[i] = o;
                }
            }
        }

        private String getFormattedString(String msgtext, Object[] params) {
            String localizedStr = null;

            if (params == null || msgtext == null) {
                return msgtext;
            }
            StringBuffer b = new StringBuffer(100);
            MessageFormat mf = new MessageFormat(msgtext);
            if (locale != null) {
                mf.setLocale(locale);
                b.append(mf.format(params));
                localizedStr = b.toString();
            }
            return localizedStr;
        }

        private Locale locale;
        private Object[] parameters;
        private Object[] resolvedParameters;
    }
}
