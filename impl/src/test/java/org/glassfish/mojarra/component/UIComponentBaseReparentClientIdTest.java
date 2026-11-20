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
package org.glassfish.mojarra.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKitFactory;

import org.glassfish.mojarra.facelets.component.UIRepeat;
import org.glassfish.mojarra.junit.JUnitFacesTestCaseBase;
import org.glassfish.mojarra.mock.MockRenderKit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A component's client id is derived from its current parent chain. After an ancestor is moved, the client ids of all its descendants must reflect the new
 * parent chain. A removal and re-add to the same parent with an unchanged parent chain keeps the cached client ids of all descendants.
 */
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
        }
        catch (IllegalArgumentException e) {
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
    public void testRemovedAndReaddedToSameParentKeepsCachedDescendantClientIds() {
        UINamingContainer moved = newNamingContainer("moved");
        moved.getChildren().add(descendant);
        oldContainer.getChildren().add(moved);
        String clientId = descendant.getClientId(facesContext);

        oldContainer.getChildren().remove(moved);
        oldContainer.getChildren().add(moved);

        assertSame(clientId, descendant.getClientId(facesContext));
    }

    @Test
    public void testClientIdResolvedWhileDetachedIsRecomputedAfterReaddToSameParent() {
        UINamingContainer moved = newNamingContainer("moved");
        moved.getChildren().add(descendant);
        oldContainer.getChildren().add(moved);
        assertEquals("oldContainer:moved:descendant", descendant.getClientId(facesContext));

        oldContainer.getChildren().remove(moved);
        UIOutput added = new UIOutput();
        added.setId("added");
        moved.getChildren().add(added);
        assertEquals("moved:added", added.getClientId(facesContext));
        oldContainer.getChildren().add(moved);

        assertEquals("oldContainer:moved:added", added.getClientId(facesContext));
        assertEquals("oldContainer:moved:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testClientIdResolvedWhileDetachedFromPlainComponentIsRecomputedAfterReaddToSameParent() {
        UIPanel moved = new UIPanel();
        moved.setId("moved");
        oldContainer.getChildren().add(moved);

        oldContainer.getChildren().remove(moved);
        moved.getChildren().add(descendant);
        assertEquals("descendant", descendant.getClientId(facesContext));
        oldContainer.getChildren().add(moved);

        assertEquals("oldContainer:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testReaddToSameParentAfterParentMovedRecomputesDescendantClientIds() {
        UINamingContainer parent = newNamingContainer("parent");
        oldContainer.getChildren().add(parent);
        UINamingContainer moved = newNamingContainer("moved");
        moved.getChildren().add(descendant);
        parent.getChildren().add(moved);
        assertEquals("oldContainer:parent:moved:descendant", descendant.getClientId(facesContext));

        parent.getChildren().remove(moved);
        newContainer.getChildren().add(parent);
        parent.getChildren().add(moved);

        assertEquals("newContainer:parent:moved:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testReaddToSameParentAfterParentSetDirectlyRecomputesDescendantClientIds() {
        UINamingContainer parent = newNamingContainer("parent");
        oldContainer.getChildren().add(parent);
        UINamingContainer moved = newNamingContainer("moved");
        moved.getChildren().add(descendant);
        parent.getChildren().add(moved);
        assertEquals("oldContainer:parent:moved:descendant", descendant.getClientId(facesContext));

        parent.getChildren().remove(moved);
        parent.setParent(newContainer);
        parent.getChildren().add(moved);

        assertEquals("newContainer:parent:moved:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testReaddToSameParentAfterGrandparentMovedRecomputesDescendantClientIds() {
        UIPanel grandparent = new UIPanel();
        oldContainer.getChildren().add(grandparent);
        UIPanel parent = new UIPanel();
        grandparent.getChildren().add(parent);
        UINamingContainer moved = newNamingContainer("moved");
        moved.getChildren().add(descendant);
        parent.getChildren().add(moved);
        assertEquals("oldContainer:moved:descendant", descendant.getClientId(facesContext));

        parent.getChildren().remove(moved);
        newContainer.getChildren().add(grandparent);
        parent.getChildren().add(moved);

        assertEquals("newContainer:moved:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testClientIdResolvedWhileDetachedUnderFormWithoutPrependIdIsRecomputedAfterReaddToSameParent() {
        UIForm form = new UIForm();
        form.setId("form");
        form.setPrependId(false);
        oldContainer.getChildren().add(form);

        oldContainer.getChildren().remove(form);
        form.getChildren().add(descendant);
        assertEquals("descendant", descendant.getClientId(facesContext));
        oldContainer.getChildren().add(form);

        assertEquals("oldContainer:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testMovedDataRecomputesOwnAndDescendantClientIds() {
        UIData data = new UIData();
        data.setId("data");
        data.getChildren().add(descendant);
        oldContainer.getChildren().add(data);
        assertEquals("oldContainer:data", data.getClientId(facesContext));
        assertEquals("oldContainer:data:descendant", descendant.getClientId(facesContext));

        newContainer.getChildren().add(data);

        assertEquals("newContainer:data", data.getClientId(facesContext));
        assertEquals("newContainer:data:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testDataClientIdResolvedBeforeFirstAddIsRecomputedAfterAdd() {
        UIData data = new UIData();
        data.setId("data");
        data.getChildren().add(descendant);
        assertEquals("data:descendant", descendant.getClientId(facesContext));

        oldContainer.getChildren().add(data);

        assertEquals("oldContainer:data", data.getClientId(facesContext));
        assertEquals("oldContainer:data:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testDataUnderMovedAncestorRecomputesOwnAndDescendantClientIds() {
        UIPanel moved = new UIPanel();
        UIData data = new UIData();
        data.setId("data");
        data.getChildren().add(descendant);
        moved.getChildren().add(data);
        oldContainer.getChildren().add(moved);
        assertEquals("oldContainer:data", data.getClientId(facesContext));
        assertEquals("oldContainer:data:descendant", descendant.getClientId(facesContext));

        newContainer.getChildren().add(moved);

        assertEquals("newContainer:data", data.getClientId(facesContext));
        assertEquals("newContainer:data:descendant", descendant.getClientId(facesContext));
    }

    @Test
    public void testDataMovedIntoIteratingParentRecomputesPerRowClientIds() {
        UIData data = newData("data");
        oldContainer.getChildren().add(data);
        assertEquals("oldContainer:data", data.getClientId(facesContext));
        UIData outer = newData("outer");
        UIColumn column = new UIColumn();
        outer.getChildren().add(column);
        newContainer.getChildren().add(outer);

        column.getChildren().add(data);

        outer.setRowIndex(0);
        assertEquals("newContainer:outer:0:data", data.getClientId(facesContext));
        outer.setRowIndex(1);
        assertEquals("newContainer:outer:1:data", data.getClientId(facesContext));
    }

    @Test
    public void testDataClientIdResolvedUnderDetachedAncestorIsRecomputedPerRowAfterReadd() {
        UIData outer = newData("outer");
        oldContainer.getChildren().add(outer);
        UIColumn column = new UIColumn();
        outer.getChildren().add(column);
        UINamingContainer container = newNamingContainer("container");
        column.getChildren().add(container);
        assertEquals("oldContainer:outer:container", container.getClientId(facesContext));

        outer.getChildren().remove(column);
        UIData data = newData("data");
        container.getChildren().add(data);
        assertEquals("oldContainer:outer:container:data", data.getClientId(facesContext));
        outer.getChildren().add(column);

        outer.setRowIndex(0);
        assertEquals("oldContainer:outer:0:container:data", data.getClientId(facesContext));
    }

    @Test
    public void testDataReaddedToSameParentUnderIteratorKeepsPerRowClientIds() {
        RowIterator outer = new RowIterator();
        outer.setId("outer");
        newContainer.getChildren().add(outer);
        UIPanel parent = new UIPanel();
        outer.getChildren().add(parent);
        UIData data = newData("data");
        parent.getChildren().add(data);
        outer.setRow(0);
        assertEquals("newContainer:outer:0:data", data.getClientId(facesContext));
        outer.setRow(-1);

        parent.getChildren().remove(data);
        parent.getChildren().add(data);
        assertEquals("newContainer:outer:data", data.getClientId(facesContext));

        outer.setRow(1);
        assertEquals("newContainer:outer:1:data", data.getClientId(facesContext));
    }

    @Test
    public void testRepeatMovedIntoIteratingParentIsNested() throws Exception {
        UIRepeat repeat = new UIRepeat();
        oldContainer.getChildren().add(repeat);
        assertFalse(keepsSavedState(repeat));
        UIData outer = newData("outer");
        UIColumn column = new UIColumn();
        outer.getChildren().add(column);
        newContainer.getChildren().add(outer);

        column.getChildren().add(repeat);

        assertTrue(keepsSavedState(repeat));
    }

    @Test
    public void testRepeatUnderAncestorMovedIntoIteratingParentIsNested() throws Exception {
        UIPanel moved = new UIPanel();
        UIRepeat repeat = new UIRepeat();
        moved.getChildren().add(repeat);
        oldContainer.getChildren().add(moved);
        assertFalse(keepsSavedState(repeat));
        UIData outer = newData("outer");
        UIColumn column = new UIColumn();
        outer.getChildren().add(column);
        newContainer.getChildren().add(outer);

        column.getChildren().add(moved);

        assertTrue(keepsSavedState(repeat));
    }

    @Test
    public void testClientIdResolvedBeforeFirstAddIsRecomputedAfterAdd() {
        UINamingContainer moved = newNamingContainer("moved");
        moved.getChildren().add(descendant);
        assertEquals("moved:descendant", descendant.getClientId(facesContext));

        oldContainer.getChildren().add(moved);

        assertEquals("oldContainer:moved:descendant", descendant.getClientId(facesContext));
    }

    /**
     * A naming container iterating over its children like a third-party iterator: it is not a {@link UIData}, and its container client id carries the current
     * row.
     */
    private static class RowIterator extends UINamingContainer {

        private int row = -1;

        void setRow(int row) {
            this.row = row;
            resetDescendantClientIds(this);
        }

        private static void resetDescendantClientIds(UIComponent component) {
            for (UIComponent child : component.getChildren()) {
                child.setId(child.getId());
                resetDescendantClientIds(child);
            }
        }

        @Override
        public String getContainerClientId(FacesContext context) {
            String clientId = getClientId(context);
            return row == -1 ? clientId : clientId + UINamingContainer.getSeparatorChar(context) + row;
        }

    }

    /**
     * A repeat keeps the saved state of its rows when it has error messages or is nested in an iterating component.
     */
    private boolean keepsSavedState(UIRepeat repeat) throws Exception {
        Method keepSaved = UIRepeat.class.getDeclaredMethod("keepSaved", FacesContext.class);
        keepSaved.setAccessible(true);
        return (Boolean) keepSaved.invoke(repeat, facesContext);
    }

    private static UIData newData(String id) {
        UIData data = new UIData();
        data.setId(id);
        data.setVar("row");
        data.setValue(List.of("first", "second"));
        return data;
    }

    private static UINamingContainer newNamingContainer(String id) {
        UINamingContainer container = new UINamingContainer();
        container.setId(id);
        return container;
    }

}
