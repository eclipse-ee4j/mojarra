/*
 * Bundle entry for the delegated behavior event attribute bootstrap.
 *
 * Rendered inline, ahead of every element which can carry a `data-mojarra-on*` attribute, and therefore deliberately
 * kept apart from the faces.js bundle: it must run before the first resource element is parsed, which a `<script src>`
 * anywhere in the document cannot be relied upon to do.
 *
 * The `load` and `error` of an element whose fetch of an external resource starts as the element is parsed can be
 * dispatched before any script following the element runs, so a handler for those cannot be attached to the element
 * afterwards. It is carried by the attribute instead and run from here. The listeners capture at the window because a
 * resource load event does not bubble.
 */

import { executeScriptWithNonce, getHead, getNonce, WindowAsDict } from "./faces/dom";

const ATTRIBUTE_PREFIX = "data-mojarra-on";
const SLOT_PREFIX = "__mojarraDelegated";
const INSTALLED_SLOT = SLOT_PREFIX + "Installed";

const nonce = getNonce();

// Distinct per invocation, so that a handler which synchronously dispatches another delegated event does not have its
// own slots deleted by the nested run before it reads its result back.
let slotSequence = 0;

/**
 * Runs the script of the delegated behavior event attribute matching the given event, if its target carries one. The
 * script is evaluated through a nonce bearing script element rather than through `eval()`, so that it also runs under
 * a Content Security Policy, and is removed first so that it runs at most once.
 */
function run(event: Event): void {
    const element = event.target as Element;
    const name = ATTRIBUTE_PREFIX + event.type;

    if (!element || element.nodeType !== Node.ELEMENT_NODE || !element.hasAttribute(name)) {
        return;
    }

    const script = element.getAttribute(name);
    element.removeAttribute(name);

    const w = window as unknown as WindowAsDict;
    const slot = SLOT_PREFIX + slotSequence++;
    const thisSlot = slot + "This";
    const eventSlot = slot + "Event";
    const resultSlot = slot + "Result";
    let result: unknown = undefined;

    try {
        w[thisSlot] = element;
        w[eventSlot] = event;
        // The handler body is put on its own line, as a handler ending in a line comment would otherwise comment out
        // the closing brace of the function it is wrapped in.
        executeScriptWithNonce(
            getHead(),
            "window." + resultSlot + " = (function(event) {\n" + script + "\n}).call(window." + thisSlot + ", window." + eventSlot + ");",
            nonce);
        result = w[resultSlot];
    }
    finally {
        delete w[thisSlot];
        delete w[eventSlot];
        delete w[resultSlot];
    }

    if (result === false) {
        event.preventDefault();
    }
}

const global = window as unknown as WindowAsDict;

// An ajax response carrying a delegated attribute brings the bootstrap along again, so the listeners are installed
// at most once per document however often that happens.
if (!global[INSTALLED_SLOT]) {
    global[INSTALLED_SLOT] = true;
    window.addEventListener("load", run, true);
    window.addEventListener("error", run, true);
}
