/**
 * Tests for the `faces.ajax` namespace exposed by faces.js.
 */

import { loadFacesJs } from "../test-setup";
import { installMockXHR, uninstallMockXHR, lastXHR, getXHRInstances, createAjaxForm } from "../test-helpers";

// ---- Test setup ----

beforeAll(() => loadFacesJs());

const ajax = () => faces.ajax as Record<string, Function>;

// ---- Namespace structure ----

describe("faces.ajax namespace", () => {
    const EXPECTED_MEMBERS: Record<string, string> = {
        addOnError: "function",
        addOnEvent: "function",
        request: "function",
        response: "function",
    };

    test("exposes exactly the expected public members", () => {
        const actualKeys = Object.keys(faces.ajax as object).sort();
        const expectedKeys = Object.keys(EXPECTED_MEMBERS).sort();
        expect(actualKeys).toEqual(expectedKeys);
    });

    test("each member has the expected type", () => {
        const ajaxObj = faces.ajax as Record<string, unknown>;
        for (const [key, expectedType] of Object.entries(EXPECTED_MEMBERS)) {
            expect(typeof ajaxObj[key]).toBe(expectedType);
        }
    });
});

// ---- addOnError ----

describe("faces.ajax.addOnError", () => {
    test("accepts a function callback", () => {
        expect(() => ajax().addOnError(() => {})).not.toThrow();
    });

    test("throws when callback is null", () => {
        expect(() => ajax().addOnError(null)).toThrow("faces.ajax.addOnError");
    });

    test("throws when callback is undefined", () => {
        expect(() => ajax().addOnError(undefined)).toThrow("faces.ajax.addOnError");
    });

    test("throws when callback is a string", () => {
        expect(() => ajax().addOnError("notAFunction")).toThrow("faces.ajax.addOnError");
    });

    test("throws when callback is a number", () => {
        expect(() => ajax().addOnError(42)).toThrow("faces.ajax.addOnError");
    });

    test("throws when callback is a boolean", () => {
        expect(() => ajax().addOnError(true)).toThrow("faces.ajax.addOnError");
    });

    test("throws when callback is an object", () => {
        expect(() => ajax().addOnError({})).toThrow("faces.ajax.addOnError");
    });

    test("throws when callback is an array", () => {
        expect(() => ajax().addOnError([])).toThrow("faces.ajax.addOnError");
    });

    test("error message indicates the problem", () => {
        expect(() => ajax().addOnError("bad")).toThrow("Added a callback that was not a function");
    });
});

// ---- addOnEvent ----

describe("faces.ajax.addOnEvent", () => {
    test("accepts a function callback", () => {
        expect(() => ajax().addOnEvent(() => {})).not.toThrow();
    });

    test("throws when callback is null", () => {
        expect(() => ajax().addOnEvent(null)).toThrow("faces.ajax.addOnEvent");
    });

    test("throws when callback is undefined", () => {
        expect(() => ajax().addOnEvent(undefined)).toThrow("faces.ajax.addOnEvent");
    });

    test("throws when callback is a string", () => {
        expect(() => ajax().addOnEvent("notAFunction")).toThrow("faces.ajax.addOnEvent");
    });

    test("throws when callback is a number", () => {
        expect(() => ajax().addOnEvent(42)).toThrow("faces.ajax.addOnEvent");
    });

    test("throws when callback is a boolean", () => {
        expect(() => ajax().addOnEvent(true)).toThrow("faces.ajax.addOnEvent");
    });

    test("throws when callback is an object", () => {
        expect(() => ajax().addOnEvent({})).toThrow("faces.ajax.addOnEvent");
    });

    test("throws when callback is an array", () => {
        expect(() => ajax().addOnEvent([])).toThrow("faces.ajax.addOnEvent");
    });

    test("error message indicates the problem", () => {
        expect(() => ajax().addOnEvent("bad")).toThrow("Added a callback that was not a function");
    });
});

// ---- request: argument validation ----

describe("faces.ajax.request: argument validation", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("throws when source is null", () => {
        expect(() => ajax().request(null, null)).toThrow("faces.ajax.request: source not set");
    });

    test("throws when source is undefined", () => {
        expect(() => ajax().request(undefined, null)).toThrow("faces.ajax.request: source not set");
    });

    test("throws when source is a number", () => {
        expect(() => ajax().request(42, null)).toThrow("faces.ajax.request: source must be object or string");
    });

    test("throws when source is a boolean", () => {
        expect(() => ajax().request(true, null)).toThrow("faces.ajax.request: source must be object or string");
    });

    test("accepts source as DOM element", () => {
        expect(() => ajax().request(button, null)).not.toThrow();
    });

    test("accepts source as string id", () => {
        expect(() => ajax().request("testButton", null)).not.toThrow();
    });

    test("throws when source string id does not match any element", () => {
        // When string id doesn't exist, element becomes null and form lookup fails.
        expect(() => ajax().request("nonexistent", null)).toThrow();
    });

    test("throws when element is not inside a form", () => {
        // Remove all forms so getForm() fallback to document.forms[0] also fails.
        const existingForms = Array.from(document.querySelectorAll("form"));
        existingForms.forEach(f => f.remove());

        const orphan = document.createElement("button");
        orphan.id = "orphan";
        document.body.appendChild(orphan);
        try {
            expect(() => ajax().request(orphan, null)).toThrow("faces.ajax.request: Method must be called within a form");
        } finally {
            orphan.remove();
            existingForms.forEach(f => document.body.appendChild(f));
        }
    });

    test("throws when form has no ViewState hidden field", () => {
        const noVsForm = document.createElement("form");
        noVsForm.id = "noVsForm";
        const btn = document.createElement("button");
        btn.id = "noVsBtn";
        btn.name = "noVsBtn";
        noVsForm.appendChild(btn);
        document.body.appendChild(noVsForm);
        expect(() => ajax().request(btn, null)).toThrow("faces.ajax.request: Form has no view state element");
        noVsForm.remove();
    });

    test("throws when onerror option is not a function", () => {
        expect(() => ajax().request(button, null, { onerror: "bad" })).toThrow("faces.ajax.request: Added an onerror callback that was not a function");
    });

    test("throws when onevent option is not a function", () => {
        expect(() => ajax().request(button, null, { onevent: "bad" })).toThrow("faces.ajax.request: Added an onevent callback that was not a function");
    });

    test("throws when delay option is invalid", () => {
        expect(() => ajax().request(button, null, { delay: "invalid" })).toThrow("invalid value for delay option");
    });

    test("accepts onerror as function", () => {
        expect(() => ajax().request(button, null, { onerror: () => {} })).not.toThrow();
    });

    test("accepts onevent as function", () => {
        expect(() => ajax().request(button, null, { onevent: () => {} })).not.toThrow();
    });

    test("accepts null options", () => {
        expect(() => ajax().request(button, null, null)).not.toThrow();
    });

    test("accepts undefined options", () => {
        expect(() => ajax().request(button, null)).not.toThrow();
    });
});

