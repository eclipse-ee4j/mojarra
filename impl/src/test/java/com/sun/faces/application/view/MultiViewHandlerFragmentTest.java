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

package com.sun.faces.application.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.sun.faces.junit.JUnitFacesTestCaseBase;

/**
 * Tests for the URL fragment support in
 * {@link MultiViewHandler#getRedirectURL(FacesContext, String, Map, String, boolean)} and
 * {@link MultiViewHandler#getBookmarkableURL(FacesContext, String, Map, String, boolean)}.
 *
 * <h2>Strategy</h2>
 * <p>{@link MultiViewHandler}'s constructor calls {@code FactoryFinder} for the
 * {@code ViewDeclarationLanguageFactory} and reads an init parameter from
 * {@code FacesContext.getCurrentInstance()}.  Both preconditions are satisfied by
 * extending {@link JUnitFacesTestCaseBase} (which installs a mock {@link FacesContext}
 * and the core Faces factories) and registering the concrete
 * {@code ViewDeclarationLanguageFactory} implementation in an additional
 * {@link BeforeEach} method that runs after the base-class setup.</p>
 *
 * <p>The {@link FacesContext} passed to the methods under test is a separate Mockito mock
 * so that every dependency of the methods ({@code getResponseEncoding}, {@code getActionURL},
 * {@code encodeRedirectURL}, {@code encodeBookmarkableURL}, …) can be controlled precisely
 * without disturbing the mock context that the constructor already consumed.</p>
 *
 * <h2>What is verified</h2>
 * <ul>
 *   <li>When a non-{@code null} fragment is supplied, {@code "#" + fragment} is appended to
 *       the action URL <em>before</em> the URL is handed to
 *       {@code ExternalContext.encodeRedirectURL} / {@code encodeBookmarkableURL}.</li>
 *   <li>When the fragment is {@code null}, no {@code '#'} character is appended to the URL.</li>
 *   <li>URL parameters and fragments coexist correctly.</li>
 *   <li>The 4-arg (no-fragment) overloads delegate to the 5-arg overloads with a {@code null}
 *       fragment, producing the same result as calling the 5-arg form with {@code null}.</li>
 * </ul>
 */
class MultiViewHandlerFragmentTest extends JUnitFacesTestCaseBase {

    private static final String VIEW_ID         = "/target.xhtml";
    private static final String BASE_ACTION_URL = "/ctx/target.xhtml";

    private MultiViewHandler handler;

    // Mockito doubles used for every individual test
    private FacesContext    testContext;
    private Application     testApplication;
    private ViewHandler     testViewHandler;
    private ExternalContext testExternalContext;

    /**
     * Registers the {@code ViewDeclarationLanguageFactory} — which the
     * {@link MultiViewHandler} constructor requires — and creates the instance under test.
     *
     * <p>JUnit 5 guarantees that a superclass {@code @BeforeEach} runs before the subclass
     * {@code @BeforeEach}, so by the time this method executes the full mock Faces
     * environment from {@link JUnitFacesTestCaseBase#setUp()} is already in place.</p>
     */
    @BeforeEach
    void setUpHandler() {
        FactoryFinder.setFactory(
                FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY,
                "com.sun.faces.application.view.ViewDeclarationLanguageFactoryImpl");

        handler = new MultiViewHandler();
    }

