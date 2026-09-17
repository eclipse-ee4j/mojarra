///
/// Copyright (c) 2026 Contributors to the Eclipse Foundation.
///
/// This program and the accompanying materials are made available under the
/// terms of the Eclipse Public License v. 2.0, which is available at
/// http://www.eclipse.org/legal/epl-2.0.
///
/// This Source Code may also be made available under the following Secondary
/// Licenses when the conditions for such availability set forth in the
/// Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
/// version 2 with the GNU Classpath Exception, which is available at
/// https://www.gnu.org/software/classpath/license.html.
///
/// SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
///

/**
 * Implementation of the `faces.util` namespace from the Jakarta Faces JavaScript API.
 * @see api/.../faces.d.ts namespace `faces.util`
 */

import type FacesSpec from "../../../../../faces/api/src/main/resources/META-INF/resources/jakarta.faces/faces";
import { getHead, getNonce, executeScriptWithNonce, type WindowAsDict } from "./dom";

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
    const w = window as unknown as WindowAsDict;

    for (let i = 2; i < arguments.length; i++) {
        const facesChainThis = "__facesChainThis" + i;
        const facesChainEvent = "__facesChainEvent" + i;
        const facesChainResult = "__facesChainResult" + i;

        let result = undefined;

        try {
            w[facesChainThis] = thisArg;
            w[facesChainEvent] = event;
            const script = "window." + facesChainResult + " = (function(event) { " + arguments[i] + " }).call(window." +
                facesChainThis + ", window." + facesChainEvent + ");";
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
