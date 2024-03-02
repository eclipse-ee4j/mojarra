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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.renderkit.SelectItemsIterator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

/**
 * <p>
 * Unit tests for {@link UISelectMany}.
 * </p>
 */
public class UISelectManyTestCase extends UIInputTestCase {

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        component = new UISelectMany();
        expectedFamily = UISelectMany.COMPONENT_FAMILY;
        expectedRendererType = "jakarta.faces.Listbox";
    }

    // ------------------------------------------------- Individual Test Methods
    // Test the compareValues() method
    @Override
    @Test
    public void testCompareValues() {
        SelectManyTestImpl selectMany = new SelectManyTestImpl();
        Object values1a[] = new Object[] { "foo", "bar", "baz" };
        Object values1b[] = new Object[] { "foo", "baz", "bar" };
        Object values1c[] = new Object[] { "baz", "foo", "bar" };
        Object values2[] = new Object[] { "foo", "bar" };
        Object values3[] = new Object[] { "foo", "bar", "baz", "bop" };
        Object values4[] = null;

        assertTrue(!selectMany.compareValues(values1a, values1a));
        assertTrue(!selectMany.compareValues(values1a, values1b));
        assertTrue(!selectMany.compareValues(values1a, values1c));
        assertTrue(!selectMany.compareValues(values2, values2));
        assertTrue(!selectMany.compareValues(values3, values3));
        assertTrue(!selectMany.compareValues(values4, values4));

        assertTrue(selectMany.compareValues(values1a, values2));
        assertTrue(selectMany.compareValues(values1a, values3));
        assertTrue(selectMany.compareValues(values1a, values4));
        assertTrue(selectMany.compareValues(values2, values3));
        assertTrue(selectMany.compareValues(values2, values4));
        assertTrue(selectMany.compareValues(values4, values1a));
        assertTrue(selectMany.compareValues(values4, values2));
        assertTrue(selectMany.compareValues(values4, values3));
    }

    // Test a pristine UISelectMany instance
    @Override
    @Test
    public void testPristine() {
        super.testPristine();
        UISelectMany selectMany = (UISelectMany) component;

        assertNull(selectMany.getSelectedValues());
    }

    // Test setting properties to invalid values
    @Override
    @Test
    public void testPropertiesInvalid() throws Exception {
        super.testPropertiesInvalid();
    }

    // Test validation of value against the valid list
    @Test
    public void testValidation() throws Exception {
        // Put our component under test in a tree under a UIViewRoot
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        // Add valid options to the component under test
        UISelectMany selectMany = (UISelectMany) component;
        selectMany.getChildren().add(new UISelectItemSub("foo", null, null));
        selectMany.getChildren().add(new UISelectItemSub("bar", null, null));
        selectMany.getChildren().add(new UISelectItemSub("baz", null, null));

        // Validate two values that are on the list
        selectMany.setValid(true);
        selectMany.setSubmittedValue(new Object[] { "foo", "baz" });
        selectMany.validate(facesContext);
        assertTrue(selectMany.isValid());

        // Validate one value on the list and one not on the list
        selectMany.getAttributes().put("label", "mylabel");
        selectMany.setValid(true);
        selectMany.setSubmittedValue(new Object[] { "bar", "bop" });
        selectMany.setRendererType(null); // We don't have any renderers
        selectMany.validate(facesContext);
        assertTrue(!selectMany.isValid());

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
        UISelectMany selectMany = (UISelectMany) component;
        selectMany.getChildren().add(items);

        // Validate two values that are on the list
        selectMany.setValid(true);
        selectMany.setSubmittedValue(new Object[] { "foo", "baz" });
        selectMany.validate(facesContext);
        assertTrue(selectMany.isValid());

        // Validate one value on the list and one not on the list
        selectMany.setValid(true);
        selectMany.setSubmittedValue(new Object[] { "bar", "bop" });
        selectMany.setRendererType(null); // We don't have any renderers
        selectMany.validate(facesContext);
        assertTrue(!selectMany.isValid());
    }

    // Test validation of component with UISelectItems pointing to Set and the
    // value of the component is Set
    @Test
    public void testValidation3() throws Exception {
        Set<SelectItem> items = new HashSet<>();
        items.add(new SelectItem("foo"));
        items.add(new SelectItem("bar"));
        items.add(new SelectItem("baz"));
        Set<String> submittedValues = new HashSet<>();
        submittedValues.add("bar");
        submittedValues.add("baz");
        Set<String> invalidValues = new HashSet<>();
        invalidValues.add("bar");
        invalidValues.add("car");
        testValidateWithCollection(items, submittedValues, invalidValues);
    }

    // Test validation of component with UISelectItems pointing to List
    @Test
    public void testValidation4() throws Exception {
        List<SelectItem> items = new ArrayList<>();
        items.add(new SelectItem("foo"));
        items.add(new SelectItem("bar"));
        items.add(new SelectItem("baz"));
        List<String> submittedValues = new ArrayList<>();
        submittedValues.add("bar");
        submittedValues.add("baz");
        ArrayList<String> invalidValues = new ArrayList<>();
        invalidValues.add("bar");
        invalidValues.add("car");
        testValidateWithCollection(items, submittedValues, invalidValues);
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
        UISelectMany selectMany = (UISelectMany) component;
        selectMany.getChildren().add(items);

        selectMany.setValid(true);
        selectMany.setSubmittedValue(new String[] { "bar", "baz" });
        selectMany.validate(facesContext);
        assertTrue(selectMany.isValid());

        // Validate one value on the list and one not on the list
        selectMany.setValid(true);
        selectMany.setSubmittedValue(new String[] { "bar", "car" });
        selectMany.setRendererType(null); // We don't have any renderers
        selectMany.validate(facesContext);
        assertTrue(!selectMany.isValid());
    }

    private void testValidateWithCollection(Collection<SelectItem> selectItems, Object validValues, Object invalidValues) throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        UISelectItems itemsComponent = new UISelectItems();
        itemsComponent.setValue(selectItems);
        UISelectMany selectMany = (UISelectMany) component;
        selectMany.setRendererType(null);
        selectMany.getChildren().add(itemsComponent);

        selectMany.setValue(true);
        selectMany.setSubmittedValue(validValues);
        selectMany.validate(facesContext);
        assertTrue(selectMany.isValid());
        selectMany.updateModel(facesContext);

        selectMany.setValid(true);
        selectMany.setSubmittedValue(invalidValues);
        selectMany.validate(facesContext);
        assertTrue(!selectMany.isValid());

    }

    private String legalValues[] = { "A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3" };

    private String illegalValues[] = { "D1", "D2", "Group A", "Group B", "Group C" };

    // Test validation against a nested list of available options
    @Test
    public void testValidateNested() throws Exception {
        // Set up UISelectMany with nested UISelectItems
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);
        UISelectMany selectMany = (UISelectMany) component;
        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(setupOptions());
        selectMany.getChildren().add(selectItems);
        selectMany.setRequired(true);
        checkMessages(0);

        // Verify that all legal values will validate
        for (int i = 0; i < legalValues.length; i++) {
            selectMany.setValid(true);
            selectMany.setSubmittedValue(new Object[] { legalValues[0], legalValues[i] });
            selectMany.validate(facesContext);
            assertTrue(selectMany.isValid(), "Value '" + legalValues[i] + "' found");
            checkMessages(0);
        }

        // Verify that illegal values will not validate
        for (int i = 0; i < illegalValues.length; i++) {
            selectMany.setValid(true);
            selectMany.setSubmittedValue(new Object[] { legalValues[0], illegalValues[i] });
            selectMany.validate(facesContext);
            assertTrue(!selectMany.isValid(), "Value '" + illegalValues[i] + "' not found");
            checkMessages(i + 1);
        }
    }

    // Test validation against a nested Set of available options
    @Test
    public void testValidateNestedSet() throws Exception {
        // Set up UISelectMany with nested UISelectItems
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);
        UISelectMany selectMany = (UISelectMany) component;
        UISelectItems selectItems = new UISelectItems();
        selectItems.setValue(setupOptionsSet());
        selectMany.getChildren().add(selectItems);
        selectMany.setRequired(true);
        checkMessages(0);

        // Verify that all legal values will validate
        for (int i = 0; i < legalValues.length; i++) {
            selectMany.setValid(true);
            selectMany.setSubmittedValue(new Object[] { legalValues[0], legalValues[i] });
            selectMany.validate(facesContext);
            assertTrue(selectMany.isValid(), "Value '" + legalValues[i] + "' found");
            checkMessages(0);
        }

        // Verify that illegal values will not validate
        for (int i = 0; i < illegalValues.length; i++) {
            selectMany.setValid(true);
            selectMany.setSubmittedValue(new Object[] { legalValues[0], illegalValues[i] });
            selectMany.validate(facesContext);
            assertTrue(!selectMany.isValid(), "Value '" + illegalValues[i] + "' not found");
            checkMessages(i + 1);
        }
    }

    // Test validation of a required field
    @Override
    @Test
    public void testValidateRequired() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);
        UISelectMany selectMany = (UISelectMany) component;
        selectMany.getChildren().add(new UISelectItemSub("foo", null, null));
        selectMany.getChildren().add(new UISelectItemSub("bar", null, null));
        selectMany.getChildren().add(new UISelectItemSub("baz", null, null));
        selectMany.setRequired(true);
        checkMessages(0);

        selectMany.setValid(true);
        selectMany.setSubmittedValue(new Object[] { "foo" });
        selectMany.validate(facesContext);
        checkMessages(0);
        assertTrue(selectMany.isValid());

        selectMany.setValid(true);
        selectMany.setSubmittedValue(new Object[] { "" });
        selectMany.validate(facesContext);
        checkMessages(1);
        assertTrue(!selectMany.isValid());

        selectMany.setValid(true);
        selectMany.setSubmittedValue(null);
        // this execution of validate shouldn't add any messages to the
        // queue, since a value of null means "don't validate". This is
        // different behavior than in previous versions of this
        // testcase, which expected the UISelectMany.validate() to
        // operate on the previously validated value, which is not
        // correct.
        selectMany.validate(facesContext);
        checkMessages(1);
        // since we're setting the submitted value to null, we don't
        // want validation to occurr, therefore, the valid state of the
        // componet should be as we left it.
        assertTrue(selectMany.isValid());
    }

    @Test
    public void testSelectItemsIterator() {
        // sub test 1: non-selectitem at end
        UISelectMany selectMany = (UISelectMany) component;
        selectMany.getChildren().add(new UISelectItemSub("orr", null, null));
        UIParameter param = new UIParameter();
        param.setName("param");
        param.setValue("paramValue");
        selectMany.getChildren().add(param);
        selectMany.getChildren().add(new UISelectItemSub("esposito", null, null));
        Iterator<SelectItem> iter = new SelectItemsIterator<>(facesContext, selectMany);
        while (iter.hasNext()) {
            Object object = iter.next();
            assertTrue(object instanceof jakarta.faces.model.SelectItem);
            assertTrue(((SelectItem) object).getValue().equals("orr") || ((SelectItem) object).getValue().equals("esposito"));
        }

        // sub test 2: non-selectitem in middle
        selectMany = new UISelectMany();
        selectMany.getChildren().add(new UISelectItemSub("gretsky", null, null));
        selectMany.getChildren().add(param);
        selectMany.getChildren().add(new UISelectItemSub("howe", null, null));
        iter = new SelectItemsIterator<>(facesContext, selectMany);
        while (iter.hasNext()) {
            Object object = iter.next();
            assertTrue(object instanceof jakarta.faces.model.SelectItem);
            assertTrue(((SelectItem) object).getValue().equals("gretsky") || ((SelectItem) object).getValue().equals("howe"));
        }

        // sub test 3: Empty collection
        selectMany = new UISelectMany();
        UISelectItems items = new UISelectItems();
        items.setValue(Collections.emptyList());
        selectMany.getChildren().add(items);
        iter = new SelectItemsIterator<>(facesContext, selectMany);
        assertTrue(!iter.hasNext());
        try {
            iter.next();
            assertTrue(false);
        } catch (NoSuchElementException nsee) {
            // expected
        }

        // sub test 4: items exposed as generic collection of non-SelectItem
        // instances
        Collection<Integer> cItems = new ArrayList<>(5);
        Collections.addAll(cItems, 0, 1, 2, 3, 4);
        selectMany = new UISelectMany();
        items = new UISelectItems();
        items.setValue(cItems);
        selectMany.getChildren().add(items);
        iter = new SelectItemsIterator<>(facesContext, selectMany);
        SelectItem previous = null;
        for (int i = 0, len = cItems.size(); i < len; i++) {
            assertTrue(iter.hasNext());
            SelectItem item = iter.next();
            assertNotNull(item);
            assertEquals(i, item.getValue());
            assertEquals(Integer.toString(i), item.getLabel());
            assertNull(item.getDescription());
            assertFalse(item.isDisabled());
            assertTrue(item.isEscape());
            if (previous != null) {
                // using fly-weight pattern make sure we use the same
                // instance through out the iteration
                assertTrue(item == previous);
            }
            previous = item;
        }
        assertFalse(iter.hasNext());
        try {
            iter.next();
            assertTrue(false);
        } catch (NoSuchElementException nsee) {
            // expected
        }

        // sub-test 5: DataModel providing the instances to produce
        // SelectItems from
        selectMany = new UISelectMany();
        items = new UISelectItems();
        items.setValue(new ListDataModel<>((List<Integer>) cItems));
        selectMany.getChildren().add(items);
        iter = new SelectItemsIterator<>(facesContext, selectMany);
        previous = null;
        for (int i = 0, len = cItems.size(); i < len; i++) {
            assertTrue(iter.hasNext());
            SelectItem item = iter.next();
            assertNotNull(item);
            assertEquals(i, item.getValue());
            assertEquals(Integer.toString(i), item.getLabel());
            assertNull(item.getDescription());
            assertFalse(item.isDisabled());
            assertTrue(item.isEscape());
            if (previous != null) {
                // using fly-weight pattern make sure we use the same
                // instance through out the iteration
                assertTrue(item == previous);
            }
            previous = item;
        }
        assertFalse(iter.hasNext());
        try {
            iter.next();
            assertTrue(false);
        } catch (NoSuchElementException nsee) {
            // expected
        }
    }

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UISelectMany();
        component.setRendererType(null);
        return component;
    }

    @Override
    protected void setupNewValue(UIInput input) {
        input.setSubmittedValue(new Object[] { "foo" });
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

    // Create an options list with nested groups
    protected Set<SelectItem> setupOptionsSet() {
        SelectItemGroup group, subgroup;
        subgroup = new SelectItemGroup("Group C");
        subgroup.setSelectItems(new SelectItem[] { new SelectItem("C1"), new SelectItem("C2"), new SelectItem("C3") });
        Set<SelectItem> options = new HashSet<>();
        options.add(new SelectItem("A1"));
        group = new SelectItemGroup("Group B");
        group.setSelectItems(new SelectItem[] { new SelectItem("B1"), subgroup, new SelectItem("B2"), new SelectItem("B3") });
        options.add(group);
        options.add(new SelectItem("A2"));
        options.add(new SelectItem("A3"));
        return options;
    }
}
