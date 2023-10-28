/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import static com.sun.faces.util.Util.createLocalDocumentBuilderFactory;
import static com.sun.faces.util.Util.notNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.facelets.util.ReflectionUtil;
import com.sun.faces.flow.FlowImpl;
import com.sun.faces.flow.ParameterImpl;
import com.sun.faces.flow.builder.FlowBuilderImpl;
import com.sun.faces.util.FacesLogger;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PostConstructApplicationEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.flow.FlowHandler;
import jakarta.faces.flow.FlowHandlerFactory;
import jakarta.faces.flow.FlowNode;
import jakarta.faces.flow.Parameter;
import jakarta.faces.flow.builder.FlowBuilder;
import jakarta.faces.flow.builder.FlowCallBuilder;
import jakarta.faces.flow.builder.MethodCallBuilder;
import jakarta.faces.flow.builder.NavigationCaseBuilder;
import jakarta.faces.flow.builder.SwitchBuilder;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/flow-definition</code>.
 * </p>
 */
public class FacesFlowDefinitionConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <code>/faces-config/flow-definition</code>
     */
    private static final String FACES_FLOW_DEFINITION = "flow-definition";

    private static final String flowDefinitionListKey = RIConstants.FACES_PREFIX + "FacesFlowDefinitions";

    public FacesFlowDefinitionConfigProcessor() {
    }

    public static boolean uriIsFlowDefinition(URI uri) {
        boolean result = false;
        String path = uri.getPath();
        String[] segments = path.split("/");
        if (1 < segments.length) {
            String flowName = segments[segments.length - 2];
            String definingName = segments[segments.length - 1];
            result = definingName.equals(flowName + "-flow.xml");
        }

        return result;
    }

    /*
     * Implement the requirements of 11.4.3.3
     *
     * @param uri
     *
     * @param toPopulate
     *
     * @return
     */
    public static Document synthesizeEmptyFlowDefinition(URI uri) throws ParserConfigurationException {
        Document newDoc = null;

        String path = uri.getPath();
        String[] segments = path.split("/");
        if (segments.length < 2) {
            return newDoc;
        }
        String flowName = segments[segments.length - 2];

        DocumentBuilderFactory dbf = createLocalDocumentBuilderFactory();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        DOMImplementation domImpl = builder.getDOMImplementation();
        newDoc = domImpl.createDocument(RIConstants.DOCUMENT_NAMESPACE, "faces-config", null);
        Node documentElement = newDoc.getDocumentElement();
        Attr versionAttribute = newDoc.createAttribute("version");
        versionAttribute.setValue(RIConstants.DOCUMENT_VERSION);
        documentElement.getAttributes().setNamedItem(versionAttribute);

        Node facesConfig = newDoc.getFirstChild();

        Element flowDefinition = newDoc.createElementNS(RIConstants.DOCUMENT_NAMESPACE, "flow-definition");
        flowDefinition.setAttribute("id", flowName);
        facesConfig.appendChild(flowDefinition);
        final String flowReturnStr = flowName + "-return";

        Element flowReturn = newDoc.createElementNS(RIConstants.DOCUMENT_NAMESPACE, "flow-return");
        flowReturn.setAttribute("id", flowReturnStr);
        flowDefinition.appendChild(flowReturn);

        Element fromOutcome = newDoc.createElementNS(RIConstants.DOCUMENT_NAMESPACE, "from-outcome");
        flowReturn.appendChild(fromOutcome);
        fromOutcome.setTextContent("/" + flowReturnStr);

        return newDoc;
    }

    @Override
    public void process(ServletContext sc, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {

        WebConfiguration config = WebConfiguration.getInstance(sc);

        for (DocumentInfo documentInfo : documentInfos) {
            URI definingDocumentURI = documentInfo.getSourceURI();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, MessageFormat.format("Processing factory elements for document: ''{0}''", definingDocumentURI));
            }
            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList flowDefinitions = document.getDocumentElement().getElementsByTagNameNS(namespace, FACES_FLOW_DEFINITION);
            if (flowDefinitions != null && flowDefinitions.getLength() > 0) {
                config.setHasFlows(true);

                saveFlowDefinition(facesContext, definingDocumentURI, document);
            }
        }

        if (config.isHasFlows()) {
            String optionValue = config.getOptionValue(WebConfiguration.WebContextInitParameter.ClientWindowMode);
            boolean clientWindowNeedsEnabling = false;
            if ("none".equals(optionValue)) {
                clientWindowNeedsEnabling = true;
                String featureName = WebConfiguration.WebContextInitParameter.ClientWindowMode.getQualifiedName();
                LOGGER.log(Level.WARNING, "{0} was set to none, but Faces Flows requires {0} is enabled.  Setting to ''url''.", new Object[] { featureName });
            } else if (null == optionValue) {
                clientWindowNeedsEnabling = true;
            }
            if (clientWindowNeedsEnabling) {
                config.setOptionValue(WebConfiguration.WebContextInitParameter.ClientWindowMode, "url");
            }

            facesContext.getApplication().subscribeToEvent(PostConstructApplicationEvent.class, Application.class, new PerformDeferredFlowProcessing());
        }

    }

    private void saveFlowDefinition(FacesContext context, URI definingDocumentURI, Document flowDefinitions) {
        Map<String, Object> appMap = context.getExternalContext().getApplicationMap();
        List<FlowDefinitionDocument> def = (List<FlowDefinitionDocument>) appMap.get(flowDefinitionListKey);
        if (null == def) {
            def = new ArrayList<>();
            appMap.put(flowDefinitionListKey, def);
        }
        def.add(new FlowDefinitionDocument(definingDocumentURI, flowDefinitions));
    }

    private List<FlowDefinitionDocument> getSavedFlowDefinitions(FacesContext context) {
        Map<String, Object> appMap = context.getExternalContext().getApplicationMap();
        List<FlowDefinitionDocument> def = (List<FlowDefinitionDocument>) appMap.get(flowDefinitionListKey);
        return null != def ? def : Collections.emptyList();
    }

    private void clearSavedFlowDefinitions(FacesContext context) {
        Map<String, Object> appMap = context.getExternalContext().getApplicationMap();
        List<FlowDefinitionDocument> def = (List<FlowDefinitionDocument>) appMap.get(flowDefinitionListKey);
        if (null != def) {
            for (FlowDefinitionDocument cur : def) {
                cur.clear();
            }
            def.clear();
            appMap.remove(flowDefinitionListKey);
        }
    }

    private static class FlowDefinitionDocument {
        URI definingDocumentURI;
        Document flowDefinitions;

        public FlowDefinitionDocument(URI definingDocumentURI, Document flowDefinitions) {
            this.definingDocumentURI = definingDocumentURI;
            this.flowDefinitions = flowDefinitions;
        }

        public void clear() {
            definingDocumentURI = null;
            flowDefinitions = null;
        }

    }

    private class PerformDeferredFlowProcessing implements SystemEventListener {

        @Override
        public boolean isListenerForSource(Object source) {
            return source instanceof Application;
        }

        @Override
        public void processEvent(SystemEvent event) throws AbortProcessingException {
            FacesContext facesContext = event.getFacesContext();

            for (FlowDefinitionDocument flowDefinition : getSavedFlowDefinitions(facesContext)) {
                try {
                    processFacesFlowDefinitions(facesContext, flowDefinition.definingDocumentURI,
                            flowDefinition.flowDefinitions);
                } catch (XPathExpressionException ex) {
                    throw new FacesException(ex);
                }
            }

            clearSavedFlowDefinitions(facesContext);
        }
    }

    private void processFacesFlowDefinitions(FacesContext context, URI definingDocumentURI, Document document) throws XPathExpressionException {
        String namespace = document.getDocumentElement().getNamespaceURI();
        NodeList flowDefinitions = document.getDocumentElement().getElementsByTagNameNS(namespace, FACES_FLOW_DEFINITION);

        if (flowDefinitions.getLength() == 0) {
            return;
        }

        Application application = context.getApplication();
        FlowHandler flowHandler = application.getFlowHandler();

        if (flowHandler == null) {
            FlowHandlerFactory flowHandlerFactory = (FlowHandlerFactory) FactoryFinder.getFactory(FactoryFinder.FLOW_HANDLER_FACTORY);
            application.setFlowHandler(flowHandler = flowHandlerFactory.createFlowHandler(context));
        }

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new FacesConfigNamespaceContext(namespace));

        String nameStr = "";
        NodeList nameList = (NodeList) xpath.evaluate("./ns1:name/text()", document.getDocumentElement(), XPathConstants.NODESET);
        if (null != nameList && 1 < nameList.getLength()) {
            throw new XPathExpressionException("<faces-config> must have at most one <name> element.");
        }

        if (null != nameList && 1 == nameList.getLength()) {
            nameStr = nameList.item(0).getNodeValue().trim();
            if (0 < nameStr.length()) {
                ApplicationAssociate associate = ApplicationAssociate.getInstance(context.getExternalContext());
                try {
                    associate.relateUrlToDefiningDocumentInJar(definingDocumentURI.toURL(), nameStr);
                } catch (MalformedURLException ex) {
                    throw new XPathExpressionException(ex);
                }
            }
        }

        for (int c = 0, size = flowDefinitions.getLength(); c < size; c++) {
            Node flowDefinition = flowDefinitions.item(c);
            String flowId = getIdAttribute(flowDefinition);

            String uriStr = definingDocumentURI.toASCIIString();
            if (uriStr.endsWith(RIConstants.FLOW_DEFINITION_ID_SUFFIX)) {
                nameStr = "";
            }

            FlowBuilderImpl flowBuilder = new FlowBuilderImpl(context);
            flowBuilder.id(nameStr, flowId);

            processViews(xpath, flowDefinition, flowBuilder);
            processNavigationRules(xpath, flowDefinition, flowBuilder);
            processReturns(xpath, flowDefinition, flowBuilder);
            processInboundParameters(xpath, flowDefinition, flowBuilder);
            processFlowCalls(xpath, flowDefinition, flowBuilder);
            processSwitches(xpath, flowDefinition, flowBuilder);
            processMethodCalls(context, xpath, flowDefinition, flowBuilder);
            processInitializerFinalizer(xpath, flowDefinition, flowBuilder);

            String startNodeId = processStartNode(xpath, flowDefinition, flowBuilder);

            if (null != startNodeId) {
                FlowImpl toAdd = flowBuilder._getFlow();
                FlowNode startNode = toAdd.getNode(startNodeId);
                if (null == startNode) {
                    throw new XPathExpressionException("Unable to find flow node with id " + startNodeId + " to mark as start node");
                } else {
                    toAdd.setStartNodeId(startNodeId);
                }
            } else {
                flowBuilder.viewNode(flowId, "/" + flowId + "/" + flowId + ".xhtml").markAsStartNode();
            }
            flowHandler.addFlow(context, flowBuilder.getFlow());

        }

    }

    private void processNavigationRules(XPath xpath, Node flowDefinition, FlowBuilder flowBuilder) throws XPathExpressionException {
        // <editor-fold defaultstate="collapsed">
        NodeList navRules = (NodeList) xpath.evaluate(".//ns1:navigation-rule", flowDefinition, XPathConstants.NODESET);
        for (int i_navRule = 0; i_navRule < navRules.getLength(); i_navRule++) {
            Node navRule = navRules.item(i_navRule);
            NodeList fromViewIdList = (NodeList) xpath.evaluate(".//ns1:from-view-id/text()", navRule, XPathConstants.NODESET);
            if (1 != fromViewIdList.getLength()) {
                throw new XPathExpressionException("Within <navigation-rule> must have exactly one <from-view-id>");
            }
            String fromViewId = fromViewIdList.item(0).getNodeValue().trim();

            NodeList navCases = (NodeList) xpath.evaluate(".//ns1:navigation-case", navRule, XPathConstants.NODESET);
            for (int i_navCase = 0; i_navCase < navCases.getLength(); i_navCase++) {
                Node navCase = navCases.item(i_navCase);
                NodeList toViewIdList = (NodeList) xpath.evaluate(".//ns1:to-view-id/text()", navCase, XPathConstants.NODESET);
                if (1 != toViewIdList.getLength()) {
                    throw new XPathExpressionException("Within <navigation-case>, must have exactly one <to-view-id>");
                }
                String toViewId = toViewIdList.item(0).getNodeValue().trim();

                NavigationCaseBuilder ncb = flowBuilder.navigationCase();
                ncb.fromViewId(fromViewId).toViewId(toViewId);

                {
                    NodeList fromOutcomeList = (NodeList) xpath.evaluate(".//ns1:from-outcome/text()", navCase, XPathConstants.NODESET);
                    if (null != fromOutcomeList && 1 < fromOutcomeList.getLength()) {
                        throw new XPathExpressionException("Within <navigation-case>, must have at most one <from-outcome>");
                    }
                    if (null != fromOutcomeList && 1 == fromOutcomeList.getLength()) {
                        String fromOutcome = fromOutcomeList.item(0).getNodeValue().trim();
                        ncb.fromOutcome(fromOutcome);
                    }
                }

                {
                    NodeList fromActionList = (NodeList) xpath.evaluate(".//ns1:from-action/text()", navCase, XPathConstants.NODESET);
                    if (null != fromActionList && 1 < fromActionList.getLength()) {
                        throw new XPathExpressionException("Within <navigation-case>, must have at most one <from-action>");
                    }
                    if (null != fromActionList && 1 == fromActionList.getLength()) {
                        String fromAction = fromActionList.item(0).getNodeValue().trim();
                        ncb.fromAction(fromAction);
                    }
                }

                {
                    NodeList ifList = (NodeList) xpath.evaluate(".//ns1:if/text()", navCase, XPathConstants.NODESET);
                    if (null != ifList && 1 < ifList.getLength()) {
                        throw new XPathExpressionException("Within <navigation-case>, must have zero or one <if>");
                    }
                    if (null != ifList && 1 == ifList.getLength()) {
                        String ifStr = ifList.item(0).getNodeValue().trim();
                        ncb.condition(ifStr);
                    }

                }

                {
                    NodeList redirectList = (NodeList) xpath.evaluate(".//ns1:redirect", navCase, XPathConstants.NODESET);
                    if (null != redirectList && 1 < redirectList.getLength()) {
                        throw new XPathExpressionException("Within <navigation-case>, must have zero or one <redirect>");
                    }
                    if (null != redirectList && 1 == redirectList.getLength()) {
                        NavigationCaseBuilder.RedirectBuilder redirector = ncb.redirect();
                        Node redirectNode = redirectList.item(0);
                        String includeViewParams = getAttribute(redirectNode, "include-view-params");
                        if (null != includeViewParams && "true".equalsIgnoreCase(includeViewParams)) {
                            redirector.includeViewParams();
                        }
                        NodeList viewParamList = (NodeList) xpath.evaluate(".//ns1:redirect-param", redirectNode, XPathConstants.NODESET);
                        if (null != viewParamList) {
                            for (int i_viewParam = 0; i_viewParam < viewParamList.getLength(); i_viewParam++) {
                                Node viewParam = viewParamList.item(i_viewParam);
                                NodeList nameList = (NodeList) xpath.evaluate(".//ns1:name/text()", viewParam, XPathConstants.NODESET);
                                if (null == nameList || 1 != nameList.getLength()) {
                                    throw new XPathExpressionException("Within <redirect-param> must have <name>.");
                                }
                                String nameStr = nameList.item(0).getNodeValue().trim();

                                NodeList valueList = (NodeList) xpath.evaluate(".//ns1:value/text()", viewParam, XPathConstants.NODESET);
                                if (null == valueList || 1 != valueList.getLength()) {
                                    throw new XPathExpressionException("Within <redirect-param> must have <value>.");
                                }
                                String valueStr = valueList.item(0).getNodeValue().trim();
                                redirector.parameter(nameStr, valueStr);
                            }
                        }
                    }
                }

            }
        }
        // </editor-fold>
    }

    private void processViews(XPath xpath, Node flowDefinition, FlowBuilder flowBuilder) throws XPathExpressionException {
        NodeList views = (NodeList) xpath.evaluate(".//ns1:view", flowDefinition, XPathConstants.NODESET);
        for (int i_view = 0; i_view < views.getLength(); i_view++) {
            Node viewNode = views.item(i_view);
            String viewNodeId = getIdAttribute(viewNode);
            NodeList vdlDocumentList = (NodeList) xpath.evaluate(".//ns1:vdl-document/text()", viewNode, XPathConstants.NODESET);
            if (1 != vdlDocumentList.getLength()) {
                throw new XPathExpressionException("Within <view> exactly one child is allowed, and it must be a <vdl-document>");
            }
            String vdlDocumentStr = vdlDocumentList.item(0).getNodeValue().trim();
            flowBuilder.viewNode(viewNodeId, vdlDocumentStr);
        }
    }

    private void processReturns(XPath xpath, Node flowDefinition, FlowBuilder flowBuilder) throws XPathExpressionException {

        NodeList returns = (NodeList) xpath.evaluate(".//ns1:flow-return", flowDefinition, XPathConstants.NODESET);
        for (int i_return = 0; i_return < returns.getLength(); i_return++) {
            Node returnNode = returns.item(i_return);
            NodeList fromOutcomeList = (NodeList) xpath.evaluate(".//ns1:from-outcome/text()", returnNode, XPathConstants.NODESET);
            String id = getIdAttribute(returnNode);
            if (null != fromOutcomeList && 1 < fromOutcomeList.getLength()) {
                throw new XPathExpressionException("Within <flow-return id=\"" + id + "\"> only one child is allowed, and it must be a <from-outcome>");
            }
            if (null != fromOutcomeList && 1 == fromOutcomeList.getLength()) {
                String fromOutcomeStr = fromOutcomeList.item(0).getNodeValue().trim();
                flowBuilder.returnNode(id).fromOutcome(fromOutcomeStr);
            }

        }
    }

    private void processInboundParameters(XPath xpath, Node flowDefinition, FlowBuilder flowBuilder) throws XPathExpressionException {
        NodeList inboundParameters = (NodeList) xpath.evaluate(".//ns1:inbound-parameter", flowDefinition, XPathConstants.NODESET);
        for (int i_inbound = 0; i_inbound < inboundParameters.getLength(); i_inbound++) {
            Node inboundParamNode = inboundParameters.item(i_inbound);
            NodeList nameList = (NodeList) xpath.evaluate(".//ns1:name/text()", inboundParamNode, XPathConstants.NODESET);
            if (1 < nameList.getLength()) {
                throw new XPathExpressionException("Within <inbound-parameter> only one <name> child is allowed");
            }
            String nameStr = nameList.item(0).getNodeValue().trim();

            NodeList valueList = (NodeList) xpath.evaluate(".//ns1:value/text()", inboundParamNode, XPathConstants.NODESET);
            if (1 < valueList.getLength()) {
                throw new XPathExpressionException("Within <inbound-parameter> only one <value> child is allowed");
            }
            String valueStr = valueList.item(0).getNodeValue().trim();
            flowBuilder.inboundParameter(nameStr, valueStr);
        }
    }

    private void processFlowCalls(XPath xpath, Node flowDefinition, FlowBuilder flowBuilder) throws XPathExpressionException {
        NodeList flowCalls = (NodeList) xpath.evaluate(".//ns1:flow-call", flowDefinition, XPathConstants.NODESET);
        for (int i_flowCall = 0; i_flowCall < flowCalls.getLength(); i_flowCall++) {
            Node flowCallNode = flowCalls.item(i_flowCall);
            String flowCallId = getIdAttribute(flowCallNode);
            NodeList facesFlowRefList = (NodeList) xpath.evaluate(".//ns1:flow-reference", flowCallNode, XPathConstants.NODESET);
            if (null == facesFlowRefList || 1 != facesFlowRefList.getLength()) {
                throw new XPathExpressionException("Within <flow-call> must have exactly one <flow-reference>");
            }
            Node facesFlowRefNode = facesFlowRefList.item(0);

            NodeList facesFlowIdList = (NodeList) xpath.evaluate(".//ns1:flow-id/text()", facesFlowRefNode, XPathConstants.NODESET);
            if (null == facesFlowIdList || 1 != facesFlowIdList.getLength()) {
                throw new XPathExpressionException("Within <flow-reference> must have exactly one <flow-id>");
            }

            String destinationId = facesFlowIdList.item(0).getNodeValue().trim();

            NodeList definingDocumentIdList = (NodeList) xpath.evaluate(".//ns1:flow-document-id/text()", facesFlowRefNode, XPathConstants.NODESET);
            if (null == definingDocumentIdList && 1 != definingDocumentIdList.getLength()) {
                throw new XPathExpressionException("Within <flow-reference> must have at most one <flow-document-id>");
            }
            String definingDocumentId = "";
            if (null != definingDocumentIdList && 1 == definingDocumentIdList.getLength()) {
                definingDocumentId = definingDocumentIdList.item(0).getNodeValue().trim();
            }

            FlowCallBuilder flowCallBuilder = flowBuilder.flowCallNode(flowCallId);

            flowCallBuilder.flowReference(definingDocumentId, destinationId);

            NodeList outboundParameters = (NodeList) xpath.evaluate(".//ns1:outbound-parameter", flowDefinition, XPathConstants.NODESET);
            if (null != outboundParameters) {
                for (int i_outbound = 0; i_outbound < outboundParameters.getLength(); i_outbound++) {
                    Node outboundParamNode = outboundParameters.item(i_outbound);
                    NodeList nameList = (NodeList) xpath.evaluate(".//ns1:name/text()", outboundParamNode, XPathConstants.NODESET);
                    if (1 < nameList.getLength()) {
                        throw new XPathExpressionException("Within <outbound-parameter> only one <name> child is allowed");
                    }
                    String nameStr = nameList.item(0).getNodeValue().trim();

                    NodeList valueList = (NodeList) xpath.evaluate(".//ns1:value/text()", outboundParamNode, XPathConstants.NODESET);
                    if (1 < valueList.getLength()) {
                        throw new XPathExpressionException("Within <inbound-parameter> only one <value> child is allowed");
                    }
                    String valueStr = valueList.item(0).getNodeValue().trim();
                    flowCallBuilder.outboundParameter(nameStr, valueStr);
                }
            }

        }
    }

    private void processSwitches(XPath xpath, Node flowDefinition, FlowBuilder flowBuilder) throws XPathExpressionException {
        NodeList switches = (NodeList) xpath.evaluate(".//ns1:switch", flowDefinition, XPathConstants.NODESET);
        if (null == switches) {
            return;
        }
        for (int i_switch = 0; i_switch < switches.getLength(); i_switch++) {
            Node switchNode = switches.item(i_switch);
            String switchId = getIdAttribute(switchNode);
            SwitchBuilder switchBuilder = flowBuilder.switchNode(switchId);
            NodeList cases = (NodeList) xpath.evaluate(".//ns1:case", switchNode, XPathConstants.NODESET);
            if (null != cases) {
                for (int i_case = 0; i_case < cases.getLength(); i_case++) {
                    Node caseNode = cases.item(i_case);
                    NodeList ifList = (NodeList) xpath.evaluate(".//ns1:if/text()", caseNode, XPathConstants.NODESET);
                    if (1 < ifList.getLength()) {
                        throw new XPathExpressionException("Within <case> only one <if> child is allowed");
                    }
                    String ifStr = ifList.item(0).getNodeValue().trim();

                    NodeList fromOutcomeList = (NodeList) xpath.evaluate(".//ns1:from-outcome/text()", caseNode, XPathConstants.NODESET);
                    if (1 < fromOutcomeList.getLength()) {
                        throw new XPathExpressionException("Within <case> only one <from-outcome> child is allowed");
                    }
                    String fromOutcomeStr = fromOutcomeList.item(0).getNodeValue().trim();

                    switchBuilder.switchCase().condition(ifStr).fromOutcome(fromOutcomeStr);
                }
            }

            NodeList defaultOutcomeList = (NodeList) xpath.evaluate(".//ns1:default-outcome/text()", switchNode, XPathConstants.NODESET);
            if (null != defaultOutcomeList && 1 < defaultOutcomeList.getLength()) {
                throw new XPathExpressionException("Within <switch> only one <default-outcome> child is allowed");
            }
            if (null != defaultOutcomeList) {
                Node defaultOutcomeNode = defaultOutcomeList.item(0);
                if (null != defaultOutcomeNode) {
                    String defaultOutcomeStr = defaultOutcomeNode.getNodeValue().trim();
                    switchBuilder.defaultOutcome(defaultOutcomeStr);
                }
            }
        }

    }

    private void processMethodCalls(FacesContext context, XPath xpath, Node flowDefinition, FlowBuilder flowBuilder) throws XPathExpressionException {
        NodeList methodCalls = (NodeList) xpath.evaluate(".//ns1:method-call", flowDefinition, XPathConstants.NODESET);
        if (null == methodCalls) {
            return;
        }
        for (int i_methodCall = 0; i_methodCall < methodCalls.getLength(); i_methodCall++) {
            Node methodCallNode = methodCalls.item(i_methodCall);
            String methodCallId = getIdAttribute(methodCallNode);
            MethodCallBuilder methodCallBuilder = flowBuilder.methodCallNode(methodCallId);
            NodeList methodList = (NodeList) xpath.evaluate(".//ns1:method/text()", methodCallNode, XPathConstants.NODESET);
            if (1 != methodList.getLength()) {
                throw new XPathExpressionException("Within <method-call> exactly one <method> child is allowed");
            }
            String methodStr = methodList.item(0).getNodeValue().trim();

            NodeList params = (NodeList) xpath.evaluate(".//ns1:parameter", methodCallNode, XPathConstants.NODESET);
            if (null != params) {
                List<Class<?>> paramTypes = Collections.emptyList();
                if (0 < params.getLength()) {
                    paramTypes = new ArrayList<>();
                    List<Parameter> paramList = new ArrayList<>();
                    Parameter toAdd = null;
                    ExpressionFactory ef = context.getApplication().getExpressionFactory();
                    ELContext elContext = context.getELContext();
                    ValueExpression ve = null;

                    for (int i_param = 0; i_param < params.getLength(); i_param++) {
                        Node param = params.item(i_param);
                        NodeList valueList = (NodeList) xpath.evaluate(".//ns1:value/text()", param, XPathConstants.NODESET);
                        if (null == valueList || 1 != valueList.getLength()) {
                            throw new XPathExpressionException("Within <parameter> exactly one <value> child is allowed");
                        }
                        String valueStr = valueList.item(0).getNodeValue().trim();
                        String classStr = null;

                        NodeList classList = (NodeList) xpath.evaluate(".//ns1:class/text()", param, XPathConstants.NODESET);
                        if (null != classList && 1 < classList.getLength()) {
                            throw new XPathExpressionException("Within <parameter> at most one <class> child is allowed");
                        }
                        if (null != classList && 1 == classList.getLength()) {
                            classStr = classList.item(0).getNodeValue().trim();
                        }
                        Class<?> clazz = String.class;
                        if (null != classStr) {
                            try {
                                clazz = ReflectionUtil.forName(classStr);
                            } catch (ClassNotFoundException e) {
                                clazz = Object.class;
                            }
                        }

                        ve = ef.createValueExpression(elContext, valueStr, clazz);
                        toAdd = new ParameterImpl(classStr, ve);
                        paramList.add(toAdd);
                        paramTypes.add(clazz);
                    }
                    methodCallBuilder.parameters(paramList);
                }
                Class<?>[] paramArray = new Class[paramTypes.size()];
                paramTypes.toArray(paramArray);
                methodCallBuilder.expression(methodStr, paramArray);
            }

            NodeList defaultOutcomeList = (NodeList) xpath.evaluate(".//ns1:default-outcome/text()", methodCallNode, XPathConstants.NODESET);
            if (null != defaultOutcomeList && 1 < defaultOutcomeList.getLength()) {
                throw new XPathExpressionException("Within <method-call> only one <default-outcome> child is allowed");
            }
            if (null != defaultOutcomeList) {
                String defaultOutcomeStr = defaultOutcomeList.item(0).getNodeValue().trim();
                methodCallBuilder.defaultOutcome(defaultOutcomeStr);
            }

        }

    }

    private void processInitializerFinalizer(XPath xpath, Node flowDefinition, FlowBuilder flowBuilder) throws XPathExpressionException {
        NodeList initializerNodeList = (NodeList) xpath.evaluate(".//ns1:initializer/text()", flowDefinition, XPathConstants.NODESET);
        if (1 < initializerNodeList.getLength()) {
            throw new XPathExpressionException("At most one <initializer> is allowed.");
        }

        if (1 == initializerNodeList.getLength()) {
            String initializerStr = initializerNodeList.item(0).getNodeValue().trim();
            flowBuilder.initializer(initializerStr);
        }

        NodeList finalizerNodeList = (NodeList) xpath.evaluate(".//ns1:finalizer/text()", flowDefinition, XPathConstants.NODESET);
        if (1 < finalizerNodeList.getLength()) {
            throw new XPathExpressionException("At most one <finalizer> is allowed.");
        }

        if (1 == finalizerNodeList.getLength()) {
            String finalizerStr = finalizerNodeList.item(0).getNodeValue().trim();
            flowBuilder.finalizer(finalizerStr);
        }

    }

    private String processStartNode(XPath xpath, Node flowDefinition, FlowBuilder flowBuilder) throws XPathExpressionException {
        String startNodeId = null;
        NodeList startNodeList = (NodeList) xpath.evaluate(".//ns1:start-node/text()", flowDefinition, XPathConstants.NODESET);
        if (1 < startNodeList.getLength()) {
            throw new XPathExpressionException("Within <flow-definition> at most one <start-node> is allowed");
        }
        if (null != startNodeList && 1 == startNodeList.getLength()) {
            startNodeId = startNodeList.item(0).getNodeValue().trim();
        }

        return startNodeId;
    }

    protected String getAttribute(Node node, String attrName) {
        notNull("flow definition element", node);

        String result = null;
        NamedNodeMap attrs = node.getAttributes();

        if (null != attrs) {
            Attr idAttr = (Attr) attrs.getNamedItem(attrName);
            if (null != idAttr) {
                result = idAttr.getValue();
            }
        }

        return result;
    }

    protected String getIdAttribute(Node node) throws XPathExpressionException {
        notNull("flow definition element", node);

        String result = null;
        NamedNodeMap attrs = node.getAttributes();
        String localName = "";
        boolean throwException = false;

        if (null != attrs) {
            Attr idAttr = (Attr) attrs.getNamedItem("id");
            if (null != idAttr) {
                result = idAttr.getValue();
                if (!idAttr.isId()) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Element {0} has an id attribute, but it is not declared as type xsd:id", node.getLocalName());
                    }
                }
            } else {
                localName = node.getLocalName();
                throwException = true;
            }
        } else {
            localName = node.getLocalName();
            throwException = true;
        }

        if (throwException) {
            throw new XPathExpressionException("<" + localName + "> must have an \"id\" attribute.");
        }

        return result;
    }

}
