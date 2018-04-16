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

package com.sun.faces.application.applicationimpl;

import static com.sun.faces.application.ApplicationImpl.THIS_LIBRARY;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.DateTimeConverterUsesSystemTimezone;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.RegisterConverterPropertyEditors;
import static com.sun.faces.util.Util.isEmpty;
import static com.sun.faces.util.Util.loadClass;
import static com.sun.faces.util.Util.notNull;
import static com.sun.faces.util.Util.notNullNamedObject;
import static java.beans.Introspector.getBeanInfo;
import static java.beans.PropertyEditorManager.findEditor;
import static java.text.MessageFormat.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.faces.application.Resource.COMPONENT_RESOURCE_KEY;
import static javax.faces.component.UIComponent.ATTRS_WITH_DECLARED_DEFAULT_VALUES;
import static javax.faces.component.UIComponent.BEANINFO_KEY;
import static javax.faces.component.UIComponent.COMPOSITE_COMPONENT_TYPE_KEY;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.DateTimeConverter;
import javax.faces.el.ValueBinding;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;
import javax.faces.view.ViewDeclarationLanguage;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ConverterPropertyEditorFactory;
import com.sun.faces.application.ViewMemberInstanceFactoryMetadataMap;
import com.sun.faces.cdi.CdiUtils;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.ReflectionUtils;
import com.sun.faces.util.Util;

public class InstanceFactory {
    
    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();
    
    private static final String CONTEXT = "context";
    private static final String COMPONENT_EXPRESSION = "componentExpression";
    private static final String COMPONENT_TYPE = "componentType";
    private static final String COMPONENT_CLASS = "componentClass";
    
    private static final Map<String, Class<?>[]> STANDARD_CONV_ID_TO_TYPE_MAP = new HashMap<>(8, 1.0f);
    private static final Map<Class<?>, String> STANDARD_TYPE_TO_CONV_ID_MAP = new HashMap<>(16, 1.0f);

    static {
        STANDARD_CONV_ID_TO_TYPE_MAP.put("javax.faces.Byte", new Class[] { Byte.TYPE, Byte.class });
        STANDARD_CONV_ID_TO_TYPE_MAP.put("javax.faces.Boolean", new Class[] { Boolean.TYPE, Boolean.class });
        STANDARD_CONV_ID_TO_TYPE_MAP.put("javax.faces.Character", new Class[] { Character.TYPE, Character.class });
        STANDARD_CONV_ID_TO_TYPE_MAP.put("javax.faces.Short", new Class[] { Short.TYPE, Short.class });
        STANDARD_CONV_ID_TO_TYPE_MAP.put("javax.faces.Integer", new Class[] { Integer.TYPE, Integer.class });
        STANDARD_CONV_ID_TO_TYPE_MAP.put("javax.faces.Long", new Class[] { Long.TYPE, Long.class });
        STANDARD_CONV_ID_TO_TYPE_MAP.put("javax.faces.Float", new Class[] { Float.TYPE, Float.class });
        STANDARD_CONV_ID_TO_TYPE_MAP.put("javax.faces.Double", new Class[] { Double.TYPE, Double.class });
        for (Map.Entry<String, Class<?>[]> entry : STANDARD_CONV_ID_TO_TYPE_MAP.entrySet()) {
            Class<?>[] types = entry.getValue();
            String key = entry.getKey();
            for (Class<?> clazz : types) {
                STANDARD_TYPE_TO_CONV_ID_MAP.put(clazz, key);
            }
        }
    }
    
    private final String[] STANDARD_BY_TYPE_CONVERTER_CLASSES = { "java.math.BigDecimal", "java.lang.Boolean", "java.lang.Byte", "java.lang.Character",
            "java.lang.Double", "java.lang.Float", "java.lang.Integer", "java.lang.Long", "java.lang.Short", "java.lang.Enum" };

    private Map<Class<?>, Object> converterTypeMap;
    private boolean registerPropertyEditors;
    private boolean passDefaultTimeZone;
    
    private TimeZone systemTimeZone;
    
    private static final class ComponentResourceClassNotFound{}
    
    //
    // These four maps store store "identifier" | "class name"
    // mappings.
    //
    private ViewMemberInstanceFactoryMetadataMap<String, Object> componentMap;
    private ViewMemberInstanceFactoryMetadataMap<String, Object> behaviorMap;
    private ViewMemberInstanceFactoryMetadataMap<String, Object> converterIdMap;
    private ViewMemberInstanceFactoryMetadataMap<String, Object> validatorMap;
    
    private Set<String> defaultValidatorIds;
    private volatile Map<String, String> defaultValidatorInfo;
    
