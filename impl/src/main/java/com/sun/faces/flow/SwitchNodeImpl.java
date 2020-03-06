/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.SwitchCase;
import jakarta.faces.flow.SwitchNode;

public class SwitchNodeImpl extends SwitchNode implements Serializable {
    
    private static final long serialVersionUID = -9203493858518714933L;
        
    private final String id;
    private ValueExpression defaultOutcome;
    private CopyOnWriteArrayList<SwitchCase> _cases;
    private List<SwitchCase> cases;

    public SwitchNodeImpl(String id) {
        this.id = id;
        
        this.defaultOutcome = null;
        _cases = new CopyOnWriteArrayList<>();
        cases = Collections.unmodifiableList(_cases);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SwitchNodeImpl other = (SwitchNodeImpl) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if (this.defaultOutcome != other.defaultOutcome && (this.defaultOutcome == null || !this.defaultOutcome.equals(other.defaultOutcome))) {
            return false;
        }
        if (this._cases != other._cases && (this._cases == null || !this._cases.equals(other._cases))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 47 * hash + (this.defaultOutcome != null ? this.defaultOutcome.hashCode() : 0);
        hash = 47 * hash + (this._cases != null ? this._cases.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String getId() {
        return id;
    }
        
    @Override
    public List<SwitchCase> getCases() {
        return cases;
    }

    public List<SwitchCase> _getCases() {
        return _cases;
    }

    @Override
    public String getDefaultOutcome(FacesContext context) {
        String result = null;
        
        if (null != defaultOutcome) {
            Object objResult = defaultOutcome.getValue(context.getELContext());
            result = (null != objResult) ? objResult.toString() : null;
        }
        return result;
    }
    
    public void setDefaultOutcome(String defaultOutcome) {
        if (null == defaultOutcome) {
            this.defaultOutcome = null;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        ExpressionFactory eFactory = context.getApplication().getExpressionFactory();
        this.defaultOutcome = eFactory.createValueExpression(context.getELContext(), 
                defaultOutcome, Object.class);
    }
    
    public void setDefaultOutcome(ValueExpression defaultOutcome) {
        this.defaultOutcome = defaultOutcome;
    }

    
}
