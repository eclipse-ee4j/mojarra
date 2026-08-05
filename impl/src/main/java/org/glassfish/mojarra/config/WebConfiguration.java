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
import java.util.stream.Stream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.RIConstants;
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
     * Parameters which exist to make debugging easier and would weaken a deployment if honored anywhere else, so outside
     * Development they revert to their default, which is in each case the safe value.
     */
    private static final Set<MojarraContextParam> DEVELOPMENT_ONLY_OPTIONS = EnumSet.of(
            MojarraContextParam.ENABLE_CLIENT_STATE_DEBUGGING,
            MojarraContextParam.GENERATE_UNIQUE_SERVER_STATE_IDS);

    private static final String LEGACY_PARAM_PREFIX = "com.sun.faces.";
    private static final String CURRENT_PARAM_PREFIX = RIConstants.RI_PREFIX;

    // Key under which we store our WebConfiguration instance.
    private static final String WEB_CONFIG_KEY = "org.glassfish.mojarra.config.WebConfiguration";

    public static final String META_INF_CONTRACTS_DIR = "META-INF/" + ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE;

    private static final int META_INF_CONTRACTS_DIR_LEN = META_INF_CONTRACTS_DIR.length();

    private static final String RESOURCE_CONTRACT_SUFFIX = "/" + ResourceHandler.RESOURCE_CONTRACT_XML;

    private final Level loggingLevel;

    private final Map<String, Map<String, String>> facesConfigParameters = new HashMap<>();

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
     * </p>
     */
    private Level resolveLoggingLevel() {
        return resolve(MojarraContextParam.DISPLAY_CONFIGURATION) == Boolean.TRUE ? Level.INFO : Level.FINE;
    }

    /**
     * <p>
     * The value as declared, under either prefix and without reporting the legacy one, which the regular pass over the
     * parameters does later. This exists because the level at which that pass reports has to be known before it runs.
     * </p>
     */
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
        return getInstance((ServletContext) extContext.getContext());
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

    public Map<String, String> getFacesConfigOptionValue(String name, boolean create) {
        Map<String, String> result = facesConfigParameters.get(name);
        if (result == null) {
            if (create) {
                result = new ConcurrentHashMap<>(3);
                facesConfigParameters.put(name, result);
            } else {
                result = emptyMap();
            }
        }

        return result;
    }

    public Map<String, String> getFacesConfigOptionValue(String name) {
        return getFacesConfigOptionValue(name, false);
    }

    /**
     * The one <code>faces-config.xml</code> supplied mapping, which is not a context parameter and only ever needed a
     * key.
     */
    public static final String FACELETS_PROCESSING_FILE_EXTENSION_PROCESS_AS = "faceletsProcessingFileExtensionProcessAs";

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
        if (!isSet(MojarraContextParam.VIEW_STATE_AUTOCOMPLETE) && isEnabled(MojarraContextParam.AUTO_COMPLETE_OFF_ON_VIEW_STATE)) {
            return "off";
        }

        return getValue(MojarraContextParam.VIEW_STATE_AUTOCOMPLETE);
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
        return isEnabled(MojarraContextParam.CACHE_RESOURCE_MODIFICATION_TIMESTAMP);
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
        return this.<Integer>getValue(MojarraContextParam.RESOURCE_UPDATE_CHECK_PERIOD);
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
    /**
     * @return the trimmed value of a tri-state parameter, or <code>null</code> when it says <code>auto</code> or
     * says something which cannot be used, in which case that is reported first.
     */
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

            if (alternate != null && alternate.getType() == param.getType() && !isSet(alternate)) {
                resolvedValues.put(alternate, resolvedValues.get(param));
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

} // END WebConfiguration
