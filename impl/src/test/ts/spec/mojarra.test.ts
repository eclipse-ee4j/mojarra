/**
 * Tests for the `mojarra` namespace exposed by faces.js.
 *
 * The mojarra namespace contains Mojarra-specific helper functions used by
 * server-side renderers (CommandLinkRenderer, AjaxBehaviorRenderer, etc.).
 * These are NOT part of the Jakarta Faces JavaScript spec but are public
 * API of the Mojarra implementation.
 */

import { loadFacesJs } from "../test-setup";

beforeAll(() => loadFacesJs());

const moj = () => mojarra as Record<string, unknown>;

// ---- Mock XMLHttpRequest (needed for mojarra.ab which calls faces.ajax.request) ----

interface MockXHRInstance {
    method: string | null;
    url: string | null;
    async: boolean;
    requestHeaders: Record<string, string>;
    body: string | null;
    readyState: number;
    status: number;
    responseText: string;
    responseXML: Document | null;
    onreadystatechange: ((this: XMLHttpRequest, ev: Event) => void) | null;
    open(method: string, url: string, async?: boolean): void;
    setRequestHeader(name: string, value: string): void;
    send(body?: string | null): void;
    respond(status: number, responseText: string, responseXML?: string): void;
}

let xhrInstances: MockXHRInstance[];
let OriginalXHR: typeof XMLHttpRequest;

function installMockXHR(): void {
    xhrInstances = [];
    OriginalXHR = window.XMLHttpRequest;

    (window as unknown as Record<string, unknown>).XMLHttpRequest = function MockXHR(this: MockXHRInstance) {
        this.method = null;
        this.url = null;
        this.async = true;
        this.requestHeaders = {};
        this.body = null;
        this.readyState = 0;
        this.status = 0;
        this.responseText = "";
        this.responseXML = null;
        this.onreadystatechange = null;

        this.open = function (method: string, url: string, async = true) {
            this.method = method;
            this.url = url;
            this.async = async;
            this.readyState = 1;
        };

        this.setRequestHeader = function (name: string, value: string) {
            this.requestHeaders[name] = value;
        };

        this.send = function (body?: string | null) {
            this.body = body ?? null;
        };

        this.respond = function (status: number, responseText: string, responseXML?: string) {
            this.status = status;
            this.responseText = responseText;
            if (responseXML) {
                this.responseXML = new DOMParser().parseFromString(responseXML, "application/xml");
            }
            this.readyState = 4;
            if (this.onreadystatechange) {
                this.onreadystatechange.call(this as unknown as XMLHttpRequest, new Event("readystatechange"));
            }
        };

        xhrInstances.push(this);
    } as unknown as typeof XMLHttpRequest;
}

function drainXHRQueue(): void {
    for (const xhr of xhrInstances) {
        if (xhr.readyState !== 4) {
            xhr.status = 200;
            xhr.responseText = "";
            xhr.responseXML = new DOMParser().parseFromString(
                '<?xml version="1.0" encoding="UTF-8"?><partial-response id="j_id1"><changes></changes></partial-response>',
                "application/xml"
            );
            xhr.readyState = 4;
            if (xhr.onreadystatechange) {
                xhr.onreadystatechange.call(xhr as unknown as XMLHttpRequest, new Event("readystatechange"));
            }
        }
    }
}

function uninstallMockXHR(): void {
    drainXHRQueue();
    window.XMLHttpRequest = OriginalXHR;
}

function lastXHR(): MockXHRInstance {
    return xhrInstances[xhrInstances.length - 1];
}

// ---- DOM helpers ----

function createForm(formId = "testForm"): HTMLFormElement {
    const form = document.createElement("form");
    form.id = formId;
    form.method = "post";
    form.action = "/test/action";

    const viewState = Object.assign(document.createElement("input"), {
        type: "hidden",
        name: "jakarta.faces.ViewState",
        value: "testViewState123",
    });
    form.appendChild(viewState);

    document.body.appendChild(form);
    return form;
}

