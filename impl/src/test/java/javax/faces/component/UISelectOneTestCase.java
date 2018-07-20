/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link UISelectOne}.</p>
 */
public class UISelectOneTestCase extends UIInputTestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public UISelectOneTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        super.setUp();
        component = new UISelectOne();
        expectedFamily = UISelectOne.COMPONENT_FAMILY;
        expectedRendererType = "javax.faces.Menu";
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(UISelectOneTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    // Test a pristine UISelectOne instance
    @Override
    public void testPristine() {
        super.testPristine();
        UISelectOne selectOne = (UISelectOne) component;
    }

    // Test setting properties to invalid values
    @Override
    public void testPropertiesInvalid() throws Exception {
        super.testPropertiesInvalid();
        UISelectOne selectOne = (UISelectOne) component;
    }

    // Test setting properties to valid values
    @Override
    public void testPropertiesValid() throws Exception {
        super.testPropertiesValid();
        UISelectOne selectOne = (UISelectOne) component;
    }

    // Test validation of value against the valid list
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
        Iterator messages = facesContext.getMessages();
        while (messages.hasNext()) {
            FacesMessage message = (FacesMessage) messages.next();
            assertTrue(message.getSummary().indexOf("mylabel") >= 0);
        }
    }

    // Test validation of component with UISelectItems pointing to map
    public void testValidation2() throws Exception {
        // Put our component under test in a tree under a UIViewRoot
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        // Add valid options to the component under test
        Map map = new HashMap();
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
    public void testValidation3() throws Exception {
        Set<SelectItem> items = new HashSet<SelectItem>();
        items.add(new SelectItem("foo"));
        items.add(new SelectItem("bar"));
        items.add(new SelectItem("baz"));

        testValidateWithCollection(items, "bar", "car");
    }

    // Test validation of component with UISelectItems pointing to List
    public void testValidation4() throws Exception {
        List<SelectItem> items = new ArrayList<SelectItem>();
        items.add(new SelectItem("foo"));
        items.add(new SelectItem("bar"));
        items.add(new SelectItem("baz"));

        testValidateWithCollection(items, "bar", "car");
    }

    // Test validation of component with UISelectItems pointing to an Array
    public void testValidation5() throws Exception {
        // Put our component under test in a tree under a UIViewRoot
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);

        // Add valid options to the component under test
        SelectItem[] itemsArray = {
            new SelectItem("foo"),
            new SelectItem("bar"),
            new SelectItem("baz")
        };
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

    private void testValidateWithCollection(Collection<SelectItem> selectItems,
            String validValue,
            String invalidValue)
            throws Exception {
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

    private String legalValues[]
            = {"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"};

    private String illegalValues[]
            = {"D1", "D2", "Group A", "Group B", "Group C"};

    // Test validation against a nested list of available options
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
            assertTrue("Value '" + legalValues[i] + "' found",
                    selectOne.isValid());
            checkMessages(0);
        }

        // Verify that illegal values will not validate
        for (int i = 0; i < illegalValues.length; i++) {
            selectOne.setValid(true);
            selectOne.setSubmittedValue(illegalValues[i]);
            selectOne.validate(facesContext);
            assertTrue("Value '" + illegalValues[i] + "' not found",
                    !selectOne.isValid());
            checkMessages(i + 1);
        }
    }

    // Test validation of a required field
    @Override
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

    public void testSelectItemsIterator() {
        // sub test 1 : non-selectItem at end
        UISelectOne selectOne = (UISelectOne) component;
        selectOne.getChildren().add(new UISelectItemSub("orr", null, null));
        selectOne.getChildren().add(new UISelectItemSub("esposito", null, null));
        UIParameter param = new UIParameter();
        param.setName("param");
        param.setValue("paramValue");
        selectOne.getChildren().add(param);
        Iterator iter = new SelectItemsIterator(facesContext, selectOne);
        while (iter.hasNext()) {
            Object object = iter.next();
            assertTrue(object instanceof javax.faces.model.SelectItem);
            assertTrue((((SelectItem) object).getValue().equals("orr"))
                    || (((SelectItem) object).getValue().equals("esposito")));
        }

        // sub test 2: non-selectitem in middle
        selectOne = new UISelectOne();
        selectOne.getChildren().add(new UISelectItemSub("gretsky", null, null));
        selectOne.getChildren().add(param);
        selectOne.getChildren().add(new UISelectItemSub("howe", null, null));
        iter = new SelectItemsIterator(facesContext, selectOne);
        while (iter.hasNext()) {
            Object object = iter.next();
            assertTrue(object instanceof javax.faces.model.SelectItem);
            assertTrue((((SelectItem) object).getValue().equals("gretsky"))
                    || (((SelectItem) object).getValue().equals("howe")));
        }
    }

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UISelectOne();
        component.setRendererType(null);
        return (component);
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
    protected List setupOptions() {
        SelectItemGroup group, subgroup;
        subgroup = new SelectItemGroup("Group C");
        subgroup.setSelectItems(new SelectItem[]{new SelectItem("C1"),
            new SelectItem("C2"),
            new SelectItem("C3")});
        List options = new ArrayList();
        options.add(new SelectItem("A1"));
        group = new SelectItemGroup("Group B");
        group.setSelectItems(new SelectItem[]{new SelectItem("B1"),
            subgroup,
            new SelectItem("B2"),
            new SelectItem("B3")});
        options.add(group);
        options.add(new SelectItem("A2"));
        options.add(new SelectItem("A3"));
        return (options);
    }
}
