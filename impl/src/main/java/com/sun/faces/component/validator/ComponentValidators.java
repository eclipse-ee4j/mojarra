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

package com.sun.faces.component.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.util.RequestStateManager;

import jakarta.faces.application.Application;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.ValidatorHandler;

/**
 * <p>
 * This class is responsible for adding default validators and/or validators that wrap multiple
 * <code>EditableValueHolder</code> instances within the view.
 * </p>
 */
public class ComponentValidators {

    /**
     * Key within the <code>FacesContext</code>'s attribute map under which a single <code>ComponentValidators</code>
     * instance will be stored.
     */
    private static final String COMPONENT_VALIDATORS = "jakarta.faces.component.ComponentValidators";

    /**
     * Stack of <code>ValidatorInfo<code> instances.  Each instance represents
     * a particular nesting level within the view.  As a nesting level is encountered,
     * a <code>ValidatorInfo</code> will be pushed to the stack and all <code>EditableValueHolder</code> instances will be
     * configured based on all <code>ValidatorInfo</code>s on the stack. When the current nesting level is closed, the
     * <code>ValidatorInfo</code> instance will be popped and thus have no impact on other
     * <code>EditableValueHolder</code>s.
     */
    private LinkedList<ValidatorInfo> validatorStack = null;

    // ------------------------------------------------------------ Constructors

    public ComponentValidators() {

        validatorStack = new LinkedList<>();

    }

    // ---------------------------------------------------------- Public Methods

    /**
     * @param context the <code>FacesContext</code> for the current request
     * @param createIfNull flag indicating whether or not a <code>ComponentValidators</code> instance should be created or
     * not
     * @return a <code>ComponentValidators</code> instance for processing a view request. If <code>createIfNull</code> is
     * <code>false</code> and no <code>ComponentValidators</code> has been created, this method will return
     * <code>null</code>
     */
    public static ComponentValidators getValidators(FacesContext context, boolean createIfNull) {

        Map<Object, Object> attrs = context.getAttributes();
        ComponentValidators componentValidators = (ComponentValidators) attrs.get(COMPONENT_VALIDATORS);

        if (componentValidators == null && createIfNull) {
            componentValidators = new ComponentValidators();
            attrs.put(COMPONENT_VALIDATORS, componentValidators);
        }

        return componentValidators;
    }

    /**
     * <p>
     * Creates and installs default validators, if any, into the argument <code>EditableValueHolder</code>. This method is
     * merely a utility method to be called when there is no <code>ComponentValidators</code> available, or there are no
     * <code>ValidatorInfo</code> instances on the stack.
     * </p>
     *
     * @param ctx the <code>FacesContext</code> for the current request
     * @param editableValueHolder the component receiving the <code>Validator</code>s
     */
    @SuppressWarnings({ "unchecked" })
    public static void addDefaultValidatorsToComponent(FacesContext ctx, EditableValueHolder editableValueHolder) {

        if (ComponentSupport.isBuildingNewComponentTree(ctx)) {
            Set<String> keySet = ctx.getApplication().getDefaultValidatorInfo().keySet();
            List<String> validatorIds = new ArrayList<>(keySet.size());
            Set<String> disabledValidatorIds = (Set<String>) RequestStateManager.remove(ctx, RequestStateManager.DISABLED_VALIDATORS);
            for (String key : keySet) {
                if (disabledValidatorIds != null && disabledValidatorIds.contains(key)) {
                    continue;
                }
                validatorIds.add(key);
            }

            addValidatorsToComponent(ctx, validatorIds, editableValueHolder, null);
        }
    }

