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

package jakarta.faces.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p class="changed_added_2_0">
 * Classes tagged with this annotation are installed as listeners using the method
 * {@link jakarta.faces.application.Application#subscribeToEvent} or
 * {@link jakarta.faces.component.UIComponent#subscribeToEvent} (depending on the circumstances, described below).
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * The default implementation must support attaching this annotation to {@link jakarta.faces.component.UIComponent} or
 * {@link jakarta.faces.render.Renderer} classes. In both cases, the annotation processing described herein must
 * commence during the implementation of any variant of
 * {@link jakarta.faces.application.Application}<code>.createComponent()</code> and must complete before the
 * <code>UIComponent</code> instance is returned from <code>createComponent()</code>. The annotation processing must
 * proceed according to an algorithm semantically equivalent to the following.
 * </p>
 *
 * <ul>
 *
 * <li>
 * <p>
 * If this annotation is not present on the class in question, no action must be taken.
 * </p>
 *
 * <p>
 * Determine the "target" on which to call <code>subscribeToEvent</code>.
 * </p>
 *
 * <p>
 * If the class to which this annotation is attached implements {@link ComponentSystemEventListener} and is a
 * <code>UIComponent</code> instance, "target" is the <code>UIComponent</code> instance.
 * </p>
 *
 * <p>
 * If the class to which this annotation is attached implements {@link ComponentSystemEventListener} and is a
 * <code>Renderer</code> instance, "target" is the <code>UIComponent</code> instance that is to be rendered by this
 * <code>Renderer</code> instance.
 * </p>
 *
 * <p>
 * If the class to which this annotation is attached implements {@link ComponentSystemEventListener} and is neither an
 * instance of <code>Renderer</code> nor <code>UIComponent</code>, the action taken is unspecified. This case must not
 * trigger any kind of error.
 * </p>
 *
 * <p>
 * If the class to which this annotation is attached implements <code>SystemEventListener</code> and does not implement
 * <code>ComponentSystemEventListener</code>, "target" is the {@link jakarta.faces.application.Application} instance.
 * </p>
 *
 * </li>
 *
 * <li>
 *
 * <p>
 * Determine the variant of <code>subscribeToEvent()</code>to call and the parameters to pass to it.
 * </p>
 *
 * <p>
 * If "target" is a <code>UIComponent</code> call
 * {@link jakarta.faces.component.UIComponent#subscribeToEvent(Class, ComponentSystemEventListener)}, passing the
 * {@link #systemEventClass} of the annotation as the first argument and the instance of the class to which this
 * annotation is attached (which must implement <code>ComponentSystemEventListener</code>) as the second argument.
 * </p>
 *
 * <p>
 * If "target" is the {@link jakarta.faces.application.Application} instance, inspect the value of the
 * {@link #sourceClass} annotation attribute value.
 * </p>
 *
 * <p>
 * If the value is <code>Void.class</code>, call
 * {@link jakarta.faces.application.Application#subscribeToEvent(Class, SystemEventListener)}, passing the value of
 * {@link #systemEventClass} as the first argument and the instance of the class to which this annotation is attached
 * (which must implement <code>SystemEventListener) as the second
 argument.</code>
 * </p>
 *
 * <p>
 * Otherwise, call {@link jakarta.faces.application.Application#subscribeToEvent(Class, Class, SystemEventListener)},
 * passing the value of {@link #systemEventClass} as the first argument, the value of {@link #sourceClass} as the second
 * argument, and the instance of the class to which this annotation is attached (which must implement
 * <code>SystemEventListener) as the third
 argument.</code>
 * </p>
 *
 * </li>
 *
 * </ul>
 *
 * <p>
 * Example: The standard renderer for <code>jakarta.faces.resource.Stylesheet</code> must have the following annotation
 * declaration:
 * </p>
 *
 * <pre>
 * <code>@ListenerFor(systemEventClass=PostAddToViewEvent.class)</code>
 * </pre>
 *
 * <p>
 * This will cause the renderer to be added as a listener for the {@link PostAddToViewEvent} to all components that list
 * it as their renderer.
 * </p>
 *
 * </div>
 *
 * @since 2.0
 */

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Inherited
@Repeatable(ListenersFor.class)
public @interface ListenerFor {

    /**
     * <p class="changed_added_2_0">
     * The kind of system event for which this class will be installed as a listener. The implementation only supports exact
     * matches on the <code>Class</code> and must not honor subclass relationships. It is valid to have EL Expressions in
     * the value of this attribute, as long as the expression resolves to an instance of the expected type.
     * </p>
     *
     * @return the event class
     */

    public Class<? extends SystemEvent> systemEventClass();

    /**
     * <p class="changed_added_2_0">
     * The kind of object that emits events of the type given by the value of the {@link #systemEventClass} attribute. It is
     * valid to have Jakarta Expression Language Expressions in the value of this attribute, as long as the expression
     * resolves to an instance of the expected type.
     * </p>
     *
     * @return the source class
     */
    public Class sourceClass() default Void.class;

}
