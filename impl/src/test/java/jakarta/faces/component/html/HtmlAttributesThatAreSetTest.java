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

package jakarta.faces.component.html;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * A standard component records the pass-through HTML attributes that were set on it, and rendering emits them in the
 * order of that list. The list is therefore kept in natural order as it is written, which is what lets the render side
 * walk it as it finds it instead of sorting it once per component render.
 */
class HtmlAttributesThatAreSetTest {

    private static final String ATTRIBUTES_THAT_ARE_SET = "jakarta.faces.component.UIComponentBase.attributesThatAreSet";

    @Test
    void attributesAreRecordedInNaturalOrderWhateverOrderTheyWereSetIn() {
        HtmlOutputText component = new HtmlOutputText();
        component.setTitle("t");
        component.setDir("ltr");
        component.setStyle("color:red");
        component.setLang("en");

        assertEquals(asList("dir", "lang", "style", "title"), attributesThatAreSet(component));
    }

    @Test
    void settingAnAttributeTwiceRecordsItOnce() {
        HtmlOutputText component = new HtmlOutputText();
        component.setStyle("color:red");
        component.setDir("ltr");
        component.setStyle("color:blue");

        assertEquals(asList("dir", "style"), attributesThatAreSet(component));
    }

    /**
     * An attribute that has no component property is recorded by {@code UIComponentBase.AttributesMap} at the tail of
     * the very same list, so the list as a whole is not necessarily ordered. Recording a component property after one
     * of those must still neither duplicate it nor lose its place among the other properties - a duplicate would emit
     * the HTML attribute twice.
     */
    @Test
    void propertylessAttributesNeitherDuplicateNorDisplaceTheProperties() {
        HtmlOutputText component = new HtmlOutputText();
        component.setDir("ltr");
        component.setTitle("t");
        component.getAttributes().put("aaa-custom", "1");
        component.getAttributes().put("bbb-custom", "2");
        component.getAttributes().put("ccc-custom", "3");
        component.setTitle("another");

        assertEquals(asList("dir", "title", "aaa-custom", "bbb-custom", "ccc-custom"), attributesThatAreSet(component));
    }

    @Test
    void clearingAnAttributeStopsRecordingItAndLeavesTheRestInOrder() {
        HtmlOutputText component = new HtmlOutputText();
        component.setTitle("t");
        component.setDir("ltr");
        component.setStyle("color:red");
        component.setDir(null);

        assertEquals(asList("style", "title"), attributesThatAreSet(component));
    }

    @SuppressWarnings("unchecked")
    private static List<String> attributesThatAreSet(HtmlOutputText component) {
        return (List<String>) component.getAttributes().get(ATTRIBUTES_THAT_ARE_SET);
    }
}
