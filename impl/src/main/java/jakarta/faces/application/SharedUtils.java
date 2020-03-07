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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.faces.context.FacesContext;

class SharedUtils {

    /*
     * Determine whether String is a mixed value binding expression or not.
     */
    static boolean isMixedExpression(String expression) {

        if (null == expression) {
            return false;
        }

        // if it doesn't start and end with delimiters
        return !(expression.startsWith("#{") && expression.endsWith("}")) && isExpression(expression);

    }

    /*
     * Determine whether String is a value binding expression or not.
     */
    static boolean isExpression(String expression) {

        if (null == expression) {
            return false;
        }

        // check to see if attribute has an expression
        int start = expression.indexOf("#{");
        return start != -1 && expression.indexOf('}', start + 2) != -1;
    }

    static Map<String, List<String>> evaluateExpressions(FacesContext context, Map<String, List<String>> map) {
        if (map != null && !map.isEmpty()) {
            Map<String, List<String>> ret = new HashMap<>(map.size());
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                ret.put(entry.getKey(), evaluateExpressions(context, entry.getValue()));
            }

            return ret;
        }

        return map;
    }

    static List<String> evaluateExpressions(FacesContext context, List<String> values) {
        if (!values.isEmpty()) {
            List<String> ret = new ArrayList<>(values.size());
            Application app = context.getApplication();
            for (String val : values) {
                if (val != null) {
                    String value = val.trim();
                    if (isExpression(value)) {
                        value = app.evaluateExpressionGet(context, value, String.class);
                    }
                    ret.add(value);
                }
            }

            return ret;
        }
        return values;
    }

}
