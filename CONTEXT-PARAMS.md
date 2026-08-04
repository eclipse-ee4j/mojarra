# Context parameters

Every context parameter recognized by this version of Mojarra, configured as a `<context-param>` in `web.xml`.

The *Default* column holds the value the runtime falls back to when the parameter is absent, which is not always the same as the effective behavior, so read the description too.

The *Performance* column tells where the cost of changing the parameter lands: `startup` at application startup, `request` on every request, `memory` in the size of the session or application, and `-` when it is not a performance knob.

A description starting with **Deprecated** marks a parameter which is on its way out. It is still honored and it warns at startup when set, and where a replacement exists the description names it and the replacement wins when both are set.

Several parameters derive their behavior from `jakarta.faces.PROJECT_STAGE` rather than from a fixed default, which the description says where it applies. A parameter which only makes debugging easier is honored in `Development` alone, and reverts to its default elsewhere with a warning.

An `org.glassfish.mojarra.*` name which this page does not list is not recognized, and is reported at startup unless the stage is `Production`.

[`ContextParamsMdTest`](impl/src/test/java/org/glassfish/mojarra/config/ContextParamsMdTest.java) validates this page against the enums which declare the parameters, so a parameter cannot be added, renamed or given another default without this page being updated.

## Jakarta Faces API context parameters

### Configuration and startup

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.AUTOMATIC_EXTENSIONLESS_MAPPING</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>4.0</td><td>startup</td><td>Additionally maps the <code>FacesServlet</code> to the extensionless variant of every view, so that <code>/foo</code> serves <code>/foo.xhtml</code>. Enabling it walks the entire web application root during startup and registers a servlet mapping per view found, so the cost grows with the amount of views.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.CONFIG_FILES</code></th></tr>
<tr><td><code>String[]</code></td><td><em>(none)</em></td><td>1.0</td><td>startup</td><td>Comma separated list of context relative paths to additional Faces configuration files. <code>/WEB-INF/faces-config.xml</code> is always loaded and must not be listed. Every listed file is parsed at startup, and validated against its schema unless the stage is <code>Production</code>, so a shorter list starts faster.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.DISABLE_FACESSERVLET_TO_XHTML</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.3</td><td>-</td><td>Disables the automatic mapping of the <code>FacesServlet</code> to <code>*.xhtml</code>.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.EXCEPTION_TYPES_TO_IGNORE_IN_LOGGING</code></th></tr>
<tr><td><code>String[]</code></td><td><em>(none)</em></td><td>5.0</td><td>-</td><td>Comma separated list of fully qualified exception class names the default <code>ExceptionHandler</code> must not log. It was <code>com.sun.faces.exceptionTypesToIgnoreInLogging</code> before 5.0, which is still accepted, as is <code>org.glassfish.mojarra.exceptionTypesToIgnoreInLogging</code>, and either warns.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.LIFECYCLE_ID</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>1.0</td><td>-</td><td>Identifier under which a <code>jakarta.faces.lifecycle.Lifecycle</code> was registered in the <code>jakarta.faces.lifecycle.LifecycleFactory</code>, which the <code>FacesServlet</code> must use. This is not a class name but the key passed to <code>LifecycleFactory.addLifecycle()</code>, so it only resolves when something registered a lifecycle under it. Empty means <code>DEFAULT</code>, the standard lifecycle.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.PROJECT_STAGE</code></th></tr>
<tr><td><code>ProjectStage</code></td><td><code>Production</code></td><td>2.0</td><td>request</td><td>Stage the application currently runs in. The runtime recognizes <code>Development</code>, <code>UnitTest</code>, <code>SystemTest</code> and <code>Production</code>, and a custom value is allowed. <code>Production</code> is by far the fastest, <code>Development</code> adds diagnostics and disables several caches. It also decides what a number of other parameters do: the configuration is validated at startup unless the stage is <code>Production</code>, resources are cached unless it is <code>Development</code>, and a parameter which only makes debugging easier is honored in <code>Development</code> alone. Anything other than <code>Production</code> is reported once at startup, since none of it belongs in a deployed application. It may also be set through the JNDI environment entry <code>java:comp/env/faces/ProjectStage</code>, which takes precedence.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.VIEWROOT_PHASE_LISTENER_QUEUES_EXCEPTIONS</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.3</td><td>-</td><td>Queues an exception thrown by a phase listener installed on the <code>UIViewRoot</code> to the <code>ExceptionHandler</code>, instead of logging and swallowing it.</td></tr>
</tbody>
</table>

