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

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2">A</span> set of TagAttributes, usually representing all attributes on a Tag.
 * </p>
 *
 * @since 2.0
 */
public abstract class TagAttributes {

    /**
     * Return an array of all TagAttributes in this set
     *
     * @return a non-null array of TagAttributes
     */
    public abstract TagAttribute[] getAll();

    /**
     * Using no namespace, find the TagAttribute
     *
     * @see #get(String, String)
     * @param localName tag attribute name
     * @return the TagAttribute found, otherwise null
     */
    public abstract TagAttribute get(String localName);

    /**
     * Find a TagAttribute that matches the passed namespace and local name.
     *
     * @param ns namespace of the desired attribute
     * @param localName local name of the attribute
     * @return a TagAttribute found, otherwise null
     */
    public abstract TagAttribute get(String ns, String localName);

    /**
     * Get all TagAttributes for the passed namespace
     *
     * @param namespace namespace to search
     * @return a non-null array of TagAttributes
     */
    public abstract TagAttribute[] getAll(String namespace);

    /**
     * A list of Namespaces found in this set
     *
     * @return a list of Namespaces found in this set
     */
    public abstract String[] getNamespaces();

    /**
     * <p class="changed_added_2_2">
     * A reference to the Tag for which this class represents the attributes. For compatibility with previous
     * implementations, an implementation is provided that returns {@code null}.
     * </p>
     *
     * @since 2.2
     *
     * @return the {@link Tag} for which this class represents the attributes.
     */
    public Tag getTag() {
        return null;
    }

    /**
     * <p class="changed_added_2_2">
     * Set a reference to the Tag for which this class represents the attributes. The VDL runtime must ensure that this
     * method is called before any {@link FaceletHandler}s for this element are instantiated. For compatibility with
     * previous implementations, a no-op implementation is provided.
     * </p>
     *
     * @param tag the parent tag.
     * @since 2.2
     *
     */
    public void setTag(Tag tag) {
    }
}
