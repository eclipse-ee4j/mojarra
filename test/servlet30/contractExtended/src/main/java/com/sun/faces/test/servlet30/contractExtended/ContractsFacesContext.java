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

package com.sun.faces.test.servlet30.contractExtended;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;
import javax.faces.event.PhaseId;

/**
 * Wrap FacesContext to allow general contracts activation through a managed
 * bean.
 *
 * @author dueni
 *
 */
public class ContractsFacesContext extends FacesContextWrapper {

    private static final Logger LOG = Logger.getLogger(ContractsFacesContext.class.getName());

    private static final String ACTIVE_CONTRACTS = "active-contracts";

    private FacesContext wrapped;

    private boolean activeContractsEvaluated = false;

    public ContractsFacesContext(FacesContext toWrap) {
        wrapped = toWrap;
    }

    @Override
    public FacesContext getWrapped() {
        return wrapped;
    }

    @Override
    public List<String> getResourceLibraryContracts() {
        if (!activeContractsEvaluated) {
            activeContractsEvaluated = true;
            String value = getExternalContext().getInitParameter(ACTIVE_CONTRACTS);
            if (value != null) {
                try {
                    ELContext el = getELContext();
                    ExpressionFactory elFactory = getApplication().getExpressionFactory();
                    ValueExpression ve = elFactory.createValueExpression(el, value, Object.class);
                    Object result = ve.getValue(el);
                    if (result instanceof String && !((String) result).isEmpty()) {
                        String[] contracts = ((String) result).split(",");
                        getWrapped().setResourceLibraryContracts(Arrays.asList(contracts));
                    }
                } catch (ELException elx) {
                    LOG.log(Level.SEVERE, "Exception while evaluating '" + ACTIVE_CONTRACTS
                            + "' web.xml context-parameter!", elx);
                }
            }
        }
        return getWrapped().getResourceLibraryContracts();
    }

    @Override
    public void setCurrentPhaseId(PhaseId currentPhaseId) {
        if (currentPhaseId == PhaseId.RENDER_RESPONSE) {
            activeContractsEvaluated = false;
        }
        super.setCurrentPhaseId(currentPhaseId);
    }
}
