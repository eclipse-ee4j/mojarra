package com.sun.faces.config.configpopulator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;

import jakarta.faces.application.ApplicationConfigurationPopulator;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIGraphic;
import jakarta.faces.component.UIImportConstants;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIMessage;
import jakarta.faces.component.UIMessages;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIOutcomeTarget;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIParameter;
import jakarta.faces.component.UISelectBoolean;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItemGroup;
import jakarta.faces.component.UISelectItemGroups;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.component.UIViewAction;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.UIWebsocket;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlColumn;
import jakarta.faces.component.html.HtmlCommandButton;
import jakarta.faces.component.html.HtmlCommandLink;
import jakarta.faces.component.html.HtmlCommandScript;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.component.html.HtmlDoctype;
import jakarta.faces.component.html.HtmlForm;
import jakarta.faces.component.html.HtmlGraphicImage;
import jakarta.faces.component.html.HtmlHead;
import jakarta.faces.component.html.HtmlInputFile;
import jakarta.faces.component.html.HtmlInputHidden;
import jakarta.faces.component.html.HtmlInputSecret;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.component.html.HtmlInputTextarea;
import jakarta.faces.component.html.HtmlMessage;
import jakarta.faces.component.html.HtmlMessages;
import jakarta.faces.component.html.HtmlOutcomeTargetButton;
import jakarta.faces.component.html.HtmlOutcomeTargetLink;
import jakarta.faces.component.html.HtmlOutputFormat;
import jakarta.faces.component.html.HtmlOutputLabel;
import jakarta.faces.component.html.HtmlOutputLink;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.component.html.HtmlPanelGrid;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.component.html.HtmlSelectBooleanCheckbox;
import jakarta.faces.component.html.HtmlSelectManyCheckbox;
import jakarta.faces.component.html.HtmlSelectManyListbox;
import jakarta.faces.component.html.HtmlSelectManyMenu;
import jakarta.faces.component.html.HtmlSelectOneListbox;
import jakarta.faces.component.html.HtmlSelectOneMenu;
import jakarta.faces.component.html.HtmlSelectOneRadio;
import jakarta.faces.convert.BigDecimalConverter;
import jakarta.faces.convert.BigIntegerConverter;
import jakarta.faces.convert.BooleanConverter;
import jakarta.faces.convert.ByteConverter;
import jakarta.faces.convert.CharacterConverter;
import jakarta.faces.convert.DateTimeConverter;
import jakarta.faces.convert.DoubleConverter;
import jakarta.faces.convert.EnumConverter;
import jakarta.faces.convert.FloatConverter;
import jakarta.faces.convert.IntegerConverter;
import jakarta.faces.convert.LongConverter;
import jakarta.faces.convert.NumberConverter;
import jakarta.faces.convert.ShortConverter;
import jakarta.faces.convert.UUIDConverter;
import jakarta.faces.event.PostConstructViewMapEvent;
import jakarta.faces.event.PreDestroyViewMapEvent;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.validator.DoubleRangeValidator;
import jakarta.faces.validator.LengthValidator;
import jakarta.faces.validator.LongRangeValidator;
import jakarta.faces.validator.RegexValidator;
import jakarta.faces.validator.RequiredValidator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.faces.ext.component.UIValidateWholeBean;
import com.sun.faces.facelets.component.UIRepeat;
import com.sun.faces.facelets.tag.ui.ComponentRef;
import com.sun.faces.facelets.tag.ui.UIDebug;

public final class MojarraRuntimePopulator extends ApplicationConfigurationPopulator {

    @Override
    public void populateApplicationConfiguration(Document doc) {
        var namespace = doc.getDocumentElement().getNamespaceURI();
        var rootElement = doc.getDocumentElement();

        populateApplicationElements(doc, namespace, rootElement);
        populateFactoryElements(doc, namespace, rootElement);
        populateLifecycleElements(doc, namespace, rootElement);
        populateConverters(doc, namespace, rootElement);
        populateValidators(doc, namespace, rootElement);
        populateBehaviors(doc, namespace, rootElement);
        populateComponents(doc, namespace, rootElement);
        populateRenderKitElements(doc, namespace, rootElement);
    }

