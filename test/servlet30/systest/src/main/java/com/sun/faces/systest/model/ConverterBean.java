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

package com.sun.faces.systest.model;

import com.sun.faces.systest.TestConverter01;

import javax.faces.convert.Converter;

public class ConverterBean extends Object {

    public ConverterBean() {
    }

    private Converter converter = null;

    public Converter getConverter() {
        if (converter == null) {
            return new TestConverter01();
        }
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    private Converter dateTimeConverter = null;

    public Converter getDateTimeConverter() {
        if (dateTimeConverter == null) {
            return new javax.faces.convert.DateTimeConverter();
        }
        return dateTimeConverter;
    }

    public void setDateTimeConverter(Converter dateTimeConverter) {
        this.dateTimeConverter = dateTimeConverter;
    }

    private Converter doubleConverter = null;

    public Converter getDoubleConverter() {
        if (doubleConverter == null) {
            return new javax.faces.convert.DoubleConverter();
        }
        return doubleConverter;
    }

    public void setDoubleConverter(Converter doubleConverter) {
        this.doubleConverter = doubleConverter;
    }

    private Converter numberConverter = null;

    public Converter getNumberConverter() {
        if (numberConverter == null) {
            return new javax.faces.convert.NumberConverter();
        }
        return numberConverter;
    }

    public void setNumberConverter(Converter numberConverter) {
        this.numberConverter = numberConverter;
    }
}