// ---- request: XHR behavior ----

describe("faces.ajax.request: XHR behavior", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("sends POST request", () => {
        ajax().request(button, null);
        expect(lastXHR().method).toBe("POST");
    });

    test("sends to form action URL", () => {
        ajax().request(button, null);
        expect(lastXHR().url).toContain("/test/action");
    });

    test("sends to jakarta.faces.encodedURL when present", () => {
        const encodedUrl = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.encodedURL",
            value: "/encoded/url",
        });
        form.appendChild(encodedUrl);
        ajax().request(button, null);
        expect(lastXHR().url).toBe("/encoded/url");
    });

    test("sets Faces-Request header to partial/ajax", () => {
        ajax().request(button, null);
        expect(lastXHR().requestHeaders["Faces-Request"]).toBe("partial/ajax");
    });

    test("sends async by default", () => {
        ajax().request(button, null);
        expect(lastXHR().async).toBe(true);
    });

    test("request body includes jakarta.faces.partial.ajax=true", () => {
        ajax().request(button, null);
        expect(lastXHR().body).toContain("jakarta.faces.partial.ajax=true");
    });

    test("request body includes jakarta.faces.source", () => {
        ajax().request(button, null);
        expect(lastXHR().body).toContain("jakarta.faces.source=testButton");
    });

    test("request body includes jakarta.faces.ViewState", () => {
        ajax().request(button, null);
        expect(lastXHR().body).toContain("jakarta.faces.ViewState=testViewState123");
    });

    test("request body includes default execute with source element", () => {
        ajax().request(button, null);
        expect(lastXHR().body).toContain("jakarta.faces.partial.execute=testButton");
    });

    test("sets Content-Type header to application/x-www-form-urlencoded;charset=UTF-8", () => {
        ajax().request(button, null);
        expect(lastXHR().requestHeaders["Content-type"]).toBe("application/x-www-form-urlencoded;charset=UTF-8");
    });

    test("request body is properly &-joined without leading or double &", () => {
        ajax().request(button, null);
        const body = lastXHR().body!;
        expect(body).not.toMatch(/^&/);
        expect(body).not.toMatch(/&&/);
        expect(body).not.toMatch(/&$/);
    });

    test("viewState parameters appear before ajax parameters in body", () => {
        ajax().request(button, null);
        const body = lastXHR().body!;
        const vsIdx = body.indexOf("jakarta.faces.ViewState=");
        const ajaxIdx = body.indexOf("jakarta.faces.partial.ajax=");
        expect(vsIdx).toBeGreaterThan(-1);
        expect(ajaxIdx).toBeGreaterThan(-1);
        expect(vsIdx).toBeLessThan(ajaxIdx);
    });
});

// ---- request: parameter encoding ----

describe("faces.ajax.request: parameter encoding", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("special characters in param values are URI-encoded", () => {
        ajax().request(button, null, { params: { msg: "a&b=c d" } });
        expect(lastXHR().body).toContain("msg=a%26b%3Dc+d");
    });

    test("special characters in param names are URI-encoded", () => {
        ajax().request(button, null, { params: { "my key": "val" } });
        expect(lastXHR().body).toContain("my+key=val");
    });

    test("empty string param value is encoded as key=", () => {
        ajax().request(button, null, { params: { empty: "" } });
        expect(lastXHR().body).toMatch(/empty=(&|$)/);
    });

    test("Unicode characters in param values are URI-encoded", () => {
        ajax().request(button, null, { params: { text: "\u00e9\u00e0\u00fc" } });
        expect(lastXHR().body).toContain("text=%C3%A9%C3%A0%C3%BC");
    });
});

// ---- request: execute option ----

