/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.glassfish.mojarra.test.impl.issue5970;

import java.io.IOException;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.context.FacesContext;

/**
 * A naming container which renders its children once per row, handing them a client id carrying the row it is
 * positioned on, and which is left positioned on the last row it rendered. Its children therefore hold another client
 * id while the response is rendered than they do on any request which does not iterate it.
 */
@FacesComponent(createTag = true, namespace = "eclipse.mojarra.test", tagName = "iterating")
public class IteratingContainer extends UIComponentBase implements NamingContainer {

    private static final String FAMILY = "org.eclipse.mojarra.test.issue5970.IteratingContainer";

    private static final int ROWS = 2;

    private Integer row;

    @Override
    public String getFamily() {
        return FAMILY;
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public String getContainerClientId(FacesContext context) {
        String containerClientId = super.getContainerClientId(context);
        return row == null ? containerClientId : containerClientId + UINamingContainer.getSeparatorChar(context) + row;
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        for (int i = 0; i < ROWS; i++) {
            position(i);

            for (UIComponent child : getChildren()) {
                child.encodeAll(context);
            }
        }
    }

    /**
     * Position on the given row, dropping the client id each child cached while standing on the previous one, which
     * is what {@link UIData#setRowIndex(int)} does with {@code setId} as well.
     *
     * @param row the row to position on.
     */
    private void position(int row) {
        this.row = row;

        for (UIComponent child : getChildren()) {
            child.setId(child.getId());
        }
    }
}
