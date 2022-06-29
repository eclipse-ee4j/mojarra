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

package com.sun.faces.facelets.tag;

import java.beans.IntrospectionException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public class MetaRulesetImpl extends MetaRuleset {

    private final static Logger LOGGER = FacesLogger.FACELETS_META.getLogger();
    private final static Map<Class, WeakReference<MetadataTarget>> metadata = Collections.synchronizedMap(new WeakHashMap<>());

    private final Tag tag;
    private final Class<?> type;
    private final Map<String, TagAttribute> attributes;
    private final List<Metadata> mappers;
    private final List<MetaRule> rules;

    // ------------------------------------------------------------ Constructors

    public MetaRulesetImpl(Tag tag, Class<?> type) {

        this.tag = tag;
        this.type = type;
        attributes = new HashMap<>();
        mappers = new ArrayList<>();
        rules = new ArrayList<>();

        // setup attributes
        TagAttribute[] attrs = this.tag.getAttributes().getAll();
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].getLocalName().equals("class")) {
                attributes.put("styleClass", attrs[i]);
            } else {
                attributes.put(attrs[i].getLocalName(), attrs[i]);
            }
        }

        // add default rules
        rules.add(BeanPropertyTagRule.Instance);

    }

    // ---------------------------------------------------------- Public Methods

    @Override
    public MetaRuleset ignore(String attribute) {

        Util.notNull("attribute", attribute);
        attributes.remove(attribute);
        return this;

    }

    @Override
    public MetaRuleset alias(String attribute, String property) {

        Util.notNull("attribute", attribute);
        Util.notNull("property", property);
        TagAttribute attr = attributes.remove(attribute);
        if (attr != null) {
            attributes.put(property, attr);
        }
        return this;

    }

    @Override
    public MetaRuleset add(Metadata mapper) {

        Util.notNull("mapper", mapper);
        if (!mappers.contains(mapper)) {
            mappers.add(mapper);
        }
        return this;

    }

    @Override
    public MetaRuleset addRule(MetaRule rule) {

        Util.notNull("rule", rule);
        rules.add(rule);
        return this;

    }

    @Override
    public Metadata finish() {

        if (!attributes.isEmpty()) {
            if (rules.isEmpty()) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    for (Iterator<TagAttribute> itr = attributes.values().iterator(); itr.hasNext();) {
                        LOGGER.severe(itr.next() + " Unhandled by MetaTagHandler for type " + type.getName());
                    }
                }
            } else {
                MetadataTarget target = getMetadataTarget();
                // now iterate over attributes
                int ruleEnd = rules.size() - 1;
                for (Map.Entry<String, TagAttribute> entry : attributes.entrySet()) {
                    Metadata data = null;
                    int i = ruleEnd;
                    while (data == null && i >= 0) {
                        MetaRule rule = rules.get(i);
                        data = rule.applyRule(entry.getKey(), entry.getValue(), target);
                        i--;
                    }
                    if (data == null) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.severe(entry.getValue() + " Unhandled by MetaTagHandler for type " + type.getName());
                        }
                    } else {
                        mappers.add(data);
                    }
                }
            }
        }

        if (mappers.isEmpty()) {
            return NONE;
        } else {
            return new MetadataImpl(mappers.toArray(new Metadata[mappers.size()]));
        }

    }

    @Override
    public MetaRuleset ignoreAll() {

        attributes.clear();
        return this;

    }

    // ------------------------------------------------------- Protected Methods

    protected MetadataTarget getMetadataTarget() {
        WeakReference<MetadataTarget> metaRef = metadata.get(type);
        MetadataTarget meta = metaRef == null ? null : metaRef.get();
        if (meta == null) {
            try {
                meta = new MetadataTargetImpl(type);
            } catch (IntrospectionException e) {
                throw new TagException(tag, "Error Creating TargetMetadata", e);
            }
            metadata.put(type, new WeakReference<>(meta));
        }
        return meta;

    }

    // --------------------------------------------------------- Private Methods

    private final static Metadata NONE = new Metadata() {

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            // do nothing
        }

    };

}
