/*
 * Copyright (c) 2023 Contributors to Eclipse Foundation.
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

package com.sun.faces.facelets.tag;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.sun.faces.facelets.tag.faces.CompositeComponentTagHandler;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.BehaviorConfig;
import jakarta.faces.view.facelets.BehaviorHandler;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.ConverterConfig;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;
import jakarta.faces.view.facelets.ValidatorConfig;
import jakarta.faces.view.facelets.ValidatorHandler;

/**
 * Base class for defining TagLibraries in Java
 *
 * @author Jacob Hookom
 */
public abstract class AbstractTagLibrary implements TagLibrary {

    private static class ValidatorConfigWrapper implements ValidatorConfig {

        private final TagConfig parent;
        private final String validatorId;

        public ValidatorConfigWrapper(TagConfig parent, String validatorId) {
            this.parent = parent;
            this.validatorId = validatorId;
        }

        @Override
        public String getValidatorId() {
            return validatorId;
        }

        @Override
        public FaceletHandler getNextHandler() {
            return parent.getNextHandler();
        }

        @Override
        public Tag getTag() {
            return parent.getTag();
        }

        @Override
        public String getTagId() {
            return parent.getTagId();
        }
    }

    private static class ConverterConfigWrapper implements ConverterConfig {
        private final TagConfig parent;
        private final String converterId;

        public ConverterConfigWrapper(TagConfig parent, String converterId) {
            this.parent = parent;
            this.converterId = converterId;
        }

        @Override
        public String getConverterId() {
            return converterId;
        }

        @Override
        public FaceletHandler getNextHandler() {
            return parent.getNextHandler();
        }

        @Override
        public Tag getTag() {
            return parent.getTag();
        }

        @Override
        public String getTagId() {
            return parent.getTagId();
        }
    }

    private static final class BehaviorConfigWrapper implements BehaviorConfig {
        private final TagConfig parent;
        private final String behaviorId;

        /**
         * <p class="changed_added_2_0">
         * </p>
         *
         * @param parent
         * @param behaviorId
         */
        public BehaviorConfigWrapper(TagConfig parent, String behaviorId) {
            this.parent = parent;
            this.behaviorId = behaviorId;
        }

        /**
         * <p class="changed_added_2_0">
         * </p>
         *
         * @return the behaviorId
         */
        @Override
        public String getBehaviorId() {
            return behaviorId;
        }

        /**
         * <p class="changed_added_2_0">
         * </p>
         *
         * @see jakarta.faces.view.facelets.TagConfig#getNextHandler()
         */
        @Override
        public FaceletHandler getNextHandler() {
            return parent.getNextHandler();
        }

        /**
         * <p class="changed_added_2_0">
         * </p>
         *
         * @see jakarta.faces.view.facelets.TagConfig#getTag()
         */
        @Override
        public Tag getTag() {
            return parent.getTag();
        }

        /**
         * <p class="changed_added_2_0">
         * </p>
         *
         * @see jakarta.faces.view.facelets.TagConfig#getTagId()
         */
        @Override
        public String getTagId() {
            return parent.getTagId();
        }

    }

    private static class HandlerFactory implements TagHandlerFactory {
        private final static Class[] CONSTRUCTOR_SIG = new Class[] { TagConfig.class };

        protected final Class handlerType;

        public HandlerFactory(Class handlerType) {
            this.handlerType = handlerType;
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            try {
                return (TagHandler) handlerType.getConstructor(CONSTRUCTOR_SIG).newInstance(cfg);
            } catch (InvocationTargetException ite) {
                Throwable t = ite.getCause();
                if (t instanceof FacesException) {
                    throw (FacesException) t;
                } else if (t instanceof ELException) {
                    throw (ELException) t;
                } else {
                    throw new FacesException("Error Instantiating: " + handlerType.getName(), t);
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                throw new FacesException("Error Instantiating: " + handlerType.getName(), e);
            }
        }
    }

    protected static class ComponentConfigWrapper implements ComponentConfig {

        protected final TagConfig parent;

        protected final String componentType;

        protected final String rendererType;

        public ComponentConfigWrapper(TagConfig parent, String componentType, String rendererType) {
            this.parent = parent;
            this.componentType = componentType;
            this.rendererType = rendererType;
        }

        @Override
        public String getComponentType() {
            return componentType;
        }

        @Override
        public String getRendererType() {
            return rendererType;
        }

