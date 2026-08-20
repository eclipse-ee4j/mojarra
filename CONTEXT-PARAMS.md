# Context parameters

Every context parameter recognized by this version of Mojarra, configured as a `<context-param>` in `web.xml`.

The *Default* column holds the value the runtime falls back to when the parameter is absent, which is not always the same as the effective behavior, so read the description too.

The *Performance* column tells where the cost of changing the parameter lands: `startup` at application startup, `request` on every request, `memory` in the size of the session or application, and `-` when it is not a performance knob.

`ContextParamsMdTest` validates this page against the enums which declare the parameters, so a parameter cannot be added, renamed or given another default without this page being updated.

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
<tr><td><code>String</code></td><td><em>(none)</em></td><td>1.0</td><td>startup</td><td>Comma separated list of context relative paths to additional Faces configuration files. <code>/WEB-INF/faces-config.xml</code> is always loaded and must not be listed. Every listed file is parsed and validated at startup, so a shorter list starts faster.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.DISABLE_FACESSERVLET_TO_XHTML</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.3</td><td>-</td><td>Disables the automatic mapping of the <code>FacesServlet</code> to <code>*.xhtml</code>.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.LIFECYCLE_ID</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>1.0</td><td>-</td><td>Identifier under which a <code>jakarta.faces.lifecycle.Lifecycle</code> was registered in the <code>jakarta.faces.lifecycle.LifecycleFactory</code>, which the <code>FacesServlet</code> must use. This is not a class name but the key passed to <code>LifecycleFactory.addLifecycle()</code>, so it only resolves when something registered a lifecycle under it. Empty means <code>DEFAULT</code>, the standard lifecycle.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.PROJECT_STAGE</code></th></tr>
<tr><td><code>String</code></td><td><code>Production</code></td><td>2.0</td><td>request</td><td>Stage the application currently runs in. The runtime recognizes <code>Development</code>, <code>UnitTest</code>, <code>SystemTest</code> and <code>Production</code>, and a custom value is allowed. <code>Production</code> is by far the fastest, <code>Development</code> adds diagnostics and disables several caches.</td></tr>
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
<tr><td><code>String</code></td><td><em>(none)</em></td><td>2.0</td><td>-</td><td>Semicolon separated list of fully qualified class names of <code>jakarta.faces.view.facelets.TagDecorator</code> implementations with a default constructor, applied while compiling a view.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_LIBRARIES</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>2.0</td><td>startup</td><td>Semicolon separated list of web application root relative paths to additional Facelets tag library files, each starting with a <code>/</code>. Every listed file is parsed at startup, so a shorter list starts faster.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_REFRESH_PERIOD</code></th></tr>
<tr><td><code>int</code></td><td><code>0</code></td><td>2.0</td><td>request</td><td>Amount of seconds between two checks whether a compiled view has been modified on disk. <code>-1</code> is the fastest as it drops the check altogether, and the runtime already applies that by itself when the project stage is <code>Production</code> and this parameter is not set. The declared default of <code>0</code> is what every other project stage falls back to, which re-checks on every request so that a view can be edited while the application runs.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_SKIP_COMMENTS</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0</td><td>request</td><td>Strips XML comments from the view so that they are not delivered to the client. <code>true</code> is faster as the comments are dropped while compiling and every response is smaller for it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_SUFFIX</code></th></tr>
<tr><td><code>String</code></td><td><code>.xhtml</code></td><td>2.0</td><td>-</td><td>Space separated list of file extensions, each starting with a <code>.</code>, which a view may be physically stored under. The first extension which resolves to an existing file wins.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_VIEW_MAPPINGS</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>2.0</td><td>-</td><td>Semicolon separated list of file extensions such as <code>.xhtml</code>, or web application root relative prefixes such as <code>/user/</code>, whose resources must be interpreted as Facelets regardless of their extension.</td></tr>
</tbody>
</table>

