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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.sun.faces.config.beans.ComponentBean;
import com.sun.faces.config.beans.DescriptionBean;
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.PropertyBean;
import com.sun.faces.util.ToolsUtil;


/**
 * <p>Generate concrete HTML component classes.  Classes will be generated for
 * each <code>&lt;component&gt;</code> element in the specified configuration
 * file whose absolute class name is in package <code>javax.faces.component.html</code>.</p>
 * <p/>
 * <p>This application requires the following command line options:</p> <ul>
 * <li><strong>--config</strong> Absolute pathname to an input configuration
 * file that will be parsed by the <code>parse()</code> method.</li>
 * <li><strong>--copyright</strong> Absolute pathname to a file containing the
 * copyright material for the top of each Java source file.</li>
 * <li><strong>--dir</strong> Absolute pathname to the directory into which
 * generated Java source code will be created.</li> <li><strong>--dtd</strong>
 * Pipe delimited list of public identifiers and absolute pathnames to files
 * containing the DTDs used to validate the input configuration files.
 * PRECONDITION: The list is the sequence: <public id>|<dtd path>|<public
 * id>|<dtd path>...</li> </ul>
 */

public class HtmlComponentGenerator extends AbstractGenerator {


    // -------------------------------------------------------- Static Variables


    private static final Logger logger = Logger.getLogger(ToolsUtil.FACES_LOGGER +
            ToolsUtil.GENERATE_LOGGER, ToolsUtil.TOOLS_LOG_STRINGS);

    // The component configuration bean for the component class to be generated
    private ComponentBean cb;


    // Base object of the configuration beans
    private FacesConfigBean configBean;

    // List of relevant properties for the component class to be generated
    private List<PropertyBean> properties;

    // The Writer for each component class to be generated
    private CodeWriter writer;

    private PropertyManager propManager;

	private boolean useBehavior;

    // ------------------------------------------------------------ Constructors


    public HtmlComponentGenerator(PropertyManager propManager) {

        this.propManager = propManager;

    }


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Generate the concrete HTML component class based on the current
     * component configuration bean.</p>
     */
    public void generate(FacesConfigBean configBean) {

        this.configBean = configBean;

        try {
            generateClasses();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    // ---------------------------------------------------------- Private Method

    private void generateClasses() throws Exception {

        final String compPackage = "javax.faces.component.html";

        // Component generator doesn't use the TARGET_PACKAGE property
        String packagePath = compPackage.replace('.', File.separatorChar);
        File dir = new File(System.getProperty("user.dir") +
            File.separatorChar +
            propManager.getProperty(PropertyManager.BASE_OUTPUT_DIR) +
            File.separatorChar + packagePath);
        ComponentBean[] cbs = configBean.getComponents();
        for (ComponentBean cb1 : cbs) {
            if (cb1.isIgnore()) {
                continue;
            }
            String componentClass = cb1.getComponentClass();
            if (componentClass.startsWith(compPackage)) {
                cb = cb1;

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE,
                               "Generating concrete HTML component class " +
                               cb.getComponentClass());
                }

                // Initialize all per-class static variables
                properties = new ArrayList<PropertyBean>();

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = cb.getComponentClass();
                fileName = fileName.substring(fileName.lastIndexOf('.') + 1) +
                           ".java";
                File file = new File(dir, fileName);
                writer = new CodeWriter(new FileWriter(file));
                useBehavior = false;
                for (PropertyBean property : cb.getProperties()) {
        			if(null != property.getBehaviors() && !property.getBehaviors().isEmpty()){
        				useBehavior = true;
        				break;
        			}
        		}
                // Generate the various portions of each class
                prefix();
                properties();
                suffix();

                // Flush and close the Writer for this class
                writer.flush();
                writer.close();
            }


        }
    }


