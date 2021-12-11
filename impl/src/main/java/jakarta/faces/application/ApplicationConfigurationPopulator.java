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

package jakarta.faces.application;

import org.w3c.dom.Document;

/**
 *
 * <p class="changed_added_2_2">
 * This class defines a {@code java.util.ServiceLoader} service which enables programmatic configuration of the Jakarta
 * Server Faces runtime using the existing Application Configuration Resources schema. See the 
 * section 11.3.2 "Application Startup Behavior" in the Jakarta Faces Specification Document 
 * for the specification on when and how implementations of this
 * service are used.
 * </p>
 *
 * @since 2.2
 *
 */
public abstract class ApplicationConfigurationPopulator {

    /**
     * <p class="changed_added_2_2">
     * Service providers that implement this service must be called by the Jakarta Faces runtime exactly once for
     * each implementation, at startup, before any requests have been serviced. Before calling the
     * {@link #populateApplicationConfiguration} method, the runtime must ensure that the {@code Document} argument is empty
     * aside from being pre-configured to be in the proper namespace for an Application Configuration Resources file:
     * {@code https://jakarta.ee/xml/ns/jakartaee}. Implementations of this service must ensure that any changes made to the
     * argument {@code
     * Document} conform to that schema as defined in the specification. The Jakarta Faces runtime is not required to
     * validate the {@code Document} after control returns from the service implementation, though it may do so.
     * </p>
     *
     * <div class="changed_added_2_2">
     *
     * <p>
     * Ordering of Artifacts
     * </p>
     *
     * <p>
     * If the document is made to contain an {@code <ordering>} element, as specified in the 
     * section 11.3.8 "Ordering of Artifacts" in the Jakarta Faces Specification Document,
     * the document will be prioritized accordingly. Otherwise, the
     * runtime must place the document in the list of other Application Configuration Resources documents at the "lowest"
     * priority, meaning any conflicts that may arise between the argument document and any other Application Configuration
     * Resources are resolved in favor of the other document.
     * </p>
     *
     * </div>
     *
     * @param toPopulate The Document to populate with configuration.
     *
     * @since 2.2
     */

    public abstract void populateApplicationConfiguration(Document toPopulate);

}
