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

package jakarta.faces.view.facelets;

import java.io.IOException;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIComponent;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_0_rev_a">Enable</span> the Jakarta Faces implementation to provide the
 * appropriate behavior for the kind of {@link MetaTagHandler} subclass for each kind of element in the view, while
 * providing a base-class from which those wanting to make a Java language custom tag handler can inherit. The Jakarta
 * Server Faces runtime provides the implementation of {@link #getTagHandlerDelegate} for the appropriate subclass.
 * </p>
 */

public abstract class DelegatingMetaTagHandler extends MetaTagHandler {

    private final TagAttribute binding;
    private final TagAttribute disabled;

    /**
     * Class that defines methods relating to helping tag handler instances.
     */
    protected TagHandlerDelegateFactory delegateFactory;

    /**
     * Instantiates this handler with the given config.
     *
     * @param config the config used to instantiate this handler.
     */
    public DelegatingMetaTagHandler(TagConfig config) {
        super(config);
        binding = getAttribute("binding");
        disabled = getAttribute("disabled");
        delegateFactory = (TagHandlerDelegateFactory) FactoryFinder.getFactory(FactoryFinder.TAG_HANDLER_DELEGATE_FACTORY);
    }

    /**
     * <p class="changed_added_2_3">
     * Get the tag handler delegate.
     * </p>
     *
     * <p class="changed_added_2_3">
     * Code that extends from DelegatingMetaTagHandler (directly or indirectly, as through extending ComponentHandler) must
     * take care to decorate, not replace, the TagHandlerDelegate instance returned by this method. Failure to do so may
     * produce unexpected results.
     * </p>
     *
     * @return the tag handler delegate.
     */
    protected abstract TagHandlerDelegate getTagHandlerDelegate();


    // Properties ----------------------------------------

    /**
     * Returns the value of the "disabled" attribute.
     *
     * @param ctx the context used for resolving the underlying attribute.
     * @return true if the "disabled" attribute has been set to true, false otherwise.
     */
    public boolean isDisabled(FaceletContext ctx) {
        return disabled != null && Boolean.TRUE.equals(disabled.getBoolean(ctx));
    }

    /**
     * Return the "binding" attribute.
     * @return the "binding" attribute.
     */
    public TagAttribute getBinding() {
        return binding;
    }

    /**
     * Return a reference to the <code>Tag</code> instance corresponding to this <code>TagHandler</code> instance.
     * @return a reference to the <code>Tag</code> instance.
     */
    public Tag getTag() {
        return tag;
    }

    /**
     * Return the tag id from the <code>TagConfig</code> used to instantiate this handler.
     * @return the tag id from the <code>TagConfig</code>.
     */
    public String getTagId() {
        return tagId;
    }

    /**
     * Return the named attribute from the tag attributes.
     * @param localName the name of the attribute.
     * @return the named attribute from the tag attributes.
     */
    public TagAttribute getTagAttribute(String localName) {
        return super.getAttribute(localName);
    }

    @Override
    public void setAttributes(FaceletContext ctx, Object instance) {
        super.setAttributes(ctx, instance);
    }


    // Methods ----------------------------------------

    /**
     * <p class="changed_added_2_0">
     * The default implementation simply calls through to {@link TagHandlerDelegate#apply}.
     * </p>
     *
     * @param ctx the <code>FaceletContext</code> for this view execution
     *
     * @param parent the parent <code>UIComponent</code> of the component represented by this element instance.
     * @since 2.0
     */

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        getTagHandlerDelegate().apply(ctx, parent);
    }

    /**
     * <p class="changed_added_2_0_rev_a">
     * Invoke the <code>apply()</code> method on this instance's {@link TagHandler#nextHandler}.
     * </p>
     *
     * @param ctx the <code>FaceletContext</code> for this view execution
     *
     * @param c the <code>UIComponent</code> of the component represented by this element instance.
     *
     * @throws IOException if thrown by the next {@link FaceletHandler}
     *
     * @throws FaceletException if thrown by the next {@link FaceletHandler}
     *
     * @throws jakarta.faces.FacesException if thrown by the next {@link FaceletHandler}
     *
     * @throws jakarta.el.ELException if thrown by the next {@link FaceletHandler}
     *
     * @since 2.0
     */
    public void applyNextHandler(FaceletContext ctx, UIComponent c) throws IOException, FacesException, ELException {
        // first allow c to get populated
        nextHandler.apply(ctx, c);
    }

    /**
     * <p class="changed_added_2_0">
     * The default implementation simply calls through to {@link TagHandlerDelegate#createMetaRuleset} and returns the
     * result.
     * </p>
     *
     * @param type the <code>Class</code> for which the <code>MetaRuleset</code> must be created.
     *
     * @since 2.0
     */
    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        return getTagHandlerDelegate().createMetaRuleset(type);
    }

}
