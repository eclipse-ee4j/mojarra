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
 * CharacterBean represents the data of an individual character
 * </p>
 */
public class CharacterBean {

    String name;
    SpeciesBean species;

    /**
     * <p>
     * Get the character name
     * </p>
     *
     * @return character name String
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * Set the character name
     * </p>
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * <p>
     * Get the species bean
     * </p>
     *
     * @return species SpeciesBean
     */
    public SpeciesBean getSpecies() {
        return species;
    }

    /**
     * <p>
     * Set the species bean
     * </p>
     *
     * @param species SpeciesBean
     */
    public void setSpecies(SpeciesBean species) {
        this.species = species;
    }

}
