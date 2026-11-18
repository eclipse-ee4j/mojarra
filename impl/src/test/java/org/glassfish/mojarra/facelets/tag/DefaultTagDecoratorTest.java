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

package org.glassfish.mojarra.facelets.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.faces.render.Renderer;
import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;

import org.junit.jupiter.api.Test;

/**
 * The default {@link jakarta.faces.view.facelets.TagDecorator} maps an HTML element with a {@code jakarta.faces} attribute to the target tag of the matching
 * row in the localName and selector attribute table, and to {@code faces:element} when no row matches, also when the localName is in the table but none of its
 * selector attributes is present.
 */
class DefaultTagDecoratorTest {

    private static final String FACES = "jakarta.faces";
    private static final String HTML = "jakarta.faces.html";
    private static final String PASSTHROUGH = "jakarta.faces.passthrough";
    private static final Location LOCATION = new Location("test.xhtml", 1, 1);

    @Test
    void anchorWithoutSelectorAttributeIsElement() {
        Tag decorated = decorate("a", htmlAttribute("href", "#"), facesAttribute("rendered", "#{false}"));

        assertTarget(FACES, "element", decorated);
        assertEquals("a", decorated.getAttributes().get(PASSTHROUGH, Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY).getValue());
        assertEquals("#{false}", decorated.getAttributes().get("rendered").getValue());
    }

    @Test
    void anchorWithValueIsOutputLink() {
        assertTarget(HTML, "outputLink", decorate("a", facesAttribute("value", "page.xhtml")));
    }

    @Test
    void inputWithoutTypeIsElement() {
        assertTarget(FACES, "element", decorate("input", facesAttribute("id", "input")));
    }

    @Test
    void inputWithTypeIsInputText() {
        assertTarget(HTML, "inputText", decorate("input", htmlAttribute("type", "text"), facesAttribute("id", "input")));
    }

    @Test
    void buttonWithoutSelectorAttributeIsCommandButton() {
        assertTarget(HTML, "commandButton", decorate("button", facesAttribute("id", "button")));
    }

    @Test
    void elementNotInTableIsElement() {
        assertTarget(FACES, "element", decorate("div", facesAttribute("rendered", "#{false}")));
    }

    private static Tag decorate(String localName, TagAttribute... attributes) {
        Tag tag = new Tag(LOCATION, "", localName, localName, new TagAttributesImpl(attributes));
        tag.getAttributes().setTag(tag);
        return new DefaultTagDecorator().decorate(tag);
    }

    private static TagAttribute facesAttribute(String localName, String value) {
        return new TagAttributeImpl(LOCATION, FACES, localName, "faces:" + localName, value);
    }

    private static TagAttribute htmlAttribute(String localName, String value) {
        return new TagAttributeImpl(LOCATION, "", localName, localName, value);
    }

    private static void assertTarget(String namespace, String localName, Tag decorated) {
        assertEquals(namespace + ":" + localName, decorated.getNamespace() + ":" + decorated.getLocalName());
    }

}
