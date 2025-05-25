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

package com.sun.faces.facelets.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author edburns
 */
public class CompilationMessageHolderImpl implements CompilerPackageCompilationMessageHolder {

    private Map<String, List<FacesMessage>> messageListMap;
    private CompilationManager compilationManager;

    private Map<String, List<FacesMessage>> getMessageListMap() {
        if (null == messageListMap) {
            messageListMap = new HashMap<>();
        }
        return messageListMap;
    }

    @Override
    public List<FacesMessage> getNamespacePrefixMessages(FacesContext context, String prefix) {
        List<FacesMessage> result = null;
        Map<String, List<FacesMessage>> map = getMessageListMap();
        if (null == (result = map.get(prefix))) {
            result = new ArrayList<>();
            map.put(prefix, result);
        }

        return result;
    }

    @Override
    public void processCompilationMessages(FacesContext context) {
        Map<String, List<FacesMessage>> map = getMessageListMap();
        Collection<List<FacesMessage>> values = map.values();
        for (List<FacesMessage> curList : values) {
            for (FacesMessage curMessage : curList) {
                context.addMessage(null, curMessage);
            }
        }
    }

    @Override
    public CompilationManager getCurrentCompositeComponentCompilationManager() {
        return compilationManager;
    }

    @Override
    public void setCurrentCompositeComponentCompilationManager(CompilationManager manager) {
        compilationManager = manager;
    }

}
