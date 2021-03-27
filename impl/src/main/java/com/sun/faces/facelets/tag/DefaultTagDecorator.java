/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import com.sun.faces.facelets.tag.faces.PassThroughAttributeLibrary;
import com.sun.faces.facelets.tag.faces.PassThroughElementLibrary;
import com.sun.faces.facelets.tag.faces.html.HtmlLibrary;

import jakarta.faces.render.Renderer;
import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.FaceletException;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributes;
import jakarta.faces.view.facelets.TagDecorator;

/**
 * A simple tag decorator to enable faces: syntax
 */
class DefaultTagDecorator implements TagDecorator {

    private enum Mapper {
        a(new ElementConverter("h:commandLink", "faces:action"), new ElementConverter("h:commandLink", "faces:actionListener"),
                new ElementConverter("h:outputLink", "faces:value"), new ElementConverter("h:link", "faces:outcome")),

        img("h:graphicImage"), body("h:body"), head("h:head"), label("h:outputLabel"), script("h:outputScript"), link("h:outputStylesheet"),

        form("h:form"), textarea("h:inputTextarea"),
        // TODO if we want the name of the button to become the id, we have to do .id("name")
        button(new ElementConverter("h:button", "faces:outcome"), new ElementConverter("h:commandButton")),

        select(new ElementConverter("h:selectManyListbox", "multiple").id("name"),
                // TODO this is a little bit ugly to handle the name as if it were faces:id. we should not support this
                new ElementConverter("h:selectOneListbox").id("name")),

        input(new ElementConverter("h:inputText", "type")
                // TODO this is a little bit ugly to handle the name as if it were faces:id. we should not support this
                .id("name").map("hidden", "inputHidden").map("password", "inputSecret").map("number", "inputText").map("search", "inputText")
                .map("email", "inputText").map("datetime", "inputText").map("date", "inputText").map("month", "inputText").map("week", "inputText")
                .map("time", "inputText").map("datetime-local", "inputText").map("range", "inputText").map("color", "inputText").map("url", "inputText")
                .map("checkbox", "selectBooleanCheckbox").map("file", "inputFile").map("submit", "commandButton").map("reset", "commandButton")
                .map("button", "button"));

        private ElementConverter elementConverter;

        private Mapper(final ElementConverter... elementConverters) {
            if (elementConverters.length == 1) {
                elementConverter = elementConverters[0];
            } else {
                elementConverter = new ElementConverter() {
                    @Override
                    public Tag decorate(Tag tag) {
                        for (ElementConverter converter : elementConverters) {
                            Tag decorated = converter.decorate(tag);
                            if (decorated != null) {
                                return decorated;
                            }
                        }
                        return null;
                    }
                };
            }
        }

        private Mapper(String faceletTag) {
            elementConverter = new ElementConverter(faceletTag);
        }
    }

    private enum Namespace {
        p(PassThroughAttributeLibrary.DEFAULT_NAMESPACE), faces(PassThroughElementLibrary.DEFAULT_NAMESPACE), h(HtmlLibrary.DEFAULT_NAMESPACE);

        private String uri;

        Namespace(String uri) {
            this.uri = uri;
        }
    }

    private ElementConverter defaultElementConverter = new ElementConverter("faces:element");

    @Override
    public Tag decorate(Tag tag) {
        String ns = tag.getNamespace();
        if (!hasFacesAttribute(tag)) {
            // return immediately, if we have no faces: attribute
            return null;
        }
        // we only handle html tags!
        if (!("".equals(ns) || "http://www.w3.org/1999/xhtml".equals(ns))) {
            throw new FaceletException("Elements with namespace " + ns + " may not have attributes in namespace " + Namespace.faces.uri + "." + " Namespace "
                    + Namespace.faces.uri + " is intended for otherwise non-JSF-aware markup, such as <input type=\"text\" " + Namespace.faces.name() + ":id >"
                    + " It is not valid to have <h:commandButton faces:id=\"button\" />.");
        }
        for (Mapper mapper : Mapper.values()) {
            if (tag.getLocalName().equals(mapper.name())) {
                return mapper.elementConverter.decorate(tag);
            }
        }

        return defaultElementConverter.decorate(tag);
    }

    private boolean hasFacesAttribute(Tag tag) {
        for (String ns : tag.getAttributes().getNamespaces()) {
            if (PassThroughElementLibrary.NAMESPACES.contains(ns)) {
                return true;
            }
        }
        return false;
    }

