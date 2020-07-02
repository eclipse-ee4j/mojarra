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

// Util.java

package com.sun.faces.util;

import static com.sun.faces.util.MessageUtils.ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID;
import static com.sun.faces.util.MessageUtils.NAMED_OBJECT_NOT_FOUND_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.NULL_VIEW_ID_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.RequestStateManager.INVOCATION_PATH;
import static java.util.Collections.emptyList;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.AbortProcessingException;
import javax.faces.render.ResponseStateManager;
import javax.faces.webapp.FacesServlet;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.io.FastStringWriter;

/**
 * <B>Util</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 *
 */

public class Util {

    
    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    // README - make sure to add the message identifier constant
    // (ex: Util.CONVERSION_ERROR_MESSAGE_ID) and the number of substitution
    // parameters to test/com/sun/faces/util/TestUtil_messages (see comment there).

    /**
     * Flag that, when true, enables special behavior in the RI to enable
     * unit testing.
     */
    private static boolean unitTestModeEnabled = false;
    
    /**
     * RegEx patterns
     */
    private static final String PATTERN_CACKE_KEY = RIConstants.FACES_PREFIX + "patternCache";
    
    private static final String FACES_SERVLET_CLASS = FacesServlet.class.getName();


    private Util() {
        throw new IllegalStateException();
    }

    private static Map<String, Pattern> getPatternCache(Map<String, Object> appMap) {
        @SuppressWarnings("unchecked")
        Map<String, Pattern> result = (Map<String, Pattern>) appMap.get(PATTERN_CACKE_KEY);
        if (result == null) {
            result = new LRUMap<>(15);
            appMap.put(PATTERN_CACKE_KEY, result);
        }
        
        return result;
    }

    private static Map<String, Pattern> getPatternCache(ServletContext sc) {
        @SuppressWarnings("unchecked")
        Map<String, Pattern> result = (Map<String, Pattern>) sc.getAttribute(PATTERN_CACKE_KEY);
        if (result == null) {
            result = new LRUMap<>(15);
            sc.setAttribute(PATTERN_CACKE_KEY, result);
        }

        return result;
    }
    
    private static Collection<String> getFacesServletMappings(ServletContext servletContext) {
        ServletRegistration facesRegistration = getExistingFacesServletRegistration(servletContext);
        
        if (facesRegistration != null) {
            return facesRegistration.getMappings();
        }
        
        return emptyList();
    }
    
    private static ServletRegistration getExistingFacesServletRegistration(ServletContext servletContext) {
        Map<String,? extends ServletRegistration> existing = servletContext.getServletRegistrations();
        for (ServletRegistration registration : existing.values()) {
            if (FACES_SERVLET_CLASS.equals(registration.getClassName())) {
                return registration;
            }
        }
        
        return null;
    }

    /**
     * <p>
     * Convenience method for determining if the request associated
     * with the specified <code>FacesContext</code> is a PortletRequest
     * submitted by the JSR-301 bridge.
     * </p>
     * @param context the <code>FacesContext</code> associated with
     *  the request.
     */
    public static boolean isPortletRequest (FacesContext context) {
        return context.getExternalContext().getRequestMap().get("javax.portlet.faces.phase") != null;
    }
    
    public static String generateCreatedBy(FacesContext facesContext) {
        String applicationContextPath = "unitTest";
        try {
            applicationContextPath = facesContext.getExternalContext().getApplicationContextPath();
        } catch (Throwable e) {
            // ignore
        }
        
        return applicationContextPath + " " +  
               Thread.currentThread().toString() + " " +
               System.currentTimeMillis();
               
    }
    

    /**
     * <p>Factory method for creating the varius JSF listener
     *  instances that may be referenced by <code>type</code>
     *  or <code>binding</code>.</p>
     * <p>If <code>binding</code> is not <code>null</code>
     * and the evaluation result is not <code>null</code> return
     * that instance.  Otherwise try to instantiate an instances
     * based on <code>type</code>.</p>
     * 
     * @param type the <code>Listener</code> type
     * @param binding a <code>ValueExpression</code> which resolves
     *  to a <code>Listener</code> instance
     * @return a <code>Listener</code> instance based off the provided
     *  <code>type</code> and <binding>
     */
    public static Object getListenerInstance(ValueExpression type,
                                             ValueExpression binding) {

        FacesContext faces = FacesContext.getCurrentInstance();
        Object instance = null;
        if (faces == null) {
            return null;
        }
        if (binding != null) {
            instance = binding.getValue(faces.getELContext());
        }
        if (instance == null && type != null) {
            try {
                instance = ReflectionUtils.newInstance(((String) type.getValue(faces.getELContext())));
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AbortProcessingException(e.getMessage(), e);
            }

            if (binding != null) {
                binding.setValue(faces.getELContext(), instance);
            }
        }

        return instance;

    }

    public static void setUnitTestModeEnabled(boolean enabled) {
        unitTestModeEnabled = enabled;
    }

    public static boolean isUnitTestModeEnabled() {
        return unitTestModeEnabled;
    }

    public static TransformerFactory createTransformerFactory() {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         TransformerFactory factory;
         try {
             Thread.currentThread().setContextClassLoader(Util.class.getClassLoader());
             factory = TransformerFactory.newInstance();
         } finally {
             Thread.currentThread().setContextClassLoader(cl);
         }
         return factory;
     }

    public static SAXParserFactory createSAXParserFactory() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        SAXParserFactory factory;
        try {
            Thread.currentThread().setContextClassLoader(Util.class.getClassLoader());
            factory = SAXParserFactory.newInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return factory;
    }

    public static DocumentBuilderFactory createDocumentBuilderFactory() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        DocumentBuilderFactory factory;
        try {
            Thread.currentThread().setContextClassLoader(Util.class.getClassLoader());
            factory = DocumentBuilderFactory.newInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return factory;
    }

