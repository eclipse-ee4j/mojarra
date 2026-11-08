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

package org.glassfish.mojarra.facelets.impl;

import static java.util.Collections.emptyList;
import static org.glassfish.mojarra.facelets.impl.DefaultFaceletFactory.canRefresh;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import jakarta.faces.view.facelets.FaceletCache;

import org.junit.jupiter.api.Test;

/**
 * A facelet can be refreshed unless every facelet cache in use is the default one with refreshing disabled.
 */
class DefaultFaceletFactoryCanRefreshTest {

    private static final long NEVER_REFRESH = -1;
    private static final long ALWAYS_REFRESH = 0;
    private static final long REFRESH_PERIOD = 2;

    @Test
    void aDefaultCacheWithRefreshingDisabledCannotRefresh() {
        assertFalse(canRefresh(new DefaultFaceletCache(NEVER_REFRESH), emptyList()));
    }

    @Test
    void aDefaultCacheWithARefreshPeriodCanRefresh() {
        assertTrue(canRefresh(new DefaultFaceletCache(REFRESH_PERIOD), emptyList()));
    }

    @Test
    void aDefaultCacheWhichAlwaysRefreshesCanRefresh() {
        assertTrue(canRefresh(new DefaultFaceletCache(ALWAYS_REFRESH), emptyList()));
    }

    /**
     * A facelet cache which an application provides may expire by rules of its own.
     */
    @Test
    @SuppressWarnings("unchecked")
    void aCacheWhichAnApplicationProvidesCanRefresh() {
        assertTrue(canRefresh(mock(FaceletCache.class), emptyList()));
    }

    @Test
    void contractCachesWithRefreshingDisabledCannotRefresh() {
        assertFalse(
            canRefresh(new DefaultFaceletCache(NEVER_REFRESH), List.of(new DefaultFaceletCache(NEVER_REFRESH), new DefaultFaceletCache(NEVER_REFRESH)))
        );
    }

    @Test
    void oneContractCacheWhichCanExpireCanRefresh() {
        assertTrue(
            canRefresh(new DefaultFaceletCache(NEVER_REFRESH), List.of(new DefaultFaceletCache(NEVER_REFRESH), new DefaultFaceletCache(REFRESH_PERIOD)))
        );
    }

}
