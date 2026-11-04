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
package com.sun.faces.application.view;

import static com.sun.faces.application.view.FaceletFullStateManagementStrategy.refreshAddIndex;
import static com.sun.faces.util.ComponentStruct.ADD;
import static com.sun.faces.util.ComponentStruct.APPEND;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sun.faces.util.ComponentStruct;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;

/**
 * Full state saving replays each ADD action on the saved tree, which holds only the non-transient children. So the
 * index the action carries is the position of its child among those, as the view is saved.
 */
class FaceletFullStateManagementStrategyTest {

    private static final int ADD_TIME_INDEX = 7;

    @Test
    void anAddActionCarriesThePositionOfItsChildAmongTheNonTransientChildren() {
        UIComponent child = new UIOutput();
        parentOf(new UIOutput(), transientOutput(), child);
        ComponentStruct action = add();

        refreshAddIndex(action, child);

        assertEquals(1, action.getIndex());
    }

    /**
     * A leading transient child leaves no position to restore the child at, so it is appended.
     */
    @Test
    void anAddActionOfAChildBehindALeadingTransientChildAppendsIt() {
        UIComponent child = new UIOutput();
        parentOf(transientOutput(), child);
        ComponentStruct action = add();

        refreshAddIndex(action, child);

        assertEquals(APPEND, action.getIndex());
    }

    @Test
    void anAddActionOfAFacetKeepsItsIndex() {
        UIComponent facet = new UIOutput();
        new UIPanel().getFacets().put("header", facet);
        ComponentStruct action = new ComponentStruct(ADD, "header", "parent", "facet", "facet");
        action.setIndex(ADD_TIME_INDEX);

        refreshAddIndex(action, facet);

        assertEquals(ADD_TIME_INDEX, action.getIndex());
    }

    @Test
    void anAddActionOfAChildNoLongerInTheViewKeepsItsIndex() {
        ComponentStruct action = add();

        refreshAddIndex(action, new UIOutput());

        assertEquals(ADD_TIME_INDEX, action.getIndex());
    }

    private static ComponentStruct add() {
        ComponentStruct action = new ComponentStruct(ADD, null, "parent", "child", "child");
        action.setIndex(ADD_TIME_INDEX);
        return action;
    }

    private static UIComponent transientOutput() {
        UIComponent output = new UIOutput();
        output.setTransient(true);
        return output;
    }

    private static void parentOf(UIComponent... children) {
        UIComponent parent = new UIPanel();
        for (UIComponent child : children) {
            parent.getChildren().add(child);
        }
    }
}
