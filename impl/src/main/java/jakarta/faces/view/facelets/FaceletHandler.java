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

import java.io.IOException;

import jakarta.faces.component.UIComponent;

/**
 * <p class="changed_added_2_0">
 * This is the root class for markup elements in Facelets VDL. Facelets is XHTML, and XHTML is XML, and the root unit of
 * abstraction in XML is the element. A <code>FaceletHandler</code> instance represents an XML element at runtime. Two
 * direct implementations exist to embody the contract for more specific behavior.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <dl>
 *
 * <dt>{@link CompositeFaceletHandler}</dt>
 *
 * <dd>A container for other <code>FaceletHandler</code>s</dd>
 *
 * <dt>{@link TagHandler}</dt>
 *
 * <dd>The foundation class for <code>FaceletHandler</code>s associated with markup in a Facelet document.</dd>
 *
 * </dl>
 *
 * </div>
 *
 * @since 2.0
 */
public interface FaceletHandler {

    /**
     * <p class="changed_added_2_0">
     * Process changes on a particular UIComponent
     * </p>
     *
     * @param ctx the current FaceletContext instance for this execution
     * @param parent the parent UIComponent to operate upon
     *
     * @throws IOException if unable to load <code>relativePath</code>
     *
     * @throws FaceletException if unable to parse the markup loaded from <code>relativePath</code>
     *
     * @throws jakarta.faces.FacesException if unable to create child <code>UIComponent</code> instances
     *
     * @throws jakarta.el.ELException if any of the expressions in the markup loaded from <code>relativePath</code> fail
     *
     * @since 2.0
     *
     */
    void apply(FaceletContext ctx, UIComponent parent) throws IOException;
}
