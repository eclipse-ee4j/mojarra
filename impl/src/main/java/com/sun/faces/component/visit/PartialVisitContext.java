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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;

/**
 *
 * <p class="changed_added_2_0">
 * A VisitContext implementation that is used when performing a partial component tree visit.
 * </p>
 *
 * RELEASE_PENDING
 *
 * @since 2.0
 */
public class PartialVisitContext extends VisitContext {

    /**
     * Creates a PartialVisitorContext instance.
     *
     * @param facesContext the FacesContext for the current request
     * @param clientIds the client ids of the components to visit
     * @throws NullPointerException if {@code facesContext} is {@code null}
     */
    public PartialVisitContext(FacesContext facesContext, Collection<String> clientIds) {
        this(facesContext, clientIds, null);
    }

    /**
     * Creates a PartialVisitorContext instance with the specified hints.
     *
     * @param facesContext the FacesContext for the current request
     * @param clientIds the client ids of the components to visit
     * @param hints a the VisitHints for this visit
     * @throws NullPointerException if {@code facesContext} is {@code null}
     */
    public PartialVisitContext(FacesContext facesContext, Collection<String> clientIds, Set<VisitHint> hints) {
        if (facesContext == null) {
            throw new NullPointerException();
        }

        this.facesContext = facesContext;

        // Initialize our various collections
        initializeCollections(clientIds);

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
     * @see VisitContext#getHints VisitContext.getHints
     */
    @Override
    public Set<VisitHint> getHints() {
        return hints;
    }

    /**
     * @see VisitContext#getIdsToVisit VisitContext.getIdsToVisit()
     */
    @Override
    public Collection<String> getIdsToVisit() {

        // We just return our clientIds collection. This is
        // the modifiable (but proxied) collection of all of
        // the client ids to visit.
        return clientIds;
    }

    public Collection<String> getUnvisitedClientIds() {
        return unvisitedClientIds;
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

        String clientId = component.getClientId();
        Collection<String> ids = subtreeClientIds.get(clientId);

        if (ids == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(ids);
    }

    /**
     * @see VisitContext#invokeVisitCallback VisitContext.invokeVisitCallback()
     */
    @Override
    public VisitResult invokeVisitCallback(UIComponent component, VisitCallback callback) {

        // First sure that we should visit this component - ie.
        // that this component is represented in our id set.
        String clientId = getVisitId(component);

        if (clientId == null) {
            // Not visiting this component, but allow visit to
            // continue into this subtree in case we've got
            // visit targets there.
            return VisitResult.ACCEPT;
        }

        // If we made it this far, the component matches one of
        // client ids, so perform the visit.
        VisitResult result = callback.visit(this, component);

        // Remove the component from our "unvisited" collection
        unvisitedClientIds.remove(clientId);

        // If the unvisited collection is now empty, we are done.
        // Return VisitResult.COMPLETE to terminate the visit.
        if (unvisitedClientIds.isEmpty()) {
            return VisitResult.COMPLETE;
        }

        // Otherwise, just return the callback's result
        return result;
    }

    // Called by CollectionProxy to notify PartialVisitContext that
    // an new id has been added.
    private void idAdded(String clientId) {

        // An id to visit has been added, update our other
        // collections to reflect this.

        // Update the ids collection
        ids.add(getIdFromClientId(clientId));

        // Update the unvisited ids collection
        unvisitedClientIds.add(clientId);

        // Update the subtree ids collection
        addSubtreeClientId(clientId);
    }

    // Called by CollectionProxy to notify PartialVisitContext that
    // an id has been removed
    private void idRemoved(String clientId) {

        // An id to visit has been removed, update our other
        // collections to reflect this. Note that we don't
        // update the ids collection, since we ids (non-client ids)
        // may not be unique.

        // Update the unvisited ids collection
        unvisitedClientIds.remove(clientId);

        // Update the subtree ids collection
        removeSubtreeClientId(clientId);
    }

    // Called to initialize our various collections.
    private void initializeCollections(Collection<String> clientIds) {

        // We maintain 4 collections:
        //
        // 1. clientIds: contains all of the client ids to visit
        // 2. ids: contains just ids (not client ids) to visit.
        // We use this to optimize our check to see whether a
        // particular component is in the visit set (ie. to
        // avoid having to compute the client id).
        // 3. subtreeClientIds: contains client ids to visit broken
        // out by naming container subtree. (Needed by
        // getSubtreeIdsToVisit()).
        // 4. unvisitedClientIds: contains the client ids to visit that
        // have not yet been visited.
        //
        // We populate these now.
        //
        // Note that we use default HashSet/Map initial capacities, though
        // perhaps we could pick more intelligent defaults.

        // Initialize unvisitedClientIds collection
        unvisitedClientIds = new HashSet<>();

        // Initialize ids collection
        ids = new HashSet<>();

        // Initialize subtreeClientIds collection
        subtreeClientIds = new HashMap<>();

        // Initialize the clientIds collection. Note that we proxy
        // this collection so that we can trap adds/removes and sync
        // up all of the other collections.
        this.clientIds = new CollectionProxy<>(new HashSet<>());

        // Finally, populate the clientIds collection. This has the
        // side effect of populating all of the other collections.
        this.clientIds.addAll(clientIds);
    }

    // Tests whether the specified component should be visited.
    // If so, returns its client id. If not, returns null.
    private String getVisitId(UIComponent component) {

        // We first check to see whether the component's id
        // is in our id collection. We do this before checking
        // for the full client id because getting the full client id
        // is more expensive than just getting the local id.
        String id = component.getId();
        if (id != null && !ids.contains(id)) {
            return null;
        }

        // The id was a match - now check the client id.
        // note that client id should never be null (should be
        // generated even if id is null, so asserting this.)
        String clientId = component.getClientId();
        assert clientId != null;

        return clientIds.contains(clientId) ? clientId : null;
    }

    // Converts a client id into a plain old id by ripping
    // out the trailing id segment.
    private String getIdFromClientId(String clientId) {
        FacesContext facesContext = getFacesContext();
        char separator = UINamingContainer.getSeparatorChar(facesContext);
        int lastIndex = clientId.lastIndexOf(separator);

        String id = null;

        if (lastIndex < 0) {
            id = clientId;
        } else if (lastIndex < clientId.length() - 1) {
            id = clientId.substring(lastIndex + 1);
        }

        return id;
    }

    // Given a single client id, populate the subtree map with all possible
    // subtree client ids
    private void addSubtreeClientId(String clientId) {

        FacesContext facesContext = getFacesContext();
        char separator = UINamingContainer.getSeparatorChar(facesContext);

        // Loop over the client id and find the substring corresponding to
        // each ancestor NamingContainer client id. For each ancestor
        // NamingContainer, add an entry into the map for the full client
        // id.

        int length = clientId.length();

        for (int i = 0; i < length; i++) {

            if (clientId.charAt(i) == separator) {

                // We found an ancestor NamingContainer client id - add
                // an entry to the map.
                String namingContainerClientId = clientId.substring(0, i);

                // Check to see whether we've already ids under this
                // NamingContainer client id. If not, create the
                // Collection for this NamingContainer client id and
                // stash it away in our map
                Collection<String> c = subtreeClientIds.computeIfAbsent(namingContainerClientId, k -> new ArrayList<>());

                // Stash away the client id
                c.add(clientId);
            }
        }
    }

    // Given a single client id, remove any entries corresponding
    // entries from our subtree collections
    private void removeSubtreeClientId(String clientId) {

        // Loop through each entry in the map and check to see whether
        // the client id to remove should be contained in the corresponding
        // collection - ie. whether the key (the NamingContainer client id)
        // is present at the start of the client id to remove.
        for (String key : subtreeClientIds.keySet()) {

            if (clientId.startsWith(key)) {

                // If the clientId starts with the key, we should
                // have an entry for this clientId in the corresponding
                // collection. Remove it.
                Collection<String> ids = subtreeClientIds.get(key);
                ids.remove(clientId);
            }
        }
    }

    // Little proxy collection implementation. We proxy the id
    // collection so that we can detect modifications and update
    // our internal state when ids to visit are added or removed.
    private class CollectionProxy<E extends String> extends AbstractCollection<E> {

        private CollectionProxy(Collection<E> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int size() {
            return wrapped.size();
        }

        @Override
        public Iterator<E> iterator() {
            return new IteratorProxy<>(wrapped.iterator());
        }

        @Override
        public boolean add(E o) {
            boolean added = wrapped.add(o);

            if (added) {
                idAdded(o);
            }

            return added;
        }

        private final Collection<E> wrapped;
    }

    // Little proxy iterator implementation used by CollectionProxy
    // so that we can catch removes.
    private class IteratorProxy<E extends String> implements Iterator<E> {
        private IteratorProxy(Iterator<E> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean hasNext() {
            return wrapped.hasNext();
        }

        @Override
        public E next() {
            current = wrapped.next();

            return current;
        }

        @Override
        public void remove() {

            if (current != null) {
                idRemoved(current);
            }

            wrapped.remove();
        }

        private final Iterator<E> wrapped;

        private E current = null;
    }

    // The client ids to visit
    private Collection<String> clientIds;

    // The ids to visit
    private Collection<String> ids;

    // The client ids that have yet to be visited
    private Collection<String> unvisitedClientIds;

    // This map contains the information needed by getSubtreeIdsToVisit().
    // The keys in this map are NamingContainer client ids. The values
    // are collections containing all of the client ids to visit within
    // corresponding naming container.
    private Map<String, Collection<String>> subtreeClientIds;

    // The FacesContext for this request
    private final FacesContext facesContext;

    // Our visit hints
    private final Set<VisitHint> hints;
}
