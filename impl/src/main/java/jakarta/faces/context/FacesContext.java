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

package jakarta.faces.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.faces.config.InitFacesContext;
import jakarta.el.ELContext;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.event.PhaseId;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.render.RenderKit;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_1 changed_modified_2_2">FacesContext</strong> contains all of
 * the per-request state information related to the processing of a single Jakarta Faces request, and the
 * rendering of the corresponding response. It is passed to, and potentially modified by, each phase of the request
 * processing lifecycle.
 * </p>
 *
 * <p>
 * A {@link FacesContext} instance is associated with a particular request at the beginning of request processing, by a
 * call to the <code>getFacesContext()</code> method of the {@link FacesContextFactory} instance associated with the
 * current web application. The instance remains active until its <code>release()</code> method is called, after which
 * no further references to this instance are allowed. While a {@link FacesContext} instance is active, it must not be
 * referenced from any thread other than the one upon which the Jakarta Servlet container executing this web application
 * utilizes for the processing of this request.
 * </p>
 *
 * <p class="changed_added_2_3">
 * A FacesContext can be injected into a request scoped bean using <code>@Inject FacesContext facesContext;</code>
 * </p>
 */
public abstract class FacesContext {

    private FacesContext defaultFacesContext;
    private boolean processingEvents = true;
    private boolean isCreatedFromValidFactory = true;

    private static final ConcurrentHashMap<Thread,FacesContext> threadInitContext = new ConcurrentHashMap<>(2);
    private static final ConcurrentHashMap<Thread,InitFacesContext> initContextServletContext = new ConcurrentHashMap<>(2);

    /**
     * Default constructor.
     * <p>
     * This looks at the callstack to see if we're created from a factory.
     * </p>
     */
    public FacesContext() {
        Thread curThread = Thread.currentThread();
        StackTraceElement[] callstack = curThread.getStackTrace();
        String declaringClassName = callstack[3].getClassName();
        try {
            ClassLoader curLoader = curThread.getContextClassLoader();
            Class<?> declaringClass = curLoader.loadClass(declaringClassName);
            if (!FacesContextFactory.class.isAssignableFrom(declaringClass)) {
                isCreatedFromValidFactory = false;
            }
        } catch (ClassNotFoundException ignored) {

        }
    }

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> the {@link Application} instance associated with this web
     * application.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, returns the correct current {@link jakarta.faces.application.Application} instance.
     * </p>
     *
     * @return the <code>Application</code> instance associated with this web application.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract Application getApplication();

    /**
     * <p class="changed_added_2_0">
     * Return a mutable <code>Map</code> representing the attributes associated wth this <code>FacesContext</code> instance.
     * This <code>Map</code> is useful to store attributes that you want to go out of scope when the Faces lifecycle for the
     * current request ends, which is not always the same as the request ending, especially in the case of Jakarta Servlet
     * filters that are invoked <strong>after</strong> the Faces lifecycle for this request completes. Accessing this
     * <code>Map</code> does not cause any events to fire, as is the case with the other maps: for request, session, and
     * application scope. When {@link #release()} is invoked, the attributes must be cleared.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * The <code>Map</code> returned by this method is not associated with the request. If you would like to get or set
     * request attributes, see {@link ExternalContext#getRequestMap}.
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * </div>
     *
     * @return mutable <code>Map</code> representing the attributes associated wth this <code>FacesContext</code> instance.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     */
    public Map<Object, Object> getAttributes() {
        if (defaultFacesContext != null) {
            return defaultFacesContext.getAttributes();
        }

        if (!isCreatedFromValidFactory) {
            if (attributesForInvalidFactoryConstruction == null) {
                attributesForInvalidFactoryConstruction = new HashMap<>();
            }
            return attributesForInvalidFactoryConstruction;
        }

        throw new UnsupportedOperationException();
    }

    private Map<Object, Object> attributesForInvalidFactoryConstruction;

