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
package org.glassfish.mojarra.test.impl.restorebuildtimedecisionsafteredit;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/**
 * What the two blocks of a page which {@link SwapBlocksServlet} swaps decide on. Each block decides differently from the other, and the same way on every
 * request, so that a block which replays the decision of the other block builds what the other block built.
 */
@Named
@RequestScoped
public class SwappedBlocksBean {

    private boolean shown;
    private String valueA;
    private String valueB;
    private final Map<String, String> values = new TreeMap<>();

    /**
     * Shows the input which a build time condition guards, for the render of this request only.
     */
    public void show() {
        shown = true;
    }

    public boolean isShown() {
        return shown;
    }

    public boolean isA() {
        return true;
    }

    public boolean isB() {
        return false;
    }

    public List<String> getFoos() {
        return List.of("f0", "f1");
    }

    public List<String> getBars() {
        return List.of("b0", "b1", "b2");
    }

    public List<Map<String, Object>> getFlagged() {
        return List.of(flagged("a", true), flagged("b", false), flagged("c", false), flagged("d", true));
    }

    public List<Map<String, Object>> getOther() {
        return List.of(flagged("o0", true), flagged("o1", false));
    }

    public List<Map<String, Object>> getNone() {
        return List.of();
    }

    private static Map<String, Object> flagged(String name, boolean flag) {
        return Map.of("name", name, "flag", flag);
    }

    public String getValueA() {
        return valueA;
    }

    public void setValueA(String valueA) {
        this.valueA = valueA;
    }

    public String getValueB() {
        return valueB;
    }

    public void setValueB(String valueB) {
        this.valueB = valueB;
    }

    public Map<String, String> getValues() {
        return values;
    }

}