### View build

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_DECORATORS</code></th></tr>
<tr><td><code>String[]</code></td><td><em>(none)</em></td><td>2.0</td><td>-</td><td>Semicolon separated list of fully qualified class names of <code>jakarta.faces.view.facelets.TagDecorator</code> implementations with a default constructor, applied while compiling a view.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_LIBRARIES</code></th></tr>
<tr><td><code>String[]</code></td><td><em>(none)</em></td><td>2.0</td><td>startup</td><td>Semicolon separated list of web application root relative paths to additional Facelets tag library files, each starting with a <code>/</code>. Every listed file is parsed at startup, so a shorter list starts faster.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_REFRESH_PERIOD</code></th></tr>
<tr><td><code>int</code></td><td><code>0</code></td><td>2.0</td><td>request</td><td>Amount of seconds between two checks whether a compiled view has been modified on disk. <code>-1</code> is the fastest as it drops the check altogether, and the runtime already applies that by itself when the project stage is <code>Production</code> and this parameter is not set. The declared default of <code>0</code> is what every other project stage falls back to, which re-checks on every request so that a view can be edited while the application runs.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_SKIP_COMMENTS</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0</td><td>request</td><td>Strips XML comments from the view so that they are not delivered to the client. <code>true</code> is faster as the comments are dropped while compiling and every response is smaller for it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_SUFFIX</code></th></tr>
<tr><td><code>String</code></td><td><code>.xhtml</code></td><td>2.0</td><td>-</td><td>Space separated list of file extensions, each starting with a <code>.</code>, which a view may be physically stored under. The first extension which resolves to an existing file wins.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_VIEW_MAPPINGS</code></th></tr>
<tr><td><code>String[]</code></td><td><em>(none)</em></td><td>2.0</td><td>-</td><td>Semicolon separated list of file extensions such as <code>.xhtml</code>, or web application root relative prefixes such as <code>/user/</code>, whose resources must be interpreted as Facelets regardless of their extension.</td></tr>
</tbody>
</table>

### State saving

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FULL_STATE_SAVING_VIEW_IDS</code></th></tr>
<tr><td><code>String[]</code></td><td><em>(none)</em></td><td>2.0</td><td>request</td><td>Comma separated list of view IDs which must save their state in full, even when partial state saving is enabled. Every listed view falls back to the slower and much larger full state saving, so an empty list is the fastest.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.PARTIAL_STATE_SAVING</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>2.0</td><td>request</td><td>Saves only the state which differs from the initial state of the view, instead of the state of the entire component tree. <code>true</code> is faster and produces a fraction of the state.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.SERIALIZE_SERVER_STATE</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.2</td><td>request</td><td>Serializes the state into a byte array before storing it in the session, instead of storing the object graph itself. Only relevant when the state saving method is <code>server</code>. <code>false</code> is faster, <code>true</code> costs a serialize and a deserialize per request and only buys the guarantee that the state is serializable, so that a <code>NotSerializableException</code> surfaces during the request which causes it instead of much later at replication or passivation time.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.STATE_SAVING_METHOD</code></th></tr>
<tr><td><code>StateSavingMethod</code></td><td><code>SERVER</code></td><td>1.0</td><td>request</td><td>Where the view state is saved. <code>server</code> keeps it in the session and sends only a reference to the client, and is the faster one, but it is bounded by <code>org.glassfish.mojarra.numberOfStatefulPagesPerSession</code> times <code>org.glassfish.mojarra.numberOfViewStatesPerStatefulPage</code> and is lost when the session is invalidated, both of which surface as a <code>ViewExpiredException</code>. <code>client</code> sends the entire state to the client, which is effectively unbounded and survives session invalidation, at the price of CPU and bandwidth on every request.</td></tr>
</tbody>
</table>

