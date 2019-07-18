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

package com.sun.faces.el;

import static com.sun.faces.RIConstants.EMPTY_CLASS_ARGS;
import static com.sun.faces.cdi.CdiUtils.getBeanReference;
import static com.sun.faces.util.MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID;
import static com.sun.faces.util.MessageUtils.getExceptionMessageString;
import static com.sun.faces.util.ReflectionUtils.lookupMethod;
import static com.sun.faces.util.ReflectionUtils.newInstance;
import static com.sun.faces.util.Util.getCdiBeanManager;
import static com.sun.faces.util.Util.getFacesConfigXmlVersion;
import static com.sun.faces.util.Util.getWebXmlVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspFactory;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.cdi.CdiExtension;
import com.sun.faces.context.flash.FlashELResolver;

/**
 * <p>
 * Utility class for EL related methods.
 * </p>
 */
public class ELUtils {

    /**
     * Helps to determine if a EL expression represents a composite component EL expression.
     */
    private static final Pattern COMPOSITE_COMPONENT_EXPRESSION = Pattern.compile(".(?:[ ]+|[\\[{,(])cc[.].+[}]");

    /**
     * Used to determine if EL method arguments are being passed to a composite component lookup
     * expression.
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


    public enum Scope {
        NONE("none"), REQUEST("request"), VIEW("view"), SESSION("session"), APPLICATION("application");

        String scope;

        Scope(String scope) {
            this.scope = scope;
        }

        @Override
        public String toString() {
            return scope;
        }

    }

    public static final ArrayELResolver ARRAY_RESOLVER = new ArrayELResolver();
    public static final BeanELResolver BEAN_RESOLVER = new BeanELResolver();
    public static final FacesResourceBundleELResolver FACES_BUNDLE_RESOLVER = new FacesResourceBundleELResolver();
    public static final ImplicitObjectELResolver IMPLICIT_RESOLVER = new ImplicitObjectELResolver();
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
        // TODO we should be trying to re-use the Matcher by calling
        // m.reset(expression);
        return COMPOSITE_COMPONENT_EXPRESSION.matcher(expression).find();
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

        if (!tryAddCDIELResolver(composite)) {
            // The CDI ELResolver that among others takes care of handling the implicit objects
            // was not added. Add the old native implicit resolver.
            composite.addRootELResolver(IMPLICIT_RESOLVER);
        }

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

    private static boolean tryAddCDIELResolver(FacesCompositeELResolver composite) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        javax.enterprise.inject.spi.BeanManager beanManager = getCdiBeanManager(facesContext);

        if (beanManager == null) {
            // TODO: use version enum and >=
            if (getFacesConfigXmlVersion(facesContext).equals("2.3") || getWebXmlVersion(facesContext).equals("4.0")) {
                throw new FacesException("Unable to find CDI BeanManager");
            }
        } else {
            CdiExtension cdiExtension = getBeanReference(beanManager, CdiExtension.class);
            if (cdiExtension.isAddBeansForJSFImplicitObjects()) {
                composite.add(beanManager.getELResolver());
                return true;
            }
        }

        return false;
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
                    // javax.el.staticFieldELResolver
                    composite.addRootELResolver((ELResolver) newInstance("javax.el.StaticFieldELResolver"));
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException t) {
                // This is normal on containers that do not have these ELResolvers
            }
        }
    }

    public static Object evaluateValueExpression(ValueExpression expression, ELContext elContext) {
        if (expression.isLiteralText()) {
            return expression.getExpressionString();
        } else {
            return expression.getValue(elContext);
        }
    }

    public static ValueExpression createValueExpression(String expression, Class<?> expectedType) {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), expression, expectedType);
    }



    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Add the <code>ELResolvers</code> from the provided list to the target
     * <code>CompositeELResolver</code>.
     * </p>
     *
     * @param target the <code>CompositeELResolver</code> to which the <code>ELResolver</code>s will be
     * added.
     * @param resolvers a <code>List</code> of <code>ELResolver</code>s
     */
    private static void addELResolvers(CompositeELResolver target, List<ELResolver> resolvers) {
        if (resolvers != null && !resolvers.isEmpty()) {
            for (ELResolver resolver : resolvers) {
                target.add(resolver);
            }
        }
    }

    public static boolean isScopeValid(String scopeName) {
        if (scopeName == null) {
            return false;
        }

        for (Scope scope : Scope.values()) {
            if (scopeName.equals(scope.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * First look in the ApplicationAssociate. If that fails, try the Jsp engine. If that fails, return
     * null;
     */
    public static ExpressionFactory getDefaultExpressionFactory(FacesContext facesContext) {
        ExpressionFactory result;
        if (null == facesContext) {
            return null;
        }
        ExternalContext extContext = facesContext.getExternalContext();
        if (null == extContext) {
            return null;
        }

        ApplicationAssociate associate = ApplicationAssociate.getInstance(extContext);
        result = getDefaultExpressionFactory(associate, facesContext);

        return result;
    }

    public static ExpressionFactory getDefaultExpressionFactory(ApplicationAssociate associate, FacesContext facesContext) {
        ExpressionFactory result = null;

        if (null != associate) {
            result = associate.getExpressionFactory();
        }

        if (null == result) {
            if (null == facesContext) {
                return null;
            }
            ExternalContext extContext = facesContext.getExternalContext();
            if (null == extContext) {
                return null;
            }

            Object servletContext = extContext.getContext();
            if (null != servletContext) {
                if (servletContext instanceof ServletContext) {
                    ServletContext sc = (ServletContext) servletContext;
                    JspApplicationContext jspAppContext = JspFactory.getDefaultFactory().getJspApplicationContext(sc);
                    if (null != jspAppContext) {
                        result = jspAppContext.getExpressionFactory();
                    }
                }
            }
        }

        return result;
    }
}
