/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package util.jsf;

import java.net.URL;
import javax.faces.view.facelets.ResourceResolver;

/** Allows to share jsf files (placing them in shared jar) 
 *  between several applications (war or ear)
 */
public class CustomResourceResolver extends ResourceResolver {

    private ResourceResolver parent;
    private String basePath;

    public CustomResourceResolver(ResourceResolver parent) {
        this.parent = parent;
        this.basePath = "/META-INF/resources"; // TODO: Make configureable?
    }

    @Override
    public URL resolveUrl(String path) {
        // Resolves from WAR
        URL url = parent.resolveUrl(path); 

        if (url == null) {
            // Resolves from JAR
            url = getClass().getResource(basePath + path); 
        }

        return url;
    }

}
