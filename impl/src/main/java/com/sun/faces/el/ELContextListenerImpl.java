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

package com.sun.faces.el;

import jakarta.el.ELContext;
import jakarta.el.ELContextEvent;
import jakarta.el.ELContextListener;
import jakarta.el.ExpressionFactory;
import jakarta.faces.context.FacesContext;

public class ELContextListenerImpl implements ELContextListener {

    public ELContextListenerImpl() {
    }

    /**
     * Invoked when a new <code>ELContext</code> has been created.
     *
     * @param ece the notification event.
     */
    @Override
    public void contextCreated(ELContextEvent ece) {

        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }
        ELContext source = (ELContext) ece.getSource();
        // Register FacesContext with Jakarta Server Pages
        source.putContext(FacesContext.class, context);
        ExpressionFactory exFactory = ELUtils.getDefaultExpressionFactory(context);
        if (null != exFactory) {
            source.putContext(ExpressionFactory.class, exFactory);
        }

        // dispatch the event to any Faces applications interested in
        // the event.
        ELContextListener[] listeners = context.getApplication().getELContextListeners();
        if (listeners == null) {
            return;
        }
        for (ELContextListener elcl : listeners) {
            elcl.contextCreated(new ELContextEvent(source));
        }
    }

}
