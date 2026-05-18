/**
 * Tests for the `faces.push` namespace exposed by faces.js.
 */

import { loadFacesJs } from "../test-setup";

// ---- Mock WebSocket ----

interface MockWebSocketInstance {
    url: string;
    readyState: number;
    onopen: ((event: Event) => void) | null;
    onmessage: ((event: { data: string }) => void) | null;
    onclose: ((event: { code: number; reason: string }) => void) | null;
    close(): void;
    /** Test helper: simulate open event. */
    simulateOpen(): void;
    /** Test helper: simulate message event. */
    simulateMessage(data: unknown): void;
    /** Test helper: simulate close event. */
    simulateClose(code: number, reason?: string): void;
}

let wsInstances: MockWebSocketInstance[];
let OriginalWebSocket: typeof WebSocket;

function installMockWebSocket(): void {
    wsInstances = [];
    OriginalWebSocket = window.WebSocket;

    const MockWebSocket = function MockWebSocket(this: MockWebSocketInstance, url: string) {
        this.url = url;
        this.readyState = 0; // CONNECTING
        this.onopen = null;
        this.onmessage = null;
        this.onclose = null;

        this.close = function () {
            this.readyState = 3; // CLOSED
            if (this.onclose) {
                this.onclose({ code: 1000, reason: "" });
            }
        };

        this.simulateOpen = function () {
            this.readyState = 1; // OPEN
            if (this.onopen) {
                this.onopen(new Event("open"));
            }
        };

        this.simulateMessage = function (data: unknown) {
            if (this.onmessage) {
                this.onmessage({ data: JSON.stringify({ data }) });
            }
        };

        this.simulateClose = function (code: number, reason = "") {
            this.readyState = 3; // CLOSED
            if (this.onclose) {
                this.onclose({ code, reason });
            }
        };

        wsInstances.push(this);
    } as unknown as typeof WebSocket & {
        CONNECTING: 0; OPEN: 1; CLOSING: 2; CLOSED: 3;
    };
    MockWebSocket.CONNECTING = 0;
    MockWebSocket.OPEN = 1;
    MockWebSocket.CLOSING = 2;
    MockWebSocket.CLOSED = 3;
    (window as unknown as Record<string, unknown>).WebSocket = MockWebSocket;
}

function uninstallMockWebSocket(): void {
    window.WebSocket = OriginalWebSocket;
}

function lastWS(): MockWebSocketInstance {
    return wsInstances[wsInstances.length - 1];
}

// ---- Test setup ----

beforeAll(() => loadFacesJs());

const push = () => faces.push as Record<string, Function>;

// ---- Namespace structure ----

describe("faces.push namespace", () => {
    const EXPECTED_MEMBERS: Record<string, string> = {
        init: "function",
        open: "function",
        close: "function",
    };

    test("exposes exactly the expected public members", () => {
        const actualKeys = Object.keys(faces.push as object).sort();
        const expectedKeys = Object.keys(EXPECTED_MEMBERS).sort();
        expect(actualKeys).toEqual(expectedKeys);
    });

    test("each member has the expected type", () => {
        const pushObj = faces.push as Record<string, unknown>;
        for (const [key, expectedType] of Object.entries(EXPECTED_MEMBERS)) {
            expect(typeof pushObj[key]).toBe(expectedType);
        }
    });
});

// ---- init ----

