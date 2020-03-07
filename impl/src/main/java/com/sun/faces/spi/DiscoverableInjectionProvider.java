/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.spi;

import com.sun.faces.util.Util;

/**
 * <p>
 * <code>InjectionProvider</code>s that implement this interface can be configured via
 * <code>META-INF/services/com.sun.faces.spi.injectionprovider</code>.
 *
 * <p>
 * The format of the configuration entries is:
 * </p>
 * <ul>
 * <li><code>&lt;InjectionProviderClassName&gt;:&lt;DelegateClassName&gt;</code></li>
 * <ul>
 *
 * <p>
 * Example:</p}
 * <ul>
 * <li><code>com.sun.faces.vendor.GlassFishInjectionProvider:com.sun.enterprise.InjectionManager</code></li>
 * </ul>
 *
 * <p>
 * Multiple <code>DiscoverableInjectionProvider</code>s can be configured within a single services entry.
 * </p>
 */
public abstract class DiscoverableInjectionProvider implements InjectionProvider {

    /**
     * @param delegateClass the name of the delegate used by the <code>InjectionProvider</code> implementation.
     * @return returns <code>true</code> if the <code>InjectionProvider</code> instance is appropriate for the container its
     * currently deployed within, otherwise return <code>false</code>
     */
    public static boolean isInjectionFeatureAvailable(String delegateClass) {

        try {
            Util.loadClass(delegateClass, null);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

}
