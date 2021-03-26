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

// TestApplicationImpl_Config.java

package com.sun.faces.application;

import com.sun.faces.cactus.ServletFacesTestCase;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;

/**
 * <B>TestApplicationImpl_Config</B> is a class ...
 * <p/>
 * <B>Lifetime And Scope</B> <P>
 */

public class TestApplicationImpl_Config extends ServletFacesTestCase {

//
// Protected Constants
//

    public static String standardComponentTypes[] = {
        "jakarta.faces.Column",
        "jakarta.faces.Command",
        "jakarta.faces.Data",
        "jakarta.faces.Form",
        "jakarta.faces.Graphic",
        "jakarta.faces.Input",
        "jakarta.faces.Message",
        "jakarta.faces.Messages",
        "jakarta.faces.NamingContainer",
        "jakarta.faces.Output",
        "jakarta.faces.Panel",
        "jakarta.faces.Parameter",
        "jakarta.faces.SelectBoolean",
        "jakarta.faces.SelectItem",
        "jakarta.faces.SelectItems",
        "jakarta.faces.SelectMany",
        "jakarta.faces.SelectOne",
        "jakarta.faces.ViewRoot",
        "jakarta.faces.HtmlCommandButton",
        "jakarta.faces.HtmlCommandLink",
        "jakarta.faces.HtmlDataTable",
        "jakarta.faces.HtmlForm",
        "jakarta.faces.HtmlGraphicImage",
        "jakarta.faces.HtmlInputHidden",
        "jakarta.faces.HtmlInputSecret",
        "jakarta.faces.HtmlInputText",
        "jakarta.faces.HtmlInputTextarea",
        "jakarta.faces.HtmlMessage",
        "jakarta.faces.HtmlMessages",
        "jakarta.faces.HtmlOutputFormat",
        "jakarta.faces.HtmlOutputLabel",
        "jakarta.faces.HtmlOutputLink",
        "jakarta.faces.HtmlOutputText",
        "jakarta.faces.HtmlPanelGrid",
        "jakarta.faces.HtmlPanelGroup",
        "jakarta.faces.HtmlSelectBooleanCheckbox",
        "jakarta.faces.HtmlSelectManyCheckbox",
        "jakarta.faces.HtmlSelectManyListbox",
        "jakarta.faces.HtmlSelectManyMenu",
        "jakarta.faces.HtmlSelectOneListbox",
        "jakarta.faces.HtmlSelectOneMenu",
        "jakarta.faces.HtmlSelectOneRadio"
    };

    public static Class standardComponentClasses[] = {
        javax.faces.component.UIColumn.class,
        javax.faces.component.UICommand.class,
        javax.faces.component.UIData.class,
        javax.faces.component.UIForm.class,
        javax.faces.component.UIGraphic.class,
        javax.faces.component.UIInput.class,
        javax.faces.component.UIMessage.class,
        javax.faces.component.UIMessages.class,
        javax.faces.component.UINamingContainer.class,
        javax.faces.component.UIOutput.class,
        javax.faces.component.UIPanel.class,
        javax.faces.component.UIParameter.class,
        javax.faces.component.UISelectBoolean.class,
        javax.faces.component.UISelectItem.class,
        javax.faces.component.UISelectItems.class,
        javax.faces.component.UISelectMany.class,
        javax.faces.component.UISelectOne.class,
        javax.faces.component.UIViewRoot.class,
        javax.faces.component.html.HtmlCommandButton.class,
        javax.faces.component.html.HtmlCommandLink.class,
        javax.faces.component.html.HtmlDataTable.class,
        javax.faces.component.html.HtmlForm.class,
        javax.faces.component.html.HtmlGraphicImage.class,
        javax.faces.component.html.HtmlInputHidden.class,
        javax.faces.component.html.HtmlInputSecret.class,
        javax.faces.component.html.HtmlInputText.class,
        javax.faces.component.html.HtmlInputTextarea.class,
        javax.faces.component.html.HtmlMessage.class,
        javax.faces.component.html.HtmlMessages.class,
        javax.faces.component.html.HtmlOutputFormat.class,
        javax.faces.component.html.HtmlOutputLabel.class,
        javax.faces.component.html.HtmlOutputLink.class,
        javax.faces.component.html.HtmlOutputText.class,
        javax.faces.component.html.HtmlPanelGrid.class,
        javax.faces.component.html.HtmlPanelGroup.class,
        javax.faces.component.html.HtmlSelectBooleanCheckbox.class,
        javax.faces.component.html.HtmlSelectManyCheckbox.class,
        javax.faces.component.html.HtmlSelectManyListbox.class,
        javax.faces.component.html.HtmlSelectManyMenu.class,
        javax.faces.component.html.HtmlSelectOneListbox.class,
        javax.faces.component.html.HtmlSelectOneMenu.class,
        javax.faces.component.html.HtmlSelectOneRadio.class
    };

