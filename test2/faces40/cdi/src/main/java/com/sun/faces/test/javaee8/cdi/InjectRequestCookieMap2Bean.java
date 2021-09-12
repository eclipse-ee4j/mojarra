/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;
import java.util.Map;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.annotation.RequestCookieMap;
import jakarta.inject.Named;

@Named(value = "injectRequestCookieMap2Bean")
@SessionScoped
public class InjectRequestCookieMap2Bean implements Serializable {

    public String getValue() {
        @SuppressWarnings("unchecked")
        Map<String, Object> requestCookieMap = CDI.current().select(Map.class, RequestCookieMap.Literal.INSTANCE).get();

        return requestCookieMap.toString();
    }
}
