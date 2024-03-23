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

package jakarta.faces.webapp;

/**
 * <p>
 * Config Bean for an Attribute.</p>
 */
public class ConfigAttribute extends ConfigFeature {

    private String attributeClass;

    public String getAttributeClass() {
        return (this.attributeClass);
    }

    public void setAttributeClass(String attributeClass) {
        this.attributeClass = attributeClass;
    }

    private String attributeName;

    public String getAttributeName() {
        return (this.attributeName);
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
}
