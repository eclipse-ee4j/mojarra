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

// RadioRenderer.java

package com.sun.faces.renderkit.html_basic;

import static java.lang.Boolean.TRUE;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.model.SelectItem;

import com.sun.faces.RIConstants;
import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.SelectItemsIterator;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.Util;

/**
 * <B>ReadoRenderer</B> is a class that renders the current value of <code>UISelectOne</code> or <code>UISelectMany</code>
 * component as a list of radio buttons
 */
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class RadioRenderer extends SelectManyCheckboxListRenderer implements ComponentSystemEventListener {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.SELECTONERADIO);

    // -------------------------------------------------------------------------------------------------- Public Methods

    /**
     * After adding component to view, if component has group attribute set, then pre-collect the components by group.
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        UISelectOne radio = (UISelectOne) event.getComponent();
        Group group = getGroup(event.getFacesContext(), radio);

        if (group != null) {
            group.addRadio(event.getFacesContext(), radio);
        }
    }

    /**
     * This override delegates to <code>decodeGroup(FacesContext context, UISelectOne radio, Group group)</code> when 'group' attribute is set. It
     * will only decode when the current component is the first one of group.
     */
    @Override
    public void decode(FacesContext context, UIComponent component) {
        UISelectOne radio = (UISelectOne) component;
        Group group = getGroup(context, radio);

        if (group != null) {
            decodeGroup(context, radio, group);
        } else {
            super.decode(context, component); // Continue default decoding.
        }
    }

    /**
     * This override delegates to <code>encodeEndGroup(FacesContext context, UISelectOne radio, Group group)</code> when 'group' attribute is set.
     */
    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        UISelectOne radio = (UISelectOne) component;
        Group group = getGroup(context, radio);

        if (group != null) {
            encodeEndGroup(context, radio, group);
        } else {
            super.encodeEnd(context, component); // Continue default table rendering.
        }
    }

    // ------------------------------------------------------- Protected Methods

    /**
     * The difference with default decoding is:
     * <ul>
     *   <li>Submitted value is obtained by group name.
     *   <li>Submitted value is prefixed with client ID of radio button component, this need to be compared and trimmed.
     *   <li>If any submitted value does not belong to current radio button component, reset its value.
     * </ul>
     */
    protected void decodeGroup(FacesContext context, UISelectOne radio, Group group) {
        rendererParamsNotNull(context, radio);

        if (!shouldDecode(radio)) {
            return;
        }

        String clientId = decodeBehaviors(context, radio);

        if (clientId == null) {
            clientId = radio.getClientId(context);
        }

        assert clientId != null;
        Map<String, String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
        String newValue = requestParameterMap.get(group.getClientName());
        String prefix = clientId + UINamingContainer.getSeparatorChar(context);

        if (newValue != null) {
            if (newValue.startsWith(prefix)) {
                String submittedValue = newValue.substring(prefix.length());
                setSubmittedValue(radio, submittedValue);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("submitted value for UISelectOne group component " + radio.getId() + " after decoding " + submittedValue);
                }
            } else {
                radio.resetValue();
            }
        } else {
            // There is no submitted value at all, but this is different from a null value.
            radio.setSubmittedValue(RIConstants.NO_VALUE);
        }
    }

    /**
     * The difference with default encoding is:
     * 
     * <ol>
     *   <li>Every radio button of same 'group' will have same 'name' attribute rendered, relative to UIForm parent.
     *   <li>The 'value' attribute of every radio button is prefixed with client ID of radio button component itself.
     *   <li>No additional (table) markup is being rendered.
     *   <li>Label, if any, is rendered directly after radio button element, without additional markup.
     * </ol>
     */
    protected void encodeEndGroup(FacesContext context, UISelectOne radio, Group group) throws IOException {
        rendererParamsNotNull(context, radio);

        if (!shouldEncode(radio)) {
            return;
        }

        SelectItem currentItem = RenderKitUtils.getSelectItems(context, radio).next();
        String clientId = radio.getClientId(context);
        Object itemValue = currentItem.getValue();
        Converter<?> converter = radio.getConverter();
        boolean checked = isChecked(context, radio, itemValue);
        boolean disabled = Util.componentIsDisabled(radio);

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        renderInput(context, writer, radio, clientId, itemValue, converter, checked, disabled, group);

        if (currentItem.getLabel() != null) {
            renderLabel(writer, radio, clientId, currentItem, new OptionComponentInfo(radio));
        }
    }

    protected boolean isChecked(FacesContext context, UISelectOne radio, Object itemValue) {
        Object currentValue = radio.getSubmittedValue();

        if (currentValue == null) {
            currentValue = radio.getValue();
        }

        Class<?> type = String.class;

        if (currentValue != null) {
            type = currentValue.getClass();

            if (type.isArray()) {
                currentValue = ((Object[]) currentValue)[0];

                if (null != currentValue) {
                    type = currentValue.getClass();
                }
            } else if (Collection.class.isAssignableFrom(type)) {
                Iterator<?> valueIter = ((Collection<?>) currentValue).iterator();

                if (null != valueIter && valueIter.hasNext()) {
                    currentValue = valueIter.next();

                    if (null != currentValue) {
                        type = currentValue.getClass();
                    }
                }
            }
        }

        RequestStateManager.set(context, RequestStateManager.TARGET_COMPONENT_ATTRIBUTE_NAME, radio);
        Object newValue;

        try {
            newValue = context.getApplication().getExpressionFactory().coerceToType(itemValue, type);
        } catch (ELException | IllegalArgumentException e) {
            // If coerceToType fails, per the docs it should throw an ELException, however, SJAS 9.0 and 9.0u1 will
            // throw an IllegalArgumentException instead (see https://java.net/jira/browse/GLASSFISH-1527).
            newValue = itemValue;
        }

        return newValue != null && newValue.equals(currentValue);
    }

    @Override
    protected void renderOption(FacesContext context, UIComponent component, Converter converter, SelectItem curItem, Object currentSelections,
            Object[] submittedValues, Boolean newTableRow, int itemNumber, OptionComponentInfo optionInfo) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        UISelectOne selectOne = (UISelectOne) component;
        Object curValue = curItem.getValue();
        boolean checked = isChecked(context, selectOne, curValue);

        if (optionInfo.isHideNoSelection() && curItem.isNoSelectionOption() && curValue != null && !checked) {
            return;
        }

        if (newTableRow == TRUE) {
            writer.writeText("\t", component, null);
            writer.startElement("tr", component);
            writer.writeText("\n", component, null);
        }

        writer.startElement(newTableRow != null ? "td" : "li", component);
        writer.writeText("\n", component, null);

        String clientId = component.getClientId(context) + UINamingContainer.getSeparatorChar(context) + Integer.toString(itemNumber);

        // Don't render the disabled attribute twice if the 'parent' component is already marked disabled.
        boolean disabled = !optionInfo.isDisabled() && curItem.isDisabled();

        renderInput(context, writer, component, clientId, curValue, converter, checked, disabled, null);
        renderLabel(writer, component, clientId, curItem, optionInfo);

        writer.endElement(newTableRow != null ? "td" : "li");
        writer.writeText("\n", component, null);
        if (newTableRow == TRUE) {
            writer.writeText("\t", component, null);
            writer.endElement("tr");
            writer.writeText("\n", component, null);
        }
    }

    protected void renderInput(FacesContext context, ResponseWriter writer, UIComponent component, String clientId, Object itemValue, Converter<?> converter,
            boolean checked, boolean disabled, Group group) throws IOException {
        writer.startElement("input", component);
        writer.writeAttribute("type", "radio", "type");

        if (checked) {
            writer.writeAttribute("checked", Boolean.TRUE, null);
        }

        Object value = getFormattedValue(context, component, itemValue, converter);

        if (group == null) {
            writer.writeAttribute("name", component.getClientId(context), "clientId");
            writer.writeAttribute("id", clientId, "id");
            writer.writeAttribute("value", value, "value");
        } else {
            writer.writeAttribute("name", group.getClientName(), "group");
            writer.writeAttribute("id", clientId, "id");
            writer.writeAttribute("value", clientId + UINamingContainer.getSeparatorChar(context) + value, "value");

            String styleClass = (String) component.getAttributes().get("styleClass");
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, "class");
            }

            String style = (String) component.getAttributes().get("style");
            if (style != null) {
                writer.writeAttribute("style", style, "style");
            }
        }

        if (disabled) {
            writer.writeAttribute("disabled", true, "disabled");
        }
        // Apply HTML 4.x attributes specified on UISelectMany component to all
        // items in the list except styleClass and style which are rendered as
        // attributes of outer most table.
        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES, getNonOnClickSelectBehaviors(component));
        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);

        writer.endElement("input");

        RenderKitUtils.renderSelectOnclickEventListener(context, component, clientId, false);
    }

    protected void renderLabel(ResponseWriter writer, UIComponent component, String forClientId, SelectItem curItem, OptionComponentInfo optionInfo)
            throws IOException {
        String labelClass;
        if (optionInfo.isDisabled() || curItem.isDisabled()) {
            labelClass = optionInfo.getDisabledClass();
        } else {
            labelClass = optionInfo.getEnabledClass();
        }

        writer.startElement("label", component);
        writer.writeAttribute("for", forClientId, "for");
        // if enabledClass or disabledClass attributes are specified, apply
        // it on the label.
        if (labelClass != null) {
            writer.writeAttribute("class", labelClass, "labelClass");
        }
        String itemLabel = curItem.getLabel();
        if (itemLabel != null) {
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
        }
        writer.endElement("label");
    }

    protected static Group getGroup(FacesContext context, UISelectOne radio) {
        String groupName = radio.getGroup();

        if (groupName == null) {
            return null;
        }

        UIComponent groupContainer = RenderKitUtils.getForm(radio, context);

        if (groupContainer == null) {
            groupContainer = context.getViewRoot();
        }

        String clientName = groupContainer.getClientId(context) + UINamingContainer.getSeparatorChar(context) + groupName;
        Map<String, Group> radioButtonGroups = RequestStateManager.get(context, RequestStateManager.PROCESSED_RADIO_BUTTON_GROUPS);

        if (radioButtonGroups == null) {
            radioButtonGroups = new HashMap<>();
            RequestStateManager.set(context, RequestStateManager.PROCESSED_RADIO_BUTTON_GROUPS, radioButtonGroups);
        }

        Group group = radioButtonGroups.get(clientName);

        if (group == null) {
            group = new Group(context, clientName);
            radioButtonGroups.put(clientName, group);
        }

        return group;
    }

    /**
     * Keeps track of all <code>&lt;h:selectOneRadio group&gt;</code> detail.
     */
    protected static class Group {

        private final String clientName;
        private final List<String> clientIds;
        private ValueExpression value;

        public Group(FacesContext context, String clientName) {
            this.clientName = clientName;
            clientIds = new ArrayList<>();
        }

        public String getClientName() {
            return clientName;
        }

        public void addRadio(FacesContext context, UISelectOne radio) {
            String clientId = radio.getClientId(context);

            if (!clientIds.contains(clientId)) {
                if (clientIds.isEmpty()) {
                    value = radio.getValueExpression("value");
                } else if (radio.getValueExpression("value") == null) {
                    radio.setValueExpression("value", value);
                }

                if (!RenderKitUtils.getSelectItems(context, radio).hasNext()) {
                    radio.getChildren().add(new GroupSelectItem());
                }

                clientIds.add(clientId);
                radio.getAttributes().put(GroupSelectItem.class.getName(), Collections.unmodifiableList(clientIds));
            }
        }

    }

    /**
     * Used when a <code>&lt;h:selectOneRadio group&gt;</code> doesn't have a select item; it will then get it via first radio of the group.
     */
    public static class GroupSelectItem extends UISelectItem {

        private SelectItem selectItem;

        @SuppressWarnings("unchecked")
        private SelectItem getSelectItem() {
            if (selectItem == null) {
                FacesContext context = getFacesContext();
                UISelectOne radio = (UISelectOne) getParent();
                List<String> groupClientIds = (List<String>) radio.getAttributes().get(GroupSelectItem.class.getName());
                UIComponent firstRadioOfGroup = context.getViewRoot().findComponent(groupClientIds.get(0));
                SelectItemsIterator<SelectItem> iterator = RenderKitUtils.getSelectItems(context, firstRadioOfGroup);
                int index = groupClientIds.indexOf(radio.getClientId(context));

                while (index-- > 0 && iterator.hasNext()) {
                    iterator.next();
                }

                if (!iterator.hasNext()) {
                    throw new IllegalStateException(MessageFormat.format("UISelectOne component id=\"{0}\" group=\"{1}\" has no UISelectItem", radio.getId(), radio.getGroup()));
                }

                selectItem = iterator.next();
            }

            return selectItem;
        }

        @Override
        public Object getItemValue() {
            return getSelectItem().getValue();
        }

        @Override
        public String getItemLabel() {
            return getSelectItem().getLabel();
        }

        @Override
        public String getItemDescription() {
            return getSelectItem().getDescription();
        }

        @Override
        public boolean isItemEscaped() {
            return getSelectItem().isEscape();
        }

        @Override
        public boolean isNoSelectionOption() {
            return getSelectItem().isNoSelectionOption();
        }

        @Override
        public boolean isItemDisabled() {
            return getSelectItem().isDisabled();
        }
    }

}
