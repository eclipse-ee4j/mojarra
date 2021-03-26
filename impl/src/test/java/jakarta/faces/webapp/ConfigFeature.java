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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Base bean for configuration beans that have common feature elements.</p>
 */
public abstract class ConfigFeature {

    private Map attributes = null;

    public void addAttribute(ConfigAttribute attribute) {
        if (attributes == null) {
            attributes = new HashMap();
        }
        attributes.put(attribute.getAttributeName(), attribute);
    }

    public Map getAttributes() {
        if (attributes == null) {
            return (Collections.EMPTY_MAP);
        } else {
            return (attributes);
        }
    }

    private String description;

    public String getDescription() {
        return (this.description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String displayName;

    public String getDisplayName() {
        return (this.displayName);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private String largeIcon;

    public String getLargeIcon() {
        return (this.largeIcon);
    }

    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }

    private Map properties = null;

    public void addProperty(ConfigProperty property) {
        if (properties == null) {
            properties = new HashMap();
        }
        properties.put(property.getPropertyName(), property);
    }

    public Map getProperties() {
        if (properties == null) {
            return (Collections.EMPTY_MAP);
        } else {
            return (properties);
        }
    }

    private String smallIcon;

    public String getSmallIcon() {
        return (this.smallIcon);
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }
}
