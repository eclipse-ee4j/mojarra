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

package com.sun.faces.test.javaee8.uiinput;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Model;

@Model
public class Spec1422 {

    public enum Item {
        ONE, TWO, THREE;
    }

    private List<Item> selectedItems;
    private static List<Item> availableItems = Arrays.asList(Item.values());
    private List<Object> selectedNumbers;
    private static List<? extends Number> availableNumbers = Arrays.asList(null, 1, 2L, new BigInteger("3"), 4.5, 6.7F, new BigDecimal("8.9")); 

    public void submit() {
        for (Item item : selectedItems) {
            // Should not throw ClassCastException here.
        }
        
        // Should nowhere throw ClassCastException here (assuming every item is selected).
        Integer one = (Integer) selectedNumbers.get(1);
        Long two = (Long) selectedNumbers.get(2);
        BigInteger three = (BigInteger) selectedNumbers.get(3);
        Double fourDotFive = (Double) selectedNumbers.get(4);
        Float sixDotSeven = (Float) selectedNumbers.get(5);
        BigDecimal eightDotNine = (BigDecimal) selectedNumbers.get(6);
    }

    public List<Item> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<Item> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public List<Item> getAvailableItems() {
        return availableItems;
    }
    
    public List<Object> getSelectedNumbers() {
        return selectedNumbers;
    }
    
    public void setSelectedNumbers(List<Object> selectedNumbers) {
        this.selectedNumbers = selectedNumbers;
    }
    
    public List<? extends Number> getAvailableNumbers() {
        return availableNumbers;
    }

}
