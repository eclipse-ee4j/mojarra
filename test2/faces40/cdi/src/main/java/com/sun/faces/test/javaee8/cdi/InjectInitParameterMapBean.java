/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee8.cdi;

import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.annotation.InitParameterMap;
import jakarta.inject.Named;

@Named
@RequestScoped
public class InjectInitParameterMapBean {

    public String getInitParameterValue() {
        @SuppressWarnings("unchecked")
        Map<String, String> initParameterMap = CDI.current().select(Map.class, InitParameterMap.Literal.INSTANCE).get();

        // MY_TEST_PARAMETER set in web.xml, should be available
        return initParameterMap.get("MY_TEST_PARAMETER");
    }

}
