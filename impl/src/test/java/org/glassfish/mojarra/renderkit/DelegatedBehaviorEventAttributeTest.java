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

package org.glassfish.mojarra.renderkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.faces.application.Application;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.component.TransientStateHelper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

import org.glassfish.mojarra.renderkit.html_basic.HtmlResponseWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The <code>load</code> and <code>error</code> of an element whose fetch of an external resource starts as the element is parsed can be dispatched before any
 * script following the element runs, so such a handler is carried by a <code>data-mojarra-on*</code> attribute and run by a bootstrap which the writer puts
 * immediately ahead of that very element. Any other behavior event attribute is wired afterwards by <code>mojarra.ael()</code>, which locates its element by
 * the component's client id and therefore needs the element to carry that very id.
 */
class DelegatedBehaviorEventAttributeTest {

    private static final String NONCE = "test-nonce";

    private StringWriter output;
    private HtmlResponseWriter writer;

    @BeforeEach
    void setUpCurrentFacesContext() throws Exception {
        writer = newWriter("text/html");

        ResourceHandler resourceHandler = mock(ResourceHandler.class);
        when(resourceHandler.getCurrentNonce(any(FacesContext.class))).thenReturn(NONCE);

        Application application = mock(Application.class);
        when(application.getResourceHandler()).thenReturn(resourceHandler);

        FacesContext facesContext = mock(FacesContext.class);
        when(facesContext.getApplication()).thenReturn(application);
        when(facesContext.getResponseWriter()).thenReturn(writer);
        when(facesContext.getAttributes()).thenReturn(new HashMap<>());
        setCurrentInstance(facesContext);
        writer.startDocument();
    }

    @AfterEach
    void tearDownCurrentFacesContext() throws Exception {
        setCurrentInstance(null);
    }

    @Test
    void theBootstrapPrecedesTheElementWhichCarriesADelegatedAttribute() throws Exception {
        writeElement("script", attributes("onerror", "fallback()"));

        assertTrue(output.toString().startsWith("<script data-mojarra-independent type=\"text/javascript\" nonce=\"" + NONCE + "\">"), output.toString());
        assertTrue(output.toString().contains(RenderKitUtils.DELEGATED_BEHAVIOR_EVENT_ATTRIBUTE_PREFIX + "onerror=\"fallback()\""), output.toString());
        assertFalse(output.toString().contains(" onerror="), output.toString());
    }

    /**
     * Under XHTML the response is parsed as XML, where the script body is not character data, so it needs the same CDATA section that every other script this
     * writer emits gets.
     */
    @Test
    void theBootstrapIsWrappedInCdataForXhtml() throws Exception {
        writer = newWriter("application/xhtml+xml");
        writer.startDocument();
        writeElement("script", attributes("onerror", "fallback()"));

        assertTrue(output.toString().contains("//<![CDATA[\n" + RenderKitUtils.getBehaviorEventBootstrap() + "\n//]]>"), output.toString());
    }

    /**
     * The element and attribute names keep the case the page author wrote them in, and the delegation rule is about the element and event, not about the
     * spelling.
     */
    @Test
    void delegationIgnoresTheCaseOfTheElementAndAttributeName() throws Exception {
        writeElement("IMG", attributes("onLoad", "init()"));

        assertEquals(1, countBootstraps(), output.toString());
        assertTrue(output.toString().contains(RenderKitUtils.DELEGATED_BEHAVIOR_EVENT_ATTRIBUTE_PREFIX + "onload=\"init()\""), output.toString());
    }

    /**
     * The pass-through localName renames the element, and it is the rendered element which decides whether its handler can outrun the wiring.
     */
    @Test
    void delegationFollowsTheRenamedElement() throws Exception {
        writeElement("div", attributes(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY, "img", "onerror", "fallback()"));

        assertEquals(1, countBootstraps(), output.toString());
        assertTrue(output.toString().contains(RenderKitUtils.DELEGATED_BEHAVIOR_EVENT_ATTRIBUTE_PREFIX + "onerror=\"fallback()\""), output.toString());
    }

    @Test
    void delegationIsRefusedForAnElementRenamedOutOfTheDelegatedSet() throws Exception {
        writeElement("img", attributes(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY, "div", "onerror", "fallback()"));

        assertEquals(0, countBootstraps(), output.toString());
        assertTrue(output.toString().contains("onerror=\"fallback()\""), output.toString());
    }

    @Test
    void theBootstrapIsWrittenOnlyOncePerResponse() throws Exception {
        writeElement("script", attributes("onerror", "fallback()"));
        writeElement("link", attributes("onerror", "fallback()"));

        assertEquals(1, countBootstraps(), output.toString());
    }

