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

package javax.faces.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;

import com.sun.faces.mock.MockExternalContext;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link UICommand}.
 * </p>
 */
public class UICommandTestCase extends UIComponentBaseTestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public UICommandTestCase(String name) {
        super(name);
    }

    private static Class actionListenerSignature[] = { ActionEvent.class };

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        super.setUp();
        component = new UICommand();
        expectedFamily = UICommand.COMPONENT_FAMILY;
        expectedId = null;
        expectedRendererType = "javax.faces.Button";
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(UICommandTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods

    // Test order of action listener calls with actionListener also
    public void PENDING_FIXME_testActionOrder() throws Exception {

        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(facesContext, RenderKitFactory.HTML_BASIC_RENDER_KIT);
        renderKit.addRenderer(UICommand.COMPONENT_FAMILY, "javax.faces.Button", new ButtonRenderer());
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);
        UICommand command = (UICommand) component;
        command.setId("command");
        command.addActionListener(new CommandActionListenerTestImpl("l1"));
        command.addActionListener(new CommandActionListenerTestImpl("l2"));
        command.setImmediate(true);
        request.setAttribute("l3", new CommandActionListenerTestImpl("l3"));

        // Override the default action listener to test ordering
        ActionListener oldDefaultActionListener = facesContext.getApplication().getActionListener();
        facesContext.getApplication().setActionListener(new CommandActionListenerTestImpl("14"));
        Map map = new HashMap();
        map.put(command.getClientId(facesContext), "");
        MockExternalContext econtext = (MockExternalContext) facesContext.getExternalContext();
        econtext.setRequestParameterMap(map);
        CommandActionListenerTestImpl.trace(null);
        root.processDecodes(facesContext);
        assertEquals("/l1/l2/l3", CommandActionListenerTestImpl.trace());

        // Restore the default action listener
        facesContext.getApplication().setActionListener(oldDefaultActionListener);
    }


    // Test event queuing and broadcasting (any phase listeners)
    public void testEventsGeneric() {

        UICommand command = (UICommand) component;
        command.setRendererType(null);
        ActionEvent event = new ActionEvent(command);

        // Register three listeners
        command.addActionListener(new ActionListenerTestImpl("AP0"));
        command.addActionListener(new ActionListenerTestImpl("AP1"));
        command.addActionListener(new ActionListenerTestImpl("AP2"));

        // Fire events and evaluate results
        ActionListenerTestImpl.trace(null);
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(command);
        command.queueEvent(event);
        root.processDecodes(facesContext);
        root.processValidators(facesContext);
        root.processApplication(facesContext);
        assertEquals("/AP0@INVOKE_APPLICATION 5/AP1@INVOKE_APPLICATION 5/AP2@INVOKE_APPLICATION 5", ActionListenerTestImpl.trace());

    }

    // Test event queuing and broadcasting (mixed phase listeners)
    public void testEventsMixed() {

        UICommand command = (UICommand) component;
        command.setRendererType(null);
        ActionEvent event = new ActionEvent(command);

        // Register three listeners
        command.addActionListener(new ActionListenerTestImpl("ARV"));
        command.addActionListener(new ActionListenerTestImpl("PV"));
        command.addActionListener(new ActionListenerTestImpl("AP"));

        // Fire events and evaluate results
        ActionListenerTestImpl.trace(null);
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(command);
        command.queueEvent(event);
        root.processDecodes(facesContext);
        root.processValidators(facesContext);
        root.processApplication(facesContext);
        assertEquals("/ARV@INVOKE_APPLICATION 5/PV@INVOKE_APPLICATION 5/AP@INVOKE_APPLICATION 5", ActionListenerTestImpl.trace());

    }

    // Test event queuing and broadcasting (mixed phase listeners), with
    // immediate set.
    public void testEventsMixedImmediate() {

        UICommand command = (UICommand) component;
        command.setImmediate(true);
        command.setRendererType(null);
        ActionEvent event = new ActionEvent(command);

        // Register three listeners
        command.addActionListener(new ActionListenerTestImpl("ARV"));
        command.addActionListener(new ActionListenerTestImpl("PV"));
        command.addActionListener(new ActionListenerTestImpl("AP"));

        // Fire events and evaluate results
        ActionListenerTestImpl.trace(null);
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(command);
        command.queueEvent(event);
        root.processDecodes(facesContext);
        root.processValidators(facesContext);
        root.processApplication(facesContext);
        assertEquals("/ARV@APPLY_REQUEST_VALUES 2/PV@APPLY_REQUEST_VALUES 2/AP@APPLY_REQUEST_VALUES 2", ActionListenerTestImpl.trace());

    }

    // Test listener registration and deregistration
    public void testListeners() {

        CommandTestImpl command = new CommandTestImpl();
        ActionListenerTestImpl listener = null;

        command.addActionListener(new ActionListenerTestImpl("ARV0"));
        command.addActionListener(new ActionListenerTestImpl("ARV1"));
        command.addActionListener(new ActionListenerTestImpl("PV0"));
        command.addActionListener(new ActionListenerTestImpl("PV1"));
        command.addActionListener(new ActionListenerTestImpl("PV2"));

        ActionListener listeners[] = command.getActionListeners();
        assertEquals(5, listeners.length);
        command.removeActionListener(listeners[2]);
        listeners = command.getActionListeners();
        assertEquals(4, listeners.length);

    }

    // Test empty listener list
    public void testEmptyListeners() {

        CommandTestImpl command = new CommandTestImpl();
        ActionListenerTestImpl listener = null;

        // No listeners added, should be empty
        ActionListener listeners[] = command.getActionListeners();
        assertEquals(0, listeners.length);

    }

    // Suppress lifecycle tests since we do not have a renderer
    @Override
    public void testLifecycleManagement() {
    }


    // Test setting properties to invalid values
    @Override
    public void testPropertiesInvalid() throws Exception {
        super.testPropertiesInvalid();
        UICommand command = (UICommand) component;
    }


    public void testNestedCommands() {
        UIViewRoot root = new UIViewRoot();
        UICommand c1 = new UICommand();
        UICommand c2 = new UICommand();
        root.getChildren().add(c1);
        c2.setImmediate(true);
        c1.getChildren().add(c2);
        ActionEvent ae = new ActionEvent(c2);
        c2.queueEvent(ae);
        assertTrue(ae.getPhaseId().equals(PhaseId.APPLY_REQUEST_VALUES));

        root = new UIViewRoot();
        c1 = new UICommand();
        c2 = new UICommand();
        root.getChildren().add(c1);
        c1.setImmediate(true);
        c2.setImmediate(false);
        c1.getChildren().add(c2);
        ae = new ActionEvent(c2);
        c2.queueEvent(ae);
        assertTrue(ae.getPhaseId().equals(PhaseId.INVOKE_APPLICATION));
    }


    public void testGetActionListeners() throws Exception {
        UICommand command = (UICommand) component;
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(command);

        ActionListenerTestImpl ta1 = new ActionListenerTestImpl("ta1"), ta2 = new ActionListenerTestImpl("ta2");

        command.addActionListener(ta1);
        command.addActionListener(ta2);
        ActionListener[] listeners = command.getActionListeners();
        assertEquals(2, listeners.length);
        ActionListenerTestImpl[] taListeners = (ActionListenerTestImpl[]) command.getFacesListeners(ActionListenerTestImpl.class);
    }

    // --------------------------------------------------------- Support Methods



    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UICommand();
        component.setRendererType(null);
        return (component);
    }


    protected boolean listenersAreEqual(FacesContext context, UICommand comp1, UICommand comp2) {
        ActionListener[] list1 = comp1.getActionListeners();
        ActionListener[] list2 = comp2.getActionListeners();
        // make sure they're either both null or both non-null
        if ((null == list1 && null != list2) || (null != list1 && null == list2)) {
            return false;
        }
        if (null == list1) {
            return true;
        }
        int j = 0, outerLen = list1.length;
        boolean result = true;
        if (outerLen != list2.length) {
            return false;
        }
        for (j = 0; j < outerLen; j++) {
            result = list1[j].equals(list2[j]);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    // --------------------------------------------------------- Private Classes
    // "Button" Renderer
    class ButtonRenderer extends Renderer {

        @Override
        public void decode(FacesContext context, UIComponent component) {

            if ((context == null) || (component == null)) {
                throw new NullPointerException();
            }

            if (!(component instanceof ActionSource)) {
                return;
            }
            String clientId = component.getClientId(context);
            Map params = context.getExternalContext().getRequestParameterMap();
            if (params.containsKey(clientId)) {
                component.queueEvent(new ActionEvent(component));
            }
        }

        @Override
        public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
            if ((context == null) || (component == null)) {
                throw new NullPointerException();
            }
        }

        @Override
        public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
            if ((context == null) || (component == null)) {
                throw new NullPointerException();
            }
        }

        @Override
        public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
            if ((context == null) || (component == null)) {
                throw new NullPointerException();
            }
        }
    }
}
