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

package com.sun.faces.facelets.tag.faces;

import com.sun.faces.facelets.tag.faces.html.ScriptResourceDelegate;
import com.sun.faces.facelets.tag.faces.html.ScriptResourceHandler;
import com.sun.faces.facelets.tag.faces.html.StylesheetResourceDelegate;
import com.sun.faces.facelets.tag.faces.html.StylesheetResourceHandler;

import jakarta.faces.view.facelets.BehaviorHandler;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.TagHandlerDelegate;
import jakarta.faces.view.facelets.TagHandlerDelegateFactory;
import jakarta.faces.view.facelets.ValidatorHandler;

/**
 * Default implementation of {@link TagHandlerDelegateFactory}.
 *
 */
public class TagHandlerDelegateFactoryImpl extends TagHandlerDelegateFactory {

    public TagHandlerDelegateFactoryImpl() {
        super(null);
    }

    @Override
    public TagHandlerDelegate createComponentHandlerDelegate(ComponentHandler owner) {
        if (owner instanceof StylesheetResourceHandler) {
            return new StylesheetResourceDelegate(owner);
        } else if (owner instanceof ScriptResourceHandler) {
            return new ScriptResourceDelegate(owner);
        } else {
            return new ComponentTagHandlerDelegateImpl(owner);
        }
    }

    @Override
    public TagHandlerDelegate createValidatorHandlerDelegate(ValidatorHandler owner) {
        return new ValidatorTagHandlerDelegateImpl(owner);
    }

    @Override
    public TagHandlerDelegate createConverterHandlerDelegate(ConverterHandler owner) {
        return new ConverterTagHandlerDelegateImpl(owner);
    }

    @Override
    public TagHandlerDelegate createBehaviorHandlerDelegate(BehaviorHandler owner) {
        return new BehaviorTagHandlerDelegateImpl(owner);
    }

}
