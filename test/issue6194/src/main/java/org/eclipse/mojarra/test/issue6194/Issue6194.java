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
package org.eclipse.mojarra.test.issue6194;

import java.io.Serializable;
import java.util.List;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Backs views whose c:if inside a c:forEach admits either all rows or only some of them.
 */
@Named
@ViewScoped
public class Issue6194 implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Integer> items = List.of(1, 2, 3, 4, 5, 6);

    private final List<List<Integer>> groups = List.of(List.of(1, 2), List.of(3, 4), List.of(5, 6));

    private boolean evenOnly;

    private boolean skipFirstGroup;

    public List<Integer> getItems() {
        return items;
    }

    public List<List<Integer>> getGroups() {
        return groups;
    }

    public boolean isEvenOnly() {
        return evenOnly;
    }

    public void setEvenOnly(boolean evenOnly) {
        this.evenOnly = evenOnly;
    }

    public boolean isSkipFirstGroup() {
        return skipFirstGroup;
    }

    public void setSkipFirstGroup(boolean skipFirstGroup) {
        this.skipFirstGroup = skipFirstGroup;
    }
}
