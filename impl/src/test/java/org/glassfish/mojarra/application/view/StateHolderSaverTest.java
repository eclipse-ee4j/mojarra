/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.application.view;

import static org.glassfish.mojarra.junit.Serialization.deserialize;
import static org.glassfish.mojarra.junit.Serialization.serialize;
import static org.glassfish.mojarra.junit.Serialization.serializeAndDeserialize;
import static org.glassfish.mojarra.junit.Serialization.withSerialVersionUID;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.InvalidClassException;
import java.lang.reflect.Field;

import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.Test;

/**
 * A dynamically added component is saved along with its class name, and restored as a new instance of that class.
 */
class StateHolderSaverTest {

    private static final long OLDER_SERIAL_VERSION_UID = 4866548262845392093L;

    /**
     * The state of the component is saved as is, without a wrapper.
     */
    @Test
    void dynamicallyAddedComponentRoundTrips() throws Exception {
        FacesContext context = mock(FacesContext.class);
        HtmlOutputText component = outputText();
        StateHolderSaver saver = new StateHolderSaver(context, component);

        assertArrayEquals((Object[]) component.saveState(context), (Object[]) savedState().get(saver));
        assertRestoredOutputText(serializeAndDeserialize(saver).restore(context));
    }

    /**
     * A state in the serialized form of an older version, which wraps the state of the component along with whether it was added dynamically, is not read, so
     * the state helpers expire the view.
     */
    @Test
    void dynamicallyAddedComponentInTheSerializedFormOfAnOlderVersionIsNotRead() throws Exception {
        FacesContext context = mock(FacesContext.class);
        byte[] serialized = withSerialVersionUID(
            serialize(new StateHolderSaver(context, outputText())), StateHolderSaver.class.getName(), OLDER_SERIAL_VERSION_UID
        );

        assertThrows(InvalidClassException.class, () -> deserialize(serialized));
    }

    private static Field savedState() throws NoSuchFieldException {
        Field savedState = StateHolderSaver.class.getDeclaredField("savedState");
        savedState.setAccessible(true);
        return savedState;
    }

    private static HtmlOutputText outputText() {
        HtmlOutputText component = new HtmlOutputText();
        component.setId("output");
        component.setValue("value");
        return component;
    }

    private static void assertRestoredOutputText(Object restored) {
        HtmlOutputText component = (HtmlOutputText) restored;
        assertEquals("output", component.getId());
        assertEquals("value", component.getValue());
    }

}
