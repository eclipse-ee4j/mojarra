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

package com.sun.faces.config.beans;


import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.ToolsUtil;



/**
 * <p>Configuration bean for <code>&lt;render-kit&gt;</code> element.</p>
 */

public class RenderKitBean extends FeatureBean {


    private static final Logger logger = ToolsUtil.getLogger(ToolsUtil.FACES_LOGGER +
            ToolsUtil.BEANS_LOGGER);


    // -------------------------------------------------------------- Properties


    private String renderKitClass;
    public String getRenderKitClass() { return renderKitClass; }
    public void setRenderKitClass(String renderKitClass)
    { this.renderKitClass = renderKitClass; }


    private String renderKitId = "HTML_BASIC";
    public String getRenderKitId() { return renderKitId; }
    public void setRenderKitId(String renderKitId)
    { this.renderKitId = renderKitId; }


    // -------------------------------------------------- RendererHolder Methods


    // Key is family + rendererType
    private Map<String,RendererBean> renderers = new TreeMap<String, RendererBean>();


    public void addRenderer(RendererBean descriptor) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "addRenderer(" +
                      descriptor.getComponentFamily() + "," +
                      descriptor.getRendererType() + ")");
        }
        renderers.put(descriptor.getComponentFamily() + "|" +
                      descriptor.getRendererType(), descriptor);
    }


    public RendererBean getRenderer(String componentFamily,
                                    String rendererType) {
        return (renderers.get
                (componentFamily + "|" + rendererType));
    }


    public RendererBean[] getRenderers() {
        RendererBean results[] = new RendererBean[renderers.size()];
        return (renderers.values().toArray(results));
    }


    public void removeRenderer(RendererBean descriptor) {
        renderers.remove(descriptor.getComponentFamily() + "|" +
                         descriptor.getRendererType());
    }


    // -------------------------------------------------------------- Extensions


    // ----------------------------------------------------------------- Methods


}
