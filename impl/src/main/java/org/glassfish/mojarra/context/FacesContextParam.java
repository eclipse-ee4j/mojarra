package org.glassfish.mojarra.context;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.glassfish.mojarra.RIConstants.EMPTY_STRING_ARRAY;

import java.util.Objects;
import java.util.function.Function;

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
import jakarta.faces.component.html.HtmlEvents;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.push.PushContext;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.webapp.FacesServlet;

import org.glassfish.mojarra.config.WebConfiguration;

/**
 * <p class="changed_added_5_0">
 * Enumeration of all available {@code jakarta.faces.*} context parameters.
 * The {@link FacesContextParam#getValue(FacesContext)} can be used to obtain the value of the context parameter.
 * </p>
 * <p>
 * Historical note: this was originally part of FacesConfig.Context param as per https://github.com/jakartaee/faces/issues/1416
 * But it has been reverted as per https://github.com/jakartaee/faces/issues/2013,
 * because it might overlap/conflict the upcoming Jakarta Config and we'd rather not have yet another "dead on arrival" like ManagedBean/CDI in JSF 2.0.
 * </p>
 * 
 * @since 5.0
 */
public enum FacesContextParam implements ContextParam {

    /**
     * Returns {@value HtmlEvents#ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME} as {@link String} array with default of empty string array.
     */
    ADDITIONAL_HTML_EVENT_NAMES(HtmlEvents.ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME, EMPTY_STRING_ARRAY, Separator.SPACE),

    /**
     * Returns {@value UIInput#ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE} as {@link Boolean} with default of {@code false}.
     */
    ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE(UIInput.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE, false),

    /**
     * Returns {@value FacesServlet#AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    AUTOMATIC_EXTENSIONLESS_MAPPING(FacesServlet.AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME, false),

    /**
     * Returns {@value ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME} as {@link String} with default of {@value ClientWindow#CLIENT_WINDOW_MODE_DEFAULT_VALUE}.
     */
    CLIENT_WINDOW_MODE(ClientWindow.CLIENT_WINDOW_MODE_PARAM_NAME, ClientWindow.CLIENT_WINDOW_MODE_DEFAULT_VALUE),

    /**
     * Returns {@value FacesServlet#CONFIG_FILES_ATTR} as {@link String} array with default of empty string array.
     */
    CONFIG_FILES(FacesServlet.CONFIG_FILES_ATTR, EMPTY_STRING_ARRAY, Separator.COMMA),

    /**
     * Returns {@value ResourceHandler#CSP_POLICY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#DEFAULT_CSP_POLICY}.
     */
    CSP_POLICY(ResourceHandler.CSP_POLICY_PARAM_NAME, ResourceHandler.DEFAULT_CSP_POLICY),

    /**
     * Returns {@value Converter#DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE(Converter.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME, false),

    /**
     * Returns {@value BeanValidator#DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    DISABLE_DEFAULT_BEAN_VALIDATOR(BeanValidator.DISABLE_DEFAULT_BEAN_VALIDATOR_PARAM_NAME, false),

    /**
     * Returns {@value FacesServlet#DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    DISABLE_FACESSERVLET_TO_XHTML(FacesServlet.DISABLE_FACESSERVLET_TO_XHTML_PARAM_NAME, false),

    /**
     * Returns {@value ResourceHandler#ENABLE_CSP_NONCE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    ENABLE_CSP_NONCE(ResourceHandler.ENABLE_CSP_NONCE_PARAM_NAME, false),

    /**
     * Returns {@value BeanValidator#ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    ENABLE_VALIDATE_WHOLE_BEAN(BeanValidator.ENABLE_VALIDATE_WHOLE_BEAN_PARAM_NAME, false),

    /**
     * Returns {@value PushContext#ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    ENABLE_WEBSOCKET_ENDPOINT(PushContext.ENABLE_WEBSOCKET_ENDPOINT_PARAM_NAME, false),

    /**
     * Returns {@value ExceptionHandler#EXCEPTION_TYPES_TO_IGNORE_IN_LOGGING_PARAM_NAME} as {@link String} array with default of empty string array.
     */
    EXCEPTION_TYPES_TO_IGNORE_IN_LOGGING(ExceptionHandler.EXCEPTION_TYPES_TO_IGNORE_IN_LOGGING_PARAM_NAME, EMPTY_STRING_ARRAY, Separator.COMMA),

    /**
     * Returns {@value ViewHandler#FACELETS_BUFFER_SIZE_PARAM_NAME} as {@link Integer} with default of {@value ViewHandler#FACELETS_BUFFER_SIZE_DEFAULT_VALUE}.
     */
    FACELETS_BUFFER_SIZE(ViewHandler.FACELETS_BUFFER_SIZE_PARAM_NAME, ViewHandler.FACELETS_BUFFER_SIZE_DEFAULT_VALUE),

