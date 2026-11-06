/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import static com.sun.faces.junit.Serialization.serializeAndDeserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.sun.faces.component.PassthroughElement;

import jakarta.el.ValueExpression;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;

/**
 * A list or map which is put whole into the state helper restores as a new instance of the nearest class in its
 * hierarchy which can be instantiated, else of {@link ArrayList} or {@link LinkedHashMap}, with its order and
 * duplicates, and replaces the value of the build. An enum map, and any other value, keeps the saved form of
 * {@link UIComponentBase#saveAttachedState(FacesContext, Object)}. A list built by {@link StateHelper#add} or a map
 * built by {@link StateHelper#put(java.io.Serializable, String, Object)} saves only its additions after the initial
 * state was marked, and restores merged into the one of the build.
 */
class ComponentStateHelperCollectionValueTest {

    private static final List<String> LIST_WITH_DUPLICATES = List.of("a", "a", "b");

    private final FacesContext context = mock(FacesContext.class);

    @Test
    void wholeListKeepsDuplicatesInFullState() throws Exception {
        UIData table = new UIData();
        table.setValue(LIST_WITH_DUPLICATES);

        UIData restored = new UIData();
        restored.restoreState(context, serializeAndDeserialize(table.saveState(context)));

        assertEquals(LIST_WITH_DUPLICATES, restored.getValue());
    }

    @Test
    void wholeMapKeepsTypeAndOrderInFullState() throws Exception {
        Map<String, String> value = new LinkedHashMap<>();
        value.put("z", "1");
        value.put("a", "2");
        value.put("m", "3");
        UIOutput output = new UIOutput();
        output.setValue(value);

        UIOutput restored = new UIOutput();
        restored.restoreState(context, serializeAndDeserialize(output.saveState(context)));

        assertEquals(LinkedHashMap.class, restored.getValue().getClass());
        assertEquals(List.of("z", "a", "m"), new ArrayList<>(((Map<?, ?>) restored.getValue()).keySet()));
    }

    @Test
    void wholeListReplacesTheValueOfTheBuildInDeltaState() throws Exception {
        UIData table = tableBuiltWithValue(new ArrayList<>(List.of("x")));
        table.setValue(LIST_WITH_DUPLICATES);

        List<String> rebuiltValue = new ArrayList<>(List.of("x"));
        UIData restored = tableBuiltWithValue(rebuiltValue);
        restored.restoreState(context, serializeAndDeserialize(table.saveState(context)));

        assertEquals(LIST_WITH_DUPLICATES, restored.getValue());
        assertEquals(List.of("x"), rebuiltValue);
    }

    @Test
    void wholeListInDeltaStateSurvivesTheNextPostback() throws Exception {
        UIData table = tableBuiltWithValue(new ArrayList<>(List.of("x")));
        table.setValue(LIST_WITH_DUPLICATES);

        UIData restored = tableBuiltWithValue(new ArrayList<>(List.of("x")));
        restored.restoreState(context, serializeAndDeserialize(table.saveState(context)));
        UIData restoredAgain = tableBuiltWithValue(new ArrayList<>(List.of("x")));
        restoredAgain.restoreState(context, serializeAndDeserialize(restored.saveState(context)));

        assertEquals(LIST_WITH_DUPLICATES, restoredAgain.getValue());
    }

    /**
     * A state which is kept without serializing it, as server side state saving does by default, restores a new
     * instance each time, so that a later request does not change what an earlier view state restores.
     */
    @Test
    void wholeListRestoresAsNewInstanceEachTime() {
        List<String> value = new ArrayList<>(List.of("a", "b"));
        UIOutput output = new UIOutput();
        output.setValue(value);
        Object state = output.saveState(context);

        UIOutput restored = new UIOutput();
        restored.restoreState(context, state);
        @SuppressWarnings("unchecked")
        List<String> restoredValue = (List<String>) restored.getValue();
        restoredValue.add("c");
        UIOutput restoredAgain = new UIOutput();
        restoredAgain.restoreState(context, state);

        assertNotSame(value, restored.getValue());
        assertEquals(List.of("a", "b"), restoredAgain.getValue());
    }

    @Test
    void wholeListSavesItsStateHolderItemsWithTheirOwnState() throws Exception {
        StatefulItem transientItem = new StatefulItem();
        transientItem.setTransient(true);
        UIOutput output = new UIOutput();
        output.setValue(new ArrayList<>(List.of(new StatefulItem("saved"), transientItem)));

        UIOutput restored = new UIOutput();
        restored.restoreState(context, serializeAndDeserialize(output.saveState(context)));

        List<?> value = (List<?>) restored.getValue();
        assertEquals(1, value.size());
        assertEquals("saved", ((StatefulItem) value.get(0)).state);
    }

