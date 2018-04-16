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

package com.sun.faces.test.servlet31.facelets;

import java.util.Arrays;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean(name = "repeatNestedBean")
@RequestScoped
public class RepeatNestedBean {

    private final Cell[][] matrix = new Cell[2][2];

    public RepeatNestedBean() {
        for (Cell[] matrix1 : matrix) {
            for (int y = 0; y < matrix1.length; y++) {
                matrix1[y] = new Cell();
            }
        }
    }

    public void refresh() {
        matrix[0][0].setValue(true);
        System.out.println("Refresh: " + Arrays.deepToString(matrix));
    }

    public Cell[][] getMatrix() {
        return matrix;
    }

    public static class Cell {

        private Boolean value = false;

        public Boolean getValue() {
            return this.value;
        }

        public void setValue(Boolean value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
