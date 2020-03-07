/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2005-2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jakarta.faces.view.facelets;

import jakarta.faces.view.EditableValueHolderAttachedObjectHandler;

/**
 * <p class="changed_added_2_0">
 * Handles setting a {@link jakarta.faces.validator.Validator} instance on an
 * {@link jakarta.faces.component.EditableValueHolder} parent. Will wire all attributes set to the
 * <code>Validator</code> instance created/fetched. Uses the "binding" attribute for grabbing instances to apply
 * attributes to.
 * </p>
 *
 * <p>
 * Will only set/create Validator is the passed UIComponent's parent is null, signifying that it wasn't restored from an
 * existing tree.
 * </p>
 *
 */
public class ValidatorHandler extends FaceletsAttachedObjectHandler implements EditableValueHolderAttachedObjectHandler {

    private String validatorId;

    private TagHandlerDelegate helper;

    private ValidatorConfig config;

    /**
     * <p class="changed_added_2_0">
     * Construct this instance around the configuration information in argument <code>config</code>
     * </p>
     *
     * @param config the <code>TagConfig</code> subclass for this kind of attached object.
     */
    public ValidatorHandler(ValidatorConfig config) {
        super(config);
        this.config = config;
        validatorId = config.getValidatorId();
    }

    /**
     * <p class="changed_added_2_0">
     * Return the implementation specific delegate instance that provides the bulk of the work for this handler instance.
     * </p>
     *
     * @return the implementation specific delegate instance
     */
    @Override
    protected TagHandlerDelegate getTagHandlerDelegate() {
        if (null == helper) {
            helper = delegateFactory.createValidatorHandlerDelegate(this);
        }
        return helper;
    }

    /**
     * <p>
     * Retrieve the id of the validator that is to be created and added to the parent <code>EditableValueHolder</code>. All
     * subclasses should override this method because it is important for Facelets to have a unique way of identifying the
     * validators that are added to this <code>EditableValueHolder</code> and allows exclusions to work properly. An
     * exclusion is a validator declaration that has the attribute "disabled" which resolves to false, instructing Facelets
     * not to register a default validator with the same id.
     * </p>
     *
     * @param ctx the FaceletContext for this {@code Facelet}
     *
     * @return the validator-id
     */
    public String getValidatorId(FaceletContext ctx) {
        if (validatorId == null) {
            TagAttribute idAttr = getAttribute("validatorId");
            if (idAttr == null) {
                return null;
            } else {
                return idAttr.getValue(ctx);
            }
        }
        return validatorId;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the <code>TagConfig</code> subclass used to configure this handler instance.
     * </p>
     *
     * @return the <code>TagConfig</code> subclass used to configure this handler instance.
     */
    public ValidatorConfig getValidatorConfig() {

        return config;

    }

}
