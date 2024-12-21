package com.sun.faces.config.configpopulator;

import jakarta.faces.application.ApplicationConfigurationPopulator;
import jakarta.faces.component.UIViewRoot;
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
import jakarta.faces.event.PostConstructViewMapEvent;
import jakarta.faces.event.PreDestroyViewMapEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class MojarraRuntimePopulator extends ApplicationConfigurationPopulator {

    @Override
    public void populateApplicationConfiguration(Document doc) {
        var namespace = doc.getDocumentElement().getNamespaceURI();
        var rootElement = doc.getDocumentElement();

        populateApplicationElements(doc, namespace, rootElement);
        populateFactoryElements(doc, namespace, rootElement);
        populateLifecycleElements(doc, namespace, rootElement);
        populateConverters(doc, namespace, rootElement);
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

        for (String[] systemEventListener: systemEventListeners) {
            var listenerElement = doc.createElementNS(namespace, "system-event-listener");

            appendChildElements(doc, namespace, listenerElement, new String[][] {
                { "system-event-listener-class", systemEventListener[0] },
                { "system-event-class", systemEventListener[1] },
                { "source-class", systemEventListener[2] }
            });

            applicationElement.appendChild(listenerElement);
        }
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
                { EnumConverter.CONVERTER_ID, EnumConverter.class.getName() }
        };

        for (String[] converter : converters) {
            var converterElement = doc.createElementNS(namespace, "converter");

            appendChildElements(doc, namespace, converterElement, new String[][] {
                { "converter-id", converter[0] },
                { "converter-class", converter[1] }
            });

            rootElement.appendChild(converterElement);
        }
    }

    private void appendChildElements(Document doc, String namespace, Element parentElement, String[][] childElementNamesAndValues) {
        for (String[] childElementNameAndValue : childElementNamesAndValues) {
            var child = doc.createElementNS(namespace, childElementNameAndValue[0]);
            child.appendChild(doc.createTextNode(childElementNameAndValue[1]));
            parentElement.appendChild(child);
        }
    }
}
