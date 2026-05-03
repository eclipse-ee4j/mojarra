/**
 * Tests for the top-level `faces` namespace exposed by faces.js.
 */

import { parseFacesJsVersion, loadFacesJs } from "../test-setup";

const facesJsVersion = parseFacesJsVersion();

beforeAll(() => loadFacesJs());

describe("faces namespace", () => {

    const EXPECTED_MEMBERS: Record<string, string> = {
        // Nested namespaces / objects
        ajax: "object",
        push: "object",
        util: "object",
        // Functions
        getProjectStage: "function",
        getViewState: "function",
        getClientWindow: "function",
        // Properties
        separatorchar: "string",
        contextpath: "string",
        specversion: "number",
        implversion: "number",
    };

    test("exposes exactly the expected public members", () => {
        const actualKeys = Object.keys(faces).sort();
        const expectedKeys = Object.keys(EXPECTED_MEMBERS).sort();
        expect(actualKeys).toEqual(expectedKeys);
    });

    test("each member has the expected type", () => {
        for (const [key, expectedType] of Object.entries(EXPECTED_MEMBERS)) {
            expect(typeof faces[key]).toBe(expectedType);
        }
    });
});

describe("faces.specversion", () => {
    test("matches spec major.minor from @version tag", () => {
        expect(faces.specversion).toBeGreaterThanOrEqual(facesJsVersion.specversion);
        expect(faces.specversion).toBeLessThanOrEqual(facesJsVersion.specversion + 99);
    });
});

describe("faces.implversion", () => {
    test("matches patch version from pom.xml", () => {
        expect(faces.implversion).toBe(facesJsVersion.implversion);
    });
});

describe("version guard", () => {

    test("skips re-initialization when same version is already loaded", () => {
        const origSpec = faces.specversion;
        const origImpl = faces.implversion;
        loadFacesJs();
        expect(faces.specversion).toBe(origSpec);
        expect(faces.implversion).toBe(origImpl);
    });

    test("re-initializes when a lower specversion is present", () => {
        (faces as Record<string, unknown>).specversion = 1;
        loadFacesJs();
        expect(faces.specversion).toBeGreaterThanOrEqual(facesJsVersion.specversion);
    });

    test("re-initializes when a lower implversion is present", () => {
        (faces as Record<string, unknown>).implversion = 0;
        loadFacesJs();
        expect(faces.implversion).toBe(facesJsVersion.implversion);
    });

    test("re-initializes when previous specversion is only one less than current", () => {
        const original = faces.specversion as number;
        (faces as Record<string, unknown>).specversion = original - 1;
        loadFacesJs();
        expect(faces.specversion).toBe(original);
    });

    test("re-initializes when previous implversion is only one less than current", () => {
        (faces as Record<string, unknown>).implversion = facesJsVersion.implversion - 1;
        loadFacesJs();
        expect(faces.implversion).toBe(facesJsVersion.implversion);
    });
});

describe("faces.separatorchar", () => {
    test("is ':'", () => {
        expect(faces.separatorchar).toBe(":");
    });
});

describe("faces.contextpath", () => {
    test("is '/test'", () => {
        expect(faces.contextpath).toBe("/test");
    });
});

