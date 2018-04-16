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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;

/**
 * <p><span class="changed_modified_2_2">The</span> parent or root object
 * in a FaceletHandler composition. The Facelet will take care of
 * populating the passed UIComponent parent in relation to the
 * create/restore lifecycle of JSF.</p>
 * 
 */
public abstract class Facelet {

    /**
     * <p><span class="changed_modified_2_2">The</span> passed
     * UIComponent parent will be populated/restored in accordance with
     * the <span class="changed_modified_2_2">Facelets chapter in the
     * spec prose document.</span></p>
     * 
     * @param facesContext
     *            The current FacesContext (Should be the same as
     *            FacesContext.getInstance())
     * @param parent
     *            The UIComponent to populate in a compositional fashion. In
     *            most cases a Facelet will be base a UIViewRoot.
     * 
     * @throws IOException if unable to load a file necessary to apply this {@code Facelet}

     * @throws FaceletException if unable to parse the markup loaded in applying this {@code Facelet}

     * @throws javax.faces.FacesException if unable to create child <code>UIComponent</code> instances

     * @throws javax.el.ELException if any of the expressions in the markup
     * loaded during the apply fail

     */
    public abstract void apply(FacesContext facesContext, UIComponent parent)
    throws IOException;
}
