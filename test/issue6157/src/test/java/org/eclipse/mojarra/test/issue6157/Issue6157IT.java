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
package org.eclipse.mojarra.test.issue6157;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class Issue6157IT extends BaseIT {

    private static final String PAGE = "issue6157.xhtml";
    private static final String SYNTAX_PAGE = "issue6157Syntax.xhtml";

    /**
     * An f:event listener is invoked with the event when its method takes the event, without the event when its method
     * takes no arguments, and with the event when the bean has both methods. A listener from a composite component
     * attribute whose method-signature takes the event is invoked with the event, and one whose method-signature takes
     * no arguments is invoked without the event. A listener from a composite component attribute whose method-signature
     * takes the event, and whose bean has both methods, is invoked with the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testEventListenerSignatures() {
        open("issue6157Event.xhtml");
        assertEquals("eventNoArg() eventOneArg(event) eventBoth(event) eventComposite(event) eventCompositeNoArg() eventBoth(event)",
                getInvoked("event"));
    }

    /**
     * An f:ajax listener whose method takes no arguments is invoked without the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerTakingNoArguments() {
        assertEquals("ajaxNoArg()", clickAndGetInvoked(PAGE, "noArg"));
    }

    /**
     * An f:ajax listener whose method takes the event is invoked with the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerTakingEvent() {
        assertEquals("ajaxOneArg(event)", clickAndGetInvoked(PAGE, "oneArg"));
    }

    /**
     * An f:ajax listener whose bean has a method taking the event and one taking no arguments invokes the one taking the
     * event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerWithBothMethodsTakesEvent() {
        assertEquals("ajaxBoth(event)", clickAndGetInvoked(PAGE, "both"));
    }

    /**
     * An f:ajax listener from a composite component attribute whose method-signature takes the event is invoked with the
     * event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerFromCompositeAttributeTakesEvent() {
        assertEquals("ajaxComposite(event)", clickAndGetInvoked(PAGE, "composite:button"));
    }

    /**
     * An f:ajax listener from a composite component attribute whose method-signature takes no arguments is invoked
     * without the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerFromCompositeAttributeTakingNoArguments() {
        assertEquals("ajaxCompositeNoArg()", clickAndGetInvoked(PAGE, "compositeNoArg:button"));
    }

    /**
     * An actionListener whose method takes no arguments is invoked without the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerTakingNoArguments() {
        assertEquals("actionNoArg()", submitAndGetInvoked(PAGE, "action_noArg", "button"));
    }

    /**
     * An actionListener whose method takes the event is invoked with the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerTakingEvent() {
        assertEquals("actionOneArg(event)", submitAndGetInvoked(PAGE, "action_oneArg", "button"));
    }

    /**
     * An actionListener whose bean has a method taking the event and one taking no arguments invokes the one taking the
     * event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerWithBothMethodsTakesEvent() {
        assertEquals("actionBoth(event)", submitAndGetInvoked(PAGE, "action_both", "button"));
    }

    /**
     * An actionListener retargeted from a composite component attribute to a method taking no arguments is invoked
     * without the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerFromCompositeAttributeTakingNoArguments() {
        assertEquals("actionNoArg()", submitAndGetInvoked(PAGE, "action_composite_noArg", "composite:button"));
    }

    /**
     * An actionListener retargeted from a composite component attribute to a method taking the event is invoked with the
     * event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerFromCompositeAttributeTakingEvent() {
        assertEquals("actionOneArg(event)", submitAndGetInvoked(PAGE, "action_composite_oneArg", "composite:button"));
    }

    /**
     * A valueChangeListener whose method takes no arguments is invoked without the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerTakingNoArguments() {
        assertEquals("valueNoArg()", changeAndGetInvoked(PAGE, "value_noArg", "input"));
    }

    /**
     * A valueChangeListener whose method takes the event is invoked with the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerTakingEvent() {
        assertEquals("valueOneArg(event)", changeAndGetInvoked(PAGE, "value_oneArg", "input"));
    }

    /**
     * A valueChangeListener whose bean has a method taking the event and one taking no arguments invokes the one taking
     * the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerWithBothMethodsTakesEvent() {
        assertEquals("valueBoth(event)", changeAndGetInvoked(PAGE, "value_both", "input"));
    }

    /**
     * A valueChangeListener retargeted from a composite component attribute to a method taking no arguments is invoked
     * without the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerFromCompositeAttributeTakingNoArguments() {
        assertEquals("valueNoArg()", changeAndGetInvoked(PAGE, "value_composite_noArg", "composite:input"));
    }

    /**
     * A valueChangeListener retargeted from a composite component attribute to a method taking the event is invoked with
     * the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerFromCompositeAttributeTakingEvent() {
        assertEquals("valueOneArg(event)", changeAndGetInvoked(PAGE, "value_composite_oneArg", "composite:input"));
    }

    /**
     * An f:event listener invokes a method call with parentheses, also when the bean has a method taking the event too,
     * and with the component or a literal as argument. It invokes a method named in brackets, a method taking a
     * supertype of the event or Object with the event, a method returning a value, a method taking varargs with the
     * event, the method of a bean from a ui:param, a tag file attribute or a composite component attribute, a method
     * call given to a composite component method attribute, and a method call with the var of c:forEach and ui:repeat.
     * It does so for preRenderView in f:metadata and for postAddToView too.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testEventListenerSyntaxes() {
        open("issue6157EventSyntax.xhtml");
        assertEquals("eventNoArg() noArg()"
                + " eventNoArg() eventBoth() withComponent(component) withString(x)"
                + " eventNoArg() withSystemEvent(event) withObject(event) returnsString() varargs(1)"
                + " noArg() noArg() noArg() withString(composite)"
                + " withItem(r0) withItem(r1) withItem(r0) withItem(r1)", getInvoked("event"));
    }

    /**
     * An f:event listener of a ui:repeat over items of different classes invokes the method taking the event of an item
     * which has both methods, also after it invoked the method taking no arguments of an item which has only that one.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testEventListenerOfMixedItemsTakesEventWhenItemHasBoth() {
        open("issue6157EventMixed.xhtml");
        assertEquals("A.onEvent() B.onEvent(event)", getInvoked("event"));
    }

    /**
     * An f:ajax listener with a method call invokes that method: without arguments, also when the bean has a method
     * taking the event too, with the component, and with a literal.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerMethodCalls() {
        assertAll(
                () -> assertEquals("ajaxNoArg()", clickAndGetInvoked(SYNTAX_PAGE, "noArgParens")),
                () -> assertEquals("ajaxBoth()", clickAndGetInvoked(SYNTAX_PAGE, "bothParens")),
                () -> assertEquals("withComponent(component)", clickAndGetInvoked(SYNTAX_PAGE, "component")),
                () -> assertEquals("withString(x)", clickAndGetInvoked(SYNTAX_PAGE, "string")));
    }

    /**
     * An f:ajax listener invokes a method named in brackets, a method taking a supertype of the event or Object with the
     * event, a method returning a value, and a method taking varargs with the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerMethodReferences() {
        assertAll(
                () -> assertEquals("ajaxNoArg()", clickAndGetInvoked(SYNTAX_PAGE, "bracket")),
                () -> assertEquals("withFacesEvent(event)", clickAndGetInvoked(SYNTAX_PAGE, "facesEvent")),
                () -> assertEquals("withObject(event)", clickAndGetInvoked(SYNTAX_PAGE, "object")),
                () -> assertEquals("returnsString()", clickAndGetInvoked(SYNTAX_PAGE, "returnsString")),
                () -> assertEquals("varargs(1)", clickAndGetInvoked(SYNTAX_PAGE, "varargs")));
    }

    /**
     * An f:ajax listener invokes the method of a bean from a ui:param, from a tag file attribute and from a composite
     * component attribute, and the method call given to a composite component method attribute.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerFromVariables() {
        assertAll(
                () -> assertEquals("noArg()", clickAndGetInvoked(SYNTAX_PAGE, "include")),
                () -> assertEquals("noArg()", clickAndGetInvoked(SYNTAX_PAGE, "tag")),
                () -> assertEquals("noArg()", clickAndGetInvoked(SYNTAX_PAGE, "bean:button")),
                () -> assertEquals("ajaxNoArg()", clickAndGetInvoked(SYNTAX_PAGE, "compositeParens:button")));
    }

    /**
     * An f:ajax listener with a method call taking the var of c:forEach, ui:repeat and h:dataTable invokes it with the
     * item of the clicked row.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerInIterations() {
        assertAll(
                () -> assertEquals("withItem(r1)", clickAndGetInvoked(SYNTAX_PAGE, "forEach1")),
                () -> assertEquals("withItem(r1)", clickAndGetInvoked(SYNTAX_PAGE, "repeat:1:button")),
                () -> assertEquals("withItem(r1)", clickAndGetInvoked(SYNTAX_PAGE, "table:1:button")));
    }

    /**
     * An f:ajax listener wrapping its component, and one attached to a cc:clientBehavior of a composite component,
     * invoke their method taking no arguments.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerWrappingOrRetargeted() {
        assertAll(
                () -> assertEquals("ajaxNoArg()", clickAndGetInvoked(SYNTAX_PAGE, "wrapped")),
                () -> assertEquals("ajaxNoArg()", clickAndGetInvoked(SYNTAX_PAGE, "behavior:button")));
    }

    /**
     * An f:ajax listener from a composite component attribute whose method-signature takes the event, and whose bean has
     * both methods, is invoked with the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerFromCompositeAttributeWithBothMethodsTakesEvent() {
        assertEquals("ajaxBoth(event)", clickAndGetInvoked(SYNTAX_PAGE, "compositeBoth:button"));
    }

    /**
     * An f:ajax listener of a ui:repeat over items of different classes invokes the method taking the event of an item
     * which has both methods, also after it invoked the method taking no arguments of an item which has only that one.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testAjaxListenerOfMixedItemsTakesEventWhenItemHasBoth() {
        assertEquals("A.onAjax()", clickAndGetInvoked(SYNTAX_PAGE, "mixed:0:button"));
        guardAjax(browser.findElement(By.id("ajax:mixed:1:button"))::click);
        assertEquals("B.onAjax(event)", getInvoked("ajax"));
    }

    /**
     * An actionListener with a method call invokes that method: without arguments, also when the bean has a method
     * taking the event too, with the component, and with a literal.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerMethodCalls() {
        assertAll(
                () -> assertEquals("actionNoArg()", submitAndGetInvoked(SYNTAX_PAGE, "action", "noArgParens")),
                () -> assertEquals("actionBoth()", submitAndGetInvoked(SYNTAX_PAGE, "action", "bothParens")),
                () -> assertEquals("withComponent(component)", submitAndGetInvoked(SYNTAX_PAGE, "action", "component")),
                () -> assertEquals("withString(x)", submitAndGetInvoked(SYNTAX_PAGE, "action", "string")));
    }

    /**
     * An actionListener invokes a method named in brackets, a method taking a supertype of the event or Object with the
     * event, a method returning a value, and a method taking varargs with the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerMethodReferences() {
        assertAll(
                () -> assertEquals("actionNoArg()", submitAndGetInvoked(SYNTAX_PAGE, "action", "bracket")),
                () -> assertEquals("withFacesEvent(event)", submitAndGetInvoked(SYNTAX_PAGE, "action", "facesEvent")),
                () -> assertEquals("withObject(event)", submitAndGetInvoked(SYNTAX_PAGE, "action", "object")),
                () -> assertEquals("returnsString()", submitAndGetInvoked(SYNTAX_PAGE, "action", "returnsString")),
                () -> assertEquals("varargs(1)", submitAndGetInvoked(SYNTAX_PAGE, "action", "varargs")));
    }

    /**
     * An actionListener invokes the method of a bean from a ui:param, from a tag file attribute and from a composite
     * component attribute, and the method call retargeted from a composite component attribute.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerFromVariables() {
        assertAll(
                () -> assertEquals("noArg()", submitAndGetInvoked(SYNTAX_PAGE, "action", "include")),
                () -> assertEquals("noArg()", submitAndGetInvoked(SYNTAX_PAGE, "action", "tag")),
                () -> assertEquals("noArg()", submitAndGetInvoked(SYNTAX_PAGE, "action", "bean:button")),
                () -> assertEquals("actionNoArg()", submitAndGetInvoked(SYNTAX_PAGE, "action", "compositeParens:button")));
    }

    /**
     * An actionListener with a method call taking the var of c:forEach, ui:repeat and h:dataTable invokes it with the
     * item of the clicked row.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerInIterations() {
        assertAll(
                () -> assertEquals("withItem(r1)", submitAndGetInvoked(SYNTAX_PAGE, "action", "forEach1")),
                () -> assertEquals("withItem(r1)", submitAndGetInvoked(SYNTAX_PAGE, "action", "repeat:1:button")),
                () -> assertEquals("withItem(r1)", submitAndGetInvoked(SYNTAX_PAGE, "action", "table:1:button")));
    }

    /**
     * An f:actionListener whose binding is a method call invokes that method.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerBindingMethodCall() {
        assertEquals("binding()", submitAndGetInvoked(SYNTAX_PAGE, "action", "binding"));
    }

    /**
     * An actionListener of a ui:repeat over items of different classes invokes the method taking the event of an item
     * which has both methods, also after it invoked the method taking no arguments of an item which has only that one.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testActionListenerOfMixedItemsTakesEventWhenItemHasBoth() {
        assertEquals("A.onAction()", submitAndGetInvoked(SYNTAX_PAGE, "action", "mixed:0:button"));
        guardHttp(browser.findElement(By.id("action:mixed:1:button"))::click);
        assertEquals("B.onAction(event)", getInvoked("action"));
    }

    /**
     * A valueChangeListener with a method call invokes that method: without arguments, also when the bean has a method
     * taking the event too, with the component, and with a literal.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerMethodCalls() {
        assertAll(
                () -> assertEquals("valueNoArg()", changeAndGetInvoked(SYNTAX_PAGE, "value_noArgParens", "input")),
                () -> assertEquals("valueBoth()", changeAndGetInvoked(SYNTAX_PAGE, "value_bothParens", "input")),
                () -> assertEquals("withComponent(input)", changeAndGetInvoked(SYNTAX_PAGE, "value_component", "input")),
                () -> assertEquals("withString(x)", changeAndGetInvoked(SYNTAX_PAGE, "value_string", "input")));
    }

    /**
     * A valueChangeListener invokes a method named in brackets, a method taking a supertype of the event or Object with
     * the event, a method returning a value, and a method taking varargs with the event.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerMethodReferences() {
        assertAll(
                () -> assertEquals("valueNoArg()", changeAndGetInvoked(SYNTAX_PAGE, "value_bracket", "input")),
                () -> assertEquals("withFacesEvent(event)", changeAndGetInvoked(SYNTAX_PAGE, "value_facesEvent", "input")),
                () -> assertEquals("withObject(event)", changeAndGetInvoked(SYNTAX_PAGE, "value_object", "input")),
                () -> assertEquals("returnsString()", changeAndGetInvoked(SYNTAX_PAGE, "value_returnsString", "input")),
                () -> assertEquals("varargs(1)", changeAndGetInvoked(SYNTAX_PAGE, "value_varargs", "input")));
    }

    /**
     * A valueChangeListener invokes the method taking no arguments of a bean from a ui:param, from a tag file attribute
     * and from a composite component attribute, and the method call retargeted from a composite component attribute.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerFromVariables() {
        assertAll(
                () -> assertEquals("noArg()", changeAndGetInvoked(SYNTAX_PAGE, "value_include", "input")),
                () -> assertEquals("noArg()", changeAndGetInvoked(SYNTAX_PAGE, "value_tag", "input")),
                () -> assertEquals("noArg()", changeAndGetInvoked(SYNTAX_PAGE, "value_bean", "bean:input")),
                () -> assertEquals("valueNoArg()", changeAndGetInvoked(SYNTAX_PAGE, "value_compositeParens", "compositeParens:input")));
    }

    /**
     * A valueChangeListener with a method call taking the var of c:forEach, ui:repeat and h:dataTable invokes it with the
     * item of each changed row.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerInIterations() {
        assertAll(
                () -> assertEquals("withItem(r0) withItem(r1)", changeAndGetInvoked(SYNTAX_PAGE, "value_forEach", "input0", "input1")),
                () -> assertEquals("withItem(r0) withItem(r1)", changeAndGetInvoked(SYNTAX_PAGE, "value_repeat", "repeat:0:input", "repeat:1:input")),
                () -> assertEquals("withItem(r0) withItem(r1)", changeAndGetInvoked(SYNTAX_PAGE, "value_table", "table:0:input", "table:1:input")));
    }

    /**
     * An f:valueChangeListener whose binding is a method call invokes that method.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerBindingMethodCall() {
        assertEquals("binding()", changeAndGetInvoked(SYNTAX_PAGE, "value_binding", "input"));
    }

    /**
     * A valueChangeListener of a ui:repeat over items of different classes invokes the method taking the event of an
     * item which has both methods, also after it invoked the method taking no arguments of an item which has only that
     * one.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6157">issue 6157</a>
     */
    @Test
    void testValueChangeListenerOfMixedItemsTakesEventWhenItemHasBoth() {
        assertEquals("A.onValueChange() B.onValueChange(event)", changeAndGetInvoked(SYNTAX_PAGE, "value_mixed", "mixed:0:input", "mixed:1:input"));
    }

    private String submitAndGetInvoked(String page, String form, String button) {
        open(page);
        guardHttp(browser.findElement(By.id(form + ":" + button))::click);
        return getInvoked(form);
    }

    private String changeAndGetInvoked(String page, String form, String... inputs) {
        open(page);

        for (String input : inputs) {
            browser.findElement(By.id(form + ":" + input)).sendKeys("changed");
        }

        guardHttp(browser.findElement(By.id(form + ":button"))::click);
        return getInvoked(form);
    }

    private String clickAndGetInvoked(String page, String button) {
        open(page);
        guardAjax(browser.findElement(By.id("ajax:" + button))::click);
        return getInvoked("ajax");
    }

    private String getInvoked(String form) {
        return browser.findElement(By.id(form + ":invoked")).getText();
    }
}
