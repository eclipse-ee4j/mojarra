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

package com.sun.faces.facelets.tag.ui;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

public class SchemaCompliantRemoveHandler extends TagHandler {

    public SchemaCompliantRemoveHandler(TagConfig config) {
        super(config);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        throw new FaceletException("Error: The Facelet parser is responsible for handling the <ui:remove> element.  This TagHandler implementation is only provided to allow the ui.taglib.xml file to be compliant with web-facelettaglibrary_2_2.xsd.  If you are seeing this exception, there is something wrong with how the JSF runtime is configuring its Facelets compiler.");
    }



}
