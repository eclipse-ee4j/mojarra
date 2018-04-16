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

package com.sun.faces.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.text.MessageFormat;

/**
 * <p>An <code>Ant</code> task to perform schema validation of the
 * <code>standard-html-renderkit.xml</code> document.  The optional task,
 * <code>xmlvalidate</code> is a hassel when it comes to schema.
 */
public class ValidateTask extends Task {

    private String schemaDir;


    // -------------------------------------------------------------- Properties


    public void setSchemaDir(String schemaDir) {

        this.schemaDir = schemaDir;

    } // END setSchemaDir


    // ------------------------------------------------------- Methods from Task


    public void execute() throws BuildException {

        File dir = new File(schemaDir);
        if (!dir.isDirectory()) {
            throw new BuildException("The schemaDir '" + schemaDir +
                "' is not a directory");
        }

        if (!dir.canRead()) {
            throw new BuildException("The schemaDir '" + schemaDir +
                "' cannot be read");
        }

        SAXParser parser = getParser();
        String file = schemaDir + File.separatorChar +
            "standard-html-renderkit.xml";
        try {
            parser.parse(new File(file), new Resolver(schemaDir));
            System.out.println("The document, standard-html-renderkit.xml, is valid.");
        } catch (Exception e) {
            throw new BuildException(e);
        }

    } // END execute


    // --------------------------------------------------------- Private Methods


    private SAXParser getParser() throws BuildException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        File schemaSource = new File(schemaDir + File.separatorChar +
            "web-facesconfig_2_0.xsd");
        try {
            SAXParser parser = factory.newSAXParser();
            parser.setProperty(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
            parser.setProperty(
                "http://java.sun.com/xml/jaxp/properties/schemaSource",
                schemaSource);
            return parser;
        } catch (Exception e) {
            throw new BuildException(e);
        }

    } // END getParser


    // ----------------------------------------------------------- Inner Classes


    private static class Resolver extends DefaultHandler {

        private String j2ee14;
        private String webServices;
        private String xml;
        private String facesConfig;

        private MessageFormat message =
            new MessageFormat("({0}: {1}, {2}): {3}");


        public Resolver(String schemaDir) {

            String basePath = schemaDir + File.separatorChar;
            j2ee14 = basePath + "j2ee_1_4.xsd";
            webServices = basePath + "j2ee_web_services_client_1_1.xsd";
            xml = basePath + "xml.xsd";
            facesConfig = basePath + "web-facesconfig_1_2.xsd";

        } // END Resolver


        private String print(SAXParseException x) {

            String msg = message.format(new Object[]
            {
                x.getSystemId(),
                new Integer(x.getLineNumber()),
                new Integer(x.getColumnNumber()),
                x.getMessage()
            });

            return msg;

        } // END print


        public void warning(SAXParseException x) {

            System.out.println("WARNING: " + print(x));

        } // END warning


        public void error(SAXParseException x) throws SAXParseException{

            System.out.println("ERROR: " + print(x));
            throw x;

        } // END error


        public void fatalError(SAXParseException x) throws SAXParseException {

            System.out.println("FATAL: " + print(x));
            throw x;

        } // END fatalError


        public InputSource resolveEntity(String publicId,
                                         String systemId) {

            InputSource source = null;
            if (systemId.indexOf("j2ee_1_4") > 0) {
                try {
                    source =
                    new InputSource(new FileInputStream(j2ee14));
                    source.setSystemId(new File(j2ee14).toURL().toString());
                } catch (FileNotFoundException e) {
                    //
                } catch (MalformedURLException e) {
		    //
		}
            } else if (systemId.indexOf("webservice") > 0) {
                try {
                    source =
                    new InputSource(new FileInputStream(webServices));
                    source.setSystemId(
                        new File(webServices).toURL().toString());
                } catch (FileNotFoundException e) {
                    //
                } catch (MalformedURLException e) {
		    //
		}
            } else if (systemId.indexOf("xml.xsd") > 0) {
                try {
                    source =
                    new InputSource(new FileInputStream(xml));
                    source.setSystemId(xml);
                } catch (FileNotFoundException e) {
                    //
		}
            } else if (systemId.indexOf("web-facesconfig_2_0.xsd") > 0) {
                try {
                    source =
                    new InputSource(new FileInputStream(facesConfig));
                    source.setSystemId(
                        new File(facesConfig).toURL().toString());
                } catch (FileNotFoundException e) {
                    //
                } catch (MalformedURLException e) {
		    //
		}
            } else {
                try {
                    source = super.resolveEntity(publicId, systemId);
                    if (source != null && publicId != null)
                        source.setPublicId(publicId);
                    if (source != null && systemId != null)
                        source.setSystemId(systemId);
                } catch (Exception e) {
                    //
                }
            }

            return source;

        } // END resolveEntity

    } // END Resolver

}
