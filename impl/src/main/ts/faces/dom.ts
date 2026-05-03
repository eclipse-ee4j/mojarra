/**
 * Internal DOM helpers shared across the `faces` namespace modules.
 * Not part of the public Jakarta Faces JavaScript API.
 */

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
