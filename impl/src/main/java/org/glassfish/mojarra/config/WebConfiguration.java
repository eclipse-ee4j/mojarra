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

package org.glassfish.mojarra.config;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.logging.Level.FINE;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.application.ApplicationAssociate;
import org.glassfish.mojarra.application.view.FaceletViewHandlingStrategy;
import org.glassfish.mojarra.context.ContextParam;
import org.glassfish.mojarra.context.FacesContextParam;
import org.glassfish.mojarra.context.MojarraContextParam;
import org.glassfish.mojarra.facelets.util.Classpath;
import org.glassfish.mojarra.util.FacesLogger;
import org.glassfish.mojarra.util.MojarraVersion;
import org.glassfish.mojarra.util.Util;

/**
 * Class Documentation
 */
public class WebConfiguration {

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();

    // A Simple regular expression of allowable boolean values
    private static final Pattern ALLOWABLE_BOOLEANS = compile("true|false", CASE_INSENSITIVE);

    /**
     * <p>
     * Maps a specification context parameter to the unqualified name it had while it was Mojarra specific. Both
     * prefixes are accepted for it: the one it actually carried, and the one the 5.0 rename produces for anybody
     * replacing com.sun.faces with org.glassfish.mojarra throughout, which is a name it never had, but is the single
     * most likely wrong spelling in this release.
     * </p>
     *
     * <p>
     * This lives here rather than beside the parameters themselves because it is Mojarra's own history. Those
     * parameters belong to the specification now, and so does the enum which declares them.
     * </p>
     */
    private static final Map<FacesContextParam, String> PROMOTED_PARAMETERS = Map.of(
            FacesContextParam.ENABLE_CSP_NONCE, "enableCspNonce",
            FacesContextParam.CSP_POLICY, "cspPolicy",
            FacesContextParam.EXCEPTION_TYPES_TO_IGNORE_IN_LOGGING, "exceptionTypesToIgnoreInLogging");

    /**
     * The value of a tri-state parameter which leaves the decision to the project stage.
     */
    private static final String AUTO = "auto";

    /**
     * How often, in minutes, a changed resource is noticed while developing.
     */
    private static final long RESOURCE_UPDATE_CHECK_PERIOD_IN_DEVELOPMENT = 5;

    /**
     * Parameters which exist to make debugging easier and would weaken a deployment if honored anywhere else, so outside
     * Development they revert to their default, which is in each case the safe value.
     */
    private static final Set<MojarraContextParam> DEVELOPMENT_ONLY_OPTIONS = EnumSet.of(
            MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING,
            MojarraContextParam.GENERATE_UNIQUE_SERVER_STATE_IDS);

    // Reads better than a bare boolean at the deprecated parameter declarations below.
    private static final boolean DEPRECATED = true;

    private static final String LEGACY_PARAM_PREFIX = "com.sun.faces.";
    private static final String CURRENT_PARAM_PREFIX = "org.glassfish.mojarra.";

    // Key under which we store our WebConfiguration instance.
    private static final String WEB_CONFIG_KEY = "org.glassfish.mojarra.config.WebConfiguration";

    public static final String META_INF_CONTRACTS_DIR = "META-INF/" + ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE;

    private static final int META_INF_CONTRACTS_DIR_LEN = META_INF_CONTRACTS_DIR.length();

    private static final String RESOURCE_CONTRACT_SUFFIX = "/" + ResourceHandler.RESOURCE_CONTRACT_XML;

    private final Level loggingLevel;

    private final Map<WebContextInitParameter, String> contextParameters = new EnumMap<>(WebContextInitParameter.class);

    private final Map<WebContextInitParameter, Map<String, String>> facesConfigParameters = new EnumMap<>(WebContextInitParameter.class);

    private final Map<WebEnvironmentEntry, String> envEntries = new EnumMap<>(WebEnvironmentEntry.class);

    private final Map<ContextParam, Object> resolvedValues = new HashMap<>();

    private final Set<String> setParams = new HashSet<>();

    private final ServletContext servletContext;

    private final ProjectStage projectStage;

    private FaceletsConfiguration faceletsConfig;

    private boolean hasFlows;

    // ------------------------------------------------------------ Constructors

    private WebConfiguration(ServletContext servletContext) {
        this.servletContext = servletContext;

        initSetList();

        // Before the parameters, because displayConfiguration decides at which level they are logged, and some of the
        // others derive their default from the stage.
        if (canProcessJndiEntries()) {
            processJndiEntries();
        }
        projectStage = resolveProjectStage();
        loggingLevel = resolveLoggingLevel();
        logEnvironmentEntries();

        processContextParams();
        processInitParameters();

        processDeprecatedParameters();
        warnAboutUnrecognizedParameters();
    }