### Rendering

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.ADDITIONAL_HTML_EVENT_NAMES</code></th></tr>
<tr><td><code>String[]</code></td><td><em>(none)</em></td><td>5.0</td><td>-</td><td>Space separated list of additional HTML event names to recognize on top of those declared by <code>jakarta.faces.component.html.HtmlEvents</code>, so that an event those enums do not list can still be used. Duplicates are filtered and the names are case sensitive.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_BUFFER_SIZE</code></th></tr>
<tr><td><code>int</code></td><td><code>1024</code></td><td>2.0</td><td>request</td><td>Amount of bytes the response is buffered with while a view is being rendered. <code>-1</code> leaves the buffer size of the response untouched. A larger buffer flushes less often at the price of more memory per concurrent request, and guarantees during development that an error cannot arrive after the response was already partially flushed.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.SEPARATOR_CHAR</code></th></tr>
<tr><td><code>Character</code></td><td><code>:</code></td><td>2.0</td><td>-</td><td>Character which separates the segments of a client ID. The value must be exactly one character, anything else is rejected with an <code>IllegalArgumentException</code> when the parameter is first read.</td></tr>
</tbody>
</table>

### Resource handling

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.RESOURCE_EXCLUDES</code></th></tr>
<tr><td><code>String[]</code></td><td><code>.class .jsp .jspx .properties .xhtml .groovy</code></td><td>2.0</td><td>-</td><td>Space separated list of file extensions, each starting with a <code>.</code>, which must never be served as a resource. A configured value replaces the default entirely instead of adding to it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.WEBAPP_CONTRACTS_DIRECTORY</code></th></tr>
<tr><td><code>String</code></td><td><code>contracts</code></td><td>2.2</td><td>-</td><td>Web application root relative path where resource library contracts are located. It may denote a nested path such as <code>WEB-INF/contracts</code>, which keeps them out of direct reach of the client.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.WEBAPP_RESOURCES_DIRECTORY</code></th></tr>
<tr><td><code>String</code></td><td><code>resources</code></td><td>2.2</td><td>-</td><td>Web application root relative path where resources are located. It may denote a nested path such as <code>WEB-INF/resources</code>, which keeps them out of direct reach of the client.</td></tr>
</tbody>
</table>

### Validation and conversion

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.ALWAYS_PERFORM_VALIDATION_WHEN_REQUIRED_IS_TRUE</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.2</td><td>-</td><td>Validates a required input component even when the request parameter for it is absent.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0</td><td>-</td><td>Defaults the time zone of every <code>&lt;f:convertDateTime&gt;</code> to the system time zone instead of to GMT.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0</td><td>-</td><td>Converts an empty submitted value to <code>null</code> before it is converted and validated, so that it does not reach the model as an empty string where a <code>@NotNull</code> or a NOT NULL database constraint expects <code>null</code>. It does not affect <code>required="true"</code>, which treats an empty string as no value either way.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.VALIDATE_EMPTY_FIELDS</code></th></tr>
<tr><td><code>ValidateEmptyFields</code></td><td><code>AUTO</code></td><td>2.0</td><td>-</td><td>Whether the validators of an input component also run when its submitted value is empty. <code>auto</code> means <code>true</code> when Jakarta Validation is on the classpath and <code>false</code> otherwise. Faces 1.x never ran them on an empty value, 2.0 had to in order for a <code>@NotNull</code> to be able to reject an empty field, as a validator which never runs cannot reject anything. Set it to <code>false</code> to restore the old behavior, for instance when an existing validator does not expect an empty value.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.validator.DISABLE_DEFAULT_BEAN_VALIDATOR</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0</td><td>request</td><td>Stops the runtime from adding the Jakarta Validation validator to every input component. Registering it manually stays possible. <code>true</code> is faster when the application does not use Jakarta Validation at all, as the validator then no longer runs per input per request.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.validator.ENABLE_VALIDATE_WHOLE_BEAN</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.3</td><td>-</td><td>Enables <code>&lt;f:validateWholeBean&gt;</code>, which validates a copy of the bean as a whole after its individual properties have each passed their own validation. Without this parameter that tag does nothing. The parameter by itself costs no more than a map lookup per validated input, the extra validation pass runs only where the tag is actually used.</td></tr>
</tbody>
</table>

