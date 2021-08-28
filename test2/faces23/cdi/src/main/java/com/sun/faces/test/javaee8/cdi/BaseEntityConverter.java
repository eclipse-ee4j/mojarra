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

package com.sun.faces.test.javaee8.cdi;

import jakarta.inject.Inject;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(forClass=BaseEntity.class, managed=true)
public class BaseEntityConverter implements Converter<BaseEntity> {

	@Inject
	private BaseEntityService baseEntityService;

	@Override
	public String getAsString(FacesContext context, UIComponent component, BaseEntity entity) {
		if (baseEntityService == null) {
			throw new ConverterException();
		}
		return entity.getClass().getSimpleName();
	}

	@Override
	public BaseEntity getAsObject(FacesContext context, UIComponent component, String value) {
		if (baseEntityService == null) {
			throw new ConverterException();
		}
		return new ConcreteEntity();
	}

}