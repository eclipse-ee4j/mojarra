/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue5837;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;

class Issue5837IT extends BaseIT {

    /**
     * A DOM event handler attribute set via a (non-literal) expression must be rendered exactly once, even when the
     * renderer handles that event specially (chained submit/behavior script). Regression from #5723.
     *
     * https://github.com/eclipse-ee4j/mojarra/issues/5837
     */
    @Test
    void testEventHandlerAttributeRenderedOnce() {
        String body = getResponseBody("issue5837.xhtml");

        assertEquals(1, countAttributeInTag(body, "form:commandLink", "onclick"), body);
        assertEquals(1, countAttributeInTag(body, "form:commandButton", "onclick"), body);
        assertEquals(1, countAttributeInTag(body, "form:checkbox", "onclick"), body);
        assertEquals(1, countAttributeInTag(body, "form:input", "onchange"), body);
        assertEquals(1, countAttributeInTag(body, "form:commandLinkLiteral", "onclick"), body);
    }

    /**
     * Counts how many times the given attribute occurs in the opening tag of the element with the given id.
     */
    private static int countAttributeInTag(String body, String id, String attribute) {
        int idIndex = body.indexOf("id=\"" + id + "\"");
        if (idIndex == -1) {
            throw new IllegalStateException("No element with id " + id + " in response:\n" + body);
        }

        int tagStart = body.lastIndexOf('<', idIndex);
        int tagEnd = body.indexOf('>', idIndex);
        String tag = body.substring(tagStart, tagEnd);

        int count = 0;
        for (int from = tag.indexOf(attribute + "="); from != -1; from = tag.indexOf(attribute + "=", from + 1)) {
            count++;
        }
        return count;
    }
}
