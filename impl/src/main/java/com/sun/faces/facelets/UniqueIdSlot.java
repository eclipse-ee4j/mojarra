/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

import jakarta.faces.view.facelets.Facelet;
import jakarta.faces.view.facelets.FaceletContext;

/**
 * The unique-id counter slot a tag handler reserves once and reuses for every build.
 * <p>
 * A handler that asks for its id by tag id makes the context count in a map keyed by that id, which is one lookup and,
 * while the map is still growing, a share of a rehash for every tag applied. Reserving a slot moves the counter into an
 * array indexed per tag, so the same id costs an array increment. The slot belongs to the {@link Facelet} it was
 * reserved from, so it is re-reserved whenever a handler is applied from a different one.
 * <p>
 * A handler holds one of these per instance and is shared across concurrent builds, hence the volatile: a stale read
 * only costs a re-reservation, never a wrong id.
 */
public final class UniqueIdSlot {

    private volatile Reservation reservation;

    /**
     * Returns the next unique id for {@code tagId}, through the reserved slot where the context supports it and
     * through the public {@link FaceletContext} API otherwise, which a wrapping context or a foreign implementation
     * may supply.
     *
     * @param ctx the context to generate the id from
     * @param tagId the tag id to generate an id for
     * @return the generated unique id
     */
    public String generateUniqueId(FaceletContext ctx, String tagId) {
        if (!(ctx instanceof FaceletContextImplBase)) {
            return ctx.generateUniqueId(tagId);
        }

        FaceletContextImplBase context = (FaceletContextImplBase) ctx;
        Facelet owner = context.getUniqueIdSlotOwner();

        if (owner == null) {
            return ctx.generateUniqueId(tagId);
        }

        Reservation current = reservation;

        if (current == null || current.owner != owner) {
            current = new Reservation(owner, context.getUniqueIdSlot(tagId));
            reservation = current;
        }

        return context.generateUniqueId(tagId, current.owner, current.slot);
    }

    private static final class Reservation {

        private final Facelet owner;
        private final int slot;

        private Reservation(Facelet owner, int slot) {
            this.owner = owner;
            this.slot = slot;
        }
    }
}
