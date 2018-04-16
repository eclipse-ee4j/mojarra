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

package com.sun.faces.test.servlet30.ajaxnamespace;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.sun.faces.context.RequestParameterMap;
import com.sun.faces.util.Util;

public class NamespacedRequestParameterMap extends RequestParameterMap {

	private final ServletRequest request;

	public NamespacedRequestParameterMap(ServletRequest request) {
		super(request);
		this.request = request;
	}

	@Override
	public String get(Object key) {
		String mapKey = key.toString();

		String value = request.getParameter(getNamingContainerPrefix() + mapKey);

		if (value == null && !mapKey.equals("param")) {
			value = request.getParameter(mapKey);
		}
		return value;
	}

	@Override
	public boolean containsKey(Object key) {
		String mapKey = key.toString();
		boolean contains = (request.getParameter(getNamingContainerPrefix()
				+ mapKey) != null);

		if (!contains && !mapKey.equals("param")) {
			contains = (request.getParameter(mapKey) != null);
		}

		return contains;
	}
}