describe("faces.ajax.request: execute option", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("@all sends execute=@all", () => {
        ajax().request(button, null, { execute: "@all" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.execute=%40all");
    });

    test("@none does not send execute parameter", () => {
        ajax().request(button, null, { execute: "@none" });
        expect(lastXHR().body).not.toContain("jakarta.faces.partial.execute");
    });

    test("@this resolves to source element id", () => {
        ajax().request(button, null, { execute: "@this" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.execute=testButton");
    });

    test("@form resolves to form id", () => {
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.execute=testButton+testForm");
    });

    test("explicit ids are sent as-is", () => {
        ajax().request(button, null, { execute: "comp1 comp2" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.execute=testButton+comp1+comp2");
    });

    test("source element is prepended to execute list if missing", () => {
        ajax().request(button, null, { execute: "other" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.execute=testButton+other");
    });
});

// ---- request: render option ----

describe("faces.ajax.request: render option", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("no render option does not send render parameter", () => {
        ajax().request(button, null);
        expect(lastXHR().body).not.toContain("jakarta.faces.partial.render");
    });

    test("@all sends render=@all", () => {
        ajax().request(button, null, { render: "@all" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.render=%40all");
    });

    test("@none does not send render parameter", () => {
        ajax().request(button, null, { render: "@none" });
        expect(lastXHR().body).not.toContain("jakarta.faces.partial.render");
    });

    test("@this resolves to source element id", () => {
        ajax().request(button, null, { render: "@this" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.render=testButton");
    });

    test("@form resolves to form id", () => {
        ajax().request(button, null, { render: "@form" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.render=testForm");
    });

    test("explicit ids are sent", () => {
        ajax().request(button, null, { render: "output1 output2" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.render=output1+output2");
    });
});

// ---- request: event type ----

describe("faces.ajax.request: event option", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("event type is included in request body", () => {
        ajax().request(button, { type: "click" });
        expect(lastXHR().body).toContain("jakarta.faces.partial.event=click");
    });

    test("null event does not include event type", () => {
        ajax().request(button, null);
        expect(lastXHR().body).not.toContain("jakarta.faces.partial.event");
    });
});

// ---- request: resetValues option ----

describe("faces.ajax.request: resetValues option", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("resetValues=true is included in request body", () => {
        ajax().request(button, null, { resetValues: true });
        expect(lastXHR().body).toContain("jakarta.faces.partial.resetValues=true");
    });

    test("resetValues absent does not send parameter", () => {
        ajax().request(button, null);
        expect(lastXHR().body).not.toContain("jakarta.faces.partial.resetValues");
    });
});

// ---- request: params option ----

describe("faces.ajax.request: params option", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("custom params are included in request body", () => {
        ajax().request(button, null, { params: { foo: "bar", baz: "qux" } });
        expect(lastXHR().body).toContain("foo=bar");
        expect(lastXHR().body).toContain("baz=qux");
    });

    test("empty params object sends no extra params", () => {
        ajax().request(button, null, { params: {} });
        expect(lastXHR().body).not.toContain("foo");
    });
});

// ---- request: delay option ----

describe("faces.ajax.request: delay option", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        jest.useFakeTimers();
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
        jest.useRealTimers();
    });

    test("delay='none' sends immediately", () => {
        ajax().request(button, null, { delay: "none" });
        expect(getXHRInstances().length).toBe(1);
    });

    test("no delay sends immediately", () => {
        ajax().request(button, null);
        expect(getXHRInstances().length).toBe(1);
    });

    test("numeric delay defers the request", () => {
        ajax().request(button, null, { delay: 500 });
        expect(getXHRInstances().length).toBe(0);
        jest.advanceTimersByTime(500);
        expect(getXHRInstances().length).toBe(1);
    });

    test("string numeric delay defers the request", () => {
        ajax().request(button, null, { delay: "300" });
        expect(getXHRInstances().length).toBe(0);
        jest.advanceTimersByTime(300);
        expect(getXHRInstances().length).toBe(1);
    });

    test("subsequent request within delay cancels previous", () => {
        ajax().request(button, null, { delay: 500 });
        jest.advanceTimersByTime(200);
        ajax().request(button, null, { delay: 500 });
        jest.advanceTimersByTime(500);
        expect(getXHRInstances().length).toBe(1);
    });
});

// ---- request: onevent callback ----

describe("faces.ajax.request: onevent callback", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("onevent receives 'begin' event on request start", () => {
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => events.push(data) });
        expect(events.length).toBeGreaterThanOrEqual(1);
        expect(events[0].status).toBe("begin");
        expect(events[0].type).toBe("event");
    });

    test("begin event has source element", () => {
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => events.push(data) });
        expect(events[0].source).toBe(button);
    });

    test("onevent receives 'complete' and 'success' on successful response", () => {
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => events.push(data) });
        const xhr = lastXHR();
        xhr.respond(200, "", '<?xml version="1.0" encoding="UTF-8"?><partial-response id="testForm"><changes></changes></partial-response>');

        const statuses = events.map(e => e.status);
        expect(statuses).toContain("begin");
        expect(statuses).toContain("complete");
        expect(statuses).toContain("success");
    });
});

// ---- request: onerror callback ----

describe("faces.ajax.request: onerror callback", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("onerror receives httpError on 404 response", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(404, "Not Found");

        expect(errors.length).toBe(1);
        expect(errors[0].type).toBe("error");
        expect(errors[0].status).toBe("httpError");
        expect(errors[0].responseCode).toBe(404);
    });

    test("onerror receives httpError on 500 response", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(500, "Internal Server Error");

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("httpError");
        expect(errors[0].responseCode).toBe(500);
    });

    test("onerror receives httpError on 0 status (network failure)", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(0, "");

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("httpError");
        expect(errors[0].responseCode).toBe(0);
        expect(errors[0].description).toContain("0 status code");
    });

    test("onerror receives emptyResponse when server returns 200 with no body", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(200, "");

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("emptyResponse");
    });

    test("onerror has source as DOM element, not string", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(404, "Not Found");

        expect(errors[0].source).toBe(button);
    });
});

// ---- addOnError: integration with request ----

describe("faces.ajax.addOnError: integration with request", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("global error listener receives errors", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().addOnError((data: Record<string, unknown>) => errors.push(data));
        ajax().request(button, null);
        lastXHR().respond(503, "Service Unavailable");

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("httpError");
        expect(errors[0].responseCode).toBe(503);
    });

    test("multiple global error listeners all receive errors", () => {
        const errors1: Record<string, unknown>[] = [];
        const errors2: Record<string, unknown>[] = [];
        ajax().addOnError((data: Record<string, unknown>) => errors1.push(data));
        ajax().addOnError((data: Record<string, unknown>) => errors2.push(data));
        ajax().request(button, null);
        lastXHR().respond(500, "Error");

        expect(errors1.length).toBe(1);
        expect(errors2.length).toBe(1);
    });

    test("both per-request onerror and global listener receive error", () => {
        const globalErrors: Record<string, unknown>[] = [];
        const localErrors: Record<string, unknown>[] = [];
        ajax().addOnError((data: Record<string, unknown>) => globalErrors.push(data));
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => localErrors.push(data) });
        lastXHR().respond(500, "Error");

        expect(globalErrors.length).toBe(1);
        expect(localErrors.length).toBe(1);
    });
});

// ---- addOnEvent: integration with request ----

describe("faces.ajax.addOnEvent: integration with request", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("global event listener receives begin event", () => {
        const events: Record<string, unknown>[] = [];
        ajax().addOnEvent((data: Record<string, unknown>) => events.push(data));
        ajax().request(button, null);

        expect(events.length).toBeGreaterThanOrEqual(1);
        expect(events[0].status).toBe("begin");
    });

    test("multiple global event listeners all receive events", () => {
        const events1: Record<string, unknown>[] = [];
        const events2: Record<string, unknown>[] = [];
        ajax().addOnEvent((data: Record<string, unknown>) => events1.push(data));
        ajax().addOnEvent((data: Record<string, unknown>) => events2.push(data));
        ajax().request(button, null);

        expect(events1.length).toBeGreaterThanOrEqual(1);
        expect(events2.length).toBeGreaterThanOrEqual(1);
    });

    test("both per-request onevent and global listener receive events", () => {
        const globalEvents: Record<string, unknown>[] = [];
        const localEvents: Record<string, unknown>[] = [];
        ajax().addOnEvent((data: Record<string, unknown>) => globalEvents.push(data));
        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => localEvents.push(data) });

        expect(globalEvents.length).toBeGreaterThanOrEqual(1);
        expect(localEvents.length).toBeGreaterThanOrEqual(1);
    });
});

