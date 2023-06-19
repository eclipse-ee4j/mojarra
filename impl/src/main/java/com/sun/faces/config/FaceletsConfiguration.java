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

package com.sun.faces.config;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.faces.context.FacesContext;

/*
 * This read-only singleton class is vended by the WebConfiguration.
 * It is queried from any point in the program that needs to take action based
 * on configuration options pertaining to facelets.
 *
 */
public class FaceletsConfiguration {

    public static final String FACELETS_CONFIGURATION_ATTRIBUTE_NAME = "com.sun.faces.config.FaceletsConfiguration";

    private static final String ESCAPE_INLINE_TEXT_ATTRIBUTE_NAME = "com.sun.faces.config.EscapeInlineText";

//    private static final String CONSUME_COMMENTS_ATTRIBUTE_NAME = "com.sun.faces.config.ConsumeComments";

    private static final Pattern EXTENSION_PATTERN = Pattern.compile("\\.[^/]+$");

    private final WebConfiguration config;

    private final Map<String, String> faceletsProcessingMappings;

    public FaceletsConfiguration(WebConfiguration config) {
        this.config = config;

        faceletsProcessingMappings = config.getFacesConfigOptionValue(WebConfiguration.WebContextInitParameter.FaceletsProcessingFileExtensionProcessAs);

    }

    public boolean isProcessCurrentDocumentAsFaceletsXhtml(String alias) {
        // We want to write the XML declaration if and only if
        // The SuppressXmlDeclaration context-param is NOT enabled
        // and the file extension for the current file has a mapping
        // with the value of XHTML
        boolean currentModeIsXhtml = true;

        String extension = getExtension(alias);

        assert null != faceletsProcessingMappings;
        if (faceletsProcessingMappings.containsKey(extension)) {
            String value = faceletsProcessingMappings.get(extension);
            currentModeIsXhtml = value.equals("xhtml");
        }

        return currentModeIsXhtml;
    }

    public boolean isOutputHtml5Doctype(String alias) {
        boolean currentModeIsHtml5 = true;

        String extension = getExtension(alias);

        assert null != faceletsProcessingMappings;
        if (faceletsProcessingMappings.containsKey(extension)) {
            String value = faceletsProcessingMappings.get(extension);
            currentModeIsHtml5 = value.equals("html5");
        }

        return currentModeIsHtml5;
    }

    public boolean isConsumeComments(String alias) {
        boolean consumeComments = false;

        String extension = getExtension(alias);

        assert null != faceletsProcessingMappings;
        if (faceletsProcessingMappings.containsKey(extension)) {
            String value = faceletsProcessingMappings.get(extension);
            consumeComments = value.equals("xml") || value.equals("jspx");
        }

        return consumeComments;

    }

    public boolean isConsumeCDATA(String alias) {
        boolean consumeCDATA = false;

        String extension = getExtension(alias);

        assert null != faceletsProcessingMappings;
        if (faceletsProcessingMappings.containsKey(extension)) {
            String value = faceletsProcessingMappings.get(extension);
            consumeCDATA = value.equals("jspx") || value.equals("xml");
        }

        return consumeCDATA;

    }

    public boolean isEscapeInlineText(FacesContext context) {
        Boolean result = Boolean.TRUE;

        result = (Boolean) context.getAttributes().get(ESCAPE_INLINE_TEXT_ATTRIBUTE_NAME);
        if (null == result) {

            String extension = getExtension(context.getViewRoot().getViewId());

            assert null != faceletsProcessingMappings;
            if (faceletsProcessingMappings.containsKey(extension)) {
                String value = faceletsProcessingMappings.get(extension);
                result = value.equals("xml") || value.equals("xhtml");
            } else {
                result = Boolean.TRUE;
            }
            context.getAttributes().put(ESCAPE_INLINE_TEXT_ATTRIBUTE_NAME, result);
        }

        return result;
    }

    public static FaceletsConfiguration getInstance(FacesContext context) {
        FaceletsConfiguration result = null;
        Map<Object, Object> attrs = context.getAttributes();
        result = (FaceletsConfiguration) attrs.get(FaceletsConfiguration.FACELETS_CONFIGURATION_ATTRIBUTE_NAME);
        if (null == result) {
            WebConfiguration config = WebConfiguration.getInstance(context.getExternalContext());
            result = config.getFaceletsConfiguration();
            attrs.put(FaceletsConfiguration.FACELETS_CONFIGURATION_ATTRIBUTE_NAME, result);
        }
        return result;
    }

    public static FaceletsConfiguration getInstance() {
        FacesContext context = FacesContext.getCurrentInstance();
        return FaceletsConfiguration.getInstance(context);
    }

    private static String getExtension(String alias) {
        String ext = null;

        if (alias != null) {
            Matcher matcher = EXTENSION_PATTERN.matcher(alias);
            if (matcher.find()) {
                ext = alias.substring(matcher.start(), matcher.end());
            }
        }

        return ext == null ? "xhtml" : ext;
    }

}