        @Override
        public FaceletHandler getNextHandler() {
            return parent.getNextHandler();
        }

        @Override
        public Tag getTag() {
            return parent.getTag();
        }

        @Override
        public String getTagId() {
            return parent.getTagId();
        }
    }

    private static class UserTagFactory implements TagHandlerFactory {
        protected final URL location;

        public UserTagFactory(URL location) {
            this.location = location;
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            return new UserTagHandler(cfg, location);
        }
    }

    private static class CompositeComponentTagFactory implements TagHandlerFactory {
        protected final String resourceId;

        public CompositeComponentTagFactory(String resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            ComponentConfig componentConfig = new ComponentConfigWrapper(cfg, "jakarta.faces.NamingContainer", "jakarta.faces.Composite");
            ResourceHandler resourceHandler = FacesContext.getCurrentInstance().getApplication().getResourceHandler();
            TagHandler result = null;
            // Use the naming convention to extract the library name and
            // component name from the resourceId.
            Resource resource = resourceHandler.createResourceFromId(resourceId);
            if (null != resource) {
                result = new CompositeComponentTagHandler(resource, componentConfig);
            } else {
                Location loc = new Location(resourceId, 0, 0);
                Tag tag = new Tag(loc, "", "", "", null);
                throw new TagException(tag, "Cannot create composite component tag handler for composite-source element in taglib.xml file");
            }
            return result;
        }
    }

    private static class ComponentHandlerFactory implements TagHandlerFactory {

        protected final String componentType;

        protected final String renderType;

        public ComponentHandlerFactory(String componentType, String renderType) {
            this.componentType = componentType;
            this.renderType = renderType;
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            ComponentConfig ccfg = new ComponentConfigWrapper(cfg, componentType, renderType);
            return new ComponentHandler(ccfg);
        }
    }

    private static class UserComponentHandlerFactory implements TagHandlerFactory {

        private final static Class[] CONS_SIG = new Class[] { ComponentConfig.class };

        protected final String componentType;

        protected final String renderType;

        protected final Class type;

        protected final Constructor<?> constructor;

        public UserComponentHandlerFactory(String componentType, String renderType, Class type) {
            this.componentType = componentType;
            this.renderType = renderType;
            this.type = type;
            try {
                constructor = this.type.getConstructor(CONS_SIG);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new FaceletException("Must have a Constructor that takes in a ComponentConfig", e);
            }
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            try {
                ComponentConfig ccfg = new ComponentConfigWrapper(cfg, componentType, renderType);
                return (TagHandler) constructor.newInstance(ccfg);
            } catch (InvocationTargetException e) {
                throw new FaceletException(e.getCause().getMessage(), e.getCause().getCause());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                throw new FaceletException("Error Instantiating ComponentHandler: " + type.getName(), e);
            }
        }
    }

    private static class ValidatorHandlerFactory implements TagHandlerFactory {

        protected final String validatorId;

        public ValidatorHandlerFactory(String validatorId) {
            this.validatorId = validatorId;
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            return new ValidatorHandler(new ValidatorConfigWrapper(cfg, validatorId));
        }
    }

    private static class ConverterHandlerFactory implements TagHandlerFactory {

        protected final String converterId;

        public ConverterHandlerFactory(String converterId) {
            this.converterId = converterId;
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            return new ConverterHandler(new ConverterConfigWrapper(cfg, converterId));
        }
    }

    private static final class BehaviorHandlerFactory implements TagHandlerFactory {
        private final String behaviorId;

        /**
         * <p class="changed_added_2_0">
         * </p>
         *
         * @param behaviorId
         */
        public BehaviorHandlerFactory(String behaviorId) {
            this.behaviorId = behaviorId;
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            return new BehaviorHandler(new BehaviorConfigWrapper(cfg, behaviorId));
        }
    }

    private static class UserConverterHandlerFactory implements TagHandlerFactory {
        private final static Class[] CONS_SIG = new Class[] { ConverterConfig.class };

        protected final String converterId;

        protected final Class type;

        protected final Constructor<?> constructor;

