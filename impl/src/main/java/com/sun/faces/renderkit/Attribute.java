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

package com.sun.faces.renderkit;

/**
 * <p class="changed_added_2_0">
 * </p>
 *
 * @author asmirnov@exadel.com
 *
 */
public class Attribute implements Comparable<Attribute> {

    private final String name;

    private final String[] events;

    /**
     * <p class="changed_added_2_0">
     * </p>
     *
     * @param name
     * @param events
     */
    public Attribute(String name, String[] events) {
        this.name = name;
        this.events = events;
    }

    public static Attribute attr(String name) {
        return new Attribute(name, null);
    }

    public static Attribute attr(String name, String... events) {
        return new Attribute(name, events);
    }

    /**
     * <p class="changed_added_2_0">
     * </p>
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * <p class="changed_added_2_0">
     * </p>
     *
     * @return the events
     */
    public String[] getEvents() {
        return events;
    }

    @Override
    public int compareTo(Attribute o) {
        // Compare attributes by name for a fast search in the RenderKitUtils methods.
        return getName().compareTo(o.getName());
    }

}
