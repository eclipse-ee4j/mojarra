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
package com.sun.faces.el;

import static com.sun.faces.RIConstants.EMPTY_CLASS_ARGS;
import static com.sun.faces.util.MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.ReflectionUtils.lookupMethod;
import static com.sun.faces.util.ReflectionUtils.newInstance;
import static com.sun.faces.util.Util.getCdiBeanManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.context.flash.FlashELResolver;

import jakarta.el.ArrayELResolver;
import jakarta.el.BeanELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.ResourceBundleELResolver;
import jakarta.el.ValueExpression;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * Utility class for EL related methods.
 */
public class ELUtils {

    /**
     * Private cache for storing evaluation results for composite components checks.
     */
    private static final HashMap<String, Boolean> compositeComponentEvaluationCache = new HashMap<String, Boolean>();

    /**
     * The maximum size of the <code>compositeComponentEvaluationCache</code>.
     */
    private static final int compositeComponentEvaluationCacheMaxSize = 1000;

    /**
     * FIFO queue, holding access information about the <code>compositeComponentEvaluationCache</code>.
     */
    private static final LinkedList<String> evaluationCacheFifoQueue = new LinkedList<String>();

    /**
     * Class member, indicating a <I>positive</I> evaluation result.
     */
    private static final Boolean IS_COMPOSITE_COMPONENT = Boolean.TRUE;

    /**
     * Class member, indicating a <I>negative</I> evaluation result.
     */
    private static final Boolean IS_NOT_A_COMPOSITE_COMPONENT = Boolean.FALSE;

    /**
     * Helps to determine if a EL expression represents a composite component EL expression.
     */
    private static final Pattern COMPOSITE_COMPONENT_EXPRESSION = Pattern.compile(".(?:[ ]+|[\\[{,(])cc[.].+[}]");

    /**
     * Used to determine if EL method arguments are being passed to a composite component lookup expression.
     *
     * For example:
     *
     * #{cc.attrs.label('foo')}
     *
     * is illegal, while:
     *
     * #{cc.attrs.bean.label('foo')}
     *
     * is legal.
     */
    private static final Pattern COMPOSITE_COMPONENT_LOOKUP_WITH_ARGS = Pattern.compile("(?:[ ]+|[\\[{,(])cc[.]attrs[.]\\w+[(].+[)]");

    /**
     * Use to determine if an expression being considered as a MethodExpression is a simple lookup (i.e.
     * #{cc.attrs.myaction}).
     */
    private static final Pattern METHOD_EXPRESSION_LOOKUP = Pattern.compile(".[{]cc[.]attrs[.]\\w+[}]");

    public static final ArrayELResolver ARRAY_RESOLVER = new ArrayELResolver();
    public static final BeanELResolver BEAN_RESOLVER = new BeanELResolver();
    public static final FacesResourceBundleELResolver FACES_BUNDLE_RESOLVER = new FacesResourceBundleELResolver();
    public static final FlashELResolver FLASH_RESOLVER = new FlashELResolver();
    public static final ListELResolver LIST_RESOLVER = new ListELResolver();
    public static final MapELResolver MAP_RESOLVER = new MapELResolver();
    public static final ResourceBundleELResolver BUNDLE_RESOLVER = new ResourceBundleELResolver();
    public static final ScopedAttributeELResolver SCOPED_RESOLVER = new ScopedAttributeELResolver();
    public static final ResourceELResolver RESOURCE_RESOLVER = new ResourceELResolver();
    public static final CompositeComponentAttributesELResolver COMPOSITE_COMPONENT_ATTRIBUTES_EL_RESOLVER = new CompositeComponentAttributesELResolver();

    // ------------------------------------------------------------ Constructors

    private ELUtils() {
        throw new IllegalStateException();
    }

    // ---------------------------------------------------------- Public Methods

    public static boolean isCompositeComponentExpr(String expression) {
        Boolean evaluationResult = compositeComponentEvaluationCache.get(expression);

        if (evaluationResult != null) {
            // fast path - this expression has already been evaluated, therefore return its evaluation result
            return evaluationResult.booleanValue();
        }

        // TODO we should be trying to re-use the Matcher by calling
        // m.reset(expression);
        boolean returnValue = COMPOSITE_COMPONENT_EXPRESSION
                .matcher(expression)
                .find();

        // remember the evaluation result for this expression
        rememberEvaluationResult(expression, returnValue);

        return returnValue;
    }

    public static boolean isCompositeComponentMethodExprLookup(String expression) {
        return METHOD_EXPRESSION_LOOKUP.matcher(expression).matches();
    }

    public static boolean isCompositeComponentLookupWithArgs(String expression) {
        // TODO we should be trying to re-use the Matcher by calling
        // m.reset(expression);
        return COMPOSITE_COMPONENT_LOOKUP_WITH_ARGS.matcher(expression).find();
    }

