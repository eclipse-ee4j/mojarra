/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.renderkit;

import com.sun.faces.test.servlet30.renderkit.SelectMany05Bean.HobbitBean;
import java.util.Collection;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "hobbit-converter")
public class HobbitConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        SelectMany05Bean bean = new SelectMany05Bean();
        HobbitBean result = null;
        Collection<HobbitBean> hobbits = bean.getHobbitCollection();
        for (HobbitBean cur : hobbits) {
            if (value.equals(cur.getName())) {
                result = cur;
                break;
            }
        }

        return result;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((HobbitBean) value).getName();
    }

}
