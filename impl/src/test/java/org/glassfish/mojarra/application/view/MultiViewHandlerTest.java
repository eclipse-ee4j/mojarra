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

package org.glassfish.mojarra.application.view;

import static jakarta.servlet.http.MappingMatch.EXACT;
import static jakarta.servlet.http.MappingMatch.EXTENSION;
import static jakarta.servlet.http.MappingMatch.PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.faces.FactoryFinder;

import org.glassfish.mojarra.junit.JUnitFacesTestCaseBase;
import org.glassfish.mojarra.mock.MockFacesMappingSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultiViewHandlerTest extends JUnitFacesTestCaseBase {

    private static final String CONTEXT_PATH = "/app";

    private MultiViewHandler viewHandler;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        FactoryFinder.setFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY, ViewDeclarationLanguageFactoryImpl.class.getName());
        viewHandler = new MultiViewHandler();
    }

    @Test
    public void getActionURLReturnsExactHitForConfiguredFaceletsExtension() {
        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact", "/target", "*.xhtml", "/faces/*");

        assertEquals("/app/target", viewHandler.getActionURL(facesContext, "/target.xhtml"));
    }

    @Test
    public void getActionURLKeepsExtensionFallbackForExactRequests() {
        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact", "*.xhtml");
        String exactRequestUrl = viewHandler.getActionURL(facesContext, "/fallback.xhtml");

        configureRequest(MockFacesMappingSupport.mapping(EXTENSION, "*.xhtml", "fallback"), "/exact", "*.xhtml");
        String extensionBaselineUrl = viewHandler.getActionURL(facesContext, "/fallback.xhtml");

        assertEquals("/app/fallback.xhtml", exactRequestUrl);
        assertEquals(extensionBaselineUrl, exactRequestUrl);
    }

    @Test
    public void getActionURLKeepsDirectPathBehavior() {
        configureRequest(MockFacesMappingSupport.mapping(PATH, "/faces/*", "fallback"), "/exact", "/faces/*");

        assertEquals("/app/faces/fallback.xhtml", viewHandler.getActionURL(facesContext, "/fallback.xhtml"));
    }

    @Test
    public void getActionURLPrefersExtensionFallbackWhenBothWildcardsExist() {
        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact", "*.jsf", "/faces/*");

        String exactRequestUrl = viewHandler.getActionURL(facesContext, "/fallback.xhtml");

        assertEquals("/app/fallback.jsf", exactRequestUrl);
    }

    @Test
    public void getActionURLThrowsWhenNoWildcardFallbackExists() {
        configureRequest(MockFacesMappingSupport.mapping(EXACT, "/exact", "exact"), "/exact");

        assertThrows(IllegalStateException.class, () -> viewHandler.getActionURL(facesContext, "/fallback.xhtml"));
    }

    private void configureRequest(jakarta.servlet.http.HttpServletMapping mapping, String... servletMappings) {
        request = MockFacesMappingSupport.request(CONTEXT_PATH, mapping, session);
        externalContext = MockFacesMappingSupport.externalContext(servletContext, request, response);
        facesContext.setExternalContext(externalContext);
        MockFacesMappingSupport.setFacesServletMappings(servletContext, servletMappings);
    }
}
