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

package com.sun.faces.mock;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.PartialViewContext;
import javax.faces.event.PhaseId;

/**
 * MockPartialViewContext implementation.
 */
public class MockPartialViewContext extends PartialViewContext {

    private Map<Object, Object> attributes;
    private boolean partial = false;
    private boolean renderAll = false;

    // ------------------------------------------------------------ Constructors
    public MockPartialViewContext() {

        attributes = new HashMap<Object, Object>();

    }

    // ----------------------------------------- Methods from PartialViewContext
    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public List<String> getExecutePhaseClientIds() {
        throw new UnsupportedOperationException();
    }

    public void setExecutePhaseClientIds(List<String> executePhaseClientIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getExecuteIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<String> getRenderIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getEvalScripts() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRenderPhaseClientIds(List<String> renderPhaseClientIds) {
        throw new UnsupportedOperationException();
    }

    public PartialResponseWriter getPartialResponseWriter() {
        throw new UnsupportedOperationException();
    }

    public boolean isAjaxRequest() {
        return false;
    }

    public boolean isPartialRequest() {
        return partial;
    }

    public boolean isExecuteNone() {
        throw new UnsupportedOperationException();
    }

    public boolean isExecuteAll() {
        throw new UnsupportedOperationException();
    }

    public boolean isRenderAll() {
        return renderAll;
    }

    public void setRenderAll(boolean renderAll) {
        this.renderAll = renderAll;
    }

    public boolean isRenderNone() {
        throw new UnsupportedOperationException();
    }

    public void enableResponseWriting(boolean enable) {
        throw new UnsupportedOperationException();
    }

    public void processPartial(PhaseId phaseId) {
        throw new UnsupportedOperationException();
    }

    public void release() {
        // no-op
    }

    @Override
    public void setPartialRequest(boolean partial) {
        this.partial = partial;
    }

}
