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

// SelectManyCheckboxListRenderer.java

package com.sun.faces.renderkit.html_basic;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.io.IOException;
import java.util.Iterator;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.RequestStateManager;

/**
 * <B>SelectManyCheckboxListRenderer</B> is a class that renders the current value of <code>UISelectMany</code> component
 * as a list of checkboxes.
 */

public class SelectManyCheckboxListRenderer extends MenuRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.SELECTMANYCHECKBOX);

    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        String alignStr;
        Object borderObj;
        Boolean newTableRow = false;
        int border = 0;

        if (null != (alignStr = (String) component.getAttributes().get("layout"))) {
            if (alignStr.equalsIgnoreCase("list")) {
                newTableRow = null;
            }
            else {
                newTableRow = alignStr.equalsIgnoreCase("pageDirection");
            }
        }
        if (null != (borderObj = component.getAttributes().get("border"))) {
            border = (Integer) borderObj;
        }

        Converter converter = null;
        if (component instanceof ValueHolder) {
            converter = ((ValueHolder) component).getConverter();
        }

        renderBeginText(component, border, newTableRow, context, true);

        Iterator<SelectItem> items = RenderKitUtils.getSelectItems(context, component);

        Object currentSelections = getCurrentSelectedValues(component);
        Object[] submittedValues = getSubmittedSelectedValues(component);
        OptionComponentInfo optionInfo = new OptionComponentInfo(component);
        int idx = -1;
        while (items.hasNext()) {
            SelectItem curItem = items.next();
            idx++;
            // If we come across a group of options, render them as a nested
            // table.
            if (curItem instanceof SelectItemGroup) {
                // write out the label for the group.
                if (curItem.getLabel() != null) {
                    if (newTableRow == TRUE) {
                        writer.startElement("tr", component);
                    }
                    writer.startElement(newTableRow != null ? "td" : "li", component);
                    writer.writeText(curItem.getLabel(), component, "label");
                    if (newTableRow != null) {
                        writer.endElement("td");
                        if (newTableRow) {
                            writer.endElement("tr");
                        }
                    }

                }
                if (newTableRow != null) {
                    if (newTableRow) {
                        writer.startElement("tr", component);
                    }
                    writer.startElement("td", component);
                }
                writer.writeText("\n", component, null);
                renderBeginText(component, 0, newTableRow, context, false);
                // render options of this group.
                SelectItem[] itemsArray = ((SelectItemGroup) curItem).getSelectItems();
                for (SelectItem element : itemsArray) {
                    renderOption(context, component, converter, element, currentSelections, submittedValues, newTableRow, idx++, optionInfo);
                }
                renderEndText(component, newTableRow, context);
                writer.endElement(newTableRow != null ? "td" : "li");
                if (newTableRow == TRUE) {
                    writer.endElement("tr");
                    writer.writeText("\n", component, null);
                }
            } else {
                renderOption(context, component, converter, curItem, currentSelections, submittedValues, newTableRow, idx, optionInfo);
            }
        }

        renderEndText(component, newTableRow, context);

    }

    // ------------------------------------------------------- Protected Methods

    /**
     * We override isBehaviorSource since the ID of the activated check box will have been augmented with the option number.
     *
     * @see HtmlBasicRenderer#isBehaviorSource(FacesContext, String, String)
     */
    @Override
    protected boolean isBehaviorSource(FacesContext ctx, String behaviorSourceId, String componentClientId) {

        if (behaviorSourceId == null) {
            return false;
        }
        char sepChar = UINamingContainer.getSeparatorChar(ctx);
        String actualBehaviorId;
        if (behaviorSourceId.lastIndexOf(sepChar) != -1) {
            actualBehaviorId = behaviorSourceId.substring(0, behaviorSourceId.lastIndexOf(sepChar));
        } else {
            actualBehaviorId = behaviorSourceId;
        }

        return actualBehaviorId.equals(componentClientId);

    }

    protected void renderBeginText(UIComponent component, int border, Boolean newTableRow, FacesContext context, boolean outerElement) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        writer.startElement(newTableRow != null ? "table" : "ul", component);
        if (newTableRow != null && border != Integer.MIN_VALUE) {
            writer.writeAttribute("border", border, "border");
        }

        // render style and styleclass attribute on the outer element instead of
        // rendering it as pass through attribute on every option in the list.
        if (outerElement) {
            // render "id" only for outerTable.
            if (shouldWriteIdAttribute(component)) {
                writeIdAttributeIfNecessary(context, writer, component);
            }
            String styleClass = (String) component.getAttributes().get("styleClass");
            String style = (String) component.getAttributes().get("style");
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, "class");
            }
            if (style != null) {
                writer.writeAttribute("style", style, "style");
            }
        }
        writer.writeText("\n", component, null);

        if (newTableRow == FALSE) {
            writer.writeText("\t", component, null);
            writer.startElement("tr", component);
            writer.writeText("\n", component, null);
        }

    }

    protected void renderEndText(UIComponent component, Boolean newTableRow, FacesContext context) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        if (newTableRow == FALSE) {
            writer.writeText("\t", component, null);
            writer.endElement("tr");
            writer.writeText("\n", component, null);
        }
        writer.endElement(newTableRow != null ? "table" : "ul");

    }

    protected void renderOption(FacesContext context, UIComponent component, Converter converter, SelectItem curItem, Object currentSelections,
            Object[] submittedValues, Boolean newTableRow, int itemNumber, OptionComponentInfo optionInfo) throws IOException {

        String valueString = getFormattedValue(context, component, curItem.getValue(), converter);

        Object valuesArray;
        Object itemValue;
        if (submittedValues != null) {
            valuesArray = submittedValues;
            itemValue = valueString;
        } else {
            valuesArray = currentSelections;
            itemValue = curItem.getValue();
        }

        RequestStateManager.set(context, RequestStateManager.TARGET_COMPONENT_ATTRIBUTE_NAME, component);

        boolean isSelected = isSelected(context, component, itemValue, valuesArray, converter);
        if (optionInfo.isHideNoSelection() && curItem.isNoSelectionOption() && currentSelections != null && !isSelected) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        if (newTableRow == TRUE) {
            writer.writeText("\t", component, null);
            writer.startElement("tr", component);
            writer.writeText("\n", component, null);
        }
        writer.startElement(newTableRow != null ? "td" : "li", component);
        writer.writeText("\n", component, null);

        writer.startElement("input", component);
        writer.writeAttribute("name", component.getClientId(context), "clientId");
        String idString = component.getClientId(context) + UINamingContainer.getSeparatorChar(context) + Integer.toString(itemNumber);
        writer.writeAttribute("id", idString, "id");

        writer.writeAttribute("value", valueString, "value");
        writer.writeAttribute("type", "checkbox", null);

        if (isSelected) {
            writer.writeAttribute(getSelectedTextString(), Boolean.TRUE, null);
        }

        // Don't render the disabled attribute twice if the 'parent'
        // component is already marked disabled.
        if (!optionInfo.isDisabled()) {
            if (curItem.isDisabled()) {
                writer.writeAttribute("disabled", true, "disabled");
            }
        }

        // Apply HTML 4.x attributes specified on UISelectMany component to all
        // items in the list except styleClass and style which are rendered as
        // attributes of outer most table.
        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES, getNonOnClickSelectBehaviors(component));

        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

        writer.endElement("input");

        RenderKitUtils.renderSelectOnclickEventListener(context, component, idString, false);

        writer.startElement("label", component);
        writer.writeAttribute("for", idString, "for");

        // Set up the label's class, if appropriate
        StringBuilder labelClass = new StringBuilder();
        String style;
        // If disabledClass or enabledClass set, add it to the label's class
        if (optionInfo.isDisabled() || curItem.isDisabled()) {
            style = optionInfo.getDisabledClass();
        } else { // enabled
            style = optionInfo.getEnabledClass();
        }
        if (style != null) {
            labelClass.append(style);
        }
        // If selectedClass or unselectedClass set, add it to the label's class
        if (isSelected(context, component, itemValue, valuesArray, converter)) {
            style = optionInfo.getSelectedClass();
        } else { // not selected
            style = optionInfo.getUnselectedClass();
        }
        if (style != null) {
            if (labelClass.length() > 0) {
                labelClass.append(' ');
            }
            labelClass.append(style);
        }
        writer.writeAttribute("class", labelClass.toString(), "labelClass");
        String itemLabel = curItem.getLabel();
        if (itemLabel == null) {
            itemLabel = valueString;
        }
        writer.writeText(" ", component, null);
        if (!curItem.isEscape()) {
            // It seems the ResponseWriter API should
            // have a writeText() with a boolean property
            // to determine if it content written should
            // be escaped or not.
            writer.write(itemLabel);
        } else {
            writer.writeText(itemLabel, component, "label");
        }

        writer.endElement("label");
        writer.endElement(newTableRow != null ? "td" : "li");
        writer.writeText("\n", component, null);
        if (newTableRow == TRUE) {
            writer.writeText("\t", component, null);
            writer.endElement("tr");
            writer.writeText("\n", component, null);
        }
    }

    // ------------------------------------------------- Package Private Methods

    String getSelectedTextString() {

        return "checked";

    }

} // end of class SelectManyCheckboxListRenderer