    /**
     * <p>Generate the prefix for this component class, down to (and including)
     * the class declaration.</p>
     */
    private void prefix() throws Exception {

        // Acquire the config bean for our base component
        ComponentBean base = configBean.getComponent(cb.getBaseComponentType());
        if (base == null) {
            throw new IllegalArgumentException("No base component type for '" +
                cb.getComponentType() + "'");
        }
        // Generate the copyright information
        writer.writeBlockComment(
            propManager.getProperty(PropertyManager.COPYRIGHT));

        // Generate the package declaration
        writer.writePackage("javax.faces.component.html");

        writer.write('\n');

        // Generate the imports
        writer.writeImport("java.io.IOException");
        writer.writeImport("java.util.ArrayList");
        writer.writeImport("java.util.Arrays");
        if(useBehavior){
            writer.writeImport("java.util.Collection");        	
            writer.writeImport("java.util.Collections");        	
        }
        writer.writeImport("java.util.List");
        writer.write('\n');
        writer.writeImport("javax.faces.context.FacesContext");
        if( useBehavior){
        	writer.writeImport("javax.faces.component.behavior.ClientBehaviorHolder");
        }
        writer.writeImport("javax.el.MethodExpression");
        writer.writeImport("javax.el.ValueExpression");
        writer.write("\n\n");

        writer.writeBlockComment("******* GENERATED CODE - DO NOT EDIT *******");
        writer.write("\n\n");

        // Generate the class JavaDocs (if any)
        DescriptionBean db = cb.getDescription("");
        String rendererType = cb.getRendererType();

        String description = null;
        if (db != null) {
            description = db.getDescription().trim();
        }

        if (rendererType != null) {
            if (description == null) {
                description = "";
            }
            description +=
            "\n<p>By default, the <code>rendererType</code> property must be set to \"<code>" +
                rendererType +
                "</code>\".\nThis value can be changed by calling the <code>setRendererType()</code> method.</p>\n";
        }

        if (description != null && description.length() > 0) {
            writer.writeJavadocComment(description);
        }

        // Generate the class declaration
        writer.writePublicClassDeclaration(shortName(cb.getComponentClass()),
                                           base.getComponentClass(),
                                           useBehavior?(new String[]{"ClientBehaviorHolder"}):null, false, false);

        writer.write("\n\n");

        writer.indent();

        writer.fwrite("private static final String OPTIMIZED_PACKAGE = \"javax.faces.component.\";\n\n");

        // Generate the constructor

        writer.fwrite("public ");
        writer.write(shortName(cb.getComponentClass()));
        writer.write("() {\n");
        writer.indent();
        writer.fwrite("super();\n");
        if (rendererType != null) {
            writer.fwrite("setRendererType(\"");
            writer.write(rendererType);
            writer.write("\");\n");
        }

        PropertyBean[] pbs = cb.getProperties();
        for (PropertyBean pb : pbs) {
            if (pb.isPassThrough() && pb.getDefaultValue() != null) {
                writer.fwrite("handleAttribute(\"");
                writer.write(pb.getPropertyName());
                writer.write("\", ");
                writer.write(pb.getDefaultValue());
                writer.write(");\n");

            }
        }

        writer.outdent();
        writer.fwrite("}\n\n\n");

        // Generate the manifest constant for the component type
        writer.writeJavadocComment(
            "<p>The standard component type for this component.</p>\n");
        writer.fwrite("public static final String COMPONENT_TYPE = \"");
        writer.write(cb.getComponentType());
        writer.write("\";\n\n\n");

    }

    /**
     * <p>Generate the property instance variable, and getter and setter
     * methods, for all non-duplicate properties of this component class.</p>
     */
    private void properties() throws Exception {

        ComponentBean base = configBean.getComponent(cb.getBaseComponentType());
        PropertyBean[] pbs = cb.getProperties();
        writer.fwrite("protected enum PropertyKeys {\n");
        writer.indent();

        for (PropertyBean pb : pbs) {
            if (base.getProperty(pb.getPropertyName()) != null) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Skipping base class property '" +
                                            pb.getPropertyName() + "'");
                }
                continue;
            }
            if ("for".equals(pb.getPropertyName())) {
                writer.fwrite(pb.getPropertyName());
                writer.write("Val(\"for\")");
            } else if ("public".equals(pb.getPropertyName())) {
                writer.fwrite(pb.getPropertyName());
                writer.write("Val(\"public\")");
            } else {
                writer.fwrite(pb.getPropertyName());
            }
            writer.write(",\n");
        }
        writer.write(";\n");
        writer.fwrite("String toString;\n");
        writer.fwrite("PropertyKeys(String toString) { this.toString = toString; }\n");
        writer.fwrite("PropertyKeys() { }\n");
        writer.fwrite("public String toString() {\n");
        writer.indent();
        writer.fwrite("return ((toString != null) ? toString : super.toString());\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.outdent();
        writer.write("}\n\n");

        for (PropertyBean pb : pbs) {

            // Should we generate this property?
            if (base.getProperty(pb.getPropertyName()) != null) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Skipping base class property '" +
                                            pb.getPropertyName() + "'");
                }
                continue;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
                           "Generating property variable/getter/setter for '" +
                           pb.getPropertyName() + "'");
            }
            properties.add(pb);
            String type = pb.getPropertyClass();
            String var = mangle(pb.getPropertyName());

            // Generate the instance variable
            //writer.fwrite("private ");
            //writer.write(primitive(type) ? GeneratorUtil.convertToObject(type) : type);
            //writer.write(' ');
            //writer.write(var);
            //writer.write(";\n");

            // Document getter method
            String description = "<p>Return the value of the <code>" +
                                 pb.getPropertyName() + "</code> property.</p>" + 
                                 "\n@return the property value";
            DescriptionBean db = pb.getDescription("");
            if (db != null) {
                String temp = db.getDescription().trim();
                if (temp != null && temp.length() > 0) {
                    description += '\n' + "<p>Contents: " + temp;
                }
            }
            writer.writeJavadocComment(description.trim());

            // Generate the getter method
            writer.fwrite("public ");
            writer.write(type);
            if ("boolean".equals(type)) {
                writer.write(" is");
            } else {
                writer.write(" get");
            }
            writer.write(capitalize(pb.getPropertyName()));
            writer.write("() {\n");
            writer.indent();
            writer.fwrite("return (");
            writer.write(primitive(type) ? GeneratorUtil.convertToObject(type) : type);
            writer.write(") getStateHelper().eval(PropertyKeys.");
            writer.write((pb.getPropertyName().equals("for") || pb.getPropertyName().equals("public")) ? pb
                  .getPropertyName() + "Val" : pb.getPropertyName());
             if (primitive(type) || (pb.getDefaultValue() != null)) {
                writer.write(", ");
                writer.write(pb.getDefaultValue() != null
                             ? pb.getDefaultValue()
                             : TYPE_DEFAULTS.get(type));
            }
            writer.write(");\n\n");
            writer.outdent();
            writer.fwrite("}\n\n");