    @Test
    void wholeMapOfNonPublicClassRestoresAsItsNearestPublicSuperclass() throws Exception {
        UIOutput output = new UIOutput();
        Map<String, String> value = new PrivateLinkedHashMap();
        value.put("z", "1");
        value.put("a", "2");
        output.setValue(value);

        Object restored = restoreFullState(output).getValue();

        assertEquals(LinkedHashMap.class, restored.getClass());
        assertEquals(List.of("z", "a"), new ArrayList<>(((Map<?, ?>) restored).keySet()));
    }

    /**
     * A list without a public class in its hierarchy, such as an immutable one, restores as a new list each time.
     */
    @Test
    void wholeListWithoutPublicClassInItsHierarchyRestoresAsNewArrayListEachTime() {
        UIOutput output = new UIOutput();
        output.setValue(LIST_WITH_DUPLICATES);
        Object state = output.saveState(context);

        UIOutput restored = new UIOutput();
        restored.restoreState(context, state);
        UIOutput restoredAgain = new UIOutput();
        restoredAgain.restoreState(context, state);

        assertEquals(ArrayList.class, restored.getValue().getClass());
        assertEquals(LIST_WITH_DUPLICATES, restored.getValue());
        assertNotSame(restored.getValue(), restoredAgain.getValue());
    }

    @Test
    void wholeMapWithoutPublicClassInItsHierarchyRestoresAsLinkedHashMap() throws Exception {
        Map<String, String> value = new LinkedHashMap<>();
        value.put("z", "1");
        value.put("a", "2");
        value.put("m", "3");
        UIOutput output = new UIOutput();
        output.setValue(Collections.unmodifiableMap(value));

        Object restored = restoreFullState(output).getValue();

        assertEquals(LinkedHashMap.class, restored.getClass());
        assertEquals(List.of("z", "a", "m"), new ArrayList<>(((Map<?, ?>) restored).keySet()));
    }

    @Test
    void wholeSetOfClassWithoutNoArgConstructorKeepsItsTypeAndOrder() throws Exception {
        ReverseSet value = new ReverseSet(Collections.reverseOrder());
        value.addAll(List.of("a", "b"));
        UIOutput output = new UIOutput();
        output.setValue(value);

        Object restored = restoreFullState(output).getValue();

        assertEquals(ReverseSet.class, restored.getClass());
        assertEquals(List.of("b", "a"), new ArrayList<>((Set<?>) restored));
    }

    @Test
    void enumSetKeepsItsType() throws Exception {
        UIOutput output = new UIOutput();
        EnumSet<DayOfWeek> value = EnumSet.of(DayOfWeek.MONDAY);
        output.setValue(value);

        Object restored = restoreFullState(output).getValue();

        assertEquals(value.getClass(), restored.getClass());
        assertEquals(value, restored);
    }

    /**
     * No generated setter records a collection or map value in the attributesThatAreSet list, so its restore does not
     * either.
     */
    @Test
    void wholeCollectionIsNotRecordedInAttributesThatAreSet() throws Exception {
        HtmlOutputText output = new HtmlOutputText();
        output.setStyle("style");
        output.setValue(new LinkedHashSet<>(List.of("a")));

        Map<String, Object> restored = restoreFullState(output).getAttributes();

        assertEquals(List.of("style"), restored.get(UIComponentBase.ATTRIBUTES_THAT_ARE_SET));
    }

    /**
     * Under partial state saving, the full state of a component skips the attributesThatAreSet list only when the
     * restore rebuilds it. A whole collection value is not recorded by the restore, so a list which names it must be
     * saved.
     */
    @Test
    void attributesThatAreSetIsSavedWhenItNamesAWholeCollectionValue() {
        UIViewRoot viewRoot = new UIViewRoot();
        viewRoot.markInitialState();
        when(context.getViewRoot()).thenReturn(viewRoot);
        HtmlOutputText output = new HtmlOutputText();
        output.setValueExpression("value", mock(ValueExpression.class));
        output.setStyle("style");
        output.setValue(new LinkedHashSet<>(List.of("a")));

        HtmlOutputText restored = new HtmlOutputText();
        restored.restoreState(context, output.saveState(context));

        assertEquals(List.of("style", "value"), restored.getAttributes().get(UIComponentBase.ATTRIBUTES_THAT_ARE_SET));
    }

    @Test
    void enumMapKeepsItsType() throws Exception {
        UIOutput output = new UIOutput();
        EnumMap<DayOfWeek, String> value = new EnumMap<>(DayOfWeek.class);
        value.put(DayOfWeek.MONDAY, "1");
        output.setValue(value);

        Object restored = restoreFullState(output).getValue();

        assertEquals(EnumMap.class, restored.getClass());
        assertEquals(value, restored);
    }

