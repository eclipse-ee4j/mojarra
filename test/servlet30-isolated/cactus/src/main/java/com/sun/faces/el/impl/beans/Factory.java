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

package com.sun.faces.el.impl.beans;

/**
 * <p>A factory for generating the various beans
 *
 * @author Nathan Abramson - Art Technology Group
 * @version $Change: 181181 $$DateTime: 2001/06/26 09:55:09 $$Author: ofung $
 */

public class Factory {

    public static PublicBean1 createBean1() {
        return new PublicBean1();
    }


    public static PublicBean1 createBean2() {
        return new PrivateBean1a();
    }


    public static PublicBean1 createBean3() {
        return new PublicBean1b();
    }


    public static PublicInterface2 createBean4() {
        return new PublicBean2a();
    }


    public static PublicInterface2 createBean5() {
        return new PrivateBean2b();
    }


    public static PublicInterface2 createBean6() {
        return new PrivateBean2c();
    }


    public static PublicInterface2 createBean7() {
        return new PrivateBean2d();
    }
}
