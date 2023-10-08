/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package jakarta.faces.annotation;

import static com.sun.faces.RIConstants.EMPTY_STRING_ARRAY;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.push.PushContext;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.webapp.FacesServlet;
import jakarta.inject.Qualifier;

/**
 * <p class="changed_added_2_3">
 * The presence of this annotation on <span class="changed_modified_4_0">a class</span> deployed within an application 
 * <span class="changed_modified_4_0">guarantees activation of Jakarta Faces and its CDI specific features, even when 
 * {@code /WEB-INF/faces-config.xml} is absent and {@code FacesServlet} is not explicitly registered</span>.
 * </p>
 */
@Qualifier
@Target(TYPE)
@Retention(RUNTIME)
public @interface FacesConfig {

    /**
     * <p class="changed_added_4_0">
     * Supports inline instantiation of the {@link FacesConfig} qualifier.
     * </p>
     *
     * @since 4.0
     */
    public static final class Literal extends AnnotationLiteral<FacesConfig> implements FacesConfig {
        private static final long serialVersionUID = 1L;

        /**
         * Instance of the {@link FacesConfig} qualifier.
         */
        public static final Literal INSTANCE = new Literal();

        @Override
        public boolean alwaysPerformValidationWhenRequiredIsTrue() {
            return false;
        }

        @Override
        public boolean automaticExtensionlessMapping() {
            return false;
        }

        @Override
        public String clientWindowMode() {
            return ClientWindow.CLIENT_WINDOW_MODE_DEFAULT_VALUE;
        }

        @Override
        public String[] configFiles() {
            return EMPTY_STRING_ARRAY;
        }

        @Override
        public boolean datetimeConverterDefaultTimezoneIsSystemTimezone() {
            return false;
        }

        @Override
        public boolean disableDefaultBeanValidator() {
            return false;
        }

        @Override
        public boolean disableFacesservletToXhtml() {
            return false;
        }

        @Override
        public boolean enableValidateWholeBean() {
            return false;
        }

        @Override
        public boolean enableWebsocketEndpoint() {
            return false;
        }

        @Override
        public int faceletsBufferSize() {
            return ViewHandler.FACELETS_BUFFER_SIZE_DEFAULT_VALUE;
        }

        @Override
        public String[] faceletsDecorators() {
            return EMPTY_STRING_ARRAY;
        }

        @Override
        public String[] faceletsLibraries() {
            return EMPTY_STRING_ARRAY;
        }

        @Override
        public int faceletsRefreshPeriod() {
            return Integer.MIN_VALUE;
        }

        @Override
        public boolean faceletsSkipComments() {
            return false;
        }

        @Override
        public String faceletsSuffix() {
            return ViewHandler.DEFAULT_FACELETS_SUFFIX;
        }

        @Override
        public String[] faceletsViewMappings() {
            return EMPTY_STRING_ARRAY;
        }

        @Override
        public String[] fullStateSavingViewIds() {
            return EMPTY_STRING_ARRAY;
        }

        @Override
        public boolean interpretEmptyStringSubmittedValuesAsNull() {
            return false;
        }

        @Override
        public int numberOfClientWindows() {
            return ClientWindow.NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE;
        }
        
        @Override
        public boolean partialStateSaving() {
            return true;
        }

        @Override
        public ProjectStage projectStage() {
            return ProjectStage.Production;
        }

        @Override
        public String[] resourceExcludes() {
            return ContextParam.StringArray.SPACE_SEPARATED.split(ResourceHandler.RESOURCE_EXCLUDES_DEFAULT_VALUE);
        }

        @Override
        public boolean serializeServerState() {
            return false;
        }

        @Override
        public char separatorChar() {
            return NamingContainer.SEPARATOR_CHAR;
        }

        @Override
        public String stateSavingMethod() {
            return StateManager.STATE_SAVING_METHOD_CLIENT;
        }

        @Override
        public String validateEmptyFields() {
            return UIInput.VALIDATE_EMPTY_FIELDS_DEFAULT_VALUE;
        }

        @Override
        public boolean viewrootPhaseListenerQueuesExceptions() {
            return false;
        }

        @Override
        public String webappContractsDirectory() {
            return ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE;
        }

        @Override
        public String webappResourcesDirectory() {
            return ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE;
        }

        @Override
        public int websocketEndpointPort() {
            return 0;
        }
    }

    /**
     * <p class="changed_added_5_0">
     * Enumeration of all available {@code jakarta.faces.*} context parameters.
     * </p>
     * 
     * @since 5.0
     */
    public static enum ContextParam {