    /**
     * <p>
     * Based on the <code>ValidatorInfo</code> instances present on the stack, configure the argument
     * <code>EditableValueHolder</code> with <code>Validator</code>s created from the available info.
     * </p>
     *
     * @param ctx the <code>FacesContext</code> for the current request
     * @param editableValueHolder the component receiving the <code>Validator</code>s
     */
    @SuppressWarnings({ "unchecked" })
    public void addValidators(FacesContext ctx, EditableValueHolder editableValueHolder) {

        if (validatorStack == null || validatorStack.isEmpty()) {
            addDefaultValidatorsToComponent(ctx, editableValueHolder);
            return;
        }

        Application application = ctx.getApplication();
        Map<String, String> defaultValidatorInfo = application.getDefaultValidatorInfo();
        Set<String> keySet = defaultValidatorInfo.keySet();

        List<String> validatorIds = new ArrayList<>(keySet.size());
        validatorIds.addAll(keySet);

        Set<String> disabledIds = (Set<String>) RequestStateManager.remove(ctx, RequestStateManager.DISABLED_VALIDATORS);
        int count = validatorStack.size();
        for (int i = count - 1; i >= 0; i--) {
            ValidatorInfo info = validatorStack.get(i);
            if (!info.isEnabled() || disabledIds != null && disabledIds.contains(info.getValidatorId())) {
                validatorIds.remove(info.getValidatorId());
            } else {
                if (!validatorIds.contains(info.getValidatorId())) {
                    validatorIds.add(info.getValidatorId());
                }
            }
        }

        // add the validators to the EditableValueHolder.
        addValidatorsToComponent(ctx, validatorIds, editableValueHolder, validatorStack == null || validatorStack.isEmpty() ? null : validatorStack);

    }

    /**
     * <p>
     * Pushes the provided <code>ValidatorInfo</code> onto the stack.
     * </p>
     *
     * @param info validator info
     */
    public void pushValidatorInfo(ValidatorInfo info) {

        validatorStack.add(info);

    }

    /**
     * <p>
     * Pops the last <code>ValidatorInfo</code> instance from the stack.
     * </p>
     */
    public void popValidatorInfo() {

        if (validatorStack.size() > 0) {
            validatorStack.removeLast();
        }

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Install the validators, if not already present on the component, using the IDs included in <code>validatorIds</code>.
     * </p>
     *
     * @param ctx the <code>FacesContext</code> for the current request
     * @param validatorIds the validator IDs to be added to the <code>EditableValueHolder</code>
     * @param editableValueHolder the target component to which the validators installed
     * @param validatorStack current stack of ValidatorInfo instances
     */
    private static void addValidatorsToComponent(FacesContext ctx, Collection<String> validatorIds, EditableValueHolder editableValueHolder,
            LinkedList<ValidatorInfo> validatorStack) {

        if (validatorIds == null || validatorIds.isEmpty()) {
            return;
        }

        Application application = ctx.getApplication();
        Map<String, String> defaultValidatorInfo = application.getDefaultValidatorInfo();
        Validator<?>[] validators = editableValueHolder.getValidators();
        // check to make sure that Validator instances haven't already
        // been added.
        for (Map.Entry<String, String> defaultValidator : defaultValidatorInfo.entrySet()) {
            for (Validator<?> validator : validators) {
                if (defaultValidator.getValue().equals(validator.getClass().getName())) {
                    validatorIds.remove(defaultValidator.getKey());
                    break;
                }
            }
        }

        // we now have the complete List of Validator IDs to add to the
        // target EditablValueHolder
        for (String id : validatorIds) {
            Validator<?> v = application.createValidator(id);
            // work backwards up the stack of ValidatorInfo to find the
            // nearest matching ValidatorInfo to apply attributes
            if (validatorStack != null) {
                for (int i = validatorStack.size() - 1; i >= 0; i--) {
                    ValidatorInfo info = validatorStack.get(i);
                    if (id.equals(info.getValidatorId())) {
                        info.applyAttributes(v);
                        break;
                    }
                }
            }
            editableValueHolder.addValidator(v);
        }

    }

    // ---------------------------------------------------------- Nested Classes

    /**
     * Generic information container for a validator at a particular nesting Level.
     */
    public static class ValidatorInfo {

        private final String validatorId;
        private final boolean enabled;
        private final ValidatorHandler owner;
        private final FaceletContext ctx;

        // ------------------------------------------------------------ Constructors

        public ValidatorInfo(FaceletContext ctx, ValidatorHandler owner) {

            this.owner = owner;
            this.ctx = ctx;
            validatorId = owner.getValidatorId(ctx);
            enabled = !owner.isDisabled(ctx);

        }

        // -------------------------------------------------------------------------

        public String getValidatorId() {

            return validatorId;

        }

        public boolean isEnabled() {

            return enabled;

        }

        public void applyAttributes(Validator v) {

            owner.setAttributes(ctx, v);

        }

    } // END ValidatorInfo

}
