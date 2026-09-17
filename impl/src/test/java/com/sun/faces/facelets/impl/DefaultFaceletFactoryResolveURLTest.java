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

package com.sun.faces.facelets.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.sun.faces.context.FacesFileNotFoundException;

/**
 * A container serves its Facelets from a union of resource roots: the webapp itself, any further resource base it was
 * configured with, and every {@code META-INF/resources} on the classpath, exploded on disk as readily as archived. A
 * relative {@code src} or {@code template} must resolve within the root of the Facelet declaring it, whichever root
 * that is, and must not reach out of it.
 * <p>
 * This contract is not expressible as an integration test in a Jakarta EE compatible container: a deployed test webapp
 * has one single resource root, because per the Servlet specification {@code META-INF/resources} is honored for JARs in
 * {@code WEB-INF/lib} only, and those carry a {@code jar:} url rather than the {@code file:} url of a root exploded next
 * to the webapp. Merging a second physical root takes container specific deployment configuration, which is why the
 * resolver is stubbed here instead.
 */
class DefaultFaceletFactoryResolveURLTest {

    private static final String WEBAPP = "file:/app/";
    private static final String EXPLODED_LIBRARY = "file:/library/classes/META-INF/resources/";
    private static final String ARCHIVED_LIBRARY = "jar:file:/app/WEB-INF/lib/library.jar!/META-INF/resources/";
    private static final String ARCHIVED_CONTRACT = "jar:file:/app/WEB-INF/lib/library.jar!/META-INF/contracts/theme/";
    private static final String ARCHIVED_FLOW = "jar:file:/app/WEB-INF/lib/library.jar!/META-INF/flows/checkout/";
    private static final String SPACED_WEBAPP = "file:/my apps/app (1)/";
    private static final String SPACED_LIBRARY = "jar:file:/app/WEB-INF/lib/my library (1).jar!/META-INF/resources/";
    private static final String BRACKETED_CONTRACT = "jar:file:/app/WEB-INF/lib/library.jar!/META-INF/contracts/[theme]/";

    private final DefaultFaceletFactory factory = factoryWithWebappRoot(WEBAPP);

    @Test
    void aRelativePathResolvesWithinTheWebappRoot() throws Exception {
        assertEquals(WEBAPP + "shared/template.xhtml", resolve(WEBAPP + "page.xhtml", "shared/template.xhtml"));
    }

    @Test
    void aRelativePathResolvesWithinAnExplodedResourceLibrary() throws Exception {
        assertEquals(EXPLODED_LIBRARY + "exploded/included.xhtml", resolve(EXPLODED_LIBRARY + "exploded/page.xhtml", "included.xhtml"));
        assertEquals(EXPLODED_LIBRARY + "shared/template.xhtml", resolve(EXPLODED_LIBRARY + "exploded/page.xhtml", "../shared/template.xhtml"));
    }

    @Test
    void aRelativePathResolvesWithinAnArchivedResourceLibrary() throws Exception {
        assertEquals(ARCHIVED_LIBRARY + "archived/included.xhtml", resolve(ARCHIVED_LIBRARY + "archived/page.xhtml", "included.xhtml"));
        assertEquals(ARCHIVED_LIBRARY + "shared/template.xhtml", resolve(ARCHIVED_LIBRARY + "archived/page.xhtml", "../shared/template.xhtml"));
    }

    @Test
    void aRelativePathResolvesWithinTheContractItWasDeclaredIn() throws Exception {
        assertEquals(ARCHIVED_CONTRACT + "shared/template.xhtml", resolve(ARCHIVED_CONTRACT + "sub/page.xhtml", "../shared/template.xhtml"));
    }

    @Test
    void aRelativePathResolvesWithinTheFlowItWasDeclaredIn() throws Exception {
        assertEquals(ARCHIVED_FLOW + "shared/template.xhtml", resolve(ARCHIVED_FLOW + "sub/page.xhtml", "../shared/template.xhtml"));
    }

    @Test
    void aRelativePathMayNotReachOutOfTheFlowItWasDeclaredIn() {
        assertRejected(ARCHIVED_FLOW + "sub/page.xhtml", "../../other/page.xhtml");
    }

    /**
     * Only a whole path segment names a resource root, so a directory whose name merely contains one bounds the Facelet
     * to its own directory, and the innermost of two nested roots is the one that bounds it.
     */
    @Test
    void onlyAWholePathSegmentNamesAResourceRoot() {
        assertRejected("file:/library/aMETA-INF/resources/sub/page.xhtml", "../shared/template.xhtml");
        assertRejected("file:/library/META-INF/resourcesOther/sub/page.xhtml", "../shared/template.xhtml");
        assertRejected(EXPLODED_LIBRARY + "nested/META-INF/resources/sub/page.xhtml", "../../shared/template.xhtml");
    }

