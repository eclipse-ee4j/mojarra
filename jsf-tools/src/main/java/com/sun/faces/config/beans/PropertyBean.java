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
 * <p>Configuration bean for <code>&lt;property&gt;</code> element.</p>
 */

public class PropertyBean extends FeatureBean {


    // -------------------------------------------------------------- Properties


    private String propertyClass;
    public String getPropertyClass() { return propertyClass; }
    public void setPropertyClass(String propertyClass)
    { this.propertyClass = propertyClass; }


    private String propertyName;
    public String getPropertyName() { return propertyName; }
    public void setPropertyName(String propertyName)
    { this.propertyName = propertyName; }


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


    // readOnly == Do not generate a property setter [default=false]
    private boolean readOnly = false;
    public boolean isReadOnly() { return readOnly; }
    public void setReadOnly(boolean readOnly)
    { this.readOnly = readOnly; }


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

    // Set to TRUE if property-extension contains method-signature element
    // [default=false]
    private boolean methodExpressionEnabled = false;
    public boolean isMethodExpressionEnabled() {
        return methodExpressionEnabled;
    }
    public void setMethodExpressionEnabled(boolean methodExpressionEnabled) {
        this.methodExpressionEnabled = methodExpressionEnabled;
    }

    private String methodSignature;
    public String getMethodSignature() { return methodSignature; }
    public void setMethodSignature(String methodSignature) {
        if (methodSignature != null) {
            methodSignature = methodSignature.trim();
            if (methodSignature.length() > 0) {
                setMethodExpressionEnabled(true);
                this.methodSignature = methodSignature.trim();
            }
        }
    }

    // value-expression-enabled - if the property can accept ValueExpressions
    // [default=false]
    private boolean valueExpressionEnabled = false;
    public boolean isValueExpressionEnabled() {
        return valueExpressionEnabled;
    }
    public void setValueExpressionEnabled(boolean valueExpressionEnabled) {
        this.valueExpressionEnabled = valueExpressionEnabled;
    }

    // Behavior attribute, [dafault=false]
    private List<String> behaviors = null;
	/**
	 * <p class="changed_added_2_0">Something changed</p>
	 * @return the behaviorAttribute
	 */
	public List<String> getBehaviors() {
		return behaviors;
	}
	/**
	 * <p class="changed_added_2_0">Something changed</p>
	 * @param behavior the behaviorAttribute to set
	 */
	public void addBehavior(String behavior) {
		if(null == this.behaviors){
			this.behaviors = new ArrayList<String>(5);
		}
		this.behaviors.add(behavior);
	}

	public void addAllBehaviors(List<String>behaviors) {
		if(null != behaviors){
//			if(null == this.behaviors){
				this.behaviors = new ArrayList<String>(behaviors);
//			} else {
//				this.behaviors.addAll(behaviors);
//			}
		}
	}

    private boolean defaultBehavior = false;

	/**
	 * <p class="changed_added_2_0">Something changed</p>
	 * @return the defaultBehavior
	 */
	public boolean isDefaultBehavior() {
		return defaultBehavior;
	}

	/**
	 * <p class="changed_added_2_0">Something changed</p>
	 * @param defaultBehavior the defaultBehavior to set
	 */
	public void setDefaultBehavior(boolean defaultBehavior) {
		this.defaultBehavior = defaultBehavior;
	}

    // ----------------------------------------------------------------- Methods


}
