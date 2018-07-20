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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRule;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.MetadataTarget;
import javax.faces.view.facelets.TagAttribute;

import com.sun.faces.util.FacesLogger;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
final class ComponentRule extends MetaRule {

    final static class LiteralAttributeMetadata extends Metadata {

        private final String name;
        private final String value;

        public LiteralAttributeMetadata(String name, String value) {
            this.value = value;
            this.name = name;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((UIComponent) instance).getAttributes().put(this.name, this.value);
        }
    }

    final static class ValueExpressionMetadata extends Metadata {

        private final String name;

        private final TagAttribute attr;

        private final Class type;

        public ValueExpressionMetadata(String name, Class type,
                TagAttribute attr) {
            this.name = name;
            this.attr = attr;
            this.type = type;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            ((UIComponent) instance).setValueExpression(this.name, this.attr
                    .getValueExpression(ctx, this.type));
        }

    }

    private final static Logger log = FacesLogger.FACELETS_COMPONENT.getLogger();

    public final static ComponentRule Instance = new ComponentRule();

    public ComponentRule() {
        super();
    }

    @Override
    public Metadata applyRule(String name, TagAttribute attribute,
            MetadataTarget meta) {
        if (meta.isTargetInstanceOf(UIComponent.class)) {

            // if component and dynamic, then must set expression
            if (!attribute.isLiteral()) {
                Class type = meta.getPropertyType(name);
                if (type == null) {
                    type = Object.class;
                }
                return new ValueExpressionMetadata(name, type, attribute);
            } else if (meta.getWriteMethod(name) == null) {

                // this was an attribute literal, but not property
                warnAttr(attribute, meta.getTargetClass(), name);

                return new LiteralAttributeMetadata(name, attribute.getValue());
            }
        }
        return null;
    }

    private static void warnAttr(TagAttribute attr, Class type, String n) {
        if (log.isLoggable(Level.FINER)) {
            log.finer(attr + " Property '" + n + "' is not on type: "
                    + type.getName());
        }
    }

}
