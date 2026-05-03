/**
 * Internal DOM helpers shared across the `faces` namespace modules.
 * Not part of the public Jakarta Faces JavaScript API.
 */

/**
 * `Window` augmented with a string-keyed index signature, for the few places
 * the impl resolves a global by name (e.g. `window[fn]` lookups in
 * `faces.push.init` and the `__facesChain*` slots in `faces.util.chain`).
 */
export type WindowAsDict = Window & { [key: string]: unknown };

/** Get the head from the document. */
export const getHead = (): HTMLElement => {
    return document.head || document.getElementsByTagName("head")[0] || document.documentElement;
};

/**
 * Get the nonce from the faces.js script element for CSP support.
 * Captured at load time via document.currentScript for robustness, with DOM query fallback.
 */
export const getNonce: () => string | undefined = (() => {
    const loadTimeNonce = document.currentScript ? document.currentScript.nonce : undefined;
    return () => {
        if (loadTimeNonce) {
            return loadTimeNonce;
        }
        const thisScript = document.querySelector<HTMLScriptElement>("script[src*='jakarta.faces.resource/faces.js']");
        return thisScript ? thisScript.nonce : undefined;
    };
})();

/** Execute a script string in the given head element with the given CSP nonce. */
export const executeScriptWithNonce = (head: HTMLElement, script: string, nonce: string | undefined): void => {
    const scriptNode = document.createElement("script");
    scriptNode.nonce = nonce as string;
    scriptNode.text = script;
    head.appendChild(scriptNode);
    head.removeChild(scriptNode);
};

/** If a string is given, look up the element by id; otherwise return the element as-is. */
export const getElemById = function getElemById(elementOrId: Element | string): Element | null {
    return typeof elementOrId === "string" ? document.getElementById(elementOrId) : elementOrId;
};

/**
 * Find a child element by its `name` attribute, or null if not found.
 * @param element the DOM base element
 * @param name the value of the name attribute
 */
export const getElementByName = function (element: Element | Document, name: string): Element | null {
    return element.querySelector("[name='" + name + "']");
};

/**
 * Find an input element inside a form identified by name attribute, or null if not found.
 * Accesses by indexed property first (HTMLFormElement maps name → element/RadioNodeList),
 * falls back to a querySelector by name.
 */
export const getFormInputElementByName = function (form: HTMLFormElement, inputElementName: string): Element | null {
    return inputElementName in form ? (form as any)[inputElementName] : getElementByName(form, inputElementName);
};

/** True if one of the elements contains a child whose `name` attribute equals `name`. */
export const containsNamedChild = function (elements: Element[], name: string): boolean {
    return elements.some(elem => !!getElementByName(elem, name));
};
