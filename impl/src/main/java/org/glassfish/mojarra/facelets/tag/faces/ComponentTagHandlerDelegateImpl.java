/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.mojarra.facelets.tag.faces;

import static org.glassfish.mojarra.component.CompositeComponentStackManager.StackType.TreeCreation;
import static org.glassfish.mojarra.facelets.tag.faces.ComponentSupport.DYNAMIC_COMPONENT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.UniqueIdVendor;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandlerDelegate;

import org.glassfish.mojarra.component.CompositeComponentStackManager;
import org.glassfish.mojarra.component.behavior.AjaxBehaviors;
import org.glassfish.mojarra.component.validator.ComponentValidators;
import org.glassfish.mojarra.context.StateContext;
import org.glassfish.mojarra.facelets.UniqueIdSlot;
import org.glassfish.mojarra.facelets.tag.BuildTimeDecisions;
import org.glassfish.mojarra.facelets.tag.MetaRulesetImpl;
import org.glassfish.mojarra.facelets.tag.SavedBuildTimeDecisions;
import org.glassfish.mojarra.facelets.tag.faces.core.FacetHandler;
import org.glassfish.mojarra.util.FacesLogger;
import org.glassfish.mojarra.util.Util;

public class ComponentTagHandlerDelegateImpl extends TagHandlerDelegate {

    private ComponentHandler owner;

    private final static Logger log = FacesLogger.FACELETS_COMPONENT.getLogger();

    private final TagAttribute binding;

    protected String componentType;

    protected final TagAttribute id;

    private final String rendererType;

    private CreateComponentDelegate createCompositeComponentDelegate;

    /**
     * This tag's unique-id counter slot and the Facelet holding it, so {@link #apply} can count this tag's generated
     * ids by array index instead of by a per-build map lookup on the tag id. Held as one object so that both are read
     * together: a build that saw the owner must see that owner's slot. Bound to the Facelet being applied rather than
     * the one this tag was compiled in, because a {@code ui:define} body applies under the template it is inserted
     * into; a tag that alternates between templates simply rebinds.
     */
    private final UniqueIdSlot idSlot = new UniqueIdSlot();

    public ComponentTagHandlerDelegateImpl(ComponentHandler owner) {
        this.owner = owner;
        ComponentConfig config = owner.getComponentConfig();
        componentType = config.getComponentType();
        rendererType = config.getRendererType();
        id = owner.getTagAttribute("id");
        binding = owner.getTagAttribute("binding");

    }

    /**
     * Method handles UIComponent tree creation in accordance with the Faces 1.2 spec.
     * <ol>
     * <li>First determines this UIComponent's id by calling
     * {@link jakarta.faces.view.facelets.ComponentHandler#getTagId()}.</li>
     * <li>Search the parent for an existing UIComponent of the id we just grabbed</li>
     * <li>If found,
     * {@link org.glassfish.mojarra.facelets.tag.faces.ComponentSupport#markForDeletion(jakarta.faces.component.UIComponent) mark} its
     * children for deletion.</li>
     * <li>If <i>not</i> found, call {@link #createComponent(FaceletContext) createComponent}.
     * <ol>
     * <li>Only here do we apply
     * {@link org.glassfish.mojarra.facelets.tag.MetaTagHandlerImpl#setAttributes(FaceletContext, Object)}</li>
     * <li>Set the UIComponent's id</li>
     * <li>Set the RendererType of this instance</li>
     * </ol>
     * </li>
     * <li>Now apply the nextHandler, passing the UIComponent we've created/found.</li>
     * <li>Now add the UIComponent to the passed parent</li>
     * <li>Lastly, if the UIComponent already existed (found), then {@link ComponentSupport#finalizeForDeletion(UIComponent)
     * finalize} for deletion.</li>
     * </ol>
     *
     * @throws TagException if the UIComponent parent is null
     */

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        FacesContext context = ctx.getFacesContext();

