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
 * An Exception caused by a {@link Tag}
 * </p>
 *
 * @since 2.0
 */
public final class TagException extends FaceletException {

    private static final long serialVersionUID = 1L;

    /**
     * <p class="changed_added_2_0">
     * Wrap the argument <code>tag</code> so the exception can reference its information.
     * </p>
     *
     * @param tag the <code>Tag</code> that caused this exception.
     */
    public TagException(Tag tag) {
        super(tag.toString());
    }

    /**
     * <p class="changed_added_2_0">
     * Wrap the argument <code>tag</code> so the exception can reference its information.
     * </p>
     *
     * @param tag the <code>Tag</code> that caused this exception.
     * @param message a message describing the exception
     */
    public TagException(Tag tag, String message) {
        super(tag + " " + message);
    }

    /**
     * <p class="changed_added_2_0">
     * Wrap the argument <code>tag</code> so the exception can reference its information.
     * </p>
     *
     * @param tag the <code>Tag</code> that caused this exception.
     * @param cause the root cause for this exception.
     */
    public TagException(Tag tag, Throwable cause) {
        super(tag.toString(), cause);
    }

    /**
     * <p class="changed_added_2_0">
     * Wrap the argument <code>tag</code> so the exception can reference its information.
     * </p>
     *
     * @param tag the <code>Tag</code> that caused this exception.
     * @param message a message describing the exception
     * @param cause the root cause for this exception.
     */
    public TagException(Tag tag, String message, Throwable cause) {
        super(tag + " " + message, cause);
    }

}
