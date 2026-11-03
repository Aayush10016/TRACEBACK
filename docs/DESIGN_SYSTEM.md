# Design system and UI

## Current direction — minimal graphite

The product uses neutral graphite surfaces with restrained blue-gray, green and coral only where they support selection or status. Large decorative gradients, boxed workflow art, the sidebar promotion, the decorative version badge and fictitious avatar have been removed. The primary action is off-white. Spacing, typography and borders provide hierarchy.

`frontend/src/styles.css` contains the base responsive component system. `frontend/src/refinement.css`, imported after it, defines the current refinement and overrides: background `#0E1013`, surface `#14161B`, elevated `#1B1E24`, subtle border `#FFFFFF0F`, text `#E7E9ED`, muted `#959AA5`, accent `#B6C5DF`, mint `#94C4AA`, coral `#D898A0`, and an 8px panel radius. Typography uses Inter with Segoe UI/system fallback, and Cascadia Code/Consolas for technical values. System fallbacks remain functional when Google Fonts is unavailable.

## Information hierarchy

- Overview: one restrained introductory region, a clear simulation action, unboxed database metrics and recent run history.
- Investigation: the actual hypothesis, linked evidence and recommendation are visible first. Supporting causal explanation, verification plan, alternatives, missing information and risks remain expandable. Event essentials stay visible; IDs/timestamp/correlation/key and JSON metadata are expandable. The timeline stays visible while reading on desktop.
- Verification: actual before/after values, a functional execution action, measured checks and a protected replay.
- History: persisted rows, clearly labeled current-page filtering, bounded pagination and reopening.
- Status: safe service and selected-provider configuration. No API key is displayed; selected-run analysis is distinguished from key configuration.

All metrics and event/analysis values still come from the backend. No new model requests are needed to open saved analysis. Dashboard/history data refreshes when navigating, so external persisted updates do not remain stale.

## Responsive and accessible behavior

Desktop sidebar becomes a scrollable horizontal navigation under 700px; the investigation stacks under 980px. Tables scroll inside positioned containers, preventing the accessible table label from creating page overflow. Status uses text and icons/dots in addition to color. Semantic controls, visible focus, a skip link, dialog focus containment/Escape and reduced-motion support are retained. Native expandable sections preserve keyboard access to supporting details.

The live product was inspected at 1366×900 and 390×844. All five screens, saved AI evidence links, expanded event details, a new normal simulation and verification were checked. The mobile page fits its viewport. Formal accessibility/screen-reader and browser-matrix audits have not been performed.

## Design reference and evidence

The initial editable Figma direction remains at https://www.figma.com/design/K7qGxbod6fHYBYC8OrHovF. It represents the original visual direction, not the latest refined implementation or a completed component library. Current implementation evidence: `minimal-ui-overview.jpg` and `ui-refinement-results.json`. Details and actual test outcomes are in REPORT.md and TESTING.md.

## Responsive repair — 2026-11-03

Bounded timeline always scrolls; its footer never overlaps rows. Inspector stretches to column width. Phones show all five navigation entries and verification checks as labeled cards. History retains a keyboard-focusable horizontal scroll region and a phone scroll cue. Headers and identifiers wrap; offline state does not imply missing Meta credentials.