### Scopes and flash

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.CLIENT_WINDOW_MODE</code></th></tr>
<tr><td><code>String</code></td><td><code>none</code></td><td>2.2</td><td>-</td><td>Enables the client window feature. The runtime supports <code>none</code> and <code>url</code>, other values may be supplied by a custom <code>jakarta.faces.lifecycle.ClientWindowFactory</code>.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.NUMBER_OF_CLIENT_WINDOWS</code></th></tr>
<tr><td><code>int</code></td><td><code>10</code></td><td>4.0</td><td>memory</td><td>Maximum amount of client windows kept in the session, which bounds <code>@ClientWindowScoped</code>. Only relevant when <code>jakarta.faces.CLIENT_WINDOW_MODE</code> is enabled. A lower value bounds the session size.</td></tr>
</tbody>
</table>

### WebSocket

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.ENABLE_WEBSOCKET_ENDPOINT</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.3</td><td>startup</td><td>Registers the web socket endpoint which backs <code>&lt;f:websocket&gt;</code> during startup. <code>false</code> skips the registration entirely, so only enable it when <code>&lt;f:websocket&gt;</code> is actually used.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.WEBSOCKET_ENDPOINT_PORT</code></th></tr>
<tr><td><code>int</code></td><td><code>0</code></td><td>2.3</td><td>-</td><td>Port of the web socket endpoint when it differs from the HTTP port. <code>0</code> reuses the port of the request.</td></tr>
</tbody>
</table>

### Security

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.CSP_POLICY</code></th></tr>
<tr><td><code>String</code></td><td><code>script-src 'self' 'nonce-#{nonce}' 'strict-dynamic'</code></td><td>5.0</td><td>-</td><td>Value of the <code>Content-Security-Policy</code> response header sent when <code>jakarta.faces.ENABLE_CSP_NONCE</code> is enabled. Every <code>#{nonce}</code> in it is substituted by the nonce generated for the current response. It was <code>com.sun.faces.cspPolicy</code> before 5.0, which is still accepted, as is <code>org.glassfish.mojarra.cspPolicy</code>, and either warns.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.ENABLE_CSP_NONCE</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>5.0</td><td>request</td><td>Generates a nonce per request, stamps it on every script it renders and sends it in the <code>Content-Security-Policy</code> response header configured by <code>jakarta.faces.CSP_POLICY</code>. An ajax postback reuses the nonce of the view it updates, since the browser still enforces the policy sent with the full page. <code>true</code> costs one secure random draw and a header per response, which is a small price for the protection. It was <code>com.sun.faces.enableCspNonce</code> before 5.0, which is still accepted, as is <code>org.glassfish.mojarra.enableCspNonce</code>, and either warns.</td></tr>
</tbody>
</table>

## Mojarra specific context parameters

### Configuration and startup

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.disableOptionalELResolver</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>4.1.4</td><td>request</td><td>Drops the <code>jakarta.el.OptionalELResolver</code> from the EL resolver chain, so that <code>#{bean.optional.value}</code> no longer unwraps an <code>Optional</code> by itself. Set it when an application resolves properties on the <code>Optional</code> instance itself, or wants the older behavior back. Leaving it enabled costs one more resolver in the chain for every property resolution which reaches it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.displayConfiguration</code></th></tr>
<tr><td><code>String</code></td><td><code>auto</code></td><td>1.2_01</td><td>-</td><td>Logs the value of every context parameter during startup. <code>auto</code>, the default, logs at <code>INFO</code> unless the project stage is <code>Production</code>, where it logs at <code>FINE</code>. <code>true</code> and <code>false</code> pin it to <code>INFO</code> and <code>FINE</code> respectively, which keeps it usable in <code>Production</code> for a deployment whose parameters are substituted at build time.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.duplicateJARPattern</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>1.2_15</td><td>startup</td><td>Regular expression matched against JAR file names to recognize the same library packaged more than once, so that its <code>faces-config.xml</code> is loaded only once. The first capturing group is the identity of the library. Setting it speeds up startup on a classpath which ships the same library twice.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.forceLoadConfiguration</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2_08</td><td>startup</td><td>Loads the Faces configuration even when no <code>FacesServlet</code> was found. Faces already starts up by itself when a <code>FacesServlet</code> is mapped, when <code>/WEB-INF/faces-config.xml</code> exists, or when a Faces annotated class is found, so this is only needed when none of those hold, such as a Spring Boot application which registers everything programmatically and ships neither a <code>web.xml</code> nor a <code>faces-config.xml</code>.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.injectionProvider</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>1.2_01</td><td>-</td><td>Fully qualified class name of the <code>org.glassfish.mojarra.spi.InjectionProvider</code> implementation which injects resources into Faces artifacts.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.sendPoweredByHeader</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2</td><td>-</td><td>Sends the <code>X-Powered-By</code> response header.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.viewStateAutocomplete</code></th></tr>
<tr><td><code>String</code></td><td><code>one-time-code</code></td><td>5.0</td><td>-</td><td>Value of the <code>autocomplete</code> attribute rendered on the hidden view state field, which keeps password managers and browser autofill away from it. <code>one-time-code</code> is the default because several browsers ignore <code>off</code> on a hidden input but do honor the one time code hint. Set it to <code>off</code> for the older behavior, or to any other token a browser is known to honor.</td></tr>
</tbody>
</table>

