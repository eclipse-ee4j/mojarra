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

// TestUtil.java

package com.sun.faces.util;

import com.sun.faces.cactus.ServletFacesTestCase;
import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import javax.faces.FactoryFinder;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.component.UISelectMany;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.ResponseWriter;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.el.ValueExpression;
import javax.el.ExpressionFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.List;
import java.util.Arrays;

/**
 * Currently tests RenderKitUtils.
 */

public class TestUtil extends ServletFacesTestCase {


    // -------------------------------------------------------------- Test Setup


    public TestUtil() {
        super("TestUtil");
    }


    public TestUtil(String name) {
        super(name);
    }


    // ------------------------------------------------------------ Test Methods

    
    public void testRenderPassthruAttributes() {
        try {
            RenderKitFactory renderKitFactory = (RenderKitFactory)
                FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            RenderKit renderKit =
                renderKitFactory.getRenderKit(getFacesContext(),
                                              RenderKitFactory.HTML_BASIC_RENDER_KIT);
            StringWriter sw = new StringWriter();
            ResponseWriter writer = renderKit.createResponseWriter(sw,
                                                                   "text/html",
                                                                   "ISO-8859-1");
            getFacesContext().setResponseWriter(writer);
            Attribute[] attrs = AttributeManager.getAttributes(AttributeManager.Key.INPUTTEXT);
            UIInput input = new UIInput();
            input.setId("testRenderPassthruAttributes");
            input.getAttributes().put("notPresent", "notPresent");
            input.getAttributes().put("onblur", "javascript:f.blur()");
            writer.startElement("input", input);
            RenderKitUtils.renderPassThruAttributes(getFacesContext(),
                  writer,
                                                    input,
                                                    attrs);
            writer.endElement("input");
            String expectedResult = " onblur=\"javascript:f.blur()\"";
            assertTrue(sw.toString().contains(expectedResult));

            // verify no passthru attributes returns empty string
            sw = new StringWriter();
            writer =
                renderKit.createResponseWriter(sw, "text/html", "ISO-8859-1");
            getFacesContext().setResponseWriter(writer);
            input.getAttributes().remove("onblur");
            writer.startElement("input", input);
            RenderKitUtils.renderPassThruAttributes(getFacesContext(),writer, input, attrs);
            writer.endElement("input");
            assertTrue(sw.toString().equals("<input />"));
        } catch (IOException e) {
            assertTrue(false);
        }
    }


    public void testRenderPassthruAttributesFromConcreteHtmlComponent() {
        try {
            RenderKitFactory renderKitFactory = (RenderKitFactory)
                FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            RenderKit renderKit =
                renderKitFactory.getRenderKit(getFacesContext(),
                                              RenderKitFactory.HTML_BASIC_RENDER_KIT);
            StringWriter sw = new StringWriter();
            ResponseWriter writer = renderKit.createResponseWriter(sw,
                                                                   "text/html",
                                                                   "ISO-8859-1");
            getFacesContext().setResponseWriter(writer);
            Attribute[] attrs = AttributeManager.getAttributes(AttributeManager.Key.INPUTTEXT);
            HtmlInputText input = new HtmlInputText();
            input.setId("testRenderPassthruAttributes");
            input.setSize(12);
            writer.startElement("input", input);
            RenderKitUtils.renderPassThruAttributes(getFacesContext(),writer, input, attrs);
            writer.endElement("input");
            String expectedResult = " size=\"12\"";
            assertTrue(sw.toString().contains(expectedResult));

            // test that setting the values to the default value causes
            // the attributes to not be rendered.
            sw = new StringWriter();
            writer = renderKit.createResponseWriter(sw, "text/html",
                                                    "ISO-8859-1");
            input.setSize(Integer.MIN_VALUE);
            writer.startElement("input", input);
            RenderKitUtils.renderPassThruAttributes(getFacesContext(),writer, input, attrs);
            writer.endElement("input");
            expectedResult = "<input />";
            assertEquals(expectedResult, sw.toString());

            sw = new StringWriter();
            writer = renderKit.createResponseWriter(sw, "text/html",
                                                    "ISO-8859-1");
            input.setReadonly(false);
            writer.startElement("input", input);
            RenderKitUtils.renderPassThruAttributes(getFacesContext(),
                  writer,
                                                    input,
                                                    attrs);
            writer.endElement("input");
            assertEquals(expectedResult, sw.toString());


        } catch (IOException e) {
            assertTrue(false);
        }
    }


