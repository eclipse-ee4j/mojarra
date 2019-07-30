/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.faces.webapp;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.JspIdConsumer;
import javax.servlet.jsp.tagext.Tag;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;


/**
 * <p><strong><code>UIComponentTagBase</code></strong> is the base class
 * for all Jakarta Server Pages tags that use the "classic" Jakarta Server Pages tag interface that
 * correspond to a {@link javax.faces.component.UIComponent} instance in
 * the view.  In Faces 1.2, all component tags are <code>BodyTag</code>
 * instances to allow for the execution of the page to build the
 * component tree, but not render it.  Rendering happens only after the
 * component tree is completely built.</p>
 *
 * <p>{@link UIComponentTag} extends
 * <code>UIComponentClassicTagBase</code> to add support for properties
 * that conform to the Faces 1.1 Expression Language.</p>
 *
 * <p>{@link UIComponentELTag} extends
 * <code>UIComponentClassicTagBase</code> class to add support for
 * properties that conform to the Expression Language API.</p>
 *
 * <p>The default implementation allows the proper interweaving of
 * template text, non-Faces Jakarta Server Pages tag output, and Faces component tag
 * output in the same page, as expected by the page author.</p>
 *
 * <p>The CASE markers in the following example will be cited in the
 * method descriptions of this class.</p>
 *
 * <ul>
 *
 * <li><p>CASE 1 describes template text and/or non-component custom tag
 * output occurring as the child of a component tag, but before the
 * first component tag child of that component tag.</p></li>
 *
 * <li><p>CASE 2 describes template text and/or non-component custom tag
 * output occurring between two sibling component tags.</p></li>
 *
 * <li><p>CASE 3 describes template text and/or non-component custom tag
 * output occurring as the child content of an &lt;f:verbatim&gt;
 * tag at any point in the page.</p></li>
 *
 * <li><p>CASE 4 describes template text and/or non-component custom tag
 * output occurring between the last child component tag and its
 * enclosing parent component tag's end tag.</p></li>
 *
 * </ul>
 *
 * <pre><code>

 &lt;h:panelGrid style="color:red" border="4" columns="2"&gt;
 CASE 1
 &lt;h:outputText value="component 1"/&gt;
 CASE 2
 &lt;h:outputText value="component 2"/&gt;
 &lt;f:verbatim&gt;CASE 3&lt;/f:verbatim&gt;
 &lt;c:out value="${pageScope.CASE4}" /&gt;
 &lt;/h:panelGrid&gt;

 * </code></pre>
 *
 * <p>The preceding arrangement of faces component tags, must yield
 * markup that will render identically to the following (assuming that
 * <code>${pageScope.CASE4}</code> evaluates to "<code>CASE 4</code>"
 * without the quotes).</p>
 *
 * <pre><code>

 &lt;table border="4" style="color:red"&gt;

 &lt;tbody&gt;

 &lt;tr&gt;&lt;td&gt;CASE 1&lt;/td&gt;&lt;/tr&gt; &lt;tr&gt;&lt;td&gt;component 1&lt;/td&gt;&lt;/tr&gt;

 &lt;tr&gt;&lt;td&gt;CASE 2&lt;/td&gt; &lt;tr&gt;&lt;td&gt;component 2&lt;/td&gt;&lt;/tr&gt;

 &lt;tr&gt;&lt;td&gt;CASE 3&lt;/td&gt; &lt;td&gt;CASE 4&lt;/td&gt;&lt;/tr&gt;

 &lt;/tbody&gt;

 &lt;/table&gt;

 * </code></pre>

 *
 */

public abstract class UIComponentClassicTagBase extends UIComponentTagBase implements JspIdConsumer, BodyTag {

    // ------------------------------------------------------ Manifest Constants
    /**
     * <p>The facesContext scope attribute under which a component tag stack
     * for the current facesContext will be maintained.</p>
     */
    private static final String COMPONENT_TAG_STACK_ATTR =
            "javax.faces.webapp.COMPONENT_TAG_STACK";

    /**
     * <p>The {@link UIComponent} attribute under which we will store a
     * <code>List</code> of the component identifiers of child components
     * created on the previous generation of this page (if any).</p>
     */
    private static final String JSP_CREATED_COMPONENT_IDS =
            "javax.faces.webapp.COMPONENT_IDS";


    /**
     * <p>The {@link UIComponent} attribute under which we will store a
     * <code>List</code> of the facet names of facets created on the previous
     * generation of this page (if any).
     */
    private static final String JSP_CREATED_FACET_NAMES =
            "javax.faces.webapp.FACET_NAMES";


    /**
     * <p>The attribute name under which we will store all {@link UIComponent}
     * IDs of the current translation unit.</p>
     */
    private static final String GLOBAL_ID_VIEW =
            "javax.faces.webapp.GLOBAL_ID_VIEW";

    /**
     * <p>The attribute name under which we will store the {@link FacesContext}
     * for this request.</p>
     */
    private static final String CURRENT_FACES_CONTEXT =
            "javax.faces.webapp.CURRENT_FACES_CONTEXT";

    /**
     * <p>The attribute name under which we will store the {@link UIViewRoot}
     * for this request.</p>
     */
    private static final String CURRENT_VIEW_ROOT =
            "javax.faces.webapp.CURRENT_VIEW_ROOT";

    /**
     * Used as the prefix for ids.  This is necessary to avoid
     * uniqueness conflicts with the transient verbatim components.
     */
    protected static final String UNIQUE_ID_PREFIX =
            UIViewRoot.UNIQUE_ID_PREFIX + '_';

    /**
     * Used to store the previousJspId Map in facesContextScope
     */
    private static final String PREVIOUS_JSP_ID_SET =
            "javax.faces.webapp.PREVIOUS_JSP_ID_SET";

    /**
     * This is a <code>Page</code> scoped marker to help us
     * keep track of the different execution context we could
     * be operating within, e.g. an include, or a tag file.
     * The value of the attribute is an Integer that is unqiue
     * to this page context.
     */
    private static final String JAVAX_FACES_PAGECONTEXT_MARKER =
            "javax.faces.webapp.PAGECONTEXT_MARKER";

    /**
     * This is a <code>facesContext</code> scoped attribute which contains
     * an AtomicInteger which we use to increment the PageContext
     * count.
     */
    private static final String JAVAX_FACES_PAGECONTEXT_COUNTER =
            "javax.faces.webapp.PAGECONTEXT_COUNTER";

    // ------------------------------------------------------ Instance Variables
    /**
     * <p>The <code>bodyContent</code> for this tag handler.</p>
     */
    protected BodyContent bodyContent = null;

    /**
     * <p>The {@link UIComponent} that is being encoded by this tag,
     * if any.</p>
     */
    private UIComponent component = null;


    /**
     * <p>The {@link FacesContext} for the request being processed, if any.
     * </p>
     */
    private FacesContext context = null;


    /**
     * <p>Was a new component instance dynamically created when our
     * <code>findComponent()</code> method was called.</p>
     */
    private boolean created = false;


    /**
     * <p>The <code>Lst</code> of {@link UIComponent} ids created or located
     * by nested {@link UIComponentTag}s while processing the current
     * request.</p>
     */
    private List<String> createdComponents = null;


    /**
     * <p>The <code>List</code> of facet names created or located by nested
     * {@link UIComponentTag}s while processing the current request.</p>
     */
    private List<String> createdFacets = null;


    /**
     * <p>The Jakarta Server Pages <code>PageContext</code> for the page we are embedded in.</p>
     */
    protected PageContext pageContext = null;


    /**
     * <p>The Jakarta Server Pages <code>Tag</code> that is the parent of this tag.</p>
     */
    private Tag parent = null;

    /**
     * {@link #setJspId}
     */

    private String jspId = null;

    /**
     * Only consulted in setJspId to detect the iterator case.
     * Set in {@link #release}.  Never cleared.
     */

    //private String oldJspId = null;

    /**
     * This is simply the jspId prefixed by {@link #UNIQUE_ID_PREFIX}.
     */

    private String facesJspId = null;

