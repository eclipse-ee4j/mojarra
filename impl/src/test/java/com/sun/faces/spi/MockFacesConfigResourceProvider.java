package com.sun.faces.spi;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.servlet.ServletContext;

/**
 * Mock class for unit testing {@link FacesConfigResourceProvider} instantiating via service file.
 */
class MockFacesConfigResourceProvider implements FacesConfigResourceProvider {

    @Override
    public Collection<URI> getResources(ServletContext context) {
        return new ArrayList<>();
    }
}