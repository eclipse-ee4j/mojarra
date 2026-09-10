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
package org.eclipse.mojarra.test.spec1904;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * An attribute declared as required must be supplied where the tag is used, whether it is declared on a tag file in a
 * tag library descriptor or on a composite component, which this application asks to be told about by running in the
 * Development project stage.
 *
 * @see <a href="https://github.com/jakartaee/faces/issues/1904">faces issue 1904</a>
 */
class Spec1904IT extends BaseIT {

    @FindBy(id = "supplied")
    private WebElement supplied;

    @FindBy(id = "compositeSupplied")
    private WebElement compositeSupplied;

    @Test
    void testTagFileRendersWhenTheRequiredAttributeIsSupplied() {
        open("supplied.xhtml");
        assertEquals("[R|O]", supplied.getText(), "Both attributes reach the tag file under the names they were declared with");
    }

    @Test
    void testUsingTheTagWithoutTheRequiredAttributeFails() {
        String body = getResponseBody("omitted.xhtml");
        assertTrue(!body.contains("[|O]") && body.contains("t:tagFile") && body.contains("requiredAttribute"), "Omitting an attribute the tag library declares as required must fail the page and identify both the tag and the attribute, instead of rendering it unset: " + body);
    }

    @Test
    void testCompositeComponentRendersWhenTheRequiredAttributeIsSupplied() {
        open("compositeSupplied.xhtml");
        assertEquals("[R|O]", compositeSupplied.getText(), "Both attributes reach the composite component under the names they were declared with");
    }

    @Test
    void testUsingTheCompositeComponentWithoutTheRequiredAttributeFails() {
        String body = getResponseBody("compositeOmitted.xhtml");
        assertTrue(!body.contains("[|O]") && body.contains("cc:compositeComponent") && body.contains("requiredAttribute"), "Omitting an attribute a composite component declares as required must fail the page and identify both the tag and the attribute, instead of rendering it unset: " + body);
    }
}