    /**
     * Builds a Mockito-based {@link FacesContext} that provides just enough
     * collaboration for the methods under test to run:
     *
     * <ul>
     *   <li>No view root → {@code getResponseEncoding} will fall through to the
     *       hard-coded UTF-8 default without needing a session or request encoding.</li>
     *   <li>{@code encodeRedirectURL}, {@code encodeBookmarkableURL}, and
     *       {@code encodeActionURL} are stubs that return their first string argument
     *       unchanged, so assertions can be made on raw URL values.</li>
     *   <li>{@code getActionURL} returns {@value #BASE_ACTION_URL} for {@value #VIEW_ID}.</li>
     * </ul>
     */
    @BeforeEach
    void setUpTestContext() {
        testContext         = Mockito.mock(FacesContext.class);
        testApplication     = Mockito.mock(Application.class);
        testViewHandler     = Mockito.mock(ViewHandler.class);
        testExternalContext = Mockito.mock(ExternalContext.class);

        when(testContext.getApplication()).thenReturn(testApplication);
        when(testApplication.getViewHandler()).thenReturn(testViewHandler);
        when(testContext.getExternalContext()).thenReturn(testExternalContext);

        // getResponseEncoding: no view root → skip view-root lookup
        when(testContext.getViewRoot()).thenReturn(null);
        // getResponseEncoding: no encoding key in context attributes → fall through
        when(testContext.getAttributes()).thenReturn(new HashMap<>());
        // getResponseEncoding: no request encoding → fall through to UTF-8 default
        when(testExternalContext.getRequestCharacterEncoding()).thenReturn(null);
        // getResponseEncoding: no session → skip session encoding lookup
        when(testExternalContext.getSession(false)).thenReturn(null);

        // getActionURL
        when(testViewHandler.getActionURL(testContext, VIEW_ID)).thenReturn(BASE_ACTION_URL);

        // encode stubs: return first String argument unchanged
        when(testExternalContext.encodeRedirectURL(anyString(), any())).thenAnswer(
                inv -> inv.getArgument(0));
        when(testExternalContext.encodeBookmarkableURL(anyString(), any())).thenAnswer(
                inv -> inv.getArgument(0));
        when(testExternalContext.encodeActionURL(anyString())).thenAnswer(
                inv -> inv.getArgument(0));

        // getProtectedViewsUnmodifiable (used by getActionURL inside the view-protection branch)
        when(testApplication.getViewHandler()).thenReturn(testViewHandler);
        when(testViewHandler.getProtectedViewsUnmodifiable()).thenReturn(java.util.Collections.emptySet());
    }

    // ======================================================================
    // getRedirectURL — fragment behaviour
    // ======================================================================

    /**
     * When a non-{@code null} fragment is supplied, {@code getRedirectURL} must append
     * {@code "#" + fragment} to the action URL <em>before</em> passing it to
     * {@code ExternalContext.encodeRedirectURL}.
     */
    @Test
    void getRedirectURL_withFragment_fragmentAppendedToActionUrlBeforeEncoding() {
        String result = handler.getRedirectURL(testContext, VIEW_ID, null, "section1", false);

        assertTrue(result.contains("#section1"),
                "Result must contain the fragment appended with '#'");
        assertTrue(result.startsWith(BASE_ACTION_URL),
                "Fragment must be appended after the action URL, not prepended");

        // Verify the fragment was part of the URL passed to encodeRedirectURL
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(testExternalContext).encodeRedirectURL(urlCaptor.capture(), any());
        assertTrue(urlCaptor.getValue().endsWith("#section1"),
                "encodeRedirectURL must receive the URL with the fragment already appended");
    }

    /**
     * When the fragment is {@code null}, {@code getRedirectURL} must not append any
     * {@code '#'} character to the URL.
     */
    @Test
    void getRedirectURL_withNullFragment_noHashAppended() {
        String result = handler.getRedirectURL(testContext, VIEW_ID, null, null, false);

        assertFalse(result.contains("#"),
                "Result must not contain '#' when fragment is null");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(testExternalContext).encodeRedirectURL(urlCaptor.capture(), any());
        assertFalse(urlCaptor.getValue().contains("#"),
                "encodeRedirectURL must not receive a '#' in the URL when fragment is null");
    }

    /**
     * The 4-arg overload (without explicit fragment parameter) must behave identically to
     * calling the 5-arg overload with a {@code null} fragment.
     */
    @Test
    void getRedirectURL_noFragmentOverload_delegatesToNullFragment() {
        String withNull    = handler.getRedirectURL(testContext, VIEW_ID, null, (String) null, false);
        String withOverload = handler.getRedirectURL(testContext, VIEW_ID, null, false);

        assertFalse(withNull.contains("#"));
        assertFalse(withOverload.contains("#"));
    }

    /**
     * An empty-string fragment must still cause {@code '#'} to be appended, because the
     * contract is {@code fragment != null → append}, which an empty string satisfies.
     */
    @Test
    void getRedirectURL_withEmptyStringFragment_hashAppended() {
        String result = handler.getRedirectURL(testContext, VIEW_ID, null, "", false);

        assertTrue(result.endsWith("#"),
                "An empty-string fragment must still produce a trailing '#'");
    }

