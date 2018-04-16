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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.el.ValueBinding;

import com.sun.faces.el.impl.beans.Factory;
import com.sun.faces.cactus.ServletFacesTestCase;

/**
 * @author jhook
 */
public class TestELImpl extends ServletFacesTestCase
{
    /**
     *  
     */
    public TestELImpl()
    {
        super();
    }

    /**
     * @param name
     */
    public TestELImpl(String name)
    {
        super(name);
    }

    protected Object evaluate(String ref) throws Exception
    {
        ValueBinding vb = this.getFacesContext().getApplication().createValueBinding(ref);
        return vb.getValue(this.getFacesContext());
    }

    public void testELEvaluation() throws Exception
    {
        /* setup our test date */
        Bean1 b = this.createBean1();
        getExternalContext().getRequestMap().put("bean1a", b);

        getExternalContext().getSessionMap().put("val1c", "session-scoped1");
        getExternalContext().getRequestMap().put("val1b", "request-scoped1");
        getExternalContext().getApplicationMap().put("val1d", "app-scoped1");
        
        Map m = new HashMap();
        m.put("emptyArray", new Object[0]);
        m.put("nonemptyArray", new Object[]
        { "abc" });
        m.put("emptyList", new ArrayList());
        {
            List l = new ArrayList();
            l.add("hello"); 
            m.put("nonemptyList", l);
        }
        m.put("emptyMap", new HashMap());
        {
            Map m2 = new HashMap();
            m2.put("a", "a");
            m.put("nonemptyMap", m2);
        }
        m.put("emptySet", new HashSet());
        {
            Set s = new HashSet();
            s.add("hello");
            m.put("nonemptySet", s);
        }
        getExternalContext().getRequestMap().put("emptyTests", m);

        getExternalContext().getRequestMap().put("pbean1",
                Factory.createBean1());
        getExternalContext().getRequestMap().put("pbean2",
                Factory.createBean2());
        getExternalContext().getRequestMap().put("pbean3",
                Factory.createBean3());
        getExternalContext().getRequestMap().put("pbean4",
                Factory.createBean4());
        getExternalContext().getRequestMap().put("pbean5",
                Factory.createBean5());
        getExternalContext().getRequestMap().put("pbean6",
                Factory.createBean6());
        getExternalContext().getRequestMap().put("pbean7",
                Factory.createBean7());

        /* testing mixture of strings and expressions */
        evaluateTest("abc", "abc");
        evaluateTest("#{ 3}", new Long(3));
        evaluateTestFailure("a#{");
        evaluateTest("a#{ 5 }", "a5");
        evaluateTest("#{ 3 }b", "3b");
        evaluateTest("#{ 1 }#{ 2 }", "12");
        evaluateTest("abc #{ 1} #{ 2} def", "abc 1 2 def");

        /* testing values that end with or contain "#" */
        evaluateTest("#", "#");
        
        evaluateTest("\\#", "#");
        evaluateTest("#  ", "#  ");
        evaluateTest("test#", "test#");
        evaluateTest("#test", "#test");
        evaluateTest("test#test", "test#test");
        evaluateTest("test###", "test###");
        evaluateTest("test###{ 34 }", "test##34");
        evaluateTest("test###{ 34 }##", "test##34##");
        evaluateTest("test##{ 34 }", "test#34");
        evaluateTest("##{ 34 }", "#34");
        evaluateTest("##", "##");
        evaluateTest("test##", "test##");
        evaluateTest("test##test", "test##test");
        evaluateTest("#{ 34 }##{ 34 }", "34#34");

        /* basic literals */
        evaluateTest("#{1}", new Long(1));
        evaluateTest("#{-12}", new Long(-12));
        evaluateTest("#{true}", Boolean.TRUE);
        evaluateTest("#{false}", Boolean.FALSE);
        evaluateTest("#{null}", null);
        evaluateTest("#{4.2}", new Double(4.2));
        evaluateTest("#{-21.3}", new Double(-21.3));
        evaluateTest("#{4.}", new Double(4.0));
        evaluateTest("#{.21}", new Double(0.21));
        evaluateTest("#{3e-1}", new Double(0.3));
        evaluateTest("#{.2222222222}", new Double(0.2222222222));

        /* basic relationals between literals */
        evaluateTest("#{1 < 2}", Boolean.TRUE);
        evaluateTest("#{1 > 2}", Boolean.FALSE);
        evaluateTest("#{1 >= 2}", Boolean.FALSE);
        evaluateTest("#{1 <= 2}", Boolean.TRUE);
        evaluateTest("#{1 == 2}", Boolean.FALSE);
        evaluateTest("#{1 != 2}", Boolean.TRUE);
        evaluateTest("#{3 >= 3}", Boolean.TRUE);
        evaluateTest("#{3 <= 3}", Boolean.TRUE);
        evaluateTest("#{3 == 3}", Boolean.TRUE);
        evaluateTest("#{3 < 3}", Boolean.FALSE);
        evaluateTest("#{3 > 3}", Boolean.FALSE);
        evaluateTest("#{3 != 3}", Boolean.FALSE);

        // PENDING (visvan) check with Kinman/Jacob
        /* relationals between booleans         
        evaluateTestFailure("#{false < true}");
        evaluateTestFailure("#{false > true}");
        evaluateTest("#{true >= true}", Boolean.TRUE);
        evaluateTest("#{true <= true}", Boolean.TRUE);
        evaluateTest("#{true == true}", Boolean.TRUE);
        evaluateTest("#{true != true}", Boolean.FALSE); */

        /* looking up objects in scopes */
        evaluateTest("#{requestScope.val1b}", "request-scoped1");
        evaluateTest("#{sessionScope.val1b}", null);
        evaluateTest("#{applicationScope.val1b}", null);
        evaluateTest("#{val1b}", "request-scoped1");

        evaluateTest("#{requestScope.val1c}", null);
        evaluateTest("#{sessionScope.val1c}", "session-scoped1");
        evaluateTest("#{applicationScope.val1c}", null);
        evaluateTest("#{val1c}", "session-scoped1");

        evaluateTest("#{requestScope.val1d}", null);
        evaluateTest("#{sessionScope.val1d}", null);
        evaluateTest("#{applicationScope.val1d}", "app-scoped1");
        evaluateTest("#{val1d}", "app-scoped1");

        /* accessing properties */
        evaluateTest("#{bean1a.int1}", new Integer(b.getInt1()));
        evaluateTest("#{bean1a.boolean1}", Boolean.valueOf(b.getBoolean1()));
        evaluateTest("#{bean1a.string1}", b.getString1());
        evaluateTest("#{bean1a.bean1.int2}", b.getBean1().getInt2());
        evaluateTest("#{bean1a.bean1.bean2.string2}", b.getBean1().getBean2()
                .getString2());
        evaluateTest("#{bean1a.byte1}", new Byte(b.getByte1()));
        evaluateTest("#{bean1a.char1}", new Character(b.getChar1()));
        evaluateTest("#{bean1a.short1}", new Short(b.getShort1()));
        evaluateTest("#{bean1a.long1}", new Long(b.getLong1()));
        evaluateTest("#{bean1a.float1}", new Float(b.getFloat1()));
        evaluateTest("#{bean1a.double1}", new Double(b.getDouble1()));

        /* test the entire relational comparison type promotion matrix */
        evaluateTest("#{bean1a.byte1 < bean1a.byte1}", Boolean.FALSE);
        evaluateTest("#{bean1a.byte1 < bean1a.char1}", Boolean.TRUE);
        evaluateTest("#{bean1a.byte1 < bean1a.short1}", Boolean.TRUE);
        evaluateTest("#{bean1a.byte1 < bean1a.int1}", Boolean.FALSE);
        evaluateTest("#{bean1a.byte1 < bean1a.long1}", Boolean.TRUE);
        evaluateTest("#{bean1a.byte1 < bean1a.float1}", Boolean.TRUE);
        evaluateTest("#{bean1a.byte1 < bean1a.double1}", Boolean.TRUE);

        evaluateTest("#{bean1a.char1 < bean1a.byte1}", Boolean.FALSE);
        evaluateTest("#{bean1a.char1 < bean1a.char1}", Boolean.FALSE);
        evaluateTest("#{bean1a.char1 < bean1a.short1}", Boolean.FALSE);
        evaluateTest("#{bean1a.char1 < bean1a.int1}", Boolean.FALSE);
        evaluateTest("#{bean1a.char1 < bean1a.long1}", Boolean.FALSE);
        evaluateTest("#{bean1a.char1 < bean1a.float1}", Boolean.FALSE);
        evaluateTest("#{bean1a.char1 < bean1a.double1}", Boolean.FALSE);

        evaluateTest("#{bean1a.short1 < bean1a.byte1}", Boolean.FALSE);
        evaluateTest("#{bean1a.short1 < bean1a.char1}", Boolean.FALSE);
        evaluateTest("#{bean1a.short1 < bean1a.short1}", Boolean.FALSE);
        evaluateTest("#{bean1a.short1 < bean1a.int1}", Boolean.FALSE);
        evaluateTest("#{bean1a.short1 < bean1a.float1}", Boolean.FALSE);
        evaluateTest("#{bean1a.short1 < bean1a.long1}", Boolean.FALSE);
        evaluateTest("#{bean1a.short1 < bean1a.double1}", Boolean.FALSE);

        evaluateTest("#{bean1a.int1 < bean1a.byte1}", Boolean.TRUE);
        evaluateTest("#{bean1a.int1 < bean1a.char1}", Boolean.TRUE);
        evaluateTest("#{bean1a.int1 < bean1a.short1}", Boolean.TRUE);
        evaluateTest("#{bean1a.int1 < bean1a.int1}", Boolean.FALSE);
        evaluateTest("#{bean1a.int1 < bean1a.long1}", Boolean.TRUE);
        evaluateTest("#{bean1a.int1 < bean1a.float1}", Boolean.TRUE);
        evaluateTest("#{bean1a.int1 < bean1a.double1}", Boolean.TRUE);

        evaluateTest("#{bean1a.long1 < bean1a.byte1}", Boolean.FALSE);
        evaluateTest("#{bean1a.long1 < bean1a.char1}", Boolean.FALSE);
        evaluateTest("#{bean1a.long1 < bean1a.short1}", Boolean.FALSE);
        evaluateTest("#{bean1a.long1 < bean1a.int1}", Boolean.FALSE);
        evaluateTest("#{bean1a.long1 < bean1a.long1}", Boolean.FALSE);
        evaluateTest("#{bean1a.long1 < bean1a.float1}", Boolean.FALSE);
        evaluateTest("#{bean1a.long1 < bean1a.double1}", Boolean.FALSE);

        evaluateTest("#{bean1a.float1 < bean1a.byte1}", Boolean.FALSE);
        evaluateTest("#{bean1a.float1 < bean1a.char1}", Boolean.TRUE);
        evaluateTest("#{bean1a.float1 < bean1a.short1}", Boolean.TRUE);
        evaluateTest("#{bean1a.float1 < bean1a.int1}", Boolean.FALSE);
        evaluateTest("#{bean1a.float1 < bean1a.long1}", Boolean.TRUE);
        evaluateTest("#{bean1a.float1 < bean1a.float1}", Boolean.FALSE);
        evaluateTest("#{bean1a.float1 < bean1a.double1}", Boolean.TRUE);

        evaluateTest("#{bean1a.double1 < bean1a.byte1}", Boolean.FALSE);
        evaluateTest("#{bean1a.double1 < bean1a.char1}", Boolean.TRUE);
        evaluateTest("#{bean1a.double1 < bean1a.short1}", Boolean.TRUE);
        evaluateTest("#{bean1a.double1 < bean1a.int1}", Boolean.FALSE);
        evaluateTest("#{bean1a.double1 < bean1a.long1}", Boolean.TRUE);
        evaluateTest("#{bean1a.double1 < bean1a.float1}", Boolean.FALSE);
        evaluateTest("#{bean1a.double1 < bean1a.double1}", Boolean.FALSE);

        /* test other relational comparison rules */
        evaluateTest("#{null == null}", Boolean.TRUE);
        evaluateTest("#{noSuchAttribute == noSuchAttribute}", Boolean.TRUE);
        evaluateTest("#{noSuchAttribute == null}", Boolean.TRUE);
        evaluateTest("#{null == noSuchAttribute}", Boolean.TRUE);
        evaluateTest("#{bean1a == null}", Boolean.FALSE);
        evaluateTest("#{null == bean1a}", Boolean.FALSE);
        evaluateTest("#{bean1a == bean1a}", Boolean.TRUE);
        // JSF1.2 BI: This test used to fail with JSF 1.1 EL. With unified EL
        // the first portion of the expression is coerced to a string, so
        // the expression evaluates to true.
        evaluateTest("#{bean1a > \"hello\"}", Boolean.FALSE);
        evaluateTestFailure("#{bean1a.bean1 < 14}");
        evaluateTest("#{bean1a.bean1 == \"hello\"}", Boolean.FALSE);

        /* test String comparisons */
        evaluateTest("#{bean1a.string1 == \"hello\"}", Boolean.TRUE);
        evaluateTest("#{bean1a.string1 != \"hello\"}", Boolean.FALSE);
        evaluateTest("#{bean1a.string1 == \"goodbye\"}", Boolean.FALSE);
        evaluateTest("#{bean1a.string1 != \"goodbye\"}", Boolean.TRUE);
        evaluateTest("#{bean1a.string1 > \"goodbye\"}", Boolean.TRUE);
        evaluateTest("#{\"hello\" == bean1a.string1}", Boolean.TRUE);
        evaluateTest("#{\"goodbye\" > bean1a.string1}", Boolean.FALSE);

        /* test errors in property traversal */
        evaluateTest("#{noSuchAttribute.abc}", null);
        evaluateTest("#{bean1a.bean2.byte1}", null);
        evaluateTestFailure("#{bean1a.noProperty}");
        evaluateTestFailure("#{bean1a.noGetter}");
        evaluateTestFailure("#{bean1a.errorInGetter}");
        evaluateTest("#{bean1a.bean2.string2}", null);

        /* test accessing public properties from private classes */
        evaluateTest("#{pbean1.value}", "got the value");
        evaluateTest("#{pbean2.value}", "got the value");
        evaluateTest("#{pbean3.value}", "got the value");
        evaluateTest("#{pbean4.value}", "got the value");
        evaluateTest("#{pbean5.value}", "got the value");
        evaluateTest("#{pbean6.value}", "got the value");
        evaluateTest("#{pbean7.value}", "got the value");

        /* test reserved words as identifiers */
        evaluateTestFailure("#{and}");
        evaluateTestFailure("#{or}");
        evaluateTestFailure("#{not}");
        evaluateTestFailure("#{eq}");
        evaluateTestFailure("#{ne}");
        evaluateTestFailure("#{lt}");
        evaluateTestFailure("#{gt}");
        evaluateTestFailure("#{le}");
        evaluateTestFailure("#{ge}");
        evaluateTest("#{true}", Boolean.TRUE);
        evaluateTest("#{false}", Boolean.FALSE);
        evaluateTest("#{null}", null);

        /* test reserved words as property names */
        evaluateTestFailure("#{bean1a.and}");
        evaluateTestFailure("#{bean1a.or}");
        evaluateTestFailure("#{bean1a.not}"); 
        evaluateTestFailure("#{bean1a.eq}");
        evaluateTestFailure("#{bean1a.ne}");
        evaluateTestFailure("#{bean1a.lt}");
        evaluateTestFailure("#{bean1a.gt}");
        evaluateTestFailure("#{bean1a.le}");
        evaluateTestFailure("#{bean1a.ge}");
        evaluateTestFailure("#{bean1a.instanceof}");
        evaluateTestFailure("#{bean1a.page}");
        evaluateTestFailure("#{bean1a.request}");
        evaluateTestFailure("#{bean1a.session}");
        evaluateTestFailure("#{bean1a.application}");
        evaluateTestFailure("#{bean1a.true}");
        evaluateTestFailure("#{bean1a.false}");
        evaluateTestFailure("#{bean1a.null}");

        /* test arithmetic */
        evaluateTest("#{3+5}", new Long(8));
        evaluateTest("#{3-5}", new Long(-2));
        evaluateTest("#{3/5}", new Double(0.6));
        evaluateTest("#{3*5}", new Long(15));
        evaluateTest("#{3*5.0}", new Double(15.0));
        evaluateTest("#{3.0*5}", new Double(15.0));
        evaluateTest("#{3.0*5.0}", new Double(15.0));
        evaluateTest("#{225 % 17}", new Long(4));
        evaluateTest("#{ 1 + 2 + 3 * 5 + 6}", new Long(24));
        evaluateTest("#{ 1 + (2 + 3) * 5 + 6}", new Long(32));

        /* test logical operators */
        evaluateTest("#{ true}", Boolean.TRUE);
        evaluateTest("#{ not true}", Boolean.FALSE);
        evaluateTest("#{ not false}", Boolean.TRUE);
        evaluateTest("#{ not not true}", Boolean.TRUE);
        evaluateTest("#{ not not false}", Boolean.FALSE);
        evaluateTest("#{ true and false}", Boolean.FALSE);
        evaluateTest("#{ true and true}", Boolean.TRUE);
        evaluateTest("#{ false and true}", Boolean.FALSE);
        evaluateTest("#{ false and false}", Boolean.FALSE);
        evaluateTest("#{ true or false}", Boolean.TRUE);
        evaluateTest("#{ true or true}", Boolean.TRUE);
        evaluateTest("#{ false or true}", Boolean.TRUE);
        evaluateTest("#{ false or false}", Boolean.FALSE);
        evaluateTest("#{ false or false or false or true and false}",
                Boolean.FALSE);
        evaluateTest("#{ false or false or false or true and false or true}",
                Boolean.TRUE);

        /* test indexed access operator */
        evaluateTest("#{ bean1a[\"double1\"] }", new Double(b.getDouble1()));
        evaluateTest("#{ bean1a[\"double1\"].class }", Double.class);
        evaluateTest("#{ bean1a.stringArray1[-1]}", null);
        evaluateTest("#{ bean1a.stringArray1[0]}", b.getStringArray1()[0]);
        evaluateTest("#{ bean1a.stringArray1[1]}", b.getStringArray1()[1]);
        evaluateTest("#{ bean1a.stringArray1[2]}", b.getStringArray1()[2]);
        evaluateTest("#{ bean1a.stringArray1[3]}", b.getStringArray1()[3]);
        evaluateTest("#{ bean1a.stringArray1[4]}", null);

        
        /* Test as list accessor */
        evaluateTest("#{ bean1a.list1 [0] }", b.getList1().get(0));
        evaluateTest("#{ bean1a.list1 [1] }", b.getList1().get(1));
        evaluateTest("#{ bean1a.list1 [2][2] }", "string3");

        
        /* Test as indexed property accessor */
        evaluateTestFailure("#{ bean1a.indexed1[-1]}");
        evaluateTestFailure("#{ bean1a.indexed1[0]}");
        evaluateTestFailure("#{ bean1a.indexed1[1]}");
        evaluateTestFailure("#{ bean1a.indexed1[2]}");
        evaluateTestFailure("#{ bean1a.indexed1[3]}");
        evaluateTestFailure("#{ bean1a.indexed1[4]}");

        
        /* Test as map accessor */
        evaluateTest("#{ bean1a.map1.noKey }", null);
        evaluateTest("#{ bean1a.map1.key1 }", b.getMap1().get("key1"));
        evaluateTest("#{ bean1a.map1 [\"key1\"] }", b.getMap1().get("key1"));
        evaluateTest("#{ bean1a.map1 [14] }", "value3");
        evaluateTest("#{ bean1a.map1 [2 * 7] }", "value3");
        evaluateTest("#{ bean1a.map1.recurse.list1[0] }", new Integer(14));

        
        /* Test UIComponent as bean
        evaluateTest("#{view.rendered}", Boolean.TRUE);
        evaluateTest("#{view.attributes.rendered}", Boolean.TRUE);
        evaluateTest("#{view.children[0].value}", "inputValue");
        evaluateTest("#{view.children[0].rendered}", Boolean.TRUE);
         */
        
        /* test String concatenation */
        evaluateTestFailure("#{ \"a\" + \"bcd\" }");
        evaluateTestFailure("#{ \"a\" + (4*3) }");
        evaluateTestFailure("#{ bean1a.map1 [\"key\" + (5-4)] }");

        
        /* test String comparisons */
        evaluateTest("#{ \"30\" < \"4\" }", Boolean.TRUE);
        evaluateTest("#{ 30 < \"4\" }", Boolean.FALSE);
        evaluateTest("#{ 30 > \"4\" }", Boolean.TRUE);
        evaluateTest("#{ \"0004\" == \"4\" }", Boolean.FALSE);
        
        
        /* test relational comparison with alternate symbols */
        evaluateTest("#{ 4 eq 3}", Boolean.FALSE);
        evaluateTest("#{ 4 ne 3}", Boolean.TRUE);
        evaluateTest("#{ 4 eq 4}", Boolean.TRUE);
        evaluateTest("#{ 4 ne 4}", Boolean.FALSE);
        evaluateTest("#{ 4 lt 3}", Boolean.FALSE);
        evaluateTest("#{ 4 gt 3}", Boolean.TRUE);
        evaluateTest("#{ 4 le 3}", Boolean.FALSE);
        evaluateTest("#{ 4 ge 3}", Boolean.TRUE);
        evaluateTest("#{ 4 le 4}", Boolean.TRUE);
        evaluateTest("#{ 4 ge 4}", Boolean.TRUE);
        
        
        /* test expressions on the left side of a value suffix */
        evaluateTest("#{(3).class}", Long.class);
        evaluateTest("#{(bean1a.map1)[\"key1\"]}", "value1");
        
        /* test String/boolean logical operators */
        evaluateTest("#{'true' and false}", Boolean.FALSE);
        evaluateTest("#{'true' or true}", Boolean.TRUE);
        evaluateTest("#{false and 'true'}", Boolean.FALSE);
        evaluateTest("#{false or 'true'}", Boolean.TRUE);
        
        
        /* test empty operator */
        evaluateTest("#{ empty \"A\"}", Boolean.FALSE);
        evaluateTest("#{ empty \"\" }", Boolean.TRUE);
        evaluateTest("#{ empty null }", Boolean.TRUE);
        evaluateTest("#{ empty false}", Boolean.FALSE);
        evaluateTest("#{ empty 0}", Boolean.FALSE);
        evaluateTest("#{ not empty 0}", Boolean.TRUE);
        evaluateTest("#{ not empty empty 0}", Boolean.TRUE);
        evaluateTest("#{ empty emptyTests.emptyArray }", Boolean.TRUE);
        evaluateTest("#{ empty emptyTests.nonemptyArray }", Boolean.FALSE);
        evaluateTest("#{ empty emptyTests.emptyList }", Boolean.TRUE);
        evaluateTest("#{ empty emptyTests.nonemptyList }", Boolean.FALSE);
        evaluateTest("#{ empty emptyTests.emptyMap }", Boolean.TRUE);
        evaluateTest("#{ empty emptyTests.nonemptyMap }", Boolean.FALSE);
        evaluateTest("#{ empty emptyTests.emptySet }", Boolean.TRUE);
        evaluateTest("#{ empty emptyTests.nonemptySet }", Boolean.FALSE);
        
        
        /* test String arithmetic */
        evaluateTest("#{ \"6\" / \"3\" }", new Double(2.0));
        evaluateTest("#{ 3 + \"4\" }", new Long(7));
        evaluateTest("#{ \"4\" + 3 }", new Long(7));
        evaluateTest("#{ 3 + \"4.5\" }", new Double(7.5));
        evaluateTest("#{ \"4.5\" + 3 }", new Double(7.5));
        evaluateTest("#{ 3.0 + 6.0}", new Double(9.0));
        evaluateTest("#{ 31121.0 * 61553.0 }", new Double(1.915590913E9));
        evaluateTest("#{ 31121 * 61553 }", new Long(1915590913));
        evaluateTest("#{ 65536 * 65536 * 65536 * 32759 }", new Long("9220838762064379904"));
        evaluateTest("#{ 9220838762064379904.0 - 9220838762064379900.0 }", new Double(0.0));
        evaluateTest("#{ 9220838762064379904 - 9220838762064379900 }", new Long(4));
        
        
        /* test relational operators involving null */
        evaluateTest("#{ null == null }", Boolean.TRUE);
        evaluateTest("#{ null != null }", Boolean.FALSE);
        evaluateTest("#{ null > null }", Boolean.FALSE);
        evaluateTest("#{ null < null }", Boolean.FALSE);
        evaluateTest("#{ null >= null }", Boolean.TRUE);
        evaluateTest("#{ null <= null }", Boolean.TRUE);
        evaluateTest("#{ null == 3 }", Boolean.FALSE);
        evaluateTest("#{ null != 3 }", Boolean.TRUE);
        evaluateTest("#{ null > 3 }", Boolean.FALSE);
        evaluateTest("#{ null < 3 }", Boolean.FALSE);
        evaluateTest("#{ null >= 3 }", Boolean.FALSE);
        evaluateTest("#{ null <= 3 }", Boolean.FALSE);
        evaluateTest("#{ 3 == null }", Boolean.FALSE);
        evaluateTest("#{ 3 != null }", Boolean.TRUE);
        evaluateTest("#{ 3 > null }", Boolean.FALSE);
        evaluateTest("#{ 3 < null }", Boolean.FALSE);
        evaluateTest("#{ 3 >= null }", Boolean.FALSE);
        evaluateTest("#{ 3 <= null }", Boolean.FALSE);
        evaluateTest("#{ null == \"\" }", Boolean.FALSE);
        evaluateTest("#{ null != \"\" }", Boolean.TRUE);
        evaluateTest("#{ \"\" == null }", Boolean.FALSE);
        evaluateTest("#{ \"\" != null }", Boolean.TRUE);
        
        
        /* arithmetic operators involving Strings */
        evaluateTest("#{ 4 + 3 }", new Long(7));
        evaluateTest("#{ 4.0 + 3 }", new Double(7.0));
        evaluateTest("#{ 4 + 3.0 }", new Double(7.0));
        evaluateTest("#{ 4.0 + 3.0 }", new Double(7.0));
        evaluateTest("#{ \"4\" + 3 }", new Long(7));
        evaluateTest("#{ \"4.0\" + 3 }", new Double(7.0));
        evaluateTest("#{ \"4\" + 3.0 }", new Double(7.0));
        evaluateTest("#{ \"4.0\" + 3.0 }", new Double(7.0));
        evaluateTest("#{ 4 + \"3\" }", new Long(7));
        evaluateTest("#{ 4.0 + \"3\" }", new Double(7.0));
        evaluateTest("#{ 4 + \"3.0\" }", new Double(7.0));
        evaluateTest("#{ 4.0 + \"3.0\" }", new Double(7.0));
        evaluateTest("#{ \"4\" + \"3\" }", new Long(7));
        evaluateTest("#{ \"4.0\" + \"3\" }", new Double(7.0));
        evaluateTest("#{ \"4\" + \"3.0\" }", new Double(7.0));
        evaluateTest("#{ \"4.0\" + \"3.0\" }", new Double(7.0));
        
        evaluateTest("#{ 4 - 3 }", new Long(1));
        evaluateTest("#{ 4.0 - 3 }", new Double(1.0));
        evaluateTest("#{ 4 - 3.0 }", new Double(1.0));
        evaluateTest("#{ 4.0 - 3.0 }", new Double(1.0));
        evaluateTest("#{ \"4\" - 3 }", new Long(1));
        evaluateTest("#{ \"4.0\" - 3 }", new Double(1.0));
        evaluateTest("#{ \"4\" - 3.0 }", new Double(1.0));
        evaluateTest("#{ \"4.0\" - 3.0 }", new Double(1.0));
        evaluateTest("#{ 4 - \"3\" }", new Long(1));
        evaluateTest("#{ 4.0 - \"3\" }", new Double(1.0));
        evaluateTest("#{ 4 - \"3.0\" }", new Double(1.0));
        evaluateTest("#{ 4.0 - \"3.0\" }", new Double(1.0));
        evaluateTest("#{ \"4\" - \"3\" }", new Long(1));
        evaluateTest("#{ \"4.0\" - \"3\" }", new Double(1.0));
        evaluateTest("#{ \"4\" - \"3.0\" }", new Double(1.0));
        evaluateTest("#{ \"4.0\" - \"3.0\" }", new Double(1.0));
        
        evaluateTest("#{ 4 * 3 }", new Long(12));
        evaluateTest("#{ 4.0 * 3 }", new Double(12.0));
        evaluateTest("#{ 4 * 3.0 }", new Double(12.0));
        evaluateTest("#{ 4.0 * 3.0 }", new Double(12.0));
        evaluateTest("#{ \"4\" * 3 }", new Long(12));
        evaluateTest("#{ \"4.0\" * 3 }", new Double(12.0));
        evaluateTest("#{ \"4\" * 3.0 }", new Double(12.0));
        evaluateTest("#{ \"4.0\" * 3.0 }", new Double(12.0));
        evaluateTest("#{ 4 * \"3\" }", new Long(12));
        evaluateTest("#{ 4.0 * \"3\" }", new Double(12.0));
        evaluateTest("#{ 4 * \"3.0\" }", new Double(12.0));
        evaluateTest("#{ 4.0 * \"3.0\" }", new Double(12.0));
        evaluateTest("#{ \"4\" * \"3\" }", new Long(12));
        evaluateTest("#{ \"4.0\" * \"3\" }", new Double(12.0));
        evaluateTest("#{ \"4\" * \"3.0\" }", new Double(12.0));
        evaluateTest("#{ \"4.0\" * \"3.0\" }", new Double(12.0));
        
        evaluateTest("#{ 4 / 3 }", new Double(4.0 / 3.0));
        evaluateTest("#{ 4.0 / 3 }", new Double(4.0 / 3.0));
        evaluateTest("#{ 4 / 3.0 }", new Double(4.0 / 3.0));
        evaluateTest("#{ 4.0 / 3.0 }", new Double(4.0 / 3.0));
        evaluateTest("#{ \"4\" / 3 }", new Double(4.0 / 3.0));
        evaluateTest("#{ \"4.0\" / 3 }", new Double(4.0 / 3.0));
        evaluateTest("#{ \"4\" / 3.0 }", new Double(4.0 / 3.0));
        evaluateTest("#{ \"4.0\" / 3.0 }", new Double(4.0 / 3.0));
        evaluateTest("#{ 4 / \"3\" }", new Double(4.0 / 3.0));
        evaluateTest("#{ 4.0 / \"3\" }", new Double(4.0 / 3.0));
        evaluateTest("#{ 4 / \"3.0\" }", new Double(4.0 / 3.0));
        evaluateTest("#{ 4.0 / \"3.0\" }", new Double(4.0 / 3.0));
        evaluateTest("#{ \"4\" / \"3\" }", new Double(4.0 / 3.0));
        evaluateTest("#{ \"4.0\" / \"3\" }", new Double(4.0 / 3.0));
        evaluateTest("#{ \"4\" / \"3.0\" }", new Double(4.0 / 3.0));
        evaluateTest("#{ \"4.0\" / \"3.0\" }", new Double(4.0 / 3.0));
        
        evaluateTest("#{ 4 % 3 }", new Long(1));
        evaluateTest("#{ 4.0 % 3 }", new Double(1.0));
        evaluateTest("#{ 4 % 3.0 }", new Double(1.0));
        evaluateTest("#{ 4.0 % 3.0 }", new Double(1.0));
        evaluateTest("#{ \"4\" % 3 }", new Long(1));
        evaluateTest("#{ \"4.0\" % 3 }", new Double(1.0));
        evaluateTest("#{ \"4\" % 3.0 }", new Double(1.0));
        evaluateTest("#{ \"4.0\" % 3.0 }", new Double(1.0));
        evaluateTest("#{ 4 % \"3\" }", new Long(1));
        evaluateTest("#{ 4.0 % \"3\" }", new Double(1.0));
        evaluateTest("#{ 4 % \"3.0\" }", new Double(1.0));
        evaluateTest("#{ 4.0 % \"3.0\" }", new Double(1.0));
        evaluateTest("#{ \"4\" % \"3\" }", new Long(1));
        evaluateTest("#{ \"4.0\" % \"3\" }", new Double(1.0));
        evaluateTest("#{ \"4\" % \"3.0\" }", new Double(1.0));
        evaluateTest("#{ \"4.0\" % \"3.0\" }", new Double(1.0));
        
        evaluateTest("#{ \"8\" / \"2\" }", new Double(4.0));
        evaluateTest("#{ \"4e2\" + \"3\" }", new Double(403));
        evaluateTest("#{ \"4\" + \"3e2\" }", new Double(304));
        evaluateTest("#{ \"4e2\" + \"3e2\" }", new Double(700));
        
        
        /* unary minus operator involving Strings */
        evaluateTest("#{ -3 }", new Long(-3));
        evaluateTest("#{ -3.0 }", new Double(-3.0));
        evaluateTest("#{ -\"3\" }", new Long(-3));
        evaluateTest("#{ -\"3.0\" }", new Double(-3));
        evaluateTest("#{ -\"3e2\" }", new Double(-300));
    }

