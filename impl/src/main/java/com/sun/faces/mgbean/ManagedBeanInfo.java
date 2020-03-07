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

package com.sun.faces.mgbean;

import java.util.List;
import java.util.Map;

import com.sun.faces.el.ELUtils;

/**
 * This class represents the parsed metadata for a <code>managed-bean</code> entry within a faces-config.xml.
 */
public class ManagedBeanInfo {

    public static final String NULL_VALUE = "null_value";

    private String name;
    private String className;
    private String beanScope;
    boolean eager;
    private ManagedBeanInfo.MapEntry mapEntry;
    private ManagedBeanInfo.ListEntry listEntry;
    private List<ManagedBeanInfo.ManagedProperty> managedProperties;
    private Map<String, String> descriptions;

    // ------------------------------------------------------------ Constructors

    public ManagedBeanInfo(String name, String className, String beanScope, ManagedBeanInfo.MapEntry mapEntry, ManagedBeanInfo.ListEntry listEntry,
            List<ManagedBeanInfo.ManagedProperty> managedProperties, Map<String, String> descriptions) {

        this(name, className, beanScope, false, mapEntry, listEntry, managedProperties, descriptions);

    }

    public ManagedBeanInfo(String name, String className, String beanScope, boolean eager, ManagedBeanInfo.MapEntry mapEntry,
            ManagedBeanInfo.ListEntry listEntry, List<ManagedBeanInfo.ManagedProperty> managedProperties, Map<String, String> descriptions) {

        this.name = name;
        this.className = className;
        this.beanScope = beanScope;
        this.eager = eager;
        this.mapEntry = mapEntry;
        this.listEntry = listEntry;
        this.managedProperties = managedProperties;
        this.descriptions = descriptions;

        if (eager && !ELUtils.Scope.APPLICATION.toString().equals(beanScope)) {
            this.eager = false;
        }

    }

    // ---------------------------------------------------------- Public Methods

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public String getScope() {
        return beanScope;
    }

    public boolean isEager() {
        return eager;
    }

    public boolean hasMapEntry() {
        return mapEntry != null;
    }

    public MapEntry getMapEntry() {
        return mapEntry;
    }

    public boolean hasListEntry() {
        return listEntry != null;
    }

    public ListEntry getListEntry() {
        return listEntry;
    }

    public boolean hasManagedProperties() {
        return managedProperties != null;
    }

    public List<ManagedBeanInfo.ManagedProperty> getManagedProperties() {
        return managedProperties;
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public ManagedBeanInfo clone(String name, String scope, boolean eager, ManagedBeanInfo source) {

        return new ManagedBeanInfo(name, source.className, scope, eager, source.mapEntry, source.listEntry, source.managedProperties, source.descriptions);

    }

    // ----------------------------------------------------------- Inner Classes

    public static class MapEntry {

        private String keyClass;
        private String valueClass;
        private Map<String, String> entries;

        public MapEntry(String keyClass, String valueClass, Map<String, String> entries) {

            this.keyClass = keyClass;
            this.valueClass = valueClass;
            this.entries = entries;

        }

        public String getKeyClass() {
            return keyClass;
        }

        public String getValueClass() {
            return valueClass;
        }

        public Map<String, String> getEntries() {
            return entries;
        }

    }

    public static class ListEntry {

        private String valueClass;
        private List<String> values;

        public ListEntry(String valueClass, List<String> values) {

            this.valueClass = valueClass;
            this.values = values;

        }

        public String getValueClass() {
            return valueClass;
        }

        public List<String> getValues() {
            return values;
        }

    }

    public static class ManagedProperty {

        private String propertyAlias;
        private String propertyName;
        private String propertyClass;
        private String propertyValue;
        private ManagedBeanInfo.MapEntry mapEntry;
        private ManagedBeanInfo.ListEntry listEntry;

        public ManagedProperty(String propertyName, String propertyClass, String propertyValue, ManagedBeanInfo.MapEntry mapEntry,
                ManagedBeanInfo.ListEntry listEntry) {

            this.propertyName = propertyName;
            this.propertyClass = propertyClass;
            this.propertyValue = propertyValue;
            this.mapEntry = mapEntry;
            this.listEntry = listEntry;

        }

        public ManagedProperty(String propertyAlias, String propertyName, String propertyClass, String propertyValue, ManagedBeanInfo.MapEntry mapEntry,
                ManagedBeanInfo.ListEntry listEntry) {

            this(propertyName, propertyClass, propertyValue, mapEntry, listEntry);
            this.propertyAlias = propertyAlias;

        }

        public String getPropertyAlias() {
            return propertyAlias;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getPropertyClass() {
            return propertyClass;
        }

        public boolean hasPropertyValue() {
            return propertyValue != null;
        }

        public String getPropertyValue() {
            return propertyValue;
        }

        public boolean hasMapEntry() {
            return mapEntry != null;
        }

        public ManagedBeanInfo.MapEntry getMapEntry() {
            return mapEntry;
        }

        public boolean hasListEntry() {
            return listEntry != null;
        }

        public ManagedBeanInfo.ListEntry getListEntry() {
            return listEntry;
        }

    }
}
