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

package com.sun.faces.test.servlet30.configBasic;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIColumn;
import javax.faces.component.UICommand;
import javax.faces.component.UIData;
import javax.faces.component.UIForm;
import javax.faces.component.UIGraphic;
import javax.faces.component.UIInput;
import javax.faces.component.UIMessage;
import javax.faces.component.UIMessages;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIParameter;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlInputSecret;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.component.html.HtmlMessage;
import javax.faces.component.html.HtmlMessages;
import javax.faces.component.html.HtmlOutputFormat;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.component.html.HtmlSelectManyListbox;
import javax.faces.component.html.HtmlSelectManyMenu;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.convert.BigDecimalConverter;
import javax.faces.convert.BigIntegerConverter;
import javax.faces.convert.BooleanConverter;
import javax.faces.convert.ByteConverter;
import javax.faces.convert.CharacterConverter;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.FloatConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.convert.NumberConverter;
import javax.faces.convert.ShortConverter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import javax.faces.validator.DoubleRangeValidator;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.LongRangeValidator;


import javax.faces.context.FacesContext;
import javax.el.ExpressionFactory;
import javax.faces.el.ValueBinding;
import javax.el.ValueExpression;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ConfigFileBean {

    private String title = "Test Config File";
    public String getTitle() {
        return title; 
    }

    public ConfigFileBean() {
    }

    public String getBasic() throws Exception {
        checkComponentsGeneric();
        checkComponentsHtml();
        checkConvertersByClass();
        checkConvertersById();
        checkRenderers();
        checkValidators();

        return "SUCCESS";
    }

    public String getBool() throws Exception {
        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        // Test for isRendersChildren=false
        for (int i = 0; i < rendersChildrenFalse.length; i++) {
            Renderer r = rk.getRenderer(rendersChildrenFalse[i][0],
                                        rendersChildrenFalse[i][1]);
            assertEquals("(" + rendersChildrenFalse[i][0] + "," +
                         rendersChildrenFalse[i][1] + ")", false,
                         r.getRendersChildren());
        }

        // Test for isRendersChildren=true
        for (int i = 0; i < rendersChildrenTrue.length; i++) {
            Renderer r = rk.getRenderer(rendersChildrenTrue[i][0],
                                        rendersChildrenTrue[i][1]);
            assertEquals("(" + rendersChildrenTrue[i][0] + "," +
                         rendersChildrenTrue[i][1] + ")", true,
                         r.getRendersChildren());
        }

        return "SUCCESS";
    }

    // Test a webapp with a default faces-config.xml resource
    public String getDef() throws Exception {
        // Validate standard configuration
        checkComponentsGeneric();
        checkComponentsHtml();
        checkConvertersByClass();
        checkConvertersById();
        checkRenderers();
        checkValidators();

        // Validate what was actually configured
        checkDefaultConfiguration();
        checkExtraConfiguration(false);
        checkEmbedConfiguration(false);

        return "SUCCESS";
    }


    // Check that all of the required generic components have been registered
    private void checkComponentsGeneric() throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        assertTrue(application.createComponent
                   ("jakarta.faces.Column") instanceof UIColumn);
        assertTrue(application.createComponent
                   (UIColumn.COMPONENT_TYPE) instanceof UIColumn);
        assertTrue(application.createComponent
                   ("jakarta.faces.Command") instanceof UICommand);
        assertTrue(application.createComponent
                   (UICommand.COMPONENT_TYPE) instanceof UICommand);
        assertTrue(application.createComponent
                   ("jakarta.faces.Data") instanceof UIData);
        assertTrue(application.createComponent
                   (UIData.COMPONENT_TYPE) instanceof UIData);
        assertTrue(application.createComponent
                   ("jakarta.faces.Form") instanceof UIForm);
        assertTrue(application.createComponent
                   (UIForm.COMPONENT_TYPE) instanceof UIForm);
        assertTrue(application.createComponent
                   ("jakarta.faces.Graphic") instanceof UIGraphic);
        assertTrue(application.createComponent
                   (UIGraphic.COMPONENT_TYPE) instanceof UIGraphic);
        assertTrue(application.createComponent
                   ("jakarta.faces.Input") instanceof UIInput);
        assertTrue(application.createComponent
                   (UIInput.COMPONENT_TYPE) instanceof UIInput);
        assertTrue(application.createComponent
                   ("jakarta.faces.Message") instanceof UIMessage);
        assertTrue(application.createComponent
                   (UIMessage.COMPONENT_TYPE) instanceof UIMessage);
        assertTrue(application.createComponent
                   ("jakarta.faces.Messages") instanceof UIMessages);
        assertTrue(application.createComponent
                   (UIMessages.COMPONENT_TYPE) instanceof UIMessages);
        assertTrue(application.createComponent
                   ("jakarta.faces.NamingContainer") instanceof UINamingContainer);
        assertTrue(application.createComponent
                   (UINamingContainer.COMPONENT_TYPE) instanceof UINamingContainer);
        assertTrue(application.createComponent
                   ("jakarta.faces.Output") instanceof UIOutput);
        assertTrue(application.createComponent
                   (UIOutput.COMPONENT_TYPE) instanceof UIOutput);
        assertTrue(application.createComponent
                   ("jakarta.faces.Panel") instanceof UIPanel);
        assertTrue(application.createComponent
                   (UIPanel.COMPONENT_TYPE) instanceof UIPanel);
        assertTrue(application.createComponent
                   ("jakarta.faces.Parameter") instanceof UIParameter);
        assertTrue(application.createComponent
                   (UIParameter.COMPONENT_TYPE) instanceof UIParameter);
        assertTrue(application.createComponent
                   ("jakarta.faces.SelectBoolean") instanceof UISelectBoolean);
        assertTrue(application.createComponent
                   (UISelectBoolean.COMPONENT_TYPE) instanceof UISelectBoolean);
        assertTrue(application.createComponent
                   ("jakarta.faces.SelectItem") instanceof UISelectItem);
        assertTrue(application.createComponent
                   (UISelectItem.COMPONENT_TYPE) instanceof UISelectItem);
        assertTrue(application.createComponent
                   ("jakarta.faces.SelectItems") instanceof UISelectItems);
        assertTrue(application.createComponent
                   (UISelectItems.COMPONENT_TYPE) instanceof UISelectItems);
        assertTrue(application.createComponent
                   ("jakarta.faces.SelectMany") instanceof UISelectMany);
        assertTrue(application.createComponent
                   (UISelectMany.COMPONENT_TYPE) instanceof UISelectMany);
        assertTrue(application.createComponent
                   ("jakarta.faces.SelectOne") instanceof UISelectOne);
        assertTrue(application.createComponent
                   (UISelectOne.COMPONENT_TYPE) instanceof UISelectOne);
    }

    // Check that all of the required HTML components have been registered
    private void checkComponentsHtml() throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlCommandButton") instanceof HtmlCommandButton);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlCommandLink") instanceof HtmlCommandLink);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlDataTable") instanceof HtmlDataTable);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlForm") instanceof HtmlForm);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlGraphicImage") instanceof HtmlGraphicImage);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlInputHidden") instanceof HtmlInputHidden);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlInputSecret") instanceof HtmlInputSecret);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlInputText") instanceof HtmlInputText);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlInputTextarea") instanceof HtmlInputTextarea);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlMessage") instanceof HtmlMessage);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlMessages") instanceof HtmlMessages);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlOutputFormat") instanceof HtmlOutputFormat);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlOutputLabel") instanceof HtmlOutputLabel);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlOutputLink") instanceof HtmlOutputLink);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlOutputText") instanceof HtmlOutputText);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlPanelGrid") instanceof HtmlPanelGrid);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlPanelGroup") instanceof HtmlPanelGroup);
        assertTrue(
            application.createComponent
            ("jakarta.faces.HtmlSelectBooleanCheckbox") instanceof HtmlSelectBooleanCheckbox);
        assertTrue(
            application.createComponent
            ("jakarta.faces.HtmlSelectManyCheckbox") instanceof HtmlSelectManyCheckbox);
        assertTrue(
            application.createComponent
            ("jakarta.faces.HtmlSelectManyListbox") instanceof HtmlSelectManyListbox);
        assertTrue(
            application.createComponent
            ("jakarta.faces.HtmlSelectManyMenu") instanceof HtmlSelectManyMenu);
        assertTrue(
            application.createComponent
            ("jakarta.faces.HtmlSelectOneListbox") instanceof HtmlSelectOneListbox);
        assertTrue(application.createComponent
                   ("jakarta.faces.HtmlSelectOneMenu") instanceof HtmlSelectOneMenu);
        assertTrue(
            application.createComponent
            ("jakarta.faces.HtmlSelectOneRadio") instanceof HtmlSelectOneRadio);
    }

    // Check that all required by-class Converters have been registered
    private void checkConvertersByClass() throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        assertTrue(application.createConverter
                   (BigDecimal.class) instanceof BigDecimalConverter);
        assertTrue(application.createConverter
                   (BigInteger.class) instanceof BigIntegerConverter);
        assertTrue(application.createConverter
                   (Boolean.class) instanceof BooleanConverter);
        assertTrue(application.createConverter
                   (Byte.class) instanceof ByteConverter);
        assertTrue(application.createConverter
                   (Character.class) instanceof CharacterConverter);
        assertTrue(application.createConverter
                   (Double.class) instanceof DoubleConverter);
        assertTrue(application.createConverter
                   (Float.class) instanceof FloatConverter);
        assertTrue(application.createConverter
                   (Integer.class) instanceof IntegerConverter);
        assertTrue(application.createConverter
                   (Long.class) instanceof LongConverter);
        assertTrue(application.createConverter
                   (Short.class) instanceof ShortConverter);
    }

    // Check that all required by-id Converters have been registered
    private void checkConvertersById() throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        assertTrue(application.createConverter
                   ("jakarta.faces.BigDecimal") instanceof BigDecimalConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.BigInteger") instanceof BigIntegerConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.Boolean") instanceof BooleanConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.Byte") instanceof ByteConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.Character") instanceof CharacterConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.DateTime") instanceof DateTimeConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.Double") instanceof DoubleConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.Float") instanceof FloatConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.Integer") instanceof IntegerConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.Long") instanceof LongConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.Number") instanceof NumberConverter);
        assertTrue(application.createConverter
                   ("jakarta.faces.Short") instanceof ShortConverter);
    }

    // Check that all required Renderers have been registered
    private void checkRenderers() throws Exception {
        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        assertNotNull(
            rk.getRenderer("jakarta.faces.Command", "jakarta.faces.Button"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.Command", "jakarta.faces.Link"));
        assertNotNull(rk.getRenderer("jakarta.faces.Data", "jakarta.faces.Table"));
        assertNotNull(rk.getRenderer("jakarta.faces.Form", "jakarta.faces.Form"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.Graphic", "jakarta.faces.Image"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.Input", "jakarta.faces.Hidden"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.Input", "jakarta.faces.Secret"));
        assertNotNull(rk.getRenderer("jakarta.faces.Input", "jakarta.faces.Text"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.Input", "jakarta.faces.Textarea"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.Message", "jakarta.faces.Message"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.Messages", "jakarta.faces.Messages"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.Output", "jakarta.faces.Format"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.Output", "jakarta.faces.Label"));
        assertNotNull(rk.getRenderer("jakarta.faces.Output", "jakarta.faces.Link"));
        assertNotNull(rk.getRenderer("jakarta.faces.Output", "jakarta.faces.Text"));
        assertNotNull(rk.getRenderer("jakarta.faces.Panel", "jakarta.faces.Grid"));
        assertNotNull(rk.getRenderer("jakarta.faces.Panel", "jakarta.faces.Group"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.SelectBoolean", "jakarta.faces.Checkbox"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.SelectMany", "jakarta.faces.Checkbox"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.SelectMany", "jakarta.faces.Listbox"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.SelectMany", "jakarta.faces.Menu"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.SelectOne", "jakarta.faces.Listbox"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.SelectOne", "jakarta.faces.Menu"));
        assertNotNull(
            rk.getRenderer("jakarta.faces.SelectOne", "jakarta.faces.Radio"));
    }

    // Check that all required Validators have been registered
    private void checkValidators() throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        assertTrue(application.createValidator
                   ("jakarta.faces.DoubleRange") instanceof DoubleRangeValidator);
        assertTrue(application.createValidator
                   ("jakarta.faces.Length") instanceof LengthValidator);
        assertTrue(application.createValidator
                   ("jakarta.faces.LongRange") instanceof LongRangeValidator);
    }

    // Check whether embed configuration occurred or did not occur
    private void checkEmbedConfiguration(boolean should) throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        if (should) {
            assertTrue(application.createComponent
                       ("EmbedComponent") instanceof TestComponent);
            assertTrue(application.createConverter
                       ("EmbedConverter") instanceof TestConverter);
            assertTrue(application.createValidator
                       ("EmbedValidator") instanceof TestValidator);
            assertNotNull(rk.getRenderer("Test", "EmbedRenderer"));
        } else {
            try {
                application.createComponent("EmbedComponent");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            try {
                application.createConverter("EmbedConverter");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            try {
                application.createValidator("EmbedValidator");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            assertNull(rk.getRenderer("Test", "EmbedRenderer"));
        }

    }

    // Check whether extra configuration occurred or did not occur
    private void checkExtraConfiguration(boolean should) throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        if (should) {
            assertTrue(application.createComponent
                       ("ExtraComponent") instanceof TestComponent);
            assertTrue(application.createConverter
                       ("ExtraConverter") instanceof TestConverter);
            assertTrue(application.createValidator
                       ("ExtraValidator") instanceof TestValidator);
            assertNotNull(rk.getRenderer("Test", "ExtraRenderer"));
        } else {
            try {
                application.createComponent("ExtraComponent");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            try {
                application.createConverter("ExtraConverter");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            try {
                application.createValidator("ExtraValidator");
                fail("Should have thrown FacesException");
            } catch (FacesException e) {
                ; // Expected result
            }
            assertNull(rk.getRenderer("Test", "ExtraRenderer"));
        }

    }

    // Check that the default configuration took place
    private void checkDefaultConfiguration() throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        assertTrue(application.createComponent
                   ("DefaultComponent") instanceof TestComponent);
        assertTrue(application.createConverter
                   ("DefaultConverter") instanceof TestConverter);
        assertTrue(application.createValidator
                   ("DefaultValidator") instanceof TestValidator);
        assertNotNull(rk.getRenderer("Test", "DefaultRenderer"));

    }

    // Representative sample only
    private String rendersChildrenFalse[][] = {

    };

    private String rendersChildrenTrue[][] = {
        {"jakarta.faces.Command", "jakarta.faces.Link"},
        {"jakarta.faces.Data", "jakarta.faces.Table"},
        {"jakarta.faces.Output", "jakarta.faces.Link"},
        {"jakarta.faces.Panel", "jakarta.faces.Grid"},
        {"jakarta.faces.Panel", "jakarta.faces.Group"},
        {"jakarta.faces.Command", "jakarta.faces.Button"},
        {"jakarta.faces.Form", "jakarta.faces.Form"}
    };


    private String status="";

    public String getStatus() {
        return status;
    }
}

