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

package jakarta.faces.application;

/**
 *
 * <p class="changed_added_2_0">
 * This class enables a feature similar to the <code>RAILS_ENV</code> feature of the Ruby on Rails web framework. The
 * constants in this class represent the current state of the running application in a typical product development
 * lifecycle. The value of this state may be queried at any time after application startup by calling
 * {@link Application#getProjectStage}.
 * </p>
 *
 * @since 2.0
 */
public enum ProjectStage {

    /**
     * <p class="changed_added_2_0">
     * This value indicates the currently running application is right now, at this moment, being developed. This value will
     * usually be set during iterative development.
     * </p>
     */
    Development,

    /**
     * <p class="changed_added_2_0">
     * This value indicates the currently running application is undergoing unit testing.
     * </p>
     */
    UnitTest,
    /**
     * <p class="changed_added_2_0">
     * This value indicates the currently running application is undergoing system testing.
     * </p>
     */
    SystemTest,

    /**
     * <p class="changed_added_2_0">
     * This value indicates the currently running application is deployed in production.
     * </p>
     */
    Production;

    /**
     * <p class="changed_added_2_0">
     * The value of this constant is the value of the <code>param-name</code> for setting the current value to be returned
     * by {@link Application#getProjectStage}.
     * </p>
     */
    public static final String PROJECT_STAGE_PARAM_NAME = "jakarta.faces.PROJECT_STAGE";

    /**
     * <p class="changed_added_2_0">
     * The value of this constant is the name used for JNDI lookups for setting the current value to be returned by
     * {@link Application#getProjectStage}.
     * </p>
     */
    public static final String PROJECT_STAGE_JNDI_NAME = "java:comp/env/faces/ProjectStage";

}
