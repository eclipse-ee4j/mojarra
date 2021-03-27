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

package com.sun.faces.facelets.tag.ui;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.TagAttribute;

public class RepeatHandler extends ComponentHandler {

    private static final Logger log = FacesLogger.FACELETS_COMPOSITION.getLogger();

    public RepeatHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        MetaRuleset meta = super.createMetaRuleset(type);
        String myNamespace = tag.getNamespace();

        if (!UILibrary.NAMESPACES.contains(myNamespace)) {
            meta.add(new TagMetaData(type));
        }

        meta.alias("class", "styleClass");

        return meta;
    }

    private class TagMetaData extends Metadata {

        private final String[] attrs;

        public TagMetaData(Class type) {
            Set s = new HashSet();
            TagAttribute[] ta = tag.getAttributes().getAll();
            for (int i = 0; i < ta.length; i++) {
                if ("class".equals(ta[i].getLocalName())) {
                    s.add("styleClass");
                } else {
                    s.add(ta[i].getLocalName());
                }
            }
            try {
                PropertyDescriptor[] pd = Introspector.getBeanInfo(type).getPropertyDescriptors();
                for (int i = 0; i < pd.length; i++) {
                    if (pd[i].getWriteMethod() != null) {
                        s.remove(pd[i].getName());
                    }
                }
            } catch (Exception e) {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "Unable to get bean info", e);
                }
            }
            attrs = (String[]) s.toArray(new String[s.size()]);
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            UIComponent c = (UIComponent) instance;
            Map localAttrs = c.getAttributes();
            localAttrs.put("alias.element", tag.getQName());
            if (attrs.length > 0) {
                localAttrs.put("alias.attributes", attrs);
            }
        }

    }

}
