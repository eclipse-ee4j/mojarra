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

import java.beans.FeatureDescriptor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationImpl;
import com.sun.faces.mock.MockELContext;
import com.sun.faces.mock.MockExternalContext;
import com.sun.faces.mock.MockFacesContext;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockHttpSession;
import com.sun.faces.mock.MockServletContext;

/**
 * Tests needs to be run with assertions enabled (-ea:com.sun.faces.el.ChainAwareVariableResolver)
 */
public class TestNestedELResolver extends TestCase {

    private static final String UNPREFIXED_VALUE = "unprefixedValue";

    private static final String UNPREFIXED_KEY = "unprefixedKey";

    private static final String PREFIXED_VALUE = "prefixedValue";

    private static final String PREFIX = "test:";

    private static final String PREFIXED_KEY = PREFIX + "value";

    private ELContext elContext;

    public void setUp() throws Exception {
//        FacesContext facesContext = createStubbedFacesContext();
//        this.elContext = facesContext.getELContext();
//        facesContext.getExternalContext().getApplicationMap()
//              .put(PREFIXED_KEY, PREFIXED_VALUE);
//        facesContext.getExternalContext().getApplicationMap()
//              .put(UNPREFIXED_KEY, UNPREFIXED_VALUE);
    }

    public void testShouldResolveVariableWhenNestedELResolverCallCanNotResolve()
          throws Exception {
//        assertEquals(UNPREFIXED_VALUE,
//                     this.elContext.getELResolver().getValue(this.elContext,
//                                                             null,
//                                                             UNPREFIXED_KEY));
    }

    public void testShouldResolveVariableViaNestedELResolverCall()
          throws Exception {
//        assertEquals(PREFIXED_VALUE,
//                     this.elContext.getELResolver().getValue(this.elContext,
//                                                             null,
//                                                             PREFIXED_KEY));
    }

    ///wish I could use FacesTester ;-)

    private FacesContext createStubbedFacesContext() throws Exception {
        ServletContext context = new MockServletContext();
        ((MockServletContext) context).addInitParameter("jakarta.faces.DISABLE_FACELET_JSF_VIEWHANDLER", "true");
        HttpSession session = new MockHttpSession(context);
        ServletRequest request = new MockHttpServletRequest(session);
        ServletResponse response = new MockHttpServletResponse();
        ExternalContext externalContect = new MockExternalContext(context,
                                                                  request,
                                                                  response);
        MockFacesContext mockFacesContext = new MockFacesContext(externalContect);
        mockFacesContext.setApplication(new ApplicationImpl());
        ApplicationAssociate associate = getApplicationAssociate(
              mockFacesContext);
        associate
              .setELResolversFromFacesConfig(Collections.<ELResolver>singletonList(
                    new NestedELResolver(PREFIX)));
        associate.setLegacyVariableResolver(new ChainAwareVariableResolver());
        FacesCompositeELResolver facesCompositeELResolver = 
      new DemuxCompositeELResolver(
           FacesCompositeELResolver.ELResolverChainType.Faces);
        ELUtils.buildFacesResolver(facesCompositeELResolver, associate);
        ELContext elContext = mockFacesContext.getELContext();
        setELResolverOnElContext(facesCompositeELResolver, elContext);
        return mockFacesContext;
    }


    private ApplicationAssociate getApplicationAssociate(MockFacesContext mockFacesContext) {
        return (ApplicationAssociate) mockFacesContext.getExternalContext()
              .getApplicationMap()
              .get(RIConstants.FACES_PREFIX + "ApplicationAssociate");
    }

    //there should be a better way for doing this when using mocks/stubs
    private void setELResolverOnElContext(FacesCompositeELResolver facesCompositeELResolver, ELContext elContext)
          throws Exception {
        Field field = elContext.getClass().getDeclaredField("resolver");
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(elContext, facesCompositeELResolver);
        } finally {
            field.setAccessible(accessible);
        }
    }

    /*
      * Uses ElContext.getElResolver().getValue() inside its own getValue()
      */
    public static class NestedELResolver extends ELResolver {

        private final String prefix;

        public NestedELResolver(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Class<?> getCommonPropertyType(ELContext arg0, Object arg1) {
            return null;
        }

        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
            return null;
        }

        @Override
        public Class<?> getType(ELContext arg0, Object arg1, Object arg2) {
            return null;
        }

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            if (context.getContext(NestedELResolver.class) != Boolean.TRUE) {
                context.putContext(NestedELResolver.class, Boolean.TRUE);
                try {
                    Object value = context.getELResolver()
                          .getValue(context, base, this.prefix + property);
                    context.setPropertyResolved(value != null);
                    return value;
                } finally {
                    context.putContext(NestedELResolver.class, Boolean.FALSE);
                }
            }
            return null;
        }

        @Override
        public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) {
            return false;
        }

        @Override
        public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) {

        }

    }

}
