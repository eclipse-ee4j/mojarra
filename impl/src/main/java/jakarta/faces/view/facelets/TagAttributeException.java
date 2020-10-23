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
 * An Exception caused by a {@link TagAttribute}
 * </p>
 *
 * @since 2.0
 */
public final class TagAttributeException extends FaceletException {

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param attr the {@link TagAttribute}.
     */
    public TagAttributeException(TagAttribute attr) {
        super(attr.toString());
    }

    /**
     * Constructor.
     *
     * @param attr the {@link TagAttribute}.
     * @param message the message.
     */
    public TagAttributeException(TagAttribute attr, String message) {
        super(attr + " " + message);
    }

    /**
     * Constructor.
     *
     * @param attr the {@link TagAttribute}.
     * @param cause the cause.
     */
    public TagAttributeException(TagAttribute attr, Throwable cause) {
        super(attr + " " + cause.getMessage(), cause);
    }

    /**
     * Constructor.
     *
     * @param attr the {@link TagAttribute}.
     * @param message the message.
     * @param cause the cause.
     */
    public TagAttributeException(TagAttribute attr, String message, Throwable cause) {
        super(attr + " " + message, cause);
    }

    /**
     * Constructor.
     *
     * @param tag the {@link Tag}.
     * @param attr the {@link TagAttribute}.
     */
    public TagAttributeException(Tag tag, TagAttribute attr) {
        super(print(tag, attr));
    }

    private static String print(Tag tag, TagAttribute attr) {
        return tag.getLocation() + " <" + tag.getQName() + " " + attr.getQName() + "=\"" + attr.getValue() + "\">";
    }

    /**
     * Constructor.
     *
     * @param tag the {@link Tag}.
     * @param attr the {@link TagAttribute}.
     * @param message the message.
     */
    public TagAttributeException(Tag tag, TagAttribute attr, String message) {
        super(print(tag, attr) + " " + message);
    }

    /**
     * Constructor.
     *
     * @param tag the {@link Tag}.
     * @param attr the {@link TagAttribute}.
     * @param cause the cause.
     */
    public TagAttributeException(Tag tag, TagAttribute attr, Throwable cause) {
        super(print(tag, attr) + " " + cause.getMessage(), cause);
    }

    /**
     * Constructor.
     *
     * @param tag the {@link Tag}.
     * @param attr the {@link TagAttribute}.
     * @param message the message.
     * @param cause the cause.
     */
    public TagAttributeException(Tag tag, TagAttribute attr, String message, Throwable cause) {
        super(print(tag, attr) + " " + message, cause);
    }
}