### State saving

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FULL_STATE_SAVING_VIEW_IDS</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>2.0</td><td>request</td><td>Comma separated list of view IDs which must save their state in full, even when partial state saving is enabled. Every listed view falls back to the slower and much larger full state saving, so an empty list is the fastest.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.PARTIAL_STATE_SAVING</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>2.0</td><td>request</td><td>Saves only the state which differs from the initial state of the view, instead of the state of the entire component tree. <code>true</code> is faster and produces a fraction of the state.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.SERIALIZE_SERVER_STATE</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.2</td><td>request</td><td>Serializes the state into a byte array before storing it in the session, instead of storing the object graph itself. Only relevant when the state saving method is <code>server</code>. <code>false</code> is faster, <code>true</code> costs a serialize and a deserialize per request and only buys the guarantee that the state is serializable, so that a <code>NotSerializableException</code> surfaces during the request which causes it instead of much later at replication or passivation time.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.STATE_SAVING_METHOD</code></th></tr>
<tr><td><code>String</code></td><td><code>server</code></td><td>1.0</td><td>request</td><td>Where the view state is saved. <code>server</code> keeps it in the session and sends only a reference to the client, and is the faster one, but it is bounded by <code>com.sun.faces.numberOfLogicalViews</code> times <code>com.sun.faces.numberOfViewsInSession</code> and is lost when the session is invalidated, both of which surface as a <code>ViewExpiredException</code>. <code>client</code> sends the entire state to the client, which is effectively unbounded and survives session invalidation, at the price of CPU and bandwidth on every request.</td></tr>
</tbody>
</table>

### Rendering

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.FACELETS_BUFFER_SIZE</code></th></tr>
<tr><td><code>int</code></td><td><code>1024</code></td><td>2.0</td><td>request</td><td>Amount of bytes the response is buffered with while a view is being rendered. <code>-1</code> leaves the buffer size of the response untouched. A larger buffer flushes less often at the price of more memory per concurrent request, and guarantees during development that an error cannot arrive after the response was already partially flushed.</td></tr>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.SEPARATOR_CHAR</code></th></tr>
<tr><td><code>String</code></td><td><code>:</code></td><td>2.0</td><td>-</td><td>Character which separates the segments of a client ID. Only the first character of the value is used.</td></tr>
</tbody>
</table>

### Resource handling

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>jakarta.faces.RESOURCE_EXCLUDES</code></th></tr>
<tr><td><code>String</code></td><td><code>.class .jsp .jspx .properties .xhtml .groovy</code></td><td>2.0</td><td>-</td><td>Space separated list of file extensions, each starting with a <code>.</code>, which must never be served as a resource. A configured value replaces the default entirely instead of adding to it.</td></tr>
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
<tr><td><code>String</code></td><td><code>auto</code></td><td>2.0</td><td>-</td><td>Whether the validators of an input component also run when its submitted value is empty. <code>auto</code> means <code>true</code> when Jakarta Validation is on the classpath and <code>false</code> otherwise. Faces 1.x never ran them on an empty value, 2.0 had to in order for a <code>@NotNull</code> to be able to reject an empty field, as a validator which never runs cannot reject anything. Set it to <code>false</code> to restore the old behavior, for instance when an existing validator does not expect an empty value.</td></tr>
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

## Mojarra specific context parameters

### Configuration and startup

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.annotationScanPackages</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>2.0.0</td><td>startup</td><td>Space separated list restricting which packages are scanned for Faces annotations, instead of scanning everything. An entry is either a package name, which applies to <code>/WEB-INF/classes</code>, or <code>jar:&lt;jar name&gt;:&lt;comma separated packages&gt;</code>, where <code>*</code> as jar name applies to every JAR. Restricting the scan is the largest startup win on a big classpath.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.disableOptionalELResolver</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>4.1.4</td><td>request</td><td>Drops the <code>jakarta.el.OptionalELResolver</code> from the EL resolver chain, so that <code>#{bean.optional.value}</code> no longer unwraps an <code>Optional</code> by itself. Set it when an application resolves properties on the <code>Optional</code> instance itself, or wants the older behavior back. Leaving it enabled costs one more resolver in the chain for every property resolution which reaches it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.displayConfiguration</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2_01</td><td>-</td><td>Logs the value of every context parameter at <code>INFO</code> level during startup, instead of at <code>FINE</code> level.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.duplicateJARPattern</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>1.2_15</td><td>startup</td><td>Regular expression matched against JAR file names to recognize the same library packaged more than once, so that its <code>faces-config.xml</code> is loaded only once. The first capturing group is the identity of the library. Setting it speeds up startup on a classpath which ships the same library twice.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.enableThreading</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2_13</td><td>startup</td><td>Parses the Faces configuration files and scans the annotations on a thread pool during startup instead of serially, which can shorten startup on a large application. It additionally gates the background thread which watches those files for changes, but only when the project stage is <code>Development</code>, where that watch is already active unless this parameter is explicitly set to <code>false</code>.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.exceptionTypesToIgnoreInLogging</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>4.0.14</td><td>-</td><td>Comma separated list of fully qualified exception class names the default <code>ExceptionHandler</code> must not log.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.forceLoadConfiguration</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2_08</td><td>startup</td><td>Loads the Faces configuration even when no <code>FacesServlet</code> was found. Faces already starts up by itself when a <code>FacesServlet</code> is mapped, when <code>/WEB-INF/faces-config.xml</code> exists, or when a Faces annotated class is found, so this is only needed when none of those hold, such as a Spring Boot application which registers everything programmatically and ships neither a <code>web.xml</code> nor a <code>faces-config.xml</code>.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.injectionProvider</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>1.2_01</td><td>-</td><td>Fully qualified class name of the <code>com.sun.faces.spi.InjectionProvider</code> implementation which injects resources into Faces artifacts.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.sendPoweredByHeader</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2</td><td>-</td><td>Sends the <code>X-Powered-By</code> response header.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.validateXml</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2</td><td>startup</td><td>Validates every Faces configuration file against its schema during startup. <code>false</code> is faster, the validation only pays off while developing those files.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.verifyObjects</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2</td><td>startup</td><td>Checks during startup that every component, converter, validator and renderer named in the Faces configuration really exists, has a public constructor without arguments and is of the expected type. <code>false</code> is faster because the check has to load every one of those classes. It catches mistakes in the configuration, so it is only useful while developing.</td></tr>
</tbody>
</table>

