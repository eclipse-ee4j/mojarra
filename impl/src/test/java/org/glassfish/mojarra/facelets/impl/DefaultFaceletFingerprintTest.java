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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.URL;

import org.glassfish.mojarra.facelets.compiler.Compiler;
import org.glassfish.mojarra.facelets.compiler.EncodingHandler;
import org.glassfish.mojarra.facelets.tag.SavedBuildTimeDecisions;
import org.junit.jupiter.api.Test;

/**
 * A Facelet saves the fingerprint of its source beside the build time decisions, under the hash of its path. A Facelet fabricated for
 * {@code ViewDeclarationLanguage.createComponent} takes no part in replaying build time decisions, whether it installs an id mapper of its own or the
 * application aliases no ids at all.
 */
class DefaultFaceletFingerprintTest {

    private static final String ALIAS = "/page.xhtml";

    private final DefaultFaceletFactory factory = new DefaultFaceletFactory();

    @Test
    void faceletSavesItsFingerprintUnderTheHashOfItsPath() throws Exception {
        assertEquals(SavedBuildTimeDecisions.sourceFingerprintKey(ALIAS), facelet(null, false).getSourceFingerprintKey());
    }

    @Test
    void fabricatedFaceletWithIdMapperOfItsOwnSavesNoFingerprint() throws Exception {
        assertNull(facelet(new IdMapper(), true).getSourceFingerprintKey());
    }

    @Test
    void fabricatedFaceletWithoutIdMapperSavesNoFingerprint() throws Exception {
        assertNull(facelet(null, true).getSourceFingerprintKey());
    }

    /**
     * Without an id mapper of its own, which the factory gives no fabricated Facelet when the application aliases no ids, a fabricated Facelet is still
     * recognized as one.
     */
    @Test
    void factoryFabricatesAFaceletWhichSavesNoFingerprintWhenNoIdsAreAliased() throws Exception {
        Compiler compiler = mock(Compiler.class);
        when(compiler.compile(any(URL.class), anyString())).thenReturn(root());
        DefaultResourceResolver resolver = mock(DefaultResourceResolver.class);
        when(resolver.resolveUrl("/")).thenReturn(new URL("file:/"));
        factory.initResourceResolution(resolver, new String[0], null);
        Field compilerField = DefaultFaceletFactory.class.getDeclaredField("compiler");
        compilerField.setAccessible(true);
        compilerField.set(factory, compiler);

        assertNull(factory.createFabricatedIdMapper());
        assertNull(factory.createFabricatedFacelet(new URL("file:/mojarra1.tmp")).getSourceFingerprintKey());
    }

    private static EncodingHandler root() {
        return new EncodingHandler((ctx, parent) -> {
        }, "UTF-8", null, false, 1);
    }

    private DefaultFacelet facelet(IdMapper ownIdMapper, boolean fabricated) throws Exception {
        return new DefaultFacelet(factory, null, new URL("file:" + ALIAS), ALIAS, root(), ownIdMapper, fabricated);
    }

}
