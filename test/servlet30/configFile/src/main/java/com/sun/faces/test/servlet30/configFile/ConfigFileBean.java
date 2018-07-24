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

package com.sun.faces.test.servlet30.configFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@SessionScoped
public class ConfigFileBean {

    private String title = "Test Config File";

    public String getTitle() {
        return title;
    }

    public ConfigFileBean() {
    }

    public String getMapAndListPositive() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application app = fc.getApplication();

        ValueExpression valueExpression = app.getExpressionFactory().createValueExpression(fc.getELContext(), "#{simpleList}", List.class);
        assertNotNull(valueExpression);

        List list = (List) valueExpression.getValue(fc.getELContext());
        assertNotNull(list);

        assertEquals("simpleList size not as expected", 4, list.size());
        assertEquals("simpleList.get(0) not as expected",
                new Integer(10), list.get(0));
        assertEquals("simpleList.get(1) not as expected",
                new Integer(20), list.get(1));
        assertEquals("simpleList.get(2) not as expected",
                new Integer(60), list.get(2));
        assertNull("simpleList.get(3) not as expected", list.get(3));

        valueExpression = app.getExpressionFactory().createValueExpression(fc.getELContext(), "#{objectList}", List.class);
        assertNotNull(valueExpression);

        list = (List) valueExpression.getValue(fc.getELContext());
        assertNotNull(list);

        assertEquals("simpleList size not as expected", 4, list.size());
        assertTrue("simpleList.get(0) not as expected",
                list.get(0) instanceof SimpleBean);
        assertTrue("simpleList.get(1) not as expected",
                list.get(1) instanceof SimpleBean);
        assertTrue("simpleList.get(2) not as expected",
                list.get(2) instanceof SimpleBean);
        assertNull("simpleList.get(3) not as expected", list.get(3));

        valueExpression = app.getExpressionFactory().createValueExpression(fc.getELContext(), "#{floatMap}", Map.class);
        assertNotNull(valueExpression);

        Map nestedMap = null,
                map = (Map) valueExpression.getValue(fc.getELContext());
        assertNotNull(map);

        Iterator keys = map.keySet().iterator();
        Float key1 = new Float(3.1415),
                key2 = new Float(3.14),
                key3 = new Float(6.02),
                key4 = new Float(0.00001);
        Object curKey = null,
                value = null;

        while (keys.hasNext()) {
            assertTrue((curKey = keys.next()) instanceof Float);
            if (null != (value = map.get(curKey))) {
                assertTrue(value instanceof SimpleBean);
            }
        }

        assertTrue("map.get(key1) not a SimpleBean",
                map.get(key1) instanceof SimpleBean);
        assertTrue("map.get(key2) not a SimpleBean",
                map.get(key2) instanceof SimpleBean);
        assertTrue("map.get(key3) not a SimpleBean",
                map.get(key3) instanceof SimpleBean);
        assertNull("map.get(key4) not null", map.get(key4));

        valueExpression = app.getExpressionFactory().createValueExpression(fc.getELContext(), "#{crazyMap}", Map.class);
        assertNotNull(valueExpression);

        map = (Map) valueExpression.getValue(fc.getELContext());
        assertNotNull(map);

        keys = map.keySet().iterator();
        while (keys.hasNext()) {
            assertTrue((curKey = keys.next()) instanceof String);
            if (null != (value = map.get(curKey))) {
                assertTrue(value instanceof Map);
                nestedMap = (Map) value;
                assertTrue("nestedMap.get(key1) not a SimpleBean",
                        nestedMap.get(key1) instanceof SimpleBean);
                assertTrue("nestedMap.get(key2) not a SimpleBean",
                        nestedMap.get(key2) instanceof SimpleBean);
                assertTrue("nestedMap.get(key3) not a SimpleBean",
                        nestedMap.get(key3) instanceof SimpleBean);
                assertNull("nestedMap.get(key4) not null",
                        nestedMap.get(key4));
            }
        }
        assertTrue("map.get(one) not a Map",
                map.get("one") instanceof Map);
        assertTrue("map.get(two) not a Map",
                map.get("two") instanceof Map);
        assertNull("map.get(three) not null", map.get("three"));

        return "SUCCESS";
    }

    public String getMap1701() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application app = fc.getApplication();
        ExpressionFactory ef = app.getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(fc.getELContext(),
                "#{headAndFoot}", Map.class);
        Map headAndFoot = (Map) ve.getValue(fc.getELContext());
        assertNotNull(headAndFoot);
        Map banners = (Map) headAndFoot.get("banners");
        Object result = banners.get("headerUrl");
        assertNotNull(result);
        assertEquals("http://foo.utah.edu", result);
        result = banners.get("urlName");
        assertNotNull(result);
        assertEquals("Request For Change", result);

        return "SUCCESS";
    }

    private String status = "";

    public String getStatus() {
        return status;
    }
}