//            writer.fwrite("if (null != this.");
//            writer.write(var);
//            writer.write(") {\n");
//            writer.indent();
//            writer.fwrite("return this.");
//            writer.write(var);
//            writer.write(";\n");
//            writer.outdent();
//            writer.fwrite("}\n");
//            writer.fwrite("ValueExpression _ve = getValueExpression(\"");
//            writer.write(pb.getPropertyName());
//            writer.write("\");\n");
//            writer.fwrite("if (_ve != null) {\n");
//            writer.indent();
//            writer.fwrite("return (");
//            writer.write(primitive(type)
//                         ? GeneratorUtil.convertToObject(type)
//                         : type);
//            writer.write(
//                  ") _ve.getValue(getFacesContext().getELContext());\n");
//            writer.outdent();
//            writer.fwrite("} else {\n");
//            writer.indent();
//            if (primitive(type) || (pb.getDefaultValue() != null)) {
//                writer.fwrite("return ");
//                writer.write(pb.getDefaultValue() != null
//                             ? pb.getDefaultValue()
//                             : TYPE_DEFAULTS.get(type));
//                writer.write(";\n");
//            } else {
//                writer.fwrite("return null;\n");
//            }
            //writer.outdent();
            //writer.fwrite("}\n");
            //writer.outdent();
            //writer.fwrite("}\n\n");

            // Generate the setter method
            
            String maybeUnderscore = (pb.getPropertyName().equals("for") || pb.getPropertyName().equals("public"))
                    ? "_" : "";
            
            writer.writeJavadocComment("<p>Set the value of the <code>"
                                       +
                                       pb.getPropertyName()
                                       + "</code> property.</p>"
                                       + "\n@param " + maybeUnderscore + pb.getPropertyName() + " the new property value\n");

            writer.fwrite("public void set");
            writer.write(capitalize(pb.getPropertyName()));
            writer.write("(");
            writer.write(type);
            writer.write(' ');
            writer.write(var);
            writer.write(") {\n");
            writer.indent();
            //writer.fwrite("this.");
            //writer.write(var);
            //writer.write(" = ");
            //writer.write(var);
            writer.fwrite("getStateHelper().put(PropertyKeys.");
            writer.write((pb.getPropertyName().equals("for") || pb.getPropertyName().equals("public")) ? pb
                  .getPropertyName() + "Val" : pb.getPropertyName());
            writer.write(", ");
            writer.write(var);
            writer.write(");\n");
           // writer.write(";\n");
            if ((pb.isPassThrough() && pb.getDefaultValue() == null)
                  || (cb.getComponentClass().contains("HtmlCommandButton")
                        && "onclick".equals(pb.getPropertyName()))) {
                writer.fwrite("handleAttribute(\"");
                writer.write(pb.getPropertyName());
                 writer.write("\", ");
                writer.write(var);
                writer.write(");\n");
            }

            writer.outdent();
            writer.fwrite("}\n\n");

            // Generate spacing between properties
            writer.write("\n");

        }

    }


    /**
     * <p>Generate the suffix for this component class.</p>
     */
    private void suffix() throws Exception {
        /*
        writer.fwrite("private Object[] _values;\n\n");
        // Generate the saveState() method
        writer.fwrite("public Object saveState(FacesContext _context) {\n");
        writer.indent();
        writer.fwrite("if (_values == null) {\n");
        writer.indent();
        writer.fwrite("_values = new Object[");
        writer.write(String.valueOf((properties.size() + 1)));
        writer.write("];\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.fwrite("_values[0] = super.saveState(_context);\n");

        int n = 1; // Index into values array
        for (int i = 0; i < properties.size(); i++) {
            PropertyBean pb = properties.get(i);
            String name = mangle(pb.getPropertyName());
            writer.fwrite("_values[");
            writer.write(String.valueOf(n++));
            writer.write("] = ");
            writer.write(name);
            writer.write(";\n");
        }
        writer.fwrite("return _values;\n");
        writer.outdent();
        writer.write("}\n\n\n");

        // Generate the restoreState() method
        writer.fwrite(
            "public void restoreState(FacesContext _context, Object _state) {\n");
        writer.indent();
        writer.fwrite("_values = (Object[]) _state;\n");
        writer.fwrite("super.restoreState(_context, _values[0]);\n");
        n = 1;
        for (int i = 0, size = properties.size(); i < size; i++) {
            PropertyBean pb = properties.get(i);
            String name = mangle(pb.getPropertyName());
            String type = pb.getPropertyClass();
            writer.fwrite("this.");
            writer.write(name);
            writer.write(" = ");

            writer.write("(");
            writer.write(primitive(type)
                         ? GeneratorUtil.convertToObject(type)
                         : type);
            writer.write(") _values[");
            writer.write(String.valueOf(n++));
            writer.write("]");

            writer.write(";\n");

        }
        writer.outdent();
        writer.fwrite("}\n\n\n");
        */
        // ClientBehaviorHolder methods
        if(useBehavior){
        	writer.fwrite("private static final Collection<String> EVENT_NAMES = Collections.unmodifiableCollection(Arrays.asList(");
        	boolean first = true;
        	String defaultEventName = null;
        	for (PropertyBean property : cb.getProperties()) {
				if (null != property.getBehaviors()
						&& !property.getBehaviors().isEmpty()) {
					String behaviorName = null;
					for (String behavior : property.getBehaviors()) {
						if (!first) {
							writer.write(",");
						} else {
							first = false;
						}
						writer.write("\"");
						if (0 == behavior.length()) {
							behaviorName = property.getPropertyName();
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
					if (property.isDefaultBehavior()) {
						defaultEventName = behaviorName;
					}
				}
			}
        	writer.write("));\n\n");
            writer.fwrite("public Collection<String> getEventNames() {\n");
            writer.indent();
            writer.fwrite("return EVENT_NAMES;");
            writer.outdent();
            writer.fwrite("}\n\n\n");        	
            writer.fwrite("public String getDefaultEventName() {\n");
            writer.indent();
            if(null == defaultEventName){
                writer.fwrite("return null;");
            } else {
                writer.fwrite("return \""+defaultEventName+"\";");
            }
            writer.outdent();
            writer.fwrite("}\n\n\n");        	
        }
        writer.fwrite( "private void handleAttribute(String name, Object value) {\n");
        writer.indent();
        writer.fwrite("List<String> setAttributes = (List<String>) this.getAttributes().get(\"javax.faces.component.UIComponentBase.attributesThatAreSet\");\n");
        writer.fwrite("if (setAttributes == null) {\n");
        writer.indent();
        writer.fwrite("String cname = this.getClass().getName();\n");
        writer.fwrite("if (cname != null && cname.startsWith(OPTIMIZED_PACKAGE)) {\n");
        writer.indent();
        writer.fwrite("setAttributes = new ArrayList<String>(6);\n");
        writer.fwrite("this.getAttributes().put(\"javax.faces.component.UIComponentBase.attributesThatAreSet\", setAttributes);\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.fwrite("if (setAttributes != null) {\n");
        writer.indent();
        writer.fwrite("if (value == null) {\n");
        writer.indent();
        writer.fwrite("ValueExpression ve = getValueExpression(name);\n");
        writer.fwrite("if (ve == null) {\n");
        writer.indent();
        writer.fwrite("setAttributes.remove(name);\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.outdent();
        writer.fwrite("} else if (!setAttributes.contains(name)) {\n");
        writer.indent();
        writer.fwrite("setAttributes.add(name);\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.outdent();
        writer.fwrite("}\n\n");


        // Generate the ending of this class
        writer.outdent();
        writer.write("}\n");

    }



    // ------------------------------------------------------------- Main Method


    public static void main(String[] args) throws Exception {
        PropertyManager propManager = PropertyManager.newInstance(args[0]);
        Generator generator = new HtmlComponentGenerator(propManager);
        generator.generate(GeneratorUtil.getConfigBean(args[1]));
    }

}
