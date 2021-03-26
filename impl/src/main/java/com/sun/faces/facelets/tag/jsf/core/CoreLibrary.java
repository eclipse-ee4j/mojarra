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

package com.sun.faces.facelets.tag.jsf.core;

import com.sun.faces.ext.component.UIValidateWholeBean;
import com.sun.faces.facelets.tag.AbstractTagLibrary;
import com.sun.faces.renderkit.html_basic.WebsocketRenderer;

import jakarta.faces.component.UIImportConstants;
import jakarta.faces.component.UIParameter;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.UIViewAction;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.UIWebsocket;
import jakarta.faces.convert.DateTimeConverter;
import jakarta.faces.convert.NumberConverter;
import jakarta.faces.validator.BeanValidator;
import jakarta.faces.validator.DoubleRangeValidator;
import jakarta.faces.validator.LengthValidator;
import jakarta.faces.validator.LongRangeValidator;
import jakarta.faces.validator.RegexValidator;
import jakarta.faces.validator.RequiredValidator;

/**
 * For Tag details, see Faces Core
 * <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/tld-summary.html">taglib
 * documentation</a>.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class CoreLibrary extends AbstractTagLibrary {

    public final static String Namespace = "http://java.sun.com/jsf/core";
    public final static String XMLNSNamespace = "http://xmlns.jcp.org/jsf/core";

    public final static CoreLibrary Instance = new CoreLibrary();

    public CoreLibrary() {
        this(Namespace);
    }

    public CoreLibrary(String namespace) {
        super(namespace);

        addTagHandler("actionListener", ActionListenerHandler.class);

        addTagHandler("ajax", AjaxHandler.class);

        addTagHandler("attribute", AttributeHandler.class);

        addTagHandler("attributes", AttributesHandler.class);

        addTagHandler("passThroughAttribute", PassThroughAttributeHandler.class);

        addTagHandler("passThroughAttributes", PassThroughAttributesHandler.class);

        this.addConverter("convertDateTime", DateTimeConverter.CONVERTER_ID, ConvertDateTimeHandler.class);

        this.addConverter("convertNumber", NumberConverter.CONVERTER_ID, ConvertNumberHandler.class);

        this.addConverter("converter", null, ConvertDelegateHandler.class);

        addTagHandler("event", EventHandler.class);

        addTagHandler("facet", FacetHandler.class);

        addTagHandler("metadata", MetadataHandler.class);

        this.addComponent("importConstants", UIImportConstants.COMPONENT_TYPE, null);

        addTagHandler("loadBundle", LoadBundleHandler.class);

        addTagHandler("resetValues", ResetValuesHandler.class);

        this.addComponent("viewParam", UIViewParameter.COMPONENT_TYPE, null);

        this.addComponent("viewAction", UIViewAction.COMPONENT_TYPE, null);

        this.addComponent("param", UIParameter.COMPONENT_TYPE, null);

        addTagHandler("phaseListener", PhaseListenerHandler.class);

        this.addComponent("selectItem", UISelectItem.COMPONENT_TYPE, null);

        this.addComponent("selectItems", UISelectItems.COMPONENT_TYPE, null);

        addTagHandler("setPropertyActionListener", SetPropertyActionListenerHandler.class);

        this.addComponent("subview", "jakarta.faces.NamingContainer", null);

        this.addValidator("validateBean", BeanValidator.VALIDATOR_ID);

        this.addValidator("validateLength", LengthValidator.VALIDATOR_ID);

        this.addValidator("validateLongRange", LongRangeValidator.VALIDATOR_ID);

        this.addValidator("validateDoubleRange", DoubleRangeValidator.VALIDATOR_ID);

        this.addValidator("validateRegex", RegexValidator.VALIDATOR_ID);

        this.addValidator("validateRequired", RequiredValidator.VALIDATOR_ID);

        this.addComponent("validateWholeBean", UIValidateWholeBean.FAMILY, null);

        this.addValidator("validator", null, ValidateDelegateHandler.class);

        addTagHandler("valueChangeListener", ValueChangeListenerHandler.class);

        addTagHandler("view", ViewHandler.class);

        this.addComponent("verbatim", "jakarta.faces.HtmlOutputText", "jakarta.faces.Text", VerbatimHandler.class);

        this.addComponent("websocket", UIWebsocket.COMPONENT_TYPE, WebsocketRenderer.RENDERER_TYPE);
    }
}
