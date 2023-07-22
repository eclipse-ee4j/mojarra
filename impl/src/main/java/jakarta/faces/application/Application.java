/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.el.ELContextListener;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.component.search.SearchKeywordResolver;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.validator.Validator;
import jakarta.faces.view.ViewDeclarationLanguage;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_0_rev_a changed_modified_2_2
 * changed_modified_2_3">Application</strong> represents a per-web-application singleton object where applications based
 * on Jakarta Faces (or implementations wishing to provide extended functionality) can register application-wide
 * singletons that provide functionality required by Jakarta Faces. Default implementations of each object are
 * provided for cases where the application does not choose to customize the behavior.
 * </p>
 *
 * <p>
 * The instance of {@link Application} is created by calling the <code>getApplication()</code> method of
 * {@link ApplicationFactory}. Because this instance is shared, it must be implemented in a thread-safe manner.
 * </p>
 *
 * <p>
 * The application also acts as a factory for several types of Objects specified in the Faces Configuration file. Please
 * see {@link Application#createComponent}, {@link Application#createConverter}, and
 * {@link Application#createValidator}.
 * </p>
 */
public abstract class Application {

    private Application defaultApplication;

    // ------------------------------------------------------------- Properties

    /**
     * <p>
     * <span class="changed_modified_2_2">Return</span> the default {@link ActionListener} to be registered for all
     * {@link jakarta.faces.component.ActionSource} components in this application. If not explicitly set, a default
     * implementation must be provided that performs the <span class="changed_modified_2_2">functions as specified in the
     * section 7.1.1 "ActionListener Property" in the chapter 7 "Application Integration" of the Jakarta Faces Specification Document.</span>
     * </p>
     *
     * <p>
     * Note that the specification for the default <code>ActionListener</code> contiues to call for the use of a
     * <strong>deprecated</strong> property (<code>action</code>) and class (<code>MethodBinding</code>). Unfortunately,
     * this is necessary because the default <code>ActionListener</code> must continue to work with components that do not
     * implement {@link jakarta.faces.component.ActionSource2}, and only implement
     * {@link jakarta.faces.component.ActionSource}.
     * </p>
     *
     * @return the action listener.
     */
    public abstract ActionListener getActionListener();

    /**
     * <p>
     * Set the default {@link ActionListener} to be registered for all {@link jakarta.faces.component.ActionSource}
     * components.
     * </p>
     *
     * @param listener The new default {@link ActionListener}
     *
     * @throws NullPointerException if <code>listener</code> is <code>null</code>
     */
    public abstract void setActionListener(ActionListener listener);

    /**
     * <p>
     * Return the default <code>Locale</code> for this application. If not explicitly set, <code>null</code> is returned.
     * </p>
     *
     * @return the default Locale, or <code>null</code>.
     */
    public abstract Locale getDefaultLocale();

    /**
     * <p>
     * Set the default <code>Locale</code> for this application.
     * </p>
     *
     * @param locale The new default <code>Locale</code>
     *
     * @throws NullPointerException if <code>locale</code> is <code>null</code>
     */
    public abstract void setDefaultLocale(Locale locale);

    /**
     * <p>
     * Return the <code>renderKitId</code> to be used for rendering this application. If not explicitly set,
     * <code>null</code> is returned.
     * </p>
     *
     * @return the default render kit id, or <code>null</code>.
     */
    public abstract String getDefaultRenderKitId();

    /**
     * <p>
     * Set the <code>renderKitId</code> to be used to render this application. Unless the client has provided a custom
     * {@link ViewHandler} that supports the use of multiple {@link jakarta.faces.render.RenderKit} instances in the same
     * application, this method must only be called at application startup, before any Faces requests have been processed.
     * This is a limitation of the current Specification, and may be lifted in a future release.
     * </p>
     *
     * @param renderKitId the render kit id to set.
     */
    public abstract void setDefaultRenderKitId(String renderKitId);

    /**
     * <p>
     * Return the fully qualified class name of the <code>ResourceBundle</code> to be used for Jakarta Faces messages
     * for this application. If not explicitly set, <code>null</code> is returned.
     * </p>
     *
     * @return the message bundle class name, or <code>null</code>.
     */
    public abstract String getMessageBundle();

    /**
     * <p>
     * Set the fully qualified class name of the <code>ResourceBundle</code> to be used for Jakarta Faces messages
     * for this application. See the JavaDocs for the <code>java.util.ResourceBundle</code> class for more information about
     * the syntax for resource bundle names.
     * </p>
     *
     * @param bundle Base name of the resource bundle to be used
     *
     * @throws NullPointerException if <code>bundle</code> is <code>null</code>
     */
    public abstract void setMessageBundle(String bundle);

    /**
     * <p>
     * Return the {@link NavigationHandler} instance that will be passed the outcome returned by any invoked application
     * action for this web application. If not explicitly set, a default implementation must be provided that performs the
     * functions described in the {@link NavigationHandler} class description.
     * </p>
     *
     * <div class="changed_added_2_3">
     * <ul>
     * <li>The <code>NavigationHandler</code> implementation is declared in the application configuration resources by
     * giving the fully qualified class name as the value of the <code>&lt;navigation-handler&gt;</code> element within the
     * <code>&lt;application&gt;</code> element.</li>
     * </ul>
     * <p>
     * The runtime must employ the decorator pattern as for every other pluggable artifact in Jakarta Faces.
     * </p>
     * </div>
     *
     * @return the navigation handler.
     */
    public abstract NavigationHandler getNavigationHandler();

    /**
     * <p>
     * Set the {@link NavigationHandler} instance that will be passed the outcome returned by any invoked application action
     * for this web application.
     * </p>
     *
     * @param handler The new {@link NavigationHandler} instance
     *
     * @throws NullPointerException if <code>handler</code> is <code>null</code>
     */
    public abstract void setNavigationHandler(NavigationHandler handler);

    /**
     * <p class="changed_added_2_0">
     * Return the singleton, stateless, thread-safe {@link ResourceHandler} for this application. The Jakarta Faces
     * implementation must support the following techniques for declaring an alternate implementation of
     * <code>ResourceHandler</code>.
     * </p>
     * <div class="changed_added_2_0">
     * <ul>
     * <li>
     * <p>
     * The <code>ResourceHandler</code> implementation is declared in the application configuration resources by giving the
     * fully qualified class name as the value of the <code>&lt;resource-handler&gt;</code> element within the
     * <code>&lt;application&gt;</code> element.
     * </p>
     * </li>
     * </ul>
     * <p>
     * In all of the above cases, the runtime must employ the decorator pattern as for every other pluggable artifact in
     * Jakarta Faces.
     * </p>
     *
     * <p class="changed_added_2_0">
     * A default implementation is provided that throws <code>UnsupportedOperationException</code> so that users that
     * decorate <code>Application</code> can continue to function
     * </p>
     * .
     *
     *
     * </div>
     *
     * @return the resource handler.
     * @since 2.0
     */
    public ResourceHandler getResourceHandler() {
        if (defaultApplication != null) {
            return defaultApplication.getResourceHandler();
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_0">
     * Set the {@link ResourceHandler} instance that will be utilized for rendering the markup for resources, and for
     * satisfying client requests to serve up resources.
     * </p>
     *
     * @param resourceHandler The new <code>ResourceHandler</code> instance
     *
     * @throws IllegalStateException if this method is called after at least one request has been processed by the
     * <code>Lifecycle</code> instance for this application.
     * @throws NullPointerException if <code>resourceHandler</code> is <code>null</code>
     * @since 2.0
     */
    public void setResourceHandler(ResourceHandler resourceHandler) {
        if (defaultApplication != null) {
            defaultApplication.setResourceHandler(resourceHandler);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p>
     * Find a <code>ResourceBundle</code> as defined in the application configuration resources under the specified name. If
     * a <code>ResourceBundle</code> was defined for the name, return an instance that uses the locale of the current
     * {@link jakarta.faces.component.UIViewRoot}.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param ctx the Faces context.
     * @param name the name of the resource bundle.
     * @return the resource bundle.
     * @throws FacesException if a bundle was defined, but not resolvable
     * @throws NullPointerException if ctx == null || name == null
     * @since 1.2
     */
    public ResourceBundle getResourceBundle(FacesContext ctx, String name) {
        if (defaultApplication != null) {
            return defaultApplication.getResourceBundle(ctx, name);
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_0">
     * Return the project stage for the currently running application instance. The default value is
     * {@link ProjectStage#Production}
     * </p>
     *
     * <div class="changed_added_2_0">
     * <p>
     * The implementation of this method must perform the following algorithm or an equivalent with the same end result to
     * determine the value to return.
     * </p>
     *
     * <blockquote>
     *
     * <p>
     * If the value has already been determined by a previous call to this method, simply return that value.
     * </p>
     *
     * <p>
     * Look for a <code>JNDI</code> environment entry under the key given by the value of
     * {@link ProjectStage#PROJECT_STAGE_JNDI_NAME} (return type of <code>java.lang.String</code>). If found, continue with
     * the algorithm below, otherwise, look for an entry in the <code>initParamMap</code> of the
     * <code>ExternalContext</code> from the current <code>FacesContext</code> with the key given by the value of
     * {@link ProjectStage#PROJECT_STAGE_PARAM_NAME}
     *
     * </p>
     *
     * <p>
     * If a value is found, see if an enum constant can be obtained by calling <code>ProjectStage.valueOf()</code>, passing
     * the value from the <code>initParamMap</code>. If this succeeds without exception, save the value and return it.
     * </p>
     *
     * <p>
     * If not found, or any of the previous attempts to discover the enum constant value have failed, log a descriptive
     * error message, assign the value as <code>ProjectStage.Production</code> and return it.
     * </p>
     *
     * </blockquote>
     *
     * <p class="changed_added_2_0">
     * A default implementation is provided that throws <code>UnsupportedOperationException</code> so that users that
     * decorate <code>Application</code> can continue to function
     * </p>
     * .
     *
     * </div>
     *
     * @return the project stage.
     * @since 2.0
     */
    public ProjectStage getProjectStage() {
        if (defaultApplication != null) {
            return defaultApplication.getProjectStage();
        }

        return ProjectStage.Production;
    }

    /**
     * <p>
     * <span class="changed_modified_2_0_rev_a">Cause</span> an the argument <code>resolver</code> to be added to the
     * resolver chain as specified in section 5.3.2 "ELResolver" of the Jakarta Faces Specification Document.
     * </p>
     *
     * <p>
     * It is not possible to remove an <code>ELResolver</code> registered with this method, once it has been registered.
     * </p>
     *
     * <p>
     * It is illegal to register an <code>ELResolver</code> after the application has received any requests from the client.
     * If an attempt is made to register a listener after that time, an <code>IllegalStateException</code> must be thrown.
     * This restriction is in place to allow the Jakarta Server Pages container to optimize for the common case where no
     * additional <code>ELResolver</code>s are in the chain, aside from the standard ones. It is permissible to add
     * <code>ELResolver</code>s before or after initialization to a <code>CompositeELResolver</code> that is already in the
     * chain.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend {@link Application}.
     * </p>
     *
     * @param resolver the Jakarta Expression Language resolver to add.
     *
     * @throws IllegalStateException <span class="changed_modified_2_0_rev_a">if called after the first request to the
     * {@link jakarta.faces.webapp.FacesServlet} has been serviced.</span>
     *
     * @since 1.2
     */
    public void addELResolver(ELResolver resolver) {
        if (defaultApplication != null) {
            defaultApplication.addELResolver(resolver);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p>
     * Return the singleton {@link ELResolver} instance to be used for all Jakarta Expression Language resolution. This is
     * actually an instance of {@link jakarta.el.CompositeELResolver} that must contain the following
     * <code>ELResolver</code> instances in the following order:
     * </p>
     *
     * <ol>
     *
     * <li>
     * <p>
     * <code>ELResolver</code> instances declared using the &lt;el-resolver&gt; element in the application configuration
     * resources.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * An <code>implementation</code> that wraps the head of the legacy <code>VariableResolver</code> chain, as per section
     * <i>VariableResolver ChainWrapper</i> in Chapter 5 in the spec document.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * An <code>implementation</code> that wraps the head of the legacy <code>PropertyResolver</code> chain, as per section
     * <i>PropertyResolver ChainWrapper</i> in Chapter 5 in the spec document.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Any <code>ELResolver</code> instances added by calls to {@link #addELResolver}.
     * </p>
     * </li>
     *
     * </ol>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend {@link Application}.
     * </p>
     *
     * @return the Jakarta Expression Language resolver.
     * @since 1.2
     */
    public ELResolver getELResolver() {
        if (defaultApplication != null) {
            return defaultApplication.getELResolver();
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_2">
     * Return the thread-safe singleton {@link FlowHandler} for this application. For implementations declaring compliance
     * with version 2.2 of the specification, this method must never return {@code null}, even if the application has no
     * flows. This is necessary to enable dynamic flow creation during the application's lifetime.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * All implementations that declare compliance with version 2.2 of the specification must implement this method. For the
     * purpose of backward compatibility with environments that extend {@code
     * Application} but do not override this method, an implementation is provided that returns {@code null}. Due to the
     * decoratable nature of {@code Application}, code calling this method should always check for a {@code null} return.
     * </p>
     * </div>
     *
     * @return the flow handler.
     * @since 2.2
     */
    public FlowHandler getFlowHandler() {
        if (defaultApplication != null) {
            return defaultApplication.getFlowHandler();
        }

        return null;
    }

    /**
     * <p class="changed_added_2_2">
     * Set the {@link FlowHandler} instance used by the {@link NavigationHandler} to satisfy the requirements of the faces
     * flows feature.
     * </p>
     *
     * @param newHandler the flow handler to set.
     * @throws NullPointerException if newHandler is <code>null</code>
     * @throws IllegalStateException if this method is called after at least one request has been processed by the
     * {@code Lifecycle} instance for this application.
     * @since 2.2
     */
    public void setFlowHandler(FlowHandler newHandler) {
        if (defaultApplication != null) {
            defaultApplication.setFlowHandler(newHandler);
        }
    }

    /**
     * <p>
     * Return the {@link ViewHandler} instance that will be utilized during the <em>Restore View</em> and <em>Render
     * Response</em> phases of the request processing lifecycle. If not explicitly set, a default implementation must be
     * provided that performs the functions described in the {@link ViewHandler} description in the Jakarta Faces
     * Specification.
     * </p>
     *
     * @return the view handler.
     */
    public abstract ViewHandler getViewHandler();

    /**
     * <p>
     * Set the {@link ViewHandler} instance that will be utilized during the <em>Restore View</em> and <em>Render
     * Response</em> phases of the request processing lifecycle.
     * </p>
     *
     * @param handler The new {@link ViewHandler} instance
     *
     * @throws IllegalStateException if this method is called after at least one request has been processed by the
     * <code>Lifecycle</code> instance for this application.
     * @throws NullPointerException if <code>handler</code> is <code>null</code>
     */
    public abstract void setViewHandler(ViewHandler handler);

    /**
     * <p>
     * Return the {@link StateManager} instance that will be utilized during the <em>Restore View</em> and <em>Render
     * Response</em> phases of the request processing lifecycle. If not explicitly set, a default implementation must be
     * provided that performs the functions described in the {@link StateManager} description in the Jakarta Faces
     * Specification.
     * </p>
     *
     * @return the state manager.
     */
    public abstract StateManager getStateManager();

    /**
     * <p>
     * Set the {@link StateManager} instance that will be utilized during the <em>Restore View</em> and <em>Render
     * Response</em> phases of the request processing lifecycle.
     * </p>
     *
     * @param manager The new {@link StateManager} instance
     *
     * @throws IllegalStateException if this method is called after at least one request has been processed by the
     * <code>Lifecycle</code> instance for this application.
     * @throws NullPointerException if <code>manager</code> is <code>null</code>
     */
    public abstract void setStateManager(StateManager manager);

    // ------------------------------------------------------- Object Factories

    /**
     * <p>
     * <span class="changed_added_2_0">Register</span> a new mapping of behavior id to the name of the corresponding
     * {@link Behavior} class. This allows subsequent calls to <code>createBehavior()</code> to serve as a factory for
     * {@link Behavior} instances.
     * </p>
     *
     * @param behaviorId The behavior id to be registered
     * @param behaviorClass The fully qualified class name of the corresponding {@link Behavior} implementation
     *
     * @throws NullPointerException if <code>behaviorId</code> or <code>behaviorClass</code> is <code>null</code>
     *
     * @since 2.0
     */
    public void addBehavior(String behaviorId, String behaviorClass) {
        if (defaultApplication != null) {
            defaultApplication.addBehavior(behaviorId, behaviorClass);
        }
    }

    /**
     * <p>
     * <span class="changed_added_2_0">Instantiate</span> and return a new {@link Behavior} instance of the class specified
     * by a previous call to <code>addBehavior()</code> for the specified behavior id.
     * </p>
     *
     * @param behaviorId The behavior id for which to create and return a new {@link Behavior} instance
     * @return the behavior.
     * @throws FacesException if the {@link Behavior} cannot be created
     * @throws NullPointerException if <code>behaviorId</code> is <code>null</code>
     */
    public Behavior createBehavior(String behaviorId) throws FacesException {
        if (defaultApplication != null) {
            return defaultApplication.createBehavior(behaviorId);
        }

        return null;
    }

    /**
     * <p>
     * Return an <code>Iterator</code> over the set of currently registered behavior ids for this <code>Application</code>.
     * </p>
     *
     * @return an iterator with behavior ids.
     */
    public Iterator<String> getBehaviorIds() {
        if (defaultApplication != null) {
            return defaultApplication.getBehaviorIds();
        }

        return Collections.<String>emptyList().iterator();
    }

    /**
     * <p>
     * Register a new mapping of component type to the name of the corresponding {@link UIComponent} class. This allows
     * subsequent calls to <code>createComponent()</code> to serve as a factory for {@link UIComponent} instances.
     * </p>
     *
     * @param componentType The component type to be registered
     * @param componentClass The fully qualified class name of the corresponding {@link UIComponent} implementation
     *
     * @throws NullPointerException if <code>componentType</code> or <code>componentClass</code> is <code>null</code>
     */
    public abstract void addComponent(String componentType, String componentClass);

    /**
     * <p>
     * <span class="changed_modified_2_0">Instantiate</span> and return a new {@link UIComponent} instance of the class
     * specified by a previous call to <code>addComponent()</code> for the specified component type.
     * </p>
     *
     * <p class="changed_added_2_0">
     * Before the component instance is returned, it must be inspected for the presence of a
     * {@link jakarta.faces.event.ListenerFor} (or {@link jakarta.faces.event.ListenersFor}) or {@link ResourceDependency}
     * (or {@link ResourceDependencies}) annotation. If any of these annotations are present, the action listed in
     * {@link jakarta.faces.event.ListenerFor} or {@link ResourceDependency} must be taken on the component, before it is
     * returned from this method. This variant of <code>createComponent</code> must <strong>not</strong> inspect the
     * {@link jakarta.faces.render.Renderer} for the component to be returned for any of the afore mentioned annotations.
     * Such inspection is the province of {@link #createComponent(ValueExpression, FacesContext, String, String)} or
     * {@link #createComponent(FacesContext, String, String)}.
     * </p>
     *
     * @param componentType The component type for which to create and return a new {@link UIComponent} instance
     * @return the UI component.
     * @throws FacesException if a {@link UIComponent} of the specified type cannot be created
     * @throws NullPointerException if <code>componentType</code> is <code>null</code>
     */
    public abstract UIComponent createComponent(String componentType) throws FacesException;

    /**
     * <p>
     * <span class="changed_modified_2_0">Call</span> the <code>getValue()</code> method on the specified
     * {@link ValueExpression}. If it returns a {@link UIComponent} instance, return it as the value of this method. If it
     * does not, instantiate a new {@link UIComponent} instance of the specified component type, pass the new component to
     * the <code>setValue()</code> method of the specified {@link ValueExpression}, and return it.
     * </p>
     *
     * <p class="changed_added_2_0">
     * Before the component instance is returned, it must be inspected for the presence of a
     * {@link jakarta.faces.event.ListenerFor} (or {@link jakarta.faces.event.ListenersFor}) or {@link ResourceDependency}
     * (or {@link ResourceDependencies}) annotation. If any of these annotations are present, the action listed in
     * {@link jakarta.faces.event.ListenerFor} or {@link ResourceDependency} must be taken on the component, before it is
     * returned from this method. This variant of <code>createComponent</code> must <strong>not</strong> inspect the
     * {@link jakarta.faces.render.Renderer} for the component to be returned for any of the afore mentioned annotations.
     * Such inspection is the province of {@link #createComponent(ValueExpression, FacesContext, String, String)} or
     * {@link #createComponent(FacesContext, String, String)}.
     * </p>
     *
     * <p>
     * A default implementation is provided that throws <code>UnsupportedOperationException</code> so that users that
     * decorate <code>Application</code> can continue to function.
     * </p>
     *
     * @param componentExpression {@link ValueExpression} representing a component value expression (typically specified by
     * the <code>component</code> attribute of a custom tag)
     * @param context {@link FacesContext} for the current request
     * @param componentType Component type to create if the {@link ValueExpression} does not return a component instance
     * @return the UI component.
     * @throws FacesException if a {@link UIComponent} cannot be created
     * @throws NullPointerException if any parameter is <code>null</code>
     * @since 1.2
     */
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType) throws FacesException {
        if (defaultApplication != null) {
            return defaultApplication.createComponent(componentExpression, context, componentType);
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_0">
     * Like {@link #createComponent(ValueExpression, FacesContext, String)} except the <code>Renderer</code> for the
     * component to be returned must be inspected for the annotations mentioned in
     * {@link #createComponent(ValueExpression, FacesContext, String)} as specified in the documentation for that method.
     * The <code>Renderer</code> instance to inspect must be obtained by calling {@link FacesContext#getRenderKit} and
     * calling {@link jakarta.faces.render.RenderKit#getRenderer} on the result, passing the argument
     * <code>componentType</code> as the first argument and the result of calling {@link UIComponent#getFamily} on the newly
     * created component as the second argument. If no such <code>Renderer</code> can be found, a message must be logged
     * with a helpful error message. Otherwise, {@link UIComponent#setRendererType} must be called on the newly created
     * <code>UIComponent</code> instance, passing the argument <code>rendererType</code> as the argument.
     * </p>
     *
     * <p class="changed_added_2_0">
     * A default implementation is provided that throws <code>UnsupportedOperationException</code> so that users that
     * decorate <code>Application</code> can continue to function.
     * </p>
     *
     *
     * @param componentExpression {@link ValueExpression} representing a component value expression (typically specified by
     * the <code>component</code> attribute of a custom tag)
     * @param context {@link FacesContext} for the current request
     * @param componentType Component type to create if the {@link ValueExpression} does not return a component instance
     * @param rendererType The renderer-type of the <code>Renderer</code> that will render this component. A
     * <code>null</code> value must be accepted for this parameter.
     * @return the UI component.
     * @throws FacesException if a {@link UIComponent} cannot be created
     * @throws NullPointerException if any of the parameters <code>componentExpression</code>, <code>context</code>, or
     * <code>componentType</code> are <code>null</code>
     * @since 2.0
     */
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType, String rendererType) {
        if (defaultApplication != null) {
            return defaultApplication.createComponent(componentExpression, context, componentType, rendererType);
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_0">
     * Like {@link #createComponent(String)} except the <code>Renderer</code> for the component to be returned must be
     * inspected for the annotations mentioned in {@link #createComponent(ValueExpression, FacesContext, String)} as
     * specified in the documentation for that method. The <code>Renderer</code> instance to inspect must be obtained by
     * calling {@link FacesContext#getRenderKit} and calling {@link jakarta.faces.render.RenderKit#getRenderer} on the
     * result, passing the argument <code>componentType</code> as the first argument and the result of calling
     * {@link UIComponent#getFamily} on the newly created component as the second argument. If no such <code>Renderer</code>
     * can be found, a message must be logged with a helpful error message. Otherwise, {@link UIComponent#setRendererType}
     * must be called on the newly created <code>UIComponent</code> instance, passing the argument <code>rendererType</code>
     * as the argument.
     * </p>
     *
     * <p class="changed_added_2_0">
     * A default implementation is provided that throws <code>UnsupportedOperationException</code> so that users that
     * decorate <code>Application</code> can continue to function
     * </p>
     * .
     *
     *
     * @param context {@link FacesContext} for the current request
     * @param componentType Component type to create
     * @param rendererType The renderer-type of the <code>Renderer</code> that will render this component. A
     * <code>null</code> value must be accepted for this parameter.
     * @return the UI component.
     * @throws FacesException if a {@link UIComponent} cannot be created
     * @throws NullPointerException if any of the parameters <code>context</code>, or <code>componentType</code> are
     * <code>null</code>
     * @since 2.0
     */
    public UIComponent createComponent(FacesContext context, String componentType, String rendererType) {
        if (defaultApplication != null) {
            return defaultApplication.createComponent(context, componentType, rendererType);
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_0_rev_a">Instantiate</span> and return a new {@link UIComponent} instance from the
     * argument {@link Resource}. An algorithm semantically equivalent to the following must be followed to instantiate the
     * <code>UIComponent</code> to return.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <ul>
     *
     * <li>
     * <p>
     * Obtain a reference to the {@link ViewDeclarationLanguage} for this <code>Application</code> instance by calling
     * {@link ViewHandler#getViewDeclarationLanguage}, passing the <code>viewId</code> found by calling
     * {@link jakarta.faces.component.UIViewRoot#getViewId} on the {@link jakarta.faces.component.UIViewRoot} in the
     * argument {@link FacesContext}.
     * </p>
     * </li>
     *
     *
     * <li>
     * <p>
     * Obtain a reference to the <em>composite component metadata</em> for this composite component by calling
     * {@link ViewDeclarationLanguage#getComponentMetadata}, passing the <code>facesContext</code> and
     * <code>componentResource</code> arguments to this method. This version of the Jakarta Faces Specification uses
     * JavaBeans as the API to the component metadata.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Determine if the component author declared a <code><span
     class="changed_modified_2_0_rev_a">componentType</span></code> for this component instance by obtaining the
     * <code>BeanDescriptor</code> from the component metadata and calling its <code>getValue()</code> method, passing
     * {@link UIComponent#COMPOSITE_COMPONENT_TYPE_KEY} as the argument. If non-<code>null</code>, the result must be a
     * <code>ValueExpression</code> whose value is the <code>component-type</code> of the <code>UIComponent</code> to be
     * created for this <code>Resource</code> component. Call through to {@link #createComponent(java.lang.String)} to
     * create the component.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Otherwise, determine if a script based component for this <code>Resource</code> can be found by calling
     * {@link ViewDeclarationLanguage#getScriptComponentResource}. If the result is non-<code>null</code>, and is a script
     * written in <span class="changed_modified_4_0">a language satisfying the content type <code>text/javascript</code></span>, create a
     * <code>UIComponent</code> instance from the script resource.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Otherwise, let <em>library-name</em> be the return from calling {@link Resource#getLibraryName} on the argument
     * <code>componentResource</code> and <em>resource-name</em> be the return from calling {@link Resource#getResourceName}
     * on the argument <code>componentResource</code>. Create a fully qualified Java class name by removing any file
     * extension from <em>resource-name</em> and let <em>fqcn</em> be <code><em>library-name</em> + "." +
     <em>resource-name</em></code>. If a class with the name of <em>fqcn</em> cannot be found, take no action and continue
     * to the next step. If any of <code>InstantiationException</code>, <code>IllegalAccessException</code>, or
     * <code>ClassCastException</code> are thrown, wrap the exception in a <code>FacesException</code> and re-throw it. If
     * any other exception is thrown, log the exception and continue to the next step.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * If none of the previous steps have yielded a <code>UIComponent</code> instance, call
     * {@link #createComponent(java.lang.String)} passing "<code>jakarta.faces.NamingContainer</code>" as the argument.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Call {@link UIComponent#setRendererType} on the <code>UIComponent</code> instance, passing
     * "<code>jakarta.faces.Composite</code>" as the argument.
     * </p>
     * </li>
     *
     * <li>
     *
     * <p>
     * Store the argument <code>Resource</code> in the attributes <code>Map</code> of the <code>UIComponent</code> under the
     * key, {@link Resource#COMPONENT_RESOURCE_KEY}.
     * </p>
     *
     * </li>
     *
     * <li>
     *
     * <p>
     * Store <em>composite component metadata</em> in the attributes <code>Map</code> of the <code>UIComponent</code> under
     * the key, {@link UIComponent#BEANINFO_KEY}.
     * </p>
     *
     * </li>
     *
     * </ul>
     *
     * <p>
     * Before the component instance is returned, it must be inspected for the presence of a
     * {@link jakarta.faces.event.ListenerFor} annotation. If this annotation is present, the action listed in
     * {@link jakarta.faces.event.ListenerFor} must be taken on the component, before it is returned from this method.
     * </p>
     *
     * <p>
     * A default implementation is provided that throws <code>UnsupportedOperationException</code> so that users that
     * decorate <code>Application</code> can continue to function.
     * </p>
     *
     * </div>
     *
     * @param context {@link FacesContext} for the current request
     * @param componentResource A {@link Resource} that points to a source file that provides an implementation of a
     * component.
     * @return the UI component.
     * @throws FacesException if a {@link UIComponent} from the {@link Resource} cannot be created
     * @throws NullPointerException if any parameter is <code>null</code>
     * @throws NullPointerException if unable, for any reason, to obtain a <code>ViewDeclarationLanguage</code> instance as
     * described above.
     * @since 2.0
     */
    public UIComponent createComponent(FacesContext context, Resource componentResource) {
        if (defaultApplication != null) {
            return defaultApplication.createComponent(context, componentResource);
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Return an <code>Iterator</code> over the set of currently defined component types for this <code>Application</code>.
     * </p>
     *
     * @return an iterator with component types.
     */
    public abstract Iterator<String> getComponentTypes();

    /**
     * <p>
     * Register a new mapping of converter id to the name of the corresponding {@link Converter} class. This allows
     * subsequent calls to <code>createConverter()</code> to serve as a factory for {@link Converter} instances.
     * </p>
     *
     * @param converterId The converter id to be registered
     * @param converterClass The fully qualified class name of the corresponding {@link Converter} implementation
     *
     * @throws NullPointerException if <code>converterId</code> or <code>converterClass</code> is <code>null</code>
     */
    public abstract void addConverter(String converterId, String converterClass);

    /**
     * <p>
     * Register a new converter class that is capable of performing conversions for the specified target class.
     * </p>
     *
     * @param targetClass The class for which this converter is registered
     * @param converterClass The fully qualified class name of the corresponding {@link Converter} implementation
     *
     * @throws NullPointerException if <code>targetClass</code> or <code>converterClass</code> is <code>null</code>
     */
    public abstract void addConverter(Class<?> targetClass, String converterClass);

    /**
     * <p>
     * <span class="changed_modified_2_0">Instantiate</span> and return a new {@link Converter} instance of the class
     * specified by a previous call to <code>addConverter()</code> for the specified converter id.
     * </p>
     *
     * <p class="changed_added_2_0">
     * If the <code>toLowerCase()</code> of the <code>String</code> represenation of the value of the
     * "<code>jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE</code>" application configuration
     * parameter is "<code>true</code>" (without the quotes) and the <code>Converter</code> instance to be returned is an
     * instance of {@link jakarta.faces.convert.DateTimeConverter},
     * {@link jakarta.faces.convert.DateTimeConverter#setTimeZone} must be called, passing the return from
     * <code>TimeZone.getDefault()</code>.
     * </p>
     *
     * <p class="changed_added_2_0">
     * The argument <code>converter</code> must be inspected for the presence of the
     * {@link jakarta.faces.application.ResourceDependency} annotation. If the <code>ResourceDependency</code> annotation is
     * present, the action described in <code>ResourceDependency</code> must be taken. If the
     * <code>ResourceDependency</code> annotation is not present, the argument <code>converter</code> must be inspected for
     * the presence of the {@link jakarta.faces.application.ResourceDependencies} annotation. If the
     * <code>ResourceDependencies</code> annotation is present, the action described in <code>ResourceDependencies</code>
     * must be taken.
     * </p>
     *
     * @param converterId The converter id for which to create and return a new {@link Converter} instance
     * @return the converter.
     * @throws FacesException if the {@link Converter} cannot be created
     * @throws NullPointerException if <code>converterId</code> is <code>null</code>
     */
    public abstract Converter createConverter(String converterId);

    /**
     * <p>
     * <span class="changed_modified_2_0">Instantiate</span> and return a new {@link Converter} instance of the class that
     * has registered itself as capable of performing conversions for objects of the specified type. If no such
     * {@link Converter} class can be identified, return <code>null</code>.
     * </p>
     *
     * <p>
     * To locate an appropriate {@link Converter} class, the following algorithm is performed, stopping as soon as an
     * appropriate {@link Converter} class is found:
     * </p>
     * <ul>
     * <li>Locate a {@link Converter} registered for the target class itself.</li>
     * <li>Locate a {@link Converter} registered for interfaces that are implemented by the target class (directly or
     * indirectly).</li>
     * <li>Locate a {@link Converter} registered for the superclass (if any) of the target class, recursively working up the
     * inheritance hierarchy.</li>
     * </ul>
     *
     * <p>
     * If the <code>Converter</code> has a single argument constructor that accepts a <code>Class</code>, instantiate the
     * <code>Converter</code> using that constructor, passing the argument <code>targetClass</code> as the sole argument.
     * Otherwise, simply use the zero-argument constructor.
     * </p>
     *
     * <p class="changed_added_2_0">
     * If the <code>toLowerCase()</code> of the <code>String</code> represenation of the value of the
     * "<code>jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE</code>" application configuration
     * parameter is "<code>true</code>" (without the quotes) and the <code>Converter</code> instance to be returned is an
     * instance of {@link jakarta.faces.convert.DateTimeConverter},
     * {@link jakarta.faces.convert.DateTimeConverter#setTimeZone} must be called, passing the return from
     * <code>TimeZone.getDefault()</code>.
     * </p>
     *
     * @param targetClass Target class for which to return a {@link Converter}
     * @return the converter.
     * @throws FacesException if the {@link Converter} cannot be created
     * @throws NullPointerException if <code>targetClass</code> is <code>null</code>
     */
    public abstract Converter createConverter(Class<?> targetClass);

    /**
     * <p>
     * Return an <code>Iterator</code> over the set of currently registered converter ids for this <code>Application</code>.
     * </p>
     *
     * @return an iterator with converter ids.
     */
    public abstract Iterator<String> getConverterIds();

    /**
     * <p>
     * Return an <code>Iterator</code> over the set of <code>Class</code> instances for which {@link Converter} classes have
     * been explicitly registered.
     * </p>
     *
     * @return an iterator with converter types.
     */
    public abstract Iterator<Class<?>> getConverterTypes();

    /**
     * <p class="changed_added_2_0">
     * Register a validator by its id that is applied to all <code>UIInput</code> components in a view. The validator to
     * most often serve this role is the <code>BeanValidator</code>. The usage contract for this method assumes that the
     * validator has been registered using the normal &#8220;by-id&#8221; registration mechanism.
     * </p>
     *
     * <p>
     * An implementation is provided that takes no action so that users that decorate the <code>Application</code> continue
     * to work.
     *
     * @param validatorId the validator id.
     * @since 2.0
     */
    public void addDefaultValidatorId(String validatorId) {
        if (defaultApplication != null) {
            defaultApplication.addDefaultValidatorId(validatorId);
        }
    }

    /**
     * <p class="changed_added_2_0">
     * Return an immutable <code>Map</code> over the set of currently registered default validator IDs and their class name
     * for this <code>Application</code>.
     * </p>
     *
     * <p>
     * An implementation is provided that returns <code>Collections.emptyMap</code> so that users that decorate the
     * <code>Application</code> continue to work.
     *
     * @return a map of default validator information.
     * @since 2.0
     */
    public Map<String, String> getDefaultValidatorInfo() {
        if (defaultApplication != null) {
            return defaultApplication.getDefaultValidatorInfo();
        }

        return Collections.emptyMap();
    }

    /**
     * <p>
     * Return the {@link ExpressionFactory} instance for this application. This instance is used by the convenience method
     * {@link #evaluateExpressionGet}.
     * </p>
     *
     * <p>
     * The implementation must return the <code>ExpressionFactory</code> from the Expression Language container by calling
     * <code>jakarta.el.ELManager.getExpressionFactory()</code>.
     * </p>
     *
     * <p>
     * An implementation is provided that throws <code>UnsupportedOperationException</code> so that users that decorate the
     * <code>Application</code> continue to work.
     *
     * @return the expression factory.
     * @since 1.2
     */
    public ExpressionFactory getExpressionFactory() {
        if (defaultApplication != null) {
            return defaultApplication.getExpressionFactory();
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Get a value by evaluating an expression.
     * </p>
     *
     * <p>
     * Call {@link #getExpressionFactory} then call {@link ExpressionFactory#createValueExpression} passing the argument
     * <code>expression</code> and <code>expectedType</code>. Call {@link FacesContext#getELContext} and pass it to
     * {@link ValueExpression#getValue}, returning the result.
     * </p>
     *
     * <p>
     * An implementation is provided that throws <code>UnsupportedOperationException</code> so that users that decorate the
     * <code>Application</code> continue to work.
     *
     * @param <T> the return type.
     * @param context the Faces context.
     * @param expression the expression.
     * @param expectedType the expected type.
     * @return the result of the evaluation.
     */
    public <T> T evaluateExpressionGet(FacesContext context, String expression, Class<? extends T> expectedType) throws ELException {
        if (defaultApplication != null) {
            return defaultApplication.evaluateExpressionGet(context, expression, expectedType);
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Return an <code>Iterator</code> over the supported <code>Locale</code>s for this appication.
     * </p>
     *
     * @return an iterator of the supported locales.
     */
    public abstract Iterator<Locale> getSupportedLocales();

    /**
     * <p>
     * Set the <code>Locale</code> instances representing the supported <code>Locale</code>s for this application.
     * </p>
     *
     * @param locales The set of supported <code>Locale</code>s for this application
     *
     * @throws NullPointerException if the argument <code>newLocales</code> is <code>null</code>.
     *
     */
    public abstract void setSupportedLocales(Collection<Locale> locales);

    /**
     * <p>
     * Provide a way for Faces applications to register an <code>ELContextListener</code> that will be notified on creation
     * of <code>ELContext</code> instances. This listener will be called once per request.
     * </p>
     *
     * <p>
     * An implementation is provided that throws <code>UnsupportedOperationException</code> so that users that decorate the
     * <code>Application</code> continue to work.
     *
     * @param listener the Jakarta Expression Language context listener to add.
     * @since 1.2
     */

    public void addELContextListener(ELContextListener listener) {
        if (defaultApplication != null) {
            defaultApplication.addELContextListener(listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p>
     * Remove the argument <code>listener</code> from the list of {@link ELContextListener}s. If <code>listener</code> is
     * null, no exception is thrown and no action is performed. If <code>listener</code> is not in the list, no exception is
     * thrown and no action is performed.
     * </p>
     *
     * <p>
     * An implementation is provided that throws <code>UnsupportedOperationException</code> so that users that decorate the
     * <code>Application</code> continue to work.
     *
     * @param listener the Jakarta Expression Language context listener to remove.
     * @since 1.2
     */

    public void removeELContextListener(ELContextListener listener) {
        if (defaultApplication != null) {
            defaultApplication.removeELContextListener(listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p>
     * If no calls have been made to {@link #addELContextListener}, this method must return an empty array.
     * </p>
     *
     * <p>
     * Otherwise, return an array representing the list of listeners added by calls to {@link #addELContextListener}.
     * </p>
     *
     * <p>
     * An implementation is provided that throws <code>UnsupportedOperationException</code> so that users that decorate the
     * <code>Application</code> continue to work.
     *
     * @return an array of Jakarta Expression Language context listeners.
     * @since 1.2
     */

    public ELContextListener[] getELContextListeners() {
        if (defaultApplication != null) {
            return defaultApplication.getELContextListeners();
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Register a new mapping of validator id to the name of the corresponding {@link Validator} class. This allows
     * subsequent calls to <code>createValidator()</code> to serve as a factory for {@link Validator} instances.
     * </p>
     *
     * @param validatorId The validator id to be registered
     * @param validatorClass The fully qualified class name of the corresponding {@link Validator} implementation
     *
     * @throws NullPointerException if <code>validatorId</code> or <code>validatorClass</code> is <code>null</code>
     */
    public abstract void addValidator(String validatorId, String validatorClass);

    /**
     * <p>
     * <span class="changed_modified_2_0">Instantiate</span> and return a new {@link Validator} instance of the class
     * specified by a previous call to <code>addValidator()</code> for the specified validator id.
     * </p>
     *
     * <p class="changed_added_2_0">
     * The argument <code>validator</code> must be inspected for the presence of the
     * {@link jakarta.faces.application.ResourceDependency} annotation. If the <code>ResourceDependency</code> annotation is
     * present, the action described in <code>ResourceDependency</code> must be taken. If the
     * <code>ResourceDependency</code> annotation is not present, the argument <code>validator</code> must be inspected for
     * the presence of the {@link jakarta.faces.application.ResourceDependencies} annotation. If the
     * <code>ResourceDependencies</code> annotation is present, the action described in <code>ResourceDependencies</code>
     * must be taken.
     * </p>
     *
     * @param validatorId The validator id for which to create and return a new {@link Validator} instance
     * @return the validator.
     * @throws FacesException if a {@link Validator} of the specified id cannot be created
     * @throws NullPointerException if <code>validatorId</code> is <code>null</code>
     */
    public abstract Validator createValidator(String validatorId) throws FacesException;

    /**
     * <p>
     * Return an <code>Iterator</code> over the set of currently registered validator ids for this <code>Application</code>.
     * </p>
     *
     * @return an iterator of validator ids.
     */
    public abstract Iterator<String> getValidatorIds();

    /**
     * <p class="changed_added_2_0">
     * If {@link jakarta.faces.context.FacesContext#isProcessingEvents()} is <code>true</code> and there are one or more
     * listeners for events of the type represented by <code>systemEventClass</code>, call those listeners, passing
     * <code>source</code> as the source of the event. The implementation should be as fast as possible in determining
     * whether or not a listener for the given <code>systemEventClass</code> and <code>source</code> has been installed, and
     * should return immediately once such a determination has been made. The implementation of <code>publishEvent</code>
     * must honor the requirements stated in {@link #subscribeToEvent} regarding the storage and retrieval of listener
     * instances. Specifically, if {@link #subscribeToEvent(Class,Class,SystemEventListener)} was called, the
     * <code>sourceClass</code> argument must match exactly the <code>Class</code> of the <code>source</code> argument in
     * the call to <code>publishEvent()</code>. The implementation must not do any inheritance hierarachy inspection when
     * looking for a match between the <code>sourceClass</code> passed to
     * {@link #subscribeToEvent(Class,Class,SystemEventListener)} and the <code>sourceClass</code> passed to
     * <code>publishEvent()</code> in order to find any listeners to which the event should be published. In the case where
     * the <code>Class</code> of the <code>source</code> argument does not match the <code>Class</code> of the
     * <code>sourceClass</code> used when the listener was subscribed using <code>subscribeToEvent()</code>,
     * {@link #publishEvent(FacesContext,Class,Class,Object)} can be used to provide the <code>Class</code> used to perform
     * the listener lookup and match.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * The default implementation must implement an algorithm semantically equivalent to the following to locate listener
     * instances and to invoke them.
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * If the <code>source</code> argument implements {@link jakarta.faces.event.SystemEventListenerHolder}, call
     * {@link jakarta.faces.event.SystemEventListenerHolder#getListenersForEventClass} on it, passing the
     * <code>systemEventClass</code> argument. If the list is not empty, perform algorithm <em>traverseListenerList</em> on
     * the list.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * If any <em>view</em> level listeners have been installed by previous calls to
     * {@link #subscribeToEvent(Class, Class, jakarta.faces.event.SystemEventListener)} on the
     * {@link jakarta.faces.component.UIViewRoot}, perform algorithm <em>traverseListenerList</em> on the list of listeners
     * for that event installed on the <code>UIViewRoot</code>.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * If any <code>Application</code> level listeners have been installed by previous calls to
     * {@link #subscribeToEvent(Class, Class, jakarta.faces.event.SystemEventListener)}, perform algorithm
     * <em>traverseListenerList</em> on the list.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * If any <code>Application</code> level listeners have been installed by previous calls to
     * {@link #subscribeToEvent(Class, jakarta.faces.event.SystemEventListener)}, perform algorithm
     * <em>traverseListenerList</em> on the list.
     * </p>
     * </li>
     *
     * </ul>
     *
     * <p>
     * If the act of invoking the <code>processListener</code> method causes an
     * {@link jakarta.faces.event.AbortProcessingException} to be thrown, processing of the listeners must be aborted, no
     * further processing of the listeners for this event must take place, and the exception must be logged with
     * <code>Level.SEVERE</code>.
     * </p>
     *
     * <p>
     * Algorithm <em>traverseListenerList</em>: For each listener in the list,
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * Call {@link jakarta.faces.event.SystemEventListener#isListenerForSource}, passing the <code>source</code> argument.
     * If this returns <code>false</code>, take no action on the listener.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Otherwise, if the event to be passed to the listener instances has not yet been constructed, construct the event,
     * passing <code>source</code> as the argument to the one-argument constructor that takes an <code>Object</code>. This
     * same event instance must be passed to all listener instances.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Call {@link jakarta.faces.event.SystemEvent#isAppropriateListener}, passing the listener instance as the argument. If
     * this returns <code>false</code>, take no action on the listener.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Call {@link jakarta.faces.event.SystemEvent#processListener}, passing the listener instance.
     * </p>
     * </li>
     *
     * </ul>
     *
     * <p class="changed_added_2_0">
     * A default implementation is provided that throws <code>UnsupportedOperationException</code> so that users that
     * decorate <code>Application</code> can continue to function
     * </p>
     * .
     *
     * </div>
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param systemEventClass The <code>Class</code> of event that is being published.
     * @param source The source for the event of type <code>systemEventClass</code>.
     *
     * @throws NullPointerException if either <code>context</code>, <code>systemEventClass</code> or <code>source</code> is
     * <code>null</code>
     *
     * @since 2.0
     *
     */
    public void publishEvent(FacesContext context, Class<? extends SystemEvent> systemEventClass, Object source) {
        if (defaultApplication != null) {
            defaultApplication.publishEvent(context, systemEventClass, source);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p class="changed_added_2_0">
     * This method functions exactly like {@link #publishEvent(FacesContext,Class,Object)}, except the run-time must use the
     * argument <code>sourceBaseType</code> to find the matching listener instead of using the <code>Class</code> of the
     * <code>source</code> argument.
     * </p>
     *
     * <p class="changed_added_2_0">
     * A default implementation is provided that throws <code>UnsupportedOperationException</code> so that users that
     * decorate <code>Application</code> can continue to function
     * </p>
     * .
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param systemEventClass The <code>Class</code> of event that is being published.
     * @param sourceBaseType The <code>Class</code> of the source event that must be used to lookup the listener to which
     * this event must be published. If this argument is <code>null</code> the return from <code>source.getClass()</code>
     * must be used as the <code>sourceBaseType</code>.
     * @param source The source for the event of type <code>systemEventClass</code>.
     *
     * @throws NullPointerException if any arguments except for <code>sourceBaseType</code> are <code>null</code>
     *
     * @since 2.0
     */

    public void publishEvent(FacesContext context, Class<? extends SystemEvent> systemEventClass, Class<?> sourceBaseType, Object source) {
        if (defaultApplication != null) {
            defaultApplication.publishEvent(context, systemEventClass, sourceBaseType, source);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Install</span> the listener instance referenced by argument <code>listener</code>
     * into the application as a listener for events of type <code>systemEventClass</code> that originate from objects of
     * type <code>sourceClass</code>.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * If argument <code>sourceClass</code> is non-<code>null</code>, <code>sourceClass</code> and
     * <code>systemEventClass</code> must be used to store the argument <code>listener</code> in the application in such a
     * way that the <code>listener</code> can be quickly looked up by the implementation of {@link #publishEvent} given
     * <code>systemEventClass</code> and an instance of the <code>Class</code> referenced by <code>sourceClass</code>. If
     * argument <code>sourceClass</code> is <code>null</code>, the <code>listener</code> must be discoverable by the
     * implementation of {@link #publishEvent} given only <code>systemEventClass</code>.
     * </p>
     *
     * </div>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * It is valid to call this method <strong>during</strong> the processing of an event which was subscribed to by a
     * previous call to this method.
     * </p>
     *
     * </div>
     *
     *
     * @param systemEventClass the <code>Class</code> of event for which <code>listener</code> must be fired.
     *
     * @param sourceClass the <code>Class</code> of the instance which causes events of type <code>systemEventClass</code>
     * to be fired. May be <code>null</code>.
     *
     * @param listener the implementation of {@link jakarta.faces.event.SystemEventListener} whose
     * {@link jakarta.faces.event.SystemEventListener#processEvent} method must be called when events of type
     * <code>systemEventClass</code> are fired.
     *
     * @throws NullPointerException if any combination of <code>systemEventClass</code>, or <code>listener</code> are
     * <code>null</code>.
     *
     * @since 2.0
     */
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {
        if (defaultApplication != null) {
            defaultApplication.subscribeToEvent(systemEventClass, sourceClass, listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Install</span> the listener instance referenced by argument <code>listener</code>
     * into application as a listener for events of type <code>systemEventClass</code>. The default implementation simply
     * calls through to {@link #subscribeToEvent(Class, Class, jakarta.faces.event.SystemEventListener)} passing
     * <code>null</code> as the <code>sourceClass</code> argument
     * </p>
     *
     * <p class="changed_added_2_0">
     * A default implementation is provided that throws <code>UnsupportedOperationException</code> so that users that
     * decorate <code>Application</code> can continue to function
     * </p>
     * .
     *
     * @param systemEventClass the <code>Class</code> of event for which <code>listener</code> must be fired.
     *
     * @param listener the implementation of {@link jakarta.faces.event.SystemEventListener} whose
     * {@link jakarta.faces.event.SystemEventListener#processEvent} method must be called when events of type
     * <code>systemEventClass</code> are fired.
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * See {@link #subscribeToEvent(java.lang.Class,java.lang.Class,jakarta.faces.event.SystemEventListener)} for an
     * additional requirement regarding when it is valid to call this method.
     * </p>
     *
     * </div>
     *
     * @throws NullPointerException if any combination of <code>systemEventClass</code>, or <code>listener</code> are
     * <code>null</code>.
     *
     * @since 2.0
     */
    public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener) {
        if (defaultApplication != null) {
            defaultApplication.subscribeToEvent(systemEventClass, listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Remove</span> the listener instance referenced by argument <code>listener</code>
     * from the application as a listener for events of type <code>systemEventClass</code> that originate from objects of
     * type <code>sourceClass</code>. See {@link #subscribeToEvent(Class, Class, jakarta.faces.event.SystemEventListener)}
     * for the specification of how the listener is stored, and therefore, how it must be removed.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * See {@link #subscribeToEvent(java.lang.Class,java.lang.Class,jakarta.faces.event.SystemEventListener)} for an
     * additional requirement regarding when it is valid to call this method.
     * </p>
     *
     * </div>
     *
     * @param systemEventClass the <code>Class</code> of event for which <code>listener</code> must be fired.
     *
     * @param sourceClass the <code>Class</code> of the instance which causes events of type <code>systemEventClass</code>
     * to be fired. May be <code>null</code>.
     *
     * @param listener the implementation of {@link jakarta.faces.event.SystemEventListener} to remove from the internal
     * data structure.
     *
     * @throws NullPointerException if any combination of <code>context</code>, <code>systemEventClass</code>, or
     * <code>listener</code> are <code>null</code>.
     *
     * @since 2.0
     */
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, Class<?> sourceClass, SystemEventListener listener) {
        if (defaultApplication != null) {
            defaultApplication.unsubscribeFromEvent(systemEventClass, sourceClass, listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">Remove</span> the listener instance referenced by argument <code>listener</code>
     * from the application as a listener for events of type <code>systemEventClass</code>. The default implementation
     * simply calls through to {@link #unsubscribeFromEvent(Class, jakarta.faces.event.SystemEventListener)} passing
     * <code>null</code> as the <code>sourceClass</code> argument
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * See {@link #subscribeToEvent(java.lang.Class,java.lang.Class,jakarta.faces.event.SystemEventListener)} for an
     * additional requirement regarding when it is valid to call this method.
     * </p>
     *
     * </div>
     *
     * @param systemEventClass the <code>Class</code> of event for which <code>listener</code> must be fired.
     *
     * @param listener the implementation of {@link jakarta.faces.event.SystemEventListener} to remove from the internal
     * data structure.
     *
     * @throws NullPointerException if any combination of <code>context</code>, <code>systemEventClass</code>, or
     * <code>listener</code> are <code>null</code>.
     *
     * @since 2.0
     */
    public void unsubscribeFromEvent(Class<? extends SystemEvent> systemEventClass, SystemEventListener listener) {
        if (defaultApplication != null) {
            defaultApplication.unsubscribeFromEvent(systemEventClass, listener);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p class="changed_added_2_3">
     * Return the thread-safe singleton {@link SearchExpressionHandler} for this application.
     * </p>
     *
     * @return the {@link SearchExpressionHandler}.
     * @since 2.3
     */
    public SearchExpressionHandler getSearchExpressionHandler() {
        if (defaultApplication != null) {
            return defaultApplication.getSearchExpressionHandler();
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_3">
     * Set the {@link SearchExpressionHandler} instance used by the application.
     * </p>
     *
     * @param searchExpressionHandler the {@link SearchExpressionHandler}.
     * @throws NullPointerException if searchExpressionHandler is <code>null</code>
     * @throws IllegalStateException if this method is called after at least one request has been processed by the
     * {@code Lifecycle} instance for this application.
     *
     * @since 2.3
     */
    public void setSearchExpressionHandler(SearchExpressionHandler searchExpressionHandler) {
        if (defaultApplication != null) {
            defaultApplication.setSearchExpressionHandler(searchExpressionHandler);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p class="changed_added_2_3">
     * Cause the argument <code>resolver</code> to be added to the head of the resolver chain.
     * </p>
     *
     * <div class="changed_added_2_3">
     *
     * <p>
     * It is not possible to remove a {@link SearchKeywordResolver} registered with this method, once it has been
     * registered.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend {@link Application}.
     * </p>
     *
     * </div>
     *
     * @param resolver the SearchKeywordResolver to add.
     *
     * @throws IllegalStateException if called after the first request to the {@link jakarta.faces.webapp.FacesServlet} has
     * been serviced.
     * @throws NullPointerException when resolver is null.
     *
     * @since 2.3
     */
    public void addSearchKeywordResolver(SearchKeywordResolver resolver) {
        if (defaultApplication != null) {
            defaultApplication.addSearchKeywordResolver(resolver);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p class="changed_added_2_3">
     * Return the singleton {@link SearchKeywordResolver} instance to be used for all search keyword resolution. This is
     * actually an instance of a composite SearchKeywordResolver that must contain the following
     * <code>SearchKeywordResolver</code> instances in the following order:
     * </p>
     *
     * <ol>
     *
     * <li>
     * <p>
     * <code>SearchKeywordResolver</code> instances declared using the &lt;search-keyword-resolver&gt; element in the
     * application configuration resources.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Any <code>SearchKeywordResolver</code> instances added by calls to {@link #addSearchKeywordResolver}.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * The <code>SearchKeywordResolver</code> implementations for <code>@all</code>, <code>@child(n)</code>,
     * <code>@form</code>, <code>@id(...)</code>, <code>@namingcontainer</code>, <code>@next</code>, <code>@none</code>,
     * <code>@parent</code>, <code>@previous</code>, <code>@root</code> and <code>@this</code>.
     * </p>
     * </li>
     *
     * </ol>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend {@link Application}.
     * </p>
     *
     * @return the {@link SearchKeywordResolver}.
     * @since 2.3
     */
    public SearchKeywordResolver getSearchKeywordResolver() {
        if (defaultApplication != null) {
            return defaultApplication.getSearchKeywordResolver();
        }

        throw new UnsupportedOperationException();
    }
}
