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

import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.ConfigurationException;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.facelets.compiler.Compiler;
import com.sun.faces.facelets.tag.TagLibrary;
import com.sun.faces.facelets.tag.TagLibraryImpl;
import com.sun.faces.facelets.tag.faces.CompositeComponentTagLibrary;
import com.sun.faces.facelets.util.ReflectionUtil;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-taglib</code>.
 * </p>
 */
public class FaceletTaglibConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <p>
     * /facelet-taglib/library-class
     * </p>
     */
    private static final String LIBRARY_CLASS = "library-class";

    /**
     * <p>
     * /facelet-taglib/namespace
     * </p>
     */
    private static final String TAGLIB_NAMESPACE = "namespace";

    /**
     * <p>
     * /facelet-taglib/tag
     * </p>
     */
    private static final String TAG = "tag";

    /**
     * <p>
     * /facelet-taglib/function
     * </p>
     */
    private static final String FUNCTION = "function";

    /**
     * <p>
     * /facelet-taglib/tag/tag-name
     * </p>
     */
    private static final String TAG_NAME = "tag-name";

    /**
     * <p>
     * /facelet-taglib/tag/component
     * </p>
     */
    private static final String COMPONENT = "component";

    /**
     * <p>
     * /facelet-taglib/tag/validator
     * </p>
     */
    private static final String VALIDATOR = "validator";

    /**
     * <p>
     * /facelet-taglib/tag/converter
     * </p>
     */
    private static final String CONVERTER = "converter";

    /**
     * <p>
     * /facelet-taglib/tag/behavior
     * </p>
     */
    private static final String BEHAVIOR = "behavior";
    /**
     * <p>
     * /facelet-taglib/tag/source
     * </p>
     */
    private static final String SOURCE = "source";

    /**
     * <p>
     * /facelet-taglib/tag/resource-id
     * </p>
     */
    private static final String RESOURCE_ID = "resource-id";

    /**
     * <p>
     * <ul>
     * <li>/facelet-taglib/tag/tag-handler</li>
     * <li>/facelet-taglib/tag/converter/handler-class</li>
     * <li>/facelet-taglib/tag/validator/handler-class</li>
     * <li>/facelet-taglib/tag/component/handler-class</li>
     * </ul>
     * </p>
     */
    private static final String HANDLER_CLASS = "handler-class";

    /**
     * <p>
     * /facelet-taglib/tag/validator/validator-id
     * </p>
     */
    private static final String VALIDATOR_ID = "validator-id";

    /**
     * <p>
     * /facelet-taglib/tag/converter/converter-id
     * </p>
     */
    private static final String CONVERTER_ID = "converter-id";

    /**
     * <p>
     * /facelet-taglib/tag/behavior/behavior-id
     * </p>
     */
    private static final String BEHAVIOR_ID = "behavior-id";

    /**
     * <p>
     * /facelet-taglib/tag/component/component-type
     * </p>
     */
    private static final String COMPONENT_TYPE = "component-type";

    /**
     * <p>
     * /facelet-taglib/tag/component/renderer-type
     * </p>
     */
    private static final String RENDERER_TYPE = "renderer-type";

    /**
     * <p>
     * /facelet-taglib/tag/function/function-name
     * </p>
     */
    private static final String FUNCTION_NAME = "function-name";

    /**
     * <p>
     * /facelet-taglib/tag/function/function-class
     * </p>
     */
    private static final String FUNCTION_CLASS = "function-class";

    /**
     * <p>
     * /facelet-taglib/tag/function/function-signature
     * </p>
     */
    private static final String FUNCTION_SIGNATURE = "function-signature";

    /**
     * <p>
     * /facelet-taglib/composite-library-name
     * </p>
     */
    private static final String COMPOSITE_LIBRARY_NAME = "composite-library-name";

    // -------------------------------------------- Methods from ConfigProcessor

    /**
     * @see ConfigProcessor#process(jakarta.servlet.ServletContext,com.sun.faces.config.manager.documents.DocumentInfo[])
     */
    @Override
    public void process(ServletContext sc, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {

        ApplicationAssociate associate = ApplicationAssociate.getInstance(facesContext.getExternalContext());
        Compiler compiler = associate.getCompiler();

        for (int i = 0, length = documentInfos.length; i < length; i++) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing facelet-taglibrary document: ''{0}''", documentInfos[i].getSourceURI()));
            }

            Document document = documentInfos[i].getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            Element documentElement = document.getDocumentElement();
            NodeList libraryClass = documentElement.getElementsByTagNameNS(namespace, LIBRARY_CLASS);

            if (libraryClass != null && libraryClass.getLength() > 0) {
                processTaglibraryClass(sc, facesContext, libraryClass, compiler);
            } else {
                processTagLibrary(sc, facesContext, documentElement, namespace, compiler);
            }
        }

    }

    // --------------------------------------------------------- Private Methods

    private void processTaglibraryClass(ServletContext servletContext, FacesContext facesContext, NodeList libraryClass, Compiler compiler) {
        Node n = libraryClass.item(0);
        String className = getNodeText(n);
        TagLibrary taglib = (TagLibrary) createInstance(servletContext, facesContext, className, n);
        compiler.addTagLibrary(taglib);
    }

    private void processTagLibrary(ServletContext sc, FacesContext facesContext, Element documentElement, String namespace, Compiler compiler) {

        NodeList children = documentElement.getChildNodes();
        if (children != null && children.getLength() > 0) {
            String taglibNamespace = null;
            String compositeLibraryName = null;
            for (int i = 0, ilen = children.getLength(); i < ilen; i++) {
                Node n = children.item(i);
                if (n.getLocalName() != null) {
                    switch (n.getLocalName()) {
                    case TAGLIB_NAMESPACE:
                        taglibNamespace = getNodeText(n);
                        break;
                    case COMPOSITE_LIBRARY_NAME:
                        compositeLibraryName = getNodeText(n);
                        break;
                    }
                }
            }

            TagLibraryImpl taglibrary;
            if (compositeLibraryName != null) {
                taglibrary = new CompositeComponentTagLibrary(taglibNamespace, compositeLibraryName);
                compiler.addTagLibrary(taglibrary);
            } else {
                taglibrary = new TagLibraryImpl(taglibNamespace);

            }
            NodeList tags = documentElement.getElementsByTagNameNS(namespace, TAG);
            processTags(sc, facesContext, documentElement, tags, taglibrary);
            NodeList functions = documentElement.getElementsByTagNameNS(namespace, FUNCTION);
            processFunctions(sc, facesContext, functions, taglibrary);
            compiler.addTagLibrary(taglibrary);
        }

    }

    private void processTags(ServletContext servletContext, FacesContext facesContext, Element documentElement, NodeList tags, TagLibraryImpl taglibrary) {

        if (tags != null && tags.getLength() > 0) {
            for (int i = 0, ilen = tags.getLength(); i < ilen; i++) {

                Node tagNode = tags.item(i);
                NodeList children = tagNode.getChildNodes();
                String tagName = null;
                NodeList component = null;
                NodeList converter = null;
                NodeList validator = null;
                NodeList behavior = null;
                Node source = null;
                Node handlerClass = null;

                for (int j = 0, jlen = children.getLength(); j < jlen; j++) {
                    Node n = children.item(j);

                    // Process the nodes to see what children we have
                    if (n.getLocalName() != null) {
                        switch (n.getLocalName()) {
                        case TAG_NAME:
                            tagName = getNodeText(n);
                            break;
                        case COMPONENT:
                            component = n.getChildNodes();
                            break;
                        case CONVERTER:
                            converter = n.getChildNodes();
                            break;
                        case VALIDATOR:
                            validator = n.getChildNodes();
                            break;
                        case BEHAVIOR:
                            behavior = n.getChildNodes();
                            break;
                        case SOURCE:
                            source = n;
                            break;
                        case HANDLER_CLASS:
                            handlerClass = n;
                            break;
                        }
                    }
                }

                if (component != null) {
                    processComponent(servletContext, facesContext, documentElement, component, taglibrary, tagName);
                } else if (converter != null) {
                    processConverter(servletContext, facesContext, converter, taglibrary, tagName);
                } else if (validator != null) {
                    processValidator(servletContext, facesContext, validator, taglibrary, tagName);
                } else if (behavior != null) {
                    processBehavior(servletContext, facesContext, behavior, taglibrary, tagName);
                } else if (source != null) {
                    processSource(documentElement, source, taglibrary, tagName);
                } else if (handlerClass != null) {
                    processHandlerClass(servletContext, facesContext, handlerClass, taglibrary, tagName);
                }
            }
        }

    }

    private void processBehavior(ServletContext sc, FacesContext facesContext, NodeList behavior, TagLibraryImpl taglibrary, String tagName) {
        if (behavior != null && behavior.getLength() > 0) {
            String behaviorId = null;
            String handlerClass = null;
            for (int i = 0, ilen = behavior.getLength(); i < ilen; i++) {
                Node n = behavior.item(i);
                if (n.getLocalName() != null) {
                    switch (n.getLocalName()) {
                    case BEHAVIOR_ID:
                        behaviorId = getNodeText(n);
                        break;
                    case HANDLER_CLASS:
                        handlerClass = getNodeText(n);
                        break;
                    }
                }
            }

            if (handlerClass != null) {
                try {
                    Class<?> clazz = loadClass(sc, facesContext, handlerClass, this, null);
                    taglibrary.putBehavior(tagName, behaviorId, clazz);
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException(e);
                }

            } else {
                taglibrary.putBehavior(tagName, behaviorId);
            }
        }

    }

    private void processHandlerClass(ServletContext sc, FacesContext facesContext, Node handlerClass, TagLibraryImpl taglibrary, String name) {

        String className = getNodeText(handlerClass);
        if (className == null) {
            throw new ConfigurationException("The tag named " + name + " from namespace " + taglibrary.getNamespace() + " has a null handler-class defined");
        }

        try {
            Class<?> clazz;
            try {
                clazz = loadClass(sc, facesContext, className, this, null);
                taglibrary.putTagHandler(name, clazz);
            } catch (NoClassDefFoundError defNotFound) {
                String message = defNotFound.toString();
                if (message.contains("com/sun/facelets/") || message.contains("com.sun.facelets.")) {
                    if (LOGGER.isLoggable(WARNING)) {
                        LOGGER.log(WARNING, "faces.config.legacy.facelet.warning", new Object[] { handlerClass, });
                    }
                } else {
                    throw defNotFound;
                }
            }
        } catch (ClassNotFoundException cnfe) {
            throw new ConfigurationException(cnfe);
        }

    }

    private void processSource(Element documentElement, Node source, TagLibraryImpl taglibrary, String name) {

        String docURI = documentElement.getOwnerDocument().getDocumentURI();
        String s = getNodeText(source);
        try {
            URL url = new URL(new URL(docURI), s);
            taglibrary.putUserTag(name, url);
        } catch (MalformedURLException e) {
            throw new FacesException(e);
        }

    }

    private void processResourceId(Element documentElement, Node compositeSource, TagLibraryImpl taglibrary, String name) {

        String resourceId = getNodeText(compositeSource);
        taglibrary.putCompositeComponentTag(name, resourceId);

    }

    private void processValidator(ServletContext sc, FacesContext facesContext, NodeList validator, TagLibraryImpl taglibrary, String name) {

        if (validator != null && validator.getLength() > 0) {
            String validatorId = null;
            String handlerClass = null;
            for (int i = 0, ilen = validator.getLength(); i < ilen; i++) {
                Node n = validator.item(i);
                if (n.getLocalName() != null) {
                    switch (n.getLocalName()) {
                    case VALIDATOR_ID:
                        validatorId = getNodeText(n);
                        break;
                    case HANDLER_CLASS:
                        handlerClass = getNodeText(n);
                        break;
                    }
                }
            }
            if (handlerClass != null) {
                try {
                    Class<?> clazz = loadClass(sc, facesContext, handlerClass, this, null);
                    taglibrary.putValidator(name, validatorId, clazz);
                } catch (NoClassDefFoundError defNotFound) {
                    String message = defNotFound.toString();
                    if (message.contains("com/sun/facelets/") || message.contains("com.sun.facelets.")) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING, "faces.config.legacy.facelet.warning", new Object[] { handlerClass, });
                        }
                    } else {
                        throw defNotFound;
                    }
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException(e);
                }

            } else {
                taglibrary.putValidator(name, validatorId);
            }
        }

    }

    private void processConverter(ServletContext sc, FacesContext facesContext, NodeList converter, TagLibraryImpl taglibrary, String name) {

        if (converter != null && converter.getLength() > 0) {
            String converterId = null;
            String handlerClass = null;
            for (int i = 0, ilen = converter.getLength(); i < ilen; i++) {
                Node n = converter.item(i);
                if (n.getLocalName() != null) {
                    switch (n.getLocalName()) {
                    case CONVERTER_ID:
                        converterId = getNodeText(n);
                        break;
                    case HANDLER_CLASS:
                        handlerClass = getNodeText(n);
                        break;
                    }
                }
            }
            if (handlerClass != null) {
                try {
                    Class<?> clazz = loadClass(sc, facesContext, handlerClass, this, null);
                    taglibrary.putConverter(name, converterId, clazz);
                } catch (NoClassDefFoundError defNotFound) {
                    String message = defNotFound.toString();
                    if (message.contains("com/sun/facelets/") || message.contains("com.sun.facelets.")) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING, "faces.config.legacy.facelet.warning", new Object[] { handlerClass, });
                        }
                    } else {
                        throw defNotFound;
                    }
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException(e);
                }

            } else {
                taglibrary.putConverter(name, converterId);
            }
        }

    }

    private void processComponent(ServletContext sc, FacesContext facesContext, Element documentElement, NodeList component, TagLibraryImpl taglibrary,
            String name) {

        if (component != null && component.getLength() > 0) {
            String componentType = null;
            String rendererType = null;
            String handlerClass = null;
            Node resourceId = null;
            for (int i = 0, ilen = component.getLength(); i < ilen; i++) {
                Node n = component.item(i);
                if (n.getLocalName() != null) {
                    switch (n.getLocalName()) {
                    case COMPONENT_TYPE:
                        componentType = getNodeText(n);
                        break;
                    case RENDERER_TYPE:
                        rendererType = getNodeText(n);
                        break;
                    case HANDLER_CLASS:
                        handlerClass = getNodeText(n);
                        break;
                    case RESOURCE_ID:
                        resourceId = n;
                        break;
                    }
                }
            }
            if (handlerClass != null) {
                try {
                    Class<?> clazz = loadClass(sc, facesContext, handlerClass, this, null);
                    taglibrary.putComponent(name, componentType, rendererType, clazz);
                } catch (NoClassDefFoundError defNotFound) {
                    String message = defNotFound.toString();
                    if (message.contains("com/sun/facelets/") || message.contains("com.sun.facelets.")) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING, "faces.config.legacy.facelet.warning", new Object[] { handlerClass, });
                        }
                    } else {
                        throw defNotFound;
                    }
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException(e);
                }
            } else if (resourceId != null) {
                processResourceId(documentElement, resourceId, taglibrary, name);
            } else {
                taglibrary.putComponent(name, componentType, rendererType);
            }

        }

    }

    private void processFunctions(ServletContext sc, FacesContext facesContext, NodeList functions, TagLibraryImpl taglibrary) {

        if (functions != null && functions.getLength() > 0) {
            for (int i = 0, ilen = functions.getLength(); i < ilen; i++) {
                NodeList children = functions.item(i).getChildNodes();
                String functionName = null;
                String functionClass = null;
                String functionSignature = null;
                for (int j = 0, jlen = children.getLength(); j < jlen; j++) {
                    Node n = children.item(j);
                    if (n.getLocalName() != null) {
                        switch (n.getLocalName()) {
                        case FUNCTION_NAME:
                            functionName = getNodeText(n);
                            break;
                        case FUNCTION_CLASS:
                            functionClass = getNodeText(n);
                            break;
                        case FUNCTION_SIGNATURE:
                            functionSignature = getNodeText(n);
                            break;
                        }
                    }
                }
                try {
                    Class<?> clazz = loadClass(sc, facesContext, functionClass, this, null);
                    Method m = createMethod(clazz, functionSignature);
                    taglibrary.putFunction(functionName, m);
                } catch (Exception e) {
                    throw new ConfigurationException(e);
                }
            }
        }

    }

    private static Method createMethod(Class<?> type, String signatureParam) throws Exception {

        // Formatted XML might cause \n\t characters - make sure we only have space characters left

        String signature = signatureParam.replaceAll("\\s+", " ");
        int pos = signature.indexOf(' ');
        if (pos == -1) {
            throw new Exception("Must Provide Return Type: " + signature);
        } else {
            int pos2 = signature.indexOf('(', pos + 1);
            if (pos2 == -1) {
                throw new Exception("Must provide a method name, followed by '(': " + signature);
            } else {
                String mn = signature.substring(pos + 1, pos2).trim();
                pos = signature.indexOf(')', pos2 + 1);
                if (pos == -1) {
                    throw new Exception("Must close parentheses, ')' missing: " + signature);
                } else {
                    String[] ps = signature.substring(pos2 + 1, pos).trim().split(",");
                    Class<?>[] pc;
                    if (ps.length == 1 && "".equals(ps[0])) {
                        pc = new Class[0];
                    } else {
                        pc = new Class[ps.length];
                        for (int i = 0; i < pc.length; i++) {
                            pc[i] = ReflectionUtil.forName(ps[i].trim());
                        }
                    }
                    try {
                        return type.getMethod(mn, pc);
                    } catch (NoSuchMethodException e) {
                        throw new Exception("No Function Found on type: " + type.getName() + " with signature: " + signature);
                    }

                }
            }
        }

    }

}
