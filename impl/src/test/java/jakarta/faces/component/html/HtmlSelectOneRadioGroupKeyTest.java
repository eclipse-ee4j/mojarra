/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package jakarta.faces.component.html;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * {@link HtmlSelectOneRadio} keeps its group under its own {@code PropertyKeys.group}. That enum is protected API, so a
 * subclass may read and write the group through the state helper with that key.
 */
class HtmlSelectOneRadioGroupKeyTest {

    @Test
    void subclassSeesTheGroupUnderTheProtectedKey() {
        Subclass radio = new Subclass();
        radio.setGroup("set");
        assertEquals("set", radio.groupFromStateHelper());

        radio.putGroupInStateHelper("put");
        assertEquals("put", radio.getGroup());
    }

    private static final class Subclass extends HtmlSelectOneRadio {

        Object groupFromStateHelper() {
            return getStateHelper().get(PropertyKeys.group);
        }

        void putGroupInStateHelper(String group) {
            getStateHelper().put(PropertyKeys.group, group);
        }
    }
}