// ---- Namespace structure ----

describe("mojarra namespace", () => {
    const EXPECTED_MEMBERS: Record<string, string> = {
        dpf: "function",
        apf: "function",
        cljs: "function",
        facescbk: "function",
        ab: "function",
        ael: "function",
        l: "function",
    };

    test("exposes exactly the expected public members (excluding caches)", () => {
        // projectStageCache is dynamically set, so filter it out
        const actualKeys = Object.keys(mojarra)
            .filter(k => k !== "projectStageCache")
            .sort();
        const expectedKeys = Object.keys(EXPECTED_MEMBERS).sort();
        expect(actualKeys).toEqual(expectedKeys);
    });

    test("each member has the expected type", () => {
        for (const [key, expectedType] of Object.entries(EXPECTED_MEMBERS)) {
            expect(typeof moj()[key]).toBe(expectedType);
        }
    });
});

// ---- dpf: delete parameters from form ----

describe("mojarra.dpf", () => {
    let form: HTMLFormElement;

    beforeEach(() => {
        form = createForm();
    });

    afterEach(() => {
        form?.remove();
    });

    test("removes parameters previously added via adp array", () => {
        const p1 = Object.assign(document.createElement("input"), { type: "hidden", name: "p1", value: "v1" });
        const p2 = Object.assign(document.createElement("input"), { type: "hidden", name: "p2", value: "v2" });
        form.appendChild(p1);
        form.appendChild(p2);
        (form as unknown as Record<string, unknown>).adp = [p1, p2];

        (moj().dpf as Function)(form);

        expect(form.querySelector("input[name='p1']")).toBeNull();
        expect(form.querySelector("input[name='p2']")).toBeNull();
    });

    test("does not remove elements not in adp array", () => {
        const kept = Object.assign(document.createElement("input"), { type: "hidden", name: "kept", value: "yes" });
        form.appendChild(kept);
        (form as unknown as Record<string, unknown>).adp = [];

        (moj().dpf as Function)(form);

        expect(form.querySelector("input[name='kept']")).toBe(kept);
    });

    test("handles null adp gracefully", () => {
        (form as unknown as Record<string, unknown>).adp = null;
        expect(() => (moj().dpf as Function)(form)).not.toThrow();
    });

    test("throws when form has no adp property", () => {
        // adp is undefined — impl checks `adp !== null`, undefined !== null is true,
        // then tries to read adp.length which throws TypeError
        expect(() => (moj().dpf as Function)(form)).toThrow();
    });

    test("removes all parameters from a large adp array", () => {
        const params: HTMLInputElement[] = [];
        for (let i = 0; i < 10; i++) {
            const p = Object.assign(document.createElement("input"), { type: "hidden", name: `p${i}`, value: `v${i}` });
            form.appendChild(p);
            params.push(p);
        }
        (form as unknown as Record<string, unknown>).adp = params;

        (moj().dpf as Function)(form);

        for (let i = 0; i < 10; i++) {
            expect(form.querySelector(`input[name='p${i}']`)).toBeNull();
        }
    });

    test("preserves ViewState input after cleanup", () => {
        const p = Object.assign(document.createElement("input"), { type: "hidden", name: "extra", value: "val" });
        form.appendChild(p);
        (form as unknown as Record<string, unknown>).adp = [p];

        (moj().dpf as Function)(form);

        expect(form.querySelector("input[name='jakarta.faces.ViewState']")).not.toBeNull();
    });
});

// ---- apf: add parameters to form ----

