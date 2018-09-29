/**
 * <p class="changed_added_2_3">APIs for searching for components in the
 * component tree by using expressions.</p>
 * <div class="changed_added_2_3">
 * <p>
 * This feature has two entry points: for the page author and for the Java
 * programmer. Following is a brief overview of each.</p>
 * <h2>For the Page Author</h2>
 * <p>
 * The following tags support the use of search expressions:
 * <code>&lt;h:message /&gt;</code>, <code>&lt;h:messages
 * /&gt;</code>, <code>&lt;h:outputLabel /&gt;</code>, and
 * <code>&lt;f:ajax /&gt;</code>. Each of those tags have one or more attributes
 * whose value must be a client identifier. This feature expands the capability
 * of those attributes to be search expressions. A search expression is space
 * separated list of tokens, where a token can either be a client identifier, a
 * search keyword, or a combination of both. See the specification for
 * <code>{@link javax.faces.component.search.SearchKeywordResolver}</code>
 * for the list of keywords that must be supported. See the specification for
 * <code>{@link javax.faces.component.search.SearchExpressionHandler}</code>
 * to learn how the search is performed.</p>
 * <p>
 * Here is a brief example of the page author use case:</p>
 * <div class="syntax"><div class="text" style="font-family: monospace;"><ol><li class="li1"><div class="de1">&lt;h:body&gt;</div></li>
 * <li class="li2"><div class="de2">&nbsp; &lt;h:outputLabel
 * id=&quot;label&quot; for=&quot;@next&quot; value=&quot;Test&quot;
 * /&gt;</div></li>
 * <li class="li1"><div class="de1">&nbsp; &lt;h:inputText
 * id=&quot;input&quot;&gt;</div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &lt;f:ajax render=&quot;@this @next&quot; /&gt;</div></li>
 * <li class="li1"><div class="de1">&nbsp; &lt;/h:inputText&gt;</div></li>
 * <li class="li2"><div class="de2">&nbsp; &lt;h:inputText id=&quot;input2&quot;
 * /&gt;</div></li>
 * <li class="li1"><div class="de1">&lt;/h:body&gt; </div></li></ol></div>
 * <h2>For the Java Programmer</h2>
 * <p>
 * Using search expressions from Java code offers more flexibility. One must
 * obtain a handle to the
 * <code>{@link javax.faces.component.search.SearchExpressionHandler}</code>
 * and invoke methods on it as desired.</p>
 * <p>
 * The following example resolves to a clientId:</p>
 * <div class="syntax">
 * <div class="java" style="font-family: monospace;">
 * <ol>
 * <li class="li1"><div class="de1">SearchExpressionHandler handler =
 * facesContext.<span class="me1">getApplication</span><span class="br0">&#40;</span><span class="br0">&#41;</span>.<span class="me1">getSearchExpressionHandler</span><span class="br0">&#40;</span><span class="br0">&#41;</span>;</div></li>
 * <li class="li2"><div class="de2">SearchExpressionContext context =
 * SearchExpressionContext.<span class="me1">createSearchExpressionContext</span><span class="br0">&#40;</span>facesContext,
 * facesContext.<span class="me1">getViewRoot</span><span class="br0">&#40;</span><span class="br0">&#41;</span><span class="br0">&#41;</span>;</div></li>
 * <li class="li1"><div class="de1">String clientId =
 * handler.<span class="me1">resolveClientId</span><span class="br0">&#40;</span>context,
 * ":form:container:@next"<span class="br0">&#41;</span>;</div></li>
 * </ol>
 * </div>
 * </div>
 * <p>
 * The following example resolves to a component:</p>
 * <div class="syntax">
 * <div class="java" style="font-family: monospace;">
 * <ol>
 * <li class="li1"><div class="de1">SearchExpressionHandler handler =
 * facesContext.<span class="me1">getApplication</span><span class="br0">&#40;</span><span class="br0">&#41;</span>.<span class="me1">getSearchExpressionHandler</span><span class="br0">&#40;</span><span class="br0">&#41;</span>;</div></li>
 * <li class="li2"><div class="de2">SearchExpressionContext context =
 * SearchExpressionContext.<span class="me1">createSearchExpressionContext</span><span class="br0">&#40;</span>facesContext,
 * facesContext.<span class="me1">getViewRoot</span><span class="br0">&#40;</span><span class="br0">&#41;</span><span class="br0">&#41;</span>;</div></li>
 * <li class="li1"><div class="de1">handler.<span class="me1">resolveComponent</span><span class="br0">&#40;</span>context,
 * ":form:container:@next",</div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;
 * &nbsp; &nbsp; <span class="kw2">new</span>
 * ContextCallback<span class="br0">&#40;</span><span class="br0">&#41;</span>
 * <span class="br0">&#123;</span></div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; <span class="kw2">public</span> void
 * invokeContextCallback<span class="br0">&#40;</span>FacesContext context,
 * </div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;UIComponent
 * target<span class="br0">&#41;</span>
 * <span class="br0">&#123;</span></div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp; <span class="co1">// target == the
 * resolved component</span></div></li>
 * <li class="li1"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; <span class="br0">&#125;</span></div></li>
 * <li class="li2"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;
 * &nbsp; &nbsp; <span class="br0">&#125;</span><span class="br0">&#41;</span>;
 * </div></li></ol></div></div>
 * <p>
 * The following example uses multiple expressions and therefor resolves to
 * multiple components:</p>
 * <div class="syntax">
 * <div class="java" style="font-family: monospace;">
 * <ol>
 * <li class="li1"><div class="de1">SearchExpressionHandler handler =
 * facesContext.<span class="me1">getApplication</span><span class="br0">&#40;</span><span class="br0">&#41;</span>.<span class="me1">getSearchExpressionHandler</span><span class="br0">&#40;</span><span class="br0">&#41;</span>;</div></li>
 * <li class="li2"><div class="de2">SearchExpressionContext context =
 * SearchExpressionContext.<span class="me1">createSearchExpressionContext</span><span class="br0">&#40;</span>facesContext,
 * facesContext.<span class="me1">getViewRoot</span><span class="br0">&#40;</span><span class="br0">&#41;</span><span class="br0">&#41;</span>;</div></li>
 * <li class="li1"><div class="de1">handler.<span class="me1">resolveComponents</span><span class="br0">&#40;</span>context,
 * ":form:container:@next :input1 input2:@parent",</div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;
 * &nbsp; &nbsp; <span class="kw2">new</span>
 * ContextCallback<span class="br0">&#40;</span><span class="br0">&#41;</span>
 * <span class="br0">&#123;</span></div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; <span class="kw2">public</span> void
 * invokeContextCallback<span class="br0">&#40;</span>FacesContext context,
 * </div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;UIComponent
 * target<span class="br0">&#41;</span>
 * <span class="br0">&#123;</span></div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp; <span class="co1">// target == the
 * resolved component</span></div></li>
 * <li class="li1"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; <span class="br0">&#125;</span></div></li>
 * <li class="li2"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;
 * &nbsp; &nbsp; <span class="br0">&#125;</span><span class="br0">&#41;</span>;
 * </div></li></ol></div></div>
 * </div>
 * <h2>Extending the Capabilities of the Component Search Facility</h2>
 * <p>
 * <strong>Creation of the
 * <code>{@link javax.faces.component.search.SearchExpressionContext}</code></strong></p>
 * <p>
 * As with other factories in JSF, the <code>FactoryFinder</code> is used by the
 * system to create instances of the <code>SearchExpressionContext</code> which
 * holds state during the operation of this API.</p>
 * <p>
 * <strong>Adding new <code>{@link javax.faces.component.search.SearchKewordResolver}</code>s</strong></p>
 * <p>
 * See
 * <code>{@link javax.faces.component.search.SearchKeywordResolver}</code>
 * for the specification of how the set of keywords can be expanded.</p>
 * </div>
 */
package javax.faces.component.search;
