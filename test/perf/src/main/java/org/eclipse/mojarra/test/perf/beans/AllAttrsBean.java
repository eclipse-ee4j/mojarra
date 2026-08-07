/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package org.eclipse.mojarra.test.perf.beans;

import java.io.Serializable;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Named;

/**
 * Backs the {@code flat-allattrs} scenario, which sets every attribute the HTML taglib declares on every component it
 * declares. The attributes that cannot take a literal need a target here: a value of the type each component expects,
 * and a method for each listener signature.
 * <p>
 * Application scoped and immutable, so reading any of it costs one CDI lookup and no state: what the scenario measures
 * is the attribute machinery, not the beans behind it.
 */
@Named("allAttrs")
@ApplicationScoped
public class AllAttrsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final List<String> ITEMS = List.of("alpha", "beta", "gamma");

    /**
     * Backs every {@code rendered} attribute. An expression rather than a literal on purpose: a literal is folded at
     * tag-compile time, so it would never reach the per-component expression path the scenario exists to exercise.
     */
    public boolean isRendered() {
        return true;
    }

    public String getText() {
        return "text";
    }

    public boolean isFlag() {
        return true;
    }

    public String getChoice() {
        return "alpha";
    }

    public List<String> getChoices() {
        return ITEMS;
    }

    public List<String> getItems() {
        return ITEMS;
    }

    public String action() {
        return null;
    }

    public void actionListener(ActionEvent event) {
        // no-op: the scenario measures attribute handling, not what a listener does
    }

    public void valueChanged(ValueChangeEvent event) {
        // no-op: see actionListener
    }
}
