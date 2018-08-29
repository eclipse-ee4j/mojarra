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

package com.sun.faces.test.servlet30.wcagdatatable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author edburns
 */
public class WindowsCodePageData {

    /** Creates a new instance of WindowsCodePageData */
    public WindowsCodePageData() {
        codePageData = new ArrayList<WindowsCodePageDataBean>();
        WindowsCodePageDataBean bean = null;
        bean = new WindowsCodePageDataBean("1200", "Unicode (BMP of ISO 10646)", false, false, true, true, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("1251", "Windows 3.1 Cyrillic", true, false, true, true, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("1250", "Windows 3.1 Eastern European", true, false, true, true, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("1252", "Windows 3.1 US (ANSI)", true, false, true, true, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("1253", "Windows 3.1 Greek", true, false, true, true, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("1254", "Windows 3.1 Turkish", true, false, true, true, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("1255", "Hebrew", true, false, false, false, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("1256", "Arabic", true, false, false, false, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("1257", "Baltic", true, false, false, false, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("1361", "Korean (Johab)", true, false, false, true, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("437", "MS-DOS United States", false, true, true, true, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("708", "Arabic (ASMO 708)", false, true, false, false, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("709", "Arabic (ASMO 449+, BCON V4)", false, true, false, false, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("710", "Arabic (Transparent Arabic)", false, true, false, false, true);
        codePageData.add(bean);
        bean = new WindowsCodePageDataBean("720", "Arabic (Transparent ASMO)", false, true, false, false, true);
        codePageData.add(bean);
    }

    private List<WindowsCodePageDataBean> codePageData = null;

    public List<WindowsCodePageDataBean> getCodePageData() {
        return codePageData;
    }

}
