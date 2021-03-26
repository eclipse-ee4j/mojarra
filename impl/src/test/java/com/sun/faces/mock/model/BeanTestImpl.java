/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.mock.model;

/**
 * <p>
 * JavaBean for data model tests.</p>
 */
public class BeanTestImpl {

    // -------------------------------------------------------------- Properties
    /**
     * A boolean property.
     */
    private boolean booleanProperty = true;

    public boolean getBooleanProperty() {
        return (booleanProperty);
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    /**
     * A boolean property that uses an "is" method for the getter.
     */
    private boolean booleanSecond = true;

    public boolean isBooleanSecond() {
        return (booleanSecond);
    }

    public void setBooleanSecond(boolean booleanSecond) {
        this.booleanSecond = booleanSecond;
    }

    /**
     * A byte property.
     */
    private byte byteProperty = (byte) 121;

    public byte getByteProperty() {
        return (this.byteProperty);
    }

    public void setByteProperty(byte byteProperty) {
        this.byteProperty = byteProperty;
    }

    /**
     * A double property.
     */
    private double doubleProperty = 321.0;

    public double getDoubleProperty() {
        return (this.doubleProperty);
    }

    public void setDoubleProperty(double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }

    /**
     * A float property.
     */
    private float floatProperty = (float) 123.0;

    public float getFloatProperty() {
        return (this.floatProperty);
    }

    public void setFloatProperty(float floatProperty) {
        this.floatProperty = floatProperty;
    }

    /**
     * An integer property.
     */
    private int intProperty = 123;

    public int getIntProperty() {
        return (this.intProperty);
    }

    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }

    /**
     * A long property.
     */
    private long longProperty = 321;

    public long getLongProperty() {
        return (this.longProperty);
    }

    public void setLongProperty(long longProperty) {
        this.longProperty = longProperty;
    }

    /**
     * A String property with an initial value of null.
     */
    private String nullProperty = null;

    public String getNullProperty() {
        return (this.nullProperty);
    }

    public void setNullProperty(String nullProperty) {
        this.nullProperty = nullProperty;
    }

    /**
     * A read-only String property.
     */
    private String readOnlyProperty = "Read Only String Property";

    public String getReadOnlyProperty() {
        return (this.readOnlyProperty);
    }

    /**
     * A short property.
     */
    private short shortProperty = (short) 987;

    public short getShortProperty() {
        return (this.shortProperty);
    }

    public void setShortProperty(short shortProperty) {
        this.shortProperty = shortProperty;
    }

    /**
     * A String property.
     */
    private String stringProperty = "This is a string";

    public String getStringProperty() {
        return (this.stringProperty);
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    /**
     * A write-only String property.
     */
    private String writeOnlyProperty = "Write Only String Property";

    public String getWriteOnlyPropertyValue() {
        return (this.writeOnlyProperty);
    }

    public void setWriteOnlyProperty(String writeOnlyProperty) {
        this.writeOnlyProperty = writeOnlyProperty;
    }
}
