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

package jakarta.faces.model;

import java.util.Map;
import com.sun.faces.mock.MockResult;

import jakarta.faces.model.ResultDataModel;

import static junit.framework.Assert.assertTrue;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link ResultDataModel}.</p>
 */
public class ResultDataModelTestCase extends DataModelTestCaseBase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ResultDataModelTestCase(String name) {

        super(name);

    }

    // ------------------------------------------------------ Instance Variables
    // The Result passed to our ResultDataModel
    protected MockResult result = null;

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        beans = new BeanTestImpl[5];
        for (int i = 0; i < beans.length; i++) {
            beans[i] = new BeanTestImpl();
        }
        configure();
        result = new MockResult(beans);
        model = new ResultDataModel(result);
        super.setUp();
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(ResultDataModelTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    // ------------------------------------------------------- Protected Methods
    @Override
    protected BeanTestImpl data() throws Exception {
        Object data = model.getRowData();
        assertTrue(data instanceof Map);
        BeanTestImpl bean = new BeanTestImpl();
        Map map = (Map) data;

        bean.setBooleanProperty(((Boolean) map.get("booleanProperty")).booleanValue());
        bean.setBooleanSecond(((Boolean) map.get("booleanSecond")).booleanValue());
        bean.setByteProperty(((Byte) map.get("byteProperty")).byteValue());
        bean.setDoubleProperty(((Double) map.get("doubleProperty")).doubleValue());
        bean.setFloatProperty(((Float) map.get("floatProperty")).floatValue());
        bean.setIntProperty(((Integer) map.get("intProperty")).intValue());
        bean.setLongProperty(((Long) map.get("longProperty")).longValue());
        bean.setNullProperty((String) map.get("nullProperty"));
        bean.setShortProperty(((Short) map.get("shortProperty")).shortValue());
        bean.setStringProperty((String) map.get("stringProperty"));
        bean.setWriteOnlyProperty((String) map.get("writeOnlyPropertyValue"));

        return (bean);
    }
}
