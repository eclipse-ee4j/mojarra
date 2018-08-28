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

package com.sun.faces.facelets.tag.jsp;

import com.sun.faces.facelets.tag.TagHandlerImpl;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;

/**
 * @author Jacob Hookom
 */
public class ParamHandler extends TagHandlerImpl {

    private final TagAttribute name;

    private final TagAttribute value;

    /**
     * @param config
     */
    public ParamHandler(TagConfig config) {
        super(config);
        this.name = this.getRequiredAttribute("name");
        this.value = this.getRequiredAttribute("value");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
     * javax.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        String nameStr = this.name.getValue(ctx);
        ValueExpression valueVE = this.value.getValueExpression(ctx, Object.class);
        ctx.getVariableMapper().setVariable(nameStr, valueVE);
        setParam(ctx.getFacesContext(), parent, nameStr, valueVE);
    }

    private void setParam(FacesContext context, UIComponent component, String name, ValueExpression valueVE) {
        Map<String, ValueExpression> params = (Map<String, ValueExpression>) context.getAttributes().get(component);
        if (null == params) {
            params = new HashMap<String, ValueExpression>();
            context.getAttributes().put(component, params);
        }
        params.put(name, valueVE);
    }

    static Map<String, ValueExpression> getParams(FacesContext context, UIComponent component) {
        return (Map<String, ValueExpression>) context.getAttributes().get(component);
    }

}