    /**
     * URL parameters and a fragment must coexist: the parameters are decoded (then
     * re-encoded by {@code encodeRedirectURL}) and the fragment is appended to the
     * action URL independently.
     */
    @Test
    void getRedirectURL_withParametersAndFragment_bothHonoured() {
        Map<String, List<String>> params = Map.of("page", List.of("2"));

        String result = handler.getRedirectURL(testContext, VIEW_ID, params, "top", false);

        assertTrue(result.contains("#top"),
                "Fragment must be present in the result even when parameters are also supplied");

        // encodeRedirectURL must be called with the URL that already carries the fragment
        ArgumentCaptor<String> urlCaptor  = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, List<String>>> paramsCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(testExternalContext).encodeRedirectURL(urlCaptor.capture(), paramsCaptor.capture());

        assertTrue(urlCaptor.getValue().endsWith("#top"),
                "Fragment must be part of the URL passed to encodeRedirectURL");
        assertTrue(paramsCaptor.getValue().containsKey("page"),
                "Parameters must be forwarded to encodeRedirectURL");
    }

    /**
     * Encoded parameter values must be URL-decoded before being forwarded to
     * {@code encodeRedirectURL} (the method decodes values using the response encoding).
     * The fragment must remain unaffected by this decoding step.
     */
    @Test
    void getRedirectURL_withEncodedParamValueAndFragment_paramDecodedFragmentPreserved() {
        // "hello+world" URL-decodes to "hello world"
        Map<String, List<String>> params = Map.of("q", List.of("hello+world"));

        handler.getRedirectURL(testContext, VIEW_ID, params, "footer", false);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, List<String>>> paramsCaptor =
                ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(testExternalContext).encodeRedirectURL(urlCaptor.capture(), paramsCaptor.capture());

        assertEquals("hello world", paramsCaptor.getValue().get("q").get(0),
                "URL-encoded parameter value must be decoded before being forwarded");
        assertTrue(urlCaptor.getValue().endsWith("#footer"),
                "Fragment must be preserved after parameter decoding");
    }

    /**
     * A fragment containing special characters (e.g. a colon used in ARIA / scroll
     * targets) must be passed through verbatim — {@code MultiViewHandler} does not
     * encode the fragment itself, leaving that responsibility to the external context.
     */
    @Test
    void getRedirectURL_withSpecialCharsInFragment_passedThroughVerbatim() {
        String result = handler.getRedirectURL(testContext, VIEW_ID, null, "id:main", false);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(testExternalContext).encodeRedirectURL(urlCaptor.capture(), any());
        assertTrue(urlCaptor.getValue().endsWith("#id:main"),
                "Fragment with special characters must be passed to encodeRedirectURL verbatim");
    }

    // ======================================================================
    // getBookmarkableURL — fragment behaviour
    // ======================================================================

    /**
     * When a non-{@code null} fragment is supplied, {@code getBookmarkableURL} must append
     * {@code "#" + fragment} to the action URL before passing it to
     * {@code ExternalContext.encodeBookmarkableURL}.
     */
    @Test
    void getBookmarkableURL_withFragment_fragmentAppendedBeforeEncoding() {
        String result = handler.getBookmarkableURL(testContext, VIEW_ID, null, "hero", false);

        assertTrue(result.contains("#hero"),
                "Result must contain the fragment appended with '#'");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(testExternalContext).encodeBookmarkableURL(urlCaptor.capture(), any());
        assertTrue(urlCaptor.getValue().endsWith("#hero"),
                "encodeBookmarkableURL must receive the URL with the fragment already appended");
    }

    /**
     * When the fragment is {@code null}, {@code getBookmarkableURL} must not append any
     * {@code '#'} to the URL.
     */
    @Test
    void getBookmarkableURL_withNullFragment_noHashAppended() {
        String result = handler.getBookmarkableURL(testContext, VIEW_ID, null, null, false);

        assertFalse(result.contains("#"),
                "Result must not contain '#' when fragment is null");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(testExternalContext).encodeBookmarkableURL(urlCaptor.capture(), any());
        assertFalse(urlCaptor.getValue().contains("#"),
                "encodeBookmarkableURL must not receive a '#' in the URL when fragment is null");
    }

