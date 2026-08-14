/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package com.sun.faces.facelets.tag;

import static com.sun.faces.facelets.tag.BuildTimeDecisions.keepsBuildReproducible;
import static com.sun.faces.facelets.tag.BuildTimeDecisions.markUnreproducible;
import static com.sun.faces.facelets.tag.BuildTimeDecisions.record;
import static com.sun.faces.facelets.tag.BuildTimeDecisions.reproducesBuild;
import static com.sun.faces.facelets.tag.BuildTimeDecisions.reset;
import static com.sun.faces.facelets.tag.BuildTimeDecisions.size;
import static com.sun.faces.facelets.tag.BuildTimeDecisions.unreproducibleMarks;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.sun.faces.test.tag.ForeignTagHandler;

import jakarta.el.ELContext;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.BehaviorHandler;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.FaceletHandler;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributes;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.ValidatorHandler;

/**
 * A view whose build takes a decision this implementation cannot see is built as unreproducible, so the redundant
 * render-time re-apply is performed rather than skipped ({@link BuildTimeDecisions}). Every tag handler of this
 * implementation is exempt from that, since each is audited to either contribute the same thing to every build of a
 * view or to record what it decided. This holds the audited set, so a handler added or changed since fails here until
 * it has been audited too.
 */
class BuildTimeDecisionsTest {

    private static final Path TAG_HANDLER_SOURCES = Path.of("src/main/java/com/sun/faces/facelets/tag");

    private static final Pattern APPLY_DECLARATION = Pattern.compile(
            "public\\s+void\\s+apply\\s*\\(\\s*(jakarta\\.faces\\.view\\.facelets\\.)?FaceletContext");

    private static final Set<String> AUDITED_TAG_HANDLERS = new TreeSet<>(Set.of(
            "composite.AttachedObjectTargetHandler",
            "composite.AttributeHandler",
            "composite.DeclareFacetHandler",
            "composite.ExtensionHandler",
            "composite.ImplementationHandler",
            "composite.InsertChildrenHandler",
            "composite.InsertFacetHandler",
            "composite.InterfaceHandler",
            "composite.PropertyHandlerManager",
            "faces.BehaviorTagHandlerDelegateImpl",
            "faces.ComponentTagHandlerDelegateImpl",
            "faces.ConverterTagHandlerDelegateImpl",
            "faces.core.ActionListenerHandlerBase",
            "faces.core.AjaxHandler",
            "faces.core.AttributeHandler",
            "faces.core.AttributesHandler",
            "faces.core.EventHandler",
            "faces.core.FacetHandler",
            "faces.core.LoadBundleHandler",
            "faces.core.MetadataHandler",
            "faces.core.PassThroughAttributeHandler",
            "faces.core.PassThroughAttributesHandler",
            "faces.core.PhaseListenerHandler",
            "faces.core.SetPropertyActionListenerHandler",
            "faces.core.ValueChangeListenerHandler",
            "faces.core.ViewHandler",
            "faces.ValidatorTagHandlerDelegateImpl",
            "jstl.core.CatchHandler",
            "jstl.core.ChooseHandler",
            "jstl.core.ChooseOtherwiseHandler",
            "jstl.core.ChooseWhenHandler",
            "jstl.core.ForEachHandler",
            "jstl.core.IfHandler",
            "jstl.core.SetHandler",
            "ui.CompositionHandler",
            "ui.DecorateHandler",
            "ui.DefineHandler",
            "ui.IncludeHandler",
            "ui.InsertHandler",
            "ui.ParamHandler",
            "ui.SchemaCompliantRemoveHandler",
            "UserTagHandler"));

    /**
     * Every tag handler that takes over {@code apply} must be one of the audited ones. A new or renamed handler is
     * exempted from the re-apply by the package it lives in, so it must be audited before it is added here: it either
     * contributes the same thing to every build, or it records the expression it decided on, or it marks the build
     * unreproducible.
     *
     * @see BuildTimeDecisions#keepsBuildReproducible(jakarta.faces.view.facelets.FaceletHandler, jakarta.faces.view.facelets.Tag)
     */
    @Test
    void everyTagHandlerTakingOverApplyIsAudited() throws IOException {
        assertEquals(AUDITED_TAG_HANDLERS, tagHandlersTakingOverApply(), "audited tag handlers");
    }

