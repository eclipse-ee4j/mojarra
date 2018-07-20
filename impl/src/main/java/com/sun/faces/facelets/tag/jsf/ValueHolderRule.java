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

package com.sun.faces.facelets.tag.jsf;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.ValueHolder;
import javax.faces.convert.Converter;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRule;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.MetadataTarget;
import javax.faces.view.facelets.TagAttribute;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
final class ValueHolderRule extends MetaRule {

    final static class LiteralConverterMetadata extends Metadata {

        private final String converterId;

        public LiteralConverterMetadata(String converterId) {
            this.converterId = converterId;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((ValueHolder) instance).setConverter(ctx.getFacesContext()
                    .getApplication().createConverter(this.converterId));
        }
    }

    final static class DynamicConverterMetadata2 extends Metadata {

        private final TagAttribute attr;

        public DynamicConverterMetadata2(TagAttribute attr) {
            this.attr = attr;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((UIComponent) instance).setValueExpression("converter", attr
                    .getValueExpression(ctx, Converter.class));
        }
    }

    final static class DynamicValueExpressionMetadata extends Metadata {

        private final TagAttribute attr;

        public DynamicValueExpressionMetadata(TagAttribute attr) {
            this.attr = attr;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            UIComponent c = (UIComponent) instance;
            c.setValueExpression("value", attr.getValueExpression(ctx, ((c instanceof UISelectBoolean)
                                                                        ? Boolean.class
                                                                        : Object.class)));
        }
    }

    public final static ValueHolderRule Instance = new ValueHolderRule();

    @Override
    public Metadata applyRule(String name, TagAttribute attribute,
            MetadataTarget meta) {
        if (meta.isTargetInstanceOf(ValueHolder.class)) {

            if ("converter".equals(name)) {
                if (attribute.isLiteral()) {
                    return new LiteralConverterMetadata(attribute.getValue());
                } else {
                    return new DynamicConverterMetadata2(attribute);
                }
            }

            if ("value".equals(name)) {
                return new DynamicValueExpressionMetadata(attribute);
            }
        }

        return null;
    }

}