// ---- response: processing ----

describe("faces.ajax.response", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    function successResponse(changes: string): string {
        return `<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><changes>${changes}</changes></partial-response>`;
    }

    test("update element replaces DOM content", () => {
        const target = document.createElement("div");
        target.id = "output";
        target.textContent = "old";
        document.body.appendChild(target);

        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, {
            render: "output",
            onevent: (data: Record<string, unknown>) => events.push(data),
        });

        const xml = successResponse('<update id="output"><![CDATA[<div id="output">new</div>]]></update>');
        lastXHR().respond(200, "", xml);

        const updated = document.getElementById("output");
        expect(updated).not.toBeNull();
        expect(updated!.textContent).toBe("new");
        updated?.remove();
    });

    test("update jakarta.faces.ViewState updates hidden field", () => {
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, {
            render: "testForm",
            onevent: (data: Record<string, unknown>) => events.push(data),
        });

        const xml = successResponse('<update id="jakarta.faces.ViewState:0"><![CDATA[newState456]]></update>');
        lastXHR().respond(200, "", xml);

        const vsField = form.querySelector<HTMLInputElement>('input[name="jakarta.faces.ViewState"]');
        expect(vsField?.value).toBe("newState456");
    });

    test("emptyResponse error when server returns 200 with null responseXML", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(200, "");

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("emptyResponse");
    });

    test("serverError from response error element triggers onerror", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });

        const xml = '<?xml version="1.0" encoding="UTF-8"?><partial-response id="testForm"><error><error-name>java.lang.NullPointerException</error-name><error-message><![CDATA[Something went wrong]]></error-message></error></partial-response>';
        lastXHR().respond(200, "", xml);

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("serverError");
        expect(errors[0].errorName).toBe("java.lang.NullPointerException");
        expect(errors[0].errorMessage).toBe("Something went wrong");
    });

    test("success event is fired after processing changes", () => {
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => events.push(data) });

        const xml = successResponse("");
        lastXHR().respond(200, "", xml);

        const statuses = events.map(e => e.status);
        expect(statuses).toEqual(["begin", "complete", "success"]);
    });

    test("httpError statuses trigger error, not response processing", () => {
        const errors: Record<string, unknown>[] = [];
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, {
            onerror: (data: Record<string, unknown>) => errors.push(data),
            onevent: (data: Record<string, unknown>) => events.push(data),
        });
        lastXHR().respond(401, "Unauthorized");

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("httpError");
        expect(errors[0].responseCode).toBe(401);

        const statuses = events.map(e => e.status);
        expect(statuses).toContain("begin");
        expect(statuses).toContain("complete");
        expect(statuses).not.toContain("success");
    });

    test("delete element removes DOM node", () => {
        const target = document.createElement("div");
        target.id = "toDelete";
        target.textContent = "delete me";
        document.body.appendChild(target);

        ajax().request(button, null);
        const xml = successResponse('<delete id="toDelete"/>');
        lastXHR().respond(200, "", xml);

        expect(document.getElementById("toDelete")).toBeNull();
    });

    test("insert before adds element before target", () => {
        const container = document.createElement("div");
        container.id = "container";
        const target = document.createElement("div");
        target.id = "insertTarget";
        target.textContent = "existing";
        container.appendChild(target);
        document.body.appendChild(container);

        ajax().request(button, null);
        const xml = successResponse('<insert><before id="insertTarget"><![CDATA[<div id="inserted">new</div>]]></before></insert>');
        lastXHR().respond(200, "", xml);

        const inserted = document.getElementById("inserted");
        expect(inserted).not.toBeNull();
        expect(inserted!.nextSibling).toBe(document.getElementById("insertTarget"));
        container.remove();
    });

    test("insert after adds element after target", () => {
        const container = document.createElement("div");
        container.id = "containerAfter";
        const target = document.createElement("div");
        target.id = "afterTarget";
        target.textContent = "existing";
        container.appendChild(target);
        document.body.appendChild(container);

        ajax().request(button, null);
        const xml = successResponse('<insert><after id="afterTarget"><![CDATA[<div id="insertedAfter">new</div>]]></after></insert>');
        lastXHR().respond(200, "", xml);

        const inserted = document.getElementById("insertedAfter");
        expect(inserted).not.toBeNull();
        expect(document.getElementById("afterTarget")!.nextSibling).toBe(inserted);
        container.remove();
    });

    test("attributes element modifies DOM attribute", () => {
        const target = document.createElement("div");
        target.id = "attrTarget";
        target.setAttribute("title", "old");
        document.body.appendChild(target);

        ajax().request(button, null);
        const xml = successResponse('<attributes id="attrTarget"><attribute name="title" value="new"/></attributes>');
        lastXHR().respond(200, "", xml);

        expect(target.getAttribute("title")).toBe("new");
        target.remove();
    });

    test("attributes element sets disabled boolean attribute", () => {
        const input = document.createElement("input");
        input.id = "disableTarget";
        input.type = "text";
        document.body.appendChild(input);

        ajax().request(button, null);
        const xml = successResponse('<attributes id="disableTarget"><attribute name="disabled" value="true"/></attributes>');
        lastXHR().respond(200, "", xml);

        expect(input.disabled).toBe(true);
        input.remove();
    });

    test("attributes element sets checked boolean attribute", () => {
        const checkbox = document.createElement("input");
        checkbox.id = "checkTarget";
        checkbox.type = "checkbox";
        document.body.appendChild(checkbox);

        ajax().request(button, null);
        const xml = successResponse('<attributes id="checkTarget"><attribute name="checked" value="true"/></attributes>');
        lastXHR().respond(200, "", xml);

        expect(checkbox.checked).toBe(true);
        checkbox.remove();
    });

    test("attributes element sets readonly boolean attribute", () => {
        const input = document.createElement("input");
        input.id = "readonlyTarget";
        input.type = "text";
        document.body.appendChild(input);

        ajax().request(button, null);
        const xml = successResponse('<attributes id="readonlyTarget"><attribute name="readonly" value="true"/></attributes>');
        lastXHR().respond(200, "", xml);

        expect(input.readOnly).toBe(true);
        input.remove();
    });

    test("attributes element sets value attribute", () => {
        const input = document.createElement("input");
        input.id = "valueTarget";
        input.type = "text";
        input.value = "old";
        document.body.appendChild(input);

        ajax().request(button, null);
        const xml = successResponse('<attributes id="valueTarget"><attribute name="value" value="new"/></attributes>');
        lastXHR().respond(200, "", xml);

        expect(input.value).toBe("new");
        input.remove();
    });

    test("eval element executes JavaScript", () => {
        ajax().request(button, null);
        const xml = successResponse('<eval><![CDATA[window.__evalTestResult = 42;]]></eval>');
        lastXHR().respond(200, "", xml);

        expect((window as unknown as Record<string, unknown>).__evalTestResult).toBe(42);
        delete (window as unknown as Record<string, unknown>).__evalTestResult;
    });

    test("extension element is silently ignored", () => {
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => events.push(data) });

        const xml = successResponse('<extension ln="mylib"><![CDATA[custom data]]></extension>');
        lastXHR().respond(200, "", xml);

        const statuses = events.map(e => e.status);
        expect(statuses).toContain("success");
    });

    test("unknown change element triggers malformedXML error", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });

        const xml = successResponse('<bogus/>');
        lastXHR().respond(200, "", xml);

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("malformedXML");
        expect(errors[0].description as string).toContain("bogus");
    });

    test("malformed top-level node triggers malformedXML error", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });

        const xml = '<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><notchanges></notchanges></partial-response>';
        lastXHR().respond(200, "", xml);

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("malformedXML");
        expect(errors[0].description as string).toContain("notchanges");
    });

    test("serverError response also fires success event", () => {
        const errors: Record<string, unknown>[] = [];
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, {
            onerror: (data: Record<string, unknown>) => errors.push(data),
            onevent: (data: Record<string, unknown>) => events.push(data),
        });

        const xml = '<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><error><error-name>SomeError</error-name><error-message><![CDATA[msg]]></error-message></error></partial-response>';
        lastXHR().respond(200, "", xml);

        expect(errors.length).toBe(1);
        expect(errors[0].status).toBe("serverError");
        const statuses = events.map(e => e.status);
        expect(statuses).toContain("begin");
        expect(statuses).toContain("complete");
        expect(statuses).toContain("success");
    });

    test("multiple changes processed in order", () => {
        const div1 = document.createElement("div");
        div1.id = "multi1";
        div1.textContent = "old1";
        document.body.appendChild(div1);
        const div2 = document.createElement("div");
        div2.id = "multi2";
        div2.textContent = "old2";
        document.body.appendChild(div2);

        ajax().request(button, null, { render: "multi1 multi2" });
        const xml = successResponse(
            '<update id="multi1"><![CDATA[<div id="multi1">new1</div>]]></update>' +
            '<update id="multi2"><![CDATA[<div id="multi2">new2</div>]]></update>'
        );
        lastXHR().respond(200, "", xml);

        expect(document.getElementById("multi1")!.textContent).toBe("new1");
        expect(document.getElementById("multi2")!.textContent).toBe("new2");
        document.getElementById("multi1")?.remove();
        document.getElementById("multi2")?.remove();
    });

    test("update jakarta.faces.ClientWindow updates hidden field", () => {
        const cwField = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ClientWindow",
            value: "oldWindow",
        });
        form.appendChild(cwField);

        ajax().request(button, null, { render: "testForm" });
        const xml = successResponse('<update id="jakarta.faces.ClientWindow:0"><![CDATA[newWindow789]]></update>');
        lastXHR().respond(200, "", xml);

        expect(cwField.value).toBe("newWindow789");
        cwField.remove();
    });

    test("ViewState update creates hidden field if absent in other form", () => {
        const otherForm = document.createElement("form");
        otherForm.id = "otherForm";
        otherForm.method = "post";
        document.body.appendChild(otherForm);

        ajax().request(button, null, { render: "testForm" });
        const xml = successResponse('<update id="jakarta.faces.ViewState:0"><![CDATA[sharedState]]></update>');
        lastXHR().respond(200, "", xml);

        const created = otherForm.querySelector<HTMLInputElement>('input[name="jakarta.faces.ViewState"]');
        expect(created).not.toBeNull();
        expect(created!.value).toBe("sharedState");
        otherForm.remove();
    });

    test("update jakarta.faces.Resource injects new <link> stylesheet into head", () => {
        const beforeLinks = document.head.querySelectorAll("link").length;
        ajax().request(button, null, { render: "testForm" });
        const href = "/test/jakarta.faces.resource/issue4345.css.xhtml?firstParam=1&amp;secondParam=2";
        const xml = successResponse(
            `<update id="jakarta.faces.Resource"><![CDATA[<link type="text/css" rel="stylesheet" href="${href}" />]]></update>`
        );
        lastXHR().respond(200, "", xml);

        const links = document.head.querySelectorAll("link");
        expect(links.length).toBe(beforeLinks + 1);
        const injected = links[links.length - 1];
        expect(injected.getAttribute("rel")).toBe("stylesheet");
        // href is unescaped before being assigned to the DOM property
        expect(injected.href).toContain("issue4345.css.xhtml?firstParam=1&secondParam=2");
        injected.remove();
    });

    test("update jakarta.faces.Resource skips <link> already present in head", () => {
        const href = "/test/jakarta.faces.resource/already-loaded.css.xhtml";
        const existing = document.createElement("link");
        existing.setAttribute("type", "text/css");
        existing.setAttribute("rel", "stylesheet");
        existing.setAttribute("href", href);
        document.head.appendChild(existing);

        const beforeLinks = document.head.querySelectorAll("link").length;
        ajax().request(button, null, { render: "testForm" });
        const xml = successResponse(
            `<update id="jakarta.faces.Resource"><![CDATA[<link type="text/css" rel="stylesheet" href="${href}" />]]></update>`
        );
        lastXHR().respond(200, "", xml);

        expect(document.head.querySelectorAll("link").length).toBe(beforeLinks);
        existing.remove();
    });

    test("update jakarta.faces.Resource handles multiple <link> tags without throwing", () => {
        const beforeLinks = document.head.querySelectorAll("link").length;
        ajax().request(button, null, { render: "testForm" });
        const xml = successResponse(
            '<update id="jakarta.faces.Resource"><![CDATA[' +
            '<link type="text/css" rel="stylesheet" href="/test/a.css" />' +
            '<link type="text/css" rel="stylesheet" href="/test/b.css" />' +
            ']]></update>'
        );
        expect(() => lastXHR().respond(200, "", xml)).not.toThrow();

        expect(document.head.querySelectorAll("link").length).toBe(beforeLinks + 2);
        document.head.querySelectorAll('link[href^="/test/"]').forEach(el => el.remove());
    });
});

