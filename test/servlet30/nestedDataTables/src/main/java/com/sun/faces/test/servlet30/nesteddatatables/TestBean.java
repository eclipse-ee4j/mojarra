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

package com.sun.faces.test.servlet30.nesteddatatables;

import java.util.Vector;
import java.io.Serializable;
import java.util.Iterator;

import javax.faces.context.FacesContext;

public class TestBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Vector _services = new Vector();

    public TestBean() {

        System.err.println("Constructing a TestBean");

        Service service1 = new Service("Service 1");
        // service1.addPort(new Port("80"));

        _services.addElement(service1);

        Service service2 = new Service("Service 2");
        // service1.addPort(new Port("90"));

        _services.addElement(service2);
    }

    public Vector getServices() {
        return _services;
    }

    public void setServices(Vector services) {
        _services = services;
    }

    public String addService() {

        System.err.println("addService");

        _services.add(new Service("New Service"));

        return "OK";
    }

    public String deleteService() {

        System.err.println("deleteService");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Service service = (Service) facesContext.getExternalContext().getRequestMap().get("service");

        _services.remove(service);

        return "OK";
    }

    public String addPortNumber() {

        System.err.println("addPortNumber");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Service service = (Service) facesContext.getExternalContext().getRequestMap().get("service");

        service.addPort(new Port());

        return "OK";
    }

    public String deletePortNumber() {

        System.err.println("deletePortNumber");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Service service = (Service) facesContext.getExternalContext().getRequestMap().get("service");
        Port port = (Port) facesContext.getExternalContext().getRequestMap().get("portNumber");

        service.deletePort(port);

        return "OK";
    }

    public String getCurrentStateTable() {
        StringBuffer out = new StringBuffer();
        Iterator inner, outer = _services.iterator();
        Service curService;
        Port curPort;

        out.append("<table border=\"1\">\n");
        while (outer.hasNext()) {
            curService = (Service) outer.next();
            out.append("  <tr>\n");
            inner = curService.getPorts().iterator();
            out.append("<td>service: " + curService.getName() + "</td>");
            while (inner.hasNext()) {
                curPort = (Port) inner.next();
                out.append(" <td>port: " + curPort.getPortNumber() + "</td>\n");
            }
            out.append("</tr>\n");
        }
        out.append("</table>\n");
        return out.toString();

    }

    public String printTree() {
        Iterator inner, outer = _services.iterator();
        Service curService;
        Port curPort;
        while (outer.hasNext()) {
            curService = (Service) outer.next();
            System.out.println("service: " + curService + " " + curService.getName());
            inner = curService.getPorts().iterator();
            while (inner.hasNext()) {
                curPort = (Port) inner.next();
                System.out.println("\tport: " + curPort + " " + curPort.getPortNumber());
            }
        }
        return null;
    }

}
