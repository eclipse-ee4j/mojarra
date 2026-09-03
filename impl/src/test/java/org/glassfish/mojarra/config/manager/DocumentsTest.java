/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.config.manager;

import static java.nio.file.Files.writeString;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.List.of;
import static org.glassfish.mojarra.config.manager.Documents.getXMLDocuments;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;

import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.config.ConfigurationException;
import org.glassfish.mojarra.config.manager.documents.DocumentInfo;
import org.glassfish.mojarra.spi.ConfigurationResourceProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentsTest {

    private static final String FACES_CONFIG = """
        <?xml version="1.0" encoding="UTF-8"?>
        <faces-config xmlns="https://jakarta.ee/xml/ns/jakartaee" version="5.0"/>
        """;

    private final ServletContext servletContext = mock(ServletContext.class);

    @TempDir
    private Path folder;

    /**
     * The documents must come back in the order in which the providers offered them, because that order is what
     * {@link Documents#sortDocuments(DocumentInfo[], FacesConfigInfo)} subsequently interprets as the classpath order to apply <code>absolute-ordering</code>
     * against.
     */
    @Test
    void documentsAreReturnedInProviderOrder() throws Exception {
        URI first = writeFacesConfig("first");
        URI second = writeFacesConfig("second");
        URI third = writeFacesConfig("third");

        DocumentInfo[] documents = getXMLDocuments(servletContext, of(provider(first, second), provider(third)), false);

        assertArrayEquals(new URI[] { first, second, third }, sourceURIs(documents));
    }

    /**
     * The same configuration resource offered by more than one provider must be parsed exactly once, at the position where it was first offered.
     */
    @Test
    void duplicateUriAcrossProvidersIsParsedOnce() throws Exception {
        URI first = writeFacesConfig("first");
        URI second = writeFacesConfig("second");

        DocumentInfo[] documents = getXMLDocuments(servletContext, of(provider(first, second), provider(second, first)), false);

        assertArrayEquals(new URI[] { first, second }, sourceURIs(documents));
    }

    /**
     * A provider which blows up must fail the configuration rather than being skipped, otherwise the application starts with silently incomplete configuration.
     */
    @Test
    void providerFailureIsNotSwallowed() {
        ConfigurationException expected = new ConfigurationException("Provider is broken");

        ConfigurationException actual = assertThrows(
            ConfigurationException.class,
            () -> getXMLDocuments(servletContext, of(context -> {
                throw expected;
            }), false)
        );

        assertSame(expected, actual);
    }

    /**
     * A configuration resource which cannot be read must fail the configuration as well, wrapped so that the caller sees the same exception type regardless of
     * what the parser threw.
     */
    @Test
    void unreadableResourceIsNotSwallowed() {
        URI absent = folder.resolve("absent-faces-config.xml").toUri();

        ConfigurationException e = assertThrows(
            ConfigurationException.class,
            () -> getXMLDocuments(servletContext, of(provider(absent)), false)
        );

        assertInstanceOf(IOException.class, e.getCause());
    }

    private URI writeFacesConfig(String name) throws IOException {
        return writeString(folder.resolve(name + "-faces-config.xml"), FACES_CONFIG).toUri();
    }

    private static ConfigurationResourceProvider provider(URI... uris) {
        Collection<URI> resources = asList(uris);
        return context -> resources;
    }

    private static URI[] sourceURIs(DocumentInfo[] documents) {
        return stream(documents).map(DocumentInfo::getSourceURI).toArray(URI[]::new);
    }

}