    /**
     * <p>The component identifier for the associated component.</p>
     */
    private String id = null;

    /**
     * Caches the nearest enclosing {@link UIComponentClassicTagBase} of this
     * tag. This is used for duplicate id detection.
     */
    private UIComponentClassicTagBase parentTag = null;

    /**
     * Set to true if this component is nested inside of an iterating
     * tag
     */
    private boolean isNestedInIterator = false;

    /**
     * The next child index to get in getChild()
     */
    private int _nextChildIndex = 0;

    Map<String, Map<String, UIComponentTagBase>> namingContainerChildIds = null;

    public UIComponentClassicTagBase() {}

    UIComponentClassicTagBase(PageContext pageContext, FacesContext facesContext) {
        this.pageContext = pageContext;
        this.context = facesContext;
    }


    // --------------------------------------------- Support Methods for Tag

    //
    // Simple methods to be overridden by subclasses if necessary
    //

    /**
     * <p>Return the flag value that should be returned from the
     * <code>doStart()</code> method when it is called.  Subclasses
     * may override this method to return the appropriate value.</p>
     *
     * @throws JspException to cause <code>doStart()</code> to
     *  throw an exception
     *
     * @return the value to return from <code>doStart()</code>
     */
    protected int getDoStartValue() throws JspException {

        int result = EVAL_BODY_BUFFERED;

        return result;
    }

    /**
     * <p>Return the flag value that should be returned from the
     * <code>doEnd()</code> method when it is called.  Subclasses
     * may override this method to return the appropriate value.</p>
     *
     * @throws JspException to cause <code>doEnd()</code> to
     *  throw an exception
     *
     * @return the value to return from <code>doEnd()</code>
     */
    protected int getDoEndValue() throws JspException {

        return (EVAL_PAGE);

    }

    /**
     * <p>Delegate to the <code>encodeBegin()</code> method of our
     * corresponding {@link UIComponent}.  This method is called from
     * <code>doStartTag()</code>.  Normally, delegation occurs unconditionally;
     * however, this method is abstracted out so that advanced tags can
     * conditionally perform this call.
     *
     * @throws IOException if an input/output error occurs
     *
     * @deprecated No encoding is done during Jakarta Server Pages page execution.
     * Encoding is deferred until the page has completed executing to
     * allow the entire tree to be built before any encoding occurs.
     */
    protected void encodeBegin() throws IOException {

        component.encodeBegin(context);

    }


    /**
     * <p>Delegate to the <code>encodeChildren()</code> method of our
     * corresponding {@link UIComponent}.  This method is called from
     * <code>doStartTag()</code>.  Normally, delegation occurs unconditionally;
     * however, this method is abstracted out so that advanced tags can
     * conditionally perform this call.
     *
     * @throws IOException if an input/output error occurs
     *
     * @deprecated No encoding is done during Jakarta Server Pages page execution.
     * Encoding is deferred until the page has completed executing to
     * allow the entire tree to be built before any encoding occurs.
     */
    protected void encodeChildren() throws IOException {

        component.encodeChildren(context);

    }


    /**
     * <p>Delegate to the <code>encodeEnd()</code> method of our
     * corresponding {@link UIComponent}.  This method is called from
     * <code>doStartTag()</code>.  Normally, delegation occurs unconditionally;
     * however, this method is abstracted out so that advanced tags can
     * conditionally perform this call.
     *
     * @throws IOException if an input/output error occurs
     *
     * @deprecated No encoding is done during Jakarta Server Pages page execution.
     * Encoding is deferred until the page has completed executing to
     * allow the entire tree to be built before any encoding occurs.
     */
    protected void encodeEnd() throws IOException {

        component.encodeEnd(context);

    }

    // --------------------------------------------------------- Tag Properties


    /**
     * <p>Set the <code>PageContext</code> of the page containing this
     * tag instance.</p>
     *
     * @param pageContext The enclosing <code>PageContext</code>
     */
    @Override
    public void setPageContext(PageContext pageContext) {

        this.pageContext = pageContext;

    }


    /**
     * <p>Return the <code>Tag</code> that is the parent of this instance.</p>
     */
    @Override
    public Tag getParent() {

        return (this.parent);

    }


    /**
     * <p>Set the <code>Tag</code> that is the parent of this instance.</p>
     *
     * @param parent The new parent <code>Tag</code>
     */
    @Override
    public void setParent(Tag parent) {

        this.parent = parent;

    }




    //
    // Complex methods to support Tag
    //

    /**
     * <p>Set up the {@link javax.faces.context.ResponseWriter} for the
     * current response, if this has not been done already.</p>
     *
     * <p>@deprecated.  {@link
     * javax.faces.application.ViewHandler#renderView} is now
     * responsible for setting up the response writer.  This method is
     * now a no-op.</p>

     */
    protected void setupResponseWriter() {
    }


    /**
     * <p>Create a new child component using <code>createComponent</code>,
     * initialize its properties, and add it to its parent as a child.
     * </p>
     * @param context {@link FacesContext} for the current request
     * @param parent Parent {@link UIComponent} for the new child
     * @param componentId Component identifier for the new child,
     *  or <code>null</code> for no explicit identifier
     */
    private UIComponent createChild(
            FacesContext context,
            UIComponent parent,
            UIComponentClassicTagBase parentTag,
            String componentId) throws JspException {

        UIComponent component = createComponent(context, componentId);
        int indexOfNextChildTag = parentTag.getIndexOfNextChildTag();
        if (indexOfNextChildTag > parent.getChildCount()) {
            indexOfNextChildTag = parent.getChildCount();
        }
        parent.getChildren().add(indexOfNextChildTag, component);
        created = true;
        return (component);

    }

    /**
     * <p>Create a new child component using <code>createComponent</code>,
     * initialize its properties, and add it to its parent as a facet.
     * </p>
     * @param context {@link FacesContext} for the current request
     * @param parent Parent {@link UIComponent} of the new facet
     * @param name Name of the new facet
     * @param newId id of the new facet
     */
    private UIComponent createFacet(FacesContext context, UIComponent parent,
                                    String name, String newId) throws JspException {

        UIComponent component = createComponent(context, newId);
        parent.getFacets().put(name, component);
        created = true;
        return (component);

    }

    /**
     * <p>Return a child with the specified component id from the specified
     * component, if any; otherwise, return <code>null</code>.</p>
     *
     * @param component {@link UIComponent} to be searched
     * @param componentId Component id to search for
     */
    private static UIComponent getChild(
            UIComponentClassicTagBase tag, UIComponent component, String componentId)
    {
        int childCount = component.getChildCount();

        // we only need to bother to check if we even have children
        if (childCount > 0)
        {
            List<UIComponent> children = component.getChildren();

            // Most Lists implement RandomAccess, so iterate directly rather than creating
            // and iterator
            if (children instanceof RandomAccess)
            {
                // in the most common case, the first component we are asked for will be the
                // our first child, the second, our second, etc.  Take advantage of this by
                // remembering the index to check for the next child.  This changes this code
                // from O(n^2) for all of the children to O(n)
                int startIndex;

                if (tag != null)
                    startIndex = tag._nextChildIndex;
                else
                    startIndex = 0;

                // start searching from location remembered from last time
                for (int i = startIndex; i < childCount; i++)
                {
                    UIComponent child = children.get(i);

                    if (componentId.equals(child.getId()))
                    {
                        // bump up the index to search next and wrap around
                        i++;

                        tag._nextChildIndex = (i < childCount) ? i : 0;
                        return child;
                    }
                }

                // handle case where we started past the first item and didn't find our
                // child.  Now search from the beginning to where we started
                if (startIndex > 0)
                {
                    for (int i = 0; i < startIndex; i++)
                    {
                        UIComponent child = children.get(i);

                        if (componentId.equals(child.getId()))
                        {
                            i++;

                            tag._nextChildIndex = i;
                            return child;
                        }
                    }
                }
            }
            else
            {
                // List doesn't support RandomAccess, do it the iterator way
                for (UIComponent child : children)
                {
                    if (componentId.equals(child.getId()))
                        return child;
                }
            }
        }

        return null;
    }