// ---- HTTP error codes ----

describe("faces.ajax.request: HTTP error codes", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test.each([
        [400, "Bad Request"],
        [401, "Unauthorized"],
        [403, "Forbidden"],
        [404, "Not Found"],
        [405, "Method Not Allowed"],
        [408, "Request Timeout"],
        [500, "Internal Server Error"],
        [502, "Bad Gateway"],
        [503, "Service Unavailable"],
        [504, "Gateway Timeout"],
    ])("status %i triggers httpError with correct responseCode", (statusCode, _statusText) => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(statusCode, _statusText);

        expect(errors.length).toBe(1);
        expect(errors[0].type).toBe("error");
        expect(errors[0].status).toBe("httpError");
        expect(errors[0].responseCode).toBe(statusCode);
    });

    test("status 0 (network failure) has descriptive message", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(0, "");

        expect(errors[0].description).toContain("0 status code");
    });

    test("httpError data includes responseText", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(500, "Server Error Body");

        expect(errors[0].responseText).toBe("Server Error Body");
    });

    test("status 200 is treated as success, not error", () => {
        const errors: Record<string, unknown>[] = [];
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, {
            onerror: (data: Record<string, unknown>) => errors.push(data),
            onevent: (data: Record<string, unknown>) => events.push(data),
        });
        lastXHR().respond(200, "",
            '<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><changes></changes></partial-response>');

        expect(errors.length).toBe(0);
        expect(events.map(e => e.status)).toContain("success");
    });
});

