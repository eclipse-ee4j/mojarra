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

import static org.glassfish.mojarra.config.manager.FacesSchema.CURRENT_NAMESPACE;
import static org.glassfish.mojarra.config.manager.FacesSchema.CURRENT_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Every schema version which a <code>faces-config.xml</code> on a supported classpath may declare has to resolve, and the resource it names has to be present
 * and compile.
 */
class FacesSchemaTest {

    @ParameterizedTest
    @ValueSource(strings = { "5.0", "4.1", "4.0", "3.0" })
    void resolvesAndLoadsEveryJakartaFacesConfigSchema(String version) {
        FacesSchema schema = FacesSchema.fromDocumentId(CURRENT_NAMESPACE, version, "faces-config");

        assertNotNull(schema, version);
        assertNotNull(schema.loadSchema(), version);
    }

    @ParameterizedTest
    @ValueSource(strings = { "5.0", "4.1", "4.0", "3.0" })
    void resolvesAndLoadsEveryFaceletTaglibSchema(String version) {
        FacesSchema schema = FacesSchema.fromDocumentId(CURRENT_NAMESPACE, version, "facelet-taglib");

        assertNotNull(schema, version);
        assertNotNull(schema.loadSchema(), version);
    }

    /**
     * Mojarra stamps this version on the documents it synthesizes itself, so a schema for it must exist.
     */
    @Test
    void resolvesTheVersionMojarraSynthesizes() {
        assertEquals(FacesSchema.FACES_50, FacesSchema.fromDocumentId(CURRENT_NAMESPACE, CURRENT_VERSION, "faces-config"));
    }

}
