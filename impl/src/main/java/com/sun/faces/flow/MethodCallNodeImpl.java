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

package com.sun.faces.flow;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.facelets.util.ReflectionUtil;
import com.sun.faces.util.FacesLogger;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.MethodCallNode;
import jakarta.faces.flow.Parameter;

public class MethodCallNodeImpl extends MethodCallNode implements Serializable {

    private static final long serialVersionUID = -5400138716176841428L;

    private final String id;

    private static final Logger LOGGER = FacesLogger.FLOW.getLogger();

    public MethodCallNodeImpl(String id) {
        this.id = id;
        _parameters = new CopyOnWriteArrayList<>();
    }

    public MethodCallNodeImpl(FacesContext context, String id, String methodExpressionString, String defaultOutcomeString,
            List<Parameter> parametersFromConfig) {
        this(id);
        if (null != parametersFromConfig) {
            _parameters.addAll(parametersFromConfig);
        }
        parameters = Collections.unmodifiableList(_parameters);

        ExpressionFactory ef = context.getApplication().getExpressionFactory();
        Class<?>[] paramTypes = new Class[0];
        if (0 < parameters.size()) {
            paramTypes = new Class[parameters.size()];
            int i = 0;
            for (Parameter cur : parameters) {
                if (null != cur.getName()) {
                    try {
                        paramTypes[i] = ReflectionUtil.forName(cur.getName());
                    } catch (ClassNotFoundException cnfe) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, "parameter " + cur.getName() + "incorrect type", cnfe);
                        }
                        paramTypes[i] = null;
                    }
                } else {
                    paramTypes[i] = String.class;
                }
                i++;
            }
        }
        ELContext elContext = context.getELContext();
        methodExpression = ef.createMethodExpression(elContext, methodExpressionString, null, paramTypes);

        if (null != defaultOutcomeString) {
            outcome = ef.createValueExpression(elContext, defaultOutcomeString, Object.class);
        }

    }

    private MethodExpression methodExpression;

    private ValueExpression outcome;

    private List<Parameter> _parameters;
    private List<Parameter> parameters;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<Parameter> _getParameters() {
        if (null == parameters) {
            parameters = Collections.unmodifiableList(_parameters);
        }
        return _parameters;
    }

    @Override
    public MethodExpression getMethodExpression() {
        return methodExpression;
    }

    public void setMethodExpression(MethodExpression methodExpression) {
        this.methodExpression = methodExpression;
    }

    @Override
    public ValueExpression getOutcome() {
        return outcome;
    }

    public void setOutcome(ValueExpression outcome) {
        this.outcome = outcome;
    }

}
