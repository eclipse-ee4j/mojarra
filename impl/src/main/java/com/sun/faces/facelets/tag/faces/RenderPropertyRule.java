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

import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.TagAttribute;

public final class RenderPropertyRule extends MetaRule {

    final static class HideNoSelectionLiteralMetadata extends Metadata {
        private final String hideOption;

        public HideNoSelectionLiteralMetadata(String hideOption) {
            this.hideOption = hideOption;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            Map<String, Object> attributes = ((UIInput) instance).getAttributes();
            attributes.put("hideNoSelectionOption", Boolean.valueOf(hideOption));
        }
    }

    final static class HideNoSelectionExpressionMetadata extends Metadata {
        private final TagAttribute attr;

        public HideNoSelectionExpressionMetadata(TagAttribute attr) {
            this.attr = attr;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((UIComponent) instance).setValueExpression("hideNoSelectionOption", attr.getValueExpression(ctx, Boolean.class));
        }
    }

    public final static RenderPropertyRule Instance = new RenderPropertyRule();

    @Override
    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta) {

        if ("hideNoSelectionOption".equals(name)) {
            if (attribute.isLiteral()) {
                return new HideNoSelectionLiteralMetadata(attribute.getValue());
            } else {
                return new HideNoSelectionExpressionMetadata(attribute);
            }
        }
        return null;
    }

}