    /**
     * Returns {@value ViewHandler#FACELETS_DECORATORS_PARAM_NAME} as {@link String} array with default of empty string array.
     */
    FACELETS_DECORATORS(ViewHandler.FACELETS_DECORATORS_PARAM_NAME, EMPTY_STRING_ARRAY, Separator.SEMICOLON),

    /**
     * Returns {@value ViewHandler#FACELETS_LIBRARIES_PARAM_NAME} as {@link String} array with default of empty string array.
     */
    FACELETS_LIBRARIES(ViewHandler.FACELETS_LIBRARIES_PARAM_NAME, EMPTY_STRING_ARRAY, Separator.SEMICOLON),

    /**
     * Returns {@value ViewHandler#FACELETS_REFRESH_PERIOD_PARAM_NAME} as {@link Integer} with default of {@code -1} when
     * {@link Application#getProjectStage()} is {@link ProjectStage#Production} else default of {@code 0}.
     */
    FACELETS_REFRESH_PERIOD(ViewHandler.FACELETS_REFRESH_PERIOD_PARAM_NAME, Integer.MIN_VALUE, (Function<ProjectStage, Integer>) projectStage -> projectStage == ProjectStage.Production ? -1 : 0),

    /**
     * Returns {@value ViewHandler#FACELETS_SKIP_COMMENTS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    FACELETS_SKIP_COMMENTS(ViewHandler.FACELETS_SKIP_COMMENTS_PARAM_NAME, false),

    /**
     * Returns {@value ViewHandler#FACELETS_SUFFIX_PARAM_NAME} as {@link String} array with default of {@value ViewHandler#DEFAULT_FACELETS_SUFFIX}.
     */
    FACELETS_SUFFIX(ViewHandler.FACELETS_SUFFIX_PARAM_NAME, new String[] { ViewHandler.DEFAULT_FACELETS_SUFFIX }, Separator.SPACE),

    /**
     * Returns {@value ViewHandler#FACELETS_VIEW_MAPPINGS_PARAM_NAME} as {@link String} array with default of empty string array.
     */
    FACELETS_VIEW_MAPPINGS(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME, EMPTY_STRING_ARRAY, Separator.SEMICOLON),

    /**
     * Returns {@value StateManager#FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME} as {@link String} array with default of empty string array.
     * @deprecated Full state saving will be removed in favor of partial state saving in order to keep the spec simple.
     */
    @Deprecated(since = "4.1", forRemoval = true)
    FULL_STATE_SAVING_VIEW_IDS(StateManager.FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME, EMPTY_STRING_ARRAY, Separator.COMMA, Deprecation.DEPRECATED),

    /**
     * Returns {@value UIInput#EMPTY_STRING_AS_NULL_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL(UIInput.EMPTY_STRING_AS_NULL_PARAM_NAME, false),

    /**
     * Returns {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME} as {@link Integer} with default of {@value ClientWindow#NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE}.
     */
    NUMBER_OF_CLIENT_WINDOWS(ClientWindow.NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME, ClientWindow.NUMBER_OF_CLIENT_WINDOWS_DEFAULT_VALUE),

    /**
     * Returns {@value StateManager#PARTIAL_STATE_SAVING_PARAM_NAME} as {@link Boolean} with default of {@code true}.
     * @deprecated Full state saving will be removed in favor of partial state saving in order to keep the spec simple.
     */
    @Deprecated(since = "4.1", forRemoval = true)
    PARTIAL_STATE_SAVING(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, true, Deprecation.DEPRECATED),

    /**
     * Returns {@value ProjectStage#PROJECT_STAGE_PARAM_NAME} as {@link ProjectStage} with default of {@link ProjectStage#Production}.
     * Note that this value can be overridden via JNDI entry {@value ProjectStage#PROJECT_STAGE_JNDI_NAME}.
     */
    PROJECT_STAGE(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.Production),

    /**
     * Returns {@value ResourceHandler#RESOURCE_EXCLUDES_PARAM_NAME} as {@link String} array with default of {@value ResourceHandler#RESOURCE_EXCLUDES_DEFAULT_VALUE}.
     */
    RESOURCE_EXCLUDES(ResourceHandler.RESOURCE_EXCLUDES_PARAM_NAME, ResourceHandler.RESOURCE_EXCLUDES_DEFAULT_VALUE.split(" "), Separator.SPACE),

    /**
     * Returns {@value StateManager#SERIALIZE_SERVER_STATE_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    SERIALIZE_SERVER_STATE(StateManager.SERIALIZE_SERVER_STATE_PARAM_NAME, false),

    /**
     * Returns {@value UINamingContainer#SEPARATOR_CHAR_PARAM_NAME} as {@link Character} with default of {@value NamingContainer#SEPARATOR_CHAR}.
     */
    SEPARATOR_CHAR(UINamingContainer.SEPARATOR_CHAR_PARAM_NAME, NamingContainer.SEPARATOR_CHAR),

