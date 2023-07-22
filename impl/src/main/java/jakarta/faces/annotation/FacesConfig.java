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
            return 0;
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
        public boolean interpretEmptyStringSubmittedValuesAsNull() {
            return false;
        }

        @Override
        public int numberOfClientWindows() {
            return ClientWindow.NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE;
        }

        @Override
        public ProjectStage projectStage() {
            return ProjectStage.PROJECT_STAGE_DEFAULT_VALUE;
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
         * Returns {@code jakarta.faces.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE} as {@link Boolean} with default of {@code false}.
         * @see UIInput#ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE
         */
        ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE(UIInput.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE, Boolean.class, false),

        /**
         * Returns {@code jakarta.faces.AUTOMATIC_EXTENSIONLESS_MAPPING} as {@link Boolean} with default of {@code false}.
         * @see FacesServlet#AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME
         */
        AUTOMATIC_EXTENSIONLESS_MAPPING(FacesServlet.AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@code jakarta.faces.CLIENT_WINDOW_MODE} as {@link String} with default of {@code none}.
         * @see ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME
         */
        CLIENT_WINDOW_MODE(ClientWindow.CLIENT_WINDOW_MODE_PARAM_NAME, String.class, ClientWindow.CLIENT_WINDOW_MODE_DEFAULT_VALUE),

        /**
         * Returns {@code jakarta.faces.CONFIG_FILES} as {@link String} array with default of empty string array.
         * @see FacesServlet#CONFIG_FILES_ATTR
         */
        CONFIG_FILES(FacesServlet.CONFIG_FILES_ATTR, StringArray.COMMA_SEPARATED, EMPTY_STRING_ARRAY),

        /**
         * Returns {@code jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE} as {@link Boolean} with default of {@code false}.
         * @see Converter#DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME
         */
        DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE(Converter.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@code jakarta.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR} as {@link Boolean} with default of {@code false}.
         * @see BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME
         */
        DISABLE_DEFAULT_BEAN_VALIDATOR(BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@code jakarta.faces.DISABLE_FACESSERVLET_TO_XHTML} as {@link Boolean} with default of {@code false}.
         * @see FacesServlet#DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME
         */
        DISABLE_FACESSERVLET_TO_XHTML(FacesServlet.DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns {@code jakarta.faces.validator.ENABLE_VALIDATE_WHOLE_BEAN} as {@link Boolean} with default of {@code false}.
         * @see BeanValidator#ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME
         */
        ENABLE_VALIDATE_WHOLE_BEAN(BeanValidator.ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns {@code jakarta.faces.ENABLE_WEBSOCKET_ENDPOINT} as {@link Boolean} with default of {@code false}.
         * @see PushContext#ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME
         */
        ENABLE_WEBSOCKET_ENDPOINT(PushContext.ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns {@code jakarta.faces.FACELETS_BUFFER_SIZE} as {@link Integer} with default of {@code 1024}.
         * @see ViewHandler#FACELETS_BUFFER_SIZE_PARAM_NAME
         */
        FACELETS_BUFFER_SIZE(ViewHandler.FACELETS_BUFFER_SIZE_PARAM_NAME, Integer.class, ViewHandler.FACELETS_BUFFER_SIZE_DEFAULT_VALUE),

        /** 
         * Returns {@code jakarta.faces.FACELETS_DECORATORS} as {@link String} array with default of empty string array.
         * @see ViewHandler#FACELETS_DECORATORS_PARAM_NAME
         */
        FACELETS_DECORATORS(ViewHandler.FACELETS_DECORATORS_PARAM_NAME, StringArray.SEMICOLON_SEPARATED, EMPTY_STRING_ARRAY),

        /** 
         * Returns {@code jakarta.faces.FACELETS_LIBRARIES} as {@link String} array with default of empty string array.
         * @see ViewHandler#FACELETS_LIBRARIES_PARAM_NAME
         */
        FACELETS_LIBRARIES(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, StringArray.SEMICOLON_SEPARATED, EMPTY_STRING_ARRAY),

        /**
         * Returns {@code jakarta.faces.FACELETS_REFRESH_PERIOD} as {@link Integer} with default of {@code -1} when
         * {@link Application#getProjectStage()} is {@link ProjectStage#Production} else default of {@code 0}.
         * @see ViewHandler#FACELETS_REFRESH_PERIOD_PARAM_NAME
         */
        FACELETS_REFRESH_PERIOD(ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME, Integer.class, context -> context.getApplication().getProjectStage() == ProjectStage.Production ? -1 : 0),

        /**
         * Returns {@code jakarta.faces.FACELETS_SKIP_COMMENTS} as {@link Boolean} with default of {@code false}.
         * @see ViewHandler#FACELETS_SKIP_COMMENTS_PARAM_NAME
         */
        FACELETS_SKIP_COMMENTS(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns {@code jakarta.faces.FACELETS_SUFFIX} as {@link String} with default of {@link ViewHandler#DEFAULT_FACELETS_SUFFIX}.
         * @see ViewHandler#FACELETS_SUFFIX_PARAM_NAME
         */
        FACELETS_SUFFIX(ViewHandler.FACELETS_SUFFIX_PARAM_NAME, String.class, ViewHandler.DEFAULT_FACELETS_SUFFIX),

        /**
         * Returns {@code jakarta.faces.FACELETS_VIEW_MAPPINGS} as {@link String} array with default of empty string array.
         * @see ViewHandler#FACELETS_VIEW_MAPPINGS_PARAM_NAME
         */
        FACELETS_VIEW_MAPPINGS(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME, StringArray.SEMICOLON_SEPARATED, EMPTY_STRING_ARRAY),

        /**
         * Returns {@code jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL} as {@link Boolean} with default of {@code false}.
         * @see UIInput#EMPTY_STRING_AS_NULL_PARAM_NAME
         */
        INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL(UIInput.EMPTY_STRING_AS_NULL_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@code jakarta.faces.NUMBER_OF_CLIENT_WINDOWS} as {@link Integer} with default of {@code 10}.
         * @see ClientWindow#NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME
         */
        NUMBER_OF_CLIENT_WINDOWS(ClientWindow.NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME, Integer.class, ClientWindow.NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE),

        /**
         * Returns {@code jakarta.faces.PROJECT_STAGE} as {@link ProjectStage} with default of {@code Production}.
         * @see ProjectStage#PROJECT_STAGE_PARAM_NAME
         */
        PROJECT_STAGE(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.class, ProjectStage.PROJECT_STAGE_DEFAULT_VALUE),

        /**
         * Returns {@code jakarta.faces.RESOURCE_EXCLUDES} as {@link String} array with default of {@link ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE}.
         * @see ResourceHandler#RESOURCE_EXCLUDES_PARAM_NAME
         */
        RESOURCE_EXCLUDES(ResourceHandler.RESOURCE_EXCLUDES_PARAM_NAME, StringArray.SPACE_SEPARATED, StringArray.SPACE_SEPARATED.split(ResourceHandler.RESOURCE_EXCLUDES_DEFAULT_VALUE)),

        /**
         * Returns {@code jakarta.faces.SERIALIZE_SERVER_STATE} as {@link Boolean} with default of {@code false}.
         * @see StateManager#SERIALIZE_SERVER_STATE_PARAM_NAME
         */
        SERIALIZE_SERVER_STATE(StateManager.SERIALIZE_SERVER_STATE_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@code jakarta.faces.SEPARATOR_CHAR} as {@link Character} with default of {@link NamingContainer#SEPARATOR_CHAR}.
         * @see UINamingContainer#SEPARATOR_CHAR_PARAM_NAME
         */
        SEPARATOR_CHAR(UINamingContainer.SEPARATOR_CHAR_PARAM_NAME, Character.class, NamingContainer.SEPARATOR_CHAR),

        /**
         * Returns {@code jakarta.faces.VALIDATE_EMPTY_FIELDS} as {@link String} with default of {@code auto}.
         * @see UIInput#VALIDATE_EMPTY_FIELDS_PARAM_NAME
         */
        VALIDATE_EMPTY_FIELDS(UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME, String.class, UIInput.VALIDATE_EMPTY_FIELDS_DEFAULT_VALUE),

        /**
         * Returns {@code jakarta.faces.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS} as {@link Boolean} with default of {@code false}.
         * @see UIViewRoot#VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME
         */
        VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS(UIViewRoot.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME, Boolean.class, false),

        /**
         * Returns {@code jakarta.faces.WEBAPP_CONTRACTS_DIRECTORY} as {@link Path} with default of {@link ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE}.
         * @see ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME
         */
        WEBAPP_CONTRACTS_DIRECTORY(ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME, Path.class, Paths.get(ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE)),

        /**
         * Returns {@code jakarta.faces.WEBAPP_RESOURCES_DIRECTORY} as {@link Path} with default of {@link ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE}.
         * @see ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME
         */
        WEBAPP_RESOURCES_DIRECTORY(ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME, Path.class, Paths.get(ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE)),

        /**
         * Returns {@code jakarta.faces.WEBSOCKET_ENDPOINT_PORT} as {@link Integer} with default of {@code 0}.
         * @see PushContext#WEBSOCKET_ENDPOINT_PORT_PARAM_NAME
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

        @SuppressWarnings("unchecked")
        private <T> T parseValue(FacesContext context) {
            String value = context.getExternalContext().getInitParameter(name);

            if (value == null) {
                return (T) defaultValue.apply(context);
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
     * Returns {@code jakarta.faces.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see UIInput#ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE
     */
    boolean alwaysPerformValidationWhenRequiredIsTrue() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.AUTOMATIC_EXTENSIONLESS_MAPPING} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see FacesServlet#AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME
     */
    boolean automaticExtensionlessMapping() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.CLIENT_WINDOW_MODE} as {@link String} with default of {@code none}.
     * </p>
     *
     * @see ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME
     */
    String clientWindowMode() default ClientWindow.CLIENT_WINDOW_MODE_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.CONFIG_FILES} as {@link String} array with default of empty string array.
     * </p>
     *
     * @see FacesServlet#CONFIG_FILES_ATTR
     */
    String[] configFiles() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see Converter#DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME
     */
    boolean datetimeConverterDefaultTimezoneIsSystemTimezone() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME
     */
    boolean disableDefaultBeanValidator() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.DISABLE_FACESSERVLET_TO_XHTML} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see FacesServlet#DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME
     */
    boolean disableFacesservletToXhtml() default false;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.validator.ENABLE_VALIDATE_WHOLE_BEAN} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see BeanValidator#ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME
     */
    boolean enableValidateWholeBean() default false;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.ENABLE_WEBSOCKET_ENDPOINT} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see PushContext#ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME
     */
    boolean enableWebsocketEndpoint() default false;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.FACELETS_BUFFER_SIZE} as {@link Integer} with default of {@code 1024}.
     * </p>
     *
     * @see ViewHandler#FACELETS_BUFFER_SIZE_PARAM_NAME
     */
    int faceletsBufferSize() default ViewHandler.FACELETS_BUFFER_SIZE_DEFAULT_VALUE;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.FACELETS_DECORATORS} as {@link String} array with default of empty string array.
     * </p>
     *
     * @see ViewHandler#FACELETS_DECORATORS_PARAM_NAME
     */
    String[] faceletsDecorators() default {};

    /** 
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.FACELETS_LIBRARIES} as {@link String} array with default of empty string array.
     * </p>
     *
     * @see ViewHandler#FACELETS_LIBRARIES_PARAM_NAME
     */
    String[] faceletsLibraries() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.FACELETS_REFRESH_PERIOD} as {@link Integer} with default of {@code -1} when
     * {@link Application#getProjectStage()} is {@link ProjectStage#Production} else default of {@code 0}.
     * </p>
     *
     * @see ViewHandler#FACELETS_REFRESH_PERIOD_PARAM_NAME
     */
    int faceletsRefreshPeriod() default 0; // TODO

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.FACELETS_SKIP_COMMENTS} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see ViewHandler#FACELETS_SKIP_COMMENTS_PARAM_NAME
     */
    boolean faceletsSkipComments() default false;

    /** 
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.FACELETS_SUFFIX} as {@link String} with default of {@link ViewHandler#DEFAULT_FACELETS_SUFFIX}.
     * </p>
     *
     * @see ViewHandler#FACELETS_SUFFIX_PARAM_NAME
     */
    String faceletsSuffix() default ViewHandler.DEFAULT_FACELETS_SUFFIX;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.FACELETS_VIEW_MAPPINGS} as {@link String} array with default of empty string array.
     * </p>
     *
     * @see ViewHandler#FACELETS_VIEW_MAPPINGS_PARAM_NAME
     */
    String[] faceletsViewMappings() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see UIInput#EMPTY_STRING_AS_NULL_PARAM_NAME
     */
    boolean interpretEmptyStringSubmittedValuesAsNull() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.NUMBER_OF_CLIENT_WINDOWS} as {@link Integer} with default of {@code 10}.
     * </p>
     *
     * @see ClientWindow#NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME
     */
    int numberOfClientWindows() default ClientWindow.NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.PROJECT_STAGE} as {@link ProjectStage} with default of {@code Production}.
     * </p>
     *
     * @see ProjectStage#PROJECT_STAGE_PARAM_NAME
     */
    ProjectStage projectStage() default ProjectStage.PROJECT_STAGE_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.RESOURCE_EXCLUDES} as {@link String} array with default of {@link ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE}.
     * </p>
     *
     * @see ResourceHandler#RESOURCE_EXCLUDES_PARAM_NAME
     */
    String[] resourceExcludes() default { ".class", ".jsp", ".jspx", ".properties", ".xhtml", ".groovy" }; // We cannot reference ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE, it had to be hardcoded.

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.SERIALIZE_SERVER_STATE} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see StateManager#SERIALIZE_SERVER_STATE_PARAM_NAME
     */
    boolean serializeServerState() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.SEPARATOR_CHAR} as {@link Character} with default of {@link NamingContainer#SEPARATOR_CHAR}.
     * </p>
     *
     * @see UINamingContainer#SEPARATOR_CHAR_PARAM_NAME
     */
    char separatorChar() default NamingContainer.SEPARATOR_CHAR;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.VALIDATE_EMPTY_FIELDS} as {@link String} with default of {@code auto}.
     * </p>
     *
     * @see UIInput#VALIDATE_EMPTY_FIELDS_PARAM_NAME
     */
    String validateEmptyFields() default UIInput.VALIDATE_EMPTY_FIELDS_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS} as {@link Boolean} with default of {@code false}.
     * </p>
     *
     * @see UIViewRoot#VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME
     */
    boolean viewrootPhaseListenerQueuesExceptions() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.WEBAPP_CONTRACTS_DIRECTORY} as {@link String} with default of {@link ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE}.
     * </p>
     *
     * @see ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME
     */
    String webappContractsDirectory() default ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.WEBAPP_RESOURCES_DIRECTORY} as {@link String} with default of {@link ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE}.
     * </p>
     *
     * @see ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME
     */
    String webappResourcesDirectory() default ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@code jakarta.faces.WEBSOCKET_ENDPOINT_PORT} as {@link Integer} with default of {@code 0}.
     * </p>
     *
     * @see PushContext#WEBSOCKET_ENDPOINT_PORT_PARAM_NAME
     */
    int websocketEndpointPort() default 0;
    
}
