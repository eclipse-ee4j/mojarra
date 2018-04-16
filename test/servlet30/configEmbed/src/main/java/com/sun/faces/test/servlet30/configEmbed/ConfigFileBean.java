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

package com.sun.faces.test.servlet30.configEmbed;

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

@ManagedBean
@SessionScoped
public class ConfigFileBean {

    private String title = "Test Config File";
    public String getTitle() {
        return title; 
    }

    public ConfigFileBean() {
    }

    public String getEmbed() throws Exception {
        // Validate standard configuration
        checkComponentsGeneric();
        checkComponentsHtml();
        checkConvertersByClass();
        checkConvertersById();
        checkRenderers();
        checkValidators();

        // Validate what was actually configured
        checkDefaultConfiguration();
        checkExtraConfiguration(true);
        checkEmbedConfiguration(true);

        return "SUCCESS";
    }

    // Check that all of the required generic components have been registered
    private void checkComponentsGeneric() throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        assertTrue(application.createComponent
                   ("javax.faces.Column") instanceof UIColumn);
        assertTrue(application.createComponent
                   (UIColumn.COMPONENT_TYPE) instanceof UIColumn);
        assertTrue(application.createComponent
                   ("javax.faces.Command") instanceof UICommand);
        assertTrue(application.createComponent
                   (UICommand.COMPONENT_TYPE) instanceof UICommand);
        assertTrue(application.createComponent
                   ("javax.faces.Data") instanceof UIData);
        assertTrue(application.createComponent
                   (UIData.COMPONENT_TYPE) instanceof UIData);
        assertTrue(application.createComponent
                   ("javax.faces.Form") instanceof UIForm);
        assertTrue(application.createComponent
                   (UIForm.COMPONENT_TYPE) instanceof UIForm);
        assertTrue(application.createComponent
                   ("javax.faces.Graphic") instanceof UIGraphic);
        assertTrue(application.createComponent
                   (UIGraphic.COMPONENT_TYPE) instanceof UIGraphic);
        assertTrue(application.createComponent
                   ("javax.faces.Input") instanceof UIInput);
        assertTrue(application.createComponent
                   (UIInput.COMPONENT_TYPE) instanceof UIInput);
        assertTrue(application.createComponent
                   ("javax.faces.Message") instanceof UIMessage);
        assertTrue(application.createComponent
                   (UIMessage.COMPONENT_TYPE) instanceof UIMessage);
        assertTrue(application.createComponent
                   ("javax.faces.Messages") instanceof UIMessages);
        assertTrue(application.createComponent
                   (UIMessages.COMPONENT_TYPE) instanceof UIMessages);
        assertTrue(application.createComponent
                   ("javax.faces.NamingContainer") instanceof UINamingContainer);
        assertTrue(application.createComponent
                   (UINamingContainer.COMPONENT_TYPE) instanceof UINamingContainer);
        assertTrue(application.createComponent
                   ("javax.faces.Output") instanceof UIOutput);
        assertTrue(application.createComponent
                   (UIOutput.COMPONENT_TYPE) instanceof UIOutput);
        assertTrue(application.createComponent
                   ("javax.faces.Panel") instanceof UIPanel);
        assertTrue(application.createComponent
                   (UIPanel.COMPONENT_TYPE) instanceof UIPanel);
        assertTrue(application.createComponent
                   ("javax.faces.Parameter") instanceof UIParameter);
        assertTrue(application.createComponent
                   (UIParameter.COMPONENT_TYPE) instanceof UIParameter);
        assertTrue(application.createComponent
                   ("javax.faces.SelectBoolean") instanceof UISelectBoolean);
        assertTrue(application.createComponent
                   (UISelectBoolean.COMPONENT_TYPE) instanceof UISelectBoolean);
        assertTrue(application.createComponent
                   ("javax.faces.SelectItem") instanceof UISelectItem);
        assertTrue(application.createComponent
                   (UISelectItem.COMPONENT_TYPE) instanceof UISelectItem);
        assertTrue(application.createComponent
                   ("javax.faces.SelectItems") instanceof UISelectItems);
        assertTrue(application.createComponent
                   (UISelectItems.COMPONENT_TYPE) instanceof UISelectItems);
        assertTrue(application.createComponent
                   ("javax.faces.SelectMany") instanceof UISelectMany);
        assertTrue(application.createComponent
                   (UISelectMany.COMPONENT_TYPE) instanceof UISelectMany);
        assertTrue(application.createComponent
                   ("javax.faces.SelectOne") instanceof UISelectOne);
        assertTrue(application.createComponent
                   (UISelectOne.COMPONENT_TYPE) instanceof UISelectOne);
    }

    // Check that all of the required HTML components have been registered
    private void checkComponentsHtml() throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        assertTrue(application.createComponent
                   ("javax.faces.HtmlCommandButton") instanceof HtmlCommandButton);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlCommandLink") instanceof HtmlCommandLink);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlDataTable") instanceof HtmlDataTable);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlForm") instanceof HtmlForm);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlGraphicImage") instanceof HtmlGraphicImage);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlInputHidden") instanceof HtmlInputHidden);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlInputSecret") instanceof HtmlInputSecret);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlInputText") instanceof HtmlInputText);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlInputTextarea") instanceof HtmlInputTextarea);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlMessage") instanceof HtmlMessage);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlMessages") instanceof HtmlMessages);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlOutputFormat") instanceof HtmlOutputFormat);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlOutputLabel") instanceof HtmlOutputLabel);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlOutputLink") instanceof HtmlOutputLink);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlOutputText") instanceof HtmlOutputText);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlPanelGrid") instanceof HtmlPanelGrid);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlPanelGroup") instanceof HtmlPanelGroup);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectBooleanCheckbox") instanceof HtmlSelectBooleanCheckbox);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectManyCheckbox") instanceof HtmlSelectManyCheckbox);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectManyListbox") instanceof HtmlSelectManyListbox);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectManyMenu") instanceof HtmlSelectManyMenu);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectOneListbox") instanceof HtmlSelectOneListbox);
        assertTrue(application.createComponent
                   ("javax.faces.HtmlSelectOneMenu") instanceof HtmlSelectOneMenu);
        assertTrue(
            application.createComponent
            ("javax.faces.HtmlSelectOneRadio") instanceof HtmlSelectOneRadio);
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
                   ("javax.faces.BigDecimal") instanceof BigDecimalConverter);
        assertTrue(application.createConverter
                   ("javax.faces.BigInteger") instanceof BigIntegerConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Boolean") instanceof BooleanConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Byte") instanceof ByteConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Character") instanceof CharacterConverter);
        assertTrue(application.createConverter
                   ("javax.faces.DateTime") instanceof DateTimeConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Double") instanceof DoubleConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Float") instanceof FloatConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Integer") instanceof IntegerConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Long") instanceof LongConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Number") instanceof NumberConverter);
        assertTrue(application.createConverter
                   ("javax.faces.Short") instanceof ShortConverter);
    }

    // Check that all required Renderers have been registered
    private void checkRenderers() throws Exception {
        RenderKitFactory rkFactory = (RenderKitFactory)
            FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit rk =
            rkFactory.getRenderKit(null,
                                   RenderKitFactory.HTML_BASIC_RENDER_KIT);

        assertNotNull(
            rk.getRenderer("javax.faces.Command", "javax.faces.Button"));
        assertNotNull(
            rk.getRenderer("javax.faces.Command", "javax.faces.Link"));
        assertNotNull(rk.getRenderer("javax.faces.Data", "javax.faces.Table"));
        assertNotNull(rk.getRenderer("javax.faces.Form", "javax.faces.Form"));
        assertNotNull(
            rk.getRenderer("javax.faces.Graphic", "javax.faces.Image"));
        assertNotNull(
            rk.getRenderer("javax.faces.Input", "javax.faces.Hidden"));
        assertNotNull(
            rk.getRenderer("javax.faces.Input", "javax.faces.Secret"));
        assertNotNull(rk.getRenderer("javax.faces.Input", "javax.faces.Text"));
        assertNotNull(
            rk.getRenderer("javax.faces.Input", "javax.faces.Textarea"));
        assertNotNull(
            rk.getRenderer("javax.faces.Message", "javax.faces.Message"));
        assertNotNull(
            rk.getRenderer("javax.faces.Messages", "javax.faces.Messages"));
        assertNotNull(
            rk.getRenderer("javax.faces.Output", "javax.faces.Format"));
        assertNotNull(
            rk.getRenderer("javax.faces.Output", "javax.faces.Label"));
        assertNotNull(rk.getRenderer("javax.faces.Output", "javax.faces.Link"));
        assertNotNull(rk.getRenderer("javax.faces.Output", "javax.faces.Text"));
        assertNotNull(rk.getRenderer("javax.faces.Panel", "javax.faces.Grid"));
        assertNotNull(rk.getRenderer("javax.faces.Panel", "javax.faces.Group"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectBoolean", "javax.faces.Checkbox"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectMany", "javax.faces.Checkbox"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectMany", "javax.faces.Listbox"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectMany", "javax.faces.Menu"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectOne", "javax.faces.Listbox"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectOne", "javax.faces.Menu"));
        assertNotNull(
            rk.getRenderer("javax.faces.SelectOne", "javax.faces.Radio"));
    }

    // Check that all required Validators have been registered
    private void checkValidators() throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();
        Application application = fc.getApplication();

        assertTrue(application.createValidator
                   ("javax.faces.DoubleRange") instanceof DoubleRangeValidator);
        assertTrue(application.createValidator
                   ("javax.faces.Length") instanceof LengthValidator);
        assertTrue(application.createValidator
                   ("javax.faces.LongRange") instanceof LongRangeValidator);
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
        {"javax.faces.Command", "javax.faces.Link"},
        {"javax.faces.Data", "javax.faces.Table"},
        {"javax.faces.Output", "javax.faces.Link"},
        {"javax.faces.Panel", "javax.faces.Grid"},
        {"javax.faces.Panel", "javax.faces.Group"},
        {"javax.faces.Command", "javax.faces.Button"},
        {"javax.faces.Form", "javax.faces.Form"}
    };


    private String status="";

    public String getStatus() {
        return status;
    }
}

