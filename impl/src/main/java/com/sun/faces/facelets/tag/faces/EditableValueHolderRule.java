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

package com.sun.faces.facelets.tag.faces;


import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.MethodExpressionValueChangeListener;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.validator.MethodExpressionValidator;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.TagAttribute;

/**
 *
 * @author Jacob Hookom
 */
public final class EditableValueHolderRule extends MetaRule {

    final static class LiteralValidatorMetadata extends Metadata {

        private final String validatorId;

        public LiteralValidatorMetadata(String validatorId) {
            this.validatorId = validatorId;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((EditableValueHolder) instance).addValidator(ctx.getFacesContext().getApplication().createValidator(validatorId));
        }
    }

    final static class ValueChangedExpressionMetadata extends Metadata {
        private final TagAttribute attr;

        public ValueChangedExpressionMetadata(TagAttribute attr) {
            this.attr = attr;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((EditableValueHolder) instance)
                    .addValueChangeListener(new MethodExpressionValueChangeListener(attr.getMethodExpression(ctx, null, VALUECHANGE_SIG)));
        }
    }

    final static class ValidatorExpressionMetadata extends Metadata {
        private final TagAttribute attr;

        public ValidatorExpressionMetadata(TagAttribute attr) {
            this.attr = attr;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((EditableValueHolder) instance).addValidator(new MethodExpressionValidator(attr.getMethodExpression(ctx, null, VALIDATOR_SIG)));
        }
    }

    private final static Class<?>[] VALIDATOR_SIG = new Class<?>[] { FacesContext.class, UIComponent.class, Object.class };

    private final static Class<?>[] VALUECHANGE_SIG = new Class<?>[] { ValueChangeEvent.class };

    public final static EditableValueHolderRule Instance = new EditableValueHolderRule();

    @Override
    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta) {

        if (meta.isTargetInstanceOf(EditableValueHolder.class)) {

            if ("validator".equals(name)) {
                if (attribute.isLiteral()) {
                    return new LiteralValidatorMetadata(attribute.getValue());
                }

                return new ValidatorExpressionMetadata(attribute);
            }

            if ("valueChangeListener".equals(name)) {
                return new ValueChangedExpressionMetadata(attribute);
            }

        }
        return null;
    }

}
