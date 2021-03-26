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

package com.sun.faces.spi;

import java.net.URI;
import java.util.Collection;

import jakarta.servlet.ServletContext;

/**
 * <p>
 * Classes that implement this interface return zero or more <code>URL</code>s which refer to application configuration
 * resources.
 * </p>
 *
 * @see FacesConfigResourceProvider
 * @see FaceletConfigResourceProvider
 */
public interface ConfigurationResourceProvider {

    /**
     * @param context the <code>ServletContext</code> for this application
     *
     * @return a List zero or more <code>URL</code> instances representing application configuration resources
     */
    Collection<URI> getResources(ServletContext context);

    /**
     * Parameter to force the XML validation of the Faces configuration files returned by getResources.
     *
     * @param uri the URI for which Mojarra asks if validation is needed. Should always be one as returned by getResources
     * @param globalValidateXml the global (application level) value of the validateXml parameter
     * @return true if Mojarra should validate the given URI file, false if not
     */
    default boolean validateXml(URI uri, boolean globalValidateXml) {
        return globalValidateXml;
    }

}
