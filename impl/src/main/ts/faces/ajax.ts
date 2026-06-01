import type FacesSpec from "../../../../../faces/api/src/main/resources/META-INF/resources/jakarta.faces/faces";
import {
    UDEF, EMPTY, SPACE, FORM,
    VIEW_STATE_PARAM, CLIENT_WINDOW_PARAM, ALWAYS_EXECUTE_IDS, ENCODED_URL_PARAM,
    SOURCE_PARAM, PARTIAL_AJAX_PARAM, PARTIAL_EVENT_PARAM, PARTIAL_EXECUTE_PARAM,
    PARTIAL_RENDER_PARAM, PARTIAL_RESET_VALUES_PARAM,
    PARTIAL_SUBMIT_ENABLED,
} from "./constants";
import { isNull, isNotNull, contains } from "./lang";
import {
    getHead, getNonce, executeScriptWithNonce,
    getElemById, getElementByName, getFormInputElementByName, containsNamedChild,
} from "./dom";
import { getPartialViewState } from "./api";

/**
 * Internal request context built up by `request()` and read by `response()` and the listeners.
 *
 * Extends the spec's {@link FacesSpec.ajax.RequestContext} with the impl-private fields
 * the engine needs to carry across the request/response lifecycle. `sourceid` is widened
 * to `string | Element` because `response()` resolves the id to its DOM element in place.
 */
interface AjaxContext extends Omit<FacesSpec.ajax.RequestContext, "sourceid"> {
    sourceid?: string | Element;
    render?: string;
    formId?: string;
    namingContainerId?: string;
    namingContainerPrefix?: string;
    includeViewParams?: boolean;
    [key: string]: unknown;
}

/** Lightweight global lookup used for the few faces.* cross-namespace reads (separatorchar, etc.). */
type FacesGlobal = { faces: { separatorchar: string; getProjectStage(): FacesSpec.ProjectStage; getViewState(form: HTMLFormElement): string; getClientWindow(node?: HTMLElement | string): string | null; ajax: { response(req: XMLHttpRequest, ctx: AjaxContext): void } } };
const facesGlobal = (): FacesGlobal["faces"] => (window as unknown as FacesGlobal).faces;

