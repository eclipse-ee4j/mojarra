/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.context;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.glassfish.mojarra.RIConstants.EMPTY_STRING_ARRAY;

import java.util.Optional;
import java.util.function.Function;

import jakarta.faces.application.ProjectStage;

import org.glassfish.mojarra.RIConstants;

/**
 * <p class="changed_added_5_0">
 * Enumeration of all available {@code org.glassfish.mojarra.*} context parameters, which is every parameter this
 * implementation recognizes beyond the {@code jakarta.faces.*} ones declared by {@link FacesContextParam}.
 * A typed accessor such as {@link ContextParam#getString(jakarta.faces.context.FacesContext)} obtains the value of one.
 * </p>
 *
 * <p>
 * Each one also answers to the {@code com.sun.faces.*} prefix it carried before 5.0, which
 * {@link org.glassfish.mojarra.config.WebConfiguration} resolves and reports. What each parameter does, and since when, is documented in
 * {@code CONTEXT-PARAMS.md} in the repository root, which {@code ContextParamsMdTest} holds against this enum.
 * </p>
 *
 * @since 5.0
 */
public enum MojarraContextParam implements ContextParam {

    /**
     * @deprecated Neither setting is coherent, use {@code <h:panelGroup>} or plain markup instead.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    ALLOW_TEXT_CHILDREN("allowTextChildren", false, Deprecation.DEPRECATED),

    /**
     * @deprecated Replaced by {@code org.glassfish.mojarra.viewStateAutocomplete}, which expresses the value rather
     * than one of its settings.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    AUTO_COMPLETE_OFF_ON_VIEW_STATE("autoCompleteOffOnViewState", false, Deprecation.replacedBy("viewStateAutocomplete")),

    ALLOWED_HTTP_METHODS("allowedHttpMethods", EMPTY_STRING_ARRAY, Separator.SPACE),

    /**
     * Whether the last modified timestamp of a resource is remembered rather than read on every request, which only
     * Development has a reason not to do.
     */
    CACHE_RESOURCE_MODIFICATION_TIMESTAMP("cacheResourceModificationTimestamp", true, projectStage -> projectStage != ProjectStage.Development),

    /**
     * How long a client side view stays valid, in seconds, where a negative value means it never expires.
     */
    CLIENT_STATE_TIMEOUT("clientStateTimeout", -1),

    CLIENT_STATE_WRITE_BUFFER_SIZE("clientStateWriteBufferSize", 8192),

    COMPRESSABLE_MIME_TYPES("compressableMimeTypes", EMPTY_STRING_ARRAY, Separator.COMMA),

    COMPRESS_VIEW_STATE("compressViewState", true),

    /**
     * How long a resource may be cached by the client, in milliseconds.
     */
    DEFAULT_RESOURCE_MAX_AGE("defaultResourceMaxAge", 604800000),

    /**
     * @deprecated Replaced by {@link #ENABLE_CLIENT_STATE_DEBUGGING}, which is what it was ever used for.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    DISABLE_CLIENT_STATE_ENCRYPTION("disableClientStateEncryption", false, Deprecation.replacedBy("enableClientStateDebugging")),

    /**
     * Whether the duplicate component id check is skipped, which only Development has a reason to pay for.
     */
    DISABLE_ID_UNIQUENESS_CHECK("disableIdUniquenessCheck", true, projectStage -> projectStage != ProjectStage.Development),

    DISABLE_OPTIONAL_EL_RESOLVER("disableOptionalELResolver", false),

    DISABLE_UNICODE_ESCAPING("disableUnicodeEscaping", Tristate.AUTO),

    DISALLOW_DOCTYPE_DECL("disallowDoctypeDecl", false),

    /**
     * Whether every recognized parameter is reported at startup, which outside Production is worth the log lines.
     */
    DISPLAY_CONFIGURATION("displayConfiguration", false, projectStage -> projectStage != ProjectStage.Production),

    DUPLICATE_JAR_PATTERN("duplicateJARPattern", ""),

    ENABLE_CLIENT_STATE_DEBUGGING("enableClientStateDebugging", false),

    /**
     * Also enabled when {@code web.xml} declares {@code <distributable/>}, which no context parameter can observe.
     *
     * @see org.glassfish.mojarra.config.ConfigureListener
     */
    ENABLE_DISTRIBUTABLE("enableDistributable", false),

    ENABLE_MISSING_RESOURCE_LIBRARY_DETECTION("enableMissingResourceLibraryDetection", false),

    ENABLE_SCRIPTS_IN_ATTRIBUTE_VALUES("enableScriptsInAttributeValues", true),

    ENABLE_TRANSITION_TIME_NO_OP_FLASH("enableTransitionTimeNoOpFlash", false),

    ENABLE_VIEW_STATE_ID_RENDERING("enableViewStateIdRendering", true),

    FORCE_ALWAYS_WRITE_FLASH_COOKIE("forceAlwaysWriteFlashCookie", false),

    FORCE_LOAD_CONFIGURATION("forceLoadConfiguration", false),

    GENERATE_UNIQUE_SERVER_STATE_IDS("generateUniqueServerStateIds", true),

    INJECTION_PROVIDER("injectionProvider", ""),

    NUMBER_OF_ACTIVE_VIEW_MAPS("numberOfActiveViewMaps", 25),

    NUMBER_OF_CONCURRENT_FLASH_USERS("numberOfConcurrentFlashUsers", 5000),

