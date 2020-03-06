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

package jakarta.faces.component;

import java.io.Serializable;
import java.util.*;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHint;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.BehaviorEvent;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * <p class="changed_added_2_0">
 * Test case for component behaviors.
 * </p>
 *
 * @since 2.0
 */
public class UIComponentBaseBehaviorTestCase extends UIComponentTestCase {

    private static final String ONTEST = "ontest";
    private static final String ONCLICK = "onclick";
    private static final String ONCHANGE = "onchange";
    private static final String TEST_FAMILY = "jakarta.faces.Test";
    private static final Collection<String> EVENTS = set(ONTEST, ONCLICK, ONCHANGE);

    public static class BehaviorComponent extends UIComponentBase implements ClientBehaviorHolder {

        /* (non-Javadoc)
         * @see jakarta.faces.component.UIComponent#getFamily()
         */
        @Override
        public String getFamily() {
            return TEST_FAMILY;
        }

        @Override
        public Collection<String> getEventNames() {
            return EVENTS;
        }

        @Override
        public String getDefaultEventName() {
            return ONTEST;
        }

    }

    @SuppressWarnings("serial")
    public static class TestBehavior implements ClientBehavior, Serializable {

        private static final Set<ClientBehaviorHint> HINTS
                = Collections.unmodifiableSet(EnumSet.of(ClientBehaviorHint.SUBMITTING));

        private static int sequence = 0;

        private final int id;

        public TestBehavior() {
            id = sequence++;
        }

        public String getRendererType() {
            return TEST_FAMILY;
        }

        @Override
        public Set<ClientBehaviorHint> getHints() {
            return HINTS;
        }

        @Override
        public void broadcast(BehaviorEvent event) {
        }

        @Override
        public void decode(FacesContext context, UIComponent component) {
        }

        @Override
        public String getScript(ClientBehaviorContext bContext) {
            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TestBehavior other = (TestBehavior) obj;
            if (id != other.id) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Behavior #" + id;
        }
    }

    public UIComponentBaseBehaviorTestCase(String name) {
        super(name);
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIComponentBase#saveState(jakarta.faces.context.FacesContext)}.
     */
    public void testSaveState() {
        BehaviorComponent comp = new BehaviorComponent();
        // Cast component to the interface, to be sure about method definition.
        ClientBehaviorHolder holder = (ClientBehaviorHolder) comp;
        TestBehavior behavior = new TestBehavior();
        holder.addClientBehavior(ONCLICK, behavior);
        TestBehavior behavior2 = new TestBehavior();
        holder.addClientBehavior(ONCLICK, behavior2);
        TestBehavior behavior3 = new TestBehavior();
        holder.addClientBehavior(ONCHANGE, behavior3);
        Object state = comp.saveState(facesContext);
        BehaviorComponent restoredComp = new BehaviorComponent();
        restoredComp.restoreState(facesContext, state);
        Map<String, List<ClientBehavior>> behaviors = restoredComp.getClientBehaviors();
        assertFalse(behaviors.isEmpty());
        assertTrue(behaviors.containsKey(ONCLICK));
        assertTrue(behaviors.containsKey(ONCHANGE));
        assertFalse(behaviors.containsKey(ONTEST));
        assertEquals(2, behaviors.entrySet().size());
        assertEquals(2, behaviors.keySet().size());
        assertEquals(2, behaviors.values().size());
        assertEquals(2, behaviors.get(ONCLICK).size());
        assertEquals(1, behaviors.get(ONCHANGE).size());
        assertEquals(behavior3, behaviors.get(ONCHANGE).get(0));
        assertEquals(behavior, behaviors.get(ONCLICK).get(0));
        assertEquals(behavior2, behaviors.get(ONCLICK).get(1));
    }

    public void testNonClientBehaviorHolder() throws Exception {
        UIInput input = new UIInput();
        try {
            input.addClientBehavior(ONTEST, new TestBehavior());
        } catch (IllegalStateException e) {
            return;
        }
        assertFalse(true);
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIComponentBase#addClientBehavior(java.lang.String, jakarta.faces.component.behavior.Behavior)}.
     */
    public void testAddBehavior() {
        BehaviorComponent comp = new BehaviorComponent();
        // Cast component to the interface, to be sure about method definition.
        ClientBehaviorHolder holder = (ClientBehaviorHolder) comp;
        holder.addClientBehavior(ONCLICK, new TestBehavior());
        holder.addClientBehavior(ONCLICK, new TestBehavior());
        holder.addClientBehavior(ONCHANGE, new TestBehavior());
        try {
            holder.addClientBehavior("foo", new TestBehavior());
        } catch (IllegalArgumentException e) {
            return;
        }
        assertFalse(true);
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIComponentBase#getEventNames()}.
     */
    public void testGetEventNames() {
        BehaviorComponent comp = new BehaviorComponent();
        ClientBehaviorHolder holder = (ClientBehaviorHolder) comp;
        assertEquals(EVENTS, holder.getEventNames());
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIComponentBase#getClientBehaviors()}.
     */
    public void testGetBehaviors() {
        BehaviorComponent comp = new BehaviorComponent();
        // Cast component to the interface, to be sure about method definition.
        ClientBehaviorHolder holder = (ClientBehaviorHolder) comp;
        Map<String, List<ClientBehavior>> behaviors = holder.getClientBehaviors();
        assertTrue(behaviors.isEmpty());
        assertFalse(behaviors.containsKey(ONCLICK));
        assertFalse(behaviors.containsValue(new TestBehavior()));
        assertEquals(0, behaviors.entrySet().size());
        holder.addClientBehavior(ONCLICK, new TestBehavior());
        holder.addClientBehavior(ONCLICK, new TestBehavior());
        holder.addClientBehavior(ONCHANGE, new TestBehavior());
        assertFalse(behaviors.isEmpty());
        assertTrue(behaviors.containsKey(ONCLICK));
        assertTrue(behaviors.containsKey(ONCHANGE));
        assertFalse(behaviors.containsKey(ONTEST));
        assertEquals(2, behaviors.entrySet().size());
        assertEquals(2, behaviors.keySet().size());
        assertEquals(2, behaviors.values().size());
        assertEquals(2, behaviors.get(ONCLICK).size());
        assertEquals(1, behaviors.get(ONCHANGE).size());
    }

    /**
     * Test method for
     * {@link jakarta.faces.component.UIComponentBase#getDefaultEventName()}.
     */
    public void testGetDefaultEventName() {
        BehaviorComponent comp = new BehaviorComponent();
        // Cast component to the interface, to be sure about method definition.
        ClientBehaviorHolder holder = (ClientBehaviorHolder) comp;
        assertEquals(ONTEST, holder.getDefaultEventName());
    }

    public static <T> Set<T> set(T... ts) {
        return new HashSet<T>(Arrays.asList(ts));
    }
}
