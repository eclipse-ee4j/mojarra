/*
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

package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

/**
 * <p>
 * Unit tests for {@link UISelectOne}.
 * </p>
 */
public class UISelectOneTestCase extends UIInputTestCase {

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        component = new UISelectOne();
        expectedFamily = UISelectOne.COMPONENT_FAMILY;
        expectedRendererType = "jakarta.faces.Menu";
    }

    // ------------------------------------------------- Individual Test Methods
    // Test validation of value against the valid list
    @Test
    public void testValidation() throws Exception {
        // Put our component under test in a tree under a UIViewRoot
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        // Add valid options to the component under test
        UISelectOne selectOne = (UISelectOne) component;
        selectOne.getChildren().add(new UISelectItemSub("foo", null, null));
        selectOne.getChildren().add(new UISelectItemSub("bar", null, null));
        selectOne.getChildren().add(new UISelectItemSub("baz", null, null));

        // Validate a value that is on the list
        selectOne.setValid(true);
        selectOne.setSubmittedValue("bar");
        selectOne.setRendererType(null); // We don't have any renderers
        selectOne.validate(facesContext);
        assertTrue(selectOne.isValid());

        // Validate a value that is not on the list
        selectOne.getAttributes().put("label", "mylabel");
        selectOne.setValid(true);
        selectOne.setSubmittedValue("bop");
        selectOne.validate(facesContext);
        assertTrue(!selectOne.isValid());
        Iterator<FacesMessage> messages = facesContext.getMessages();
        while (messages.hasNext()) {
            FacesMessage message = messages.next();
            assertTrue(message.getSummary().indexOf("mylabel") >= 0);
        }
    }

    // Test validation of component with UISelectItems pointing to map
    @Test
    public void testValidation2() throws Exception {
        // Put our component under test in a tree under a UIViewRoot
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        // Add valid options to the component under test
        Map<String, String> map = new HashMap<>();
        map.put("key_foo", "foo");
        map.put("key_bar", "bar");
        map.put("key_baz", "baz");
        UISelectItems items = new UISelectItems();
        items.setValue(map);
        UISelectOne selectOne = (UISelectOne) component;
        selectOne.getChildren().add(items);

        selectOne.setValid(true);
        selectOne.setSubmittedValue("foo");
        selectOne.validate(facesContext);
        assertTrue(selectOne.isValid());

        // Validate one value on the list and one not on the list
        selectOne.setValid(true);
        selectOne.setSubmittedValue("car");
        selectOne.setRendererType(null); // We don't have any renderers
        selectOne.validate(facesContext);
        assertTrue(!selectOne.isValid());
    }

    // Test validation of component with UISelectItems pointing to Set
    @Test
    public void testValidation3() throws Exception {
        Set<SelectItem> items = new HashSet<>();
        items.add(new SelectItem("foo"));
        items.add(new SelectItem("bar"));
        items.add(new SelectItem("baz"));

        testValidateWithCollection(items, "bar", "car");
    }

    // Test validation of component with UISelectItems pointing to List
    @Test
    public void testValidation4() throws Exception {
        List<SelectItem> items = new ArrayList<>();
        items.add(new SelectItem("foo"));
        items.add(new SelectItem("bar"));
        items.add(new SelectItem("baz"));

        testValidateWithCollection(items, "bar", "car");
    }

    // Test validation of component with UISelectItems pointing to an Array
    @Test
    public void testValidation5() throws Exception {
        // Put our component under test in a tree under a UIViewRoot
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        // Add valid options to the component under test
        SelectItem[] itemsArray = { new SelectItem("foo"), new SelectItem("bar"), new SelectItem("baz") };
        UISelectItems items = new UISelectItems();
        items.setValue(itemsArray);
        UISelectOne selectOne = (UISelectOne) component;
        selectOne.getChildren().add(items);

        selectOne.setValid(true);
        selectOne.setSubmittedValue("foo");
        selectOne.validate(facesContext);
        assertTrue(selectOne.isValid());

        // Validate one value on the list and one not on the list
        selectOne.setValid(true);
        selectOne.setSubmittedValue("car");
        selectOne.setRendererType(null); // We don't have any renderers
        selectOne.validate(facesContext);
        assertTrue(!selectOne.isValid());
    }

    private void testValidateWithCollection(Collection<SelectItem> selectItems, String validValue, String invalidValue) throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        UISelectItems itemsComponent = new UISelectItems();
        itemsComponent.setValue(selectItems);
        UISelectOne selectOne = (UISelectOne) component;
        selectOne.setRendererType(null);
        selectOne.getChildren().add(itemsComponent);

        selectOne.setValue(true);
        selectOne.setSubmittedValue(validValue);
        selectOne.validate(facesContext);
        assertTrue(selectOne.isValid());
        selectOne.updateModel(facesContext);

        selectOne.setValid(true);
        selectOne.setSubmittedValue(invalidValue);
        selectOne.validate(facesContext);
        assertTrue(!selectOne.isValid());
    }

    private String legalValues[] = { "A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3" };

    private String illegalValues[] = { "D1", "D2", "Group A", "Group B", "Group C" };

    // Test validation against a nested list of available options
    @Test
    public void testValidateNested() throws Exception {
        // Set up UISelectOne with nested UISelectItems
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);
        UISelectOne selectOne = (UISelectOne) component;
        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(setupOptions());
        selectOne.getChildren().add(selectItems);
        selectOne.setRequired(true);
        checkMessages(0);

        // Verify that all legal values will validate
        for (int i = 0; i < legalValues.length; i++) {
            selectOne.setValid(true);
            selectOne.setSubmittedValue(legalValues[i]);
            selectOne.validate(facesContext);
            assertTrue(selectOne.isValid(), "Value '" + legalValues[i] + "' found");
            checkMessages(0);
        }

        // Verify that illegal values will not validate
        for (int i = 0; i < illegalValues.length; i++) {
            selectOne.setValid(true);
            selectOne.setSubmittedValue(illegalValues[i]);
            selectOne.validate(facesContext);
            assertTrue(!selectOne.isValid(), "Value '" + illegalValues[i] + "' not found");
            checkMessages(i + 1);
        }
    }

    // Test validation of a required field
    @Override
    @Test
    public void testValidateRequired() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);
        UISelectOne selectOne = (UISelectOne) component;
        selectOne.getChildren().add(new UISelectItemSub("foo", null, null));
        selectOne.getChildren().add(new UISelectItemSub("bar", null, null));
        selectOne.getChildren().add(new UISelectItemSub("baz", null, null));
        selectOne.setRequired(true);
        checkMessages(0);

        selectOne.setValid(true);
        selectOne.setSubmittedValue("foo");
        selectOne.validate(facesContext);
        checkMessages(0);
        assertTrue(selectOne.isValid());

        selectOne.setValid(true);
        selectOne.setSubmittedValue("");
        selectOne.validate(facesContext);
        checkMessages(1);
        assertTrue(!selectOne.isValid());

        selectOne.setValid(true);
        selectOne.setSubmittedValue(null);
        // awiner: see UIInputTestCase
        selectOne.validate(facesContext);
        checkMessages(1);
        assertTrue(selectOne.isValid());
    }

    @Test
    public void testSelectItemsIterator() {
        // sub test 1 : non-selectItem at end
        UISelectOne selectOne = (UISelectOne) component;
        selectOne.getChildren().add(new UISelectItemSub("orr", null, null));
        selectOne.getChildren().add(new UISelectItemSub("esposito", null, null));
        UIParameter param = new UIParameter();
        param.setName("param");
        param.setValue("paramValue");
        selectOne.getChildren().add(param);
        Iterator<SelectItem> iter = new SelectItemsIterator(facesContext, selectOne);
        while (iter.hasNext()) {
            SelectItem selectItem = iter.next();
            assertTrue(selectItem.getValue().equals("orr") || selectItem.getValue().equals("esposito"));
        }

        // sub test 2: non-selectitem in middle
        selectOne = new UISelectOne();
        selectOne.getChildren().add(new UISelectItemSub("gretsky", null, null));
        selectOne.getChildren().add(param);
        selectOne.getChildren().add(new UISelectItemSub("howe", null, null));
        iter = new SelectItemsIterator(facesContext, selectOne);
        while (iter.hasNext()) {
            SelectItem selectItem = iter.next();
            assertTrue(selectItem.getValue().equals("gretsky") || selectItem.getValue().equals("howe"));
        }
    }

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UISelectOne();
        component.setRendererType(null);
        return component;
    }

    @Override
    protected void setupNewValue(UIInput input) {
        input.setSubmittedValue("foo");
        UISelectItem si = new UISelectItem();
        si.setItemValue("foo");
        si.setItemLabel("foo label");
        input.getChildren().add(si);
    }

    // Create an options list with nested groups
    protected List<SelectItem> setupOptions() {
        SelectItemGroup group, subgroup;
        subgroup = new SelectItemGroup("Group C");
        subgroup.setSelectItems(new SelectItem[] { new SelectItem("C1"), new SelectItem("C2"), new SelectItem("C3") });
        List<SelectItem> options = new ArrayList<>();
        options.add(new SelectItem("A1"));
        group = new SelectItemGroup("Group B");
        group.setSelectItems(new SelectItem[] { new SelectItem("B1"), subgroup, new SelectItem("B2"), new SelectItem("B3") });
        options.add(group);
        options.add(new SelectItem("A2"));
        options.add(new SelectItem("A3"));
        return options;
    }
}