    private void populateApplicationElements(Document doc, String namespace, Element rootElement) {
        var applicationElement = doc.createElementNS(namespace, "application");

        String[][] applicationElements = {
                { "action-listener", com.sun.faces.application.ActionListenerImpl.class.getName() },
                { "navigation-handler", com.sun.faces.application.NavigationHandlerImpl.class.getName() },
                { "state-manager", com.sun.faces.application.StateManagerImpl.class.getName() },
                { "view-handler", com.sun.faces.application.view.MultiViewHandler.class.getName() },
                { "resource-handler", com.sun.faces.application.resource.ResourceHandlerImpl.class.getName() },
                { "search-expression-handler", com.sun.faces.component.search.SearchExpressionHandlerImpl.class.getName() }
        };

        appendChildElements(doc, namespace, applicationElement, applicationElements);
        populateSystemEventListeners(doc, namespace, applicationElement);
        rootElement.appendChild(applicationElement);
    }

    private void populateSystemEventListeners(Document doc, String namespace, Element applicationElement) {
        String[][] systemEventListeners = {
                { com.sun.faces.application.view.ViewScopeEventListener.class.getName(), PostConstructViewMapEvent.class.getName(), UIViewRoot.class.getName() },
                { com.sun.faces.application.view.ViewScopeEventListener.class.getName(), PreDestroyViewMapEvent.class.getName(), UIViewRoot.class.getName() }   
        };

        appendNestedChildElements(doc, namespace, applicationElement, "system-event-listener", new String[] { "system-event-listener-class", "system-event-class", "source-class" }, systemEventListeners);
    }

    private void populateFactoryElements(Document doc, String namespace, Element rootElement) {
        var factoryElement = doc.createElementNS(namespace, "factory");

        String[][] factoryElements = {
                { "faces-servlet-factory", com.sun.faces.webapp.FacesServletFactoryImpl.class.getName() },
                { "application-factory", com.sun.faces.application.ApplicationFactoryImpl.class.getName() },
                { "exception-handler-factory", com.sun.faces.context.ExceptionHandlerFactoryImpl.class.getName() },
                { "visit-context-factory", com.sun.faces.component.visit.VisitContextFactoryImpl.class.getName() },
                { "faces-context-factory", com.sun.faces.context.FacesContextFactoryImpl.class.getName() },
                { "client-window-factory", com.sun.faces.lifecycle.ClientWindowFactoryImpl.class.getName() },
                { "flash-factory", com.sun.faces.context.flash.FlashFactoryImpl.class.getName() },
                { "partial-view-context-factory", com.sun.faces.context.PartialViewContextFactoryImpl.class.getName() },
                { "lifecycle-factory", com.sun.faces.lifecycle.LifecycleFactoryImpl.class.getName() },
                { "render-kit-factory", com.sun.faces.renderkit.RenderKitFactoryImpl.class.getName() },
                { "view-declaration-language-factory", com.sun.faces.application.view.ViewDeclarationLanguageFactoryImpl.class.getName() },
                { "tag-handler-delegate-factory", com.sun.faces.facelets.tag.faces.TagHandlerDelegateFactoryImpl.class.getName() },
                { "external-context-factory", com.sun.faces.context.ExternalContextFactoryImpl.class.getName() },
                { "facelet-cache-factory", com.sun.faces.facelets.impl.FaceletCacheFactoryImpl.class.getName() },
                { "flow-handler-factory", com.sun.faces.flow.FlowHandlerFactoryImpl.class.getName() },
                { "search-expression-context-factory", com.sun.faces.component.search.SearchExpressionContextFactoryImpl.class.getName() }
        };

        appendChildElements(doc, namespace, factoryElement, factoryElements);
        rootElement.appendChild(factoryElement);
    }

    private void populateLifecycleElements(Document doc, String namespace, Element rootElement) {
        var lifecycleElement = doc.createElementNS(namespace, "lifecycle");

        String[][] lifecycleElements = {
                { "phase-listener", com.sun.faces.lifecycle.ELResolverInitPhaseListener.class.getName() }
        };

        appendChildElements(doc, namespace, lifecycleElement, lifecycleElements);
        rootElement.appendChild(lifecycleElement);
    }

