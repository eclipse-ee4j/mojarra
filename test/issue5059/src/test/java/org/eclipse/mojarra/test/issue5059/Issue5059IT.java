/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue5059;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class Issue5059IT extends BaseIT {

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5059
     */
    @Test
    void test() {
        open("issue5059.xhtml");
        assertStyleAttributePresent();

        // submit form, the style attribute should be there
        submitForm();
        assertStyleAttributePresent();

        // submit form once more, the style should still be there, this did not happen before 5059 fix
        submitForm();
        assertStyleAttributePresent();

        // yet once more, just to be on the safe side
        submitForm();
        assertStyleAttributePresent();
    }

    private void submitForm() {
        guardHttp(browser.findElement(By.id("form:submit"))::click);
    }

    private void assertStyleAttributePresent() {
        String style = browser.findElement(By.id("form")).getAttribute("style");
        assertTrue(style != null && !style.trim().isEmpty());
    }

}
