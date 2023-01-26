package com.sun.faces.spi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.sun.faces.spi.ConfigurationResourceProviderFactory.ProviderType;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link ConfigurationResourceProviderFactory}.
 */
public class ConfigurationResourceProviderFactoryTest {

    /**
     * Test creating configuration resource providers from a service file.
     * <p>
     * It tests the correct parsing of the service file with empty lines and comments.
     *
     * @throws FileNotFoundException
     *         Thrown when the test service file can't be written.
     */
    @Test
    public void testCreateProvidersSuccessful() throws FileNotFoundException {
        ProviderType facesConfig = ProviderType.FacesConfig;

        File servicesFile = prepareServiceFile(facesConfig);

        try (PrintWriter writer = new PrintWriter(servicesFile)) {
            writer.println("");
            writer.println("# Notices for Eclipse Mojarra");
            writer.println("com.sun.faces.spi.MockFacesConfigResourceProvider");
            writer.println("com.sun.faces.spi.MockFacesConfigResourceProvider   # This is a comment.");
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
     * It tests the correct parsing of the service file with a comment and if it correctly denies the attempt to load
     * the wrong class ({@link MockConfigurationResourceProvider}) for the given {@link ProviderType}.
     *
     * @throws FileNotFoundException
     *         Thrown when the test service file can't be written.
     */
    @Test(expected = IllegalStateException.class)
    public void testCreateProvidersWrongService() throws FileNotFoundException {
        ProviderType facesConfig = ProviderType.FacesConfig;

        File servicesFile = prepareServiceFile(facesConfig);

        try (PrintWriter writer = new PrintWriter(servicesFile)) {
            writer.println("com.sun.faces.spi.MockConfigurationResourceProvider"
                    + "   # This should fail, since this is the wrong class for this ProviderType");
            writer.flush();
        }

        try {
            ConfigurationResourceProviderFactory.createProviders(facesConfig);
        } finally {
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