describe("faces.getViewState", () => {
    let form: HTMLFormElement;

    beforeEach(() => {
        form = document.createElement("form");
        document.body.appendChild(form);
    });

    afterEach(() => {
        form?.remove();
    });

    test("throws when form is null", () => {
        expect(() => (faces.getViewState as Function)(null)).toThrow("faces.getViewState:  form must be set");
    });

    test("throws when form is undefined", () => {
        expect(() => (faces.getViewState as Function)(undefined)).toThrow("faces.getViewState:  form must be set");
    });

    test("throws when form is false", () => {
        expect(() => (faces.getViewState as Function)(false)).toThrow("faces.getViewState:  form must be set");
    });

    test("throws when form is 0", () => {
        expect(() => (faces.getViewState as Function)(0)).toThrow("faces.getViewState:  form must be set");
    });

    test("throws when form is empty string", () => {
        expect(() => (faces.getViewState as Function)("")).toThrow("faces.getViewState:  form must be set");
    });

    test("returns empty string for empty form", () => {
        expect((faces.getViewState as Function)(form)).toBe("");
    });

    test("encodes hidden input", () => {
        const input = Object.assign(document.createElement("input"), { type: "hidden", name: "key", value: "value" });
        form.appendChild(input);
        expect((faces.getViewState as Function)(form)).toBe("key=value");
    });

    test("encodes text input", () => {
        const input = Object.assign(document.createElement("input"), { type: "text", name: "username", value: "john" });
        form.appendChild(input);
        expect((faces.getViewState as Function)(form)).toBe("username=john");
    });

    test("encodes multiple inputs with & separator", () => {
        form.appendChild(Object.assign(document.createElement("input"), { type: "hidden", name: "a", value: "1" }));
        form.appendChild(Object.assign(document.createElement("input"), { type: "hidden", name: "b", value: "2" }));
        expect((faces.getViewState as Function)(form)).toBe("a=1&b=2");
    });

    test("skips disabled inputs", () => {
        const input = Object.assign(document.createElement("input"), { type: "text", name: "skip", value: "me", disabled: true });
        form.appendChild(input);
        expect((faces.getViewState as Function)(form)).toBe("");
    });

    test("skips inputs with empty name", () => {
        const input = Object.assign(document.createElement("input"), { type: "text", name: "", value: "anonymous" });
        form.appendChild(input);
        expect((faces.getViewState as Function)(form)).toBe("");
    });

    test("skips submit, reset, image, and file inputs", () => {
        for (const type of ["submit", "reset", "image", "file"]) {
            const input = document.createElement("input");
            input.type = type;
            input.name = type;
            form.appendChild(input);
        }
        expect((faces.getViewState as Function)(form)).toBe("");
    });

    test("encodes checked checkbox", () => {
        const cb = Object.assign(document.createElement("input"), { type: "checkbox", name: "agree", value: "yes", checked: true });
        form.appendChild(cb);
        expect((faces.getViewState as Function)(form)).toBe("agree=yes");
    });

    test("skips unchecked checkbox", () => {
        const cb = Object.assign(document.createElement("input"), { type: "checkbox", name: "agree", value: "yes", checked: false });
        form.appendChild(cb);
        expect((faces.getViewState as Function)(form)).toBe("");
    });

    test("checkbox without value defaults to 'on'", () => {
        const cb = document.createElement("input");
        cb.type = "checkbox";
        cb.name = "toggle";
        cb.checked = true;
        cb.removeAttribute("value");
        form.appendChild(cb);
        expect((faces.getViewState as Function)(form)).toBe("toggle=on");
    });

    test("encodes checked radio", () => {
        const radio = Object.assign(document.createElement("input"), { type: "radio", name: "choice", value: "a", checked: true });
        form.appendChild(radio);
        expect((faces.getViewState as Function)(form)).toBe("choice=a");
    });

    test("skips unchecked radio", () => {
        const radio = Object.assign(document.createElement("input"), { type: "radio", name: "choice", value: "a", checked: false });
        form.appendChild(radio);
        expect((faces.getViewState as Function)(form)).toBe("");
    });

    test("encodes select-one", () => {
        const select = document.createElement("select");
        select.name = "color";
        select.appendChild(Object.assign(document.createElement("option"), { value: "red", selected: true }));
        select.appendChild(Object.assign(document.createElement("option"), { value: "blue" }));
        form.appendChild(select);
        expect((faces.getViewState as Function)(form)).toBe("color=red");
    });

    test("encodes select-multiple with multiple selections", () => {
        const select = document.createElement("select");
        select.name = "colors";
        select.multiple = true;
        select.appendChild(Object.assign(document.createElement("option"), { value: "red", selected: true }));
        select.appendChild(Object.assign(document.createElement("option"), { value: "green", selected: false }));
        select.appendChild(Object.assign(document.createElement("option"), { value: "blue", selected: true }));
        form.appendChild(select);
        expect((faces.getViewState as Function)(form)).toBe("colors=red&colors=blue");
    });

    test("encodes textarea", () => {
        const ta = document.createElement("textarea");
        ta.name = "comment";
        ta.value = "hello world";
        form.appendChild(ta);
        expect((faces.getViewState as Function)(form)).toBe("comment=hello+world");
    });

    test("URL-encodes special characters", () => {
        const input = Object.assign(document.createElement("input"), { type: "hidden", name: "key=1", value: "a&b" });
        form.appendChild(input);
        expect((faces.getViewState as Function)(form)).toBe("key%3D1=a%26b");
    });

    test("encodes password input", () => {
        const input = Object.assign(document.createElement("input"), { type: "password", name: "secret", value: "p@ss" });
        form.appendChild(input);
        expect((faces.getViewState as Function)(form)).toBe("secret=p%40ss");
    });

    test("skips button element with default type (submit)", () => {
        const btn = Object.assign(document.createElement("button"), { name: "action", value: "save" });
        form.appendChild(btn);
        expect((faces.getViewState as Function)(form)).toBe("");
    });

    test("encodes button element with type='button'", () => {
        const btn = Object.assign(document.createElement("button"), { type: "button", name: "action", value: "save" });
        form.appendChild(btn);
        expect((faces.getViewState as Function)(form)).toBe("action=save");
    });

    test("encodes input with empty value", () => {
        const input = Object.assign(document.createElement("input"), { type: "text", name: "empty", value: "" });
        form.appendChild(input);
        expect((faces.getViewState as Function)(form)).toBe("empty=");
    });

    test("select-one with no selection returns nothing", () => {
        const select = document.createElement("select");
        select.name = "color";
        select.appendChild(document.createElement("option"));
        select.selectedIndex = -1;
        form.appendChild(select);
        expect((faces.getViewState as Function)(form)).toBe("");
    });

    test("select-multiple with no selections returns nothing", () => {
        const select = document.createElement("select");
        select.name = "colors";
        select.multiple = true;
        select.appendChild(Object.assign(document.createElement("option"), { value: "red", selected: false }));
        select.appendChild(Object.assign(document.createElement("option"), { value: "blue", selected: false }));
        form.appendChild(select);
        expect((faces.getViewState as Function)(form)).toBe("");
    });

    test("encodes only enabled inputs in mixed enabled/disabled form", () => {
        form.appendChild(Object.assign(document.createElement("input"), { type: "hidden", name: "a", value: "1" }));
        form.appendChild(Object.assign(document.createElement("input"), { type: "hidden", name: "b", value: "2", disabled: true }));
        form.appendChild(Object.assign(document.createElement("input"), { type: "hidden", name: "c", value: "3" }));
        expect((faces.getViewState as Function)(form)).toBe("a=1&c=3");
    });

    test("encodes Unicode values", () => {
        const input = Object.assign(document.createElement("input"), { type: "hidden", name: "msg", value: "\u00e9\u00e0\u00fc" });
        form.appendChild(input);
        expect((faces.getViewState as Function)(form)).toBe("msg=%C3%A9%C3%A0%C3%BC");
    });
});