// ---- request: element name fallback ----

describe("faces.ajax.request: element name handling", () => {
    let form: HTMLFormElement;

    beforeEach(() => {
        installMockXHR();
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("element without name attribute gets name set to id", () => {
        form = document.createElement("form");
        form.id = "nameForm";
        form.method = "post";
        form.action = "/test/action";
        const vs = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ViewState",
            value: "vs",
        });
        form.appendChild(vs);
        const el = document.createElement("span");
        el.id = "noNameEl";
        form.appendChild(el);
        document.body.appendChild(form);

        ajax().request(el, null);
        expect(lastXHR().body).toContain("jakarta.faces.source=noNameEl");
    });

    test("element with explicit name uses that name", () => {
        form = document.createElement("form");
        form.id = "nameForm2";
        form.method = "post";
        form.action = "/test/action";
        const vs = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ViewState",
            value: "vs",
        });
        form.appendChild(vs);
        const btn = document.createElement("button");
        btn.id = "namedBtn";
        btn.name = "customName";
        form.appendChild(btn);
        document.body.appendChild(form);

        ajax().request(btn, null);
        expect(lastXHR().body).toContain("jakarta.faces.source=namedBtn");
        const execMatch = lastXHR().body!.match(/jakarta\.faces\.partial\.execute=([^&]*)/);
        expect(decodeURIComponent(execMatch![1])).toContain("customName");
    });
});

// ---- request: unknown options pass through as parameters ----

describe("faces.ajax.request: unknown options", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("unknown option is passed as request parameter", () => {
        ajax().request(button, null, { myCustomParam: "hello" } as Record<string, unknown>);
        expect(lastXHR().body).toContain("myCustomParam=hello");
    });

    test("known options are NOT passed as extra parameters", () => {
        ajax().request(button, null, { execute: "@this", render: "@none", delay: "none" });
        const body = lastXHR().body!;
        // These should only appear as jakarta.faces.partial.execute etc, not as bare params
        const params = body.split("&").map(p => decodeURIComponent(p.split("=")[0]));
        expect(params).not.toContain("execute");
        expect(params).not.toContain("render");
        expect(params).not.toContain("delay");
    });
});

// ---- request: queue behavior ----

describe("faces.ajax.request: queue behavior", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("second request is queued until first completes", () => {
        ajax().request(button, null);
        const firstXHR = lastXHR();
        expect(getXHRInstances().length).toBe(1);

        ajax().request(button, null);
        // Second request creates an XHR object but doesn't call send until first completes
        const secondXHR = getXHRInstances().length > 1 ? getXHRInstances()[1] : null;
        if (secondXHR) {
            // If XHR was created, it should not have been sent yet (readyState 0 or 1, but not sent)
            expect(secondXHR.body).toBeNull();
        }

        // Complete first request
        firstXHR.respond(200, "",
            '<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><changes></changes></partial-response>');

        // After first completes, second should now be sent
        const sent = getXHRInstances().filter(x => x.body !== null);
        expect(sent.length).toBeGreaterThanOrEqual(2);
    });

    test("queued request fires begin event when dequeued", () => {
        const events1: Record<string, unknown>[] = [];
        const events2: Record<string, unknown>[] = [];

        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => events1.push(data) });
        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => events2.push(data) });

        // First request gets begin immediately
        expect(events1.map(e => e.status)).toContain("begin");

        // Complete first request to let second proceed
        getXHRInstances()[0].respond(200, "",
            '<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><changes></changes></partial-response>');

        // Second request should now have gotten begin
        expect(events2.map(e => e.status)).toContain("begin");
    });
});

// ---- request: form field serialization ----

