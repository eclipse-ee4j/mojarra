/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.el;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A resource name may carry a query string, as specified in section 2.6.1.3 "Resource Identifiers" of the Jakarta Faces Specification Document. The colon
 * separating the library name is therefore the one preceding the first '?', and the query string, which may hold colons of its own, stays with the resource
 * name.
 */
class ResourceELResolverQueryStringTest {

    private static final String REQUEST_PATH = "/jakarta.faces.resource/theme.css.xhtml";

    private final ResourceELResolver elResolver = new ResourceELResolver();
    private final ResourceHandler resourceHandler = mock(ResourceHandler.class);
    private final ELContext elContext = mock(ELContext.class);

    @BeforeEach
    void letEveryResourceResolveToOneRequestPath() {
        Resource resource = mock(Resource.class);
        when(resource.getRequestPath()).thenReturn(REQUEST_PATH);
        when(resourceHandler.createResource(anyString())).thenReturn(resource);
        when(resourceHandler.createResource(anyString(), anyString())).thenReturn(resource);

        FacesContext facesContext = mock(FacesContext.class);
        ExternalContext externalContext = mock(ExternalContext.class);
        when(elContext.getContext(FacesContext.class)).thenReturn(facesContext);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.encodeResourceURL(REQUEST_PATH)).thenReturn(REQUEST_PATH);
    }

    @Test
    void aColonInTheQueryStringDoesNotSeparateTheLibraryName() {
        assertEquals(REQUEST_PATH, elResolver.getValue(elContext, resourceHandler, "lib:theme.css?v=1:2"));

        verify(resourceHandler).createResource("theme.css?v=1:2", "lib");
    }

    @Test
    void aColonInTheQueryStringDoesNotIntroduceALibraryName() {
        assertEquals(REQUEST_PATH, elResolver.getValue(elContext, resourceHandler, "theme.css?v=1:2"));

        verify(resourceHandler).createResource("theme.css?v=1:2");
    }

    @Test
    void aQueryStringMayHoldMoreThanOneColon() {
        assertEquals(REQUEST_PATH, elResolver.getValue(elContext, resourceHandler, "lib:theme.css?a=1:2&b=3:4"));

        verify(resourceHandler).createResource("theme.css?a=1:2&b=3:4", "lib");
    }

    @Test
    void moreThanOneColonBeforeTheQueryStringIsStillInvalid() {
        assertThrows(ELException.class, () -> elResolver.getValue(elContext, resourceHandler, "lib:sub:theme.css?v=1"));
    }

}
