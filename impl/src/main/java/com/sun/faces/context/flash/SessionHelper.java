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

package com.sun.faces.context.flash;

import java.io.Serializable;
import java.util.Map;

import jakarta.faces.context.ExternalContext;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;

class SessionHelper implements Serializable, HttpSessionActivationListener {

    private static final long serialVersionUID = -4146679754778263071L;

    static final String FLASH_SESSIONACTIVATIONLISTENER_ATTRIBUTE_NAME = ELFlash.FLASH_ATTRIBUTE_NAME + "FSAL";

    private static final String FLASH_INNER_MAP_KEY = ELFlash.FLASH_ATTRIBUTE_NAME + "FIM";
    private boolean didPassivate;

    static SessionHelper getInstance(ExternalContext extContext) {
        return (SessionHelper) extContext.getSessionMap().get(FLASH_SESSIONACTIVATIONLISTENER_ATTRIBUTE_NAME);
    }

    void update(ExternalContext extContext, ELFlash flash) {
        Map<String, Object> sessionMap = extContext.getSessionMap();
        if (didPassivate) {
            Map<String, Map<String, Object>> flashInnerMap = (Map<String, Map<String, Object>>) sessionMap.get(FLASH_INNER_MAP_KEY);
            flash.setFlashInnerMap(flashInnerMap);
            didPassivate = false;
        } else {
            sessionMap.put(FLASH_SESSIONACTIVATIONLISTENER_ATTRIBUTE_NAME, this);
            sessionMap.put(FLASH_INNER_MAP_KEY, flash.getFlashInnerMap());
        }
    }

    void remove(ExternalContext extContext) {
        Map<String, Object> sessionMap = extContext.getSessionMap();
        sessionMap.remove(FLASH_SESSIONACTIVATIONLISTENER_ATTRIBUTE_NAME);
        sessionMap.remove(FLASH_INNER_MAP_KEY);
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent hse) {
        didPassivate = true;
    }

    @Override
    public void sessionWillPassivate(HttpSessionEvent hse) {
        didPassivate = true;

    }

}
