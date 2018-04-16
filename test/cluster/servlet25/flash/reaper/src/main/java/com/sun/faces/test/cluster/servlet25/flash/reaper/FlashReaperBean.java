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

package com.sun.faces.test.cluster.servlet25.flash.reaper;

import com.sun.faces.context.flash.ELFlash;
import java.lang.reflect.Field;
import java.util.Map;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

@ManagedBean(eager=true)
@ApplicationScoped
public class FlashReaperBean {

    public static final int NUMBER_OF_ZOMBIES = 12;

    public  FlashReaperBean() {

    }

    public String getNumberEntriesInInnerMap() throws Exception {
        String result = null;

        ELFlash flash = (ELFlash) FacesContext.getCurrentInstance().getExternalContext().getFlash();
        Field innerMapField = ELFlash.class.getDeclaredField("flashInnerMap");
        innerMapField.setAccessible(true);
        Map<String,Map<String, Object>> innerMap =
                (Map<String,Map<String, Object>>) innerMapField.get(flash);
        result = "" + innerMap.size();

        return result;
    }

}
