package com.sun.faces.mock;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.CDIProvider;

public class MockCDIProvider implements CDIProvider {

    @Override
    public CDI<Object> getCDI() {
        return new MockCDI<Object>();
    }

}