    private static class ElementConverter implements TagDecorator {
        private String localName;
        private Namespace namespace;
        private String arbiterAttributeName;
        private String arbiterAttributeNamespace = "";
        private Map<String, String> additionalMappings = new HashMap<>();
        private String otherHtmlIdAttribute;

        private ElementConverter() {
            super();
        }

        private ElementConverter(String faceletsTag) {
            this(faceletsTag, null);
        }

        private ElementConverter(String faceletsTag, String arbiterAttributeName) {
            String[] strings = faceletsTag.split(":");
            namespace = Namespace.valueOf(strings[0]);
            localName = strings[1];
            this.arbiterAttributeName = arbiterAttributeName;

            if (arbiterAttributeName != null && arbiterAttributeName.indexOf(':') > 0) {
                strings = arbiterAttributeName.split(":");
                arbiterAttributeNamespace = Namespace.valueOf(strings[0]).uri;
                this.arbiterAttributeName = strings[1];
            }
        }

        private ElementConverter map(String arbiterAttributeValue, String faceletsTagLocalName) {
            additionalMappings.put(arbiterAttributeValue, faceletsTagLocalName);
            return this;
        }

        private ElementConverter id(String otherHtmlIdAttribute) {
            this.otherHtmlIdAttribute = otherHtmlIdAttribute;
            return this;
        }

        @Override
        public Tag decorate(Tag tag) {
            if (arbiterAttributeName == null) {
                // no arbiter
                return convertTag(tag, namespace, localName);
            }

            TagAttribute arbiterAttribute = tag.getAttributes().get(arbiterAttributeNamespace, arbiterAttributeName);

            if (arbiterAttribute == null) {
                // no arbiter
                return null;// convertTag(tag, namespace, localName);
            }

            // PENDING
            /**
             * if (!arbiterAttribute.isLiteral()) { // TODO should we throw an exception here? }
             **/

            String myLocalName = additionalMappings.get(arbiterAttribute.getValue());

            if (myLocalName == null) {
                myLocalName = localName;
            }

            return convertTag(tag, namespace, myLocalName);
        }

        protected Tag convertTag(Tag tag, Namespace namespace, String localName) {
            Location location = tag.getLocation();
            String ns = namespace.uri;
            String qName = namespace.name() + ":" + localName;

            TagAttributes attributes = convertAttributes(tag.getAttributes());

            Tag converted = new Tag(location, ns, localName, qName, attributes);

            for (TagAttribute tagAttribute : attributes.getAll()) {
                // set the correct tag
                tagAttribute.setTag(converted);
            }

            return converted;
        }

        protected TagAttributes convertAttributes(TagAttributes original) {
            Map<String, TagAttribute> attributes = new HashMap<>();
            TagAttribute elementName = createElementName(original.getTag());
            attributes.put(elementName.getQName(), elementName);

            for (TagAttribute attribute : original.getAll()) {
                TagAttribute converted = convertTagAttribute(attribute);
                // avoid duplicates
                attributes.put(converted.getQName(), converted);
            }

            return new TagAttributesImpl(attributes.values().toArray(new TagAttribute[attributes.size()]));
        }

        private TagAttribute createElementName(Tag tag) {
            Location location = tag.getLocation();
            String ns = Namespace.p.uri;
            String myLocalName = Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY;
            String qName = "p:" + myLocalName;
            String value = tag.getLocalName();

            return new TagAttributeImpl(location, ns, myLocalName, qName, value);
        }

        protected TagAttribute convertTagAttribute(TagAttribute attribute) {
            Location location = attribute.getLocation();
            String ns = attribute.getNamespace();
            String myLocalName = attribute.getLocalName();
            String qName;
            String value = attribute.getValue();

            if (PassThroughElementLibrary.NAMESPACES.contains(attribute.getNamespace())) {
                // make this a component attribute
                qName = myLocalName;
                ns = "";
            } else {
                if (ns.length() != 0 && !ns.equals(attribute.getTag().getNamespace())) {
                    // the attribute has a different namespace than the tag. preserve it.
                    return attribute;
                }
                if (attribute.getLocalName().equals(otherHtmlIdAttribute)) {
                    // special case for input name
                    qName = "id";
                    myLocalName = "id";
                } else {
                    // make this a pass through attribute
                    qName = "p:" + myLocalName;
                    ns = Namespace.p.uri;
                }
            }
            return new TagAttributeImpl(location, ns, myLocalName, qName, value);
        }
    }

}
