/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
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

/**
 @project JSF Ajax Library
 @version 2.0
 @description This is the standard implementation of the JSF Ajax Library.
 */

/**
 * Register with OpenAjax
 */
if (typeof OpenAjax !== "undefined" &&
    typeof OpenAjax.hub.registerLibrary !== "undefined") {
    OpenAjax.hub.registerLibrary("mojarra", "www.sun.com", "1.0", null);
}

/**
 * @name mojarra
 * @namespace
 */

/*
 * Create our top level namespaces - mojarra
 */
var mojarra = mojarra || {};


/**
 * This function deletes any hidden parameters added
 * to the form by checking for a variable called 'adp'
 * defined on the form.  If present, this variable will
 * contain all the params added by 'apf'.
 *
 * @param f - the target form
 */
mojarra.dpf = function dpf(f) {
    var adp = f.adp;
    if (adp !== null) {
        for (var i = 0; i < adp.length; i++) {
            f.removeChild(adp[i]);
        }
    }
};

/*
 * This function adds any parameters specified by the
 * parameter 'pvp' to the form represented by param 'f'.
 * Any parameters added will be stored in a variable
 * called 'adp' and stored on the form.
 *
 * @param f - the target form
 * @param pvp - associative array of parameter
 *  key/value pairs to be added to the form as hidden input
 *  fields.
 */
mojarra.apf = function apf(f, pvp) {
    var adp = new Array();
    f.adp = adp;
    var i = 0;
    for (var k in pvp) {
        if (pvp.hasOwnProperty(k)) {
            var p = document.createElement("input");
            p.type = "hidden";
            p.name = k;
            p.value = pvp[k];
            f.appendChild(p);
            adp[i++] = p;
        }
    }
};

/*
 * This is called by command link and command button.  It provides
 * the form it is nested in, the parameters that need to be
 * added and finally, the target of the action.  This function
 * will delete any parameters added <em>after</em> the form
 * has been submitted to handle DOM caching issues.
 *
 * @param f - the target form
 * @param pvp - associative array of parameter
 *  key/value pairs to be added to the form as hidden input
 *  fields.
 * @param t - the target of the form submission
 */
mojarra.jsfcljs = function jsfcljs(f, pvp, t) {
    mojarra.apf(f, pvp);
    var ft = f.target;
    if (t) {
        f.target = t;
    }

    var input = document.createElement('input');
    input.type = 'submit';
    f.appendChild(input);
    input.click();
    f.removeChild(input);

    f.target = ft;
    mojarra.dpf(f);
};

/*
 * This is called by functions that need access to their calling
 * context, in the form of <code>this</code> and <code>event</code>
 * objects.
 *
 *  @param f the function to execute
 *  @param t this of the calling function
 *  @param e event of the calling function
 *  @return object that f returns
 */
mojarra.jsfcbk = function jsfcbk(f, t, e) {
    return f.call(t,e);
};

/*
 * This is called by the AjaxBehaviorRenderer script to
 * trigger a jsf.ajax.request() call.
 *
 *  @param s the source element or id
 *  @param e event of the calling function
 *  @param n name of the behavior event that has fired
 *  @param ex execute list
 *  @param re render list
 *  @param op options object
 */
mojarra.ab = function ab(s, e, n, ex, re, op) {
    if (!op) {
        op = {};
    }

    if (n) {
        op["javax.faces.behavior.event"] = n;
    }

    if (ex) {
        op["execute"] = ex;
    }

    if (re) {
        op["render"] = re;
    }

    jsf.ajax.request(s, e, op);
};

/*
 * This is called by command script when autorun=true.
 * 
 * @param l window onload callback function
 */
mojarra.l = function l(l) {
    if (document.readyState === "complete") {
        setTimeout(l);
    }
    else if (window.addEventListener) {
        window.addEventListener("load", l, false);
    }
    else if (window.attachEvent) {
        window.attachEvent("onload", l);
    }
    else if (typeof window.onload === "function") {
        var oldListener = window.onload;
        window.onload = function() { oldListener(); l(); };
    }
    else {
        window.onload = l;
    }
};
