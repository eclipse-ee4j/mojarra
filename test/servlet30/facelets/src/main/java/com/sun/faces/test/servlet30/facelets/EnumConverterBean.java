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

package com.sun.faces.test.servlet30.facelets;

import static com.sun.faces.test.servlet30.facelets.EnumConverterEnum.CHOICE_ONE;
import static com.sun.faces.test.servlet30.facelets.EnumConverterEnum.CHOICE_TWO;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.convert.EnumConverter;
import javax.inject.Named;

@Named
@RequestScoped
public class EnumConverterBean {

    private Map<String, EnumConverterEnum> choices;
    private EnumConverterEnum actualChoice;
    private EnumConverter myEnumConverter = new EnumConverter(EnumConverterEnum.class);

    public EnumConverterBean() {
        choices = new HashMap<String, EnumConverterEnum>(2);

        choices.put("First choice", CHOICE_ONE);
        choices.put("Second choice", CHOICE_TWO);
    }

    public EnumConverter getConverter() {
        return myEnumConverter;
    }

    public EnumConverterEnum getActualChoice() {
        return actualChoice;
    }

    public void setActualChoice(EnumConverterEnum actualChoice) {
        this.actualChoice = actualChoice;
    }

    public Map<String, EnumConverterEnum> getChoices() {
        return choices;
    }
}