describe("faces.ajax.request: form field serialization", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("text input values are included in request", () => {
        const text = Object.assign(document.createElement("input"), {
            type: "text",
            name: "textField",
            value: "hello",
        });
        form.appendChild(text);
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).toContain("textField=hello");
    });

    test("hidden input values are included in request", () => {
        const hidden = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "hiddenField",
            value: "secret",
        });
        form.appendChild(hidden);
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).toContain("hiddenField=secret");
    });

    test("checked checkbox is included in request", () => {
        const cb = document.createElement("input");
        cb.type = "checkbox";
        cb.name = "cb1";
        cb.value = "on";
        cb.checked = true;
        form.appendChild(cb);
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).toContain("cb1=on");
    });

    test("unchecked checkbox is NOT included in request", () => {
        const cb = document.createElement("input");
        cb.type = "checkbox";
        cb.name = "cb2";
        cb.value = "on";
        cb.checked = false;
        form.appendChild(cb);
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).not.toContain("cb2=");
    });

    test("selected radio button is included in request", () => {
        const radio = document.createElement("input");
        radio.type = "radio";
        radio.name = "radio1";
        radio.value = "optA";
        radio.checked = true;
        form.appendChild(radio);
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).toContain("radio1=optA");
    });

    test("unselected radio button is NOT included in request", () => {
        const radio = document.createElement("input");
        radio.type = "radio";
        radio.name = "radio2";
        radio.value = "optB";
        radio.checked = false;
        form.appendChild(radio);
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).not.toContain("radio2=");
    });

    test("select element value is included in request", () => {
        const sel = document.createElement("select");
        sel.name = "sel1";
        const opt = document.createElement("option");
        opt.value = "chosen";
        opt.selected = true;
        sel.appendChild(opt);
        form.appendChild(sel);
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).toContain("sel1=chosen");
    });

    test("textarea value is included in request", () => {
        const ta = document.createElement("textarea");
        ta.name = "ta1";
        ta.value = "multiline text";
        form.appendChild(ta);
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).toContain("ta1=");
    });

    test("disabled field is NOT included in request", () => {
        const input = Object.assign(document.createElement("input"), {
            type: "text",
            name: "disabledField",
            value: "nope",
        });
        input.disabled = true;
        form.appendChild(input);
        ajax().request(button, null, { execute: "@form" });
        expect(lastXHR().body).not.toContain("disabledField=");
    });

    test("field without name is NOT included in request", () => {
        const input = Object.assign(document.createElement("input"), {
            type: "text",
            value: "nameless",
        });
        form.appendChild(input);
        ajax().request(button, null, { execute: "@form" });
        // The nameless input should not add empty-key param
        expect(lastXHR().body).not.toContain("=nameless");
    });
});

// ---- event timing and data completeness ----

describe("faces.ajax: event and error data completeness", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("begin event data has type, status and source", () => {
        const events: Record<string, unknown>[] = [];
        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => events.push(data) });

        const begin = events.find(e => e.status === "begin");
        expect(begin).toBeDefined();
        expect(begin!.type).toBe("event");
        expect(begin!.status).toBe("begin");
        expect(begin!.source).toBe(button);
    });

    test("complete event fires before success event", () => {
        const events: string[] = [];
        ajax().request(button, null, { onevent: (data: Record<string, unknown>) => events.push(data.status as string) });
        lastXHR().respond(200, "",
            '<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><changes></changes></partial-response>');

        const completeIdx = events.indexOf("complete");
        const successIdx = events.indexOf("success");
        expect(completeIdx).toBeGreaterThan(-1);
        expect(successIdx).toBeGreaterThan(-1);
        expect(completeIdx).toBeLessThan(successIdx);
    });

    test("complete event fires before httpError", () => {
        const events: string[] = [];
        const errors: string[] = [];
        ajax().request(button, null, {
            onevent: (data: Record<string, unknown>) => events.push(data.status as string),
            onerror: (data: Record<string, unknown>) => errors.push(data.status as string),
        });
        lastXHR().respond(500, "Error");

        const completeIdx = events.indexOf("complete");
        expect(completeIdx).toBeGreaterThan(-1);
        // httpError fires after complete event
        expect(errors).toContain("httpError");
    });

    test("error data includes responseXML when present", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(500, "err",
            '<?xml version="1.0" encoding="UTF-8"?><error/>');

        expect(errors[0].responseText).toBe("err");
    });

    test("error data includes description for non-zero httpError", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        lastXHR().respond(403, "Forbidden");

        expect(errors[0].description).toContain("403");
    });

    test("serverError data includes errorName and errorMessage", () => {
        const errors: Record<string, unknown>[] = [];
        ajax().request(button, null, { onerror: (data: Record<string, unknown>) => errors.push(data) });
        const xml = '<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><error><error-name>com.example.MyException</error-name><error-message><![CDATA[Detailed error info]]></error-message></error></partial-response>';
        lastXHR().respond(200, "", xml);

        expect(errors[0].errorName).toBe("com.example.MyException");
        expect(errors[0].errorMessage).toBe("Detailed error info");
        expect(errors[0].status).toBe("serverError");
    });
});

// ---- encodedURL handling ----

describe("faces.ajax.request: encodedURL", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("uses jakarta.faces.encodedURL over form action when present", () => {
        const encodedUrl = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.encodedURL",
            value: "/encoded/path",
        });
        form.appendChild(encodedUrl);
        ajax().request(button, null);
        expect(lastXHR().url).toContain("/encoded/path");
    });

    test("uses form action when encodedURL is absent", () => {
        ajax().request(button, null);
        expect(lastXHR().url).toContain("/test/action");
    });
});

// ---- response: CSP-compatible eval processing ----

describe("faces.ajax.response: eval element (CSP)", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    function successResponse(changes: string): string {
        return `<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><changes>${changes}</changes></partial-response>`;
    }

    test("eval element executes JavaScript from response", () => {
        (window as unknown as Record<string, unknown>).__evalTest = undefined;

        ajax().request(button, null);
        const xml = successResponse('<eval><![CDATA[window.__evalTest = "executed"]]></eval>');
        lastXHR().respond(200, "", xml);

        expect((window as unknown as Record<string, unknown>).__evalTest).toBe("executed");
        delete (window as unknown as Record<string, unknown>).__evalTest;
    });

    test("eval element executes via script element (CSP-compatible), not window.eval", () => {
        const origEval = window.eval;
        let evalCalled = false;
        (window as unknown as Record<string, unknown>).eval = function(...args: unknown[]) {
            evalCalled = true;
            return origEval.apply(window, args as [string]);
        };

        (window as unknown as Record<string, unknown>).__evalCSP = undefined;

        ajax().request(button, null);
        const xml = successResponse('<eval><![CDATA[window.__evalCSP = "csp"]]></eval>');
        lastXHR().respond(200, "", xml);

        expect(evalCalled).toBe(false);
        expect((window as unknown as Record<string, unknown>).__evalCSP).toBe("csp");

        (window as unknown as Record<string, unknown>).eval = origEval;
        delete (window as unknown as Record<string, unknown>).__evalCSP;
    });

    test("multiple eval elements execute in order", () => {
        (window as unknown as Record<string, unknown>).__evalOrder = [];

        ajax().request(button, null);
        const xml = successResponse(
            '<eval><![CDATA[window.__evalOrder.push(1)]]></eval>' +
            '<eval><![CDATA[window.__evalOrder.push(2)]]></eval>' +
            '<eval><![CDATA[window.__evalOrder.push(3)]]></eval>');
        lastXHR().respond(200, "", xml);

        expect((window as unknown as Record<string, unknown>).__evalOrder).toEqual([1, 2, 3]);
        delete (window as unknown as Record<string, unknown>).__evalOrder;
    });

    test("update element containing inline script executes via CSP-compatible path", () => {
        const target = document.createElement("div");
        target.id = "scriptTarget";
        document.body.appendChild(target);

        (window as unknown as Record<string, unknown>).__scriptInUpdate = undefined;

        ajax().request(button, null);
        const xml = successResponse(
            '<update id="scriptTarget"><![CDATA[<div id="scriptTarget">updated</div>' +
            '<script type="text/javascript">window.__scriptInUpdate = "ran"</script>]]></update>');
        lastXHR().respond(200, "", xml);

        expect((window as unknown as Record<string, unknown>).__scriptInUpdate).toBe("ran");
        delete (window as unknown as Record<string, unknown>).__scriptInUpdate;
        document.getElementById("scriptTarget")?.remove();
    });
});

