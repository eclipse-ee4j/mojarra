/**
 * Implementation of the `faces.util` namespace from the Jakarta Faces JavaScript API.
 * @see api/.../faces.d.ts namespace `faces.util`
 */

import type { faces as FacesSpec } from "../../../../../faces/api/src/main/resources/META-INF/resources/jakarta.faces/faces";
import { getHead, getNonce, executeScriptWithNonce } from "./dom";

type GlobalDictWindow = Window & { [key: string]: unknown };

/**
 * A varargs function that invokes an arbitrary number of scripts.
 * If any script in the chain returns false, the chain is short-circuited
 * and subsequent scripts are not invoked. Any number of scripts may be
 * specified after the `event` argument.
 */
export const chain: typeof FacesSpec.util.chain = function chain(source, event) {

    if (arguments.length < 3) {
        return true;
    }

    const thisArg = (typeof source === "object") ? source : null;

    const head = getHead();
    const nonce = getNonce();
    const w = window as unknown as GlobalDictWindow;

    for (let i = 2; i < arguments.length; i++) {
        const facesChainThis = "__facesChainThis" + i;
        const facesChainEvent = "__facesChainEvent" + i;
        const facesChainResult = "__facesChainResult" + i;

        let result = undefined;

        try {
            w[facesChainThis] = thisArg;
            w[facesChainEvent] = event;
            const script = "window." + facesChainResult + " = (function(event) { " + arguments[i] + " }).call(window." + facesChainThis + ", window." + facesChainEvent + ");";
            executeScriptWithNonce(head, script, nonce);
            result = w[facesChainResult];
        }
        finally {
            delete w[facesChainThis];
            delete w[facesChainEvent];
            delete w[facesChainResult];
        }

        if (result === false) {
            return false;
        }
    }
    return true;
};
