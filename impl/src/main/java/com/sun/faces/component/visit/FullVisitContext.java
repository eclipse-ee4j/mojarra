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

package com.sun.faces.component.visit;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;

/**
 *
 * <p class="changed_added_2_0">
 * A VisitContext implementation that is used when performing a full component tree visit.
 * </p>
 *
 * RELEASE_PENDING
 *
 * @since 2.0
 */
public class FullVisitContext extends VisitContext {

    /**
     * Creates a FullVisitorContext instance.
     *
     * @param facesContext the FacesContext for the current request
     * @throws NullPointerException if {@code facesContext} is {@code null}
     */
    public FullVisitContext(FacesContext facesContext) {
        this(facesContext, null);
    }

    /**
     * Creates a FullVisitorContext instance with the specified hints.
     *
     * @param facesContext the FacesContext for the current request
     * @param hints a the VisitHints for this visit
     * @throws NullPointerException if {@code facesContext} is {@code null}
     */
    public FullVisitContext(FacesContext facesContext, Set<VisitHint> hints) {

        if (facesContext == null) {
            throw new NullPointerException();
        }

        this.facesContext = facesContext;

        // Copy and store hints - ensure unmodifiable and non-empty
        EnumSet<VisitHint> hintsEnumSet = hints == null || hints.isEmpty() ? EnumSet.noneOf(VisitHint.class) : EnumSet.copyOf(hints);

        this.hints = Collections.unmodifiableSet(hintsEnumSet);
    }

    /**
     * @see VisitContext#getFacesContext VisitContext.getFacesContext()
     */
    @Override
    public FacesContext getFacesContext() {
        return facesContext;
    }

    /**
     * @see VisitContext#getIdsToVisit VisitContext.getIdsToVisit()
     */
    @Override
    public Collection<String> getIdsToVisit() {

        // We always visits all ids
        return ALL_IDS;
    }

    /**
     * @see VisitContext#getSubtreeIdsToVisit VisitContext.getSubtreeIdsToVisit()
     */
    @Override
    public Collection<String> getSubtreeIdsToVisit(UIComponent component) {

        // Make sure component is a NamingContainer
        if (!(component instanceof NamingContainer)) {
            throw new IllegalArgumentException("Component is not a NamingContainer: " + component);
        }

        // We always visits all ids
        return ALL_IDS;
    }

    /**
     * @see VisitContext#getHints VisitContext.getHints
     */
    @Override
    public Set<VisitHint> getHints() {
        return hints;
    }

    /**
     * @see VisitContext#invokeVisitCallback VisitContext.invokeVisitCallback()
     */
    @Override
    public VisitResult invokeVisitCallback(UIComponent component, VisitCallback callback) {

        // Nothing interesting here - just invoke the callback.
        // (PartialVisitContext.invokeVisitCallback() does all of the
        // interesting work.)
        return callback.visit(this, component);
    }

    // The FacesContext for this request
    private final FacesContext facesContext;

    // Our visit hints
    private final Set<VisitHint> hints;
}