        /**
         * Returns {@value UIInput#ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE} as {@link Boolean} with default of {@code false}.
         */
        ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE(UIInput.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE, Boolean.class, false),

        /**
         * Returns {@value FacesServlet#AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        AUTOMATIC_EXTENSIONLESS_MAPPING(FacesServlet.AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@value ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME} as {@link String} with default of {@code none}.
         */
        CLIENT_WINDOW_MODE(ClientWindow.CLIENT_WINDOW_MODE_PARAM_NAME, String.class, ClientWindow.CLIENT_WINDOW_MODE_DEFAULT_VALUE),

        /**
         * Returns {@value FacesServlet#CONFIG_FILES_ATTR} as {@link String} array with default of empty string array.
         */
        CONFIG_FILES(FacesServlet.CONFIG_FILES_ATTR, StringArray.COMMA_SEPARATED, EMPTY_STRING_ARRAY),

        /**
         * Returns {@value Converter#DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE(Converter.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@value BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        DISABLE_DEFAULT_BEAN_VALIDATOR(BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@value FacesServlet#DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        DISABLE_FACESSERVLET_TO_XHTML(FacesServlet.DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns {@value BeanValidator#ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        ENABLE_VALIDATE_WHOLE_BEAN(BeanValidator.ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns {@value PushContext#ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        ENABLE_WEBSOCKET_ENDPOINT(PushContext.ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns {@value ViewHandler#FACELETS_BUFFER_SIZE_PARAM_NAME} as {@link Integer} with default of {@value ViewHandler#FACELETS_BUFFER_SIZE_DEFAULT_VALUE}.
         */
        FACELETS_BUFFER_SIZE(ViewHandler.FACELETS_BUFFER_SIZE_PARAM_NAME, Integer.class, ViewHandler.FACELETS_BUFFER_SIZE_DEFAULT_VALUE),

        /** 
         * Returns {@value ViewHandler#FACELETS_DECORATORS_PARAM_NAME} as {@link String} array with default of empty string array.
         */
        FACELETS_DECORATORS(ViewHandler.FACELETS_DECORATORS_PARAM_NAME, StringArray.SEMICOLON_SEPARATED, EMPTY_STRING_ARRAY),

        /** 
         * Returns {@value ViewHandler#FACELETS_LIBRARIES_PARAM_NAME} as {@link String} array with default of empty string array.
         */
        FACELETS_LIBRARIES(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, StringArray.SEMICOLON_SEPARATED, EMPTY_STRING_ARRAY),

        /**
         * Returns {@value ViewHandler#FACELETS_REFRESH_PERIOD_PARAM_NAME} as {@link Integer} with default of {@code -1} when
         * {@link Application#getProjectStage()} is {@link ProjectStage#Production} else default of {@code 0}.
         */
        FACELETS_REFRESH_PERIOD(ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME, Integer.class, (Function<FacesContext, Integer>) context -> context.getApplication().getProjectStage() == ProjectStage.Production ? -1 : 0),

        /**
         * Returns {@value ViewHandler#FACELETS_SKIP_COMMENTS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        FACELETS_SKIP_COMMENTS(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns {@value ViewHandler#FACELETS_SUFFIX_PARAM_NAME} as {@link String} with default of {@value ViewHandler#DEFAULT_FACELETS_SUFFIX}.
         */
        FACELETS_SUFFIX(ViewHandler.FACELETS_SUFFIX_PARAM_NAME, String.class, ViewHandler.DEFAULT_FACELETS_SUFFIX),

        /**
         * Returns {@value ViewHandler#FACELETS_VIEW_MAPPINGS_PARAM_NAME} as {@link String} array with default of empty string array.
         */
        FACELETS_VIEW_MAPPINGS(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME, StringArray.SEMICOLON_SEPARATED, EMPTY_STRING_ARRAY),

        /**
         * Returns {@value StateManager#FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME} as {@link String} array with default of empty string array.
         */
        FULL_STATE_SAVING_VIEW_IDS(StateManager.FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME, StringArray.COMMA_SEPARATED, EMPTY_STRING_ARRAY),

        /**
         * Returns {@value UIInput#EMPTY_STRING_AS_NULL_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL(UIInput.EMPTY_STRING_AS_NULL_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME} as {@link Integer} with default of {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE}.
         */
        NUMBER_OF_CLIENT_WINDOWS(ClientWindow.NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME, Integer.class, ClientWindow.NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE),

