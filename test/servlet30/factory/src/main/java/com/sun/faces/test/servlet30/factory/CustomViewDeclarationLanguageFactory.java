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

package com.sun.faces.test.servlet30.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;

public class CustomViewDeclarationLanguageFactory extends ViewDeclarationLanguageFactory {

    public CustomViewDeclarationLanguageFactory() {
    }

    private ViewDeclarationLanguageFactory toWrap;
    private Map<String, CustomViewDeclarationLanguage> vdlImpls;

    public CustomViewDeclarationLanguageFactory(ViewDeclarationLanguageFactory toWrap) {
        this.toWrap = toWrap;
        vdlImpls = new ConcurrentHashMap<String, CustomViewDeclarationLanguage>();
    }

    @Override
    public ViewDeclarationLanguageFactory getWrapped() {
        return toWrap;
    }

    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(String string) {
        CustomViewDeclarationLanguage result = null;

        if (null == (result = vdlImpls.get(string))) {
            result = new CustomViewDeclarationLanguage(getWrapped().getViewDeclarationLanguage(string));
            vdlImpls.put(string, result);
        }

        return result;
    }
}
