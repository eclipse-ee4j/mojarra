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

package com.sun.faces.application.applicationimpl;

import static com.sun.faces.cdi.CdiUtils.getBeanReference;
import static com.sun.faces.util.Util.getCdiBeanManager;
import static com.sun.faces.util.Util.getWebXmlVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import com.sun.faces.cdi.CdiExtension;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;

public class Version {

    private Boolean isJsf23;

    /**
     * Are we running in JSF 2.3+
     *
     * @return true if we are, false otherwise.
     */
    public boolean isJsf23() {

        if (isJsf23 == null) {

            FacesContext facesContext = FacesContext.getCurrentInstance();

            BeanManager beanManager = getCdiBeanManager(facesContext);

            if (beanManager == null) {
                // TODO: use version enum and >=

                String facesVersion = getFacesConfigXmlVersion(facesContext);

                if (facesVersion.equals("3.0") || getWebXmlVersion(facesContext).equals("4.0")) {
                    throw new FacesException("Unable to find CDI BeanManager");
                }
                isJsf23 = false;
            } else {
                isJsf23 = getBeanReference(beanManager, CdiExtension.class).isAddBeansForJSFImplicitObjects();
            }

        }

        return isJsf23;
    }

    /**
     * Get the faces-config.xml version (if any).
     *
     * @param facesContext the Faces context.
     * @return the version found, or "" if none found.
     */
    private String getFacesConfigXmlVersion(FacesContext facesContext) {
        String result = "";
        try {
            URL url = facesContext.getExternalContext().getResource("/WEB-INF/faces-config.xml");
            if (url != null) {
                XPath xpath = XPathFactory.newInstance().newXPath();
                xpath.setNamespaceContext(new JakartaeeNamespaceContext());
                try (InputStream stream = url.openStream()) {
                    result = xpath.evaluate("string(/jakartaee:faces-config/@version)", new InputSource(stream));
                }
            }
        } catch (XPathExpressionException | IOException  mue) {
            // Ignore
        }

        return result;
    }

    public class JakartaeeNamespaceContext implements NamespaceContext {

        @Override
        public String getNamespaceURI(String prefix) {
            return "https://jakarta.ee/xml/ns/jakartaee";
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return "jakartaee";
        }

        @Override
        public Iterator<?> getPrefixes(String namespaceURI) {
            return null;
        }
    }

}