        /**
         * Returns {@value StateManager#PARTIAL_STATE_SAVING_PARAM_NAME} as {@link Boolean} with default of {@code true}.
         */
        PARTIAL_STATE_SAVING(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, Boolean.class, true),

        /**
         * Returns {@value ProjectStage#PROJECT_STAGE_PARAM_NAME} as {@link ProjectStage} with default of {@link ProjectStage#Production}.
         * Note that this value can be overridden via JNDI entry {@value ProjectStage#PROJECT_STAGE_JNDI_NAME}.
         */
        PROJECT_STAGE(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.class, ProjectStage.Production),

        /**
         * Returns {@value ResourceHandler#RESOURCE_EXCLUDES_PARAM_NAME} as {@link String} array with default of {@value ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE}.
         */
        RESOURCE_EXCLUDES(ResourceHandler.RESOURCE_EXCLUDES_PARAM_NAME, StringArray.SPACE_SEPARATED, StringArray.SPACE_SEPARATED.split(ResourceHandler.RESOURCE_EXCLUDES_DEFAULT_VALUE)),

        /**
         * Returns {@value StateManager#SERIALIZE_SERVER_STATE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        SERIALIZE_SERVER_STATE(StateManager.SERIALIZE_SERVER_STATE_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@value UINamingContainer#SEPARATOR_CHAR_PARAM_NAME} as {@link Character} with default of {@value NamingContainer#SEPARATOR_CHAR}.
         */
        SEPARATOR_CHAR(UINamingContainer.SEPARATOR_CHAR_PARAM_NAME, Character.class, NamingContainer.SEPARATOR_CHAR),

        /**
         * Returns {@value StateManager#STATE_SAVING_METHOD_PARAM_NAME} as {@link String} with default of {@value StateManager#STATE_SAVING_METHOD_CLIENT}.
         * @see StateManager#STATE_SAVING_METHOD_PARAM_NAME
         */
        STATE_SAVING_METHOD(StateManager.STATE_SAVING_METHOD_PARAM_NAME, String.class, StateManager.STATE_SAVING_METHOD_CLIENT),

        /**
         * Returns {@value UIInput#VALIDATE_EMPTY_FIELDS_PARAM_NAME} as {@link String} with default of {@value UIInput#VALIDATE_EMPTY_FIELDS_DEFAULT_VALUE}.
         */
        VALIDATE_EMPTY_FIELDS(UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME, String.class, UIInput.VALIDATE_EMPTY_FIELDS_DEFAULT_VALUE),

        /**
         * Returns {@value UIViewRoot#VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS(UIViewRoot.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE}.
         */
        WEBAPP_CONTRACTS_DIRECTORY(ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME, String.class, ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE),

        /**
         * Returns {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE}.
         */
        WEBAPP_RESOURCES_DIRECTORY(ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME, String.class, ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE),

        /**
         * Returns {@value PushContext#WEBSOCKET_ENDPOINT_PORT_PARAM_NAME} as {@link Integer} with default of {@code 0} (default 0 means the code will take the port from the request).
         */
        WEBSOCKET_ENDPOINT_PORT(PushContext.WEBSOCKET_ENDPOINT_PORT_PARAM_NAME, Integer.class, 0),

        ;

        private static final Map<ContextParam, Object> VALUES = new EnumMap<>(ContextParam.class);

        private enum StringArray {
            SPACE_SEPARATED(Pattern.compile("\\s+")),
            SEMICOLON_SEPARATED(Pattern.compile("\\s*;\\s*")),
            COMMA_SEPARATED(Pattern.compile("\\s*,\\s*"));

            private Pattern pattern;
            
            private StringArray(Pattern pattern) {
                this.pattern = pattern;
            }

            public String[] split(String value) {
                return pattern.split(value);
            }
        }

        private final String name;
        private final Class<?> type;
        private final StringArray separated;
        private final Function<FacesContext, ?> defaultValue;        

        private <T> ContextParam(String name, Class<T> type, T defaultValue) {
            this.name = name;
            this.type = type;
            this.separated = null;
            this.defaultValue = $ -> defaultValue;
        }

        private <T> ContextParam(String name, Class<T> type, Function<FacesContext, T> defaultValue) {
            this.name = name;
            this.type = type;
            this.separated = null;
            this.defaultValue = defaultValue;
        }

        private ContextParam(String name, StringArray separated, String[] defaultValue) {
            this.name = name;
            this.type = String[].class;
            this.separated = separated;
            this.defaultValue = $ -> defaultValue;
        }

        /**
         * <p>
         * Returns the name of the context parameter.
         * @return The name of the context parameter.
         */
        public String getName() {
            return name;
        }