    private void populateConverters(Document doc, String namespace, Element rootElement) {
        String[][] converters = {
                { BigDecimalConverter.CONVERTER_ID, BigDecimalConverter.class.getName() },
                { BigIntegerConverter.CONVERTER_ID, BigIntegerConverter.class.getName() },
                { BooleanConverter.CONVERTER_ID,BooleanConverter.class.getName() },
                { ByteConverter.CONVERTER_ID, ByteConverter.class.getName() },
                { CharacterConverter.CONVERTER_ID, CharacterConverter.class.getName() },
                { DateTimeConverter.CONVERTER_ID, DateTimeConverter.class.getName() },
                { DoubleConverter.CONVERTER_ID, DoubleConverter.class.getName() },
                { FloatConverter.CONVERTER_ID, FloatConverter.class.getName() },
                { IntegerConverter.CONVERTER_ID, IntegerConverter.class.getName() },
                { LongConverter.CONVERTER_ID, LongConverter.class.getName() },
                { NumberConverter.CONVERTER_ID, NumberConverter.class.getName() },
                { ShortConverter.CONVERTER_ID, ShortConverter.class.getName() },
                { EnumConverter.CONVERTER_ID, EnumConverter.class.getName() },
                { UUIDConverter.CONVERTER_ID, UUIDConverter.class.getName() }
        };

        appendNestedChildElements(doc, namespace, rootElement, "converter", new String[] { "converter-id", "converter-class" }, converters);
        populateConvertersForClass(doc, namespace, rootElement);
    }

    private void populateConvertersForClass(Document doc, String namespace, Element rootElement) {
        String[][] convertersForClass = {
                { BigDecimal.class.getName(), BigDecimalConverter.class.getName() },
                { BigInteger.class.getName(), BigIntegerConverter.class.getName() },
                { Boolean.class.getName(),BooleanConverter.class.getName() },
                { Byte.class.getName(), ByteConverter.class.getName() },
                { Character.class.getName(), CharacterConverter.class.getName() },
                { Double.class.getName(), DoubleConverter.class.getName() },
                { Float.class.getName(), FloatConverter.class.getName() },
                { Integer.class.getName(), IntegerConverter.class.getName() },
                { Long.class.getName(), LongConverter.class.getName() },
                { Short.class.getName(), ShortConverter.class.getName() },
                { Enum.class.getName(), EnumConverter.class.getName() },
                { UUID.class.getName(), UUIDConverter.class.getName() }
        };

        appendNestedChildElements(doc, namespace, rootElement, "converter", new String[] { "converter-for-class", "converter-class" }, convertersForClass);
    }

    private void populateValidators(Document doc, String namespace, Element rootElement) {
        String[][] validators = {
                { BeanValidator.VALIDATOR_ID, BeanValidator.class.getName() },
                { DoubleRangeValidator.VALIDATOR_ID, DoubleRangeValidator.class.getName() },
                { LengthValidator.VALIDATOR_ID, LengthValidator.class.getName() },
                { LongRangeValidator.VALIDATOR_ID, LongRangeValidator.class.getName() },
                { RegexValidator.VALIDATOR_ID, RegexValidator.class.getName() },
                { RequiredValidator.VALIDATOR_ID, RequiredValidator.class.getName() }
        };

        appendNestedChildElements(doc, namespace, rootElement, "validator", new String[] { "validator-id", "validator-class" }, validators);
    }
    
    private void populateBehaviors(Document doc, String namespace, Element rootElement) {
        String[][] behaviors = {
                { AjaxBehavior.BEHAVIOR_ID, jakarta.faces.component.behavior.AjaxBehavior.class.getName() }
        };

        appendNestedChildElements(doc, namespace, rootElement, "behavior", new String[] { "behavior-id", "behavior-class" }, behaviors);
    }