### View build

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.disableIdUniquenessCheck</code></th></tr>
<tr><td><code>String</code></td><td><code>false</code></td><td>2.1.9</td><td>request</td><td>Skips the walk which verifies that every component ID within a naming container is unique. <code>true</code> is the fastest as it drops a full tree walk per state save in every project stage, <code>auto</code> drops it only when the project stage is <code>Production</code>, on the assumption that a duplicate ID already surfaced during development.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.refreshTransientBuildOnPSS</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>4.0.19</td><td>request</td><td>Re-applies the Facelets page to the restored component tree right before rendering. <code>false</code> is much faster, <code>true</code> costs a second full build of the view on every postback and is only needed by a view whose structure is changed by non Faces means between restoring and rendering. Fixing that change to go through Faces itself is the better solution, this parameter only papers over it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.restoreBuildTimeDecisions</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>4.0.23</td><td>request</td><td>Saves what every build time condition evaluated to while the response was rendered, and replays it while the postback is restored, so that the restore rebuilds the view that was submitted instead of the one the current state of the model asks for. Without it, a <code>&lt;c:if&gt;</code>, <code>&lt;c:choose&gt;</code>, <code>&lt;c:forEach&gt;</code> range or variable <code>&lt;ui:include&gt;</code> path which evaluates to another value than it did produces another view: the state saved for a component the rebuild does not produce is restored into nothing, and a value submitted for it is decoded by nothing. The re-apply which precedes rendering evaluates the conditions again, so the response still follows the model, one phase later. A component id an expression decides is replayed the same way, so that the state saved for a component is restored into the component it was saved for. The cost is one state entry per condition, per component with an expression for an id, and per row of an iteration over a map, and a component which is restored is also validated: an input the response held inside a condition which no longer holds fails a <code>required</code> check the postback would otherwise never have run. The elements a <code>&lt;c:forEach&gt;</code> iterated are not saved - only the range, the row count and, over a map, the keys - since each row reads its own element live: a row whose element the items no longer hold reads a stand-in which reads <code>null</code> and swallows what is written to it, reported with a warning. Only partial state saving restores a view by rebuilding it, so a view listed in <code>jakarta.faces.FULL_STATE_SAVING_VIEW_IDS</code> neither saves nor replays a decision. An iteration whose body names its rows with an expression for an id re-applies that body before rendering rather than retaining what the restore built, since the id is a decision of its own.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.useFaceletsID</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.3.15</td><td>request</td><td>Bases an automatically generated component ID on the position of the tag in the Facelets page, instead of on a per view counter. This keeps generated IDs stable across views which include the same page, and <code>true</code> is marginally faster as it drops the per view ID mapper cache.</td></tr>
</tbody>
</table>

