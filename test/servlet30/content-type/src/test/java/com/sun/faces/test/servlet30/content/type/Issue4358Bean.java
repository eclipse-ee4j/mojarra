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

package com.sun.faces.test.servlet30.content.type;

import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;


/**
 * @author  Kyle Stiemann
 */
@ManagedBean
@RequestScoped
public class Issue4358Bean {

    public List<String> getExternalContextCalls() {
        return getExternalContextCalls(FacesContext.getCurrentInstance());
    }

    private List<String> getExternalContextCalls(FacesContext facesContext) {
        return ((ExternalContextIssue4358Impl) facesContext.getExternalContext()).getExternalContextCalls();
    }

    public String getResult() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        List<String> externalContextCalls = getExternalContextCalls(facesContext);
        int indexOfGetWriterCall =
                externalContextCalls.indexOf(ExternalContextIssue4358Impl.GET_RESPONSE_OUTPUT_WRITER);
        int indexOfGetStreamCall =
                externalContextCalls.indexOf(ExternalContextIssue4358Impl.GET_RESPONSE_OUTPUT_STREAM);
        int indexOfFirstGetResponseOutputCall;

        if (indexOfGetWriterCall < 0) {

            if (indexOfGetStreamCall < 0) {
                return "FAILURE";
            }

            indexOfFirstGetResponseOutputCall = indexOfGetStreamCall;
        }
        else if (indexOfGetStreamCall < 0) {
            indexOfFirstGetResponseOutputCall = indexOfGetWriterCall;
        }
        else if (indexOfGetWriterCall < indexOfGetStreamCall) {
            indexOfFirstGetResponseOutputCall = indexOfGetWriterCall;
        }
        else {
            indexOfFirstGetResponseOutputCall = indexOfGetStreamCall;
        }

        String lastSetContentTypeCall = externalContextCalls.get(indexOfFirstGetResponseOutputCall - 1);
		boolean partialRequest = facesContext.getPartialViewContext().isPartialRequest();

        if ((partialRequest && lastSetContentTypeCall.contains("text/xml")) ||
            (!partialRequest && lastSetContentTypeCall.contains("text/html"))) {
            return "SUCCESS";
        }

        return "FAILURE";
    }
}