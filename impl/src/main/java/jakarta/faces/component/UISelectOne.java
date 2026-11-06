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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <strong class="changed_modified_2_0">UISelectOne</strong> is a {@link UIComponent} that represents the user's choice
 * of zero or one items from among a discrete set of available options. The user can modify the selected value.
 * Optionally, the component can be preconfigured with a currently selected item, by storing it as the
 * <code>value</code> property of the component.
 * </p>
 *
 * <p>
 * This component is generally rendered as a select box or a group of radio buttons.
 * </p>
 *
 * <p>
 * By default, the <code>rendererType</code> property is set to "<code>jakarta.faces.Menu</code>". This value can be
 * changed by calling the <code>setRendererType()</code> method.
 * </p>
 */

public class UISelectOne extends UIInput {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.SelectOne";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.SelectOne";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if a value not matching
     * the available options is specified.
     */
    public static final String INVALID_MESSAGE_ID = "jakarta.faces.component.UISelectOne.INVALID";

    private static final String SUBMITTED_OR_VALIDATED_GROUPS = UISelectOne.class.getName() + ".SUBMITTED_OR_VALIDATED_GROUPS";

    enum PropertyKeys {

        /**
         * <p>
         * Specifies the name of the radio button group.
         *
         * @since 2.3
         */
        group

    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UISelectOne} instance with default property values.
     * </p>
     */
    public UISelectOne() {

        super();
        setRendererType("jakarta.faces.Menu");

    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {

        return COMPONENT_FAMILY;

    }

    /**
     * <p class="changed_added_2_3">
     * Returns the name of the radio button group.
     * <p>
     * Radio button components having the same group within a <code>UIForm</code> parent will uncheck all others when being
     * checked. If the <code>value</code> attribute is absent then the one from first component of the group will be used.
     * If the <code>UISelectItem</code> child is absent then the one from first component of the group will be used.
     *
     * @return The name of the radio button group.
     * @since 2.3
     */
    public String getGroup() {

        return (String) getStateHelper().eval(PropertyKeys.group);

    }

    /**
     * <p class="changed_added_2_3">
     * Sets the name of the radio button group.
     *
     * @param group The name of the radio button group.
     * @since 2.3
     */
    public void setGroup(String group) {

        getStateHelper().put(PropertyKeys.group, group);

    }

    // ----------------------------------------------------- UIComponent Methods

    @Override
    public void decode(FacesContext context) {
        super.decode(context);

        String group = getGroup();

        if (group != null && !isEmpty(getSubmittedValue())) {
            markGroupAsSubmittedOrValidated(context, group);
        }
    }

    // ------------------------------------------------------ Validation Methods

    /**
     * <p class="changed_added_2_3">
     * If {@link #getGroup()} is set, and {@link #getSubmittedValue()} is empty, and at least one other component having the
     * same group within a <code>UIForm</code> parent has a non-empty {@link #getSubmittedValue()} or returns
     * <code>true</code> on {@link #isLocalValueSet()} or returns <code>false</code> on {@link #isValid()}, then skip
     * validation for current component, else perform standard superclass processing by
     * <code>super.processValidators(context)</code>.
     * </p>
     */
    @Override
    public void processValidators(FacesContext context) {

        String group = getGroup();

        if (group != null && isEmpty(getSubmittedValue()) && isGroupSubmittedOrValidated(context, group)) {
            return;
        }

        super.processValidators(context);

        if (group != null && isRendered() && isAlreadySubmittedOrValidated(this)) {
            markGroupAsSubmittedOrValidated(context, group);
        }
    }

    /**
     * The groups of this request which have a member with a submitted value, or a rendered member which was validated.
     * Keyed by the client ID of their <code>UIForm</code> parent, or of the view root when there is none, and the group
     * name. A member with an empty submitted value finds only the record of another member, as the "other component" of
     * {@link #processValidators(FacesContext)} requires: its own decode record implies a non-empty submitted value, and
     * its own validation record is made after the check. Only an immediate member can find its own record, which is
     * harmless, because it is not validated there.
     */
    @SuppressWarnings("unchecked")
    private static Set<String> getSubmittedOrValidatedGroups(FacesContext context, boolean create) {
        Map<Object, Object> attributes = context.getAttributes();
        Set<String> groups = (Set<String>) attributes.get(SUBMITTED_OR_VALIDATED_GROUPS);

        if (groups == null && create) {
            groups = new HashSet<>();
            attributes.put(SUBMITTED_OR_VALIDATED_GROUPS, groups);
        }

        return groups;
    }

    private boolean isGroupSubmittedOrValidated(FacesContext context, String group) {
        Set<String> groups = getSubmittedOrValidatedGroups(context, false);
        return groups != null && groups.contains(getGroupKey(context, group));
    }

    private void markGroupAsSubmittedOrValidated(FacesContext context, String group) {
        getSubmittedOrValidatedGroups(context, true).add(getGroupKey(context, group));
    }

    private String getGroupKey(FacesContext context, String group) {
        return getGroupContainer(context, this).getClientId(context) + UINamingContainer.getSeparatorChar(context) + group;
    }

    private static UIComponent getGroupContainer(FacesContext context, UISelectOne radio) {
        UIComponent namingContainer = radio.getNamingContainer();

        while (namingContainer != null && !(namingContainer instanceof UIForm) && namingContainer.getParent() != null) {
            namingContainer = namingContainer.getParent().getNamingContainer();
        }

        return namingContainer != null ? namingContainer : context.getViewRoot();
    }

    private static boolean isAlreadySubmittedOrValidated(EditableValueHolder input) {
        return !isEmpty(input.getSubmittedValue()) || input.isLocalValueSet() || !input.isValid();
    }

    /**
     * <p>
     * <span class="changed_modified_2_0">In</span> addition to the standard validation behavior inherited from
     * {@link UIInput}, ensure that any specified value is equal to one of the available options. Before comparing each
     * option, coerce the option value type to the type of this component's value following the Expression Language coercion
     * rules. If the specified value is not equal to any of the options, enqueue an error message and set the
     * <code>valid</code> property to <code>false</code>.
     * </p>
     *
     * <p class="changed_added_2_0">
     * If {@link #isRequired} returns <code>true</code>, and the current value is equal to the value of an inner
     * {@link UISelectItem} whose {@link UISelectItem#isNoSelectionOption} method returns <code>true</code>, enqueue an
     * error message and set the <code>valid</code> property to <code>false</code>.
     * </p>
     *
     * @param context The {@link FacesContext} for the current request
     *
     * @param value The converted value to test for membership.
     *
     * @throws NullPointerException if <code>context</code> is <code>null</code>
     */
    @Override
    protected void validateValue(FacesContext context, Object value) {

        super.validateValue(context, value);

        // Skip validation if it is not necessary
        if (!isValid() || value == null) {
            return;
        }

        // Ensure that the value matches one of the available options
        boolean found = SelectUtils.matchValue(getFacesContext(), this, value, new SelectItemsIterator(context, this), getConverter());

        boolean isNoSelection = SelectUtils.valueIsNoSelectionOption(getFacesContext(), this, value, new SelectItemsIterator(context, this), getConverter());

        // Enqueue an error message if an invalid value was specified
        if (!found || isRequired() && isNoSelection) {
            FacesMessage message = MessageFactory.getMessage(context, INVALID_MESSAGE_ID, MessageFactory.getLabel(context, this));
            context.addMessage(getClientId(context), message);
            setValid(false);
        }

    }

}
