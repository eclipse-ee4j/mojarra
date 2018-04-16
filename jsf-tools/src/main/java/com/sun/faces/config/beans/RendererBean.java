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
 * <p>Configuration bean for <code>&lt;renderer&gt; element.</p>
 */

public class RendererBean extends FeatureBean implements AttributeHolder {


    // -------------------------------------------------------------- Properties


    private String componentFamily;
    public String getComponentFamily() { return componentFamily; }
    public void setComponentFamily(String componentFamily)
    { this.componentFamily = componentFamily; }


    private String rendererClass;
    public String getRendererClass() { return rendererClass; }
    public void setRendererClass(String rendererClass)
    { this.rendererClass = rendererClass; }


    private String rendererType;
    public String getRendererType() { return rendererType; }
    public void setRendererType(String rendererType)
    { this.rendererType = rendererType; }


    // -------------------------------------------------------------- Extensions


    // true == this Renderer returns true for getRendersChildren()
    private boolean rendersChildren = false;
    public boolean isRendersChildren() { return rendersChildren; }
    public void setRendersChildren(boolean rendersChildren)
    { this.rendersChildren = rendersChildren; }

    private String excludeAttributes;
    public String getExcludeAttributes() {
        return excludeAttributes;
    }
    public void setExcludeAttributes(String newExcludeAttributes) {
        excludeAttributes = newExcludeAttributes;
    }

    private boolean notForJsp = false;
    public boolean isIgnoreForJsp() { return notForJsp; }
    public void setIgnoreForJsp(boolean notForJsp)
    { this.notForJsp = notForJsp; }

    private boolean ignoreAll = false;
    public boolean isIgnoreAll() { return ignoreAll; }
    public void setIgnoreAll(boolean ignoreAll)
    { this.ignoreAll = ignoreAll; }

    // true if the tag handler for this renderer should be a BodyTag
    // [default=false]
    public boolean bodyTag = false;
    public boolean isBodyTag() { return bodyTag; }
    public void setBodyTag(boolean bodyTag) { this.bodyTag = bodyTag; }

    // Tag name (if it doesn't follow the standard convention)
    private String tagName;
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }


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


    // ----------------------------------------------------------------- Methods


}