        public UserConverterHandlerFactory(String converterId, Class type) {
            this.converterId = converterId;
            this.type = type;
            try {
                constructor = this.type.getConstructor(CONS_SIG);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new FaceletException("Must have a Constructor that takes in a ConverterConfig", e);
            }
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            try {
                ConverterConfig ccfg = new ConverterConfigWrapper(cfg, converterId);
                return (TagHandler) constructor.newInstance(ccfg);
            } catch (InvocationTargetException e) {
                throw new FaceletException(e.getCause().getMessage(), e.getCause().getCause());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                throw new FaceletException("Error Instantiating ConverterHandler: " + type.getName(), e);
            }
        }
    }

    private static class UserValidatorHandlerFactory implements TagHandlerFactory {
        private final static Class[] CONS_SIG = new Class[] { ValidatorConfig.class };

        protected final String validatorId;

        protected final Class type;

        protected final Constructor<?> constructor;

        public UserValidatorHandlerFactory(String validatorId, Class type) {
            this.validatorId = validatorId;
            this.type = type;
            try {
                constructor = this.type.getConstructor(CONS_SIG);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new FaceletException("Must have a Constructor that takes in a ValidatorConfig", e);
            }
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            try {
                ValidatorConfig ccfg = new ValidatorConfigWrapper(cfg, validatorId);
                return (TagHandler) constructor.newInstance(ccfg);
            } catch (InvocationTargetException e) {
                throw new FaceletException(e.getCause().getMessage(), e.getCause().getCause());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                throw new FaceletException("Error Instantiating ValidatorHandler: " + type.getName(), e);
            }
        }
    }

    private static class UserBehaviorHandlerFactory implements TagHandlerFactory {
        private final static Class[] CONS_SIG = new Class[] { BehaviorConfig.class };

        protected final String behaviorId;

        protected final Class type;

        protected final Constructor<?> constructor;

        public UserBehaviorHandlerFactory(String behaviorId, Class type) {
            this.behaviorId = behaviorId;
            this.type = type;
            try {
                constructor = this.type.getConstructor(CONS_SIG);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new FaceletException("Must have a Constructor that takes in a BehaviorConfig", e);
            }
        }

        @Override
        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException {
            try {
                BehaviorConfig ccfg = new BehaviorConfigWrapper(cfg, behaviorId);
                return (TagHandler) constructor.newInstance(ccfg);
            } catch (InvocationTargetException e) {
                throw new FaceletException(e.getCause().getMessage(), e.getCause().getCause());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                throw new FaceletException("Error Instantiating BehaviorHandler: " + type.getName(), e);
            }
        }
    }

    private final Map<String, TagHandlerFactory> factories;

    private final String namespace;

    private final Map<String, Method> functions;

    public AbstractTagLibrary(String namespace) {
        this.namespace = namespace;
        factories = new HashMap<String, TagHandlerFactory>();
        functions = new HashMap<String, Method>();
    }

    /**
     * Add a ComponentHandlerImpl with the specified componentType and rendererType, aliased by the tag name.
     *
     * @see jakarta.faces.application.Application#createComponent(java.lang.String)
     * @param name name to use, "foo" would be {@code <my:foo />}
     * @param componentType componentType to use
     * @param rendererType rendererType to use
     */
    protected final void addComponent(String name, String componentType, String rendererType) {
        factories.put(name, new ComponentHandlerFactory(componentType, rendererType));
    }

    /**
     * Add a ComponentHandlerImpl with the specified componentType and rendererType, aliased by the tag name. The Facelet
     * will be compiled with the specified HandlerType (which must extend AbstractComponentHandler).
     *
     * @param name name to use, "foo" would be {@code <my:foo />}
     * @param componentType componentType to use
     * @param rendererType rendererType to use
     * @param handlerType a Class that extends ComponentHandler
     */
    protected final void addComponent(String name, String componentType, String rendererType, Class handlerType) {
        factories.put(name, new UserComponentHandlerFactory(componentType, rendererType, handlerType));
    }

    /**
     * Add a ConverterHandler for the specified converterId
     *
     * @see ConverterHandler
     * @see jakarta.faces.application.Application#createConverter(java.lang.String)
     * @param name name to use, "foo" would be {@code <my:foo />}
     * @param converterId id to pass to Application instance
     */
    protected final void addConverter(String name, String converterId) {
        factories.put(name, new ConverterHandlerFactory(converterId));
    }

    /**
     * Add a ConverterHandler for the specified converterId of a TagHandler type
     *
     * @see ConverterHandler
     * @see ConverterConfig
     * @see jakarta.faces.application.Application#createConverter(java.lang.String)
     * @param name name to use, "foo" would be {@code <my:foo />}
     * @param converterId id to pass to Application instance
     * @param type TagHandler type that takes in a ConverterConfig
     */
    protected final void addConverter(String name, String converterId, Class type) {
        factories.put(name, new UserConverterHandlerFactory(converterId, type));
    }

