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
 * Base bean for parsing configuration information.
 * </p>
 */
public class ConfigBase {

    // ---------------------------------------------------------- <application>
    private String actionListener = null;

    public String getActionListener() {
        return (this.actionListener);
    }

    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }

    private String navigationHandler = null;

    public String getNavigationHandler() {
        return (this.navigationHandler);
    }

    public void setNavigationHandler(String navigationHandler) {
        this.navigationHandler = navigationHandler;
    }


    // ------------------------------------------------------------ <component>
    private Map components = null;

    public void addComponent(ConfigComponent component) {
        if (components == null) {
            components = new HashMap();
        }
        components.put(component.getComponentType(), component);
    }

    public Map getComponents() {
        if (components == null) {
            return (Collections.EMPTY_MAP);
        } else {
            return (this.components);
        }
    }

    // ------------------------------------------------------------ <converter>
    private Map converters = null;

    public void addConverter(ConfigConverter converter) {
        if (converters == null) {
            converters = new HashMap();
        }
        converters.put(converter.getConverterId(), converter);
    }

    public Map getConverters() {
        if (converters == null) {
            return (Collections.EMPTY_MAP);
        } else {
            return (this.converters);
        }
    }

    // ------------------------------------------------------------ <validator>
    private Map validators = null;

    public void addValidator(ConfigValidator validator) {
        if (validators == null) {
            validators = new HashMap();
        }
        validators.put(validator.getValidatorId(), validator);
    }

    public Map getValidators() {
        if (validators == null) {
            return (Collections.EMPTY_MAP);
        } else {
            return (this.validators);
        }
    }
}
