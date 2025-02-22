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

package com.sun.faces.context;

import static com.sun.faces.util.ReflectionUtils.invokeMethod;
import static com.sun.faces.util.Util.isOneOf;
import static jakarta.faces.annotation.FacesConfig.ContextParam.ADDITIONAL_HTML_EVENT_NAMES;
import static jakarta.faces.annotation.FacesConfig.ContextParam.CONFIG_FILES;
import static jakarta.faces.annotation.FacesConfig.ContextParam.FACELETS_DECORATORS;
import static jakarta.faces.annotation.FacesConfig.ContextParam.FACELETS_LIBRARIES;
import static jakarta.faces.annotation.FacesConfig.ContextParam.FACELETS_VIEW_MAPPINGS;
import static jakarta.faces.annotation.FacesConfig.ContextParam.RESOURCE_EXCLUDES;
import static java.lang.Character.toUpperCase;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jakarta.faces.annotation.FacesConfig;
import jakarta.faces.annotation.FacesConfig.ContextParam;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import com.sun.faces.cdi.CdiUtils;
import com.sun.faces.util.FacesLogger;

/**
 * A utility class for dealing with context-param entries.
 */
public class ContextParamUtils {

    private static final Logger LOGGER = FacesLogger.CONTEXT.getLogger();

    private enum StringArray {
        SPACE_SEPARATED("\\s+"),
        SEMICOLON_SEPARATED("\\s*;\\s*"),
        COMMA_SEPARATED("\\s*,\\s*");

        private Pattern pattern;

        private StringArray(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        public String[] split(String value) {
            return pattern.split(value);
        }
    }

    /**
     * Constructor.
     */
    private ContextParamUtils() {
        // nothing to do here.
    }

    /**
     * Get the value.
     *
     * @param servletContext the servlet context.
     * @param contextParam the context-param.
     * @return the value.
     */
    public static Object getValue(ServletContext servletContext, com.sun.faces.context.ContextParam contextParam) {
        Object result = contextParam.getDefaultValue();
        if (servletContext.getInitParameter(contextParam.getName()) != null) {
            if (contextParam.getType().equals(Boolean.class)) {
                result = Boolean.valueOf(servletContext.getInitParameter(contextParam.getName()));
            } else if (contextParam.getType().equals(Integer.class)) {
                result = Integer.valueOf(servletContext.getInitParameter(contextParam.getName()));
            }
        }
        return result;
    }

    /**
     * Get the value.
     *
     * @param <T> the type.
     * @param servletContext the servlet context.
     * @param contextParam the context-param.
     * @param clazz the class.
     * @return the value.
     */
    public static <T extends Object> T getValue(ServletContext servletContext, com.sun.faces.context.ContextParam contextParam, Class<T> clazz) {
        return clazz.cast(getValue(servletContext, contextParam));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(ExternalContext externalContext, AtomicReference<Optional<FacesConfig>> annotatedConfig, ContextParam contextParam) {
        return (T) getInitParameterValue(externalContext, contextParam)
                .orElseGet(() -> getAnnotatedValue(annotatedConfig, contextParam)
                        .orElseGet(() -> contextParam.getDefaultValue(FacesContext.getCurrentInstance())));
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> getInitParameterValue(ExternalContext externalContext, ContextParam contextParam) {
        String name = contextParam.getName();
        Class<?> type = contextParam.getType();
        String value = externalContext.getInitParameter(name);

        if (value == null) {
            return Optional.empty();
        }
        else if (type == String.class) {
            return Optional.of((T) value);
        }
        else if (type == String[].class) {
            return Optional.of((T) splitContextParamValue(contextParam, value));
        }
        else if (type == Character.class) {
            if (value.length() == 1) {
                return Optional.of((T) Character.valueOf(value.charAt(0)));
            }
        }
        else if (type == Boolean.class) {
            return Optional.of((T) Boolean.valueOf(value));
        }
        else if (type == Integer.class) {
            try {
                return Optional.of((T) Integer.valueOf(value));
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException(name + ": invalid value: " + value, e);
            }
        }
        else if (type.isEnum()) {
            for (Object constant : type.getEnumConstants()) {
                if (constant.toString().equalsIgnoreCase(value)) {
                    return Optional.of((T) constant);
                }
            }
        }

        throw new IllegalArgumentException(name + ": invalid value: " + value);
    }

    private static String[] splitContextParamValue(ContextParam contextParam, String value) {
        if (contextParam == CONFIG_FILES) {
            return StringArray.COMMA_SEPARATED.split(value);
        } else if (isOneOf(contextParam, ADDITIONAL_HTML_EVENT_NAMES, RESOURCE_EXCLUDES)) {
            return StringArray.SPACE_SEPARATED.split(value);
        } else if (isOneOf(contextParam, FACELETS_DECORATORS, FACELETS_LIBRARIES, FACELETS_VIEW_MAPPINGS )) {
            return StringArray.SEMICOLON_SEPARATED.split(value);
        } else {
            throw new IllegalStateException(contextParam.getName());
        }
    }
    
    /**
     * We're using getBeanReferencesByQualifier() because CDI.current().select(FacesConfig.Literal.INSTANCE) would otherwise throw ambiguous
     * resolution when there are multiple beans with {@code @FacesConfig}. We'd like to order them by the optional {@code Priority} annotation.
     */
    @SuppressWarnings("unchecked")
    private static <T> Optional<T> getAnnotatedValue(AtomicReference<Optional<FacesConfig>> annotatedConfig, ContextParam contextParam) {
        Optional<FacesConfig> facesConfig = annotatedConfig.updateAndGet(config -> config != null ? config : CdiUtils
                .getBeanReferencesByQualifier(FacesConfig.Literal.INSTANCE).stream()
                .sorted(CdiUtils.BEAN_PRIORITY_COMPARATOR)
                .map(bean -> bean.getClass().getAnnotation(FacesConfig.class))
                .findFirst());
        return facesConfig.map(config -> (T) getAnnotatedValue(config, contextParam)).filter(value -> !shouldDelegateToDefaultValueSupplier(contextParam, value));
    }

    private static <T> T getAnnotatedValue(FacesConfig facesConfig, ContextParam contextParam) {
        return invokeMethod(facesConfig, convertUpperCasedSnakeCaseToCamelCase(contextParam.name()));
    }

    private static String convertUpperCasedSnakeCaseToCamelCase(String string) {
        return string.toLowerCase().codePoints().collect(StringBuilder::new, (sb, cp) -> {
            if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '_') {
                sb.deleteCharAt(sb.length() - 1);
                sb.appendCodePoint(toUpperCase(cp));
            }
            else {
                sb.appendCodePoint(cp);
            }
        }, (sb1, sb2) -> {}).toString();
    }

    private static <T> boolean shouldDelegateToDefaultValueSupplier(ContextParam contextParam, T value) {
        return contextParam == ContextParam.FACELETS_REFRESH_PERIOD && Objects.equals(value, Integer.MIN_VALUE);
    }

}
