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

import static com.sun.faces.facelets.tag.faces.ComponentSupport.MARK_CREATED;
import static com.sun.faces.facelets.tag.faces.ComponentSupport.MARK_DELETED;
import static com.sun.faces.facelets.tag.faces.core.FacetHandler.KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;

import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.Test;

/**
 * Facelets marks every component it creates with the tag id it came from, scopes a facet by putting its name on the
 * parent component's attributes for the duration of the facet body, and flags components for deletion the same way.
 * Those markers are answered from a field rather than from the state map, so the attributes map has to keep reading
 * like a map while the marker stays out of the component's state - except that the creation marker, which is live on
 * every component, still has to survive a full-state save, since restoring full state runs no view build to
 * re-establish it.
 */
class UIComponentBaseMarkerAttributesTest {

    @Test
    void fieldBackedMarkersReadBackThroughTheAttributesMap() {
        Map<String, Object> attributes = new UIPanel().getAttributes();
        attributes.put(KEY, "header");
        attributes.put(MARK_CREATED, "j_id1");

        assertEquals("header", attributes.get(KEY));
        assertEquals("j_id1", attributes.get(MARK_CREATED));
        assertTrue(attributes.containsKey(KEY));
        assertTrue(attributes.containsKey(MARK_CREATED));

        attributes.remove(KEY);
        attributes.remove(MARK_CREATED);

        assertNull(attributes.get(KEY));
        assertNull(attributes.get(MARK_CREATED));
        assertFalse(attributes.containsKey(KEY));
        assertFalse(attributes.containsKey(MARK_CREATED));
    }

    @Test
    void buildTimeMarkersStayOutOfTheStateBackedAttributes() {
        Map<String, Object> attributes = new UIPanel().getAttributes();
        attributes.put(KEY, "header");
        attributes.put(MARK_CREATED, "j_id1");
        attributes.put(MARK_DELETED, Boolean.TRUE);
        attributes.put("data-role", "banner");

        assertEquals(Set.of("data-role"), attributes.keySet(),
                "only the plain attribute is state-backed, so only it can be saved");
    }

    @Test
    void fullStateSaveCarriesTheCreationMarker() {
        FacesContext context = mock(FacesContext.class);
        UIPanel saved = new UIPanel();
        saved.getAttributes().put(MARK_CREATED, "j_id1");

        UIPanel restored = new UIPanel();
        restored.restoreState(context, saved.saveState(context));

        assertEquals("j_id1", restored.getAttributes().get(MARK_CREATED),
                "full state carries no view build to re-establish the marker");
    }
}
