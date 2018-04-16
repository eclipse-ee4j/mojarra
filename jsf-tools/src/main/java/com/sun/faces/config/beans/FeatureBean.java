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

package com.sun.faces.config.beans;


import java.util.Map;
import java.util.TreeMap;


/**
 * <p>Base class for configuration beans for "features" such as
 * attributes and components.</p>
 */

public abstract class FeatureBean {


    // -------------------------------------------------------------- Properties



    // ----------------------------------------------- DescriptionHolder Methods


    private Map<String,DescriptionBean> descriptions = new TreeMap<String, DescriptionBean>();


    public void addDescription(DescriptionBean descriptor) {
        descriptions.put(descriptor.getLang(), descriptor);
    }


    public DescriptionBean getDescription(String lang) {
        return (descriptions.get(lang));
    }


    public DescriptionBean[] getDescriptions() {
        DescriptionBean results[] = new DescriptionBean[descriptions.size()];
        return (descriptions.values().toArray(results));
    }


    public void removeDescription(DescriptionBean descriptor) {
        descriptions.remove(descriptor.getLang());
    }


    // ----------------------------------------------- DisplayNameHolder Methods


    private Map<String,DisplayNameBean> displayNames = new TreeMap<String, DisplayNameBean>();


    public void addDisplayName(DisplayNameBean descriptor) {
        displayNames.put(descriptor.getLang(), descriptor);
    }


    public DisplayNameBean getDisplayName(String lang) {
        return (displayNames.get(lang));
    }


    public DisplayNameBean[] getDisplayNames() {
        DisplayNameBean results[] = new DisplayNameBean[displayNames.size()];
        return (displayNames.values().toArray(results));
    }


    public void removeDisplayName(DisplayNameBean descriptor) {
        displayNames.remove(descriptor.getLang());
    }


    // ------------------------------------------------------ IconHolder Methods


    private Map<String,IconBean> icons = new TreeMap<String, IconBean>();


    public void addIcon(IconBean descriptor) {
        icons.put(descriptor.getLang(), descriptor);
    }


    public IconBean getIcon(String lang) {
        return (icons.get(lang));
    }


    public IconBean[] getIcons() {
        IconBean results[] = new IconBean[icons.size()];
        return (icons.values().toArray(results));
    }


    public void removeIcon(IconBean descriptor) {
        icons.remove(descriptor.getLang());
    }


    // -------------------------------------------------------------- Extensions


    // ----------------------------------------------------------------- Methods


}
