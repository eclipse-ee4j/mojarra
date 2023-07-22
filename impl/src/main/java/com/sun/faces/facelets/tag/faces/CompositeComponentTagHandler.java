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

package com.sun.faces.facelets.tag.faces;

import static com.sun.faces.facelets.tag.faces.ComponentSupport.getViewRoot;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.RIConstants;
import com.sun.faces.facelets.el.VariableMapperWrapper;
import com.sun.faces.facelets.tag.MetaRulesetImpl;
import com.sun.faces.facelets.tag.MetadataTargetImpl;
import com.sun.faces.facelets.tag.faces.ComponentTagHandlerDelegateImpl.CreateComponentDelegate;
import com.sun.faces.facelets.util.ReflectionUtil;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.el.ELException;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.component.UISelectOne;
import jakarta.faces.component.UniqueIdVendor;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.AttachedObjectHandler;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewDeclarationLanguageFactory;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;

/**
 * <p>
 * Facelet handler responsible for, building the component tree representation of a composite component based on the
 * metadata contained in the composite interface and implementation sections of the composite component template.
 * </p>
 */
public class CompositeComponentTagHandler extends ComponentHandler implements CreateComponentDelegate {

    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();
    private Resource ccResource;
    private TagAttribute binding;

    // ------------------------------------------------------------ Constructors

    public CompositeComponentTagHandler(Resource ccResource, ComponentConfig config) {
        super(config);
        this.ccResource = ccResource;
        binding = config.getTag().getAttributes().get("binding");
        ((ComponentTagHandlerDelegateImpl) getTagHandlerDelegate()).setCreateCompositeComponentDelegate(this);
    }

    // ------------------------------------ Methods from CreateComponentDelegate

