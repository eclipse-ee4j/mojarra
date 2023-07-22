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

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.faces.config.ConfigurationException;
import com.sun.faces.config.Verifier;
import com.sun.faces.config.manager.documents.DocumentInfo;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * This <code>ConfigProcessor</code> handles all elements defined under <code>/faces-config/valiator</code>.
 * </p>
 */
public class ValidatorConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    /**
     * <p>
     * /faces-config/validator
     * </p>
     */
    private static final String VALIDATOR = "validator";

    /**
     * <p>
     * /faces-config/component/validator-id
     * </p>
     */
    private static final String VALIDATOR_ID = "validator-id";

    /**
     * <p>
     * /faces-config/component/validator-class
     * </p>
     */
    private static final String VALIDATOR_CLASS = "validator-class";

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext servletContext, FacesContext facesContext, DocumentInfo[] documentInfos) throws Exception {

        // Process annotated Validators first as Validators configured
        // via config files take precedence
        processAnnotations(facesContext, FacesValidator.class);

        for (DocumentInfo documentInfo : documentInfos) {
            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, format("Processing validator elements for document: ''{0}''", documentInfo.getSourceURI()));
            }

            Document document = documentInfo.getDocument();
            String namespace = document.getDocumentElement().getNamespaceURI();
            NodeList validators = document.getDocumentElement().getElementsByTagNameNS(namespace, VALIDATOR);

            if (validators != null && validators.getLength() > 0) {
                addValidators(facesContext, validators, namespace);
            }
        }

        processDefaultValidatorIds();
    }

    // --------------------------------------------------------- Private Methods

    private void processDefaultValidatorIds() {
        Application app = getApplication();
        Map<String, String> defaultValidatorInfo = app.getDefaultValidatorInfo();
        for (Map.Entry<String, String> info : defaultValidatorInfo.entrySet()) {
            String defaultValidatorId = info.getKey();
            boolean found = false;
            for (Iterator<String> registered = app.getValidatorIds(); registered.hasNext();) {
                if (defaultValidatorId.equals(registered.next())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new ConfigurationException(format("Default validator ''{0}'' does not reference a registered validator.", defaultValidatorId));
            }
        }

    }

    private void addValidators(FacesContext facesContext, NodeList validators, String namespace) throws XPathExpressionException {
        Application application = getApplication();
        Verifier verifier = Verifier.getCurrentInstance();
        for (int i = 0, size = validators.getLength(); i < size; i++) {
            Node validator = validators.item(i);

            NodeList children = ((Element) validator).getElementsByTagNameNS(namespace, "*");
            String validatorId = null;
            String validatorClass = null;

            for (int c = 0, csize = children.getLength(); c < csize; c++) {
                Node n = children.item(c);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getLocalName()) {
                    case VALIDATOR_ID:
                        validatorId = getNodeText(n);
                        break;
                    case VALIDATOR_CLASS:
                        validatorClass = getNodeText(n);
                        break;
                    }
                }
            }

            if (validatorId != null && validatorClass != null) {
                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, format("Calling Application.addValidator({0},{1})", validatorId, validatorClass));
                }

                boolean doAdd = true;
                if (validatorId.equals(BeanValidator.VALIDATOR_ID)) {
                    doAdd = ApplicationConfigProcessor.isBeanValidatorAvailable(facesContext);
                }

                if (doAdd) {
                    if (verifier != null) {
                        verifier.validateObject(Verifier.ObjectType.VALIDATOR, validatorClass, Validator.class);
                    }
                    application.addValidator(validatorId, validatorClass);
                }
            }

        }
    }

}
