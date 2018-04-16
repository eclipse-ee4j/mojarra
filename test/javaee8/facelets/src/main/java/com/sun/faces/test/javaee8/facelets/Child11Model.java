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

package com.sun.faces.test.javaee8.facelets;

import javax.faces.model.DataModel;
import javax.faces.model.FacesDataModel;

@FacesDataModel(forClass = Child11.class)
public class Child11Model<E> extends DataModel<E> {

	private Child11 child;
	private int rowIndex = 0;
	
	@Override
	public int getRowCount() {
		return 2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E getRowData() {
		return rowIndex == 0? (E) child.getTest() : (E) child.getTest2();
	}

	@Override
	public int getRowIndex() {
		return rowIndex;
	}

	@Override
	public Object getWrappedData() {
		return child;
	}

	@Override
	public boolean isRowAvailable() {
		return rowIndex < 2;
	}

	@Override
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
		
	}

	@Override
	public void setWrappedData(Object arg0) {
		child = (Child11) arg0;
	}

}
