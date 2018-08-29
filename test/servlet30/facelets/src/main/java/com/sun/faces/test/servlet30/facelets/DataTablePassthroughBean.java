/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.faces.test.servlet30.facelets;

import static java.util.Arrays.asList;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class DataTablePassthroughBean {

    private List<Entity> entities = asList(
            new Entity("name1", new Date()),
            new Entity("name2", new Date(new Date().getTime() + (1000 * 60 * 60 * 24))),
            new Entity("name0", new Date(new Date().getTime() + (1000 * 60 * 60 * 48))));

    public List<Entity> getEntities() {
        return entities;
    }

    public class Entity {

        private String name;
        private Date modifiedOn;

        public Entity(String name, Date modifiedOn) {
            this.name = name;
            this.modifiedOn = modifiedOn;
        }

        public Date getModifiedOn() {
            return modifiedOn;
        }

        public String getName() {
            return name;
        }
    }
}
