/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.mock;

import static org.glassfish.mojarra.RIConstants.FACES_SERVLET_MAPPINGS;

import java.util.Arrays;
import java.util.LinkedHashSet;

import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.MappingMatch;

public final class MockFacesMappingSupport {

    private MockFacesMappingSupport() {
    }

    public static MockHttpServletRequest request(String contextPath, HttpServletMapping mapping, HttpSession session) {
        return new MappingHttpServletRequest(contextPath, mapping, session);
    }

    public static MockExternalContext externalContext(MockServletContext servletContext, MockHttpServletRequest request, MockHttpServletResponse response) {
        return new MappingExternalContext(servletContext, request, response);
    }

    public static void setFacesServletMappings(MockServletContext servletContext, String... mappings) {
        servletContext.setAttribute(FACES_SERVLET_MAPPINGS, new LinkedHashSet<>(Arrays.asList(mappings)));
    }

    public static HttpServletMapping mapping(MappingMatch match, String pattern, String matchValue) {
        return new HttpServletMapping() {

            @Override
            public String getMatchValue() {
                return matchValue;
            }

            @Override
            public String getPattern() {
                return pattern;
            }

            @Override
            public String getServletName() {
                return "FacesServlet";
            }

            @Override
            public MappingMatch getMappingMatch() {
                return match;
            }

        };
    }

    private static final class MappingHttpServletRequest extends MockHttpServletRequest {

        private final HttpServletMapping mapping;

        private MappingHttpServletRequest(String contextPath, HttpServletMapping mapping, HttpSession session) {
            super(session);
            this.mapping = mapping;
            setPathElements(contextPath, mapping.getPattern(), null, null);
        }

        @Override
        public HttpServletMapping getHttpServletMapping() {
            return mapping;
        }

    }

    private static final class MappingExternalContext extends MockExternalContext {

        private final MockHttpServletRequest request;

        private MappingExternalContext(MockServletContext servletContext, MockHttpServletRequest request, MockHttpServletResponse response) {
            super(servletContext, request, response);
            this.request = request;
        }

        @Override
        public String getRequestContextPath() {
            return request.getContextPath();
        }

        @Override
        public String getRequestPathInfo() {
            return request.getPathInfo();
        }

        @Override
        public String getRequestServletPath() {
            return request.getServletPath();
        }

        @Override
        public String encodeActionURL(String url) {
            return url;
        }

        @Override
        public String encodeResourceURL(String url) {
            return url;
        }

    }

}
