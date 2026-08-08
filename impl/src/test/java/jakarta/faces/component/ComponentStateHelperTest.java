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

package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;

import com.example.faces.NonStandardComponent;
import org.junit.jupiter.api.Test;

/**
 * Restoring full state runs no setters, so the helper re-records the attributes a standard component's generated
 * setters would have tracked. Which components those are is decided by the component's own package, the same test
 * {@code HtmlComponentUtils.handleAttribute} and {@code RenderKitUtils.getAttributeIfSet} apply on the write and read
 * sides.
 */
class ComponentStateHelperTest {

    @Test
    void restoringAComponentFromOutsideTheStandardPackageDoesNotStartTrackingItsAttributes() {
        FacesContext context = mock(FacesContext.class);

        NonStandardComponent saved = new NonStandardComponent();
        saved.setRendered(false);
        Object state = saved.getStateHelper().saveState(context);

        NonStandardComponent restored = new NonStandardComponent();
        restored.getStateHelper().restoreState(context, state);

        assertNull(restored.getAttributes().get(UIComponentBase.ATTRIBUTES_THAT_ARE_SET));
    }

    @Test
    void restoringAStandardComponentTracksTheAttributesItsSettersWouldHave() {
        FacesContext context = mock(FacesContext.class);

        HtmlPanelGroup saved = new HtmlPanelGroup();
        saved.setStyleClass("cell");
        Object state = saved.getStateHelper().saveState(context);

        HtmlPanelGroup restored = new HtmlPanelGroup();
        restored.getStateHelper().restoreState(context, state);

        assertTrackedAttributes(restored, "styleClass");
    }

    @SuppressWarnings("unchecked")
    private static void assertTrackedAttributes(UIComponent component, String... expected) {
        org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of(expected),
                component.getAttributes().get(UIComponentBase.ATTRIBUTES_THAT_ARE_SET));
    }
}