describe("mojarra.apf", () => {
    let form: HTMLFormElement;

    beforeEach(() => {
        form = createForm();
    });

    afterEach(() => {
        form?.remove();
    });

    test("adds hidden inputs to the form", () => {
        (moj().apf as Function)(form, { foo: "bar", baz: "qux" });

        const foo = form.querySelector("input[name='foo']") as HTMLInputElement;
        const baz = form.querySelector("input[name='baz']") as HTMLInputElement;
        expect(foo).not.toBeNull();
        expect(foo.type).toBe("hidden");
        expect(foo.value).toBe("bar");
        expect(baz).not.toBeNull();
        expect(baz.value).toBe("qux");
    });

    test("stores added elements in form.adp array", () => {
        (moj().apf as Function)(form, { a: "1", b: "2" });

        const adp = (form as unknown as Record<string, unknown>).adp as HTMLInputElement[];
        expect(adp).toHaveLength(2);
        expect(adp[0].name).toBe("a");
        expect(adp[1].name).toBe("b");
    });

    test("replaces previous adp array", () => {
        (form as unknown as Record<string, unknown>).adp = ["old"];

        (moj().apf as Function)(form, { x: "1" });

        const adp = (form as unknown as Record<string, unknown>).adp as HTMLInputElement[];
        expect(adp).toHaveLength(1);
        expect(adp[0].name).toBe("x");
    });

    test("handles empty parameter object", () => {
        (moj().apf as Function)(form, {});

        const adp = (form as unknown as Record<string, unknown>).adp as HTMLInputElement[];
        expect(adp).toHaveLength(0);
    });

    test("skips inherited properties (hasOwnProperty check)", () => {
        const pvp = Object.create({ inherited: "skip" });
        pvp.own = "keep";

        (moj().apf as Function)(form, pvp);

        expect(form.querySelector("input[name='own']")).not.toBeNull();
        expect(form.querySelector("input[name='inherited']")).toBeNull();
    });

    test("handles special characters in parameter values", () => {
        (moj().apf as Function)(form, { "key": "a&b=c" });

        const input = form.querySelector("input[name='key']") as HTMLInputElement;
        expect(input.value).toBe("a&b=c");
    });

    test("handles empty string value", () => {
        (moj().apf as Function)(form, { empty: "" });

        const input = form.querySelector("input[name='empty']") as HTMLInputElement;
        expect(input).not.toBeNull();
        expect(input.value).toBe("");
    });
});

// ---- dpf + apf round-trip ----

describe("mojarra.dpf + mojarra.apf round-trip", () => {
    let form: HTMLFormElement;

    beforeEach(() => {
        form = createForm();
    });

    afterEach(() => {
        form?.remove();
    });

    test("apf then dpf restores form to original state", () => {
        const originalChildCount = form.children.length;

        (moj().apf as Function)(form, { temp1: "a", temp2: "b" });
        expect(form.children.length).toBe(originalChildCount + 2);

        (moj().dpf as Function)(form);
        expect(form.children.length).toBe(originalChildCount);
    });

    test("multiple apf/dpf cycles work correctly", () => {
        const originalChildCount = form.children.length;

        for (let i = 0; i < 3; i++) {
            (moj().apf as Function)(form, { [`key${i}`]: `val${i}` });
            expect(form.children.length).toBe(originalChildCount + 1);
            (moj().dpf as Function)(form);
            expect(form.children.length).toBe(originalChildCount);
        }
    });
});

// ---- cljs: command link JavaScript submit ----

