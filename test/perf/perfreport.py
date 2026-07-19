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
GitHub-flavoured markdown: a scenario x phase delta matrix and a scenario x phase
timings matrix, each closed off by suite and per-request totals.

Both dumps must come from the same bench session on the same host; comparing a
run against a dump from an earlier session measures the host, not the code.

Usage:
  perfreport.py <a-stats.txt> <b-stats.txt> [--a-label Mojarra] [--b-label MyFaces]
                [--title "..."] [--out report.md]
                [--fail-on-regression [--fail-over PERCENT]]

Delta is per-invocation avg of A vs B; negative means A is faster. With
--fail-on-regression the exit code is non-zero when any phase's suite total is
slower in A than in B, which is how CI turns the report into a gate.
"""
import argparse
import sys

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

# Row and column label for the aggregates closing off each table.
TOTAL = "TOTAL"


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


def avg(rows, scenario, phase):
    """
    Microseconds one request spends in `phase` for `scenario`, or None when not exercised.
    A None `scenario` aggregates the whole suite, a None `phase` the whole request lifecycle
    (i.e. summing the per-phase averages rather than averaging them, since a request passes
    through every phase once).
    """
    if phase is None:
        totals = [avg(rows, scenario, each) for each in PHASES]
        exercised = [total for total in totals if total is not None]
        return sum(exercised) if exercised else None
    total_us = count = 0
    for (each_scenario, each_phase), (each_total_us, each_count) in rows.items():
        if each_phase == phase and scenario in (None, each_scenario):
            total_us += each_total_us
            count += each_count
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
    """Scenario rows x phase columns, closed off by a TOTAL row (the suite across scenarios) and a
    TOTAL column (one request's whole lifecycle for that scenario)."""
    headers = ["scenario"] + [SHORT[phase] for phase in PHASES] + [TOTAL]
    rows = [[scenario] + [cell(scenario, phase) for phase in PHASES] + [cell(scenario, None)]
            for scenario in scenarios]
    rows.append([TOTAL] + [cell(None, phase) for phase in PHASES] + [cell(None, None)])
    return table(caption, headers, rows)


def suite_regressions(a_rows, b_rows, fail_over):
    """
    Phases whose suite total is slower in A than in B by more than `fail_over` percent, as
    (phase, percent) pairs. Phases too short to time (see NOISE_FLOOR_US) never qualify: a
    percentage over a sub-100us average says more about the clock than about the code.
    """
    regressions = []
    for phase in PHASES:
        a_avg, b_avg = avg(a_rows, None, phase), avg(b_rows, None, phase)
        if a_avg is None or not b_avg or (a_avg < NOISE_FLOOR_US and b_avg < NOISE_FLOOR_US):
            continue
        percent = (a_avg - b_avg) / b_avg * 100
        if percent > fail_over:
            regressions.append((SHORT[phase], percent))
    return regressions


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("a")
    parser.add_argument("b")
    parser.add_argument("--a-label", default="A")
    parser.add_argument("--b-label", default="B")
    parser.add_argument("--title", default="Perf bench")
    parser.add_argument("--out")
    parser.add_argument("--fail-on-regression", action="store_true",
                        help="Exit non-zero when a phase's suite total is slower in A than in B.")
    parser.add_argument("--fail-over", type=float, default=0.0, metavar="PERCENT",
                        help="Tolerance for --fail-on-regression: only a suite total slower by more than "
                             "this many percent fails. Default 0, i.e. any regression fails.")
    args = parser.parse_args()

    a_header, a_rows = parse(args.a)
    _, b_rows = parse(args.b)
    scenarios = sorted({s for (s, _) in list(a_rows) + list(b_rows)}, key=scenario_sort_key)

    out = [f"# {args.title}", "",
           f"{args.a_label} vs {args.b_label}. {a_header}", "",
           f"Δ = per-invocation average of {args.a_label} against {args.b_label}; "
           f"negative means {args.a_label} is faster. "
           f"`noise` = both sides under {NOISE_FLOOR_US}us per invocation, "
           f"`-` = phase not exercised by that scenario. {TOTAL} row = the suite, "
           f"{TOTAL} column = one request through every phase."]

    out += phase_table("Δ — scenario × phase", scenarios,
                       lambda s, p: delta_cell(avg(a_rows, s, p), avg(b_rows, s, p)))
    out += phase_table(f"avg µs per invocation ({args.a_label} / {args.b_label}) — scenario × phase", scenarios,
                       lambda s, p: time_cell(avg(a_rows, s, p), avg(b_rows, s, p)))

    regressions = suite_regressions(a_rows, b_rows, args.fail_over)
    if regressions:
        out += ["", f"**{args.a_label} is slower than {args.b_label} on the suite total of "
                    f"{', '.join(phase for phase, _ in regressions)}.**", ""]
        out += [f"- {phase}: {percent:+.1f}%" for phase, percent in regressions]

    report = "\n".join(out) + "\n"
    if args.out:
        with open(args.out, "w", encoding="utf-8") as file:
            file.write(report)
    print(report)
    if regressions and args.fail_on_regression:
        sys.exit(1)


if __name__ == "__main__":
    main()