    /**
     * <p>Find and return the {@link UIComponent}, from the component
     * tree, that corresponds to this tag handler instance.  If there
     * is no such {@link UIComponent}, create one
     * and add it as a child or facet of the {@link UIComponent} associated
     * with our nearest enclosing {@link UIComponentTag}.  The process for
     * locating or creating the component is:</p>
     * <ol>
     * <li>If we have previously located this component, return it.</li>
     * <li>Locate the parent component by looking for a parent
     *     {@link UIComponentTag} instance, and ask it for its component.
     *     If there is no parent {@link UIComponentTag} instance, this tag
     *     represents the root component, so get it from the current
     *     <code>Tree</code> and return it.</li>
     * <li>If this {@link UIComponentTag} instance has the
     *     <code>facetName</code> attribute set, ask the parent
     *     {@link UIComponent} for a facet with this name.  If not found,
     *     create one, call <code>setProperties()</code> with the new
     *     component as a parameter, and register it under this name.
     *     Return the found or created facet {@link UIComponent}.</li>
     * <li>Determine the component id to be assigned to the new
     *     component, as follows:  if this {@link UIComponentTag} has
     *     an <code>id</code> attribute set, use that value; otherwise,
     *     generate an identifier that is guaranteed to be the same for
     *     this {@link UIComponent} every time this page is processed
     *     (i.e. one based on the location of all {@link UIComponentTag}
     *     instances without an <code>id</code> attribute set).</li>
     * <li>Ask the parent {@link UIComponent} for a child with this identifier.
     *     If not found, create one, call <code>setProperties()</code>
     *     with the new component as a parameter, and register it as a child
     *     with this identifier.  Return the found or created
     *     child {@link UIComponent}.</li>
     * </ol>
     * <p>When creating a component, the process is:</p>
     * <ol>
     * <li>Retrieve the component type by calling
     * {@link UIComponentTag#getComponentType}</li>
     * <li>If the component has a <code>binding</code> attribute,
     * create an expression from it, and call
     * {@link Application#createComponent} with that expression,
     * the {@link FacesContext}, and the component type.  Store the
     * expression using the key <code>"binding"</code>.</li>
     * <li>Otherwise, call {@link Application#createComponent} with
     * only the component type.
     * <li>Call <code>setProperties()</code>.
     * <li>Add the new component as a child or facet of its parent</li>
     * </ol>
     *
     * @param context the {@code FacesContext} for the current request.
     *
     * @return the found component
     *
     * @throws JspException if an unexpected condition arises while finding the component
     */
    protected UIComponent findComponent(FacesContext context) throws JspException
    {
        // Step 1 -- Have we already found the relevant component?
        if (component != null) {
            return (component);
        }

        // Step 2 -- Identify the component that is, or will be, our parent
        UIComponentClassicTagBase parentTag =
                _getParentUIComponentClassicTagBase(context.getAttributes());
        UIComponent parentComponent;
        if (parentTag != null) {
            parentComponent = parentTag.getComponentInstance();
        } else {
            // Special case.  The component to be found is the
            // UIViewRoot.
            // see if this is the first time this tag instance is trying
            // to be bound to the UIViewRoot
            parentComponent = context.getViewRoot();
            // Has this UIViewRoot instance had a tag bound to it
            // before?
            if (null == parentComponent.getAttributes().get(CURRENT_VIEW_ROOT)) {
                // No it hasn't.

                // make sure setProperties() and setId() are called
                // once per UIViewRoot instance.
                try {
                    setProperties(parentComponent);
                } catch (FacesException e) {
                    if (e.getCause() instanceof JspException) {
                        throw ((JspException)e.getCause());
                    }
                    throw e;
                }

                if (null != this.id) {
                    parentComponent.setId(this.id);
                } else {
                    assert(null != getFacesJspId());
                    parentComponent.setId(getFacesJspId());
                }
                parentComponent.getAttributes().put(CURRENT_VIEW_ROOT,
                        CURRENT_VIEW_ROOT);
                created = true;
            } else if (hasBinding()) {
                try {
                    setProperties(parentComponent);
                } catch (FacesException e) {
                    if (e.getCause() instanceof JspException) {
                        throw ((JspException)e.getCause());
                    }
                    throw e;
                }
            }

            // this is not the first time this tag instance is trying to
            // be bound to this UIViewRoot, take no extra action.

            component = parentComponent;
            return (component);
        }

        // Step 3 -- Calculate the component identifier for this component
        String newId = createId(context);

        // Step 4 -- Create or return a facet with the specified name (if any)
        String facetName = getFacetName();
        boolean created = parentTag.getCreated();

        if (facetName != null) {
            component = parentComponent.getFacets().get(facetName);
            if (component == null) {
                component = createFacet(context, parentComponent, facetName,
                        newId);
            }
            return (component);
        } else {

            // Step 5 -- Create or return a child with the specified id
            component = getChild(parentTag, parentComponent, newId);
            if (component == null) {
                component = createChild(context, parentComponent, parentTag, newId);
            }
            return (component);
        }

    }

    //
    // Tag tree navigation
    //

    /**
     * <p>Locate and return the nearest enclosing {@link UIComponentClassicTagBase}
     * if any; otherwise, return <code>null</code>.</p>
     *
     * @param context <code>PageContext</code> for the current page
     *
     * @return the parent tag
     */
    public static UIComponentClassicTagBase getParentUIComponentClassicTagBase(PageContext context)
    {
        return _getParentUIComponentClassicTagBase(getFacesContext(context));
    }

    private static UIComponentClassicTagBase _getParentUIComponentClassicTagBase(
            FacesContext facesContext)
    {
        return _getParentUIComponentClassicTagBase(facesContext.getAttributes());
    }

    private static UIComponentClassicTagBase _getParentUIComponentClassicTagBase(Map<Object, Object> cMap) {
        List list = null;

        if (cMap != null) {
            list = (List) cMap.get(COMPONENT_TAG_STACK_ATTR);
        }

        if (list != null)
        {
            return ((UIComponentClassicTagBase) list.get(list.size() - 1));
        }
        else
        {
            return null;
        }

    }

    //
    // Methods related to the createdComponents and createdFacets lists.
    //

    @Override
    protected int getIndexOfNextChildTag() {

        if (createdComponents != null) {
            return (createdComponents.size());
        } else {
            return (0);
        }

    }

    @Override
    protected void addChild(UIComponent child) {

        if (createdComponents == null) {
            createdComponents = new ArrayList<>(6);
        }
        createdComponents.add(child.getId());
    }

    /*
     * Adds argument child to component tree as a child of this component.
     */

    void addChildToComponentAndTag(UIComponent child) {
        UIComponent myComponent = this.getComponentInstance();

        int indexOfNextChildTag = this.getIndexOfNextChildTag();
        if (indexOfNextChildTag > myComponent.getChildCount()) {
            indexOfNextChildTag = myComponent.getChildCount();
        }
        myComponent.getChildren().add(indexOfNextChildTag, child);
        this.addChild(child);
    }

    @Override
    protected void addFacet(String name) {

        if (createdFacets == null) {
            //noinspection CollectionWithoutInitialCapacity
            createdFacets = new ArrayList<>(3);
        }
        createdFacets.add(name);

    }

    /**
     * <p>Pop the top {@link UIComponentTag} instance off of our component tag
     * stack, deleting the stack if this was the last entry.</p>
     */
    private void popUIComponentClassicTagBase() {
        List list = (List) context.getAttributes().get(COMPONENT_TAG_STACK_ATTR);

        // if an exception occurred in a nested  tag,
        //there could be a few tags left in the stack.
        UIComponentClassicTagBase uic = null;
        while (list != null && uic != this) {
            int idx = list.size() - 1;
            uic = (UIComponentClassicTagBase) list.get(idx);
            list.remove(idx);
            if (idx < 1) {
                context.getAttributes().remove(COMPONENT_TAG_STACK_ATTR);
                list = null;
            }
        }
    }