describe("faces.getClientWindow", () => {
    let form: HTMLFormElement;

    afterEach(() => {
        form?.remove();
    });

    test("returns null when no forms exist", () => {
        expect((faces.getClientWindow as Function)()).toBeNull();
    });

    test("returns null when form has no ClientWindow field", () => {
        form = document.createElement("form");
        document.body.appendChild(form);
        expect((faces.getClientWindow as Function)()).toBeNull();
    });

    test("returns windowId from form with ClientWindow hidden field", () => {
        form = document.createElement("form");
        const input = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ClientWindow",
            value: "abc123",
        });
        form.appendChild(input);
        document.body.appendChild(form);
        expect((faces.getClientWindow as Function)()).toBe("abc123");
    });

    test("returns windowId when searching from a given form node", () => {
        form = document.createElement("form");
        const input = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ClientWindow",
            value: "xyz789",
        });
        form.appendChild(input);
        document.body.appendChild(form);
        expect((faces.getClientWindow as Function)(form)).toBe("xyz789");
    });

    test("accepts string argument as DOM id", () => {
        form = document.createElement("form");
        form.id = "myForm";
        const input = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ClientWindow",
            value: "win1",
        });
        form.appendChild(input);
        document.body.appendChild(form);
        expect((faces.getClientWindow as Function)("myForm")).toBe("win1");
    });

    test("returns null for nonexistent DOM id", () => {
        expect((faces.getClientWindow as Function)("nonexistent")).toBeNull();
    });

    test("finds form inside a container div", () => {
        const div = document.createElement("div");
        form = document.createElement("form");
        const input = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ClientWindow",
            value: "nested1",
        });
        form.appendChild(input);
        div.appendChild(form);
        document.body.appendChild(div);
        expect((faces.getClientWindow as Function)(div)).toBe("nested1");
        div.remove();
    });

    test("succeeds when multiple forms have the same windowId", () => {
        const form1 = document.createElement("form");
        form1.appendChild(Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ClientWindow",
            value: "same",
        }));
        const form2 = document.createElement("form");
        form2.appendChild(Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ClientWindow",
            value: "same",
        }));
        document.body.appendChild(form1);
        document.body.appendChild(form2);
        expect((faces.getClientWindow as Function)()).toBe("same");
        form1.remove();
        form2.remove();
    });

    test("throws when multiple forms have different windowIds", () => {
        const form1 = document.createElement("form");
        form1.appendChild(Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ClientWindow",
            value: "id1",
        }));
        const form2 = document.createElement("form");
        form2.appendChild(Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "jakarta.faces.ClientWindow",
            value: "id2",
        }));
        document.body.appendChild(form1);
        document.body.appendChild(form2);
        expect(() => (faces.getClientWindow as Function)()).toThrow("Multiple different windowIds found in document");
        form1.remove();
        form2.remove();
    });

    test("finds namespaced ClientWindow field", () => {
        form = document.createElement("form");
        const input = Object.assign(document.createElement("input"), {
            type: "hidden",
            name: "portletViewId:jakarta.faces.ClientWindow",
            value: "ns1",
        });
        form.appendChild(input);
        document.body.appendChild(form);
        expect((faces.getClientWindow as Function)()).toBe("ns1");
    });
});

