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

package com.sun.faces.el;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.sun.faces.context.flash.FlashELResolver;

import jakarta.el.ArrayELResolver;
import jakarta.el.BeanELResolver;
import jakarta.el.ELClass;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.OptionalELResolver;
import jakarta.el.RecordELResolver;
import jakarta.el.ResourceBundleELResolver;
import jakarta.el.StaticFieldELResolver;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.Flash;

/**
 * Maintains an ordered composite list of child <code>ELResolver for Faces</code>.
 *
 */
public class DemuxCompositeELResolver extends FacesCompositeELResolver {
    private final ELResolverChainType _chainType;

    private ELResolver[] _rootELResolvers = new ELResolver[2];
    private ELResolver[] _propertyELResolvers = new ELResolver[2];
    private ELResolver[] _allELResolvers = new ELResolver[2];
    private ELResolver[] _convertELResolvers = new ELResolver[2];

    private int _rootELResolverCount = 0;
    private int _propertyELResolverCount = 0;
    private int _allELResolverCount = 0;
    private int _convertELResolverCount = 0;

    /*
     * convertToType shortcut. Every value expression evaluation ends with ELContext.convertToType, which consults the
     * convert resolvers. Usually these are only the OptionalELResolver, which converts nothing but an Optional, and the
     * still empty Application#addELResolver composite, so the walk can be skipped when it certainly declines.
     */
    private int _unknownConvertResolverCount = 0;
    private ApplicationELResolvers[] _applicationConvertResolvers = new ApplicationELResolvers[0];

    /*
     * getValue shortcuts. Most resolvers of the chain decide whether they resolve from the base class alone (property
     * resolution: Map, List, Bean...) or from the root name alone (CDI bean names, "cc", resource bundle vars...), so
     * for a given base class or root name every evaluation walks the same leading resolvers that all decline. These
     * caches remember where the walk can start. A resolver is only ever skipped when it is one of the known resolvers
     * below and provably cannot resolve that class or name; an unknown (application or faces-config) resolver is a
     * barrier that is always consulted, so the observable result is the same as walking the whole chain.
     */

    /** Set the system property <code>com.sun.faces.disableELResolverShortcuts=true</code> to always walk the whole chain. */
    private static final boolean SHORTCUTS = !Boolean.getBoolean("com.sun.faces.disableELResolverShortcuts");

    /** Root names: index of the first root resolver that may resolve the name. */
    private final Map<String, Integer> _rootStart = new ConcurrentHashMap<>();
    private static final int MAX_ROOT_NAMES = 4096;

    /** Base classes: index of the first property resolver that may resolve an instance of the class. */
    private volatile ClassValue<Integer> _propertyStart = newPropertyStarts();

    public DemuxCompositeELResolver(ELResolverChainType chainType) {
        if (chainType == null) {
            throw new NullPointerException();
        }

        _chainType = chainType;
    }

    @Override
    public ELResolverChainType getChainType() {
        return _chainType;
    }

    private void _addAllELResolver(ELResolver elResolver) {
        clearShortcuts();
        if (elResolver == null) {
            throw new NullPointerException();
        }

        // grow array, if necessary
        if (_allELResolverCount == _allELResolvers.length) {
            ELResolver[] biggerResolvers = new ELResolver[_allELResolverCount * 2];
            System.arraycopy(_allELResolvers, 0, biggerResolvers, 0, _allELResolverCount);
            _allELResolvers = biggerResolvers;
        }

        // assign new resolver to end
        _allELResolvers[_allELResolverCount] = elResolver;
        _allELResolverCount++;
    }

    private void _addRootELResolver(ELResolver elResolver) {
        if (elResolver == null) {
            throw new NullPointerException();
        }

        // grow array, if necessary
        if (_rootELResolverCount == _rootELResolvers.length) {
            ELResolver[] biggerResolvers = new ELResolver[_rootELResolverCount * 2];
            System.arraycopy(_rootELResolvers, 0, biggerResolvers, 0, _rootELResolverCount);
            _rootELResolvers = biggerResolvers;
        }

        // assign new resolver to end
        _rootELResolvers[_rootELResolverCount] = elResolver;
        _rootELResolverCount++;
    }