    /**
     * <p>Push the specified {@link UIComponentTag} instance onto our component
     * tag stack, creating a stack if necessary.</p>
     */
    private void pushUIComponentClassicTagBase() {

        List<UIComponentClassicTagBase> list = TypedCollections.dynamicallyCastList((List)
                context.getAttributes().get(COMPONENT_TAG_STACK_ATTR), UIComponentClassicTagBase.class);
        if (list == null) {
            //noinspection CollectionWithoutInitialCapacity
            list = new ArrayList<>();
            context.getAttributes().put(COMPONENT_TAG_STACK_ATTR, list);
        }
        list.add(this);

    }

    /**
     * Similar to List.indexOf, except that we start searching from a specific index
     * and then wrap aroud.  For this to be performant, the List should implement
     * RandomAccess.
     * @param <T>
     * @param list List to seatch
     * @param startIndex index to start searching for value from
     * @param searchValue Value to search for (null not supported)
     * @return The index at which the value was first found, or -1 if not found
     */
    private static int _indexOfStartingFrom(List<?> list, int startIndex, Object searchValue)
    {
        int itemCount = list.size();

        boolean found = false;

        // start searching from location remembered from last time
        for (int currIndex = startIndex; currIndex < itemCount; currIndex++)
        {
            Object currId = list.get(currIndex);

            if ((searchValue == currId) || ((searchValue != null) && searchValue.equals(currId)))
            {
                return currIndex;
            }
        }

        // handle case where we started past the first item and didn't find the
        // searchValue.  Now search from the beginning to where we started
        if (startIndex > 0)
        {
            for (int currIndex = 0; currIndex < startIndex; currIndex++)
            {
                Object currId = list.get(currIndex);

                if ((searchValue == currId) || ((searchValue != null) && searchValue.equals(currId)))
                {
                    return currIndex;
                }
            }
        }

        // didn't find it
        return -1;
    }

    /**
     * <p>Retrieve from the {@link UIComponent} corresponding to this tag
     * handler the list of all child component ids created by
     * {@link UIComponentTag} instances the previous time this tree was
     * rendered.  Compare it to the list of children created during this
     * page processing pass, and remove all children present on the old list
     * but not in the new list.  Save the list as a {@link UIComponent}
     * attribute so that it gets saved as part of the component's state.</p>
     */
    private void removeOldChildren()
    {
        Map<String, Object> attributes = component.getAttributes();
        List<String> currentComponents = createdComponents;

        // Get the old list of created component ids and update the current list as a
        // component attribute
        Object oldValue;

        if (currentComponents != null)
        {
            oldValue = attributes.put(JSP_CREATED_COMPONENT_IDS, currentComponents);
            createdComponents = null;
        }
        else
        {
            oldValue = attributes.remove(JSP_CREATED_COMPONENT_IDS);
        }

        // Remove old children that are no longer present
        if (oldValue != null)
        {
            List<String> oldList = TypedCollections.dynamicallyCastList((List)oldValue, String.class);

            int oldCount = oldList.size();

            if (oldCount > 0)
            {
                if (currentComponents != null)
                {
                    int currStartIndex = 0;

                    for (int oldIndex = 0; oldIndex < oldCount; oldIndex++)
                    {
                        String oldId = oldList.get(oldIndex);

                        int foundIndex = _indexOfStartingFrom(currentComponents, currStartIndex, oldId);

                        if (foundIndex != -1)
                        {
                            currStartIndex = foundIndex + 1;
                        }
                        else
                        {
                            UIComponent child = component.findComponent(oldId);
                            // if a component is marked transient, it would have
                            // been already removed from the child list, but the
                            // oldList would still have it.  In addition, the component
                            // might have manually been removed.  So, if findComponent
                            // isn't successful, don't call remove child (it will NPE)
                            if ( child != null)
                            {
                                component.getChildren().remove(child);
                            }
                        }
                    }
                }
                else
                {
                    List<UIComponent> children = component.getChildren();

                    // All old components need to be removed
                    for (String oldId : oldList)
                    {
                        UIComponent child = component.findComponent(oldId);
                        if (child != null)
                        {
                            children.remove(child);
                        }
                    }
                }
            }
        }
    }


    /**
     * <p>Retrieve from the {@link UIComponent} corresponding to this tag
     * handler the list of all facet names created by {@link UIComponentTag}
     * instances the previous time this tree was rendered.  Compare it to the
     * list of facets created during this page processing pass, and remove
     * all facets present on the old list but not in the new list.  Save the
     * list as a {@link UIComponent} attribute so that it gets saved as part
     * of the component's state.</p>
     */
    private void removeOldFacets()
    {
        Map<String, Object> attributes = component.getAttributes();
        List<String> currentComponents = createdFacets;

        // Get the old list of created component ids and update the current list as a
        // component attribute
        Object oldValue;

        if (currentComponents != null)
        {
            oldValue = attributes.put(JSP_CREATED_FACET_NAMES, currentComponents);
            createdFacets = null;
        }
        else
        {
            oldValue = attributes.remove(JSP_CREATED_FACET_NAMES);
        }

        // Remove old children that are no longer present
        if (oldValue != null)
        {
            List<String> oldList = TypedCollections.dynamicallyCastList((List)oldValue, String.class);

            int oldCount = oldList.size();

            if (oldCount > 0)
            {
                if (currentComponents != null)
                {
                    int currStartIndex = 0;

                    for (int oldIndex = 0; oldIndex < oldCount; oldIndex++)
                    {
                        String oldId = oldList.get(oldIndex);

                        int foundIndex = _indexOfStartingFrom(currentComponents, currStartIndex, oldId);

                        if (foundIndex != -1)
                        {
                            currStartIndex = foundIndex + 1;
                        }
                        else
                        {
                            component.getFacets().remove(oldId);
                        }
                    }
                }
                else
                {
                    Map<String, UIComponent> facets = component.getFacets();

                    // All old facets need to be removed
                    for (String oldId : oldList)
                    {
                        facets.remove(oldId);
                    }
                }
            }
        }
    }

    //
    // Methods to support content interweaving
    //

    /**
     *
     * <p>Create a transient UIOutput component from the body content,
     * of this tag instance or return null if there is no body content,
     * the body content is whitespace, or the body content is a
     * comment.</p>
     *
     * @return the component 
     */

    protected UIComponent createVerbatimComponentFromBodyContent() {
        UIOutput verbatim = null;
        String bodyContentString;
        String trimString;
        if (null != bodyContent &&
                null != (bodyContentString = bodyContent.getString()) &&
                0 < (trimString = bodyContent.getString().trim()).length()) {
            if (!(trimString.startsWith("<!--") &&
                    trimString.endsWith("-->"))) {
                verbatim = createVerbatimComponent();
                verbatim.setValue(bodyContentString);
                bodyContent.clearBody();
            } else {
                StringBuilder content = new StringBuilder(trimString.length());
                int sIdx = trimString.indexOf("<!--");
                int eIdx = trimString.indexOf("-->", sIdx);
                while (sIdx >= 0 && eIdx >= 0) {
                    if (sIdx == 0) {
                        trimString = trimString.substring(eIdx + 3);
                    } else {
                        content.append(trimString.substring(0, sIdx));
                        trimString = trimString.substring(eIdx + 3);
                    }
                    sIdx = trimString.indexOf("<!--");
                    eIdx = trimString.indexOf("-->", sIdx);
                }
                content.append(trimString);
                String result = content.toString();
                if (result.trim().length() > 0) {
                    verbatim = createVerbatimComponent();
                    verbatim.setValue(content.toString());
                }
                bodyContent.clearBody();
            }
        }
        return verbatim;
    }

    /**
     * <p>Use the {@link Application} instance to create a new component
     * with the following characteristics.</p>
     *
     * <p><code>componentType</code> is
     * <code>javax.faces.HtmlOutputText</code>.</p>
     *
     * <p><code>transient</code> is <code>true</code>.</p>
     *
     * <p><code>escape</code> is <code>false</code>.</p>
     *
     * <p><code>id</code> is
     * <code>FacesContext.getViewRoot().createUniqueId()</code></p>
     *
     *
     * @return the component
     */

