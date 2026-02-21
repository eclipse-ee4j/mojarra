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

package com.sun.faces.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import jakarta.faces.application.NavigationCase;
import jakarta.faces.component.UIViewRoot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;

/**
 * Tests for the URL fragment parsing in {@link NavigationHandlerImpl#findImplicitMatch}.
 *
 * <p>The {@code findImplicitMatch} method is exercised indirectly via
 * {@link NavigationHandlerImpl#getNavigationCase} with outcomes that do not match
 * any configured explicit navigation rule, so the implicit-matching algorithm is triggered.</p>
 *
 * <p>A {@code #fragment} suffix in the outcome string must be parsed out and stored on the
 * resulting {@link NavigationCase} while the remainder of the outcome is used to derive the
 * target view ID.  The fragment must be independent of, and compose correctly with, a
 * leading query string that may carry {@code faces-redirect=true},
 * {@code includeViewParams=true}, or arbitrary request parameters.</p>
 */
class NavigationHandlerImplFragmentTest extends JUnitFacesTestCaseBase {

    private NavigationHandlerImpl handler;

    /**
     * Additional per-test setup: place a {@link UIViewRoot} with a known view ID on the
     * {@link com.sun.faces.mock.MockFacesContext} so that implicit navigation matching has a
     * current view to resolve relative outcomes against and to use as the
     * {@code fromViewId} stored in the resulting {@link NavigationCase}.
     */
    @BeforeEach
    void setUpViewRoot() {
        UIViewRoot root = new UIViewRoot();
        root.setViewId("/current.xhtml");
        facesContext.setViewRoot(root);

        // NavigationHandlerImpl constructor reads ApplicationAssociate (null in tests → fine)
        handler = new NavigationHandlerImpl();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Resolves a navigation case through the implicit-match path.
     * No explicit navigation rules are registered, so every non-empty outcome
     * that cannot be found via exact/wildcard/default maps will reach
     * {@code findImplicitMatch}.
     */
    private NavigationCase resolveImplicit(String outcome) {
        return handler.getNavigationCase(facesContext, null, outcome);
    }

    // ------------------------------------------------------------------ tests: fragment extraction

    /**
     * A simple {@code #fragment} suffix must be stripped from the view path and
     * stored verbatim on the resulting {@link NavigationCase}.
     */
    @Test
    void outcomeWithFragment_fragmentExtractedAndViewIdResolved() {
        NavigationCase navCase = resolveImplicit("/target.xhtml#section1");

        assertNotNull(navCase, "Expected a navigation case for an outcome with a fragment");
        assertEquals("section1", navCase.getFragment(), "Fragment must equal the part after '#'");
        assertEquals("/target.xhtml", navCase.getToViewId(facesContext), "View ID must not include the fragment");
    }

    /**
     * When the fragment identifier is an empty string (outcome ends with {@code #}),
     * the fragment stored on the {@link NavigationCase} must be an empty string rather than
     * {@code null}.
     */
    @Test
    void outcomeWithEmptyFragment_fragmentIsEmptyString() {
        NavigationCase navCase = resolveImplicit("/target.xhtml#");

        assertNotNull(navCase);
        assertEquals("", navCase.getFragment(),
                "An outcome ending with '#' must yield an empty-string fragment, not null");
        assertEquals("/target.xhtml", navCase.getToViewId(facesContext));
    }

    /**
     * When the outcome contains multiple {@code #} characters, everything from the
     * <em>first</em> {@code #} to the end of the string is treated as the fragment
     * (which is compliant with RFC 3986 § 3.5: a fragment may itself contain {@code #}).
     */
    @Test
    void outcomeWithMultipleHashes_everythingAfterFirstHashIsFragment() {
        NavigationCase navCase = resolveImplicit("/target.xhtml#top#nested");

        assertNotNull(navCase);
        assertEquals("top#nested", navCase.getFragment(),
                "Fragment must be the full substring after the first '#', including any subsequent '#'");
        assertEquals("/target.xhtml", navCase.getToViewId(facesContext));
    }

    /**
     * An outcome without a {@code #} character must yield a {@link NavigationCase} whose
     * fragment is {@code null}.
     */
    @Test
    void outcomeWithoutFragment_fragmentIsNull() {
        NavigationCase navCase = resolveImplicit("/target.xhtml");

        assertNotNull(navCase);
        assertNull(navCase.getFragment(), "Fragment must be null when the outcome contains no '#'");
    }

    // ------------------------------------------------------------------ tests: fragment with redirect

    /**
     * {@code faces-redirect=true} and a fragment must both be honoured when they appear
     * together in the outcome query string.  The {@code faces-redirect} token must be
     * consumed by the parser (not forwarded as a query parameter), the redirect flag must
     * be set, and the fragment must be stored correctly on the {@link NavigationCase}.
     */
    @Test
    void outcomeWithRedirectAndFragment_redirectSetAndFragmentExtracted() {
        NavigationCase navCase = resolveImplicit("/target.xhtml?faces-redirect=true#section2");

        assertNotNull(navCase);
        assertTrue(navCase.isRedirect(), "faces-redirect=true must set the redirect flag");
        assertEquals("section2", navCase.getFragment(),
                "Fragment must be extracted even when faces-redirect is present");
        assertEquals("/target.xhtml", navCase.getToViewId(facesContext));
        // faces-redirect token itself must not end up in the parameter map
        assertNull(navCase.getParameters(),
                "Parameter map must be null when the only query token was faces-redirect=true");
    }

    /**
     * {@code includeViewParams=true} and a fragment must be parsed correctly.  The
     * {@code includeViewParams} token is consumed, the flag set, and the fragment returned.
     */
    @Test
    void outcomeWithIncludeViewParamsAndFragment_bothHonoured() {
        NavigationCase navCase = resolveImplicit("/target.xhtml?includeViewParams=true#section3");

        assertNotNull(navCase);
        assertTrue(navCase.isIncludeViewParams(),
                "includeViewParams=true must set the includeViewParams flag");
        assertEquals("section3", navCase.getFragment());
        assertEquals("/target.xhtml", navCase.getToViewId(facesContext));
        assertNull(navCase.getParameters(),
                "Parameter map must be null when the only query token was includeViewParams=true");
    }

    // ------------------------------------------------------------------ tests: fragment with query parameters

    /**
     * An arbitrary query parameter in the outcome must be stored in the parameter map of
     * the {@link NavigationCase} while the fragment is stored separately.
     */
    @Test
    void outcomeWithQueryParamAndFragment_paramAndFragmentBothStored() {
        NavigationCase navCase = resolveImplicit("/target.xhtml?foo=bar#anchor");

        assertNotNull(navCase);
        assertEquals("anchor", navCase.getFragment());
        assertEquals("/target.xhtml", navCase.getToViewId(facesContext));

        Map<String, List<String>> params = navCase.getParameters();
        assertNotNull(params, "Parameter map must not be null when a query param is present");
        assertTrue(params.containsKey("foo"), "Query parameter 'foo' must be in the parameter map");
        assertEquals(List.of("bar"), params.get("foo"));
    }

    /**
     * Multiple query parameters combined with a fragment must all be parsed correctly.
     */
    @Test
    void outcomeWithMultipleQueryParamsAndFragment_allParsedCorrectly() {
        NavigationCase navCase = resolveImplicit("/target.xhtml?a=1&b=2#bottom");

        assertNotNull(navCase);
        assertEquals("bottom", navCase.getFragment());

        Map<String, List<String>> params = navCase.getParameters();
        assertNotNull(params);
        assertEquals(List.of("1"), params.get("a"));
        assertEquals(List.of("2"), params.get("b"));
    }

    /**
     * Combining {@code faces-redirect=true}, an arbitrary query parameter, and a fragment
     * must result in the redirect flag being set, the parameter stored in the map, and the
     * fragment stored on the {@link NavigationCase}.
     */
    @Test
    void outcomeWithRedirectQueryParamAndFragment_allParsedCorrectly() {
        NavigationCase navCase = resolveImplicit("/target.xhtml?faces-redirect=true&page=2#section4");

        assertNotNull(navCase);
        assertTrue(navCase.isRedirect());
        assertEquals("section4", navCase.getFragment());

        Map<String, List<String>> params = navCase.getParameters();
        assertNotNull(params, "Parameter map must contain the non-faces-redirect query parameter");
        assertFalse(params.containsKey("faces-redirect"),
                "faces-redirect token must be consumed and must not appear in the parameter map");
        assertEquals(List.of("2"), params.get("page"));
    }

    // ------------------------------------------------------------------ tests: redirect flag is independent

    /**
     * A fragment without {@code faces-redirect} in the outcome must not accidentally set
     * the redirect flag.
     */
    @Test
    void outcomeWithFragmentOnly_redirectIsFalse() {
        NavigationCase navCase = resolveImplicit("/target.xhtml#section5");

        assertNotNull(navCase);
        assertFalse(navCase.isRedirect(),
                "Redirect flag must remain false when faces-redirect=true is absent from outcome");
        assertEquals("section5", navCase.getFragment());
    }

    // ------------------------------------------------------------------ tests: relative outcomes

    /**
     * A relative (no leading {@code /}) outcome with a fragment must be resolved against
     * the current view's directory and the current view's extension must be appended when
     * the outcome has no extension of its own.  The fragment is extracted before path
     * resolution so it must not interfere with extension or prefix logic.
     */
    @Test
    void relativeOutcomeWithFragment_resolvedAgainstCurrentViewAndExtensionAppended() {
        // Current view is /current.xhtml → directory is "/", extension is ".xhtml"
        // outcome "sibling#aside" → view /sibling.xhtml, fragment "aside"
        NavigationCase navCase = resolveImplicit("sibling#aside");

        assertNotNull(navCase, "Relative outcome with fragment must resolve to a navigation case");
        assertEquals("aside", navCase.getFragment(),
                "Fragment must be extracted from a relative outcome");
        assertEquals("/sibling.xhtml", navCase.getToViewId(facesContext),
                "Relative outcome must be resolved to an absolute path using the current view's directory and extension");
    }

    /**
     * A relative outcome that already carries an explicit extension must keep that extension
     * while the fragment is still extracted correctly.
     */
    @Test
    void relativeOutcomeWithExplicitExtensionAndFragment_extensionKeptFragmentExtracted() {
        NavigationCase navCase = resolveImplicit("other.xhtml#hero");

        assertNotNull(navCase);
        assertEquals("hero", navCase.getFragment());
        assertEquals("/other.xhtml", navCase.getToViewId(facesContext));
    }

    /**
     * A relative outcome that sits in a sub-directory of the current view must be resolved
     * with the current view's directory as a base.
     *
     * <p>Current view is {@code /current.xhtml} whose last slash is at index 0, so the
     * directory prefix is {@code /}.  An outcome of {@code sub/page#frag} therefore
     * resolves to {@code /sub/page.xhtml}.</p>
     */
    @Test
    void relativeOutcomeWithSubPath_resolvedFromCurrentViewDirectory() {
        NavigationCase navCase = resolveImplicit("sub/page#frag");

        assertNotNull(navCase);
        assertEquals("frag", navCase.getFragment());
        assertEquals("/sub/page.xhtml", navCase.getToViewId(facesContext));
    }

    // ------------------------------------------------------------------ tests: fromViewId stored correctly

    /**
     * The {@code fromViewId} on the resulting {@link NavigationCase} must always be the
     * current view's ID, regardless of whether the outcome contains a fragment.
     */
    @Test
    void fragmentOutcome_fromViewIdIsCurrentViewId() {
        NavigationCase navCase = resolveImplicit("/target.xhtml#check");

        assertNotNull(navCase);
        assertEquals("/current.xhtml", navCase.getFromViewId(),
                "fromViewId must be the ID of the current view");
    }

    /**
     * The original outcome string (including the fragment) must be preserved as the
     * {@code fromOutcome} on the resulting {@link NavigationCase} so that callers can
     * distinguish different navigation flows that target the same view with different
     * fragments.
     */
    @Test
    void fragmentOutcome_fromOutcomeIsOriginalOutcomeString() {
        String outcome = "/target.xhtml#preserve";
        NavigationCase navCase = resolveImplicit(outcome);

        assertNotNull(navCase);
        assertEquals(outcome, navCase.getFromOutcome(),
                "fromOutcome must be the original, unmodified outcome string (including fragment)");
    }
}