        // make sure our parent is not null
        if (parent == null) {
            throw new TagException(owner.getTag(), "Parent UIComponent was null");
        }

        // our id
        String id = generateUniqueId(ctx);

        // grab our component
        UIComponent c = findChild(ctx, parent, id);
        // Reparenting only ever applies to a child of a composite component, so that test gates the costlier ones.
        if (null == c && UIComponent.isCompositeComponent(parent) && context.isPostback() && parent.getAttributes().get(id) != null) {
            c = findReparentedComponent(ctx, parent, id);
        } else {
            /**
             * If we found a child that is dynamic, the actual parent might have changed, so we need to remove it from the actual
             * parent. The reapplyDynamicActions will then replay the actions and will make sure it ends up in the correct order.
             */
            if (c != null && c.getParent() != parent && c.getAttributes().containsKey(DYNAMIC_COMPONENT)) {
                c.getParent().getChildren().remove(c);
            }
        }

        boolean componentFound = false;
        boolean parentModified = false;
        if (c != null) {
            componentFound = true;
            doExistingComponentActions(ctx, id, c);
        } else if (suppressRemovedChild(parent, id)) {
            return;
        } else {
            // hook method
            c = owner.createComponent(ctx);
            if (c == null) {
                c = createComponent(ctx);
            }

            doNewComponentActions(ctx, id, c);
            assignUniqueId(ctx, parent, id, c);

            // hook method
            owner.onComponentCreated(ctx, c, parent);
        }

        CompositeComponentStackManager ccStackManager = CompositeComponentStackManager.getManager(context);
        StateContext stateContext = StateContext.getStateContext(context);
        boolean compcompPushed = pushComponentToEL(ctx, c, ccStackManager);

        if (ProjectStage.Development == context.getApplication().getProjectStage()) {
            ComponentSupport.setTagForComponent(context, c, owner.getTag());
        }

        // If this this a naming container, stop generating unique Ids
        // for the repeated tags
        boolean isNaming = false;
        if (c instanceof NamingContainer) {
            isNaming = true;
            IterationIdManager.startNamingContainer(ctx);
        }

        // Refresh-gate (see ComponentSupport.BUILDING_FRESH_SUBTREE): a freshly created component with no
        // children yet begins an all-new subtree, so its descendants have nothing existing to reuse and
        // findChildByTagId can skip scanning. A found or binding-supplied component (which arrives with its
        // children already attached) may contain components to reuse, so the scan stays active for its subtree.
        boolean freshSubtree = !componentFound && c.getChildCount() == 0;
        Map<Object, Object> contextAttributes = context.getAttributes();
        Object previousFreshSubtree = contextAttributes.get(ComponentSupport.BUILDING_FRESH_SUBTREE);
        // The flag holds for a whole subtree, so it only has to be written when it actually flips.
        boolean flipped = !Objects.equals(previousFreshSubtree, freshSubtree);
        if (flipped) {
            contextAttributes.put(ComponentSupport.BUILDING_FRESH_SUBTREE, freshSubtree);
        }
        try {
            // first allow c to get populated
            owner.applyNextHandler(ctx, c);
        } finally {
            if (flipped) {
                if (previousFreshSubtree != null) {
                    contextAttributes.put(ComponentSupport.BUILDING_FRESH_SUBTREE, previousFreshSubtree);
                } else {
                    contextAttributes.remove(ComponentSupport.BUILDING_FRESH_SUBTREE);
                }
            }
            if (isNaming) {
                IterationIdManager.stopNamingContainer(ctx);
            }
        }

        // finish cleaning up orphaned children
        if (componentFound) {
            parentModified = isParentChildrenModified(parent);
            doOrphanedChildCleanup(ctx, parent, c, parentModified);
        }