### State saving

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.autoCompleteOffOnViewState</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2_15</td><td>-</td><td>Renders <code>autocomplete="off"</code> on the hidden view state field instead of <code>autocomplete="one-time-code"</code>. Either way the browser is told not to restore a stale state on a back navigation; <code>one-time-code</code> is the default because several browsers ignore <code>off</code> on a hidden input but do honor the one time code hint.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.clientStateTimeout</code></th></tr>
<tr><td><code>int</code></td><td><em>(none)</em></td><td>1.2_05</td><td>-</td><td>Amount of minutes client side saved state stays valid, after which restoring it fails with a <code>ViewExpiredException</code>. Only active when explicitly set.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.clientStateWriteBufferSize</code></th></tr>
<tr><td><code>int</code></td><td><code>8192</code></td><td>1.2_01</td><td>request</td><td>Amount of bytes of the buffer used while writing client side saved state. A larger buffer copies less while writing a large state, at the price of more memory per concurrent request.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.compressViewState</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2</td><td>request</td><td>Compresses the serialized view state with GZIP. When the state saving method is <code>client</code> this shrinks the payload which travels over the wire on every request, which is almost always worth the CPU. When it is <code>server</code> it only has an effect when <code>jakarta.faces.SERIALIZE_SERVER_STATE</code> is also enabled, where it shrinks the session instead.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.enableClientStateDebugging</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.1.14</td><td>request</td><td>Deserializes client side saved state right after writing it, so that a serialization problem surfaces at the point where it is caused. <code>false</code> is the only sane production value, <code>true</code> doubles the cost of state saving.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.enableViewStateIdRendering</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2_08</td><td>-</td><td>Renders the <code>id</code> attribute on the hidden view state field.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.numberOfLogicalViews</code></th></tr>
<tr><td><code>int</code></td><td><code>15</code></td><td>1.2</td><td>memory</td><td>Maximum amount of logical views kept in the session when the state saving method is <code>server</code>. One logical view covers an initially requested view and all of its postbacks. The session holds up to this value times <code>com.sun.faces.numberOfViewsInSession</code> states, so it directly sizes the session.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.numberOfViewsInSession</code></th></tr>
<tr><td><code>int</code></td><td><code>15</code></td><td>1.2</td><td>memory</td><td>Maximum amount of views kept per logical view when the state saving method is <code>server</code>. Together with <code>com.sun.faces.numberOfLogicalViews</code> it bounds how much state a session holds.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.serializationProvider</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>1.2_01</td><td>request</td><td>Fully qualified class name of the <code>com.sun.faces.spi.SerializationProvider</code> implementation which serializes the view state. It is invoked on every state save and restore, so its speed is the speed of state saving.</td></tr>
</tbody>
</table>

### Rendering

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.allowTextChildren</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0.0</td><td>-</td><td>Renders the children of <code>&lt;h:outputText&gt;</code> and <code>&lt;h:inputText&gt;</code> again. Mojarra 2.0 stopped rendering them, and this reverts to the older behavior for an application which had come to rely on nesting markup inside a text component. Nesting there is not valid in the first place, an <code>&lt;h:inputText&gt;</code> renders an <code>&lt;input&gt;</code> element which cannot have children, so treat this as a migration aid rather than a feature.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.disableUnicodeEscaping</code></th></tr>
<tr><td><code>String</code></td><td><code>auto</code></td><td>1.2_09</td><td>request</td><td>Whether non ASCII characters are escaped into HTML entities. <code>true</code> is the fastest as it skips the scan altogether, <code>false</code> always escapes, and <code>auto</code> escapes only what the response character encoding cannot represent. On a UTF-8 application the default already escapes nothing, so move off it only to emit characters natively under a charset which is neither UTF nor <code>ISO-8859-1</code> and is therefore not recognized as capable, or to force pure ASCII output which survives a proxy stripping the charset from the <code>Content-Type</code> header.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.enableJSStyleHiding</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2_03</td><td>-</td><td>Wraps the content of an inline <code>&lt;script&gt;</code> in an XML comment, so that a browser which does not understand <code>&lt;script&gt;</code> renders nothing instead of the source. That concern is long obsolete and HTML5 has no use for the wrapper, so leave it off. It is ignored when the response content type is <code>application/xhtml+xml</code>, where the script body is wrapped in a CDATA section instead.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.preferXHTML</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2</td><td>-</td><td>Responds with content type <code>application/xhtml+xml</code> instead of <code>text/html</code> when the client accepts both.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.writeStateAtFormEnd</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2_04</td><td>-</td><td>Writes the hidden view state field just before the closing form tag, instead of directly after the opening one.</td></tr>
</tbody>
</table>