    public static String standardConverterIds[] = {
        "jakarta.faces.BigDecimal",
        "jakarta.faces.BigInteger",
        "jakarta.faces.Boolean",
        "jakarta.faces.Byte",
        "jakarta.faces.Character",
        "jakarta.faces.DateTime",
        "jakarta.faces.Double",
        "jakarta.faces.Float",
        "jakarta.faces.Integer",
        "jakarta.faces.Long",
        "jakarta.faces.Number",
        "jakarta.faces.Short"
    };
    public static Class standardConverterClasses[] = {
        javax.faces.convert.BigDecimalConverter.class,
        javax.faces.convert.BigIntegerConverter.class,
        javax.faces.convert.BooleanConverter.class,
        javax.faces.convert.ByteConverter.class,
        javax.faces.convert.CharacterConverter.class,
        javax.faces.convert.DateTimeConverter.class,
        javax.faces.convert.DoubleConverter.class,
        javax.faces.convert.FloatConverter.class,
        javax.faces.convert.IntegerConverter.class,
        javax.faces.convert.LongConverter.class,
        javax.faces.convert.NumberConverter.class,
        javax.faces.convert.ShortConverter.class
    };

    public static Class standardConverterByIdClasses[] = {
        java.math.BigDecimal.class,
        java.math.BigInteger.class,
        java.lang.Boolean.class,
        java.lang.Byte.class,
        java.lang.Character.class,
        null,
        java.lang.Double.class,
        java.lang.Float.class,
        java.lang.Integer.class,
        java.lang.Long.class,
        null,
        java.lang.Short.class
    };

    public static Class standardConverterPrimitiveClasses[] = {
        null,
        null,
        java.lang.Boolean.TYPE,
        java.lang.Byte.TYPE,
        java.lang.Character.TYPE,
        null,
        java.lang.Double.TYPE,
        java.lang.Float.TYPE,
        java.lang.Integer.TYPE,
        java.lang.Long.TYPE,
        null,
        java.lang.Short.TYPE
    };


//
// Class Variables
//

//
// Instance Variables
//
    private Application application = null;

// Attribute Instance Variables

// Relationship Instance Variables

//
// Constructors and Initializers    
//

    public TestApplicationImpl_Config() {
        super("TestApplicationImpl_Config");
    }


    public TestApplicationImpl_Config(String name) {
        super(name);
    }
//
// Class methods
//

//
// General Methods
//

    public void setUp() {
        super.setUp();
        ApplicationFactory aFactory =
            (ApplicationFactory) FactoryFinder.getFactory(
                FactoryFinder.APPLICATION_FACTORY);
        application = (Application) aFactory.getApplication();
    }
	
    //****
    //**** NOTE: We should add a test for finding a faces-config.xml file under 
    //****       WEB-INF/classes/META-INF.
    //****

    //
    // Test Config related methods
    //

    public void testComponentPositive() {
//        TestComponent
//            newTestComponent = null,
//            testComponent = new TestComponent();
//        UIComponent uic = null;
//
//        // runtime addition
//
//        application.addComponent(testComponent.getComponentType(),
//                                 "com.sun.faces.TestComponent");
//        assertTrue(
//            null !=
//            (newTestComponent =
//             (TestComponent)
//            application.createComponent(testComponent.getComponentType())));
//        assertTrue(newTestComponent != testComponent);
//
//        // built-in components
//        for (int i = 0, len = standardComponentTypes.length; i < len; i++) {
//            assertTrue(null != (uic =
//                                application.createComponent(
//                                    standardComponentTypes[i])));
//            assertTrue(
//                standardComponentClasses[i].isAssignableFrom(uic.getClass()));
//        }
    }


