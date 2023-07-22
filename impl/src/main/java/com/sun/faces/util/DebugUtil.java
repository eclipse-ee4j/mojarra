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

package com.sun.faces.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.io.FastStringWriter;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;

/**
 * <B>DebugUtil</B> is a class ...
 * 
 * <B>Lifetime And Scope</B>
 */
public class DebugUtil {

    private static final Logger LOGGER = Logger.getLogger(DebugUtil.class.getPackage().getName());

    private static boolean keepWaiting = true;

    private static int curDepth = 0;

    public DebugUtil() {
        super();
        init();
    }

    protected void init() {
    }


    public static void setKeepWaiting(boolean keepWaiting) {
        DebugUtil.keepWaiting = keepWaiting;
    }

    /**
     * Usage:
     * <P>
     * 
     * Place a call to this method in the earliest possible entry point of your servlet app. It will cause the app to enter
     * into an infinite loop, sleeping until the static var keepWaiting is set to false. The idea is that you attach your
     * debugger to the servlet, then, set a breakpoint in this method. When it is hit, you use the debugger to set the
     * keepWaiting class var to false.
     */
    public static void waitForDebugger() {
        while (keepWaiting) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("DebugUtil.waitForDebugger(): Exception: " + e.getMessage());
            }
        }
    }

    private static void indentPrintln(Writer out, String str) {
        // handle indentation
        try {
            for (int i = 0; i < curDepth; i++) {
                out.write("  ");
            }
            out.write(str + "\n");
        } catch (IOException ioe) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Unable to write indent", ioe);
            }
        }
    }

    private static void assertSerializability(StringBuilder builder, Object toPrint) {
        DebugObjectOutputStream doos = null;
        try {
            OutputStream base = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(base);
            doos = new DebugObjectOutputStream(oos);
            doos.writeObject(toPrint);
        } catch (IOException ioe) {
            List pathToBadObject = doos.getStack();
            builder.append("Path to non-Serializable Object: \n");
            for (Object cur : pathToBadObject) {
                builder.append(cur.toString()).append("\n");
            }
        }
    }

    private static void indentPrintln(Logger out, Object toPrint) {

        StringBuilder builder = new StringBuilder();
        String str = null == toPrint ? "null" : toPrint.toString();

        // handle indentation
        for (int i = 0; i < curDepth; i++) {
            builder.append("  ");
        }
        builder.append(str + "\n");

        if (!(toPrint instanceof String)) {
            assertSerializability(builder, toPrint);
        }

        out.severe(builder.toString());

    }

    /**
     * @param root the root component
     * @return the output of printTree() as a String. Useful when used with a Logger. For example:
     * logger.log(DebugUtil.printTree(root));
     */
    public static String printTree(UIComponent root) {
        Writer writer = new FastStringWriter(1024);
        printTree(root, writer);
        return writer.toString();
    }

    /**
     * Output of printTree() to a PrintStream. Usage: DebugUtil.printTree(root, System.out);
     *
     * @param root the root component
     * @param out the PrintStream to write to
     */
    public static void printTree(UIComponent root, PrintStream out) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new PrintStream(out, true, RIConstants.CHAR_ENCODING));
            printTree(root, writer);
            writer.flush();

        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void printTree(UIComponent root, Logger logger, Level level) {
        StringWriter sw = new StringWriter();
        printTree(root, sw);
        logger.log(level, sw.toString());
    }

    public static void printTree(UIComponent root, Writer out) {
        if (null == root) {
            return;
        }
        Object value = null;

        /*
         * PENDING indentPrintln(out, "===>Type:" + root.getComponentType());
         */
        indentPrintln(out, "id:" + root.getId());
        indentPrintln(out, "type:" + root.getClass().getName());

        if (root instanceof jakarta.faces.component.UISelectOne) {
            Iterator<SelectItem> items = null;
            try {
                items = RenderKitUtils.getSelectItems(FacesContext.getCurrentInstance(), root);
            } catch (Exception e) {
                // select items couldn't be resolved at this time
                indentPrintln(out, " { SelectItem(s) not resolvable at this point in time }");
            }
            if (items != null) {
                indentPrintln(out, " {");
                while (items.hasNext()) {
                    SelectItem curItem = items.next();
                    indentPrintln(out, "\t value = " + curItem.getValue() + ", label = " + curItem.getLabel() + ", description = " + curItem.getDescription());
                }
                indentPrintln(out, " }");
            }
        } else {
            ValueExpression ve = null;
            if (root instanceof ValueHolder) {
                ve = root.getValueExpression("value");
                try {
                    value = ((ValueHolder) root).getValue();
                } catch (Exception e) {
                    value = "UNAVAILABLE";
                }
            }
            if (ve != null) {
                indentPrintln(out, "expression/value = " + ve.getExpressionString() + " : " + value);
            } else {
                indentPrintln(out, "value = " + value);
            }

            Iterator<String> it = root.getAttributes().keySet().iterator();
            if (it != null) {
                while (it.hasNext()) {
                    String attrName = it.next();
                    ve = root.getValueExpression(attrName);
                    String expr = null;
                    if (ve != null) {
                        expr = ve.getExpressionString();
                    }
                    String val;
                    try {
                        val = root.getAttributes().get(attrName).toString();
                    } catch (Exception e) {
                        val = "UNAVAILABLE";
                    }
                    if (expr != null) {
                        indentPrintln(out, "attr = " + attrName + " : [" + expr + " : " + val + " ]");
                    } else {
                        indentPrintln(out, "attr = " + attrName + " : " + val);
                    }
                }
            }
        }

        curDepth++;
        Iterator<UIComponent> it = root.getChildren().iterator();
        // print all the facets of this component
        for (UIComponent uiComponent : root.getFacets().values()) {
            printTree(uiComponent, out);
        }
        // print all the children of this component
        while (it.hasNext()) {
            printTree(it.next(), out);
        }
        curDepth--;
    }

    public static void simplePrintTree(UIComponent root, String duplicateId, Writer out) {
        if (null == root) {
            return;
        }

        if (duplicateId.equals(root.getClientId())) {
            indentPrintln(out, "+id: " + root.getId() + "  <===============");
        } else {
            indentPrintln(out, "+id: " + root.getId());
        }
        indentPrintln(out, " type: " + root.toString());

        curDepth++;
        // print all the facets of this component
        for (UIComponent uiComponent : root.getFacets().values()) {
            simplePrintTree(uiComponent, duplicateId, out);
        }
        // print all the children of this component
        for (UIComponent uiComponent : root.getChildren()) {
            simplePrintTree(uiComponent, duplicateId, out);
        }
        curDepth--;
    }

    public static void printState(Map state, Logger out) {
        Set<Map.Entry> entrySet = state.entrySet();
        Object key, value;
        String keyIsSerializable, valueIsSerializable;
        for (Map.Entry cur : entrySet) {
            key = cur.getKey();
            value = cur.getValue();
            keyIsSerializable = key instanceof Serializable ? "true" : "+_+_+_+FALSE+_+_+_+_";
            valueIsSerializable = value instanceof Serializable ? "true" : "+_+_+_+FALSE+_+_+_+_";
            out.severe("key: " + key.toString() + " class:" + key.getClass() + " Serializable: " + keyIsSerializable);
            out.severe("value: " + value.toString() + " class:" + value.getClass() + " Serializable: " + valueIsSerializable);
            if (value instanceof Object[]) {
                printTree((Object[]) value, out);
            }
        }

    }

    public static void printTree(Object[] root, Writer out) {

        if (null == root) {
            indentPrintln(out, "null");
            return;
        }

        Object obj;
        for (int i = 0; i < root.length; i++) {
            obj = root[i];
            if (null == obj) {
                indentPrintln(out, "null");
            } else {
                if (obj.getClass().isArray()) {
                    curDepth++;
                    printTree((Object[]) obj, out);
                    curDepth--;
                } else {
                    indentPrintln(out, obj.toString());
                }

            }
        }

    }

    public static void printTree(Object[] root, Logger out) {

        if (null == root) {
            indentPrintln(out, "null");
            return;
        }

        Object obj;
        for (int i = 0; i < root.length; i++) {
            obj = root[i];
            if (null == obj) {
                indentPrintln(out, "null");
            } else {
                if (obj.getClass().isArray()) {
                    curDepth++;
                    printTree((Object[]) obj, out);
                    curDepth--;
                } else if (obj instanceof List) {
                    printList((List) obj, out);
                } else {
                    indentPrintln(out, obj);
                }

            }
        }

    }

    public static void printList(List list, Logger out) {
        for (Object cur : list) {
            if (cur instanceof List) {
                curDepth++;
                printList((List) cur, out);
                curDepth--;
            } else {
                indentPrintln(out, cur);
            }
        }
    }

}
