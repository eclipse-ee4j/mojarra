/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.facelets.tag.composite;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.Resource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.view.Location;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

/**
 * This <code>TagHandler</code> is responsible for relocating children defined within a composite component to a
 * component within the composite component's <code>composite:implementation</code> section.
 */
public class InsertChildrenHandler extends TagHandlerImpl {

    private final Logger LOGGER = FacesLogger.TAGLIB.getLogger();
    private static final String REQUIRED_ATTRIBUTE = "required";
    
    public static final String INDEX_ATTRIBUTE = "InsertChildrenHandler.idx";

    // This attribute is not required. If not defined, then assume the facet
    // isn't necessary.
    private TagAttribute required;

    // ------------------------------------------------------------ Constructors

    public InsertChildrenHandler(TagConfig config) {

        super(config);
        required = getAttribute(REQUIRED_ATTRIBUTE);

    }

    // ------------------------------------------------- Methods from TagHandler

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        UIComponent compositeParent = UIComponent.getCurrentCompositeComponent(ctx.getFacesContext());
        if (compositeParent != null) {
            int count = parent.getChildCount();
            compositeParent.subscribeToEvent(PostAddToViewEvent.class, new RelocateChildrenListener(ctx, parent, count, tag.getLocation()));
        }

    }

    // ----------------------------------------------------------- Inner Classes

    private class RelocateChildrenListener extends RelocateListener {

        private FaceletContext ctx;
        private UIComponent component;
        private int idx;
        private Location location;

        // -------------------------------------------------------- Constructors

        RelocateChildrenListener(FaceletContext ctx, UIComponent component, int idx, Location location) {

            this.ctx = ctx;
            this.component = component;
            if (!component.getAttributes().containsKey(INDEX_ATTRIBUTE)) {
                component.getAttributes().put(INDEX_ATTRIBUTE, idx);
            }
            this.idx = idx;
            this.location = location;

        }

        // --------------------------- Methods from ComponentSystemEventListener

        @Override
        public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {

            UIComponent compositeParent = event.getComponent();
            if (compositeParent == null) {
                return;
            }

            // ensure we're working with the expected composite component as
            // nesting levels may mask this.
            Resource resource = getBackingResource(compositeParent);
            while (compositeParent != null && !resourcesMatch(resource, location)) {
                compositeParent = UIComponent.getCompositeComponentParent(compositeParent);
                if (compositeParent != null) {
                    resource = getBackingResource(compositeParent);
                }
            }

            if (compositeParent == null) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "faces.composite.component.insertchildren.missing.template", location.toString());
                }
                return;
            }

            if (compositeParent.getChildCount() == 0 && isRequired()) {
                throwRequiredException(ctx, compositeParent);
            }

            List<UIComponent> compositeChildren = compositeParent.getChildren();
            List<UIComponent> parentChildren = component.getChildren();

            // store the new parent's info per child in the old parent's attr map
            // <child id, new parent>
            for (UIComponent c : compositeChildren) {
                String key = (String) c.getAttributes().get(ComponentSupport.MARK_CREATED);
                String value = component.getId();
                if (key != null && value != null) {
                    compositeParent.getAttributes().put(key, value);
                }
            }

            if (parentChildren.size() < getIdx()) {
                parentChildren.addAll(compositeChildren);
            } else {
                parentChildren.addAll(getIdx(), compositeChildren);
            }

        }

        // ----------------------------------------------------- Private Methods

        private int getIdx() {
            Integer idx = (Integer) component.getAttributes().get(INDEX_ATTRIBUTE);
            return idx != null ? idx : this.idx;
        }

        private void throwRequiredException(FaceletContext ctx, UIComponent compositeParent) {

            throw new TagException(tag, "Unable to find any children components " + "nested within parent composite component with id '"
                    + compositeParent.getClientId(ctx.getFacesContext()) + '\'');

        }

        private boolean isRequired() {

            return required != null && required.getBoolean(ctx);

        }

    } // END RelocateChildrenListener

}
