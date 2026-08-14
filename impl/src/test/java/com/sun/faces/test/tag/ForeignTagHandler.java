/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package com.sun.faces.test.tag;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagHandler;

/**
 * A tag handler taking over {@code apply} from outside the packages of this implementation, which is what every tag
 * handler of another tag library does. It lives here rather than beside the test using it so that it does not fall
 * under the package this implementation audits.
 */
public class ForeignTagHandler extends TagHandler {

    public ForeignTagHandler(TagConfig config) {
        super(config);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        // Whatever this contributes to the view is exactly what this implementation cannot know.
    }
}