    /**
     * Add a ValidatorHandler for the specified validatorId
     *
     * @see ValidatorHandler
     * @see jakarta.faces.application.Application#createValidator(java.lang.String)
     * @param name name to use, "foo" would be {@code <my:foo />}
     * @param validatorId id to pass to Application instance
     */
    protected final void addValidator(String name, String validatorId) {
        factories.put(name, new ValidatorHandlerFactory(validatorId));
    }

    /**
     * Add a ValidatorHandler for the specified validatorId
     *
     * @see ValidatorHandler
     * @see ValidatorConfig
     * @see jakarta.faces.application.Application#createValidator(java.lang.String)
     * @param name name to use, "foo" would be {@code <my:foo />}
     * @param validatorId id to pass to Application instance
     * @param type TagHandler type that takes in a ValidatorConfig
     */
    protected final void addValidator(String name, String validatorId, Class type) {
        factories.put(name, new UserValidatorHandlerFactory(validatorId, type));
    }

    /**
     * <p class="changed_added_2_0">
     * Adds a named behavior
     * </p>
     *
     * @param name
     * @param behaviorId
     */
    protected final void addBehavior(String name, String behaviorId) {
        factories.put(name, new BehaviorHandlerFactory(behaviorId));
    }

    protected final void addBehavior(String name, String behaviorId, Class type) {
        factories.put(name, new UserBehaviorHandlerFactory(behaviorId, type));
    }

    /**
     * Use the specified HandlerType in compiling Facelets. HandlerType must extend TagHandler.
     *
     * @see TagHandler
     * @param name name to use, "foo" would be {@code <my:foo />}
     * @param handlerType must extend TagHandler
     */
    protected final void addTagHandler(String name, Class handlerType) {
        factories.put(name, new HandlerFactory(handlerType));
    }

    /**
     * Add a UserTagHandler specified a the URL source.
     *
     * @see UserTagHandler
     * @param name name to use, "foo" would be {@code <my:foo />}
     * @param source source where the Facelet (Tag) source is
     */
    protected final void addUserTag(String name, URL source) {
        factories.put(name, new UserTagFactory(source));
    }

    /**
     * Add a CompositeComponentTagHandler for the specified resource.
     *
     * @see UserTagHandler
     * @param name name to use, "foo" would be {@code <my:foo />}
     * @param resourceId source where the Facelet (Tag) source is
     */
    protected final void addCompositeComponentTag(String name, String resourceId) {
        factories.put(name, new CompositeComponentTagFactory(resourceId));
    }

    /**
     * Add a Method to be used as a Function at Compilation.
     *
     * @see jakarta.el.FunctionMapper
     *
     * @param name (suffix) of function name
     * @param method method instance
     */
    protected final void addFunction(String name, Method method) {
        functions.put(name, method);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.TagLibrary#containsNamespace(java.lang.String)
     */
    @Override
    public boolean containsNamespace(String ns, Tag t) {
        return namespace.equals(ns);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.TagLibrary#containsTagHandler(java.lang.String, java.lang.String)
     */
    @Override
    public boolean containsTagHandler(String ns, String localName) {
        if (namespace.equals(ns)) {
            if (factories.containsKey(localName)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.TagLibrary#createTagHandler(java.lang.String, java.lang.String, com.sun.facelets.TagConfig)
     */
    @Override
    public TagHandler createTagHandler(String ns, String localName, TagConfig tag) throws FacesException {
        if (namespace.equals(ns)) {
            TagHandlerFactory f = factories.get(localName);
            if (f != null) {
                return f.createHandler(tag);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.TagLibrary#containsFunction(java.lang.String, java.lang.String)
     */
    @Override
    public boolean containsFunction(String ns, String name) {
        if (namespace.equals(ns)) {
            return functions.containsKey(name);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.TagLibrary#createFunction(java.lang.String, java.lang.String)
     */
    @Override
    public Method createFunction(String ns, String name) {
        if (namespace.equals(ns)) {
            return functions.get(name);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TagLibrary && obj.hashCode() == hashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return namespace.hashCode();
    }

    public String getNamespace() {
        return namespace;
    }
}
