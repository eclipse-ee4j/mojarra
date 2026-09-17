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
 * Tests for the fallback error handling behavior when no error handler is registered (issue #5681).
 *
 * This is a separate test file because the fallback code path requires that no global
 * addOnError listeners are registered. Since errorListeners is module-level state with
 * no removal API, these tests need a fresh faces.js instance (provided by a separate
 * jest test file which gets its own jsdom environment).
 */

import { loadFacesJs } from "../test-setup";
import { installMockXHR, uninstallMockXHR, lastXHR, createAjaxForm } from "../test-helpers";

beforeAll(() => loadFacesJs());

const ajax = () => faces.ajax as Record<string, Function>;

// ---- Fallback error handling when no error handler registered (issue #5681) ----

describe("faces.ajax: fallback error handling without registered handler", () => {
    let form: HTMLFormElement;
    let button: HTMLButtonElement;
    let savedProjectStageCache: unknown;

    beforeEach(() => {
        installMockXHR();
        ({ form, button } = createAjaxForm());
        savedProjectStageCache = mojarra.projectStageCache;
    });

    afterEach(() => {
        mojarra.projectStageCache = savedProjectStageCache;
        form?.remove();
        uninstallMockXHR();
        jest.restoreAllMocks();
    });

    test("logs console.error with detailed message in Production mode", () => {
        mojarra.projectStageCache = "Production";
        const errorSpy = jest.spyOn(console, "error").mockImplementation(() => {});
        jest.spyOn(console, "warn").mockImplementation(() => {});

        ajax().request(button, null);
        lastXHR().respond(404, "Not Found");

        expect(errorSpy).toHaveBeenCalledTimes(1);
        const msg = errorSpy.mock.calls[0][0] as string;
        expect(msg).toContain("httpError");
        expect(msg).toContain("(HTTP 404)");
    });

    test("shows alert in Development mode", () => {
        mojarra.projectStageCache = "Development";
        const alertSpy = jest.spyOn(window, "alert").mockImplementation(() => {});
        jest.spyOn(console, "warn").mockImplementation(() => {});

        ajax().request(button, null);
        lastXHR().respond(500, "Server Error");

        expect(alertSpy).toHaveBeenCalledTimes(1);
        const msg = alertSpy.mock.calls[0][0] as string;
        expect(msg).toContain("httpError");
        expect(msg).toContain("(HTTP 500)");
    });

    test("error message includes source element id", () => {
        mojarra.projectStageCache = "Production";
        const errorSpy = jest.spyOn(console, "error").mockImplementation(() => {});
        jest.spyOn(console, "warn").mockImplementation(() => {});

        button.id = "myButton";
        ajax().request(button, null);
        lastXHR().respond(404, "Not Found");

        const msg = errorSpy.mock.calls[0][0] as string;
        expect(msg).toContain("[source: myButton]");
    });

    test("error message includes responseCode", () => {
        mojarra.projectStageCache = "Production";
        const errorSpy = jest.spyOn(console, "error").mockImplementation(() => {});
        jest.spyOn(console, "warn").mockImplementation(() => {});

        ajax().request(button, null);
        lastXHR().respond(503, "Service Unavailable");

        const msg = errorSpy.mock.calls[0][0] as string;
        expect(msg).toContain("(HTTP 503)");
    });

    test("always logs console.warn about missing handler", () => {
        mojarra.projectStageCache = "Production";
        jest.spyOn(console, "error").mockImplementation(() => {});
        const warnSpy = jest.spyOn(console, "warn").mockImplementation(() => {});

        ajax().request(button, null);
        lastXHR().respond(404, "Not Found");

        expect(warnSpy).toHaveBeenCalledTimes(1);
        expect(warnSpy.mock.calls[0][0] as string).toContain("No faces.ajax.addOnError handler registered");
    });

    test("console.warn logged in Development mode too", () => {
        mojarra.projectStageCache = "Development";
        jest.spyOn(window, "alert").mockImplementation(() => {});
        const warnSpy = jest.spyOn(console, "warn").mockImplementation(() => {});

        ajax().request(button, null);
        lastXHR().respond(404, "Not Found");

        expect(warnSpy).toHaveBeenCalledTimes(1);
        expect(warnSpy.mock.calls[0][0] as string).toContain("No faces.ajax.addOnError handler registered");
    });

    test("no fallback when per-request onerror handler registered", () => {
        mojarra.projectStageCache = "Production";
        const errorSpy = jest.spyOn(console, "error").mockImplementation(() => {});
        const warnSpy = jest.spyOn(console, "warn").mockImplementation(() => {});
        const alertSpy = jest.spyOn(window, "alert").mockImplementation(() => {});

        ajax().request(button, null, { onerror: () => {} });
        lastXHR().respond(404, "Not Found");

        expect(errorSpy).not.toHaveBeenCalled();
        expect(warnSpy).not.toHaveBeenCalled();
        expect(alertSpy).not.toHaveBeenCalled();
    });

    test("emptyResponse fallback includes status and source", () => {
        mojarra.projectStageCache = "Production";
        const errorSpy = jest.spyOn(console, "error").mockImplementation(() => {});
        jest.spyOn(console, "warn").mockImplementation(() => {});

        button.id = "emptyBtn";
        ajax().request(button, null);
        lastXHR().respond(200, "");

        const msg = errorSpy.mock.calls[0][0] as string;
        expect(msg).toContain("emptyResponse");
        expect(msg).toContain("[source: emptyBtn]");
    });

    test("serverError fallback includes errorName in message", () => {
        mojarra.projectStageCache = "Production";
        const errorSpy = jest.spyOn(console, "error").mockImplementation(() => {});
        jest.spyOn(console, "warn").mockImplementation(() => {});

        ajax().request(button, null);
        const xml = '<?xml version="1.0" encoding="UTF-8"?><partial-response id=""><error><error-name>java.lang.NullPointerException</error-name><error-message><![CDATA[Something broke]]></error-message></error></partial-response>';
        lastXHR().respond(200, "", xml);

        const msg = errorSpy.mock.calls[0][0] as string;
        expect(msg).toContain("serverError");
        expect(msg).toContain("java.lang.NullPointerException");
    });

    test("calls window.onerror in Production mode when handler is registered", () => {
        mojarra.projectStageCache = "Production";
        jest.spyOn(console, "error").mockImplementation(() => {});
        jest.spyOn(console, "warn").mockImplementation(() => {});
        const onerrorSpy = jest.fn();
        window.onerror = onerrorSpy;

        ajax().request(button, null);
        lastXHR().respond(404, "Not Found");

        expect(onerrorSpy).toHaveBeenCalledTimes(1);
        expect(onerrorSpy.mock.calls[0][0]).toContain("httpError");
        expect(onerrorSpy.mock.calls[0][0]).toContain("WARNING: No faces.ajax.addOnError handler registered");
        expect(onerrorSpy.mock.calls[0][1]).toBe("jakarta.faces:faces.js");
        expect(onerrorSpy.mock.calls[0][4]).toBeInstanceOf(Error);

        window.onerror = null;
    });

    test("calls window.onerror in Development mode when handler is registered", () => {
        mojarra.projectStageCache = "Development";
        jest.spyOn(window, "alert").mockImplementation(() => {});
        jest.spyOn(console, "warn").mockImplementation(() => {});
        const onerrorSpy = jest.fn();
        window.onerror = onerrorSpy;

        ajax().request(button, null);
        lastXHR().respond(500, "Server Error");

        expect(onerrorSpy).toHaveBeenCalledTimes(1);
        expect(onerrorSpy.mock.calls[0][0]).toContain("httpError");
        expect(onerrorSpy.mock.calls[0][0]).toContain("WARNING: No faces.ajax.addOnError handler registered");
        expect(onerrorSpy.mock.calls[0][1]).toBe("jakarta.faces:faces.js");

        window.onerror = null;
    });

    test("does not call window.onerror when no handler is registered", () => {
        mojarra.projectStageCache = "Production";
        jest.spyOn(console, "error").mockImplementation(() => {});
        jest.spyOn(console, "warn").mockImplementation(() => {});
        window.onerror = null;

        // Should not throw
        ajax().request(button, null);
        lastXHR().respond(404, "Not Found");
    });

    // This test registers a global addOnError listener which cannot be removed,
    // so it must be the last test in this file.
    test("no fallback when global addOnError listener registered", () => {
        mojarra.projectStageCache = "Production";
        const errorSpy = jest.spyOn(console, "error").mockImplementation(() => {});
        const warnSpy = jest.spyOn(console, "warn").mockImplementation(() => {});

        ajax().addOnError(() => {});
        ajax().request(button, null);
        lastXHR().respond(404, "Not Found");

        expect(errorSpy).not.toHaveBeenCalled();
        expect(warnSpy).not.toHaveBeenCalled();
    });
});
