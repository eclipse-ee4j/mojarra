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

package com.sun.faces.annotation;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.render.ClientBehaviorRenderer;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;
import javax.servlet.http.HttpServletRequest;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.mgbean.BeanBuilder;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.mgbean.ManagedBeanInfo;
import javax.faces.FacesException;


public class AnnotationTestBean {

    public String getTestResult() {

        try {
            testAnnotatedComponentsWebInfClasses();
            testAnnotatedComponentsWebInfLib();
            return Boolean.TRUE.toString();
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE,
                                            "AnnotationTestBean validation failure!",
                                            e);
            return Boolean.FALSE.toString();
        }
    }

    private void testAnnotatedComponentsWebInfClasses() throws Exception {
        
        String injectedString;

        FacesContext ctx = FacesContext.getCurrentInstance();
        Application app = ctx.getApplication();

        UIComponent c = app.createComponent("AnnotatedComponent");
        assertNotNull(c);
        assertTrue(c instanceof AnnotatedComponent);

        Converter cv = app.createConverter("AnnotatedConverter");
        assertNotNull(cv);
        assertTrue(cv instanceof AnnotatedConverter);

        cv = app.createConverter(java.lang.CharSequence.class);
        assertNotNull(cv);
        assertTrue(cv instanceof AnnotatedConverterForClass);

        Validator v = app.createValidator("AnnotatedValidator");
        assertNotNull(v);
        assertTrue(v instanceof AnnotatedValidator);
        Set<String> defaultValidatorIds = app.getDefaultValidatorInfo().keySet();
        assertFalse(defaultValidatorIds.contains("AnnotatedValidator"));

        /*****  JAVASERVERFACES-3266
        v = app.createValidator("annotatedValidatorNoValue");
        assertNotNull(v);
        assertTrue(v instanceof AnnotatedValidatorNoValue);
        defaultValidatorIds = app.getDefaultValidatorInfo().keySet();
        assertFalse(defaultValidatorIds.contains("AnnotatedValidatorNoValue"));
        String welcomeMessage = ((AnnotatedValidatorNoValue)v).getWelcomeMessage();
        assertTrue(welcomeMessage.equals("AnnotatedValidatorNoValue"));
        
        boolean exceptionThrown = false;
        v = null;
        try {
            v = app.createValidator("AnnotatedValidatorNoValue");
        }
        catch (FacesException fe) {
            assertTrue(null == v);
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
         ***/
        

        // AnnotatedValidatorDefault has isDefault set to true.  Make sure
        // it's present in the default validator info obtained above.
        assertTrue(defaultValidatorIds.contains("AnnotatedValidatorDefault"));
        
        Behavior b = app.createBehavior("AnnotatedBehavior");
        assertNotNull(b);
        assertTrue(b instanceof AnnotatedBehavior);

        RenderKitFactory rkf = (RenderKitFactory) FactoryFinder
              .getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
              rkf.getRenderKit(ctx, RenderKitFactory.HTML_BASIC_RENDER_KIT);
        assertNotNull(rk);

        Renderer r = rk.getRenderer("AnnotatedRenderer", "AnnotatedRenderer");
        assertNotNull(r);
        assertTrue(r instanceof AnnotatedRenderer);

        ClientBehaviorRenderer br = rk.getClientBehaviorRenderer("AnnotatedBehaviorRenderer");
        assertNotNull(br);
        assertTrue(br instanceof AnnotatedBehaviorRenderer);
        // validate class annotated with @ManagedBean
        ApplicationAssociate associate =
              ApplicationAssociate.getInstance(ctx.getExternalContext());
        BeanManager manager = associate.getBeanManager();
        BeanBuilder bean1 = manager.getBuilder("annotatedBean");
        assertNotNull(bean1);
        ManagedBeanInfo bean1Info = bean1.getManagedBeanInfo();
        assertEquals("annotatedBean", bean1Info.getName());
        assertEquals("request", bean1Info.getScope());
        assertFalse(bean1Info.isEager());
        List<ManagedBeanInfo.ManagedProperty> managedProperties =
              bean1Info.getManagedProperties();
        assertNotNull(managedProperties);
        assertTrue(managedProperties.size() == 2);
        ManagedBeanInfo.ManagedProperty p1 = managedProperties.get(0);
        assertEquals("silly", p1.getPropertyName());
        assertEquals(String.class.getName(), p1.getPropertyClass());
        assertEquals("#{requestScope.name}", p1.getPropertyValue());
        ManagedBeanInfo.ManagedProperty p2 = managedProperties.get(1);
        assertEquals("age", p2.getPropertyName());
        assertEquals(Integer.TYPE.getName(), p2.getPropertyClass());
        assertEquals("#{requestScope.age}", p2.getPropertyValue());
        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
        request.setAttribute("name", "Bill");
        request.setAttribute("age", 33);
        AnnotatedBean bean1Instance =
              (AnnotatedBean) manager.create("annotatedBean", ctx);
        assertEquals("Bill", bean1Instance.getSilly());
        assertEquals(33, bean1Instance.getAge());
        assertNotNull(request.getAttribute("annotatedBean"));
        request.removeAttribute("annotatedBean");

        // custom scope
        ExpressionFactory factory = ctx.getApplication().getExpressionFactory();
        ValueExpression ve = factory.createValueExpression(ctx.getELContext(),
                                                           "#{customScopeAnnotatedBean.greeting}",
                                                           String.class);
        String greeting = (String) ve.getValue(ctx.getELContext());
        assertEquals("Hello", greeting);
        assertTrue(ctx.getExternalContext().getRequestMap().get("customScopeAnnotatedBean") instanceof CustomScopeAnnotatedBean);

        // validate inheritance
        ve = factory.createValueExpression(ctx.getELContext(),
                                           "#{baseBean}",
                                           Object.class);
        BaseBeanImplementation impl = (BaseBeanImplementation) ve.getValue(ctx.getELContext());
        assertEquals(20, impl.getAge());
        assertEquals("Bill", impl.getName());

    }

    private void testAnnotatedComponentsWebInfLib() throws Exception {

        FacesContext ctx = FacesContext.getCurrentInstance();
        Application app = ctx.getApplication();

        UIComponent c = app.createComponent("AnnotatedComponent2");
        assertNotNull(c);
        assertTrue(c.getClass().getName().endsWith("AnnotatedComponent2"));

        Converter cv = app.createConverter("AnnotatedConverter2");
        assertNotNull(cv);
        assertTrue(cv.getClass().getName().endsWith("AnnotatedConverter2"));


        Validator v = app.createValidator("AnnotatedValidator2");
        assertNotNull(v);
        assertTrue(v.getClass().getName().endsWith("AnnotatedValidator2"));
        Set<String> defaultValidatorIds = app.getDefaultValidatorInfo().keySet();
        assertFalse(defaultValidatorIds.contains("AnnotatedValidator2"));

        RenderKitFactory rkf = (RenderKitFactory) FactoryFinder
              .getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
              rkf.getRenderKit(ctx, RenderKitFactory.HTML_BASIC_RENDER_KIT);
        assertNotNull(rk);

        Renderer r = rk.getRenderer("AnnotatedRenderer2", "AnnotatedRenderer2");
        assertNotNull(r);
        assertTrue(r.getClass().getName().endsWith("AnnotatedRenderer2"));

        // Test default naming logic
        assertNotNull(ApplicationAssociate.getInstance(ctx.getExternalContext())
              .getNamedEventManager().getNamedEvent("com.sun.faces.annotation.annotatedComponentSystem"));
        // Test short name
        assertNotNull(ApplicationAssociate.getInstance(ctx.getExternalContext())
              .getNamedEventManager().getNamedEvent("com.sun.faces.annotation.anotherAnnotatedComponentSystem"));
        // Test FQCN
        assertNotNull(ApplicationAssociate.getInstance(ctx.getExternalContext())
              .getNamedEventManager().getNamedEvent(AnnotatedComponentSystemEvent.class.getName()));
        assertNotNull(ApplicationAssociate.getInstance(ctx.getExternalContext())
              .getNamedEventManager().getNamedEvent("explicitEventName"));

        Object bean = ctx.getApplication().evaluateExpressionGet(ctx,
                                                                 "#{annotatedBean4}",
                                                                 Object.class);
        assertNotNull(bean);
        assertEquals("com.sun.faces.annotation.AnnotatedBean4", bean.getClass().getName());

        // negative tests - if the jar files are metadata-complete, then their
        // annotated classes shouldn't be scanned/registered

        // faces-config is versioned at 2.0 and is marked metadata-complete
        bean = ctx.getApplication().evaluateExpressionGet(ctx,
                                                          "#{notFoundBean1}",
                                                          Object.class);
        assertTrue(bean == null);

        // faces-config is versioned at 1.2 which assumes metadata-complete
        bean = ctx.getApplication().evaluateExpressionGet(ctx,
                                                          "#{notFoundBean2}",
                                                          Object.class);
        assertTrue(bean == null);

    }

    private void assertNotNull(Object v) {
        if (v == null) {
            throw new RuntimeException();
        }
    }

    private void assertTrue(boolean t) {
        if (!t) {
            throw new RuntimeException();
        }
    }

    private void assertEquals(Object o1, Object o2) {
        if (o1 == null && o2 != null) {
            throw new RuntimeException();
        }
        if (o2 == null && o1 != null) {
            throw new RuntimeException();
        }
        if (o1 == null) {
            return;
        }
        if (!o1.equals(o2)) {
            throw new RuntimeException();
        }

    }

    private void assertFalse(boolean t) {
        if (t) {
            throw new RuntimeException();
        }
    }
}
