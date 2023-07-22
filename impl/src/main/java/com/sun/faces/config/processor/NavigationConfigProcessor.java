/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.config.processor;

import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/navigation-rule</code>.
 * </p>
 */
public class NavigationConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <p>
     * /faces-config/navigation-rule
     * </p>
     */
    private static final String NAVIGATION_RULE = "navigation-rule";

    /**
     * <p>
     * /faces-config/navigation-rule/from-view-id
     * </p>
     */
    private static final String FROM_VIEW_ID = "from-view-id";

    /**
     * <p>
     * /faces-config/navigation-rule/navigation-case
     * </p>
     */
    private static final String NAVIGATION_CASE = "navigation-case";

    /**
     * <p>
     * /faces-config/navigation-rule/navigation-case/from-action
     * </p>
     */
    private static final String FROM_ACTION = "from-action";

    /**
     * <p>
     * /faces-config/navigation-rule/navigation-case/from-outcome
     * </p>
     */
    private static final String FROM_OUTCOME = "from-outcome";

    /**
     * <p>
     * /faces-config/navigation-rule/navigation-case/if
     * </p>
     */
    private static final String IF = "if";

    /**
     * <p>
     * /faces-config/navigation-rule/navigation-case/to-view-id
     * </p>
     */
    private static final String TO_VIEW_ID = "to-view-id";

    /**
     * <p>
     * /faces-config/navigation-rule/navigation-case/to-flow-document-id
     * </p>
     */
    private static final String TO_FLOW_DOCUMENT_ID = "to-flow-document-id";

    /**
     * <p>
     * /faces-config/navigation-rule/navigation-case/redirect
     * </p>
     */
    private static final String REDIRECT = "redirect";

    /**
     * <p>
     * /faces-confg/navigation-rule/navigation-case/redirect/view-param
     * </p>
     */
    private static final String VIEW_PARAM = "view-param";

    /**
     * <p>
     * /faces-confg/navigation-rule/navigation-case/redirect/view-param/name
     * </p>
     */
    private static final String VIEW_PARAM_NAME = "name";

    /**
     * <p>
     * /faces-confg/navigation-rule/navigation-case/redirect/view-param/value
     * </p>
     */
    private static final String VIEW_PARAM_VALUE = "value";

    /**
     * <p>
     * /faces-confg/navigation-rule/navigation-case/redirect/redirect-param
     * </p>
     */
    private static final String REDIRECT_PARAM = "redirect-param";

    /**
     * <p>
     * /faces-confg/navigation-rule/navigation-case/redirect/redirect-param/name
     * </p>
     */
    private static final String REDIRECT_PARAM_NAME = "name";

    /**
     * <p>
     * /faces-confg/navigation-rule/navigation-case/redirect/redirect-param/value
     * </p>
     */
    private static final String REDIRECT_PARAM_VALUE = "value";

    /**
     * <p>
     * /faces-config/navigation-rule/navigation-case/redirect[@include-page-params]
     * </p>
     */
    private static final String INCLUDE_VIEW_PARAMS_ATTRIBUTE = "include-view-params";

    /**
     * <p>
     * If <code>from-view-id</code> is not defined.
     * <p>
     */
    private static final String FROM_VIEW_ID_DEFAULT = "*";

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext sc, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {
        NavigationHandler handler = getApplication().getNavigationHandler();
        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing navigation-rule elements for document: ''{0}''", documentInfo.getSourceURI()));
            }

            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList navigationRules = document.getDocumentElement().getElementsByTagNameNS(namespace, NAVIGATION_RULE);
            if (navigationRules != null && navigationRules.getLength() > 0) {
                addNavigationRules(navigationRules, handler, sc);
            }

        }

    }

    // --------------------------------------------------------- Private Methods

    private void addNavigationRules(NodeList navigationRules, NavigationHandler navHandler, ServletContext sc) throws XPathExpressionException {
        for (int i = 0, size = navigationRules.getLength(); i < size; i++) {
            Node navigationRule = navigationRules.item(i);
            if (!"flow-definition".equals(navigationRule.getParentNode().getLocalName()) && navigationRule.getNodeType() == Node.ELEMENT_NODE) {
                NodeList children = navigationRule.getChildNodes();
                String fromViewId = FROM_VIEW_ID_DEFAULT;
                List<Node> navigationCases = null;
                for (int c = 0, csize = children.getLength(); c < csize; c++) {
                    Node n = children.item(c);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        switch (n.getLocalName()) {
                        case FROM_VIEW_ID:
                            String t = getNodeText(n);
                            fromViewId = t == null ? FROM_VIEW_ID_DEFAULT : t;
                            if (!fromViewId.equals(FROM_VIEW_ID_DEFAULT) && fromViewId.charAt(0) != '/') {
                                if (LOGGER.isLoggable(Level.WARNING)) {
                                    LOGGER.log(Level.WARNING, "faces.config.navigation.from_view_id_leading_slash", new String[] { fromViewId });
                                }
                                fromViewId = '/' + fromViewId;
                            }
                            break;
                        case NAVIGATION_CASE:
                            if (navigationCases == null) {
                                navigationCases = new ArrayList<>(csize);
                            }
                            navigationCases.add(n);
                            break;
                        }
                    }
                }

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, MessageFormat.format("Processing navigation rule with 'from-view-id' of ''{0}''", fromViewId));
                }
                addNavigationCasesForRule(fromViewId, navigationCases, navHandler, sc);
            }
        }
    }

    private void addNavigationCasesForRule(String fromViewId, List<Node> navigationCases, NavigationHandler navHandler, ServletContext sc) {
        if (navigationCases != null && !navigationCases.isEmpty()) {
            ApplicationAssociate associate = ApplicationAssociate.getInstance(sc);

            for (Node navigationCase : navigationCases) {
                if (navigationCase.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                NodeList children = navigationCase.getChildNodes();
                String outcome = null;
                String action = null;
                String condition = null;
                String toViewId = null;
                String toFlowDocumentId = null;
                Map<String, List<String>> parameters = null;
                boolean redirect = false;
                boolean includeViewParams = false;
                for (int i = 0, size = children.getLength(); i < size; i++) {
                    Node n = children.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        switch (n.getLocalName()) {
                        case FROM_OUTCOME:
                            outcome = getNodeText(n);
                            break;
                        case FROM_ACTION:
                            action = getNodeText(n);
                            break;
                        case IF:
                            String expression = getNodeText(n);
                            if (SharedUtils.isExpression(expression) && !SharedUtils.isMixedExpression(expression)) {
                                condition = expression;
                            } else {
                                if (LOGGER.isLoggable(Level.WARNING)) {
                                    LOGGER.log(Level.WARNING, "faces.config.navigation.if_invalid_expression", new String[] { expression, fromViewId });
                                }
                            }
                            break;
                        case TO_VIEW_ID:
                            String toViewIdString = getNodeText(n);
                            if (toViewIdString.charAt(0) != '/' && toViewIdString.charAt(0) != '#') {
                                if (LOGGER.isLoggable(Level.WARNING)) {
                                    LOGGER.log(Level.WARNING, "faces.config.navigation.to_view_id_leading_slash", new String[] { toViewIdString, fromViewId });
                                }
                                toViewId = '/' + toViewIdString;
                            } else {
                                toViewId = toViewIdString;
                            }
                            break;
                        case TO_FLOW_DOCUMENT_ID:
                            toFlowDocumentId = getNodeText(n);
                            break;
                        case REDIRECT:
                            parameters = processParameters(n.getChildNodes());
                            includeViewParams = isIncludeViewParams(n);
                            redirect = true;
                            break;
                        }
                    }
                }

                NavigationCase cnc = new NavigationCase(fromViewId, action, outcome, condition, toViewId, toFlowDocumentId, parameters, redirect,
                        includeViewParams);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, MessageFormat.format("Adding NavigationCase: {0}", cnc.toString()));
                }

                // if the top-level NavigationHandler is an instance of
                // ConfigurableNavigationHandler, add the NavigationCases to
                // that instance as well as adding them to the application associate.
                // We have to add them to the ApplicationAssociate in the case
                // where the top-level NavigationHandler may be custom and delegates
                // to the default NavigationHandler implementation. In 1.2, they
                // could be guaranteed that the default implementation had all
                // defined navigation mappings.
                if (navHandler instanceof ConfigurableNavigationHandler) {
                    ConfigurableNavigationHandler cnav = (ConfigurableNavigationHandler) navHandler;
                    Set<NavigationCase> cases = cnav.getNavigationCases().computeIfAbsent(fromViewId, k -> new LinkedHashSet<>());
                    cases.add(cnc);
                }
                associate.addNavigationCase(cnc);

            }

        }

    }

    private Map<String, List<String>> processParameters(NodeList children) {

        Map<String, List<String>> parameters = null;

        if (children.getLength() > 0) {
            parameters = new LinkedHashMap<>(4, 1.0f);
            for (int i = 0, size = children.getLength(); i < size; i++) {
                Node n = children.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    if (VIEW_PARAM.equals(n.getLocalName())) {
                        String name = null;
                        String value = null;
                        NodeList params = n.getChildNodes();
                        for (int j = 0, jsize = params.getLength(); j < jsize; j++) {
                            Node pn = params.item(j);
                            if (pn.getNodeType() == Node.ELEMENT_NODE) {
                                if (VIEW_PARAM_NAME.equals(pn.getLocalName())) {
                                    name = getNodeText(pn);
                                }
                                if (VIEW_PARAM_VALUE.equals(pn.getLocalName())) {
                                    value = getNodeText(pn);
                                }
                            }
                        }
                        if (name != null) {
                            List<String> values = parameters.get(name);
                            if (values == null && value != null) {
                                values = new ArrayList<>(2);
                                parameters.put(name, values);
                            }
                            if (values != null) {
                                values.add(value);
                            }
                        }
                    }
                    if (REDIRECT_PARAM.equals(n.getLocalName())) {
                        String name = null;
                        String value = null;
                        NodeList params = n.getChildNodes();
                        for (int j = 0, jsize = params.getLength(); j < jsize; j++) {
                            Node pn = params.item(j);
                            if (pn.getNodeType() == Node.ELEMENT_NODE) {
                                if (REDIRECT_PARAM_NAME.equals(pn.getLocalName())) {
                                    name = getNodeText(pn);
                                }
                                if (REDIRECT_PARAM_VALUE.equals(pn.getLocalName())) {
                                    value = getNodeText(pn);
                                }
                            }
                        }
                        if (name != null) {
                            List<String> values = parameters.get(name);
                            if (values == null && value != null) {
                                values = new ArrayList<>(2);
                                parameters.put(name, values);
                            }
                            if (values != null) {
                                values.add(value);
                            }
                        }
                    }
                }
            }
        }

        return parameters;
    }

    private boolean isIncludeViewParams(Node n) {

        return Boolean.parseBoolean(getNodeText(n.getAttributes().getNamedItem(INCLUDE_VIEW_PARAMS_ATTRIBUTE)));

    }

}
