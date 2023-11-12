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

package jakarta.faces.view;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.context.NormalScope;

/**
 * <p class="changed_added_2_2">
 * When this annotation, along with {@code
 * jakarta.inject.Named} is found on a class, the runtime must place the bean in a CDI scope such that it remains active
 * as long as {@link jakarta.faces.application.NavigationHandler#handleNavigation} does not cause a navigation to a view
 * with a viewId that is different than the viewId of the current view. Any injections and notifications required by CDI
 * and the Jakarta EE platform must occur as usual at the expected time.
 * </p>
 *
 * <div class="changed_added_2_2">
 *
 * <p>
 * If <code>ProjectStage</code> is not <code>ProjectStage.Production</code>, verify that the current
 * {@link jakarta.faces.component.UIViewRoot} does not have its {@code transient} property set to {@code true}. If so,
 * add a <code>FacesMessage</code> for the current {@code viewId} to the <code>FacesContext</code> stating
 * {@code @ViewScoped} beans cannot work if the view is marked as transient. Also log a <code>Level.WARNING</code>
 * message to the log. If <code>ProjectStage</code> <strong>is</strong> <code>ProjectStage.Production</code>, do not do
 * this verification.
 * </p>
 *
 * <p>
 * The bean must be stored in the map returned from {@link jakarta.faces.component.UIViewRoot#getViewMap(boolean)}.
 * </p>
 *
 * <p>
 * Use of this annotation requires that any beans stored in view scope must be serializable and proxyable as defined in
 * the CDI specification.
 * </p>
 *
 * <p>
 * The runtime must ensure that any methods on the bean annotated with {@code PostConstruct} or {@code PreDestroy} are
 * called when the scope begins and ends, respectively. Two circumstances can cause the scope to end.
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * {@link jakarta.faces.context.FacesContext#setViewRoot} is called with the new {@code UIViewRoot} being different than
 * the current one.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * The session, that happened to be active when the bean was created, expires. If no session existed when the bean was
 * created, then this circumstance does not apply.
 * </p>
 * </li>
 *
 * </ul>
 *
 * <p>
 * In the session expiration case, the runtime must ensure that
 * {@link jakarta.faces.context.FacesContext#getCurrentInstance} returns a valid instance if it is called during the
 * processing of the {@code @PreDestroy} annotated method. The set of methods on {@code
 * FacesContext} that are valid to call in this circumstance is identical to those documented as "valid to call this
 * method during application startup or shutdown". On the {@link jakarta.faces.context.ExternalContext} returned from
 * that {@code
 * FacesContext}, all of the methods documented as "valid to call this method during application startup or shutdown"
 * are valid to call. In addition, the method {@link jakarta.faces.context.ExternalContext#getSessionMap} is also valid
 * to call.
 * </p>
 *
 *
 *
 * </div>
 * 
 * <p class="changed_added_4_1">
 * Events with qualifiers  {@code @Initialized}, {@code @BeforeDestroyed}, and {@code @Destroyed} as defined by the CDI specification  must fire for this built-in scope. 
 * </p>
 *
 * @since 2.2
 */
@NormalScope(passivating = true)
@Inherited
@Documented
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ViewScoped {
}
