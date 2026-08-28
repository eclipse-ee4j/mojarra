/**
 * Tests for the delegated behavior event attribute bootstrap.
 *
 * The bootstrap runs the script of a `data-mojarra-on*` attribute when the matching event is dispatched at its element.
 * It exists for the `load` and `error` of an element whose fetch of an external resource starts as the element is
 * parsed, which can be dispatched before any script following the element runs, so those handlers cannot be attached
 * to the element afterwards.
 *
 * Only `error` is dispatched here: jsdom does not deliver a `load` dispatched at an element to a window capture
 * listener, where a browser does, so the `load` half of the contract is covered by an integration test instead.
 */

import { loadBehaviorEventBootstrap } from "../test-setup";

beforeAll(() => loadBehaviorEventBootstrap());

beforeEach(() => {
    document.body.innerHTML = "";
    delete (window as unknown as Record<string, unknown>).probe;
});

function appendScript(attributes: Record<string, string>): HTMLScriptElement {
    const script = document.createElement("script");

    for (const [name, value] of Object.entries(attributes)) {
        script.setAttribute(name, value);
    }

    document.body.appendChild(script);
    return script;
}

function probe(): unknown {
    return (window as unknown as Record<string, unknown>).probe;
}

it("runs the handler of the event which was dispatched", () => {
    const element = appendScript({ "data-mojarra-onerror": "window.probe = 'error'" });

    element.dispatchEvent(new Event("error"));

    expect(probe()).toBe("error");
});

it("runs the handler with the element as this and the event as event", () => {
    const element = appendScript({ "data-mojarra-onerror": "window.probe = this.tagName + ':' + event.type" });

    element.dispatchEvent(new Event("error"));

    expect(probe()).toBe("SCRIPT:error");
});

it("runs the handler at most once", () => {
    const element = appendScript({ "data-mojarra-onerror": "window.probe = (window.probe || 0) + 1" });

    element.dispatchEvent(new Event("error"));
    element.dispatchEvent(new Event("error"));

    expect(probe()).toBe(1);
    expect(element.hasAttribute("data-mojarra-onerror")).toBe(false);
});

it("leaves an event which the element has no handler for alone", () => {
    const element = appendScript({ "data-mojarra-onerror": "window.probe = 'error'" });

    element.dispatchEvent(new Event("load"));

    expect(probe()).toBeUndefined();
    expect(element.hasAttribute("data-mojarra-onerror")).toBe(true);
});

it("leaves an element without a handler alone", () => {
    appendScript({ src: "any.js" }).dispatchEvent(new Event("error"));

    expect(probe()).toBeUndefined();
});

it("prevents the default of an event whose handler returns false", () => {
    const element = appendScript({ "data-mojarra-onerror": "return false" });
    const event = new Event("error", { cancelable: true });

    element.dispatchEvent(event);

    expect(event.defaultPrevented).toBe(true);
});

it("does not prevent the default of an event whose handler returns nothing", () => {
    const element = appendScript({ "data-mojarra-onerror": "window.probe = 'error'" });
    const event = new Event("error", { cancelable: true });

    element.dispatchEvent(event);

    expect(event.defaultPrevented).toBe(false);
});

it("leaves an event whose target is not an element alone", () => {
    window.dispatchEvent(new Event("error"));
    document.dispatchEvent(new Event("error"));

    expect(probe()).toBeUndefined();
});

it("leaves no window slots behind when the handler throws", () => {
    // The throw is what this asserts against, and jsdom reports it as an uncaught error, so the report is silenced
    // to keep it out of the test log where it reads like a failure.
    const consoleError = jest.spyOn(console, "error").mockImplementation(() => undefined);

    try {
        const element = appendScript({ "data-mojarra-onerror": "throw new Error('boom')" });

        element.dispatchEvent(new Event("error"));

        // The per-invocation slots must be gone; the install marker is permanent by design.
        expect(Object.keys(window).filter(key => /^__mojarraDelegated\d/.test(key))).toEqual([]);
    }
    finally {
        consoleError.mockRestore();
    }
});

it("runs a handler which ends in a line comment", () => {
    const element = appendScript({ "data-mojarra-onerror": "window.probe = 'error'; // and no more" });

    element.dispatchEvent(new Event("error"));

    expect(probe()).toBe("error");
});

it("keeps the result of the outer handler when it dispatches a nested delegated event", () => {
    const inner = appendScript({ "data-mojarra-onerror": "window.probe = 'inner'" });
    inner.id = "inner";
    const outer = appendScript({ "data-mojarra-onerror": "document.getElementById('inner').dispatchEvent(new Event('error')); return false" });
    const event = new Event("error", { cancelable: true });

    outer.dispatchEvent(event);

    expect(probe()).toBe("inner");
    expect(event.defaultPrevented).toBe(true);
});

it("installs its listeners only once however often it is loaded", () => {
    const addEventListener = jest.spyOn(window, "addEventListener");

    try {
        loadBehaviorEventBootstrap();

        const events = addEventListener.mock.calls.map(call => call[0]);
        expect(events).not.toContain("load");
        expect(events).not.toContain("error");
    }
    finally {
        addEventListener.mockRestore();
    }
});
