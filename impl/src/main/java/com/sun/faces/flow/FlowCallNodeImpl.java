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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.FlowCallNode;
import jakarta.faces.flow.Parameter;

public class FlowCallNodeImpl extends FlowCallNode implements Serializable {
    private static final long serialVersionUID = 543332738561754405L;

    private final String id;
    private final ValueExpression calledFlowIdVE;

    private final ValueExpression calledFlowDocumentIdVE;

    private Map<String, Parameter> _outboundParameters;
    private Map<String, Parameter> outboundParameters;

    public FlowCallNodeImpl(String id, String calledFlowDocumentId, String calledFlowId, List<Parameter> outboundParametersFromConfig) {
        FacesContext context = FacesContext.getCurrentInstance();
        this.id = id;

        if (null != calledFlowDocumentId) {
            calledFlowDocumentIdVE = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), calledFlowDocumentId,
                    String.class);
        } else {
            calledFlowDocumentIdVE = null;
        }

        if (null != calledFlowId) {
            calledFlowIdVE = context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(), calledFlowId, String.class);
        } else {
            calledFlowIdVE = null;
        }

        _outboundParameters = new ConcurrentHashMap<>();
        if (null != outboundParametersFromConfig) {
            for (Parameter cur : outboundParametersFromConfig) {
                _outboundParameters.put(cur.getName(), cur);
            }
        }
        outboundParameters = Collections.unmodifiableMap(_outboundParameters);

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FlowCallNodeImpl other = (FlowCallNodeImpl) obj;
        if (id == null ? other.id != null : !id.equals(other.id)) {
            return false;
        }
        if (calledFlowIdVE != other.calledFlowIdVE && (calledFlowIdVE == null || !calledFlowIdVE.equals(other.calledFlowIdVE))) {
            return false;
        }
        if (calledFlowDocumentIdVE != other.calledFlowDocumentIdVE
                && (calledFlowDocumentIdVE == null || !calledFlowDocumentIdVE.equals(other.calledFlowDocumentIdVE))) {
            return false;
        }
        if (_outboundParameters != other._outboundParameters
                && (_outboundParameters == null || !_outboundParameters.equals(other._outboundParameters))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (id != null ? id.hashCode() : 0);
        hash = 59 * hash + (calledFlowIdVE != null ? calledFlowIdVE.hashCode() : 0);
        hash = 59 * hash + (calledFlowDocumentIdVE != null ? calledFlowDocumentIdVE.hashCode() : 0);
        hash = 59 * hash + (_outboundParameters != null ? _outboundParameters.hashCode() : 0);
        return hash;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCalledFlowDocumentId(FacesContext context) {
        String result = null;

        if (null != calledFlowDocumentIdVE) {
            result = (String) calledFlowDocumentIdVE.getValue(context.getELContext());
        }

        return result;
    }

    @Override
    public String getCalledFlowId(FacesContext context) {
        String result = null;

        if (null != calledFlowIdVE) {
            result = (String) calledFlowIdVE.getValue(context.getELContext());
        }

        return result;
    }

    public Map<String, Parameter> _getOutboundParameters() {
        return _outboundParameters;
    }

    @Override
    public Map<String, Parameter> getOutboundParameters() {
        return _outboundParameters;
    }
}
