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

package com.sun.faces.config.processor;

import static com.sun.faces.application.ApplicationResourceBundle.DEFAULT_KEY;
import static com.sun.faces.config.ConfigManager.INJECTION_PROVIDER_KEY;
import static com.sun.faces.util.ReflectionUtils.lookupConstructor;
import static jakarta.faces.FactoryFinder.APPLICATION_FACTORY;
import static jakarta.faces.application.ProjectStage.Development;
import static jakarta.faces.application.ProjectStage.Production;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationInstanceFactoryMetadataMap;
import com.sun.faces.application.ApplicationResourceBundle;
import com.sun.faces.config.ConfigManager;
import com.sun.faces.config.ConfigurationException;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.spi.InjectionProvider;
import com.sun.faces.spi.InjectionProviderException;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This is the base <code>ConfigProcessor</code> that all concrete <code>ConfigProcessor</code> implementations should
 * extend.
 * </p>
 */
public abstract class AbstractConfigProcessor implements ConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();
    private static final String CLASS_METADATA_MAP_KEY_SUFFIX = ".METADATA";

    // -------------------------------------------- Methods from ConfigProcessor

    private ApplicationInstanceFactoryMetadataMap<String, Object> getClassMetadataMap(ServletContext servletContext) {
        ApplicationInstanceFactoryMetadataMap<String, Object> classMetadataMap = (ApplicationInstanceFactoryMetadataMap<String, Object>) servletContext
                .getAttribute(getClassMetadataMapKey());

        if (classMetadataMap == null) {
            classMetadataMap = new ApplicationInstanceFactoryMetadataMap(new ConcurrentHashMap<>());
            servletContext.setAttribute(getClassMetadataMapKey(), classMetadataMap);
        }

        return classMetadataMap;
    }

    @Override
    public void initializeClassMetadataMap(ServletContext servletContext, FacesContext facesContext) {
        getClassMetadataMap(servletContext);
    }

    protected String getClassMetadataMapKey() {
        return getClass().getName() + CLASS_METADATA_MAP_KEY_SUFFIX;
    }

    @Override
    public void destroy(ServletContext sc, FacesContext facesContext) {
    }

    // ------------------------------------------------------- Protected Methods

    /**
     * @return return the Application instance for this context.
     */
    protected Application getApplication() {
        return ((ApplicationFactory) FactoryFinder.getFactory(APPLICATION_FACTORY)).getApplication();

    }

    /**
     * <p>
     * Return the text of the specified <code>Node</code>, if any.
     *
     * @param node the <code>Node</code>
     * @return the text of the <code>Node</code> If the length of the text is zero, this method will return
     * <code>null</code>
     */
    protected String getNodeText(Node node) {
        String res = null;
        if (node != null) {
            res = node.getTextContent();
            if (res != null) {
                res = res.trim();
            }
        }

        return res != null && res.length() != 0 ? res : null;
    }

    /**
     * @return a <code>Map</code> of of textual values keyed off the values of any lang or xml:lang attributes specified on
     * an attribute. If no such attribute exists, then the key {@link ApplicationResourceBundle#DEFAULT_KEY} will be used
     * (i.e. this represents the default Locale).
     * @param list a list of nodes representing textual elements such as description or display-name
     */
    protected Map<String, String> getTextMap(List<Node> list) {

        if (list != null && !list.isEmpty()) {
            int len = list.size();
            HashMap<String, String> names = new HashMap<>(len, 1.0f);
            for (Node node : list) {
                String textValue = getNodeText(node);
                if (textValue != null) {
                    if (node.hasAttributes()) {
                        NamedNodeMap attributes = node.getAttributes();
                        String lang = getNodeText(attributes.getNamedItem("lang"));
                        if (lang == null) {
                            lang = getNodeText(attributes.getNamedItem("xml:lang"));
                        }
                        if (lang != null) {
                            names.put(lang, textValue);
                        } else {
                            names.put(DEFAULT_KEY, textValue);
                        }
                    } else {
                        names.put(DEFAULT_KEY, textValue);
                    }
                }
            }

            return names;
        }

        return null;
    }

    protected Class<?> findRootType(ServletContext sc, FacesContext facesContext, String source, Node sourceNode, Class<?>[] ctorArguments) {

        try {
            Class<?> sourceClass = loadClass(sc, facesContext, source, this, null);
            for (Class<?> ctorArg : ctorArguments) {
                if (lookupConstructor(sourceClass, ctorArg) != null) {
                    return ctorArg;
                }
            }
        } catch (ClassNotFoundException cnfe) {
            throw new ConfigurationException(buildMessage(format("Unable to find class ''{0}''", source), sourceNode), cnfe);
        }

        return null;
    }

    protected Object createInstance(ServletContext sc, FacesContext facesContext, String className, Node source) {
        return createInstance(sc, facesContext, className, null, null, source);
    }

    protected Object createInstance(ServletContext sc, FacesContext facesContext, String className, Class<?> rootType, Object root, Node source) {
        boolean[] didPerformInjection = { false };
        Object result = createInstance(sc, facesContext, className, rootType, root, source, true, didPerformInjection);
        return result;
    }

    protected Object createInstance(ServletContext sc, FacesContext facesContext, String className, Class<?> rootType, Object root, Node source,
            boolean performInjection, boolean[] didPerformInjection) {
        Class<?> clazz;
        Object returnObject = null;
        if (className != null) {
            try {
                clazz = loadClass(sc, facesContext, className, returnObject, null);
                if (clazz != null) {
                    if (returnObject == null) {
                        // Look for an adapter constructor if we've got
                        // an object to adapt
                        if (rootType != null && root != null) {
                            Constructor<?> construct = lookupConstructor(clazz, rootType);
                            if (construct != null) {
                                returnObject = construct.newInstance(root);
                            }
                        }
                    }

                    if (clazz != null && returnObject == null) {
                        returnObject = clazz.getDeclaredConstructor().newInstance();
                    }

                    ApplicationInstanceFactoryMetadataMap<String, Object> classMetadataMap = getClassMetadataMap(sc);

                    if (classMetadataMap.hasAnnotations(className) && performInjection) {
                        InjectionProvider injectionProvider = (InjectionProvider) facesContext.getAttributes().get(INJECTION_PROVIDER_KEY);

                        try {
                            injectionProvider.inject(returnObject);
                        } catch (InjectionProviderException ex) {
                            LOGGER.log(SEVERE, "Unable to inject instance" + className, ex);
                            throw new FacesException(ex);
                        }

                        try {
                            injectionProvider.invokePostConstruct(returnObject);
                        } catch (InjectionProviderException ex) {
                            LOGGER.log(SEVERE, "Unable to invoke @PostConstruct annotated method on instance " + className, ex);
                            throw new FacesException(ex);
                        }

                        didPerformInjection[0] = true;
                    }

                }

            } catch (ClassNotFoundException cnfe) {
                throw new ConfigurationException(buildMessage(format("Unable to find class ''{0}''", className), source), cnfe);
            } catch (NoClassDefFoundError ncdfe) {
                throw new ConfigurationException(
                        buildMessage(format("Class ''{0}'' is missing a runtime dependency: {1}", className, ncdfe.toString()), source), ncdfe);
            } catch (ClassCastException cce) {
                throw new ConfigurationException(buildMessage(format("Class ''{0}'' is not an instance of ''{1}''", className, rootType), source), cce);
            } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException | FacesException e) {
                throw new ConfigurationException(buildMessage(format("Unable to create a new instance of ''{0}'': {1}", className, e.toString()), source), e);
            }
        }

        return returnObject;
    }

    protected void destroyInstance(ServletContext sc, FacesContext facesContext, String className, Object instance) {
        if (instance != null) {
            ApplicationInstanceFactoryMetadataMap<String, Object> classMetadataMap = getClassMetadataMap(sc);

            if (classMetadataMap.hasAnnotations(className)) {
                InjectionProvider injectionProvider = (InjectionProvider) facesContext.getAttributes().get(INJECTION_PROVIDER_KEY);

                if (injectionProvider != null) {
                    try {
                        injectionProvider.invokePreDestroy(instance);
                    } catch (InjectionProviderException ex) {
                        LOGGER.log(SEVERE, "Unable to invoke @PreDestroy annotated method on instance " + className, ex);
                        throw new FacesException(ex);
                    }
                }
            }
        }
    }

    protected Class<?> loadClass(ServletContext sc, FacesContext facesContext, String className, Object fallback, Class<?> expectedType)
            throws ClassNotFoundException {
        ApplicationInstanceFactoryMetadataMap<String, Object> classMetadataMap = getClassMetadataMap(sc);

        Class<?> clazz = (Class<?>) classMetadataMap.get(className);
        if (clazz == null) {
            try {
                clazz = Util.loadClass(className, fallback);
                if (!isDevModeEnabled(sc, facesContext)) {
                    classMetadataMap.put(className, clazz);
                } else {
                    classMetadataMap.scanForAnnotations(className, clazz);
                }
            } catch (Exception e) {
                throw new FacesException(e.getMessage(), e);
            }

        }

        if (expectedType != null && !expectedType.isAssignableFrom(clazz)) {
            throw new ClassCastException();
        }

        return clazz;
    }

    protected void processAnnotations(FacesContext ctx, Class<? extends Annotation> annotationType) {
        ApplicationAssociate.getInstance(ctx.getExternalContext()).getAnnotationManager().applyConfigAnnotations(ctx, annotationType,
                ConfigManager.getAnnotatedClasses(ctx).get(annotationType));
    }

    // --------------------------------------------------------- Private Methods

    private String buildMessage(String cause, Node source) {
        return MessageFormat.format("\n  Source Document: {0}\n  Cause: {1}", source.getOwnerDocument().getDocumentURI(), cause);
    }

    private boolean isDevModeEnabled(ServletContext sc, FacesContext facesContext) {
        return getProjectStage(sc, facesContext).equals(Development);
    }

    private ProjectStage getProjectStage(ServletContext sc, FacesContext facesContext) {
        final String projectStageKey = AbstractConfigProcessor.class.getName() + ".PROJECTSTAGE";
        ProjectStage projectStage = (ProjectStage) sc.getAttribute(projectStageKey);

        if (projectStage == null) {
            WebConfiguration webConfig = WebConfiguration.getInstance(facesContext.getExternalContext());
            String value = webConfig.getEnvironmentEntry(WebConfiguration.WebEnvironmentEntry.ProjectStage);

            if (value != null) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, "ProjectStage configured via JNDI: {0}", value);
                }
                try {
                    projectStage = ProjectStage.valueOf(value);
                } catch (IllegalArgumentException iae) {
                    if (LOGGER.isLoggable(INFO)) {
                        LOGGER.log(INFO, "Unable to discern ProjectStage for value {0}.", value);
                    }
                }
            } else {
                projectStage = ContextParam.PROJECT_STAGE.getValue(facesContext);
                if (projectStage != null) {
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE, "ProjectStage configured via servlet context init parameter: {0}", value);
                    }
                }
            }

            if (projectStage == null) {
                projectStage = Production;
            }

            sc.setAttribute(projectStageKey, projectStage);
        }

        return projectStage;
    }

}
