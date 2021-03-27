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
import java.util.Map;
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
 * This <code>TagHandler</code> is responsible for relocating Facets defined within a composite component to a component
 * within the composite component's <code>composite:implementation</code> section.
 */
public class InsertFacetHandler extends TagHandlerImpl {

    private final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    // Supported attribute names
    private static final String NAME_ATTRIBUTE = "name";

    private static final String REQUIRED_ATTRIBUTE = "required";

    // Attributes

    // This attribute is required.
    private TagAttribute name;

    // This attribute is not required. If it's not defined or false,
    // then the facet associated with name need not be present in the
    // using page.
    private TagAttribute required;

    // ------------------------------------------------------------ Constructors

    public InsertFacetHandler(TagConfig config) {

        super(config);
        name = getRequiredAttribute(NAME_ATTRIBUTE);
        required = getAttribute(REQUIRED_ATTRIBUTE);

    }

    // ------------------------------------------------- Methods from TagHandler

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        UIComponent compositeParent = UIComponent.getCurrentCompositeComponent(ctx.getFacesContext());

        if (compositeParent != null) {
            compositeParent.subscribeToEvent(PostAddToViewEvent.class, new RelocateFacetListener(ctx, parent, tag.getLocation()));
        }

    }

    // ----------------------------------------------------------- Inner Classes

    private class RelocateFacetListener extends RelocateListener {

        private FaceletContext ctx;
        private UIComponent component;
        private Location location;

        // -------------------------------------------------------- Constructors

        RelocateFacetListener(FaceletContext ctx, UIComponent component, Location location) {

            this.ctx = ctx;
            this.component = component;
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
                    LOGGER.log(Level.WARNING, "faces.composite.component.insertfacet.missing.template", location.toString());
                }
                return;
            }
            boolean req = isRequired();
            String facetName = name.getValue(ctx);
            if (compositeParent.getFacetCount() == 0 && req) {
                throwRequiredException(ctx, facetName, compositeParent);
            }

            Map<String, UIComponent> facets = compositeParent.getFacets();
            UIComponent facet = facets.remove(facetName);
            if (facet == null) {
                facet = compositeParent.getParent().getFacets().remove(facetName);
            }
            if (facet != null) {
                component.getFacets().put(facetName, facet);

                String key = (String) facet.getAttributes().get(ComponentSupport.MARK_CREATED);

                String value = component.getId();
                if (key != null && value != null) {
                    // store the new parent's info per child in the old parent's attr map
                    compositeParent.getAttributes().put(key, value);
                }

            } else {
                // In the case of full state saving, the compositeParent won't
                // have the facet to be relocated as its own - it will have already
                // been made a facet of the target component, so we need
                // to only throw the Exception if required, and the target component
                // doesn't have the facet defined
                if (req && component.getFacets().get(facetName) == null) {
                    throwRequiredException(ctx, facetName, compositeParent);
                }
            }

        }

        // ----------------------------------------------------- Private Methods

        private void throwRequiredException(FaceletContext ctx, String facetName, UIComponent compositeParent) {

            throw new TagException(tag, "Unable to find facet named '" + facetName + "' in parent composite component with id '"
                    + compositeParent.getClientId(ctx.getFacesContext()) + '\'');

        }

        private boolean isRequired() {

            return required != null && required.getBoolean(ctx);

        }

    } // END RelocateFacetListener

}