### View build

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.disableIdUniquenessCheck</code></th></tr>
<tr><td><code>String</code></td><td><code>auto</code></td><td>2.1.9</td><td>request</td><td>Skips the walk which verifies that every component ID within a naming container is unique. <code>auto</code>, the default, skips it unless the project stage is <code>Development</code>, where a duplicate ID surfaces long before the application ships. <code>true</code> always skips it and <code>false</code> always walks.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.refreshTransientBuildOnPSS</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>4.0.19</td><td>request</td><td>Re-applies the Facelets page to the restored component tree right before rendering. <code>false</code> is much faster, <code>true</code> costs a second full build of the view on every postback and is only needed by a view whose structure is changed by non Faces means between restoring and rendering. Fixing that change to go through Faces itself is the better solution, this parameter only papers over it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.useFaceletsID</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.3.15</td><td>request</td><td>Bases an automatically generated component ID on the position of the tag in the Facelets page, instead of on a per view counter. This keeps generated IDs stable across views which include the same page, and <code>true</code> is marginally faster as it drops the per view ID mapper cache.</td></tr>
</tbody>
</table>

### State saving

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.autoCompleteOffOnViewState</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2_15</td><td>-</td><td><strong>Deprecated</strong> since 5.0, replaced by <code>org.glassfish.mojarra.viewStateAutocomplete</code>, which is honored instead when both are set. It warns at startup when set, and <code>true</code> still means <code>autocomplete="off"</code>.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.clientStateTimeout</code></th></tr>
<tr><td><code>int</code></td><td><em>(none)</em></td><td>1.2_05</td><td>-</td><td>Amount of minutes client side saved state stays valid, after which restoring it fails with a <code>ViewExpiredException</code>. Only active when explicitly set.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.clientStateWriteBufferSize</code></th></tr>
<tr><td><code>int</code></td><td><code>8192</code></td><td>1.2_01</td><td>request</td><td>Amount of bytes of the buffer used while writing client side saved state. A larger buffer copies less while writing a large state, at the price of more memory per concurrent request.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.compressViewState</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2</td><td>request</td><td>Compresses the serialized view state with GZIP. When the state saving method is <code>client</code> this shrinks the payload which travels over the wire on every request, which is almost always worth the CPU. When it is <code>server</code> it only has an effect when <code>jakarta.faces.SERIALIZE_SERVER_STATE</code> is also enabled, where it shrinks the session instead.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.enableClientStateDebugging</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.1.14</td><td>request</td><td><strong>Honored only when the project stage is <code>Development</code></strong>, and it warns and reverts elsewhere. Makes the client side saved state readable while debugging it: the state is deserialized right after writing it, which doubles the cost of every state save and surfaces a non serializable property immediately, and the <code>ByteArrayGuard</code> is dropped, which is what provides both the confidentiality and the tamper detection of that state. It therefore replaces <code>org.glassfish.mojarra.disableClientStateEncryption</code>, which did the second half alone.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.enableViewStateIdRendering</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2_08</td><td>-</td><td>Renders the <code>id</code> attribute on the hidden view state field.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.numberOfLogicalViews</code></th></tr>
<tr><td><code>int</code></td><td><code>15</code></td><td>1.2</td><td>memory</td><td><strong>Deprecated</strong> since 5.0, renamed to <code>org.glassfish.mojarra.numberOfStatefulPagesPerSession</code>, which inherits its value and is honored instead when both are set. It warns at startup when set. The old name said the opposite of what it sized.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.numberOfStatefulPagesPerSession</code></th></tr>
<tr><td><code>int</code></td><td><code>15</code></td><td>5.0</td><td>memory</td><td>Maximum amount of stateful pages kept in the session when the state saving method is <code>server</code>. A page becomes stateful the moment it is requested, and stays one entry for as long as it is posted back to, so this is in practice how many pages a user may have open at once before the least recently used one loses its state and yields a <code>ViewExpiredException</code>. Note it counts page <em>visits</em>, not distinct pages: the same page open in two browser tabs occupies two entries, because each initial request starts its own. The session holds up to this value times <code>org.glassfish.mojarra.numberOfViewStatesPerStatefulPage</code> states, so it directly sizes the session.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.numberOfViewsInSession</code></th></tr>
<tr><td><code>int</code></td><td><code>15</code></td><td>1.2</td><td>memory</td><td><strong>Deprecated</strong> since 5.0, renamed to <code>org.glassfish.mojarra.numberOfViewStatesPerStatefulPage</code>, which inherits its value and is honored instead when both are set. It warns at startup when set. The old name said the opposite of what it sized.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.numberOfViewStatesPerStatefulPage</code></th></tr>
<tr><td><code>int</code></td><td><code>15</code></td><td>5.0</td><td>memory</td><td>Maximum amount of view states kept per stateful page when the state saving method is <code>server</code>, which is in practice how far back the browser history may go on that page before the state behind it is gone and a <code>ViewExpiredException</code> follows. Only a non ajax postback adds a state, because that is what adds a browser history entry; an ajax postback deliberately reuses the current one, so that a page which polls or validates over ajax does not evict its own history. Together with <code>org.glassfish.mojarra.numberOfStatefulPagesPerSession</code> it bounds how much state a session holds.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.serializationProvider</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>1.2_01</td><td>request</td><td>Fully qualified class name of the <code>org.glassfish.mojarra.spi.SerializationProvider</code> implementation which serializes the view state. It is invoked on every state save and restore, so its speed is the speed of state saving.</td></tr>
</tbody>
</table>

