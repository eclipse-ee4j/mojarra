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

import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import jakarta.faces.annotation.ManagedProperty;

@Named
@RequestScoped
public class InjectManagedPropertyBean {

    @Inject @ManagedProperty("#{managedPropertyBean.myInteger1}")
    private Integer injectedInteger1;

    @Inject @ManagedProperty("#{managedPropertyBean.myInteger2}")
    private Integer injectedInteger2;
    
    @Inject @ManagedProperty("#{managedPropertyBean.myInteger3}")
    private Integer injectedInteger3;

    @Inject @ManagedProperty("#{param['test']}")
    private String testParam;
    
    @Inject @ManagedProperty("#{managedPropertyBean.myStringMap}")
    private Map<String, String> stringMap;

    @Inject @ManagedProperty("#{managedPropertyBean.myIntegerMap}")
    private Map<Integer, Integer> integerMap;

    public Integer getInjectedInteger1() {
        return injectedInteger1;
    }

    public Integer getInjectedInteger2() {
        return injectedInteger2;
    }

    public Integer getInjectedInteger3() {
        return injectedInteger3;
    }

    public String getTestParam() {
        return testParam;
    }
    
    public Map<String, String> getStringMap() {
        return stringMap;
    }

    public Map<Integer, Integer> getIntegerMap() {
        return integerMap;
    }

}