    /**
     * A decision that still yields the value it was recorded with lets the re-apply be skipped, and one that yields
     * anything else, or cannot be evaluated at all, does not.
     */
    @Test
    void aDecisionHoldsOnlyWhileItYieldsTheValueItWasRecordedWith() {
        FacesContext context = mockFacesContext();
        assertTrue(reproducesBuild(context), "a build that recorded nothing");

        ValueExpression expression = mock(ValueExpression.class);
        when(expression.getValue(any())).thenReturn("a");
        record(context, expression, "a");
        assertTrue(reproducesBuild(context), "a decision still yielding its recorded value");

        when(expression.getValue(any())).thenReturn("b");
        assertFalse(reproducesBuild(context), "a decision yielding another value");

        when(expression.getValue(any())).thenThrow(new PropertyNotFoundException());
        assertFalse(reproducesBuild(context), "a decision that cannot be evaluated");
    }

    /**
     * A build marked unreproducible is not skipped however its decisions come out, and resetting discards both.
     */
    @Test
    void markingTheBuildUnreproducibleOverridesItsDecisions() {
        FacesContext context = mockFacesContext();
        ValueExpression expression = mock(ValueExpression.class);
        when(expression.getValue(any())).thenReturn("a");

        record(context, expression, "a");
        record(context, () -> "b", "b");
        assertEquals(2, size(context), "recorded decisions");
        assertEquals(0, unreproducibleMarks(context), "unreproducible marks");
        assertTrue(reproducesBuild(context), "decisions all holding");

        markUnreproducible(context);
        assertEquals(1, unreproducibleMarks(context), "unreproducible marks");
        assertFalse(reproducesBuild(context), "a build marked unreproducible");

        reset(context);
        assertEquals(0, size(context), "recorded decisions after a reset");
        assertTrue(reproducesBuild(context), "a build that recorded nothing after a reset");
    }

    /**
     * A tag handler of another tag library is taken to keep the build reproducible only while its tag carries no
     * expression, except when it attaches a converter, validator or behavior, which a re-apply does not act on.
     */
    @Test
    void aForeignTagHandlerKeepsTheBuildReproducibleOnlyWithoutAnExpression() {
        FaceletHandler foreign = new ForeignTagHandler(mockTagConfig());

        assertTrue(keepsBuildReproducible(foreign, mockTag(true)), "a foreign handler whose tag is all literal");
        assertFalse(keepsBuildReproducible(foreign, mockTag(false)), "a foreign handler whose tag carries an expression");

        assertTrue(keepsBuildReproducible(mock(ConverterHandler.class), mockTag(false)), "a converter handler");
        assertTrue(keepsBuildReproducible(mock(ValidatorHandler.class), mockTag(false)), "a validator handler");
        assertTrue(keepsBuildReproducible(mock(BehaviorHandler.class), mockTag(false)), "a behavior handler");
    }

    private static FacesContext mockFacesContext() {
        FacesContext context = mock(FacesContext.class);
        when(context.getAttributes()).thenReturn(new HashMap<>());
        when(context.getELContext()).thenReturn(mock(ELContext.class));
        return context;
    }

    private static TagConfig mockTagConfig() {
        Tag tag = mockTag(true);
        TagConfig config = mock(TagConfig.class);
        when(config.getTag()).thenReturn(tag);
        return config;
    }

    private static Tag mockTag(boolean literal) {
        TagAttribute attribute = mock(TagAttribute.class);
        when(attribute.isLiteral()).thenReturn(literal);
        TagAttributes attributes = mock(TagAttributes.class);
        when(attributes.getAll()).thenReturn(new TagAttribute[] { attribute });
        Tag tag = mock(Tag.class);
        when(tag.getAttributes()).thenReturn(attributes);
        return tag;
    }

    private static Set<String> tagHandlersTakingOverApply() throws IOException {
        try (Stream<Path> sources = walk(TAG_HANDLER_SOURCES)) {
            return sources.filter(source -> source.toString().endsWith(".java"))
                          .filter(BuildTimeDecisionsTest::declaresApply)
                          .map(BuildTimeDecisionsTest::toClassName)
                          .collect(toCollection(TreeSet::new));
        }
    }

    private static boolean declaresApply(Path source) {
        try {
            return APPLY_DECLARATION.matcher(readString(source)).find();
        } catch (IOException e) {
            throw new IllegalStateException(source.toString(), e);
        }
    }

    private static String toClassName(Path source) {
        return TAG_HANDLER_SOURCES.relativize(source).toString().replace(".java", "").replace('/', '.');
    }
}