### Rendering

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.allowTextChildren</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0.0</td><td>-</td><td><strong>Deprecated</strong> since 5.0 and still honored, but scheduled for removal, and it warns at startup when set. Renders the children of <code>&lt;h:outputText&gt;</code> and <code>&lt;h:inputText&gt;</code> again. Mojarra 2.0 stopped rendering them, and this reverts to the older behavior for an application which had come to rely on nesting markup inside a text component. Neither setting is coherent: leaving it off discards the children silently, and turning it on emits them past an <code>&lt;input&gt;</code> element which cannot have children, and past the <code>escape</code> attribute which only governs the value. Use <code>&lt;h:panelGroup&gt;</code> or plain markup instead.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.disableUnicodeEscaping</code></th></tr>
<tr><td><code>String</code></td><td><code>auto</code></td><td>1.2_09</td><td>request</td><td>Whether non ASCII characters are escaped into HTML entities such as <code>&amp;#233;</code>. <code>auto</code>, the default, escapes only what the response character encoding cannot represent: nothing under a UTF encoding, everything above <code>ISO-8859-1</code> under that one, and everything non ASCII under any other. <code>true</code> never escapes and is the fastest, since it skips the per character scan entirely. <code>false</code> always escapes, which is the slowest and produces the largest output. <strong>Leave it alone under UTF-8</strong>, where <code>auto</code> already escapes nothing and <code>true</code> only saves the scan. Set <code>true</code> to emit characters natively under a charset which is neither UTF nor <code>ISO-8859-1</code>, which <code>auto</code> does not recognize as capable and would therefore escape needlessly; the output is then mojibake if that charset cannot actually represent them. Set <code>false</code> to force pure ASCII output, which survives a proxy or mail gateway stripping the charset from the <code>Content-Type</code> header. It never escapes the characters which matter for correctness, such as <code>&lt;</code> and <code>&amp;</code>, which are always escaped.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.preferXHTML</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2</td><td>-</td><td>Prefers <code>application/xhtml+xml</code> over <code>text/html</code> when the client accepts both equally well. It is a tie break within the <code>Accept</code> negotiation rather than an override: a client which does not offer <code>application/xhtml+xml</code>, or offers it at a lower quality, still gets <code>text/html</code>. It also decides the content type when the request expresses no preference at all. Every current browser offers both at the same quality, so enabling it does change what they receive. <strong>Only enable it if you deliberately serve XHTML</strong>, and understand that it is served as XML: a browser then rejects the whole page on the first well-formedness error instead of recovering from it, and the response writer switches to wrapping inline <code>&lt;script&gt;</code> and <code>&lt;style&gt;</code> bodies in <code>&lt;![CDATA[</code> sections rather than emitting them as is. Neither the DOCTYPE of the view nor <code>&lt;facelets-processing&gt;&lt;process-as&gt;</code> influences this; those decide how the view is parsed, not what it is served as. To pick the content type per view rather than per application, use <code>&lt;f:view contentType&gt;</code>, which takes precedence over this parameter.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.writeStateAtFormEnd</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2_04</td><td>-</td><td>Writes the hidden view state field just before the closing form tag, instead of directly after the opening one.</td></tr>
</tbody>
</table>