    public void testComponentNegative() {
//        boolean exceptionThrown = false;
//
//        // componentType/componentClass with non-existent class
//        try {
//            application.addComponent("William",
//                                     "BillyBoy");
//            application.createComponent("William");
//        } catch (FacesException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        // non-existent mapping
//        exceptionThrown = false;
//        try {
//            application.createComponent("Joebob");
//        } catch (FacesException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
    }


    public void testGetComponentTypes() {
//        Iterator iter = application.getComponentTypes();
//        assertTrue(null != iter);
//
//        assertTrue(isSubset(standardComponentTypes, iter));
    }


    public void testConverterPositive() {
//        TestConverter
//            newTestConverter = null,
//            testConverter = new TestConverter();
//        Converter conv = null;
//
//        // runtime addition
//
//        application.addConverter(testConverter.getConverterId(),
//                                 "com.sun.faces.TestConverter");
//        assertTrue(
//            null !=
//            (newTestConverter =
//             (TestConverter)
//            application.createConverter(testConverter.getConverterId())));
//        assertTrue(newTestConverter != testConverter);
//
//        // built-in components
//
//        // by-id
//        for (int i = 0, len = standardConverterIds.length; i < len; i++) {
//            assertTrue(null != (conv =
//                                application.createConverter(
//                                    standardConverterIds[i])));
//            assertTrue(
//                standardConverterClasses[i].isAssignableFrom(conv.getClass()));
//        }
//
//        // by-class
//        for (int i = 0, len = standardConverterByIdClasses.length; i < len; i++) {
//            // skip entries for which by-class registation doesn't make sense.
//            if (null == standardConverterByIdClasses[i]) {
//                continue;
//            }
//            assertTrue("null != " + standardConverterByIdClasses[i].toString(),
//                       null !=
//                       (conv =
//                        application.createConverter(
//                            standardConverterByIdClasses[i])));
//            assertTrue(
//                standardConverterClasses[i].isAssignableFrom(conv.getClass()));
//        }
//
//        // primitive classes
//        for (int i = 0, len = standardConverterPrimitiveClasses.length; i <
//            len; i++) {
//            if (null == standardConverterPrimitiveClasses[i]) {
//                continue;
//            }
//
//            assertTrue(
//                "null != " + standardConverterPrimitiveClasses[i].toString(),
//                null !=
//                (conv =
//                 application.createConverter(
//                     standardConverterPrimitiveClasses[i])));
//            assertTrue(
//                standardConverterClasses[i].isAssignableFrom(conv.getClass()));
//        }
    }


    public void testConverterNegative() {
//        boolean exceptionThrown = false;
//
//        // componentType/componentClass with non-existent class
//        try {
//            application.addConverter("William",
//                                     "BillyBoy");
//            application.createConverter("William");
//        } catch (FacesException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        // non-existent mapping
//        exceptionThrown = false;
//        try {
//            application.createConverter("Joebob");
//        } catch (FacesException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);

    }


    public void testGetConverterIds() {
//        Iterator iter = application.getConverterIds();
//        assertTrue(null != iter);
//
//        assertTrue(isSubset(standardConverterIds, iter));
    }


    public void testValidatorPositive() {
//        Validator
//            newTestValidator = null,
//            testValidator = new LengthValidator();
//        Validator val = null;
//
//        // runtime addition
//
//        application.addValidator("Billybob",
//                                 "jakarta.faces.validator.LengthValidator");
//        assertTrue(null != (newTestValidator =
//            application.createValidator("Billybob")));
//        assertTrue(newTestValidator != testValidator);
//
//        // test standard components
//        assertTrue(
//            null !=
//            (val = application.createValidator("jakarta.faces.DoubleRange")));
//        assertTrue(val instanceof Validator);
//        assertTrue(
//            null != (val = application.createValidator("jakarta.faces.Length")));
//        assertTrue(val instanceof Validator);
//        assertTrue(
//            null !=
//            (val = application.createValidator("jakarta.faces.LongRange")));
//        assertTrue(val instanceof Validator);

    }


    public void testValidatorNegative() {
//        boolean exceptionThrown = false;
//
//        // componentType/componentClass with non-existent class
//        try {
//            application.addValidator("William",
//                                     "BillyBoy");
//            application.createValidator("William");
//        } catch (FacesException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
//        // non-existent mapping
//        exceptionThrown = false;
//        try {
//            application.createValidator("Joebob");
//        } catch (FacesException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);

    }


