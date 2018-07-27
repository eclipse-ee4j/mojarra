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

package com.sun.faces.test.servlet30.charactercombat;

/**
 * <p>
 * SpeciesBean represents the data associated with a species type
 * </p>
 */
public class SpeciesBean {

    String type;
    String language;
    boolean immortal;

    /**
     * <p>
     * Get the species type
     * </p>
     *
     * @return species type String
     */
    public String getType() {
        return type;
    }

    /**
     * <p>
     * Set the species type
     * </p>
     *
     * @param type - species type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * <p>
     * Get the language associated with the species
     * </p>
     *
     * @return species language String
     */
    public String getLanguage() {
        return language;
    }

    /**
     * <p>
     * Set the language associated with the species
     * </p>
     *
     * @param language - species language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * <p>
     * Get the immortal state associated with the species
     * </p>
     *
     * @return species immortal boolean
     */
    public boolean isImmortal() {
        return immortal;
    }

    /**
     * <p>
     * Set the immortal state associated with the species
     * </p>
     *
     * @param immortal - is the species immortal
     */
    public void setImmortal(boolean immortal) {
        this.immortal = immortal;
    }

}