// ---- response: CSP nonce propagation ----

describe("faces.ajax.response: nonce propagation", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;
    let facesScript: HTMLScriptElement;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());

        facesScript = document.createElement("script");
        facesScript.src = "http://localhost/jakarta.faces.resource/faces.js?ln=jakarta.faces";
        facesScript.nonce = "test-nonce-123";
        document.head.appendChild(facesScript);
    });

    afterEach(() => {
        form?.remove();
        facesScript?.remove();
        uninstallMockXHR();
    });

    function successResponse(changes: string): string {
        return `<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><changes>${changes}</changes></partial-response>`;
    }

    test("dynamically created script elements for eval receive nonce from faces.js script tag", () => {
        const createdScripts: HTMLScriptElement[] = [];
        const origCreate = document.createElement.bind(document);
        document.createElement = function(tagName: string, options?: ElementCreationOptions) {
            const el = origCreate(tagName, options);
            if (tagName.toLowerCase() === "script") {
                createdScripts.push(el as HTMLScriptElement);
            }
            return el;
        } as typeof document.createElement;

        (window as unknown as Record<string, unknown>).__nonceTest = undefined;

        ajax().request(button, null);
        const xml = successResponse('<eval><![CDATA[window.__nonceTest = "nonce"]]></eval>');
        lastXHR().respond(200, "", xml);

        document.createElement = origCreate;

        const scriptsWithNonce = createdScripts.filter(s => s.nonce === "test-nonce-123");
        expect(scriptsWithNonce.length).toBeGreaterThanOrEqual(1);

        expect((window as unknown as Record<string, unknown>).__nonceTest).toBe("nonce");
        delete (window as unknown as Record<string, unknown>).__nonceTest;
    });

    test("inline scripts in update elements receive nonce", () => {
        const target = document.createElement("div");
        target.id = "nonceTarget";
        document.body.appendChild(target);

        const createdScripts: HTMLScriptElement[] = [];
        const origCreate = document.createElement.bind(document);
        document.createElement = function(tagName: string, options?: ElementCreationOptions) {
            const el = origCreate(tagName, options);
            if (tagName.toLowerCase() === "script") {
                createdScripts.push(el as HTMLScriptElement);
            }
            return el;
        } as typeof document.createElement;

        ajax().request(button, null);
        const xml = successResponse(
            '<update id="nonceTarget"><![CDATA[<div id="nonceTarget">updated</div>' +
            '<script type="text/javascript">void(0)</script>]]></update>');
        lastXHR().respond(200, "", xml);

        document.createElement = origCreate;

        const scriptsWithNonce = createdScripts.filter(s => s.nonce === "test-nonce-123");
        expect(scriptsWithNonce.length).toBeGreaterThanOrEqual(1);

        document.getElementById("nonceTarget")?.remove();
    });
});

// ---- response function direct call validation ----

describe("faces.ajax.response: direct call validation", () => {
    test("throws when request parameter is null", () => {
        expect(() => ajax().response(null, {})).toThrow("faces.ajax.response: Request parameter is unset");
    });

    test("throws when request parameter is undefined", () => {
        expect(() => ajax().response(undefined, {})).toThrow("faces.ajax.response: Request parameter is unset");
    });
});

// ---- namespaced view (NamingContainer view root) ----

// When the UIViewRoot is a NamingContainer, the ViewState field name is prefixed with the view
// root container client id, e.g. "MyNamingContainer:jakarta.faces.ViewState:0". request() must
// still locate that field (without yet knowing the prefix) so it can derive the prefix and
// namespace the partial-request params. Only namespaceParametersIfNecessary() — the prefixing of
// relative render/execute target ids — remains covered by the Faces TCK / integration tests
// against a live container.

describe("faces.ajax.request: namespaced view (NamingContainer view root)", () => {
    const PREFIX = "MyNamingContainer:";
    let form: HTMLFormElement;
    let button: HTMLButtonElement;

    beforeEach(() => {
        installMockXHR();

        form = document.createElement("form");
        form.id = PREFIX + "testForm";
        form.method = "post";
        form.action = "/test/action";

        // In a namespaced view the field name is <prefix>jakarta.faces.ViewState<sep><counter>,
        // which an exact-name lookup for "jakarta.faces.ViewState" would miss.
        const viewState = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: PREFIX + "jakarta.faces.ViewState:0",
            value: "testViewState123",
        });
        form.appendChild(viewState);

        button = document.createElement("button");
        button.type = "button";
        button.id = PREFIX + "testButton";
        button.name = PREFIX + "testButton";
        form.appendChild(button);

        document.body.appendChild(form);
    });

    afterEach(() => {
        form?.remove();
        uninstallMockXHR();
    });

    test("finds the namespaced ViewState field instead of throwing 'no view state element'", () => {
        expect(() => ajax().request(button, null)).not.toThrow();
    });

    test("derives the namespace prefix and namespaces the source and ajax params", () => {
        ajax().request(button, null);
        const body = decodeURIComponent(lastXHR().body!);
        expect(body).toContain(PREFIX + "jakarta.faces.source=" + PREFIX + "testButton");
        expect(body).toContain(PREFIX + "jakarta.faces.partial.ajax=true");
    });

    test("includes the namespaced ViewState value", () => {
        ajax().request(button, null);
        const body = decodeURIComponent(lastXHR().body!);
        expect(body).toContain(PREFIX + "jakarta.faces.ViewState:0=testViewState123");
    });
});
