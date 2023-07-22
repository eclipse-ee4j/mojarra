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

package com.sun.faces.facelets.util;

import static com.sun.faces.util.Util.unmodifiableSet;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.util.Util;

import jakarta.el.Expression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;

/**
 * Utility class for displaying Facelet error/debug information.
 *
 * <p>
 * The public static methods of this class are exposed as EL functions under the namespace
 * <code>mojarra.private.functions</code>
 * </p>
 *
 */
public final class DevTools {

    private final static String SunNamespace = "http://java.sun.com/mojarra/private/functions";
    private final static String JcpNamespace = "http://xmlns.jcp.org/mojarra/private/functions";
    private final static String JakartaNamespace = "mojarra.private.functions";

    public final static Set<String> NAMESPACES = unmodifiableSet(JakartaNamespace, JcpNamespace, SunNamespace);
    public final static String DEFAULT_NAMESPACE = JakartaNamespace;

    private static final Logger LOGGER = Logger.getLogger(DevTools.class.getPackage().getName());

    private final static String TS = "&lt;";

    private static final String ERROR_TEMPLATE = "META-INF/facelet-dev-error.xml";

    private static String[] ERROR_PARTS;

    private static final String DEBUG_TEMPLATE = "META-INF/facelet-dev-debug.xml";

    private static String[] DEBUG_PARTS;

    // ------------------------------------------------------------ Constructors

    private DevTools() {
        throw new IllegalStateException();
    }

    // ---------------------------------------------------------- Public Methods

    public static void debugHtml(Writer writer, FacesContext faces, Throwable e) throws IOException {

        init();
        Date now = new Date();
        for (String ERROR_PART : ERROR_PARTS) {
            if (null != ERROR_PART) {
                switch (ERROR_PART) {
                case "message":
                    writeMessage(writer, e);
                    break;
                case "trace":
                    writeException(writer, e);
                    break;
                case "now":
                    writer.write(DateFormat.getDateTimeInstance().format(now));
                    break;
                case "tree":
                    writeComponent(writer, faces.getViewRoot());
                    break;
                case "vars":
                    writeVariables(writer, faces);
                    break;
                default:
                    writer.write(ERROR_PART);
                    break;
                }
            }
        }

    }

    public static void writeMessage(Writer writer, Throwable e) throws IOException {

        if (e != null) {
            String msg = e.getMessage();
            if (msg != null) {
                writer.write(msg.replace("<", TS));
            } else {
                writer.write(e.getClass().getName());
            }
        }

    }

    public static void writeException(Writer writer, Throwable e) throws IOException {

        if (e != null) {
            StringWriter str = new StringWriter(256);
            PrintWriter pstr = new PrintWriter(str);
            e.printStackTrace(pstr);
            pstr.close();
            writer.write(str.toString().replace("<", TS));
        }

    }

    public static void debugHtml(Writer writer, FacesContext faces) throws IOException {

        // PENDING - this and debugHtml(Writer, FacesContext, Exception) should
        // be refactored to share code.
        init();
        Date now = new Date();
        for (String DEBUG_PART : DEBUG_PARTS) {
            if (null != DEBUG_PART) {
                switch (DEBUG_PART) {
                case "message":
                    writer.write(faces.getViewRoot().getViewId());
                    break;
                case "now":
                    writer.write(DateFormat.getDateTimeInstance().format(now));
                    break;
                case "tree":
                    writeComponent(writer, faces.getViewRoot());
                    break;
                case "vars":
                    writeVariables(writer, faces);
                    break;
                default:
                    writer.write(DEBUG_PART);
                    break;
                }
            }
        }

    }

