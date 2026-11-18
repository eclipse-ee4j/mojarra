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
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItemGroups;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.html.HtmlSelectManyCheckbox;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

import com.sun.faces.renderkit.Attributes;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.SelectItemsIterator;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.ScopedRunner;

/**
 * <B>SelectManyCheckboxListRenderer</B> is a class that renders the current value of <code>UISelectMany</code> component
 * as a list of checkboxes.
 */

public class SelectManyCheckboxListRenderer extends MenuRenderer {

    private static final Attributes ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.SELECTMANYCHECKBOX);

    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        Object borderObj;
        Boolean newTableRow = getNewTableRow(component);
        int border = 0;

        if (null != (borderObj = RenderKitUtils.getAttributeIfSet(component, "border"))) {
            border = (Integer) borderObj;
        }

        Converter<?> converter = component instanceof ValueHolder ? ((ValueHolder) component).getConverter() : null;

        renderBeginText(component, border, newTableRow, context, true);

        SelectItemsIterator<SelectItem> items = RenderKitUtils.getSelectItems(context, component);

        Object currentSelections = getCurrentSelectedValues(component);
        Object[] submittedValues = getSubmittedSelectedValues(component);
        OptionComponentInfo optionInfo = new OptionComponentInfo(component);

        try {
            renderItems(context, component, items, converter, currentSelections, submittedValues, newTableRow, optionInfo);
        } finally {
            RenderKitUtils.setCombinedPassThroughAttributes(component, null);
        }

        renderEndText(component, newTableRow, context);

    }

    // ------------------------------------------------------- Protected Methods

    /**
     * Returns the component to start the inputs of the given item component with, so that each input carries the
     * pass-through attributes of the given select component and those of the item component, which take precedence for
     * the same name. When both have them, it is the select component, which then carries the combined attributes until
     * the next call of this method for it, or until {@link RenderKitUtils#setCombinedPassThroughAttributes(UIComponent,
     * Map)} removes them. An item component which is not a <code>UISelectItem</code> or <code>UISelectItems</code>
     * contributes none, and neither does a <code>UISelectItemGroups</code>, whose pass-through attributes belong to the
     * nested element of each group.
     */
    protected static UIComponent prepareInputComponent(UIComponent component, UIComponent itemComponent) {
        boolean isItemComponent = itemComponent instanceof UISelectItem
                || itemComponent instanceof UISelectItems && !(itemComponent instanceof UISelectItemGroups);
        Map<String, Object> itemAttributes = isItemComponent ? itemComponent.getPassThroughAttributes(false) : null;
        Map<String, Object> attributes = component.getPassThroughAttributes(false);
        Map<String, Object> combinedAttributes = null;
        UIComponent inputComponent = component;

        if (itemAttributes != null && !itemAttributes.isEmpty()) {
            if (attributes == null || attributes.isEmpty()) {
                inputComponent = itemComponent;
            } else {
                combinedAttributes = new LinkedHashMap<>(attributes);
                combinedAttributes.putAll(itemAttributes);
            }
        }

        RenderKitUtils.setCombinedPassThroughAttributes(component, combinedAttributes);
        return inputComponent;
    }

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

    /**
     * Starts the table or list with the pass-through attributes of the given component: the select component for the
     * outer element, or the component which produced the group for the nested element of a group.
     */
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
            writeStyleClassAttributeIfNecessary(writer, component);
            writeStyleAttributeIfNecessary(writer, component);
        }
        writer.writeText("\n", component, null);

        if (newTableRow == FALSE) {
            writer.writeText("\t", component, null);
            writer.startElement("tr", null);
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

    /**
     * Renders the input and label of the given item. The input is started with the given input component, whose
     * pass-through attributes it carries.
     */
    protected void renderOption(FacesContext context, UIComponent component, UIComponent inputComponent, Converter converter, SelectItem curItem,
            Object currentSelections, Object[] submittedValues, Boolean newTableRow, int itemNumber, OptionComponentInfo optionInfo) throws IOException {

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
            writer.startElement("tr", null);
            writer.writeText("\n", component, null);
        }
        writer.startElement(newTableRow != null ? "td" : "li", null);
        writer.writeText("\n", component, null);

        writer.startElement("input", inputComponent);
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
        RenderKitUtils.renderPassThruAttributes(context, writer, component, idString, false, ATTRIBUTES, "click", "valueChange");
        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

        writer.endElement("input");

        RenderKitUtils.flushPendingBehaviorEventListeners(context, component, idString);

        writer.startElement("label", null);
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

    // --------------------------------------------------------- Private Methods

    /**
     * Returns <code>null</code> for the list layout, <code>true</code> for the page direction layout, else
     * <code>false</code>.
     */
    private static Boolean getNewTableRow(UIComponent component) {
        String layout = component instanceof HtmlSelectManyCheckbox checkbox ? checkbox.getLayout()
                : (String) component.getAttributes().get("layout");

        if (layout == null) {
            return false;
        }

        return layout.equalsIgnoreCase("list") ? null : layout.equalsIgnoreCase("pageDirection");
    }

    /**
     * Renders the input of each item, or the group with all items in it, with the <code>var</code> of the component
     * which produced the item bound to its current element.
     */
    private void renderItems(FacesContext context, UIComponent component, SelectItemsIterator<SelectItem> items, Converter<?> converter,
            Object currentSelections, Object[] submittedValues, Boolean newTableRow, OptionComponentInfo optionInfo) throws IOException {

        UIComponent previousSelectComponent = null;
        UIComponent inputComponent = component;
        String var = null;
        int idx = -1;
        while (items.hasNext()) {
            SelectItem curItem = items.next();
            UIComponent selectComponent = items.currentSelectComponent();

            if (selectComponent != previousSelectComponent) {
                previousSelectComponent = selectComponent;
                inputComponent = prepareInputComponent(component, selectComponent);
                var = RenderKitUtils.getPassThroughVar(selectComponent);
            }

            int itemNumber = ++idx;
            UIComponent itemInputComponent = inputComponent;

            if (var != null) {
                idx = new ScopedRunner(context).with(var, items.currentElement()).invokeThrowing(() -> renderItem(context, component, selectComponent,
                        itemInputComponent, converter, curItem, currentSelections, submittedValues, newTableRow, itemNumber, optionInfo));
            } else {
                idx = renderItem(context, component, selectComponent, itemInputComponent, converter, curItem, currentSelections, submittedValues,
                        newTableRow, itemNumber, optionInfo);
            }
        }
    }

    /**
     * Renders the given item, or the group with all items in it, and returns the item number after which the next item
     * is numbered.
     */
    private int renderItem(FacesContext context, UIComponent component, UIComponent selectComponent, UIComponent inputComponent, Converter<?> converter,
            SelectItem curItem, Object currentSelections, Object[] submittedValues, Boolean newTableRow, int itemNumber, OptionComponentInfo optionInfo)
            throws IOException {

        if (curItem instanceof SelectItemGroup) {
            return renderGroup(context, component, selectComponent, inputComponent, converter, (SelectItemGroup) curItem, currentSelections, submittedValues,
                    newTableRow, itemNumber, optionInfo);
        }

        renderOption(context, component, inputComponent, converter, curItem, currentSelections, submittedValues, newTableRow, itemNumber, optionInfo);
        return itemNumber;
    }

    /**
     * Renders the label of the given group and a nested table or list with its items. The nested element carries the
     * pass-through attributes of the given select component, which produced the group, and each input those of the
     * given input component.
     */
    private int renderGroup(FacesContext context, UIComponent component, UIComponent selectComponent, UIComponent inputComponent, Converter<?> converter,
            SelectItemGroup group, Object currentSelections, Object[] submittedValues, Boolean newTableRow, int itemNumber, OptionComponentInfo optionInfo)
            throws IOException {

        ResponseWriter writer = context.getResponseWriter();

        if (group.getLabel() != null) {
            if (newTableRow == TRUE) {
                writer.startElement("tr", null);
            }
            writer.startElement(newTableRow != null ? "td" : "li", null);
            writer.writeText(group.getLabel(), component, "label");
            if (newTableRow != null) {
                writer.endElement("td");
                if (newTableRow) {
                    writer.endElement("tr");
                }
            }
        }
        if (newTableRow != null) {
            if (newTableRow) {
                writer.startElement("tr", null);
            }
            writer.startElement("td", null);
        }
        writer.writeText("\n", component, null);
        renderBeginText(selectComponent, 0, newTableRow, context, false);

        int idx = itemNumber;

        for (SelectItem item : group.getSelectItems()) {
            renderOption(context, component, inputComponent, converter, item, currentSelections, submittedValues, newTableRow, idx++, optionInfo);
        }

        renderEndText(component, newTableRow, context);
        writer.endElement(newTableRow != null ? "td" : "li");
        if (newTableRow == TRUE) {
            writer.endElement("tr");
            writer.writeText("\n", component, null);
        }

        return idx;
    }

} // end of class SelectManyCheckboxListRenderer
