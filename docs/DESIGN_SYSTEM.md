# Design system and UI

## Direction

Graphite surfaces, precise typography, restrained blue/cyan accents, clear evidence hierarchy. Figma composition: https://www.figma.com/design/K7qGxbod6fHYBYC8OrHovF. It is an editable design-direction composition with illustrative text clearly labeled, not a live-data prototype or a completed reusable component library. The screenshot is `figma-direction.png`. The actual React product uses backend values exclusively.

## Tokens and components

`frontend/src/styles.css` owns background `#0B0D12`, surface `#11151D`, elevated surface `#171D28`, border `#242C3B`, text `#E8EDF7`, muted `#929FB3`, blue `#91B9FF`, cyan `#83DCE9`, mint `#80D6B1`, coral `#FF929C`, 9px panel radius, 24px base spacing and 140ms transitions. Application text uses Inter with Segoe UI/system fallback; technical IDs use Cascadia Code/Consolas. Inter is requested through Google Fonts; the app remains usable offline with fallbacks.

Reusable patterns include Badge, Empty, CopyId, typed API helpers, shared table rendering, panel headings, primary/secondary/icon buttons, timeline events, status rows, loading bars and notifications. Pages are composed in App.tsx; further component extraction is a P2 maintenance improvement.

## Screens

Overview explains the workflow, opens the scenario dialog, shows actual aggregates, useful empty states and recent runs. Investigation exposes the original run, chronological event timeline, selected-event inspector, expandable metadata, AI configuration/analysis panel and verification action. AI references select and scroll to actual events. Verification compares actual before/after values, displays five checks and the separate protected replay. History offers bounded pages, a current-page text filter and reopening. System status safely exposes service/database readiness, provider/model and honest credential-access state.

## Accessibility and responsive behavior

Semantic navigation/buttons, labels, keyboard focus outlines, skip link, dialog focus containment and Escape handling, status text/icons as well as color, reduced-motion support. Loading requests disable their actions; missing AI credentials disable paid investigation while preserving simulation and verification.

The desktop sidebar becomes horizontal mobile navigation under 700px. Investigation stacks under 980px; tables scroll within their own container. Laptop QA uses 1366×900; phone QA uses 390×844. Automated accessibility auditing and assistive-technology testing have not been performed. Recorded visual/manual outcomes are in REPORT.md.
