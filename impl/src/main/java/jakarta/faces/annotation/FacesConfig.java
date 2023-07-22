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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.Nonbinding;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.push.PushContext;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.webapp.FacesServlet;
import jakarta.inject.Qualifier;

/**
 * <p class="changed_added_2_3">
 * The presence of this annotation on <span class="changed_modified_4_0">a class</span> deployed within an application 
 * <span class="changed_modified_4_0">guarantees activation of Jakarta Faces and its CDI specific features, even when 
 * <code>/WEB-INF/faces-config.xml</code> is absent and <code>FacesServlet</code> is not explicitly registered</span>.
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
        public Version version() {
            return Version.JSF_2_3;
        }

        @Override
        public boolean alwaysPerformValidationWhenRequiredIsTrue() {
            return false;
        }

        @Override
        public String clientWindowMode() {
            return null;
        }

        @Override
        public String[] configFiles() {
            return null;
        }
        
        // TODO: add the rest :X
        
    }

    /**
     * The Faces spec version
     * 
     * @deprecated It has no effect anymore as per Jakarta Faces version 4.0; the actual impl version should be leading. 
     */
    @Deprecated(forRemoval = true, since = "4.0")
    public static enum Version {

        /**
         * <p class="changed_added_2_3">
         * This value indicates CDI should be used for Jakarta Expression Language resolution as well as enabling Jakarta Server
         * Faces CDI injection, as specified in Section 5.6.3 "CDI for EL Resolution" and Section 5.9 "CDI Integration".
         * </p>
         */
        JSF_2_3
    }

    /**
     * <p class="changed_added_4_0">
     * Enumeration of all available <code>jakarta.faces.*</code> context parameters.
     * </p>
     * 
     * @since 4.0
     */
    public static enum ContextParam {

        /**
         * Returns jakarta.faces.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE as {@link Boolean} with default of <code>false</code>.
         * @see UIInput#ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE
         */
        ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE(UIInput.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE, Boolean.class, false),

        /**
         * Returns jakarta.faces.CLIENT_WINDOW_MODE as {@link String} with default of <code>none</code>.
         * @see ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME
         */
        CLIENT_WINDOW_MODE(ClientWindow.CLIENT_WINDOW_MODE_PARAM_NAME, String.class, "none"),

        /**
         * Returns jakarta.faces.CONFIG_FILES as {@link String} array with default of <code>null</code>.
         * @see FacesServlet#CONFIG_FILES_ATTR
         */
        CONFIG_FILES(FacesServlet.CONFIG_FILES_ATTR, StringArray.COMMA_SEPARATED, null),

        /**
         * Returns jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE as {@link Boolean} with default of <code>false</code>.
         * @see Converter#DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME
         */
        DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE(Converter.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME, Boolean.class, false),

        /**
         * Returns jakarta.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR as {@link Boolean} with default of <code>false</code>.
         * @see BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME
         */
        DISABLE_DEFAULT_BEAN_VALIDATOR(BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME, Boolean.class, false),

        /**
         * Returns jakarta.faces.DISABLE_FACESSERVLET_TO_XHTML as {@link Boolean} with default of <code>false</code>.
         * @see FacesServlet#DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME
         */
        DISABLE_FACESSERVLET_TO_XHTML(FacesServlet.DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns jakarta.faces.validator.ENABLE_VALIDATE_WHOLE_BEAN as {@link Boolean} with default of <code>false</code>.
         * @see BeanValidator#ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME
         */
        ENABLE_VALIDATE_WHOLE_BEAN(BeanValidator.ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns jakarta.faces.ENABLE_WEBSOCKET_ENDPOINT as {@link Boolean} with default of <code>false</code>.
         * @see PushContext#ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME
         */
        ENABLE_WEBSOCKET_ENDPOINT(PushContext.ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns jakarta.faces.FACELETS_BUFFER_SIZE as {@link Integer} with default of <code>1024</code>.
         * @see ViewHandler#FACELETS_BUFFER_SIZE_PARAM_NAME
         */
        FACELETS_BUFFER_SIZE(ViewHandler.FACELETS_BUFFER_SIZE_PARAM_NAME, Integer.class, 1024),

        /** 
         * Returns jakarta.faces.FACELETS_DECORATORS as {@link String} array with default of <code>null</code>.
         * @see ViewHandler#FACELETS_DECORATORS_PARAM_NAME
         */
        FACELETS_DECORATORS(ViewHandler.FACELETS_DECORATORS_PARAM_NAME, StringArray.SEMICOLON_SEPARATED, null),

        /** 
         * Returns jakarta.faces.FACELETS_LIBRARIES as {@link String} array with default of <code>null</code>.
         * @see ViewHandler#FACELETS_LIBRARIES_PARAM_NAME
         */
        FACELETS_LIBRARIES(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, StringArray.SEMICOLON_SEPARATED, null),

        /**
         * Returns jakarta.faces.FACELETS_REFRESH_PERIOD as {@link Integer} with default of <code>2</code>.
         * @see ViewHandler#FACELETS_REFRESH_PERIOD_PARAM_NAME
         */
        FACELETS_REFRESH_PERIOD(ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME, Integer.class, 2),

        /**
         * Returns jakarta.faces.FACELETS_SKIP_COMMENTS as {@link Boolean} with default of <code>false</code>.
         * @see ViewHandler#FACELETS_SKIP_COMMENTS_PARAM_NAME
         */
        FACELETS_SKIP_COMMENTS(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, Boolean.class, false),

        /** 
         * Returns jakarta.faces.FACELETS_SUFFIX as {@link String} with default of {@link ViewHandler#DEFAULT_FACELETS_SUFFIX}.
         * @see ViewHandler#FACELETS_SUFFIX_PARAM_NAME
         */
        FACELETS_SUFFIX(ViewHandler.FACELETS_SUFFIX_PARAM_NAME, String.class, ViewHandler.DEFAULT_FACELETS_SUFFIX),

        /**
         * Returns jakarta.faces.FACELETS_VIEW_MAPPINGS as {@link String} array with default of <code>null</code>.
         * @see ViewHandler#FACELETS_VIEW_MAPPINGS_PARAM_NAME
         */
        FACELETS_VIEW_MAPPINGS(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME, StringArray.SEMICOLON_SEPARATED, null),

        /**
         * Returns jakarta.faces.FULL_STATE_SAVING_VIEW_IDS as {@link String} array with default of <code>null</code>.
         * @see StateManager#FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME
         */
        FULL_STATE_SAVING_VIEW_IDS(StateManager.FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME, StringArray.COMMA_SEPARATED, null),

        /**
         * Returns jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL as {@link Boolean} with default of <code>false</code>.
         * @see UIInput#EMPTY_STRING_AS_NULL_PARAM_NAME
         */
        INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL(UIInput.EMPTY_STRING_AS_NULL_PARAM_NAME, Boolean.class, false),

        /**
         * Returns jakarta.faces.LIFECYCLE_ID as <code>Class&lt;Lifecycle&gt;</code> with default of <code>null</code>.
         * @see FacesServlet#LIFECYCLE_ID_ATTR
         */
        LIFECYCLE_ID(FacesServlet.LIFECYCLE_ID_ATTR, Lifecycle.class, null),

        /**
         * Returns jakarta.faces.PARTIAL_STATE_SAVING as {@link Boolean} with default of <code>true</code>.
         * @see StateManager#PARTIAL_STATE_SAVING_PARAM_NAME
         */
        PARTIAL_STATE_SAVING(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, Boolean.class, true),

        /**
         * Returns jakarta.faces.PROJECT_STAGE as {@link ProjectStage} with default of <code>Production</code>.
         * @see ProjectStage#PROJECT_STAGE_PARAM_NAME
         */
        PROJECT_STAGE(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.class, ProjectStage.Production),

        /**
         * Returns jakarta.faces.RESOURCE_EXCLUDES as {@link String} array with default of {@link ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE}.
         * @see ResourceHandler#RESOURCE_EXCLUDES_PARAM_NAME
         */
        RESOURCE_EXCLUDES(ResourceHandler.RESOURCE_EXCLUDES_PARAM_NAME, StringArray.SPACE_SEPARATED, StringArray.SPACE_SEPARATED.split(ResourceHandler.RESOURCE_EXCLUDES_DEFAULT_VALUE)),

        /**
         * Returns jakarta.faces.SERIALIZE_SERVER_STATE as {@link Boolean} with default of <code>false</code>.
         * @see StateManager#SERIALIZE_SERVER_STATE_PARAM_NAME
         */
        SERIALIZE_SERVER_STATE(StateManager.SERIALIZE_SERVER_STATE_PARAM_NAME, Boolean.class, false),

        /**
         * Returns jakarta.faces.STATE_SAVING_METHOD as {@link String} with default of <code>server</code>.
         * @see StateManager#STATE_SAVING_METHOD_PARAM_NAME
         */
        STATE_SAVING_METHOD(StateManager.STATE_SAVING_METHOD_PARAM_NAME, String.class, "server"),

        /**
         * Returns jakarta.faces.VALIDATE_EMPTY_FIELDS as {@link String} with default of <code>auto</code>.
         * @see UIInput#VALIDATE_EMPTY_FIELDS_PARAM_NAME
         */
        VALIDATE_EMPTY_FIELDS(UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME, String.class, "auto"),

        /**
         * Returns jakarta.faces.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS as {@link Boolean} with default of <code>false</code>.
         * @see UIViewRoot#VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME
         */
        VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS(UIViewRoot.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME, Boolean.class, false),

        /**
         * Returns jakarta.faces.WEBAPP_CONTRACTS_DIRECTORY as {@link Path} with default of <code>/contracts</code>.
         * @see ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME
         */
        WEBAPP_CONTRACTS_DIRECTORY(ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME, Path.class, Paths.get("/contracts")),

        /**
         * Returns jakarta.faces.WEBAPP_RESOURCES_DIRECTORY as {@link Path} with default of <code>/resources</code>.
         * @see ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME
         */
        WEBAPP_RESOURCES_DIRECTORY(ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME, Path.class, Paths.get("/resources")),

        /**
         * Returns jakarta.faces.WEBSOCKET_ENDPOINT_PORT as {@link Integer} with default of <code>null</code>.
         * @see PushContext#WEBSOCKET_ENDPOINT_PORT_PARAM_NAME
         */
        WEBSOCKET_ENDPOINT_PORT(PushContext.WEBSOCKET_ENDPOINT_PORT_PARAM_NAME, Integer.class, null),

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

        private String name;
        private Class<?> type;
        private StringArray separated;
        private Object defaultValue;        

        private <T> ContextParam(String name, Class<T> type, T defaultValue) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        private ContextParam(String name, StringArray separated, String[] defaultValue) {
            this(name, String[].class, defaultValue);
            this.separated = separated;
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
         * <li>{@link String}<code>[]</code>
         * <li>{@link Boolean}
         * <li>{@link Integer}
         * <li>{@link Path}
         * <li>{@link Enum}
         * <li>{@link Class}
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
            return (T) VALUES.computeIfAbsent(this, param -> param.parseValue(context.getExternalContext().getInitParameter(name)));
        }

        @SuppressWarnings("unchecked")
        private <T> T parseValue(String value) {
            if (value == null) {
                return (T) defaultValue;
            }
            else if (type == String.class) {
                return (T) value;
            }
            else if (type == String[].class) {
                return (T) separated.split(value);
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

                throw new IllegalArgumentException(getName() + ": invalid value: " + value);
            }
            else {
                try {
                    return (T) Class.forName(value);
                }
                catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(getName() + ": invalid value: " + value, e);
                }
            }
        }
    }

    /**
     * <p class="changed_added_2_3">
     * The value of this attribute indicates that features corresponding to this version must be enabled for this
     * application.
     * </p>
     *
     * @return the spec version for which the features must be enabled.
     * 
     * @deprecated It has no effect anymore as per Jakarta Faces version 4.0; the actual impl version should be leading. 
     */
    @Nonbinding
    @Deprecated(forRemoval = true, since = "4.0")
    Version version() default Version.JSF_2_3;

    /**
     * <p class="changed_added_4_0">
     * </p>
     *
     * @return 
     */
    @Nonbinding
    boolean alwaysPerformValidationWhenRequiredIsTrue() default false;

    String clientWindowMode() default "none";

    String[] configFiles() default "";

    
}
