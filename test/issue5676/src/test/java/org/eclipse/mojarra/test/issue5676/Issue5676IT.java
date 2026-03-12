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
package org.eclipse.mojarra.test.issue5676;

import static java.net.URI.create;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue5676IT extends BaseIT {

    @FindBy(id = "webjars")
    private WebElement webjars;

    @FindBy(id = "pftheme")
    private WebElement pftheme;

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5676
     */
    @Test
    void test() {
        open("issue5676.xhtml");
        assertEquals(getContextPath() + "/jakarta.faces.resource/font-awesome/7.2.0/webfonts/fa-regular-400.woff2.xhtml?ln=webjars", webjars.getText());
        assertEquals(getContextPath() + "/jakarta.faces.resource/images/ui-bg_flat_75_ffffff_40x100.png.xhtml?ln=primefaces-casablanca", pftheme.getText());

        try {
            var css = newHttpClient().send(newBuilder(create(getUrl("jakarta.faces.resource/issue5676.css.xhtml"))).build(), ofString()).body();
            assertTrue(css.contains("content: \"" + webjars.getText() + "\";"));
            assertTrue(css.contains("background: url(\"" + pftheme.getText() + "\");"));
        }
        catch (Exception e) {
            fail(e);
        }
    }

}
