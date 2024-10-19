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

package jakarta.faces.webapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import com.sun.faces.config.DigesterFactory;

/**
 * <p>
 * Unit tests for Configuration File processing.
 * </p>
 */
public class ConfigFileTestCase {

    // The public identifier of our DTD
    protected String CONFIG_DTD_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN";

    // ----------------------------------------------------- Instance Variables
    /**
     * The Digester instance we will use to parse configuration files.
     */
    protected Digester digester = null;

    // --------------------------------------------------- Overall Test Methods
    /**
     * Set up instance variables required by this test case.
     *
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        digester = createDigester();
        configureRules(digester);
    }

    // ------------------------------------------------ Individual Test Methods
    // Test parsing an empty configuration file
    @Test
    public void testEmpty() throws Exception {
        ConfigBase base = parseConfig(relativeURL("src/test/java/jakarta/faces/webapp/config-file-0.xml"));
        assertNotNull(base);
    }

    // Test parsing a full configuration file
    @Test
    public void testFull() throws Exception {
        // Retrieve entire configuration metadata instance
        ConfigBase base = parseConfig(relativeURL("src/test/java/jakarta/faces/webapp/config-file-1.xml"));
        assertNotNull(base);

        // <application>
        assertEquals("com.mycompany.MyActionListener", base.getActionListener());
        assertEquals("com.mycompany.MyNavigationHandler", base.getNavigationHandler());

        // <component>
        Map<?, ?> components = base.getComponents();
        assertNotNull(components);
        ConfigComponent ccomp1 = (ConfigComponent) components.get("Command");
        assertNotNull(ccomp1);
        assertEquals("User Interface Command Component", ccomp1.getDescription());
        assertEquals("User Interface Command", ccomp1.getDisplayName());
        assertEquals("Command", ccomp1.getComponentType());
        assertEquals("jakarta.faces.component.UICommand", ccomp1.getComponentClass());
        assertNull(ccomp1.getLargeIcon());
        assertNull(ccomp1.getSmallIcon());
        assertEquals(0, ccomp1.getAttributes().size());
        assertEquals(0, ccomp1.getProperties().size());

        // <converter>
        Map<?, ?> converters = base.getConverters();
        assertNotNull(converters);
        ConfigConverter cc1 = (ConfigConverter) converters.get("First");
        assertNotNull(cc1);
        assertEquals("First Converter Description", cc1.getDescription());
        assertEquals("First Converter Display Name", cc1.getDisplayName());
        assertEquals("firstConverter.gif", cc1.getLargeIcon());
        assertEquals("firstConverter.jpg", cc1.getSmallIcon());
        assertEquals("First", cc1.getConverterId());
        assertEquals("com.mycompany.MyFirstConverter", cc1.getConverterClass());
        assertEquals(1, cc1.getAttributes().size());
        ConfigAttribute cc1a1 = (ConfigAttribute) cc1.getAttributes().get("attr1");
        assertNotNull(cc1a1);
        assertEquals("First Converter Attribute 1 Description", cc1a1.getDescription());
        assertEquals("First Converter Attribute 1 Display Name", cc1a1.getDisplayName());
        assertNull(cc1a1.getLargeIcon());
        assertNull(cc1a1.getSmallIcon());
        assertEquals("attr1", cc1a1.getAttributeName());
        assertEquals("java.lang.String", cc1a1.getAttributeClass());
        assertEquals(0, cc1.getProperties().size());
        ConfigConverter cc2 = (ConfigConverter) converters.get("Second");
        assertNotNull(cc2);
        assertEquals("Second Converter Description", cc2.getDescription());
        assertEquals("Second Converter Display Name", cc2.getDisplayName());
        assertEquals("secondConverter.gif", cc2.getLargeIcon());
        assertEquals("secondConverter.jpg", cc2.getSmallIcon());
        assertEquals("Second", cc2.getConverterId());
        assertEquals("com.mycompany.MySecondConverter", cc2.getConverterClass());
        assertEquals(0, cc2.getAttributes().size());
        assertEquals(1, cc2.getProperties().size());
        ConfigProperty cc2p1 = (ConfigProperty) cc2.getProperties().get("prop1");
        assertNotNull(cc2p1);
        assertEquals("Second Converter Property 1 Description", cc2p1.getDescription());
        assertEquals("Second Converter Property 1 Display Name", cc2p1.getDisplayName());
        assertNull(cc2p1.getLargeIcon());
        assertNull(cc2p1.getSmallIcon());
        assertEquals("prop1", cc2p1.getPropertyName());
        assertEquals("java.lang.String", cc2p1.getPropertyClass());

        // <validator>
        Map<?, ?> validators = base.getValidators();
        assertNotNull(validators);
        ConfigValidator cv1 = (ConfigValidator) validators.get("First");
        assertNotNull(cv1);
        assertEquals("First Validator Description", cv1.getDescription());
        assertEquals("First Validator Display Name", cv1.getDisplayName());
        assertEquals("firstValidator.gif", cv1.getLargeIcon());
        assertEquals("firstValidator.jpg", cv1.getSmallIcon());
        assertEquals("First", cv1.getValidatorId());
        assertEquals("com.mycompany.MyFirstValidator", cv1.getValidatorClass());
        assertEquals(1, cv1.getAttributes().size());
        ConfigAttribute cv1a1 = (ConfigAttribute) cv1.getAttributes().get("attr1");
        assertNotNull(cv1a1);
        assertEquals("First Validator Attribute 1 Description", cv1a1.getDescription());
        assertEquals("First Validator Attribute 1 Display Name", cv1a1.getDisplayName());
        assertNull(cv1a1.getLargeIcon());
        assertNull(cv1a1.getSmallIcon());
        assertEquals("attr1", cv1a1.getAttributeName());
        assertEquals("java.lang.String", cv1a1.getAttributeClass());
        assertEquals(0, cv1.getProperties().size());
        ConfigValidator cv2 = (ConfigValidator) validators.get("Second");
        assertNotNull(cv2);
        assertEquals("Second Validator Description", cv2.getDescription());
        assertEquals("Second Validator Display Name", cv2.getDisplayName());
        assertEquals("secondValidator.gif", cv2.getLargeIcon());
        assertEquals("secondValidator.jpg", cv2.getSmallIcon());
        assertEquals("Second", cv2.getValidatorId());
        assertEquals("com.mycompany.MySecondValidator", cv2.getValidatorClass());
        assertEquals(0, cv2.getAttributes().size());
        assertEquals(1, cv2.getProperties().size());
        ConfigProperty cv2p1 = (ConfigProperty) cv2.getProperties().get("prop1");
        assertNotNull(cv2p1);
        assertEquals("Second Validator Property 1 Description", cv2p1.getDescription());
        assertEquals("Second Validator Property 1 Display Name", cv2p1.getDisplayName());
        assertNull(cv2p1.getLargeIcon());
        assertNull(cv2p1.getSmallIcon());
        assertEquals("prop1", cv2p1.getPropertyName());
        assertEquals("java.lang.String", cv2p1.getPropertyClass());
    }

    // ------------------------------------------------------ Protected Methods
    // Create a Digester instance with no rules yet
    protected Digester createDigester() throws Exception {
        digester = DigesterFactory.newInstance(true).createDigester();
        return (digester);
    }

    // Configure the matching rules for the specified Digester instance
    // Rules assume that a ConfigBase bean is pushed on the stack first
    protected void configureRules(Digester digester) {
        configureRulesApplication(digester);
        configureRulesConverter(digester);
        configureRulesComponent(digester);
        configureRulesValidator(digester);
    }

    // Configure the rules for an <application> element
    protected void configureRulesApplication(Digester digester) {
        digester.addCallMethod("faces-config/application/action-listener", "setActionListener", 0);
        digester.addCallMethod("faces-config/application/navigation-handler", "setNavigationHandler", 0);
    }

    // Configure the rules for a <attribute> element
    protected void configureRulesAttribute(Digester digester, String prefix) {
        digester.addObjectCreate(prefix, "jakarta.faces.webapp.ConfigAttribute");
        digester.addSetNext(prefix, "addAttribute", "jakarta.faces.webapp.ConfigAttribute");
        configureRulesFeature(digester, prefix);
        digester.addCallMethod(prefix + "/attribute-name", "setAttributeName", 0);
        digester.addCallMethod(prefix + "/attribute-class", "setAttributeClass", 0);
    }

    // Configure the rules for a <component> element
    protected void configureRulesComponent(Digester digester) {
        String prefix = "faces-config/component";

        digester.addObjectCreate(prefix, "jakarta.faces.webapp.ConfigComponent");
        digester.addSetNext(prefix, "addComponent", "jakarta.faces.webapp.ConfigComponent");
        configureRulesFeature(digester, prefix);
        digester.addCallMethod(prefix + "/component-type", "setComponentType", 0);
        digester.addCallMethod(prefix + "/component-class", "setComponentClass", 0);
        configureRulesAttribute(digester, prefix + "/attribute");
        configureRulesProperty(digester, prefix + "/property");
    }

    // Configure the rules for a <converter> element
    protected void configureRulesConverter(Digester digester) {
        String prefix = "faces-config/converter";

        digester.addObjectCreate(prefix, "jakarta.faces.webapp.ConfigConverter");
        digester.addSetNext(prefix, "addConverter", "jakarta.faces.webapp.ConfigConverter");
        configureRulesFeature(digester, prefix);
        digester.addCallMethod(prefix + "/converter-id", "setConverterId", 0);
        digester.addCallMethod(prefix + "/converter-class", "setConverterClass", 0);
        configureRulesAttribute(digester, prefix + "/attribute");
        configureRulesProperty(digester, prefix + "/property");
    }

    // Configure the generic feature rules for the specified prefix
    protected void configureRulesFeature(Digester digester, String prefix) {
        digester.addCallMethod(prefix + "/description", "setDescription", 0);
        digester.addCallMethod(prefix + "/display-name", "setDisplayName", 0);
        digester.addCallMethod(prefix + "/icon/large-icon", "setLargeIcon", 0);
        digester.addCallMethod(prefix + "/icon/small-icon", "setSmallIcon", 0);
    }

    // Configure the rules for a <property> element
    protected void configureRulesProperty(Digester digester, String prefix) {
        digester.addObjectCreate(prefix, "jakarta.faces.webapp.ConfigProperty");
        digester.addSetNext(prefix, "addProperty", "jakarta.faces.webapp.ConfigProperty");
        configureRulesFeature(digester, prefix);
        digester.addCallMethod(prefix + "/property-name", "setPropertyName", 0);
        digester.addCallMethod(prefix + "/property-class", "setPropertyClass", 0);
    }

    // Configure the rules for a <validator> element
    protected void configureRulesValidator(Digester digester) {
        String prefix = "faces-config/validator";

        digester.addObjectCreate(prefix, "jakarta.faces.webapp.ConfigValidator");
        digester.addSetNext(prefix, "addValidator", "jakarta.faces.webapp.ConfigValidator");
        configureRulesFeature(digester, prefix);
        digester.addCallMethod(prefix + "/validator-id", "setValidatorId", 0);
        digester.addCallMethod(prefix + "/validator-class", "setValidatorClass", 0);
        configureRulesAttribute(digester, prefix + "/attribute");
        configureRulesProperty(digester, prefix + "/property");
    }

    // Parse the configuration file at the specified URL
    protected ConfigBase parseConfig(URL config) throws Exception {
        digester.clear();
        digester.push(new ConfigBase());
        InputSource iso = new InputSource(config.toExternalForm());
        InputStream ist = config.openStream();
        iso.setByteStream(ist);
        ConfigBase base = (ConfigBase) digester.parse(iso);
        ist.close();
        return (base);
    }

    // Return the URL of the specified path, relative to our base directory
    protected URL relativeURL(String relativePath) throws Exception {
        File file = new File(System.getProperty("base.dir"), relativePath);
        return (file.toURI().toURL());
    }
}
