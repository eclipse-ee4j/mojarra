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

package com.sun.faces.facelets.tag.faces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.sun.faces.facelets.tag.MetaRulesetImpl;
import com.sun.faces.facelets.tag.TagAttributeImpl;
import com.sun.faces.facelets.tag.TagAttributesImpl;

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;

/**
 * A pass-through attribute belongs to the component's pass-through attribute map alone, where
 * {@code ComponentSupport.copyPassthroughAttributes} puts it. The meta rules key a tag's attributes by local name, so
 * unless the pass-through namespaces are excluded a {@code p:data-row} is applied as an ordinary attribute as well:
 * stored a second time under {@code data-row}, listed in {@code attributesThatAreSet} -- which puts it in saved state
 * and in the pass-through render loop, where it can never match -- and, when its local name is a real property's,
 * displacing that property's own attribute.
 */
class PassthroughAttributeMetadataTest {

    private static final String HTML = "jakarta.faces.html";
    private static final String PASSTHROUGH = "jakarta.faces.passthrough";
    private static final String PASSTHROUGH_JCP = "http://xmlns.jcp.org/jsf/passthrough";
    private static final String ATTRIBUTES_THAT_ARE_SET = "jakarta.faces.component.UIComponentBase.attributesThatAreSet";

    @Test
    void aPassthroughAttributeIsNotAppliedAsAnOrdinaryAttribute() {
        HtmlPanelGroup component = apply(attribute(PASSTHROUGH, "data-row", "1"),
                attribute(PASSTHROUGH_JCP, "data-legacy", "2"));

        assertNull(component.getAttributes().get("data-row"));
        assertNull(component.getAttributes().get("data-legacy"));
        // Not containsKey: AttributesMap answers that unconditionally for this key.
        assertNull(component.getAttributes().get(ATTRIBUTES_THAT_ARE_SET));
    }

    @Test
    void anOrdinaryAttributeIsStillApplied() {
        HtmlPanelGroup component = apply(attribute(HTML, "layout", "block"));

        assertEquals("block", component.getLayout());
    }

    @Test
    void aPassthroughAttributeDoesNotDisplaceThePropertyOfTheSameName() {
        // The rules key attributes by local name, so the pass-through one would otherwise overwrite the property's.
        HtmlPanelGroup component = apply(attribute(HTML, "layout", "block"), attribute(PASSTHROUGH, "layout", "inline"));

        assertEquals("block", component.getLayout());
    }

    private static HtmlPanelGroup apply(TagAttribute... attributes) {
        // A literal property is coerced to the setter's type through the context's expression factory.
        FaceletContext context = mock(FaceletContext.class);
        when(context.getExpressionFactory()).thenReturn(ExpressionFactory.newInstance());

        HtmlPanelGroup component = new HtmlPanelGroup();
        metadata(attributes).applyMetadata(context, component);
        return component;
    }

    private static Metadata metadata(TagAttribute... attributes) {
        Tag tag = new Tag(new Location("test.xhtml", 1, 1), HTML, "panelGroup", "h:panelGroup",
                new TagAttributesImpl(attributes));
        MetaRulesetImpl ruleset = new MetaRulesetImpl(tag, HtmlPanelGroup.class);
        ruleset.addRule(ComponentRule.Instance);
        return ruleset.finish();
    }

    private static TagAttribute attribute(String namespace, String localName, String value) {
        return new TagAttributeImpl(new Location("test.xhtml", 1, 1), namespace, localName, localName, value);
    }
}
