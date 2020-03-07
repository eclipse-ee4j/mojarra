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

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * This event must be published by the runtime <em>before</em> the factories associated with this {@link Application}
 * are released.
 * </p>
 *
 * <p class="changed_added_2_0">
 * This event is useful for listeners that need to perform custom shutdown processing without having to rely on
 * <code>ServletContextListener</code>s which will be invoked after all of the application artifacts have been removed.
 * </p>
 *
 * @since 2.0
 */
public class PreDestroyApplicationEvent extends SystemEvent {

    private static final long serialVersionUID = 8105212785161493162L;

    /**
     * <p class="changed_added_2_0">
     * Constructs a new <code>PreDestroyApplicationEvent</code> for this application.
     * </p>
     *
     * @param application the application that has been configured
     *
     * @since 2.0
     */
    public PreDestroyApplicationEvent(Application application) {
        super(application);
    }

    /**
     * <p class="changed_added_2_0">
     * Constructs a new <code>PreDestroyApplicationEvent</code> for this application.
     * </p>
     *
     * @param facesContext the Faces context.
     * @param application the application that has been configured
     * @since 2.0
     */
    public PreDestroyApplicationEvent(FacesContext facesContext, Application application) {
        super(facesContext, application);
    }

    /**
     * <p class="changed_added_2_0">
     * The source {@link Application} that sent this event.
     * </p>
     *
     * @return the application.
     * @since 2.0
     */

    public Application getApplication() {
        return (Application) getSource();
    }

}