    protected UIOutput createVerbatimComponent() {
        assert(null != getFacesContext());
        UIOutput verbatim;
        Application application = getFacesContext().getApplication();
        verbatim = (UIOutput)
                application.createComponent("javax.faces.HtmlOutputText");
        verbatim.setTransient(true);
        verbatim.getAttributes().put("escape", Boolean.FALSE);
        verbatim.setId(getFacesContext().getViewRoot().createUniqueId());
        return verbatim;
    }

    /**
     * <p>Add <i>verbatim</i> as a sibling of <i>component</i> in
     * <i>component</i> in the parent's child list.  <i>verbatim</i> is
     * added to the list at the position immediatly preceding
     * <i>component</i>.</p>
     *
     * 
     * @param parentTag the parent tag
     *
     * @param verbatim the verbatim to add before the component
     *
     * @param component the component to be added after the component
     *
     */

    protected void addVerbatimBeforeComponent(
            UIComponentClassicTagBase parentTag,
            UIComponent verbatim,
            UIComponent component) {

        UIComponent parent = component.getParent();
        if (null == parent) {
            return;
        }

        List<UIComponent> children = parent.getChildren();
        // EDGE CASE:
        // Consider CASE 1 or 2 where the component is provided via a
        // component binding in session or application scope.
        // The automatically created UIOuput instances for the template text
        // will already be present.  Check the JSP_CREATED_COMPONENT_IDS attribute,
        // if present and the number of created components is the same
        // as the number of children replace at a -1 offset from the current
        // value of indexOfComponentInParent, otherwise, call add()
        List createdIds = (List)
                parent.getAttributes().get(JSP_CREATED_COMPONENT_IDS);
        int indexOfComponentInParent = children.indexOf(component);
        boolean replace =
                (indexOfComponentInParent > 0 && createdIds != null &&
                        createdIds.size() == children.size());
        if (replace) {
            UIComponent oldVerbatim = children.get(indexOfComponentInParent - 1);
            if (oldVerbatim instanceof UIOutput && oldVerbatim.isTransient()) {
                children.set((indexOfComponentInParent - 1), verbatim);
            } else {
                children.add(indexOfComponentInParent, verbatim);
            }
        } else {
            children.add(indexOfComponentInParent, verbatim);
        }
        parentTag.addChild(verbatim);
    }

    /**
     * <p>Add <i>verbatim</i> as a sibling of <i>component</i> in
     * <i>component</i> in the parent's child list.  <i>verbatim</i> is
     * added to the list at the position immediatly following
     * <i>component</i>.</p>
     *
     * @param parentTag the parent tag
     *
     * @param verbatim the verbatim to add after the component
     *
     * @param component the component to be added before the component
     */

    protected void addVerbatimAfterComponent(UIComponentClassicTagBase parentTag,
                                             UIComponent verbatim,
                                             UIComponent component) {
        int indexOfComponentInParent;
        UIComponent parent = component.getParent();

        // invert the order of this if and the assignment below.  Since this line is
        // here, it appears an early return is acceptable/desired if parent is null,
        // and, if it is null, we should probably check for that before we try to
        // access it.  2006-03-15 jdl
        if (null == parent) {
            return;
        }
        List<UIComponent> children = parent.getChildren();
        indexOfComponentInParent = children.indexOf(component);
        if (children.size() - 1 == indexOfComponentInParent) {
            children.add(verbatim);
        }
        else {
            children.add(indexOfComponentInParent + 1, verbatim);
        }
        parentTag.addChild(verbatim);
    }

    // ------------------------------------------------------------ Tag Methods

    /**
     *
     * <p>Perform any processing necessary to find (or create) the
     * {@link UIComponent} instance in the view corresponding to this
     * tag instance in the page and, if and only if a component was
     * created, insert it into the tree at the proper location as
     * expected by the page author.  Secondarily, cause a transient
     * {@link UIOutput} component to be created and placed in the tree
     * <b>before</b> the <code>UIComponent</code> instance for
     * <b>this</b> tag.  The value of this <code>UIOutput</code>
     * component must include anything covered by <code>CASE 1</code> or
     * <code>CASE 2</code> in the class description.</p>
     *
     * <p>The default implementation, which is intended to be sufficient
     * for most components, implements this secondary requirement by
     * calling {@link #getParentUIComponentClassicTagBase}, and calling
     * {@link #createVerbatimComponentFromBodyContent} on the result.
     * It then adds the returned component to the tree <b>before</b> the
     * actual component for <b>this</b> tag instance instance by calling
     * {@link #addVerbatimBeforeComponent}.</p>
     *
     * <p>Before returning, the component is pushed onto the component
     * stack for this response to enable the {@link
     * #getParentUIComponentClassicTagBase} method to work properly.</p>
     *
     * <p>The flag value to be returned is acquired by calling the
     * <code>getDoStartValue()</code> method, which tag subclasses may
     * override if they do not want the default value.</p>
     *
     * @throws JspException if an error occurs
     */
    @Override
    public int doStartTag() throws JspException {
        // make sure that these ivars are reset at the beginning of the
        // lifecycle for this tag.
        createdComponents = null;
        createdFacets = null;
        UIComponent verbatim = null;

        context = getFacesContext();
        if (null == context) {
            // PENDING(edburns): I18N
            throw new JspException("Can't find FacesContext");
        }

        List list = (List) context.getAttributes().get(COMPONENT_TAG_STACK_ATTR);
        if (list != null) {
            parentTag = ((UIComponentClassicTagBase) list.get(list.size() - 1));
        } else {
            parentTag = null;
        }

        Map<String,UIComponentTagBase> componentIds;

        // If we're not inside of a facet, and if we are inside of a
        // rendersChildren==true component, stuff any template text or
        // custom tag output into a transient component.
        if (null == getFacetName() &&
                null != parentTag) {
            Tag p = this.getParent();
            // If we're not inside a Jakarta Server Pages tag or we're not inside
            // a UIComponentTag flush the buffer
            if (null == p || !(p instanceof UIComponentTagBase)) {
                JspWriter out = pageContext.getOut();
                if (!(out instanceof BodyContent)) {
                    try {
                        out.flush();
                    }
                    catch (IOException ioe) {
                        throw new JspException(ioe);
                    }
                }
            }
            verbatim = parentTag.createVerbatimComponentFromBodyContent();
        }

        // Locate the UIComponent associated with this UIComponentTag,
        // creating one if necessary
        component = findComponent(context);

        // if we have a verbatim component, add it after this component.
        if (null != verbatim) {
            addVerbatimBeforeComponent(parentTag,
                    verbatim, component);
        }

        Object tagInstance = null;
        String clientId = null;

        if (component instanceof NamingContainer || (parentTag == null)) {
            namingContainerChildIds = new HashMap<>();
        }

        if (this.id != null) {
            clientId = getId();

            UIComponentClassicTagBase temp = (UIComponentClassicTagBase)
                    getParentNamingContainerTag().getNamingContainerChildIds().get(clientId);

            // According to the JavaDocs for JspIdConsumer tag handlers
            // that implement this interface are not to be pooled, however
            // due to a bug in Jasper this is not the case.
            // Because of this, two tags with the same ID within the same
            // naming container will not be detected as duplicates.  So
            // in order to ensure we detect it, if the instance is the same,
            // verify the Jakarta Server Pages IDs are different.  If they are, then continue,
            // if they aren't, then we're dealing with EVAL_BODY_AGAIN (see
            // below)
            //noinspection ObjectEquality
            if (temp == this
                    && !this.getJspId().equals(temp.getJspId())) {
                tagInstance = this;
            } else if (temp != null
                    && temp != this
                    && this.getJspId().equals(temp.getJspId())) {
                // new instance, same Jakarta Server Pages ID - this is the EVAL_BODY_AGAIN case.
                tagInstance = temp;
            }
        }

        // If we have a tag instance, then, most likely, a tag handler
        // returned EVAL_BODY_AGAIN somewhere.  Make sure the instance
        // returned is the same as the current instance and if this is the case,
        // do not perform ID validation as it has already been done.
        if (tagInstance == null) {

            // only check for id uniqueness if the author has manually given
            // us an id.
            if (null != this.id) {

                // assert component ID uniqueness
                if (clientId != null) {
                    if (getParentNamingContainerTag().getNamingContainerChildIds().containsKey(clientId)) {
                        // PENDING i18n
                        StringWriter writer = new StringWriter(128);
                        printTree(context.getViewRoot(), clientId, writer, 0);
                        String msg = "Duplicate component id: '"
                                + clientId
                                + "', first used in tag: '"
                                + getParentNamingContainerTag().getNamingContainerChildIds().get(clientId).getClass().getName()

                                + "'\n"
                                + writer.toString();
                        throw new JspException(new IllegalStateException(msg));
                    } else {
                        getParentNamingContainerTag().getNamingContainerChildIds().put(clientId, this);
                    }
                }
            }

            // Add to parent's list of created components or facets if needed

            if (parentTag != null) {
                if (getFacetName() == null) {
                    parentTag.addChild(component);
                } else {
                    parentTag.addFacet(getFacetName());
                }
            }
        }

        // Rendering is deferred until after the tree is completely
        // created

        // Return the appropriate control value
        pushUIComponentClassicTagBase();
        return (getDoStartValue());

    }

