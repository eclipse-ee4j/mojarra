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

package com.sun.faces.test.javaee8.converter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Locale;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class BackingBean implements Serializable {
    private static final long serialVersionUID = 1544275452223321526L;

    private Locale locale = new Locale("en", "US");

    public Locale getLocale() {
       return locale;
    }

    private LocalDateTime localDateTime;

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    private LocalDate localDate;

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    private LocalTime localTime;

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    private OffsetTime offsetTime;

    public OffsetTime getOffsetTime() {
        return offsetTime;
    }

    public void setOffsetTime(OffsetTime offsetTime) {
        this.offsetTime = offsetTime;
    }

    private OffsetDateTime offsetDateTime;

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    private ZonedDateTime zonedDateTime;

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    private LocalDateTime localDateTime1;

    public LocalDateTime getLocalDateTime1() {
        return localDateTime1;
    }

    public void setLocalDateTime1(LocalDateTime localDateTime) {
        this.localDateTime1 = localDateTime;
    }

    private LocalDateTime localDateTime2;

    public LocalDateTime getLocalDateTime2() {
        return localDateTime2;
    }

    public void setLocalDateTime2(LocalDateTime localDateTime) {
        this.localDateTime2 = localDateTime;
    }

    private LocalTime localTime1;

    public LocalTime getLocalTime1() {
        return localTime1;
    }

    public void setLocalTime1(LocalTime localTime) {
        this.localTime1 = localTime;
    }

    private LocalTime localTime2;

    public LocalTime getLocalTime2() {
        return localTime2;
    }

    public void setLocalTime2(LocalTime localTime) {
        this.localTime2 = localTime;
    }

}
