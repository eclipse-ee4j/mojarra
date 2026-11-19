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
package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;

import jakarta.faces.FactoryFinder;
import jakarta.faces.render.RenderKitFactory;

public class UIComponentBaseReparentClientIdTest extends JUnitFacesTestCaseBase {

    private UINamingContainer oldContainer;
    private UINamingContainer newContainer;
    private UIOutput descendant;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        UIViewRoot root = new UIViewRoot();
        root.setViewId("/viewId");
        root.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(root);
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        try {
            renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, new MockRenderKit());
        } catch (IllegalArgumentException e) {
        }

        oldContainer = newNamingContainer("oldContainer");
        newContainer = newNamingContainer("newContainer");
        root.getChildren().add(oldContainer);
        root.getChildren().add(newContainer);

        descendant = new UIOutput();
        descendant.setId("descendant");
    }

    @Test
    public void testReparentedNamingContainerRecomputesDescendantClientIds() {
        UINamingContainer moved = newNamingContainer("moved");
        moved.getChildren().add(descendant);
        oldContainer.getChildren().add(moved);
        assertEquals("oldContainer:moved:descendant", descendant.getClientId(facesContext));

        newContainer.getFacets().put("moved", moved);

        assertEquals("newContainer:moved", moved.getClientId(facesContext));
        assertEquals("newContainer:moved:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testReparentedPlainComponentRecomputesDescendantClientIds() {
        UIPanel moved = new UIPanel();
        moved.setId("moved");
        moved.getChildren().add(descendant);
        oldContainer.getChildren().add(moved);
        assertEquals("oldContainer:descendant", descendant.getClientId(facesContext));

        newContainer.getChildren().add(moved);

        assertEquals("newContainer:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testRemovedNamingContainerRecomputesDescendantClientIds() {
        UINamingContainer moved = newNamingContainer("moved");
        moved.getChildren().add(descendant);
        oldContainer.getChildren().add(moved);
        assertEquals("oldContainer:moved:descendant", descendant.getClientId(facesContext));

        oldContainer.getChildren().remove(moved);

        assertEquals("moved:descendant", descendant.getClientId(facesContext));
    }

    private static UINamingContainer newNamingContainer(String id) {
        UINamingContainer container = new UINamingContainer();
        container.setId(id);
        return container;
    }
}