    @Test
    void passThroughAttributesKeepTheirOrderInFullState() throws Exception {
        UIOutput output = new UIOutput();
        output.getPassThroughAttributes().put("z", "1");
        output.getPassThroughAttributes().put("a", "2");
        output.getPassThroughAttributes().put("m", "3");

        UIOutput restored = new UIOutput();
        restored.restoreState(context, serializeAndDeserialize(output.saveState(context)));

        assertEquals(List.of("z", "a", "m"), new ArrayList<>(restored.getPassThroughAttributes().keySet()));
    }

    /**
     * A component outside the standard packages keeps its attributesThatAreSet list in the delta. A list put whole
     * there merges into the list of the build.
     */
    @Test
    void attributesThatAreSetPutWholeMergesIntoTheListOfTheBuildInDeltaState() throws Exception {
        PassthroughElement component = componentBuiltWithAttributeSet("a");
        component.getAttributes().put(UIComponentBase.ATTRIBUTES_THAT_ARE_SET, new ArrayList<>(List.of("b")));

        PassthroughElement restored = componentBuiltWithAttributeSet("a");
        restored.restoreState(context, serializeAndDeserialize(component.saveState(context)));

        assertEquals(List.of("a", "b"), restored.getAttributes().get(UIComponentBase.ATTRIBUTES_THAT_ARE_SET));
    }

    @Test
    void addedItemsMergeIntoTheListOfTheBuildInDeltaState() throws Exception {
        UIOutput output = outputBuiltWithAddedItem();
        output.getStateHelper().add(Key.items, "y");

        UIOutput restored = outputBuiltWithAddedItem();
        Object state = serializeAndDeserialize(output.getStateHelper().saveState(context));
        restored.getStateHelper().restoreState(context, state);

        assertEquals(List.of("x", "y"), restored.getStateHelper().get(Key.items));
    }

    @Test
    void putEntriesMergeIntoTheMapOfTheBuildInDeltaState() throws Exception {
        UIOutput output = outputBuiltWithPutEntry();
        output.getStateHelper().put(Key.entries, "y", "2");

        UIOutput restored = outputBuiltWithPutEntry();
        Object state = serializeAndDeserialize(output.getStateHelper().saveState(context));
        restored.getStateHelper().restoreState(context, state);

        assertEquals(Map.of("x", "1", "y", "2"), restored.getStateHelper().get(Key.entries));
    }

    private enum Key {
        items, entries
    }

    private static class PrivateLinkedHashMap extends LinkedHashMap<String, String> {

        private static final long serialVersionUID = 1L;
    }

    public static class ReverseSet extends TreeSet<String> {

        private static final long serialVersionUID = 1L;

        public ReverseSet(Comparator<String> comparator) {
            super(comparator);
        }
    }

    /**
     * A state holder which is not serializable itself.
     */
    public static class StatefulItem implements StateHolder {

        private Object state;
        private boolean transientFlag;

        public StatefulItem() {
        }

        StatefulItem(Object state) {
            this.state = state;
        }

        @Override
        public Object saveState(FacesContext context) {
            return state;
        }

        @Override
        public void restoreState(FacesContext context, Object state) {
            this.state = state;
        }

        @Override
        public boolean isTransient() {
            return transientFlag;
        }

        @Override
        public void setTransient(boolean newTransientValue) {
            transientFlag = newTransientValue;
        }
    }

    private <C extends UIOutput> C restoreFullState(C output) throws Exception {
        @SuppressWarnings("unchecked")
        C restored = (C) output.getClass().getConstructor().newInstance();
        restored.restoreState(context, serializeAndDeserialize(output.saveState(context)));
        return restored;
    }

    private static PassthroughElement componentBuiltWithAttributeSet(String name) {
        PassthroughElement component = new PassthroughElement();
        component.getAttributes().put(UIComponentBase.ATTRIBUTES_THAT_ARE_SET, new ArrayList<>(List.of(name)));
        component.markInitialState();
        return component;
    }

    private static UIData tableBuiltWithValue(List<String> value) {
        UIData table = new UIData();
        table.setValue(value);
        table.markInitialState();
        return table;
    }

    private static UIOutput outputBuiltWithAddedItem() {
        UIOutput output = new UIOutput();
        output.getStateHelper().add(Key.items, "x");
        output.markInitialState();
        return output;
    }

    private static UIOutput outputBuiltWithPutEntry() {
        UIOutput output = new UIOutput();
        output.getStateHelper().put(Key.entries, "x", "1");
        output.markInitialState();
        return output;
    }
}
