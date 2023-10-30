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

package jakarta.faces.component.behavior;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <strong class="changed_modified_2_3">ClientBehaviorContext</strong> provides context information that may be useful
 * to {@link jakarta.faces.component.behavior.ClientBehavior#getScript} implementations.
 * </p>
 *
 * @since 2.0
 */
public abstract class ClientBehaviorContext {

    /**
     * <p class="changed_added_2_3">
     * The request parameter name whose request parameter value identifies the source component of behavior event.
     * </p>
     *
     * @since 2.3
     */
    public static final String BEHAVIOR_SOURCE_PARAM_NAME = "jakarta.faces.source";

    /**
     * <p class="changed_added_2_3">
     * The request parameter name whose request parameter value identifies the type of behavior event.
     * </p>
     *
     * @since 2.3
     */
    public static final String BEHAVIOR_EVENT_PARAM_NAME = "jakarta.faces.behavior.event";

    /**
     * <p class="changed_added_2_0">
     * Creates a ClientBehaviorContext instance.
     * </p>
     *
     * @param context the <code>FacesContext</code> for the current request.
     * @param component the component instance to which the <code>ClientBehavior</code> is attached.
     * @param eventName the name of the behavior event to which the <code>ClientBehavior</code> is attached.
     * @param sourceId the id to use as the ClientBehavior's "source".
     * @param parameters the collection of parameters for submitting ClientBehaviors to include in the request.
     * @return a <code>ClientBehaviorContext</code> instance configured with the provided values.
     * @throws NullPointerException if <code>context</code>, <code>component</code> or <code>eventName</code> is
     * <code>null</code>
     *
     * @since 2.0
     */
    public static ClientBehaviorContext createClientBehaviorContext(FacesContext context, UIComponent component, String eventName, String sourceId,
            Collection<ClientBehaviorContext.Parameter> parameters) {

        return new ClientBehaviorContextImpl(context, component, eventName, sourceId, parameters);
    }

    /**
     * <p class="changed_added_2_0">
     * Returns the {@link FacesContext} for the current request.
     * </p>
     *
     * @return the {@link FacesContext}.
     *
     * @since 2.0
     */
    abstract public FacesContext getFacesContext();

    /**
     * <p class="changed_added_2_0">
     * Returns the {@link UIComponent} that is requesting the {@link ClientBehavior} script.
     * </p>
     *
     * @return the component.
     *
     * @since 2.0
     */
    abstract public UIComponent getComponent();

    /**
     * <p class="changed_added_2_0">
     * Returns the name of the behavior event for which the ClientBehavior script is being requested.
     * </p>
     *
     * @return the event name.
     *
     * @since 2.0
     */
    abstract public String getEventName();

    /**
     * <p class="changed_added_2_0">
     * Returns an id for use as the {@link ClientBehavior} source. ClientBehavior implementations that submit back to the
     * Faces lifecycle are required to identify which component triggered the ClientBehavior-initiated request via the
     * <code>jakarta.faces.source</code> request parameter. In most cases, th source id can be trivially derived from the
     * element to which the behavior's client-side script is attached - ie. the source id is typically the id of this
     * element. However, in components which produce more complex content, the behavior script may not be able to determine
     * the correct id to use for the jakarta.faces.source value. The {@link ClientBehaviorContext#getSourceId} method allows
     * the component to pass this information into the {@link ClientBehavior#getScript} implementation.
     * </p>
     *
     * @return the id for the behavior's script to use as the "source", or null if the Behavior's script can identify the
     * source from the DOM.
     *
     * @since 2.0
     */
    abstract public String getSourceId();

    /**
     * <p class="changed_added_2_0">
     * Returns parameters that "submitting" {@link ClientBehavior} implementations should include when posting back data
     * into the Faces lifecycle. If no parameters are specified, this method returns an empty (non-null) collection.
     * </p>
     *
     * @return the parameters.
     *
     * @since 2.0
     */
    abstract public Collection<ClientBehaviorContext.Parameter> getParameters();

    // Little static member class that provides a default implementation
    private static final class ClientBehaviorContextImpl extends ClientBehaviorContext {
        private final FacesContext context;
        private final UIComponent component;
        private final String eventName;
        private final String sourceId;
        private final Collection<ClientBehaviorContext.Parameter> parameters;

        private ClientBehaviorContextImpl(FacesContext context, UIComponent component, String eventName, String sourceId,
                Collection<ClientBehaviorContext.Parameter> parameters) {

            Objects.requireNonNull(context);
            Objects.requireNonNull(component);
            Objects.requireNonNull(eventName);

            this.context = context;
            this.component = component;
            this.eventName = eventName;
            this.sourceId = sourceId;
            this.parameters = parameters == null ? Collections.emptyList() : parameters;
        }

        @Override
        public FacesContext getFacesContext() {
            return context;
        }

        @Override
        public UIComponent getComponent() {
            return component;
        }

        @Override
        public String getEventName() {
            return eventName;
        }

        @Override
        public String getSourceId() {
            return sourceId;
        }

        @Override
        public Collection<ClientBehaviorContext.Parameter> getParameters() {
            return parameters;
        }
    }

    /**
     * <p class="changed_added_2_0">
     * <strong>Parameter</strong> instances represent name/value pairs that "submitting" ClientBehavior implementations
     * should include when posting back into the Faces lifecycle. ClientBehavior implementations can determine which
     * Parameters to include by calling ClientBehaviorContext.getParameters().
     * </p>
     *
     * @since 2.0
     */
    public static class Parameter {

        private final String name;
        private final Object value;

        /**
         * <p class="changed_added_2_0">
         * Creates a Parameter instance.
         * </p>
         *
         * @param name the name of the parameter
         * @param value the value of the parameter
         * @throws NullPointerException if <code>name</code> is null.
         *
         * @since 2.0
         */
        public Parameter(String name, Object value) {

            if (null == name) {
                throw new NullPointerException();
            }

            this.name = name;
            this.value = value;
        }

        /**
         * <p class="changed_added_2_0">
         * Returns the Parameter's name.
         * </p>
         *
         * @return the parameter's name.
         *
         * @since 2.0
         */
        public String getName() {
            return name;
        }

        /**
         * <p class="changed_added_2_0">
         * Returns the Parameter's value.
         * </p>
         *
         * @return the parameter's value.
         *
         * @since 2.0
         */
        public Object getValue() {
            return value;
        }
    }
}
