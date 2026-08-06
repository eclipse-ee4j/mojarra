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

package org.glassfish.mojarra.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.faces.application.StateManager;

import org.glassfish.mojarra.config.FacesContextParam;
import org.glassfish.mojarra.context.ExternalContextImpl;
import org.glassfish.mojarra.mock.MockFacesContext;
import org.glassfish.mojarra.mock.MockHttpServletRequest;
import org.glassfish.mojarra.mock.MockHttpServletResponse;
import org.glassfish.mojarra.mock.MockHttpSession;
import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.Test;

/**
 * Whether a view uses partial state saving is not the same question as whether the application does: a view named by
 * {@value StateManager#FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME} opts out on its own. Anything deciding on partial state
 * saving has to ask per view, which is why reading
 * {@link FacesContextParam#PARTIAL_STATE_SAVING} alone is not enough.
 */
class ApplicationStateInfoTest {

    private static final String FULL_STATE_VIEW_ID = "/legacy.xhtml";
    private static final String PARTIAL_STATE_VIEW_ID = "/current.xhtml";

    @Test
    void aViewNamedAsFullStateSavingOptsOutWhilePartialStateSavingIsOn() {
        ApplicationStateInfo stateInfo = configure(FULL_STATE_VIEW_ID);

        assertFalse(stateInfo.usePartialStateSaving(FULL_STATE_VIEW_ID), FULL_STATE_VIEW_ID);
        assertTrue(stateInfo.usePartialStateSaving(PARTIAL_STATE_VIEW_ID), PARTIAL_STATE_VIEW_ID);
    }

    /**
     * And with none named, every view keeps the application wide answer.
     */
    @Test
    void everyViewUsesPartialStateSavingWhenNoneIsNamed() {
        ApplicationStateInfo stateInfo = configure();

        assertTrue(stateInfo.usePartialStateSaving(FULL_STATE_VIEW_ID), FULL_STATE_VIEW_ID);
        assertTrue(stateInfo.usePartialStateSaving(PARTIAL_STATE_VIEW_ID), PARTIAL_STATE_VIEW_ID);
    }

    /**
     * And turning it off application wide takes every view with it, named or not.
     */
    @Test
    void noViewUsesPartialStateSavingWhenItIsOffApplicationWide() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, "false");
        servletContext.addInitParameter(StateManager.FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME, FULL_STATE_VIEW_ID);

        ApplicationStateInfo stateInfo = stateInfoFor(servletContext);

        assertFalse(stateInfo.usePartialStateSaving(FULL_STATE_VIEW_ID), FULL_STATE_VIEW_ID);
        assertFalse(stateInfo.usePartialStateSaving(PARTIAL_STATE_VIEW_ID), PARTIAL_STATE_VIEW_ID);
    }

    private static ApplicationStateInfo configure(String... fullStateSavingViewIds) {
        MockServletContext servletContext = new MockServletContext();

        if (fullStateSavingViewIds.length > 0) {
            servletContext.addInitParameter(StateManager.FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME, String.join(",", fullStateSavingViewIds));
        }

        return stateInfoFor(servletContext);
    }

    private static ApplicationStateInfo stateInfoFor(MockServletContext servletContext) {
        ExternalContextImpl externalContext = new ExternalContextImpl(servletContext, new MockHttpServletRequest(new MockHttpSession()),
                new MockHttpServletResponse());
        new MockFacesContext(externalContext);

        return new ApplicationStateInfo();
    }
}
