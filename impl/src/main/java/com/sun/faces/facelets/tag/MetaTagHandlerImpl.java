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

import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.MetaTagHandler;
import jakarta.faces.view.facelets.TagConfig;

/**
 * A base tag for wiring state to an object instance based on rules populated at the time of creating a MetaRuleset.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public abstract class MetaTagHandlerImpl extends MetaTagHandler {

    public MetaTagHandlerImpl(TagConfig config) {
        super(config);
    }

    /**
     * Extend this method in order to add your own rules.
     *
     * @param type
     */
    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        Util.notNull("type", type);
        return new MetaRulesetImpl(tag, type);
    }

}