    /**
     * <p>
     * Create the <code>ELResolver</code> chain for programmatic EL calls.
     * </p>
     *
     * @param composite a <code>CompositeELResolver</code>
     * @param associate our ApplicationAssociate
     */
    public static void buildFacesResolver(FacesCompositeELResolver composite, ApplicationAssociate associate) {
        checkNotNull(composite, associate);
        addCDIELResolver(composite);
        composite.add(FLASH_RESOLVER);
        composite.addPropertyELResolver(COMPOSITE_COMPONENT_ATTRIBUTES_EL_RESOLVER);
        addELResolvers(composite, associate.getELResolversFromFacesConfig());
        composite.add(associate.getApplicationELResolvers());
        composite.addPropertyELResolver(RESOURCE_RESOLVER);
        composite.addPropertyELResolver(BUNDLE_RESOLVER);
        composite.addRootELResolver(FACES_BUNDLE_RESOLVER);
        addEL3_0_Resolvers(composite, associate);
        composite.addPropertyELResolver(MAP_RESOLVER);
        composite.addPropertyELResolver(LIST_RESOLVER);
        composite.addPropertyELResolver(ARRAY_RESOLVER);
        composite.addPropertyELResolver(BEAN_RESOLVER);
        composite.addRootELResolver(SCOPED_RESOLVER);
    }

    private static void checkNotNull(FacesCompositeELResolver composite, ApplicationAssociate associate) {
        if (associate == null) {
            throw new NullPointerException(getExceptionMessageString(NULL_PARAMETERS_ERROR_MESSAGE_ID, "associate"));
        }

        if (composite == null) {
            throw new NullPointerException(getExceptionMessageString(NULL_PARAMETERS_ERROR_MESSAGE_ID, "composite"));
        }
    }

    private static void addCDIELResolver(FacesCompositeELResolver composite) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        BeanManager beanManager = getCdiBeanManager(facesContext);
        composite.add(beanManager.getELResolver());
    }

    private static void addEL3_0_Resolvers(FacesCompositeELResolver composite, ApplicationAssociate associate) {
        ExpressionFactory expressionFactory = associate.getExpressionFactory();

        Method getStreamELResolverMethod = lookupMethod(ExpressionFactory.class, "getStreamELResolver", EMPTY_CLASS_ARGS);

        if (getStreamELResolverMethod != null) {
            try {
                ELResolver streamELResolver = (ELResolver) getStreamELResolverMethod.invoke(expressionFactory, (Object[]) null);
                if (streamELResolver != null) {
                    composite.addRootELResolver(streamELResolver);

                    // Assume that if we have getStreamELResolver, then we must have
                    // jakarta.el.staticFieldELResolver
                    composite.addRootELResolver((ELResolver) newInstance("jakarta.el.StaticFieldELResolver"));
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException t) {
                // This is normal on containers that do not have these ELResolvers
            }
        }
    }

    public static Object evaluateValueExpression(ValueExpression expression, ELContext elContext) {
        if (expression.isLiteralText()) {
            return expression.getExpressionString();
        }

        return expression.getValue(elContext);
    }

    /**
     * Create a <code>ValueExpression</code> with the expected type of <code>Object.class</code>
     *
     * @param expression an EL expression
     * @return a new <code>ValueExpression</code> instance based off the provided <code>valueRef</code>
     */
    public static ValueExpression createValueExpression(String expression) {
        return createValueExpression(expression, Object.class);
    }

    public static ValueExpression createValueExpression(String expression, Class<?> expectedType) {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), expression, expectedType);
    }

    public static Object coerce(Object value, Class<?> toType) {
        return FacesContext.getCurrentInstance().getApplication().getExpressionFactory().coerceToType(value, toType);

    }


    // --------------------------------------------------------- Private Methods

    /**
     * Adds the specified <code>expression</code> with its evaluation result <code>isCompositeComponent</code> to the <code>compositeComponentEvaluationCache</code>,
     * taking into account the maximum cache size.
     */
    private static void rememberEvaluationResult(String expression, boolean isCompositeComponent) {
        synchronized (compositeComponentEvaluationCache) {
            if (compositeComponentEvaluationCache.size() >= compositeComponentEvaluationCacheMaxSize) {
                // obtain the oldest cached element
                String oldestExpression = evaluationCacheFifoQueue.removeFirst();

                // remove the mapping for this element
                compositeComponentEvaluationCache.remove(oldestExpression);
            }

            // add the mapping to the cache
            if (isCompositeComponent) {
                compositeComponentEvaluationCache.put(expression, IS_COMPOSITE_COMPONENT);
            } else {
                compositeComponentEvaluationCache.put(expression, IS_NOT_A_COMPOSITE_COMPONENT);
            }

            // remember the sequence of the hash map "put" operations
            evaluationCacheFifoQueue.add(expression);
        }
    }

    /**
     * <p>
     * Add the <code>ELResolvers</code> from the provided list to the target <code>CompositeELResolver</code>.
     * </p>
     *
     * @param target the <code>CompositeELResolver</code> to which the <code>ELResolver</code>s will be added.
     * @param resolvers a <code>List</code> of <code>ELResolver</code>s
     */
    private static void addELResolvers(CompositeELResolver target, List<ELResolver> resolvers) {
        if (resolvers != null && !resolvers.isEmpty()) {
            for (ELResolver resolver : resolvers) {
                target.add(resolver);
            }
        }

    }

    /*
     * First look in the ApplicationAssociate. If that fails, return null;
     *
     */
    public static ExpressionFactory getDefaultExpressionFactory(FacesContext facesContext) {
        if (facesContext == null) {
            return null;
        }

        ExternalContext extContext = facesContext.getExternalContext();
        if (extContext == null) {
            return null;
        }

        return getDefaultExpressionFactory(ApplicationAssociate.getInstance(extContext), facesContext);
    }

    public static ExpressionFactory getDefaultExpressionFactory(ApplicationAssociate associate, FacesContext facesContext) {
        if (associate == null) {
            return null;
        }

        return associate.getExpressionFactory();
    }
}
