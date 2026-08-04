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

package org.glassfish.mojarra.context;

import static org.glassfish.mojarra.context.FacesContextParam.FACELETS_SUFFIX;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * {@link jakarta.faces.application.ViewHandler#FACELETS_SUFFIX_PARAM_NAME} takes a space separated list of extensions
 * since 4.0, of which the first one resolving to an existing file wins, so it has to resolve to every extension
 * declared rather than to the value as written.
 */
class FaceletsSuffixTest {

    @Test
    void everyDeclaredSuffixIsResolved() {
        Optional<String[]> suffixes = FACELETS_SUFFIX.toValue(".xhtml .html .jsf");

        assertArrayEquals(new String[] { ".xhtml", ".html", ".jsf" }, suffixes.orElseThrow());
    }

    @Test
    void aSingleSuffixResolvesToOne() {
        Optional<String[]> suffixes = FACELETS_SUFFIX.toValue(".html");

        assertArrayEquals(new String[] { ".html" }, suffixes.orElseThrow());
    }

    /**
     * The default is the single extension the specification names, in the same shape, so that a consumer does not have
     * to tell a declared value apart from an absent one.
     */
    @Test
    void theDefaultIsTheOneSuffixTheSpecificationNames() {
        String[] suffixes = FACELETS_SUFFIX.getDefaultValue(null);

        assertArrayEquals(new String[] { ".xhtml" }, suffixes);
    }
}
