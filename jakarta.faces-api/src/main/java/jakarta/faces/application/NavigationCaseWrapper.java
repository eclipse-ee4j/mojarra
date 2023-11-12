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

package jakarta.faces.application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_2">
 * <span class="changed_modified_2_3">Provides</span> a simple implementation of {@link NavigationCase} that can be
 * subclassed by developers wishing to provide specialized behavior to an existing {@link NavigationCase} instance. The
 * default implementation of all methods is to call through to the wrapped {@link NavigationCase} instance.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.2
 */
public abstract class NavigationCaseWrapper extends NavigationCase implements FacesWrapper<NavigationCase> {

    private final NavigationCase wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public NavigationCaseWrapper() {
        this(null);
    }

    /**
     * <p class="changed_added_2_3">
     * If this navigation case has been decorated, the implementation doing the decorating should push the implementation
     * being wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public NavigationCaseWrapper(NavigationCase wrapped) {
        super((String) null, (String) null, (String) null, (String) null, (String) null, (Map<String, List<String>>) null, false, false);
        this.wrapped = wrapped;
    }

    @Override
    public NavigationCase getWrapped() {
        return wrapped;
    }

    @Override
    public boolean equals(Object o) {
        return getWrapped().equals(o);
    }

    @Override
    public int hashCode() {
        return getWrapped().hashCode();
    }

    @Override
    public String toString() {
        return getWrapped().toString();
    }

    @Override
    public URL getActionURL(FacesContext context) throws MalformedURLException {
        return getWrapped().getActionURL(context);
    }

    @Override
    public URL getBookmarkableURL(FacesContext context) throws MalformedURLException {
        return getWrapped().getBookmarkableURL(context);
    }

    @Override
    public Boolean getCondition(FacesContext context) {
        return getWrapped().getCondition(context);
    }

    @Override
    public String getFromAction() {
        return getWrapped().getFromAction();
    }

    @Override
    public String getFromOutcome() {
        return getWrapped().getFromOutcome();
    }

    @Override
    public String getFromViewId() {
        return getWrapped().getFromViewId();
    }

    @Override
    public Map<String, List<String>> getParameters() {
        return getWrapped().getParameters();
    }

    @Override
    public URL getRedirectURL(FacesContext context) throws MalformedURLException {
        return getWrapped().getRedirectURL(context);
    }

    @Override
    public URL getResourceURL(FacesContext context) throws MalformedURLException {
        return getWrapped().getResourceURL(context);
    }

    @Override
    public String getToViewId(FacesContext context) {
        return getWrapped().getToViewId(context);
    }

    @Override
    public String getToFlowDocumentId() {
        return getWrapped().getToFlowDocumentId();
    }

    @Override
    public boolean hasCondition() {
        return getWrapped().hasCondition();
    }

    @Override
    public boolean isIncludeViewParams() {
        return getWrapped().isIncludeViewParams();
    }

    @Override
    public boolean isRedirect() {
        return getWrapped().isRedirect();
    }

}