    /**
     * Registers <code>elResolver</code> for {@link #convertToType} only when it actually declares that method.
     * {@link ELResolver#convertToType} returns <code>null</code> without marking the property as resolved, so a
     * resolver which inherits it can never contribute a conversion, and consulting it is a no-op.
     */
    private void _addConvertELResolver(ELResolver elResolver) {
        if (!declaresConvertToType(elResolver)) {
            return;
        }

        if (elResolver.getClass() == ApplicationELResolvers.class) {
            _applicationConvertResolvers = Arrays.copyOf(_applicationConvertResolvers, _applicationConvertResolvers.length + 1);
            _applicationConvertResolvers[_applicationConvertResolvers.length - 1] = (ApplicationELResolvers) elResolver;
        } else if (elResolver.getClass() != OptionalELResolver.class) {
            _unknownConvertResolverCount++;
        }

        // grow array, if necessary
        if (_convertELResolverCount == _convertELResolvers.length) {
            ELResolver[] biggerResolvers = new ELResolver[_convertELResolverCount * 2];
            System.arraycopy(_convertELResolvers, 0, biggerResolvers, 0, _convertELResolverCount);
            _convertELResolvers = biggerResolvers;
        }

        // assign new resolver to end
        _convertELResolvers[_convertELResolverCount] = elResolver;
        _convertELResolverCount++;
    }

    static boolean declaresConvertToType(ELResolver elResolver) {
        try {
            return elResolver.getClass().getMethod("convertToType", ELContext.class, Object.class, Class.class).getDeclaringClass() != ELResolver.class;
        } catch (NoSuchMethodException e) {
            // Cannot happen: convertToType is public on ELResolver itself.
            return true;
        }
    }

    public void _addPropertyELResolver(ELResolver elResolver) {
        if (elResolver == null) {
            throw new NullPointerException();
        }

        // grow array, if necessary
        if (_propertyELResolverCount == _propertyELResolvers.length) {
            ELResolver[] biggerResolvers = new ELResolver[_propertyELResolverCount * 2];
            System.arraycopy(_propertyELResolvers, 0, biggerResolvers, 0, _propertyELResolverCount);
            _propertyELResolvers = biggerResolvers;
        }

        // assign new resolver to end
        _propertyELResolvers[_propertyELResolverCount] = elResolver;
        _propertyELResolverCount++;
    }

    @Override
    public void addRootELResolver(ELResolver elResolver) {
        // pass ELResolver to CompositeELResolver so that J2EE6 invoke() method works. Once we can
        // have a compile dependency on J2EE6, we can override invoke() ourselves and remove this.
        super.add(elResolver);

        _addRootELResolver(elResolver);
        _addAllELResolver(elResolver);
        _addConvertELResolver(elResolver);
    }

    @Override
    public void addPropertyELResolver(ELResolver elResolver) {
        // pass ELResolver to CompositeELResolver so that J2EE6 invoke() method works. Once we can
        // have a compile dependency on J2EE6, we can override invoke() ourselves and remove this.
        super.add(elResolver);

        _addPropertyELResolver(elResolver);
        _addAllELResolver(elResolver);
        _addConvertELResolver(elResolver);
    }

    @Override
    public void add(ELResolver elResolver) {
        // pass ELResolver to CompositeELResolver so that J2EE6 invoke() method works. Once we can
        // have a compile dependency on J2EE6, we can override invoke() ourselves and remove this.
        super.add(elResolver);

        _addRootELResolver(elResolver);
        _addPropertyELResolver(elResolver);
        _addAllELResolver(elResolver);
        _addConvertELResolver(elResolver);
    }