describe("mojarra.cljs", () => {
    let form: HTMLFormElement;

    beforeEach(() => {
        form = createForm();
    });

    afterEach(() => {
        form?.remove();
    });

    test("adds parameters, submits, then removes parameters", () => {
        // Track submit via click on injected submit button
        let submitted = false;
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            submitted = true;
        });

        (moj().cljs as Function)(form, { action: "save" }, null);

        // Parameters should be removed after submit
        expect(form.querySelector("input[name='action']")).toBeNull();
        // The temporary submit button should also be removed
        expect(form.querySelector("input[type='submit']")).toBeNull();
    });

    test("sets form target when target argument is provided", () => {
        form.addEventListener("submit", (e) => e.preventDefault());
        form.target = "";

        (moj().cljs as Function)(form, {}, "_blank");

        // target should be restored to original value after submit
        expect(form.target).toBe("");
    });

    test("preserves original form target after submit", () => {
        form.addEventListener("submit", (e) => e.preventDefault());
        form.target = "_self";

        (moj().cljs as Function)(form, {}, "_blank");

        expect(form.target).toBe("_self");
    });

    test("does not change form target when target is null/falsy", () => {
        form.addEventListener("submit", (e) => e.preventDefault());
        form.target = "_parent";

        (moj().cljs as Function)(form, {}, null);

        expect(form.target).toBe("_parent");
    });

    test("does not change form target when target is empty string", () => {
        form.addEventListener("submit", (e) => e.preventDefault());
        form.target = "_top";

        (moj().cljs as Function)(form, {}, "");

        expect(form.target).toBe("_top");
    });

    test("cleans up parameters even when no target is specified", () => {
        form.addEventListener("submit", (e) => e.preventDefault());

        (moj().cljs as Function)(form, { p1: "v1", p2: "v2" }, null);

        expect(form.querySelector("input[name='p1']")).toBeNull();
        expect(form.querySelector("input[name='p2']")).toBeNull();
    });

    test("removes temporary submit button after submission", () => {
        form.addEventListener("submit", (e) => e.preventDefault());

        (moj().cljs as Function)(form, {}, null);

        // No leftover submit input
        const submits = form.querySelectorAll("input[type='submit']");
        expect(submits.length).toBe(0);
    });

    test("preserves existing form children", () => {
        form.addEventListener("submit", (e) => e.preventDefault());
        const existing = Object.assign(document.createElement("input"), { type: "text", name: "existing", value: "keep" });
        form.appendChild(existing);

        (moj().cljs as Function)(form, { temp: "val" }, null);

        expect(form.querySelector("input[name='existing']")).toBe(existing);
    });

    test("works with empty parameter object", () => {
        form.addEventListener("submit", (e) => e.preventDefault());

        expect(() => (moj().cljs as Function)(form, {}, null)).not.toThrow();
    });
});

// ---- facescbk: faces callback ----

describe("mojarra.facescbk", () => {
    test("calls function with provided this and event", () => {
        const thisObj = { id: "test" };
        const evt = new Event("click");
        let receivedThis: unknown;
        let receivedEvent: unknown;

        const fn = function (this: unknown, e: unknown) {
            receivedThis = this;
            receivedEvent = e;
        };

        (moj().facescbk as Function)(fn, thisObj, evt);

        expect(receivedThis).toBe(thisObj);
        expect(receivedEvent).toBe(evt);
    });

    test("returns the value returned by the function", () => {
        const fn = function () { return 42; };
        const result = (moj().facescbk as Function)(fn, null, null);
        expect(result).toBe(42);
    });

    test("returns undefined when function returns nothing", () => {
        const fn = function () { /* no return */ };
        const result = (moj().facescbk as Function)(fn, null, null);
        expect(result).toBeUndefined();
    });

    test("passes null this correctly", () => {
        let receivedThis: unknown = "sentinel";
        const fn = function (this: unknown) { receivedThis = this; };

        (moj().facescbk as Function)(fn, null, null);

        // In non-strict mode, null this becomes window
        expect(receivedThis).not.toBe("sentinel");
    });

    test("passes DOM element as this", () => {
        const el = document.createElement("div");
        let receivedThis: unknown;
        const fn = function (this: unknown) { receivedThis = this; };

        (moj().facescbk as Function)(fn, el, null);

        expect(receivedThis).toBe(el);
    });

    test("propagates exceptions from the function", () => {
        const fn = function () { throw new Error("callback error"); };

        expect(() => (moj().facescbk as Function)(fn, null, null)).toThrow("callback error");
    });

    test("passes null event when event is null", () => {
        let receivedEvent: unknown = "sentinel";
        const fn = function (_e: unknown) { receivedEvent = _e; };

        (moj().facescbk as Function)(fn, null, null);

        expect(receivedEvent).toBeNull();
    });
});

