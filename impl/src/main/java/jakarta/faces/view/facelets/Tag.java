/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2005-2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jakarta.faces.view.facelets;

import jakarta.faces.view.Location;

/**
 * <p class="changed_added_2_0">
 * The runtime must create an instance of this class for each element in the Facelets XHTML view. A {@link TagConfig}
 * subinterface instance is responsible for providing an instance of <code>Tag</code> to the {@link TagHandler} instance
 * that is passed the <code>TagConfig</code> in its constructor.
 * </p>
 *
 * @since 2.0
 */
public final class Tag {

    /**
     * Stores the tag attributes.
     */
    private final TagAttributes attributes;

    /**
     * Stores the location.
     */
    private final Location location;

    /**
     * Stores the XML namespace.
     */
    private final String namespace;

    /**
     * Stores the XML local name.
     */
    private final String localName;

    /**
     * Stores the XML qualified name.
     */
    private final String qName;

    /**
     * Constructor.
     *
     * @param location the location.
     * @param namespace the XML namespace.
     * @param localName the XML local name.
     * @param qName the XML qualified name.
     * @param attributes the tag attributes.
     */
    public Tag(Location location, String namespace, String localName, String qName, TagAttributes attributes) {
        this.location = location;
        this.namespace = namespace;
        this.localName = localName;
        this.qName = qName;
        this.attributes = attributes;
    }

    /**
     * Constructor.
     *
     * @param orig the original tag.
     * @param attributes the tag attributes.
     */
    public Tag(Tag orig, TagAttributes attributes) {
        this(orig.getLocation(), orig.getNamespace(), orig.getLocalName(), orig.getQName(), attributes);
    }

    /**
     * <p class="changed_added_2_0">
     * Return an object encapsulating the {@link TagAttributes} specified on this element in the view.
     * </p>
     *
     * @return the {@link TagAttributes}.
     */
    public TagAttributes getAttributes() {
        return attributes;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the XML local name of the tag. For example, &lt;my:tag /&gt; would be "tag".
     * </p>
     *
     * @return the XML local name.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the Location of this <code>Tag</code> instance in the Facelet view.
     * </p>
     *
     * @return the Location.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the resolved XML Namespace for this tag in the Facelets view.
     * </p>
     *
     * @return the XML namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the XML qualified name for this tag. For example, &lt;my:tag /&gt; would be "my:tag".
     *
     * @return the XML qualified name.
     */
    public String getQName() {
        return qName;
    }

    /**
     * Get the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return location + " <" + qName + ">";
    }
}
