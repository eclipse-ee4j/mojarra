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
import static java.util.logging.Level.WARNING;
import static org.glassfish.mojarra.config.manager.Documents.getXMLDocuments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.config.manager.documents.DocumentInfo;
import org.glassfish.mojarra.util.FacesLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Covers schema validation of the configuration documents, which reports what it finds and never fails the deployment
 * over it.
 */
class SchemaValidationTest {

    private static final String VIOLATION = "faces.config.schema.violation";
    private static final String UNKNOWN_VERSION = "faces.config.schema.unknown";
    private static final String WRONG_SCHEME = "faces.config.namespace.wrong_scheme";

    @TempDir
    private Path folder;

    private final Logger logger = FacesLogger.CONFIG.getLogger();
    private final List<LogRecord> records = new ArrayList<>();
    private final Handler handler = new Handler() {

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    @BeforeEach
    void captureLogging() {
        logger.addHandler(handler);
    }

    @AfterEach
    void stopCapturingLogging() {
        logger.removeHandler(handler);
    }

    @Test
    void aValidDocumentIsSilent() throws Exception {
        parse(facesConfig("5.0", ""), true);

        assertEquals(List.of(), messageKeys());
    }

    /**
     * The whole point of the parameter, which until now resolved a schema and then never validated against it.
     */
    @Test
    void aSchemaViolationIsReported() throws Exception {
        parse(facesConfig("5.0", "<bogus-element>nonsense</bogus-element>"), true);

        assertEquals(List.of(VIOLATION), messageKeys());
    }

    /**
     * A configuration file which a previous release accepted has to keep deploying, so a violation is a warning and the
     * document is still returned.
     */
    @Test
    void aSchemaViolationDoesNotFailTheDeployment() throws Exception {
        DocumentInfo[] documents = parse(facesConfig("5.0", "<bogus-element>nonsense</bogus-element>"), true);

        assertEquals(1, documents.length);
    }

    /**
     * A library on the classpath may declare a newer configuration version than this release knows, which skips
     * validation of that document rather than taking the application down.
     */
    @Test
    void anUnknownVersionSkipsValidationInsteadOfFailing() throws Exception {
        DocumentInfo[] documents = parse(facesConfig("99.0", "<bogus-element>nonsense</bogus-element>"), true);

        assertEquals(1, documents.length);
        assertEquals(List.of(UNKNOWN_VERSION), messageKeys());
    }

    /**
     * A namespace written with the wrong scheme is the mistake behind an otherwise baffling schema violation, which
     * blames whatever sits in that namespace rather than the declaration which is actually wrong.
     */
    @Test
    void aNamespaceWrittenWithTheWrongSchemeIsNamed() throws Exception {
        parse(facesConfigWithXsi("https://www.w3.org/2001/XMLSchema-instance"), true);

        assertEquals(List.of(WRONG_SCHEME, VIOLATION), messageKeys());
    }

    /**
     * And the same document with the namespace spelled correctly is silent, which is what makes the warning above a
     * diagnosis rather than noise.
     */
    @Test
    void theSameDocumentIsSilentWithTheCorrectScheme() throws Exception {
        parse(facesConfigWithXsi("http://www.w3.org/2001/XMLSchema-instance"), true);

        assertEquals(List.of(), messageKeys());
    }

    /**
     * And none of it happens when validation was not asked for.
     */
    @Test
    void nothingIsValidatedWhenTurnedOff() throws Exception {
        parse(facesConfig("5.0", "<bogus-element>nonsense</bogus-element>"), false);

        assertEquals(List.of(), messageKeys());
    }

    private String facesConfigWithXsi(String xsiNamespace) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <faces-config xmlns="https://jakarta.ee/xml/ns/jakartaee"
                    xmlns:xsi="%s"
                    xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-facesconfig_5_0.xsd"
                    version="5.0">
                </faces-config>
                """.formatted(xsiNamespace);
    }

    private String facesConfig(String version, String body) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <faces-config xmlns="https://jakarta.ee/xml/ns/jakartaee" version="%s">
                    %s
                </faces-config>
                """.formatted(version, body);
    }

    private DocumentInfo[] parse(String facesConfig, boolean validating) throws Exception {
        URI uri = writeString(folder.resolve("faces-config.xml"), facesConfig).toUri();
        ServletContext servletContext = mock(ServletContext.class);

        return getXMLDocuments(servletContext, List.of(context -> List.of(uri)), validating);
    }

    private List<String> messageKeys() {
        assertTrue(records.stream().allMatch(record -> record.getLevel() == WARNING), "everything reported is a warning");

        return records.stream().map(LogRecord::getMessage).toList();
    }
}
