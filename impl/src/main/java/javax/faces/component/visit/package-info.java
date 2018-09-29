/**
 * <p class="changed_added_2_0"><span class="changed_modified_2_1 changed_modified_2_2">APIs</span>
 * for traversing a user interface component view.</p>
 *
<div class="changed_added_2_0">
 *
<p>
 * The following example visits all nodes in the view.</p>
 *
<div class="syntax"><div class="java" style="font-family: monospace;"><ol><li class="li1"><div class="de1">UIViewRoot
 * root =
 * facesContext.<span class="me1">getViewRoot</span><span class="br0">&#40;</span><span class="br0">&#41;</span>;</div></li>
 * <li class="li2"><div class="de2">root.<span class="me1">visitTree</span><span class="br0">&#40;</span>VisitContext.<span class="me1">createVisitContext</span><span class="br0">&#40;</span>context<span class="br0">&#41;</span>,
 * </div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;
 * &nbsp; &nbsp; <span class="kw2">new</span>
 * VisitCallback<span class="br0">&#40;</span><span class="br0">&#41;</span>
 * <span class="br0">&#123;</span></div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; <span class="kw2">public</span> VisitResult
 * visit<span class="br0">&#40;</span>VisitContext context, </div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;UIComponent
 * target<span class="br0">&#41;</span>
 * <span class="br0">&#123;</span></div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp; <span class="co1">// take some
 * action on target</span></div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp; <span class="kw2">return</span>
 * VisitResult.<span class="me1">ACCEPT</span>;</div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; <span class="br0">&#125;</span></div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;
 * &nbsp; &nbsp; <span class="br0">&#125;</span><span class="br0">&#41;</span>;
 * </div></li></ol></div></div>
 *
<p>
 * The following example visits two subtrees within the component view.</p>
 *
<div class="syntax"><div class="java" style="font-family: monospace;"><ol><li class="li1"><div class="de1">Set&lt;String&gt;
 * toVisit =
 * getSet<span class="br0">&#40;</span><span class="st0">&quot;form1:optionsPanel&quot;</span>,
 * <span class="st0">&quot;form2:detailPanel&quot;</span><span class="br0">&#41;</span>;</div></li>
 * <li class="li2"><div class="de2">UIViewRoot root =
 * facesContext.<span class="me1">getViewRoot</span><span class="br0">&#40;</span><span class="br0">&#41;</span>;</div></li>
 * <li class="li1"><div class="de1">root.<span class="me1">visitTree</span><span class="br0">&#40;</span>VisitContext.<span class="me1">createVisitContext</span><span class="br0">&#40;</span>context,
 * toVisit, <span class="kw2">null</span><span class="br0">&#41;</span>,
 * </div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;
 * &nbsp; &nbsp; <span class="kw2">new</span>
 * VisitCallback<span class="br0">&#40;</span><span class="br0">&#41;</span>
 * <span class="br0">&#123;</span></div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; <span class="kw2">public</span> VisitResult
 * visit<span class="br0">&#40;</span>VisitContext context, </div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp;UIComponent
 * target<span class="br0">&#41;</span>
 * <span class="br0">&#123;</span></div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp; <span class="co1">// take some
 * action on target</span></div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp; <span class="kw2">return</span>
 * VisitResult.<span class="me1">ACCEPT</span>;</div></li>
 * <li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
 * &nbsp; &nbsp;&nbsp; &nbsp; <span class="br0">&#125;</span></div></li>
 * <li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;
 * &nbsp; &nbsp; <span class="br0">&#125;</span><span class="br0">&#41;</span>;
 * </div></li></ol></div></div>
 *
<p>
 * Note that every child node of those two subtrees is visited.</p>
 *
</div>
 */
package javax.faces.component.visit;