    public static void writeVariables(Writer writer, FacesContext faces) throws IOException {

        ExternalContext ctx = faces.getExternalContext();
        writeVariables(writer, ctx.getRequestParameterMap(), "Request Parameters");
        if (faces.getViewRoot() != null) {
            Map<String, Object> viewMap = faces.getViewRoot().getViewMap(false);
            if (viewMap != null) {
                writeVariables(writer, viewMap, "View Attributes");
            } else {
                writeVariables(writer, Collections.<String, Object>emptyMap(), "View Attributes");
            }
        } else {
            writeVariables(writer, Collections.<String, Object>emptyMap(), "View Attributes");
        }
        writeVariables(writer, ctx.getRequestMap(), "Request Attributes");
        Flash flash = ctx.getFlash();
        try {
            flash = ctx.getFlash();
        } catch (UnsupportedOperationException uoe) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Flash not supported", uoe);
            }
        }
        if (flash != null) {
            writeVariables(writer, flash, "Flash Attributes");
        } else {
            writeVariables(writer, Collections.<String, Object>emptyMap(), "Flash Attributes");
        }
        if (ctx.getSession(false) != null) {
            writeVariables(writer, ctx.getSessionMap(), "Session Attributes");
        } else {
            writeVariables(writer, Collections.<String, Object>emptyMap(), "Session Attributes");
        }
        writeVariables(writer, ctx.getApplicationMap(), "Application Attributes");

    }

    public static void writeComponent(Writer writer, UIComponent c) throws IOException {

        writer.write(
                "<dl style=\"color: #006;\"><dt style=\"border: 1px solid #DDD; padding: 4px; border-left: 2px solid #666; font-family: 'Courier New', Courier, mono; font-size: small;");
        if (c != null) {
            if (isText(c)) {
                writer.write("color: #999;");
            }
        }
        writer.write("\">");
        if (c == null) {
            return;
        }

        boolean hasChildren = c.getChildCount() > 0 || c.getFacets().size() > 0;

        writeStart(writer, c, hasChildren);
        writer.write("</dt>");
        if (hasChildren) {
            if (c.getFacets().size() > 0) {
                for (Map.Entry entry : c.getFacets().entrySet()) {
                    writer.write("<dd style=\"margin-top: 2px; margin-bottom: 2px;\">");
                    writer.write("<span style=\"font-family: 'Trebuchet MS', Verdana, Arial, Sans-Serif; font-size: small;\">");
                    writer.write((String) entry.getKey());
                    writer.write("</span>");
                    writeComponent(writer, (UIComponent) entry.getValue());
                    writer.write("</dd>");
                }
            }
            if (c.getChildCount() > 0) {
                for (UIComponent child : c.getChildren()) {
                    writer.write("<dd style=\"margin-top: 2px; margin-bottom: 2px;\">");
                    writeComponent(writer, child);
                    writer.write("</dd>");
                }
            }
            writer.write(
                    "<dt style=\"border: 1px solid #DDD; padding: 4px; border-left: 2px solid #666; font-family: 'Courier New', Courier, mono; font-size: small;\">");
            writeEnd(writer, c);
            writer.write("</dt>");
        }
        writer.write("</dl>");

    }

    // --------------------------------------------------------- Private Methods

    private static void init() throws IOException {

        if (ERROR_PARTS == null) {
            ERROR_PARTS = splitTemplate(ERROR_TEMPLATE);
        }

        if (DEBUG_PARTS == null) {
            DEBUG_PARTS = splitTemplate(DEBUG_TEMPLATE);
        }

    }

    private static String[] splitTemplate(String rsc) throws IOException {

        ClassLoader loader = Util.getCurrentLoader(DevTools.class);
        String str = "";
        InputStream is = null;
        try {
            is = loader.getResourceAsStream(rsc);
            if (is == null) {
                loader = DevTools.class.getClassLoader();
                is = loader.getResourceAsStream(rsc);
                if (is == null) {
                    throw new FileNotFoundException(rsc);
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[512];
            int read;
            while ((read = is.read(buff)) != -1) {
                baos.write(buff, 0, read);
            }
            str = baos.toString(RIConstants.CHAR_ENCODING);
        } finally {
            if (null != is) {
                is.close();
            }
        }
        return str.split("@@");

    }

    private static void writeVariables(Writer writer, Map<String, ?> vars, String caption) throws IOException {

        writer.write(
                "<table style=\"border: 1px solid #CCC; border-collapse: collapse; border-spacing: 0px; width: 100%; text-align: left;\"><caption style=\"text-align: left; padding: 10px 0; font-size: large;\">");
        writer.write(caption);
        writer.write(
                "</caption><thead stype=\"padding: 2px; color: #030; background-color: #F9F9F9;\"><tr style=\"padding: 2px; color: #030; background-color: #F9F9F9;\"><th style=\"padding: 2px; color: #030; background-color: #F9F9F9;width: 10%; \">Name</th><th style=\"padding: 2px; color: #030; background-color: #F9F9F9;width: 90%; \">Value</th></tr></thead><tbody style=\"padding: 10px 6px;\">");
        boolean written = false;
        if (!vars.isEmpty()) {
            SortedMap<String, Object> map = new TreeMap<>(vars);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                if (key.indexOf('.') == -1) {
                    writer.write("<tr style=\"padding: 10px 6px;\"><td style=\"padding: 10px 6px;\">");
                    writer.write(key.replace("<", TS));
                    writer.write("</td><td>");
                    writer.write(entry.getValue() == null ? "null" : entry.getValue().toString().replace("<", TS));
                    writer.write("</td></tr>");
                    written = true;
                }
            }
        }
        if (!written) {
            writer.write("<tr style=\"padding: 10px 6px;\"><td colspan=\"2\" style=\"padding: 10px 6px;\"><em>None</em></td></tr>");
        }
        writer.write("</tbody></table>");

    }

    private static void writeEnd(Writer writer, UIComponent c) throws IOException {

        if (!isText(c)) {
            writer.write(TS);
            writer.write('/');
            writer.write(getName(c));
            writer.write('>');
        }

    }

    private final static String[] IGNORE = new String[] { "parent", "rendererType" };

    private static void writeAttributes(Writer writer, UIComponent c) {

        try {
            BeanInfo info = Introspector.getBeanInfo(c.getClass());
            PropertyDescriptor[] pd = info.getPropertyDescriptors();
            for (PropertyDescriptor aPd : pd) {
                if (aPd.getWriteMethod() != null && Arrays.binarySearch(IGNORE, aPd.getName()) < 0) {
                    Method m = aPd.getReadMethod();
                    try {
                        Object v = m.invoke(c);
                        if (v != null) {
                            if (v instanceof Collection || v instanceof Map || v instanceof Iterator) {
                                continue;
                            }
                            writer.write(" ");
                            writer.write(aPd.getName());
                            writer.write("=\"");
                            String str;
                            if (v instanceof Expression) {
                                str = ((Expression) v).getExpressionString();
                            } else {
                                str = v.toString();
                            }
                            writer.write(str.replace("<", TS));
                            writer.write("\"");
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            LOGGER.log(Level.FINEST, "Error writing out attribute", e);
                        }
                    } catch (Exception e) {
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            LOGGER.log(Level.FINEST, "Error writing out attribute", e);
                        }
                    }
                }
            }

        } catch (IntrospectionException e) {
            LOGGER.log(Level.FINEST, e, () -> "Error writing out attributes");
        }

    }

    private static void writeStart(Writer writer, UIComponent c, boolean children) throws IOException {
        if (isText(c)) {
            String str = c.toString().trim();
            writer.write(str.replace("<", TS));
        } else {
            writer.write(TS);
            writer.write(getName(c));
            writeAttributes(writer, c);
            if (children) {
                writer.write('>');
            } else {
                writer.write("/>");
            }
        }

    }

    private static String getName(UIComponent component) {
        String componentName = component.getClass().getName();
        return componentName.substring(componentName.lastIndexOf('.') + 1);

    }

    private static boolean isText(UIComponent c) {
        return c.getClass().getName().startsWith("com.sun.faces.facelets.compiler");
    }

}
