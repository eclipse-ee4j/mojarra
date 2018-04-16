/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

jsf.ajax.addOnEvent(function (data) {
    // the status is checked in the unittest
    window.status = data.status;
});

jsfAjaxRequest = jsf.ajax.request;

jsf.ajax.request = function (source, event, options) {
    // always make synchronous ajax calls to make live easier for HtmlUnit
    options.async = false;

    jsfAjaxRequest(source, event, options);
};