    /**
     * The webapp is reachable from a resource library only where the container happened to explode that library, so
     * this pins that the webapp bounds the include, not that any particular climb out of a library resolves.
     */
    @Test
    void aRelativePathMayReachOutOfAResourceLibraryIntoTheWebapp() throws Exception {
        assertEquals(WEBAPP + "page.xhtml", resolve(EXPLODED_LIBRARY + "exploded/page.xhtml", "../../../../../app/page.xhtml"));
    }

    @Test
    void aRelativePathMayNotReachOutOfTheWebappRoot() {
        assertRejected(WEBAPP + "page.xhtml", "../evil.xhtml");
    }

    /**
     * The webapp root is a directory, so a sibling deployment directory sharing its name as a prefix lies outside it.
     */
    @Test
    void aRelativePathMayNotReachASiblingOfTheWebappRoot() throws Exception {
        DefaultFaceletFactory withoutTrailingSlash = factoryWithWebappRoot("file:/app");

        assertEquals(WEBAPP + "shared/template.xhtml", resolve(withoutTrailingSlash, WEBAPP + "page.xhtml", "shared/template.xhtml"));
        assertRejected(withoutTrailingSlash, WEBAPP + "page.xhtml", "../appEvil/evil.xhtml");
    }

    @Test
    void aRelativePathMayNotReachOutOfAnExplodedResourceLibrary() {
        assertRejected(EXPLODED_LIBRARY + "exploded/page.xhtml", "../../../../../evil.xhtml");
        assertRejected(EXPLODED_LIBRARY + "exploded/page.xhtml", "../../evil.xhtml");
        assertRejected(EXPLODED_LIBRARY + "exploded/page.xhtml", "../../resources-sibling/evil.xhtml");
    }

    @Test
    void aRelativePathMayNotReachOutOfAnArchivedResourceLibrary() {
        assertRejected(ARCHIVED_LIBRARY + "archived/page.xhtml", "../../WEB-INF/evil.xhtml");
    }

    /**
     * The {@code !/} of an archive url floors traversal, so climbing past it lands at the archive root rather than
     * outside the archive. That is still outside the resource library the Facelet was declared in.
     */
    @Test
    void aRelativePathMayNotReachTheRootOfTheArchiveItWasDeclaredIn() {
        assertRejected(ARCHIVED_LIBRARY + "archived/page.xhtml", "../../../../../../app/page.xhtml");
    }

    @Test
    void aRelativePathMayNotReachOutOfTheContractItWasDeclaredIn() {
        assertRejected(ARCHIVED_CONTRACT + "sub/page.xhtml", "../../other/template.xhtml");
    }

    @Test
    void aRelativePathMayNotCarryAnAbsoluteUrlOfItsOwn() {
        assertRejected(ARCHIVED_LIBRARY + "archived/page.xhtml", "jar:https://evil.example/evil.jar!/evil.xhtml");
        assertRejected(WEBAPP + "page.xhtml", "https://evil.example/evil.xhtml");
    }

    @Test
    void aRelativePathMayNotPointAtANonFaceletResource() {
        assertRejected(WEBAPP + "page.xhtml", "WEB-INF/web.xml");
    }

    /**
     * A percent sign in an authored path is an encoded {@code /} or {@code .} that survives the containment check
     * literal and traverses only once the file handler decodes it. The container decodes the request once, so the
     * live vector reaching a Facelet path is a double-encoded separator such as {@code ..%252Fevil.xhtml}.
     */
    @Test
    void aRelativePathMayNotBePercentEncoded() {
        assertRejected(WEBAPP + "page.xhtml", "..%2Fevil.xhtml");
        assertRejected(WEBAPP + "page.xhtml", "%2e%2e/evil.xhtml");
        assertRejected(EXPLODED_LIBRARY + "exploded/page.xhtml", "..%2F..%2F..%2F..%2F..%2Fevil.xhtml");
    }

    /**
     * A backslash is a separator to a file system though not to {@link java.net.URL}, so a backslash path passes the
     * containment check literal and traverses when a Windows file handler reads it.
     */
    @Test
    void aRelativePathMayNotHoldABackslash() {
        assertRejected(WEBAPP + "page.xhtml", "..\\evil.xhtml");
        assertRejected(WEBAPP + "sub/page.xhtml", "..\\..\\evil.xhtml");
    }