### Resource handling

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.cacheResourceModificationTimestamp</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0.4</td><td>request</td><td>Caches the last modified timestamp of a resource instead of reading it from the file system on every request. <code>true</code> is faster and is what you want in production.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.compressableMimeTypes</code></th></tr>
<tr><td><code>String</code></td><td><em>(none)</em></td><td>2.0.0</td><td>request</td><td>Comma separated list of mime types of resources which are GZIP compressed when served. A trailing <code>/*</code> acts as a wildcard, as in <code>text/*</code>. Compression spends CPU to save bandwidth, so list the text based types only.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.defaultResourceMaxAge</code></th></tr>
<tr><td><code>int</code></td><td><code>604800000</code></td><td>2.0.0</td><td>request</td><td>Amount of milliseconds a resource may be cached by the client, as expressed in the <code>Expires</code> response header. A higher value means fewer resource requests reach the server.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.enableMissingResourceLibraryDetection</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0.5</td><td>request</td><td>Fails the view when a composite component refers to a resource library which does not exist, instead of rendering the tag as unresolved markup. <code>false</code> is faster as the check resolves the library on every composite component tag.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.resourceBufferSize</code></th></tr>
<tr><td><code>int</code></td><td><code>2048</code></td><td>2.0.0</td><td>request</td><td>Amount of bytes of the buffer used while streaming a resource to the client. A larger buffer reads less often while streaming a large resource, at the price of more memory per concurrent request.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.resourceUpdateCheckPeriod</code></th></tr>
<tr><td><code>int</code></td><td><code>5</code></td><td>2.0.0</td><td>request</td><td>Amount of minutes between two checks whether a cached resource has been modified. <code>-1</code> is the fastest as it drops the check altogether.</td></tr>
</tbody>
</table>

### Validation and conversion

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.registerConverterPropertyEditors</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>1.2_09</td><td>startup</td><td>Registers a <code>PropertyEditor</code> for every converter, so that EL coercion outside of Faces uses the Faces converters as well. <code>false</code> is faster as the registration walks every converter at startup.</td></tr>
</tbody>
</table>

### Scopes and flash

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.enableDistributable</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.2.7</td><td>request</td><td>Tells the runtime that the application is distributed over multiple servers, so that a session attribute is re-set after every change to it in order to trigger session replication. <code>false</code> is faster, the extra writes are the price of replication.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.enableTransitionTimeNoOpFlash</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.2.5</td><td>-</td><td>Returns a no-op <code>Flash</code> during startup and shutdown, so that a listener touching the flash outside of a request does not fail.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.forceAlwaysWriteFlashCookie</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.1.20</td><td>-</td><td>Writes the flash cookie on every response, instead of only when the flash actually holds data.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.numberOfActiveViewMaps</code></th></tr>
<tr><td><code>int</code></td><td><code>25</code></td><td>2.3.18</td><td>memory</td><td>Maximum amount of view scope maps kept in the session. The least recently used one is destroyed when the maximum is exceeded. Each map holds the view scoped beans of one view, so this directly sizes the session.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.numberOfConcerrentFlashUsers</code></th></tr>
<tr><td><code>int</code></td><td><code>5000</code></td><td>2.0.0</td><td>memory</td><td>Deprecated, misspelled name of <code>com.sun.faces.numberOfConcurrentFlashUsers</code>.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.numberOfConcurrentFlashUsers</code></th></tr>
<tr><td><code>int</code></td><td><code>5000</code></td><td>4.0.7</td><td>memory</td><td>Maximum amount of concurrent users of the flash scope. It directly sizes the application wide flash store.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.numberOfFlashesBetweenFlashReapings</code></th></tr>
<tr><td><code>int</code></td><td><code>5000</code></td><td>2.0.0</td><td>memory</td><td>Amount of flash scope creations between two sweeps which remove expired flash entries. A lower value keeps the store smaller at the price of sweeping more often.</td></tr>
</tbody>
</table>

### WebSocket

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.websocketEndpointIdleTimeout</code></th></tr>
<tr><td><code>int</code></td><td><code>0</code></td><td>4.0.21</td><td>memory</td><td>Amount of milliseconds after which an idle web socket session is closed. <code>0</code> never closes one, which lets idle sessions accumulate on a busy application.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.websocketMaxSessionsPerChannel</code></th></tr>
<tr><td><code>int</code></td><td><em>(none)</em></td><td>4.0.21</td><td>memory</td><td>Maximum amount of concurrent web socket sessions per channel. Empty means unbounded, which lets a channel grow without limit.</td></tr>
</tbody>
</table>

### Security

<table>
<thead>
<tr><th>Type</th><th>Default</th><th>Since</th><th>Performance</th><th>Description</th></tr>
</thead>
<tbody>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.ENABLE_HTTP_METHOD_RESTRICTION_PHASE_LISTENER</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.0.6</td><td>request</td><td>Registers a phase listener which completes the response during Restore View when the HTTP method is <code>OPTIONS</code>, so that the rest of the lifecycle is skipped. The listener itself runs on every request, so <code>false</code> is faster unless <code>OPTIONS</code> requests must be short circuited.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.allowedHttpMethods</code></th></tr>
<tr><td><code>String</code></td><td><code>OPTIONS GET HEAD POST PUT DELETE TRACE CONNECT</code></td><td>2.0.6</td><td>-</td><td>Space separated list of case sensitive HTTP method names the <code>FacesServlet</code> accepts. <code>*</code> allows every method. Any other method is rejected.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.cspPolicy</code></th></tr>
<tr><td><code>String</code></td><td><code>script-src 'self' 'nonce-#{nonce}' 'strict-dynamic'</code></td><td>4.0.16</td><td>-</td><td>Value of the <code>Content-Security-Policy</code> response header sent when <code>com.sun.faces.enableCspNonce</code> is enabled. Every <code>#{nonce}</code> in it is substituted by the nonce generated for the current response.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.disableClientStateEncryption</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.1.14</td><td>request</td><td>Disables encryption of client side saved state. It exists to make the state readable while debugging, together with <code>com.sun.faces.enableClientStateDebugging</code>, and drops the <code>ByteArrayGuard</code> which makes the state both unreadable and tamper evident, so never set it in production. Encryption is only set up at all when JNDI entries can be processed.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.disallowDoctypeDecl</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>2.2.14</td><td>-</td><td>Rejects a Facelets source file which contains a <code>&lt;!DOCTYPE&gt;</code> declaration, by setting the XML parser's <code>disallow-doctype-decl</code> feature. A DOCTYPE is where XML entities are defined, so forbidding it rules out external entity resolution and entity expansion attacks. This concerns the source being parsed, not the rendered output, and is only worth it when Facelets sources arrive from somewhere untrusted such as a database or a user upload; sources shipped inside the war are already as trusted as the application itself. A page can then no longer start with <code>&lt;!DOCTYPE html&gt;</code>, render it with <code>&lt;h:doctype&gt;</code> instead.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.enableCspNonce</code></th></tr>
<tr><td><code>boolean</code></td><td><code>false</code></td><td>4.0.16</td><td>request</td><td>Generates a nonce per request, stamps it on every script it renders and sends it in the <code>Content-Security-Policy</code> response header configured by <code>com.sun.faces.cspPolicy</code>. An ajax postback reuses the nonce of the view it updates, since the browser still enforces the policy sent with the full page. <code>true</code> costs one secure random draw and a header per response, which is a small price for the protection.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.enableScriptsInAttributeValues</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2_08</td><td>-</td><td>Allows an attribute value to start with <code>javascript:</code>. Disable it to reject such values, which closes off a class of cross site scripting attacks. It stays relevant when <code>com.sun.faces.enableCspNonce</code> is enabled, which only takes the DOM event handlers out of its scope by rendering them as listeners in a nonce tagged script instead of as an <code>on*</code> attribute; every other attribute, such as <code>href</code> or a pass-through attribute, is still written as before, and unlike the header this check does not depend on the client honoring it.</td></tr>
<tr><th colspan="5" align="left"><br/><code>com.sun.faces.generateUniqueServerStateIds</code></th></tr>
<tr><td><code>boolean</code></td><td><code>true</code></td><td>1.2_15</td><td>request</td><td>Generates a random server side state identifier instead of an incremental counter. Only relevant when the state saving method is <code>server</code>. The state is looked up in the caller's own session, so a guessed identifier does not reach another user's state; randomness buys that an attacker who can make a browser submit a chosen <code>jakarta.faces.ViewState</code> cannot name a specific stored view, and that the value no longer leaks how many views the session has built. <code>false</code> saves a secure random draw per state save and is only worth it when reproducible identifiers help while debugging.</td></tr>
</tbody>
</table>