    private final ApplicationAssociate associate;
    private Version version;
    
    /**
     * Stores the bean manager.
     */
    private BeanManager beanManager;
    
    public InstanceFactory(ApplicationAssociate applicationAssociate) {
        this.associate = applicationAssociate;
        version = new Version();
        
        componentMap = new ViewMemberInstanceFactoryMetadataMap<>(new ConcurrentHashMap<>());
        converterIdMap = new ViewMemberInstanceFactoryMetadataMap<>(new ConcurrentHashMap<>());
        converterTypeMap = new ConcurrentHashMap<>();
        validatorMap = new ViewMemberInstanceFactoryMetadataMap<>(new ConcurrentHashMap<>());
        defaultValidatorIds = new LinkedHashSet<>();
        behaviorMap = new ViewMemberInstanceFactoryMetadataMap<>(new ConcurrentHashMap<>());
        
        WebConfiguration webConfig = WebConfiguration.getInstance(FacesContext.getCurrentInstance().getExternalContext());
        registerPropertyEditors = webConfig.isOptionEnabled(RegisterConverterPropertyEditors);
        
        passDefaultTimeZone = webConfig.isOptionEnabled(DateTimeConverterUsesSystemTimezone);
        if (passDefaultTimeZone) {
            systemTimeZone = TimeZone.getDefault();
        }
    }
    
