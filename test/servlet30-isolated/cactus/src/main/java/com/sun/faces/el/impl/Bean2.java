/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.faces.el.impl;

/**
 * <p>This is a test bean that holds a single String
 *
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: ofung $
 */

public class Bean2 {

    //-------------------------------------
    // Properties
    //-------------------------------------
    // property value

    String mValue;


    public String getValue() {
        return mValue;
    }


    public void setValue(String pValue) {
        mValue = pValue;
    }

    //-------------------------------------
    // Member variables
    //-------------------------------------

    //-------------------------------------
    /**
     * Constructor
     */
    public Bean2(String pValue) {
        mValue = pValue;
    }


    //-------------------------------------
    public String toString() {
        return ("Bean2[" + mValue + "]");
    }

    //-------------------------------------

}
