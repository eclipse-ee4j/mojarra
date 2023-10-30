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

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.model.SelectItem;
import static java.lang.Character.isDigit;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class PackageUtils {

    public final static String MARK_CREATED = "com.sun.faces.facelets.MARK_ID";
    private final static String MARK_ID_CACHE = "com.sun.faces.facelets.MARK_ID_CACHE";
    private static final String PATTERN_CACHE_KEY = "com.sun.faces.patternCache";
    private static final String CLIENT_ID_NESTED_IN_ITERATOR_PATTERN = "CLIENT_ID_NESTED_IN_ITERATOR_PATTERN";

    private PackageUtils() {
    }

    /**
     * Adds all specified <code>otherMarkIds</code> to the mark id cache of this
     * component. Changes are propagated up the component tree.
     */
    private static void addAllDescendantMarkIds(UIComponent component, Map<String, UIComponent> otherMarkIds) {
        getDescendantMarkIdCache(component).putAll(otherMarkIds);
        UIComponent parent = component.getParent();
        if (parent != null) {
            addAllDescendantMarkIds(parent, otherMarkIds);
        }
    }

    /**
     * Adds the specified <code>markId</code> and its corresponding
     * {@link UIComponent} <code>otherComponent</code> to the mark id cache of
     * this component. Changes are propagated up the component tree.
     */
    private static void addSingleDescendantMarkId(UIComponent component, String markId, UIComponent otherComponent) {
        getDescendantMarkIdCache(component).put(markId, otherComponent);
        UIComponent parent = component.getParent();
        if (parent != null) {
            addSingleDescendantMarkId(parent, markId, otherComponent);
        }
    }

    /**
     * Adds the mark id of the specified {@link UIComponent}
     * <code>otherComponent</code> to the mark id cache of this component,
     * including all its descendant mark ids. Changes are propagated up the
     * component tree.
     */
    public static void addToDescendantMarkIdCache(UIComponent component, UIComponent otherComponent) {
        String markId = (String) otherComponent.getAttributes().get(MARK_CREATED);
        if (markId != null) {
            addSingleDescendantMarkId(component, markId, otherComponent);
        }
        Map<String, UIComponent> otherMarkIds = getDescendantMarkIdCache(otherComponent);
        if (!otherMarkIds.isEmpty()) {
            addAllDescendantMarkIds(component, otherMarkIds);
        }
    }

    /**
     * @return an unmodifiable Iterator over the passed array of SelectItem
     */
    public static <T extends SelectItem> Iterator<T> asIterator(T[] items) {
        return unmodifiableIterator(Stream.of(items).iterator());
    }

    /**
     * Returns the first non-<code>null</code> object of the argument list, or
     * <code>null</code> if there is no such element.
     *
     * @param <T> The generic object type.
     * @param objects The argument list of objects to be tested for
     * non-<code>null</code>.
     * @return The first non-<code>null</code> object of the argument list, or
     * <code>null</code> if there is no such element.
     */
    @SafeVarargs
    public static <T> T coalesce(T... objects) {
        for (T object : objects) {
            if (object != null) {
                return object;
            }
        }

        return null;
    }

    public static List<SelectItem> collectSelectItems(FacesContext context, UIComponent component) {
        List<SelectItem> items = new ArrayList<>();

        for (UIComponent child : component.getChildren()) {
            if (child instanceof UISelectItems) {
                createSelectItems(context, child, ((UISelectItems) child).getValue(), SelectItem::new, items::add);
            } else if (child instanceof UISelectItem) {
                items.add(createSelectItem(child, null, SelectItem::new));
            }
        }
        return items;
    }

    public static <S extends SelectItem> void createSelectItems(FacesContext context, UIComponent component, Object values, Supplier<S> supplier, Consumer<S> callback) {
        Map<String, Object> attributes = component.getAttributes();
        String var = coalesce((String) attributes.get("var"), "item");
        stream(values).forEach(value -> new ScopedRunner(context).with(var, value).invoke(() -> callback.accept(createSelectItem(component, getItemValue(attributes, value), supplier))));
    }

    public static <S extends SelectItem> S createSelectItem(UIComponent component, Object value, Supplier<S> supplier) {
        Map<String, Object> attributes = component.getAttributes();
        Object itemValue = getItemValue(attributes, value);
        Object itemLabel = attributes.get("itemLabel");
        Object itemEscaped = coalesce(attributes.get("itemEscaped"), attributes.get("itemLabelEscaped")); // f:selectItem || f:selectItems -- TODO: this should be aligned in their APIs.
        Object itemDisabled = attributes.get("itemDisabled");

        S selectItem = supplier.get();
        selectItem.setValue(itemValue);
        selectItem.setLabel(String.valueOf(itemLabel != null ? itemLabel : selectItem.getValue()));
        selectItem.setEscape(itemEscaped == null || Boolean.parseBoolean(itemEscaped.toString()));
        selectItem.setDisabled(itemDisabled != null && Boolean.parseBoolean(itemDisabled.toString()));
        return selectItem;
    }

    /**
     * Extract first numeric segment from given client ID.
     * <ul>
     * <li>'table:1:button' should return 1</li>
     * <li>'table:2' should return 2</li>
     * <li>'3:button' should return 3</li>
     * <li>'4' should return 4</li>
     * </ul>
     *
     * @param clientId the client ID
     * @param separatorChar the separator character
     * @return first numeric segment from given client ID.
     * @throws NumberFormatException when given client ID doesn't have any
     * numeric segment at all.
     */
    public static int extractFirstNumericSegment(String clientId, char separatorChar) {
        int nextSeparatorChar = clientId.indexOf(separatorChar);

        while (clientId.length() > 0 && !isDigit(clientId.charAt(0)) && nextSeparatorChar >= 0) {
            clientId = clientId.substring(nextSeparatorChar + 1);
            nextSeparatorChar = clientId.indexOf(separatorChar);
        }

        if (clientId.length() > 0 && isDigit(clientId.charAt(0))) {
            String firstNumericSegment = nextSeparatorChar >= 0 ? clientId.substring(0, nextSeparatorChar) : clientId;
            return Integer.parseInt(firstNumericSegment);
        }

        throw new NumberFormatException("there is no numeric segment");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, UIComponent> getDescendantMarkIdCache(UIComponent component) {
        Map<String, UIComponent> descendantMarkIdCache = (Map<String, UIComponent>) component.getTransientStateHelper().getTransient(MARK_ID_CACHE);

        if (descendantMarkIdCache == null) {
            descendantMarkIdCache = new HashMap<String, UIComponent>();
            component.getTransientStateHelper().putTransient(MARK_ID_CACHE, descendantMarkIdCache);
        }

        return descendantMarkIdCache;
    }

    /**
     * Returns item value attribute, taking into account any value expression
     * which actually evaluates to null.
     */
    private static Object getItemValue(Map<String, Object> attributes, Object defaultValue) {
        Object itemValue = attributes.get("itemValue");
        return itemValue != null || attributes.containsKey("itemValue") ? itemValue : defaultValue;
    }

    private static Map<String, Pattern> getPatternCache(Map<String, Object> appMap) {
        @SuppressWarnings("unchecked")
        Map<String, Pattern> result = (Map<String, Pattern>) appMap.get(PATTERN_CACHE_KEY);
        if (result == null) {
            result = Collections.synchronizedMap(new LRUMap<>(15));
            appMap.put(PATTERN_CACHE_KEY, result);
        }

        return result;
    }

    public static boolean isAllNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return false;
            }
        }

        return true;
    }

    public static boolean isAnyNull(Object... values) {
        for (Object value : values) {
            if (value == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the given value is null or is empty. Types
     * of String, Collection, Map, Optional and Array are recognized. If none is
     * recognized, then examine the emptiness of the toString() representation
     * instead.
     *
     * @param value The value to be checked on emptiness.
     * @return <code>true</code> if the given value is null or is empty.
     */
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof String) {
            return ((String) value).isEmpty();
        } else if (value instanceof Collection<?>) {
            return ((Collection<?>) value).isEmpty();
        } else if (value instanceof Map<?, ?>) {
            return ((Map<?, ?>) value).isEmpty();
        } else if (value instanceof Optional<?>) {
            return ((Optional<?>) value).isEmpty();
        } else if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        } else {
            return value.toString() == null || value.toString().isEmpty();
        }
    }

    public static boolean isNestedInIterator(FacesContext context, UIComponent component) {
        UIComponent parent = component.getParent();

        if (parent == null) {
            return false;
        }

        for (UIComponent p = parent; p != null; p = p.getParent()) {
            if (p instanceof UIData || p.getClass().getName().contains("UIRepeat")) {
                return true;
            }
        }

        // https://github.com/eclipse-ee4j/mojarra/issues/4957
        // We should in long term probably introduce a common interface like UIIterable.
        // But this is solid for now as all known implementing components already follow this pattern.
        // We could theoretically even remove the above instanceof checks.
        Pattern clientIdNestedInIteratorPattern = getPatternCache(context.getExternalContext().getApplicationMap()).computeIfAbsent(CLIENT_ID_NESTED_IN_ITERATOR_PATTERN, k -> {
            String separatorChar = Pattern.quote(String.valueOf(UINamingContainer.getSeparatorChar(context)));
            return Pattern.compile(".+" + separatorChar + "[0-9]+" + separatorChar + ".+");
        });

        return clientIdNestedInIteratorPattern.matcher(parent.getClientId(context)).matches();
    }

    /**
     * Returns <code>true</code> if the given faces context is
     * <strong>not</strong> {@link FacesContext#isReleased()}, and its current
     * phase ID is <strong>not</strong> {@link PhaseId#RENDER_RESPONSE}.
     */
    public static boolean isNotRenderingResponse(FacesContext context) {
        return !context.isReleased() && context.getCurrentPhaseId() != PhaseId.RENDER_RESPONSE;
    }

    /**
     * Returns <code>true</code> if the given object equals one of the given
     * objects.
     *
     * @param <T> The generic object type.
     * @param object The object to be checked if it equals one of the given
     * objects.
     * @param objects The argument list of objects to be tested for equality.
     * @return <code>true</code> if the given object equals one of the given
     * objects.
     */
    @SafeVarargs
    public static <T> boolean isOneOf(T object, T... objects) {
        for (Object other : objects) {
            if (object == null ? other == null : object.equals(other)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes all specified <code>otherMarkIds</code> from the mark id cache of
     * this component. Changes are propagated up the component tree.
     */
    private static void removeAllDescendantMarkIds(UIComponent component, Map<String, UIComponent> otherMarkIds) {
        Map<String, UIComponent> descendantMarkIdCache = getDescendantMarkIdCache(component);
        Iterator<String> iterator = otherMarkIds.keySet().iterator();
        while (iterator.hasNext()) {
            descendantMarkIdCache.remove(iterator.next());
        }
        UIComponent parent = component.getParent();
        if (parent != null) {
            removeAllDescendantMarkIds(parent, otherMarkIds);
        }
    }

    /**
     * Removes the mark id of the specified {@link UIComponent}
     * <code>otherComponent</code> from the mark id cache of this component,
     * including all its descendant mark ids. Changes are propagated up the
     * component tree.
     */
    public static void removeFromDescendantMarkIdCache(UIComponent component, UIComponent otherComponent) {
        String markId = (String) otherComponent.getAttributes().get(MARK_CREATED);
        if (markId != null) {
            removeSingleDescendantMarkId(component, markId);
        }
        Map<String, UIComponent> otherMarkIds = getDescendantMarkIdCache(otherComponent);
        if (!otherMarkIds.isEmpty()) {
            removeAllDescendantMarkIds(component, otherMarkIds);
        }
    }

    /**
     * Removes the specified <code>markId</code> from the mark id cache of this
     * component. Changes are propagated up the component tree.
     */
    private static void removeSingleDescendantMarkId(UIComponent component, String markId) {
        getDescendantMarkIdCache(component).remove(markId);
        UIComponent parent = component.getParent();
        if (parent != null) {
            removeSingleDescendantMarkId(parent, markId);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> stream(Object object) {
        if (object == null) {
            return Stream.empty();
        } else if (object instanceof Stream) {
            return (Stream<T>) object;
        } else if (object instanceof Collection) {
            return ((Collection) object).stream();   // little bonus with sized spliterator...
        } else if (object instanceof Enumeration) { // recursive call wrapping in an Iterator (Java 9+)
            return stream(((Enumeration) object).asIterator());
        } else if (object instanceof Iterable) {
            return (Stream<T>) StreamSupport.stream(((Iterable<?>) object).spliterator(), false);
        } else if (object instanceof Map) {
            return (Stream<T>) ((Map<?, ?>) object).entrySet().stream();
        } else if (object instanceof int[]) {
            return (Stream<T>) Arrays.stream((int[]) object).boxed();
        } else if (object instanceof long[]) {
            return (Stream<T>) Arrays.stream((long[]) object).boxed();
        } else if (object instanceof double[]) {
            return (Stream<T>) Arrays.stream((double[]) object).boxed();
        } else if (object instanceof Object[]) {
            return (Stream<T>) Arrays.stream((Object[]) object);
        } else {
            return (Stream<T>) Stream.of(object);
        }
    }

    /**
     * @return an Iterator over the passed Iterator with no remove support
     */
    public static <T> Iterator<T> unmodifiableIterator(Iterator<T> iterator) {
        return new UnmodifiableIterator<>(iterator);
    }

    public static class UnmodifiableIterator<T> implements Iterator<T> {

        private final Iterator<T> iterator;

        public UnmodifiableIterator(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
