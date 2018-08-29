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

package com.sun.faces.test.servlet30.facelets;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

@Named
@SessionScoped
public class InputTextSetBean implements Serializable {

    private static final long serialVersionUID = 1L;
    protected Set<String> tags = new TreeSet<String>(asList("seed"));

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public static class StringToSetConverter implements Converter<Collection<String>> {

        @Override
        public Collection<String> getAsObject(FacesContext ctx, UIComponent component, String value) {
            if (value == null) {
                return null;
            }

            Set<String> tagSet = new TreeSet<String>();
            for (String tag : value.split("\\s+")) {
                if (!tag.isEmpty()) {
                    tagSet.add(tag);
                }
            }

            return tagSet;
        }

        @Override
        public String getAsString(FacesContext ctx, UIComponent component, Collection<String> value) {
            if (value == null) {
                return "";
            }

            StringBuilder builder = new StringBuilder();
            Collection<String> tags = value;
            for (String tag : tags) {
                builder.append(tag);
                builder.append(" ");
            }

            return builder.toString().trim();
        }
    }

    public String printTags() {
        if (tags.isEmpty()) {
            return "No tags";
        }

        StringBuilder builder = new StringBuilder();
        for (String tag : tags) {
            builder.append("'");
            builder.append(tag);
            builder.append("' ");
        }

        return builder.toString().trim();
    }

    public Converter<Collection<String>> getTagsConverter() {
        return new StringToSetConverter();
    }
}
