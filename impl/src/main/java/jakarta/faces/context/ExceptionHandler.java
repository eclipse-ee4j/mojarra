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

import jakarta.faces.FacesException;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;

/**
 * <p class="changed_added_2_0">
 * <strong class="changed_modified_2_3">ExceptionHandler</strong> is the central point for handling <em>unexpected</em>
 * <code>Exception</code>s that are thrown during the Faces lifecycle. The <code>ExceptionHandler</code> must not be
 * notified of any <code>Exception</code>s that occur during application startup or shutdown.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * See the Jakarta Faces Specification Document for the requirements for the default implementation. <code>Exception</code>s may
 * be passed to the <code>ExceptionHandler</code> in one of two ways:
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * by ensuring that <code>Exception</code>s are not caught, or are caught and re-thrown.
 * </p>
 *
 * <p>
 * This approach allows the <code>ExceptionHandler</code> facility specified in 
 * section 6.2 "ExceptionHandler" of the Jakarta Faces Specification Document to 
 * operate on the <code>Exception</code>.
 * </p>
 *
 * </li>
 *
 * <li>
 * <p>
 * By using the system event facility to publish an {@link ExceptionQueuedEvent} that wraps the <code>Exception</code>.
 * </p>
 *
 * <p>
 * This approach requires manually publishing the {@link ExceptionQueuedEvent}, but allows more information about the
 * <code>Exception</code>to be stored in the event. The following code is an example of how to do this.
 * </p>
 *
 * <pre>
 * <code>
 *
 * //...
 * } catch (Exception e) {
 *   FacesContext ctx = FacesContext.getCurrentInstance();
 *   ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext(ctx, e);
 *   eventContext.getAttributes().put("key", "value");
 *   ctx.getApplication().publishEvent(ExceptionQueuedEvent.class, eventContext);
 * }
 *
 * </code>
 * </pre>
 *
 * <p>
 * Because the <code>Exception</code> must not be re-thrown when using this approach, lifecycle processing may continue
 * as normal, allowing more <code>Exception</code>s to be published if necessary.
 * </p>
 *
 * </li>
 * </ul>
 *
 * <p>
 * With either approach, any <code>ExceptionQueuedEvent</code> instances that are published in this way are accessible
 * to the {@link #handle} method, which is called at the end of each lifecycle phase, as specified in 
 * section 6.2 "ExceptionHandler" of the Jakarta Faces Specification Document.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Note that if {@link #handle} happens to be invoked during {@link jakarta.faces.event.PhaseId#RENDER_RESPONSE}, the
 * recovery options are more limited than when it is invoked during other phases. Specifically, it is not valid to call
 * {@link jakarta.faces.application.NavigationHandler#handleNavigation} during {@code RENDER_RESPONSE}.
 * </p>
 *
 * <p>
 * Instances of this class are request scoped and are created by virtue of {@link FacesContextFactory#getFacesContext}
 * calling {@link ExceptionHandlerFactory#getExceptionHandler}.
 * </p>
 *
 * </div>
 *
 * @since 2.0
 */
public abstract class ExceptionHandler implements SystemEventListener {

    /**
     * <p class="changed_added_2_0">
     * Take action to handle the <code>Exception</code> instances residing inside the {@link ExceptionQueuedEvent} instances
     * that have been queued by calls to <code>Application().publishEvent(ExceptionQueuedEvent.class,
     * <em>eventContext</em>)</code>. The requirements of the default implementation are detailed in 
     * section 6.2.1 "Default ExceptionHandler implementation" of the Jakarta Faces Specification Document.
     * </p>
     *
     * @throws FacesException if and only if a problem occurs while performing the algorithm to handle the
     * <code>Exception</code>, not as a means of conveying a handled <code>Exception</code> itself.
     *
     * @since 2.0
     */
    public abstract void handle() throws FacesException;

    /**
     * <p class="changed_added_2_0">
     * Return the first <code>ExceptionQueuedEvent</code> handled by this handler.
     * </p>
     *
     * @return instance of <code>ExceptionQueuedEvent</code>.
     *
     */
    public abstract ExceptionQueuedEvent getHandledExceptionQueuedEvent();

    /**
     * <p class="changed_added_2_0">
     * Return an <code>Iterable</code> over all <code>ExceptionQueuedEvent</code>s that have not yet been handled by the
     * {@link #handle} method.
     * </p>
     *
     * @return the unhandled set of <code>ExceptionQueuedEvent</code>s.
     *
     */
    public abstract Iterable<ExceptionQueuedEvent> getUnhandledExceptionQueuedEvents();

    /**
     * <p class="changed_added_2_0">
     * The default implementation must return an <code>Iterable</code> over all <code>ExceptionQueuedEvent</code>s that have
     * been handled by the {@link #handle} method.
     * </p>
     *
     * @return an <code>Iterable</code> over all <code>ExceptionQueuedEvent</code>s.
     *
     */
    public abstract Iterable<ExceptionQueuedEvent> getHandledExceptionQueuedEvents();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void processEvent(SystemEvent exceptionQueuedEvent) throws AbortProcessingException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean isListenerForSource(Object source);

    /**
     * <p class="changed_added_2_0">
     * Unwrap the argument <code>t</code> until the unwrapping encounters an Object whose <code>getClass()</code> is not
     * equal to <code>FacesException.class</code> or <code>jakarta.el.ELException.class</code>. If there is no root cause,
     * <code>null</code> is returned.
     * </p>
     *
     * @param t passed-in wrapped <code>Throwable</code>.
     *
     * @return unwrapped object.
     *
     * @throws NullPointerException if argument <code>t</code> is <code>null</code>.
     *
     * @since 2.0
     */
    public abstract Throwable getRootCause(Throwable t);

}
