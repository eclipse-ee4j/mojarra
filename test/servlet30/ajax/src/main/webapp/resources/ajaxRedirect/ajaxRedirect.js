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

var ajaxEvent = function ajaxEvent(data) {
    if (data.status == "complete") {
        var responseText = data.responseText;
        var re = new RegExp('<', 'g');
        responseText = responseText.replace(re, "&lt;");
        re = new RegExp('>', 'g');
        responseText = responseText.replace(re, "&gt;");
        re = new RegExp('\'', 'g');
        responseText = responseText.replace(re, "&quot;");
        
        var partialResponseElement = data.responseXML.getElementsByTagName('partial-response')[0];
        var redirectElement = partialResponseElement.getElementsByTagName('redirect')[0];
        partialResponseElement.removeChild(redirectElement);
        
        var changesElement = document.createElement('changes');
        partialResponseElement.appendChild(changesElement);
        
        var responseTextElementDiv = document.getElementById('responseText');
        responseTextElementDiv.innerHTML = responseText;
        
    }
};