    /**
     * <p class="changed_added_2_0">
     * Return the {@link PartialViewContext} for this request. The {@link PartialViewContext} is used to control the
     * processing of specified components during the execute portion of the request processing lifecycle (known as partial
     * processing) and the rendering of specified components (known as partial rendering). This method must return a new
     * {@link PartialViewContext} if one does not already exist.
     * </p>
     *
     * @return the instance of <code>PartialViewContext</code> for this request.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     */
    public PartialViewContext getPartialViewContext() {
        if (defaultFacesContext != null) {
            return defaultFacesContext.getPartialViewContext();
        }

        if (!isCreatedFromValidFactory) {
            if (partialViewContextForInvalidFactoryConstruction == null) {
                PartialViewContextFactory f = (PartialViewContextFactory) FactoryFinder.getFactory(FactoryFinder.PARTIAL_VIEW_CONTEXT_FACTORY);
                partialViewContextForInvalidFactoryConstruction = f.getPartialViewContext(FacesContext.getCurrentInstance());
            }
            return partialViewContextForInvalidFactoryConstruction;
        }

        throw new UnsupportedOperationException();
    }

    private PartialViewContext partialViewContextForInvalidFactoryConstruction;

    /**
     * <p>
     * Return an <code>Iterator</code> over the client identifiers for which at least one
     * {@link jakarta.faces.application.FacesMessage} has been queued. If there are no such client identifiers, an empty
     * <code>Iterator</code> is returned. If any messages have been queued that were not associated with any specific client
     * identifier, a <code>null</code> value will be included in the iterated values. The elements in the
     * <code>Iterator</code> must be returned in the order in which they were added with {@link #addMessage}.
     * </p>
     *
     * @return the <code>Iterator</code> over the client identifiers for which at least one
     * {@link jakarta.faces.application.FacesMessage} has been queued.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract Iterator<String> getClientIdsWithMessages();

    /**
     * <p>
     * Return the <code>ELContext</code> instance for this <code>FacesContext</code> instance. This <code>ELContext</code>
     * instance has the same lifetime and scope as the <code>FacesContext</code> instance with which it is associated, and
     * may be created lazily the first time this method is called for a given <code>FacesContext</code> instance. Upon
     * creation of the ELContext instance, the implementation must take the following action:
     * </p>
     *
     * <ul>
     *
     * <li>
     * <p>
     * Call the {@link ELContext#putContext} method on the instance, passing in <code>FacesContext.class</code> and the
     * <code>this</code> reference for the <code>FacesContext</code> instance itself.
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * If the <code>Collection</code> returned by {@link jakarta.faces.application.Application#getELContextListeners} is
     * non-empty, create an instance of {@link jakarta.el.ELContextEvent} and pass it to each
     * {@link jakarta.el.ELContextListener} instance in the <code>Collection</code> by calling the
     * {@link jakarta.el.ELContextListener#contextCreated} method.
     * </p>
     * </li>
     *
     * </ul>
     *
     * @return instance of <code>ELContext</code>.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 1.2
     */
    public ELContext getELContext() {
        if (defaultFacesContext != null) {
            return defaultFacesContext.getELContext();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * Return the {@link ExceptionHandler} for this request.
     * </p>
     *
     * @return instance of <code>ExceptionHandler</code>.
     */
    public ExceptionHandler getExceptionHandler() {
        if (defaultFacesContext != null) {
            return defaultFacesContext.getExceptionHandler();
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p class="changed_added_2_0">
     * Set the {@link ExceptionHandler} for this request.
     * </p>
     *
     * @param exceptionHandler the <code>ExceptionHandler</code> for this request.
     */
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        if (defaultFacesContext != null) {
            defaultFacesContext.setExceptionHandler(exceptionHandler);
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p>
     * <span class="changed_added_4_0">Return</span> the {@link Lifecycle} instance for this
     * <code>FacesContext</code> instance.
     * </p>
     *
     * @return instance of <code>Lifecycle</code>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 4.0
     */
    public abstract Lifecycle getLifecycle();

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> the {@link ExternalContext} instance for this
     * <code>FacesContext</code> instance.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this method returns an {@link ExternalContext} instance with the special behaviors indicated in the javadoc
     * for that class. Methods document as being valid to call during application startup or shutdown must be supported.
     * </p>
     *
     * @return instance of <code>ExternalContext</code>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract ExternalContext getExternalContext();

    /**
     * <p>
     * Return the maximum severity level recorded on any {@link jakarta.faces.application.FacesMessage}s that has been
     * queued, whether or not they are associated with any specific {@link jakarta.faces.component.UIComponent}. If no such
     * messages have been queued, return <code>null</code>.
     * </p>
     *
     * @return the maximum severity level.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract Severity getMaximumSeverity();

    /**
     * <p>
     * Return an <code>Iterator</code> over the {@link jakarta.faces.application.FacesMessage}s that have been queued,
     * whether or not they are associated with any specific client identifier. If no such messages have been queued, return
     * an empty <code>Iterator</code>. The elements of the <code>Iterator</code> must be returned in the order in which they
     * were added with calls to {@link #addMessage}.
     * </p>
     *
     * @return <code>Iterator</code> over the <code>FacesMessage</code>s that have been queued.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract Iterator<FacesMessage> getMessages();

    /**
     * <p class="changed_added_2_0">
     * Like {@link #getMessages}, but returns a <code>List&lt;FacesMessage&gt;</code>, enabling use from Jakarta Expression
     * Language expressions.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return an immutable <code>List</code> which is effectively a snapshot of the messages present at the time of
     * invocation.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     */
    public List<FacesMessage> getMessageList() {

        if (defaultFacesContext != null) {
            return defaultFacesContext.getMessageList();
        }
        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * Like {@link #getMessages(java.lang.String)}, but returns a <code>List&lt;FacesMessage&gt;</code> of messages for the
     * component with client id matching argument <code>clientId</code>.
     * </p>
     *
     * <p>
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @param clientId the client id of a component.
     *
     * @return an immutable <code>List</code> which is effectively a snapshot of the messages present at the time of
     * invocation.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     */

    public List<FacesMessage> getMessageList(String clientId) {
        if (defaultFacesContext != null) {
            return defaultFacesContext.getMessageList(clientId);
        }
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Return an <code>Iterator</code> over the {@link jakarta.faces.application.FacesMessage}s that have been queued that
     * are associated with the specified client identifier (if <code>clientId</code> is not <code>null</code>), or over the
     * {@link jakarta.faces.application.FacesMessage}s that have been queued that are not associated with any specific
     * client identifier (if <code>clientId</code> is <code>null</code>). If no such messages have been queued, return an
     * empty <code>Iterator</code>. The elements of the <code>Iterator</code> must be returned in the order in which they
     * were added with calls to {@link #addMessage}.
     * </p>
     *
     * @param clientId The client identifier for which messages are requested, or <code>null</code> for messages not
     * associated with any client identifier
     *
     * @return <code>Iterator</code> over the <code>FacesMessage</code>s.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract Iterator<FacesMessage> getMessages(String clientId);

    /**
     * <p class="changed_added_2_2">
     * Return the result of calling {@link UINamingContainer#getSeparatorChar}, passing <code>this</code> as the argument.
     * Note that this enables accessing the value of this property from the Jakarta Expression Language expression
     * <code>#{facesContext.namingContainerSeparatorChar}</code>.
     * </p>
     *
     * @return the separator char.
     *
     */

    public char getNamingContainerSeparatorChar() {
        return UINamingContainer.getSeparatorChar(this);
    }

    /**
     * <p>
     * Return the {@link RenderKit} instance for the render kit identifier specified on our {@link UIViewRoot}, if there is
     * one. If there is no current {@link UIViewRoot}, if the {@link UIViewRoot} does not have a specified
     * <code>renderKitId</code>, or if there is no {@link RenderKit} for the specified identifier, return <code>null</code>
     * instead.
     * </p>
     *
     * @return instance of <code>RenderKit</code> associated with the <code>UIViewRoot</code>.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract RenderKit getRenderKit();

    /**
     * <p>
     * Return <code>true</code> if the <code>renderResponse()</code> method has been called for the current request.
     * </p>
     *
     * @return flag indicating whether the <code>renderResponse()</code> has been called.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract boolean getRenderResponse();

    /**
     * <p>
     * Return <code>true</code> if the <code>responseComplete()</code> method has been called for the current request.
     * </p>
     *
     * @return the boolean indicating whether <code>responseComplete()</code> method has been called.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract boolean getResponseComplete();

    /**
     * <p class="changed_added_2_2">
     * Return the list of resource library contracts that have been calculated to be appropriate for use with this view, or
     * an empty list if there are no such resource library contracts. The list returned by this method must be immutable.
     * For backward compatibility with implementations of the specification prior to when this method was introduced, an
     * implementation is provided that returns an empty list. Implementations compliant with the version in which this
     * method was introduced must implement this method as specified.
     * </p>
     *
     * @return the list of resource library contracts.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.2
     */
    public List<String> getResourceLibraryContracts() {
        return Collections.emptyList();
    }

    /**
     * <p class="changed_added_2_2">
     * Set the resource library contracts calculated as valid to use with this view. The implementation must copy the
     * contents of the incoming {@code List} into an immutable {@code List} for return from
     * {@link #getResourceLibraryContracts}. If the argument is {@code null} or empty, the action taken is the same as if
     * the argument is {@code null}: a subsequent call to {@code getResourceLibraryContracts} returns {@code null}. This
     * method may only be called during the processing of {@link jakarta.faces.view.ViewDeclarationLanguage#createView} and
     * during the VDL tag handler for the tag corresponding to an instance of {@code UIViewRoot}. For backward compatibility
     * with implementations of the specification prior to when this method was introduced, an implementation is provided
     * that takes no action. Implementations compliant with the version in which this method was introduced must implement
     * this method as specified.
     *
     * </p>
     *
     * @param contracts The new contracts to be returned, as an immutable {@code List}. from a subsequent call to
     * {@link #getResourceLibraryContracts}.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.2
     *
     */

    public void setResourceLibraryContracts(List<String> contracts) {
    }

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if the <code>validationFailed()</code> method has been called for the current request.
     * </p>
     *
     * @return boolean indicating if the <code>validationFailed()</code> method has been called for the current request
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public boolean isValidationFailed() {
        if (defaultFacesContext != null) {
            return defaultFacesContext.isValidationFailed();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p>
     * Return the {@link ResponseStream} to which components should direct their binary output. Within a given response,
     * components can use either the ResponseStream or the ResponseWriter, but not both.
     *
     * @return <code>ResponseStream</code> instance.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract ResponseStream getResponseStream();

    /**
     * <p>
     * Set the {@link ResponseStream} to which components should direct their binary output.
     *
     * @param responseStream The new ResponseStream for this response
     *
     * @throws NullPointerException if <code>responseStream</code> is <code>null</code>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract void setResponseStream(ResponseStream responseStream);

    /**
     * <p>
     * Return the {@link ResponseWriter} to which components should direct their character-based output. Within a given
     * response, components can use either the ResponseStream or the ResponseWriter, but not both.
     * </p>
     *
     * @return <code>ResponseWriter</code> instance.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract ResponseWriter getResponseWriter();

    /**
     * <p>
     * Set the {@link ResponseWriter} to which components should direct their character-based output.
     *
     * @param responseWriter The new ResponseWriter for this response
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     * @throws NullPointerException if <code>responseWriter</code> is <code>null</code>
     */
    public abstract void setResponseWriter(ResponseWriter responseWriter);

    /**
     * <p>
     * <span class="changed_modified_2_0">Return</span> the root component that is associated with the this request.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It is valid to call this method during application startup or shutdown. If called during application startup or
     * shutdown, this method returns a new <code>UIViewRoot</code> with its locale set to <code>Locale.getDefault()</code>.
     * </p>
     *
     * @return <code>UIViewRoot</code> instance.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract UIViewRoot getViewRoot();

    /**
     * <p>
     * <span class="changed_modified_2_0 changed_modified_2_1">Set</span> the root component that is associated with this
     * request.
     *
     * <p class="changed_modified_2_1">
     * This method can be called by the application handler (or a class that the handler calls), during the <em>Invoke
     * Application</em> phase of the request processing lifecycle and during the <em>Restore View</em> phase of the request
     * processing lifecycle (especially when a new root component is created). In the present version of the specification,
     * implementations are not required to enforce this restriction, though a future version of the specification may
     * require enforcement.
     * </p>
     *
     * <p class="changed_added_2_0">
     * If the current <code>UIViewRoot</code> is non-<code>null</code>, and calling <code>equals()</code> on the argument
     * <code>root</code>, passing the current <code>UIViewRoot</code> returns <code>false</code>, the <code>clear</code>
     * method must be called on the <code>Map</code> returned from {@link UIViewRoot#getViewMap}.
     * </p>
     *
     * @param root The new component {@link UIViewRoot} component
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     * @throws NullPointerException if <code>root</code> is <code>null</code>
     */
    public abstract void setViewRoot(UIViewRoot root);

    // ---------------------------------------------------------- Public Methods

    /**
     * <p>
     * Append a {@link jakarta.faces.application.FacesMessage} to the set of messages associated with the specified client
     * identifier, if <code>clientId</code> is not <code>null</code>. If <code>clientId</code> is <code>null</code>, this
     * {@link jakarta.faces.application.FacesMessage} is assumed to not be associated with any specific component instance.
     * </p>
     *
     * @param clientId The client identifier with which this message is associated (if any)
     * @param message The message to be appended
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     * @throws NullPointerException if <code>message</code> is <code>null</code>
     */
    public abstract void addMessage(String clientId, FacesMessage message);

    /**
     * <p class="changed_added_2_1">
     * Return a flag indicating if the resources associated with this <code>FacesContext</code> instance have been released.
     * </p>
     *
     * @return <code>true</code> if the resources have been released.
     *
     * @since 2.1
     */
    public boolean isReleased() {
        if (defaultFacesContext != null) {
            return defaultFacesContext.isReleased();
        }

        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * <span class="changed_modified_2_0">Release</span> any resources associated with this <code>FacesContext</code>
     * instance. Faces implementations may choose to pool instances in the associated {@link FacesContextFactory} to avoid
     * repeated object creation and garbage collection. After <code>release()</code> is called on a
     * <code>FacesContext</code> instance (until the <code>FacesContext</code> instance has been recycled by the
     * implementation for re-use), calling any other methods will cause an <code>IllegalStateException</code> to be thrown.
     * </p>
     *
     * <p class="changed_added_2_0">
     * If a call was made to {@link #getAttributes} during the processing for this request, the implementation must call
     * <code>clear()</code> on the <code>Map</code> returned from <code>getAttributes()</code>, and then de-allocate the
     * data-structure behind that <code>Map</code>.
     * </p>
     *
     * <p>
     * The implementation must call {@link #setCurrentInstance} passing <code>null</code> to remove the association between
     * this thread and this dead <code>FacesContext</code> instance.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract void release();

    /**
     * <p>
     * Signal the Jakarta Faces implementation that, as soon as the current phase of the request processing lifecycle
     * has been completed, control should be passed to the <em>Render Response</em> phase, bypassing any phases that have
     * not been executed yet.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract void renderResponse();

    /**
     * <p class="changed_added_2_0">
     * This utility method simply returns the result of
     * {@link jakarta.faces.render.ResponseStateManager#isPostback(FacesContext)}.
     * </p>
     *
     * <p class="changed_added_2_0">
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing applications that extend this class.
     * </p>
     *
     * @return the boolean indicating whether this request is a post one.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     */
    public boolean isPostback() {

        if (defaultFacesContext != null) {
            return defaultFacesContext.isPostback();
        }

        throw new UnsupportedOperationException();

    }

    /**
     * <p>
     * Signal the Jakarta Faces implementation that the HTTP response for this request has already been generated
     * (such as an HTTP redirect), and that the request processing lifecycle should be terminated as soon as the current
     * phase is completed.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public abstract void responseComplete();

    /**
     * <p class="changed_added_2_0">
     * Sets a flag which indicates that a conversion or validation error occurred while processing the inputs. Inputs
     * consist of either page parameters or form bindings. This flag can be read using {@link #isValidationFailed}.
     * </p>
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     */
    public void validationFailed() {

        if (defaultFacesContext != null) {
            defaultFacesContext.validationFailed();
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * <p class="changed_added_2_0">
     * Return the value last set on this <code>FacesContext</code> instance when {@link #setCurrentPhaseId} was called.
     * </p>
     *
     * @return the current phase id.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     */
    public PhaseId getCurrentPhaseId() {

        if (defaultFacesContext != null) {
            return defaultFacesContext.getCurrentPhaseId();
        }
        if (!isCreatedFromValidFactory) {
            return currentPhaseIdForInvalidFactoryConstruction;
        }
        throw new UnsupportedOperationException();

    }

    /**
     * <p class="changed_added_2_0">
     * The implementation must call this method at the earliest possble point in time after entering into a new phase in the
     * request processing lifecycle.
     * </p>
     *
     * @param currentPhaseId The {@link jakarta.faces.event.PhaseId} for the current phase.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     *
     * @since 2.0
     */
    public void setCurrentPhaseId(PhaseId currentPhaseId) {

        if (defaultFacesContext != null) {
            defaultFacesContext.setCurrentPhaseId(currentPhaseId);
        } else if (!isCreatedFromValidFactory) {
            currentPhaseIdForInvalidFactoryConstruction = currentPhaseId;
        } else {
            throw new UnsupportedOperationException();
        }

    }

    private PhaseId currentPhaseIdForInvalidFactoryConstruction;

    /**
     * <p class="changed_added_2_0">
     * Allows control of wheter or not the runtime will publish events when
     * {@link Application#publishEvent(FacesContext, Class, Object)} or
     * {@link Application#publishEvent(FacesContext, Class, Class, Object)} is called.
     * </p>
     *
     * @param processingEvents flag indicating events should be processed or not
     */
    public void setProcessingEvents(boolean processingEvents) {
        this.processingEvents = processingEvents;
    }

    /**
     * <p class="changed_added_2_0">
     * Returns a flag indicating whether or not the runtime should publish events when asked to do so.
     * </p>
     *
     * @return <code>true</code> if events should be published, otherwise <code>false</code>
     */
    public boolean isProcessingEvents() {
        return processingEvents;
    }

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if the current {@link ProjectStage} as returned by the {@link Application} instance is equal
     * to <code>stage</code>, otherwise return <code>false</code>
     * </p>
     *
     * @param stage the {@link ProjectStage} to check
     *
     * @return boolean indicating whether the application has the same stage.
     *
     * @throws IllegalStateException if this method is called after this instance has been released
     * @throws NullPointerException if <code>stage</code> is <code>null</code>
     */
    public boolean isProjectStage(ProjectStage stage) {

        if (stage == null) {
            throw new NullPointerException();
        }
        return stage.equals(getApplication().getProjectStage());

    }

    // ---------------------------------------------------------- Static Methods

    /**
     * <p>
     * The <code>ThreadLocal</code> variable used to record the {@link FacesContext} instance for each processing thread.
     * </p>
     */
    private static final ThreadLocal<FacesContext> instance = ThreadLocal.withInitial(() -> null);

    /**
     * <p class="changed_modified_2_0">
     * Return the {@link FacesContext} instance for the request that is being processed by the current thread. If called
     * during application initialization or shutdown, any method documented as "valid to call this method during application
     * startup or shutdown" must be supported during application startup or shutdown time. The result of calling a method
     * during application startup or shutdown time that does not have this designation is undefined.
     * </p>
     *
     * @return the instance of <code>FacesContext</code>.
     */
    public static FacesContext getCurrentInstance() {
        FacesContext facesContext = instance.get();

        if (null == facesContext) {
            facesContext = threadInitContext.get(Thread.currentThread());
        }
        // Bug 20458755: If not found in the threadInitContext, use
        // a special FacesContextFactory implementation that knows how to
        // use the initContextServletContext map to obtain current ServletContext
        // out of thin air (actually, using the current ClassLoader), and use it
        // to obtain the init FacesContext corresponding to that ServletContext.
        if (null == facesContext) {
            // In the non-init case, this will immediately return null.
            // In the init case, this will return null if Jakarta Faces hasn't been
            // initialized in the ServletContext corresponding to this
            // Thread's context ClassLoader.
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                return null;
            }

            FacesContextFactory privateFacesContextFactory = (FacesContextFactory) FactoryFinder.getFactory("com.sun.faces.ServletContextFacesContextFactory");
            if (null != privateFacesContextFactory) {
                facesContext = privateFacesContextFactory.getFacesContext(null, null, null, null);
            }
        }
        return facesContext;
    }

    /**
     * <p>
     * Set the {@link FacesContext} instance for the request that is being processed by the current thread.
     * </p>
     *
     * @param context The {@link FacesContext} instance for the current thread, or <code>null</code> if this thread no
     * longer has a <code>FacesContext</code> instance.
     *
     */
    protected static void setCurrentInstance(FacesContext context) {

        if (context == null) {
            instance.remove();
        } else {
            instance.set(context);
        }

    }

}
