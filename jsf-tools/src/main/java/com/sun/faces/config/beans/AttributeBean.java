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

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Configuration bean for <code>&lt;attribute&gt; element.</p>
 */

public class AttributeBean extends FeatureBean {


    // -------------------------------------------------------------- Properties


    private String attributeClass;
    public String getAttributeClass() { return attributeClass; }
    public void setAttributeClass(String attributeClass)
    { this.attributeClass = attributeClass; }


    private String attributeName;
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName)
    { this.attributeName = attributeName; }


    private String suggestedValue;
    public String getSuggestedValue() { return suggestedValue; }
    public void setSuggestedValue(String suggestedValue)
    { this.suggestedValue = suggestedValue; }


    // -------------------------------------------------------------- Extensions


    // defaultValue == Non-standard default value (if any)
    private String defaultValue = null;
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue)
    { this.defaultValue = defaultValue; }

    // passThrough == HTML attribute that passes through [default=false]
    private boolean passThrough = false;
    public boolean isPassThrough() { return passThrough; }
    public void setPassThrough(boolean passThrough)
    { this.passThrough = passThrough; }


    // required == in TLD <attribute>, set required to true [default=false]
    private boolean required = false;
    public boolean isRequired() { return required; }
    public void setRequired(boolean required)
    { this.required = required; }


    // tagAttribute == Generate TLD attribute [default=true]
    private boolean tagAttribute = true;
    public boolean isTagAttribute() { return tagAttribute; }
    public void setTagAttribute(boolean tagAttribute)
    { this.tagAttribute = tagAttribute; }

    private boolean renderAttributeIgnore = false;
    public boolean isAttributeIgnoredForRenderer() {
        return renderAttributeIgnore;
    }
    public void setAttributeIgnoredForRenderer(boolean renderAttributeIgnore) {
        this.renderAttributeIgnore = renderAttributeIgnore;
    }


    // Behavior attribute, [dafault=false]
    private List<String> behaviors = null;
	/**
	 * <p class="changed_added_2_0"></p>
	 * @return the behaviorAttribute
	 */
	public List<String> getBehaviors() {
		return behaviors;
	}
	/**
	 * <p class="changed_added_2_0"></p>
	 * @param behaviorAttribute the behaviorAttribute to set
	 */
	public void addBehavior(String behavior) {
		if(null == this.behaviors){
			this.behaviors = new ArrayList<String>(5);
		}
		this.behaviors.add(behavior);
	}
	
	public void addAllBehaviors(List<String>behaviors) {
		if(null != behaviors){
			if(null == this.behaviors){
				this.behaviors = new ArrayList<String>(behaviors);
			} else {
				this.behaviors.addAll(behaviors);
			}
		}
	}

    private boolean defaultBehavior = false;
	/**
	 * <p class="changed_added_2_0"></p>
	 * @return the defaultBehavior
	 */
	public boolean isDefaultBehavior() {
		return defaultBehavior;
	}
	/**
	 * <p class="changed_added_2_0"></p>
	 * @param defaultBehavior the defaultBehavior to set
	 */
	public void setDefaultBehavior(boolean defaultBehavior) {
		this.defaultBehavior = defaultBehavior;
	}
    
    // ----------------------------------------------------------------- Methods


}
