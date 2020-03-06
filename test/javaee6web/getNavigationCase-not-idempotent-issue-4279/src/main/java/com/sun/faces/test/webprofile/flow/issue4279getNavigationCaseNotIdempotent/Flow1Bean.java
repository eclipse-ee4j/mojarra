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

package com.sun.faces.test.webprofile.flow.issue4279getNavigationCaseNotIdempotent;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.faces.flow.FlowScoped;
import javax.inject.Named;


/**
 * @author  Kyle Stiemann
 */
@Named
@FlowScoped(Flow1Bean.TITLE)
public class Flow1Bean implements Serializable {

	static final String TITLE = "flow1";
	private static final long serialVersionUID = 4380151863471254093L;

	@PreDestroy
	public void destroy() {
		System.out.println("Flow1Bean @PreDestroy Called.");
	}

	public String getTitle() {
		return TITLE;
	}

	@PostConstruct
	public void initialize() {
		System.out.println("Flow1Bean @PostConstruct Called.");
	}
}
