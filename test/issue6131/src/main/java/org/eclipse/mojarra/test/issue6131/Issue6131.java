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
package org.eclipse.mojarra.test.issue6131;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue6131 {

    private final List<String> groups = List.of("group_0", "group_1", "group_2");
    private final List<Integer> numbers = List.of(0, 1, 2);
    private final Map<String, Object> values = new LinkedHashMap<>();
    private Object single;

    public List<String> getGroups() {
        return groups;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public Object getSingle() {
        return single;
    }

    public void setSingle(Object single) {
        this.single = single;
    }
}
