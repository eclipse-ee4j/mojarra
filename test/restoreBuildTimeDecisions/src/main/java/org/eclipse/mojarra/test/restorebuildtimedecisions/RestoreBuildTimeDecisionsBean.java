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
package org.eclipse.mojarra.test.restorebuildtimedecisions;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/**
 * A request scoped bean whose {@code shown} flag a build time condition evaluates over. The flag is therefore true
 * during the render which puts the guarded input in the response, and false again during the restore of the postback
 * which submits it.
 */
@Named
@RequestScoped
public class RestoreBuildTimeDecisionsBean {

    /**
     * The key of an entry which cannot be saved with the state, so that an iteration over it cannot be reproduced.
     */
    public static class UnsavableKey {

        private final String name;

        public UnsavableKey(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private boolean shown;
    private String value;
    private String otherValue;
    private final Map<Integer, String> values = new LinkedHashMap<>();

    private final Map<UnsavableKey, String> unsavable = unsavable();

    private static Map<UnsavableKey, String> unsavable() {
        Map<UnsavableKey, String> unsavable = new LinkedHashMap<>();
        unsavable.put(new UnsavableKey("first"), "a");
        unsavable.put(new UnsavableKey("second"), "b");
        return unsavable;
    }

    public void show() {
        shown = true;
    }

    public boolean isShown() {
        return shown;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOtherValue() {
        return otherValue;
    }

    public void setOtherValue(String otherValue) {
        this.otherValue = otherValue;
    }

    public Map<Integer, String> getValues() {
        return values;
    }

    public Map<UnsavableKey, String> getUnsavable() {
        return unsavable;
    }
}