    private Object _getValue(int resolverCount, ELResolver[] resolvers, ELContext context, Object base, Object property) throws ELException {
        for (int i = 0; i < resolverCount; i++) {
            Object result = resolvers[i].getValue(context, base, property);

            if (context.isPropertyResolved()) {
                return result;
            }
        }

        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws ELException {
        context.setPropertyResolved(false);

        int resolverCount;
        ELResolver[] resolvers;

        if (base == null) {
            if (SHORTCUTS && property instanceof String) {
                return _getRootValue(context, (String) property);
            }
            resolverCount = _rootELResolverCount;
            resolvers = _rootELResolvers;
        } else if (base instanceof ELClass) {
            resolverCount = _rootELResolverCount;
            resolvers = _rootELResolvers;
        } else {
            if (SHORTCUTS && property != null) {
                return _getPropertyValue(context, base, property);
            }
            resolverCount = _propertyELResolverCount;
            resolvers = _propertyELResolvers;
        }

        return _getValue(resolverCount, resolvers, context, base, property);
    }

    private Object _getRootValue(ELContext context, String name) {
        ELResolver[] resolvers = _rootELResolvers;
        int count = _rootELResolverCount;
        Integer known = _rootStart.get(name);
        int start = known == null ? 0 : known;

        for (int i = start; i < count; i++) {
            Object result = resolvers[i].getValue(context, null, name);

            if (context.isPropertyResolved()) {
                if (known == null) {
                    rememberRootStart(name, resolvers, i);
                }
                return result;
            }
        }

        if (known == null) {
            rememberRootStart(name, resolvers, count);
        }
        return null;
    }

    private void rememberRootStart(String name, ELResolver[] resolvers, int resolvedAt) {
        if (resolvers != _rootELResolvers || _rootStart.size() >= MAX_ROOT_NAMES) {
            return;
        }
        int start = 0;
        while (start < resolvedAt && neverResolvesRootName(resolvers[start], name)) {
            start++;
        }
        // Stored even when 0, so the name is not examined again.
        _rootStart.put(name, start);
    }

    private Object _getPropertyValue(ELContext context, Object base, Object property) {
        ClassValue<Integer> starts = _propertyStart;
        int start = starts.get(base.getClass());
        ELResolver[] resolvers = _propertyELResolvers;
        int count = _propertyELResolverCount;

        for (int i = start; i < count; i++) {
            Object result = resolvers[i].getValue(context, base, property);

            if (context.isPropertyResolved()) {
                return result;
            }
        }

        return null;
    }

    private Integer computePropertyStart(Class<?> baseClass) {
        ELResolver[] resolvers = _propertyELResolvers;
        int count = _propertyELResolverCount;
        int start = 0;
        while (start < count && neverResolvesProperty(resolvers[start], baseClass)) {
            start++;
        }
        return start;
    }

    /**
     * Forgets every learned shortcut. Called whenever the chain changes, including when an application resolver is
     * added to a composite this chain contains.
     */
    public void clearShortcuts() {
        _rootStart.clear();
        // A fresh ClassValue drops every per-class start computed so far.
        _propertyStart = newPropertyStarts();
        _beanReaders = newBeanReaders();
    }

    /*
     * Bean property readers for PathValueExpression: when the first resolver that may resolve a property of a class is
     * the BeanELResolver, the result is always the BeanELResolver's (it resolves every property of every base, or
     * throws), so the expression can call the getter directly.
     */
    private volatile ClassValue<BeanPropertyReaders> _beanReaders = newBeanReaders();

    private ClassValue<BeanPropertyReaders> newBeanReaders() {
        return new ClassValue<>() {
            @Override
            protected BeanPropertyReaders computeValue(Class<?> baseClass) {
                int start = computePropertyStart(baseClass);
                ELResolver resolver = start < _propertyELResolverCount ? _propertyELResolvers[start] : null;
                return new BeanPropertyReaders(baseClass, SHORTCUTS && resolver != null && resolver.getClass() == BeanELResolver.class ? resolver : null);
            }
        };
    }

    /**
     * The current bean property readers per base class. A new instance is returned after {@link #clearShortcuts()}, so
     * callers caching a reader can check that their cache is still current by identity.
     *
     * @return the bean property readers per base class
     */
    ClassValue<BeanPropertyReaders> beanReaders() {
        return _beanReaders;
    }

    private ClassValue<Integer> newPropertyStarts() {
        return new ClassValue<>() {
            @Override
            protected Integer computeValue(Class<?> baseClass) {
                return computePropertyStart(baseClass);
            }
        };
    }

    /**
     * Whether <code>resolver</code> can never resolve <code>getValue(context, null, name)</code>, whatever the request,
     * because it only resolves names fixed at deployment that do not include this one, or never resolves root names.
     * Only resolvers known to behave this way qualify.
     */
    private static boolean neverResolvesRootName(ELResolver resolver, String name) {
        if (isCdiResolver(resolver)) {
            // Bean names and their namespaces are fixed once the container has started, and the name was just declined.
            return true;
        }
        if (resolver instanceof FacesResourceBundleELResolver) {
            // resource-bundle vars are fixed once the application is configured, and the name was just declined.
            return resolver.getClass() == FacesResourceBundleELResolver.class;
        }
        Class<?> type = resolver.getClass();
        return type == FlashELResolver.class // root names are never resolved: flash is a property base only
                || type == CompositeComponentELResolver.class && !CompositeComponentELResolver.COMPOSITE_COMPONENT_NAME.equals(name)
                || type == StaticFieldELResolver.class // only resolves an ELClass base
                || type == EmptyStringToNullELResolver.class
                || isStreamResolver(type)
                || isApplicationComposite(resolver, child -> neverResolvesRootName(child, name));
    }

    /**
     * Whether <code>resolver</code> can never resolve <code>getValue(context, base, property)</code> for any instance of
     * <code>baseClass</code> and any property. Only resolvers known to decide from the base type alone qualify.
     */
    private static boolean neverResolvesProperty(ELResolver resolver, Class<?> baseClass) {
        if (isCdiResolver(resolver)) {
            // CDI resolvers only resolve properties of their own namespace objects.
            return !isCdiClass(baseClass);
        }
        Class<?> type = resolver.getClass();
        if (type == FlashELResolver.class) {
            return !Flash.class.isAssignableFrom(baseClass);
        }
        if (type == CompositeComponentELResolver.class || type == EmptyStringToNullELResolver.class || isStreamResolver(type)
                || isApplicationComposite(resolver, child -> neverResolvesProperty(child, baseClass))) {
            return true;
        }
        if (type == CompositeComponentAttributesELResolver.class) {
            return !UIComponent.class.isAssignableFrom(baseClass);
        }
        if (type == ResourceELResolver.class) {
            return !ResourceHandler.class.isAssignableFrom(baseClass);
        }
        if (type == ResourceBundleELResolver.class) {
            return !ResourceBundle.class.isAssignableFrom(baseClass);
        }
        if (type == MapELResolver.class) {
            return !Map.class.isAssignableFrom(baseClass);
        }
        if (type == ListELResolver.class) {
            return !List.class.isAssignableFrom(baseClass);
        }
        if (type == ArrayELResolver.class) {
            return !baseClass.isArray();
        }
        if (type == OptionalELResolver.class) {
            return !Optional.class.isAssignableFrom(baseClass);
        }
        if (type == RecordELResolver.class) {
            return !baseClass.isRecord();
        }
        return false;
    }

    private static boolean isCdiResolver(ELResolver resolver) {
        return isCdiClass(resolver.getClass());
    }

    private static boolean isCdiClass(Class<?> type) {
        String name = type.getName();
        return name.startsWith("org.jboss.weld.") || name.startsWith("org.apache.webbeans.");
    }

    private static boolean isStreamResolver(Class<?> type) {
        // getValue() of the stream resolvers of Tomcat EL and Expressly always returns null without resolving.
        String name = type.getName();
        return name.equals("org.apache.el.stream.StreamELResolverImpl") || name.equals("org.glassfish.expressly.stream.StreamELResolver");
    }

    /**
     * Whether the resolver is the composite of the application resolvers and all of them satisfy the given rule (as
     * when it is empty).
     */
    private static boolean isApplicationComposite(ELResolver resolver, Predicate<ELResolver> never) {
        if (resolver.getClass() != ApplicationELResolvers.class) {
            return false;
        }
        for (ELResolver child : ((ApplicationELResolvers) resolver).getResolvers()) {
            if (!never.test(child)) {
                return false;
            }
        }
        return true;
    }

    private Class<?> _getType(int resolverCount, ELResolver[] resolvers, ELContext context, Object base, Object property) throws ELException {
        for (int i = 0; i < resolverCount; i++) {
            Class<?> type = resolvers[i].getType(context, base, property);

            if (context.isPropertyResolved()) {
                return type;
            }
        }

        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {
        context.setPropertyResolved(false);

        int resolverCount;
        ELResolver[] resolvers;

        if (base == null || base instanceof ELClass) {
            resolverCount = _rootELResolverCount;
            resolvers = _rootELResolvers;
        } else {
            resolverCount = _propertyELResolverCount;
            resolvers = _propertyELResolvers;
        }

        return _getType(resolverCount, resolvers, context, base, property);
    }

    private void _setValue(int resolverCount, ELResolver[] resolvers, ELContext context, Object base, Object property, Object val) throws ELException {
        for (int i = 0; i < resolverCount; i++) {
            resolvers[i].setValue(context, base, property, val);

            if (context.isPropertyResolved()) {
                return;
            }
        }
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) throws ELException {
        context.setPropertyResolved(false);

        int resolverCount;
        ELResolver[] resolvers;

        if (base == null || base instanceof ELClass) {
            resolverCount = _rootELResolverCount;
            resolvers = _rootELResolvers;
        } else {
            resolverCount = _propertyELResolverCount;
            resolvers = _propertyELResolvers;
        }

        _setValue(resolverCount, resolvers, context, base, property, val);
    }

    private boolean _isReadOnly(int resolverCount, ELResolver[] resolvers, ELContext context, Object base, Object property) throws ELException {
        for (int i = 0; i < resolverCount; i++) {
            boolean isReadOnly = resolvers[i].isReadOnly(context, base, property);

            if (context.isPropertyResolved()) {
                return isReadOnly;
            }
        }

        return false;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws ELException {
        context.setPropertyResolved(false);

        int resolverCount;
        ELResolver[] resolvers;

        if (base == null || base instanceof ELClass) {
            resolverCount = _rootELResolverCount;
            resolvers = _rootELResolvers;
        } else {
            resolverCount = _propertyELResolverCount;
            resolvers = _propertyELResolvers;
        }

        return _isReadOnly(resolverCount, resolvers, context, base, property);
    }

    @Override
    public <T> T convertToType(ELContext context, Object obj, Class<T> targetType) {
        context.setPropertyResolved(false);

        if (neverConverts(obj)) {
            return null;
        }

        for (int i = 0; i < _convertELResolverCount; i++) {
            T value = _convertELResolvers[i].convertToType(context, obj, targetType);

            if (context.isPropertyResolved()) {
                return value;
            }
        }

        return null;
    }

    /**
     * Whether {@link #convertToType} certainly declines to convert <code>obj</code>: all resolvers that can convert are
     * the {@link OptionalELResolver}, which only converts an {@link Optional}, and application resolver composites holding
     * no resolver that declares <code>convertToType</code>.
     *
     * @param obj the object to convert
     * @return <code>true</code> when no resolver of this chain would convert <code>obj</code>
     */
    boolean neverConverts(Object obj) {
        if (!SHORTCUTS || _unknownConvertResolverCount != 0 || obj instanceof Optional) {
            return false;
        }

        for (ApplicationELResolvers resolvers : _applicationConvertResolvers) {
            if (resolvers.mayConvert()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }
}
