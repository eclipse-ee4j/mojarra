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

package com.sun.faces.test.servlet30.dynamic;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import com.sun.faces.facelets.compiler.UIText;

/**
 * The managed bean for issue #4664.
 */
@ManagedBean(name = "issue4664Bean")
@RequestScoped
public class Issue4664Bean {

	/**
	 * Remove the component resources.
	 */
	public void removeFacet() {
		FacesContext context = FacesContext.getCurrentInstance();
		UIViewRoot viewRoot = context.getViewRoot();

		UIComponent c = viewRoot.findComponent("issue");

		UIOutput copy = null;
		
		// if facet is there, create a copy of the component and add the copy as child
		if (c.getFacet("remove") != null) {
			UIOutput facet = (UIOutput) c.getFacets().remove("remove");
			copy = new UIOutput();
			copy.setValue(facet.getValue());
			c.getChildren().add(copy);
		} else {
			// after postback facet should be removed, so no "cannot remove same component twice" error is thrown.
			// instead, the value of the copied component is changed here, so we can verify behavior.
			copy = (UIOutput) c.getChildren().get(0);
			copy.setValue("facet already removed");
		}
	}
}
