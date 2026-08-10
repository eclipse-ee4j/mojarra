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

package com.sun.faces.facelets;

import java.io.IOException;

import com.sun.faces.facelets.impl.IdMapper;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.Facelet;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.FaceletException;

/**
 *
 * @author edburns
 */
public abstract class FaceletContextImplBase extends FaceletContext {

    /**
     * Returns the Facelet a tag handler applying under this context reserves its unique-id counter slot from, or
     * {@code null} when this context generates unique ids by tag id alone. A handler pairs the returned Facelet with
     * the slot it reserves, so that it can tell an unchanged pairing (the common case, where its counter is a plain
     * array index) from one it has to resolve again.
     *
     * @return the Facelet being applied, or {@code null} when slot-based id generation is unsupported
     */
    public Facelet getUniqueIdSlotOwner() {
        return null;
    }

    /**
     * Returns the unique-id counter slot the Facelet returned by {@link #getUniqueIdSlotOwner()} holds for the given
     * tag, for a tag handler to hold onto for as long as it lives. Asking twice for the same tag yields the same slot.
     *
     * @param tagId the tag id to get a slot for
     * @return the tag's slot, or -1 when slot-based id generation is unsupported
     */
    public int getUniqueIdSlot(String tagId) {
        return -1;
    }

    /**
     * Slot-based variant of {@link #generateUniqueId(String)}, generating the same id from a counter held at
     * {@code slot} in {@code owner}'s counter array. Implementations that do not support slot-based generation
     * ignore both and count by tag id.
     *
     * @param base the tag id
     * @param owner the Facelet the slot was reserved from
     * @param slot the reserved slot
     * @return the generated unique id
     */
    public String generateUniqueId(String base, Facelet owner, int slot) {
        return generateUniqueId(base);
    }

    /**
     * Returns the alias the {@link IdMapper} in effect for this build gives to the given id, or the id itself when no
     * mapper is in effect. The mapper is fixed for the duration of a build, so an implementation that resolves it once
     * answers this without the lookup in {@link jakarta.faces.context.FacesContext#getAttributes()} that this default
     * performs per call.
     *
     * @param id the id to alias
     * @return the aliased id
     */
    public String getAliasedId(String id) {
        return IdMapper.getAliasedId(getFacesContext(), id);
    }

    /**
     * Push the passed TemplateClient onto the stack for Definition Resolution
     *
     * @param client the template client
     * @see TemplateClient
     */
    public abstract void pushClient(TemplateClient client);

    /**
     * Pop the last added TemplateClient
     *
     * @param client the template client
     * @see TemplateClient
     */
    public abstract void popClient(TemplateClient client);

    public abstract void extendClient(TemplateClient client);

    /**
     * This method will walk through the TemplateClient stack to resolve and apply the definition for the passed name. If
     * it's been resolved and applied, this method will return true.
     *
     * @param parent the UIComponent to apply to
     * @param name name or null of the definition you want to apply
     * @return true if successfully applied, otherwise false
     * @throws IOException when an I/O exception occurs
     * @throws FaceletException when a Facelet exception occurs
     * @throws FacesException when a Faces exception occurs
     * @throws ELException when an EL exception occurs
     */
    public abstract boolean includeDefinition(UIComponent parent, String name) throws IOException, FaceletException, FacesException, ELException;

}
