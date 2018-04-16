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

package com.sun.faces.generate;

import com.sun.faces.config.beans.AttributeBean;
import com.sun.faces.config.beans.PropertyBean;

/**
 * This class generates tag handler class code that is specifc to the
 * "html_basic" package and JSP 2.1.
 */
public class HtmlTaglib21Generator extends HtmlTaglib12Generator {


    // ------------------------------------------------------------ Constructors


    public HtmlTaglib21Generator(PropertyManager propManager) {

        super(propManager);
        addImport("javax.el.*");
        addImport("javax.faces.validator.*");
        addImport("javax.faces.event.*");
        addImport("javax.faces.*");

    } // END HtmlTaglib21Generator


    public static void main(String[] args) {

        PropertyManager manager = PropertyManager.newInstance(args[0]);
        try {
            Generator generator = new HtmlTaglib21Generator(manager);
            generator.generate(GeneratorUtil.getConfigBean(args[1]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    } // END main


    // ------------------------------------------------------- Protected Methods


    protected void writeClassDeclaration() throws Exception {

        // Generate the class declaration
        writer.writePublicClassDeclaration(tagClassName,
                                           "UIComponentELTag",
                                           null, false, false);


    } // END writeClassDeclaration


    /*
     * Override to take into account javax.el.ValueExpression and
     * javax.el.MethodExpression types.
     */
    protected void tagHandlerSetterMethods() throws Exception {

        writer.writeLineComment("Setter Methods");

        // Generate from component properties
        //
        PropertyBean[] properties = component.getProperties();

        for (int i = 0, len = properties.length; i < len; i++) {
            PropertyBean property = properties[i];

            if (property == null) {
                continue;
            }
            if (!property.isTagAttribute()) {
                continue;
            }

            String propertyName = property.getPropertyName();
            String propertyType = property.getPropertyClass();

            // SPECIAL - Don't generate these properties
            if ("binding".equals(propertyName)
                || "id".equals(propertyName)
                || "rendered".equals(propertyName)) {
                continue;
            }

            if (property.isMethodExpressionEnabled()) {
                writer.writeWriteOnlyProperty(propertyName, "javax.el.MethodExpression");
            } else if (property.isValueExpressionEnabled()) {
                writer.writeWriteOnlyProperty(propertyName, "javax.el.ValueExpression");
            } else {
                writer.writeWriteOnlyProperty(propertyName, propertyType);
            }
        }

        // Generate from renderer attributes..
        //
        AttributeBean[] attributes = renderer.getAttributes();
        for (int i = 0, len = attributes.length; i < len; i++) {
            AttributeBean attribute = attributes[i];

            if (attribute == null) {
                continue;
            }
            if (!attribute.isTagAttribute()) {
                continue;
            }
            String attributeName = attribute.getAttributeName();

            writer.writeWriteOnlyProperty(attributeName,
                "javax.el.ValueExpression");

        }
        writer.write("\n");

    } // END tagHandlerSetterMethods


    /*
     * Override to take into account javax.el.ValueExpression and
     * javax.el.MethodExpression types.
     */
    protected void tagHanderSetPropertiesMethod() throws Exception {

        String componentType = component.getComponentType();
        String componentClass = component.getComponentClass();

        writer.fwrite("protected void setProperties(UIComponent component) {\n");
        writer.indent();
        writer.fwrite("super.setProperties(component);\n");

        String iVar =
            GeneratorUtil.stripJavaxFacesPrefix(componentType).toLowerCase();

        writer.fwrite(componentClass + ' ' + iVar + " = null;\n");

        writer.fwrite("try {\n");
        writer.indent();
        writer.fwrite(iVar + " = (" + componentClass + ") component;\n");
        writer.outdent();
        writer.fwrite("} catch (ClassCastException cce) {\n");
        writer.indent();
        writer.fwrite("throw new IllegalStateException(\"Component \" + " +
            "component.toString() + \" not expected type.  Expected: " +
            componentClass +
            ".  Perhaps you're missing a tag?\");\n");
        writer.outdent();
        writer.fwrite("}\n\n");

        if (isValueHolder(componentClass)) {

            writer.fwrite("if (converter != null) {\n");
            writer.indent();
            writer.fwrite("if (!converter.isLiteralText()) {\n");
            writer.indent();
            writer.fwrite(iVar +
                ".setValueExpression(\"converter\", converter);\n");
            writer.outdent();
            writer.fwrite("} else {\n");
            writer.indent();
            writer.fwrite("Converter conv = FacesContext.getCurrentInstance().getApplication()." +
                "createConverter(converter." +
                "getExpressionString());\n");
            writer.fwrite(iVar + ".setConverter(conv);\n");
            writer.outdent();
            writer.fwrite("}\n");
            writer.outdent();
            writer.fwrite("}\n\n");
           
        }

        // Generate "setProperties" method contents from component properties
        //
        PropertyBean[] properties = component.getProperties();
        for (int i = 0, len = properties.length; i < len; i++) {
            PropertyBean property = properties[i];

            if (property == null) {
                continue;
            }
            if (!property.isTagAttribute()) {
                continue;
            }

            String propertyName = property.getPropertyName();           

            // SPECIAL - Don't generate these properties
            if ("binding".equals(propertyName) ||
                "id".equals(propertyName) ||
                "rendered".equals(propertyName) ||
                "converter".equals(propertyName)) {
                continue;
            }
            String ivar = mangle(propertyName);           
            String comp =
                GeneratorUtil.stripJavaxFacesPrefix(componentType).toLowerCase();
            String capPropName = capitalize(propertyName);

            if (property.isValueExpressionEnabled()) {
                writer.fwrite("if (" + ivar + " != null) {\n");
                writer.indent();
                writer.fwrite(comp);
                if ("_for".equals(ivar)) {
                    writer.write(".setValueExpression(\"for\", " +
                                 ivar + ");\n");
                } else {
                    writer.write(".setValueExpression(\"" + ivar + "\", " +
                                 ivar + ");\n");
                }
                writer.outdent();
                writer.fwrite("}\n");
            } else if (property.isMethodExpressionEnabled()) {
                if ("action".equals(ivar)) {
                    writer.fwrite("if (" + ivar + " != null) {\n");
                    writer.indent();

                    writer.fwrite(comp + ".setActionExpression(" + ivar +
                        ");\n");
                    
                    writer.outdent();
                    writer.fwrite("}\n");
                } else {
                    writer.fwrite("if (" + ivar + " != null) {\n");
                    writer.indent();
                    
                    writer.fwrite(comp + ".add" + capitalize(ivar) +
                        "(new MethodExpression" + capitalize(ivar) + '(' +
                        ivar + "));\n");

                    writer.outdent();
                    writer.fwrite("}\n");
                }
            } else {
                writer.fwrite("if (" + ivar + " != null) {\n");
                writer.indent();
                writer.fwrite(comp + ".set" + capPropName + "(" + ivar + ");\n");
                writer.outdent();
                writer.fwrite("}\n");
            }
        }


        // Generate "setProperties" method contents from renderer attributes
        //
        AttributeBean[] attributes = renderer.getAttributes();
        for (int i = 0, len = attributes.length; i < len; i++) {
            AttributeBean attribute = attributes[i];
            if (attribute == null) {
                continue;
            }
            if (!attribute.isTagAttribute()) {
                continue;
            }
            String attributeName = attribute.getAttributeName();           

            String ivar = mangle(attributeName);
            String comp =
                GeneratorUtil.stripJavaxFacesPrefix(componentType).toLowerCase();

            writer.fwrite("if (" + ivar + " != null) {\n");
            writer.indent();
           
            writer.fwrite(comp);
            if ("_for".equals(ivar)) {              
                writer.write(".setValueExpression(\"for\", " +
                    ivar + ");\n");
            } else {
                writer.write(".setValueExpression(\"" + ivar + "\", " +
                    ivar + ");\n");
            }
            writer.outdent();          
            writer.fwrite("}\n");
        }



        writer.outdent();
        writer.fwrite("}\n");

    }

}
