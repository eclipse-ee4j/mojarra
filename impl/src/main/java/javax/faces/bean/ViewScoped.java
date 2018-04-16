/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.bean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;

/**
 * <p class="changed_added_2_0"><span
 * class="changed_modified_2_2">When</span> this annotation, along with
 * {@code ManagedBean} is found on a class, the runtime must act as if a
 * <code>&lt;managed-bean-scope&gt;view&lt;managed-bean-scope&gt;</code> element
 * was declared for the corresponding managed bean.</p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * If <code>ProjectStage</code> is not <code>ProjectStage.Production</code>,
 * verify that the current {@code
 * UIViewRoot} does not have its {@code transient} property set to {@code true}.
 * If so, add a <code>FacesMessage</code> for the current {@code viewId} to the
 * <code>FacesContext</code> stating {@code @ViewScoped} beans cannot work if
 * the view is marked as transient. Also log a <code>Level.WARNING</code>
 * message to the log. If <code>ProjectStage</code> <strong>is</strong>
 * <code>ProjectStage.Production</code>, do not do this verification.</p>
 *
 * <p>
 * The bean must be stored in the map returned from
 * {@code javax.faces.component.UIViewRoot.getViewMap(boolean)}.</p>
 *
 * <p>
 * The runtime must ensure that any methods on the bean annotated with
 * {@code PostConstruct} or {@code PreDestroy} are called when the scope begins
 * and ends, respectively. Two circumstances can cause the scope to end.</p>
 *
 * <ul>
 *
 * <li><p>
 * {@code FacesContext.setViewRoot()} is called with the new {@code UIViewRoot}
 * being different than the current one.</p></li>
 *
 * <li><p>
 * The session, that happened to be active when the bean was created, expires.
 * If no session existed when the bean was created, then this circumstance does
 * not apply.</p></li>
 *
 * </ul>
 *
 * <p>
 * In the session expiration case, the runtime must ensure that
 * {@code FacesContext.getCurrentInstance()} returns a valid instance if it is
 * called during the processing of the {@code @PreDestroy} annotated method. The
 * set of methods on {@code FacesContext} that are valid to call in this
 * circumstance is identical to those documented as "valid to call this method
 * during application startup or shutdown". On the {@code ExternalContext}
 * returned from that {@code FacesContext}, all of the methods documented as
 * "valid to call this method during application startup or shutdown" are valid
 * to call. In addition, the method {@code
 * ExternalContext.getSessionMap()} is also valid to call.</p>
 *
 * </div>
 *
 *
 * @since 2.0
 * @deprecated This has been replaced by {@code javax.faces.view.ViewScoped}. 
 * The functionality of this corresponding annotation is identical to this one, 
 * but it is implemented as a CDI custom scope.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Deprecated
public @interface ViewScoped {
}
