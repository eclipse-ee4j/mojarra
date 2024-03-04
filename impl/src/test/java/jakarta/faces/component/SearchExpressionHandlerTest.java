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

package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.component.search.CompositeSearchKeywordResolver;
import com.sun.faces.component.search.SearchExpressionContextFactoryImpl;
import com.sun.faces.component.search.SearchExpressionHandlerImpl;
import com.sun.faces.component.search.SearchKeywordResolverImplAll;
import com.sun.faces.component.search.SearchKeywordResolverImplChild;
import com.sun.faces.component.search.SearchKeywordResolverImplComposite;
import com.sun.faces.component.search.SearchKeywordResolverImplForm;
import com.sun.faces.component.search.SearchKeywordResolverImplId;
import com.sun.faces.component.search.SearchKeywordResolverImplNamingContainer;
import com.sun.faces.component.search.SearchKeywordResolverImplNext;
import com.sun.faces.component.search.SearchKeywordResolverImplNone;
import com.sun.faces.component.search.SearchKeywordResolverImplParent;
import com.sun.faces.component.search.SearchKeywordResolverImplPrevious;
import com.sun.faces.component.search.SearchKeywordResolverImplRoot;
import com.sun.faces.component.search.SearchKeywordResolverImplThis;
import com.sun.faces.component.visit.VisitContextFactoryImpl;
import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;

import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.component.search.ComponentNotFoundException;
import jakarta.faces.component.search.SearchExpressionContext;
import jakarta.faces.component.search.SearchExpressionHandler;
import jakarta.faces.component.search.SearchExpressionHint;
import jakarta.faces.component.search.SearchKeywordContext;
import jakarta.faces.component.search.SearchKeywordResolver;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