    @Override
    public UIComponent createComponent(FaceletContext ctx) {

        FacesContext context = ctx.getFacesContext();
        UIComponent cc;
        // we have to handle the binding here, as Application doesn't
        // expose a method to do so with Resource.
        if (binding != null) {
            ValueExpression ve = binding.getValueExpression(ctx, UIComponent.class);
            cc = (UIComponent) ve.getValue(ctx);
            if (cc != null && !UIComponent.isCompositeComponent(cc)) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "faces.compcomp.binding.eval.non.compcomp", binding.toString());
                }
                cc = null;
            }
            if (cc == null) {
                cc = context.getApplication().createComponent(context, ccResource);
                cc.setValueExpression("binding", ve);
                ve.setValue(ctx, cc);
            }
        } else {
            cc = context.getApplication().createComponent(context, ccResource);
        }
        context.getViewRoot().getAttributes().put(RIConstants.TREE_HAS_DYNAMIC_COMPONENTS, Boolean.TRUE);
        setCompositeComponent(context, cc);

        return cc;

    }

    // ------------------------------------------- Methods from ComponentHandler

    @Override
    public void applyNextHandler(FaceletContext ctx, UIComponent c) throws IOException, FacesException, ELException {

        // attributes need to be applied before any action is taken on
        // nested children handlers or the composite component handlers
        // as there may be an expression evaluated at tree creation time
        // that needs access to these attributes
        setAttributes(ctx, c);

        // Allow any nested elements that reside inside the markup element
        // for this tag to get applied
        super.applyNextHandler(ctx, c);

        // Apply the facelet for this composite component
        applyCompositeComponent(ctx, c);

        // Allow any PDL declared attached objects to be retargeted
        if (ComponentHandler.isNew(c)) {
            FacesContext context = ctx.getFacesContext();
            String viewId = context.getViewRoot().getViewId();
            // PENDING(rlubke): performance
            ViewDeclarationLanguageFactory factory = (ViewDeclarationLanguageFactory) FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY);

            ViewDeclarationLanguage vdl = factory.getViewDeclarationLanguage(viewId);
            vdl.retargetAttachedObjects(context, c, getAttachedObjectHandlers(c, false));
            vdl.retargetMethodExpressions(context, c);
            getAttachedObjectHandlers(c).clear();

//            getAttachedObjectHandlers(c, false).clear();
        }

    }

    // The value of this string, prepended to this.tagId, is used as a
    // key in the FacesContext attributes map, the value for which is
    // the UIComponent that formerly was stored in an instance variable called
    // cc.
    private static final String ccInstanceVariableStandinKey = CompositeComponentTagHandler.class.getName() + "_";

    @Override
    public void setCompositeComponent(FacesContext context, UIComponent cc) {
        Map contextMap = context.getAttributes();
        String key = ccInstanceVariableStandinKey + tagId;
        if (!contextMap.containsKey(key)) {
            contextMap.put(key, cc);
        }
    }

    @Override
    public UIComponent getCompositeComponent(FacesContext context) {
        Map contextMap = context.getAttributes();
        String key = ccInstanceVariableStandinKey + tagId;
        UIComponent result = (UIComponent) contextMap.get(key);

        return result;
    }

    /**
     * Specialized implementation to prevent caching of the MetaRuleset when ProjectStage is Development.
     */
    @Override
    public void setAttributes(FaceletContext ctx, Object instance) {

        if (instance != null) {
            if (ctx.getFacesContext().isProjectStage(ProjectStage.Development)) {
                Metadata meta = createMetaRuleset(instance.getClass()).finish();
                meta.applyMetadata(ctx, instance);
            } else {
                super.setAttributes(ctx, instance);
            }
        }

    }

    /**
     * This is basically a copy of what's define in ComponentTagHandlerDelegateImpl except for the MetaRuleset
     * implementation that's being used.
     *
     * This also allows us to treat composite component's backed by custom component implementation classes based on their
     * type.
     *
     * @param type the <code>Class</code> for which the <code>MetaRuleset</code> must be created.
     * @return
     *
     */
    @Override
    protected MetaRuleset createMetaRuleset(Class type) {

        Util.notNull("type", type);
        FacesContext context = FacesContext.getCurrentInstance();
        UIComponent cc = getCompositeComponent(context);
        if (null == cc) {
            FaceletContext faceletContext = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
            cc = createComponent(faceletContext);
            setCompositeComponent(context, cc);

        }
        MetaRuleset m = new CompositeComponentMetaRuleset(getTag(), type, (BeanInfo) cc.getAttributes().get(UIComponent.BEANINFO_KEY));

        // ignore standard component attributes
        m.ignore("binding").ignore("id");

        m.addRule(CompositeComponentRule.Instance);

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

    // ---------------------------------------------------------- Public Methods

    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component) {

        return getAttachedObjectHandlers(component, true);

    }

    /**
     * <p class="changed_added_2_2">
     * The key in the value set of the <em>composite component <code>BeanDescriptor</code></em>, the value for which is a
     * <code>List&lt;AttachedObjectHandler&gt;</code>.
     * </p>
     */
    private static final String ATTACHED_OBJECT_HANDLERS_KEY = "jakarta.faces.view.AttachedObjectHandlers";

    @SuppressWarnings({ "unchecked" })
    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component, boolean create) {
        Map<String, Object> attrs = component.getAttributes();
        List<AttachedObjectHandler> result = (List<AttachedObjectHandler>) attrs.get(ATTACHED_OBJECT_HANDLERS_KEY);

        if (result == null) {
            if (create) {
                result = new ArrayList<>();
                attrs.put(ATTACHED_OBJECT_HANDLERS_KEY, result);
            } else {
                result = Collections.EMPTY_LIST;
            }
        }
        return result;

    }

    // --------------------------------------------------------- Private Methods

    private void applyCompositeComponent(FaceletContext ctx, UIComponent c) throws IOException {

        FacesContext facesContext = ctx.getFacesContext();
        VariableMapper orig = ctx.getVariableMapper();

        UIPanel facetComponent;
        if (ComponentHandler.isNew(c)) {
            facetComponent = (UIPanel) facesContext.getApplication().createComponent("jakarta.faces.Panel");
            facetComponent.setRendererType("jakarta.faces.Group");
            facetComponent.setId((c instanceof UniqueIdVendor ? (UniqueIdVendor) c : getViewRoot(ctx, c)).createUniqueId(facesContext, null));
            c.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, facetComponent);
        } else {
            facetComponent = (UIPanel) c.getFacets().get(UIComponent.COMPOSITE_FACET_NAME);
        }
        assert null != facetComponent;

        try {
            VariableMapper wrapper = new VariableMapperWrapper(orig) {

                @Override
                public ValueExpression resolveVariable(String variable) {
                    return super.resolveVariable(variable);
                }

            };
            ctx.setVariableMapper(wrapper);

            /*
             * We need to use includeFacelet because our facelet component map expects each Facelet component to generate a unique
             * id (MARK_ID).
             */
            ctx.includeFacelet(facetComponent, ccResource.getURL());
        } finally {
            ctx.setVariableMapper(orig);
        }
    }

    // ---------------------------------------------------------- Nested Classes

    /**
     * Specialized MetaRulesetImpl to return CompositeMetadataTarget for component attribute handling.
     */
    private static final class CompositeComponentMetaRuleset extends MetaRulesetImpl {

        private BeanInfo compBeanInfo;
        private Class<?> type;

        public CompositeComponentMetaRuleset(Tag tag, Class<?> type, BeanInfo compBeanInfo) {

            super(tag, type);
            this.compBeanInfo = compBeanInfo;
            this.type = type;

        }

        @Override
        protected MetadataTarget getMetadataTarget() {
            try {
                return new CompositeMetadataTarget(type, compBeanInfo);
            } catch (IntrospectionException ie) {
                throw new FacesException(ie);
            }
        }

        // ------------------------------------------------------ Nested Classes

        /**
         * This class is responsible for creating ValueExpression instances with the expected type based off the following:
         *
         * - if the composite:attribute metadata is present, then use the type if specified by the author, or default to
         * Object.class - if no composite:attribute is specified, then attempt to return the type based off the bean info for
         * this component
         */
        private static final class CompositeMetadataTarget extends MetadataTargetImpl {

            private BeanInfo compBeanInfo;

            // ---------------------------------------------------- Construcrors

            public CompositeMetadataTarget(Class<?> type, BeanInfo compBeanInfo) throws IntrospectionException {

                super(type);
                this.compBeanInfo = compBeanInfo;

            }

            // --------------------------------- Methods from MetadataTargetImpl

            @Override
            public Class getPropertyType(String name) {
                PropertyDescriptor compDescriptor = findDescriptor(name);
                if (compDescriptor != null) {
                    // composite:attribute declaration...
                    Object obj = compDescriptor.getValue("type");
                    if (null != obj && !(obj instanceof Class)) {
                        ValueExpression typeVE = (ValueExpression) obj;
                        String className = (String) typeVE.getValue(FacesContext.getCurrentInstance().getELContext());
                        if (className != null) {
                            className = prefix(className);
                            try {
                                return ReflectionUtil.forName(className);
                            } catch (ClassNotFoundException cnfe) {
                                throw new FacesException(cnfe);
                            }
                        } else {
                            return Object.class;
                        }
                    } else {
                        return (Class<?>) obj;
                    }
                } else {
                    // defer to the default processing which will inspect the
                    // PropertyDescriptor of the UIComponent type
                    return super.getPropertyType(name);
                }
            }

            // ------------------------------------------------- Private Methods

            private PropertyDescriptor findDescriptor(String name) {

                for (PropertyDescriptor pd : compBeanInfo.getPropertyDescriptors()) {

                    if (pd.getName().equals(name)) {
                        return pd;
                    }

                }
                return null;

            }

            private String prefix(String className) {

                if (className.indexOf('.') == -1 && Character.isUpperCase(className.charAt(0))) {
                    return "java.lang." + className;
                } else {
                    return className;
                }

            }
        }

    } // END CompositeComponentMetaRuleset

    /**
     * <code>MetaRule</code> for populating the ValueExpression map of a composite component.
     */
    private static class CompositeComponentRule extends MetaRule {

        private static final CompositeComponentRule Instance = new CompositeComponentRule();

        // ------------------------------------------ Methods from ComponentRule

        @Override
        public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta) {

            if (meta.isTargetInstanceOf(UIComponent.class)) {
                Class<?> type = meta.getPropertyType(name);
                if (type == null) {
                    type = Object.class;
                }

                if (!attribute.isLiteral()) {
                    return new CompositeExpressionMetadata(name, type, attribute);
                } else {
                    return new LiteralAttributeMetadata(name, type, attribute);
                }
            }
            return null;

        }

        // ------------------------------------------------------ Nested Classes

        /**
         * For literal expressions, coerce the literal value to the type as provided to the constructor prior to setting the
         * value into the component's attribute map.
         */
        private static final class LiteralAttributeMetadata extends Metadata {

            private String name;
            private Class<?> type;
            private TagAttribute attribute;

            // ---------------------------------------------------- Constructors

            public LiteralAttributeMetadata(String name, Class<?> type, TagAttribute attribute) {

                this.name = name;
                this.type = type;
                this.attribute = attribute;

            }

            // ------------------------------------------- Methods from Metadata

            @Override
            public void applyMetadata(FaceletContext ctx, Object instance) {

                UIComponent c = (UIComponent) instance;
                Object value = attribute.getObject(ctx, type);
                // don't set the attributes value in the components attributemap
                // if it is null, as this will throw a NullPointerException.
                if (value != null) {
                    c.getAttributes().put(name, value);
                }

            }

        } // END LiteralAttributeMetadata

        /**
         * CompositeExpressionMetadata sets up specialized wrapper ValueExpression instances around the source ValueExpression
         * that, when evaluated, will cause the parent composite component of the currently available composite component to be
         * pushed onto a stack that the ImplicitObjectELResolver will check for.
         */
        private static final class CompositeExpressionMetadata extends Metadata {

            private String name;
            private Class<?> type;
            private TagAttribute attr;

            // ---------------------------------------------------- Constructors

            public CompositeExpressionMetadata(String name, Class<?> type, TagAttribute attr) {
                this.name = name;
                this.type = type;
                this.attr = attr;

            }

            // ------------------------------------------- Methods from Metadata

            @Override
            public void applyMetadata(FaceletContext ctx, Object instance) {

                ValueExpression ve = attr.getValueExpression(ctx, type);
                UIComponent cc = (UIComponent) instance;
                assert UIComponent.isCompositeComponent(cc);
                Map<String, Object> attrs = cc.getAttributes();
                BeanInfo componentMetadata = (BeanInfo) attrs.get(UIComponent.BEANINFO_KEY);
                BeanDescriptor desc = componentMetadata.getBeanDescriptor();
                Collection<String> attributesWithDeclaredDefaultValues = (Collection<String>) desc.getValue(UIComponent.ATTRS_WITH_DECLARED_DEFAULT_VALUES);
                if (null != attributesWithDeclaredDefaultValues && attributesWithDeclaredDefaultValues.contains(name) && attrs.containsKey(name)) {
                    // It is necessary to remove the value from the attribute
                    // map because the ELexpression transparancy doesn't know
                    // about the value's existence.
                    attrs.remove(name);
                }
                cc.setValueExpression(name, ve);

            }

        } // END CompositeExpressionMetadata

    } // END CompositeComponentRule

}
