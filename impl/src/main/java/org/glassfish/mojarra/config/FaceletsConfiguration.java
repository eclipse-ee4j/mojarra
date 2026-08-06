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

package org.glassfish.mojarra.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

/**
 * <p>
 * How a facelet is processed, per file extension, as declared by the <code>facelets-processing</code> elements of
 * <code>faces-config.xml</code>. This is configuration which is not expressible as a context parameter, which is why
 * it is held here rather than by {@link WebConfiguration}.
 * </p>
 *
 * <p>
 * The mappings are registered while the configuration is read and only queried afterwards, so a facelet compiled at any
 * later point sees all of them.
 * </p>
 */
public class FaceletsConfiguration {

    private static final String FACELETS_CONFIGURATION_ATTRIBUTE_NAME = "org.glassfish.mojarra.config.FaceletsConfiguration";

    private static final String ESCAPE_INLINE_TEXT_ATTRIBUTE_NAME = "org.glassfish.mojarra.config.EscapeInlineText";

    private static final Pattern EXTENSION_PATTERN = Pattern.compile("\\.[^/]+$");

    private static final String DEFAULT_EXTENSION = "xhtml";

    private final Map<String, String> processingMappings = new ConcurrentHashMap<>(3);

    /**
     * @param servletContext the involved servlet context.
     * @return the instance for this application.
     */
    public static FaceletsConfiguration getInstance(ServletContext servletContext) {
        FaceletsConfiguration faceletsConfig = (FaceletsConfiguration) servletContext.getAttribute(FACELETS_CONFIGURATION_ATTRIBUTE_NAME);

        if (faceletsConfig == null) {
            faceletsConfig = new FaceletsConfiguration();
            servletContext.setAttribute(FACELETS_CONFIGURATION_ATTRIBUTE_NAME, faceletsConfig);
        }

        return faceletsConfig;
    }

    /**
     * @param context the involved faces context.
     * @return the instance for this application, remembered on the context, because a facelet is queried about while it
     * renders and every one of those queries would otherwise walk out to the servlet context.
     */
    public static FaceletsConfiguration getInstance(FacesContext context) {
        Map<Object, Object> attributes = context.getAttributes();
        FaceletsConfiguration faceletsConfig = (FaceletsConfiguration) attributes.get(FACELETS_CONFIGURATION_ATTRIBUTE_NAME);

        if (faceletsConfig == null) {
            faceletsConfig = getInstance((ServletContext) context.getExternalContext().getContext());
            attributes.put(FACELETS_CONFIGURATION_ATTRIBUTE_NAME, faceletsConfig);
        }

        return faceletsConfig;
    }

    /**
     * Registers how a file extension is processed, as declared by a <code>facelets-processing</code> element.
     *
     * @param fileExtension the file extension, including the leading dot.
     * @param processAs what a facelet carrying that extension is processed as.
     */
    public void addProcessingMapping(String fileExtension, String processAs) {
        processingMappings.put(fileExtension, processAs);
    }

    public boolean isProcessCurrentDocumentAsFaceletsXhtml(String alias) {
        return isProcessedAs(alias, true, "xhtml");
    }

    public boolean isOutputHtml5Doctype(String alias) {
        return isProcessedAs(alias, true, "html5");
    }

    public boolean isConsumeComments(String alias) {
        return isProcessedAs(alias, false, "xml", "jspx");
    }

    public boolean isConsumeCDATA(String alias) {
        return isProcessedAs(alias, false, "xml", "jspx");
    }

    public boolean isEscapeInlineText(FacesContext context) {
        Boolean escapeInlineText = (Boolean) context.getAttributes().get(ESCAPE_INLINE_TEXT_ATTRIBUTE_NAME);

        if (escapeInlineText == null) {
            escapeInlineText = isProcessedAs(context.getViewRoot().getViewId(), true, "xml", "xhtml");
            context.getAttributes().put(ESCAPE_INLINE_TEXT_ATTRIBUTE_NAME, escapeInlineText);
        }

        return escapeInlineText;
    }

    /**
     * @param alias the facelet whose file extension decides this.
     * @param whenUnmapped what applies when its extension carries no declaration at all.
     * @param processAs the declarations which answer to this question.
     * @return whether the facelet is processed as any of the given ones.
     */
    private boolean isProcessedAs(String alias, boolean whenUnmapped, String... processAs) {
        String value = processingMappings.get(getExtension(alias));

        if (value == null) {
            return whenUnmapped;
        }

        for (String candidate : processAs) {
            if (value.equals(candidate)) {
                return true;
            }
        }

        return false;
    }

    private static String getExtension(String alias) {
        if (alias != null) {
            Matcher matcher = EXTENSION_PATTERN.matcher(alias);

            if (matcher.find()) {
                return alias.substring(matcher.start(), matcher.end());
            }
        }

        return DEFAULT_EXTENSION;
    }

}
