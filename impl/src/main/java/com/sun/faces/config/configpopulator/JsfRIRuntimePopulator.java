/*
 * Copyright (c) 2018 Payara Services Limited.
 * All rights reserved.
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
package com.sun.faces.config.configpopulator;

import javax.faces.application.ApplicationConfigurationPopulator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class JsfRIRuntimePopulator extends ApplicationConfigurationPopulator {

    @Override
    public void populateApplicationConfiguration(Document toPopulate) {
        String ns = toPopulate.getDocumentElement().getNamespaceURI();
        Element faces_configElement = toPopulate.getDocumentElement();

        {
            Element factoryElement = toPopulate.createElementNS(ns, "factory");
            {
                Element application_factoryElement = toPopulate.createElementNS(ns, "application-factory");
                application_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.application.ApplicationFactoryImpl"));
                factoryElement.appendChild(application_factoryElement);
            }
            {
                Element exception_handler_factoryElement = toPopulate.createElementNS(ns, "exception-handler-factory");
                exception_handler_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.context.ExceptionHandlerFactoryImpl"));
                factoryElement.appendChild(exception_handler_factoryElement);
            }
            {
                Element visit_context_factoryElement = toPopulate.createElementNS(ns, "visit-context-factory");
                visit_context_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.component.visit.VisitContextFactoryImpl"));
                factoryElement.appendChild(visit_context_factoryElement);
            }
            {
                Element faces_context_factoryElement = toPopulate.createElementNS(ns, "faces-context-factory");
                faces_context_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.context.FacesContextFactoryImpl"));
                factoryElement.appendChild(faces_context_factoryElement);
            }
            {
                Element client_window_factoryElement = toPopulate.createElementNS(ns, "client-window-factory");
                client_window_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.lifecycle.ClientWindowFactoryImpl"));
                factoryElement.appendChild(client_window_factoryElement);
            }
            {
                Element flash_factoryElement = toPopulate.createElementNS(ns, "flash-factory");
                flash_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.context.flash.FlashFactoryImpl"));
                factoryElement.appendChild(flash_factoryElement);
            }
            {
                Element partial_view_context_factoryElement = toPopulate.createElementNS(ns, "partial-view-context-factory");
                partial_view_context_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.context.PartialViewContextFactoryImpl"));
                factoryElement.appendChild(partial_view_context_factoryElement);
            }
            {
                Element lifecycle_factoryElement = toPopulate.createElementNS(ns, "lifecycle-factory");
                lifecycle_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.lifecycle.LifecycleFactoryImpl"));
                factoryElement.appendChild(lifecycle_factoryElement);
            }
            {
                Element render_kit_factoryElement = toPopulate.createElementNS(ns, "render-kit-factory");
                render_kit_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.RenderKitFactoryImpl"));
                factoryElement.appendChild(render_kit_factoryElement);
            }
            {
                Element view_declaration_language_factoryElement = toPopulate.createElementNS(ns, "view-declaration-language-factory");
                view_declaration_language_factoryElement
                        .appendChild(toPopulate.createTextNode("com.sun.faces.application.view.ViewDeclarationLanguageFactoryImpl"));
                factoryElement.appendChild(view_declaration_language_factoryElement);
            }
            {
                Element tag_handler_delegate_factoryElement = toPopulate.createElementNS(ns, "tag-handler-delegate-factory");
                tag_handler_delegate_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.facelets.tag.jsf.TagHandlerDelegateFactoryImpl"));
                factoryElement.appendChild(tag_handler_delegate_factoryElement);
            }
            {
                Element external_context_factoryElement = toPopulate.createElementNS(ns, "external-context-factory");
                external_context_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.context.ExternalContextFactoryImpl"));
                factoryElement.appendChild(external_context_factoryElement);
            }
            {
                Element facelet_cache_factoryElement = toPopulate.createElementNS(ns, "facelet-cache-factory");
                facelet_cache_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.facelets.impl.FaceletCacheFactoryImpl"));
                factoryElement.appendChild(facelet_cache_factoryElement);
            }
            {
                Element flow_handler_factoryElement = toPopulate.createElementNS(ns, "flow-handler-factory");
                flow_handler_factoryElement.appendChild(toPopulate.createTextNode("com.sun.faces.flow.FlowHandlerFactoryImpl"));
                factoryElement.appendChild(flow_handler_factoryElement);
            }
            {
                Element search_expression_context_factoryElement = toPopulate.createElementNS(ns, "search-expression-context-factory");
                search_expression_context_factoryElement
                        .appendChild(toPopulate.createTextNode("com.sun.faces.component.search.SearchExpressionContextFactoryImpl"));
                factoryElement.appendChild(search_expression_context_factoryElement);
            }
            faces_configElement.appendChild(factoryElement);
        }

        // Application

        {
            Element applicationElement = toPopulate.createElementNS(ns, "application");
            {
                Element action_listenerElement = toPopulate.createElementNS(ns, "action-listener");
                action_listenerElement.appendChild(toPopulate.createTextNode("com.sun.faces.application.ActionListenerImpl"));
                applicationElement.appendChild(action_listenerElement);
            }
            {
                Element navigation_handlerElement = toPopulate.createElementNS(ns, "navigation-handler");
                navigation_handlerElement.appendChild(toPopulate.createTextNode("com.sun.faces.application.NavigationHandlerImpl"));
                applicationElement.appendChild(navigation_handlerElement);
            }
            {
                Element state_managerElement = toPopulate.createElementNS(ns, "state-manager");
                state_managerElement.appendChild(toPopulate.createTextNode("com.sun.faces.application.StateManagerImpl"));
                applicationElement.appendChild(state_managerElement);
            }
            {
                Element view_handlerElement = toPopulate.createElementNS(ns, "view-handler");
                view_handlerElement.appendChild(toPopulate.createTextNode("com.sun.faces.application.view.MultiViewHandler"));
                applicationElement.appendChild(view_handlerElement);
            }
            {
                Element resource_handlerElement = toPopulate.createElementNS(ns, "resource-handler");
                resource_handlerElement.appendChild(toPopulate.createTextNode("com.sun.faces.application.resource.ResourceHandlerImpl"));
                applicationElement.appendChild(resource_handlerElement);
            }
            {
                Element search_expression_handlerElement = toPopulate.createElementNS(ns, "search-expression-handler");
                search_expression_handlerElement.appendChild(toPopulate.createTextNode("com.sun.faces.component.search.SearchExpressionHandlerImpl"));
                applicationElement.appendChild(search_expression_handlerElement);
            }
            {
                Element system_event_listenerElement = toPopulate.createElementNS(ns, "system-event-listener");
                {
                    Element system_event_listener_classElement = toPopulate.createElementNS(ns, "system-event-listener-class");
                    system_event_listener_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.application.view.ViewScopeEventListener"));
                    system_event_listenerElement.appendChild(system_event_listener_classElement);
                }
                {
                    Element system_event_classElement = toPopulate.createElementNS(ns, "system-event-class");
                    system_event_classElement.appendChild(toPopulate.createTextNode("javax.faces.event.PostConstructViewMapEvent"));
                    system_event_listenerElement.appendChild(system_event_classElement);
                }
                {
                    Element source_classElement = toPopulate.createElementNS(ns, "source-class");
                    source_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIViewRoot"));
                    system_event_listenerElement.appendChild(source_classElement);
                }
                applicationElement.appendChild(system_event_listenerElement);
            }
            {
                Element system_event_listenerElement = toPopulate.createElementNS(ns, "system-event-listener");
                {
                    Element system_event_listener_classElement = toPopulate.createElementNS(ns, "system-event-listener-class");
                    system_event_listener_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.application.view.ViewScopeEventListener"));
                    system_event_listenerElement.appendChild(system_event_listener_classElement);
                }
                {
                    Element system_event_classElement = toPopulate.createElementNS(ns, "system-event-class");
                    system_event_classElement.appendChild(toPopulate.createTextNode("javax.faces.event.PreDestroyViewMapEvent"));
                    system_event_listenerElement.appendChild(system_event_classElement);
                }
                {
                    Element source_classElement = toPopulate.createElementNS(ns, "source-class");
                    source_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIViewRoot"));
                    system_event_listenerElement.appendChild(source_classElement);
                }
                applicationElement.appendChild(system_event_listenerElement);
            }
            faces_configElement.appendChild(applicationElement);
        }


        // Converter

        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.BigDecimal"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.BigDecimalConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.BigInteger"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.BigIntegerConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.Boolean"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.BooleanConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.Byte"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.ByteConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.Character"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.CharacterConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.DateTime"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.DateTimeConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.Double"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.DoubleConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.Float"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.FloatConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.Integer"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.IntegerConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.Long"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.LongConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.Number"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.NumberConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_idElement = toPopulate.createElementNS(ns, "converter-id");
                converter_idElement.appendChild(toPopulate.createTextNode("javax.faces.Short"));
                converterElement.appendChild(converter_idElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.ShortConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.math.BigDecimal"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.BigDecimalConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.math.BigInteger"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.BigIntegerConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.lang.Boolean"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.BooleanConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.lang.Byte"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.ByteConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.lang.Character"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.CharacterConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.lang.Double"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.DoubleConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.lang.Float"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.FloatConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.lang.Integer"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.IntegerConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.lang.Long"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.LongConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.lang.Short"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.ShortConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }
        {
            Element converterElement = toPopulate.createElementNS(ns, "converter");
            {
                Element converter_for_classElement = toPopulate.createElementNS(ns, "converter-for-class");
                converter_for_classElement.appendChild(toPopulate.createTextNode("java.lang.Enum"));
                converterElement.appendChild(converter_for_classElement);
            }
            {
                Element converter_classElement = toPopulate.createElementNS(ns, "converter-class");
                converter_classElement.appendChild(toPopulate.createTextNode("javax.faces.convert.EnumConverter"));
                converterElement.appendChild(converter_classElement);
            }
            faces_configElement.appendChild(converterElement);
        }


        // Lifecycle

        {
            Element lifecycleElement = toPopulate.createElementNS(ns, "lifecycle");
            {
                Element phase_listenerElement = toPopulate.createElementNS(ns, "phase-listener");
                phase_listenerElement.appendChild(toPopulate.createTextNode("com.sun.faces.lifecycle.ELResolverInitPhaseListener"));
                lifecycleElement.appendChild(phase_listenerElement);
            }
            faces_configElement.appendChild(lifecycleElement);
        }


        // Behavior

        {
            Element behaviorElement = toPopulate.createElementNS(ns, "behavior");
            {
                Element behavior_idElement = toPopulate.createElementNS(ns, "behavior-id");
                behavior_idElement.appendChild(toPopulate.createTextNode("javax.faces.behavior.Ajax"));
                behaviorElement.appendChild(behavior_idElement);
            }
            {
                Element behavior_classElement = toPopulate.createElementNS(ns, "behavior-class");
                behavior_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.behavior.AjaxBehavior"));
                behaviorElement.appendChild(behavior_classElement);
            }
            faces_configElement.appendChild(behaviorElement);
        }


        // Validator

        {
            Element validatorElement = toPopulate.createElementNS(ns, "validator");
            {
                Element validator_idElement = toPopulate.createElementNS(ns, "validator-id");
                validator_idElement.appendChild(toPopulate.createTextNode("javax.faces.Bean"));
                validatorElement.appendChild(validator_idElement);
            }
            {
                Element validator_classElement = toPopulate.createElementNS(ns, "validator-class");
                validator_classElement.appendChild(toPopulate.createTextNode("javax.faces.validator.BeanValidator"));
                validatorElement.appendChild(validator_classElement);
            }
            faces_configElement.appendChild(validatorElement);
        }
        {
            Element validatorElement = toPopulate.createElementNS(ns, "validator");
            {
                Element validator_idElement = toPopulate.createElementNS(ns, "validator-id");
                validator_idElement.appendChild(toPopulate.createTextNode("javax.faces.DoubleRange"));
                validatorElement.appendChild(validator_idElement);
            }
            {
                Element validator_classElement = toPopulate.createElementNS(ns, "validator-class");
                validator_classElement.appendChild(toPopulate.createTextNode("javax.faces.validator.DoubleRangeValidator"));
                validatorElement.appendChild(validator_classElement);
            }
            faces_configElement.appendChild(validatorElement);
        }
        {
            Element validatorElement = toPopulate.createElementNS(ns, "validator");
            {
                Element validator_idElement = toPopulate.createElementNS(ns, "validator-id");
                validator_idElement.appendChild(toPopulate.createTextNode("javax.faces.Length"));
                validatorElement.appendChild(validator_idElement);
            }
            {
                Element validator_classElement = toPopulate.createElementNS(ns, "validator-class");
                validator_classElement.appendChild(toPopulate.createTextNode("javax.faces.validator.LengthValidator"));
                validatorElement.appendChild(validator_classElement);
            }
            faces_configElement.appendChild(validatorElement);
        }
        {
            Element validatorElement = toPopulate.createElementNS(ns, "validator");
            {
                Element validator_idElement = toPopulate.createElementNS(ns, "validator-id");
                validator_idElement.appendChild(toPopulate.createTextNode("javax.faces.LongRange"));
                validatorElement.appendChild(validator_idElement);
            }
            {
                Element validator_classElement = toPopulate.createElementNS(ns, "validator-class");
                validator_classElement.appendChild(toPopulate.createTextNode("javax.faces.validator.LongRangeValidator"));
                validatorElement.appendChild(validator_classElement);
            }
            faces_configElement.appendChild(validatorElement);
        }
        {
            Element validatorElement = toPopulate.createElementNS(ns, "validator");
            {
                Element validator_idElement = toPopulate.createElementNS(ns, "validator-id");
                validator_idElement.appendChild(toPopulate.createTextNode("javax.faces.RegularExpression"));
                validatorElement.appendChild(validator_idElement);
            }
            {
                Element validator_classElement = toPopulate.createElementNS(ns, "validator-class");
                validator_classElement.appendChild(toPopulate.createTextNode("javax.faces.validator.RegexValidator"));
                validatorElement.appendChild(validator_classElement);
            }
            faces_configElement.appendChild(validatorElement);
        }
        {
            Element validatorElement = toPopulate.createElementNS(ns, "validator");
            {
                Element validator_idElement = toPopulate.createElementNS(ns, "validator-id");
                validator_idElement.appendChild(toPopulate.createTextNode("javax.faces.Required"));
                validatorElement.appendChild(validator_idElement);
            }
            {
                Element validator_classElement = toPopulate.createElementNS(ns, "validator-class");
                validator_classElement.appendChild(toPopulate.createTextNode("javax.faces.validator.RequiredValidator"));
                validatorElement.appendChild(validator_classElement);
            }
            faces_configElement.appendChild(validatorElement);
        }
        {
            Element validatorElement = toPopulate.createElementNS(ns, "validator");
            {
                Element validator_idElement = toPopulate.createElementNS(ns, "validator-id");
                validator_idElement.appendChild(toPopulate.createTextNode("com.sun.faces.ext.validator.CreditCardValidator"));
                validatorElement.appendChild(validator_idElement);
            }
            {
                Element validator_classElement = toPopulate.createElementNS(ns, "validator-class");
                validator_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.ext.validator.CreditCardValidator"));
                validatorElement.appendChild(validator_classElement);
            }
            faces_configElement.appendChild(validatorElement);
        }


        // Component

        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("com.sun.faces.ext.focus"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.ext.component.UIFocus"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("com.sun.faces.ext.validateWholeBean"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.ext.component.UIValidateWholeBean"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("facelets.ui.Repeat"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.facelets.component.UIRepeat"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("facelets.ui.ComponentRef"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.facelets.tag.ui.ComponentRef"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("facelets.ui.Debug"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.facelets.tag.ui.UIDebug"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Composite"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.facelets.tag.jsf.CompositeComponentImpl"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.ComponentResourceContainer"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.component.ComponentResourceContainer"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }


        // Render Kit

        {
            Element render_kitElement = toPopulate.createElementNS(ns, "render-kit");
            {
                Element render_kit_idElement = toPopulate.createElementNS(ns, "render-kit-id");
                render_kit_idElement.appendChild(toPopulate.createTextNode("HTML_BASIC"));
                render_kitElement.appendChild(render_kit_idElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("FocusFamily"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("com.sun.faces.ext.render.FocusHTMLRenderer"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.ext.render.FocusHTMLRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("facelets"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("facelets.ui.Repeat"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.facelets.component.RepeatRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element client_behavior_rendererElement = toPopulate.createElementNS(ns, "client-behavior-renderer");
                {
                    Element client_behavior_renderer_typeElement = toPopulate.createElementNS(ns, "client-behavior-renderer-type");
                    client_behavior_renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.behavior.Ajax"));
                    client_behavior_rendererElement.appendChild(client_behavior_renderer_typeElement);
                }
                {
                    Element client_behavior_renderer_classElement = toPopulate.createElementNS(ns, "client-behavior-renderer-class");
                    client_behavior_renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.AjaxBehaviorRenderer"));
                    client_behavior_rendererElement.appendChild(client_behavior_renderer_classElement);
                }
                render_kitElement.appendChild(client_behavior_rendererElement);
            }
            faces_configElement.appendChild(render_kitElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Column"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIColumn"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Command"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UICommand"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Data"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIData"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Form"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIForm"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Graphic"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIGraphic"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.ImportConstants"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIImportConstants"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Input"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIInput"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Message"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIMessage"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Messages"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIMessages"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.NamingContainer"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UINamingContainer"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIOutput"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.OutcomeTarget"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIOutcomeTarget"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Panel"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIPanel"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.ViewParameter"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIViewParameter"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.ViewAction"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIViewAction"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Parameter"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIParameter"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.SelectBoolean"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UISelectBoolean"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.SelectItem"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UISelectItem"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.SelectItems"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UISelectItems"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.SelectMany"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UISelectMany"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.SelectOne"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UISelectOne"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.ViewRoot"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIViewRoot"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Websocket"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.UIWebsocket"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlColumn"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlColumn"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlCommandButton"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlCommandButton"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlCommandLink"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlCommandLink"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlCommandScript"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlCommandScript"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlDataTable"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlDataTable"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlForm"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlForm"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlGraphicImage"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlGraphicImage"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlInputFile"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlInputFile"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlInputHidden"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlInputHidden"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlInputSecret"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlInputSecret"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlInputText"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlInputText"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlInputTextarea"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlInputTextarea"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlMessage"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlMessage"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlMessages"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlMessages"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlOutputFormat"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlOutputFormat"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlOutputLabel"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlOutputLabel"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlOutputLink"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlOutputLink"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlOutcomeTargetLink"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlOutcomeTargetLink"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlOutcomeTargetButton"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlOutcomeTargetButton"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlOutputText"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlOutputText"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlPanelGrid"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlPanelGrid"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlPanelGroup"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlPanelGroup"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlSelectBooleanCheckbox"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlSelectBooleanCheckbox"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlSelectManyCheckbox"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlSelectManyCheckbox"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlSelectManyListbox"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlSelectManyListbox"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlSelectManyMenu"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlSelectManyMenu"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlSelectOneListbox"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlSelectOneListbox"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlSelectOneMenu"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlSelectOneMenu"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.HtmlSelectOneRadio"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlSelectOneRadio"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.OutputDoctype"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlDoctype"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.OutputHead"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlHead"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element componentElement = toPopulate.createElementNS(ns, "component");
            {
                Element component_typeElement = toPopulate.createElementNS(ns, "component-type");
                component_typeElement.appendChild(toPopulate.createTextNode("javax.faces.OutputBody"));
                componentElement.appendChild(component_typeElement);
            }
            {
                Element component_classElement = toPopulate.createElementNS(ns, "component-class");
                component_classElement.appendChild(toPopulate.createTextNode("javax.faces.component.html.HtmlBody"));
                componentElement.appendChild(component_classElement);
            }
            faces_configElement.appendChild(componentElement);
        }
        {
            Element render_kitElement = toPopulate.createElementNS(ns, "render-kit");
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Command"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Button"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.ButtonRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Command"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Link"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.CommandLinkRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Command"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Script"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.CommandScriptRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Data"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Table"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.TableRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Form"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Form"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.FormRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Graphic"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Image"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.ImageRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Panel"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.passthrough.Element"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.PassthroughRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Input"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.File"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.FileRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Input"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Hidden"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.HiddenRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Input"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Secret"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.SecretRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Input"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Text"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.TextRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Input"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Textarea"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.TextareaRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Message"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Message"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.MessageRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Messages"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Messages"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.MessagesRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Format"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.OutputMessageRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Label"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.LabelRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Link"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.OutputLinkRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.OutcomeTarget"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Link"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.OutcomeTargetLinkRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.OutcomeTarget"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Button"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.OutcomeTargetButtonRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Text"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.TextRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Panel"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Grid"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.GridRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Panel"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Group"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.GroupRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.SelectBoolean"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Checkbox"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.CheckboxRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.SelectMany"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Checkbox"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.SelectManyCheckboxListRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.SelectMany"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Listbox"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.ListboxRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.SelectMany"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Menu"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.MenuRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.SelectOne"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Listbox"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.ListboxRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.SelectOne"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Menu"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.MenuRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.SelectOne"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Radio"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.RadioRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.NamingContainer"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Composite"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.CompositeRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.CompositeFacet"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.CompositeFacetRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.resource.Script"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.ScriptRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.resource.Stylesheet"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.StylesheetRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Doctype"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.DoctypeRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Head"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.HeadRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Output"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Body"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.BodyRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            {
                Element rendererElement = toPopulate.createElementNS(ns, "renderer");
                {
                    Element component_familyElement = toPopulate.createElementNS(ns, "component-family");
                    component_familyElement.appendChild(toPopulate.createTextNode("javax.faces.Script"));
                    rendererElement.appendChild(component_familyElement);
                }
                {
                    Element renderer_typeElement = toPopulate.createElementNS(ns, "renderer-type");
                    renderer_typeElement.appendChild(toPopulate.createTextNode("javax.faces.Websocket"));
                    rendererElement.appendChild(renderer_typeElement);
                }
                {
                    Element renderer_classElement = toPopulate.createElementNS(ns, "renderer-class");
                    renderer_classElement.appendChild(toPopulate.createTextNode("com.sun.faces.renderkit.html_basic.WebsocketRenderer"));
                    rendererElement.appendChild(renderer_classElement);
                }
                render_kitElement.appendChild(rendererElement);
            }
            faces_configElement.appendChild(render_kitElement);
        }
    }
}