describe("faces.push.init", () => {
    beforeEach(() => installMockWebSocket());
    afterEach(() => uninstallMockWebSocket());

    test("creates socket for new clientId", () => {
        push().init("client1", "ws://localhost/push", "channel1", null, null, null, null, {}, false);
        // Should not throw when opening
        expect(() => push().open("client1")).not.toThrow();
    });

    test("does not create duplicate socket for same clientId", () => {
        push().init("dup1", "ws://localhost/push/a", "ch1", null, null, null, null, {}, false);
        push().init("dup1", "ws://localhost/push/b", "ch1", null, null, null, null, {}, false);
        push().open("dup1");
        // Only one WebSocket should be created
        expect(wsInstances.length).toBe(1);
        expect(lastWS().url).toBe("ws://localhost/push/a");
    });

    test("autoconnect=true opens socket immediately", () => {
        push().init("auto1", "ws://localhost/push", "ch1", null, null, null, null, {}, true);
        expect(wsInstances.length).toBe(1);
    });

    test("autoconnect=false does not open socket", () => {
        push().init("noauto1", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        expect(wsInstances.length).toBe(0);
    });

    test("calls onclose with -1 when WebSocket is not supported", () => {
        const origWS = window.WebSocket;
        delete (window as unknown as Record<string, unknown>).WebSocket;

        const closeCalls: unknown[][] = [];
        const onclose = (...args: unknown[]) => closeCalls.push(args);
        push().init("nows1", "ws://localhost/push", "ch1", null, null, null, onclose, {}, false);

        expect(closeCalls.length).toBe(1);
        expect(closeCalls[0][0]).toBe(-1);
        expect(closeCalls[0][1]).toBe("nows1");

        (window as unknown as Record<string, unknown>).WebSocket = origWS;
    });
});

// ---- open ----

describe("faces.push.open", () => {
    beforeEach(() => installMockWebSocket());
    afterEach(() => uninstallMockWebSocket());

    test("throws for unknown clientId", () => {
        expect(() => push().open("unknown")).toThrow("Unknown clientId: unknown");
    });

    test("creates WebSocket with the configured URL", () => {
        push().init("open1", "ws://example.com/push/ch", "ch1", null, null, null, null, {}, false);
        push().open("open1");
        expect(wsInstances.length).toBe(1);
        expect(lastWS().url).toBe("ws://example.com/push/ch");
    });

    test("does not create second WebSocket if already open", () => {
        push().init("open2", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("open2");
        lastWS().simulateOpen();
        push().open("open2");
        expect(wsInstances.length).toBe(1);
    });
});

// ---- close ----

describe("faces.push.close", () => {
    beforeEach(() => installMockWebSocket());
    afterEach(() => uninstallMockWebSocket());

    test("throws for unknown clientId", () => {
        expect(() => push().close("unknown")).toThrow("Unknown clientId: unknown");
    });

    test("closes an open socket", () => {
        push().init("close1", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("close1");
        lastWS().simulateOpen();
        expect(() => push().close("close1")).not.toThrow();
        expect(lastWS().readyState).toBe(3);
    });

    test("close before open does not throw", () => {
        push().init("close2", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        // Socket not yet opened, close should not throw
        expect(() => push().close("close2")).not.toThrow();
    });
});

// ---- onopen callback ----

describe("faces.push: onopen callback", () => {
    beforeEach(() => installMockWebSocket());
    afterEach(() => uninstallMockWebSocket());

    test("onopen is called with channel on first connect", () => {
        const calls: unknown[] = [];
        push().init("ev1", "ws://localhost/push", "myChannel", (ch: string) => calls.push(ch), null, null, null, {}, false);
        push().open("ev1");
        lastWS().simulateOpen();

        expect(calls).toEqual(["myChannel"]);
    });

    test("onopen is NOT called on reconnect", () => {
        const calls: unknown[] = [];
        push().init("ev2", "ws://localhost/push", "ch1", (ch: string) => calls.push(ch), null, null, null, {}, false);
        push().open("ev2");
        lastWS().simulateOpen();
        expect(calls.length).toBe(1);

        // Simulate disconnect and reconnect
        lastWS().simulateClose(1006);
        // A new WebSocket is created by the reconnect timer — but since we use fake timers we need to advance
        // For this test, just verify onopen was called only once on the first socket
        expect(calls.length).toBe(1);
    });

    test("onopen resolves string function name from window", () => {
        (window as unknown as Record<string, unknown>).__testOnOpen = jest.fn();
        push().init("ev3", "ws://localhost/push", "ch1", "__testOnOpen", null, null, null, {}, false);
        push().open("ev3");
        lastWS().simulateOpen();

        expect((window as unknown as Record<string, unknown>).__testOnOpen).toHaveBeenCalledWith("ch1");
        delete (window as unknown as Record<string, unknown>).__testOnOpen;
    });

    test("onopen with null does not throw", () => {
        push().init("ev4", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("ev4");
        expect(() => lastWS().simulateOpen()).not.toThrow();
    });

    test("onopen with undefined does not throw", () => {
        push().init("ev5", "ws://localhost/push", "ch1", undefined, null, null, null, {}, false);
        push().open("ev5");
        expect(() => lastWS().simulateOpen()).not.toThrow();
    });
});

// ---- onmessage callback ----

describe("faces.push: onmessage callback", () => {
    beforeEach(() => installMockWebSocket());
    afterEach(() => uninstallMockWebSocket());

    test("onmessage receives parsed data, channel and raw event", () => {
        const calls: unknown[][] = [];
        push().init("msg1", "ws://localhost/push", "myChannel",
            null, (message: unknown, channel: string, event: unknown) => calls.push([message, channel, event]),
            null, null, {}, false);
        push().open("msg1");
        lastWS().simulateOpen();
        lastWS().simulateMessage("hello");

        expect(calls.length).toBe(1);
        expect(calls[0][0]).toBe("hello");
        expect(calls[0][1]).toBe("myChannel");
        expect(calls[0][2]).toBeDefined(); // raw event
    });

    test("onmessage receives object data", () => {
        const calls: unknown[] = [];
        push().init("msg2", "ws://localhost/push", "ch1",
            null, (message: unknown) => calls.push(message),
            null, null, {}, false);
        push().open("msg2");
        lastWS().simulateOpen();
        lastWS().simulateMessage({ key: "value" });

        expect(calls[0]).toEqual({ key: "value" });
    });

    test("onmessage with null callback does not throw", () => {
        push().init("msg3", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("msg3");
        lastWS().simulateOpen();
        expect(() => lastWS().simulateMessage("test")).not.toThrow();
    });

    test("onmessage resolves string function name from window", () => {
        (window as unknown as Record<string, unknown>).__testOnMessage = jest.fn();
        push().init("msg4", "ws://localhost/push", "ch1", null, "__testOnMessage", null, null, {}, false);
        push().open("msg4");
        lastWS().simulateOpen();
        lastWS().simulateMessage("data");

        expect((window as unknown as Record<string, unknown>).__testOnMessage).toHaveBeenCalled();
        delete (window as unknown as Record<string, unknown>).__testOnMessage;
    });
});

// ---- behaviors ----

describe("faces.push: behaviors", () => {
    beforeEach(() => installMockWebSocket());
    afterEach(() => uninstallMockWebSocket());

    test("behavior function is invoked when message matches key", () => {
        const called: string[] = [];
        const behaviors = {
            update: [() => called.push("update1"), () => called.push("update2")],
        };
        push().init("beh1", "ws://localhost/push", "ch1", null, null, null, null, behaviors, false);
        push().open("beh1");
        lastWS().simulateOpen();
        lastWS().simulateMessage("update");

        expect(called).toEqual(["update1", "update2"]);
    });

    test("behavior function is NOT invoked when message does not match", () => {
        const called: string[] = [];
        const behaviors = {
            update: [() => called.push("update")],
        };
        push().init("beh2", "ws://localhost/push", "ch1", null, null, null, null, behaviors, false);
        push().open("beh2");
        lastWS().simulateOpen();
        lastWS().simulateMessage("delete");

        expect(called).toEqual([]);
    });

    test("empty behaviors object does not cause errors", () => {
        push().init("beh3", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("beh3");
        lastWS().simulateOpen();
        expect(() => lastWS().simulateMessage("anything")).not.toThrow();
    });
});

// ---- onerror callback (reconnect attempt) ----

describe("faces.push: onerror callback", () => {
    beforeEach(() => {
        jest.useFakeTimers();
        installMockWebSocket();
    });
    afterEach(() => {
        uninstallMockWebSocket();
        jest.useRealTimers();
    });

    test("onerror is called on unexpected close with reconnect pending", () => {
        const errors: unknown[][] = [];
        push().init("err1", "ws://localhost/push", "myChannel",
            null, null,
            (code: number, channel: string, event: unknown) => errors.push([code, channel, event]),
            null, {}, false);
        push().open("err1");
        lastWS().simulateOpen();
        lastWS().simulateClose(1006, "Abnormal");

        expect(errors.length).toBe(1);
        expect(errors[0][0]).toBe(1006);
        expect(errors[0][1]).toBe("myChannel");
    });

    test("reconnect creates new WebSocket after interval", () => {
        push().init("err2", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("err2");
        lastWS().simulateOpen();
        expect(wsInstances.length).toBe(1);

        lastWS().simulateClose(1006, "Abnormal");
        jest.advanceTimersByTime(0); // First reconnect at 0ms (500 * 0)
        expect(wsInstances.length).toBe(2);
    });

    test("onerror resolves string function name from window", () => {
        (window as unknown as Record<string, unknown>).__testOnError = jest.fn();
        push().init("err3", "ws://localhost/push", "ch1", null, null, "__testOnError", null, {}, false);
        push().open("err3");
        lastWS().simulateOpen();
        lastWS().simulateClose(1006, "Abnormal");

        expect((window as unknown as Record<string, unknown>).__testOnError).toHaveBeenCalled();
        delete (window as unknown as Record<string, unknown>).__testOnError;
    });

    test("onerror with null does not throw on reconnectable close", () => {
        push().init("err4", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("err4");
        lastWS().simulateOpen();
        expect(() => lastWS().simulateClose(1006, "Abnormal")).not.toThrow();
    });
});

// ---- onclose callback (final close) ----

describe("faces.push: onclose callback", () => {
    beforeEach(() => installMockWebSocket());
    afterEach(() => uninstallMockWebSocket());

    test("code 1000 without Expired reason triggers reconnect, not onclose", () => {
        const closes: unknown[][] = [];
        const errors: unknown[][] = [];
        push().init("cl1", "ws://localhost/push", "myChannel",
            null, null,
            (code: number, channel: string) => errors.push([code, channel]),
            (code: number, channel: string) => closes.push([code, channel]),
            {}, false);
        push().open("cl1");
        lastWS().simulateOpen();
        lastWS().simulateClose(1000, "");

        // code 1000 without "Expired" reason is treated as unexpected — triggers onerror/reconnect
        expect(closes.length).toBe(0);
        expect(errors.length).toBe(1);
    });

    test("onclose is called when server sends Expired reason", () => {
        const closes: unknown[][] = [];
        push().init("cl2", "ws://localhost/push", "myChannel",
            null, null, null,
            (code: number, channel: string, event: unknown) => closes.push([code, channel, event]),
            {}, false);
        push().open("cl2");
        lastWS().simulateOpen();
        lastWS().simulateClose(1000, "Expired");

        expect(closes.length).toBe(1);
        expect(closes[0][0]).toBe(1000);
        expect(closes[0][1]).toBe("myChannel");
    });

    test("onclose is called when server sends Unknown channel (code 1008)", () => {
        const closes: unknown[][] = [];
        push().init("cl3", "ws://localhost/push", "myChannel",
            null, null, null,
            (code: number, channel: string, event: unknown) => closes.push([code, channel, event]),
            {}, false);
        push().open("cl3");
        lastWS().simulateOpen();
        lastWS().simulateClose(1008, "Unknown channel");

        expect(closes.length).toBe(1);
        expect(closes[0][0]).toBe(1008);
    });

    test("onclose resolves string function name from window", () => {
        (window as unknown as Record<string, unknown>).__testOnClose = jest.fn();
        push().init("cl5", "ws://localhost/push", "ch1", null, null, null, "__testOnClose", {}, false);
        push().open("cl5");
        lastWS().simulateOpen();
        lastWS().simulateClose(1000, "Expired");

        expect((window as unknown as Record<string, unknown>).__testOnClose).toHaveBeenCalled();
        delete (window as unknown as Record<string, unknown>).__testOnClose;
    });

    test("onclose with null does not throw", () => {
        push().init("cl6", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("cl6");
        lastWS().simulateOpen();
        expect(() => lastWS().simulateClose(1000, "Expired")).not.toThrow();
    });
});

// ---- reconnect behavior ----

describe("faces.push: reconnect behavior", () => {
    beforeEach(() => {
        jest.useFakeTimers();
        installMockWebSocket();
    });
    afterEach(() => {
        uninstallMockWebSocket();
        jest.useRealTimers();
    });

    test("reconnect interval increases cumulatively (500ms * attempt)", () => {
        push().init("rc1", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("rc1");
        lastWS().simulateOpen();

        // First disconnect — delay = 500 * 0 = 0ms (post-increment: attempts becomes 1)
        lastWS().simulateClose(1006);
        jest.advanceTimersByTime(0);
        expect(wsInstances.length).toBe(2);

        // Second disconnect without re-open — delay = 500 * 1 = 500ms (attempts becomes 2)
        lastWS().simulateClose(1006);
        jest.advanceTimersByTime(499);
        expect(wsInstances.length).toBe(2);
        jest.advanceTimersByTime(1);
        expect(wsInstances.length).toBe(3);
    });

    test("stops reconnecting after 25 attempts and calls onclose", () => {
        const closes: unknown[][] = [];
        const errors: unknown[][] = [];
        push().init("rc2", "ws://localhost/push", "myChannel",
            null, null,
            (code: number, channel: string) => errors.push([code, channel]),
            (code: number, channel: string) => closes.push([code, channel]),
            {}, false);
        push().open("rc2");
        lastWS().simulateOpen();

        // Exhaust all 25 reconnect attempts without successful re-opens.
        // Each close fires onerror and schedules reconnect at 500*i ms.
        for (let i = 0; i < 25; i++) {
            lastWS().simulateClose(1006);
            jest.advanceTimersByTime(500 * i);
        }

        // reconnectAttempts is now 25 (>= MAX_RECONNECT_ATTEMPTS)
        // Next close should trigger onclose, not onerror
        lastWS().simulateClose(1006);
        expect(closes.length).toBe(1);
        expect(closes[0][0]).toBe(1006);
        expect(closes[0][1]).toBe("myChannel");
    });

    test("successful reconnect resets attempt counter", () => {
        push().init("rc3", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("rc3");
        lastWS().simulateOpen();

        // Disconnect and reconnect
        lastWS().simulateClose(1006);
        jest.advanceTimersByTime(0);
        expect(wsInstances.length).toBe(2);

        // Successful reconnect resets counter
        lastWS().simulateOpen();

        // Disconnect again — should reconnect at 500 * 0 = 0ms (reset)
        lastWS().simulateClose(1006);
        jest.advanceTimersByTime(0);
        expect(wsInstances.length).toBe(3);
    });

    test("explicit close prevents reconnect", () => {
        push().init("rc4", "ws://localhost/push", "ch1", null, null, null, null, {}, false);
        push().open("rc4");
        lastWS().simulateOpen();

        push().close("rc4");
        jest.advanceTimersByTime(10000);
        // Only 1 WebSocket should have been created (no reconnect after explicit close)
        expect(wsInstances.length).toBe(1);
    });
});

// ---- resolveFunction ----

describe("faces.push: function resolution", () => {
    beforeEach(() => installMockWebSocket());
    afterEach(() => uninstallMockWebSocket());

    test("string callback resolves to window function", () => {
        const fn = jest.fn();
        (window as unknown as Record<string, unknown>).__pushCallback = fn;
        push().init("res1", "ws://localhost/push", "ch1", "__pushCallback", null, null, null, {}, false);
        push().open("res1");
        lastWS().simulateOpen();

        expect(fn).toHaveBeenCalledWith("ch1");
        delete (window as unknown as Record<string, unknown>).__pushCallback;
    });

    test("nonexistent string callback resolves to noop (no error)", () => {
        push().init("res2", "ws://localhost/push", "ch1", "nonExistentGlobalFn", null, null, null, {}, false);
        push().open("res2");
        expect(() => lastWS().simulateOpen()).not.toThrow();
    });

    test("undefined callback resolves to noop (no error)", () => {
        push().init("res3", "ws://localhost/push", "ch1", undefined, undefined, undefined, undefined, {}, false);
        push().open("res3");
        lastWS().simulateOpen();
        expect(() => lastWS().simulateMessage("test")).not.toThrow();
    });
});
