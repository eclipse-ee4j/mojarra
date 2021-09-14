/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.faces.view.BehaviorHolderAttachedObjectHandler;

/**
 * <p class="changed_added_2_0">
 * The {@link FaceletHandler} that corresponds to attached objects that represent an instance of
 * {@link jakarta.faces.component.behavior.ClientBehavior} that must be added to the parent component, which must
 * implement {@link jakarta.faces.component.behavior.ClientBehaviorHolder}, with a call to
 * {@link jakarta.faces.component.behavior.ClientBehaviorHolder#addClientBehavior}. The current specification defines
 * one Facelet element for this sort of attached object, <code>&lt;f:ajax&gt;</code>.
 * </p>
 */
public class BehaviorHandler extends FaceletsAttachedObjectHandler implements BehaviorHolderAttachedObjectHandler {

    private final TagAttribute event;
    private String behaviorId;
    private TagHandlerDelegate helper;

    /**
     * Constructs the handler using the Id from the config.
     *
     * @param config config instance to get the behavior Id from.
     */
    public BehaviorHandler(BehaviorConfig config) {
        super(config);
        behaviorId = config.getBehaviorId();
        event = getAttribute("event");
        if (event != null && !event.isLiteral()) {
            throw new TagException(tag, "The 'event' attribute for behavior tag must be a literal");
        }
    }

    @Override
    public String getEventName() {
        if (getEvent() != null) {
            return getEvent().getValue();
        }

        return null;
    }

    @Override
    protected TagHandlerDelegate getTagHandlerDelegate() {
        if (helper == null) {
            helper = delegateFactory.createBehaviorHandlerDelegate(this);
        }

        return helper;
    }

    /**
     * Returns the behavior Id obtained from the passed-in config.
     * @return the behavior Id
     */
    public String getBehaviorId() {
        return behaviorId;
    }

    /**
     * Returns the event that is set as the "event" attribute on the behavior tag.
     *
     * @return the event tag attribute
     */
    public TagAttribute getEvent() {
        return event;
    }

}