    public void testRenderBooleanPassthruAttributes() {
        try {
            RenderKitFactory renderKitFactory = (RenderKitFactory)
                FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            RenderKit renderKit =
                renderKitFactory.getRenderKit(getFacesContext(),
                                              RenderKitFactory.HTML_BASIC_RENDER_KIT);
            StringWriter sw = new StringWriter();
            ResponseWriter writer = renderKit.createResponseWriter(sw,
                                                                   "text/html",
                                                                   "ISO-8859-1");
            getFacesContext().setResponseWriter(writer);

            UIInput input = new UIInput();
            input.setId("testBooleanRenderPassthruAttributes");
            input.getAttributes().put("disabled", "true");
            input.getAttributes().put("readonly", "false");
            writer.startElement("input", input);
            RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, input);
            writer.endElement("input");
            String expectedResult = " disabled=\"disabled\"";
            assertTrue(sw.toString().contains(expectedResult));

            // verify no passthru attributes returns empty string
            sw = new StringWriter();
            writer =
                renderKit.createResponseWriter(sw, "text/html", "ISO-8859-1");
            getFacesContext().setResponseWriter(writer);
            input.getAttributes().remove("disabled");
            input.getAttributes().remove("readonly");
            writer.startElement("input", input);
            RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, input);
            writer.endElement("input");
            assertTrue("<input />".equals(sw.toString()));
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    public void testGetSelectItems() {
        SelectItem item1 = new SelectItem("value", "label");
        SelectItem item2 = new SelectItem("value2", "label2");
        SelectItem[] itemsArray = {
              item1, item2
        };
        Collection<SelectItem> itemsCollection = new ArrayList<SelectItem>(2);
        itemsCollection.add(item1);
        itemsCollection.add(item2);
        Map<String,String> selectItemMap = new LinkedHashMap<String,String>(2);
        selectItemMap.put("label", "value");
        selectItemMap.put("label2", "value2");

        // test arrays
        UISelectItems items = new UISelectItems();
        items.setValue(itemsArray);
        UISelectOne selectOne = new UISelectOne();
        selectOne.getChildren().add(items);
        Iterator iterator = RenderKitUtils.getSelectItems(getFacesContext(),
                                                          selectOne);
        assertTrue(item1.equals(iterator.next()));
        assertTrue(item2.equals(iterator.next()));

        items.setValue(itemsCollection);
        iterator = RenderKitUtils.getSelectItems(getFacesContext(),
                                                 selectOne);
        assertTrue(item1.equals(iterator.next()));
        assertTrue(item2.equals(iterator.next()));

        items.setValue(selectItemMap);
        iterator = RenderKitUtils.getSelectItems(getFacesContext(),
                                                 selectOne);
        SelectItem i = (SelectItem) iterator.next();
        assertTrue(item1.getLabel().equals(i.getLabel()) 
                    && item1.getValue().equals(i.getValue()));
        i = (SelectItem) iterator.next();
        assertTrue(item2.getLabel().equals(i.getLabel()) 
                    && item2.getValue().equals(i.getValue()));
    }


    public void testGetSelectItemsFromCollection() {

        UISelectMany many = new UISelectMany();
        UISelectItems items = new UISelectItems();
        FacesContext ctx = getFacesContext();
        items.getAttributes().put("var", "item");
        items.setValueExpression("itemValue",
                                 createValueExpression(ctx,
                                                       "#{item.id}",
                                                       Integer.TYPE));
        items.setValueExpression("itemLabel",
                                 createValueExpression(ctx,
                                                       "#{item.label}",
                                                       String.class));
        items.setValueExpression("itemDescription",
                                 createValueExpression(ctx,
                                                       "#{item.description}",
                                                       String.class));
        items.setValueExpression("itemLabelEscaped",
                                 createValueExpression(ctx,
                                                       "#{item.escaped}",
                                                       Boolean.TYPE));
        items.setValueExpression("itemDisabled",
                                 createValueExpression(ctx,
                                                       "#{item.disabled}",
                                                       Boolean.TYPE));
        many.getChildren().add(items);
        Collection<ModelObject> c = new TreeSet<ModelObject>();
        ModelObject[] models = {
            new ModelObject(1, "item1", "Item One", false, false),
            new ModelObject(2, "item2", "Item Two", true, false),
            new ModelObject(3, "item3", "Item Three", false, true)
        };

        c.addAll(Arrays.asList(models));

        items.setValue(c);


        Iterator<SelectItem> selectItems = RenderKitUtils.getSelectItems(ctx, many);
        int idx = 0;
        while (selectItems.hasNext()) {
            compare(models[idx], selectItems.next());
            idx++;
        }

    }

    // --------------------------------------------------------- Private Methods

    private void compare(ModelObject model, SelectItem item) {

        assertEquals(model.getId(), item.getValue());
        assertEquals(model.getLabel(), item.getLabel());
        assertEquals(model.getDescription(), item.getDescription());
        assertEquals(model.isEscaped(), item.isEscape());
        assertEquals(model.isDisabled(), item.isDisabled());

    }


    private ValueExpression createValueExpression(FacesContext ctx,
                                                  String expression,
                                                  Class<?> expectedType) {

        ExpressionFactory factory = ctx.getApplication().getExpressionFactory();
        return (factory.createValueExpression(ctx.getELContext(),
                                              expression,
                                              expectedType));
    }


    // ---------------------------------------------------------- Nested Classes


    public static class ModelObject implements Comparable {

        private int id;
        private String label;
        private String description;
        private boolean disabled;
        private boolean escaped;

        public ModelObject(int id,
                           String label,
                           String description,
                           boolean disabled,
                           boolean escaped) {

            this.id = id;
            this.label = label;
            this.description = description;
            this.disabled = disabled;
            this.escaped = escaped;

        }

        public int getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public boolean isEscaped() {
            return escaped;
        }


        // --------------------------------------------- Methods from Comparable


        public int compareTo(Object o) {
            ModelObject provided = (ModelObject) o;
            if (this.id < provided.id) {
                return -1;
            }
            if (this.id == provided.id) {
                return 0;
            }
            return 1;
        }
    }
   


} // end of class TestUtil