public class SearchExpressionHandlerTest extends JUnitFacesTestCaseBase {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.setViewId("/viewId");
        facesContext.setViewRoot(root);

        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = new MockRenderKit();
        try {
            renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, renderKit);
        } catch (IllegalArgumentException e) {
        }

        FactoryFinder.setFactory(FactoryFinder.SEARCH_EXPRESSION_CONTEXT_FACTORY, SearchExpressionContextFactoryImpl.class.getName());
        FactoryFinder.setFactory(FactoryFinder.VISIT_CONTEXT_FACTORY, VisitContextFactoryImpl.class.getName());

        SearchExpressionHandlerImpl expressionHandlerImpl = new SearchExpressionHandlerImpl();
        application.setSearchExpressionHandler(expressionHandlerImpl);

        CompositeSearchKeywordResolver searchKeywordResolvers = new CompositeSearchKeywordResolver();
        searchKeywordResolvers.add(new SearchKeywordResolverImplThis());
        searchKeywordResolvers.add(new SearchKeywordResolverImplParent());
        searchKeywordResolvers.add(new SearchKeywordResolverImplForm());
        searchKeywordResolvers.add(new SearchKeywordResolverImplComposite());
        searchKeywordResolvers.add(new SearchKeywordResolverImplNext());
        searchKeywordResolvers.add(new SearchKeywordResolverImplPrevious());
        searchKeywordResolvers.add(new SearchKeywordResolverImplNone());
        searchKeywordResolvers.add(new SearchKeywordResolverImplNamingContainer());
        searchKeywordResolvers.add(new SearchKeywordResolverImplRoot());
        searchKeywordResolvers.add(new SearchKeywordResolverImplId());
        searchKeywordResolvers.add(new SearchKeywordResolverImplChild());
        searchKeywordResolvers.add(new SearchKeywordResolverImplAll());
        application.setSearchKeywordResolver(searchKeywordResolvers);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private UIComponent resolveComponent(UIComponent source, String expression, SearchExpressionHint... hints) {

        SearchExpressionContext searchContext = SearchExpressionContext.createSearchExpressionContext(facesContext, source, new HashSet<>(Arrays.asList(hints)),
                null);

        ResolveComponentCallback callback = new ResolveComponentCallback();

        SearchExpressionHandler handler = FacesContext.getCurrentInstance().getApplication().getSearchExpressionHandler();

        handler.resolveComponent(searchContext, expression, callback);

        return callback.component;
    }

    private static class ResolveComponentCallback implements ContextCallback {
        public UIComponent component;

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target) {
            component = target;
        }
    }

    private String resolveClientId(UIComponent source, String expression) {
        SearchExpressionContext searchContext = SearchExpressionContext.createSearchExpressionContext(facesContext, source);
        SearchExpressionHandler handler = FacesContext.getCurrentInstance().getApplication().getSearchExpressionHandler();

        return handler.resolveClientId(searchContext, expression);
    }

    private List<UIComponent> resolveComponents(UIComponent source, String expression) {
        SearchExpressionContext searchContext = SearchExpressionContext.createSearchExpressionContext(facesContext, source);
        SearchExpressionHandler handler = FacesContext.getCurrentInstance().getApplication().getSearchExpressionHandler();

        ResolveComponentsCallback callback = new ResolveComponentsCallback();

        handler.resolveComponents(searchContext, expression, callback);

        return callback.components;
    }

    private static class ResolveComponentsCallback implements ContextCallback {
        public List<UIComponent> components = new ArrayList<>();

        @Override
        public void invokeContextCallback(FacesContext context, UIComponent target) {
            components.add(target);
        }
    }

    @Test
    public void test_ResolveComponent_Parent() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertSame(innerContainer, resolveComponent(source, "@parent"));
    }

    @Test
    public void test_ResolveComponent_ParentParent() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertSame(outerContainer, resolveComponent(source, "@parent:@parent"));
    }

    @Test
    public void test_ResolveComponent_Form() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertSame(form, resolveComponent(source, "@form"));
    }

    @Test
    public void test_ResolveComponent_FormParent() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertSame(root, resolveComponent(source, "@form:@parent"));
    }

    @Test
    public void test_ResolveComponent_All() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertSame(root, resolveComponent(source, "@all"));
    }

    @Test
    public void test_ResolveComponent_This() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertSame(source, resolveComponent(source, "@this"));
    }

    @Test
    public void test_ResolveComponent_ThisParent() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertSame(innerContainer, resolveComponent(source, "@this:@parent"));
    }

    @Test
    public void test_ResolveComponent_Namingcontainer() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertSame(innerContainer, resolveComponent(source, "@namingcontainer"));
    }

    @Test
    public void test_ResolveComponent_Absolute() {

        UIComponent root = new UIPanel();
        FacesContext.getCurrentInstance().getViewRoot().getChildren().add(root);

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertSame(source, resolveComponent(source, " :form:outerContainer:innerContainer:source "));
    }

    @Test
    public void test_ResolveComponent_Relative() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        component.setId("other");
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertSame(component, resolveComponent(source, " other "));
    }

    @Test
    public void test_ResolveComponent_AbsoluteForm() {

        UIComponent root = new UIPanel();
        root.setId("root");

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        component.setId("other");
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertSame(root, resolveComponent(source, " :form:@parent "));
    }

    @Test
    public void test_ResolveComponent_ParentChild() {

        UIComponent root = new UIPanel();
        root.setId("root");

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        component.setId("other");
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertSame(component, resolveComponent(source, " @parent:@child(0) "));
        assertSame(source, resolveComponent(source, " @parent:@child(1) "));
    }

    @Test
    public void test_ResolveComponent_AbsoluteNamingcontainer() {

        UIComponent root = new UIPanel();
        root.setId("root");

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        component.setId("other");
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertSame(form, resolveComponent(source, " :form:outerContainer:@namingcontainer "));
    }

    @Test
    public void test_ResolveClientId_This() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertEquals("form:outerContainer:innerContainer:source", resolveClientId(source, " @this "));
    }

    @Test
    public void test_ResolveClientId_Form() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertEquals("form", resolveClientId(source, " @form "));
    }

    @Test
    public void test_ResolveClientId_AbsoluteId() {

        UIComponent root = new UIPanel();
        FacesContext.getCurrentInstance().getViewRoot().getChildren().add(root);

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertEquals("form", resolveClientId(source, " :form "));
    }

    @Test
    public void test_ResolveClientId_Relative() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        component.setId("other");
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertEquals("form:outerContainer:innerContainer:other", resolveClientId(source, " other "));
    }

    @Test
    public void test_ResolveComponents_RelativeAndParentParent() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        component.setId("other");
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        List<UIComponent> resolvedComponents = resolveComponents(source, " other @parent:@parent ");
        assertTrue(resolvedComponents.contains(component));
        assertTrue(resolvedComponents.contains(outerContainer));
        assertEquals(2, resolvedComponents.size());
    }

    @Test
    public void test_ResolveComponents_RelativeAndThisParent() {

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        component.setId("other");
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        List<UIComponent> resolvedComponents = resolveComponents(source, " other,@this:@parent  @none ");
        assertTrue(resolvedComponents.contains(component));
        assertTrue(resolvedComponents.contains(innerContainer));
        assertEquals(2, resolvedComponents.size());
    }

    @Test
    public void test_ResolveComponent_Next() {

        UIComponent root = new UIPanel();
        root.setId("root");

        UIComponent command1 = new UICommand();
        command1.setId("command1");
        root.getChildren().add(command1);

        UIComponent command2 = new UICommand();
        command2.setId("command2");
        root.getChildren().add(command2);

        UIComponent command3 = new UICommand();
        command3.setId("command3");
        root.getChildren().add(command3);

        assertSame(command2, resolveComponent(command1, " @next "));
        assertSame(command3, resolveComponent(command2, " @next "));

        try {
            resolveComponent(command3, " @next");
            fail("This should actually raise an exception");
        } catch (Exception e) {
            assertEquals(ComponentNotFoundException.class, e.getClass());
        }
    }

    @Test
    public void test_ResolveComponent_NextNext() {

        UIComponent root = new UIPanel();
        root.setId("root");

        UIComponent command1 = new UICommand();
        command1.setId("command1");
        root.getChildren().add(command1);

        UIComponent command2 = new UICommand();
        command2.setId("command2");
        root.getChildren().add(command2);

        UIComponent command3 = new UICommand();
        command3.setId("command3");
        root.getChildren().add(command3);

        assertSame(command3, resolveComponent(command1, " @next:@next "));

        try {
            resolveComponent(command2, " @next:@next");
            fail("This should actually raise an exception");
        } catch (Exception e) {
            assertEquals(ComponentNotFoundException.class, e.getClass());
        }

        try {
            resolveComponent(command3, " @next:@next");
            fail("This should actually raise an exception");
        } catch (Exception e) {
            assertEquals(ComponentNotFoundException.class, e.getClass());
        }
    }

    @Test
    public void test_ResolveComponent_Previous() {

        UIComponent root = new UIPanel();
        root.setId("root");

        UIComponent command1 = new UICommand();
        command1.setId("command1");
        root.getChildren().add(command1);

        UIComponent command2 = new UICommand();
        command2.setId("command2");
        root.getChildren().add(command2);

        UIComponent command3 = new UICommand();
        command3.setId("command3");
        root.getChildren().add(command3);

        assertSame(command1, resolveComponent(command2, " @previous "));
        assertSame(command2, resolveComponent(command3, " @previous "));

        try {
            resolveComponent(command1, " @previous");
            fail("This should actually raise an exception");
        } catch (Exception e) {
            assertEquals(ComponentNotFoundException.class, e.getClass());
        }
    }

    @Test
    public void test_ResolveComponent_Root() {

        UIComponent root = new UIPanel();
        root.setId("root");

        UIComponent command1 = new UICommand();
        command1.setId("command1");
        root.getChildren().add(command1);

        UIComponent command2 = new UICommand();
        command2.setId("command2");
        root.getChildren().add(command2);

        UIComponent command3 = new UICommand();
        command3.setId("command3");
        root.getChildren().add(command3);

        assertSame(facesContext.getViewRoot(), resolveComponent(command2, " @root "));
    }

    @Test
    public void test_ResolveComponent_FormChildNextNext() {

        UIForm root = new UIForm();
        root.setId("form");

        UIComponent command1 = new UICommand();
        command1.setId("command1");
        root.getChildren().add(command1);

        UIComponent command2 = new UICommand();
        command2.setId("command2");
        root.getChildren().add(command2);

        UIComponent command3 = new UICommand();
        command3.setId("command3");
        root.getChildren().add(command3);

        assertSame(command3, resolveComponent(command1, " @form:@child(0):@next:@next "));
    }

    @Test
    public void test_ResolveComponent_IgnoreNoResult() {
        UIForm root = new UIForm();
        root.setId("form");

        UIComponent command1 = new UICommand();
        command1.setId("command1");
        root.getChildren().add(command1);

        UIComponent command2 = new UICommand();
        command2.setId("command2");
        root.getChildren().add(command2);

        assertSame(null, resolveComponent(command1, " command3 ", SearchExpressionHint.IGNORE_NO_RESULT));
    }

    @Test
    public void test_ResolveClientId_AbsoluteWithFormPrependIdFalse() {

        UIComponent root = new UIPanel();
        FacesContext.getCurrentInstance().getViewRoot().getChildren().add(root);

        UIForm form = new UIForm();
        form.setId("form");
        form.setPrependId(false);
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertEquals("outerContainer:innerContainer:source", resolveClientId(source, " :form:outerContainer:innerContainer:source "));
    }

    @Test
    public void test_ResolveClientId_AbsoluteWithFormPrependIdFalse_InvokeOnComponent() {

        UIComponent root = new UIPanel();
        FacesContext.getCurrentInstance().getViewRoot().getChildren().add(root);

        UIForm form = new UIForm();
        form.setId("form");
        form.setPrependId(false);
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("outerContainer");
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("innerContainer");
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        source.setId("source");
        innerContainer.getChildren().add(source);

        assertEquals("outerContainer:innerContainer:source", resolveClientId(source, " outerContainer:innerContainer:source "));
    }

    @Test
    public void test_Passthrough() {
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        SearchExpressionContext searchExpressionContext = SearchExpressionContext.createSearchExpressionContext(facesContext, null);

        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "mainForm:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "mainForm:table:3:nested:1:nestedText"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "mainForm:table:3:baseText"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "mainForm:table:0:baseText"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "mainForm:table:3:nested:0:nestedText"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "mainForm:table:3:nested"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "mainForm:table:1:nested:0:nestedText"));

        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@this"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@this:@parent:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@parent:showName:@parent:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@form"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@form:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@namingcontainer:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@previous"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@next"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@parent:@id(msgName)"));

        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@whoNows"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@parent:@whoNows"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "mainForm:@whoNows"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "!whoNows"));

        Set<SearchExpressionHint> hints = new HashSet<>();
        hints.add(SearchExpressionHint.RESOLVE_CLIENT_SIDE);
        searchExpressionContext = SearchExpressionContext.createSearchExpressionContext(facesContext, null, hints, null);

        Assertions.assertTrue(handler.isPassthroughExpression(searchExpressionContext, "@form"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@form:showName"));
        Assertions.assertFalse(handler.isPassthroughExpression(searchExpressionContext, "@form:@child(0)"));
    }

    @Test
    public void test_Valid() {
        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();

        SearchExpressionContext searchExpressionContext = SearchExpressionContext.createSearchExpressionContext(facesContext, null);

        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "mainForm:showName"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "mainForm:table:3:nested:1:nestedText"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "mainForm:table:3:baseText"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "mainForm:table:0:baseText"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "mainForm:table:3:nested:0:nestedText"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "mainForm:table:3:nested"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "mainForm:table:1:nested:0:nestedText"));

        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "@this"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "@this:@parent:showName"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "@parent:showName:@parent:showName"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "@form:showName"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "@namingcontainer:showName"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "@previous"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "@next"));
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "@parent:@id(msgName)"));

        Assertions.assertFalse(handler.isValidExpression(searchExpressionContext, "@whoNows"));
        Assertions.assertFalse(handler.isValidExpression(searchExpressionContext, "@parent:@whoNows"));
        Assertions.assertFalse(handler.isValidExpression(searchExpressionContext, "mainForm:@whoNows"));

        Assertions.assertFalse(handler.isValidExpression(searchExpressionContext, "@none:@parent"));
        Assertions.assertFalse(handler.isValidExpression(searchExpressionContext, "@all:@parent"));
    }

    @Test
    public void test_ResolveComponents_Id() {
        UIComponent root = new UIPanel();
        FacesContext.getCurrentInstance().getViewRoot().getChildren().add(root);

        UINamingContainer outerContainer = new UINamingContainer();
        outerContainer.setId("myContainer");
        root.getChildren().add(outerContainer);

        UIForm form = new UIForm();
        form.setId("form");
        root.getChildren().add(form);

        UINamingContainer innerContainer = new UINamingContainer();
        innerContainer.setId("myContainer");
        form.getChildren().add(innerContainer);

        UINamingContainer innerContainer2 = new UINamingContainer();
        innerContainer2.setId("myContainer2");
        form.getChildren().add(innerContainer2);

        UINamingContainer innerContainer3 = new UINamingContainer();
        innerContainer3.setId("myContainer3-test");
        form.getChildren().add(innerContainer3);

        List<UIComponent> result = resolveComponents(form, " @id(myContainer) ");
        assertTrue(result.size() == 1);
        assertTrue(result.contains(innerContainer));

        result = resolveComponents(form, " @id(myContainer3-test) ");
        assertTrue(result.size() == 1);
        assertTrue(result.contains(innerContainer3));
    }

    /**
     * The SearchExpression API was inspired by PrimeFaces. This test only tests, if PFS (PrimeFaces Selectors -> jQuery
     * like selectors; like @(#myId > .myStyle)) can be correctly handled by the API+IMPL as a passthrough expression.
     */
    @Test
    public void test_PFS() {
        CompositeSearchKeywordResolver s = (CompositeSearchKeywordResolver) application.getSearchKeywordResolver();
        s.add(new SearchKeywordResolver() {

            @Override
            public void resolve(SearchKeywordContext searchKeywordContext, UIComponent previous, String keyword) {

            }

            @Override
            public boolean isResolverForKeyword(SearchExpressionContext searchExpressionContext, String keyword) {
                return keyword.startsWith("(") && keyword.endsWith(")");
            }

            @Override
            public boolean isPassthrough(SearchExpressionContext searchExpressionContext, String keyword) {
                return true;
            }

            @Override
            public boolean isLeaf(SearchExpressionContext searchExpressionContext, String keyword) {
                return true;
            }
        });

        UIComponent root = new UIPanel();
        FacesContext.getCurrentInstance().getViewRoot().getChildren().add(root);

        assertEquals("@(.myPanel #id)", resolveClientId(root, " @(.myPanel #id)"));

        SearchExpressionHandler handler = facesContext.getApplication().getSearchExpressionHandler();
        SearchExpressionContext searchExpressionContext = SearchExpressionContext.createSearchExpressionContext(facesContext, null);
        Assertions.assertTrue(handler.isValidExpression(searchExpressionContext, "@(.myPanel #id)"));
        Assertions.assertFalse(handler.isValidExpression(searchExpressionContext, "@(.myPanel #id):test"));
    }

    @Test
    public void test_chainOfResponsability() {
        CompositeSearchKeywordResolver s = (CompositeSearchKeywordResolver) application.getSearchKeywordResolver();
        s.add(new CustomSearchKeywordResolverImplForm()); // drop in new @form resolver

        UIComponent root = new UIPanel();

        UIForm form = new UIForm();
        root.getChildren().add(form);

        UINamingContainer outerContainer = new UINamingContainer();
        form.getChildren().add(outerContainer);

        UINamingContainer innerContainer = new UINamingContainer();
        outerContainer.getChildren().add(innerContainer);

        UIComponent component = new UIOutput();
        innerContainer.getChildren().add(component);

        UIComponent source = new UICommand();
        innerContainer.getChildren().add(source);

        assertSame(source, resolveComponent(source, "@form"));
        assertNotSame(form, resolveComponent(source, "@form"));
    }

    class CustomSearchKeywordResolverImplForm extends SearchKeywordResolverImplForm {
        @Override
        public void resolve(SearchKeywordContext searchKeywordContext, UIComponent current, String keyword) {
            searchKeywordContext.invokeContextCallback(current);
        }
    }

    @Test
    public void test_ResolveComponent_LeafErrorHandling() {

        UIComponent root = new UIPanel();
        root.setId("root");

        try {
            resolveComponent(root, " @none:myId");
            fail("This should actually raise an exception");
        } catch (Exception e) {
            assertEquals(FacesException.class, e.getClass());
        }
    }
}
