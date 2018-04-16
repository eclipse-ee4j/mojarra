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

package com.sun.faces.test.servlet30.contractusinghostheader;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;
import javax.faces.view.ViewDeclarationLanguageWrapper;
import java.util.Arrays;
import java.util.List;

/**
 * Use resource library contracts for something like virtual hosts.
 *
 * @author Frank Caputo
 */
public class VDLFactory extends ViewDeclarationLanguageFactory {

    private ViewDeclarationLanguageFactory wrapped;

    public VDLFactory(ViewDeclarationLanguageFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ViewDeclarationLanguageFactory getWrapped() {
        return wrapped;
    }

    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(String viewId) {
        return new VDL(wrapped.getViewDeclarationLanguage(viewId));
    }

    private static class VDL extends ViewDeclarationLanguageWrapper {

        private static final List<String> KNOWN_HOSTS = Arrays.asList("host1", "host2", "host3", "host5");

        private ViewDeclarationLanguage wrapped;

        private VDL(ViewDeclarationLanguage wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public ViewDeclarationLanguage getWrapped() {
            return wrapped;
        }

        @Override
        public List<String> calculateResourceLibraryContracts(FacesContext context, String viewId) {
            String host = context.getExternalContext().getRequestHeaderMap().get("host");

            if(KNOWN_HOSTS.contains(host)) {
                return Arrays.asList(host);
            }

            if("host4".equals(host)) {
                // host4 is a special one. It extends host2 (this is something Leonardo wanted).
                return Arrays.asList("host4", "host2");
            }

            return null;
        }

    }
}