    /**
     *
     * <p>Perform any processing necessary to handle the content
     * implications of CASE 3 in the class description.</p>
     *
     * <p>The default implementation, which is intended to be sufficient
     * for most components, calls {@link
     * #createVerbatimComponentFromBodyContent} on <b>this</b> instance
     * and adds it as a child of the component for this tag's component
     * at the <b>end</b> of the child list.  In addition, the following
     * housekeeping steps are taken.</p>
     *
     * <ul>
     *
     * <li>Retrieve from the {@link UIComponent} the set of component
     * ids of child components created by {@link UIComponentTag}
     * instances the last time this page was processed (if any).
     * Compare it to the list of children created during this page
     * processing pass, and remove all children present in the old list
     * but not the new.  Save the new list as a component attribute so
     * that it gets saved as part of the component's state.</li>
     *
     * <li>Retrieve from the {@link UIComponent} the set of facet names
     * of facets created by {@link UIComponentTag} instances the last
     * time this page was processed (if any).  Compare it to the list of
     * facets created during this page processing pass, and remove all
     * facets present in the old list but not the new.  Save the new
     * list as a component attribute so that it gets saved as part of
     * the component's state.</li>
     *
     * <li>Release all references to the component, and pop it from the
     * component stack for this response, removing the stack if this was
     * the outermost component.</li> </ul>
     *
     * <p>The flag value to be returned is acquired by calling the
     * <code>getDoEndValue()</code> method, which tag subclasses may
     * override if they do not want the default value.</p>
     *
     * @throws JspException if an error occurs
     */
    @Override
    public int doEndTag() throws JspException
    {
        // Remove old children and facets as needed
        popUIComponentClassicTagBase();
        removeOldChildren();
        removeOldFacets();

        //If we are at the end tag of a NamingContainer component, reset the Map of ids
        // for the NamingContainer tag.
        if (namingContainerChildIds != null) {
            namingContainerChildIds = null;
        }

        // Render the children (if needed) and  end of the component
        // associated with this tag
        try
        {
            UIComponent verbatim;
            UIComponentClassicTagBase parentTag = _getParentUIComponentClassicTagBase(
                    context.getAttributes());

            if (null != (verbatim = this.createVerbatimComponentFromBodyContent())) {
                component.getChildren().add(verbatim);
                if (null != parentTag) {
                    parentTag.addChild(verbatim);
                }
            }

            // else, we don't render rendersChildren==true
            // components here

        } catch (Throwable e) {
            throw new JspException(e);
        } finally {
            component = null;
            context = null;
        }

        this.release();
        return (getDoEndValue());

    }

    /**
     * <p>Release any resources allocated during the execution of this
     * tag handler.</p>
     */
    @Override
    public void release() {

        this.parent = null;

        this.id = null;
        this.facesJspId = null;
        this.created = false;
        this.bodyContent = null;
        this.isNestedInIterator = false;
        _nextChildIndex = 0;
    }

    // -------------------------------------------- Support methods for BodyTag

    /**
     * <p>Return the flag value that should be returned from the
     * <code>doAfterBody()</code> method when it is called.  Subclasses
     * may override this method to return the appropriate value.</p>
     *
     * @return the value to return from <code>doAfterBody()</code>
     *
     * @return JspException if the value cannot be returned
     *
     * @throws JspException if an unexpected condition arises while
     * getting the value
     */
    protected int getDoAfterBodyValue() throws JspException {

        return (SKIP_BODY);

    }

    // -------------------------------------------------------- BodyTag Methods

    /**
     * <p>Set the <code>bodyContent</code> for this tag handler.  This method
     * is invoked by the Jakarta Server Pages page implementation object at most once per
     * action invocation, before <code>doInitiBody()</code>.  This method
     * will not be invoked for empty tags or for non-empty tags whose
     * <code>doStartTag()</code> method returns <code>SKIP_BODY</code> or
     * <code>EVAL_BODY_INCLUDE</code>.</p>
     *
     * @param bodyContent The new <code>BodyContent</code> for this tag
     */
    @Override
    public void setBodyContent(BodyContent bodyContent) {

        this.bodyContent = bodyContent;

    }

    /**
     * <p>Get the <code>JspWriter</code> from our <code>BodyContent</code>.
     * </p>
     *
     * @return the writer
     */
    public JspWriter getPreviousOut() {

        return (this.bodyContent.getEnclosingWriter());

    }

    public BodyContent getBodyContent() {

        return (this.bodyContent);

    }



    /**
     * <p>Prepare for evaluation of the body.  This method is invoked by the
     * Jakarta Server Pages page implementation object after <code>setBodyContent()</code>
     * and before the first time the body is to be evaluated.  This method
     * will not be invoked for empty tags or for non-empty tags whose
     * <code>doStartTag()</code> method returns <code>SKIP_BODY</code>
     * or <code>EVAL_BODY_INCLUDE</code>.</p>
     *
     * @throws JspException if an error is encountered
     */
    @Override
    public void doInitBody() throws JspException {

        // Default implementation does nothing

    }

    /**
     *
     * <p>Perform any processing necessary to handle the content
     * implications of CASE 4 in the class description.</p>
     *
     * <p>Return result from {@link #getDoAfterBodyValue}</p>
     * @throws JspException if an error is encountered
     */
    @Override
    public int doAfterBody() throws JspException {

        UIComponent verbatim;
        UIComponentClassicTagBase parentTag = _getParentUIComponentClassicTagBase(context.getAttributes());

        // if we are the root tag, or if we are inside of a
        // rendersChildren==true component
        //noinspection ObjectEquality
        if (this == parentTag ||
                (null != parentTag &&
                        parentTag.getComponentInstance().getRendersChildren())) {
            // stuff the template text or custom tag output into a
            // transient component
            if (null != (verbatim = this.createVerbatimComponentFromBodyContent())) {
                // EDGE CASE:
                // Consider CASE 4 where the component is provided via a
                // component binding in session or application scope.
                // The verbatim instance will already be present.  If we
                // add again, the user will get duplicate component ID
                // errors.  Check the JSP_CREATED_COMPONENT_IDS attribute.  If it is not present, we
                // need to add the new verbatim child.  If it is present, assume it is a
                // List and check its size.  If the size of the list is equal to the
                // number of children currently in the component, replace the replace
                // the child of this component at the index derived as follows.  If
                // indexOfChildInParent is 0, replace the child at the 0th index with
                // the new verbatim child.  Otherwise, replace the child at the
                // (indexOfChildInParent - 1)th index with the new verbatim child.
                List createdIds = (List)
                        component.getAttributes().get(JSP_CREATED_COMPONENT_IDS);
                if (createdIds != null) {
                    int listIdx = component.getChildCount();
                    if (createdIds.size() == listIdx) {
                        component.getChildren().set((listIdx - 1), verbatim);
                    } else {
                        component.getChildren().add(verbatim);
                    }
                } else {
                    component.getChildren().add(verbatim);
                }
                parentTag.addChild(verbatim);
            }
        }

        return (getDoAfterBodyValue());

    }

