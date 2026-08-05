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

import java.util.function.Function;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;

import org.glassfish.mojarra.config.WebConfiguration;

/**
 * <p class="changed_added_5_0">
 * Enumeration of all available {@code org.glassfish.mojarra.*} context parameters, which is every parameter this
 * implementation recognizes beyond the {@code jakarta.faces.*} ones declared by {@link FacesContextParam}.
 * {@link #getValue(FacesContext)} obtains the value of one.
 * </p>
 *
 * <p>
 * Each one also answers to the {@code com.sun.faces.*} prefix it carried before 5.0, which
 * {@link WebConfiguration} resolves and reports. What each parameter does, and since when, is documented in
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

    COMPRESS_VIEW_STATE("compressViewState", true),

    /**
     * @deprecated Replaced by {@link #ENABLE_CLIENT_STATE_DEBUGGING}, which is what it was ever used for.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    DISABLE_CLIENT_STATE_ENCRYPTION("disableClientStateEncryption", false, Deprecation.replacedBy("enableClientStateDebugging")),

    DISABLE_OPTIONAL_EL_RESOLVER("disableOptionalELResolver", false),

    DISALLOW_DOCTYPE_DECL("disallowDoctypeDecl", false),

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

    PREFER_XHTML("preferXHTML", false),

    REFRESH_TRANSIENT_BUILD_ON_PSS("refreshTransientBuildOnPSS", false),

    REGISTER_CONVERTER_PROPERTY_EDITORS("registerConverterPropertyEditors", false),

    SEND_POWERED_BY_HEADER("sendPoweredByHeader", false),

    USE_FACELETS_ID("useFaceletsID", false),

    WRITE_STATE_AT_FORM_END("writeStateAtFormEnd", true),

    ;

    private static final String PREFIX = "org.glassfish.mojarra.";

    private final String name;
    private final Function<ProjectStage, ?> defaultValueSupplier;
    private final Separator separator;
    private final Class<?> type;
    private final boolean deprecated;
    private final String alternateName;

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
    }

    /**
     * Declared unqualified, because the prefix is the same for every one of them and repeating it only invites a typo.
     */
    private static String qualify(String name) {
        return PREFIX + name;
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
     * @param <T> the expected return type.
     * @param context the involved faces context.
     * @return the value of the context parameter, in the type indicated by {@link #getType()}, which is its default
     * when it was not declared.
     */
    public <T> T getValue(FacesContext context) {
        return WebConfiguration.getInstance(context.getExternalContext()).getValue(this);
    }

    /**
     * @param context the involved faces context.
     * @return whether a boolean context parameter resolved to <code>true</code>.
     */
    public boolean isEnabled(FacesContext context) {
        return WebConfiguration.getInstance(context.getExternalContext()).isEnabled(this);
    }

    /**
     * @param context the involved faces context.
     * @return whether the context parameter was explicitly declared, under any of the names it answers to.
     */
    public boolean isSet(FacesContext context) {
        return WebConfiguration.getInstance(context.getExternalContext()).isSet(this);
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