// ---- ab: Ajax Behavior shorthand ----

describe("mojarra.ab", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        form = createForm();
        button = document.createElement("button");
        button.id = "testButton";
        button.type = "button";
        form.appendChild(button);
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("triggers faces.ajax.request", () => {
        (moj().ab as Function)(button, null, null, null, null, null);

        expect(xhrInstances.length).toBe(1);
    });

    test("passes behavior event name in options", () => {
        (moj().ab as Function)(button, null, "action", null, null, null);

        const body = lastXHR().body!;
        expect(body).toContain("jakarta.faces.behavior.event=action");
    });

    test("passes execute list in options", () => {
        (moj().ab as Function)(button, null, null, "@this", null, null);

        const body = lastXHR().body!;
        // @this is resolved by faces.ajax.request to the source element id
        expect(body).toContain("jakarta.faces.partial.execute=testButton");
    });

    test("passes render list in options", () => {
        (moj().ab as Function)(button, null, null, null, "@all", null);

        const body = lastXHR().body!;
        expect(body).toContain("jakarta.faces.partial.render=%40all");
    });

    test("passes all parameters together", () => {
        (moj().ab as Function)(button, null, "click", "form:input", "form:output", null);

        const body = lastXHR().body!;
        expect(body).toContain("jakarta.faces.behavior.event=click");
        // execute value includes source element id prepended by faces.ajax.request
        expect(body).toContain("form%3Ainput");
        expect(body).toContain("jakarta.faces.partial.render=form%3Aoutput");
    });

    test("creates empty options object when op is null", () => {
        // Should not throw when op is null
        expect(() => (moj().ab as Function)(button, null, null, null, null, null)).not.toThrow();
    });

    test("creates empty options object when op is undefined", () => {
        expect(() => (moj().ab as Function)(button, null, null, null, null, undefined)).not.toThrow();
    });

    test("merges into existing options object", () => {
        const op = { "resetValues": true } as Record<string, unknown>;

        (moj().ab as Function)(button, null, "change", "@this", "@form", op);

        const body = lastXHR().body!;
        expect(body).toContain("jakarta.faces.behavior.event=change");
        // @this resolves to source element id, @form resolves to form id
        expect(body).toContain("jakarta.faces.partial.execute=testButton");
        expect(body).toContain("jakarta.faces.partial.render=testForm");
        expect(body).toContain("jakarta.faces.partial.resetValues=true");
    });

    test("does not set behavior event when name is null", () => {
        (moj().ab as Function)(button, null, null, null, null, null);

        const body = lastXHR().body!;
        expect(body).not.toContain("jakarta.faces.behavior.event");
    });

    test("does not set behavior event when name is empty string", () => {
        (moj().ab as Function)(button, null, "", null, null, null);

        const body = lastXHR().body!;
        expect(body).not.toContain("jakarta.faces.behavior.event");
    });

    test("does not add execute option when ex is null", () => {
        // ab does not set execute in op, but faces.ajax.request always adds a default
        const op = {} as Record<string, unknown>;
        (moj().ab as Function)(button, null, null, null, null, op);

        expect(op["execute"]).toBeUndefined();
    });

    test("does not add render option when re is null", () => {
        const op = {} as Record<string, unknown>;
        (moj().ab as Function)(button, null, null, null, null, op);

        expect(op["render"]).toBeUndefined();
    });

    test("passes event object to faces.ajax.request", () => {
        const evt = new Event("click");

        (moj().ab as Function)(button, evt, null, null, null, null);

        // If the request was made, the event was passed (we can't inspect it directly,
        // but the request should succeed without error)
        expect(xhrInstances.length).toBe(1);
    });

    test("accepts string source (element id)", () => {
        expect(() => (moj().ab as Function)("testButton", null, null, null, null, null)).not.toThrow();
        expect(xhrInstances.length).toBe(1);
    });

    test("overrides behavior event in options object", () => {
        const op = { "jakarta.faces.behavior.event": "original" } as Record<string, unknown>;

        (moj().ab as Function)(button, null, "overridden", null, null, op);

        // ab sets the property directly on the options object before passing to faces.ajax.request
        expect(op["jakarta.faces.behavior.event"]).toBe("overridden");
    });
});