    private void populateComponents(Document doc, String namespace, Element rootElement) {
        String[][] components = {
                { UIValidateWholeBean.FAMILY, UIValidateWholeBean.class.getName() },
                { UIRepeat.COMPONENT_TYPE, UIRepeat.class.getName() },
                { ComponentRef.COMPONENT_TYPE, ComponentRef.class.getName() },
                { UIDebug.COMPONENT_TYPE, UIDebug.class.getName() },
                { "jakarta.faces.Composite", com.sun.faces.facelets.tag.faces.CompositeComponentImpl.class.getName() },
                { "jakarta.faces.ComponentResourceContainer", com.sun.faces.component.ComponentResourceContainer.class.getName() },
                { UIColumn.COMPONENT_TYPE, UIColumn.class.getName() },
                { UICommand.COMPONENT_TYPE, UICommand.class.getName() },
                { UIData.COMPONENT_TYPE, UIData.class.getName() },
                { UIForm.COMPONENT_TYPE, UIForm.class.getName() },
                { UIGraphic.COMPONENT_TYPE, UIGraphic.class.getName() },
                { UIImportConstants.COMPONENT_TYPE, UIImportConstants.class.getName() },
                { UIInput.COMPONENT_TYPE, UIInput.class.getName() },
                { UIMessage.COMPONENT_TYPE, UIMessage.class.getName() },
                { UIMessages.COMPONENT_TYPE, UIMessages.class.getName() },
                { UINamingContainer.COMPONENT_TYPE, UINamingContainer.class.getName() },
                { UIOutput.COMPONENT_TYPE, UIOutput.class.getName() },
                { UIOutcomeTarget.COMPONENT_TYPE, UIOutcomeTarget.class.getName() },
                { UIPanel.COMPONENT_TYPE, UIPanel.class.getName() },
                { UIViewParameter.COMPONENT_TYPE, UIViewParameter.class.getName() },
                { UIViewAction.COMPONENT_TYPE, UIViewAction.class.getName() },
                { UIParameter.COMPONENT_TYPE, UIParameter.class.getName() },
                { UISelectBoolean.COMPONENT_TYPE, UISelectBoolean.class.getName() },
                { UISelectItem.COMPONENT_TYPE, UISelectItem.class.getName() },
                { UISelectItems.COMPONENT_TYPE, UISelectItems.class.getName() },
                { UISelectItemGroup.COMPONENT_TYPE, UISelectItemGroup.class.getName() },
                { UISelectItemGroups.COMPONENT_TYPE, UISelectItemGroups.class.getName() },
                { UISelectMany.COMPONENT_TYPE, UISelectMany.class.getName() },
                { UISelectOne.COMPONENT_TYPE, UISelectOne.class.getName() },
                { UIViewRoot.COMPONENT_TYPE, UIViewRoot.class.getName() },
                { UIWebsocket.COMPONENT_TYPE, UIWebsocket.class.getName() },
                { HtmlColumn.COMPONENT_TYPE, HtmlColumn.class.getName() },
                { HtmlCommandButton.COMPONENT_TYPE, HtmlCommandButton.class.getName() },
                { HtmlCommandLink.COMPONENT_TYPE, HtmlCommandLink.class.getName() },
                { HtmlCommandScript.COMPONENT_TYPE, HtmlCommandScript.class.getName() },
                { HtmlDataTable.COMPONENT_TYPE, HtmlDataTable.class.getName() },
                { HtmlForm.COMPONENT_TYPE, HtmlForm.class.getName() },
                { HtmlGraphicImage.COMPONENT_TYPE, HtmlGraphicImage.class.getName() },
                { HtmlInputFile.COMPONENT_TYPE, HtmlInputFile.class.getName() },
                { HtmlInputHidden.COMPONENT_TYPE, HtmlInputHidden.class.getName() },
                { HtmlInputSecret.COMPONENT_TYPE, HtmlInputSecret.class.getName() },
                { HtmlInputText.COMPONENT_TYPE, HtmlInputText.class.getName() },
                { HtmlInputTextarea.COMPONENT_TYPE, HtmlInputTextarea.class.getName() },
                { HtmlMessage.COMPONENT_TYPE, HtmlMessage.class.getName() },
                { HtmlMessages.COMPONENT_TYPE, HtmlMessages.class.getName() },
                { HtmlOutputFormat.COMPONENT_TYPE, HtmlOutputFormat.class.getName() },
                { HtmlOutputLabel.COMPONENT_TYPE, HtmlOutputLabel.class.getName() },
                { HtmlOutputLink.COMPONENT_TYPE, HtmlOutputLink.class.getName() },
                { HtmlOutcomeTargetLink.COMPONENT_TYPE, HtmlOutcomeTargetLink.class.getName() },
                { HtmlOutcomeTargetButton.COMPONENT_TYPE, HtmlOutcomeTargetButton.class.getName() },
                { HtmlOutputText.COMPONENT_TYPE, HtmlOutputText.class.getName() },
                { HtmlPanelGrid.COMPONENT_TYPE, HtmlPanelGrid.class.getName() },
                { HtmlPanelGroup.COMPONENT_TYPE, HtmlPanelGroup.class.getName() },
                { HtmlSelectBooleanCheckbox.COMPONENT_TYPE, HtmlSelectBooleanCheckbox.class.getName() },
                { HtmlSelectManyCheckbox.COMPONENT_TYPE, HtmlSelectManyCheckbox.class.getName() },
                { HtmlSelectManyListbox.COMPONENT_TYPE, HtmlSelectManyListbox.class.getName() },
                { HtmlSelectManyMenu.COMPONENT_TYPE, HtmlSelectManyMenu.class.getName() },
                { HtmlSelectOneListbox.COMPONENT_TYPE, HtmlSelectOneListbox.class.getName() },
                { HtmlSelectOneMenu.COMPONENT_TYPE, HtmlSelectOneMenu.class.getName() },
                { HtmlSelectOneRadio.COMPONENT_TYPE, HtmlSelectOneRadio.class.getName() },
                { HtmlDoctype.COMPONENT_TYPE, HtmlDoctype.class.getName() },
                { HtmlHead.COMPONENT_TYPE, HtmlHead.class.getName() },
                { HtmlBody.COMPONENT_TYPE, HtmlBody.class.getName() }
        };

        appendNestedChildElements(doc, namespace, rootElement, "component", new String[] { "component-type", "component-class" }, components);
    }

