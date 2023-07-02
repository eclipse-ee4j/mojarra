/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package com.sun.faces.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.faces.context.FacesContext;

/**
 * This class helps in letting code run within its own scope. Such scope is defined by specific variables being
 * available to EL within it. The request scope is used to store the variables.
 *
 * @author Arjan Tijms
 *
 */
public class ScopedRunner {

	private final FacesContext context;
	private final Map<String, Object> scopedVariables;
	private final Map<String, Object> previousVariables = new HashMap<>();

	public ScopedRunner(FacesContext context) {
		this(context, new HashMap<>());
	}

	public ScopedRunner(FacesContext context, Map<String, Object> scopedVariables) {
		this.context = context;
		this.scopedVariables = scopedVariables;
	}

	/**
	 * Adds the given variable to this instance. Can be used in a builder-pattern.
	 *
	 * @param key the key name of the variable
	 * @param value the value of the variable
	 * @return this ScopedRunner, so adding variables and finally calling invoke can be chained.
	 */
	public ScopedRunner with(String key, Object value) {
		scopedVariables.put(key, value);
		return this;
	}

	/**
	 * Invokes the callback within the scope of the variables being given in the constructor.
	 * @param callback The callback.
	 */
	public void invoke(Runnable callback) {
		try {
			setNewScope();
			callback.run();
		} finally {
			restorePreviousScope();
		}
	}

	private void setNewScope() {
		previousVariables.clear();

		Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
		for (Map.Entry<String, Object> entry : scopedVariables.entrySet()) {
			Object previousVariable = requestMap.put(entry.getKey(), entry.getValue());
			if (previousVariable != null) {
				previousVariables.put(entry.getKey(), previousVariable);
			}
		}
	}

	private void restorePreviousScope() {
		try {
			Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
			for (Map.Entry<String, Object> entry : scopedVariables.entrySet()) {
				Object previousVariable = previousVariables.get(entry.getKey());
				if (previousVariable != null) {
					requestMap.put(entry.getKey(), previousVariable);
				} else {
					requestMap.remove(entry.getKey());
				}
			}
		} finally {
			previousVariables.clear();
		}
	}

}