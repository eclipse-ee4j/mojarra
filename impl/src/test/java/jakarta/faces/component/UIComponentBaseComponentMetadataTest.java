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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Method;

import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.Test;

/**
 * Every component construction resolves the reflective property metadata of its class, so that lookup is keyed on the
 * class alone: it is shared by all instances of a class, kept apart between classes, and available without a current
 * {@link FacesContext} - a component may be constructed outside a request, and its property attributes have to read and
 * write through to the bean properties there too.
 */
class UIComponentBaseComponentMetadataTest {

    @Test
    void metadataIsSharedPerClassAndDistinctBetweenClasses() {
        UIPanel firstPanel = new UIPanel();
        UIPanel secondPanel = new UIPanel();
        UIOutput output = new UIOutput();

        assertSame(firstPanel.getDescriptorMap(), secondPanel.getDescriptorMap());
        assertSame(firstPanel.getReadMethodMap(), secondPanel.getReadMethodMap());
        assertSame(firstPanel.getWriteMethodMap(), secondPanel.getWriteMethodMap());
        assertNotSame(firstPanel.getDescriptorMap(), output.getDescriptorMap());
    }

    @Test
    void propertyAttributesWorkWithoutACurrentFacesContext() throws Exception {
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, (FacesContext) null);

        UIOutput output = new UIOutput();
        output.getAttributes().put("rendered", false);

        assertEquals(false, output.getAttributes().get("rendered"));
        assertEquals(false, output.isRendered());
    }
}
