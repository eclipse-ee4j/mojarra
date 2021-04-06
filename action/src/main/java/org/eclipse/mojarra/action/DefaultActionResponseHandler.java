/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.action;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;

/**
 * The default ActionResponseHandler.
 */
@ApplicationScoped
public class DefaultActionResponseHandler implements ActionResponseHandler {

    /**
     * Stores the default lifecycle.
     */
    private Lifecycle defaultLifecycle;

    /**
     * Get the default lifecycle.
     *
     * <p>
     * FIXME - This method lazily gets the default Lifecycle as FactoryFinder is
     * not properly re-entrant. We should be able to initialize the
     * defaultLifecycle variable in the constructor of this class.
     * </p>
     *
     * @return the default lifecycle.
     */
    private synchronized Lifecycle getDefaultLifecycle() {
        if (defaultLifecycle == null) {
            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            defaultLifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
        }
        return defaultLifecycle;
    }

    /**
     * Response to the browser.
     *
     * @param facesContext the Faces context.
     * @throws FacesException when a serious error occurs.
     */
    @Override
    public void respond(FacesContext facesContext) throws FacesException {
        getDefaultLifecycle().render(facesContext);
    }
}
