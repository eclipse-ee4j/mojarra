/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2005-2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.faces.view.facelets;

import javax.faces.view.ValueHolderAttachedObjectHandler;

/**
 * <p class="changed_added_2_0">Handles setting a {@link
 * javax.faces.convert.Converter} instance on a {@link
 * javax.faces.component.ValueHolder} parent. Will wire all attributes
 * set to the <code>Converter</code> instance created/fetched. Uses the
 * "binding" attribute for grabbing instances to apply attributes
 * to. </p>
 *
 * <p class="changed_added_2_0">Will only set/create
 * <code>Converter</code> if the passed <code>UIComponent</code>'s
 * <code>parent</code> is <code>null</code>, signifying that it wasn't
 * restored from an existing tree.</p>
 * 
 */
public class ConverterHandler extends FaceletsAttachedObjectHandler implements ValueHolderAttachedObjectHandler {

    
    private String converterId;
    
    
    private TagHandlerDelegate helper;

    public ConverterHandler(ConverterConfig config) {
        super(config);
        this.converterId = config.getConverterId();
    }

    @Override
    protected TagHandlerDelegate getTagHandlerDelegate() {
        if (null == helper) {
            helper = delegateFactory.createConverterHandlerDelegate(this);
        }
        return helper;

    }

    public String getConverterId(FaceletContext ctx) {
        if (converterId == null) {
            TagAttribute idAttr = getAttribute("converterId");
            if (idAttr == null) {
                return null;
            } else {
                return idAttr.getValue(ctx);
            }
        }
        return converterId;
    }
    
}
