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


import com.sun.faces.util.ToolsUtil;

import java.util.Map;
import java.util.TreeMap;


/**
 * <p>Configuration bean for <code>&lt;attribute&gt; element.</p>
 */

public class ConverterBean extends FeatureBean
 implements AttributeHolder, PropertyHolder {


    // -------------------------------------------------------------- Properties


    private String converterClass;
    public String getConverterClass() { return converterClass; }
    public void setConverterClass(String converterClass)
    { this.converterClass = converterClass; }


    private Class converterForClass;
    public Class getConverterForClass() { return converterForClass; }
    public void setConverterForClass(String converterForClass) {
        try {
            this.converterForClass = ToolsUtil.loadClass(converterForClass,
                                                         this);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }
    public void setConverterForClass(Class converterForClass) {
            this.converterForClass = converterForClass;        
    }


    private String converterId;
    public String getConverterId() { return converterId; }
    public void setConverterId(String converterId)
    { this.converterId = converterId; }


    // -------------------------------------------------------------- Extensions


    // ------------------------------------------------- AttributeHolder Methods


    private Map<String,AttributeBean> attributes = new TreeMap<String, AttributeBean>();


    public void addAttribute(AttributeBean descriptor) {
        attributes.put(descriptor.getAttributeName(), descriptor);
    }


    public AttributeBean getAttribute(String name) {
        return (attributes.get(name));
    }


    public AttributeBean[] getAttributes() {
        AttributeBean results[] = new AttributeBean[attributes.size()];
        return (attributes.values().toArray(results));
    }


    public void removeAttribute(AttributeBean descriptor) {
        attributes.remove(descriptor.getAttributeName());
    }


    // ------------------------------------------------- PropertyHolder Methods


    private Map<String,PropertyBean> properties = new TreeMap<String, PropertyBean>();


    public void addProperty(PropertyBean descriptor) {
        properties.put(descriptor.getPropertyName(), descriptor);
    }


    public PropertyBean getProperty(String name) {
        return (properties.get(name));
    }


    public PropertyBean[] getProperties() {
        PropertyBean results[] = new PropertyBean[properties.size()];
        return (properties.values().toArray(results));
    }


    public void removeProperty(PropertyBean descriptor) {
        properties.remove(descriptor.getPropertyName());
    }


    // ----------------------------------------------------------------- Methods


}
