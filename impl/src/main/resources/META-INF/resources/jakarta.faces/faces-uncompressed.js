/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 * Copyright 2004-2008 Emmanouil Batsis, mailto: mbatsis at users full stop sourceforge full stop net
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
 @project Faces JavaScript Library
 @version 4.0
 @description This is the standard implementation of the Faces JavaScript Library.
 */

"use strict";

// Detect if this is already loaded, and if loaded, if it's a higher version
if ( !( (window.faces && window.faces.specversion && window.faces.specversion >= 40000 )
    && (window.faces.implversion && window.faces.implversion >= 4)) ) {

    // --- JS Lang --------------------------------------------------------------------
    const UDEF = 'undefined';
    const EMPTY = "";
    const SPACE = " ";
    const FORM = "form";
    const isNull = (value) => (typeof value === UDEF || (typeof value === "object" && !value));
    const isNotNull = (value) => !isNull(value);

    // --- Faces constants ------------------------------------------------------------
    const VIEW_STATE_PARAM = "jakarta.faces.ViewState";
    const CLIENT_WINDOW_PARAM = "jakarta.faces.ClientWindow";
    const ALWAYS_EXECUTE_IDS = [ VIEW_STATE_PARAM , CLIENT_WINDOW_PARAM ];
    const ENCODED_URL_PARAM = "jakarta.faces.encodedURL";

    /**
     * experimental: do partial submit during ajax request
     * todo: add a config parameter for this, where?
     */
    const PARTIAL_SUBMIT_ENABLED = true;

    /**
     * Check if a String or an Array contains a value
     * @ignore
     */
    const contains = function(stringOrArray,value) { return stringOrArray.indexOf(value) !== -1; }

    /**
     * Find instance of passed String via getElementById.
     * @ignore
     */
    const getElemById = function getElemById( elementOrId ) {
        return typeof elementOrId == 'string' ? document.getElementById(elementOrId) : elementOrId;
    };

    /**
     * get dom element or document child by name attribute
     * @ignore
     */
    const getElementByName = function(element, name) {
        return element.querySelector("[name='"+name+"']");
    }

    /**
     * get the input element inside a form identified by name attribute
     * @ignore
     */
    const getFormInputElementByName = function(form, inputElementName) {
        return inputElementName in form ? form[inputElementName] : getElementByName(form,inputElementName);
    }

    /**
     * append a new pair of parameter=value to a query string
     * @ignore
     */
    const appendToQueryString = function appendToQueryString( queryString , name, value) {
        return queryString + ( (queryString.length > 0 ? "&" : EMPTY) + encodeURIComponent(name) + "=" + encodeURIComponent(value) );
    };

    /**
     * return true if one of the dom elements contains
     * a child with the attribute name equals to the passed name
     * @param elements an array of DOM elements
     * @param name the value of the attribute name
     * @returns {boolean} true if at least one of the domElements contains a child with the attribute name equals to the passed param name
     * @ignore
     */
    const containsNamedChild = function (elements,name) {
        return elements.some( elem => !!getElementByName(elem,name) );
    }

    /**
     * <span class="changed_modified_2_2">The top level global namespace
     * for Jakarta Faces functionality.</span>
     * @name faces
     * @namespace
     */
    window.faces = {};

    /**
     * <span class="changed_modified_2_2 changed_modified_2_3">The namespace for Ajax
     * functionality.</span>
     * @name faces.ajax
     * @namespace
     * @exec
     */
    faces.ajax = function() {

        const eventListeners = [];
        const errorListeners = [];

        let delayHandler = null;

        /**
         * Note by pizzi80:
         * Replacing DOM element's innerHTML does not execute the (eventually) injected javascript.
         * This is standard, and we don't need anymore to do this test.
         * I'll leave this only as a placeholder to identify the places where
         * it is used in the code.
         * In the future a new and unique replace algorithm will replace all
         * the actual code, and it will be possible to remove this and the function isAutoExec()
         *
         * Determine if loading scripts into the page executes the script.
         * This is instead of doing a complicated browser detection algorithm.  Some do, some don't.
         * @returns {boolean} does including a script in the dom execute it?
         * @ignore
         */
        const isAutoExec = function isAutoExec() { return false; };

        /**
         * Utility function that determines if a file control exists for the form.
         * @ignore
         */
        const hasInputFileControl = function(form) { return isNotNull(form.querySelector("input[type='file']")); };


        // --- FACES input processing functions ---------------------------------------------------------------------------------------

        /**
         * Get the form element which encloses the supplied element.
         * @param element - element to act against in search
         * @returns form element representing enclosing form, or first form if none found.
         * @ignore
         */
        const getForm = function(element) {
            const form = element.closest(FORM);
            return form ? form : document.forms[0];
        };

        /**
         * Get an array of all Faces form elements which need their view state to be updated.
         * This covers at least the form that submitted the request and any form that is covered in the render target list.
         *
         * @param context An object containing the request context, including the following properties:
         * the source element, per call onerror callback function, per call onevent callback function, the render
         * instructions, the submitting form ID, the naming container ID and naming container prefix.
         */
        const getFormsToUpdate = function getFormsToUpdate(context) {
            const formsToUpdate = [];

            const add = function(element) {
                if (element) {
                    if (element.nodeName
                        && element.nodeName.toLowerCase() === FORM
                        && element.method === "post"
                        && element.id
                        && element.elements
                        && element.id.startsWith(context.namingContainerPrefix) ) {
                            formsToUpdate.push(element);
                    }
                    else {
                        const forms = element.getElementsByTagName(FORM);
                        for ( const form of forms )
                            add(form);
                    }
                }
            };

            if (context.formId) {
                add(document.getElementById(context.formId));
            }

            if (context.render) {
                if ( contains(context.render,"@all") ) {
                    add(document);
                } else {
                    const clientIds = context.render.split(SPACE);
                    for ( const clientId of clientIds )
                        add(document.getElementById(clientId));
                }
            }

            return formsToUpdate;
        };

        /**
         * <p>Namespace given space separated parameters if necessary (only
         * call this if there is a namingContainerPrefix!).  This
         * function is here for backwards compatibility with manual
         * faces.ajax.request() calls written before Spec790 changes.</p>

         * @param parameters Space separated string of parameters as
         * usually specified in f:ajax execute and render attributes.

         * @param sourceClientId The client ID of the f:ajax
         * source. This is to be used for prefixing relative target
         * client IDs.

         * It's expected that this already starts with
         * namingContainerPrefix.

         * @param namingContainerPrefix The naming container prefix (the
         * view root ID suffixed with separator character).

         * This is to be used for prefixing absolute target client IDs.
         * @ignore
         */
        const namespaceParametersIfNecessary = function namespaceParametersIfNecessary(parameters, sourceClientId, namingContainerPrefix) {
            if (sourceClientId.indexOf(namingContainerPrefix) !== 0) {
                return parameters; // Unexpected source client ID; let's silently do nothing.
            }

            const targetClientIds = parameters.replace(/^\s+|\s+$/g, '').split(/\s+/g);

            // adapt each targetClientId and replace the modified version inside the original array
            for ( let i = 0; i < targetClientIds.length; i++) {
                let targetClientId = targetClientIds[i];

                if (targetClientId.indexOf(faces.separatorchar) === 0) {
                    targetClientId = targetClientId.substring(1);

                    if (targetClientId.indexOf(namingContainerPrefix) !== 0) {
                        targetClientId = namingContainerPrefix + targetClientId;
                    }
                } else if (targetClientId.indexOf(namingContainerPrefix) !== 0) {
                    const parentClientId = sourceClientId.substring(0, sourceClientId.lastIndexOf(faces.separatorchar));

                    if (namingContainerPrefix + targetClientId === parentClientId) {
                        targetClientId = parentClientId;
                    } else {
                        targetClientId = parentClientId + faces.separatorchar + targetClientId;
                    }
                }

                // replace the modified target client inside the array
                targetClientIds[i] = targetClientId;
            }

            // return a space separated string of all the target client id
            return targetClientIds.join(SPACE);
        };


        // --- HTML as String processing functions ----------------------------------------------------------------------------

        // Regex to find all scripts in a string
        const SCRIPT_TAG_REGEX = /<script[^>]*>([\S\s]*?)<\/script>/igm;

        // Regex to find one script, to isolate it's content [2] and attributes [1]
        const SINGLE_SCRIPT_TAG_REGEX = /<script([^>]*)>([\S\s]*?)<\/script>/im;

        // Regex to find type attribute
        const TAG_ATTRIBUTE_TYPE_REGEX = /type="([\S]*?)"/im;

        /**
         * Get all scripts from supplied string, return them as an array for later processing.
         * @param html a String containing a portion of html
         * @returns {array} of script text
         * @ignore
         */
        const getScripts = function getScripts(html) {
            const scripts = [];
            const initialnodes = html.match(SCRIPT_TAG_REGEX);
            while (!!initialnodes && initialnodes.length > 0) {
                let scriptStr = [];
                scriptStr = initialnodes.shift().match(SINGLE_SCRIPT_TAG_REGEX); // todo: multiple shift array ... rewrite this algo
                // check the type - skip if specified but not text/javascript
                const type = scriptStr[1].match(TAG_ATTRIBUTE_TYPE_REGEX);
                if (!!type && type[1] !== "text/javascript") {
                    continue;
                }
                scripts.push(scriptStr);
            }
            return scripts;
        };

        /**
         * Remove all the portion of code matching the script pattern from the passed string
         * @param html a String containing a portion of html
         * @ignore
         */
        const removeScripts = function removeScripts(html) {
            return html.replace(/<script[^>]*type="text\/javascript"[^>]*>([\S\s]*?)<\/script>/igm, EMPTY);
        };

        /**
         * Run an array of script nodes,
         * @param scripts Array of script nodes.
         * @ignore
         */
        const runScripts = function runScripts(scripts) {
            if (!scripts || scripts.length === 0) {
                return;
            }

            const loadedScripts = document.getElementsByTagName("script");
            const loadedScriptUrls = [];

            for ( const scriptNode of loadedScripts ) {
                const url = scriptNode.getAttribute("src");
                if (url) loadedScriptUrls.push(url);
            }

            const head = document.head || document.getElementsByTagName('head')[0] || document.documentElement;
            runScript(head, loadedScriptUrls, scripts, 0);
        };

        /**
         * Run script at given index.
         * @param head Document's head.
         * @param loadedScriptUrls URLs of scripts which are already loaded.
         * @param scripts Array of script nodes.
         * @param index Index of script to be loaded.
         * @ignore
         */
        const runScript = function runScript(head, loadedScriptUrls, scripts, index) {
            if (index >= scripts.length) {
                return;
            }

            // Regex to find src attribute
            const findsrc = /src="([\S]*?)"/im;
            // Regex to remove leading cruft
            const stripStart = /^\s*(<!--)*\s*(\/\/)*\s*(\/\*)*\s*\n*\**\n*\s*\*.*\n*\s*\*\/(<!\[CDATA\[)*/;

            const scriptLoadedStates = [ 'loaded','complete' ];

            const scriptStr = scripts[index];
            const src = scriptStr[1].match(findsrc);
            let scriptLoadedViaUrl = false;

            if (!!src && src[1]) {
                // if this is a file, load it
                const url = unescapeHTML(src[1]);
                // if this is already loaded, don't load it
                // it's never necessary, and can make debugging difficult
                if (loadedScriptUrls.indexOf(url) < 0) {
                    // create script node
                    let scriptNode = document.createElement('script');
                    const parserElement = document.createElement('div');
                    parserElement.innerHTML = scriptStr[0];
                    cloneAttributes(scriptNode, parserElement.firstChild);
                    deleteNode(parserElement);
                    //scriptNode.type = 'text/javascript';
                    scriptNode.src = url; // add the src to the script node
                    scriptNode.onload = scriptNode.onreadystatechange = function(_, abort) {
                        if (abort || !scriptNode.readyState || scriptLoadedStates.includes(scriptNode.readyState) ) {
                            scriptNode = null;                                           // why?
                            runScript(head, loadedScriptUrls, scripts, index + 1); // Run next script.
                        }
                    };
                    head.appendChild(scriptNode); // add it to end of the head (and don't remove it)
                    scriptLoadedViaUrl = true;
                }
            } else if (!!scriptStr && scriptStr[2]) {
                // else get content of tag, without leading CDATA and such
                const script = scriptStr[2].replace(stripStart, EMPTY);

                if (!!script) {
                    // create script node
                    const scriptNode = document.createElement('script');
                    // scriptNode.type = 'text/javascript';
                    scriptNode.text = script; // add the code to the script node
                    head.appendChild(scriptNode); // add it to the head
                    head.removeChild(scriptNode); // then remove it
                }
            }

            if (!scriptLoadedViaUrl) {
                runScript(head, loadedScriptUrls, scripts, index + 1); // Run next script.
            }
        };

        /**
         * Get all stylesheets from supplied string and run them all.
         * @param str
         * @ignore
         */
        const runStylesheets = function runStylesheets(str) {
            // Regex to find all links in a string
            const findlinks = /<link[^>]*\/>/igm;
            // Regex to find one link, to isolate its attributes [1]
            const findlink = /<link([^>]*)\/>/im;
            // Regex to find type attribute
            const findtype = /type="([\S]*?)"/im;
            const findhref = /href="([\S]*?)"/im;

            // the head of the document, note that document.head do not always work
            const head = document.head || document.getElementsByTagName('head')[0] || document.documentElement;

            let loadedStylesheetUrls = null;
            let parserElement = null;

            const initialnodes = str.match(findlinks);
            while (!!initialnodes && initialnodes.length > 0) {
                const linkStr = initialnodes.shift().match(findlink);
                // check the type - skip if specified but not text/css
                const type = linkStr[1].match(findtype);
                if (!!type && type[1] !== "text/css") {
                    continue;
                }
                const href = linkStr[1].match(findhref);
                if (!!href && href[1]) {
                    if (loadedStylesheetUrls === null) {
                        const loadedLinks = document.getElementsByTagName("link");
                        loadedStylesheetUrls = [];

                        for ( const linkNode of loadedLinks ) {
                            const linkNodeType = linkNode.getAttribute("type");
                            if (!linkNodeType || linkNodeType === "text/css") {
                                const url = linkNode.getAttribute("href");

                                if (url) {
                                    loadedStylesheetUrls.push(url);
                                }
                            }
                        }
                    }

                    const url = unescapeHTML(href[1]);

                    if ( loadedStylesheetUrls && loadedStylesheetUrls.indexOf(url) < 0) {
                        // create stylesheet node
                        parserElement = parserElement !== null ? parserElement : document.createElement('div');
                        parserElement.innerHTML = linkStr[0];
                        const linkNode = parserElement.firstChild;
                        linkNode.type = 'text/css';
                        linkNode.rel = 'stylesheet';
                        linkNode.href = url;
                        head.appendChild(linkNode); // add it to end of the head (and don't remove it)
                    }
                }
            }

            deleteNode(parserElement);
        };

        /**
         * Replace DOM element with a new tag name and supplied innerHTML
         * @param element element to replace
         * @param tempTagName new tag name to replace with
         * @param src string new content for element
         * @ignore
         */
        const elementReplaceStr = function elementReplaceStr(element, tempTagName, src) {
            // Creating a head element isn't allowed in IE, and faulty in most browsers,
            // so it is not allowed
            if (element && element.nodeName && element.nodeName.toLowerCase() === "head")
                throw new Error("Attempted to replace a head element - this is not allowed.");

            const temp = document.createElement(tempTagName);
            if (element.id) {
                temp.id = element.id;
            }

            if (isAutoExec()) {
                temp.innerHTML = src;
                cloneAttributes(temp, element);
                replaceNode(temp, element);
            }
            else {
                // Get scripts from text
                const scripts = getScripts(src);
                // Remove scripts from text
                src = removeScripts(src);
                temp.innerHTML = src;
                cloneAttributes(temp, element);
                replaceNode(temp, element);
                runScripts(scripts);
            }

        };

        // --- Faces xml errors ---------------------------------------------------------------------------

        const PARSED_OK = "Document contains no parsing errors";
        const PARSED_EMPTY = "Document is empty";
        const PARSED_UNKNOWN_ERROR = "Not well-formed or other error";

        /**
         * <p>Returns a human readable description of the parsing error. Useful
         * for debugging. Tip: append the returned error string in a &lt;pre&gt;
         * element if you want to render it.</p>
         * @param  doc The target DOM document
         * @returns {String} The parsing error description of the target Document in
         *          human readable form (preformatted text)
         * @ignore
         * Note:  This code originally from Sarissa: http://dev.abiss.gr/sarissa
         */
        const getParseErrorText = function (doc) {
            let parseErrorText = PARSED_OK;
            if ((!doc) || (!doc.documentElement)) {
                parseErrorText = PARSED_EMPTY;
            } else if (doc.documentElement.tagName === "parsererror") {
                parseErrorText = doc.documentElement.firstChild.data;
                parseErrorText += "\n" + doc.documentElement.firstChild.nextSibling.firstChild.data;
            } else if (doc.getElementsByTagName("parsererror").length > 0) {
                const parsererror = doc.getElementsByTagName("parsererror")[0];
                // parseErrorText = getText(parsererror, true) + "\n";
                parseErrorText = parsererror.textContent + "\n";
            } else if (doc.parseError && doc.parseError.errorCode !== 0) {
                parseErrorText = PARSED_UNKNOWN_ERROR;
            }
            return parseErrorText;
        };

        // --- DOM Manipulation ---------------------------------------------------------------------------------------------------------

        // PENDING - add support for removing handlers added via DOM 2 methods

        const NODE_EVENTS = [
            'abort', 'blur', 'change', 'error', 'focus', 'load', 'reset', 'resize', 'scroll', 'select', 'submit', 'unload',
            'keydown', 'keypress', 'keyup', 'click', 'mousedown', 'mousemove', 'mouseout', 'mouseover', 'mouseup', 'dblclick'
        ];

        /**
         * Delete all events attached to a node
         * @param node
         * @ignore
         */
        const clearEvents = function clearEvents(node) {
            if (!node) {
                return;
            }
            // don't do anything for text and comment nodes - unnecessary
            if (node.nodeType === Node.TEXT_NODE || node.nodeType === Node.COMMENT_NODE) {
                return;
            }
            // remove the events from node
            try {
                for (const eventName of NODE_EVENTS)
                    node[eventName] = null;
            } catch (ex) {
                // it's OK if it fails, at least we tried
            }
        };

        /**
         * Deletes node
         * @param node
         * @ignore
         */
        const deleteNode = function deleteNode(node) {
            if (node) node.remove();
        };

        /**
         * Delete all nodes
         * @param nodes array of node
         * @ignore
         */
        const deleteNodes = function deleteNodes( nodes ) {
            for ( const node of nodes )
                deleteNode(node);
        };

        /**
         * Deletes all children of a node
         * @param node
         * @ignore
         */
        const deleteChildren = function deleteChildren(node) {
            if (node)
                while (node.lastChild)
                    node.lastChild.remove();
        };

        /**
         * <p> Copies the childNodes of nodeFrom to nodeTo</p>
         *
         * @param  nodeFrom the Node to copy the childNodes from
         * @param  nodeTo the Node to copy the childNodes to
         * @ignore
         */
        const copyChildNodes = function copyChildNodes(nodeFrom, nodeTo) {

            if ((!nodeFrom) || (!nodeTo)) {
                throw "Both source and destination nodes must be provided";
            }

            deleteChildren(nodeTo);

            // if within the same doc, just move, else copy and delete
            if (nodeFrom.ownerDocument === nodeTo.ownerDocument) {
                while (nodeFrom.firstChild)
                    nodeTo.appendChild(nodeFrom.firstChild);

            } else {
                const ownerDoc = nodeTo.nodeType === Node.DOCUMENT_NODE ? nodeTo : nodeTo.ownerDocument;
                const nodeFromChildNodes = nodeFrom.childNodes;

                //if ( typeof(ownerDoc.importNode) !== UDEF ) {
                    for ( const nodeFromChild of nodeFromChildNodes )
                        nodeTo.appendChild(ownerDoc.importNode(nodeFromChild, true));

                //} else {
                //    for ( const nodeFromChild of nodeFromChildNodes )
                //        nodeTo.appendChild(nodeFromChild.cloneNode(true));
                //}

            }
        };

        /**
         * Replace one node with another.
         * @param node node to replace
         * @param newNode the new node that's replace the old one
         * @ignore
         */
        const replaceNode = function replaceNode(newNode, node) {
            node.replaceWith(newNode);
        };

        /**
         * @ignore
         */
        const propertyToAttribute = function propertyToAttribute(name) {
            if (name === 'className')    return 'class';
            else if (name === 'xmllang') return 'xml:lang';
            else                         return name.toLowerCase();
        };

        // Enumerate all the names of the event listeners
        const LISTENER_NAMES = [
            'onclick', 'ondblclick', 'onmousedown', 'onmousemove', 'onmouseout',
            'onmouseover', 'onmouseup', 'onkeydown', 'onkeypress', 'onkeyup',
            'onhelp', 'onblur', 'onfocus', 'onchange', 'onload', 'onunload', 'onabort',
            'onreset', 'onselect', 'onsubmit'
        ];

        // enumerate core element attributes - without 'dir' as special case
        const coreElementProperties = ['className', 'title', 'lang', 'xmllang'];

        // enumerate additional input element attributes
        const inputElementProperties = [ 'name', 'value', 'size', 'maxLength', 'src', 'alt', 'useMap', 'tabIndex', 'accessKey', 'accept', 'type' ];

        // core + input element properties
        const coreAndInputElementProperties = coreElementProperties.concat(inputElementProperties);

        // enumerate additional boolean input attributes
        const inputElementBooleanProperties = [ 'checked', 'disabled', 'readOnly' ];

        const TABLE_INNER_TAGS = ['td', 'th', 'tr', 'tbody', 'thead', 'tfoot'];

        /**
         * copy all attributes from one element to another - except id
         * @param target element to copy attributes to
         * @param source element to copy attributes from
         * @ignore
         */
        const cloneAttributes = function cloneAttributes(target, source) {

            const isInputElement = target.nodeName.toLowerCase() === 'input';
            const propertyNames = isInputElement ? coreAndInputElementProperties : coreElementProperties;
            const isXML = !source.ownerDocument.contentType || source.ownerDocument.contentType === 'text/xml';

            for (const propertyName of propertyNames) {
                const attributeName = propertyToAttribute(propertyName);
                const sourceValue = isXML ? source.getAttribute(attributeName) : source[propertyName];
                if (isNotNull(sourceValue)) target[propertyName] = sourceValue;
            }

            const booleanPropertyNames = isInputElement ? inputElementBooleanProperties : [];
            for (const booleanPropertyName of booleanPropertyNames) {
                const newBooleanValue = source[booleanPropertyName];
                if (isNotNull(newBooleanValue)) target[booleanPropertyName] = newBooleanValue;
            }

            //'style' attribute special case
            if (source.hasAttribute('style')) {
                const sourceStyle = source.getAttribute('style');
                if (isNotNull(sourceStyle)) target.setAttribute('style', sourceStyle);
            } else if (target.hasAttribute('style')) {
                target.removeAttribute('style');
            }

            // Special case for 'dir' attribute
            if (source.dir !== target.dir) {
                if (source.hasAttribute('dir')) {
                    target.dir = source.dir;
                } else if (target.hasAttribute('dir')) {
                    target.dir = '';
                }
            }

            for (const name of LISTENER_NAMES) {
                target[name] = source[name] ? source[name] : null;
                if (source[name]) {
                    source[name] = null;
                }
            }

            // clone HTML5 data-* attributes
            const sourceDataset = source.dataset;
            const targetDataset = target.dataset;
            if (targetDataset || sourceDataset) {
                //cleanup the dataset
                for (const tp in targetDataset) {
                    delete targetDataset[tp];
                }
                //copy dataset's properties
                for (const sp in sourceDataset) {
                    targetDataset[sp] = sourceDataset[sp];
                }
            }
        };

        /**
         * Replace an element from one document into another
         * @param newElement new element to put in document
         * @param origElement original element to replace
         * @ignore
         */
        const elementReplace = function elementReplace(newElement, origElement) {

            // copy source attributes to target node
            try {
                cloneAttributes(origElement, newElement);
            } catch (ex) {
                // if in dev mode, report an error, else try to limp onward
                if (faces.getProjectStage() === "Development") {
                    throw new Error("Error updating attributes");
                }
            }

            // copy source html to target node
            origElement.innerHTML = newElement.innerHTML;

            // delete source node
            deleteNode(newElement);
        };

        /**
         * Create a new document, then select the body element within it
         * @param docStr Stringified version of document to create
         * @return element the body element
         * @ignore
         */
        const getBodyElement = function getBodyElement(docStr) {

            const doc = (new DOMParser()).parseFromString(docStr, "text/xml")

            // if there is an error
            const parsedError = getParseErrorText(doc);
            if (parsedError !== PARSED_OK) {
                throw new Error(parsedError);
            }

            // doc.body do not work in this situation
            const body = doc.getElementsByTagName("body")[0];

            if (!body) {
                throw new Error("Can't find body tag in returned document.");
            }

            return body;
        };

        // --- Faces Ajax response DOM operation algorithms ----------------------------------------------------------------------------------------

        /**
         * Find encoded url field for a given form.
         * @param form
         * @ignore
         */
        const getEncodedUrlElement = function getEncodedUrlElement(form) {
            return getFormInputElementByName(form,ENCODED_URL_PARAM);
        };

        /**
         * Update hidden state fields from the server into the DOM for any Faces forms which need to be updated.
         * This covers at least the form that submitted the request and any form that is covered in the render target list.
         *
         * @param updateElement The update element of partial response holding the state value.
         * @param context An object containing the request context, including the following properties:
         * the source element, per call onerror callback function, per call onevent callback function, the render
         * instructions, the submitting form ID, the naming container ID and naming container prefix.
         * @param hiddenStateFieldName The hidden state field name, e.g. jakarta.faces.ViewState or jakarta.faces.ClientWindow
         */
        const updateHiddenStateFields = function updateHiddenStateFields(updateElement, context, hiddenStateFieldName) {
            const firstChild = updateElement.firstChild;
            const state = (typeof firstChild.wholeText !== 'undefined') ? firstChild.wholeText : firstChild.nodeValue;
            const formsToUpdate = getFormsToUpdate(context);

            for ( const form of formsToUpdate ) {
                let field = getHiddenStateField(form, hiddenStateFieldName, context.namingContainerPrefix);
                if (isNull(field)) {
                    field = document.createElement("input");
                    field.type = "hidden";
                    field.name = context.namingContainerPrefix + hiddenStateFieldName;
                    form.appendChild(field);
                }
                field.value = state;
            }
        };

        /**
         * Find hidden state field for a given form.
         * @param form The form to find hidden state field in.
         * @param hiddenStateFieldName The hidden state field name, e.g. jakarta.faces.ViewState or jakarta.faces.ClientWindow
         * @param namingContainerPrefix The naming container prefix, if any (the view root ID suffixed with separator character).
         * @ignore
         */
        const getHiddenStateField = function getHiddenStateField(form, hiddenStateFieldName, namingContainerPrefix) {
            const fullHiddenStateFieldName = namingContainerPrefix ? namingContainerPrefix+hiddenStateFieldName : hiddenStateFieldName;
            return getFormInputElementByName( form , fullHiddenStateFieldName );
        };

        /**
         * Do update.
         * @param updateElement The update element of partial response.
         * @param context An object containing the request context, including the following properties:
         * the source element, per call onerror callback function, per call onevent callback function, the render
         * instructions, the submitting form ID, the naming container ID and naming container prefix.
         * @ignore
         */
        const doUpdate = function doUpdate(updateElement, context) {

            let scripts = []; // temp holding value for array of script nodes
            let newElement;

            const id = updateElement.getAttribute('id');
            const viewStateRegex = new RegExp(context.namingContainerPrefix + VIEW_STATE_PARAM + faces.separatorchar + ".+$");
            const windowIdRegex = new RegExp(context.namingContainerPrefix + CLIENT_WINDOW_PARAM + faces.separatorchar + ".+$");

            if (id.match(viewStateRegex)) {
                updateHiddenStateFields(updateElement, context, VIEW_STATE_PARAM);
                return;
            } else if (id.match(windowIdRegex)) {
                updateHiddenStateFields(updateElement, context, CLIENT_WINDOW_PARAM);
                return;
            }

            // join the CDATA sections in the markup
            let markup = EMPTY;
            for ( const updateElementChild of updateElement.childNodes ) {
                markup += updateElementChild.nodeValue;
            }

            const src = markup;

            if (id === "jakarta.faces.ViewHead") {
                throw new Error("jakarta.faces.ViewHead not supported - browsers cannot reliably replace the head's contents");
            } else if (id === "jakarta.faces.Resource") {
                runStylesheets(src);
                scripts = getScripts(src);
                runScripts(scripts);
            } else {
                const element = getElemById(id);

                if (context.namingContainerId && id === context.namingContainerId) {
                    // spec790: If UIViewRoot is a NamingContainer and this is currently being updated,
                    // then it means that ajax navigation has taken place.
                    // So, ensure that context.render has correct value for this condition,
                    // because this is not necessarily correctly specified during the request.
                    context.render = element ? context.namingContainerId : "@all";
                }

                if (id === "jakarta.faces.ViewRoot" || id === "jakarta.faces.ViewBody" || context.render === "@all") {

                    // spec790: If UIViewRoot is currently being updated,
                    // then it means that ajax navigation has taken place.
                    // So, ensure that context.render has correct value for this condition,
                    // because this is not necessarily correctly specified during the request.
                    context.render = "@all";

                    const bodyStartEx = new RegExp("< *body[^>]*>", "gi");
                    const bodyEndEx = new RegExp("< */ *body[^>]*>", "gi");

                    // document.body is not working as expected
                    const docBody = document.getElementsByTagName('body')[0];
                    const bodyStart = bodyStartEx.exec(src);

                    // replace body tag
                    if (bodyStart !== null) {
                        // First, try with XML manipulation
                        try {
                            runStylesheets(src);
                            // Get scripts from text
                            scripts = getScripts(src);
                            // Remove scripts from text
                            const newSrc = removeScripts(src);
                            elementReplace(getBodyElement(newSrc), docBody);
                            runScripts(scripts);
                        } catch (e) {
                            // OK, replacing the body didn't work with XML - fall back to quirks mode insert
                            let srcBody, bodyEnd;
                            // if src contains </body>
                            bodyEnd = bodyEndEx.exec(src);
                            if (bodyEnd !== null) {
                                srcBody = src.substring(bodyStartEx.lastIndex, bodyEnd.index);
                            } else { // can't find the </body> tag, punt
                                srcBody = src.substring(bodyStartEx.lastIndex);
                            }
                            // replace body contents with innerHTML - note, script handling happens within function
                            elementReplaceStr(docBody, "body", srcBody);
                        }

                    }
                    // replace body contents with innerHTML - note, script handling happens within function
                    else {
                        elementReplaceStr(docBody, "body", src);
                    }
                } else {
                    if (!element) {
                        throw new Error("During update: " + id + " not found");
                    }

                    // Trim space padding before assigning to innerHTML
                    let html = src.trim();
                    let newElementContainer = document.createElement('div');

                    const tag = element.nodeName.toLowerCase();
                    const isTableInnerElement = TABLE_INNER_TAGS.includes(tag);

                    if (isTableInnerElement) {
                        if (isAutoExec()) {
                            // enclose new html inside a table
                            newElementContainer.innerHTML = '<table>' + html + '</table>';
                        } else {
                            // Get the scripts from the html
                            scripts = getScripts(html);
                            // Remove scripts from html
                            html = removeScripts(html);
                            // enclose new html inside a table
                            newElementContainer.innerHTML = '<table>' + html + '</table>';
                        }
                        newElement = newElementContainer.firstChild;
                        //some browsers will also create intermediary elements such as table>tbody>tr>td
                        while ((null !== newElement) && (id !== newElement.id)) {
                            newElement = newElement.firstChild;
                        }

                        replaceNode(newElement,element);
                        runScripts(scripts);

                    } else if (element.nodeName.toLowerCase() === 'input') {
                        // special case handling for 'input' elements
                        // in order to not lose focus when updating,
                        // input elements need to be added in place.
                        newElementContainer = document.createElement('div');
                        newElementContainer.innerHTML = html;
                        newElement = newElementContainer.firstChild;

                        cloneAttributes(element, newElement);
                        deleteNode(newElementContainer);
                    } else if (html.length > 0) {
                        if (isAutoExec()) {
                            // Create html
                            newElementContainer.innerHTML = html;
                        } else {
                            // Get the scripts from the text
                            scripts = getScripts(html);
                            // Remove scripts from text
                            html = removeScripts(html);
                            newElementContainer.innerHTML = html;
                        }
                        replaceNode(newElementContainer.firstChild, element);
                        deleteNode(newElementContainer);
                        runScripts(scripts);
                    }
                }
            }
        };

        /**
         * Delete a node specified by the element.
         * @param element
         * @ignore
         */
        const doDelete = function doDelete(element) {
            if (element) deleteNode(getElemById(element.getAttribute('id')));
        };

        /**
         * Insert a node specified by the element.
         * @param element
         * @ignore
         */
        const doInsert = function doInsert(element) {

            let target = getElemById(element.firstChild.getAttribute('id'));
            const parent = target.parentNode;
            let html = element.firstChild.firstChild.nodeValue;

            // todo: check if is it possible to use the TABLE_ELEMENTS array and remove the RegExp
            const tablePattern = new RegExp("<\\s*(td|th|tr|tbody|thead|tfoot)", "i");
            const isInTable = tablePattern.test(html);

            if (!isAutoExec())  {
                // Get the scripts from the text
                const scripts = getScripts(html);
                // Remove scripts from text
                html = removeScripts(html);
                // execute scripts
                runScripts(scripts);
            }
            const tempElement = document.createElement('div');
            let newElement;
            if (isInTable)  {
                tempElement.innerHTML = '<table>' + html + '</table>';
                newElement = tempElement.firstChild;
                //some browsers will also create intermediary elements such as table>tbody>tr>td
                //test for presence of id on the new element since we do not have it directly
                while ((null !== newElement) && (EMPTY === newElement.id)) {
                    newElement = newElement.firstChild;
                }
            } else {
                tempElement.innerHTML = html;
                newElement = tempElement.firstChild;
            }

            if (element.firstChild.nodeName === 'after') {
                // Get the next in the list, to insert before
                target = target.nextSibling;
            }  // otherwise, this is a 'before' element
            if (!!tempElement.innerHTML) { // check if only scripts were inserted - if so, do nothing here
                parent.insertBefore(newElement, target);
            }

            deleteNode(tempElement);
        };

        /**
         * Modify attributes of given element id.
         * @param element
         * @ignore
         */
        const doAttributes = function doAttributes(element) {

            // Get id of element we'll act against
            const id = element.getAttribute('id');
            const target = getElemById(id);

            if (!target) {
                throw new Error("The specified id: " + id + " was not found in the page.");
            }

            // There can be multiple attributes modified.  Loop through the list.
            const nodes = element.childNodes;
            for ( const node of nodes ) {
                const name = node.getAttribute('name');
                const value = node.getAttribute('value');

                //boolean attribute handling code for all browsers
                if (name === 'disabled') {
                    target.disabled = value === 'disabled' || value === 'true';
                    return;
                } else if (name === 'checked') {
                    target.checked = value === 'checked' || value === 'on' || value === 'true';
                    return;
                } else if (name === 'readonly') {
                    target.readOnly = value === 'readonly' || value === 'true';
                    return;
                }

                if (name === 'value') {
                    target.value = value;
                } else {
                    target.setAttribute(name, value);
                }
            }
        };

        /**
         * Eval the CDATA of the element.
         * Evaluate the parsed JavaScript code in a global context.
         * @param element to eval
         * @ignore
         */
        const doEval = function doEval(element) {
            (() => { //
                const src = element ? element.textContent : undefined;
                if (src) window.eval.call(window, src);
                else console.warn('called doEval with no source code');
            })();
        };

        /**
         * Ajax Request Queue
         * @ignore
         */
        const Queue = function Queue() {

            // Create the internal queue
            let queue = [];

            // the amount of space at the front of the queue, initialised to zero
            let queueSpace = 0;

            /** Returns the size of this Queue. The size of a Queue is equal to the number
             * of elements that have been enqueued minus the number of elements that have
             * been dequeued.
             * @ignore
             */
            this.getSize = function getSize() {
                return queue.length - queueSpace;
            };

            /**
             * Returns true if this Queue is empty, and false otherwise. A Queue is empty
             * if the number of elements that have been enqueued equals the number of
             * elements that have been dequeued.
             * @ignore
             */
            this.isEmpty = function isEmpty() {
                return (queue.length === 0);
            };

            /**
             * Enqueues the specified element in this Queue.
             *
             * @param element - the element to enqueue
             * @ignore
             */
            this.enqueue = function enqueue(element) {
                queue.push(element);
            };


            /**
             * Dequeues an element from this Queue. The oldest element in this Queue is
             * removed and returned. If this Queue is empty then undefined is returned.
             *
             * @returns Object The element that was removed from the queue.
             * @ignore
             */
            this.dequeue = function dequeue() {
                // initialise the element to return to be undefined
                let element = undefined;

                // check whether the queue is empty
                if (queue.length) {
                    // fetch the oldest element in the queue
                    element = queue[queueSpace];

                    // update the amount of space and check whether a shift should occur
                    if (++queueSpace * 2 >= queue.length) {
                        // set the queue equal to the non-empty portion of the queue
                        queue = queue.slice(queueSpace);
                        // reset the amount of space at the front of the queue
                        queueSpace = 0;
                    }
                }
                // return the removed element
                return element;
            };

            /**
             * Returns the oldest element in this Queue. If this Queue is empty then
             * undefined is returned. This function returns the same value as the dequeue
             * function, but does not remove the returned element from this Queue.
             * @ignore
             */
            this.getOldestElement = function getOldestElement() {
                return queue.length ? queue[queueSpace] : undefined;
            };
        };


        /**
         * AjaxEngine handles Ajax implementation details.
         * @ignore
         */
        const AjaxEngine = function AjaxEngine(context) {

            const req = {};            // Request Object
            req.url = null;                // Request URL
            req.context = context;         // Context of request and response
            req.context.sourceid = null;   // Source of this request
            req.context.onerror = null;    // Error handler for request
            req.context.onevent = null;    // Event handler for request
            req.context.namingContainerId = null;       // If UIViewRoot is an instance of NamingContainer this represents its ID.
            req.context.namingContainerPrefix = null;   // If UIViewRoot is an instance of NamingContainer this represents its ID suffixed with separator character, else an empty string.
            req.xmlReq = null;             // XMLHttpRequest Object
            req.async = true;              // Default - Asynchronous
            req.parameters = {};           // Parameters For GET or POST
            req.queryString = null;        // Encoded Data For GET or POST
            req.method = null;             // GET or POST
            req.status = null;             // Response Status Code From Server
            req.fromQueue = false;         // Indicates if the request was taken off the queue before being sent. This prevents the request from entering the queue redundantly.
            req.que = new Queue();         // the queue for requests
            req.xmlReq = new XMLHttpRequest(); // The real XMLHttpRequest Level2

            // Set up request/response state callbacks
            /**
             * @ignore
             */
            req.xmlReq.onreadystatechange = function() {
                if (req.xmlReq.readyState === 4) {
                    req.onComplete();
                }
            };

            /**
             * This function is called when the request/response interaction
             * is complete.  If the return status code is successfull,
             * dequeue all requests from the queue that have completed.  If a
             * request has been found on the queue that has not been sent,
             * send the request.
             * @ignore
             */
            req.onComplete = function onComplete() {
                if (req.xmlReq.status && (req.xmlReq.status >= 200 && req.xmlReq.status < 300)) {
                    sendEvent(req.xmlReq, req.context, "complete");
                    faces.ajax.response(req.xmlReq, req.context);
                } else {
                    sendEvent(req.xmlReq, req.context, "complete");
                    sendError(req.xmlReq, req.context, "httpError");
                }

                // Regardless of whether the request completed successfully (or not),
                // dequeue requests that have been completed (readyState 4) and send
                // requests that ready to be sent (readyState 0).

                let nextReq = req.que.getOldestElement();
                if (isNull(nextReq)) {
                    return;
                }
                while (isNotNull(nextReq.xmlReq) && nextReq.xmlReq.readyState === 4) {
                    req.que.dequeue();
                    nextReq = req.que.getOldestElement();
                    if (isNull(nextReq)) {
                        break;
                    }
                }
                if (isNull(nextReq)) {
                    return;
                }
                if (isNotNull(nextReq.xmlReq) && nextReq.xmlReq.readyState === 0) {
                    nextReq.fromQueue = true;
                    nextReq.sendRequest();
                }
            };

            /**
             * Utility method that accepts additional arguments for the AjaxEngine.
             * If an argument is passed in that matches an AjaxEngine property, the
             * argument value becomes the value of the AjaxEngine property.
             * Arguments that don't match AjaxEngine properties are added as
             * request parameters.
             * @ignore
             */
            req.setupArguments = function(args) {
                for (const i of Object.keys(args) ) {
                    if (typeof req[i] === UDEF) {
                        req.parameters[i] = args[i];
                    } else {
                        req[i] = args[i];
                    }
                }
            };

            /**
             * This function does final encoding of parameters, determines the request method
             * (GET or POST) and sends the request using the specified url.
             * @ignore
             */
            req.sendRequest = function () {
                if (isNotNull(req.xmlReq)) {
                    // if there is already a request on the queue waiting to be processed..
                    // just queue this request
                    // TODO: add support for async ajax requests
                    // https://github.com/eclipse-ee4j/mojarra/issues/4946
                    if (!req.que.isEmpty()) {
                        if (!req.fromQueue) {
                            req.que.enqueue(req);
                            return;
                        }
                    }
                    // If the queue is empty, queue up this request and send
                    if (!req.fromQueue) {
                        req.que.enqueue(req);
                    }
                    // Some logic to get the real request URL
                    if (req.generateUniqueUrl && req.method === "GET") {
                        req.parameters["AjaxRequestUniqueId"] = new Date().getTime() + EMPTY + req.requestIndex;
                    }

                    // is a multipart form data ?
                    const isMultiPart = (req.method === "POST" && context.form.enctype === 'multipart/form-data');

                    // If multipart prepare the FormData
                    const formData = isMultiPart ? new FormData(context.form) : undefined;

                    // Add parameters encoded or multipart
                    for ( const i of Object.keys(req.parameters) ) {
                        // if is multipart request -> add parameter to FormData
                        if ( isMultiPart ) {
                            formData.append(i,req.parameters[i]);
                        }
                        // else is a normal post request -> add encoded request query string to queryString for POST
                        else {
                            if (req.queryString.length > 0) req.queryString += "&";
                            req.queryString += encodeURIComponent(i) + "=" + encodeURIComponent(req.parameters[i]);
                        }
                    }

                    // GET Request
                    if (req.method === "GET") {
                        if (req.queryString.length > 0) {
                            req.url += ((req.url.indexOf("?") > -1) ? "&" : "?") + req.queryString;
                        }
                    }

                    // Open Ajax request
                    req.xmlReq.open(req.method, req.url, req.async);

                    // note that we are including the charset=UTF-8 as part of the content type (even
                    // if encodeURIComponent encodes as UTF-8), because with some
                    // browsers it will not be set in the request.  Some server implementations need to
                    // determine the character encoding from the request header content type.
                    if (req.method === "POST") {
                        req.xmlReq.setRequestHeader('Faces-Request', 'partial/ajax');

                        // file upload
                        if ( isMultiPart ) formData.append('Faces-Request','partial/ajax');

                        // GET or POST
                        // req.xmlReq.setRequestHeader('Content-type', 'application/x-www-form-urlencoded;charset=UTF-8');
                        else req.xmlReq.setRequestHeader( 'Content-type' , context.form.enctype+';charset=UTF-8' );
                    }

                    // note that async == false is not a supported feature.  We may change it in ways
                    // that break existing programs at any time, with no warning.
                    if (!req.async) req.xmlReq.onreadystatechange = null; // no need for readystate change listening

                    // Send begin event
                    sendEvent(req.xmlReq, req.context, "begin");

                    // IF multipart/form-data use FormData
                    if (isMultiPart) req.xmlReq.send(formData);

                    // ELSE use query string
                    else req.xmlReq.send(req.queryString);

                    // call OnComplete if not async
                    if(!req.async) req.onComplete();

                }
            };

            return req;
        };

        /**
         * Error handling callback.
         * Assumes that the request has completed.
         * @ignore
         */
        const sendError = function sendError(request, context, status, description, serverErrorName, serverErrorMessage) {

            // Possible error names:
            // httpError
            // emptyResponse
            // serverError
            // malformedXML

            let sent = false;
            const data = {};  // data payload for function
            data.type = "error";
            data.status = status;
            data.source = context.sourceid;
            data.responseCode = request.status;
            data.responseXML = request.responseXML;
            data.responseText = request.responseText;

            // ensure data source is the dom element and not the ID
            // per 14.4.1 of the 2.0 specification.
            if (typeof data.source === 'string') {
                data.source = document.getElementById(data.source);
            }

            if (description) {
                data.description = description;
            } else if (status === "httpError") {
                if (data.responseCode === 0) {
                    data.description = "The Http Transport returned a 0 status code.  This is usually the result of mixing ajax and full requests.  This is usually undesired, for both performance and data integrity reasons.";
                } else {
                    data.description = "There was an error communicating with the server, status: " + data.responseCode;
                }
            } else if (status === "serverError") {
                data.description = serverErrorMessage;
            } else if (status === "emptyResponse") {
                data.description = "An empty response was received from the server.  Check server error logs.";
            } else if (status === "malformedXML") {
                const parsedErrorText = getParseErrorText(data.responseXML);
                if ( parsedErrorText !== PARSED_OK) {
                    data.description = parsedErrorText;
                } else {
                    data.description = "An invalid XML response was received from the server.";
                }
            }

            if (status === "serverError") {
                data.errorName = serverErrorName;
                data.errorMessage = serverErrorMessage;
            }

            // If we have a registered callback, send the error to it.
            // TODO: do we need to call the function in the global context?
            if (context.onerror) {
                context.onerror.call(null, data);
                sent = true;
            }

            // TODO: do we need to call these functions in the global context?
            for (const listener of errorListeners) {
                listener.call(null, data);
                sent = true;
            }

            if (!sent && faces.getProjectStage() === "Development") {
                if (status === "serverError") {
                    alert("serverError: " + serverErrorName + SPACE + serverErrorMessage);
                } else {
                    alert(status + ": " + data.description);
                }
            }
        };

        /**
         * Event handling callback.
         * Request is assumed to have completed, except in the case of event = 'begin'.
         * @ignore
         */
        const sendEvent = function sendEvent(request, context, status) {

            const data = {};
            data.type = "event";
            data.status = status;
            data.source = context.sourceid;
            // ensure data source is the dom element and not the ID
            // per 14.4.1 of the 2.0 specification.
            if (typeof data.source === 'string') {
                data.source = document.getElementById(data.source);
            }
            if (status !== 'begin') {
                data.responseCode = request.status;
                data.responseXML = request.responseXML;
                data.responseText = request.responseText;
            }

            // TODO: do we need to call this functions in the global context?
            if (context.onevent) {
                context.onevent.call(null, data);
            }

            // TODO: do we need to call these functions in the global context?
            for (const listener of eventListeners) {
                listener.call(null, data);
            }
        };

        const unescapeHTML = function unescapeHTML(escapedHTML) {
            return escapedHTML
                .replace(/&apos;/g, "'")
                .replace(/&quot;/g, '"')
                .replace(/&gt;/g, '>')
                .replace(/&lt;/g, '<')
                .replace(/&amp;/g, '&');
        };

        // Use module pattern to return the functions we actually expose
        return {
            /**
             * Register a callback for error handling.
             * <p><b>Usage:</b></p>
             * <pre><code>
             * faces.ajax.addOnError(handleError);
             * ...
             * var handleError = function handleError(data) {
             * ...
             * }
             * </pre></code>
             * <p><b>Implementation Requirements:</b></p>
             * This function must accept a reference to an existing JavaScript function.
             * The JavaScript function reference must be added to a list of callbacks, making it possible
             * to register more than one callback by invoking <code>faces.ajax.addOnError</code>
             * more than once.  This function must throw an error if the <code>callback</code>
             * argument is not a function.
             *
             * @member faces.ajax
             * @function faces.ajax.addOnError
             * @param callback a reference to a function to call on an error
             */
            addOnError: function addOnError(callback) {
                if (typeof callback === 'function') {
                    errorListeners.push(callback);
                } else {
                    throw new Error("faces.ajax.addOnError:  Added a callback that was not a function.");
                }
            },
            /**
             * Register a callback for event handling.
             * <p><b>Usage:</b></p>
             * <pre><code>
             * faces.ajax.addOnEvent(statusUpdate);
             * ...
             * var statusUpdate = function statusUpdate(data) {
             * ...
             * }
             * </pre></code>
             * <p><b>Implementation Requirements:</b></p>
             * This function must accept a reference to an existing JavaScript function.
             * The JavaScript function reference must be added to a list of callbacks, making it possible
             * to register more than one callback by invoking <code>faces.ajax.addOnEvent</code>
             * more than once.  This function must throw an error if the <code>callback</code>
             * argument is not a function.
             *
             * @member faces.ajax
             * @function faces.ajax.addOnEvent
             * @param callback a reference to a function to call on an event
             */
            addOnEvent: function addOnEvent(callback) {
                if (typeof callback === 'function') {
                    eventListeners.push(callback);
                } else {
                    throw new Error("faces.ajax.addOnEvent: Added a callback that was not a function");
                }
            },
            /**

             * <p><span class="changed_modified_2_2">Send</span> an
             * asynchronous Ajax req uest to the server.

             * <p><b>Usage:</b></p>
             * <pre><code>
             * Example showing all optional arguments:
             *
             * &lt;commandButton id="button1" value="submit"
             *     onclick="faces.ajax.request(this,event,
             *       {execute:'button1',render:'status',onevent: handleEvent,onerror: handleError});return false;"/&gt;
             * &lt;/commandButton/&gt;
             * </pre></code>
             * <p><b>Implementation Requirements:</b></p>
             * This function must:
             * <ul>
             * <li>Be used within the context of a <code>form</code><span class="changed_added_2_3">,
             * else throw an error</span>.</li>
             * <li>Capture the element that triggered this Ajax request
             * (from the <code>source</code> argument, also known as the
             * <code>source</code> element.</li>
             * <li>If the <code>source</code> element is <code>null</code> or
             * <code>undefined</code> throw an error.</li>
             * <li>If the <code>source</code> argument is not a <code>string</code> or
             * DOM element object, throw an error.</li>
             * <li>If the <code>source</code> argument is a <code>string</code>, find the
             * DOM element for that <code>string</code> identifier.
             * <li>If the DOM element could not be determined, throw an error.</li>
             * <li class="changed_added_2_3">If the <code>jakarta.faces.ViewState</code>
             * element could not be found, throw an error.</li>
             * <li class="changed_added_2_3">If the ID of the <code>jakarta.faces.ViewState</code>
             * element has a <code>&lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt;&lt;SEP&gt;</code>
             * prefix, where &lt;SEP&gt; is the currently configured
             * <code>UINamingContainer.getSeparatorChar()</code> and
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; is the return from
             * <code>UIViewRoot.getContainerClientId()</code> on the
             * view from whence this state originated, then remember it as <i>namespace prefix</i>.
             * This is needed during encoding of the set of post data arguments.</li>
             * <li>If the <code>onerror</code> and <code>onevent</code> arguments are set,
             * they must be functions, or throw an error.
             * <li>Determine the <code>source</code> element's <code>form</code>
             * element.</li>
             * <li>Get the <code>form</code> view state by calling
             * {@link faces.getViewState} passing the
             * <code>form</code> element as the argument.</li>
             * <li>Collect post data arguments for the Ajax request.
             * <ul>
             * <li>The following name/value pairs are required post data arguments:
             * <table border="1">
             * <tr>
             * <th>name</th>
             * <th>value</th>
             * </tr>
             * <tr>
             * <td><code>jakarta.faces.ViewState</code></td>
             * <td><code>Contents of jakarta.faces.ViewState hidden field.  This is included when
             * {@link faces.getViewState} is used.</code></td>
             * </tr>
             * <tr>
             * <td><code>jakarta.faces.partial.ajax</code></td>
             * <td><code>true</code></td>
             * </tr>
             * <tr>
             * <td><code>jakarta.faces.source</code></td>
             * <td><code>The identifier of the element that triggered this request.</code></td>
             * </tr>
             * <tr class="changed_added_2_2">
             * <td><code>jakarta.faces.ClientWindow</code></td>

             * <td><code>Call faces.getClientWindow(), passing the current
             * form.  If the return is non-null, it must be set as the
             * value of this name/value pair, otherwise, a name/value
             * pair for client window must not be sent.</code></td>

             * </tr>
             * </table>
             * </li>
             * </ul>
             * </li>
             * <li>Collect optional post data arguments for the Ajax request.
             * <ul>
             * <li>Determine additional arguments (if any) from the <code>options</code>
             * argument. If <code>options.execute</code> exists:
             * <ul>
             * <li>If the keyword <code>@none</code> is present, do not create and send
             * the post data argument <code>jakarta.faces.partial.execute</code>.</li>
             * <li>If the keyword <code>@all</code> is present, create the post data argument with
             * the name <code>jakarta.faces.partial.execute</code> and the value <code>@all</code>.</li>
             * <li>Otherwise, there are specific identifiers that need to be sent.  Create the post
             * data argument with the name <code>jakarta.faces.partial.execute</code> and the value as a
             * space delimited <code>string</code> of client identifiers.</li>
             * </ul>
             * </li>
             * <li>If <code>options.execute</code> does not exist, create the post data argument with the
             * name <code>jakarta.faces.partial.execute</code> and the value as the identifier of the
             * element that caused this request.</li>
             * <li>If <code>options.render</code> exists:
             * <ul>
             * <li>If the keyword <code>@none</code> is present, do not create and send
             * the post data argument <code>jakarta.faces.partial.render</code>.</li>
             * <li>If the keyword <code>@all</code> is present, create the post data argument with
             * the name <code>jakarta.faces.partial.render</code> and the value <code>@all</code>.</li>
             * <li>Otherwise, there are specific identifiers that need to be sent.  Create the post
             * data argument with the name <code>jakarta.faces.partial.render</code> and the value as a
             * space delimited <code>string</code> of client identifiers.</li>
             * </ul>
             * <li>If <code>options.render</code> does not exist do not create and send the
             * post data argument <code>jakarta.faces.partial.render</code>.</li>

             * <li class="changed_added_2_2">If
             * <code>options.delay</code> exists let it be the value
             * <em>delay</em>, for this discussion.  If
             * <code>options.delay</code> does not exist, or is the
             * literal string <code>'none'</code>, without the quotes,
             * no delay is used.  If less than <em>delay</em>
             * milliseconds elapses between calls to <em>request()</em>
             * only the most recent one is sent and all other requests
             * are discarded.</li>


             * <li class="changed_added_2_2">If
             * <code>options.resetValues</code> exists and its value is
             * <code>true</code>, ensure a post data argument with the
             * name <code>jakarta.faces.partial.resetValues</code> and the
             * value <code>true</code> is sent in addition to the other
             * post data arguments.  This will cause
             * <code>UIViewRoot.resetValues()</code> to be called,
             * passing the value of the "render" attribute.  Note: do
             * not use any of the <code>@</code> keywords such as
             * <code>@form</code> or <code>@this</code> with this option
             * because <code>UIViewRoot.resetValues()</code> does not
             * descend into the children of the listed components.</li>


             * <li>Determine additional arguments (if any) from the <code>event</code>
             * argument.  The following name/value pairs may be used from the
             * <code>event</code> object:
             * <ul>
             * <li><code>target</code> - the ID of the element that triggered the event.</li>
             * <li><code>captured</code> - the ID of the element that captured the event.</li>
             * <li><code>type</code> - the type of event (ex: onkeypress)</li>
             * <li><code>alt</code> - <code>true</code> if ALT key was pressed.</li>
             * <li><code>ctrl</code> - <code>true</code> if CTRL key was pressed.</li>
             * <li><code>shift</code> - <code>true</code> if SHIFT key was pressed. </li>
             * <li><code>meta</code> - <code>true</code> if META key was pressed. </li>
             * <li><code>right</code> - <code>true</code> if right mouse button
             * was pressed. </li>
             * <li><code>left</code> - <code>true</code> if left mouse button
             * was pressed. </li>
             * <li><code>keycode</code> - the key code.
             * </ul>
             * </li>
             * </ul>
             * </li>
             * <li>Encode the set of post data arguments. <span class="changed_added_2_3">
             * If the <code>jakarta.faces.ViewState</code> element has a namespace prefix, then
             * make sure that all post data arguments are prefixed with this namespace prefix.
             * </span></li>
             * <li>Join the encoded view state with the encoded set of post data arguments
             * to form the <code>query string</code> that will be sent to the server.</li>
             * <li>Create a request <code>context</code> object and set the properties:
             * <ul><li><code>source</code> (the source DOM element for this request)</li>
             * <li><code>onerror</code> (the error handler for this request)</li>
             * <li><code>onevent</code> (the event handler for this request)</li></ul>
             * The request context will be used during error/event handling.</li>
             * <li>Send a <code>begin</code> event following the procedure as outlined
             * in the Jakarta Faces Specification Document section 13.3.5.3 "Sending Events".</li>
             * <li>Set the request header with the name: <code>Faces-Request</code> and the
             * value: <code>partial/ajax</code>.</li>
             * <li>Determine the <code>posting URL</code> as follows: If the hidden field
             * <code>jakarta.faces.encodedURL</code> is present in the submitting form, use its
             * value as the <code>posting URL</code>.  Otherwise, use the <code>action</code>
             * property of the <code>form</code> element as the <code>URL</code>.</li>

             * <li>

             * <p><span class="changed_modified_2_2">Determine whether
             * or not the submitting form is using
             * <code>multipart/form-data</code> as its
             * <code>enctype</code> attribute.  If not, send the request
             * as an <code>asynchronous POST</code> using the
             * <code>posting URL</code> that was determined in the
             * previous step.</span> <span
             * class="changed_added_2_2">Otherwise, send the request
             * using a multi-part capable transport layer, such as a
             * hidden inline frame.  Note that using a hidden inline
             * frame does <strong>not</strong> use
             * <code>XMLHttpRequest</code>, but the request must be sent
             * with all the parameters that a Faces
             * <code>XMLHttpRequest</code> would have been sent with.
             * In this way, the server side processing of the request
             * will be identical whether or the request is multipart or
             * not.</span></p>

             * <div class="changed_added_2_2">

             * <p>The <code>begin</code>, <code>complete</code>, and
             * <code>success</code> events must be emulated when using
             * the multipart transport.  This allows any listeners to
             * behave uniformly regardless of the multipart or
             * <code>XMLHttpRequest</code> nature of the transport.</p>

             * </div></li>
             * </ul>
             * Form serialization should occur just before the request is sent to minimize
             * the amount of time between the creation of the serialized form data and the
             * sending of the serialized form data (in the case of long requests in the queue).
             * Before the request is sent it must be put into a queue to ensure requests
             * are sent in the same order as when they were initiated.  The request callback function
             * must examine the queue and determine the next request to be sent.  The behavior of the
             * request callback function must be as follows:
             * <ul>
             * <li>If the request completed successfully invoke {@link faces.ajax.response}
             * passing the <code>request</code> object.</li>
             * <li>If the request did not complete successfully, notify the client.</li>
             * <li>Regardless of the outcome of the request (success or error) every request in the
             * queue must be handled.  Examine the status of each request in the queue starting from
             * the request that has been in the queue the longest.  If the status of the request is
             * <code>complete</code> (readyState 4), dequeue the request (remove it from the queue).
             * If the request has not been sent (readyState 0), send the request.  Requests that are
             * taken off the queue and sent should not be put back on the queue.</li>
             * </ul>
             *
             * </p>
             *
             * @param source The DOM element that triggered this Ajax request, or an id string of the
             * element to use as the triggering element.
             * @param event The DOM event that triggered this Ajax request.  The
             * <code>event</code> argument is optional.
             * @param options The set of available options that can be sent as
             * request parameters to control client and/or server side
             * request processing. Acceptable name/value pair options are:
             * <table border="1">
             * <tr>
             * <th>name</th>
             * <th>value</th>
             * </tr>
             * <tr>
             * <td><code>execute</code></td>
             * <td><code>space seperated list of client identifiers</code></td>
             * </tr>
             * <tr>
             * <td><code>render</code></td>
             * <td><code>space seperated list of client identifiers</code></td>
             * </tr>
             * <tr>
             * <td><code>onevent</code></td>
             * <td><code>function to callback for event</code></td>
             * </tr>
             * <tr>
             * <td><code>onerror</code></td>
             * <td><code>function to callback for error</code></td>
             * </tr>
             * <tr>
             * <td><code>params</code></td>
             * <td><code>object containing parameters to include in the request</code></td>
             * </tr>

             * <tr class="changed_added_2_2">

             * <td><code>delay</code></td>

             * <td>If less than <em>delay</em> milliseconds elapses
             * between calls to <em>request()</em> only the most recent
             * one is sent and all other requests are discarded. If the
             * value of <em>delay</em> is the literal string
             * <code>'none'</code> without the quotes, or no delay is
             * specified, no delay is used. </td>

             * </tr>

             * <tr class="changed_added_2_2">

             * <td><code>resetValues</code></td>

             * <td>If true, ensure a post data argument with the name
             * jakarta.faces.partial.resetValues and the value true is
             * sent in addition to the other post data arguments. This
             * will cause UIViewRoot.resetValues() to be called, passing
             * the value of the "render" attribute. Note: do not use any
             * of the @ keywords such as @form or @this with this option
             * because UIViewRoot.resetValues() does not descend into
             * the children of the listed components.</td>

             * </tr>


             * </table>
             * The <code>options</code> argument is optional.
             * @member faces.ajax
             * @function faces.ajax.request

             * @throws Error if first required argument
             * <code>element</code> is not specified, or if one or more
             * of the components in the <code>options.execute</code>
             * list is a file upload component, but the form's enctype
             * is not set to <code>multipart/form-data</code>
             */

            request: function request(source, event, options) {

                const context = {};

                if (isNull(source)) {
                    throw new Error("faces.ajax.request: source not set");
                }
                if(delayHandler) {
                    clearTimeout(delayHandler);
                    delayHandler = null;
                }

                // set up the element based on source
                let element;
                if (typeof source === 'string') {
                    element = document.getElementById(source);
                } else if (typeof source === 'object') {
                    element = source;
                } else {
                    throw new Error("faces.ajax.request: source must be object or string");
                }

                // attempt to handle case of name unset
                // this might be true in a badly written composite component
                if (!element.name) {
                    element.name = element.id;
                }

                context.element = element;

                if (isNull(options)) {
                    options = {};
                }

                // Error handler for this request
                let onerror = false;

                if (options.onerror && typeof options.onerror === 'function') {
                    onerror = options.onerror;
                } else if (options.onerror && typeof options.onerror !== 'function') {
                    throw new Error("faces.ajax.request: Added an onerror callback that was not a function");
                }

                // Event handler for this request
                let onevent = false;

                if (options.onevent && typeof options.onevent === 'function') {
                    onevent = options.onevent;
                } else if (options.onevent && typeof options.onevent !== 'function') {
                    throw new Error("faces.ajax.request: Added an onevent callback that was not a function");
                }

                const form = getForm(element);
                if (!form) {
                    throw new Error("faces.ajax.request: Method must be called within a form");
                }

                const viewStateElement = getHiddenStateField(form, VIEW_STATE_PARAM);
                if (!viewStateElement) {
                    throw new Error("faces.ajax.request: Form has no view state element");
                }

                context.form = form;
                context.formId = form.id;

                // Set up additional arguments to be used in the request..
                // Make sure "jakarta.faces.source" is set up.
                // If there were "execute" ids specified, make sure we
                // include the identifier of the source element in the
                // "execute" list.  If there were no "execute" ids
                // specified, determine the default.

                const args = {};

                const namingContainerPrefix = viewStateElement.name.substring(0, viewStateElement.name.indexOf(VIEW_STATE_PARAM));

                args[namingContainerPrefix + "jakarta.faces.source"] = element.id;

                if (event && !!event.type) {
                    args[namingContainerPrefix + "jakarta.faces.partial.event"] = event.type;
                }

                if ("resetValues" in options) {
                    args[namingContainerPrefix + "jakarta.faces.partial.resetValues"] = options.resetValues;
                }

                // do a partial submit only if it is enabled and:
                // 1) option.execute is not defined, eg. <f:ajax />
                // 2) if it is defined it should not contain @form or @all
                const doPartialSubmit = PARTIAL_SUBMIT_ENABLED && ( !options.execute || ( !contains(options.execute,"@form") && !contains(options.execute,"@all") ) );

                // If we have 'execute' identifiers:
                // Handle any keywords that may be present.
                // If @none present anywhere, do not send the
                // "jakarta.faces.partial.execute" parameter.
                // The 'execute' and 'render' lists must be space
                // delimited.

                if (options.execute) {
                    const isNone = contains(options.execute,"@none");
                    if ( !isNone ) {
                        const isAll = contains(options.execute,"@all");
                        if ( !isAll ) {
                            options.execute = options.execute.replace("@this", element.id);
                            options.execute = options.execute.replace("@form", form.id);
                            const temp = options.execute.split(SPACE);
                            if ( ! temp.includes(element.name) ) {
                                options.execute = element.name + SPACE + options.execute;
                            }
                            if (namingContainerPrefix) {
                                options.execute = namespaceParametersIfNecessary(options.execute, element.name, namingContainerPrefix);
                            }
                        } else {
                            options.execute = "@all";
                        }
                        args[namingContainerPrefix + "jakarta.faces.partial.execute"] = options.execute;
                    }
                }
                // in case of <f:ajax />
                else {
                    // if id is equals to name then add only one of them to avoid duplicates inside options.execute
                    options.execute = (element.name === element.id) ? element.id : element.name+SPACE+element.id;
                    args[namingContainerPrefix + "jakarta.faces.partial.execute"] = options.execute;
                }

                if (options.render) {
                    const isNone = contains(options.render,"@none");
                    if ( !isNone ) {
                        const isAll = contains(options.render,"@all");
                        if ( !isAll ) {
                            options.render = options.render.replace("@this", element.id);
                            options.render = options.render.replace("@form", form.id);
                            if (namingContainerPrefix) {
                                options.render = namespaceParametersIfNecessary(options.render, element.name, namingContainerPrefix);
                            }
                        } else {
                            options.render = "@all";
                        }
                        args[namingContainerPrefix + "jakarta.faces.partial.render"] = options.render;
                    }
                }

                // delay value for request execution
                const explicitlyDoNotDelay =    ((typeof options.delay == 'undefined') || (typeof options.delay == 'string') &&
                                                (options.delay.toLowerCase() === 'none'));
                let delayValue;
                if (typeof options.delay == 'number') {
                    delayValue = options.delay;
                } else  {
                    const converted = parseInt(options.delay);

                    if (!explicitlyDoNotDelay && isNaN(converted)) {
                        throw new Error('invalid value for delay option: ' + options.delay);
                    }
                    delayValue = converted;
                }

                // check the "execute" ids to see if any include an input of type "file"
                context.includesInputFile = false;
                let ids = options.execute.split(SPACE);

                // if @all -> execute only this form
                if ( ids.includes("@all") ) ids = [ form.id ];

                if (ids) {
                    for ( const id of ids ) {
                        const elem = document.getElementById(id);
                        if (elem) {
                            if (elem.nodeType === Node.ELEMENT_NODE) {
                                if ( elem.hasAttribute("type") ) {
                                    if (elem.getAttribute("type") === "file") {
                                        context.includesInputFile = true;
                                        break;
                                    }
                                } else {
                                    if (hasInputFileControl(elem)) {
                                        context.includesInputFile = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                // encoded query string to process, eventually with partial submit logic enabled
                const viewState = doPartialSubmit ? faces.getPartialViewState( form , options.execute ) : faces.getViewState(form);

                // copy all params to args
                const params = options.params || {};
                for (const property of Object.keys(params) ) {
                    args[namingContainerPrefix + property] = params[property];
                }

                // remove non-passthrough options
                delete options.execute;
                delete options.render;
                delete options.onerror;
                delete options.onevent;
                delete options.delay;
                delete options.resetValues;
                delete options.params;

                // copy all other options to args (for backwards compatibility on issue 4115)
                for (const property of Object.keys(options) ) {
                    args[namingContainerPrefix + property] = options[property];
                }

                args[namingContainerPrefix + "jakarta.faces.partial.ajax"] = "true";
                args["method"] = "POST";

                // Determine the posting url
                const encodedUrlField = getEncodedUrlElement(form);
                if ( isNull(encodedUrlField) ) {
                    args["url"] = form.action;
                } else {
                    args["url"] = encodedUrlField.value;
                }

                const sendRequest = function() {
                    const ajaxEngine = new AjaxEngine(context);
                    ajaxEngine.setupArguments(args);
                    ajaxEngine.queryString = viewState;
                    ajaxEngine.context.onevent = onevent;
                    ajaxEngine.context.onerror = onerror;
                    ajaxEngine.context.sourceid = element.id;
                    ajaxEngine.context.render = args[namingContainerPrefix + "jakarta.faces.partial.render"] || EMPTY;
                    ajaxEngine.context.namingContainerPrefix = namingContainerPrefix;
                    ajaxEngine.sendRequest();
                };

                if (explicitlyDoNotDelay) {
                    sendRequest();
                } else {
                    delayHandler = setTimeout(sendRequest, delayValue);
                }

            },
            /**
             * <p><span class="changed_modified_2_2">Receive</span> an Ajax response
             * from the server.
             * <p><b>Usage:</b></p>
             * <pre><code>
             * faces.ajax.response(request, context);
             * </pre></code>
             * <p><b>Implementation Requirements:</b></p>
             * This function must evaluate the markup returned in the
             * <code>request.responseXML</code> object and perform the following action:
             * <ul>
             * <p>If there is no XML response returned, signal an <code>emptyResponse</code>
             * error. If the XML response does not follow the format as outlined
             * in Appendix A.3 "XML Schema Definition For Partial Response" of the Jakarta Faces Specification Document
             * signal a <code>malformedError</code> error.  Refer to
             * Jakarta Faces Specification Document section 13.3.6.3 "Signaling Errors".</p>
             * <p>If the response was successfully processed, send a <code>success</code>
             * event as outlined in Jakarta Faces Specification Document section 13.3.5.3 "Sending Events".</p>
             * <p><i>Update Element Processing</i></p>
             * The <code>update</code> element is used to update a single DOM element.  The
             * "id" attribute of the <code>update</code> element refers to the DOM element that
             * will be updated.  The contents of the <code>CDATA</code> section is the data that
             * will be used when updating the contents of the DOM element as specified by the
             * <code>&lt;update&gt;</code> element identifier.
             * <li>If an <code>&lt;update&gt;</code> element is found in the response
             * with the identifier <code>jakarta.faces.ViewRoot</code>:
             * <pre><code>&lt;update id="jakarta.faces.ViewRoot"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * Update the entire DOM replacing the appropriate <code>head</code> and/or
             * <code>body</code> sections with the content from the response.</li>

             * <li class="changed_modified_2_2">If an
             * <code>&lt;update&gt;</code> element is found in the
             * response with an identifier containing
             * <code>jakarta.faces.ViewState</code>:

             * <pre><code>&lt;update id="&lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt;&lt;SEP&gt;jakarta.faces.ViewState&lt;SEP&gt;&lt;UNIQUE_PER_VIEW_NUMBER&gt;"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>

             * locate and update the submitting form's
             * <code>jakarta.faces.ViewState</code> value with the
             * <code>CDATA</code> contents from the response.
             * &lt;SEP&gt; is the currently configured
             * <code>UINamingContainer.getSeparatorChar()</code>.
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; is the return from
             * <code>UIViewRoot.getContainerClientId()</code> on the
             * view from whence this state originated.
             * &lt;UNIQUE_PER_VIEW_NUMBER&gt; is a number that must be
             * unique within this view, but must not be included in the
             * view state.  This requirement is simply to satisfy XML
             * correctness in parity with what is done in the
             * corresponding non-partial Faces view.  Locate and update
             * the <code>jakarta.faces.ViewState</code> value for all
             * Faces forms covered in the <code>render</code> target
             * list whose ID starts with the same
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; value.</li>

             * <li class="changed_added_2_2">If an
             * <code>update</code> element is found in the response with
             * an identifier containing
             * <code>jakarta.faces.ClientWindow</code>:

             * <pre><code>&lt;update id="&lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt;&lt;SEP&gt;jakarta.faces.ClientWindow&lt;SEP&gt;&lt;UNIQUE_PER_VIEW_NUMBER&gt;"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>

             * locate and update the submitting form's
             * <code>jakarta.faces.ClientWindow</code> value with the
             * <code>CDATA</code> contents from the response.
             * &lt;SEP&gt; is the currently configured
             * <code>UINamingContainer.getSeparatorChar()</code>.
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; is the return from
             * <code>UIViewRoot.getContainerClientId()</code> on the
             * view from whence this state originated.
             * &lt;UNIQUE_PER_VIEW_NUMBER&gt; is a number that must be
             * unique within this view, but must not be included in the
             * view state.  This requirement is simply to satisfy XML
             * correctness in parity with what is done in the
             * corresponding non-partial Faces view.  Locate and update
             * the <code>jakarta.faces.ClientWindow</code> value for all
             * Faces forms covered in the <code>render</code> target
             * list whose ID starts with the same
             * &lt;VIEW_ROOT_CONTAINER_CLIENT_ID&gt; value.</li>

             * <li class="changed_added_2_3">If an <code>update</code> element is found in the response with the
             * identifier <code>jakarta.faces.Resource</code>:
             * <pre><code>&lt;update id="jakarta.faces.Resource"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * append any element found in the <code>CDATA</code> contents which is absent in the document to the
             * document's <code>head</code> section.
             * </li>

             * <li>If an <code>update</code> element is found in the response with the identifier
             * <code>jakarta.faces.ViewHead</code>:
             * <pre><code>&lt;update id="jakarta.faces.ViewHead"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * update the document's <code>head</code> section with the <code>CDATA</code>
             * contents from the response.</li>
             * <li>If an <code>update</code> element is found in the response with the identifier
             * <code>jakarta.faces.ViewBody</code>:
             * <pre><code>&lt;update id="jakarta.faces.ViewBody"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * update the document's <code>body</code> section with the <code>CDATA</code>
             * contents from the response.</li>
             * <li>For any other <code>&lt;update&gt;</code> element:
             * <pre><code>&lt;update id="update id"&gt;
             *    &lt;![CDATA[...]]&gt;
             * &lt;/update&gt;</code></pre>
             * Find the DOM element with the identifier that matches the
             * <code>&lt;update&gt;</code> element identifier, and replace its contents with
             * the <code>&lt;update&gt;</code> element's <code>CDATA</code> contents.</li>
             * </li>
             * <p><i>Insert Element Processing</i></p>

             * <li>If an <code>&lt;insert&gt;</code> element is found in
             * the response with a nested <code>&lt;before&gt;</code>
             * element:

             * <pre><code>&lt;insert&gt;
             *     &lt;before id="before id"&gt;
             *        &lt;![CDATA[...]]&gt;
             *     &lt;/before&gt;
             * &lt;/insert&gt;</code></pre>
             *
             * <ul>
             * <li>Extract this <code>&lt;before&gt;</code> element's <code>CDATA</code> contents
             * from the response.</li>
             * <li>Find the DOM element whose identifier matches <code>before id</code> and insert
             * the <code>&lt;before&gt;</code> element's <code>CDATA</code> content before
             * the DOM element in the document.</li>
             * </ul>
             * </li>
             *
             * <li>If an <code>&lt;insert&gt;</code> element is found in
             * the response with a nested <code>&lt;after&gt;</code>
             * element:
             *
             * <pre><code>&lt;insert&gt;
             *     &lt;after id="after id"&gt;
             *        &lt;![CDATA[...]]&gt;
             *     &lt;/after&gt;
             * &lt;/insert&gt;</code></pre>
             *
             * <ul>
             * <li>Extract this <code>&lt;after&gt;</code> element's <code>CDATA</code> contents
             * from the response.</li>
             * <li>Find the DOM element whose identifier matches <code>after id</code> and insert
             * the <code>&lt;after&gt;</code> element's <code>CDATA</code> content after
             * the DOM element in the document.</li>
             * </ul>
             * </li>
             * <p><i>Delete Element Processing</i></p>
             * <li>If a <code>&lt;delete&gt;</code> element is found in the response:
             * <pre><code>&lt;delete id="delete id"/&gt;</code></pre>
             * Find the DOM element whose identifier matches <code>delete id</code> and remove it
             * from the DOM.</li>
             * <p><i>Element Attribute Update Processing</i></p>
             * <li>If an <code>&lt;attributes&gt;</code> element is found in the response:
             * <pre><code>&lt;attributes id="id of element with attribute"&gt;
             *    &lt;attribute name="attribute name" value="attribute value"&gt;
             *    ...
             * &lt/attributes&gt;</code></pre>
             * <ul>
             * <li>Find the DOM element that matches the <code>&lt;attributes&gt;</code> identifier.</li>
             * <li>For each nested <code>&lt;attribute&gt;</code> element in <code>&lt;attribute&gt;</code>,
             * update the DOM element attribute value (whose name matches <code>attribute name</code>),
             * with <code>attribute value</code>.</li>
             * </ul>
             * </li>
             * <p><i>JavaScript Processing</i></p>
             * <li>If an <code>&lt;eval&gt;</code> element is found in the response:
             * <pre><code>&lt;eval&gt;
             *    &lt;![CDATA[...JavaScript...]]&gt;
             * &lt;/eval&gt;</code></pre>
             * <ul>
             * <li>Extract this <code>&lt;eval&gt;</code> element's <code>CDATA</code> contents
             * from the response and execute it as if it were JavaScript code.</li>
             * </ul>
             * </li>
             * <p><i>Redirect Processing</i></p>
             * <li>If a <code>&lt;redirect&gt;</code> element is found in the response:
             * <pre><code>&lt;redirect url="redirect url"/&gt;</code></pre>
             * Cause a redirect to the url <code>redirect url</code>.</li>
             * <p><i>Error Processing</i></p>
             * <li>If an <code>&lt;error&gt;</code> element is found in the response:
             * <pre><code>&lt;error&gt;
             *    &lt;error-name&gt;..fully qualified class name string...&lt;error-name&gt;
             *    &lt;error-message&gt;&lt;![CDATA[...]]&gt;&lt;error-message&gt;
             * &lt;/error&gt;</code></pre>
             * Extract this <code>&lt;error&gt;</code> element's <code>error-name</code> contents
             * and the <code>error-message</code> contents. Signal a <code>serverError</code> passing
             * the <code>errorName</code> and <code>errorMessage</code>.  Refer to
             * Jakarta Faces Specification Document section 13.3.6.3 "Signaling Errors".</li>
             * <p><i>Extensions</i></p>
             * <li>The <code>&lt;extensions&gt;</code> element provides a way for framework
             * implementations to provide their own information.</li>
             * <p><li>The implementation must check if &lt;script&gt; elements in the response can
             * be automatically run, as some browsers support this feature and some do not.
             * If they can not be run, then scripts should be extracted from the response and
             * run separately.</li></p>
             * </ul>
             *
             * </p>
             *
             * @param request The <code>XMLHttpRequest</code> instance that
             * contains the status code and response message from the server.
             *
             * @param context An object containing the request context, including the following properties:
             * the source element, per call onerror callback function, and per call onevent callback function.
             *
             * @throws  Error if request contains no data
             *
             * @function faces.ajax.response
             */
            response: function response(request, context) {

                if (!request) {
                    throw new Error("faces.ajax.response: Request parameter is unset");
                }

                // ensure context source is the dom element and not the ID
                // per 14.4.1 of the 2.0 specification.  We're doing it here
                // *before* any errors or events are propagated because the
                // DOM element may be removed after the update has been processed.
                if (typeof context.sourceid === 'string') {
                    context.sourceid = document.getElementById(context.sourceid);
                }

                const xml = request.responseXML;
                if (xml === null) {
                    sendError(request, context, "emptyResponse");
                    return;
                }

                if (getParseErrorText(xml) !== PARSED_OK) {
                    sendError(request, context, "malformedXML");
                    return;
                }

                const partialResponse = xml.getElementsByTagName("partial-response")[0];
                const namingContainerId = partialResponse.getAttribute("id");
                const namingContainerPrefix = namingContainerId ? (namingContainerId + faces.separatorchar) : EMPTY;
                let responseType = partialResponse.firstChild;

                context.namingContainerId = namingContainerId;
                context.namingContainerPrefix = namingContainerPrefix;

                for ( const partialResponseChild of partialResponse.childNodes ) {
                    if (partialResponseChild.nodeName === "error") {
                        responseType = partialResponseChild;
                        break;
                    }
                }

                if (responseType.nodeName === "error") { // it's an error
                    let errorName = EMPTY;
                    let errorMessage = EMPTY;

                    let element = responseType.firstChild;
                    if (element.nodeName === "error-name") {
                        if (null != element.firstChild) {
                            errorName = element.firstChild.nodeValue;
                        }
                    }

                    element = responseType.firstChild.nextSibling;
                    if (element.nodeName === "error-message") {
                        if (null != element.firstChild) {
                            errorMessage = element.firstChild.nodeValue;
                        }
                    }
                    sendError(request, context, "serverError", null, errorName, errorMessage);
                    sendEvent(request, context, "success");
                    return;
                }


                if (responseType.nodeName === "redirect") {
                    window.location = responseType.getAttribute("url");
                    return;
                }


                if (responseType.nodeName !== "changes") {
                    sendError(request, context, "malformedXML", "Top level node must be one of: changes, redirect, error, received: " + responseType.nodeName + " instead.");
                    return;
                }

                try {
                    for ( const change of responseType.childNodes ) {
                        switch (change.nodeName) {
                            case "update":
                                doUpdate(change, context);
                                break;
                            case "delete":
                                doDelete(change);
                                break;
                            case "insert":
                                doInsert(change);
                                break;
                            case "attributes":
                                doAttributes(change);
                                break;
                            case "eval":
                                doEval(change);
                                break;
                            case "extension":
                                // no action
                                break;
                            default:
                                sendError(request, context, "malformedXML", "Changes allowed are: update, delete, insert, attributes, eval, extension.  Received " + change.nodeName + " instead.");
                                return;
                        }
                    }
                } catch (ex) {
                    sendError(request, context, "malformedXML", ex.message);
                    return;
                }
                sendEvent(request, context, "success");

            }
        };
    }();

    /**
     *
     * <p>Return the value of <code>Application.getProjectStage()</code> for
     * the currently running application instance.  Calling this method must
     * not cause any network transaction to happen to the server.</p>
     * <p><b>Usage:</b></p>
     * <pre><code>
     * var stage = faces.getProjectStage();
     * if (stage === ProjectStage.Development) {
     *  ...
     * } else if stage === ProjectStage.Production) {
     *  ...
     * }
     * </code></pre>
     *
     * @returns String <code>String</code> representing the current state of the
     * running application in a typical product development lifecycle.  Refer
     * to <code>jakarta.faces.application.Application.getProjectStage</code> and
     * <code>jakarta.faces.application.ProjectStage</code>.
     * @function faces.getProjectStage
     */
    faces.getProjectStage = function() {
        // First, return cached value if available
        if (typeof mojarra !== 'undefined' && typeof mojarra.projectStageCache !== 'undefined') {
            return mojarra.projectStageCache;
        }
        // faces.js script
        const _script = document.querySelector("script[src*='jakarta.faces.resource/faces.js']");
        const scriptSrcSearchParam = isNotNull(_script) ? new URLSearchParams(_script.src) : null;

        const stage = ( isNotNull(scriptSrcSearchParam) && scriptSrcSearchParam.get('stage') === 'Development' ) ? 'Development' : 'Production';

        mojarra = mojarra || {};
        mojarra.projectStageCache = stage;

        return mojarra.projectStageCache;
    };

    /**
     * <p>Collect and encode state for input controls associated
     * with the specified <code>form</code> element.  This will include
     * all input controls of type <code>hidden</code>.</p>
     * <p><b>Usage:</b></p>
     * <pre><code>
     * var state = faces.getViewState(form);
     * </pre></code>
     *
     * @param form The <code>form</code> element whose contained
     * <code>input</code> controls will be collected and encoded.
     * Only successful controls will be collected and encoded in
     * accordance with: <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.2">
     * Section 17.13.2 of the HTML Specification</a>.
     *
     * @param execute The option.execute string built inside faces.ajax.request
     *
     * @returns String The encoded state for the specified form's input controls.
     */
    faces.getPartialViewState = function(form, execute) {
        if (!form) throw new Error("faces.getPartialViewState:  form must be set");

        // if execute is defined, create an array of id
        // that have to be included in the query string
        const partialExecuteIds = execute ? execute.split(SPACE).concat(ALWAYS_EXECUTE_IDS) : undefined;

        // array of element id => array of existing dom element
        const partialExecuteDomElements = partialExecuteIds.map(getElemById).filter( elem => !!elem );

        // the query string
        let qString = EMPTY;

        // if the partialExecuteIds does not include the form.id,
        // then add it because it's required by the spec to be always included!
        if ( partialExecuteIds && !partialExecuteIds.includes(form.id) ) {
            qString = appendToQueryString(qString,form.id,form.id);
        }

        // add encoded name=value string to query string parts array.
        // If partialExecuteIds is defined
        // then add the field only if there is a child element with his name
        // inside one of the element identified with the id contained in "partialExecuteIds" array (partial submit)
        const addField = function(name, value) {
            const add = !partialExecuteIds || partialExecuteIds.includes(name) || containsNamedChild(partialExecuteDomElements,name);
            if (add) qString = appendToQueryString(qString,name,value);
        };

        const els = form.elements;
        for (const el of els) {
            if (el.name === EMPTY) {
                continue;
            }
            if (!el.disabled) {
                switch (el.type) {
                    case 'submit':
                    case 'reset':
                    case 'image':
                    case 'file':
                        break;
                    case 'select-one':
                        if (el.selectedIndex >= 0) {
                            addField(el.name, el.options[el.selectedIndex].value);
                        }
                        break;
                    case 'select-multiple':
                        for ( const option of el.options) {
                            if (option.selected) {
                                addField(el.name, option.value);
                            }
                        }
                        break;
                    case 'checkbox':
                    case 'radio':
                        if (el.checked) {
                            addField(el.name, el.value || 'on');
                        }
                        break;
                    default:
                        // this is for any input incl.  text', 'password', 'hidden', 'textarea'
                        const nodeName = el.nodeName.toLowerCase();
                        if (nodeName === "input" || nodeName === "select" ||
                            nodeName === "button" || nodeName === "object" ||
                            nodeName === "textarea") {
                            addField(el.name, el.value);
                        }
                        break;
                }
            }
        }

        return qString;
    }



    /**
     * <p>Collect and encode state for input controls associated
     * with the specified <code>form</code> element.  This will include
     * all input controls of type <code>hidden</code>.</p>
     * <p><b>Usage:</b></p>
     * <pre><code>
     * var state = faces.getViewState(form);
     * </pre></code>
     *
     * @param form The <code>form</code> element whose contained
     * <code>input</code> controls will be collected and encoded.
     * Only successful controls will be collected and encoded in
     * accordance with: <a href="http://www.w3.org/TR/html401/interact/forms.html#h-17.13.2">
     * Section 17.13.2 of the HTML Specification</a>.
     *
     * @returns String The encoded state for the specified form's input controls.
     * @function faces.getViewState
     */
    faces.getViewState = function(form) {
        if (!form) throw new Error("faces.getViewState:  form must be set");

        // the query string
        let qString = EMPTY;

        // add encoded name=value string to query string parts array.
        // If partialExecuteIds is defined then add the field only if the name is inside the "partialExecuteIds" array (partial submit)
        const addField = function(name, value) {
            qString += ( (qString.length > 0 ? "&" : EMPTY) + encodeURIComponent(name) + "=" + encodeURIComponent(value) );
        };

        const els = form.elements;
        for (const el of els) {
            if (el.name === EMPTY) {
                continue;
            }
            if (!el.disabled) {
                switch (el.type) {
                    case 'submit':
                    case 'reset':
                    case 'image':
                    case 'file':
                        break;
                    case 'select-one':
                        if (el.selectedIndex >= 0) {
                            addField(el.name, el.options[el.selectedIndex].value);
                        }
                        break;
                    case 'select-multiple':
                        for ( const option of el.options) {
                            if (option.selected) {
                                addField(el.name, option.value);
                            }
                        }
                        break;
                    case 'checkbox':
                    case 'radio':
                        if (el.checked) {
                            addField(el.name, el.value || 'on');
                        }
                        break;
                    default:
                        // this is for any input incl.  text', 'password', 'hidden', 'textarea'
                        const nodeName = el.nodeName.toLowerCase();
                        if (nodeName === "input" || nodeName === "select" ||
                            nodeName === "button" || nodeName === "object" ||
                            nodeName === "textarea") {
                            addField(el.name, el.value);
                        }
                        break;
                }
            }
        }
        return qString;
    };

    /**
     * <p class="changed_added_2_2">Return the windowId of the window
     * in which the argument form is rendered.</p>
     *
     * @param {DomNode} node Determine the nature of
     * the argument.  If not present, search for the windowId within
     * <code>document.forms</code>.  If present and the value is a
     * string, assume the string is a DOM id and get the element with
     * that id and start the search from there.  If present and the
     * value is a DOM element, start the search from there.
     * @returns String The windowId of the current window, or null
     *  if the windowId cannot be determined.
     * @throws an error if more than one unique WindowId is found.
     * @function faces.getClientWindow
     */
    faces.getClientWindow = function(node) {

        /**
         * Find jakarta.faces.ClientWindow field for a given form.
         * @param form
         * @ignore
         */
        const getWindowIdElement = function(form) {
            return getFormInputElementByName(form,CLIENT_WINDOW_PARAM);
        };

        const fetchWindowIdFromForms = function(forms) {
            const result_idx = {};
            let result;
            let foundCnt = 0;

            for ( const form of forms ) {
                const windowIdElement = getWindowIdElement(form);
                const windowId = windowIdElement && windowIdElement.value;
                if (UDEF !== typeof windowId) {
                    if (foundCnt > 0 && UDEF === typeof result_idx[windowId]) throw Error("Multiple different windowIds found in document");
                    result = windowId;
                    result_idx[windowId] = true;
                    foundCnt++;
                }
            }

            return result;
        };

        /**
         * @ignore
         */
        const getChildForms = function getChildForms(currentElement) {
            //Special condition no element we return document forms
            //as search parameter, ideal would be to
            //have the viewroot here but the frameworks
            //can deal with that themselves by using
            //the viewroot as currentElement
            if (!currentElement) return document.forms;
            if (!currentElement.tagName) return [];
            if (currentElement.tagName.toLowerCase() === FORM) return [ currentElement ];
            return currentElement.querySelectorAll(FORM);
        };

        /**
         * @ignore
         */
        const fetchWindowIdFromURL = function fetchWindowIdFromURL() {
            const href = window.location.href;
            const windowId = "windowId";
            const regex = new RegExp("[\\?&]" + windowId + "=([^&#\\;]*)");
            const results = regex.exec(href);
            //initial trial over the url and a regexp
            return (results != null) ? results[1] : null;
        };

        //byId ($)
        const finalNode = (node && (typeof node == "string" || node instanceof String)) ?
            document.getElementById(node) : (node || null);

        const forms = getChildForms(finalNode);
        const result = fetchWindowIdFromForms(forms);
        return (null != result) ? result : fetchWindowIdFromURL();


    };

    /**
     * <p class="changed_added_2_3">
     * The Push functionality.
     * </p>
     * @name faces.push
     * @namespace
     * @exec
     */
    faces.push = (function(window) {

        // "Constant" fields ----------------------------------------------------------------------------------------------

        const URL_PROTOCOL = window.location.protocol.replace("http", "ws") + "//"; // todo: unused... https...?
        const RECONNECT_INTERVAL = 500;
        const MAX_RECONNECT_ATTEMPTS = 25;
        const REASON_EXPIRED = "Expired";
        const REASON_UNKNOWN_CHANNEL = "Unknown channel";

        // Private static fields ------------------------------------------------------------------------------------------

        const sockets = {};
        const self = {};

        // Private constructor functions ----------------------------------------------------------------------------------

        /**
         * Creates a reconnecting websocket. When the websocket successfully connects on first attempt, then it will
         * automatically reconnect on timeout with cumulative intervals of 500ms with a maximum of 25 attempts (~3 minutes).
         * The <code>onclose</code> function will be called with the error code of the last attempt.
         * @constructor
         * @param {string} url The URL of the websocket.
         * @param {string} channel The channel name of the websocket.
         * @param {function} onopen The function to be invoked when the websocket is opened.
         * @param {function} onmessage The function to be invoked when a message is received.
         * @param {function} onerror The function to be invoked when a connection error has occurred and the web socket will attempt to reconnect.
         * @param {function} onclose The function to be invoked when the web socket is closed and will not anymore attempt to reconnect.
         * @param {Object} behaviors Client behavior functions to be invoked when specific message is received.
         */
        function ReconnectingWebsocket(url, channel, onopen, onmessage, onerror, onclose, behaviors) {

            // Private fields -----------------------------------------------------------------------------------------

            let socket;
            let reconnectAttempts;
            const self = this;

            // Public functions ---------------------------------------------------------------------------------------

            /**
             * Opens the reconnecting websocket.
             */
            self.open = function() {
                if (socket && socket.readyState === 1) {
                    return;
                }

                socket = new WebSocket(url);

                socket.onopen = function(event) {
                    if (reconnectAttempts == null) {
                        onopen(channel);
                    }

                    reconnectAttempts = 0;
                }

                socket.onmessage = function(event) {
                    const message = JSON.parse(event.data).data;
                    onmessage(message, channel, event);
                    const functions = behaviors[message];

                    if (functions && functions.length) {
                        for (let i = 0; i < functions.length; i++) {
                            functions[i]();
                        }
                    }
                }

                socket.onclose = function(event) {
                    if (!socket
                        || (event.code === 1000 && event.reason === REASON_EXPIRED)
                        || (event.code === 1008 || event.reason === REASON_UNKNOWN_CHANNEL) // Older IE versions incorrectly return 1005 instead of 1008, hence the fallback check on the message.
                        || (reconnectAttempts == null)
                        || (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS))
                    {
                        onclose(event.code, channel, event);
                    }
                    else {
                        onerror(event.code, channel, event);
                        setTimeout(self.open, RECONNECT_INTERVAL * reconnectAttempts++);
                    }
                }
            }

            /**
             * Closes the reconnecting websocket.
             */
            self.close = function() {
                if (socket) {
                    const s = socket;
                    socket = null;
                    reconnectAttempts = null;
                    s.close();
                }
            }

        }

        // Public static functions ----------------------------------------------------------------------------------------

        /**
         * Initialize a websocket on the given client identifier. When connected, it will stay open and reconnect as
         * long as URL is valid and <code>faces.push.close()</code> hasn't explicitly been called on the same client
         * identifier.
         * @param {string} clientId The client identifier of the websocket.
         * @param {string} url The URL of the websocket. All open websockets on the same URL will receive the
         * same push notification from the server.
         * @param {string} channel The channel name of the websocket.
         * @param {function} onopen The JavaScript event handler function that is invoked when the websocket is opened.
         * The function will be invoked with one argument: the client identifier.
         * @param {function} onmessage The JavaScript event handler function that is invoked when a message is received from
         * the server. The function will be invoked with three arguments: the push message, the client identifier and
         * the raw <code>MessageEvent</code> itself.
         * @param {function} onerror The JavaScript event handler function that is invoked when a connection error has
         * occurred and the web socket will attempt to reconnect. The function will be invoked with three arguments: the
         * error reason code, the channel name and the raw <code>CloseEvent</code> itself. Note that this will not be
         * invoked on final close of the web socket, even when the final close is caused by an error. See also
         * <a href="http://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455 section 7.4.1</a> and {@link CloseCodes} API
         * for an elaborate list of all close codes.
         * @param {function} onclose The function to be invoked when the web socket is closed and will not anymore attempt
         * to reconnect. The function will be invoked with three arguments: the close reason code, the channel name
         * and the raw <code>CloseEvent</code> itself. Note that this will also be invoked when the close is caused by an
         * error and that you can inspect the close reason code if an actual connection error occurred and which one (i.e.
         * when the code is not 1000 or 1008). See also <a href="http://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455
         * section 7.4.1</a> and {@link CloseCodes} API for an elaborate list of all close codes.
         * @param {Object} behaviors Client behavior functions to be invoked when specific message is received.
         * @param {boolean} autoconnect Whether or not to automatically connect the socket. Defaults to <code>false</code>.
         * @member faces.push
         * @function faces.push.init
         */
        self.init = function(clientId, url, channel, onopen, onmessage, onerror, onclose, behaviors, autoconnect) {
            onclose = resolveFunction(onclose);

            if (!sockets[clientId]) {
                sockets[clientId] = new ReconnectingWebsocket(url, channel, resolveFunction(onopen), resolveFunction(onmessage), resolveFunction(onerror), onclose, behaviors);
            }

            if (autoconnect) {
                self.open(clientId);
            }
        }

        /**
         * Open the websocket on the given client identifier.
         * @param {string} clientId The client identifier of the websocket.
         * @throws {Error} When client identifier is unknown. You may need to initialize it first via <code>init()</code> function.
         * @member faces.push
         * @function faces.push.open
         */
        self.open = function(clientId) {
            getSocket(clientId).open();
        }

        /**
         * Close the websocket on the given client identifier.
         * @param {string} clientId The client identifier of the websocket.
         * @throws {Error} When client identifier is unknown. You may need to initialize it first via <code>init()</code> function.
         * @member faces.push
         * @function faces.push.close
         */
        self.close = function(clientId) {
            getSocket(clientId).close();
        }

        // Private static functions ---------------------------------------------------------------------------------------

        /**
         * If given function is actually not a function, then try to interpret it as name of a global function.
         * If it still doesn't resolve to anything, then return a NOOP function.
         * @param {Object} fn Can be function, or string representing function name, or undefined.
         * @return {function} The intended function, or a NOOP function when undefined.
         */
        function resolveFunction(fn) {
            return (typeof fn !== "function") && (fn = window[fn] || function(){}), fn;
        }

        /**
         * Get socket associated with given client identifier.
         * @param {string} clientId The client identifier of the websocket.
         * @return {Socket} Socket associated with given client identifier.
         * @throws {Error} When client identifier is unknown. You may need to initialize it first via <code>init()</code> function.
         */
        function getSocket(clientId) {
            const socket = sockets[clientId];
            if (socket) return socket;
            else throw new Error("Unknown clientId: " + clientId);
        }

        // Expose self to public ------------------------------------------------------------------------------------------

        return self;

    })(window);


    /**
     * The namespace for Jakarta Faces JavaScript utilities.
     * @name faces.util
     * @namespace
     */
    faces.util = {};

    /**
     * <p>A varargs function that invokes an arbitrary number of scripts.
     * If any script in the chain returns false, the chain is short-circuited
     * and subsequent scripts are not invoked.  Any number of scripts may
     * specified after the <code>event</code> argument.</p>
     *
     * @param source The DOM element that triggered this Ajax request, or an
     * id string of the element to use as the triggering element.
     * @param event The DOM event that triggered this Ajax request.  The
     * <code>event</code> argument is optional.
     *
     * @returns boolean <code>false</code> if any scripts in the chain return <code>false</code>,
     *  otherwise returns <code>true</code>
     *
     * @function faces.util.chain
     */
    faces.util.chain = function(source, event) {

        if (arguments.length < 3) {
            return true;
        }

        // RELEASE_PENDING rogerk - shouldn't this be getElementById instead of null
        const thisArg = (typeof source === 'object') ? source : null;

        // Call back any scripts that were passed in
        for (let i = 2; i < arguments.length; i++) {

            const f = new Function("event", arguments[i]);
            const returnValue = f.call(thisArg, event);

            if (returnValue === false) {
                return false;
            }
        }
        return true;

    };

    /**
     * <p class="changed_added_2_2">The result of calling
     * <code>UINamingContainer.getNamingContainerSeparatorChar().</code></p>
     */
    faces.separatorchar = '#{facesContext.namingContainerSeparatorChar}';

    /**
     * <p class="changed_added_2_3">
     * The result of calling <code>ExternalContext.getRequestContextPath()</code>.
     */
    faces.contextpath = '#{facesContext.externalContext.requestContextPath}';

    /**
     * <p>An integer specifying the specification version that this file implements.
     * Its format is: rightmost two digits, bug release number, next two digits,
     * minor release number, leftmost digits, major release number.
     * This number may only be incremented by a new release of the specification.</p>
     */
    faces.specversion = 40000;

    /**
     * <p>An integer specifying the implementation version that this file implements.
     * It's a monotonically increasing number, reset with every increment of
     * <code>faces.specversion</code>
     * This number is implementation dependent.</p>
     */
    faces.implversion = 4;


} //end if version detection block
/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
 @project Faces Ajax Library
 @version 2.0
 @description This is the standard implementation of the Faces Ajax Library.
 */

/**
 * @name mojarra
 * @namespace
 */

/*
 * Create our top level namespaces - mojarra
 */
window.mojarra = window.mojarra || {};


/**
 * This function deletes any hidden parameters added
 * to the form by checking for a variable called 'adp'
 * defined on the form.  If present, this variable will
 * contain all the params added by 'apf'.
 *
 * @param f - the target form
 */
mojarra.dpf = function dpf(f) {
    const adp = f.adp;
    if (adp !== null) {
        for ( const param of adp ) {
            param.remove();
        }
    }
};

/**
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
    const adp = [];
    f.adp = adp;
    let i = 0;
    for (const k of Object.keys(pvp) ) {
        const p = document.createElement("input");
        p.type = "hidden";
        p.name = k;
        p.value = pvp[k];
        f.appendChild(p);
        adp[i++] = p;
    }
};

/**
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
mojarra.cljs = function cljs(f, pvp, t) {
    mojarra.apf(f, pvp);
    const ft = f.target;
    if (t) {
        f.target = t;
    }

    const input = document.createElement('input');
    input.type = 'submit';
    f.appendChild(input);
    input.click();
    input.remove();

    f.target = ft;
    mojarra.dpf(f);
};

/**
 * This is called by functions that need access to their calling
 * context, in the form of <code>this</code> and <code>event</code>
 * objects.
 *
 *  @param f the function to execute
 *  @param t this of the calling function
 *  @param e event of the calling function
 *  @return object that f returns
 */
mojarra.facescbk = function facescbk(f, t, e) {
    return f.call(t,e);
};

/**
 * This is called by the AjaxBehaviorRenderer script to
 * trigger a faces.ajax.request() call.
 *
 *  @param s the source element or id
 *  @param e event of the calling function
 *  @param n name of the behavior event that has fired
 *  @param ex execute list
 *  @param re render list
 *  @param op options object
 */
mojarra.ab = function ab(s, e, n, ex, re, op) {
    if (!op) op = {};
    if (n)   op["jakarta.faces.behavior.event"] = n;
    if (ex)  op["execute"] = ex;
    if (re)  op["render"] = re;
    faces.ajax.request(s, e, op);
};

/**
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
    else if (typeof window.onload === "function") {
        const oldListener = window.onload;
        window.onload = function() { oldListener(); l(); };
    }
    else {
        window.onload = l;
    }

};