    public void testGetValidatorIds() {
//        Iterator iter = application.getValidatorIds();
//        assertTrue(null != iter);
//        String standardValidatorIds[] = {
//            "jakarta.faces.DoubleRange",
//            "jakarta.faces.Length",
//            "jakarta.faces.LongRange"
//        };
//
//        assertTrue(isSubset(standardValidatorIds, iter));
    }


    public void testUpdateRuntimeComponents() {
//        loadFromInitParam("/runtime-components.xml");
//        ApplicationFactory aFactory =
//            (ApplicationFactory) FactoryFinder.getFactory(
//                FactoryFinder.APPLICATION_FACTORY);
//        application = (ApplicationImpl) aFactory.getApplication();
//
//        ActionListener actionListener = null;
//        NavigationHandler navHandler = null;
//        PropertyResolver propResolver = null;
//        VariableResolver varResolver = null;
//        ViewHandler viewHandler = null;
//        StateManager stateManager = null;
//
//        assertTrue(null != (actionListener =
//                            application.getActionListener()));
//        assertTrue(actionListener instanceof com.sun.faces.TestActionListener);
//
//        assertTrue(null != (navHandler =
//                            application.getNavigationHandler()));
//        assertTrue(navHandler instanceof com.sun.faces.TestNavigationHandler);
//
//        // JSF1.2 BI: application.getPropertyResolver() no longer returns the 
//        // head of the PropertyResolver. Instead returns the head of the 
//        // ELResolver stack wrapped in a PropertyResolver.This also applies to
//        // VariableResolver
//        assertTrue(null != (propResolver =
//                            application.getPropertyResolver()));
//        assertTrue(
//            application.getPropertyResolver() instanceof jakarta.faces.el.PropertyResolver);
//        assertTrue(null != (varResolver =
//                            application.getVariableResolver()));
//        assertTrue(varResolver instanceof jakarta.faces.el.VariableResolver);
//
//        assertTrue(null != (viewHandler =
//                            application.getViewHandler()));
//        assertTrue(viewHandler instanceof jakarta.faces.application.ViewHandler);
//
//        assertTrue(null != (stateManager =
//                            application.getStateManager()));
//        assertTrue(
//            stateManager instanceof jakarta.faces.application.StateManager);
//        System.out.println("DEFAULT:" + application.getDefaultRenderKitId());
//        assertEquals("WackyRenderKit", application.getDefaultRenderKitId());
    }


    public void testLocaleConfigPositive() {
//        loadFromInitParam("/locale-config.xml");
//        ApplicationFactory aFactory =
//            (ApplicationFactory) FactoryFinder.getFactory(
//                FactoryFinder.APPLICATION_FACTORY);
//        application = (ApplicationImpl) aFactory.getApplication();
//
//        Locale locale;
//
//        assertNotNull("Can't get default locale from Application",
//                      locale = application.getDefaultLocale());
//        assertEquals(Locale.US, locale);
//
//        Iterator iter;
//        int j = 0, len = 0;
//        boolean found = false;
//        String[][] expected = {
//            {"de", "DE"},
//            {"en", "US"},
//            {"fr", "FR"},
//            {"ps", "PS"}
//        };
//        len = expected.length;
//
//        iter = application.getSupportedLocales();
//        System.out.println("actual supported locales: ");
//        while (iter.hasNext()) {
//            System.out.println(iter.next().toString());
//        }
//
//
//        // test that the supported locales are a superset of the
//        // expected locales
//        for (j = 0; j < len; j++) {
//            assertNotNull("Can't get supportedLocales from Application",
//                          iter = application.getSupportedLocales());
//            found = false;
//            while (iter.hasNext()) {
//                locale = (Locale) iter.next();
//                if (expected[j][0].equals(locale.getLanguage()) &&
//                    expected[j][1].equals(locale.getCountry())) {
//                    found = true;
//                }
//            }
//            assertTrue("Can't find expected locale " + expected[j][0] + "_" +
//                       expected[j][1] + " in supported-locales list",
//                       found);
//        }
//
    }


    public void testLocaleConfigNegative2() {
//        boolean exceptionThrown = false;
//        try {
//            loadFromInitParam("/locale-config2.xml");
//        } catch (Throwable e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//
    }


} // end of class TestApplicationImpl_Config
