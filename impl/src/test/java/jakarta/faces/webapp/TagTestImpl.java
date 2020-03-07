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

package jakarta.faces.webapp;

import static jakarta.faces.webapp.UIComponentTag.isValueReference;

import jakarta.faces.component.UIComponent;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.webapp.UIComponentTag;

// Test UIComponent Tag
public class TagTestImpl extends UIComponentTag {

    public TagTestImpl() {
        super();
    }

    public TagTestImpl(String componentId) {
        this(componentId, componentId);
    }

    public TagTestImpl(String componentId, String label) {
        super();
        setId(componentId);
        setLabel(label);
    }

    private String label = null;

    public void setLabel(String label) {
        this.label = label;
    }

    private boolean rendersChildren = false;
    private boolean rendersChildrenSet = false;

    public void setRendersChildren(boolean rendersChildren) {
        this.rendersChildren = rendersChildren;
        this.rendersChildrenSet = true;
    }

    @Override
    public void release() {
        super.release();
        this.label = null;
        this.rendersChildrenSet = false;
    }

    @Override
    public String getComponentType() {
        return ("TestComponent");
    }

    @Override
    public String getRendererType() {
        return (null);
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        if (rendersChildrenSet) {
            ((ComponentTestImpl) component).setRendersChildren(rendersChildren);
        }
        if (label != null) {
            if (isValueReference(label)) {
                ValueBinding vb
                        = getFacesContext().getApplication().
                        createValueBinding(label);
                component.setValueBinding("label", vb);
            } else {
                ((ComponentTestImpl) component).setLabel(label);
            }
        }
    }
}
