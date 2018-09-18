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

package com.sun.faces.config.beans;


import java.util.ArrayList;
import java.util.List;


/**
 * <p>Configuration bean for <code>&lt;map-entries&gt;</code> element.</p>
 */

public class MapEntriesBean {


    // -------------------------------------------------------------- Properties


    private String keyClass;
    public String getKeyClass() { return keyClass; }
    public void setKeyClass(String keyClass)
    { this.keyClass = keyClass; }


    private List<MapEntryBean> mapEntries = new ArrayList<MapEntryBean>();
    public MapEntryBean[] getMapEntries() {
        MapEntryBean results[] =
            new MapEntryBean[mapEntries.size()];
        return (mapEntries.toArray(results));
    }


    private String valueClass;
    public String getValueClass() { return valueClass; }
    public void setValueClass(String valueClass)
    { this.valueClass = valueClass; }


    // -------------------------------------------------------------- Extensions


    // ----------------------------------------------------------------- Methods


    public void addMapEntry(MapEntryBean mapEntry) {
        mapEntries.add(mapEntry);
    }


}