    NUMBER_OF_FLASHES_BETWEEN_FLASH_REAPINGS("numberOfFlashesBetweenFlashReapings", 5000),

    /**
     * @deprecated Renamed to {@code org.glassfish.mojarra.numberOfStatefulPagesPerSession}, since the old name said
     * the opposite of what it sized.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    NUMBER_OF_LOGICAL_VIEWS("numberOfLogicalViews", 15, Deprecation.replacedBy("numberOfStatefulPagesPerSession")),

    NUMBER_OF_STATEFUL_PAGES_PER_SESSION("numberOfStatefulPagesPerSession", 15),

    NUMBER_OF_VIEW_STATES_PER_STATEFUL_PAGE("numberOfViewStatesPerStatefulPage", 15),

    /**
     * @deprecated Renamed to {@code org.glassfish.mojarra.numberOfViewStatesPerStatefulPage}, since the old name said
     * the opposite of what it sized.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    NUMBER_OF_VIEWS_IN_SESSION("numberOfViewsInSession", 15, Deprecation.replacedBy("numberOfViewStatesPerStatefulPage")),

    PREFER_XHTML("preferXHTML", false),

    REFRESH_TRANSIENT_BUILD_ON_PSS("refreshTransientBuildOnPSS", false),

    REGISTER_CONVERTER_PROPERTY_EDITORS("registerConverterPropertyEditors", false),

    RESOURCE_BUFFER_SIZE("resourceBufferSize", 2048),

    /**
     * How many minutes apart a cached resource is checked for modification, where a negative value drops the check.
     * Only Development has a reason to check at all.
     */
    RESOURCE_UPDATE_CHECK_PERIOD("resourceUpdateCheckPeriod", -1, projectStage -> projectStage == ProjectStage.Development ? 5 : -1),

    SEND_POWERED_BY_HEADER("sendPoweredByHeader", false),

    SERIALIZATION_PROVIDER("serializationProvider", ""),

    USE_FACELETS_ID("useFaceletsID", false),

    VIEW_STATE_AUTOCOMPLETE("viewStateAutocomplete", "one-time-code"),

    /**
     * How long a websocket may stay idle before it is closed, in milliseconds, where zero means it never is.
     */
    WEBSOCKET_ENDPOINT_IDLE_TIMEOUT("websocketEndpointIdleTimeout", 0),

    /**
     * How many sessions a websocket channel accepts, where {@link Integer#MAX_VALUE} means it is unbounded.
     */
    WEBSOCKET_MAX_SESSIONS_PER_CHANNEL("websocketMaxSessionsPerChannel", Integer.MAX_VALUE),

    WRITE_STATE_AT_FORM_END("writeStateAtFormEnd", true),

    ;

    private final String name;
    private final Function<ProjectStage, ?> defaultValueSupplier;
    private final Separator separator;
    private final Class<?> type;
    private final boolean deprecated;
    private final String alternateName;
    private final boolean stageDerived;

    private <T> MojarraContextParam(String name, T defaultValue) {
        this(name, defaultValue, null, null, null);
    }

    private <T> MojarraContextParam(String name, T defaultValue, Deprecation deprecation) {
        this(name, defaultValue, null, null, deprecation);
    }

    private <T> MojarraContextParam(String name, T defaultValue, Separator separator) {
        this(name, defaultValue, null, separator, null);
    }

    private <T> MojarraContextParam(String name, T defaultValue, Function<ProjectStage, T> defaultValueSupplier) {
        this(name, defaultValue, defaultValueSupplier, null, null);
    }

    private <T> MojarraContextParam(String name, T defaultValue, Function<ProjectStage, T> defaultValueSupplier, Separator separator,
            Deprecation deprecation) {
        requireNonNull(name, "name");
        requireNonNull(defaultValue, "defaultValue");
        this.name = qualify(name);
        this.defaultValueSupplier = ofNullable(defaultValueSupplier).orElse($ -> defaultValue);
        this.separator = separator;
        this.type = defaultValue.getClass();
        this.deprecated = deprecation != null;
        this.alternateName = deprecation == null || deprecation.alternateName() == null ? null : qualify(deprecation.alternateName());
        this.stageDerived = defaultValueSupplier != null;
    }

    /**
     * <p>
     * A parameter whose default is derived from the project stage also accepts {@link Tristate#AUTO}, which asks for that
     * default rather than pinning a value, and is what such a parameter reads as when it is left alone.
     * </p>
     */
    @Override
    public <T> Optional<T> toValue(String value) {
        if (stageDerived && Tristate.AUTO.name().equalsIgnoreCase(value)) {
            return Optional.empty();
        }

        return ContextParam.super.toValue(value);
    }

    /**
     * Declared unqualified, because the prefix is the same for every one of them and repeating it only invites a typo.
     */
    private static String qualify(String name) {
        return RIConstants.RI_PREFIX + name;
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

    @Override
    public String getAlternateName() {
        return alternateName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDefaultValue(ProjectStage projectStage) {
        return (T) defaultValueSupplier.apply(projectStage);
    }

    /**
     * @param name the qualified name of a parameter.
     * @return the parameter of that name, or <code>null</code> when this enum declares none. Resolving a replacement
     * by name rather than by constant is what lets the constants stay in alphabetical order, since one may name
     * another which is declared after it.
     */
    public static MojarraContextParam of(String name) {
        for (MojarraContextParam param : values()) {
            if (param.getName().equals(name)) {
                return param;
            }
        }

        return null;
    }
}
