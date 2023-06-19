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

package jakarta.faces.component;

import static com.sun.faces.util.Util.coalesce;
import static jakarta.faces.component.UIViewRoot.UNIQUE_ID_PREFIX;
import static jakarta.faces.component.visit.VisitResult.COMPLETE;
import static java.util.logging.Level.SEVERE;

import java.util.Collection;
import java.util.logging.Logger;

import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <strong class="changed_modified_2_0">UINamingContainer</strong> is a convenience base class for components that wish
 * to implement {@link NamingContainer} functionality.
 * </p>
 */

public class UINamingContainer extends UIComponentBase implements NamingContainer, UniqueIdVendor, StateHolder {

    // ------------------------------------------------------ Manifest Constants

    private static final Logger LOGGER = Logger.getLogger("jakarta.faces.component", "jakarta.faces.LogStrings");

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.NamingContainer";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.NamingContainer";

    /**
     * <p class="changed_added_2_0">
     * The context-param that allows the separator char for clientId strings to be set on a per-web application basis.
     * </p>
     *
     * @since 2.0
     */
    public static final String SEPARATOR_CHAR_PARAM_NAME = "jakarta.faces.SEPARATOR_CHAR";

    enum PropertyKeys {
        lastId
    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UINamingContainer} instance with default property values.
     * </p>
     */
    public UINamingContainer() {
        super();
        setRendererType(null);
    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * <p class="changed_added_2_0">
     * Return the character used to separate segments of a clientId. The implementation must determine if there is a
     * &lt;<code>context-param</code>&gt; with the value given by the value of the symbolic constant
     * {@link #SEPARATOR_CHAR_PARAM_NAME}. If there is a value for this param, the first character of the value must be
     * returned from this method. Otherwise, the value of the symbolic constant {@link NamingContainer#SEPARATOR_CHAR} must
     * be returned.
     * </p>
     *
     * @param context the {@link FacesContext} for the current request
     * @return the separator char.
     * @since 2.0
     */
    public static char getSeparatorChar(FacesContext context) {

        if (context == null) {
            if (LOGGER.isLoggable(SEVERE)) {
                LOGGER.log(SEVERE, "UINamingContainer.getSeparatorChar() called with null FacesContext. This indicates a SEVERE error. Returning {0}",
                        SEPARATOR_CHAR);
            }

            return SEPARATOR_CHAR;
        }

        Character separatorChar = (Character) context.getAttributes().get(SEPARATOR_CHAR_PARAM_NAME);
        if (separatorChar == null) {
            String initParam = context.getExternalContext().getInitParameter(SEPARATOR_CHAR_PARAM_NAME);
            separatorChar = SEPARATOR_CHAR;
            if (initParam != null) {
                initParam = initParam.trim();
                if (initParam.length() != 0) {
                    separatorChar = initParam.charAt(0);
                }
            }

            context.getAttributes().put(SEPARATOR_CHAR_PARAM_NAME, separatorChar);
        }

        return separatorChar;
    }

    /**
     * @return <code>true</code> if tree should be visited, <code>false</code> otherwise.
     * @see UIComponent#visitTree
     */
    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {

        // NamingContainers can optimize partial tree visits by taking advantage
        // of the fact that it is possible to detect whether any ids to visit
        // exist underneath the NamingContainer. If no such ids exist, there
        // is no need to visit the subtree under the NamingContainer.
        Collection<String> idsToVisit = context.getSubtreeIdsToVisit(this);

        // If we have ids to visit, let the superclass implementation
        // handle the visit
        if (!idsToVisit.isEmpty()) {
            return super.visitTree(context, callback);
        }

        // If we have no child ids to visit, just visit ourselves, if
        // we are visitable.
        if (isVisitable(context)) {
            FacesContext facesContext = context.getFacesContext();
            pushComponentToEL(facesContext, null);

            try {
                return context.invokeVisitCallback(this, callback) == COMPLETE;
            } finally {
                popComponentFromEL(facesContext);
            }
        }

        // Done visiting this subtree. Return false to allow
        // visit to continue.
        return false;
    }

    @Override
    public String createUniqueId(FacesContext context, String seed) {
        int lastId = coalesce(getLastId(), 0);
        setLastId(++lastId);

        return UNIQUE_ID_PREFIX + coalesce(seed, lastId);
    }

    // ----------------------------------------------------- Private Methods

    private Integer getLastId() {
        return (Integer) getStateHelper().get(PropertyKeys.lastId);
    }

    private void setLastId(Integer lastId) {
        getStateHelper().put(PropertyKeys.lastId, lastId);
    }

}
