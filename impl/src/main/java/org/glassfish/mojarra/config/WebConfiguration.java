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

import static java.util.Collections.emptyMap;
import static java.util.logging.Level.FINE;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.glassfish.mojarra.util.Util.split;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.servlet.ServletContext;

import org.glassfish.mojarra.application.ApplicationAssociate;
import org.glassfish.mojarra.application.view.FaceletViewHandlingStrategy;
import org.glassfish.mojarra.facelets.util.Classpath;
import org.glassfish.mojarra.lifecycle.HttpMethodRestrictionsPhaseListener;
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

    private static final String LEGACY_PARAM_PREFIX = "com.sun.faces.";
    private static final String CURRENT_PARAM_PREFIX = "org.glassfish.mojarra.";

    // Key under which we store our WebConfiguration instance.
    private static final String WEB_CONFIG_KEY = "org.glassfish.mojarra.config.WebConfiguration";

    public static final String META_INF_CONTRACTS_DIR = "META-INF" + ResourceHandler.WEBAPP_CONTRACTS_DIRECTORY_DEFAULT_VALUE;

    private static final int META_INF_CONTRACTS_DIR_LEN = META_INF_CONTRACTS_DIR.length();

    private static final String RESOURCE_CONTRACT_SUFFIX = "/" + ResourceHandler.RESOURCE_CONTRACT_XML;

    // Logging level. Defaults to FINE
    private Level loggingLevel = Level.FINE;

    private final Map<BooleanWebContextInitParameter, Boolean> booleanContextParameters = new EnumMap<>(BooleanWebContextInitParameter.class);

    private final Map<WebContextInitParameter, String> contextParameters = new EnumMap<>(WebContextInitParameter.class);

    private final Map<WebContextInitParameter, Map<String, String>> facesConfigParameters = new EnumMap<>(WebContextInitParameter.class);

    private final Map<WebEnvironmentEntry, String> envEntries = new EnumMap<>(WebEnvironmentEntry.class);

    private final Map<WebContextInitParameter, String[]> cachedListParams = new HashMap<>();

    private final Set<String> setParams = new HashSet<>();

    private final ServletContext servletContext;

    private FaceletsConfiguration faceletsConfig;

    private boolean hasFlows;

    // ------------------------------------------------------------ Constructors

    private WebConfiguration(ServletContext servletContext) {
        this.servletContext = servletContext;

        String contextName = servletContext.getContextPath();

        initSetList(servletContext);
        processBooleanParameters(servletContext, contextName);
        processInitParameters(servletContext, contextName);
        if (canProcessJndiEntries()) {
            processJndiEntries(contextName);
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
     * Obtain the value of the specified boolean parameter
     *
     * @param param the parameter of interest
     * @return the value of the specified boolean parameter
     */
    public boolean isOptionEnabled(BooleanWebContextInitParameter param) {
        if (booleanContextParameters.get(param) != null) {
            return booleanContextParameters.get(param);
        }

        return param.getDefaultValue();
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

    public void setOptionEnabled(BooleanWebContextInitParameter param, boolean value) {
        booleanContextParameters.put(param, value);
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

    public String[] getOptionValue(WebContextInitParameter param, String sep) {
        String[] result;

        if ((result = cachedListParams.get(param)) == null) {
            String value = getOptionValue(param);
            if (value == null) {
                result = new String[0];
            } else {
                Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
                result = split(appMap, value, sep);
            }
            cachedListParams.put(param, result);
        }

        return result;
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

    /**
     * @param param the init parameter of interest
     * @return <code>true</code> if the parameter was explicitly set, otherwise, <code>false</code>
     */
    public boolean isSet(BooleanWebContextInitParameter param) {
        return isSet(param.getQualifiedName());
    }

    public void overrideContextInitParameter(BooleanWebContextInitParameter param, boolean value) {
        if (param == null) {
            return;
        }

        boolean oldVal = Boolean.TRUE.equals(booleanContextParameters.put(param, value));
        if (LOGGER.isLoggable(FINE) && oldVal != value) {
            LOGGER.log(FINE, "Overriding init parameter {0}.  Changing from {1} to {2}.", new Object[] { param.getQualifiedName(), oldVal, value });
        }

    }

    public void overrideContextInitParameter(WebContextInitParameter param, String value) {
        if (param == null || value == null || value.length() == 0) {
            return;
        }

        value = value.trim();
        String oldVal = contextParameters.put(param, value);
        cachedListParams.remove(param);
        if (oldVal != null && LOGGER.isLoggable(FINE) && !oldVal.equals(value)) {
            LOGGER.log(FINE, "Overriding init parameter {0}.  Changing from {1} to {2}.", new Object[] { param.getQualifiedName(), oldVal, value });
        }
    }

    public void doPostBringupActions() {
        // Add the HttpMethodRestrictionPhaseListener if the parameter is enabled.
        boolean enabled = isOptionEnabled(BooleanWebContextInitParameter.EnableHttpMethodRestrictionPhaseListener);
        if (enabled) {
            LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            PhaseListener listener = null;

            for (String lifecycleId : toIterable(factory.getLifecycleIds())) {
                Lifecycle lifecycle = factory.getLifecycle(lifecycleId);
                boolean foundExistingListenerInstance = false;
                for (PhaseListener curListener : lifecycle.getPhaseListeners()) {
                    if (curListener instanceof HttpMethodRestrictionsPhaseListener) {
                        foundExistingListenerInstance = true;
                        break;
                    }
                }

                if (!foundExistingListenerInstance) {
                    if (listener == null) {
                        listener = new HttpMethodRestrictionsPhaseListener();
                    }
                    lifecycle.addPhaseListener(listener);
                }
            }
        }

        discoverResourceLibraryContracts();
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

    static void clear(ServletContext servletContext) {

        servletContext.removeAttribute(WEB_CONFIG_KEY);

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * Is the configured value valid against the default boolean pattern.
     * </p>
     *
     * @param param the boolean parameter
     * @param value the configured value
     * @return <code>true</code> if the value is valid, otherwise <code>false</code>
     */
    private boolean isValueValid(BooleanWebContextInitParameter param, String value) {

        if (!ALLOWABLE_BOOLEANS.matcher(value).matches()) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "faces.config.webconfig.boolconfig.invalidvalue",
                        new Object[] { value, param.getQualifiedName(), "true|false", "true|false", param.getDefaultValue() });
            }
            return false;
        }

        return true;

    }

    /**
     * <p>
     * Process all boolean context initialization parameters.
     * </p>
     *
     * @param servletContext the ServletContext of interest
     * @param contextName the context name
     */
    private void processBooleanParameters(ServletContext servletContext, String contextName) {

        for (BooleanWebContextInitParameter param : BooleanWebContextInitParameter.values()) {
            String strValue = getInitParameter(servletContext, contextName, param.getQualifiedName());
            boolean value;

            if (strValue == null) {
                value = param.getDefaultValue();
            } else {
                if (isValueValid(param, strValue)) {
                    value = Boolean.parseBoolean(strValue);
                } else {
                    value = param.getDefaultValue();
                }
            }

            // first param processed should be
            // org.glassfish.mojarra.displayConfiguration
            if (BooleanWebContextInitParameter.DisplayConfiguration.equals(param) && value) {
                loggingLevel = Level.INFO;
            }

            if (LOGGER.isLoggable(loggingLevel)) {
                LOGGER.log(loggingLevel, value ? "faces.config.webconfig.boolconfiginfo.enabled" : "faces.config.webconfig.boolconfiginfo.disabled",
                        new Object[] { contextName, param.getQualifiedName() });
            }

            booleanContextParameters.put(param, value);
        }

    }

    /**
     * Adds all org.glassfish.mojarra init parameter names to a list. This allows callers to determine if a parameter was explicitly
     * set.
     *
     * @param servletContext the ServletContext of interest
     */
    private void initSetList(ServletContext servletContext) {
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
    private String getInitParameter(ServletContext servletContext, String contextName, String qualifiedName) {
        String value = servletContext.getInitParameter(qualifiedName);

        if (value == null && qualifiedName.startsWith(CURRENT_PARAM_PREFIX)) {
            String legacyName = LEGACY_PARAM_PREFIX + qualifiedName.substring(CURRENT_PARAM_PREFIX.length());
            value = servletContext.getInitParameter(legacyName);

            if (value != null && LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "faces.config.webconfig.param.deprecated", new Object[] { contextName, legacyName, qualifiedName });
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
     * @param servletContext the ServletContext of interest
     * @param contextName the context name
     */
    private void processInitParameters(ServletContext servletContext, String contextName) {

        for (WebContextInitParameter param : WebContextInitParameter.values()) {
            String value = getInitParameter(servletContext, contextName, param.getQualifiedName());

            if (value == null || value.isEmpty()) {
                value = param.getDefaultValue();
            }
            if (value == null || value.isEmpty()) {
                continue;
            }

            if (LOGGER.isLoggable(loggingLevel)) {
                LOGGER.log(loggingLevel, "faces.config.webconfig.configinfo", new Object[] { contextName, param.getQualifiedName(), value });
            }
            contextParameters.put(param, value);
        }

    }

    /**
     * <p>
     * Process all JNDI entries.
     * </p>
     *
     * @param contextName the context name
     */
    private void processJndiEntries(String contextName) {
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
                    if (LOGGER.isLoggable(Level.INFO)) {
                        if (LOGGER.isLoggable(loggingLevel)) {
                            LOGGER.log(loggingLevel, "faces.config.webconfig.enventryinfo", new Object[] { contextName, entryName, value });
                        }
                    }
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

    public <T> Iterable<T> toIterable(Iterator<T> iterator) {
        return () -> iterator;
    }

    // ------------------------------------------------------------------- Enums

    /**
     * <p>
     * An <code>enum</code> of all non-boolean context initalization parameters recognized by the implementation.
     * </p>
     */
    public enum WebContextInitParameter {

        NumberOfViews("org.glassfish.mojarra.numberOfViewsInSession", "15"),
        NumberOfLogicalViews("org.glassfish.mojarra.numberOfLogicalViews", "15"),
        NumberOfActiveViewMaps("org.glassfish.mojarra.numberOfActiveViewMaps", "25"),
        NumberOfConcurrentFlashUsers("org.glassfish.mojarra.numberOfConcurrentFlashUsers", "5000"),
        NumberOfFlashesBetweenFlashReapings("org.glassfish.mojarra.numberOfFlashesBetweenFlashReapings", "5000"),
        InjectionProviderClass("org.glassfish.mojarra.injectionProvider", ""),
        SerializationProviderClass("org.glassfish.mojarra.serializationProvider", ""),
        ClientStateWriteBufferSize("org.glassfish.mojarra.clientStateWriteBufferSize", "8192"),
        ResourceBufferSize("org.glassfish.mojarra.resourceBufferSize", "2048"),
        ClientStateTimeout("org.glassfish.mojarra.clientStateTimeout", ""),
        DefaultResourceMaxAge("org.glassfish.mojarra.defaultResourceMaxAge", "604800000"), // 7 days
        ResourceUpdateCheckPeriod("org.glassfish.mojarra.resourceUpdateCheckPeriod", "5"), // in minutes
        CompressableMimeTypes("org.glassfish.mojarra.compressableMimeTypes", ""),
        DisableUnicodeEscaping("org.glassfish.mojarra.disableUnicodeEscaping", "auto"),
        DisableIdUniquenessCheck("org.glassfish.mojarra.disableIdUniquenessCheck", "auto"), // true|false|auto; auto skips the check in Production
        DuplicateJARPattern("org.glassfish.mojarra.duplicateJARPattern", ""),
        FullStateSavingViewIds(StateManager.FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME, ""),

        FaceletsProcessingFileExtensionProcessAs("", ""),
        ;

        private final String qualifiedName;
        private final String defaultValue;

        public String getQualifiedName() {
            return qualifiedName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        WebContextInitParameter(String qualifiedName, String defaultValue) {
            this.qualifiedName = qualifiedName;
            this.defaultValue = defaultValue;
        }

    }

    /**
     * <p>
     * An <code>enum</code> of all boolean context initalization parameters recognized by the implementation.
     * </p>
     */
    public enum BooleanWebContextInitParameter {

        DisplayConfiguration("org.glassfish.mojarra.displayConfiguration", false),
        ValidateFacesConfigFiles("org.glassfish.mojarra.validateXml", false),
        VerifyFacesConfigObjects("org.glassfish.mojarra.verifyObjects", false),
        ForceLoadFacesConfigFiles("org.glassfish.mojarra.forceLoadConfiguration", false),
        DisableClientStateEncryption("org.glassfish.mojarra.disableClientStateEncryption", false),
        EnableClientStateDebugging("org.glassfish.mojarra.enableClientStateDebugging", false),
        PreferXHTMLContentType("org.glassfish.mojarra.preferXHTML", false),
        CompressViewState("org.glassfish.mojarra.compressViewState", true),
        EnableJSStyleHiding("org.glassfish.mojarra.enableJSStyleHiding", false),
        EnableScriptInAttributeValue("org.glassfish.mojarra.enableScriptsInAttributeValues", true),
        WriteStateAtFormEnd("org.glassfish.mojarra.writeStateAtFormEnd", true),
        EnableLazyBeanValidation("org.glassfish.mojarra.enableLazyBeanValidation", true),
        EnableViewStateIdRendering("org.glassfish.mojarra.enableViewStateIdRendering", true),
        RegisterConverterPropertyEditors("org.glassfish.mojarra.registerConverterPropertyEditors", false),
        EnableHttpMethodRestrictionPhaseListener("org.glassfish.mojarra.ENABLE_HTTP_METHOD_RESTRICTION_PHASE_LISTENER", false),
        PartialStateSaving(StateManager.PARTIAL_STATE_SAVING_PARAM_NAME, true),
        GenerateUniqueServerStateIds("org.glassfish.mojarra.generateUniqueServerStateIds", true),
        AutoCompleteOffOnViewState("org.glassfish.mojarra.autoCompleteOffOnViewState", false),
        EnableThreading("org.glassfish.mojarra.enableThreading", false),
        AllowTextChildren("org.glassfish.mojarra.allowTextChildren", false),
        CacheResourceModificationTimestamp("org.glassfish.mojarra.cacheResourceModificationTimestamp", false),
        EnableDistributable("org.glassfish.mojarra.enableDistributable", false), // NOTE: this is indeed implicitly set to true when web.xml distributable is also set, see ConfigureListener.
        EnableMissingResourceLibraryDetection("org.glassfish.mojarra.enableMissingResourceLibraryDetection", false),
        EnableTransitionTimeNoOpFlash("org.glassfish.mojarra.enableTransitionTimeNoOpFlash", false),
        ForceAlwaysWriteFlashCookie("org.glassfish.mojarra.forceAlwaysWriteFlashCookie", false),
        DisallowDoctypeDecl("org.glassfish.mojarra.disallowDoctypeDecl", false),
        UseFaceletsID("org.glassfish.mojarra.useFaceletsID", false),
        DisableOptionalELResolver("org.glassfish.mojarra.disableOptionalELResolver", false),
        ;

        private final String qualifiedName;
        private final boolean defaultValue;

        public String getQualifiedName() {
            return qualifiedName;
        }

        public boolean getDefaultValue() {
            return defaultValue;
        }

        BooleanWebContextInitParameter(String qualifiedName, boolean defaultValue) {
            this.qualifiedName = qualifiedName;
            this.defaultValue = defaultValue;
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
