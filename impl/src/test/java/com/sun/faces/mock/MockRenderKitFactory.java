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

package com.sun.faces.mock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

public class MockRenderKitFactory extends RenderKitFactory {

    public MockRenderKitFactory(RenderKitFactory oldImpl) {
        System.setProperty(FactoryFinder.RENDER_KIT_FACTORY,
                this.getClass().getName());
    }

    public MockRenderKitFactory() {
    }

    private Map renderKits = new HashMap();

    @Override
    public void addRenderKit(String renderKitId, RenderKit renderKit) {
        if ((renderKitId == null) || (renderKit == null)) {
            throw new NullPointerException();
        }
        synchronized (renderKits) {
            if (renderKits.containsKey(renderKitId)) {
                throw new IllegalArgumentException(renderKitId);
            }
            renderKits.put(renderKitId, renderKit);
        }
    }

    @Override
    public RenderKit getRenderKit(FacesContext context, String renderKitId) {
        if (renderKitId == null) {
            throw new NullPointerException();
        }
        synchronized (renderKits) {
            RenderKit renderKit = (RenderKit) renderKits.get(renderKitId);
            if (renderKit == null) {
                throw new IllegalArgumentException(renderKitId);
            }
            return (renderKit);
        }
    }

    @Override
    public Iterator getRenderKitIds() {
        synchronized (renderKits) {
            return (renderKits.keySet().iterator());
        }
    }
}
