/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
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

import static com.sun.faces.util.MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.Util.getCdiBeanManager;
import static com.sun.faces.util.Util.isEmpty;
import static java.lang.Boolean.FALSE;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ResolversRegistry;
import com.sun.faces.util.Cache;
import com.sun.faces.util.LRUCache;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for EL related methods.
 */
public class ELUtils {

    /**
     * The maximum size of the <code>compositeComponentEvaluationCache</code>.
     */
    private static final int compositeComponentEvaluationCacheMaxSize = 1000;

    /**
     * Helps to determine if a EL expression represents a composite component EL expression.
     */
    private static final Pattern COMPOSITE_COMPONENT_EXPRESSION = Pattern.compile(".(?:[ ]+|[\\[{,(])cc[.].+[}]");

    // do not use this Matcher, it's only for the Cache Factory
    private static final Matcher COMPOSITE_COMPONENT_EXPRESSION_MATCHER = COMPOSITE_COMPONENT_EXPRESSION.matcher("");

    /**
     * Cache.Factory that initialize an element inside the LRUCache evaluating a Matcher against the input.
     * We should be able to share a Matcher because the Factory it's executed atomically
     * and this Matcher is used only here
     */
    private static final Cache.Factory<String,Boolean> isCompositeExpressionInit = new Cache.Factory<>() {

        // it would be safer to declare the shared Matcher here, but it requires Java 16+ ... Faces 5.0 ?
        // private static final Matcher COMPOSITE_COMPONENT_EXPRESSION_MATCHER = COMPOSITE_COMPONENT_EXPRESSION.matcher("");

        @Override
        public Boolean newInstance(String expression) {
            return expression == null ? FALSE : COMPOSITE_COMPONENT_EXPRESSION_MATCHER.reset(expression).find();
        }
    };

    /**
     * Private cache for storing evaluation results for composite components checks.
     */
    private static final LRUCache<String, Boolean> compositeComponentEvaluationCache = new LRUCache<>(isCompositeExpressionInit, compositeComponentEvaluationCacheMaxSize);

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

    // ------------------------------------------------------------ Constructors

    private ELUtils() {
        throw new IllegalStateException();
    }

    // ---------------------------------------------------------- Public Methods

    public static boolean isCompositeComponentExpr(String expression) {
        return compositeComponentEvaluationCache.get(expression);
    }

    public static boolean isCompositeComponentMethodExprLookup(String expression) {
        return METHOD_EXPRESSION_LOOKUP.matcher(expression).matches();
    }

    public static boolean isCompositeComponentLookupWithArgs(String expression) {
        // TODO we should be trying to re-use the Matcher by calling
        //      pizzi80: not sure because it will require a synchronized block if this method
        //               is called by multiple threads
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

        ResolversRegistry elRegistry = associate.getGlobalResolversRegistry();
        composite.add(elRegistry.FLASH_RESOLVER);
        composite.addPropertyELResolver(elRegistry.COMPOSITE_COMPONENT_ATTRIBUTES_EL_RESOLVER);
        addELResolvers(composite, associate.getELResolversFromFacesConfig());
        composite.add(associate.getApplicationELResolvers());

        if (ContextParam.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL.isSet(FacesContext.getCurrentInstance())) {
            composite.addPropertyELResolver(elRegistry.EMPTY_STRING_TO_NULL_RESOLVER);
        }

        composite.addPropertyELResolver(elRegistry.RESOURCE_RESOLVER);
        composite.addPropertyELResolver(elRegistry.BUNDLE_RESOLVER);

        composite.addRootELResolver(elRegistry.FACES_BUNDLE_RESOLVER);

        // Not sure when/why this would ever be null, but Thomas Hoffman believes it can be null.
        ELResolver streamELResolver = associate.getExpressionFactory().getStreamELResolver();
        if (streamELResolver != null) {
            composite.addRootELResolver(streamELResolver);
        }

        composite.addRootELResolver(elRegistry.STATIC_FIELD_RESOLVER);

        composite.addPropertyELResolver(elRegistry.MAP_RESOLVER);
        composite.addPropertyELResolver(elRegistry.LIST_RESOLVER);
        composite.addPropertyELResolver(elRegistry.ARRAY_RESOLVER);
        composite.addPropertyELResolver(elRegistry.OPTIONAL_RESOLVER);
        composite.addPropertyELResolver(elRegistry.RECORD_RESOLVER);
        composite.addPropertyELResolver(elRegistry.BEAN_RESOLVER);

        composite.addRootELResolver(elRegistry.SCOPED_RESOLVER);
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
     * <p>
     * Add the <code>ELResolvers</code> from the provided list to the target <code>CompositeELResolver</code>.
     * </p>
     *
     * @param target the <code>CompositeELResolver</code> to which the <code>ELResolver</code>s will be added.
     * @param resolvers a <code>List</code> of <code>ELResolver</code>s
     */
    private static void addELResolvers(CompositeELResolver target, List<ELResolver> resolvers) {
        if (!isEmpty(resolvers)) {
            for (ELResolver resolver : resolvers) {
                target.add(resolver);
            }
        }
    }

    private static void addCDIELResolver(FacesCompositeELResolver composite) {
        composite.add(getCdiBeanManager(FacesContext.getCurrentInstance()).getELResolver());
    }

    private static void checkNotNull(FacesCompositeELResolver composite, ApplicationAssociate associate) {
        if (associate == null) {
            throw new NullPointerException(getExceptionMessageString(NULL_PARAMETERS_ERROR_MESSAGE_ID, "associate"));
        }

        if (composite == null) {
            throw new NullPointerException(getExceptionMessageString(NULL_PARAMETERS_ERROR_MESSAGE_ID, "composite"));
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

        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext == null) {
            return null;
        }

        return getDefaultExpressionFactory(ApplicationAssociate.getInstance(externalContext), facesContext);
    }

    public static ExpressionFactory getDefaultExpressionFactory(ApplicationAssociate associate, FacesContext facesContext) {
        if (associate == null) {
            return null;
        }

        return associate.getExpressionFactory();
    }

}
