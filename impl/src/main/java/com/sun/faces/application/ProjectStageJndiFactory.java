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

package com.sun.faces.application;

import static jakarta.faces.application.ProjectStage.Production;
import static java.util.logging.Level.WARNING;

import java.util.Hashtable;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import com.sun.faces.util.FacesLogger;

/**
 * Allows configuring ProjectStage at a server (or in GlassFish's case domain) level. This allows for the concept of
 * development and test servers where each application doesn't need to be individually configured, but will instead rely
 * on global JNDI configuration instead.
 */
public class ProjectStageJndiFactory implements ObjectFactory {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    /**
     * Lookup the configured stage by looking for the parameter <code>stage<code>.
     * If the value of <code>stage</code> cannot be determined, the default
     * {@link jakarta.faces.application.ProjectStage#Production} is returned.
     *
     * @see ObjectFactory#getObjectInstance(Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {

        if (obj instanceof Reference) {
            Reference ref = (Reference) obj;
            RefAddr addr = ref.get("stage");
            if (addr != null) {
                String val = (String) addr.getContent();
                if (val != null) {
                    return val.trim();
                }
            } else {
                if (LOGGER.isLoggable(WARNING)) {
                    LOGGER.warning("'stage' property not defined.  Defaulting to Production");
                }
            }
        }

        return Production.toString();
    }
}
