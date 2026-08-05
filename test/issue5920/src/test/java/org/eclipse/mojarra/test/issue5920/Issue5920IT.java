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
package org.eclipse.mojarra.test.issue5920;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;

/**
 * The webapp declares <code>jakarta.faces.FACELETS_VIEW_MAPPINGS</code> as <code>/faces/*;*.tpl</code>, a prefix entry
 * and an extension entry, neither of which names the default Facelets suffix, and does not declare
 * <code>jakarta.faces.FACELETS_SUFFIX</code> at all.
 */
public class Issue5920IT extends BaseIT {

    /**
     * <code>jakarta.faces.FACELETS_VIEW_MAPPINGS</code> declares additional views as Facelets, it does not withdraw the
     * ones which are already recognized by their suffix, so a view carrying the default suffix must still resolve. The
     * whole application fails to deploy when it does not, because the runtime resolves a view id carrying that suffix
     * during startup to instantiate the Facelets view declaration language.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/5920
     */
    @Test
    public void testDefaultSuffixView() {
        open("defaultSuffix.jsf");
        assertTrue(getPageSource().contains("default suffix view"));
    }

    /**
     * A view below a prefix entry of <code>jakarta.faces.FACELETS_VIEW_MAPPINGS</code> must resolve.
     */
    @Test
    public void testPrefixMappedView() {
        open("faces/prefixMapped.jsf");
        assertTrue(getPageSource().contains("prefix mapped view"));
    }

    /**
     * An extension entry of <code>jakarta.faces.FACELETS_VIEW_MAPPINGS</code> declares that extension to be a Facelet,
     * so an include of a fragment carrying it must resolve.
     */
    @Test
    public void testMappedSuffixInclude() {
        open("mappedSuffixInclude.jsf");
        assertTrue(getPageSource().contains("mapped suffix fragment"));
    }

    /**
     * A view carrying an extension entry of <code>jakarta.faces.FACELETS_VIEW_MAPPINGS</code> must be handled by
     * Facelets, even though that extension is not a suffix a view ID may be derived with. Requesting it through the
     * prefix mapped <code>FacesServlet</code> is what makes it reachable, as a suffix mapped request derives its view
     * ID from <code>jakarta.faces.FACELETS_SUFFIX</code> instead.
     */
    @Test
    public void testMappedSuffixView() {
        open("views/mappedSuffix.tpl");
        assertTrue(getPageSource().contains("mapped suffix view"));
    }
}
