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

package com.sun.faces.facelets.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.Test;

/**
 * A build aliases every generated id through the {@link IdMapper} the outermost Facelet installed, so the alias a tag
 * gets may not depend on which Facelet in the build is applying it, nor on the context resolving the mapper again for
 * every tag.
 */
class FaceletContextIdMapperTest {

    private final FacesContext faces = mock(FacesContext.class);

    FaceletContextIdMapperTest() {
        when(faces.getAttributes()).thenReturn(new HashMap<>());
        when(faces.getELContext()).thenReturn(mock(ELContext.class));
    }

    @Test
    void anIdIsAliasedThroughTheMapperInstalledForTheBuild() {
        IdMapper mapper = new IdMapper();
        IdMapper.setMapper(faces, mapper);

        DefaultFaceletContext context = new DefaultFaceletContext(faces, null);

        assertEquals(mapper.getAliasedId("j_id7"), context.getAliasedId("j_id7"));
        assertNotEquals("j_id7", context.getAliasedId("j_id7"), "the mapper does alias");
    }

    @Test
    void anIncludedFaceletAliasesThroughTheOutermostBuildsMapper() {
        IdMapper.setMapper(faces, new IdMapper());

        DefaultFaceletContext outer = new DefaultFaceletContext(faces, null);
        DefaultFaceletContext included = new DefaultFaceletContext(outer, null);

        assertEquals(outer.getAliasedId("j_id7"), included.getAliasedId("j_id7"));
    }

    @Test
    void withoutAMapperAnIdIsItsOwnAlias() {
        DefaultFaceletContext context = new DefaultFaceletContext(faces, null);

        assertEquals("j_id7", context.getAliasedId("j_id7"));
    }
}