    /**
     * Returns {@value StateManager#STATE_SAVING_METHOD_PARAM_NAME} as {@link String} with default of {@link StateSavingMethod#SERVER}.
     * @see StateManager#STATE_SAVING_METHOD_PARAM_NAME
     */
    STATE_SAVING_METHOD(StateManager.STATE_SAVING_METHOD_PARAM_NAME, StateSavingMethod.SERVER),

    /**
     * Returns {@value UIInput#VALIDATE_EMPTY_FIELDS_PARAM_NAME} as {@link String} with default of {@link ValidateEmptyFields#AUTO}.
     */
    VALIDATE_EMPTY_FIELDS(UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME, ValidateEmptyFields.AUTO),

    /**
     * Returns {@value UIViewRoot#VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME} as {@link Boolean} with default of {@code false}.
     */
    VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS(UIViewRoot.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS_PARAM_NAME, false),

    /**
     * Returns {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE}.
     */
    WEBAPP_CONTRACTS_DIRECTORY(ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_PARAM_NAME, ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE),

    /**
     * Returns {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME} as {@link String} with default of {@value ResourceHandler#WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE}.
     */
    WEBAPP_RESOURCES_DIRECTORY(ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME, ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_DEFAULT_VALUE),

    /**
     * Returns {@value PushContext#WEBSOCKET_ENDPOINT_PORT_PARAM_NAME} as {@link Integer} with default of {@code 0} (default 0 means the code will take the port from the request).
     */
    WEBSOCKET_ENDPOINT_PORT(PushContext.WEBSOCKET_ENDPOINT_PORT_PARAM_NAME, 0),

    ;

    private final String name;
    private final Function<ProjectStage, ?> defaultValueSupplier;
    private final Separator separator;
    private final Class<?> type;
    private final boolean deprecated;

    private <T> FacesContextParam(String name, T defaultValue) {
        this(name, defaultValue, null, null, null);
    }

    private <T> FacesContextParam(String name, T defaultValue, Deprecation deprecation) {
        this(name, defaultValue, null, null, deprecation);
    }

    private <T> FacesContextParam(String name, T defaultValue, Separator separator) {
        this(name, defaultValue, null, separator, null);
    }

    private <T> FacesContextParam(String name, T defaultValue, Separator separator, Deprecation deprecation) {
        this(name, defaultValue, null, separator, deprecation);
    }

    private <T> FacesContextParam(String name, T defaultValue, Function<ProjectStage, T> defaultValueSupplier) {
        this(name, defaultValue, defaultValueSupplier, null, null);
    }

    private <T> FacesContextParam(String name, T defaultValue, Function<ProjectStage, T> defaultValueSupplier, Separator separator,
            Deprecation deprecation) {
        requireNonNull(name, "name");
        requireNonNull(defaultValue, "defaultValue");
        this.name = name;
        this.defaultValueSupplier = ofNullable(defaultValueSupplier).orElse($ -> defaultValue);
        this.separator = separator;
        this.type = defaultValue.getClass();
        this.deprecated = deprecation != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Separator getSeparator() {
        return separator;
    }

    @Override
    public boolean isDeprecated() {
        return deprecated;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDefaultValue(ProjectStage projectStage) {
        return (T) defaultValueSupplier.apply(projectStage);
    }

    /**
     * <p>
     * Returns the value of the context parameter, converted to the expected type as indicated by {@link #getType()}.
     * This method never returns {@code null}. When the context parameter is not set, a default value is returned.
     * @param <T> The expected return type.
     * @param context The involved faces context.
     * @return The value of the context parameter, converted to the expected type as indicated by {@link #getType()}.
     * @throws ClassCastException When inferred {@code T} is of wrong type. See {@link #getType()} for the correct type.
     */
    public <T> T getValue(FacesContext context) {
        return WebConfiguration.getInstance(context.getExternalContext()).getValue(this);
    }

    /**
     * <p>
     * Returns {@code true} in case a boolean context parameter is {@code true}, or a non-boolean context parameter is explicitly set with a non-{@code null} value.
     * @param context The involved faces context.
     * @return {@code true} in case a boolean context parameter is {@code true}, or a non-boolean context parameter is explicitly set with a non-{@code null} value.
     */
    public boolean isSet(FacesContext context) {
        return (getType() == Boolean.class) ? (boolean) getValue(context) : WebConfiguration.getInstance(context.getExternalContext()).isSet(this);
    }

    /**
     * <p>
     * Returns {@code true} when the value of the context parameter equals to the default value, irrespective of whether it is explicitly set.
     * @param context The involved faces context.
     * @return {@code true} when the value of the context parameter equals to the default value, irrespective of whether it is explicitly set.
     */
    public boolean isDefault(FacesContext context) {
        WebConfiguration webConfiguration = WebConfiguration.getInstance(context.getExternalContext());
        return Objects.deepEquals(webConfiguration.getValue(this), getDefaultValue(webConfiguration.getProjectStage()));
    }
}
