/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.view.facelets;

/**
 * <p class="changed_added_2_0">There are concrete subclasses within the
 * implementation that map concepts in the Facelet VDL page to JSF Java API
 * calls the appropriate instances. For example, the
 * &#8220;<code>validator</code>&#8221; attribute on an input component is
 * specified to point to a <code>MethodExpression</code> that references a
 * method that performs the validation. There is a concrete subclass of
 * <code>Metadata</code> to perform this action when that attribute appears in a
 * Facelet VDL page.</p>
 *
 * @since 2.0
 */
public abstract class Metadata {

    /**
     * <p class="changed_added_2_0">Take the action prescribed in the JSF
     * specification for this particular VDL element attribute.</p>
     *
     * @param ctx The FaceletContext for this request.
     * @param instance The instance from the JSF Java API on which the action
     * should be taken. For example, an instance of {@link
     * javax.faces.component.EditableValueHolder}.
     */
    public abstract void applyMetadata(FaceletContext ctx, Object instance);
}
