package org.eclipse.mojarra.test.issue5464;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue5464IT extends BaseIT {

    @FindBy(id = "form:input")
    private WebElement input;

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:output")
    private WebElement output;

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5464
     */
    @Test
    void testFFFE() {
        open("issue5464.xhtml");
        input.sendKeys("f\uFFFEoo");
        guardAjax(submit::click);
        assertEquals("Result: foo", output.getText());
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/4516
     */
    @Test
    void test000C() {
        open("issue5464.xhtml");
        input.sendKeys("f\u000Coo");
        guardAjax(submit::click);
        assertEquals("Result: foo", output.getText());
    }
}
