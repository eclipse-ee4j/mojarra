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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.AttachedObjectHandler;

/**
 * <p class="changed_added_2_0">
 * Root class for all tag handlers that represent attached objetcts in a Facelets page.
 * </p>
 */
public abstract class FaceletsAttachedObjectHandler extends DelegatingMetaTagHandler implements AttachedObjectHandler {

    /**
     * <p>
     * Call through to super
     * </p>
     *
     * @param config configure this handler instance
     */
    public FaceletsAttachedObjectHandler(TagConfig config) {
        super(config);
    }

    /**
     * <p class="changed_added_2_0">
     * Return the underlying handler for this tag handler instance.
     * </p>
     *
     * @return the handler instance
     */
    protected final AttachedObjectHandler getAttachedObjectHandlerHelper() {
        return (AttachedObjectHandler) getTagHandlerDelegate();
    }

    /**
     * <p class="changed_added_2_0">
     * Take the necessary actions to apply the attached object represented by the tag for the concrete subclass of this
     * class to the argument <code>parent</code>.
     * </p>
     *
     * @param ctx the <code>FacesContext</code> for this request
     * @param parent The <code>UIComponent</code> to which this attached object must be applied.
     */
    @Override
    public final void applyAttachedObject(FacesContext ctx, UIComponent parent) {
        getAttachedObjectHandlerHelper().applyAttachedObject(ctx, parent);
    }

    /**
     * <p class="changed_added_2_0">
     * Return the value of the "for" attribute. This enables the runtime to know to which inner component this attached
     * object should be retargeted.
     * </p>
     */
    @Override
    public final String getFor() {
        return getAttachedObjectHandlerHelper().getFor();
    }

}