        /**
         * <p>
         * Returns the expected type of the context parameter value.
         * Supported values are:
         * <ul>
         * <li>{@link String}
         * <li>{@link String}{@code []}
         * <li>{@link Character}
         * <li>{@link Boolean}
         * <li>{@link Integer}
         * <li>{@link Path}
         * <li>{@link Enum}
         * </ul>
         * @return The expected type of the context parameter value.
         */
        public Class<?> getType() {
            return type;
        }

        /**
         * <p>
         * Returns the value of the context parameter, converted to the expected type as indicated by {@link #getType()}.
         * @param <T> The expected return type.
         * @param context The involved faces context.
         * @return The value of the context parameter, converted to the expected type as indicated by {@link #getType()}.
         * @throws ClassCastException When inferred T is of wrong type. See {@link #getType()} for the correct type.
         * @throws IllegalArgumentException When the value of the context parameter cannot be converted to the expected type as indicated by {@link #getType()}.
         */
        @SuppressWarnings("unchecked")
        public <T> T getValue(FacesContext context) {
            return (T) VALUES.computeIfAbsent(this, param -> param.parseValue(context));
        }

        /**
         * <p>
         * Returns {@code true} in case a boolean context parameter is {@code true}, or a non-boolean context parameter is explicitly set with a non-{@code null} value.
         * @param context The involved faces context.
         * @return {@code true} in case a boolean context parameter is {@code true}, or a non-boolean context parameter is explicitly set with a non-{@code null} value.
         * @throws IllegalArgumentException When the value of the context parameter cannot be converted to the expected type as indicated by {@link #getType()}.
         */
        public boolean isSet(FacesContext context) {
            return (getType() == Boolean.class) ? (boolean) getValue(context) : context.getExternalContext().getInitParameter(name) != null;
        }

        /**
         * <p>
         * Returns the default value of the context parameter, converted to the expected type as indicated by {@link #getType()}.
         * @param <T> The expected return type.
         * @param context The involved faces context.
         * @return The default value of the context parameter, converted to the expected type as indicated by {@link #getType()}.
         * @throws ClassCastException When inferred T is of wrong type. See {@link #getType()} for the correct type.
         */
        @SuppressWarnings("unchecked")
        public <T> T getDefaultValue(FacesContext context) {
            return (T) defaultValue.apply(context);
        }

        /**
         * <p>
         * Returns {@code true} when the value of the context parameter equals to the default value, irrespective of whether it is explicitly set.
         * @param context The involved faces context.
         * @return {@code true} when the value of the context parameter equals to the default value, irrespective of whether it is explicitly set.
         * @throws IllegalArgumentException When the value of the context parameter cannot be converted to the expected type as indicated by {@link #getType()}.
         */
        public boolean isDefault(FacesContext context) {
            return Objects.equals(getValue(context), defaultValue.apply(context));
        }

        @SuppressWarnings("unchecked")
        private <T> T parseValue(FacesContext context) {
            String value = context.getExternalContext().getInitParameter(name);

            if (value == null) {
                return getDefaultValue(context);
            }
            else if (type == String.class) {
                return (T) value;
            }
            else if (type == String[].class) {
                return (T) separated.split(value);
            }
            else if (type == Character.class) {
                if (value.length() == 1) {
                    return (T) Character.valueOf(value.charAt(0));
                }
            }
            else if (type == Boolean.class) {
                return (T) Boolean.valueOf(value);
            }
            else if (type == Integer.class) {
                try {
                    return (T) Integer.valueOf(value);
                }
                catch (NumberFormatException e) {
                    throw new IllegalArgumentException(getName() + ": invalid value: " + value, e);
                }
            }
            else if (type == Path.class) {
                return (T) Paths.get(value);
            }
            else if (type.isEnum()) {
                for (Object constant : type.getEnumConstants()) {
                    if (constant.toString().equalsIgnoreCase(value)) {
                        return (T) constant;
                    }
                }
            }

            throw new IllegalArgumentException(getName() + ": invalid value: " + value);
        }
    }


