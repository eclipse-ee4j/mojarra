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

package com.sun.faces.application.applicationimpl;

import static com.sun.faces.cdi.CdiUtils.getBeanReference;
import static com.sun.faces.util.Util.getCdiBeanManager;

import com.sun.faces.cdi.CdiExtension;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.FacesContext;

public class Version {

    private Boolean isJsf23;

    /**
     * Are we running in JSF 2.3+
     *
     * @return true if we are, false otherwise.
     */
    public boolean isJsf23() {
        if (isJsf23 == null) {
            BeanManager beanManager = getCdiBeanManager(FacesContext.getCurrentInstance());

            if (beanManager == null) {
                isJsf23 = false;
            } else {
                isJsf23 = getBeanReference(beanManager, CdiExtension.class).isAddBeansForJSFImplicitObjects();
            }
        }

        return isJsf23;
    }

}
