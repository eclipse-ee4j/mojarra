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

package jakarta.faces.component.visit;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 *
 * <p class="changed_added_2_0">
 * A context object that is used to hold state relating to performing a component tree visit.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * Component tree visits are initiated by calling {@link UIComponent#visitTree}, at which point both a
 * {@link VisitContext} and a {@link VisitCallback} must be provided.
 * </p>
 * </div>
 *
 * @see UIComponent#visitTree UIComponent.visitTree()
 * @see VisitCallback
 *
 * @since 2.0
 */
abstract public class VisitContext {

    // Design notes: The VisitContext contract could be defined
    // as an interface. However, there is the potential that we
    // may need to add new methods in the future, so leaving as
    // an abstract class in order to have room to grow.
    //
    // Since we are an abstract class rather than an interface,
    // we could provide implementations of of some of the simpler
    // methods (eg. getFacesContext() and getHints()) to avoid
    // duplicating this code in VisitContext implementations.
    // However, doing so would mean that "wrapping" VisitContext
    // implementations would be forced to pick up such implementations,
    // so going with a pure contract (no implementation).

    /**
     * <p class="changed_added_2_0">
     * This unmodifiable Collection is returned by <code>getIdsToVisit()</code> and <code>getSubtreeIdsToVisit()</code> in
     * cases where all ids should be visited.
     * </p>
     *
     * <p class="changed_added_2_0">
     * To simplify logic for <code>visitTree()</code> implementations, this Collection always returns <code>false</code> for
     * <code>isEmpty</code>. All other methods throw <code>UnsupportedOperationException</code>.
     * </p>
     *
     * @since 2.0
     */
    // Note: We cannot use Collections.emptyList() as that returns
    // a shared instance - we want to unique instance to allow for
    // identity tests.
    static public final Collection<String> ALL_IDS = new AbstractCollection<>() {

        @Override
        public Iterator<String> iterator() {
            throw new UnsupportedOperationException("VisitContext.ALL_IDS does not support this operation");
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("VisitContext.ALL_IDS does not support this operation");
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    };

    /**
     * <p class="changed_added_2_0">
     * Returns the FacesContext for the current request.
     * </p>
     *
     * @return the FacesContext.
     *
     * @since 2.0
     */
    abstract public FacesContext getFacesContext();

    /**
     * <p class="changed_added_2_0">
     * Returns the ids of the components to visit.
     * </p>
     *
     * <p class="changed_added_2_0">
     * In the case of a full tree visit, this method returns the ALL_IDS collection. Otherwise, if a partial visit is beign
     * performed, returns a modifiable collection containing the client ids of the components that should be visited.
     * </p>
     *
     * @return the ids of the components to visit
     *
     */
    abstract public Collection<String> getIdsToVisit();

    /**
     * <p class="changed_added_2_0">
     * Given a {@link jakarta.faces.component.NamingContainer} component, returns the client ids of any components
     * underneath the NamingContainer that should be visited.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * This method is called by NamingContainer visitTree() implementations to determine whether the NamingContainer
     * contains components to be visited. In the case where no such components exist, the NamingContainer can short-circuit
     * the tree visit and avoid descending into child subtrees.
     * </p>
     *
     * <p>
     * In addition, iterating components such as UIData may be able to use the returned ids to determine which iterated
     * states (ie. rows) need to be visited. This allows the visit traversal to be contstrained such only those rows that
     * contain visit targets need to be traversed.
     * </p>
     *
     * </div>
     *
     * @param component a NamingContainer component
     * @return an unmodifiable Collection containing the client ids of any components underneath the NamingContainer that
     * are known to be targets of the tree visit. If no such components exist, returns an empty Collection. If all
     * components underneath the NamingContainer should be visited, returns the {@code VisitContext.ALL_IDS} collection.
     * @throws IllegalArgumentException if {@code component} is not an instance of NamingContainer
     */
    abstract public Collection<String> getSubtreeIdsToVisit(UIComponent component);

    /**
     * <p>
     * Called by {@link UIComponent#visitTree UIComponent.visitTree()} to visit a single component.
     * </p>
     *
     * @param component the component to visit
     * @param callback the VisitCallback to call
     * @return a VisitResult value that indicates whether to continue visiting the component's subtree, skip visiting the
     * component's subtree or abort the visit altogether.
     */
    abstract public VisitResult invokeVisitCallback(UIComponent component, VisitCallback callback);

    /**
     * <p>
     * Returns hints that influence the behavior of the tree visit.
     * </p>
     *
     * <p>
     * Interested parties, such as {@link UIComponent#visitTree UIComponent.visitTree()} implementations, may check to see
     * whether a particular hint is present by calling {@code VisitContext.getHints().contains()}, passing in one of the
     * hints defined by {@link VisitHint}.
     *
     * @return a non-empty, unmodifiable collection of VisitHints
     */
    abstract public Set<VisitHint> getHints();

    /**
     * <p>
     * Returns a VisitContext instance that is initialized with the specified ids and hintsfor use with
     * {@link UIComponent#visitTree}.
     * </p>
     *
     * @param context the FacesContext for the current request
     * @param ids the client ids of the components to visit. If null, all components will be visited.
     * @param hints the VisitHints to apply to the visit. If <code>null</code>, no hints are applied.
     *
     * @return a VisitContext instance that is initialized with the specified ids and hints.
     */
    public static VisitContext createVisitContext(FacesContext context, Collection<String> ids, Set<VisitHint> hints) {

        VisitContextFactory factory = (VisitContextFactory) FactoryFinder.getFactory(FactoryFinder.VISIT_CONTEXT_FACTORY);
        return factory.getVisitContext(context, ids, hints);

    }

    /**
     * <p>
     * Creates a VisitContext instance for use with {@link UIComponent#visitTree UIComponent.visitTree()}. This method can
     * be used to obtain a VisitContext instance when all components should be visited with the default visit hints.
     * </p>
     *
     * @param context the FacesContext for the current request
     * @return a VisitContext instance
     */
    public static VisitContext createVisitContext(FacesContext context) {

        return createVisitContext(context, null, null);

    }

}
