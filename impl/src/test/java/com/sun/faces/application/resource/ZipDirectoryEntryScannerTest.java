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
package com.sun.faces.application.resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * Pins which resource libraries a scan of the JARs under <code>/WEB-INF/lib</code> reports as present. A library
 * packaged under <code>META-INF/resources</code> is found by its name, and one packaged under a locale prefix is
 * found by name and prefix together, matching the key {@link ZipDirectoryEntryScanner#libraryExists(String, String)}
 * builds for each.
 */
class ZipDirectoryEntryScannerTest {

    private static final String JAR_PATH = "/WEB-INF/lib/resources.jar";

    @Test
    void libraryIsFoundByName() {
        ZipDirectoryEntryScanner scanner = scan("META-INF/resources/mylib/foo.js");

        assertTrue(scanner.libraryExists("mylib", null));
    }

    @Test
    void localePrefixedLibraryIsFoundByNameAndPrefix() {
        ZipDirectoryEntryScanner scanner = scan("META-INF/resources/de/mylib/foo.js");

        assertTrue(scanner.libraryExists("mylib", "de"));
    }

    @Test
    void unpackagedLibraryIsNotFound() {
        ZipDirectoryEntryScanner scanner = scan("META-INF/resources/mylib/foo.js", "META-INF/resources/de/mylib/foo.js");

        assertFalse(scanner.libraryExists("otherlib", null));
        assertFalse(scanner.libraryExists("mylib", "fr"));
    }

    private static ZipDirectoryEntryScanner scan(String... entryNames) {
        ExternalContext extContext = mock(ExternalContext.class);
        when(extContext.getResourcePaths("/WEB-INF/lib")).thenReturn(Set.of(JAR_PATH));
        when(extContext.getResourceAsStream(JAR_PATH)).thenReturn(new ByteArrayInputStream(jar(entryNames)));

        FacesContext context = mock(FacesContext.class);
        when(context.getExternalContext()).thenReturn(extContext);

        try (MockedStatic<FacesContext> staticContext = mockStatic(FacesContext.class)) {
            staticContext.when(FacesContext::getCurrentInstance).thenReturn(context);
            return new ZipDirectoryEntryScanner();
        }
    }

    private static byte[] jar(String... entryNames) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(bytes)) {
            for (String entryName : entryNames) {
                zos.putNextEntry(new ZipEntry(entryName));
                zos.write(entryName.getBytes(UTF_8));
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return bytes.toByteArray();
    }

}