    /**
     * The 4-arg overload (without explicit fragment parameter) must behave identically to
     * calling the 5-arg overload with a {@code null} fragment.
     */
    @Test
    void getBookmarkableURL_noFragmentOverload_delegatesToNullFragment() {
        String withNull     = handler.getBookmarkableURL(testContext, VIEW_ID, null, (String) null, false);
        String withOverload = handler.getBookmarkableURL(testContext, VIEW_ID, null, false);

        assertFalse(withNull.contains("#"));
        assertFalse(withOverload.contains("#"));
    }

    /**
     * An empty-string fragment must still append {@code '#'} because the guard is
     * {@code fragment != null}.
     */
    @Test
    void getBookmarkableURL_withEmptyStringFragment_hashAppended() {
        String result = handler.getBookmarkableURL(testContext, VIEW_ID, null, "", false);

        assertTrue(result.endsWith("#"),
                "An empty-string fragment must produce a trailing '#' in the bookmarkable URL");
    }

    /**
     * URL parameters and a fragment must coexist in the bookmarkable URL: parameters are
     * forwarded to {@code encodeBookmarkableURL} and the fragment is part of the URL
     * passed as the first argument.
     */
    @Test
    void getBookmarkableURL_withParametersAndFragment_bothHonoured() {
        Map<String, List<String>> params = Map.of("lang", List.of("en"));

        String result = handler.getBookmarkableURL(testContext, VIEW_ID, params, "content", false);

        assertTrue(result.contains("#content"));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, List<String>>> paramsCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(testExternalContext).encodeBookmarkableURL(urlCaptor.capture(), paramsCaptor.capture());

        assertTrue(urlCaptor.getValue().endsWith("#content"),
                "Fragment must be appended to the URL handed to encodeBookmarkableURL");
        assertTrue(paramsCaptor.getValue().containsKey("lang"),
                "Parameters must be forwarded to encodeBookmarkableURL");
    }

    /**
     * Fragment ordering: the fragment must be appended directly after the action URL with no
     * additional separators or path segments inserted between them.
     */
    @Test
    void getBookmarkableURL_withFragment_fragmentImmediatelyFollowsActionUrl() {
        handler.getBookmarkableURL(testContext, VIEW_ID, null, "mySection", false);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(testExternalContext).encodeBookmarkableURL(urlCaptor.capture(), any());

        String urlPassedToEncode = urlCaptor.getValue();
        String expected = BASE_ACTION_URL + "#mySection";
        assertTrue(urlPassedToEncode.equals(expected),
                "Fragment must be appended to the raw action URL without any intervening characters; "
                        + "expected [" + expected + "] but got [" + urlPassedToEncode + "]");
    }

    // ======================================================================
    // Cross-method: consistent treatment of fragment in both methods
    // ======================================================================

    /**
     * Both {@code getRedirectURL} and {@code getBookmarkableURL} must apply the same
     * {@code "#" + fragment} concatenation logic.  When called with identical inputs, the
     * URL passed to their respective {@code encode*URL} methods must differ only in which
     * encode method is invoked, not in how the fragment is formatted.
     */
    @Test
    void bothMethods_sameFragmentFormattingLogic() {
        String fragment = "shared";

        handler.getRedirectURL(testContext, VIEW_ID, null, fragment, false);

        ArgumentCaptor<String> redirectUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(testExternalContext).encodeRedirectURL(redirectUrlCaptor.capture(), any());

        // Reset mock for bookmarkable call
        Mockito.reset(testExternalContext);
        when(testExternalContext.encodeBookmarkableURL(anyString(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(testExternalContext.encodeActionURL(anyString())).thenAnswer(inv -> inv.getArgument(0));

        handler.getBookmarkableURL(testContext, VIEW_ID, null, fragment, false);

        ArgumentCaptor<String> bookmarkUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(testExternalContext).encodeBookmarkableURL(bookmarkUrlCaptor.capture(), any());

        assertEquals(redirectUrlCaptor.getValue(), bookmarkUrlCaptor.getValue(),
                "Both methods must produce the same URL string (with fragment) before encoding");
    }
}
