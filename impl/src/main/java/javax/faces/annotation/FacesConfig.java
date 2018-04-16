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

package javax.faces.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * <p class="changed_added_2_3">The presence of this annotation on a managed
 * bean deployed within an application causes version specific features
 * to be enabled as specified in the enum {@link Version}</p>
 */
@Qualifier
@Target(TYPE)
@Retention(RUNTIME)
public @interface FacesConfig {

    public static enum Version {

        /**
         * <p class="changed_added_2_3">This value indicates CDI should be used
         * for EL resolution as well as enabling JSF CDI injection, as specified
         * in Section 5.6.3 "CDI for EL Resolution" and Section 5.9 "CDI Integration".</p>
         */
        JSF_2_3

    }

    /**
     * <p class="changed_added_2_3">The value of this attribute indicates that
     * features corresponding to this version must be enabled for this application.</p>
     * @return the spec version for which the features must be enabled.
     */
    @Nonbinding Version version() default Version.JSF_2_3;

}
