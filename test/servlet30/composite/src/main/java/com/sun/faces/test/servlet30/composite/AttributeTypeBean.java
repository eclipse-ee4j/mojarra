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

package com.sun.faces.test.servlet30.composite;

import java.util.Map;
import javax.el.ELContext;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name = "attributeTypeBean")
@RequestScoped
public class AttributeTypeBean {

    private String test;

    public String from(Map<String, Object> attrs) {
        FacesContext context = FacesContext.getCurrentInstance();
        ELContext elc = context.getELContext();
        Class<?> type = elc.getELResolver().getType(elc, attrs, test);
        return String.format("type of @%s: %s", test, type == null ? null : type.getSimpleName());
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public static abstract class Animal {
    }

    public static abstract class Dog extends Animal {
    }

    public static class Wienerdoodle extends Dog {
    }

    private final Wienerdoodle dog = new Wienerdoodle();

    public Animal getAnimal() {
        return dog;
    }

    public Dog getDog() {
        return dog;
    }

    public Wienerdoodle getWienerdoodle() {
        return dog;
    }

    public Dog getLostDog() {
        return null;
    }
}
