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

package com.sun.faces.facelets.tag;

import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.TagAttribute;

/**
 * A tag singles out its pass-through attributes when it is compiled, because every applied component tag is asked for
 * them and almost none has any.
 */
class TagAttributesImplTest {

    private static final String HTML = "jakarta.faces.html";
    private static final String PASSTHROUGH = "jakarta.faces.passthrough";
    private static final String PASSTHROUGH_JCP = "http://xmlns.jcp.org/jsf/passthrough";

    @Test
    void aTagWithoutPassthroughAttributesHasNone() {
        TagAttributesImpl attributes = tag(attribute(HTML, "styleClass"), attribute(HTML, "title"));

        assertEquals(0, attributes.getPassthroughAttributes().length);
    }

    @Test
    void passthroughAttributesAreCollectedInDeclarationOrder() {
        TagAttributesImpl attributes = tag(attribute(PASSTHROUGH, "data-row"), attribute(HTML, "styleClass"),
                attribute(PASSTHROUGH, "data-col"));

        assertArrayEquals(new String[] { "data-row", "data-col" }, localNames(attributes));
    }

    @Test
    void bothPassthroughNamespacesAreCollected() {
        TagAttributesImpl attributes = tag(attribute(PASSTHROUGH_JCP, "data-legacy"), attribute(PASSTHROUGH, "data-row"));

        assertArrayEquals(new String[] { "data-legacy", "data-row" }, localNames(attributes));
    }

    private static String[] localNames(TagAttributesImpl attributes) {
        return stream(attributes.getPassthroughAttributes()).map(TagAttribute::getLocalName).toArray(String[]::new);
    }

    private static TagAttributesImpl tag(TagAttribute... attributes) {
        return new TagAttributesImpl(attributes);
    }

    private static TagAttribute attribute(String namespace, String localName) {
        return new TagAttributeImpl(new Location("test.xhtml", 1, 1), namespace, localName, localName, "value");
    }
}
