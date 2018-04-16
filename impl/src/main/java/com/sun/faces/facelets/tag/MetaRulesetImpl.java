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

package com.sun.faces.facelets.tag;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import javax.faces.view.facelets.*;
import java.beans.IntrospectionException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.ref.WeakReference;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public class MetaRulesetImpl extends MetaRuleset {

    private final static Logger LOGGER = FacesLogger.FACELETS_META.getLogger();
    private final static Map<Class, WeakReference<MetadataTarget>> metadata =
          Collections.synchronizedMap(new WeakHashMap<>());

    private final Tag tag;
    private final Class type;
    private final Map<String,TagAttribute> attributes;
    private final List<Metadata> mappers;
    private final List<MetaRule> rules;


    // ------------------------------------------------------------ Constructors


    public MetaRulesetImpl(Tag tag, Class<?> type) {

        this.tag = tag;
        this.type = type;
        this.attributes = new HashMap<>();
        this.mappers = new ArrayList<>();
        this.rules = new ArrayList<>();

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
        this.rules.add(BeanPropertyTagRule.Instance);

    }


    // ---------------------------------------------------------- Public Methods


    @Override
    public MetaRuleset ignore(String attribute) {

        Util.notNull("attribute", attribute);
        this.attributes.remove(attribute);
        return this;

    }


    @Override
    public MetaRuleset alias(String attribute, String property) {

        Util.notNull("attribute", attribute);
        Util.notNull("property", property);
        TagAttribute attr = this.attributes.remove(attribute);
        if (attr != null) {
            this.attributes.put(property, attr);
        }
        return this;

    }

    @Override
    public MetaRuleset add(Metadata mapper) {

        Util.notNull("mapper", mapper);
        if (!this.mappers.contains(mapper)) {
            this.mappers.add(mapper);
        }
        return this;

    }

    @Override
    public MetaRuleset addRule(MetaRule rule) {

        Util.notNull("rule", rule);
        this.rules.add(rule);
        return this;

    }


    @Override
     public Metadata finish() {

        if (!this.attributes.isEmpty()) {
            if (this.rules.isEmpty()) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    for (Iterator<TagAttribute> itr = this.attributes.values().iterator(); itr.hasNext(); ) {
                        LOGGER.severe(itr.next() + " Unhandled by MetaTagHandler for type "+this.type.getName());
                    }
                }
            } else {
                MetadataTarget target = this.getMetadataTarget();
                // now iterate over attributes
                int ruleEnd = this.rules.size() - 1;
                for (Map.Entry<String,TagAttribute> entry : attributes.entrySet()) {
                    Metadata data = null;
                    int i = ruleEnd;
                    while (data == null && i >= 0) {
                        MetaRule rule = this.rules.get(i);
                        data = rule.applyRule(entry.getKey(), entry.getValue(), target);
                        i--;
                    }
                    if (data == null) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.severe(entry.getValue() + " Unhandled by MetaTagHandler for type "+this.type.getName());
                        }
                    } else {
                        this.mappers.add(data);
                    }
                }
            }
        }

        if (this.mappers.isEmpty()) {
            return NONE;
        } else {
            return new MetadataImpl(this.mappers.toArray(new Metadata[this.mappers.size()]));
        }

    }

    @Override
    public MetaRuleset ignoreAll() {

        this.attributes.clear();
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
                throw new TagException(this.tag,
                        "Error Creating TargetMetadata", e);
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
