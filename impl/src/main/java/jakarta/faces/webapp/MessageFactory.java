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

package jakarta.faces.webapp;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jakarta.el.ValueExpression;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 *
 * <p>
 * supported filters: <code>package</code> and <code>protection</code>.
 * </p>
 */

class MessageFactory {

    private static final String MOJARRA_RESOURCE_BASENAME = "com.sun.faces.resources.Messages";

    private MessageFactory() {
    }

    /**
     * @see #getMessage(String, Object...)
     * @param severity set a custom severity
     */
    static FacesMessage getMessage(String messageId, FacesMessage.Severity severity, Object... params) {
        FacesMessage message = getMessage(messageId, params);
        message.setSeverity(severity);
        return message;
    }

    /**
     * @see #getMessage(Locale, String, Object...)
     * @param severity set a custom severity
     */
    static FacesMessage getMessage(Locale locale, String messageId, FacesMessage.Severity severity, Object... params) {
        FacesMessage message = getMessage(locale, messageId, params);
        message.setSeverity(severity);
        return message;
    }

    /**
     * @see #getMessage(FacesContext, String, Object...)
     * @param severity set a custom severity
     */
    static FacesMessage getMessage(FacesContext context, String messageId, FacesMessage.Severity severity, Object... params) {
        FacesMessage message = getMessage(context, messageId, params);
        message.setSeverity(severity);
        return message;
    }

    /**
     * <p>
     * This version of getMessage() is used for localizing implementation specific messages.
     * </p>
     *
     * @param messageId - the key of the message in the resource bundle
     * @param params - substittion parameters
     *
     * @return a localized <code>FacesMessage</code> with the severity of FacesMessage.SEVERITY_ERROR
     */
    static FacesMessage getMessage(String messageId, Object... params) {
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

    /**
     * <p>
     * Creates and returns a FacesMessage for the specified Locale.
     * </p>
     *
     * @param locale - the target <code>Locale</code>
     * @param messageId - the key of the message in the resource bundle
     * @param params - substittion parameters
     *
     * @return a localized <code>FacesMessage</code> with the severity of FacesMessage.SEVERITY_ERROR
     */
    static FacesMessage getMessage(Locale locale, String messageId, Object... params) {
        String summary = null;
        String detail = null;
        ResourceBundle bundle;
        String bundleName;

        // see if we have a user-provided bundle
        Application app = getApplication();
        Class<?> appClass = app.getClass();
        if (null != (bundleName = app.getMessageBundle())) {
            if (null != (bundle = ResourceBundle.getBundle(bundleName, locale, getCurrentLoader(appClass)))) {
                // see if we have a hit
                try {
                    summary = bundle.getString(messageId);
                    detail = bundle.getString(messageId + "_detail");
                } catch (MissingResourceException e) {
                    // ignore
                }
            }
        }

        // we couldn't find a summary in the user-provided bundle
        if (null == summary) {
            // see if we have a summary in the app provided bundle
            bundle = ResourceBundle.getBundle(FacesMessage.FACES_MESSAGES, locale, getCurrentLoader(appClass));
            if (null == bundle) {
                throw new NullPointerException();
            }
            // see if we have a hit
            try {
                summary = bundle.getString(messageId);
                detail = bundle.getString(messageId + "_detail");
            } catch (MissingResourceException e) {
                // ignore
            }
        }

        // no hit found in the standard jakarta.faces.Messages bundle.
        // check the Mojarra resources
        if (summary == null) {
            // see if we have a summary in the app provided bundle
            bundle = ResourceBundle.getBundle(MOJARRA_RESOURCE_BASENAME, locale, getCurrentLoader(appClass));
            if (null == bundle) {
                throw new NullPointerException();
            }
            // see if we have a hit
            try {
                summary = bundle.getString(messageId);
            } catch (MissingResourceException e) {
                return null;
            }
        }

        // At this point, we have a summary and a bundle.
        FacesMessage ret = new BindingFacesMessage(locale, summary, detail, params);
        ret.setSeverity(FacesMessage.SEVERITY_ERROR);
        return ret;
    }

    /**
     * <p>
     * Creates and returns a FacesMessage for the specified Locale.
     * </p>
     *
     * @param context - the <code>FacesContext</code> for the current request
     * @param messageId - the key of the message in the resource bundle
     * @param params - substittion parameters
     *
     * @return a localized <code>FacesMessage</code> with the severity of FacesMessage.SEVERITY_ERROR
     */
    static FacesMessage getMessage(FacesContext context, String messageId, Object... params) {

        if (context == null || messageId == null) {
            throw new NullPointerException(" context " + context + " messageId " + messageId);
        }
        Locale locale;
        // viewRoot may not have been initialized at this point.
        if (context.getViewRoot() != null) {
            locale = context.getViewRoot().getLocale();
        } else {
            locale = Locale.getDefault();
        }

        if (null == locale) {
            throw new NullPointerException(" locale is null ");
        }

        FacesMessage message = getMessage(locale, messageId, params);
        if (message != null) {
            return message;
        }
        locale = Locale.getDefault();
        return getMessage(locale, messageId, params);
    }

    /**
     * <p>
     * Returns the <code>label</code> property from the specified component.
     * </p>
     *
     * @param context - the <code>FacesContext</code> for the current request
     * @param component - the component of interest
     *
     * @return the label, if any, of the component
     */
    static Object getLabel(FacesContext context, UIComponent component) {

        Object o = component.getAttributes().get("label");
        if (o == null || o instanceof String && ((String) o).length() == 0) {
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
            return FacesContext.getCurrentInstance().getApplication();
        }
        ApplicationFactory afactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
        return afactory.getApplication();
    }

    protected static ClassLoader getCurrentLoader(Class fallbackClass) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClassLoader();
        }
        return loader;
    }

    /**
     * This class overrides FacesMessage to provide the evaluation of binding expressions in addition to Strings. It is
     * often the case, that a binding expression may reference a localized property value that would be used as a
     * substitution parameter in the message. For example: <code>#{bundle.userLabel}</code> "bundle" may not be available
     * until the page is rendered. The "late" binding evaluation in <code>getSummary</code> and <code>getDetail</code> allow
     * the expression to be evaluated when that property is available.
     */
    static class BindingFacesMessage extends FacesMessage {
        /**
         *
         */
        private static final long serialVersionUID = 2026009902326193372L;
        BindingFacesMessage(Locale locale, String messageFormat, String detailMessageFormat,
                // array of parameters, both Strings and ValueBindings
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

        // fixme: if the locale is null, the output is null!! is it expected?
        private String getFormattedString(String msgtext, Object[] params) {
            String localizedStr = null;

            if (params == null || msgtext == null) {
                return msgtext;
            }
            StringBuilder b = new StringBuilder(100);
            MessageFormat mf = new MessageFormat(msgtext);
            if (locale != null) {
                mf.setLocale(locale);
                b.append(mf.format(params));
                localizedStr = b.toString();
            }
            return localizedStr;
        }

        private final Locale locale;
        private final Object[] parameters;
        private Object[] resolvedParameters;
    }

} // end of class MessageFactory
