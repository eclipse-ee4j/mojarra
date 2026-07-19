#!/usr/bin/env python3
#
# Copyright (c) Contributors to Eclipse Foundation.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
#
"""perfreport.py -- two-way scenario x phase report over two perf-stats dumps.

Reads two `target/perf-stats-<timestamp>.txt` files as produced by PerfBenchIT
(columns: scenario phase count total_us avg_us min_us max_us) and emits
GitHub-flavoured markdown: a scenario x phase delta matrix, a scenario x phase
timings matrix, and a suite total per phase.

Both dumps must come from the same bench session on the same host; comparing a
run against a dump from an earlier session measures the host, not the code.

Usage:
  perfreport.py <a-stats.txt> <b-stats.txt> [--a-label Mojarra] [--b-label MyFaces]
                [--title "..."] [--out report.md]

Delta is per-invocation avg of A vs B; negative means A is faster.
"""
import argparse
import sys
from collections import defaultdict

PHASES = ["RESTORE_VIEW", "APPLY_REQUEST_VALUES", "PROCESS_VALIDATIONS",
          "UPDATE_MODEL_VALUES", "INVOKE_APPLICATION", "RENDER_RESPONSE"]
SHORT = {"RESTORE_VIEW": "RV", "APPLY_REQUEST_VALUES": "ARV", "PROCESS_VALIDATIONS": "PV",
         "UPDATE_MODEL_VALUES": "UMV", "INVOKE_APPLICATION": "IA", "RENDER_RESPONSE": "RR"}

# Report scenarios in the same order as the perf-bench index.xhtml page (component family, then
# variant) rather than alphabetically, so the tables read like the WAR's navigation. Unknown
# families/variants sort to the end.
FAMILY_ORDER = ["form", "table", "repeat", "composite", "foreach", "dynamic", "viewparam"]
VARIANT_ORDER = ["readonly", "build", "inputs", "inputs-ajax", "invalid", "invalid-ajax",
                 "nested", "nested-ajax", "form-ajax", "toggle-ajax", "get"]

# Under this per-invocation average the phase is too short to time meaningfully; a percentage
# over it says more about the clock than about the code.
NOISE_FLOOR_US = 100


def scenario_sort_key(scenario):
    family, _, variant = scenario.partition("-")
    fi = FAMILY_ORDER.index(family) if family in FAMILY_ORDER else len(FAMILY_ORDER)
    vi = VARIANT_ORDER.index(variant) if variant in VARIANT_ORDER else len(VARIANT_ORDER)
    return (fi, vi, scenario)


def parse(path):
    """-> (header, {(scenario, phase): (total_us, count)}) for per-scenario rows only."""
    header = ""
    rows = {}
    with open(path, encoding="utf-8") as file:
        for line in file:
            line = line.strip()
            if line.startswith("# warmup="):
                header = line.lstrip("# ")
            if not line or line.startswith("#") or line.startswith("-"):
                continue
            parts = line.split()
            if len(parts) < 5 or parts[0] == "<TOTAL>" or parts[1] not in PHASES:
                continue
            try:
                count, total_us = int(parts[2]), int(parts[3])
            except ValueError:
                continue
            rows[(parts[0], parts[1])] = (total_us, count)
    if not rows:
        sys.exit(f"perfreport: no per-scenario rows parsed from {path}")
    return header, rows


def avg(rows, key):
    """Per-invocation average us for one (scenario, phase), or None when not exercised."""
    total_us, count = rows.get(key, (0, 0))
    return total_us / count if count else None


def delta_cell(a, b):
    if a is None and b is None:
        return "-"
    if a is None or b is None:
        return "n/a"
    if not b or (a < NOISE_FLOOR_US and b < NOISE_FLOOR_US):
        return "noise"
    return f"{(a - b) / b * 100:+.1f}%"


def time_cell(a, b):
    if a is None and b is None:
        return "-"
    return f"{'-' if a is None else round(a)} / {'-' if b is None else round(b)}"


def table(caption, headers, rows):
    """Markdown table, cells padded to a common column width so it also reads as plain text in a
    console log. First column left-aligned (labels), the rest right-aligned (numbers)."""
    widths = [max(len(row[i]) for row in [headers] + rows) for i in range(len(headers))]

    def render(cells, fill=" "):
        padded = [cells[0].ljust(widths[0], fill)]
        padded += [cell.rjust(width, fill) for cell, width in zip(cells[1:], widths[1:])]
        return "| " + " | ".join(padded) + " |"

    rule = render([":" + "-" * (widths[0] - 1)] + ["-" * (width - 1) + ":" for width in widths[1:]], "-")
    return [f"\n### {caption}\n", render(headers), rule] + [render(row) for row in rows]


def phase_table(caption, scenarios, cell):
    headers = ["scenario"] + [SHORT[phase] for phase in PHASES]
    return table(caption, headers, [[scenario] + [cell(scenario, phase) for phase in PHASES] for scenario in scenarios])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("a")
    parser.add_argument("b")
    parser.add_argument("--a-label", default="A")
    parser.add_argument("--b-label", default="B")
    parser.add_argument("--title", default="Perf bench")
    parser.add_argument("--out")
    args = parser.parse_args()

    a_header, a_rows = parse(args.a)
    _, b_rows = parse(args.b)
    scenarios = sorted({s for (s, _) in list(a_rows) + list(b_rows)}, key=scenario_sort_key)

    suite = defaultdict(lambda: [0, 0, 0, 0])  # phase -> [a_total, a_count, b_total, b_count]
    for (_, phase), (total_us, count) in a_rows.items():
        suite[phase][0] += total_us
        suite[phase][1] += count
    for (_, phase), (total_us, count) in b_rows.items():
        suite[phase][2] += total_us
        suite[phase][3] += count

    out = [f"# {args.title}", "",
           f"{args.a_label} vs {args.b_label}. {a_header}", "",
           f"Δ = per-invocation average of {args.a_label} against {args.b_label}; "
           f"negative means {args.a_label} is faster. "
           f"`noise` = both sides under {NOISE_FLOOR_US}us per invocation, "
           "`-` = phase not exercised by that scenario."]

    out += phase_table("Δ — scenario × phase", scenarios,
                       lambda s, p: delta_cell(avg(a_rows, (s, p)), avg(b_rows, (s, p))))
    out += phase_table(f"avg µs per invocation ({args.a_label} / {args.b_label}) — scenario × phase", scenarios,
                       lambda s, p: time_cell(avg(a_rows, (s, p)), avg(b_rows, (s, p))))

    suite_rows = []
    for phase in PHASES:
        a_total, a_count, b_total, b_count = suite[phase]
        a_avg = a_total / a_count if a_count else None
        b_avg = b_total / b_count if b_count else None
        suite_rows.append([SHORT[phase],
                           "-" if a_avg is None else str(round(a_avg)),
                           "-" if b_avg is None else str(round(b_avg)),
                           delta_cell(a_avg, b_avg)])
    out += table("Suite total per phase",
                 ["phase", f"{args.a_label} µs", f"{args.b_label} µs", "Δ"], suite_rows)

    report = "\n".join(out) + "\n"
    if args.out:
        with open(args.out, "w", encoding="utf-8") as file:
            file.write(report)
    print(report)


if __name__ == "__main__":
    main()
