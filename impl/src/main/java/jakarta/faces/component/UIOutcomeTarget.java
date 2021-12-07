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

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2">This</span> component is paired with the <code>jakarta.faces.Button</code> or
 * <code>jakarta.faces.Link</code> renderers and encapsulates properties relating to the rendering of outcomes directly
 * to the response. This enables bookmarkability in Jakarta Faces applications.
 * </p>
 *
 * @since 2.0
 */
public class UIOutcomeTarget extends UIOutput {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.OutcomeTarget";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.OutcomeTarget";

    enum PropertyKeys {
        includeViewParams, outcome, disableClientWindow
    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIOutcomeTarget} instance with default property values.
     * </p>
     */
    public UIOutcomeTarget() {
        super();
        setRendererType("jakarta.faces.Link");
    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /**
     * <p class="changed_added_2_0">
     * Return whether or not the view parameters should be encoded into the target url.
     * </p>
     *
     * @return <code>true</code> if the view parameters should be encoded in the url, <code>false</code> otherwise.
     * @since 2.0
     */
    public boolean isIncludeViewParams() {
        return (Boolean) getStateHelper().eval(PropertyKeys.includeViewParams, false);
    }

    /**
     * <p class="changed_added_2_0">
     * Set whether or not the page parameters should be encoded into the target url.
     * </p>
     *
     * @param includeViewParams The state of the switch for encoding page parameters
     *
     * @since 2.0
     */
    public void setIncludeViewParams(boolean includeViewParams) {
        getStateHelper().put(PropertyKeys.includeViewParams, includeViewParams);
    }

    /**
     * <p class="changed_added_2_2">
     * Return whether or not the client window should be encoded into the target url.
     * </p>
     *
     * @return <code>true</code> if the client window should NOT be encoded in the url, <code>false</code> otherwise.
     * @since 2.0
     */
    public boolean isDisableClientWindow() {
        return (Boolean) getStateHelper().eval(PropertyKeys.disableClientWindow, false);
    }

    /**
     * <p class="changed_added_2_2">
     * Set whether or not the client window should be encoded into the target url.
     * </p>
     *
     * @param disableClientWindow if @{code true}, the client window will not be included in this outcome target.
     *
     * @since 2.2
     */
    public void setDisableClientWindow(boolean disableClientWindow) {
        getStateHelper().put(PropertyKeys.disableClientWindow, disableClientWindow);
    }

    /**
     * <p class="changed_added_2_0">
     * Returns the <code>outcome</code> property of the <code>UIOutcomeTarget</code>. This value is passed to the
     * {@link jakarta.faces.application.NavigationHandler} when resolving the target url of this component.
     * </p>
     *
     * @return the outcome.
     * @since 2.0
     */
    public String getOutcome() {
        return (String) getStateHelper().eval(PropertyKeys.outcome);
    }

    /**
     * <p class="changed_added_2_0">
     * Sets the <code>outcome</code> property of the <code>UIOutcomeTarget</code>. This value is passed to the
     * NavigationHandler when resolving the target url of this component.
     * </p>
     *
     * @since 2.0
     *
     * @param outcome the navigation outcome
     */
    public void setOutcome(String outcome) {
        getStateHelper().put(PropertyKeys.outcome, outcome);
    }

}
