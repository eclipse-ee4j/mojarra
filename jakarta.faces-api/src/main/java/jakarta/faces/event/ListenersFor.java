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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p class="changed_added_2_0">
 * Container annotation to specify multiple {@link ListenerFor} annotations on a single class. Example:
 * </p>
 *
 * <pre>
 * <code>

    &#0064;ListenersFor({
        &#0064;ListenerFor(systemEventClass=PostAddToViewEvent.class),
        &#0064;ListenerFor(systemEventClass=BeforeRenderEvent.class,
                     sourceClass=CustomOutput.class)
    })

 * </code>
 * </pre>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * The action described in {@link ListenerFor} must be taken for each <code>&#0064;ListenerFor</code> present in the
 * container annotation.
 * </p>
 *
 * </div>
 *
 * @since 2.0
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Inherited
public @interface ListenersFor {

    ListenerFor[] value();

}
