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

package com.sun.faces.facelets.tag;

import com.sun.faces.util.Util;

import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagDecorator;

/**
 * A TagDecorator that is composed of 1 or more TagDecorator instances. It uses the chain of responsibility pattern to
 * stop processing if any of the TagDecorators return a value other than null.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class CompositeTagDecorator implements TagDecorator {

    private final TagDecorator[] decorators;

    private final DefaultTagDecorator defaultTagDecorator;

    public CompositeTagDecorator(TagDecorator[] decorators) {
        Util.notNull("decorators", decorators);
        this.decorators = decorators;
        defaultTagDecorator = new DefaultTagDecorator();
    }

    /**
     * Uses the chain of responsibility pattern to stop processing if any of the TagDecorators return a value other than
     * null.
     */
    @Override
    public Tag decorate(Tag tag) {
        // eliminate the faces: attributes
        Tag noFacesAttributes = defaultTagDecorator.decorate(tag);
        if (noFacesAttributes != null) {
            // pass the converted tag to the other decorators
            tag = noFacesAttributes;
        }

        Tag t = null;
        for (int i = 0; i < decorators.length; i++) {
            t = decorators[i].decorate(tag);
            if (t != null) {
                return t;
            }
        }
        return tag;
    }

}
