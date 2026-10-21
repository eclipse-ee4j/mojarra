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
package org.eclipse.mojarra.test.issue6075;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue6075IT extends BaseIT {

    @FindBy(id = "form:add")
    private WebElement add;

    @FindBy(id = "form:show")
    private WebElement show;

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:container")
    private WebElement container;

    /**
     * A component without an id of its own is added in an action, and a facet with more than one component, whose panel
     * resolves its client id while the view is built, appears afterwards. The panel of the facet must not take the id of
     * the added component when the postback rebuilds the view, so that the view can be saved and rendered again.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6075
     */
    @Test
    void testImplicitFacetPanelDoesNotTakeIdOfAddedComponent() {
        assertAddedComponentAndFacetSurvivePostback("issue6075.xhtml");
    }

    /**
     * As {@link #testImplicitFacetPanelDoesNotTakeIdOfAddedComponent()}, with three more components in the region which
     * holds the facet. The id of the panel of the facet must not depend on how many other components that region holds.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6075
     */
    @Test
    void testImplicitFacetPanelInLargerRegionDoesNotTakeIdOfAddedComponent() {
        assertAddedComponentAndFacetSurvivePostback("issue6075Region.xhtml");
    }

    private void assertAddedComponentAndFacetSurvivePostback(String page) {
        open(page);
        guardHttp(add::click);
        guardHttp(show::click);
        guardHttp(submit::click);
        assertEquals("added1", container.getText(), "added component after postback");
        assertEquals("footer1footer2", browser.findElement(By.id("form:grid")).getText(), "facet components after postback");
    }
}
