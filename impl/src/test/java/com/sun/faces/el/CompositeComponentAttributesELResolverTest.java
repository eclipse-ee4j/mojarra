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

import com.sun.faces.facelets.tag.composite.CompositeComponentBeanInfo;

import jakarta.faces.application.Resource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.context.FacesContext;

import java.beans.BeanDescriptor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import jakarta.el.ELContext;

import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 * The JUnit tests for the CompositeComponentAttributesELResolver class.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class CompositeComponentAttributesELResolverTest {

    /**
     * Test issue #2508.
     */
    @Test
    public void testGetValue() throws Exception {
        ELContext elContext1 = EasyMock.createNiceMock(ELContext.class);
        FacesContext facesContext1 = EasyMock.createNiceMock(FacesContext.class);
        ELContext elContext2 = EasyMock.createNiceMock(ELContext.class);
        FacesContext facesContext2 = EasyMock.createNiceMock(FacesContext.class);
        
        HashMap<Object, Object> ctxAttributes1 = new HashMap<Object, Object>();
        UIPanel composite = new UIPanel();
        CompositeComponentBeanInfo compositeBeanInfo = new CompositeComponentBeanInfo();
        BeanDescriptor beanDescriptor = new BeanDescriptor(composite.getClass());
        compositeBeanInfo.setBeanDescriptor(beanDescriptor);
        composite.getAttributes().put(Resource.COMPONENT_RESOURCE_KEY, "dummy");
        composite.getAttributes().put(UIComponent.BEANINFO_KEY, compositeBeanInfo);
        String property = "attrs";

        expect(elContext1.getContext(FacesContext.class)).andReturn(facesContext1);
        expect(facesContext1.getAttributes()).andReturn(ctxAttributes1);
        expect(elContext2.getContext(FacesContext.class)).andReturn(facesContext2);
        expect(facesContext2.getAttributes()).andReturn(ctxAttributes1);
        replay(elContext1, facesContext1, elContext2, facesContext2);
        
        CompositeComponentAttributesELResolver elResolver = new CompositeComponentAttributesELResolver();
        Map<String, Object> evalMap1 = (Map<String, Object>) elResolver.getValue(elContext1, composite, property);
        assertNotNull(evalMap1);
        Map<String, Object> evalMap2 = (Map<String, Object>) elResolver.getValue(elContext2, composite, property);
        assertNotNull(evalMap2);
        
        Field ctxField1 = evalMap1.getClass().getDeclaredField("ctx");
        ctxField1.setAccessible(true);       
        Field ctxField2 = evalMap2.getClass().getDeclaredField("ctx");
        ctxField2.setAccessible(true);

        assertTrue(evalMap1 == evalMap2);
        assertTrue(facesContext1 != ctxField1.get(evalMap1));
        assertTrue(facesContext2 == ctxField1.get(evalMap1));
        assertTrue(facesContext1 != ctxField2.get(evalMap2));
        assertTrue(facesContext2 == ctxField2.get(evalMap2));
        
        verify(elContext1, facesContext1, elContext2, facesContext2);
    }
}
