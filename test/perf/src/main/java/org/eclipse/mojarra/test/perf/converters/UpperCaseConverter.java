/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.perf.converters;

import org.eclipse.mojarra.test.perf.beans.AppConfig;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/** CDI-managed converter looked up by id.
 *  {@code @Dependent} is the bean-defining annotation that makes this class
 *  discoverable under the CDI 4.0 default {@code bean-discovery-mode=annotated}. */
@FacesConverter(value = "upperCaseConverter", managed = true)
@Dependent
public class UpperCaseConverter implements Converter<String> {

    @Inject
    private AppConfig appConfig;

    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return null;
        }
        appConfig.getAppName();
        return value.toUpperCase();
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, String value) {
        return value == null ? "" : value.toUpperCase();
    }
}