### Resource handling

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.cacheResourceModificationTimestamp</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>2.0.4</td><td>request</td><td>Caches the last modified timestamp of a resource instead of reading it from the file system on every request, which is what makes it the default. It defaults to <code>false</code> when the project stage is <code>Development</code>, where a resource which changed on disk has to be noticed.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.compressableMimeTypes</code></th></tr>
<tr><td><code>String[]</code></td><td><em>(none)</em></td><td>2.0.0</td><td>request</td><td>Comma separated list of mime types of resources which are GZIP compressed when served. A trailing <code>/*</code> acts as a wildcard, as in <code>text/*</code>. Compression spends CPU to save bandwidth, so list the text based types only.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.defaultResourceMaxAge</code></th></tr>
<tr><td><code>int</code></td><td><code>604800000</code></td><td>2.0.0</td><td>request</td><td>Amount of milliseconds a resource may be cached by the client, as expressed in the <code>Expires</code> response header. A higher value means fewer resource requests reach the server.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.enableMissingResourceLibraryDetection</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0.5</td><td>request</td><td>Fails the view when a composite component refers to a resource library which does not exist, instead of rendering the tag as unresolved markup. <code>false</code> is faster as the check resolves the library on every composite component tag.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.resourceBufferSize</code></th></tr>
<tr><td><code>int</code></td><td><code>2048</code></td><td>2.0.0</td><td>request</td><td>Amount of bytes of the buffer used while streaming a resource to the client. A larger buffer reads less often while streaming a large resource, at the price of more memory per concurrent request.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.resourceUpdateCheckPeriod</code></th></tr>
<tr><td><code>int</code></td><td><code>-1</code></td><td>2.0.0</td><td>request</td><td>Amount of minutes between two checks whether a cached resource has been modified. <code>-1</code> drops the check altogether, which is what makes it the default. It defaults to <code>5</code> when the project stage is <code>Development</code>, where a resource which changed on disk has to be noticed.</td></tr>
</tbody>
</table>

### Validation and conversion

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.registerConverterPropertyEditors</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2_09</td><td>startup</td><td>Registers a <code>PropertyEditor</code> for every converter, so that EL coercion outside of Faces uses the Faces converters as well. <code>false</code> is faster as the registration walks every converter at startup.</td></tr>
</tbody>
</table>

### Scopes and flash

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.enableDistributable</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.2.7</td><td>request</td><td>Tells the runtime that the application is distributed over multiple servers, so that a session attribute is re-set after every change to it in order to trigger session replication. <code>false</code> is faster, the extra writes are the price of replication.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.enableTransitionTimeNoOpFlash</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.2.5</td><td>-</td><td>Returns a no-op <code>Flash</code> during startup and shutdown, so that a listener touching the flash outside of a request does not fail.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.forceAlwaysWriteFlashCookie</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.1.20</td><td>-</td><td>Writes the flash cookie on every response, instead of only when the flash actually holds data.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.numberOfActiveViewMaps</code></th></tr>
<tr><td><code>int</code></td><td><code>25</code></td><td>2.3.18</td><td>memory</td><td>Maximum amount of view scope maps kept in the session. The least recently used one is destroyed when the maximum is exceeded. Each map holds the view scoped beans of one view, so this directly sizes the session.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.numberOfConcurrentFlashUsers</code></th></tr>
<tr><td><code>int</code></td><td><code>5000</code></td><td>4.0.7</td><td>memory</td><td>Maximum amount of concurrent users of the flash scope. It directly sizes the application wide flash store.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.numberOfFlashesBetweenFlashReapings</code></th></tr>
<tr><td><code>int</code></td><td><code>5000</code></td><td>2.0.0</td><td>memory</td><td>Amount of flash scope creations between two sweeps which remove expired flash entries. A lower value keeps the store smaller at the price of sweeping more often.</td></tr>
</tbody>
</table>

### WebSocket

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.websocketEndpointIdleTimeout</code></th></tr>
<tr><td><code>int</code></td><td><code>0</code></td><td>4.0.21</td><td>memory</td><td>Amount of milliseconds after which an idle web socket session is closed. <code>0</code> never closes one, which lets idle sessions accumulate on a busy application.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.websocketMaxSessionsPerChannel</code></th></tr>
<tr><td><code>int</code></td><td><em>(none)</em></td><td>4.0.21</td><td>memory</td><td>Maximum amount of concurrent web socket sessions per channel. Empty means unbounded, which lets a channel grow without limit.</td></tr>
</tbody>
</table>

### Security

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.allowedHttpMethods</code></th></tr>
<tr><td><code>String[]</code></td><td><em>(none)</em></td><td>2.0.6</td><td>-</td><td>Space separated list of case sensitive HTTP method names the <code>FacesServlet</code> accepts, which when unset is every method it knows: <code>OPTIONS GET HEAD POST PUT DELETE TRACE CONNECT</code>. <code>*</code> allows every method, including ones it does not know. Any other method is rejected with <code>400</code>.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.disableClientStateEncryption</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.1.14</td><td>request</td><td><strong>Deprecated</strong> since 5.0, replaced by <code>org.glassfish.mojarra.enableClientStateDebugging</code>, which inherits its value and is honored instead when both are set. It warns at startup when set. Both were added by the same change for the same purpose, so one switch covers it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.disallowDoctypeDecl</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.2.14</td><td>-</td><td>Rejects a Facelets source file which contains a <code>&lt;!DOCTYPE&gt;</code> declaration, by setting the XML parser's <code>disallow-doctype-decl</code> feature. A DOCTYPE is where XML entities are defined, so forbidding it rules out external entity resolution and entity expansion attacks. This concerns the source being parsed, not the rendered output, and is only worth it when Facelets sources arrive from somewhere untrusted such as a database or a user upload; sources shipped inside the war are already as trusted as the application itself. A page can then no longer start with <code>&lt;!DOCTYPE html&gt;</code>, render it with <code>&lt;h:doctype&gt;</code> instead.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.enableScriptsInAttributeValues</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2_08</td><td>-</td><td>Allows an attribute value to start with <code>javascript:</code>. Disable it to reject such values, which closes off a class of cross site scripting attacks. It stays relevant when <code>jakarta.faces.ENABLE_CSP_NONCE</code> is enabled, which only takes the DOM event handlers out of its scope by rendering them as listeners in a nonce tagged script instead of as an <code>on*</code> attribute; every other attribute, such as <code>href</code> or a pass-through attribute, is still written as before, and unlike the header this check does not depend on the client honoring it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>org.glassfish.mojarra.generateUniqueServerStateIds</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2_15</td><td>request</td><td><strong>Honored only when the project stage is <code>Development</code></strong>, and it warns and reverts elsewhere. Generates a random server side state identifier instead of an incremental one. <code>false</code> makes the identifier predictable, which is useful while debugging and is a way in everywhere else.</td></tr>
</tbody>
</table>