// ---- ael: add event listener ----

describe("mojarra.ael", () => {
    let div: HTMLDivElement;
    const w = () => window as unknown as Record<string, unknown>;

    beforeEach(() => {
        div = document.createElement("div");
        div.id = "ael-test";
        document.body.appendChild(div);
        w().__aelTrace = [];
    });

    afterEach(() => {
        div?.remove();
        delete w().__aelTrace;
    });

    test("attaches click event listener that runs a single script", () => {
        (moj().ael as Function)("ael-test", "click", ["window.__aelTrace.push('clicked')"]);
        div.click();
        expect(w().__aelTrace).toEqual(["clicked"]);
    });

    test("attaches non-click event listener", () => {
        (moj().ael as Function)("ael-test", "focus", ["window.__aelTrace.push('focused')"]);
        div.dispatchEvent(new Event("focus"));
        expect(w().__aelTrace).toEqual(["focused"]);
    });

    test("exposes the DOM event to the script as 'event'", () => {
        (moj().ael as Function)("ael-test", "click", ["window.__aelTrace.push(event.type)"]);
        div.click();
        expect(w().__aelTrace).toEqual(["click"]);
    });

    test("binds 'this' to the element when chain captures it", () => {
        (moj().ael as Function)("ael-test", "click", ["window.__aelTrace.push(this.id)"]);
        div.click();
        expect(w().__aelTrace).toEqual(["ael-test"]);
    });

    test("multiple scripts on same registration run in insertion order", () => {
        (moj().ael as Function)("ael-test", "click", [
            "window.__aelTrace.push('a')",
            "window.__aelTrace.push('b')",
            "window.__aelTrace.push('c')",
        ]);
        div.click();
        expect(w().__aelTrace).toEqual(["a", "b", "c"]);
    });

    test("a script returning false short-circuits the remaining scripts", () => {
        (moj().ael as Function)("ael-test", "click", [
            "window.__aelTrace.push('a')",
            "window.__aelTrace.push('b'); return false",
            "window.__aelTrace.push('never')",
        ]);
        div.click();
        expect(w().__aelTrace).toEqual(["a", "b"]);
    });

    test("a script returning false calls event.preventDefault()", () => {
        (moj().ael as Function)("ael-test", "click", ["return false"]);
        const ev = new Event("click", { cancelable: true });
        div.dispatchEvent(ev);
        expect(ev.defaultPrevented).toBe(true);
    });

    test("a script returning true does not call event.preventDefault()", () => {
        (moj().ael as Function)("ael-test", "click", ["return true"]);
        const ev = new Event("click", { cancelable: true });
        div.dispatchEvent(ev);
        expect(ev.defaultPrevented).toBe(false);
    });

    test("a script returning falsy non-false does not call event.preventDefault()", () => {
        (moj().ael as Function)("ael-test", "click", ["return 0"]);
        const ev = new Event("click", { cancelable: true });
        div.dispatchEvent(ev);
        expect(ev.defaultPrevented).toBe(false);
    });

    test("multiple registrations on same element and event chain independently", () => {
        (moj().ael as Function)("ael-test", "click", ["window.__aelTrace.push(1)"]);
        (moj().ael as Function)("ael-test", "click", ["window.__aelTrace.push(2)"]);
        div.click();
        expect(w().__aelTrace).toEqual([1, 2]);
    });

    test("registrations for different events are independent", () => {
        (moj().ael as Function)("ael-test", "click", ["window.__aelTrace.push('c')"]);
        (moj().ael as Function)("ael-test", "mouseover", ["window.__aelTrace.push('m')"]);

        div.click();
        expect(w().__aelTrace).toEqual(["c"]);

        div.dispatchEvent(new Event("mouseover"));
        expect(w().__aelTrace).toEqual(["c", "m"]);
    });

    test("throws when element id does not exist", () => {
        expect(() => (moj().ael as Function)("nonexistent", "click", ["window.__aelTrace.push('never')"])).toThrow();
    });

    test("script is not run before the event fires", () => {
        (moj().ael as Function)("ael-test", "click", ["window.__aelTrace.push('clicked')"]);
        expect(w().__aelTrace).toEqual([]);
    });

    test("works with custom events", () => {
        (moj().ael as Function)("ael-test", "myCustomEvent", ["window.__aelTrace.push(event.detail)"]);
        div.dispatchEvent(new CustomEvent("myCustomEvent", { detail: "payload" }));
        expect(w().__aelTrace).toEqual(["payload"]);
    });

    test("works with input elements", () => {
        const input = document.createElement("input");
        input.id = "ael-input-test";
        document.body.appendChild(input);

        (moj().ael as Function)("ael-input-test", "change", ["window.__aelTrace.push('changed')"]);
        input.dispatchEvent(new Event("change"));
        expect(w().__aelTrace).toEqual(["changed"]);

        input.remove();
    });

    test("empty scripts array attaches a no-op listener", () => {
        (moj().ael as Function)("ael-test", "click", []);
        const ev = new Event("click", { cancelable: true });
        div.dispatchEvent(ev);
        expect(ev.defaultPrevented).toBe(false);
        expect(w().__aelTrace).toEqual([]);
    });
});

