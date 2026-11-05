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
package org.eclipse.mojarra.test.issue6110;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The <code>UIData.encodeBegin()</code> javadoc requires that the saved per-row state of child inputs is discarded,
 * unless it is needed to re-render the current page with errors, which are messages of severity ERROR or higher. This
 * holds for a nested <code>UIData</code> as well as for a top-level one, and likewise for <code>ui:repeat</code>. When
 * the table which holds the inputs has <code>rowStatePreserved="true"</code>, the per-row state is kept in both cases.
 * The <code>rowStatePreserved</code> of an enclosing table does not matter.
 * <p>
 * Each test types a value into every row input, submits with a required field left empty, and then re-renders the
 * page with an ajax request which does not execute the tables.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6110">issue 6110</a>
 */
class Issue6110IT extends BaseIT {

    private static final String TYPED = "-typed";
    private static final String NESTED_PREFIX = "nested";
    private static final List<String> TABLES = List.of("table", "nestedTable", "tablePreserved", "nestedTablePreserved",
            "nestedTableOuterPreserved", "nestedTableInnerPreserved", "repeat", "nestedRepeat");
    private static final Set<String> INPUTS_ROW_STATE_PRESERVED = Set.of("tablePreserved", "nestedTablePreserved",
            "nestedTableInnerPreserved");

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:refresh")
    private WebElement refresh;

    @FindBy(id = "form:refreshWithInfo")
    private WebElement refreshWithInfo;

    @FindBy(id = "form:refreshWithErrors")
    private WebElement refreshWithErrors;

    @FindBy(id = "form:refreshWithFatal")
    private WebElement refreshWithFatal;

    @Test
    void testRefreshWithoutErrorsDiscardsTypedValuesUnlessRowStatePreserved() {
        assertRefresh(refresh, false);
    }

    /**
     * A message below severity ERROR is no error.
     */
    @Test
    void testRefreshWithInfoMessageDiscardsTypedValuesUnlessRowStatePreserved() {
        assertRefresh(refreshWithInfo, false);
    }

    @Test
    void testRefreshWithErrorsKeepsTypedValues() {
        assertRefresh(refreshWithErrors, true);
    }

    /**
     * A message above severity ERROR is an error as well.
     */
    @Test
    void testRefreshWithFatalMessageKeepsTypedValues() {
        assertRefresh(refreshWithFatal, true);
    }

    private void assertRefresh(WebElement refreshButton, boolean errors) {
        open("issue6110.xhtml");

        for (String table : TABLES) {
            for (String clientId : modelValues(table).keySet()) {
                browser.findElement(By.id(clientId)).sendKeys(TYPED);
            }
        }

        guardHttp(submit::click);
        assertTables(table -> true);

        guardAjax(refreshButton::click);
        assertTables(table -> errors || INPUTS_ROW_STATE_PRESERVED.contains(table));
    }

    private void assertTables(Predicate<String> typed) {
        List<Executable> assertions = new ArrayList<>();
        for (String table : TABLES) {
            String suffix = typed.test(table) ? TYPED : "";
            for (Map.Entry<String, String> input : modelValues(table).entrySet()) {
                String clientId = input.getKey();
                String expected = input.getValue() + suffix;
                String actual = browser.findElement(By.id(clientId)).getAttribute("value");
                assertions.add(() -> assertEquals(expected, actual, clientId));
            }
        }
        assertAll(assertions);
    }

    /**
     * The client ID of every input in the table, mapped to the initial model value behind it, as set up by
     * {@link Issue6110}.
     */
    private static Map<String, String> modelValues(String table) {
        Map<String, String> modelValues = new LinkedHashMap<>();
        for (int row = 0; row < Issue6110.ROWS; row++) {
            if (table.startsWith(NESTED_PREFIX)) {
                for (int innerRow = 0; innerRow < Issue6110.ROWS; innerRow++) {
                    modelValues.put("form:" + table + ":" + row + ":inner:" + innerRow + ":in", table + row + "-" + innerRow);
                }
            } else {
                modelValues.put("form:" + table + ":" + row + ":in", table + row);
            }
        }
        return modelValues;
    }
}