    /**
     * The plain-path guard covers an absolute path too, so a percent sign or backslash the container might decode into
     * a separator is rejected before it reaches the resolver.
     */
    @Test
    void anAbsolutePathMayNotBePercentEncodedOrHoldABackslash() {
        assertRejected(WEBAPP + "page.xhtml", "/includes/..%2Fevil.xhtml");
        assertRejected(WEBAPP + "page.xhtml", "/includes/..\\evil.xhtml");
    }

    /**
     * A space and a bracket are legitimate in a resource name and {@link URL} keeps either literal, so a path holding
     * one resolves and is bounded like any other, in a file name as readily as in a whole path segment. Both are
     * illegal in a {@link java.net.URI}, and a leading {@code [} opens an IPv6 literal to its authority parser, so the
     * guard on a path must stay a scan for the characters that traverse and must never parse the path as a
     * {@code URI}.
     */
    @Test
    void aRelativePathMayHoldASpaceOrABracket() throws Exception {
        assertEquals(WEBAPP + "my template.xhtml", resolve(WEBAPP + "page.xhtml", "my template.xhtml"));
        assertEquals(WEBAPP + "[theme]/template.xhtml", resolve(WEBAPP + "page.xhtml", "[theme]/template.xhtml"));
        assertEquals(WEBAPP + "shared dir/template [1].xhtml", resolve(WEBAPP + "sub dir (1)/page.xhtml", "../shared dir/template [1].xhtml"));
        assertEquals(ARCHIVED_LIBRARY + "[theme]/template.xhtml", resolve(ARCHIVED_LIBRARY + "archived/page.xhtml", "../[theme]/template.xhtml"));
        assertRejected(WEBAPP + "sub dir (1)/page.xhtml", "../../evil [1].xhtml");
        assertRejected(WEBAPP + "page.xhtml", "../[theme]/evil.xhtml");
    }

    /**
     * The resource root is read off the url textually, so a webapp, an archive or a named contract directory holding a
     * space or a bracket bounds its Facelets like any other.
     */
    @Test
    void aResourceRootMayHoldASpaceOrABracket() throws Exception {
        DefaultFaceletFactory spacedWebapp = factoryWithWebappRoot(SPACED_WEBAPP);

        assertEquals(SPACED_WEBAPP + "shared/template.xhtml", resolve(spacedWebapp, SPACED_WEBAPP + "page.xhtml", "shared/template.xhtml"));
        assertRejected(spacedWebapp, SPACED_WEBAPP + "page.xhtml", "../evil.xhtml");
        assertEquals(SPACED_LIBRARY + "shared/template.xhtml", resolve(SPACED_LIBRARY + "sub/page.xhtml", "../shared/template.xhtml"));
        assertRejected(SPACED_LIBRARY + "sub/page.xhtml", "../../evil.xhtml");
        assertEquals(BRACKETED_CONTRACT + "shared/template.xhtml", resolve(BRACKETED_CONTRACT + "sub/page.xhtml", "../shared/template.xhtml"));
        assertRejected(BRACKETED_CONTRACT + "sub/page.xhtml", "../../other/template.xhtml");
    }

    /**
     * A Facelet the application serves out of no resource root at all, such as the one-shot page which
     * {@link DefaultFaceletFactory#_createComponent} fabricates, is bounded by its own directory.
     */
    @Test
    void aFaceletOutsideEveryResourceRootIsBoundedByItsOwnDirectory() throws Exception {
        assertEquals(WEBAPP + "page.xhtml", resolve(WEBAPP + "mojarra1.tmp", "page.xhtml"));
        assertRejected(WEBAPP + "mojarra1.tmp", "../evil.xhtml");
        assertRejected("jar:file:/app/WEB-INF/lib/library.jar!/other/page.xhtml", "../evil.xhtml");
    }

    private static DefaultFaceletFactory factoryWithWebappRoot(String webappRoot) {
        DefaultResourceResolver resolver = mock(DefaultResourceResolver.class);
        when(resolver.resolveUrl("/")).thenReturn(url(webappRoot));

        DefaultFaceletFactory factory = new DefaultFaceletFactory();
        factory.initResourceResolution(resolver, new String[] { ".xhtml" });
        return factory;
    }

    private String resolve(String source, String path) throws Exception {
        return resolve(factory, source, path);
    }

    private static String resolve(DefaultFaceletFactory factory, String source, String path) throws Exception {
        return factory.resolveURL(url(source), path).toExternalForm();
    }

    private void assertRejected(String source, String path) {
        assertRejected(factory, source, path);
    }

    private static void assertRejected(DefaultFaceletFactory factory, String source, String path) {
        assertThrows(FacesFileNotFoundException.class, () -> factory.resolveURL(url(source), path), path + " must not resolve from " + source);
    }

    private static URL url(String spec) {
        try {
            return new URL(spec);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException(spec, e);
        }
    }
}
