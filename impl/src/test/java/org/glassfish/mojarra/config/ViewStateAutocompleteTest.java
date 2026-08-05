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

package org.glassfish.mojarra.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.glassfish.mojarra.context.MojarraContextParam;
import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;

/**
 * Covers how <code>org.glassfish.mojarra.viewStateAutocomplete</code> resolves, including the deprecated boolean it
 * replaces.
 */
class ViewStateAutocompleteTest {

    @Test
    void defaultsToOneTimeCode() {
        assertEquals("one-time-code", resolve());
    }

    @Test
    void isWrittenVerbatim() {
        assertEquals("off", resolve(MojarraContextParam.VIEW_STATE_AUTOCOMPLETE.getName(), "off"));
    }

    /**
     * The deprecated boolean keeps meaning what it meant, so an application which set it does not silently change.
     */
    @Test
    void deprecatedBooleanStillSelectsOff() {
        assertEquals("off", resolve(MojarraContextParam.AUTO_COMPLETE_OFF_ON_VIEW_STATE.getName(), "true"));
        assertEquals("one-time-code", resolve(MojarraContextParam.AUTO_COMPLETE_OFF_ON_VIEW_STATE.getName(), "false"));
    }

    /**
     * And the replacement wins when both are declared, rather than the declaration order deciding.
     */
    @Test
    void replacementWinsOverDeprecatedBoolean() {
        assertEquals("on", resolve(
                MojarraContextParam.AUTO_COMPLETE_OFF_ON_VIEW_STATE.getName(), "true",
                MojarraContextParam.VIEW_STATE_AUTOCOMPLETE.getName(), "on"));
    }

    private static String resolve(String... initParameterNamesAndValues) {
        MockServletContext servletContext = new MockServletContext();

        for (int i = 0; i < initParameterNamesAndValues.length; i += 2) {
            servletContext.addInitParameter(initParameterNamesAndValues[i], initParameterNamesAndValues[i + 1]);
        }

        return WebConfiguration.getInstance(servletContext).getViewStateAutocomplete();
    }
}
