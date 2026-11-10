/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.mojarra.renderkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.faces.component.UIParameter;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.SelectItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SelectItemsIteratorTest {

    private static final List<Integer> VALUES = List.of(0, 1, 2, 3, 4);

    private FacesContext facesContext;

    @BeforeEach
    void setUp() {
        facesContext = mock(FacesContext.class);
        ExternalContext externalContext = mock(ExternalContext.class);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getRequestMap()).thenReturn(new HashMap<>());
    }

    @Test
    void skipsNonSelectItemChildAtEnd() {
        UISelectMany selectMany = new UISelectMany();
        selectMany.getChildren().add(selectItem("orr"));
        selectMany.getChildren().add(parameter());
        selectMany.getChildren().add(selectItem("esposito"));

        assertValues(selectMany, "orr", "esposito");
    }

    @Test
    void skipsNonSelectItemChildInMiddle() {
        UISelectMany selectMany = new UISelectMany();
        selectMany.getChildren().add(selectItem("gretsky"));
        selectMany.getChildren().add(parameter());
        selectMany.getChildren().add(selectItem("howe"));

        assertValues(selectMany, "gretsky", "howe");
    }

    @Test
    void iteratesNothingFromEmptyCollection() {
        Iterator<SelectItem> iterator = new SelectItemsIterator<>(facesContext, selectItems(List.of()));

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void reusesOneSelectItemForCollectionOfNonSelectItems() {
        assertFlyweightSelectItems(selectItems(VALUES));
    }

    @Test
    void reusesOneSelectItemForDataModel() {
        assertFlyweightSelectItems(selectItems(new ListDataModel<>(VALUES)));
    }

    private void assertValues(UISelectMany selectMany, String... expectedValues) {
        Iterator<SelectItem> iterator = new SelectItemsIterator<>(facesContext, selectMany);

        for (String expectedValue : expectedValues) {
            assertTrue(iterator.hasNext());
            assertEquals(expectedValue, iterator.next().getValue());
        }

        assertFalse(iterator.hasNext());
    }

    private void assertFlyweightSelectItems(UISelectMany selectMany) {
        Iterator<SelectItem> iterator = new SelectItemsIterator<>(facesContext, selectMany);
        SelectItem previous = null;

        for (Integer value : VALUES) {
            assertTrue(iterator.hasNext());
            SelectItem item = iterator.next();
            assertNotNull(item);
            assertEquals(value, item.getValue());
            assertEquals(value.toString(), item.getLabel());
            assertNull(item.getDescription());
            assertFalse(item.isDisabled());
            assertTrue(item.isEscape());

            if (previous != null) {
                assertSame(previous, item);
            }

            previous = item;
        }

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    private static UISelectItem selectItem(String value) {
        UISelectItem selectItem = new UISelectItem();
        selectItem.setItemValue(value);
        return selectItem;
    }

    private static UIParameter parameter() {
        UIParameter parameter = new UIParameter();
        parameter.setName("param");
        parameter.setValue("paramValue");
        return parameter;
    }

    private static UISelectMany selectItems(Object value) {
        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(value);
        UISelectMany selectMany = new UISelectMany();
        selectMany.getChildren().add(selectItems);
        return selectMany;
    }

}
