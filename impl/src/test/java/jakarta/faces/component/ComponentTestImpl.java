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

package jakarta.faces.component;

import java.io.IOException;

import jakarta.faces.context.FacesContext;

/**
 * <p>
 * Test <code>UIComponent</code> for unit tests.
 * </p>
 */
public class ComponentTestImpl extends UIComponentBase {

    public ComponentTestImpl() {
        this("test");
    }

    public ComponentTestImpl(String componentId) {
        super();
        setId(componentId);
    }

    public String getComponentType() {
        return "TestComponent";
    }

    @Override
    public String getFamily() {
        return "Test";
    }

    // -------------------------------------------------- Trace-Enabled Methods
    @Override
    public void decode(FacesContext context) {
        trace("d-" + getId());
        super.decode(context);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        trace("eB-" + getId());
        super.encodeBegin(context);
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        trace("eC-" + getId());
        super.encodeChildren(context);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        trace("eE-" + getId());
        super.encodeEnd(context);
    }

    public void updateModel(FacesContext context) {
        trace("u-" + getId());
        // super.updateModel(context);
    }

    @Override
    public void processDecodes(FacesContext context) {
        trace("pD-" + getId());
        super.processDecodes(context);
    }

    @Override
    public void processValidators(FacesContext context) {
        trace("pV-" + getId());
        super.processValidators(context);
    }

    @Override
    public void processUpdates(FacesContext context) {
        trace("pU-" + getId());
        super.processUpdates(context);
    }

    public void callPushComponent(FacesContext context) {
        pushComponentToEL(context, null);
    }

    public void callPopComponent(FacesContext context) {
        popComponentFromEL(context);
    }

    // --------------------------------------------------- Static Trace Methods
    // Accumulated trace log
    private static StringBuffer trace = new StringBuffer();

    // Append to the current trace log (or clear if null)
    public static void trace(String text) {
        if (text == null) {
            trace.setLength(0);
        } else {
            trace.append('/');
            trace.append(text);
        }
    }

    // Retrieve the current trace log
    public static String trace() {
        return trace.toString();
    }
}