    /**
     * <p>
     * Reports every <code>org.glassfish.mojarra.*</code> name the application declared which this release does not
     * recognize, so that a typo or a parameter which has been removed says so rather than being silently ignored.
     * </p>
     *
     * <p>
     * Only Mojarra's own namespace is checked, because it is the only one this release is authoritative about. It also
     * covers the legacy <code>com.sun.faces</code> spelling, which {@link #initSetList(ServletContext)} normalizes into
     * it. This does not run in Production, where nothing can be done about it any more anyway.
     * </p>
     */
    private void warnAboutUnrecognizedParameters() {
        if (projectStage == ProjectStage.Production) {
            return;
        }

        Set<String> recognized = new HashSet<>();

        for (FacesContextParam param : FacesContextParam.values()) {
            recognized.addAll(namesOf(param));
        }

        for (MojarraContextParam param : MojarraContextParam.values()) {
            recognized.addAll(namesOf(param));
        }

        for (WebContextInitParameter param : WebContextInitParameter.values()) {
            recognized.add(param.getQualifiedName());
        }

        setParams.stream()
                 .filter(name -> name.startsWith(CURRENT_PARAM_PREFIX) && !recognized.contains(name))
                 .sorted()
                 .forEach(name -> LOGGER.log(Level.WARNING, "faces.config.webconfig.param.unrecognized", new Object[] { getContextName(), name }));
    }

    /**
     * <p>
     * Reports the JNDI environment entries, which have to be read before the level at which they are reported can be
     * resolved, because one of them decides the project stage which that level derives from.
     * </p>
     */
    private void logEnvironmentEntries() {
        if (LOGGER.isLoggable(loggingLevel)) {
            envEntries.forEach((entry, value) -> LOGGER.log(loggingLevel, "faces.config.webconfig.enventryinfo",
                    new Object[] { getContextName(), entry.getQualifiedName(), value }));
        }
    }

    /**
     * <p>
     * The level at which every recognized parameter is logged, which is informational outside Production so that a
     * deployment can be read back from its own log, and fine grained there so that it stays out of the way. Setting
     * <code>displayConfiguration</code> explicitly overrules that either way, which is what keeps it usable in
     * Production for a deployment whose parameters are substituted at build time.
     * </p>
     *
     * <p>
     * This resolves the tri-state itself rather than through {@link #isOptionEnabled(WebContextInitParameter, boolean)},
     * because it runs before the parameters that one reads are populated.
     * </p>
     */
    private Level resolveLoggingLevel() {
        Level derived = projectStage == ProjectStage.Production ? Level.FINE : Level.INFO;
        String value = getRawInitParameter(WebContextInitParameter.DisplayConfiguration.getQualifiedName());

        if (value == null || AUTO.equalsIgnoreCase(value)) {
            return derived;
        }

        if (!ALLOWABLE_BOOLEANS.matcher(value).matches()) {
            LOGGER.log(Level.WARNING, "faces.config.webconfig.boolconfig.invalidvalue", new Object[] { getContextName(), value,
                    WebContextInitParameter.DisplayConfiguration.getQualifiedName(), "true|false|" + AUTO, AUTO });
            return derived;
        }

        return Boolean.parseBoolean(value) ? Level.INFO : Level.FINE;
    }

    /**
     * <p>
     * The value as declared, under either prefix and without reporting the legacy one, which the regular pass over the
     * parameters does later. This exists because the level at which that pass reports has to be known before it runs.
     * </p>
     */
    private String getRawInitParameter(String qualifiedName) {
        String value = servletContext.getInitParameter(qualifiedName);

        if (value == null) {
            value = servletContext.getInitParameter(LEGACY_PARAM_PREFIX + qualifiedName.substring(CURRENT_PARAM_PREFIX.length()));
        }

        return value == null ? null : value.trim();
    }

    /**
     * <p>
     * Resolves the project stage from its JNDI environment entry, falling back to the context parameter, which is what
     * {@link jakarta.faces.application.Application#getProjectStage()} ends up doing as well.
     * </p>
     */
    private ProjectStage resolveProjectStage() {
        String value = getEnvironmentEntry(WebEnvironmentEntry.ProjectStage);

        if (value == null) {
            value = servletContext.getInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME);
        }