    // ----------------------------------------------- Methods relating to Id

    /**
     * <p>Set the component identifier for our component.  If the
     * argument begins with {@link
     * UIViewRoot#UNIQUE_ID_PREFIX} throw an
     * <code>IllegalArgumentException</code></p>
     *
     * @param id The new component identifier.  This may not start with
     * {@link UIViewRoot#UNIQUE_ID_PREFIX}.
     *
     * @throws IllegalArgumentException if the argument is
     * non-<code>null</code> and starts with {@link
     * UIViewRoot#UNIQUE_ID_PREFIX}.
     */
    @Override
    public void setId(String id) {
        if (null != id && id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
            throw new IllegalArgumentException();
        }

        this.id = id;

    }

    /**
     * <p>Return the <code>id</code> value assigned by the page author.</p>
     *
     * @return the id of this tag
     */
    protected String getId() {

        return (id);

    }

    /**
     * <p>If this method has been called before on this tag's useful
     * lifetime (before {@link #release} was called), return the
     * previously returned value.  Otherwise, if {@link #getJspId}
     * returns non-<code>null</code>, prepend {@link #UNIQUE_ID_PREFIX}
     * to the <code>jspId</code> and return the result.</p>
     *
     * @return the value as specified above
     */

    protected String getFacesJspId() {
        if (null == facesJspId) {
            if (null != jspId) {
                facesJspId = UNIQUE_ID_PREFIX + jspId;
                // if this tag happens to be nested within <c:forEach>,
                //  jspId will be the same for each iteration. So it is
                // transformed into a unique "id" by appending a counter which
                // gets stored in request scope with jspId as the key for use
                // during the next iteration.
                if (isDuplicateId(facesJspId)) {
                    facesJspId = generateIncrementedId(facesJspId);
                }
            } else {
                // jspId will be null if we're running in a container
                // that doesn't support JspIdConsumer
                facesJspId = getFacesContext().getViewRoot().createUniqueId();
            }
        }
        return facesJspId;
    }

    /**
     * Returns true if a component already exists with the same
     * <code>id</code>. This will be the case if this tag is
     * nested within <code><c:forEach></code> tag or any other JSTL loop tag
     * or if the page has components with the same <code>Id</code>.
     *
     * @param componentId <code>id</code> to be looked up for possible
     * duplication.
     * @return true if this nested with <code>facesJspId</code> is duplicate.
     */
    private boolean isDuplicateId(String componentId) {
        boolean result = false;
        if (parentTag != null) {

            if (parentTag.isNestedInIterator) {
                return true;
            }
            List childComponents = parentTag.createdComponents;
            // PENDING: Need to analyze the impact of this look up on pages
            // with several levels of nesting.
            if (childComponents != null) {
                result = childComponents.contains(componentId);
                if (result && (!isNestedInIterator)) {
                    return true;
                }
            }
        }

        return result;
    }

    /*
     * Appends a counter to the passed in <code>id</code> and stores the
     * <code>id</code> and counter information in request scope.
     *
     * @return String <code>id</code> with a counter appended to it.
     */
    private String generateIncrementedId (String componentId) {
        Integer serialNum = (Integer) context.getAttributes().get(componentId);
        if (null == serialNum) {
            serialNum = 1;
        } else {
            serialNum = serialNum.intValue() + 1;
        }
        context.getAttributes().put(componentId, serialNum);
        componentId = componentId + UNIQUE_ID_PREFIX + serialNum.intValue();
        return componentId;
    }

    /**
     * Returns the <code>List</code> of {@link UIComponent} ids created or
     * located by nested {@link UIComponentTag}s while processing the current
     * request.
     *
     * @return the created components
     */
    protected List<String> getCreatedComponents() {
        return createdComponents;
    }

    /**
     * <p>Create the component identifier to be used for this component.</p>
     */
    private String createId(FacesContext context)
            throws JspException {

        if (this.id == null) {
            return getFacesJspId();
        } else {
            // if this tag happens to be nested within <c:forEach>, jspId
            // will be the same for each iteration. So it is
            // transformed into a unique "id" by appending a counter which gets
            // stored in request scope with jspId as the key for use during next
            // iteration.
            if (isDuplicateId(this.id)) {
                if (!isSpecifiedIdUnique(this.id)) {
                    if (isNestedInIterator) {
                        this.id = generateIncrementedId(this.id);
                    } else {
                        StringWriter writer = new StringWriter(128);
                        printTree(context.getViewRoot(), this.id, writer, 0);
                        String msg = "Component ID '"
                                + this.id
                                + "' has already been used"
                                + " in the view.\n"
                                + "See below for the view up to the point of"
                                + " the detected error.\n"
                                + writer.toString();
                        throw new JspException(msg);
                    }
                }
            }
            return (this.id);
        }

    }


    /**
     * @param id the component ID
     * @return <code>true</code> if this ID is unique within the closest naming
     *  container, otherwise <code>false</code>
     */
    private boolean isSpecifiedIdUnique(String id) {

        UIComponentClassicTagBase containerTag = getParentNamingContainerTag();
        UIComponent c = containerTag.component.findComponent(id);
        if (c == null) {
            return true;
        } else {
            UIComponent parent = c.getParent();
            if (parent.equals(this.parentTag.component)) {
                // the component we found has the same parent, If we find
                // a sibling with the same ID, return true so that the
                // id is incremented, otherwise, return false.
                List<String> created = this.parentTag.createdComponents;
                return !(created != null && created.contains(id));
            } else {
                return false;
            }
        }

    }


    /**
     * @return the parent tag that represents the closest NamingContainer
     *  component.
     */
    private UIComponentClassicTagBase getParentNamingContainerTag() {
        if (this.parentTag == null) {
            return this;
        }
        UIComponentClassicTagBase parent = this.parentTag;
        while (parent != null) {
            if (parent.component instanceof NamingContainer
                    || parent.parentTag == null && parent.component instanceof UIViewRoot) {
                return parent;
            }
            parent = parent.parentTag;
        }
        return null;

    }


    // ------------------------------------------------   JspIdConsumer Methods


    /**
     * <p>Defined on {@link JspIdConsumer}.  This method is called by
     * the container before {@link #doStartTag}.  The argument is
     * guaranteed to be unique within the page.</p>
     *
     * <p>IMPLEMENTATION NOTE:  This method will detect where we
     * are in an include and assign a unique ID for each include
     * in a particular 'logical page'.  This allows us to avoid
     * possible duplicate ID situations for included pages that
     * have components without explicit IDs.</p>
     *
     * @param id the container generated id for this tag, guaranteed to
     * be unique within the page.
     */

    @Override
    public void setJspId(String id) {
        // reset Jakarta Server Pages ID here instead of release as we may need
        // to check the ID after the tag has been used
        this.jspId = null;

        Integer pcId = (Integer)
                pageContext.getAttribute(JAVAX_FACES_PAGECONTEXT_MARKER,
                        PageContext.PAGE_SCOPE);
        if (pcId == null) {
            if (null == context) {
                context = FacesContext.getCurrentInstance();
            }
            AtomicInteger aInt = (AtomicInteger) context.getAttributes().get(JAVAX_FACES_PAGECONTEXT_COUNTER);
            if (aInt == null) {
                aInt = new AtomicInteger();
                context.getAttributes().put(JAVAX_FACES_PAGECONTEXT_COUNTER, aInt);
            }

            pcId = aInt.incrementAndGet();
            pageContext.setAttribute(JAVAX_FACES_PAGECONTEXT_MARKER, pcId);
        }
        if (pcId.intValue() > 1) {
            StringBuilder builder = new StringBuilder(id.length() + 3);
            builder.append(id).append("pc").append(pcId);
            jspId = builder.toString();
        } else {
            jspId = id;
        }

        facesJspId = null;
        updatePreviousJspIdAndIteratorStatus(jspId);
    }

