/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation.
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
package com.sun.faces.test.javaee8.facesConverter;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.application.ResourceDependency;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(value = "issue4913Converter", managed = true)
@ResourceDependency(name = "issue4913ResourceDependency.js", target = "head")
public class Issue4913Converter implements Converter<Object> {

    @Inject
    private BeanManager cdi;
    
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return cdi == null ? "" : (value + " is successfully converted in a managed converter");
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return value;
	}

}
