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

package org.glassfish.mojarra.test.perf.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Named;

/**
 * Backs the {@code flat-allattrs-late} scenarios: puts two attributes which are no property on every component of the view before it renders. As the view is
 * built by then, each component's partial state delta carries them, the way a view scoped bean saved in the view state puts its storage on the view root.
 */
@Named("allAttrsLate")
@ApplicationScoped
public class AllAttrsLateBean {

    public void putLate(ComponentSystemEvent event) {
        put(event.getComponent());
    }

    private static void put(UIComponent component) {
        component.getAttributes().put("late1", "x");
        component.getAttributes().put("late2", "y");
        component.getFacetsAndChildren().forEachRemaining(AllAttrsLateBean::put);
    }

}