    /**
     * <p>Called from {@link #setJspId} to update the value saved for
     * the previous call to {@link #setJspId} for this component <b>on
     * this request</b>.  If this method is presented with the same
     * argument <code>id</code> for the same tag instance more than once
     * on the same request, then we know that the tag instance lies
     * inside an iterator tag, such as <code>c:forEach</code>.  If so,
     * we set the <code>isNestedInIterator</code> ivar to
     * <code>true</code> otherwise, we set it to <code>false</code>.</p>
     *
     * <p>The implementation for this method stores a Map from tag
     * instance to id String as a request scoped attribute.  This map
     * contains the value used as the previousJspId and compared with
     * the argument <code>id</code>.
     *
     * @param id the id to be compared with the previous id, if any, for
     * this tag instance on this request.
     */

    private void updatePreviousJspIdAndIteratorStatus(String id)
    {
        Set<String> previousJspIdSet = TypedCollections.dynamicallyCastSet((Set)
                pageContext.getAttribute(PREVIOUS_JSP_ID_SET, PageContext.PAGE_SCOPE), String.class);

        if (null == previousJspIdSet)
        {
            previousJspIdSet = new HashSet<>();

            //noinspection CollectionWithoutInitialCapacity
            pageContext.setAttribute(PREVIOUS_JSP_ID_SET, previousJspIdSet, PageContext.PAGE_SCOPE);
        }

        // detect the iterator case, since add will return true if the collection already
        // contains the id
        if (previousJspIdSet.add(id))
        {
            // id wasn't in Set, so we aren't nested yet
            isNestedInIterator = false;
        }
        else
        {
            // the Set didn't change, so we are nested
            if (log.isLoggable(Level.FINEST))
            {
                log.log(Level.FINEST, "Id " + id + " is nested within an iterating tag.");
            }

            isNestedInIterator = true;
        }
    }

    public String getJspId() {
        return jspId;
    }

    // ------------------------------------------------------- Abstract methods

    /**
     * <p>Override properties and attributes of the specified component,
     * if the corresponding properties of this tag handler instance were
     * explicitly set.  This method must be called <strong>ONLY</strong>
     * if the specified {@link UIComponent} was in fact created during
     * the execution of this tag handler instance, and this call will occur
     * <strong>BEFORE</strong> the {@link UIComponent} is added to
     * the view.</p>
     *
     * <p>Tag subclasses that want to support additional set properties
     * must ensure that the base class <code>setProperties()</code>
     * method is still called.  A typical implementation that supports
     * extra properties <code>foo</code> and <code>bar</code> would look
     * something like this:</p>
     * <pre>
     * protected void setProperties(UIComponent component) {
     *   super.setProperties(component);
     *   if (foo != null) {
     *     component.setAttribute("foo", foo);
     *   }
     *   if (bar != null) {
     *     component.setAttribute("bar", bar);
     *   }
     * }
     * </pre>
     *
     * <p>The default implementation overrides the following properties:</p>
     * <ul>
     * <li><code>rendered</code> - Set if a value for the
     *     <code>rendered</code> property is specified for
     *     this tag handler instance.</li>
     * <li><code>rendererType</code> - Set if the <code>getRendererType()</code>
     *     method returns a non-null value.</li>
     * </ul>
     *
     * @param component {@link UIComponent} whose properties are to be
     *  overridden
     */
    protected abstract void setProperties(UIComponent component);


    /**
     * <p>Create and return a new child component of the type returned
     * by calling <code>getComponentType()</code>.  If this {@link
     * UIComponentTag} has a non-null <code>binding</code> attribute,
     * this is done by call {@link Application#createComponent} with the
     * expression created for the <code>binding</code> attribute, and
     * the expression will be stored on the component.  Otherwise,
     * {@link Application#createComponent} is called with only the
     * component type.  Finally, initialize the components id and other
     * properties.  </p>
     * @param context {@link FacesContext} for the current request
     * @param newId id of the component
     *
     * @return the created component
     *
     * @throws JspException if the component cannot be created
     */

    protected abstract UIComponent createComponent(FacesContext context,
                                                   String newId) throws JspException;

    /**
     * <p>Return <code>true</code> if this component has a
     * non-<code>null</code> binding attribute.  This method is
     * necessary to allow subclasses that expose the
     * <code>binding</code> property as an Faces 1.1 style Expression Language property
     * as well as subclasses that expose it as a Jakarta Expression Language API property.</p>
     *
     * @return whether or not this component has a binding attribute
     */

    protected abstract boolean hasBinding();

    // --------------------------------------------------------- Properties

    /**
     * <p>Return the {@link UIComponent} instance that is associated with
     * this tag instance.  This method is designed to be used by tags nested
     * within this tag, and only returns useful results between the
     * execution of <code>doStartTag()</code> and <code>doEndTag()</code>
     * on this tag instance.</p>
     */
    @Override
    public UIComponent getComponentInstance() {

        return (this.component);

    }


    /**
     * <p>Return <code>true</code> if we dynamically created a new component
     * instance during execution of this tag.  This method is designed to be
     * used by tags nested within this tag, and only returns useful results
     * between the execution of <code>doStartTag()</code> and
     * <code>doEndTag()</code> on this tag instance.</p>
     */
    @Override
    public boolean getCreated() { // NOPMD

        return (this.created);

    }

    private Map getNamingContainerChildIds() {
        return (this.namingContainerChildIds);
    }

    @Override
    protected FacesContext getFacesContext() {

        if (context == null) {
            if (null == (context = (FacesContext)
                    pageContext.getAttribute(CURRENT_FACES_CONTEXT))) {
                context = FacesContext.getCurrentInstance();

                if (context == null) { // PENDING - i18n
                    throw new RuntimeException("Cannot find FacesContext");
                }

                // store the current FacesContext for use by other
                // UIComponentTags in the same page
                pageContext.setAttribute(CURRENT_FACES_CONTEXT, context);
            }
        }

        return (context);

    }


    /**
     * <p>Return the facet name that we should be stored under, if any;
     * otherwise, return null (indicating that we will be a child component).
     * </p>
     *
     * @return the name of the facet
     */
    protected String getFacetName() {

        Tag parent = getParent();
        if (parent instanceof FacetTag) {
            return (((FacetTag) parent).getName());
        } else {
            return (null);
        }

    }


    private static FacesContext getFacesContext(PageContext pageContext) {

        FacesContext context = (FacesContext)
                pageContext.getAttribute(CURRENT_FACES_CONTEXT);
        if (context == null) {
            context = FacesContext.getCurrentInstance();
            if (context == null) {
                throw new RuntimeException("Cannot find FacesContext");
            } else {
                pageContext.setAttribute(CURRENT_FACES_CONTEXT, context);
            }
        }

        return (context);

    }

    private static void printTree(UIComponent root,
                                  String duplicateId,
                                  Writer out,
                                  int curDepth) {
        if (null == root) {
            return;
        }

        if (duplicateId.equals(root.getId())) {
            indentPrintln(out, "+id: " + root.getId() + "  <===============",
                    curDepth);
        } else {
            indentPrintln(out, "+id: " + root.getId(), curDepth);
        }
        //noinspection ObjectToString
        indentPrintln(out, " type: " + root.toString(), curDepth);

        curDepth++;
        // print all the facets of this component
        for (UIComponent uiComponent : root.getFacets().values()) {
            printTree(uiComponent, duplicateId, out, curDepth);
        }
        // print all the children of this component
        for (UIComponent uiComponent : root.getChildren()) {
            printTree(uiComponent, duplicateId, out, curDepth);
        }

    }

    private static void indentPrintln(Writer out, String str, int curDepth) {

        // handle indentation
        try {
            for (int i = 0; i < curDepth; i++) {
                out.write("  ");
            }
            out.write(str + '\n');
        } catch (IOException ex) {
            // ignore
        }
    }

}
