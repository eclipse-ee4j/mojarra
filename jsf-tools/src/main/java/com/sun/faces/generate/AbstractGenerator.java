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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * <p>Abstract base class for Java and TLD generators.</p>
 *
 * <p>The methods in this class presume the following command line option
 * names and corresponding values:</p>
 */
public abstract class AbstractGenerator {


    // -------------------------------------------------------- Static Variables


    // The set of default values for primitives, keyed by the primitive type
    protected static final Map<String,String> TYPE_DEFAULTS = new HashMap<String, String>();
    static {
        TYPE_DEFAULTS.put("boolean", "false");
        TYPE_DEFAULTS.put("byte", "Byte.MIN_VALUE");
        TYPE_DEFAULTS.put("char", "Character.MIN_VALUE");
        TYPE_DEFAULTS.put("double", "Double.MIN_VALUE");
        TYPE_DEFAULTS.put("float", "Float.MIN_VALUE");
        TYPE_DEFAULTS.put("int", "Integer.MIN_VALUE");
        TYPE_DEFAULTS.put("long", "Long.MIN_VALUE");
        TYPE_DEFAULTS.put("short", "Short.MIN_VALUE");
    }


    // The set of reserved keywords in the Java language
    protected static final Set<String> JAVA_KEYWORDS = new HashSet<String>();
    static {
        JAVA_KEYWORDS.add("abstract");
        JAVA_KEYWORDS.add("boolean");
        JAVA_KEYWORDS.add("break");
        JAVA_KEYWORDS.add("byte");
        JAVA_KEYWORDS.add("case");
        JAVA_KEYWORDS.add("cast");
        JAVA_KEYWORDS.add("catch");
        JAVA_KEYWORDS.add("char");
        JAVA_KEYWORDS.add("class");
        JAVA_KEYWORDS.add("const");
        JAVA_KEYWORDS.add("continue");
        JAVA_KEYWORDS.add("default");
        JAVA_KEYWORDS.add("do");
        JAVA_KEYWORDS.add("double");
        JAVA_KEYWORDS.add("else");
        JAVA_KEYWORDS.add("enum");
        JAVA_KEYWORDS.add("extends");
        JAVA_KEYWORDS.add("final");
        JAVA_KEYWORDS.add("finally");
        JAVA_KEYWORDS.add("float");
        JAVA_KEYWORDS.add("for");
        JAVA_KEYWORDS.add("future");
        JAVA_KEYWORDS.add("generic");
        JAVA_KEYWORDS.add("goto");
        JAVA_KEYWORDS.add("if");
        JAVA_KEYWORDS.add("implements");
        JAVA_KEYWORDS.add("import");
        JAVA_KEYWORDS.add("inner");
        JAVA_KEYWORDS.add("instanceof");
        JAVA_KEYWORDS.add("int");
        JAVA_KEYWORDS.add("interface");
        JAVA_KEYWORDS.add("long");
        JAVA_KEYWORDS.add("native");
        JAVA_KEYWORDS.add("new");
        JAVA_KEYWORDS.add("null");
        JAVA_KEYWORDS.add("operator");
        JAVA_KEYWORDS.add("outer");
        JAVA_KEYWORDS.add("package");
        JAVA_KEYWORDS.add("private");
        JAVA_KEYWORDS.add("protected");
        JAVA_KEYWORDS.add("public");
        JAVA_KEYWORDS.add("rest");
        JAVA_KEYWORDS.add("return");
        JAVA_KEYWORDS.add("short");
        JAVA_KEYWORDS.add("static");
        JAVA_KEYWORDS.add("strictfp");
        JAVA_KEYWORDS.add("super");
        JAVA_KEYWORDS.add("switch");
        JAVA_KEYWORDS.add("synchronized");
        JAVA_KEYWORDS.add("this");
        JAVA_KEYWORDS.add("throw");
        JAVA_KEYWORDS.add("throws");
        JAVA_KEYWORDS.add("transient");
        JAVA_KEYWORDS.add("try");
        JAVA_KEYWORDS.add("var");
        JAVA_KEYWORDS.add("void");
        JAVA_KEYWORDS.add("volatile");
        JAVA_KEYWORDS.add("while");
    }


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Return the capitalized version of the specified property name.</p>
     *
     * @param name Uncapitalized property name
     * @return the capitalized version
     */
    protected static String capitalize(String name) {

        return (Character.toUpperCase(name.charAt(0)) + name.substring(1));

    }


    /**
     * <p>Return a mangled version of the specified name if it conflicts with
     * a Java keyword; otherwise, return the specified name unchanged.</p>
     *
     * @param name Name to be potentially mangled
     *
     * @return a mangled version
     */
    protected static String mangle(String name) {

        if (JAVA_KEYWORDS.contains(name)) {
            return ('_' + name);
        } else {
            return (name);
        }

    }


    /**
     * <p>Parse the command line options into a <code>Map</code>.</p>
     *
     * @param args Command line arguments passed to this program
     * @return a <code>Map</code>
     *
     * @exception IllegalArgumentException if an option flag does not start
     *  with a '-' or is missing a corresponding value
     */
    protected static Map<String,String> options(String[] args) {

        Map<String,String> options = new HashMap<String, String>();
        int i = 0;
        while (i < args.length) {
            if (!args[i].startsWith("-")) {
                throw new IllegalArgumentException
                    ("Invalid option name '" + args[i] + '\'');
            } else if ((i + 1) >= args.length) {
                throw new IllegalArgumentException
                    ("Missing value for option '" + args[i] + '\'');
            }
            options.put(args[i], args[i+1]);
            i += 2;
        }
        return (options);

    }


    /**
     * <p>Return <code>true</code> if the specified type is a primitive.</p>
     *
     * @param type Type to be tested
     * @return true if the specified type is a primitive.
     */
    protected static boolean primitive(String type) {
        return ((GeneratorUtil.convertToPrimitive(type) != null));
    }


    /**
     * <p>Return the short class name from the specified (potentially fully
     * qualified) class name.  If the specified name has no periods, the
     * input value is returned unchanged.</p>
     *
     * @param className Class name that is optionally fully qualified
     * @return the short class name
     */
    protected static String shortName(String className) {
        int index = className.lastIndexOf('.');
        if (index >= 0) {
            return (className.substring(index + 1));
        } else {
            return (className);
        }
    }

}
