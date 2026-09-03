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

package org.glassfish.mojarra.util;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.enumeration;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.config.MojarraContextParam;
import org.junit.jupiter.api.Test;

/**
 * Covers the <code>auto</code> value of <code>org.glassfish.mojarra.disableIdUniquenessCheck</code>, which skips the whole-tree duplicate id walk outside
 * {@link ProjectStage#Development}. The parameter is what the callers of the walk gate on, so it is asserted here rather than through
 * {@link Util#checkIdUniqueness}, which walks whatever it is given.
 */
class IdUniquenessCheckTest {

    private static final String DISABLE_ID_UNIQUENESS_CHECK = "org.glassfish.mojarra.disableIdUniquenessCheck";
    private static final String CLIENT_ID = "id";

    /**
     * The walk is the per-request cost the parameter exists to avoid, so <code>auto</code> disables it in every stage which is not <code>Development</code>,
     * including the test stages.
     */
    @Test
    void autoDisablesTheCheckOutsideDevelopment() {
        for (ProjectStage stage : List.of(ProjectStage.Production, ProjectStage.SystemTest, ProjectStage.UnitTest)) {
            assertTrue(MojarraContextParam.DISABLE_ID_UNIQUENESS_CHECK.isEnabled(contextFor(stage)), stage.name());
        }
    }

    /**
     * And it leaves it on while developing, which is what makes disabling it elsewhere acceptable.
     */
    @Test
    void autoLeavesTheCheckOnInDevelopment() {
        assertFalse(MojarraContextParam.DISABLE_ID_UNIQUENESS_CHECK.isEnabled(contextFor(ProjectStage.Development)));
    }

    /**
     * And the walk itself no longer asks: it visits every component it is handed, because the caller has already decided whether the view it is about to save
     * is worth walking.
     */
    @Test
    void theWalkVisitsWhateverItIsGiven() {
        UIComponent child = componentWithClientId();
        UIComponent root = rootOf(child);

        Util.checkIdUniqueness(contextFor(ProjectStage.Production), root, new HashSet<>());

        verify(child).getClientId(any());
    }

    private static UIComponent componentWithClientId() {
        UIComponent component = mock(UIComponent.class);
        when(component.getClientId(any())).thenReturn(CLIENT_ID);
        when(component.getFacetsAndChildren()).thenAnswer(invocation -> emptyIterator());
        return component;
    }

    private static UIComponent rootOf(UIComponent... children) {
        UIComponent root = mock(UIComponent.class);
        when(root.getFacetsAndChildren()).thenAnswer(invocation -> List.of(children).iterator());
        return root;
    }

    private static FacesContext contextFor(ProjectStage stage) {
        Map<String, Object> attributes = new HashMap<>();

        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getContextPath()).thenReturn("/test");
        when(servletContext.getInitParameterNames()).thenAnswer(invocation -> enumeration(List.of(DISABLE_ID_UNIQUENESS_CHECK)));
        when(servletContext.getInitParameter(DISABLE_ID_UNIQUENESS_CHECK)).thenReturn("auto");
        when(servletContext.getInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME)).thenReturn(stage.name());
        when(servletContext.getAttribute(any())).thenAnswer(invocation -> attributes.get(invocation.<String>getArgument(0)));
        doAnswer(invocation -> attributes.put(invocation.getArgument(0), invocation.getArgument(1)))
            .when(servletContext).setAttribute(any(), any());

        ExternalContext externalContext = mock(ExternalContext.class);
        when(externalContext.getApplicationMap()).thenReturn(new HashMap<>());
        when(externalContext.getContext()).thenReturn(servletContext);

        FacesContext context = mock(FacesContext.class);
        when(context.getExternalContext()).thenReturn(externalContext);
        when(context.isProjectStage(any())).thenAnswer(invocation -> invocation.getArgument(0) == stage);

        return context;
    }

}