// ---- l: window onload ----

describe("mojarra.l", () => {
    test("executes callback when document is already complete", () => {
        // jsdom sets readyState to "complete"
        let called = false;
        (moj().l as Function)(() => { called = true; });

        // setTimeout is used, so callback is async
        expect(called).toBe(false);
    });

    test("callback runs asynchronously via setTimeout when document is complete", (done) => {
        let called = false;
        (moj().l as Function)(() => {
            called = true;
            expect(called).toBe(true);
            done();
        });
    });

    test("executes multiple callbacks in order", (done) => {
        const order: number[] = [];

        (moj().l as Function)(() => { order.push(1); });
        (moj().l as Function)(() => { order.push(2); });
        (moj().l as Function)(() => {
            order.push(3);
            expect(order).toEqual([1, 2, 3]);
            done();
        });
    });

    test("uses addEventListener when readyState is not complete", () => {
        const originalReadyState = Object.getOwnPropertyDescriptor(Document.prototype, "readyState");
        Object.defineProperty(document, "readyState", { value: "loading", configurable: true });

        const spy = jest.spyOn(window, "addEventListener");
        const callback = jest.fn();

        (moj().l as Function)(callback);

        expect(spy).toHaveBeenCalledWith("load", callback);

        spy.mockRestore();
        if (originalReadyState) {
            Object.defineProperty(document, "readyState", originalReadyState);
        } else {
            delete (document as unknown as Record<string, unknown>)["readyState"];
        }
    });
});

// ---- projectStageCache ----

describe("mojarra.projectStageCache", () => {
    afterEach(() => {
        delete mojarra.projectStageCache;
    });

    test("is set after calling faces.getProjectStage()", () => {
        delete mojarra.projectStageCache;
        (faces.getProjectStage as Function)();
        expect(mojarra.projectStageCache).toBeDefined();
    });

    test("is used as cache on subsequent calls", () => {
        mojarra.projectStageCache = "UnitTest";
        expect((faces.getProjectStage as Function)()).toBe("UnitTest");
    });

    test("can be deleted to force re-evaluation", () => {
        mojarra.projectStageCache = "Cached";
        delete mojarra.projectStageCache;

        const result = (faces.getProjectStage as Function)();
        // Without a faces.js script tag with stage param, defaults to Production
        expect(result).toBe("Production");
    });
});
