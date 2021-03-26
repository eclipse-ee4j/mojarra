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

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <span class="changed_modified_2_0_rev_a">This</span> component is responsible for displaying messages for a specific
 * {@link UIComponent}, identified by a <code>clientId</code> <span class="changed_modified_2_0_rev_a"> or component id
 * relative to the closest ancestor <code>NamingContainer</code></span>. The component obtains the messages from the
 * {@link FacesContext}.
 * </p>
 *
 * <p>
 * By default, the <code>rendererType</code> property must be set to "<code>jakarta.faces.Message</code>". This value
 * can be changed by calling the <code>setRendererType()</code> method.
 * </p>
 *
 *
 */

public class UIMessage extends UIComponentBase {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.Message";

    /**
     * <p>
     * The standard component family for this component.
     * </p>
     */
    public static final String COMPONENT_FAMILY = "jakarta.faces.Message";

    enum PropertyKeys {

        forValue("for"), showDetail, showSummary, redisplay;

        String toString;

        PropertyKeys(String toString) {
            this.toString = toString;
        }

        PropertyKeys() {
        }

        @Override
        public String toString() {
            return toString != null ? toString : super.toString();
        }

    }

    // ------------------------------------------------------------ Constructors

    /**
     * <p>
     * Create a new {@link UIMessage} instance with default property values.
     * </p>
     */
    public UIMessage() {

        super();
        setRendererType("jakarta.faces.Message");

    }

    // -------------------------------------------------------------- Properties

    @Override
    public String getFamily() {

        return COMPONENT_FAMILY;

    }

    /**
     * <p>
     * <span class="changed_modified_2_0_rev_a">Return the Identifier of the component for which to render error messages.
     * If this component is within the same NamingContainer as the target component, this must be the component identifier.
     * Otherwise, it must be an absolute component identifier (starting with ":"). See the {@link UIComponent#findComponent}
     * for more information.</span>
     * </p>
     *
     * @return the for client identifier.
     */
    public String getFor() {

        return (String) getStateHelper().eval(PropertyKeys.forValue);

    }

    /**
     * <p>
     * Set <span class="changed_modified_2_0_rev_a">the identifier</span> of the component for which this component
     * represents associated message(s) (if any). This property must be set before the message is displayed.
     * </p>
     *
     * @param newFor The new client id
     */
    public void setFor(String newFor) {

        getStateHelper().put(PropertyKeys.forValue, newFor);

    }

    /**
     * <p>
     * Return the flag indicating whether the <code>detail</code> property of the associated message(s) should be displayed.
     * Defaults to <code>true</code>.
     * </p>
     *
     * @return <code>true</code> if detail is to be shown, <code>false</code> otherwise.
     */
    public boolean isShowDetail() {

        return (Boolean) getStateHelper().eval(PropertyKeys.showDetail, true);

    }

    /**
     * <p>
     * Set the flag indicating whether the <code>detail</code> property of the associated message(s) should be displayed.
     * </p>
     *
     * @param showDetail The new flag
     */
    public void setShowDetail(boolean showDetail) {

        getStateHelper().put(PropertyKeys.showDetail, showDetail);

    }

    /**
     * <p>
     * Return the flag indicating whether the <code>summary</code> property of the associated message(s) should be
     * displayed. Defaults to <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if the summary is to be shown, <code>false</code> otherwise.
     */
    public boolean isShowSummary() {

        return (Boolean) getStateHelper().eval(PropertyKeys.showSummary, false);

    }

    /**
     * <p>
     * Set the flag indicating whether the <code>summary</code> property of the associated message(s) should be displayed.
     * </p>
     *
     * @param showSummary The new flag value
     */
    public void setShowSummary(boolean showSummary) {

        getStateHelper().put(PropertyKeys.showSummary, showSummary);

    }

    /**
     * @return <code>true</code> if this <code>UIMessage</code> instance should redisplay
     * {@link jakarta.faces.application.FacesMessage}s that have already been handled, otherwise returns <code>false</code>.
     * By default this method will always return <code>true</code> if {@link #setRedisplay(boolean)} has not been called.
     *
     * @since 2.0
     */
    public boolean isRedisplay() {

        return (Boolean) getStateHelper().eval(PropertyKeys.redisplay, true);

    }

    /**
     * <p>
     * Set the flag indicating whether the <code>detail</code> property of the associated message(s) should be displayed.
     * </p>
     *
     * @param redisplay flag indicating whether previously handled messages are redisplayed or not
     *
     * @since 2.0
     */
    public void setRedisplay(boolean redisplay) {

        getStateHelper().put(PropertyKeys.redisplay, redisplay);

    }

}
