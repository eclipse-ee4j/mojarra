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

package org.glassfish.mojarra.test.impl.issue5958;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import org.glassfish.mojarra.context.StateContext;
import org.glassfish.mojarra.util.ComponentStruct;

/**
 * Exposes implementation state that decides what a view has to save, so a page can render it and an integration test can assert on it.
 */
@Named
@RequestScoped
public class Issue5958Probe {

    /**
     * The number of add/remove actions recorded for this view. Every action carries a component through the saved state instead of leaving it to the view
     * build, so a view whose tree the build fully reproduces must report zero.
     */
    public int getDynamicActionCount() {
        List<ComponentStruct> dynamicActions = StateContext.getStateContext(FacesContext.getCurrentInstance()).getDynamicActions();
        return dynamicActions == null ? 0 : dynamicActions.size();
    }

}
