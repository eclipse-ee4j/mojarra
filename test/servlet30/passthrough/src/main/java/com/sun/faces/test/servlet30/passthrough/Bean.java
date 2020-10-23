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

package com.sun.faces.test.servlet30.passthrough;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class Bean implements Serializable {
    
    private String text1 = "text1";
    private String text2 = "text2";
    
    private String publicKey;
    
    private String publicKey2;

    public String getPublicKey2() {
        return publicKey2;
    }

    public void setPublicKey2(String publicKey2) {
        this.publicKey2 = publicKey2;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    private String email = "anybody@example.com";

    private boolean checkboxValue;

    private Integer number = 10;
    private List<String> list = Arrays.asList("1", "2", "3", "4", "5", "6", "7");
    private String selectOne = "2";
    private String selectOneSize2 = "3";
    private List<String> selectMany = Arrays.asList("4", "6");
    private String longText = "Long text";

    private String lastAction;

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public boolean getCheckboxValue() {
        return checkboxValue;
    }

    public void setCheckboxValue(boolean checkboxValue) {
        this.checkboxValue = checkboxValue;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }

    public void setSelectOne(String selectOne) {
        this.selectOne = selectOne;
    }

    public String getSelectOne() {
        return selectOne;
    }

    public void setSelectOneSize2(String selectOneSize2) {
        this.selectOneSize2 = selectOneSize2;
    }

    public String getSelectOneSize2() {
        return selectOneSize2;
    }

    public void setSelectMany(List<String> selectMany) {
        this.selectMany = selectMany;
    }

    public List<String> getSelectMany() {
        return selectMany;
    }

    public void setLongText(String longText) {
        this.longText = longText;
    }

    public String getLongText() {
        return longText;
    }

    public String action1() {
        lastAction = "action1";
        return null;
    }

    public String action2() {
        lastAction = "action2";
        return null;
    }
    
    public String getOutcome1() {
        return "outcome1";
    }

    public String getLastAction() {
        return lastAction;
    }

    private String min = "100";

    public void setMin(String min) {
        this.min = min;
    }

    public String getMin() {
        return min;
    }

    private String max = "500";

    public void setMax(String max) {
        this.max = max;
    }

    public String getMax() {
        return max;
    }
}