        try {
            // Via the parameter itself, whose conversion matches an enum constant regardless of case.
            return FacesContextParam.PROJECT_STAGE.<ProjectStage>toValue(value).orElse(ProjectStage.Production);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "faces.config.webconfig.boolconfig.invalidvalue", new Object[] { getContextName(), value,
                    ProjectStage.PROJECT_STAGE_PARAM_NAME, Arrays.toString(ProjectStage.values()), ProjectStage.Production });
            return ProjectStage.Production;
        }
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * Return the WebConfiguration instance for this application passing the result of
     * FacesContext.getCurrentInstance().getExternalContext() to
     * {@link #getInstance(jakarta.faces.context.ExternalContext)}.
     *
     * @return the WebConfiguration for this application or <code>null</code> if no FacesContext is available.
     */
    public static WebConfiguration getInstance() {
        return getInstance(FacesContext.getCurrentInstance().getExternalContext());
    }

    /**
     * Return the WebConfiguration instance for this application.
     *
     * @param extContext the ExternalContext for this request
     * @return the WebConfiguration for this application
     */
    public static WebConfiguration getInstance(ExternalContext extContext) {
        WebConfiguration config = (WebConfiguration) extContext.getApplicationMap().get(WEB_CONFIG_KEY);
        if (config == null) {
            return getInstance((ServletContext) extContext.getContext());
        }

        return config;
    }

    /**
     * Return the WebConfiguration instance for this application.
     *
     * @param servletContext the ServletContext
     * @return the WebConfiguration for this application or <code>null</code> if no WebConfiguration could be located
     */
    public static WebConfiguration getInstance(ServletContext servletContext) {
        WebConfiguration webConfig = (WebConfiguration) servletContext.getAttribute(WEB_CONFIG_KEY);

        if (webConfig == null) {
            webConfig = new WebConfiguration(servletContext);
            servletContext.setAttribute(WEB_CONFIG_KEY, webConfig);
        }

        return webConfig;
    }

    public static WebConfiguration getInstanceWithoutCreating(ServletContext servletContext) {
        return (WebConfiguration) servletContext.getAttribute(WEB_CONFIG_KEY);
    }

    /**
     * @return The <code>ServletContext</code> originally used to construct this WebConfiguration instance
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    public boolean isHasFlows() {
        return hasFlows;
    }

    public void setHasFlows(boolean hasFlows) {
        this.hasFlows = hasFlows;
    }

    public String getSpecificationVersion() {
        return MojarraVersion.SPECIFICATION_VERSION;
    }

    /**
     * Obtain the value of the specified parameter
     *
     * @param param the parameter of interest
     * @return the value of the specified parameter
     */
    public String getOptionValue(WebContextInitParameter param) {
        return contextParameters.get(param);
    }

    public void setOptionValue(WebContextInitParameter param, String value) {
        contextParameters.put(param, value);
    }

    /**
     * <p>
     * Overrides what a parameter resolved to, for the one setting which is not expressible as a context parameter:
     * <code>web.xml</code> declaring <code>&lt;distributable/&gt;</code>, which no parameter can observe.
     * </p>
     *
     * @param param the parameter of interest.
     * @param value the value it resolves to from here on.
     */
    public void setValue(ContextParam param, Object value) {
        resolvedValues.put(param, value);
    }

    public FaceletsConfiguration getFaceletsConfiguration() {
        if (faceletsConfig == null) {
            faceletsConfig = new FaceletsConfiguration(this);
        }

        return faceletsConfig;
    }

    public Map<String, String> getFacesConfigOptionValue(WebContextInitParameter param, boolean create) {
        Map<String, String> result = facesConfigParameters.get(param);
        if (result == null) {
            if (create) {
                result = new ConcurrentHashMap<>(3);
                facesConfigParameters.put(param, result);
            } else {
                result = emptyMap();
            }
        }

        return result;
    }

    public Map<String, String> getFacesConfigOptionValue(WebContextInitParameter param) {
        return getFacesConfigOptionValue(param, false);
    }

    /**
     * Obtain the value of the specified env-entry
     *
     * @param entry the env-entry of interest
     * @return the value of the specified env-entry
     */
    public String getEnvironmentEntry(WebEnvironmentEntry entry) {
        return envEntries.get(entry);
    }

    /**
     * @param param the init parameter of interest
     * @return <code>true</code> if the parameter was explicitly set, otherwise, <code>false</code>
     */
    public boolean isSet(WebContextInitParameter param) {
        return isSet(param.getQualifiedName());
    }

    public void overrideContextInitParameter(WebContextInitParameter param, String value) {
        if (param == null || value == null || value.length() == 0) {
            return;
        }

        value = value.trim();
        String oldVal = contextParameters.put(param, value);
        if (oldVal != null && LOGGER.isLoggable(FINE) && !oldVal.equals(value)) {
            LOGGER.log(FINE, "Overriding init parameter {0}.  Changing from {1} to {2}.", new Object[] { param.getQualifiedName(), oldVal, value });
        }
    }

    /**
     * <p>
     * Returns the value to write as the <code>autocomplete</code> attribute of the hidden fields which carry the view
     * state.
     * </p>
     *
     * <p>
     * The deprecated <code>autoCompleteOffOnViewState</code> is honored when the replacement was not set, where
     * <code>true</code> maps to <code>off</code> and <code>false</code> to the default, which is what those two meant
     * before the replacement existed.
     * </p>
     *
     * @return the attribute value.
     */
    public String getViewStateAutocomplete() {
        String value = getOptionValue(WebContextInitParameter.ViewStateAutocomplete);

        if (!isSet(WebContextInitParameter.ViewStateAutocomplete.getQualifiedName())
                && Boolean.TRUE.equals(getValue(MojarraContextParam.AUTO_COMPLETE_OFF_ON_VIEW_STATE))) {
            value = "off";
        }

        return value;
    }

    /**
     * <p>
     * Returns the value a context parameter resolved to for this application, in the type it declares. The resolution
     * happens once, while this configuration is being read, so that a parameter costs a lookup rather than a parse
     * wherever it is consulted.
     * </p>
     *
     * @param <T> the expected type of the value.
     * @param param the parameter of interest.
     * @return the value of the parameter, which is its default when it was not declared.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(ContextParam param) {
        return (T) resolvedValues.get(param);
    }

    /**
     * @param param the boolean parameter of interest.
     * @return whether it resolved to <code>true</code>.
     */
    public boolean isEnabled(ContextParam param) {
        return Boolean.TRUE.equals(resolvedValues.get(param));
    }

    /**
     * @param param the parameter of interest.
     * @return <code>true</code> when the parameter was explicitly declared, under any of the names it answers to.
     */
    public boolean isSet(ContextParam param) {
        return namesOf(param).stream().anyMatch(this::isSet);
    }

    /**
     * <p>
     * Every name a parameter answers to as an application may spell it, most preferred first: the one it declares,
     * followed by the ones it only still answers to for compatibility. A Mojarra parameter accepts the
     * <code>com.sun.faces</code> prefix it carried before 5.0. A specification parameter which used to be a Mojarra one
     * accepts the unqualified name it had then, under both prefixes: the one it actually carried, and the one the 5.0
     * rename produces for anybody replacing <code>com.sun.faces</code> with <code>org.glassfish.mojarra</code>
     * throughout, which is a name it never had, but is the single most likely wrong spelling in this release.
     * </p>
     */
    private static List<String> declaredNamesOf(ContextParam param) {
        String name = param.getName();
        String promoted = PROMOTED_PARAMETERS.get(param);

        if (promoted != null) {
            return List.of(name, LEGACY_PARAM_PREFIX + promoted, CURRENT_PARAM_PREFIX + promoted);
        }

        if (name.startsWith(CURRENT_PARAM_PREFIX)) {
            return List.of(name, LEGACY_PARAM_PREFIX + name.substring(CURRENT_PARAM_PREFIX.length()));
        }

        return List.of(name);
    }

    /**
     * @return the same names as {@link #declaredNamesOf(ContextParam)}, in the spelling
     * {@link #initSetList(ServletContext)} records them under and {@link #isSet(String)} therefore answers to.
     */
    private static List<String> namesOf(ContextParam param) {
        return declaredNamesOf(param).stream().map(WebConfiguration::normalize).distinct().collect(toList());
    }

    private static String normalize(String name) {
        return name.startsWith(LEGACY_PARAM_PREFIX) ? CURRENT_PARAM_PREFIX + name.substring(LEGACY_PARAM_PREFIX.length()) : name;
    }

    /**
     * <p>
     * Resolves every context parameter to the value this application will see, once, so that consulting one costs a
     * lookup rather than a parse. A value which cannot be converted to the type the parameter declares is reported and
     * behaves as though the parameter was never declared, rather than as whatever a lenient parse happens to make of
     * it, which for a boolean would silently be <code>false</code>.
     * </p>
     */
    private void processContextParams() {
        for (FacesContextParam param : FacesContextParam.values()) {
            resolvedValues.put(param, resolve(param));
        }

        for (MojarraContextParam param : MojarraContextParam.values()) {
            resolvedValues.put(param, resolve(param));
        }

        // Resolved ahead of the others, because its JNDI environment entry outranks the parameter and the defaults of
        // the others derive from it.
        resolvedValues.put(FacesContextParam.PROJECT_STAGE, projectStage);
    }

    private Object resolve(ContextParam param) {
        String value = getDeclaredValue(param);

        try {
            Optional<?> resolved = param.toValue(value);

            if (resolved.isPresent()) {
                if (LOGGER.isLoggable(loggingLevel)) {
                    LOGGER.log(loggingLevel, "faces.config.webconfig.configinfo", new Object[] { getContextName(), param.getName(), value });
                }

                return resolved.get();
            }
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "faces.config.webconfig.boolconfig.invalidvalue",
                    new Object[] { getContextName(), value, param.getName(), allowedValuesOf(param), param.getDefaultValue(projectStage) });
        }

        return param.getDefaultValue(projectStage);
    }

    /**
     * @return the value the application declared for the parameter, under whichever of its names it used, reporting the
     * ones which only exist for compatibility.
     */
    private String getDeclaredValue(ContextParam param) {
        for (String name : declaredNamesOf(param)) {
            String value = servletContext.getInitParameter(name);

            if (value != null) {
                if (!name.equals(param.getName())) {
                    LOGGER.log(Level.WARNING, "faces.config.webconfig.param.deprecated", new Object[] { getContextName(), name, param.getName() });
                }

                return value.trim();
            }
        }

        return null;
    }

    /**
     * @return what the parameter accepts, for the benefit of whoever has to correct the value which was rejected.
     */
    private static String allowedValuesOf(ContextParam param) {
        Class<?> type = param.getType();

        if (type == Boolean.class) {
            return "true|false";
        }

        if (type.isEnum()) {
            return stream(type.getEnumConstants()).map(Object::toString).collect(joining("|"));
        }

        return type.getSimpleName();
    }

    /**
     * <p>
     * Whether the last modified timestamp of a resource is remembered rather than read on every request. The stage
     * decides it unless the parameter says otherwise, since a resource which changed on disk has to be noticed while
     * developing and cannot change under a deployed application without a redeploy.
     * </p>
     *
     * @return whether the timestamp is cached.
     */
    public boolean isResourceModificationTimestampCached() {
        return isOptionEnabled(WebContextInitParameter.CacheResourceModificationTimestamp, projectStage != ProjectStage.Development);
    }

    /**
     * <p>
     * How many minutes apart a cached resource is checked for modification, where <code>-1</code> drops the check. The
     * stage decides it unless the parameter says otherwise, for the same reason.
     * </p>
     *
     * @return the period in minutes.
     */
    public long getResourceUpdateCheckPeriod() {
        String value = getOptionValue(WebContextInitParameter.ResourceUpdateCheckPeriod);
        long whenAuto = projectStage == ProjectStage.Development ? RESOURCE_UPDATE_CHECK_PERIOD_IN_DEVELOPMENT : -1;

        if (value == null || AUTO.equalsIgnoreCase(value.trim())) {
            return whenAuto;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            warnAboutUnusableValue(WebContextInitParameter.ResourceUpdateCheckPeriod, value, "a number or " + AUTO);
            return whenAuto;
        }
    }

    /**
     * <p>
     * Resolves a tri-state parameter, whose value is <code>true</code>, <code>false</code> or <code>auto</code>. An
     * unusable value is reported and behaves as though the parameter was never set, rather than as <code>false</code>,
     * which is what a bare {@link Boolean#parseBoolean(String)} would silently make of a typo.
     * </p>
     *
     * @param param the parameter of interest.
     * @param whenAuto what <code>auto</code> means for it.
     * @return whether the option is enabled.
     */
    public boolean isOptionEnabled(WebContextInitParameter param, boolean whenAuto) {
        String value = getTriStateValue(param);

        return value == null ? whenAuto : Boolean.parseBoolean(value);
    }

    /**
     * @return the trimmed value of a tri-state parameter, or <code>null</code> when it says <code>auto</code> or
     * says something which cannot be used, in which case that is reported first.
     */
    private String getTriStateValue(WebContextInitParameter param) {
        String value = getOptionValue(param);

        if (value == null || AUTO.equalsIgnoreCase(value.trim())) {
            return null;
        }

        value = value.trim();

        if (!ALLOWABLE_BOOLEANS.matcher(value).matches()) {
            warnAboutUnusableValue(param, value, "true|false|" + AUTO);
            return null;
        }

        return value;
    }

    private void warnAboutUnusableValue(WebContextInitParameter param, String value, String allowed) {
        LOGGER.log(Level.WARNING, "faces.config.webconfig.boolconfig.invalidvalue",
                new Object[] { getContextName(), value, param.getQualifiedName(), allowed, AUTO });
    }

    /**
     * @return the name every message about this configuration reports the application under.
     */
    private String getContextName() {
        return servletContext.getContextPath();
    }

    public ProjectStage getProjectStage() {
        return projectStage;
    }

    public void doPostBringupActions() {
        processDevelopmentOnlyParameters();
        discoverResourceLibraryContracts();
    }

    /**
     * <p>
     * Reverts every development only parameter which was explicitly set away from its default to that default, unless
     * the project stage is Development, and says so. Silently ignoring a setting which weakens the deployment would be
     * worse than honoring it, so the warning matters as much as the gating.
     * </p>
     *
     * <p>
     * This runs after bringup rather than during construction because it needs the project stage, and therefore a
     * FacesContext, which does not exist yet while the parameters are being read.
     * </p>
     */
    void processDevelopmentOnlyParameters() {
        ProjectStage projectStage = getProjectStage();

        if (projectStage == ProjectStage.Development) {
            return;
        }

        for (MojarraContextParam param : DEVELOPMENT_ONLY_OPTIONS) {
            Object defaultValue = param.getDefaultValue(projectStage);

            if (!defaultValue.equals(getValue(param))) {
                LOGGER.log(Level.WARNING, "faces.config.webconfig.param.development_only",
                        new Object[] { getContextName(), param.getName(), projectStage, defaultValue });

                resolvedValues.put(param, defaultValue);
            }
        }
    }

    private void discoverResourceLibraryContracts() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext extContex = context.getExternalContext();
        Set<String> foundContracts = new HashSet<>();
        Set<String> candidates;

        // Scan for "contractMappings" in the web app root
        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        String contractsDirName = associate.getResourceManager().getBaseContractsPath();
        assert null != contractsDirName;
        candidates = extContex.getResourcePaths(contractsDirName);
        if (null != candidates) {
            int contractsDirNameLen = contractsDirName.length();
            int end;
            for (String cur : candidates) {
                end = cur.length();
                if (cur.endsWith("/")) {
                    end--;
                }
                foundContracts.add(cur.substring(contractsDirNameLen + 1, end));
            }
        }

        // Scan for "META-INF" contractMappings in the classpath
        try {
            URL[] candidateURLs = Classpath.search(Util.getCurrentLoader(this), META_INF_CONTRACTS_DIR, RESOURCE_CONTRACT_SUFFIX,
                    Classpath.SearchAdvice.AllMatches);
            for (URL curURL : candidateURLs) {
                String cur = curURL.toExternalForm();

                int i = cur.indexOf(META_INF_CONTRACTS_DIR) + META_INF_CONTRACTS_DIR_LEN + 1;
                int j = cur.indexOf(RESOURCE_CONTRACT_SUFFIX);
                if (i < j) {
                    foundContracts.add(cur.substring(i, j));
                }

            }
        } catch (IOException ioe) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Unable to scan " + META_INF_CONTRACTS_DIR, ioe);
            }
        }

        if (foundContracts.isEmpty()) {
            return;
        }

        Map<String, List<String>> contractMappings = new HashMap<>();

        Map<String, List<String>> contractsFromConfig = associate.getResourceLibraryContracts();
        List<String> contractsToExpose;

        if (null != contractsFromConfig && !contractsFromConfig.isEmpty()) {
            List<String> contractsFromMapping;
            for (Map.Entry<String, List<String>> cur : contractsFromConfig.entrySet()) {
                // Verify that the contractsToExpose in this mapping actually exist
                // in the application. If not, log a message.
                contractsFromMapping = cur.getValue();
                if (null == contractsFromMapping || contractsFromMapping.isEmpty()) {
                    if (LOGGER.isLoggable(Level.CONFIG)) {
                        LOGGER.log(Level.CONFIG, "resource library contract mapping for pattern {0} has no contracts.", cur.getKey());
                    }
                } else {
                    contractsToExpose = new ArrayList<>();
                    for (String curContractFromMapping : contractsFromMapping) {
                        if (foundContracts.contains(curContractFromMapping)) {
                            contractsToExpose.add(curContractFromMapping);
                        } else {
                            if (LOGGER.isLoggable(Level.CONFIG)) {
                                LOGGER.log(Level.CONFIG,
                                        "resource library contract mapping for pattern {0} exposes contract {1}, but that contract is not available to the application.",
                                        new String[] { cur.getKey(), curContractFromMapping });
                            }
                        }
                    }
                    if (!contractsToExpose.isEmpty()) {
                        contractMappings.put(cur.getKey(), contractsToExpose);
                    }
                }
            }
        } else {
            contractsToExpose = new ArrayList<>(foundContracts);
            contractMappings.put("*", contractsToExpose);
        }
        extContex.getApplicationMap().put(FaceletViewHandlingStrategy.RESOURCE_LIBRARY_CONTRACT_DATA_STRUCTURE_KEY, contractMappings);

    }

    // ------------------------------------------------- Package Private Methods

    /**
     * <p>
     * Discards the configuration read for the given context, so that the next reader reads it afresh. A container fixes
     * its context parameters before anything can read them, so this exists for the two moments which are not that:
     * bringup, which reads a provisional configuration before the application is known, and a test which declares a
     * parameter on a context it has already handed out.
     * </p>
     */
    public static void clear(ServletContext servletContext) {
        servletContext.removeAttribute(WEB_CONFIG_KEY);
    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Warn about every deprecated context initialization parameter which was explicitly set, and carry the value of each
     * one over to the parameter which replaces it, unless that one was explicitly set as well.
     * </p>
     *
     */
    private static List<ContextParam> deprecatedParams() {
        return Stream.of(Stream.of(FacesContextParam.values()), Stream.of(MojarraContextParam.values()))
                .flatMap(params -> params.filter(ContextParam::isDeprecated))
                .collect(toList());
    }

    private void processDeprecatedParameters() {

        for (ContextParam param : deprecatedParams()) {
            if (!isSet(param)) {
                continue;
            }

            warnAboutDeprecatedParameter(param.getName(), param.getAlternateName());
            MojarraContextParam alternate = MojarraContextParam.of(param.getAlternateName());

            if (alternate != null && !isSet(alternate)) {
                resolvedValues.put(alternate, resolvedValues.get(param));
            }
        }

        for (WebContextInitParameter param : WebContextInitParameter.values()) {
            if (!param.isDeprecated() || !isSet(param.getQualifiedName())) {
                continue;
            }

            WebContextInitParameter alternate = param.getAlternate();
            warnAboutDeprecatedParameter(param.getQualifiedName(), alternate == null ? null : alternate.getQualifiedName());

            if (alternate != null && !isSet(alternate.getQualifiedName())) {
                contextParameters.put(alternate, contextParameters.get(param));
            }
        }

    }

    /**
     * <p>
     * The warning is deliberately not gated on the project stage. It announces a change in the runtime rather than a
     * mistake in the application, and an application which is going to break on the next upgrade needs to hear about that
     * in production too.
     * </p>
     *
     * @param alternateName the qualified name of the replacement, or <code>null</code> when there is no replacement.
     */
    private void warnAboutDeprecatedParameter(String qualifiedName, String alternateName) {

        if (alternateName == null) {
            LOGGER.log(Level.WARNING, "faces.config.webconfig.param.deprecated.no_replacement", new Object[] { getContextName(), qualifiedName });
        } else {
            LOGGER.log(Level.WARNING, "faces.config.webconfig.param.deprecated", new Object[] { getContextName(), qualifiedName, alternateName });
        }
    }

    /**
     * Adds all org.glassfish.mojarra init parameter names to a list. This allows callers to determine if a parameter was explicitly
     * set.
     *
     */
    private void initSetList() {
        for (Enumeration<String> e = servletContext.getInitParameterNames(); e.hasMoreElements();) {
            String name = e.nextElement();
            if (name.startsWith(CURRENT_PARAM_PREFIX) || name.startsWith("jakarta.faces")) {
                setParams.add(name);
            } else if (name.startsWith(LEGACY_PARAM_PREFIX)) {
                setParams.add(CURRENT_PARAM_PREFIX + name.substring(LEGACY_PARAM_PREFIX.length()));
            }
        }
    }

    /**
     * Returns the init parameter value for the given qualified name, falling back to the legacy {@code com.sun.faces.*} equivalent
     * if the current {@code org.glassfish.mojarra.*} name is not set. Logs a deprecation warning when the legacy name is used.
     */
    private String getInitParameter(String qualifiedName) {
        String value = servletContext.getInitParameter(qualifiedName);

        if (value == null && qualifiedName.startsWith(CURRENT_PARAM_PREFIX)) {
            String legacyName = LEGACY_PARAM_PREFIX + qualifiedName.substring(CURRENT_PARAM_PREFIX.length());
            value = servletContext.getInitParameter(legacyName);

            if (value != null && LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "faces.config.webconfig.param.deprecated", new Object[] { getContextName(), legacyName, qualifiedName });
            }
        }

        return value;
    }

    /**
     * @param name the param name
     * @return <code>true</code> if the name was explicitly specified
     */
    private boolean isSet(String name) {
        return setParams.contains(name);
    }

    /**
     * <p>
     * Process all non-boolean context initialization parameters.
     * </p>
     *
     */
    private void processInitParameters() {

        for (WebContextInitParameter param : WebContextInitParameter.values()) {
            String value = getInitParameter(param.getQualifiedName());

            if (value == null || value.isEmpty()) {
                value = param.getDefaultValue();
            }
            if (value == null || value.isEmpty()) {
                continue;
            }

            if (LOGGER.isLoggable(loggingLevel)) {
                LOGGER.log(loggingLevel, "faces.config.webconfig.configinfo", new Object[] { getContextName(), param.getQualifiedName(), value });
            }
            contextParameters.put(param, value);
        }

    }

    /**
     * <p>
     * Process all JNDI entries.
     * </p>
     *
     */
    private void processJndiEntries() {
        Context initialContext = null;

        try {
            initialContext = new InitialContext();
        } catch (NoClassDefFoundError nde) {
            // On google app engine InitialContext is forbidden to use and GAE throws NoClassDefFoundError
            LOGGER.log(FINE, nde, nde::toString);
        } catch (NamingException ne) {
            LOGGER.log(Level.WARNING, ne, ne::toString);
        }

        if (initialContext != null) {
            // process environment entries
            for (WebEnvironmentEntry entry : WebEnvironmentEntry.values()) {
                String entryName = entry.getQualifiedName();
                String value = null;

                try {
                    value = (String) initialContext.lookup(entryName);
                } catch (NamingException root) {
                    LOGGER.log(Level.FINE, root::toString);
                }

                if (value != null) {
                    envEntries.put(entry, value);
                }
            }
        }
    }

    public boolean canProcessJndiEntries() {
        try {
            Util.getCurrentLoader(this).loadClass("javax.naming.InitialContext");
        } catch (Exception e) {
            LOGGER.fine("javax.naming is unavailable. JNDI entries related to Mojarra configuration will not be processed.");
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------- Enums

    /**
     * <p>
     * An <code>enum</code> of all non-boolean context initalization parameters recognized by the implementation.
     * </p>
     */
    public enum WebContextInitParameter {

        NumberOfStatefulPagesPerSession("org.glassfish.mojarra.numberOfStatefulPagesPerSession", "15"),
        NumberOfViewStatesPerStatefulPage("org.glassfish.mojarra.numberOfViewStatesPerStatefulPage", "15"),
        NumberOfLogicalViewsDeprecated("org.glassfish.mojarra.numberOfLogicalViews", "15", NumberOfStatefulPagesPerSession),
        NumberOfViewsInSessionDeprecated("org.glassfish.mojarra.numberOfViewsInSession", "15", NumberOfViewStatesPerStatefulPage),
        NumberOfActiveViewMaps("org.glassfish.mojarra.numberOfActiveViewMaps", "25"),
        NumberOfConcurrentFlashUsers("org.glassfish.mojarra.numberOfConcurrentFlashUsers", "5000"),
        NumberOfFlashesBetweenFlashReapings("org.glassfish.mojarra.numberOfFlashesBetweenFlashReapings", "5000"),
        InjectionProviderClass("org.glassfish.mojarra.injectionProvider", ""),
        SerializationProviderClass("org.glassfish.mojarra.serializationProvider", ""),
        ClientStateWriteBufferSize("org.glassfish.mojarra.clientStateWriteBufferSize", "8192"),
        ResourceBufferSize("org.glassfish.mojarra.resourceBufferSize", "2048"),
        ClientStateTimeout("org.glassfish.mojarra.clientStateTimeout", ""),
        DefaultResourceMaxAge("org.glassfish.mojarra.defaultResourceMaxAge", "604800000"), // 7 days
        ResourceUpdateCheckPeriod("org.glassfish.mojarra.resourceUpdateCheckPeriod", AUTO), // in minutes; -1 disables the check, auto leaves it to the stage
        CacheResourceModificationTimestamp("org.glassfish.mojarra.cacheResourceModificationTimestamp", AUTO), // true|false|auto
        CompressableMimeTypes("org.glassfish.mojarra.compressableMimeTypes", ""),
        DisableUnicodeEscaping("org.glassfish.mojarra.disableUnicodeEscaping", "auto"),
        DisableIdUniquenessCheck("org.glassfish.mojarra.disableIdUniquenessCheck", AUTO), // true|false|auto
        DisplayConfiguration("org.glassfish.mojarra.displayConfiguration", AUTO), // true|false|auto
        DuplicateJARPattern("org.glassfish.mojarra.duplicateJARPattern", ""),
        AllowedHttpMethods("org.glassfish.mojarra.allowedHttpMethods", ""), // white space separated, upper case; * means allow all
        ViewStateAutocomplete("org.glassfish.mojarra.viewStateAutocomplete", "one-time-code"),

        FaceletsProcessingFileExtensionProcessAs("", ""),
        WebsocketEndpointIdleTimeout("org.glassfish.mojarra.websocketEndpointIdleTimeout", "0"), // in milliseconds; 0 means no timeout
        WebsocketMaxSessionsPerChannel("org.glassfish.mojarra.websocketMaxSessionsPerChannel", ""), // empty means unbounded
        ;

        private final String qualifiedName;
        private final String defaultValue;
        private final WebContextInitParameter alternate;
        private final boolean deprecated;

        public String getQualifiedName() {
            return qualifiedName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        /**
         * @return the parameter which replaces this one, or <code>null</code> when there is no replacement.
         */
        public WebContextInitParameter getAlternate() {
            return alternate;
        }

        public boolean isDeprecated() {
            return deprecated;
        }

        WebContextInitParameter(String qualifiedName, String defaultValue) {
            this(qualifiedName, defaultValue, null, false);
        }

        WebContextInitParameter(String qualifiedName, String defaultValue, WebContextInitParameter alternate) {
            this(qualifiedName, defaultValue, alternate, true);
        }

        WebContextInitParameter(String qualifiedName, String defaultValue, boolean deprecated) {
            this(qualifiedName, defaultValue, null, deprecated);
        }

        private WebContextInitParameter(String qualifiedName, String defaultValue, WebContextInitParameter alternate, boolean deprecated) {
            this.qualifiedName = qualifiedName;
            this.defaultValue = defaultValue;
            this.alternate = alternate;
            this.deprecated = deprecated;
        }

    }

    /**
     * <p>
     * An <code>enum</code> of all environment entries (specified in the web.xml) recognized by the implemenetation.
     * </p>
     */
    public enum WebEnvironmentEntry {

        ProjectStage(jakarta.faces.application.ProjectStage.PROJECT_STAGE_JNDI_NAME);

        private static final String JNDI_PREFIX = "java:comp/env/";
        private final String qualifiedName;

        // ---------------------------------------------------------- Public Methods

        public String getQualifiedName() {

            return qualifiedName;

        }

        // ------------------------------------------------- Package Private Methods

        WebEnvironmentEntry(String qualifiedName) {

            if (qualifiedName.startsWith(JNDI_PREFIX)) {
                this.qualifiedName = qualifiedName;
            } else {
                this.qualifiedName = JNDI_PREFIX + qualifiedName;
            }

        }

    }

    /**
     * <p>
     * An <code>enum</code> of all possible values for the <code>disableUnicodeEscaping</code> configuration parameter.
     * </p>
     */
    public enum DisableUnicodeEscaping {
        True("true"), False("false"), Auto("auto");

        private final String value;

        DisableUnicodeEscaping(String value) {
            this.value = value;
        }

        public static DisableUnicodeEscaping getByValue(String value) {
            for (DisableUnicodeEscaping disableUnicodeEscaping : DisableUnicodeEscaping.values()) {
                if (disableUnicodeEscaping.value.equals(value)) {
                    return disableUnicodeEscaping;
                }
            }

            return null;
        }
    }

} // END WebConfiguration