    /**
     * <p class="changed_added_5_0">
     * Returns {@value UIInput#ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean alwaysPerformValidationWhenRequiredIsTrue() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value FacesServlet#AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean automaticExtensionlessMapping() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME} as {@link String} with default of {@value ClientWindow#CLIENT_WINDOW_MODE_DEFAULT_VALUE}.
     * </p>
     */
    String clientWindowMode() default ClientWindow.CLIENT_WINDOW_MODE_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value FacesServlet#CONFIG_FILES_ATTR} as {@link String} array with default of empty string array.
     * </p>
     */
    String[] configFiles() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@value Converter#DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean datetimeConverterDefaultTimezoneIsSystemTimezone() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean disableDefaultBeanValidator() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value FacesServlet#DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean disableFacesservletToXhtml() default false;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@value BeanValidator#ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean enableValidateWholeBean() default false;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@value PushContext#ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean enableWebsocketEndpoint() default false;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_BUFFER_SIZE_PARAM_NAME} as {@link Integer} with default of {@value ViewHandler#FACELETS_BUFFER_SIZE_DEFAULT_VALUE}.
     * </p>
     */
    int faceletsBufferSize() default ViewHandler.FACELETS_BUFFER_SIZE_DEFAULT_VALUE;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_DECORATORS_PARAM_NAME} as {@link String} array with default of empty string array.
     * </p>
     */
    String[] faceletsDecorators() default {};

    /** 
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_LIBRARIES_PARAM_NAME} as {@link String} array with default of empty string array.
     * </p>
     */
    String[] faceletsLibraries() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_REFRESH_PERIOD_PARAM_NAME} as {@link Integer} with default of {@link Integer#MIN_VALUE}.
     * </p>
     */
    int faceletsRefreshPeriod() default Integer.MIN_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_SKIP_COMMENTS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean faceletsSkipComments() default false;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_SUFFIX_PARAM_NAME} as {@link String} with default of {@value ViewHandler#DEFAULT_FACELETS_SUFFIX}.
     * </p>
     */
    String faceletsSuffix() default ViewHandler.DEFAULT_FACELETS_SUFFIX;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_VIEW_MAPPINGS_PARAM_NAME} as {@link String} array with default of empty string array.
     * </p>
     */
    String[] faceletsViewMappings() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@value StateManager#FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME} as {@link String} array with default of empty string array.
     * </p>
     */
    String[] fullStateSavingViewIds() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@value UIInput#EMPTY_STRING_AS_NULL_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean interpretEmptyStringSubmittedValuesAsNull() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME} as {@link Integer} with default of {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE}.
     * </p>
     */
    int numberOfClientWindows() default ClientWindow.NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value StateManager#PARTIAL_STATE_SAVING_PARAM_NAME} as {@link Boolean} with default of {@code true}.
     * </p>
     */
    boolean partialStateSaving() default true;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ProjectStage#PROJECT_STAGE_PARAM_NAME} as {@link ProjectStage} with default of {@link ProjectStage#Production}.
     * Note that this value can be overridden via JNDI entry {@value ProjectStage#PROJECT_STAGE_JNDI_NAME}.
     * </p>
     */
    ProjectStage projectStage() default ProjectStage.Production;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ResourceHandler#RESOURCE_EXCLUDES_PARAM_NAME} as {@link String} array with default of {@value ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE}.
     * </p>
     */
    String[] resourceExcludes() default { ".class", ".jsp", ".jspx", ".properties", ".xhtml", ".groovy" }; // We cannot reference ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE, it had to be hardcoded.

    /**
     * <p class="changed_added_5_0">
     * Returns {@value StateManager#SERIALIZE_SERVER_STATE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean serializeServerState() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value UINamingContainer#SEPARATOR_CHAR_PARAM_NAME} as {@link Character} with default of {@value NamingContainer#SEPARATOR_CHAR}.
     * </p>
     */
    char separatorChar() default NamingContainer.SEPARATOR_CHAR;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value StateManager#STATE_SAVING_METHOD_PARAM_NAME} as {@link String} with default of {@value StateManager#STATE_SAVING_METHOD_CLIENT}.
     * </p>
     */
    String stateSavingMethod() default StateManager.STATE_SAVING_METHOD_CLIENT;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value UIInput#VALIDATE_EMPTY_FIELDS_PARAM_NAME} as {@link String} with default of {@value UIInput#VALIDATE_EMPTY_FIELDS_DEFAULT_VALUE}.
     * </p>
     */
    String validateEmptyFields() default UIInput.VALIDATE_EMPTY_FIELDS_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value UIViewRoot#VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    boolean viewrootPhaseListenerQueuesExceptions() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE}.
     * </p>
     */
    String webappContractsDirectory() default ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE}.
     * </p>
     */
    String webappResourcesDirectory() default ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value PushContext#WEBSOCKET_ENDPOINT_PORT_PARAM_NAME} as {@link Integer} with default of {@code 0} (default 0 means the code will take the port from the request).
     * </p>
     */
    int websocketEndpointPort() default 0;
    
}