        privateOnComponentPopulated(ctx, c);
        owner.onComponentPopulated(ctx, c, parent);
        // add to the tree afterwards
        // this allows children to determine if it's
        // been part of the tree or not yet
        addComponentToView(ctx, parent, c, componentFound, parentModified, stateContext);
        ComponentSupport.copyPassthroughAttributes(ctx, c, owner.getTag());
        adjustIndexOfDynamicChildren(stateContext, c);
        popComponentFromEL(ctx, c, ccStackManager, compcompPushed);
    }

    protected boolean isIterating(FaceletContext context) {
        return IterationIdManager.isIterating(context);
    }

    // Tests whether the component associated with the specified tagId was
    // a child of the parent component that has been dynamically removed. If
    // so, we want to suppress re-creation of this child
    @SuppressWarnings("unchecked")
    private boolean suppressRemovedChild(UIComponent parent, String childTagId) {
        Collection<String> removedChildren = (Collection<String>) parent.getAttributes().get(ComponentSupport.REMOVED_CHILDREN);
        return removedChildren != null && removedChildren.contains(childTagId);
    }

    // Tests whether the specified parent component has had any dynamic
    // child additions or removals. If so, we avoid re-ordering its children
    // during tag re-execution, since we want to preserve the dynamically
    // specified order.
    private boolean isParentChildrenModified(UIComponent parent) {
        return parent.getAttributes().get(ComponentSupport.MARK_CHILDREN_MODIFIED) != null;
    }

    private void adjustIndexOfDynamicChildren(StateContext stateContext, UIComponent parent) {
        if (!stateContext.hasOneOrMoreDynamicChild(parent)) {
            return;
        }

        List<UIComponent> children = parent.getChildren();
        List<UIComponent> dynamicChildren = Collections.emptyList();
        boolean allInPlace = true;

        int index = 0;
        for (UIComponent cur : children) {
            if (stateContext.componentAddedDynamically(cur)) {
                if (dynamicChildren.isEmpty()) {
                    dynamicChildren = new ArrayList<>(children.size());
                }
                dynamicChildren.add(cur);
                int stored = stateContext.getIndexOfDynamicallyAddedChildInParent(cur);
                if (stored != -1 && stored != index) {
                    allInPlace = false;
                }
            }
            index++;
        }

        // reapplyDynamicActions already puts every dynamic child at its stored index, so in the steady state the
        // remove-all/re-add-all below just reproduces the current list -- at O(dynamic * children) cost, i.e. O(n^2)
        // for an all-dynamic parent (a large dynamically populated container). Skip it when nothing is out of place.
        if (allInPlace) {
            return;
        }

        // First remove all the dynamic children, this puts the non-dynamic children at
        // their original position
        for (UIComponent cur : dynamicChildren) {
            int i = stateContext.getIndexOfDynamicallyAddedChildInParent(cur);
            if (-1 != i) {
                children.remove(cur);
            }
        }

        // Now that the non-dynamic children are in the correct position add the dynamic children
        // back in.
        for (UIComponent cur : dynamicChildren) {
            int i = stateContext.getIndexOfDynamicallyAddedChildInParent(cur);
            if (-1 != i) {
                if (i < children.size()) {
                    children.add(i, cur);
                } else {
                    children.add(cur);
                }
            }
        }
    }

    @Override
    public MetaRuleset createMetaRuleset(Class<?> type) {
        Util.notNull("type", type);
        MetaRuleset m = new MetaRulesetImpl(owner.getTag(), type);

        // ignore standard component attributes
        m.ignore("binding").ignore("id");

        // add auto wiring for attributes
        m.addRule(ComponentRule.Instance);

        // if it's an ActionSource
        if (ActionSource.class.isAssignableFrom(type)) {
            m.addRule(ActionSourceRule.Instance);
        }

        // if it's a ValueHolder
        if (ValueHolder.class.isAssignableFrom(type)) {
            m.addRule(ValueHolderRule.Instance);

            // if it's an EditableValueHolder
            if (EditableValueHolder.class.isAssignableFrom(type)) {
                m.ignore("submittedValue");
                m.ignore("valid");
                m.addRule(EditableValueHolderRule.Instance);
            }
        }

        // if it's a selectone or selectmany
        if (UISelectOne.class.isAssignableFrom(type) || UISelectMany.class.isAssignableFrom(type)) {
            m.addRule(RenderPropertyRule.Instance);
        }

        return m;
    }

    // ------------------------------------------------------- Protected Methods

    private void addComponentToView(FaceletContext ctx, UIComponent parent, UIComponent c, boolean componentFound, boolean parentModified,
            StateContext stateContext) {
        if (!componentFound || !parentModified) {
            addComponentToView(ctx, parent, c, componentFound, stateContext);
        }
    }

    protected void addComponentToView(FaceletContext ctx, UIComponent parent, UIComponent c, boolean componentFound, StateContext stateContext) {

        FacesContext context = ctx.getFacesContext();
        boolean suppressEvents = ComponentSupport.suppressViewModificationEvents(context, stateContext);
        boolean compcomp = UIComponent.isCompositeComponent(c);

        if (suppressEvents && componentFound && !compcomp) {
            context.setProcessingEvents(false);
        }

        ComponentSupport.addComponent(ctx, parent, c);

        if (suppressEvents && componentFound && !compcomp) {
            context.setProcessingEvents(true);
        }

    }

    protected boolean pushComponentToEL(FaceletContext ctx, UIComponent c, CompositeComponentStackManager ccStackManager) {

        c.pushComponentToEL(ctx.getFacesContext(), c);
        boolean compcompPushed = false;

        if (UIComponent.isCompositeComponent(c)) {
            compcompPushed = ccStackManager.push(c, TreeCreation);
        }
        return compcompPushed;

    }

    protected void popComponentFromEL(FaceletContext ctx, UIComponent c, CompositeComponentStackManager ccStackManager, boolean compCompPushed) {

        c.popComponentFromEL(ctx.getFacesContext());
        if (compCompPushed) {
            ccStackManager.pop(TreeCreation);
        }

    }

    private void doOrphanedChildCleanup(FaceletContext ctx, UIComponent parent, UIComponent c, boolean parentModified) {
        if (parentModified) {
            ComponentSupport.finalizeForDeletion(c);
        } else {
            doOrphanedChildCleanup(ctx, parent, c);
        }
    }

    protected void doOrphanedChildCleanup(FaceletContext ctx, UIComponent parent, UIComponent c) {

        ComponentSupport.finalizeForDeletion(c);
        if (getFacetName(parent) == null) {
            FacesContext context = ctx.getFacesContext();
            boolean suppressEvents = ComponentSupport.suppressViewModificationEvents(context);

            if (suppressEvents) {
                // if the component has already been found, it will be removed
                // and added back to the view. We don't want to publish events
                // for this case.
                context.setProcessingEvents(false);
            }
            // suppress the remove event for this case since it will be re-added
            parent.getChildren().remove(c);
            if (suppressEvents) {
                // re-enable event processing
                context.setProcessingEvents(true);
            }
        }

    }

    protected void assignUniqueId(FaceletContext ctx, UIComponent parent, String id, UIComponent c) {

        // If the id is specified as a literal, and the component is being
        // repeated (by c:forEach, for example), use generated unique ids
        // after the first instance

        if (this.id != null && !(this.id.isLiteral() && IterationIdManager.registerLiteralId(ctx, this.id.getValue()))) {
            c.setId(resolveId(ctx, id));
        } else {
            String mid = ComponentSupport.getAliasedId(ctx, id);
            UIComponent ancestorNamingContainer = parent.getNamingContainer();
            if (ancestorNamingContainer instanceof UniqueIdVendor) {
                c.setId(((UniqueIdVendor) ancestorNamingContainer).createUniqueId(ctx.getFacesContext(), mid));
            } else {
                // No UniqueIdVendor ancestor: fall back to the view root. getViewRoot walks the parent chain,
                // so resolve it only on this branch instead of unconditionally for every component.
                UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
                if (root != null) {
                    c.setId(root.createUniqueId(ctx.getFacesContext(), mid));
                }
            }
        }

        if (rendererType != null) {
            c.setRendererType(rendererType);
        }

    }

    /**
     * The id this tag assigns, which an expression can decide per build. Where build time decisions are replayed, the
     * build which restores a view assigns the id the build which rendered it assigned, so that the state saved for a
     * component is restored into the component it was saved for, and every build after that decides for itself again:
     * the decision recorded beside it denies the skip of the render time re-apply once the expression yields another
     * id than the one this build assigned.
     *
     * <p>
     * Nothing of this happens where the decisions are not replayed: the decision would then deny the skip of that
     * re-apply for every build whose body holds a component with an expression for an id, which is what an unrolled
     * iteration naming its rows produces, while there is no replayed id for it to reconcile.
     *
     * <p>
     * The attribute evaluates itself rather than the expression recorded beside it, so that an expression which fails
     * to evaluate reports the tag it belongs to.
     *
     * @param ctx the {@link FaceletContext} for the current build.
     * @param tagId the id this tag generated for the current build, under which its decision is saved and replayed.
     * @return the id this tag assigns.
     */
    private String resolveId(FaceletContext ctx, String tagId) {
        FacesContext context = ctx.getFacesContext();

        if (id.isLiteral() || !SavedBuildTimeDecisions.isEnabled(context)) {
            return id.getValue(ctx);
        }

        Object replayed = SavedBuildTimeDecisions.replay(context, tagId);
        String value = replayed instanceof String ? (String) replayed : id.getValue(ctx);

        if (value != null) {
            // A null decision is what a build with nothing to replay reads back, so saving one would say the id was
            // never decided here at all.
            SavedBuildTimeDecisions.record(context, tagId, value);
        }

        BuildTimeDecisions.record(context, id.getValueExpression(ctx, String.class), value);
        return value;
    }

    /**
     * Generates this tag's unique id through {@link org.glassfish.mojarra.facelets.impl.DefaultFaceletContext}'s
     * slot-based counter where the context supports it (the only implementation Facelets builds views with), and
     * through the public {@link FaceletContext} API otherwise, which a wrapping context or a foreign implementation
     * may supply.
     */
    private String generateUniqueId(FaceletContext ctx) {
        return idSlot.generateUniqueId(ctx, owner.getTagId());
    }

    protected void doNewComponentActions(FaceletContext ctx, String id, UIComponent c) {

        if (log.isLoggable(Level.FINE)) {
            log.fine(owner.getTag() + " Component[" + id + "] Created: " + c.getClass().getName());
        }
        // If this is NOT a composite component...
        if (null == createCompositeComponentDelegate) {
            // set the attributes and properties into the UIComponent instance.
            owner.setAttributes(ctx, c);
        }
        // otherwise, allow the composite component code to do it.

        // mark it owned by a facelet instance
        c.getAttributes().put(ComponentSupport.MARK_CREATED, id);

        if (ctx.getFacesContext().isProjectStage(ProjectStage.Development)) {
            // inject the location into the component
            c.getAttributes().put(UIComponent.VIEW_LOCATION_KEY, owner.getTag().getLocation());
        }

    }

    protected void doExistingComponentActions(FaceletContext ctx, String id, UIComponent c) {

        // mark all children for cleaning
        if (log.isLoggable(Level.FINE)) {
            log.fine(owner.getTag() + " Component[" + id + "] Found, marking children for cleanup");
        }
        ComponentSupport.markForDeletion(c);

        if (this.id != null) {
            /*
             * Note that registerLiteralId() needs to be called here regardless of whether we keep the code for reapplying Ids
             * below. This makes IterationIdManager aware of all literal Ids on the page, so that it can ensure Id uniqueness for
             * components added during postback.
             */
            boolean autoGenerated = this.id.isLiteral() && IterationIdManager.registerLiteralId(ctx, this.id.getValue());

            /*
             * Repply the id, for the case when the component tree was changed, and the id's are set explicitly.
             */
            if (!autoGenerated) {
                c.setId(resolveId(ctx, id));
            }
        }
    }

    protected UIComponent findChild(FaceletContext ctx, UIComponent parent, String tagId) {

        return ComponentSupport.findChildByTagId(ctx.getFacesContext(), parent, tagId);

    }

    protected UIComponent findReparentedComponent(FaceletContext ctx, UIComponent parent, String tagId) {
        UIComponent facet = parent.getFacets().get(UIComponent.COMPOSITE_FACET_NAME);
        if (facet != null) {
            // Relocated children may sit below intermediate containers in the implementation, so search deep.
            return ComponentSupport.findChildByTagIdDeep(ctx.getFacesContext(), facet, tagId);
        }
        return null;
    }

    // ------------------------------------------------- Package Private Methods

    void setCreateCompositeComponentDelegate(CreateComponentDelegate createComponentDelegate) {
        createCompositeComponentDelegate = createComponentDelegate;
    }

    /**
     * If the binding attribute was specified, use that in conjuction with our componentType String variable to call
     * createComponent on the Application, otherwise just pass the componentType String.
     * <p />
     * If the binding was used, then set the ValueExpression "binding" on the created UIComponent.
     *
     * @see Application#createComponent(jakarta.faces.el.ValueBinding, jakarta.faces.context.FacesContext, java.lang.String)
     * @see Application#createComponent(java.lang.String)
     * @param ctx FaceletContext to use in creating a component
     * @return
     */
    private UIComponent createComponent(FaceletContext ctx) {

        if (null != createCompositeComponentDelegate) {
            return createCompositeComponentDelegate.createComponent(ctx);
        }

        UIComponent c;
        FacesContext faces = ctx.getFacesContext();
        Application app = faces.getApplication();
        if (binding != null) {
            ValueExpression ve = binding.getValueExpression(ctx, Object.class);
            c = app.createComponent(ve, faces, componentType, rendererType);
            if (c != null) {
                // Make sure the component supports 1.2
                c.setValueExpression("binding", ve);
            }
        } else {
            c = app.createComponent(faces, componentType, rendererType);
        }
        return c;
    }

    /*
     * Internal hook that allows us to perform common processing for all components after they are populated. At the moment,
     * the only common processing we need to perform is applying wrapping AjaxBehaviors, if any exist.
     */
    private void privateOnComponentPopulated(FaceletContext ctx, UIComponent c) {

        if (c instanceof ClientBehaviorHolder) {
            FacesContext context = ctx.getFacesContext();
            AjaxBehaviors ajaxBehaviors = AjaxBehaviors.getAjaxBehaviors(context, false);
            if (ajaxBehaviors != null) {
                ajaxBehaviors.addBehaviors(context, (ClientBehaviorHolder) c);
            }
        }
        if (c instanceof EditableValueHolder) {
            processValidators(ctx.getFacesContext(), (EditableValueHolder) c);
        }
    }

    /**
     * Process default validatior/wrapping validation information and install <code>Validators</code> based off the result.
     */
    private void processValidators(FacesContext ctx, EditableValueHolder editableValueHolder) {

        ComponentValidators componentValidators = ComponentValidators.getValidators(ctx, false);
        if (componentValidators != null) {
            // process any elements on the stack.
            componentValidators.addValidators(ctx, editableValueHolder);
        } else {
            // no custom handling required, so add the default validators
            ComponentValidators.addDefaultValidatorsToComponent(ctx, editableValueHolder);
        }

    }

    /**
     * @return the Facet name we are scoped in, otherwise null
     */
    private String getFacetName(UIComponent parent) {
        return (String) parent.getAttributes().get(FacetHandler.KEY);
    }

    interface CreateComponentDelegate {

        UIComponent createComponent(FaceletContext ctx);

        void setCompositeComponent(FacesContext context, UIComponent cc);

        UIComponent getCompositeComponent(FacesContext context);

    }

}
