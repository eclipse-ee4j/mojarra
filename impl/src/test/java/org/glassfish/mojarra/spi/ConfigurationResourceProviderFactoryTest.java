/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.mojarra.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.glassfish.mojarra.spi.ConfigurationResourceProviderFactory.ProviderType;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ConfigurationResourceProviderFactory}.
 */
public class ConfigurationResourceProviderFactoryTest {

    /**
     * Test creating configuration resource providers from a service file.
     * <p>
     * It tests the correct parsing of the service file with empty lines and comments.
     *
     * @throws FileNotFoundException Thrown when the test service file can't be written.
     */
    @Test
    public void testCreateProvidersSuccessful() throws FileNotFoundException {
        ProviderType facesConfig = ProviderType.FacesConfig;

        File servicesFile = prepareServiceFile(facesConfig);

        try (PrintWriter writer = new PrintWriter(servicesFile)) {
            writer.println("");
            writer.println("# Notices for Eclipse Mojarra");
            writer.println("org.glassfish.mojarra.spi.MockFacesConfigResourceProvider");
            writer.println("org.glassfish.mojarra.spi.MockFacesConfigResourceProvider   # This is a comment.");
            writer.flush();
        }

        ConfigurationResourceProvider[] providers = ConfigurationResourceProviderFactory.createProviders(facesConfig);
        assertNotNull(providers);
        assertEquals(2, providers.length);

        servicesFile.delete();
    }

    /**
     * Test creating configuration resource providers from a service file.
     * <p>
     * It tests the correct parsing of the service file with a comment and if it correctly denies the attempt to load the wrong class
     * ({@link MockConfigurationResourceProvider}) for the given {@link ProviderType}.
     *
     * @throws FileNotFoundException Thrown when the test service file can't be written.
     */
    @Test
    public void testCreateProvidersWrongService() throws FileNotFoundException {
        ProviderType facesConfig = ProviderType.FacesConfig;

        File servicesFile = prepareServiceFile(facesConfig);

        try (PrintWriter writer = new PrintWriter(servicesFile)) {
            writer.println(
                "org.glassfish.mojarra.spi.MockConfigurationResourceProvider"
                    + "   # This should fail, since this is the wrong class for this ProviderType"
            );
            writer.flush();
        }

        try {
            assertThrows(IllegalStateException.class, () -> ConfigurationResourceProviderFactory.createProviders(facesConfig));
        }
        finally {
            servicesFile.delete();
        }
    }

    /**
     * Test case when no service file exists.
     */
    @Test
    public void testNoServiceFile() {
        ProviderType facesConfig = ProviderType.FacesConfig;

        prepareServiceFile(facesConfig);

        ConfigurationResourceProvider[] providers = ConfigurationResourceProviderFactory.createProviders(facesConfig);
        assertNotNull(providers);
        assertEquals(0, providers.length);
    }

    private static File prepareServiceFile(ProviderType facesConfig) {
        // Write services file to test with.
        File servicesDir = new File(System.getProperty("basedir"), "target/classes/META-INF/services");
        servicesDir.mkdirs();

        File servicesFile = new File(servicesDir, facesConfig.servicesKey);

        if (servicesFile.exists()) {
            servicesFile.delete();
        }
        return servicesFile;
    }

}
