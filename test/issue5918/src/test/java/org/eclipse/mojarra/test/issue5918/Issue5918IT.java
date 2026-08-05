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
package org.eclipse.mojarra.test.issue5918;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;

/**
 * The webapp declares <code>jakarta.faces.FACELETS_SUFFIX</code> as <code>.page  .html</code>, with a redundant
 * space between the two entries, and <code>jakarta.faces.FACELETS_VIEW_MAPPINGS</code> as <code>*.page</code>.
 */
public class Issue5918IT extends BaseIT {

    /**
     * A view whose physical resource carries the first configured suffix must resolve.
     */
    @Test
    public void testFirstConfiguredSuffix() {
        open("firstSuffix.jsf");
        assertTrue(getPageSource().contains("first suffix view"));
    }

    /**
     * <code>jakarta.faces.FACELETS_SUFFIX</code> is a list of which the first suffix whose physical resource
     * exists wins, so a view which exists only under the second configured suffix must resolve as well. This must
     * also hold when <code>jakarta.faces.FACELETS_VIEW_MAPPINGS</code> is declared and does not name that suffix.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/5918
     */
    @Test
    public void testSecondConfiguredSuffix() {
        open("secondSuffix.jsf");
        assertTrue(getPageSource().contains("second suffix view"));
    }

    /**
     * The default Facelets suffix identifies a Facelet regardless of which view suffixes the webapp configures, so
     * an include of an <code>.xhtml</code> fragment must resolve even when the configured list does not name it.
     */
    @Test
    public void testIncludeDefaultSuffixFragment() {
        open("defaultSuffixInclude.jsf");
        assertTrue(getPageSource().contains("default suffix fragment"));
    }

    /**
     * Redundant whitespace in <code>jakarta.faces.FACELETS_SUFFIX</code> must not yield an empty suffix, which
     * would match every resource and so let an include disclose a deployment descriptor.
     */
    @Test
    public void testIncludeDescriptorBlocked() {
        open("descriptorInclude.jsf");
        String source = getPageSource();
        assertFalse(source.contains("<web-app"), "web.xml content must not leak");
        assertFalse(source.contains("FACELETS_SUFFIX"), "web.xml content must not leak");
    }
}