    /**
     * @see javax.faces.application.Application#addComponent(java.lang.String, java.lang.String)
     */
    public void addComponent(String componentType, String componentClass) {

        notNull(COMPONENT_TYPE, componentType);
        notNull(COMPONENT_CLASS, componentClass);

        if (LOGGER.isLoggable(FINE) && componentMap.containsKey(componentType)) {
            LOGGER.log(FINE, "componentType {0} has already been registered.  Replacing existing component class type {1} with {2}.",
                    new Object[] { componentType, componentMap.get(componentType), componentClass });
        }

        componentMap.put(componentType, componentClass);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("added component of type ''{0}'' and class ''{1}''", componentType, componentClass));
        }
    }
    
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType) throws FacesException {

        notNull(COMPONENT_EXPRESSION, componentExpression);
        notNull(CONTEXT, context);
        notNull(COMPONENT_TYPE, componentType);

        return createComponentApplyAnnotations(context, componentExpression, componentType, null, true);
    }
    
    public UIComponent createComponent(String componentType) throws FacesException {

        notNull(COMPONENT_TYPE, componentType);

        return createComponentApplyAnnotations(FacesContext.getCurrentInstance(), componentType, null, true);
    }
    
    public UIComponent createComponent(FacesContext context, Resource componentResource, ExpressionFactory expressionFactory) throws FacesException {

        // RELEASE_PENDING (rlubke,driscoll) this method needs review.

        notNull(CONTEXT, context);
        notNull("componentResource", componentResource);

        UIComponent result = null;

        // Use the application defined in the FacesContext as we may be calling
        // overriden methods
        Application app = context.getApplication();

        ViewDeclarationLanguage vdl = app.getViewHandler().getViewDeclarationLanguage(context, context.getViewRoot().getViewId());
        BeanInfo componentMetadata = vdl.getComponentMetadata(context, componentResource);

        if (componentMetadata != null) {
            BeanDescriptor componentBeanDescriptor = componentMetadata.getBeanDescriptor();

            // Step 1. See if the composite component author explicitly
            // gave a componentType as part of the composite component metadata
            ValueExpression valueExpression = (ValueExpression) componentBeanDescriptor.getValue(COMPOSITE_COMPONENT_TYPE_KEY);

            if (valueExpression != null) {
                String componentType = (String) valueExpression.getValue(context.getELContext());
                if (!isEmpty(componentType)) {
                    result = app.createComponent(componentType);
                }
            }
        }

        // Step 2. If that didn't work, if a script based resource can be
        // found for the scriptComponentResource, see if a component can be generated from it
        if (result == null) {
            Resource scriptComponentResource = vdl.getScriptComponentResource(context, componentResource);

            if (scriptComponentResource != null) {
                result = createComponentFromScriptResource(context, scriptComponentResource, componentResource);
            }
        }

        // Step 3. Use the libraryName of the resource as the java package
        // and use the resourceName as the class name. See
        // if a Java class can be loaded
        if (result == null) {
            String packageName = componentResource.getLibraryName();
            String className = componentResource.getResourceName();
            className = packageName + '.' + className.substring(0, className.lastIndexOf('.'));
            try {
                Class<?> clazz = (Class<?>) componentMap.get(className);
                if (clazz == null) {
                    clazz = loadClass(className, this);
                }
                if (clazz != ComponentResourceClassNotFound.class) {
                    if (!associate.isDevModeEnabled()) {
                        componentMap.put(className, clazz);
                    }
                    result = (UIComponent) clazz.newInstance();
                }
            } catch (ClassNotFoundException ex) {
                if (!associate.isDevModeEnabled()) {
                    componentMap.put(className, ComponentResourceClassNotFound.class);
                }
            } catch (InstantiationException | IllegalAccessException | ClassCastException ie) {
                throw new FacesException(ie);
            }
        }

        // Step 4. Use javax.faces.NamingContainer as the component type
        if (result == null) {
            result = app.createComponent("javax.faces.NamingContainer");
        }

        result.setRendererType("javax.faces.Composite");

        Map<String, Object> attrs = result.getAttributes();
        attrs.put(COMPONENT_RESOURCE_KEY, componentResource);
        attrs.put(BEANINFO_KEY, componentMetadata);

        associate.getAnnotationManager().applyComponentAnnotations(context, result);
        pushDeclaredDefaultValuesToAttributesMap(context, componentMetadata, attrs, result, expressionFactory);

        return result;
    }
    
    public UIComponent createComponent(FacesContext context, String componentType, String rendererType) {
        return createComponentApplyAnnotations(context, componentType, rendererType, true);
    }
    
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType, String rendererType) {

        notNull(COMPONENT_EXPRESSION, componentExpression);
        notNull(CONTEXT, context);
        notNull(COMPONENT_TYPE, componentType);

        return createComponentApplyAnnotations(context, componentExpression, componentType, rendererType, true);
    }
    
    public UIComponent createComponent(ValueBinding componentBinding, FacesContext context, String componentType) throws FacesException {

        notNull("componentBinding", componentBinding);
        notNull(CONTEXT, context);
        notNull(COMPONENT_TYPE, componentType);

        Object result;
        boolean createOne = false;
        try {
            result = componentBinding.getValue(context);
            if (result != null) {
                createOne = !(result instanceof UIComponent);
            }

            if (result == null || createOne) {
                result = createComponentApplyAnnotations(context, componentType, null, false);
                componentBinding.setValue(context, result);
            }
        } catch (Exception ex) {
            throw new FacesException(ex);
        }

        return (UIComponent) result;
    }
    
    /**
     * @see javax.faces.application.Application#getComponentTypes()
     */
    public Iterator<String> getComponentTypes() {
        return componentMap.keySet().iterator();
    }
    
    /**
     * @see javax.faces.application.Application#addBehavior(String, String)
     */
    public void addBehavior(String behaviorId, String behaviorClass) {

        notNull("behaviorId", behaviorId);
        notNull("behaviorClass", behaviorClass);

        if (LOGGER.isLoggable(FINE) && behaviorMap.containsKey(behaviorId)) {
            LOGGER.log(FINE, "behaviorId {0} has already been registered.  Replacing existing behavior class type {1} with {2}.",
                    new Object[] { behaviorId, behaviorMap.get(behaviorId), behaviorClass });
        }

        behaviorMap.put(behaviorId, behaviorClass);

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("added behavior of type ''{0}'' class ''{1}''", behaviorId, behaviorClass));
        }
    }
    
    /**
     * @see javax.faces.application.Application#createBehavior(String)
     */
    public Behavior createBehavior(String behaviorId) throws FacesException {

        notNull("behaviorId", behaviorId);

        Behavior behavior = createCDIBehavior(behaviorId);
        if (behavior != null) {
            return behavior;
        }

        behavior = newThing(behaviorId, behaviorMap);
        
        notNullNamedObject(behavior, behaviorId, "jsf.cannot_instantiate_behavior_error");

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("created behavior of type ''{0}''", behaviorId));
        }

        associate.getAnnotationManager().applyBehaviorAnnotations(FacesContext.getCurrentInstance(), behavior);

        return behavior;
    }
    
    /**
     * @see javax.faces.application.Application#getBehaviorIds()
     */
    public Iterator<String> getBehaviorIds() {
        return behaviorMap.keySet().iterator();
    }
    
    public void addConverter(String converterId, String converterClass) {

        notNull("converterId", converterId);
        notNull("converterClass", converterClass);

        if (LOGGER.isLoggable(FINE) && converterIdMap.containsKey(converterId)) {
            LOGGER.log(FINE, "converterId {0} has already been registered.  Replacing existing converter class type {1} with {2}.",
                    new Object[] { converterId, converterIdMap.get(converterId), converterClass });
        }

        converterIdMap.put(converterId, converterClass);

        Class<?>[] types = STANDARD_CONV_ID_TO_TYPE_MAP.get(converterId);
        if (types != null) {
            for (Class<?> clazz : types) {
                // go directly against map to prevent cyclic method calls
                converterTypeMap.put(clazz, converterClass);
                addPropertyEditorIfNecessary(clazz);
            }
        }

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(format("added converter of type ''{0}'' and class ''{1}''", converterId, converterClass));
        }
    }
    
    /**
     * @see javax.faces.application.Application#addConverter(Class, String)
     */
    public void addConverter(Class<?> targetClass, String converterClass) {

        notNull("targetClass", targetClass);
        notNull("converterClass", converterClass);

        String converterId = STANDARD_TYPE_TO_CONV_ID_MAP.get(targetClass);
        if (converterId != null) {
            addConverter(converterId, converterClass);
        } else {
            if (LOGGER.isLoggable(FINE) && converterTypeMap.containsKey(targetClass)) {
                LOGGER.log(FINE, "converter target class {0} has already been registered.  Replacing existing converter class type {1} with {2}.",
                        new Object[] { targetClass.getName(), converterTypeMap.get(targetClass), converterClass });
            }

            converterTypeMap.put(targetClass, converterClass);
            addPropertyEditorIfNecessary(targetClass);
        }

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(format("added converter of class type ''{0}''", converterClass));
        }
    }
    
    /**
     * @see javax.faces.application.Application#createConverter(String)
     */
    public Converter<?> createConverter(String converterId) {

        notNull("converterId", converterId);
        
        Converter<?> converter = createCDIConverter(converterId);
        if (converter != null) {
            return converter;
        }

        converter = newThing(converterId, converterIdMap);
        
        notNullNamedObject(converter, converterId, "jsf.cannot_instantiate_converter_error");
        
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("created converter of type ''{0}''", converterId));
        }
        
        if (passDefaultTimeZone && converter instanceof DateTimeConverter) {
            ((DateTimeConverter) converter).setTimeZone(systemTimeZone);
        }
        
        associate.getAnnotationManager().applyConverterAnnotations(FacesContext.getCurrentInstance(), converter);
        
        return converter;
    }
    
    /**
     * @see javax.faces.application.Application#createConverter(Class)
     */
    public Converter createConverter(Class<?> targetClass) {

        Util.notNull("targetClass", targetClass);
        Converter returnVal = null;

        if (version.isJsf23()) {
            BeanManager beanManager = getBeanManager();
            returnVal = CdiUtils.createConverter(beanManager, targetClass);
            if (returnVal != null) {
                return returnVal;
            }
        }

        returnVal = (Converter) newConverter(targetClass, converterTypeMap, targetClass);
        if (returnVal != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(MessageFormat.format("Created converter of type ''{0}''", returnVal.getClass().getName()));
            }
            if (passDefaultTimeZone && returnVal instanceof DateTimeConverter) {
                ((DateTimeConverter) returnVal).setTimeZone(systemTimeZone);
            }
            associate.getAnnotationManager().applyConverterAnnotations(FacesContext.getCurrentInstance(), returnVal);
            return returnVal;
        }

        // Search for converters registered to interfaces implemented by
        // targetClass
        Class<?>[] interfaces = targetClass.getInterfaces();
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                returnVal = createConverterBasedOnClass(interfaces[i], targetClass);
                if (returnVal != null) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(MessageFormat.format("Created converter of type ''{0}''", returnVal.getClass().getName()));
                    }
                    if (passDefaultTimeZone && returnVal instanceof DateTimeConverter) {
                        ((DateTimeConverter) returnVal).setTimeZone(systemTimeZone);
                    }
                    associate.getAnnotationManager().applyConverterAnnotations(FacesContext.getCurrentInstance(), returnVal);
                    return returnVal;
                }
            }
        }

        // Search for converters registered to superclasses of targetClass
        Class<?> superclass = targetClass.getSuperclass();
        if (superclass != null) {
            returnVal = createConverterBasedOnClass(superclass, targetClass);
            if (returnVal != null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(MessageFormat.format("Created converter of type ''{0}''", returnVal.getClass().getName()));
                }
                if (passDefaultTimeZone && returnVal instanceof DateTimeConverter) {
                    ((DateTimeConverter) returnVal).setTimeZone(systemTimeZone);
                }
                associate.getAnnotationManager().applyConverterAnnotations(FacesContext.getCurrentInstance(), returnVal);
                return returnVal;
            }
        }

        return returnVal;
    }
    
    /**
     * @see javax.faces.application.Application#getConverterIds()
     */
    public Iterator<String> getConverterIds() {
        return converterIdMap.keySet().iterator();

    }

    /**
     * @see javax.faces.application.Application#getConverterTypes()
     */
    public Iterator<Class<?>> getConverterTypes() {
        return converterTypeMap.keySet().iterator();
    }
    
    /**
     * @see javax.faces.application.Application#addValidator(String, String)
     */
    public void addValidator(String validatorId, String validatorClass) {

        Util.notNull("validatorId", validatorId);
        Util.notNull("validatorClass", validatorClass);

        if (LOGGER.isLoggable(Level.FINE) && validatorMap.containsKey(validatorId)) {
            LOGGER.log(Level.FINE, "validatorId {0} has already been registered.  Replacing existing validator class type {1} with {2}.",
                    new Object[] { validatorId, validatorMap.get(validatorId), validatorClass });
        }

        validatorMap.put(validatorId, validatorClass);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(MessageFormat.format("added validator of type ''{0}'' class ''{1}''", validatorId, validatorClass));
        }

    }
    
    /**
     * @see javax.faces.application.Application#createValidator(String)
     */
    public Validator<?> createValidator(String validatorId) throws FacesException {

        notNull("validatorId", validatorId);

        Validator<?> validator = createCDIValidator(validatorId);
        if (validator != null) {
            return validator;
        }

        validator = newThing(validatorId, validatorMap);
        
        notNullNamedObject(validator, validatorId, "jsf.cannot_instantiate_validator_error");
        
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.fine(MessageFormat.format("created validator of type ''{0}''", validatorId));
        }
        
        associate.getAnnotationManager().applyValidatorAnnotations(FacesContext.getCurrentInstance(), validator);
        
        return validator;
    }
    
    /**
     * @see javax.faces.application.Application#getValidatorIds()
     */
    public Iterator<String> getValidatorIds() {
        return validatorMap.keySet().iterator();
    }
    
    /**
     * @see javax.faces.application.Application#addDefaultValidatorId(String)
     */
    public synchronized void addDefaultValidatorId(String validatorId) {

        notNull("validatorId", validatorId);
        
        defaultValidatorInfo = null;
        defaultValidatorIds.add(validatorId);
    }
    
    /**
     * @see javax.faces.application.Application#getDefaultValidatorInfo()
     */
    public Map<String, String> getDefaultValidatorInfo() {

        if (defaultValidatorInfo == null) {
            synchronized (this) {
                if (defaultValidatorInfo == null) {
                    defaultValidatorInfo = new LinkedHashMap<>();
                    if (!defaultValidatorIds.isEmpty()) {
                        for (String id : defaultValidatorIds) {
                            String validatorClass;
                            Object result = validatorMap.get(id);
                            if (null != result) {
                                if (result instanceof Class) {
                                    validatorClass = ((Class) result).getName();
                                } else {
                                    validatorClass = result.toString();
                                }
                                defaultValidatorInfo.put(id, validatorClass);
                            }
                        }

                    }
                }
            }
            defaultValidatorInfo = unmodifiableMap(defaultValidatorInfo);
        }

        return defaultValidatorInfo;

    }
    
    
    
    
   
    // --------------------------------------------------------- Private Methods
    

    private UIComponent createComponentFromScriptResource(FacesContext context, Resource scriptComponentResource, Resource componentResource) {

        UIComponent result = null;

        String className = scriptComponentResource.getResourceName();
        int lastDot = className.lastIndexOf('.');
        className = className.substring(0, lastDot);

        try {

            Class<?> componentClass = (Class<?>) componentMap.get(className);
            if (componentClass == null) {
                componentClass = Util.loadClass(className, this);
            }
            if (!associate.isDevModeEnabled()) {
                componentMap.put(className, componentClass);
            }
            result = (UIComponent) componentClass.newInstance();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        if (result != null) {
            // Make sure the resource is there for the annotation processor.
            result.getAttributes().put(Resource.COMPONENT_RESOURCE_KEY, componentResource);
            // In case there are any "this" references,
            // make sure they can be resolved.
            context.getAttributes().put(THIS_LIBRARY, componentResource.getLibraryName());
            try {
                associate.getAnnotationManager().applyComponentAnnotations(context, result);
            } finally {
                context.getAttributes().remove(THIS_LIBRARY);
            }
        }

        return result;

    }
    
    /**
     * Leveraged by
     * {@link Application#createComponent(javax.el.ValueExpression, javax.faces.context.FacesContext, String)}
     * and
     * {@link Application#createComponent(javax.el.ValueExpression, javax.faces.context.FacesContext, String, String)}.
     * This method will apply any component and render annotations that may be present.
     */
    private UIComponent createComponentApplyAnnotations(FacesContext ctx, ValueExpression componentExpression, String componentType, String rendererType,
            boolean applyAnnotations) {

        UIComponent c;

        try {
            c = (UIComponent) componentExpression.getValue(ctx.getELContext());

            if (c == null) {
                c = this.createComponentApplyAnnotations(ctx, componentType, rendererType, applyAnnotations);
                componentExpression.setValue(ctx.getELContext(), c);
            } else if (applyAnnotations) {
                this.applyAnnotations(ctx, rendererType, c);
            }
        } catch (Exception ex) {
            throw new FacesException(ex);
        }

        return c;

    }
    
    /**
     * Leveraged by {@link Application#createComponent(String)} and
     * {@link Application#createComponent(javax.faces.context.FacesContext, String, String)} This
     * method will apply any component and render annotations that may be present.
     */
    private UIComponent createComponentApplyAnnotations(FacesContext ctx, String componentType, String rendererType, boolean applyAnnotations) {

        UIComponent component;
        try {
            component = newThing(componentType, componentMap);
        } catch (Exception ex) {
            if (LOGGER.isLoggable(SEVERE)) {
                LOGGER.log(Level.SEVERE, "jsf.cannot_instantiate_component_error", componentType);
            }
            throw new FacesException(ex);
        }
        
        notNullNamedObject(component, componentType, "jsf.cannot_instantiate_component_error");

        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, MessageFormat.format("Created component with component type of ''{0}''", componentType));
        }

        if (applyAnnotations) {
            applyAnnotations(ctx, rendererType, component);
        }
        
        return component;
    }
    
    
    /**
     * Process any annotations associated with this component/renderer.
     */
    private void applyAnnotations(FacesContext ctx, String rendererType, UIComponent c) {

        if (c != null && ctx != null) {
            associate.getAnnotationManager().applyComponentAnnotations(ctx, c);
            if (rendererType != null) {
                RenderKit rk = ctx.getRenderKit();
                Renderer r = null;
                if (rk != null) {
                    r = rk.getRenderer(c.getFamily(), rendererType);
                    if (r != null) {
                        c.setRendererType(rendererType);
                        associate.getAnnotationManager().applyRendererAnnotations(ctx, r, c);
                    }
                }
                if ((rk == null || r == null) && LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Unable to create Renderer with rendererType {0} for component with component type of {1}",
                            new Object[] { rendererType, c.getFamily() });
                }
            }
        }
    }

    /**
     * <p>
     * PRECONDITIONS: the values in the Map are either Strings representing fully qualified java
     * class names, or java.lang.Class instances.
     * </p>
     * <p>
     * ALGORITHM: Look in the argument map for a value for the argument key. If found, if the value
     * is instanceof String, assume the String specifies a fully qualified java class name and
     * obtain the java.lang.Class instance for that String using Util.loadClass(). Replace the
     * String instance in the argument map with the Class instance. If the value is instanceof
     * Class, proceed. Assert that the value is either instanceof java.lang.Class or
     * java.lang.String.
     * </p>
     * <p>
     * Now that you have a java.lang.class, call its newInstance and return it as the result of this
     * method.
     * </p>
     *
     * @param key Used to look up the value in the <code>Map</code>.
     * @param map The <code>Map</code> that will be searched.
     * @return The new object instance.
     */
    @SuppressWarnings("unchecked")
    private <T> T newThing(String key, ViewMemberInstanceFactoryMetadataMap<String, Object> map) {

        Object result;
        Class<?> clazz;
        Object value;

        value = map.get(key);
        if (value == null) {
            return null;
        }
        assert value instanceof String || value instanceof Class;
        if (value instanceof String) {
            String cValue = (String) value;
            try {
                clazz = Util.loadClass(cValue, value);
                if (!associate.isDevModeEnabled()) {
                    map.put(key, clazz);
                }
                assert clazz != null;
            } catch (Exception e) {
                throw new FacesException(e.getMessage(), e);
            }
        } else {
            clazz = (Class) value;
        }

        try {
            result = clazz.newInstance();
        } catch (Throwable t) {
            Throwable previousT;
            do {
                previousT = t;
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "Unable to load class: ", t);
                }
            } while (null != (t = t.getCause()));
            t = previousT;

            throw new FacesException(MessageUtils.getExceptionMessageString(MessageUtils.CANT_INSTANTIATE_CLASS_ERROR_MESSAGE_ID, clazz.getName()), t);
        }

        return (T) result;
    }
    
    /*
     * This method makes it so that any cc:attribute elements that have a "default" attribute value
     * have those values pushed into the composite component attribute map so that programmatic
     * access (as opposed to EL access) will find the attribute values.
     *
     */
    @SuppressWarnings("unchecked")
    private void pushDeclaredDefaultValuesToAttributesMap(FacesContext context, BeanInfo componentMetadata, Map<String, Object> attrs, UIComponent component, ExpressionFactory expressionFactory) {

        Collection<String> attributesWithDeclaredDefaultValues = null;
        PropertyDescriptor[] propertyDescriptors = null;

        for (PropertyDescriptor propertyDescriptor : componentMetadata.getPropertyDescriptors()) {
            Object defaultValue = propertyDescriptor.getValue("default");

            if (defaultValue != null) {
                String key = propertyDescriptor.getName();
                boolean isLiteralText = false;

                if (defaultValue instanceof ValueExpression) {
                    isLiteralText = ((ValueExpression) defaultValue).isLiteralText();
                    if (isLiteralText) {
                        defaultValue = ((ValueExpression) defaultValue).getValue(context.getELContext());
                    }
                }

                // Ensure this attribute is not a method-signature. method-signature
                // declared default values are handled in retargetMethodExpressions.
                if (propertyDescriptor.getValue("method-signature") == null || propertyDescriptor.getValue("type") != null) {

                    if (attributesWithDeclaredDefaultValues == null) {
                        BeanDescriptor beanDescriptor = componentMetadata.getBeanDescriptor();
                        attributesWithDeclaredDefaultValues = (Collection<String>) beanDescriptor.getValue(ATTRS_WITH_DECLARED_DEFAULT_VALUES);
                        if (attributesWithDeclaredDefaultValues == null) {
                            attributesWithDeclaredDefaultValues = new HashSet<>();
                            beanDescriptor.setValue(ATTRS_WITH_DECLARED_DEFAULT_VALUES, attributesWithDeclaredDefaultValues);
                        }
                    }
                    attributesWithDeclaredDefaultValues.add(key);

                    // Only store the attribute if it is literal text. If it
                    // is a ValueExpression, it will be handled explicitly in
                    // CompositeComponentAttributesELResolver.ExpressionEvalMap.get().
                    // If it is a MethodExpression, it will be dealt with in
                    // retargetMethodExpressions.
                    if (isLiteralText) {
                        try {
                            if (propertyDescriptors == null) {
                                propertyDescriptors = getBeanInfo(component.getClass()).getPropertyDescriptors();
                            }
                        } catch (IntrospectionException e) {
                            throw new FacesException(e);
                        }

                        defaultValue = convertValueToTypeIfNecessary(key, defaultValue, propertyDescriptors, expressionFactory);
                        attrs.put(key, defaultValue);
                    }
                }
            }
        }
    }
    
    /**
     * Helper method to convert a value to a type as defined in PropertyDescriptor(s)
     * 
     * @param name
     * @param value
     * @param propertyDescriptors
     * @return value
     */
    private Object convertValueToTypeIfNecessary(String name, Object value, PropertyDescriptor[] propertyDescriptors, ExpressionFactory expressionFactory) {
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equals(name)) {
                value = expressionFactory.coerceToType(value, propertyDescriptor.getPropertyType());
                break;
            }
        }

        return value;
    }
    

    /**
     * <p>
     * To enable EL Coercion to use JSF Custom converters, this method will call
     * <code>PropertyEditorManager.registerEditor()</code>, passing the
     * <code>ConverterPropertyEditor</code> class for the <code>targetClass</code> if the target
     * class is not one of the standard by-type converter target classes.
     * 
     * @param targetClass the target class for which a PropertyEditory may or may not be created
     */
    private void addPropertyEditorIfNecessary(Class<?> targetClass) {

        if (!registerPropertyEditors) {
            return;
        }

        PropertyEditor editor = findEditor(targetClass);
        if (editor != null) {
            return;
        }
        String className = targetClass.getName();

        // Don't add a PropertyEditor for the standard by-type converters.
        if (targetClass.isPrimitive()) {
            return;
        }

        for (String standardClass : STANDARD_BY_TYPE_CONVERTER_CLASSES) {
            if (standardClass.indexOf(className) != -1) {
                return;
            }
        }

        Class<?> editorClass = ConverterPropertyEditorFactory.getDefaultInstance().definePropertyEditorClassFor(targetClass);
        if (editorClass != null) {
            PropertyEditorManager.registerEditor(targetClass, editorClass);
        } else {
            if (LOGGER.isLoggable(WARNING)) {
                LOGGER.warning(MessageFormat.format("definePropertyEditorClassFor({0}) returned null.", targetClass.getName()));
            }
        }
    }
    
    private Converter createConverterBasedOnClass(Class<?> targetClass, Class<?> baseClass) {

        Converter returnVal = (Converter) newConverter(targetClass, converterTypeMap, baseClass);
        if (returnVal != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(MessageFormat.format("Created converter of type ''{0}''", returnVal.getClass().getName()));
            }
            return returnVal;
        }

        // Search for converters registered to interfaces implemented by
        // targetClass
        Class<?>[] interfaces = targetClass.getInterfaces();
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                returnVal = createConverterBasedOnClass(interfaces[i], null);
                if (returnVal != null) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(MessageFormat.format("Created converter of type ''{0}''", returnVal.getClass().getName()));
                    }
                    return returnVal;
                }
            }
        }

        // Search for converters registered to superclasses of targetClass
        Class<?> superclass = targetClass.getSuperclass();
        if (superclass != null) {
            returnVal = createConverterBasedOnClass(superclass, targetClass);
            if (returnVal != null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(MessageFormat.format("Created converter of type ''{0}''", returnVal.getClass().getName()));
                }
                return returnVal;
            }
        }
        return returnVal;
    }
    
    /**
     * <p>
     * The same as newThing except that a single argument constructor that accepts a Class is looked
     * for before calling the no-arg version.
     * </p>
     *
     * <p>
     * PRECONDITIONS: the values in the Map are either Strings representing fully qualified java
     * class names, or java.lang.Class instances.
     * </p>
     * <p>
     * ALGORITHM: Look in the argument map for a value for the argument key. If found, if the value
     * is instanceof String, assume the String specifies a fully qualified java class name and
     * obtain the java.lang.Class instance for that String using Util.loadClass(). Replace the
     * String instance in the argument map with the Class instance. If the value is instanceof
     * Class, proceed. Assert that the value is either instanceof java.lang.Class or
     * java.lang.String.
     * </p>
     * <p>
     * Now that you have a java.lang.class, call its newInstance and return it as the result of this
     * method.
     * </p>
     *
     * @param key Used to look up the value in the <code>Map</code>.
     * @param map The <code>Map</code> that will be searched.
     * @param targetClass the target class for the single argument ctor
     * @return The new object instance.
     */
    protected Object newConverter(Class<?> key, Map<Class<?>, Object> map, Class<?> targetClass) {
        assert key != null && map != null;

        Object result = null;
        Class<?> clazz;
        Object value;

        value = map.get(key);
        if (value == null) {
            return null;
        }
        assert value instanceof String || value instanceof Class;
        if (value instanceof String) {
            String cValue = (String) value;
            try {
                clazz = Util.loadClass(cValue, value);
                if (!associate.isDevModeEnabled()) {
                    map.put(key, clazz);
                }
                assert clazz != null;
            } catch (Exception e) {
                throw new FacesException(e.getMessage(), e);
            }
        } else {
            clazz = (Class) value;
        }

        Constructor ctor = ReflectionUtils.lookupConstructor(clazz, Class.class);
        Throwable cause = null;
        if (ctor != null) {
            try {
                result = ctor.newInstance(targetClass);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                cause = e;
            }
        } else {
            try {
                result = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                cause = e;
            }
        }

        if (null != cause) {
            throw new FacesException(MessageUtils.getExceptionMessageString(MessageUtils.CANT_INSTANTIATE_CLASS_ERROR_MESSAGE_ID, clazz.getName()), cause);

        }
        return result;
    }
    

    
    /**
     * Get the bean manager.
     * 
     * @return the bean manager.
     */
    private BeanManager getBeanManager() {
        if (beanManager == null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            beanManager = Util.getCdiBeanManager(facesContext);
        }
        return beanManager;
    }
    
    private Behavior createCDIBehavior(String behaviorId) {
        if (version.isJsf23()) {
            return CdiUtils.createBehavior(getBeanManager(), behaviorId);
        }
        
        return null;
    }
    
    private Converter<?> createCDIConverter(String converterId) {
        if (version.isJsf23()) {
            return CdiUtils.createConverter(getBeanManager(), converterId);
        }
        
        return null;
    }
    
    private Validator<?> createCDIValidator(String validatorId) {
        if (version.isJsf23()) {
            return CdiUtils.createValidator(getBeanManager(), validatorId);
        }
        
        return null;
    }

}
