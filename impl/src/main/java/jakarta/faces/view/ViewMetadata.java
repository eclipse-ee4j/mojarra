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

package jakarta.faces.view;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIImportConstants;
import jakarta.faces.component.UIViewAction;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_0">
 * <code>ViewMetadata</code> is reponsible for extracting and providing view parameter metadata from VDL views. Because
 * {@link ViewDeclarationLanguage#getViewMetadata} is required to return <code>null</code> for Jakarta Server Pages
 * views and non-<code>null</code> for views authored in Facelets for Jakarta Faces 2, this specification only
 * applies to Facelets for Jakarta Faces 2.
 * </p>
 *
 * @since 2.0
 */
public abstract class ViewMetadata {

    /**
     * <p class="changed_added_2_0">
     * Get the view id
     * </p>
     *
     * @return the view ID for which this <code>ViewMetadata</code> instance was created
     */
    public abstract String getViewId();

    /**
     * <p class="changed_added_2_0">
     * Creates a new {@link UIViewRoot} containing only view parameter metadata. The processing of building this
     * <code>UIViewRoot</code> with metadata should not cause any events to be published to the application. The
     * implementation must call {@link FacesContext#setProcessingEvents} passing <code>false</code> as the argument, at the
     * beginning of the method, and pass <code>true</code> to the same method at the end. The implementation must ensure
     * that this happens regardless of ant exceptions that may be thrown.
     * </p>
     *
     * <p class="changed_modified_2_3">
     * Take note a compliant implementation has to ensure that:
     * </p>
     * <ul>
     * <li>the new UIViewRoot must be set as the FacesContext's viewRoot before applying the tag handlers, restoring the old
     * FacesContext in a finally block.</li>
     * <li>The contents of the current UIViewRoot's ViewMap must be copied to the ViewMap of the new UIViewRoot before
     * applying the tag handlers.</li>
     * <li class="changed_added_2_3">The {@link UIImportConstants} must be processed after applying the tag handlers.</li>
     * </ul>
     *
     * @param context the {@link FacesContext} for the current request
     * @return a <code>UIViewRoot</code> containing only view parameter metadata (if any)
     */
    public abstract UIViewRoot createMetadataView(FacesContext context);

    /**
     * <p class="changed_added_2_0">
     * Utility method to extract view metadata from the provided {@link UIViewRoot}.
     * </p>
     *
     * @param root the {@link UIViewRoot} from which the metadata will be extracted.
     *
     * @return a <code>Collection</code> of {@link UIViewParameter} instances. If the view has no metadata, the collection
     * will be empty.
     */
    public static Collection<UIViewParameter> getViewParameters(UIViewRoot root) {
        return getMetadataChildren(root, UIViewParameter.class);
    }

    /**
     * <p class="changed_added_2_2">
     * Utility method to extract view metadata from the provided {@link UIViewRoot}.
     * </p>
     *
     * @param root the {@link UIViewRoot} from which the metadata will be extracted.
     *
     * @return a <code>Collection</code> of {@link UIViewAction} instances. If the view has no metadata, the collection will
     * be empty.
     */
    public static Collection<UIViewAction> getViewActions(UIViewRoot root) {
        return getMetadataChildren(root, UIViewAction.class);
    }

    /**
     * <p class="changed_added_2_3">
     * Utility method to extract view metadata from the provided {@link UIViewRoot}.
     * </p>
     *
     * @param root The {@link UIViewRoot} from which the metadata will be extracted.
     *
     * @return A <code>Collection</code> of {@link UIImportConstants} instances. If the view has no metadata, the collection
     * will be empty.
     */
    public static Collection<UIImportConstants> getImportConstants(UIViewRoot root) {
        return getMetadataChildren(root, UIImportConstants.class);
    }

    /**
     * <p class="changed_added_2_2">
     * Utility method to determine if the the provided {@link UIViewRoot} has metadata. The default implementation will
     * return true if the provided {@code UIViewRoot} has a facet named {@link UIViewRoot#METADATA_FACET_NAME} and that
     * facet has children. It will return false otherwise.
     * </p>
     *
     * @param root the {@link UIViewRoot} from which the metadata will be extracted from
     *
     * @return true if the view has metadata, false otherwise.
     */
    public static boolean hasMetadata(UIViewRoot root) {
        return getMetadataFacet(root).map(m -> m.getChildCount() > 0).orElse(false);
    }

    @SuppressWarnings("unchecked")
    private static <C extends UIComponent> List<C> getMetadataChildren(UIViewRoot root, Class<C> type) {
        return (List<C>) getMetadataFacet(root).map(UIComponent::getChildren)
                                               .orElseGet(Collections::emptyList)
                                               .stream()
                                               .filter(type::isInstance)
                                               .collect(Collectors.toList());
    }

    private static Optional<UIComponent> getMetadataFacet(UIViewRoot root) {
        return Optional.ofNullable(root.getFacet(UIViewRoot.METADATA_FACET_NAME));
    }

}
