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

import jakarta.faces.FacesException;

/**
 * <p class="changed_added_2_0">
 * An Exception from the Facelet implementation
 * </p>
 *
 * @since 2.0
 */
public class FaceletException extends FacesException {

    private static final long serialVersionUID = 1L;

    /**
     * <p class="changed_added_2_0">
     * Create an empty <code>FaceletException</code>.
     * </p>
     */
    public FaceletException() {
        super();
    }

    /**
     * <p class="changed_added_2_0">
     * Create a <code>FaceletException</code> with argument <code>message</code> as the message.
     * </p>
     *
     * @param message the textual message to display for the exception.
     */
    public FaceletException(String message) {
        super(message);
    }

    /**
     * <p class="changed_added_2_0">
     * Wrap argument <code>cause</code> within this <code>FaceletException</code> instance.
     * </p>
     *
     * @param cause the <code>Throwable</code> to wrap
     */
    public FaceletException(Throwable cause) {
        super(cause);
    }

    /**
     * <p class="changed_added_2_0">
     * Wrap argument <code>cause</code> in a <code>FaceletException</code> instance, with a message given by the argument
     * <code>message</code>.
     * </p>
     *
     * @param message the message for the <code>FacesException</code>
     * @param cause the root cause
     */
    public FaceletException(String message, Throwable cause) {
        super(message, cause);
    }

}