    private void populateRenderKitElements(Document doc, String namespace, Element rootElement) {
        var renderKitElement = doc.createElementNS(namespace, "render-kit");

        String[][] renderKitElements = {
                { "render-kit-id", RenderKitFactory.HTML_BASIC_RENDER_KIT }
        };

        appendChildElements(doc, namespace, renderKitElement, renderKitElements);
        populateClientBehaviorRenderers(doc, namespace, renderKitElement);
        populateRenderers(doc, namespace, renderKitElement);
        rootElement.appendChild(renderKitElement);
    }

    private void populateClientBehaviorRenderers(Document doc, String namespace, Element rootElement) {
        String[][] clientBehaviorRenderers = {
                { AjaxBehavior.BEHAVIOR_ID, com.sun.faces.renderkit.html_basic.AjaxBehaviorRenderer.class.getName() }
        };

        appendNestedChildElements(doc, namespace, rootElement, "client-behavior-renderer", new String[] { "client-behavior-renderer-type", "client-behavior-renderer-class" }, clientBehaviorRenderers);
    }

    private void populateRenderers(Document doc, String namespace, Element rootElement) {
        String[][] renderers = {
                { UIRepeat.COMPONENT_FAMILY, "facelets.ui.Repeat", com.sun.faces.facelets.component.RepeatRenderer.class.getName() },
                { UICommand.COMPONENT_FAMILY, "jakarta.faces.Button", com.sun.faces.renderkit.html_basic.ButtonRenderer.class.getName() },                    
                { UICommand.COMPONENT_FAMILY, "jakarta.faces.Link", com.sun.faces.renderkit.html_basic.CommandLinkRenderer.class.getName() },                    
                { UICommand.COMPONENT_FAMILY, "jakarta.faces.Script", com.sun.faces.renderkit.html_basic.CommandScriptRenderer.class.getName() },                    
                { UIData.COMPONENT_FAMILY, "jakarta.faces.Table", com.sun.faces.renderkit.html_basic.TableRenderer.class.getName() },                    
                { UIForm.COMPONENT_FAMILY, "jakarta.faces.Form", com.sun.faces.renderkit.html_basic.FormRenderer.class.getName() },                    
                { UIGraphic.COMPONENT_FAMILY, "jakarta.faces.Image", com.sun.faces.renderkit.html_basic.ImageRenderer.class.getName() },                    
                { UIPanel.COMPONENT_FAMILY, "jakarta.faces.passthrough.Element", com.sun.faces.renderkit.html_basic.PassthroughRenderer.class.getName() },                    
                { UIInput.COMPONENT_FAMILY, "jakarta.faces.File", com.sun.faces.renderkit.html_basic.FileRenderer.class.getName() },                    
                { UIInput.COMPONENT_FAMILY, "jakarta.faces.Hidden", com.sun.faces.renderkit.html_basic.HiddenRenderer.class.getName() },                    
                { UIInput.COMPONENT_FAMILY, "jakarta.faces.Secret", com.sun.faces.renderkit.html_basic.SecretRenderer.class.getName() },                    
                { UIInput.COMPONENT_FAMILY, "jakarta.faces.Text", com.sun.faces.renderkit.html_basic.TextRenderer.class.getName() },                    
                { UIInput.COMPONENT_FAMILY, "jakarta.faces.Textarea", com.sun.faces.renderkit.html_basic.TextareaRenderer.class.getName() },                    
                { UIMessage.COMPONENT_FAMILY, "jakarta.faces.Message", com.sun.faces.renderkit.html_basic.MessageRenderer.class.getName() },                    
                { UIMessages.COMPONENT_FAMILY, "jakarta.faces.Messages", com.sun.faces.renderkit.html_basic.MessagesRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.Format", com.sun.faces.renderkit.html_basic.OutputMessageRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.Label", com.sun.faces.renderkit.html_basic.LabelRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.Link", com.sun.faces.renderkit.html_basic.OutputLinkRenderer.class.getName() },                    
                { UIOutcomeTarget.COMPONENT_FAMILY, "jakarta.faces.Link", com.sun.faces.renderkit.html_basic.OutcomeTargetLinkRenderer.class.getName() },                    
                { UIOutcomeTarget.COMPONENT_FAMILY, "jakarta.faces.Button", com.sun.faces.renderkit.html_basic.OutcomeTargetButtonRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.Text", com.sun.faces.renderkit.html_basic.TextRenderer.class.getName() },                    
                { UIPanel.COMPONENT_FAMILY, "jakarta.faces.Grid", com.sun.faces.renderkit.html_basic.GridRenderer.class.getName() },                    
                { UIPanel.COMPONENT_FAMILY, "jakarta.faces.Group", com.sun.faces.renderkit.html_basic.GroupRenderer.class.getName() },                    
                { UISelectBoolean.COMPONENT_FAMILY, "jakarta.faces.Checkbox", com.sun.faces.renderkit.html_basic.CheckboxRenderer.class.getName() },                    
                { UISelectMany.COMPONENT_FAMILY, "jakarta.faces.Checkbox", com.sun.faces.renderkit.html_basic.SelectManyCheckboxListRenderer.class.getName() },                    
                { UISelectMany.COMPONENT_FAMILY, "jakarta.faces.Listbox", com.sun.faces.renderkit.html_basic.ListboxRenderer.class.getName() },                    
                { UISelectMany.COMPONENT_FAMILY, "jakarta.faces.Menu", com.sun.faces.renderkit.html_basic.MenuRenderer.class.getName() },                    
                { UISelectOne.COMPONENT_FAMILY, "jakarta.faces.Listbox", com.sun.faces.renderkit.html_basic.ListboxRenderer.class.getName() },                    
                { UISelectOne.COMPONENT_FAMILY, "jakarta.faces.Menu", com.sun.faces.renderkit.html_basic.MenuRenderer.class.getName() },                    
                { UISelectOne.COMPONENT_FAMILY, "jakarta.faces.Radio", com.sun.faces.renderkit.html_basic.RadioRenderer.class.getName() },                    
                { UINamingContainer.COMPONENT_FAMILY, "jakarta.faces.Composite", com.sun.faces.renderkit.html_basic.CompositeRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.CompositeFacet", com.sun.faces.renderkit.html_basic.CompositeFacetRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.resource.Script", com.sun.faces.renderkit.html_basic.ScriptRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.resource.Stylesheet", com.sun.faces.renderkit.html_basic.StylesheetRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.Doctype", com.sun.faces.renderkit.html_basic.DoctypeRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.Head", com.sun.faces.renderkit.html_basic.HeadRenderer.class.getName() },                    
                { UIOutput.COMPONENT_FAMILY, "jakarta.faces.Body", com.sun.faces.renderkit.html_basic.BodyRenderer.class.getName() },                    
                { UIWebsocket.COMPONENT_FAMILY, "jakarta.faces.Websocket", com.sun.faces.renderkit.html_basic.WebsocketRenderer.class.getName() }
        };

        appendNestedChildElements(doc, namespace, rootElement, "renderer", new String[] { "component-family", "renderer-type", "renderer-class" }, renderers);
    }

    private void appendChildElements(Document doc, String namespace, Element parentElement, String[][] childElementNamesAndValues) {
        for (String[] childElementNameAndValue : childElementNamesAndValues) {
            var child = doc.createElementNS(namespace, childElementNameAndValue[0]);
            child.appendChild(doc.createTextNode(childElementNameAndValue[1]));
            parentElement.appendChild(child);
        }
    }

    private void appendNestedChildElements(Document doc, String namespace, Element parentElement, String nestedElementName, String[] childElementNames, String[][] nestedChildElementValues) {
        for (String[] childElementValues: nestedChildElementValues) {
            var nestedElement = doc.createElementNS(namespace, nestedElementName);
            var childElementNamesAndValues = new ArrayList<String[]>();
            
            for (int i = 0; i < childElementNames.length; i++) {
                childElementNamesAndValues.add(new String[] { childElementNames[i], childElementValues[i] });
            }

            appendChildElements(doc, namespace, nestedElement, childElementNamesAndValues.toArray(String[][]::new));
            parentElement.appendChild(nestedElement);
        }
    }
}
