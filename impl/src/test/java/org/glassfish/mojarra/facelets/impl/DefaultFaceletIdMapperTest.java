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

package org.glassfish.mojarra.facelets.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.el.ELContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletHandler;

import org.glassfish.mojarra.util.Cache;
import org.junit.jupiter.api.Test;

/**
 * A Facelet the {@link DefaultFaceletFactory} does not cache is applied under an alias that never recurs, so it may not take its {@link IdMapper} from the
 * application scoped cache keyed by that alias, and it must install its own mapper for its build instead of aliasing through the one an ongoing build put in
 * place. Every other Facelet keeps sharing the mapper of its alias, and the ids of a build stay unique across mappers.
 */
class DefaultFaceletIdMapperTest {

    private final FacesContext faces = mock(FacesContext.class);
    private final DefaultFaceletFactory factory = new DefaultFaceletFactory();
    private final AtomicInteger mappersCreated = new AtomicInteger();

    DefaultFaceletIdMapperTest() {
        when(faces.getAttributes()).thenReturn(new HashMap<>());
        when(faces.getELContext()).thenReturn(mock(ELContext.class));
        factory.idMappers = new Cache<>(alias -> {
            mappersCreated.incrementAndGet();
            return new IdMapper();
        });
    }

    @Test
    void aFaceletWithItsOwnMapperLeavesTheApplicationScopedCacheUntouched() throws Exception {
        for (int i = 0; i < 1000; i++) {
            facelet("/mojarra" + i + ".tmp", IdMapper.numbered(i), null);
        }

        assertEquals(0, mappersCreated.get());
    }

    @Test
    void aFaceletWithoutItsOwnMapperTakesOneFromTheApplicationScopedCache() throws Exception {
        facelet("/page.xhtml", null, null);
        facelet("/page.xhtml", null, null);
        facelet("/other.xhtml", null, null);

        assertEquals(2, mappersCreated.get(), "one mapper per alias, shared by every Facelet under it");
    }

    @Test
    void aFaceletWithItsOwnMapperInstallsItForItsBuildAndRestoresTheOuterOne() throws Exception {
        IdMapper outer = new IdMapper();
        IdMapper own = IdMapper.numbered(1);
        IdMapper.setMapper(faces, outer);
        AtomicReference<IdMapper> duringBuild = new AtomicReference<>();

        facelet("/mojarra1.tmp", own, duringBuild).apply(faces, parent());

        assertSame(own, duringBuild.get());
        assertSame(outer, IdMapper.getMapper(faces));
    }

    @Test
    void aFaceletWithoutItsOwnMapperAliasesThroughTheOuterOne() throws Exception {
        IdMapper outer = new IdMapper();
        IdMapper.setMapper(faces, outer);
        AtomicReference<IdMapper> duringBuild = new AtomicReference<>();

        facelet("/page.xhtml", null, duringBuild).apply(faces, parent());

        assertSame(outer, duringBuild.get());
        assertSame(outer, IdMapper.getMapper(faces));
    }

    @Test
    void aFaceletStartingABuildInstallsItsOwnMapperAndLeavesNoneBehind() throws Exception {
        AtomicReference<IdMapper> duringBuild = new AtomicReference<>();

        facelet("/page.xhtml", null, duringBuild).apply(faces, parent());

        assertTrue(duringBuild.get() != null, "the outermost Facelet installs the mapper of its alias");
        assertNull(IdMapper.getMapper(faces));
    }

    @Test
    void everyFabricatedFaceletGetsAMapperWithAPrefixOfItsOwn() {
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            ids.add(factory.createFabricatedIdMapper().getAliasedId("tag1"));
        }

        assertEquals(1000, ids.size());
        assertEquals(0, mappersCreated.get(), "a fabricated Facelet's mapper never enters the application scoped cache");
    }

    @Test
    void anApplicationThatDoesNotAliasIdsGetsNoFabricatedMapperEither() {
        factory.idMappers = null;

        assertNull(factory.createFabricatedIdMapper());
    }

    @Test
    void mappersWithDistinctPrefixesGenerateDistinctIds() {
        Set<String> ids = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            IdMapper mapper = i == 0 ? new IdMapper() : IdMapper.numbered(i);

            for (int tag = 0; tag < 100; tag++) {
                ids.add(mapper.getAliasedId("tag" + tag));
            }
        }

        assertEquals(100 * 100, ids.size());
    }

    private DefaultFacelet facelet(String alias, IdMapper ownIdMapper, AtomicReference<IdMapper> duringBuild) throws IOException {
        FaceletHandler root = (ctx, parent) -> {
            if (duringBuild != null) {
                duringBuild.set(IdMapper.getMapper(ctx.getFacesContext()));
            }
        };

        return new DefaultFacelet(factory, null, new URL("file:" + alias), alias, root, ownIdMapper);
    }

    private static UIComponent parent() {
        UIComponent parent = mock(UIComponent.class);
        when(parent.getAttributes()).thenReturn(new HashMap<>());
        return parent;
    }

}