describe("faces.getProjectStage", () => {
    test("defaults to 'Production' when no faces.js script tag is present", () => {
        expect((faces.getProjectStage as Function)()).toBe("Production");
    });

    test("caches result in mojarra.projectStageCache", () => {
        (faces.getProjectStage as Function)();
        expect(mojarra.projectStageCache).toBe("Production");
    });

    test("reads stage from faces.js script src URL", () => {
        // Clear cache from previous tests
        delete mojarra.projectStageCache;

        const script = document.createElement("script");
        script.src = "http://localhost/jakarta.faces.resource/faces.js?ln=jakarta.faces&stage=Development";
        document.head.appendChild(script);

        expect((faces.getProjectStage as Function)()).toBe("Development");

        script.remove();
        delete mojarra.projectStageCache;
    });

    test.each(["Development", "UnitTest", "SystemTest", "Production"])(
        "reads stage='%s' from faces.js script src URL",
        (stage) => {
            delete mojarra.projectStageCache;

            const script = document.createElement("script");
            script.src = `http://localhost/jakarta.faces.resource/faces.js?ln=jakarta.faces&stage=${stage}`;
            document.head.appendChild(script);

            expect((faces.getProjectStage as Function)()).toBe(stage);

            script.remove();
            delete mojarra.projectStageCache;
        }
    );

    test("falls back to 'Production' for unrecognized stage values in URL", () => {
        delete mojarra.projectStageCache;

        const script = document.createElement("script");
        script.src = "http://localhost/jakarta.faces.resource/faces.js?ln=jakarta.faces&stage=Bogus";
        document.head.appendChild(script);

        expect((faces.getProjectStage as Function)()).toBe("Production");

        script.remove();
        delete mojarra.projectStageCache;
    });

    test("returns cached value on second call without re-reading the DOM", () => {
        // First call populates the cache from a Development URL.
        delete mojarra.projectStageCache;
        const script = document.createElement("script");
        script.src = "http://localhost/jakarta.faces.resource/faces.js?ln=jakarta.faces&stage=Development";
        document.head.appendChild(script);
        expect((faces.getProjectStage as Function)()).toBe("Development");

        // Remove the script tag; without the cache, the next call would default to Production.
        script.remove();

        // Second call must return the cached "Development" — the script-tag DOM was already gone.
        expect((faces.getProjectStage as Function)()).toBe("Development");

        delete mojarra.projectStageCache;
    });
});
