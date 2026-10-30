/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue6087;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Sets the max active view maps of the current session to the value of the {@value #ACTIVE_VIEW_MAPS_SIZE} request
 * parameter, or invalidates the current session when there is no such request parameter.
 */
@WebServlet("/session")
public class Issue6087SessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    static final String ACTIVE_VIEW_MAPS_SIZE = "com.sun.faces.application.view.activeViewMapsSize";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activeViewMapsSize = request.getParameter(ACTIVE_VIEW_MAPS_SIZE);
        HttpSession session = request.getSession();

        if (activeViewMapsSize != null) {
            session.setAttribute(ACTIVE_VIEW_MAPS_SIZE, Integer.valueOf(activeViewMapsSize));
        } else {
            session.invalidate();
        }

        response.getWriter().write("OK");
    }
}
