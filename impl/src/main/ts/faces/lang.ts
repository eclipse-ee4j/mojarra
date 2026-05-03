/** Tiny JS-language helpers shared across the `faces` namespace modules. */

import { UDEF } from "./constants";

export const isNull = (value: unknown): boolean =>
    typeof value === UDEF || (typeof value === "object" && !value);

export const isNotNull = (value: unknown): boolean => !isNull(value);

/** True if a string contains a substring, or an array contains a value. */
export const contains = function (stringOrArray: string | unknown[], value: unknown): boolean {
    return (stringOrArray as string).indexOf(value as string) !== -1;
};