export const ajax = (function () {

        const eventListeners: FacesSpec.ajax.OnEventCallback[] = [];
        const errorListeners: FacesSpec.ajax.OnErrorCallback[] = [];

        let delayHandler: ReturnType<typeof setTimeout> | null = null;

        /**
         * Utility function that determines if a file control exists for the form.
         * @ignore
         */
        const hasInputFileControl = function(form: HTMLFormElement): boolean { return isNotNull(form.querySelector("input[type='file']")); };


        // --- FACES input processing functions ---------------------------------------------------------------------------------------

        /**
         * Get the form element which encloses the supplied element.
         * @param element - element to act against in search
         * @returns form element representing enclosing form, or first form if none found.
         * @ignore
         */
        const getForm = function(element: Element): HTMLFormElement | null {
            const form = element.closest<HTMLFormElement>(FORM);
            return form ? form : document.forms[0] ?? null;
        };

        /**
         * Get an array of all Faces form elements which need their view state to be updated.
         * This covers at least the form that submitted the request and any form that is covered in the render target list.
         *
         * @param context {Object} An object containing the request context, including the following properties:
         * the source element, per call onerror callback function, per call onevent callback function, the render
         * instructions, the submitting form ID, the naming container ID and naming container prefix.
         * @param hiddenStateFieldName {string} The hidden state field name, e.g. jakarta.faces.ViewState or jakarta.faces.ClientWindow
         * @return {Array<HTMLFormElement>} Get an array of all Faces form elements which need their view state to be updated.
         */
        const getFormsToUpdate = function getFormsToUpdate(context: AjaxContext, hiddenStateFieldName: string): HTMLFormElement[] {
            const formsToUpdate = new Set<HTMLFormElement>();

            // return true if the passed element is a form
            const isFormElement = (element: Element | Document): element is HTMLFormElement =>
                (element as Element).nodeName != null && (element as Element).nodeName.toLowerCase() === FORM;

            // return true if the passed form needs the view state hidden field
            const isValidForm = (form: HTMLFormElement): boolean =>
                form.method === "post" && !!form.id && !!form.elements
                && (context.namingContainerPrefix == null || form.id.startsWith(context.namingContainerPrefix));

            // if the passed DOM element is a form and is valid,
            // then add to the forms to update,
            // otherwise add all the valid forms in the descendants of the specified element
            const add = (element: Element | Document | null) => {
                if (element) {
                    if (isFormElement(element) && isValidForm(element)) {
                        formsToUpdate.add(element);
                    }
                    else {
                        const forms = (element as Element | Document).getElementsByTagName(FORM) as HTMLCollectionOf<HTMLFormElement>;
                        for (const form of Array.from(forms))
                            add(form);
                    }
                }
            };

            if (context.formId) {
                add(document.getElementById(context.formId));
            }

            const isRenderAll = !!context.render && contains(context.render, "@all");

            if (context.render) {
                // if is render @all then add all the forms of the document
                if (isRenderAll) {
                    add(document);
                }
                // otherwise add the forms taken from the render attribute
                else {
                    const clientIds = context.render.split(SPACE);
                    for (const clientId of clientIds)
                        add(document.getElementById(clientId));
                }
            }

            // second pass: we have to include all the updated forms using PartialViewContext from Java
            if (!isRenderAll) { // performance bonus: only if we aren't in @all case
                const allForms = document.getElementsByTagName(FORM) as HTMLCollectionOf<HTMLFormElement>;

                for (const form of Array.from(allForms)) {
                    if (!formsToUpdate.has(form)
                        && isValidForm(form)
                        && isNull(getHiddenStateField(form, hiddenStateFieldName, context.namingContainerPrefix))) {
                        formsToUpdate.add(form);
                    }
                }
            }

            // Set to Array
            return [...formsToUpdate];
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
        const namespaceParametersIfNecessary = function namespaceParametersIfNecessary(parameters: string, sourceClientId: string, namingContainerPrefix: string): string {
            if (sourceClientId.indexOf(namingContainerPrefix) !== 0) {
                return parameters; // Unexpected source client ID; let's silently do nothing.
            }

            const sep = facesGlobal().separatorchar;
            const targetClientIds = parameters.replace(/^\s+|\s+$/g, '').split(/\s+/g);

            // adapt each targetClientId and replace the modified version inside the original array
            for ( let i = 0; i < targetClientIds.length; i++) {
                let targetClientId = targetClientIds[i];

                if (targetClientId.indexOf(sep) === 0) {
                    targetClientId = targetClientId.substring(1);

                    if (targetClientId.indexOf(namingContainerPrefix) !== 0) {
                        targetClientId = namingContainerPrefix + targetClientId;
                    }
                } else if (targetClientId.indexOf(namingContainerPrefix) !== 0) {
                    const parentClientId = sourceClientId.substring(0, sourceClientId.lastIndexOf(sep));

                    if (namingContainerPrefix + targetClientId === parentClientId) {
                        targetClientId = parentClientId;
                    } else {
                        targetClientId = parentClientId + sep + targetClientId;
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
        const getScripts = function getScripts(html: string): RegExpMatchArray[] {
            const scripts: RegExpMatchArray[] = [];
            const initialnodes = html.match(SCRIPT_TAG_REGEX);
            while (!!initialnodes && initialnodes.length > 0) {
                const scriptStr = initialnodes.shift()!.match(SINGLE_SCRIPT_TAG_REGEX); // todo: multiple shift array ... rewrite this algo
                if (!scriptStr) continue;
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
         * Remove all the portion of code matching the script pattern from the passed string,
         * preserving scripts whose type is set to something other than text/javascript.
         * @param html a String containing a portion of html
         * @ignore
         */
        const removeScripts = function removeScripts(html: string): string {
            return html.replace(SCRIPT_TAG_REGEX, (match: string) => {
                const type = match.match(TAG_ATTRIBUTE_TYPE_REGEX);
                if (!!type && type[1] !== "text/javascript") {
                    return match; // keep non-text/javascript scripts
                }
                return EMPTY;
            });
        };

        /**
         * Run an array of script nodes,
         * @param scripts Array of script nodes.
         * @ignore
         */
        const runScripts = function runScripts(scripts: RegExpMatchArray[]): void {
            if (!scripts || scripts.length === 0) {
                return;
            }

            const loadedScripts = document.getElementsByTagName("script") as HTMLCollectionOf<HTMLScriptElement>;
            const loadedScriptUrls: string[] = [];

            for (const scriptNode of Array.from(loadedScripts)) {
                const url = scriptNode.getAttribute("src");
                if (url) loadedScriptUrls.push(url);
            }

            const head = getHead();
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
        const runScript = function runScript(head: HTMLElement, loadedScriptUrls: string[], scripts: RegExpMatchArray[], index: number): void {
            if (index >= scripts.length) {
                return;
            }

            // Regex to find src attribute
            const findsrc = /src="([\S]*?)"/im;
            // Regex to remove leading cruft
            const stripStart = /^\s*(<!--)*\s*(\/\/)*\s*(\/\*)*\s*\n*\**\n*\s*\*.*\n*\s*\*\/(<!\[CDATA\[)*/;

            const scriptStr = scripts[index];
            const src = scriptStr[1].match(findsrc);
            let scriptLoadedViaUrl = false;

            const nonce = getNonce();

            if (!!src && src[1]) {
                // if this is a file, load it
                const url = unescapeHTML(src[1]);
                // if this is already loaded, don't load it
                // it's never necessary, and can make debugging difficult
                if (loadedScriptUrls.indexOf(url) < 0) {
                    const scriptNode = document.createElement('script');
                    const parserElement = document.createElement('div');
                    parserElement.innerHTML = scriptStr[0];
                    cloneAttributes(scriptNode, parserElement.firstChild as Element);
                    deleteNode(parserElement);
                    scriptNode.nonce = nonce as string;
                    scriptNode.src = url;
                    scriptNode.onload = scriptNode.onerror = () => {
                        runScript(head, loadedScriptUrls, scripts, index + 1);
                    };
                    head.appendChild(scriptNode); // append (and leave) at end of head
                    scriptLoadedViaUrl = true;
                }
            } else if (!!scriptStr && scriptStr[2]) {
                // else get content of tag, without leading CDATA and such
                const script = scriptStr[2].replace(stripStart, EMPTY);

                if (!!script) {
                    executeScriptWithNonce(head, script, nonce);
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
        const runStylesheets = function runStylesheets(str: string): void {
            // Regex to find all links in a string
            const findlinks = /<link[^>]*\/>/igm;
            // Regex to find one link, to isolate its attributes [1]
            const findlink = /<link([^>]*)\/>/im;
            // Regex to find type attribute
            const findtype = /type="([\S]*?)"/im;
            const findhref = /href="([\S]*?)"/im;

            // the head of the document, note that document.head do not always work
            const head = getHead();

            let loadedStylesheetUrls: string[] | null = null;
            let parserElement: HTMLDivElement | null = null;

            const initialnodes = str.match(findlinks);
            while (!!initialnodes && initialnodes.length > 0) {
                const linkStr = initialnodes.shift()!.match(findlink);
                if (!linkStr) continue;
                // check the type - skip if specified but not text/css
                const type = linkStr[1].match(findtype);
                if (!!type && type[1] !== "text/css") {
                    continue;
                }
                const href = linkStr[1].match(findhref);
                if (!!href && href[1]) {
                    if (loadedStylesheetUrls === null) {
                        const loadedLinks = document.getElementsByTagName("link") as HTMLCollectionOf<HTMLLinkElement>;
                        loadedStylesheetUrls = [];

                        for (const linkNode of Array.from(loadedLinks)) {
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

                    if (loadedStylesheetUrls && loadedStylesheetUrls.indexOf(url) < 0) {
                        // create stylesheet node
                        parserElement = parserElement !== null ? parserElement : document.createElement('div');
                        parserElement.innerHTML = linkStr[0];
                        const linkNode = parserElement.firstChild as HTMLLinkElement | null;
                        if (linkNode) {
                            linkNode.type = 'text/css';
                            linkNode.rel = 'stylesheet';
                            linkNode.href = url;
                            head.appendChild(linkNode); // add it to end of the head (and don't remove it)
                        }
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
        const elementReplaceStr = function elementReplaceStr(element: Element, tempTagName: string, src: string): void {
            // Replacing the head element is not supported.
            if (element && element.nodeName && element.nodeName.toLowerCase() === "head")
                throw new Error("Attempted to replace a head element - this is not allowed.");

            const temp = document.createElement(tempTagName);
            if (element.id) {
                temp.id = element.id;
            }

            // Get scripts from text, then strip them so innerHTML does not see them,
            // then run them after the DOM is in place.
            const scripts = getScripts(src);
            src = removeScripts(src);
            temp.innerHTML = src;
            cloneAttributes(temp, element);
            replaceNode(temp, element);
            runScripts(scripts);
        };

        // --- Faces xml errors ---------------------------------------------------------------------------

        const PARSED_OK = "Document contains no parsing errors";
        const PARSED_EMPTY = "Document is empty";

        /**
         * <p>Returns a human readable description of the parsing error. Useful
         * for debugging. Tip: append the returned error string in a &lt;pre&gt;
         * element if you want to render it.</p>
         *
         * Webkit reports the error as the documentElement; Firefox/Chromium nest
         * a <code>&lt;parsererror&gt;</code> element inside the document.
         */
        const getParseErrorText = function (doc: Document | null): string {
            if (!doc || !doc.documentElement) {
                return PARSED_EMPTY;
            }
            if (doc.documentElement.tagName === "parsererror") {
                const first = doc.documentElement.firstChild as (CharacterData & { nextSibling: ChildNode | null }) | null;
                let text = (first as unknown as { data: string } | null)?.data ?? "";
                const nextFirst = first?.nextSibling?.firstChild as unknown as { data?: string } | null;
                text += "\n" + (nextFirst?.data ?? "");
                return text;
            }
            if (doc.getElementsByTagName("parsererror").length > 0) {
                const parsererror = doc.getElementsByTagName("parsererror")[0];
                return (parsererror.textContent ?? "") + "\n";
            }
            return PARSED_OK;
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
        const clearEvents = function clearEvents(node: Element | null): void {
            if (!node) {
                return;
            }
            // don't do anything for text and comment nodes - unnecessary
            if (node.nodeType === Node.TEXT_NODE || node.nodeType === Node.COMMENT_NODE) {
                return;
            }
            // remove the events from node
            try {
                const indexed = node as unknown as { [key: string]: unknown };
                for (const eventName of NODE_EVENTS)
                    indexed[eventName] = null;
            } catch (_ex) {
                // it's OK if it fails, at least we tried
            }
        };

        /**
         * Deletes node
         * @param node
         * @ignore
         */
        const deleteNode = function deleteNode(node: Node | null): void {
            if (node && node.parentNode) (node as ChildNode).remove();
        };

        /**
         * Delete all nodes
         * @param nodes array of node
         * @ignore
         */
        const deleteNodes = function deleteNodes(nodes: ArrayLike<Node>): void {
            for (const node of Array.from(nodes))
                deleteNode(node);
        };

        /**
         * Deletes all children of a node
         * @param node
         * @ignore
         */
        const deleteChildren = function deleteChildren(node: Node | null): void {
            if (node)
                while (node.lastChild)
                    (node.lastChild as ChildNode).remove();
        };

        /**
         * <p> Copies the childNodes of nodeFrom to nodeTo</p>
         *
         * @param  nodeFrom the Node to copy the childNodes from
         * @param  nodeTo the Node to copy the childNodes to
         * @ignore
         */
        const copyChildNodes = function copyChildNodes(nodeFrom: Node | null, nodeTo: Node | null): void {

            if ((!nodeFrom) || (!nodeTo)) {
                throw "Both source and destination nodes must be provided";
            }

            deleteChildren(nodeTo);

            // if within the same doc, just move, else copy and delete
            if (nodeFrom.ownerDocument === nodeTo.ownerDocument) {
                while (nodeFrom.firstChild)
                    nodeTo.appendChild(nodeFrom.firstChild);

            } else {
                const ownerDoc = (nodeTo.nodeType === Node.DOCUMENT_NODE ? nodeTo as Document : nodeTo.ownerDocument)!;
                const nodeFromChildNodes = nodeFrom.childNodes;

                //if ( typeof(ownerDoc.importNode) !== UDEF ) {
                    for (const nodeFromChild of Array.from(nodeFromChildNodes))
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
        const replaceNode = function replaceNode(newNode: Node, node: ChildNode): void {
            node.replaceWith(newNode);
        };

        /**
         * @ignore
         */
        const propertyToAttribute = function propertyToAttribute(name: string): string {
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
        const inputElementProperties = [ 'name', 'value', 'src', 'alt', 'useMap', 'tabIndex', 'accessKey', 'accept', 'type' ];

        // core + input element properties
        const coreAndInputElementProperties = coreElementProperties.concat(inputElementProperties);

        // enumerate additional integer input attributes
        const inputElementPositiveIntegerProperties = [ 'size', 'maxLength' ];

        // enumerate additional boolean input attributes
        const inputElementBooleanProperties = [ 'checked', 'disabled', 'readOnly' ];

        const TABLE_INNER_TAGS = ['td', 'th', 'tr', 'tbody', 'thead', 'tfoot'];

        /**
         * copy all attributes from one element to another - except id
         * @param target element to copy attributes to
         * @param source element to copy attributes from
         * @ignore
         */
        const cloneAttributes = function cloneAttributes(target: Element, source: Element): void {

            const t = target as HTMLElement & { [key: string]: unknown };
            const s = source as HTMLElement & { [key: string]: unknown };

            const isInputElement = target.nodeName.toLowerCase() === 'input';
            const propertyNames = isInputElement ? coreAndInputElementProperties : coreElementProperties;
            const isXML = !(source.ownerDocument as Document & { contentType?: string }).contentType
                || (source.ownerDocument as Document & { contentType?: string }).contentType === 'text/xml';

            for (const propertyName of propertyNames) {
                const attributeName = propertyToAttribute(propertyName);
                const sourceValue = isXML ? source.getAttribute(attributeName) : s[propertyName];
                if (isNotNull(sourceValue)) t[propertyName] = sourceValue;
            }

            if (isInputElement) {
                for (const propertyName of inputElementPositiveIntegerProperties) {
                    const attributeName = propertyToAttribute(propertyName);
                    const sourceValue = isXML ? source.getAttribute(attributeName) : s[propertyName];
                    if (parseInt(sourceValue as string) >= 0) t[propertyName] = sourceValue;
                }

                for (const booleanPropertyName of inputElementBooleanProperties) {
                    const newBooleanValue = s[booleanPropertyName];
                    if (isNotNull(newBooleanValue)) t[booleanPropertyName] = newBooleanValue;
                }
            }

            //'style' attribute special case
            if (source.hasAttribute('style')) {
                const sourceStyle = source.getAttribute('style');
                if (isNotNull(sourceStyle)) target.setAttribute('style', sourceStyle as string);
            } else if (target.hasAttribute('style')) {
                target.removeAttribute('style');
            }

            // Special case for 'dir' attribute
            if ((source as HTMLElement).dir !== (target as HTMLElement).dir) {
                if (source.hasAttribute('dir')) {
                    (target as HTMLElement).dir = (source as HTMLElement).dir;
                } else if (target.hasAttribute('dir')) {
                    (target as HTMLElement).dir = '';
                }
            }

            for (const name of LISTENER_NAMES) {
                t[name] = s[name] ? s[name] : null;
                if (s[name]) {
                    s[name] = null;
                }
            }

            // clone HTML5 data-* attributes
            const sourceDataset = (source as HTMLElement).dataset;
            const targetDataset = (target as HTMLElement).dataset;
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
        const elementReplace = function elementReplace(newElement: HTMLElement, origElement: HTMLElement): void {

            // copy source attributes to target node
            try {
                cloneAttributes(origElement, newElement);
            } catch (_ex) {
                // if in dev mode, report an error, else try to limp onward
                if (facesGlobal().getProjectStage() === "Development") {
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
        const getBodyElement = function getBodyElement(docStr: string): Element {

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
        const getEncodedUrlElement = function getEncodedUrlElement(form: HTMLFormElement): Element | null {
            return getFormInputElementByName(form, ENCODED_URL_PARAM);
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
        const updateHiddenStateFields = function updateHiddenStateFields(updateElement: Element, context: AjaxContext, hiddenStateFieldName: string): void {
            const firstChild = updateElement.firstChild as Text | null;
            const state = firstChild?.wholeText ?? "";
            const formsToUpdate = getFormsToUpdate(context, hiddenStateFieldName);

            for (const form of formsToUpdate) {
                let field = getHiddenStateField(form, hiddenStateFieldName, context.namingContainerPrefix) as HTMLInputElement | null;
                if (isNull(field)) {
                    field = document.createElement("input");
                    field.type = "hidden";
                    field.name = (context.namingContainerPrefix ?? "") + hiddenStateFieldName;
                    form.appendChild(field);
                }
                field!.value = state;
            }
        };

        /**
         * Find hidden state field for a given form.
         * @param form {HTMLFormElement} The form to find hidden state field in.
         * @param hiddenStateFieldName {string} The hidden state field name, e.g. jakarta.faces.ViewState or jakarta.faces.ClientWindow
         * @param [namingContainerPrefix] {string} The naming container prefix, if any (the view root ID suffixed with separator character).
         * @return {HTMLInputElement} HTMLInputElement representing the hidden state field for a given form
         * @ignore
         */
        const getHiddenStateField = function getHiddenStateField(form: HTMLFormElement, hiddenStateFieldName: string, namingContainerPrefix?: string): Element | null {
            const fullHiddenStateFieldName = namingContainerPrefix ? namingContainerPrefix + hiddenStateFieldName : hiddenStateFieldName;
            return getFormInputElementByName(form, fullHiddenStateFieldName);
        };

        /**
         * Do update.
         * @param updateElement The update element of partial response.
         * @param context An object containing the request context, including the following properties:
         * the source element, per call onerror callback function, per call onevent callback function, the render
         * instructions, the submitting form ID, the naming container ID and naming container prefix.
         * @ignore
         */
        const doUpdate = function doUpdate(updateElement: Element, context: AjaxContext): void {

            let scripts: RegExpMatchArray[] = []; // temp holding value for array of script nodes

            const id = updateElement.getAttribute('id');
            if (id == null) return;
            const sep = facesGlobal().separatorchar;
            const viewStateRegex = new RegExp((context.namingContainerPrefix ?? "") + VIEW_STATE_PARAM + sep + ".+$");
            const windowIdRegex = new RegExp((context.namingContainerPrefix ?? "") + CLIENT_WINDOW_PARAM + sep + ".+$");

            if (id.match(viewStateRegex)) {
                updateHiddenStateFields(updateElement, context, VIEW_STATE_PARAM);
                return;
            } else if (id.match(windowIdRegex)) {
                updateHiddenStateFields(updateElement, context, CLIENT_WINDOW_PARAM);
                return;
            }

            // join the CDATA sections in the markup
            let markup = EMPTY;
            for (const updateElementChild of Array.from(updateElement.childNodes)) {
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
                const element = getElemById(id) as HTMLElement | null;

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
                            elementReplace(getBodyElement(newSrc) as HTMLElement, docBody);
                            runScripts(scripts);
                        } catch (_e) {
                            // OK, replacing the body didn't work with XML - fall back to quirks mode insert
                            let srcBody;
                            // if src contains </body>
                            const bodyEnd = bodyEndEx.exec(src);
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
                        // Get the scripts from the html, then strip and re-run them after insertion.
                        scripts = getScripts(html);
                        html = removeScripts(html);
                        // enclose new html inside a table
                        newElementContainer.innerHTML = '<table>' + html + '</table>';
                        let newElement: ChildNode | null = newElementContainer.firstChild;
                        //some browsers will also create intermediary elements such as table>tbody>tr>td
                        while ((null !== newElement) && (id !== (newElement as Element).id)) {
                            newElement = newElement.firstChild;
                        }

                        if (newElement) replaceNode(newElement, element);
                        runScripts(scripts);

                    } else if (element.nodeName.toLowerCase() === 'input') {
                        // special case handling for 'input' elements
                        // in order to not lose focus when updating,
                        // input elements need to be added in place.
                        newElementContainer = document.createElement('div');
                        newElementContainer.innerHTML = html;
                        const newElement = newElementContainer.firstChild as Element | null;

                        if (newElement) cloneAttributes(element, newElement);
                        deleteNode(newElementContainer);
                    } else if (html.length > 0) {
                        // Get the scripts from the text, then strip and re-run them after insertion.
                        scripts = getScripts(html);
                        html = removeScripts(html);
                        newElementContainer.innerHTML = html;
                        const firstChild = newElementContainer.firstChild;
                        if (firstChild) replaceNode(firstChild, element);
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
        const doDelete = function doDelete(element: Element | null): void {
            const id = element?.getAttribute('id');
            if (id) deleteNode(getElemById(id));
        };

        /**
         * Insert a node specified by the element.
         * @param element
         * @ignore
         */
        const doInsert = function doInsert(element: Element): void {

            const insertChild = element.firstChild as Element | null;
            if (!insertChild) return;
            const targetId = insertChild.getAttribute('id');
            if (!targetId) return;
            let target: Node | null = getElemById(targetId);
            if (!target) return;
            const parent = target.parentNode;
            const cdata = insertChild.firstChild;
            let html = cdata?.nodeValue ?? "";

            // todo: check if is it possible to use the TABLE_ELEMENTS array and remove the RegExp
            const tablePattern = new RegExp("<\\s*(td|th|tr|tbody|thead|tfoot)", "i");
            const isInTable = tablePattern.test(html);

            // Get the scripts from the text, strip them out, then execute them.
            const scripts = getScripts(html);
            html = removeScripts(html);
            runScripts(scripts);
            const tempElement = document.createElement('div');
            let newElement: Node | null;
            if (isInTable) {
                tempElement.innerHTML = '<table>' + html + '</table>';
                newElement = tempElement.firstChild;
                //some browsers will also create intermediary elements such as table>tbody>tr>td
                //test for presence of id on the new element since we do not have it directly
                while ((null !== newElement) && (EMPTY === (newElement as Element).id)) {
                    newElement = newElement.firstChild;
                }
            } else {
                tempElement.innerHTML = html;
                newElement = tempElement.firstChild;
            }

            if (insertChild.nodeName === 'after') {
                // Get the next in the list, to insert before
                target = (target as ChildNode).nextSibling;
            }  // otherwise, this is a 'before' element
            if (!!tempElement.innerHTML && parent && newElement) { // check if only scripts were inserted - if so, do nothing here
                parent.insertBefore(newElement, target);
            }

            deleteNode(tempElement);
        };

        /**
         * Modify attributes of given element id.
         * @param element
         * @ignore
         */
        const doAttributes = function doAttributes(element: Element): void {

            // Get id of element we'll act against
            const id = element.getAttribute('id');
            const target = id ? getElemById(id) as (HTMLInputElement & { [key: string]: unknown }) | null : null;

            if (!target) {
                throw new Error("The specified id: " + id + " was not found in the page.");
            }

            // There can be multiple attributes modified.  Loop through the list.
            const nodes = element.childNodes;
            for (const node of Array.from(nodes) as Element[]) {
                const name = node.getAttribute!('name');
                const value = node.getAttribute!('value');
                if (name == null) continue;

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
                    target.value = value ?? "";
                } else {
                    target.setAttribute(name, value ?? "");
                }
            }
        };

        /**
         * Eval the CDATA of the element.
         * Evaluate the parsed JavaScript code in a global context.
         * @param element to eval
         * @ignore
         */
        const doEval = function doEval(element: Element | null): void {
            (() => { //
                const script = element ? element.textContent : undefined;
                if (script) runScripts([['', '', script] as unknown as RegExpMatchArray]);
                else console.warn('called doEval with no source code');
            })();
        };

        /**
         * Ajax Request Queue
         * @ignore
         */
        interface AjaxQueue {
            getSize(): number;
            isEmpty(): boolean;
            enqueue(req: AjaxRequest): void;
            dequeue(): AjaxRequest | undefined;
            getOldestElement(): AjaxRequest | undefined;
        }

        const Queue: AjaxQueue = (function () {
            // Create the internal queue
            let queue: AjaxRequest[] = [];

            // the amount of space at the front of the queue, initialised to zero
            let queueSpace = 0;

            return {
                getSize() {
                    return queue.length - queueSpace;
                },
                isEmpty() {
                    return (queue.length === 0);
                },
                enqueue(req) {
                    queue.push(req);
                },
                dequeue() {
                    let element: AjaxRequest | undefined = undefined;
                    if (queue.length) {
                        element = queue[queueSpace];
                        if (++queueSpace * 2 >= queue.length) {
                            queue = queue.slice(queueSpace);
                            queueSpace = 0;
                        }
                    }
                    return element;
                },
                getOldestElement() {
                    return queue.length ? queue[queueSpace] : undefined;
                },
            };
        })();


        /**
         * AjaxEngine handles Ajax implementation details.
         * @ignore
         */
        interface AjaxRequest {
            url: string | null;
            context: AjaxContext & { form?: HTMLFormElement };
            xmlReq: XMLHttpRequest | null;
            async: boolean;
            parameters: Record<string, string>;
            queryString: string | null;
            method: string | null;
            status: number | null;
            fromQueue: boolean;
            que: AjaxQueue;
            generateUniqueUrl?: boolean;
            requestIndex?: number;
            onComplete(): void;
            setupArguments(args: Record<string, unknown>): void;
            sendRequest(): void;
            [key: string]: unknown;
        }

        const AjaxEngine = function AjaxEngine(context: AjaxContext & { form?: HTMLFormElement }): AjaxRequest {

            const req = {} as AjaxRequest;
            req.url = null;
            req.context = context;
            req.context.sourceid = undefined;
            req.context.onerror = undefined;
            req.context.onevent = undefined;
            req.context.namingContainerId = undefined;
            req.context.namingContainerPrefix = undefined;
            req.xmlReq = null;
            req.async = true;
            req.parameters = {};
            req.queryString = null;
            req.method = null;
            req.status = null;
            req.fromQueue = false;
            req.que = Queue;
            req.xmlReq = new XMLHttpRequest();

            req.xmlReq.onreadystatechange = function () {
                if (req.xmlReq && req.xmlReq.readyState === 4) {
                    req.onComplete();
                }
            };

            req.onComplete = function onComplete() {
                if (!req.xmlReq) return;
                if (req.xmlReq.status && (req.xmlReq.status >= 200 && req.xmlReq.status < 300)) {
                    sendEvent(req.xmlReq, req.context, "complete");
                    facesGlobal().ajax.response(req.xmlReq, req.context);
                } else {
                    sendEvent(req.xmlReq, req.context, "complete");
                    sendError(req.xmlReq, req.context, "httpError");
                }

                let nextReq = req.que.getOldestElement();
                if (isNull(nextReq)) return;
                while (nextReq && isNotNull(nextReq.xmlReq) && nextReq.xmlReq!.readyState === 4) {
                    req.que.dequeue();
                    nextReq = req.que.getOldestElement();
                    if (isNull(nextReq)) break;
                }
                if (isNull(nextReq) || !nextReq) return;
                if (isNotNull(nextReq.xmlReq) && nextReq.xmlReq!.readyState === 0) {
                    nextReq.fromQueue = true;
                    nextReq.sendRequest();
                }
            };

            req.setupArguments = function (args) {
                const indexed = req as unknown as Record<string, unknown>;
                for (const i of Object.keys(args)) {
                    if (typeof indexed[i] === UDEF) {
                        req.parameters[i] = args[i] as string;
                    } else {
                        indexed[i] = args[i];
                    }
                }
            };

            req.sendRequest = function () {
                if (!isNotNull(req.xmlReq) || !req.xmlReq) return;
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
                if (!req.fromQueue) {
                    req.que.enqueue(req);
                }
                if (req.generateUniqueUrl && req.method === "GET") {
                    req.parameters["AjaxRequestUniqueId"] = new Date().getTime() + EMPTY + req.requestIndex;
                }

                const form = context.form;
                const isMultiPart = (req.method === "POST" && form != null && form.enctype === 'multipart/form-data');
                const formData = isMultiPart && form ? new FormData(form) : undefined;

                const params = new URLSearchParams(req.queryString ?? undefined);
                for (const i of Object.keys(req.parameters)) {
                    if (isMultiPart && formData) {
                        formData.append(i, req.parameters[i]);
                    } else {
                        params.append(i, req.parameters[i]);
                    }
                }
                req.queryString = params.toString();

                if (req.method === "GET") {
                    if (req.queryString.length > 0 && req.url) {
                        req.url += ((req.url.indexOf("?") > -1) ? "&" : "?") + req.queryString;
                    }
                }

                req.xmlReq.open(req.method ?? "POST", req.url ?? "", req.async);

                if (req.method === "POST") {
                    req.xmlReq.setRequestHeader('Faces-Request', 'partial/ajax');

                    if (isMultiPart && formData) formData.append('Faces-Request', 'partial/ajax');
                    else req.xmlReq.setRequestHeader('Content-type', (form?.enctype ?? '') + ';charset=UTF-8');
                }

                if (!req.async) req.xmlReq.onreadystatechange = null;

                sendEvent(req.xmlReq, req.context, "begin");

                if (isMultiPart && formData) req.xmlReq.send(formData);
                else req.xmlReq.send(req.queryString);

                if (!req.async) req.onComplete();
            };

            return req;
        };

        type ErrorPayload = FacesSpec.AjaxError & { description?: string };

        /**
         * Resolve `context.sourceid` (either a string id or an already-resolved Element)
         * to its DOM element, per 14.4.1 of the 2.0 specification. Returns undefined when
         * the sourceid is unset or refers to a missing element.
         */
        const resolveSourceElement = (sourceid: AjaxContext["sourceid"]): Element | undefined => {
            if (typeof sourceid === "string") {
                return document.getElementById(sourceid) ?? undefined;
            }
            if (sourceid && (sourceid as Element).nodeType !== undefined) {
                return sourceid as Element;
            }
            return undefined;
        };

        /** Copy the XHR response fields onto an {@link FacesSpec.AjaxData} payload. */
        const copyResponseFields = (data: FacesSpec.AjaxData, request: XMLHttpRequest): void => {
            data.responseCode = request.status;
            data.responseXML = request.responseXML ?? undefined;
            data.responseText = request.responseText;
        };

        /**
         * Error handling callback.
         * Assumes that the request has completed.
         * @ignore
         */
        const sendError = function sendError(
            request: XMLHttpRequest,
            context: AjaxContext,
            status: FacesSpec.AjaxErrorStatus,
            description?: string,
            serverErrorName?: string,
            serverErrorMessage?: string,
        ): void {

            // Possible error names: httpError | emptyResponse | serverError | malformedXML

            let sent = false;
            const source = resolveSourceElement(context.sourceid);
            const data: ErrorPayload = {
                type: "error",
                status,
                ...(source && { source }),
            };
            copyResponseFields(data, request);

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
                const parsedErrorText = getParseErrorText(data.responseXML as unknown as Document | null);
                if (parsedErrorText !== PARSED_OK) {
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

            if (!sent) {
                const sourceForLog = data.source as (Element & { id?: string }) | undefined;
                const errorMessage = status + ": "
                    + (serverErrorName ? serverErrorName + " " : "")
                    + data.description
                    + (data.responseCode ? " (HTTP " + data.responseCode + ")" : "")
                    + (sourceForLog ? " [source: " + (sourceForLog.id || sourceForLog) + "]" : "");

                // Example outputs:
                // - httpError: There was an error communicating with the server, status: 404 (HTTP 404) [source: myButton]
                // - serverError: java.lang.NullPointerException fieldName (HTTP 500) [source: myForm]
                // - emptyResponse: An empty response was received from the server. Check server error logs. [source: myButton]

                if (facesGlobal().getProjectStage() === "Development") {
                    alert(errorMessage);
                } else {
                    console.error(errorMessage);
                }

                const warnMessage = "No faces.ajax.addOnError handler registered to handle this error. Register one to customize error handling.";

                if (window.onerror) {
                    const onerrorMessage = errorMessage + " WARNING: " + warnMessage;
                    window.onerror(onerrorMessage, "jakarta.faces:faces.js", 0, 0, new Error(onerrorMessage));
                }

                console.warn(warnMessage);
            }
        };

        /**
         * Event handling callback.
         * Request is assumed to have completed, except in the case of event = 'begin'.
         * @ignore
         */
        const sendEvent = function sendEvent(request: XMLHttpRequest, context: AjaxContext, status: FacesSpec.AjaxEventStatus): void {

            const source = resolveSourceElement(context.sourceid);
            const data: FacesSpec.AjaxEvent = {
                type: "event",
                status,
                ...(source && { source }),
            };

            if (status !== 'begin') {
                copyResponseFields(data, request);
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

        const unescapeHTML = function unescapeHTML(escapedHTML: string): string {
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
            addOnError: function addOnError(callback: FacesSpec.ajax.OnErrorCallback) {
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
            addOnEvent: function addOnEvent(callback: FacesSpec.ajax.OnEventCallback) {
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

            request: function request(source: Element | string, event?: Event | null, options?: FacesSpec.ajax.RequestOptions) {

                const context: AjaxContext & { element?: Element; form?: HTMLFormElement; includesInputFile?: boolean } = {};

                if (isNull(source)) {
                    throw new Error("faces.ajax.request: source not set");
                }
                if (delayHandler) {
                    clearTimeout(delayHandler);
                    delayHandler = null;
                }

                // set up the element based on source
                let element: (Element & { name?: string; id: string }) | null;
                if (typeof source === 'string') {
                    element = document.getElementById(source) as (HTMLElement & { name?: string }) | null;
                } else if (typeof source === 'object') {
                    element = source as Element & { name?: string; id: string };
                } else {
                    throw new Error("faces.ajax.request: source must be object or string");
                }

                if (!element) {
                    throw new Error("faces.ajax.request: source not set");
                }

                // attempt to handle case of name unset
                // this might be true in a badly written composite component
                if (!element.name) {
                    element.name = element.id;
                }

                context.element = element;

                const opts: FacesSpec.ajax.RequestOptions & Record<string, unknown> = (options ?? {}) as FacesSpec.ajax.RequestOptions & Record<string, unknown>;

                // Error handler for this request
                let onerror: FacesSpec.ajax.OnErrorCallback | undefined;

                if (opts.onerror && typeof opts.onerror === 'function') {
                    onerror = opts.onerror;
                } else if (opts.onerror && typeof opts.onerror !== 'function') {
                    throw new Error("faces.ajax.request: Added an onerror callback that was not a function");
                }

                // Event handler for this request
                let onevent: FacesSpec.ajax.OnEventCallback | undefined;

                if (opts.onevent && typeof opts.onevent === 'function') {
                    onevent = opts.onevent;
                } else if (opts.onevent && typeof opts.onevent !== 'function') {
                    throw new Error("faces.ajax.request: Added an onevent callback that was not a function");
                }

                const form = getForm(element);
                if (!form) {
                    throw new Error("faces.ajax.request: Method must be called within a form");
                }

                const viewStateElement = getHiddenStateField(form, VIEW_STATE_PARAM) as HTMLInputElement | null;
                if (!viewStateElement) {
                    throw new Error("faces.ajax.request: Form has no view state element");
                }

                context.form = form;
                context.formId = form.id;

                // Set up additional arguments to be used in the request..
                // Make sure SOURCE_PARAM is set up.
                // If there were "execute" ids specified, make sure we
                // include the identifier of the source element in the
                // "execute" list.  If there were no "execute" ids
                // specified, determine the default.

                const args: Record<string, unknown> = {};

                const namingContainerPrefix = viewStateElement.name.substring(0, viewStateElement.name.indexOf(VIEW_STATE_PARAM));

                args[namingContainerPrefix + SOURCE_PARAM] = element.id;

                if (event && !!event.type) {
                    args[namingContainerPrefix + PARTIAL_EVENT_PARAM] = event.type;
                }

                if ("resetValues" in opts) {
                    args[namingContainerPrefix + PARTIAL_RESET_VALUES_PARAM] = opts.resetValues;
                }

                // do a partial submit only if it is enabled and:
                // 1) option.execute is not defined, eg. <f:ajax />
                // 2) if it is defined it should not contain @form or @all
                const doPartialSubmit = PARTIAL_SUBMIT_ENABLED && (!opts.execute || (!contains(opts.execute, "@form") && !contains(opts.execute, "@all")));

                // If we have 'execute' identifiers:
                // Handle any keywords that may be present.
                // If @none present anywhere, do not send the PARTIAL_EXECUTE_PARAM parameter.
                // The 'execute' and 'render' lists must be space delimited.

                if (opts.execute) {
                    const isNone = contains(opts.execute, "@none");
                    if (!isNone) {
                        const isAll = contains(opts.execute, "@all");
                        if (!isAll) {
                            opts.execute = opts.execute.replace("@this", element.id);
                            opts.execute = opts.execute.replace("@form", form.id);
                            const temp = opts.execute.split(SPACE);
                            if (!temp.includes(element.name!)) {
                                opts.execute = element.name + SPACE + opts.execute;
                            }
                            if (namingContainerPrefix) {
                                opts.execute = namespaceParametersIfNecessary(opts.execute, element.name!, namingContainerPrefix);
                            }
                        } else {
                            opts.execute = "@all";
                        }
                        args[namingContainerPrefix + PARTIAL_EXECUTE_PARAM] = opts.execute;
                    }
                }
                // in case of <f:ajax />
                else {
                    // if id is equals to name then add only one of them to avoid duplicates inside opts.execute
                    opts.execute = (element.name === element.id) ? element.id : element.name + SPACE + element.id;
                    args[namingContainerPrefix + PARTIAL_EXECUTE_PARAM] = opts.execute;
                }

                if (opts.render) {
                    const isNone = contains(opts.render, "@none");
                    if (!isNone) {
                        const isAll = contains(opts.render, "@all");
                        if (!isAll) {
                            opts.render = opts.render.replace("@this", element.id);
                            opts.render = opts.render.replace("@form", form.id);
                            if (namingContainerPrefix) {
                                opts.render = namespaceParametersIfNecessary(opts.render, element.name!, namingContainerPrefix);
                            }
                        } else {
                            opts.render = "@all";
                        }
                        args[namingContainerPrefix + PARTIAL_RENDER_PARAM] = opts.render;
                    }
                }

                // delay value for request execution
                const explicitlyDoNotDelay = ((typeof opts.delay == 'undefined') || (typeof opts.delay == 'string') &&
                    ((opts.delay as string).toLowerCase() === 'none'));
                let delayValue: number;
                if (typeof opts.delay == 'number') {
                    delayValue = opts.delay;
                } else {
                    const converted = parseInt(opts.delay as unknown as string);

                    if (!explicitlyDoNotDelay && isNaN(converted)) {
                        throw new Error('invalid value for delay option: ' + opts.delay);
                    }
                    delayValue = converted;
                }

                // check the "execute" ids to see if any include an input of type "file"
                context.includesInputFile = false;
                let ids = opts.execute!.split(SPACE);

                // if @all -> execute only this form
                if (ids.includes("@all")) ids = [form.id];

                if (ids) {
                    for (const id of ids) {
                        const elem = document.getElementById(id);
                        if (elem) {
                            if (elem.nodeType === Node.ELEMENT_NODE) {
                                if (elem.hasAttribute("type")) {
                                    if (elem.getAttribute("type") === "file") {
                                        context.includesInputFile = true;
                                        break;
                                    }
                                } else {
                                    if (hasInputFileControl(elem as HTMLFormElement)) {
                                        context.includesInputFile = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                // encoded query string to process, eventually with partial submit logic enabled
                const viewState = doPartialSubmit ? getPartialViewState(form, opts.execute) : facesGlobal().getViewState(form);

                // copy all params to args
                const params = (opts.params ?? {}) as Record<string, string | number | boolean>;
                for (const property of Object.keys(params)) {
                    args[namingContainerPrefix + property] = params[property];
                }

                // remove non-passthrough options
                delete opts.execute;
                delete opts.render;
                delete opts.onerror;
                delete opts.onevent;
                delete opts.delay;
                delete opts.resetValues;
                delete opts.params;

                // copy all other options to args (for backwards compatibility on issue 4115)
                for (const property of Object.keys(opts)) {
                    args[namingContainerPrefix + property] = (opts as Record<string, unknown>)[property];
                }

                args[namingContainerPrefix + PARTIAL_AJAX_PARAM] = "true";
                args["method"] = "POST";

                // Determine the posting url
                const encodedUrlField = getEncodedUrlElement(form) as HTMLInputElement | null;
                if (isNull(encodedUrlField) || !encodedUrlField) {
                    args["url"] = form.action;
                } else {
                    args["url"] = encodedUrlField.value;
                }

                const sendRequest = function () {
                    const ajaxEngine = AjaxEngine(context);
                    ajaxEngine.setupArguments(args);
                    ajaxEngine.queryString = viewState;
                    ajaxEngine.context.onevent = onevent;
                    ajaxEngine.context.onerror = onerror;
                    ajaxEngine.context.sourceid = element!.id;
                    ajaxEngine.context.render = (args[namingContainerPrefix + PARTIAL_RENDER_PARAM] as string) || EMPTY;
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
            response: function response(request: XMLHttpRequest, context: AjaxContext) {

                if (!request) {
                    throw new Error("faces.ajax.response: Request parameter is unset");
                }

                // ensure context source is the dom element and not the ID
                // per 14.4.1 of the 2.0 specification.  We're doing it here
                // *before* any errors or events are propagated because the
                // DOM element may be removed after the update has been processed.
                if (typeof context.sourceid === 'string') {
                    const found = document.getElementById(context.sourceid);
                    if (found) context.sourceid = found;
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
                const sep = facesGlobal().separatorchar;
                const namingContainerPrefix = namingContainerId ? (namingContainerId + sep) : EMPTY;
                let responseType: ChildNode | null = partialResponse.firstChild;

                context.namingContainerId = namingContainerId ?? undefined;
                context.namingContainerPrefix = namingContainerPrefix;

                for (const partialResponseChild of Array.from(partialResponse.childNodes)) {
                    if (partialResponseChild.nodeName === "error") {
                        responseType = partialResponseChild;
                        break;
                    }
                }

                if (!responseType) {
                    sendError(request, context, "malformedXML", "No response type found.");
                    return;
                }

                if (responseType.nodeName === "error") { // it's an error
                    let errorName = EMPTY;
                    let errorMessage = EMPTY;

                    let element: ChildNode | null = responseType.firstChild;
                    if (element && element.nodeName === "error-name") {
                        if (null != element.firstChild) {
                            errorName = element.firstChild.nodeValue ?? EMPTY;
                        }
                    }

                    element = responseType.firstChild?.nextSibling ?? null;
                    if (element && element.nodeName === "error-message") {
                        if (null != element.firstChild) {
                            errorMessage = element.firstChild.nodeValue ?? EMPTY;
                        }
                    }
                    sendError(request, context, "serverError", undefined, errorName, errorMessage);
                    sendEvent(request, context, "success");
                    return;
                }


                if (responseType.nodeName === "redirect") {
                    const url = (responseType as Element).getAttribute("url");
                    if (!url) {
                        sendError(request, context, "malformedXML", "<redirect> element is missing the required 'url' attribute.");
                        return;
                    }
                    (window as unknown as { location: string }).location = url;
                    return;
                }


                if (responseType.nodeName !== "changes") {
                    sendError(request, context, "malformedXML", "Top level node must be one of: changes, redirect, error, received: " + responseType.nodeName + " instead.");
                    return;
                }

                try {
                    for (const change of Array.from(responseType.childNodes)) {
                        switch (change.nodeName) {
                            case "update":
                                doUpdate(change as Element, context);
                                break;
                            case "delete":
                                doDelete(change as Element);
                                break;
                            case "insert":
                                doInsert(change as Element);
                                break;
                            case "attributes":
                                doAttributes(change as Element);
                                break;
                            case "eval":
                                doEval(change as Element);
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
                    const message = ex instanceof Error ? ex.message : String(ex);
                    sendError(request, context, "malformedXML", message);
                    return;
                }
                sendEvent(request, context, "success");

            }
        };
})();
