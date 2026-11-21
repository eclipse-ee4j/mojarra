/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.cdi;

import static jakarta.faces.component.UIViewRoot.VIEW_MAP_NAME;

import java.util.Map;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.component.UIViewRoot;

import org.glassfish.mojarra.application.view.ViewScopeMap;

/**
 * The producer of the dependent bean named {@link UIViewRoot#VIEW_MAP_NAME}, which supplies {@link UIViewRoot#getViewMap(boolean)} with a new
 * {@link ViewScopeMap}.
 */
public class ViewScopeMapProducer extends CdiProducer<ViewScopeMap> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    public ViewScopeMapProducer(BeanManager beanManager) {
        super.name(VIEW_MAP_NAME)
            .qualifiers(new NewViewScopeMapAnnotationLiteral())
            .beanClass(beanManager, ViewScopeMap.class)
            .types(ViewScopeMap.class, Map.class, Object.class)
            .create(e -> new ViewScopeMap());
    }

}
