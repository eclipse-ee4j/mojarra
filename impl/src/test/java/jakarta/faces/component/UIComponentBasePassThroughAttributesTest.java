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

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * The map behind {@link UIComponent#getPassThroughAttributes(boolean)} carries the guarantees its javadoc states -
 * serializable, no null key or value, no non-String key - and iterates in the order the attributes were declared, so a
 * view renders its pass-through attributes byte-identically across requests.
 */
class UIComponentBasePassThroughAttributesTest {

    @Test
    void attributesIterateInTheOrderTheyWereSet() {
        Map<String, Object> passThroughAttributes = new UIOutput().getPassThroughAttributes(true);
        passThroughAttributes.put("data-zulu", "1");
        passThroughAttributes.put("data-alpha", "2");
        passThroughAttributes.put("data-mike", "3");

        assertEquals(List.of("data-zulu", "data-alpha", "data-mike"), new ArrayList<>(passThroughAttributes.keySet()));
    }

    @Test
    void theMapIsSerializable() {
        assertTrue(new UIOutput().getPassThroughAttributes(true) instanceof Serializable);
    }

    @Test
    void nullKeyOrValueIsRejected() {
        Map<String, Object> passThroughAttributes = new UIOutput().getPassThroughAttributes(true);

        assertThrows(NullPointerException.class, () -> passThroughAttributes.put(null, "value"));
        assertThrows(NullPointerException.class, () -> passThroughAttributes.put("data-x", null));
        assertThrows(NullPointerException.class, () -> passThroughAttributes.putIfAbsent("data-x", null));
        assertThrows(NullPointerException.class, () -> passThroughAttributes.putAll(singletonMap("data-x", null)));
    }

    /**
     * Reading the attributes of a component that has none must not give it a state helper, since every rendered element
     * asks for them.
     */
    @Test
    void readingWithoutCreatingLeavesTheComponentStateless() {
        UIOutput component = new UIOutput();

        assertNull(component.getPassThroughAttributes(false));
        assertNull(component.getStateHelper(false));
    }
}