    @Test
    void theBootstrapIsWrittenAgainForAResponseWhichIsRenderedAnew() throws Exception {
        writeElement("script", attributes("onerror", "fallback()"));
        writer.startDocument();
        writeElement("script", attributes("onerror", "fallback()"));

        assertEquals(2, countBootstraps(), output.toString());
    }

    @Test
    void anElementWhoseEventCannotOutrunTheWiringIsNotDelegated() throws Exception {
        writeElement("div", attributes("onerror", "fallback()"));

        assertEquals(0, countBootstraps(), output.toString());
        assertTrue(output.toString().contains("onerror=\"fallback()\""), output.toString());
    }

    /**
     * The <code>load</code> of a body is dispatched at the window rather than at the element, where the bootstrap would never see it, so it stays inline.
     */
    @Test
    void aBodyIsNotDelegated() throws Exception {
        writeElement("body", attributes("onload", "init()"));

        assertEquals(0, countBootstraps(), output.toString());
        assertTrue(output.toString().contains("onload=\"init()\""), output.toString());
    }

    @Test
    void anUndelegatedAttributeIsDeferredWhenTheElementCarriesTheComponentClientId() throws Exception {
        writeElement("button", "form:button", mockComponent("form:button", attributes("onclick", "handle()")));

        assertFalse(output.toString().contains("onclick"), output.toString());
    }

    @Test
    void anUndelegatedAttributeIsInlineWhenTheElementIdIsNotTheComponentClientId() throws Exception {
        writeElement("button", null, mockComponent("form:j_id1", attributes("id", "foo", "onclick", "handle()")));

        assertTrue(output.toString().contains("onclick=\"handle()\""), output.toString());
    }

    @Test
    void anUndelegatedAttributeIsInlineAfterAnAddressableElement() throws Exception {
        writeElement("button", "form:button", mockComponent("form:button", attributes("onclick", "handle()")));
        writeElement("button", null, mockComponent("form:j_id1", attributes("onclick", "handle()")));

        assertEquals(1, output.toString().split("onclick", -1).length - 1, output.toString());
    }

    /**
     * Constructs a writer without a current faces context, as its constructor reads that context, which the mock set up here does not serve.
     */
    private HtmlResponseWriter newWriter(String contentType) throws Exception {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        setCurrentInstance(null);
        output = new StringWriter();
        HtmlResponseWriter newWriter = new HtmlResponseWriter(output, contentType, "UTF-8");
        setCurrentInstance(facesContext);
        return newWriter;
    }

    private int countBootstraps() {
        return output.toString().split(Pattern.quote(RenderKitUtils.getBehaviorEventBootstrap()), -1).length - 1;
    }

    private void writeElement(String elementName, Map<String, Object> passThroughAttributes) throws Exception {
        writeElement(elementName, null, mockComponent("j_id1", passThroughAttributes));
    }

    private void writeElement(String elementName, String id, UIComponent component) throws Exception {
        writer.startElement(elementName, component);

        if (id != null) {
            writer.writeAttribute("id", id, "id");
        }

        writer.endElement(elementName);
        writer.flush();
    }

    private static Map<String, Object> attributes(String... namesAndValues) {
        Map<String, Object> attributes = new LinkedHashMap<>();

        for (int i = 0; i < namesAndValues.length; i += 2) {
            attributes.put(namesAndValues[i], namesAndValues[i + 1]);
        }

        return attributes;
    }

    private static UIComponent mockComponent(String clientId, Map<String, Object> passThroughAttributes) {
        // A deferred handler is parked in the component's transient state, so that has to hold what is put into it.
        Map<Object, Object> transientState = new HashMap<>();
        TransientStateHelper transientStateHelper = mock(TransientStateHelper.class);
        when(transientStateHelper.getTransient(any())).thenAnswer(invocation -> transientState.get(invocation.getArgument(0)));
        when(transientStateHelper.putTransient(any(), any()))
            .thenAnswer(invocation -> transientState.put(invocation.getArgument(0), invocation.getArgument(1)));

        UIComponent component = mock(UIComponent.class);
        when(component.getClientId(any(FacesContext.class))).thenReturn(clientId);
        when(component.getPassThroughAttributes(false)).thenReturn(passThroughAttributes);
        when(component.getTransientStateHelper()).thenReturn(transientStateHelper);
        return component;
    }

    private static void setCurrentInstance(FacesContext facesContext) throws Exception {
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, facesContext);
    }

}
