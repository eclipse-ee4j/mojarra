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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.Nonbinding;
import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.StateManager.StateSavingMethod;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIInput.ValidateEmptyFields;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
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
 * <p class="changed_added_5_0">
 * The attributes can be used to preconfigure the context parameters of your Jakarta Faces application.
 * The {@link ContextParam} can be used to obtain the context parameters of your Jakarta Faces application.
 * </p>
 */
@Qualifier
@Target(TYPE)
@Retention(RUNTIME)
public @interface FacesConfig {

    /**
     * <p class="changed_added_5_0">
     * Returns {@value UIInput#ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean alwaysPerformValidationWhenRequiredIsTrue() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value FacesServlet#AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean automaticExtensionlessMapping() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME} as {@link String} with default of {@value ClientWindow#CLIENT_WINDOW_MODE_DEFAULT_VALUE}.
     * </p>
     */
    @Nonbinding String clientWindowMode() default ClientWindow.CLIENT_WINDOW_MODE_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value FacesServlet#CONFIG_FILES_ATTR} as {@link String} array with default of empty string array.
     * </p>
     */
    @Nonbinding String[] configFiles() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@value Converter#DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean datetimeConverterDefaultTimezoneIsSystemTimezone() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean disableDefaultBeanValidator() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value FacesServlet#DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean disableFacesservletToXhtml() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value BeanValidator#ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean enableValidateWholeBean() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value PushContext#ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean enableWebsocketEndpoint() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_BUFFER_SIZE_PARAM_NAME} as {@link Integer} with default of {@value ViewHandler#FACELETS_BUFFER_SIZE_DEFAULT_VALUE}.
     * </p>
     */
    @Nonbinding int faceletsBufferSize() default ViewHandler.FACELETS_BUFFER_SIZE_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_DECORATORS_PARAM_NAME} as {@link String} array with default of empty string array.
     * </p>
     */
    @Nonbinding String[] faceletsDecorators() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_LIBRARIES_PARAM_NAME} as {@link String} array with default of empty string array.
     * </p>
     */
    @Nonbinding String[] faceletsLibraries() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_REFRESH_PERIOD_PARAM_NAME} as {@link Integer} with default of {@link Integer#MIN_VALUE},
     * meaning that the runtime then needs to determine the final default value via {@code ContextParam.FACELETS_REFRESH_PERIOD.getDefaultValue(FacesContext)}
     * because that depends on the currently configured {@link Application#getProjectStage()}.
     * </p>
     */
    @Nonbinding int faceletsRefreshPeriod() default Integer.MIN_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_SKIP_COMMENTS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean faceletsSkipComments() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_SUFFIX_PARAM_NAME} as {@link String} with default of {@value ViewHandler#DEFAULT_FACELETS_SUFFIX}.
     * </p>
     */
    @Nonbinding String faceletsSuffix() default ViewHandler.DEFAULT_FACELETS_SUFFIX;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ViewHandler#FACELETS_VIEW_MAPPINGS_PARAM_NAME} as {@link String} array with default of empty string array.
     * </p>
     */
    @Nonbinding String[] faceletsViewMappings() default {};

    /**
     * <p class="changed_added_5_0">
     * Returns {@value UIInput#EMPTY_STRING_AS_NULL_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean interpretEmptyStringSubmittedValuesAsNull() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME} as {@link Integer} with default of {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE}.
     * </p>
     */
    @Nonbinding int numberOfClientWindows() default ClientWindow.NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ProjectStage#PROJECT_STAGE_PARAM_NAME} as {@link ProjectStage} with default of {@link ProjectStage#Production}.
     * Note that this value can be overridden via JNDI entry {@value ProjectStage#PROJECT_STAGE_JNDI_NAME}.
     * </p>
     */
    @Nonbinding ProjectStage projectStage() default ProjectStage.Production;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ResourceHandler#RESOURCE_EXCLUDES_PARAM_NAME} as {@link String} array with default of {@value ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE}.
     * </p>
     */
    @Nonbinding String[] resourceExcludes() default { ".class", ".jsp", ".jspx", ".properties", ".xhtml", ".groovy" }; // We cannot reference ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE, it had to be hardcoded.

    /**
     * <p class="changed_added_5_0">
     * Returns {@value StateManager#SERIALIZE_SERVER_STATE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean serializeServerState() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value UINamingContainer#SEPARATOR_CHAR_PARAM_NAME} as {@link Character} with default of {@value NamingContainer#SEPARATOR_CHAR}.
     * </p>
     */
    @Nonbinding char separatorChar() default NamingContainer.SEPARATOR_CHAR;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value StateManager#STATE_SAVING_METHOD_PARAM_NAME} as {@link String} with default of {@link StateSavingMethod#CLIENT}.
     * </p>
     */
    @Nonbinding StateSavingMethod stateSavingMethod() default StateSavingMethod.CLIENT;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value UIInput#VALIDATE_EMPTY_FIELDS_PARAM_NAME} as {@link String} with default of {@link ValidateEmptyFields#AUTO}.
     * </p>
     */
    @Nonbinding ValidateEmptyFields validateEmptyFields() default ValidateEmptyFields.AUTO;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value UIViewRoot#VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     * </p>
     */
    @Nonbinding boolean viewrootPhaseListenerQueuesExceptions() default false;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE}.
     * </p>
     */
    @Nonbinding String webappContractsDirectory() default ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE}.
     * </p>
     */
    @Nonbinding String webappResourcesDirectory() default ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE;

    /**
     * <p class="changed_added_5_0">
     * Returns {@value PushContext#WEBSOCKET_ENDPOINT_PORT_PARAM_NAME} as {@link Integer} with default of {@code 0} (default 0 means the code will take the port from the request).
     * </p>
     */
    @Nonbinding int websocketEndpointPort() default 0;
    /**
     * <p class="changed_added_4_0">
     * Supports inline instantiation of the {@link FacesConfig} qualifier.
     * </p>
     *
     * @since 4.0
     */
    public static final class Literal extends AnnotationLiteral<FacesConfig> implements FacesConfig {
        private static final long serialVersionUID = 1L;
        private static final String[] EMPTY_STRING_ARRAY = {};

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
        public boolean interpretEmptyStringSubmittedValuesAsNull() {
            return false;
        }

        @Override
        public int numberOfClientWindows() {
            return ClientWindow.NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE;
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
        public StateSavingMethod stateSavingMethod() {
            return StateSavingMethod.CLIENT;
        }

        @Override
        public ValidateEmptyFields validateEmptyFields() {
            return ValidateEmptyFields.AUTO;
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
     * The {@link ContextParam#getValue(FacesContext)} can be used to obtain the value of the context parameter.
     * </p>
     *
     * @since 5.0
     */
    public static enum ContextParam {

        /**
         * Returns {@value UIInput#ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE} as {@link Boolean} with default of {@code false}.
         */
        ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE(UIInput.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE, FacesConfig::alwaysPerformValidationWhenRequiredIsTrue),

        /**
         * Returns {@value FacesServlet#AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        AUTOMATIC_EXTENSIONLESS_MAPPING(FacesServlet.AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME, FacesConfig::automaticExtensionlessMapping),

        /**
         * Returns {@value ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME} as {@link String} with default of {@code none}.
         */
        CLIENT_WINDOW_MODE(ClientWindow.CLIENT_WINDOW_MODE_PARAM_NAME, FacesConfig::clientWindowMode),

        /**
         * Returns {@value FacesServlet#CONFIG_FILES_ATTR} as {@link String} array with default of empty string array.
         */
        CONFIG_FILES(FacesServlet.CONFIG_FILES_ATTR, FacesConfig::configFiles, StringArray.COMMA_SEPARATED),

        /**
         * Returns {@value Converter#DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE(Converter.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME, FacesConfig::datetimeConverterDefaultTimezoneIsSystemTimezone),

        /**
         * Returns {@value BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        DISABLE_DEFAULT_BEAN_VALIDATOR(BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME, FacesConfig::disableDefaultBeanValidator),

        /**
         * Returns {@value FacesServlet#DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        DISABLE_FACESSERVLET_TO_XHTML(FacesServlet.DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME, FacesConfig::disableFacesservletToXhtml),

        /**
         * Returns {@value BeanValidator#ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        ENABLE_VALIDATE_WHOLE_BEAN(BeanValidator.ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME, FacesConfig::enableValidateWholeBean),

        /**
         * Returns {@value PushContext#ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        ENABLE_WEBSOCKET_ENDPOINT(PushContext.ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME, FacesConfig::enableWebsocketEndpoint),

        /**
         * Returns {@value ViewHandler#FACELETS_BUFFER_SIZE_PARAM_NAME} as {@link Integer} with default of {@value ViewHandler#FACELETS_BUFFER_SIZE_DEFAULT_VALUE}.
         */
        FACELETS_BUFFER_SIZE(ViewHandler.FACELETS_BUFFER_SIZE_PARAM_NAME, FacesConfig::faceletsBufferSize),

        /**
         * Returns {@value ViewHandler#FACELETS_DECORATORS_PARAM_NAME} as {@link String} array with default of empty string array.
         */
        FACELETS_DECORATORS(ViewHandler.FACELETS_DECORATORS_PARAM_NAME, FacesConfig::faceletsDecorators, StringArray.SEMICOLON_SEPARATED),

        /**
         * Returns {@value ViewHandler#FACELETS_LIBRARIES_PARAM_NAME} as {@link String} array with default of empty string array.
         */
        FACELETS_LIBRARIES(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, FacesConfig::faceletsLibraries, StringArray.SEMICOLON_SEPARATED),

        /**
         * Returns {@value ViewHandler#FACELETS_REFRESH_PERIOD_PARAM_NAME} as {@link Integer} with default of {@code -1} when
         * {@link Application#getProjectStage()} is {@link ProjectStage#Production} else default of {@code 0}.
         */
        FACELETS_REFRESH_PERIOD(ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME, FacesConfig::faceletsRefreshPeriod, (Function<FacesContext, Integer>) context -> context.getApplication().getProjectStage() == ProjectStage.Production ? -1 : 0),

        /**
         * Returns {@value ViewHandler#FACELETS_SKIP_COMMENTS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        FACELETS_SKIP_COMMENTS(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, FacesConfig::faceletsSkipComments),

        /**
         * Returns {@value ViewHandler#FACELETS_SUFFIX_PARAM_NAME} as {@link String} with default of {@value ViewHandler#DEFAULT_FACELETS_SUFFIX}.
         */
        FACELETS_SUFFIX(ViewHandler.FACELETS_SUFFIX_PARAM_NAME, FacesConfig::faceletsSuffix),

        /**
         * Returns {@value ViewHandler#FACELETS_VIEW_MAPPINGS_PARAM_NAME} as {@link String} array with default of empty string array.
         */
        FACELETS_VIEW_MAPPINGS(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME, FacesConfig::faceletsViewMappings, StringArray.SEMICOLON_SEPARATED),

        /**
         * Returns {@value UIInput#EMPTY_STRING_AS_NULL_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL(UIInput.EMPTY_STRING_AS_NULL_PARAM_NAME, FacesConfig::interpretEmptyStringSubmittedValuesAsNull),

        /**
         * Returns {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME} as {@link Integer} with default of {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE}.
         */
        NUMBER_OF_CLIENT_WINDOWS(ClientWindow.NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME, FacesConfig::numberOfClientWindows),

        /**
         * Returns {@value ProjectStage#PROJECT_STAGE_PARAM_NAME} as {@link ProjectStage} with default of {@link ProjectStage#Production}.
         * Note that this value can be overridden via JNDI entry {@value ProjectStage#PROJECT_STAGE_JNDI_NAME}.
         */
        PROJECT_STAGE(ProjectStage.PROJECT_STAGE_PARAM_NAME, FacesConfig::projectStage),

        /**
         * Returns {@value ResourceHandler#RESOURCE_EXCLUDES_PARAM_NAME} as {@link String} array with default of {@value ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE}.
         */
        RESOURCE_EXCLUDES(ResourceHandler.RESOURCE_EXCLUDES_PARAM_NAME, FacesConfig::resourceExcludes, StringArray.SPACE_SEPARATED),

        /**
         * Returns {@value StateManager#SERIALIZE_SERVER_STATE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        SERIALIZE_SERVER_STATE(StateManager.SERIALIZE_SERVER_STATE_PARAM_NAME, FacesConfig::serializeServerState),

        /**
         * Returns {@value UINamingContainer#SEPARATOR_CHAR_PARAM_NAME} as {@link Character} with default of {@value NamingContainer#SEPARATOR_CHAR}.
         */
        SEPARATOR_CHAR(UINamingContainer.SEPARATOR_CHAR_PARAM_NAME, FacesConfig::separatorChar),

        /**
         * Returns {@value StateManager#STATE_SAVING_METHOD_PARAM_NAME} as {@link String} with default of {@link StateSavingMethod#CLIENT}..
         * @see StateManager#STATE_SAVING_METHOD_PARAM_NAME
         */
        STATE_SAVING_METHOD(StateManager.STATE_SAVING_METHOD_PARAM_NAME, FacesConfig::stateSavingMethod),

        /**
         * Returns {@value UIInput#VALIDATE_EMPTY_FIELDS_PARAM_NAME} as {@link String} with default of {@link ValidateEmptyFields#AUTO}.
         */
        VALIDATE_EMPTY_FIELDS(UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME, FacesConfig::validateEmptyFields),

        /**
         * Returns {@value UIViewRoot#VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
         */
        VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS(UIViewRoot.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME, FacesConfig::viewrootPhaseListenerQueuesExceptions),

        /**
         * Returns {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE}.
         */
        WEBAPP_CONTRACTS_DIRECTORY(ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME, FacesConfig::webappContractsDirectory),

        /**
         * Returns {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE}.
         */
        WEBAPP_RESOURCES_DIRECTORY(ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME, FacesConfig::webappResourcesDirectory),

        /**
         * Returns {@value PushContext#WEBSOCKET_ENDPOINT_PORT_PARAM_NAME} as {@link Integer} with default of {@code 0} (default 0 means the code will take the port from the request).
         */
        WEBSOCKET_ENDPOINT_PORT(PushContext.WEBSOCKET_ENDPOINT_PORT_PARAM_NAME, FacesConfig::websocketEndpointPort),

        ;

        private static final Logger LOGGER = Logger.getLogger("jakarta.faces.annotation");
        private static final Map<ContextParam, Object> VALUES = new EnumMap<>(ContextParam.class);
        private static final AtomicReference<Optional<FacesConfig>> ANNOTATED_CONFIG = new AtomicReference<>();

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

        private final String name;
        private final Function<FacesConfig, ?> annotatedValue;
        private final Optional<Function<FacesContext, ?>> defaultValueSupplier;
        private final StringArray separated;
        private final Object defaultAnnotatedValue;
        private final Class<?> type;

        private <T> ContextParam(String name, Function<FacesConfig, T> annotatedValue) {
            this(name, annotatedValue, null, null);
        }

        private ContextParam(String name, Function<FacesConfig, String[]> annotatedValue, StringArray separated) {
            this(name, annotatedValue, null, separated);
        }

        private <T> ContextParam(String name, Function<FacesConfig, T> annotatedValue, Function<FacesContext, T> defaultValueSupplier) {
            this(name, annotatedValue, defaultValueSupplier, null);
        }

        private <T> ContextParam(String name, Function<FacesConfig, T> annotatedValue, Function<FacesContext, T> defaultValueSupplier, StringArray separated) {
            this.name = name;
            this.annotatedValue = annotatedValue;
            this.defaultValueSupplier = Optional.ofNullable(defaultValueSupplier);
            this.separated = separated;
            this.defaultAnnotatedValue = annotatedValue.apply(FacesConfig.Literal.INSTANCE);
            this.type = defaultAnnotatedValue.getClass();
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
         * This method never returns {@code null}. When the context parameter is not set, a default value is returned.
         * <p>
         * The implementation must first look for {@link ExternalContext#getInitParameter(String)}. If it is non-{@code null}, then return it.
         * Else look for any {@link FacesConfig} annotation. If present, then return it. Else return the default value.
         * @param <T> The expected return type.
         * @param context The involved faces context.
         * @return The value of the context parameter, converted to the expected type as indicated by {@link #getType()}.
         * @throws ClassCastException When inferred {@code T} is of wrong type. See {@link #getType()} for the correct type.
         * @throws IllegalArgumentException When the value of the context parameter cannot be converted to the expected type as indicated by {@link #getType()}.
         */
        @SuppressWarnings("unchecked")
        public <T> T getValue(FacesContext context) {
            return (T) VALUES.computeIfAbsent(this, param -> getContextParamValue(context).orElseGet(() -> getAnnotatedValue(context).orElseGet(() -> getDefaultValue(context))));
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
         * @throws ClassCastException When inferred {@code T} is of wrong type. See {@link #getType()} for the correct type.
         */
        @SuppressWarnings("unchecked")
        public <T> T getDefaultValue(FacesContext context) {
            Object supplied = defaultValueSupplier.map(supplier -> supplier.apply(context)).orElse(null);
            return (T) (supplied != null ? supplied : defaultAnnotatedValue);
        }

        /**
         * <p>
         * Returns {@code true} when the value of the context parameter equals to the default value, irrespective of whether it is explicitly set.
         * @param context The involved faces context.
         * @return {@code true} when the value of the context parameter equals to the default value, irrespective of whether it is explicitly set.
         * @throws IllegalArgumentException When the value of the context parameter cannot be converted to the expected type as indicated by {@link #getType()}.
         */
        public boolean isDefault(FacesContext context) {
            return Objects.equals(getValue(context), getDefaultValue(context));
        }

        @SuppressWarnings("unchecked")
        private <T> Optional<T> getContextParamValue(FacesContext context) {
            String value = context.getExternalContext().getInitParameter(name);

            if (value == null) {
                return Optional.empty();
            }
            else if (type == String.class) {
                return Optional.of((T) value);
            }
            else if (type == String[].class) {
                return Optional.of((T) separated.split(value));
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
                    throw new IllegalArgumentException(getName() + ": invalid value: " + value, e);
                }
            }
            else if (type.isEnum()) {
                for (Object constant : type.getEnumConstants()) {
                    if (constant.toString().equalsIgnoreCase(value)) {
                        return Optional.of((T) constant);
                    }
                }
            }

            throw new IllegalArgumentException(getName() + ": invalid value: " + value);
        }

        /**
         * We're using getBeanReferencesByQualifier() because CDI.current().select(FacesConfig.Literal.INSTANCE) would throw ambiguous resolution when
         * there are multiple beans with {@code @FacesConfig}.
         */
        @SuppressWarnings("unchecked")
        private <T> Optional<T> getAnnotatedValue(FacesContext context) {
            Optional<FacesConfig> annotatedConfig = ANNOTATED_CONFIG.updateAndGet(config -> config != null ? config : PackageUtils
                    .getBeanReferencesByQualifier(context, FacesConfig.Literal.INSTANCE).stream()
                    .sorted(PackageUtils.BEAN_PRIORITY_COMPARATOR)
                    .peek(bean -> LOGGER.info("@FacesConfig found on " + bean.getClass() + " -- if any, others are ignored"))
                    .map(bean -> bean.getClass().getAnnotation(FacesConfig.class))
                    .findFirst());
            return annotatedConfig.map(config -> (T) annotatedValue.apply(config)).filter(value -> !shouldDelegateToDefaultValueSupplier(value));
        }

        private <T> boolean shouldDelegateToDefaultValueSupplier(T value) {
            return defaultValueSupplier.isPresent() && Objects.equals(value, defaultAnnotatedValue);
        }

    }

}
