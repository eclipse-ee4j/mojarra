/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <span class="changed_modified_2_2">The</span> parent or root object in a FaceletHandler composition. The Facelet will
 * take care of populating the passed UIComponent parent in relation to the create/restore lifecycle of Jakarta Server
 * Faces.
 * </p>
 *
 */
public abstract class Facelet {

    /**
     * <p>
     * <span class="changed_added_4_0">The</span> passed UIComponent parent will be populated/restored in accordance with
     * the 
     * section 10.2.1 "Specification of the ViewDeclarationLanguage Implementation for Facelets for Jakarta Faces" in the Jakarta Faces Specification Document 
     * with only the meta data as per the <code>f:metadata</code> tag.
     * </p>
     *
     * @param facesContext The current FacesContext (Should be the same as FacesContext.getInstance())
     * @param parent The UIComponent to populate in a compositional fashion. In most cases a Facelet will be base a
     * UIViewRoot.
     *
     * @throws IOException if unable to load a file necessary to apply this {@code Facelet}
     * @throws FaceletException if unable to parse the markup loaded in applying this {@code Facelet}
     * @throws jakarta.faces.FacesException if unable to create child <code>UIComponent</code> instances
     * @throws jakarta.el.ELException if any of the expressions in the markup loaded during the apply fail
     *
     */
    public void applyMetadata(FacesContext facesContext, UIComponent parent) throws IOException {
        // By default, do nothing
    }

    /**
     * <p>
     * <span class="changed_modified_2_2">The</span> passed UIComponent parent will be populated/restored in accordance with
     * the section 10.2.1 "Specification of the ViewDeclarationLanguage Implementation for Facelets for Jakarta Faces" in the Jakarta Faces Specification Document.
     * </p>
     *
     * @param facesContext The current FacesContext (Should be the same as FacesContext.getInstance())
     * @param parent The UIComponent to populate in a compositional fashion. In most cases a Facelet will be base a
     * UIViewRoot.
     *
     * @throws IOException if unable to load a file necessary to apply this {@code Facelet}
     * @throws FaceletException if unable to parse the markup loaded in applying this {@code Facelet}
     * @throws jakarta.faces.FacesException if unable to create child <code>UIComponent</code> instances
     * @throws jakarta.el.ELException if any of the expressions in the markup loaded during the apply fail
     *
     */
    public abstract void apply(FacesContext facesContext, UIComponent parent) throws IOException;
}
