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
 * <span class="changed_modified_2_0_rev_a">Foundation</span> class for <code>FaceletHandler</code>s associated with a
 * markup element in a Facelet document. This class introduces the concept of <a href="TagAttribute.html">XML
 * attributes</a> to Facelets. See the <a href="#TagHandler(jakarta.faces.view.facelets.TagConfig)">constructor</a>
 * documentation for more details.
 * </p>
 *
 *
 * @since 2.0
 */
public abstract class TagHandler implements FaceletHandler {

    /**
     * <p class="changed_added_2_0_rev_a">
     * See {@link TagConfig#getTagId}.
     * </p>
     */
    protected final String tagId;

    /**
     * <p class="changed_added_2_0_rev_a">
     * A reference to the <code>Tag</code> instance corresponding to this <code>TagHandler</code> instance.
     * </p>
     */
    protected final Tag tag;

    /**
     * <p class="changed_added_2_0_rev_a">
     * A reference to the <code>FaceletHandler</code> that represents the first nested child of this <code>TagHandler</code>
     * instance.
     * </p>
     */
    protected final FaceletHandler nextHandler;

    /**
     * <p class="changed_added_2_0">
     * Every <code>TagHandler</code> instance is associated with a {@link Tag}. Each <code>Tag</code> instance has a
     * {@link TagAttributes} property, which is simply a collection of {@link TagAttribute} instances. Extract and save as
     * protected instance variables the {@link TagConfig#getTagId}, {@link TagConfig#getTag} and
     * {@link TagConfig#getNextHandler} returns from the argument <code>TagConfig</code>. This constructor is only called
     * when the Facelets View is compiled.
     * </p>
     *
     * @param config The structure that contains useful to the operation of this instance.
     */
    public TagHandler(TagConfig config) {
        tagId = config.getTagId();
        tag = config.getTag();
        nextHandler = config.getNextHandler();
    }

    /**
     * Utility method for fetching the appropriate TagAttribute
     *
     * @param localName name of attribute
     * @return TagAttribute if found, otherwise null
     */
    protected final TagAttribute getAttribute(String localName) {
        return tag.getAttributes().get(localName);
    }

    /**
     * Utility method for fetching a required TagAttribute
     *
     * @param localName name of the attribute
     * @return TagAttribute if found, otherwise error
     * @throws TagException if the attribute was not found
     */
    protected final TagAttribute getRequiredAttribute(String localName) throws TagException {
        TagAttribute attr = getAttribute(localName);
        if (attr == null) {
            throw new TagException(tag, "Attribute '" + localName + "' is required");
        }

        return attr;
    }

    @Override
    public String toString() {
        return tag.toString();
    }
}
