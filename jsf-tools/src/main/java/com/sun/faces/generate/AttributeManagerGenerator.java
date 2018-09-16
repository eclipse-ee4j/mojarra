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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sun.faces.config.beans.ComponentBean;
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.PropertyBean;

/**
 * This class is used to generate the AttributeManager, which is used
 * by mainly Renderers to render passthrough attribues (via
 * RenderKitUtils#renderPassThruAttributes)
 *
 * <p>
 * The AttributeManager mainly holds a [component name/attributes] map, and the code to
 * create that map is the main things this generator generates.
 *
 * Prior to this generator, Mojarra maintained a static list of passthrough attributes that would
 * be applied to all components when rendered. The main problem with doing this is that some components
 * have a very small list of passthrough attributes and so we'd waste cycles processing the generic list.
 *
 * To alleviate this issue:
 *
 *<pre>
 * 1.  Added a new code generator, AttributeManagerGenerator, which will
 *     leverage the xml metadata in jsf-api/doc to generate a class which
 *     has knowledge of which components have which pass through attributes.
 *     This generated class will expose a method that will allow renderers
 *     to lookup the set of passthrough attributes appropriate to the component
 *     they handle.
 *  2. Update all renderers that render passthrough attributes to store
 *     the attribute lookup result in a static variable
 *  3. Remove the RenderKitUtil.renderPassThruAttributes() methods containing
 *     excludes.  Rely on the generator to provide the correct attributes.
 *  4. Remove all the logic (sorting, verifying) associated with the excludes
 *  5. Update the RenderKitUtils.shouldRenderAttribute() logic to check
 *     if the value to be rendered is a String (as most are).  If it is, return
 *     true and bypass the remainder of the checks.
 *</pre>
 *
 * <p>
 * After profiling RenderKitUtils.renderPassThruAttributes has dropped quite in
 * terms of cpu usage compared to the previous implementation.
 *
 * <p>
 * The following shows an example of this generated Map:
 *
 * <pre>
 * <code>
 * private static Map&lt;String,Attribute[]&gt; ATTRIBUTE_LOOKUP=CollectionsUtils.&lt;String,Attribute[]&gt;map()
 *      .add("CommandButton",ar(
 *          attr("accesskey")
 *          ,attr("alt")
 *          ,attr("dir")
 *          ,attr("lang")
 *          ,attr("onblur","blur")
 *          ...
 * </code>
 * </pre>
 *
 *
 */
public class AttributeManagerGenerator extends AbstractGenerator {

    private static final String TARGET_PACKAGE =
          "com.sun.faces.renderkit";
    private static final String TARGET_CLASSNAME =
          "AttributeManager";

    private PropertyManager manager;
    private List<String> imports;

    // ------------------------------------------------------------ Constructors


    public AttributeManagerGenerator(PropertyManager manager) {
        this.manager = manager;
    }

    // ------------------------------------------ Methods from AbstractGenerator