    public static SchemaFactory createSchemaFactory(String uri) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        SchemaFactory factory;
        try {
            Thread.currentThread().setContextClassLoader(Util.class.getClassLoader());
            factory = SchemaFactory.newInstance(uri);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return factory;
    }

    public static Class loadClass(String name, Object fallbackClass) throws ClassNotFoundException {
        ClassLoader loader = Util.getCurrentLoader(fallbackClass);

        String[] primitiveNames = { "byte", "short", "int", "long", "float", "double", "boolean", "char" };
        Class<?>[] primitiveClasses = { byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class, char.class };

        for (int i = 0; i < primitiveNames.length; i++) {
            if (primitiveNames[i].equals(name)) {
                return primitiveClasses[i];
            }
        }

        return Class.forName(name, true, loader);
    }
    
    public static Class<?> loadClass2(String name, Object fallbackClass) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = fallbackClass.getClass().getClassLoader();
            }
        
            return Class.forName(name, true, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<?> clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public static ClassLoader getCurrentLoader(Object fallbackClass) {
        ClassLoader loader = getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }

    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return (ClassLoader)
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                        @Override
                        public java.lang.Object run() {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    });
        }
    }

    /**
     * <p>
     * Identify and return the class loader that is associated with the calling web application.
     * </p>
     *
     * @throws FacesException if the web application class loader cannot be identified
     */
    public static ClassLoader getContextClassLoader2() throws FacesException {
        // J2EE 1.3 (and later) containers are required to make the
        // web application class loader visible through the context
        // class loader of the current thread.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new FacesException("getContextClassLoader");
        }
        
        return classLoader;
    }

    public static String removeAllButLastSlashPathSegment(String input) {
        // Trim the leading lastSlash, if any.
        if (input.charAt(0) == '/') {
            input = input.substring(1);
        }
        int len = input.length();
        // Trim the trailing lastSlash, if any.
        if (input.charAt(len - 1) == '/') {
            input = input.substring(0, len - 1);
        }
        
        // Trim any path segments that remain, leaving only the 
        // last path segment.
        int slash = input.lastIndexOf("/");
        
        // Do we have a "/"?
        if (-1 != slash) {
            input = input.substring(slash + 1);
        }
        
        return input;
    }
    
    public static String removeAllButNextToLastSlashPathSegment(String input) {
        // Trim the leading lastSlash, if any.
        if (input.charAt(0) == '/') {
            input = input.substring(1);
        }
        int len = input.length();
        // Trim the trailing lastSlash, if any.
        if (input.charAt(len - 1) == '/') {
            input = input.substring(0, len - 1);
        }
        
        // Trim any path segments that remain, leaving only the 
        // last path segment.
        int lastSlash = input.lastIndexOf("/");
        
        // Do we have a "/"?
        if (-1 != lastSlash) {
        
            int startOrPreviousSlash = input.lastIndexOf("/", lastSlash - 1);
            startOrPreviousSlash = (-1 == startOrPreviousSlash) ? 0 : startOrPreviousSlash;
            
            input = input.substring(startOrPreviousSlash, lastSlash);
        }
        
        return input;
    }
    
    public static String removeLastPathSegment(String input) {
        int slash = input.lastIndexOf("/");
        
        // Do we have a "/"?
        if (-1 != slash) {
            input = input.substring(0, slash);
        }
        
        return input;
    }

    public static void notNegative(String varname, long number) {
        if (number < 0) {
            throw new IllegalArgumentException("\"" + varname + "\" is negative");
        }
    }

    public static void notNull(String varname, Object var) {
        if (var == null) {
            throw new NullPointerException(
                  getExceptionMessageString(
                      NULL_PARAMETERS_ERROR_MESSAGE_ID, varname));
        }
    }
    
    public static void notNullViewId(String viewId) {
        if (viewId == null) {
            throw new IllegalArgumentException(
                    getExceptionMessageString(
                        NULL_VIEW_ID_ERROR_MESSAGE_ID));
        }
    }
    
    public static void notNullNamedObject(Object object, String objectId, String logMsg) {
        if (object == null) {
            Object[] params = { objectId };
            if (LOGGER.isLoggable(SEVERE)) {
                LOGGER.log(SEVERE, logMsg, params);
            }
            
            throw new FacesException(
                    getExceptionMessageString(
                        NAMED_OBJECT_NOT_FOUND_ERROR_MESSAGE_ID, params));
        }
    }
    
    public static void canSetAppArtifact(ApplicationAssociate applicationAssociate, String artifactName) {
        if (applicationAssociate.hasRequestBeenServiced()) {
            throw new IllegalStateException(
                    getExceptionMessageString(
                        ILLEGAL_ATTEMPT_SETTING_APPLICATION_ARTIFACT_ID, artifactName));
        }
    }
    
    public static void notNullAttribute(String attributeName, Object attribute) {
        if (attribute == null) {
            throw new FacesException("The \"" + attributeName + "\" attribute is required");
        }
    }
    
    public static ValueExpression getValueExpressionNullSafe(UIComponent component, String name) {
        ValueExpression valueExpression = component.getValueExpression(name);
        
        notNullAttribute(name, valueExpression);
        
        return valueExpression;
    }
    
    /**
     * Returns true if the given string is null or is empty.
     *
     * @param string The string to be checked on emptiness.
     * @return True if the given string is null or is empty.
     */
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Returns <code>true</code> if the given array is null or is empty.
     *
     * @param array The array to be checked on emptiness.
     * @return <code>true</code> if the given array is null or is empty.
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns <code>true</code> if the given collection is null or is empty.
     *
     * @param collection The collection to be checked on emptiness.
     * @return <code>true</code> if the given collection is null or is empty.
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * Returns <code>true</code> if the given value is null or is empty. Types of String, Collection, Map, Optional and Array are
     * recognized. If none is recognized, then examine the emptiness of the toString() representation instead.
     *
     * @param value The value to be checked on emptiness.
     * @return <code>true</code> if the given value is null or is empty.
     */
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        else if (value instanceof String) {
            return ((String) value).isEmpty();
        }
        else if (value instanceof Collection<?>) {
            return ((Collection<?>) value).isEmpty();
        }
        else if (value instanceof Map<?, ?>) {
            return ((Map<?, ?>) value).isEmpty();
        }
        else if (value instanceof Optional<?>) {
            return !((Optional<?>)value).isPresent();
        }
        else if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        else {
            return value.toString() == null || value.toString().isEmpty();
        }
    }
    
    /**
     * Returns true if all values are empty, false if at least one value is not empty.
     * @param values the values to be checked on emptiness
     * @return True if all values are empty, false otherwise
     */
    public static boolean isAllEmpty(Object... values) {
        for (Object value : values) {
            if (!isEmpty(value)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns <code>true</code> if at least one value is empty.
     *
     * @param values the values to be checked on emptiness
     * @return <code>true</code> if any value is empty and <code>false</code> if no values are empty
     */
    public static boolean isAnyEmpty(Object... values) {
        for (Object value : values) {
            if (isEmpty(value)) {
                return true;
            }
        }

        return false;
    }
    
    public static boolean isAllNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return false;
            }
        }

        return true;
    }
    
    public static boolean isAnyNull(Object... values) {
        for (Object value : values) {
            if (value == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the given object equals one of the given objects.
     * @param <T> The generic object type.
     * @param object The object to be checked if it equals one of the given objects.
     * @param objects The argument list of objects to be tested for equality.
     * @return <code>true</code> if the given object equals one of the given objects.
     */
    @SafeVarargs
    public static <T> boolean isOneOf(T object, T... objects) {
        for (Object other : objects) {
            if (object == null ? other == null : object.equals(other)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Returns the first non-<code>null</code> object of the argument list, or <code>null</code> if there is no such element.
     *
     * @param <T> The generic object type.
     * @param objects The argument list of objects to be tested for non-<code>null</code>.
     * @return The first non-<code>null</code> object of the argument list, or <code>null</code> if there is no such element.
     */
    @SafeVarargs
    public static <T> T coalesce(T... objects) {
        for (T object : objects) {
            if (object != null) {
                return object;
            }
        }

        return null;
    }
    
    public static <T> List<T> reverse(List<T> list) {
        int length = list.size();
        List<T> result = new ArrayList<>(length);

        for (int i = length - 1; i >= 0; i--) {
            result.add(list.get(i));
        }

        return result;
    }
    
    /**
     * Returns <code>true</code> if the given string starts with one of the given prefixes.
     * 
     * @param string The object to be checked if it starts with one of the given prefixes.
     * @param prefixes The argument list of prefixes to be checked
     * 
     * @return <code>true</code> if the given string starts with one of the given prefixes.
     */
    public static boolean startsWithOneOf(String string, String... prefixes) {
        if (prefixes == null) {
            return false;
        }
        
        for (String prefix : prefixes) {
            if (string.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param context the <code>FacesContext</code> for the current request
     * @return the Locale from the UIViewRoot, the the value of Locale.getDefault()
     */
    public static Locale getLocaleFromContextOrSystem(FacesContext context) {
        Locale result, temp = Locale.getDefault();
        UIViewRoot root;
        result = temp;
        if (null != context && null != (root = context.getViewRoot()) && null == (result = root.getLocale())) {
            result = temp;
        }
        return result;
    }


    public static Converter getConverterForClass(Class converterClass,
                                                 FacesContext context) {
        if (converterClass == null) {
            return null;
        }
        try {            
            Application application = context.getApplication();
            return (application.createConverter(converterClass));
        } catch (Exception e) {
            return (null);
        }
    }


    public static Converter getConverterForIdentifer(String converterId,
                                                     FacesContext context) {
        if (converterId == null) {
            return null;
        }
        try {            
            Application application = context.getApplication();
            return (application.createConverter(converterId));
        } catch (Exception e) {
            return (null);
        }
    }


    public static StateManager getStateManager(FacesContext context)
        throws FacesException {
        return (context.getApplication().getStateManager());
    }
    
    public static Class getTypeFromString(String type) throws ClassNotFoundException {
        Class result;
        switch (type) {
            case "byte":
                result = Byte.TYPE;
                break;
            case "short":
                result = Short.TYPE;
                break;
            case "int":
                result = Integer.TYPE;
                break;
            case "long":
                result = Long.TYPE;
                break;
            case "float":
                result = Float.TYPE;
                break;
            case "double":
                result = Double.TYPE;
                break;
            case "boolean":
                result = Boolean.TYPE;
                break;
            case "char":
                result = Character.TYPE;
                break;
            case "void":
                result = Void.TYPE;
                break;
            default:
                if (type.indexOf('.') == -1) {
                    type = "java.lang." + type;
                }   result = Util.loadClass(type, Void.TYPE);
                break;
        }

        return result;
    }


    public static ViewHandler getViewHandler(FacesContext context)
        throws FacesException {
        // Get Application instance
        Application application = context.getApplication();
        assert (application != null);

        // Get the ViewHandler
        ViewHandler viewHandler = application.getViewHandler();
        assert (viewHandler != null);

        return viewHandler;
    }


    public static boolean componentIsDisabled(UIComponent component) {
        return (Boolean.valueOf(String.valueOf(component.getAttributes().get("disabled"))));
    }

    public static boolean componentIsDisabledOrReadonly(UIComponent component) {
        return Boolean.valueOf(String.valueOf(component.getAttributes().get("disabled"))) || Boolean.valueOf(String.valueOf(component.getAttributes().get("readonly")));
    }


    // W3C XML specification refers to IETF RFC 1766 for language code
    // structure, therefore the value for the xml:lang attribute should
    // be in the form of language or language-country or
    // language-country-variant.

    public static Locale getLocaleFromString(String localeStr)
        throws IllegalArgumentException {
        // length must be at least 2.
        if (null == localeStr || localeStr.length() < 2) {
            throw new IllegalArgumentException("Illegal locale String: " +
                                               localeStr);
        }
        Locale result = null;
        
        try {
            Method method = Locale.class.getMethod("forLanguageTag", String.class);
            if (method != null) {
                result = (Locale) method.invoke(null, localeStr);
            }
        } catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException throwable) {
            // if we are NOT running JavaSE 7 we end up here and we will 
            // default to the previous way of determining the Locale below.
        }

        if (result == null || result.getLanguage().equals("")) {
            String lang = null;
            String country = null;
            String variant = null;
            char[] seps = {
                '-',
                '_'
            };
            int inputLength = localeStr.length();
            int i = 0;
            int j = 0;

            // to have a language, the length must be >= 2
            if ((inputLength >= 2) &&
                ((i = indexOfSet(localeStr, seps, 0)) == -1)) {
                // we have only Language, no country or variant
                if (2 != localeStr.length()) {
                    throw new
                        IllegalArgumentException("Illegal locale String: " +
                                                 localeStr);
                }
                lang = localeStr.toLowerCase();
            }

            // we have a separator, it must be either '-' or '_'
            if (i != -1) {
                lang = localeStr.substring(0, i);
                // look for the country sep.
                // to have a country, the length must be >= 5
                if ((inputLength >= 5) &&
                    ((j = indexOfSet(localeStr, seps, i + 1)) == -1)) {
                    // no further separators, length must be 5
                    if (inputLength != 5) {
                        throw new
                            IllegalArgumentException("Illegal locale String: " +
                                                     localeStr);
                    }
                    country = localeStr.substring(i + 1);
                }
                if (j != -1) {
                    country = localeStr.substring(i + 1, j);
                    // if we have enough separators for language, locale,
                    // and variant, the length must be >= 8.
                    if (inputLength >= 8) {
                        variant = localeStr.substring(j + 1);
                    } else {
                        throw new
                            IllegalArgumentException("Illegal locale String: " +
                                                     localeStr);
                    }
                }
            }
            if (variant != null && country != null && lang != null) {
                result = new Locale(lang, country, variant);
            } else if (lang != null && country != null) {
                result = new Locale(lang, country);
            } else if (lang != null) {
                result = new Locale(lang, "");
            }
        }
        
        return result;
    }

    /**
     * @param str local string
     * @param set the substring
     * @param fromIndex starting index
     * @return starting at <code>fromIndex</code>, the index of the
     *         first occurrence of any substring from <code>set</code> in
     *         <code>toSearch</code>, or -1 if no such match is found
     */
    public static int indexOfSet(String str, char[] set, int fromIndex) {
        int result = -1;
        for (int i = fromIndex, len = str.length(); i < len; i++) {
            for (int j = 0, innerLen = set.length; j < innerLen; j++) {
                if (str.charAt(i) == set[j]) {
                    result = i;
                    break;
                }
            }
            if (-1 != result) {
                break;
            }
        }
        return result;
    }


    /**
     * <p>Leverage the Throwable.getStackTrace() method to produce a String
     * version of the stack trace, with a "\n" before each line.</p>
     *
     * @param e the Throwable to obtain the stacktrace from
     *
     * @return the String representation ofthe stack trace obtained by calling
     *         getStackTrace() on the passed in exception.  If null is passed
     *         in, we return the empty String.
     */
    public static String getStackTraceString(Throwable e) {
        if (null == e) {
            return "";
        }

        StackTraceElement[] stacks = e.getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (StackTraceElement stack : stacks) {
            sb.append(stack.toString()).append('\n');
        }
        return sb.toString();
    }

    /**
     * <p>PRECONDITION: argument <code>response</code> is non-null and
     * has a method called <code>getContentType</code> that takes no
     * arguments and returns a String, with no side-effects.</p>
     *
     * <p>This method allows us to get the contentType in both the
     * servlet and portlet cases, without introducing a compile-time
     * dependency on the portlet api.</p>
     *
     * @param response the current response
     * @return the content type of the response
     */
    public static String getContentTypeFromResponse(Object response) {
        String result = null;
        if (null != response) {           

            try {
                Method method = ReflectionUtils.lookupMethod(
                      response.getClass(),
                      "getContentType",
                      RIConstants.EMPTY_CLASS_ARGS
                );
                if (null != method) {
                    Object obj =
                          method.invoke(response, RIConstants.EMPTY_METH_ARGS);
                    if (null != obj) {
                        result = obj.toString();
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new FacesException(e);
            }
        }
        return result;
    }

    public static FeatureDescriptor getFeatureDescriptor(String name, String
        displayName, String desc, boolean expert, boolean hidden, 
        boolean preferred, Object type, Boolean designTime) {
            
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setName(name);
        fd.setDisplayName(displayName);
        fd.setShortDescription(desc);
        fd.setExpert(expert);
        fd.setHidden(hidden);
        fd.setPreferred(preferred);
        fd.setValue(ELResolver.TYPE, type);
        fd.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, designTime);
        return fd;
    }
   

    /**
     * <p>A slightly more efficient version of 
     * <code>String.split()</code> which caches
     * the <code>Pattern</code>s in an LRUMap instead of
     * creating a new <code>Pattern</code> on each
     * invocation.</p>
     * @param appMap the Application Map
     * @param toSplit the string to split
     * @param regex the regex used for splitting
     * @return the result of <code>Pattern.spit(String, int)</code>
     */
    public synchronized static String[] split(Map<String, Object> appMap, String toSplit, String regex) {
        Map<String, Pattern> patternCache = getPatternCache(appMap);
        Pattern pattern = patternCache.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            patternCache.put(regex, pattern);
        }
        return  pattern.split(toSplit, 0);
    }

     public synchronized static String[] split(ServletContext sc,
             String toSplit, String regex) {
        Map<String, Pattern> patternCache = getPatternCache(sc);
        Pattern pattern = patternCache.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            patternCache.put(regex, pattern);
        }
        return  pattern.split(toSplit, 0);
    }


    /**
     * <p>Returns the URL pattern of the
     * {@link javax.faces.webapp.FacesServlet} that
     * is executing the current request.  If there are multiple
     * URL patterns, the value returned by
     * <code>HttpServletRequest.getServletPath()</code> and
     * <code>HttpServletRequest.getPathInfo()</code> is
     * used to determine which mapping to return.</p>
     * If no mapping can be determined, it most likely means
     * that this particular request wasn't dispatched through
     * the {@link javax.faces.webapp.FacesServlet}.
     * <p>
     * 
     * <b>NOTE:</b> This method was supposed to be replaced with the "mapping API"
     * from Servlet 4, but this has not been implemented in time for JSF 2.3
     * to depend on.
     *
     * @param context the {@link FacesContext} of the current request
     *
     * @return the URL pattern of the {@link javax.faces.webapp.FacesServlet}
     *         or <code>null</code> if no mapping can be determined
     *
     * @throws NullPointerException if <code>context</code> is null
     */
    public static String getFacesMapping(FacesContext context) {

        notNull("context", context);

        // Check for a previously stored mapping   
        String mapping = (String) RequestStateManager.get(context, INVOCATION_PATH);

        if (mapping == null) {
         
            // First check for javax.servlet.forward.servlet_path
            // and javax.servlet.forward.path_info for non-null
            // values. If either is non-null, use this
            // information to generate determine the mapping.
            ExternalContext externalContext = context.getExternalContext();
            String servletPath = externalContext.getRequestServletPath();
            String pathInfo = externalContext.getRequestPathInfo();

            mapping = getMappingForRequest(externalContext, servletPath, pathInfo);
            if (mapping == null && LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE,
                    "jsf.faces_servlet_mapping_cannot_be_determined_error",
                    new Object[]{servletPath});
            }
            
            if (mapping != null) {
                RequestStateManager.set(context, INVOCATION_PATH, mapping);
            }
        }
        
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "URL pattern of the FacesServlet executing the current request " + mapping);
        }
        
        return mapping;
    }

    /**
     * <p>Return the appropriate {@link javax.faces.webapp.FacesServlet} mapping
     * based on the servlet path of the current request.</p>
     *
     * @param externalContext the external context of the request
     * @param servletPath the servlet path of the request
     * @param pathInfo the path info of the request
     *
     * @return the appropriate mapping based on the current request
     *
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    private static String getMappingForRequest(ExternalContext externalContext, String servletPath, String pathInfo) {

        if (servletPath == null) {
            return null;
        }
        
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "servletPath " + servletPath);
            LOGGER.log(FINE, "pathInfo " + pathInfo);
        }
        
        // If the path returned by HttpServletRequest.getServletPath()
        // returns a zero-length String, then the FacesServlet has
        // been mapped to '/*'.
        if (servletPath.length() == 0) {
            return "/*";
        }

        // Presence of path info means we were invoked using a prefix path mapping
        if (pathInfo != null) {
            return servletPath;
        } else if (servletPath.indexOf('.') < 0) {
            // If pathInfo is null and no '.' is present, the FacesServlet 
            // could be invoked using prefix path but without any pathInfo - 
            // i.e. GET /contextroot/faces or GET /contextroot/faces/
            // 
            // It could also be that the FacesServlet is invoked using an
            // exact mapping, i.e. GET /contextroot/foo
            // Look at the Servlet mappings to see which case we have here:
            
            Object context = externalContext.getContext();
            if (context instanceof ServletContext) {
                Collection<String> servletMappings = getFacesServletMappings((ServletContext) context);
                if (servletMappings.contains(servletPath)) {
                    return addExactMappedMarker(servletPath); // It's not ideal, to be replaced by Servlet 4 mapping API types
                }
            }
            
            return servletPath;
        } else {
            // Servlet invoked using extension mapping
            return servletPath.substring(servletPath.lastIndexOf('.'));
        }
    }
    
    /**
     * Checks if the FacesServlet is exact mapped to the given resource.
     * <p>
     * Not to be confused with {@link Util#isExactMapped(String)}, which checks
     * if a string representing a mapping, not a resource, is an exact mapping.
     * <p>
     * This should be replaced by the Servlet 4 mapping API when/if that becomes available
     * and JSF/Mojarra can depend on it.
     * 
     * @param viewId the view id to test
     * @return true if the FacesServlet is exact mapped to the given viewId, false otherwise
     */
    public static boolean isViewIdExactMappedToFacesServlet(String viewId) {
        return isResourceExactMappedToFacesServlet(FacesContext.getCurrentInstance().getExternalContext(), viewId);
    }
    
    /**
     * Checks if the FacesServlet is exact mapped to the given resource.
     * <p>
     * Not to be confused with {@link Util#isExactMapped(String)}, which checks
     * if a string representing a mapping, not a resource, is an exact mapping.
     * <p>
     * This should be replaced by the Servlet 4 mapping API when/if that becomes available
     * and JSF/Mojarra can depend on it.
     * 
     * @param externalContext the external context for this request
     * @param resource the resource to test
     * @return true if the FacesServlet is exact mapped to the given resource, false otherwise
     */
    public static boolean isResourceExactMappedToFacesServlet(ExternalContext externalContext, String resource) {
        Object context = externalContext.getContext();
        if (context instanceof ServletContext) {
            return getFacesServletMappings((ServletContext) context).contains(resource);
        }
        
        return false;
    }
    
    public static String getFirstWildCardMappingToFacesServlet(ExternalContext externalContext) {
        // If needed, cache this after initialization of JSF
        Object context = externalContext.getContext();
        if (context instanceof ServletContext) {
            return getFacesServletMappings((ServletContext) context)
                    .stream()
                    .filter(mapping -> mapping.contains("*"))
                    .findFirst()
                    .orElse(null);
        }
        
        return null;
    }
    
    private static final String EXACT_MARKER = "* *";
    
    public static String addExactMappedMarker(String mapping) {
        return EXACT_MARKER + mapping;
    }
    
    public static String removeExactMappedMarker(String mapping) {
        return mapping.substring(EXACT_MARKER.length());
    }
    
    /**
     * <p>Returns true if the provided <code>url-mapping</code> is
     * an exact mapping (starts with {@link Util#EXACT_MARKER}).</p>
     *
     * @param mapping a <code>url-pattern</code>
     * @return true if the mapping starts with <code>/</code>
     */
    public static boolean isExactMapped(String mapping) {
        return mapping.startsWith("* *");
    }
    
    /**
     * <p>Returns true if the provided <code>url-mapping</code> is
     * a prefix path mapping (starts with <code>/</code>).</p>
     *
     * @param mapping a <code>url-pattern</code>
     * @return true if the mapping starts with <code>/</code>
     */
    public static boolean isPrefixMapped(String mapping) {
        return mapping.charAt(0) == '/';
    }

    public static boolean isSpecialAttributeName(String name) {
        boolean isSpecialAttributeName = name.equals("action")  ||
                            name.equals("actionListener") || name.equals("validator")
                            || name.equals("valueChangeListener");
        return isSpecialAttributeName;
    }


    /**
     * @param ctx the {@link FacesContext} for the current request
     * @param viewToRender the {@link UIViewRoot} to check
     * @return <code>true</code> if the {@link FacesContext} attributes map
     *  contains a reference to the {@link UIViewRoot}'s view ID
     */
    public static boolean isViewPopulated(FacesContext ctx, UIViewRoot viewToRender) {

        return ctx.getAttributes().containsKey(viewToRender);

    }


    /**
     * <p>
     * Flag the specified {@link UIViewRoot} as populated.
     * </p>
     * @param ctx the {@link FacesContext} for the current request
     * @param viewToRender the {@link UIViewRoot} to mark as populated
     */
    public static void setViewPopulated(FacesContext ctx,
                                        UIViewRoot viewToRender) {

        ctx.getAttributes().put(viewToRender, Boolean.TRUE);

    }


    /**
     * Utility method to validate ID uniqueness for the tree represented
     * by <code>component</code>.
     */
    public static void checkIdUniqueness(FacesContext context,
                                          UIComponent component,
                                          Set<String> componentIds) {

        boolean uniquenessCheckDisabled = false;
        
        if (context.isProjectStage(ProjectStage.Production)) {
            WebConfiguration config = WebConfiguration.getInstance(context.getExternalContext());
            uniquenessCheckDisabled = config.isOptionEnabled(
                WebConfiguration.BooleanWebContextInitParameter.DisableIdUniquenessCheck);
        }
        
        if (!uniquenessCheckDisabled) {
        
            // deal with children/facets that are marked transient.
            for (Iterator<UIComponent> kids = component.getFacetsAndChildren();
                kids.hasNext();) {

                UIComponent kid = kids.next();
                // check for id uniqueness
                String id = kid.getClientId(context);
                if (componentIds.add(id)) {
                    checkIdUniqueness(context, kid, componentIds);
                } else {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE,
                                   "jsf.duplicate_component_id_error",
                                   id);


                        FastStringWriter writer = new FastStringWriter(128);
                        DebugUtil.simplePrintTree(context.getViewRoot(), id, writer);
                        LOGGER.severe(writer.toString());
                    }

                    String message =
                          MessageUtils.getExceptionMessageString(
                                MessageUtils.DUPLICATE_COMPONENT_ID_ERROR_ID, id);
                    throw new IllegalStateException(message);
                }
            }
        }
    }

    static public boolean classHasAnnotations(Class<?> clazz) {
        if (clazz != null) {
            while (clazz != Object.class) {
                Field[] fields = clazz.getDeclaredFields();
                if (fields != null) {
                    for (Field field : fields) {
                        if (field.getAnnotations().length > 0) {
                            return true;
                        }
                    }
                }

                Method[] methods = clazz.getDeclaredMethods();
                if (methods != null) {
                    for (Method method : methods) {
                        if (method.getDeclaredAnnotations().length > 0) {
                            return true;
                        }
                    }
                }

                clazz = clazz.getSuperclass();
            }
        }

        return false;
    }
    
    /**
     * If view root is instance of naming container, return its container client id, suffixed with separator character.
     * @param context Involved faces context.
     * @return The naming container prefix, or an empty string if the view root is not an instance of naming container.
     */
    public static String getNamingContainerPrefix(FacesContext context) {
        UIViewRoot viewRoot = context.getViewRoot();

        if (viewRoot == null) {
            Application application = context.getApplication();
            viewRoot = (UIViewRoot) application.createComponent(UIViewRoot.COMPONENT_TYPE);
        }

        if (viewRoot instanceof NamingContainer) {
            return viewRoot.getContainerClientId(context) + UINamingContainer.getSeparatorChar(context);
        } else {
            return "";
        }
    }
    
    public static String getViewStateId(FacesContext context) {
        String result = null;
        final String viewStateCounterKey = "com.sun.faces.util.ViewStateCounterKey";
        Map<Object, Object> contextAttrs = context.getAttributes();
        Integer counter = (Integer) contextAttrs.get(viewStateCounterKey);
        if (null == counter) {
            counter = Integer.valueOf(0);
        }
        
        char sep = UINamingContainer.getSeparatorChar(context);
        UIViewRoot root = context.getViewRoot();
        result = root.getContainerClientId(context) + sep + 
                ResponseStateManager.VIEW_STATE_PARAM + sep +
                + counter;
        contextAttrs.put(viewStateCounterKey, ++counter);
        
        return result;
    }

    public static String getClientWindowId(FacesContext context) {
        String result = null;
        final String clientWindowIdCounterKey = "com.sun.faces.util.ClientWindowCounterKey";
        Map<Object, Object> contextAttrs = context.getAttributes();
        Integer counter = (Integer) contextAttrs.get(clientWindowIdCounterKey);
        if (null == counter) {
            counter = Integer.valueOf(0);
        }
        
        char sep = UINamingContainer.getSeparatorChar(context);
        result = context.getViewRoot().getContainerClientId(context) + sep + 
                ResponseStateManager.CLIENT_WINDOW_PARAM + sep + counter;
        contextAttrs.put(clientWindowIdCounterKey, ++counter);
        
        return result;
    }

    private static final String FACES_CONTEXT_ATTRIBUTES_DOCTYPE_KEY = Util.class.getName() + "_FACES_CONTEXT_ATTRS_DOCTYPE_KEY";
    
    public static void saveDOCTYPEToFacesContextAttributes(String DOCTYPE) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (null == context) {
            return;
        }
        Map<Object, Object> attrs = context.getAttributes();
        attrs.put(FACES_CONTEXT_ATTRIBUTES_DOCTYPE_KEY, DOCTYPE);
        
    }
    
    public static String getDOCTYPEFromFacesContextAttributes(FacesContext context) {
        if (null == context) {
            return null;
        }
        Map<Object, Object> attrs = context.getAttributes();
        return (String) attrs.get(FACES_CONTEXT_ATTRIBUTES_DOCTYPE_KEY);
    }
    
    private static final String FACES_CONTEXT_ATTRIBUTES_XMLDECL_KEY = Util.class.getName() + "_FACES_CONTEXT_ATTRS_XMLDECL_KEY";
    
    public static void saveXMLDECLToFacesContextAttributes(String XMLDECL) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (null == context) {
            return;
        }
        Map<Object, Object> attrs = context.getAttributes();
        attrs.put(FACES_CONTEXT_ATTRIBUTES_XMLDECL_KEY, XMLDECL);
        
    }
    
    public static String getXMLDECLFromFacesContextAttributes(FacesContext context) {
        if (null == context) {
            return null;
        }
        Map<Object, Object> attrs = context.getAttributes();
        return (String) attrs.get(FACES_CONTEXT_ATTRIBUTES_XMLDECL_KEY);
    }
    
    public static long getLastModified(URL url) {
        long lastModified;
        URLConnection conn;
        InputStream is = null;

        try {
            conn = url.openConnection();

            if (conn instanceof JarURLConnection) { 
                /*
                 * Note this is a work around for JarURLConnection since the
                 * getLastModified method is buggy. See JAVASERVERFACES-2725
                 * and JAVASERVERFACES-2734.
                 */
                JarURLConnection jarUrlConnection = (JarURLConnection) conn; 
                URL jarFileUrl = jarUrlConnection.getJarFileURL(); 
                URLConnection jarFileConnection = jarFileUrl.openConnection(); 
                lastModified = jarFileConnection.getLastModified(); 
                jarFileConnection.getInputStream().close(); 
            }
            else { 
                is = conn.getInputStream(); 
                lastModified = conn.getLastModified(); 
            } 
        } catch (Exception e) { 
            throw new FacesException("Error Checking Last Modified for " + url, e);
        } finally {
            if (is != null) {
                try { 
                    is.close();
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Closing stream", e);
                    }
                }
            }
        }
        return lastModified;
    }

    /**
     * Get the faces-config.xml version (if any).
     *
     * @param facesContext the Faces context.
     * @return the version found, or "" if none found.
     */
    public static String getFacesConfigXmlVersion(FacesContext facesContext) {
        String result = "";
        InputStream stream = null;
        try {
            URL url = facesContext.getExternalContext().getResource("/WEB-INF/faces-config.xml");
            if (url != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                xpath.setNamespaceContext(new JavaeeNamespaceContext());
                stream = url.openStream();
                DocumentBuilderFactory dbf = createDocumentBuilderFactory();
                try {
                    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                } catch (ParserConfigurationException pce) {
                }
                dbf.setNamespaceAware(true);
                dbf.setValidating(false);
                dbf.setXIncludeAware(false);
                dbf.setExpandEntityReferences(false);
                result = xpath.evaluate("string(/javaee:faces-config/@version)",
                        dbf.newDocumentBuilder().parse(stream));
            }
        } catch (MalformedURLException mue) {
        } catch (XPathExpressionException | IOException xpee) {
        } catch (Exception e) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }
    /**
     * Get the web.xml version (if any).
     *
     * @param facesContext the Faces context.
     * @return the version found, or "" if none found.
     */
    public static String getWebXmlVersion(FacesContext facesContext) {
        String result = "";
        InputStream stream = null;
        try {
            URL url = facesContext.getExternalContext().getResource("/WEB-INF/web.xml");
            if (url != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                xpath.setNamespaceContext(new JavaeeNamespaceContext());
                stream = url.openStream();
                DocumentBuilderFactory dbf = createDocumentBuilderFactory();
                try {
                    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                } catch (ParserConfigurationException e) {
                }
                dbf.setNamespaceAware(true);
                dbf.setValidating(false);
                dbf.setXIncludeAware(false);
                dbf.setExpandEntityReferences(false);
                result = xpath.evaluate("string(/javaee:web-app/@version)", dbf.newDocumentBuilder().parse(stream));
            }
        } catch (MalformedURLException mue) {
        } catch (XPathExpressionException | IOException xpee) {
        } catch (Exception e) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }

    public static class JavaeeNamespaceContext implements NamespaceContext {

        @Override
        public String getNamespaceURI(String prefix) {
            return "http://xmlns.jcp.org/xml/ns/javaee";
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return "javaee";
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }    

    /**
     * Get the CDI bean manager.
     * 
     * @param facesContext the Faces context to consult
     * @return the CDI bean manager.
     */
    public static BeanManager getCdiBeanManager(FacesContext facesContext) {
        BeanManager result = null;
        
        if (facesContext != null && facesContext.getAttributes().containsKey(RIConstants.CDI_BEAN_MANAGER)) {
            result = (BeanManager) facesContext.getAttributes().get(RIConstants.CDI_BEAN_MANAGER);
        } else if (facesContext != null && facesContext.getExternalContext().getApplicationMap().containsKey(RIConstants.CDI_BEAN_MANAGER)) {
            result = (BeanManager) facesContext.getExternalContext().getApplicationMap().get(RIConstants.CDI_BEAN_MANAGER);
        } else {
            try {
                InitialContext initialContext = new InitialContext();
                result = (BeanManager) initialContext.lookup("java:comp/BeanManager");
            }
            catch (NamingException ne) {
                try {
                    InitialContext initialContext = new InitialContext();
                    result = (BeanManager) initialContext.lookup("java:comp/env/BeanManager");
                }
                catch (NamingException ne2) {
                }
            }
            
            if (result == null && facesContext != null) {
                Map<String, Object> applicationMap = facesContext.getExternalContext().getApplicationMap();
                result = (BeanManager) applicationMap.get("org.jboss.weld.environment.servlet.javax.enterprise.inject.spi.BeanManager");
            }
            
            if (result != null && facesContext != null) {
                facesContext.getAttributes().put(RIConstants.CDI_BEAN_MANAGER, result);
                facesContext.getExternalContext().getApplicationMap().put(RIConstants.CDI_BEAN_MANAGER, result);
            }
        }
        
        return result;
    }
    
    /**
     * Is CDI available.
     * 
     * @param facesContext the Faces context to consult.
     * @return true if available, false otherwise.
     */
    public static boolean isCdiAvailable(FacesContext facesContext) {
        boolean result;
        
        if (facesContext != null && facesContext.getAttributes().containsKey(RIConstants.CDI_AVAILABLE)) {
            result = (Boolean) facesContext.getAttributes().get(RIConstants.CDI_AVAILABLE);
        } else if (facesContext != null && facesContext.getExternalContext().getApplicationMap().containsKey(RIConstants.CDI_AVAILABLE)) {
            result = (Boolean) facesContext.getExternalContext().getApplicationMap().get(RIConstants.CDI_AVAILABLE);
        } else {
            result = getCdiBeanManager(facesContext) != null;
            
            if (result && facesContext != null) {
                facesContext.getAttributes().put(RIConstants.CDI_AVAILABLE, result);
                facesContext.getExternalContext().getApplicationMap().put(RIConstants.CDI_AVAILABLE, result);
            }
        }
        
        return result;
    }
    
    /**
     * Is CDI available (ServletContext variant)
     * 
     * @param servletContext the servlet context.
     * @return true if available, false otherwise.
     */
    public static boolean isCdiAvailable(ServletContext servletContext) {
        boolean result;
        
        Object value = servletContext.getAttribute(RIConstants.CDI_AVAILABLE);
        if (value != null) {
            result = (Boolean) value;
        } else {
            result = getCdiBeanManager(null) != null;
            
            if (result) {
                servletContext.setAttribute(RIConstants.CDI_AVAILABLE, result);
            }
        }
        
        return result;
    }
    
    /**
     * Is CDI 1.1 or later
     * 
     * @param facesContext the Faces context.
     * @return true if CDI 1.1 or later, false otherwise.
     */
    public static boolean isCdiOneOneOrLater(FacesContext facesContext) {
        boolean result = false;
        
        if (facesContext != null && facesContext.getAttributes().containsKey(RIConstants.CDI_1_1_OR_LATER)) {
            result = (Boolean) facesContext.getAttributes().get(RIConstants.CDI_1_1_OR_LATER);
        } else if (facesContext != null && facesContext.getExternalContext().getApplicationMap().containsKey(RIConstants.CDI_1_1_OR_LATER)) {
            result = facesContext.getExternalContext().getApplicationMap().containsKey(RIConstants.CDI_1_1_OR_LATER);
        } else {
            try {
                Class.forName("javax.enterprise.context.Initialized");
                result = true;
            } catch (ClassNotFoundException ignored) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Detected CDI 1.0", ignored);
                }
            }
            
            if (facesContext != null) {
                facesContext.getAttributes().put(RIConstants.CDI_1_1_OR_LATER, result);
                facesContext.getExternalContext().getApplicationMap().put(RIConstants.CDI_1_1_OR_LATER, result);
            }
        }
        
        return result;
    }
}