    protected Bean1 createBean1()
    {
        Bean1 b1 = new Bean1();
        b1.setBoolean1(true);
        b1.setByte1((byte) 12);
        b1.setShort1((short) 98);
        b1.setChar1('b');
        b1.setInt1(4);
        b1.setLong1(98);
        b1.setFloat1((float) 12.4);
        b1.setDouble1(89.224);
        b1.setString1("hello");
        b1.setStringArray1(new String[]
        { "string1", "string2", "string3", "string4" });
        {
            List l = new ArrayList();
            l.add(new Integer(14));
            l.add("another value");
            l.add(b1.getStringArray1());
            b1.setList1(l);
        }
        {
            Map m = new HashMap();
            m.put("key1", "value1");
            m.put(new Integer(14), "value2");
            m.put(new Long(14), "value3");
            m.put("recurse", b1);
            b1.setMap1(m);
        }

        Bean1 b2 = new Bean1();
        b2.setInt2(new Integer(-224));
        b2.setString2("bean2's string");
        b1.setBean1(b2);

        Bean1 b3 = new Bean1();
        b3.setDouble1(1422.332);
        b3.setString2("bean3's string"); 
        b2.setBean2(b3);

        return b1;
    }

    public ExternalContext getExternalContext()
    {
        return getFacesContext().getExternalContext();
    }

    public void evaluateTestFailure(String ref) throws Exception
    {
        try
        {
            this.evaluate(ref);
            fail("'" + ref + "' should have thrown an exception");
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    public void evaluateTest(String ref, Object expectedValue) throws Exception
    {
        Object returnedValue = this.evaluate(ref);
        if (returnedValue != null)
        {
            Class expectedType = expectedValue.getClass();
            Class returnedType = returnedValue.getClass();
            String msg = "'" + ref + "' expected [" + expectedValue
                    + "] of type " + expectedType.getName()
                    + ", but returned [" + returnedValue + "] of type "
                    + returnedType.getName();
            assertTrue(msg, expectedType.isAssignableFrom(returnedType));
            assertEquals(msg, expectedValue, returnedValue);
        }
        else
        {
            String msg = "'" + ref + "' expected value [" + expectedValue
                    + "], but returned [" + returnedValue + "]";
            assertEquals(msg, expectedValue, returnedValue);
        }
    }
}