    @Override
    public void generate(FacesConfigBean configBean) {
        try {
            CodeWriter writer = getCodeWriter();
            writeCopyright(writer);
            writePackage(writer);
            writeImports(writer);
            writeClassDocumentation(writer);
            writeClassDeclaration(writer);
            writeClassBody(writer, configBean);
            writeClassEnd(writer);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // ------------------------------------------------------------- Main Method


    public static void main(String[] args) throws Exception {

        PropertyManager propManager = PropertyManager.newInstance(args[0]);
        Generator generator = new AttributeManagerGenerator(propManager);
        generator.generate(GeneratorUtil.getConfigBean(args[1]));

    }

    // --------------------------------------------------------- Private Methods


    private CodeWriter getCodeWriter() throws IOException {
        FileWriter fWriter = new FileWriter(new File(getClassPackageDirectory(),
                                                     TARGET_CLASSNAME
                                                     + ".java"));
        return new CodeWriter(fWriter);
    }


    private File getClassPackageDirectory() {

        String packagePath = TARGET_PACKAGE.replace('.', File.separatorChar);
        File packageDir = new File(getBaseOutputDirectory(),
                                   packagePath);
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }

        return packageDir;

    } // END getClassPackageDirectory


    private File getBaseOutputDirectory() {

        File outputDir = new File(System.getProperty("user.dir") +
                                  File.separatorChar +
                                  manager
                                        .getProperty(PropertyManager.BASE_OUTPUT_DIR));

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        return outputDir;

    } // END getBaseOutputDirectory


    private void addImport(String fullyQualClassName) {

        if (imports == null) {
            imports = new ArrayList<String>();
        }
        imports.add(fullyQualClassName);

    }

    private void writeImports(CodeWriter writer) throws Exception {
        addImport("java.util.Map");
//        addImport("java.util.HashMap");
//        addImport("java.util.Collections");
        addImport("static com.sun.faces.util.CollectionsUtils.*");
        addImport("com.sun.faces.util.CollectionsUtils");
        addImport("static com.sun.faces.renderkit.Attribute.*");
        addImport("com.sun.faces.renderkit.Attribute");

        Collections.sort(imports);

        for (Iterator i = imports.iterator(); i.hasNext();) {
            writer.writeImport((String) i.next());
        }
        writer.write('\n');

    } // END writeImports


    private void writeCopyright(CodeWriter writer) throws Exception {

        writer.writeBlockComment(
              manager.getProperty(PropertyManager.COPYRIGHT));
        writer.write('\n');

    } // END writeCopyright


    private void writePackage(CodeWriter writer) throws Exception {

        // Generate the package declaration
        writer.writePackage(TARGET_PACKAGE);
        writer.write('\n');

    } // END writePackage


    private void writeClassDocumentation(CodeWriter writer) throws Exception {

        writer.writeJavadocComment("This class contains mappings between the standard components\n"
                                   + "and the passthrough attributes associated with them.");

    } // END writeClassDocumentation


    private void writeClassDeclaration(CodeWriter writer) throws Exception {

        // Generate the class declaration
        writer.writePublicClassDeclaration(TARGET_CLASSNAME,
                                           null,
                                           null,
                                           false,
                                           false);

    } // END writeClassDeclaration


    private void writeClassBody(CodeWriter writer, FacesConfigBean bean)
    throws Exception {
        writer.indent();
        writer.fwrite("private static Map<String,Attribute[]> ATTRIBUTE_LOOKUP=CollectionsUtils.<String,Attribute[]>map()\n");
        writer.indent();

        ComponentBean[] components = bean.getComponents();
        List<String> keys = new ArrayList<String>();
        for (int i = 0, len = components.length; i < len; i ++) {
            ComponentBean comp = components[i];
            if (!comp.getComponentClass().contains("Html")) {
                continue;
            }
            String type = comp.getRendererType();
            if (type != null) {
                String family = comp.getBaseComponentType();
                type = type.substring(type.lastIndexOf('.') + 1);
                family = family.substring(family.lastIndexOf('.') + 1);
                String key = family + type;
                PropertyBean[] props = comp.getProperties();
                boolean attributeWritten = false;
                for (int ii = 0, llen = props.length; ii < llen; ii++) {
                    PropertyBean aBean = props[ii];
                    if (aBean.isPassThrough()) {
                        if ((key.contains("Radio") || "SelectManyCheckbox".equals(key))
                            && ("style".equals(aBean.getPropertyName())
                                || ("border".equals(aBean.getPropertyName())))) {
                            continue;
                        }
                        if (attributeWritten) {
                            writer.fwrite(",attr(\"");
                        } else {
                            keys.add(key);
                            writer.fwrite(".add(\"");
                            writer.write(key);
                            writer.write("\",ar(\n");
                            writer.indent();
                        	writer.fwrite("attr(\"");
                        }
                        writer.write(aBean.getPropertyName());
                        writer.write("\"");
                        if (aBean.getBehaviors() != null
                             && !aBean.getBehaviors().isEmpty()) {
                            for (String behavior : aBean.getBehaviors()) {
                                writer.write(",\"");
                                String behaviorName;
                                if (0 == behavior.length()) {
                                    behaviorName = aBean.getPropertyName();
                                    // Strip leading "on" preffix.
                                    if (behaviorName.length() > 2
                                        && behaviorName.startsWith("on")) {
                                        StringBuilder buffer = new StringBuilder(
                                              behaviorName.substring(2, 3)
                                                    .toLowerCase());
                                        buffer.append(behaviorName.substring(3));
                                        behaviorName = buffer.toString();
                                    }
                                } else {
                                    behaviorName = behavior;
                                }
                                writer.write(behaviorName);
                                writer.write("\"");
                            }
                        }
                        writer.write(")\n");
                        attributeWritten = true;
                    }
                    if (key.contains("Button") && "onclick".equals(aBean.getPropertyName())) {
                        // reset to the original state
                        aBean.setPassThrough(false);
                    }

                }
                if (attributeWritten) {
                    writer.outdent();
                    writer.fwrite("))\n");
                }
            }
        }
        writer.fwrite(".fix();\n");

//        writer.fwrite("ATTRIBUTE_LOOKUP = Collections.unmodifiableMap(map);\n");
//        writer.outdent();
//        writer.fwrite("}\n\n");
        writer.outdent();
        writer.fwrite("public enum Key {\n");
        writer.indent();
        for (int i = 0, len = keys.size(); i < len; i++) {
            String key = keys.get(i);
            writer.fwrite(key.toUpperCase() + "(\"" + key + "\")");
            if (i == (len - 1)) {
                writer.write(";\n");
            } else {
                writer.write(",\n");
            }
        }
        writer.fwrite("private String key;\n");
        writer.fwrite("Key(String key) {\n");
        writer.indent();
        writer.fwrite("this.key = key;\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.fwrite("public String value() {\n");
        writer.indent();
        writer.fwrite("return this.key;\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.write("\n\n");
        writer.fwrite("public static Attribute[] getAttributes(Key key) {\n");
        writer.indent();
        writer.fwrite("return ATTRIBUTE_LOOKUP.get(key.value());\n");
        writer.outdent();
        writer.fwrite("}\n");
    }


    private void writeClassEnd(CodeWriter writer) throws Exception {
        writer.outdent();
        writer.fwrite("}\n");
        writer.flush();
        writer.close();
    }

}
