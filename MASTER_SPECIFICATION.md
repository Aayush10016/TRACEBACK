# TRACEBACK — ULTIMATE MASTER BUILD PROMPT
## Full-Stack Implementation, Premium UI/UX, Meta AI, Testing, and Autonomous Recovery

You are an autonomous senior engineering and product-design team responsible for building TRACEBACK into a complete, polished, functional, hackathon-ready application.

Act as a:
- Principal software architect.
- Senior Java and Spring Boot engineer.
- Senior React and TypeScript engineer.
- Product designer and UI/UX specialist.
- AI integration engineer.
- Database engineer.
- QA and test automation engineer.
- Application security engineer.
- Technical documentation writer.

Your responsibility is to implement the actual product, not merely describe it.

Inspect the environment, examine existing files, design the application, implement the backend and frontend, integrate Meta AI where possible, run tests, fix errors, inspect the user interface, and document the actual completion state.

**The frontend's visual quality is as important as the backend's technical correctness.** Do not sacrifice the user experience to produce more code.

Work independently. Make sensible decisions. Minimize unnecessary questions. Avoid wasting context on repeated planning, redundant code generation, and excessive intermediate explanations.

Build as much of the complete application as the environment reasonably permits in one substantial execution.

If a feature is blocked, continue with the remaining work and document the exact blocker.

---

# 1. PROJECT IDENTITY

**Product:** TRACEBACK

**Tagline:** Every failure has a cause. Find it, prove it, and prevent it from happening again.

**Product category:** AI-assisted software incident investigation platform.

TRACEBACK helps developers understand software failures by combining:

1. Controlled failure simulation.
2. Structured execution traces.
3. AI-assisted root-cause investigation.
4. Evidence-backed repair recommendations.
5. Deterministic verification.
6. Historical investigations and technical reports.

The application should allow a user to launch a simulation, inspect what happened, investigate the failure using Meta AI, review a recommended correction, and verify whether the corrected scenario actually works.

The complete experience should be accessible through a polished web interface.

## What TRACEBACK is

TRACEBACK is a standalone web application with the interaction quality of a professional developer tool.

It is not:
- A traditional IDE.
- A basic CRUD application.
- A generic chatbot.
- A static dashboard.
- A collection of fake metrics and hardcoded AI responses.
- A tool that claims to debug every application without implementing that capability.

The initial product will investigate reproducible, controlled software failures.

Keep the architecture extensible enough to support real application logs and additional failure scenarios in the future, but do not overengineer the first release.

---

# 2. MANDATORY PROJECT LOCATION

Create or continue the project at:

`D:\TRACEBACK`

This path is mandatory.

Before making changes:

1. Inspect the working directory.
2. Verify that the D: drive exists and is accessible.
3. Inspect `D:\TRACEBACK` if it already exists.
4. Preserve existing work that is useful.
5. Check whether the directory already contains a Git repository.
6. Never delete unrelated files or destroy existing Git history.
7. Do not silently create the project in another location if D: is inaccessible.

If the directory does not exist, create it.

If the directory contains a partially implemented TRACEBACK project, inspect and continue that implementation rather than starting over.

All application source files, documentation, configuration, and project scripts must reside under `D:\TRACEBACK`.

Inspect the available Java, Maven, Node.js, npm, Git, Docker, and PostgreSQL environments before choosing commands.

Do not unnecessarily modify global developer-tool configurations.

---

# 3. MANDATORY AUTONOMOUS EXECUTION AND RECOVERY

Treat this entire master prompt as the authoritative product specification throughout the implementation.

Do not treat it as a one-time instruction that can be forgotten after planning.

## Whenever you encounter a problem

If you become stuck, encounter a build error, lose track of implementation progress, or are uncertain about the next step:

1. Re-read the relevant sections of this master prompt.
2. Re-examine the repository's actual state.
3. Read the relevant source files, logs, and error messages.
4. Identify the root cause instead of repeatedly attempting the same failed action.
5. Choose the simplest correct solution consistent with this specification.
6. Implement the correction.
7. Run the relevant tests or build command again.
8. Update the implementation checklist and report.
9. Continue with the remaining work.

Do not abandon a feature simply because the first attempt fails.

Do not repeatedly regenerate working files without first inspecting them.

Do not fix one problem by silently breaking another working feature.

Do not substitute a mock implementation for a real feature without explicitly identifying the limitation.

Do not repeatedly retry unavailable external integrations without gaining new information.

## If context or execution time becomes limited

Before stopping, review this master prompt and the current repository state.

Prioritize:

1. A working end-to-end application.
2. Correct simulation behavior.
3. A polished, usable interface.
4. Reliable AI integration.
5. Deterministic verification.
6. Automated tests.
7. Accurate documentation.

Save meaningful progress to the repository.

Update `REPORT.md` with:
- Completed work.
- Incomplete work.
- Tests performed.
- Errors encountered.
- Fixes attempted.
- Current blockers.
- Exact next steps.

If implementation must resume in another session, first reread this master prompt, `README.md`, `REPORT.md`, and the relevant source files.

Verify existing code and test results instead of assuming previously reported progress is correct.

**The goal is the strongest practical TRACEBACK application, not the largest codebase or the most impressive-looking checklist.**

---

# 4. DESIGN TOOLS AND FIGMA WORKFLOW

Before implementing the frontend, establish a coherent design direction.

Inspect whether Figma, Product Design, browser-based design tools, or other relevant design integrations are available and authenticated in the current environment.

If Figma is available:
- Use it to establish the visual direction and design system.
- Design the most important screens.
- Define reusable colors, typography, spacing, and component styles.
- Use the design to guide the actual React implementation.
- Where possible, inspect the resulting implementation against the intended design.

If Product Design or another design tool is available and useful, use it.

If these tools are unavailable:
- Continue with direct implementation using React, TypeScript, and CSS.
- Establish the design system in the codebase.
- Do not repeatedly attempt unavailable integrations.
- Do not falsely claim that a Figma file or prototype was created.

Do not allow an optional design integration to block development.

The final application must have a polished, intentional visual identity regardless of which tools are available.

---

# 5. PREMIUM VISUAL DESIGN — CRITICAL REQUIREMENT

TRACEBACK must look like a professionally designed developer product, not a generic student dashboard.

The interface should feel:
- Technical.
- Precise.
- Intelligent.
- Trustworthy.
- Modern.
- Calm under pressure.
- Visually distinctive.

Use sophisticated observability and developer-tool products as inspiration without copying their exact layouts or branding.

## 5.1 Color system

Use a dark-first interface with carefully controlled AI-inspired blue, cyan, and indigo accents.

Suggested starting palette:

- Main background: `#0B0D12`
- Primary surfaces: `#11151D`
- Elevated surfaces: `#171D28`
- Borders: restrained slate tones
- Primary accent: `#6EA8FF`
- Secondary AI accent: `#67E8F9`
- Optional indigo accent: a muted, restrained shade
- Primary text: soft white
- Secondary text: readable cool gray
- Success: accessible green
- Warning: amber
- Failure: accessible red or coral

These are starting points. Refine the palette if doing so produces a better, more cohesive result.

### AI-inspired visual treatment

Use:
- Subtle blue-tinted surfaces.
- Restrained cyan highlights.
- Soft blue/cyan/indigo ambient gradients.
- Carefully placed illumination around active elements.
- Subtle AI analysis status indicators.
- Clear visual distinction between AI-generated analysis and deterministic results.

Avoid:
- Neon colors everywhere.
- Strong purple tint across the entire interface.
- Excessive glowing borders.
- Giant gradient cards.
- Excessive glassmorphism.
- Decorative gradients that reduce readability.
- Random accent colors.
- Generic AI landing-page aesthetics.

AI color treatments should support the information hierarchy rather than overwhelm it.

## 5.2 Typography

Use a clear, professional typography system.

Prefer:
- Inter or a similarly polished sans-serif for application text.
- A suitable monospace font for event IDs, logs, timestamps, and technical metadata.

Define consistent sizes, weights, line heights, and spacing.

Use appropriate system-font fallbacks.

## 5.3 Layout

Create a desktop-first application optimized for laptop screens, with responsive behavior for smaller viewports.

Use:
- A persistent navigation sidebar.
- A clean top navigation bar.
- Well-structured content regions.
- Strong alignment and consistent spacing.
- Readable information-dense layouts.
- Clear panel boundaries.
- Intentional empty space.
- Thoughtful hover and keyboard-focus states.

Do not turn every component into a rounded card.

Use tables, timelines, split panels, dividers, and typography where appropriate.

## 5.4 Visual consistency

Establish design tokens for:
- Colors.
- Typography.
- Spacing.
- Border radii.
- Borders.
- Shadows.
- Focus states.
- Animation timing.
- Component states.

Create reusable components for repeated interface patterns.

Do not style each page independently with inconsistent values.

## 5.5 Motion

Use restrained, purposeful motion where it improves understanding.

Examples:
- Timeline events appearing during a running simulation.
- Subtle status transitions.
- Smooth panel transitions.
- Loading indicators.
- Small, polished hover effects.

Do not animate everything.

Respect reduced-motion preferences where practical.

## 5.6 Accessibility

Maintain readable contrast, keyboard accessibility, visible focus states, sensible text sizes, and accessible status labels.

Color must not be the only way to distinguish failure, success, and warning.

---

# 6. REQUIRED APPLICATION SCREENS

Build these screens as one coherent product.

## SCREEN A: OVERVIEW DASHBOARD

This is the main entry point.

Include:
- TRACEBACK branding.
- A clear description of the current system state.
- A prominent action to start a simulation.
- Total investigations.
- Failed simulations.
- Duplicate orders detected.
- Verification pass rate when sufficient real data exists.
- Recent investigation history.
- Recent incidents and statuses.
- Backend and AI availability indicators where appropriate.

Every statistic must come from actual backend data.

When there is no data, show a useful empty state and a clear action to create the first simulation.

Do not invent statistics to make the dashboard look impressive.

## SCREEN B: INVESTIGATION WORKSPACE

This is the flagship screen and should receive the greatest design attention.

Build a professional incident-investigation workspace.

### Incident header

Show:
- Scenario name.
- Run ID.
- Creation timestamp.
- Simulation status.
- Event count.
- Order count.
- Investigation status.
- Verification status.

### Execution timeline

Display the actual event sequence.

Each event should expose appropriate information:
- Timestamp.
- Event type.
- Component.
- Severity.
- Message.
- Structured metadata.

Visually distinguish:
- Normal events.
- Payment success.
- Response timeout.
- Retry started.
- Duplicate order detected.
- Workflow completion or failure.

Use a clean chronological timeline with readable labels and an obvious failure path.

### Selected-event inspector

When a user selects an event, display its actual stored details.

Provide:
- Event identifier.
- Timestamp.
- Component.
- Severity.
- Message.
- Relevant metadata.
- Correlation or idempotency identifiers when applicable.

Selecting an AI evidence reference must navigate to or highlight the corresponding actual event.

### AI investigation panel

Display:
- Root-cause hypothesis.
- Causal explanation.
- Evidence references.
- Alternative explanations.
- Missing information.
- Uncertainty.
- Recommended correction.
- Verification plan.

Clearly label the content as AI-generated analysis.

Do not display unsupported claims as established facts.

### Layout

Use a deliberate multi-column or split-panel layout where appropriate.

The event timeline and evidence should remain readable at typical laptop resolutions.

Avoid horizontal overflow and excessive nesting.

## SCREEN C: VERIFICATION

Provide a clear before-and-after comparison.

Show actual values such as:
- Orders created.
- Unique logical orders.
- Duplicate count.
- Workflow status.
- Expected versus actual results.
- Individual test results.

Provide a functional verification action.

Display explicit states:
- Not run.
- Running.
- Passed.
- Failed.
- Error.

Never display success merely because the AI recommended a fix.

## SCREEN D: INVESTIGATION HISTORY

Provide:
- Previous runs.
- Scenario names.
- Timestamps.
- Status indicators.
- Search or filtering where practical.
- Ability to reopen a previous investigation.
- Useful empty states.

Data must be persisted by the backend.

## SCREEN E: SYSTEM STATUS OR SETTINGS

Display useful configuration status:
- Backend availability.
- Database health where safely available.
- Meta API configuration status.
- Configured model name where appropriate.
- User-friendly integration errors.

Never display API keys, authorization headers, or secrets.

Avoid unnecessary configuration complexity.

---

# 7. FRONTEND INTERACTION QUALITY

The UI must not merely look good in screenshots. It must work.

Implement:
- Real navigation.
- Functional buttons.
- Loading states.
- Error states.
- Empty states.
- Success feedback.
- Expandable event details.
- Evidence-linked navigation.
- Copy-to-clipboard for IDs where useful.
- Clear timestamps.
- Consistent tooltips where appropriate.
- Responsive layouts.
- Keyboard-focus indicators.
- Proper disabled states during requests.
- Useful feedback when the backend or Meta API is unavailable.

Do not leave decorative buttons that do nothing.

Do not create fake AI results.

Do not create fake metrics.

Do not display a simulated API success as a real Meta response.

If an optional feature is not implemented, do not present it as operational.

---

# 8. REQUIRED TECHNOLOGY STACK

## Backend

Use:
- Java 17.
- Spring Boot 3.5.15.
- Maven 3.9.16.
- Spring Web.
- Spring Data JPA.
- Jakarta Bean Validation.
- PostgreSQL-compatible persistence.
- Flyway database migrations.
- Spring Boot Actuator.
- JUnit 5.
- Mockito where appropriate.
- Spring Boot Test.

Do not upgrade to Spring Boot 4.x.

Choose compatible dependency versions.

Inspect the installed toolchain and existing project configuration before making changes.

## Frontend

Use:
- React.
- TypeScript.
- Vite.
- A well-organized styling system.
- Reusable components.
- A typed API client.

Use a component library or utility-CSS framework if it genuinely improves implementation quality and is compatible with the project.

Do not add unnecessary dependencies.

## Database

Use PostgreSQL as the intended persistent database.

If an existing PostgreSQL instance is available, use it where practical.

Provide Docker Compose configuration if useful, but do not assume Docker is installed or running.

An H2 profile may be used for tests or development where appropriate, but PostgreSQL support must remain available.

## AI

Integrate a verified, supported Meta model API.

Verify official documentation and model access before choosing API endpoints, model names, authentication formats, or request schemas.

Do not invent API contracts.

The core simulator must function without AI credentials.

---

# 9. CORE PRODUCT SCENARIOS

Implement a deterministic e-commerce order-processing simulator.

All scenarios must be reproducible.

## Scenario 1: NORMAL_ORDER

Expected sequence:
1. Order requested.
2. Payment requested.
3. Payment succeeds.
4. One order is created.
5. Workflow completes successfully.

Expected outcome: exactly one order.

## Scenario 2: DUPLICATE_ORDER

Expected sequence:
1. Order requested.
2. Payment requested.
3. Payment succeeds internally.
4. Client-facing response times out.
5. Retry begins.
6. The retry incorrectly creates another order.
7. Duplicate order is detected.
8. Workflow records the failure.

Expected outcome: the duplicate-order bug is consistently reproduced.

## Scenario 3: PAYMENT_FAILURE

Expected sequence:
1. Order requested.
2. Payment requested.
3. Payment fails.
4. No completed order is created.
5. Workflow records a failure.

## Scenario 4: TIMEOUT_WITH_IDEMPOTENCY

Expected sequence:
1. Order requested.
2. Payment succeeds internally.
3. Response times out.
4. Client retries using the same idempotency key.
5. Existing operation is recognized.
6. No second order is created.
7. Workflow completes without duplication.

Expected outcome: exactly one logical order.

Do not connect to real payment providers or process real financial transactions.

---

# 10. BACKEND ARCHITECTURE

Create a maintainable modular monolith.

Suggested structure:

backend/
  src/main/java/.../
    config/
    controller/
    dto/
    domain/
    entity/
    exception/
    repository/
    service/
    simulator/
    investigation/
    verification/
    integration/meta/

Adjust the structure as needed, while maintaining separation of responsibilities.

Implement these entities or their justified equivalents:

- SimulationRun.
- TraceEvent.
- SimulatedOrder.
- InvestigationResult.
- VerificationResult.

Use UUID identifiers where appropriate.

Implement:
- Proper relationships.
- Useful indexes.
- Database constraints.
- Transaction boundaries.
- Validation.
- Consistent API errors.
- Reliable event ordering.
- Simulation isolation.

Persist sufficient information to reconstruct a past simulation after refreshing the page or restarting the application.

Use Flyway migrations.

Never expose JPA entities directly from public APIs.

---

# 11. STRUCTURED EVENT TRACE

Each trace event should contain appropriate fields such as:

- Event ID.
- Simulation run ID.
- Sequence number.
- Timestamp.
- Event type.
- Component.
- Severity.
- Message.
- Structured metadata.
- Correlation ID.
- Idempotency key where relevant.

Support relevant event types, including:

- ORDER_REQUESTED
- PAYMENT_REQUESTED
- PAYMENT_SUCCEEDED
- PAYMENT_FAILED
- RESPONSE_TIMEOUT
- RETRY_STARTED
- IDEMPOTENCY_KEY_REUSED
- ORDER_CREATED
- DUPLICATE_ORDER_DETECTED
- WORKFLOW_COMPLETED
- WORKFLOW_FAILED

Maintain deterministic ordering.

Ensure that events from separate simulation runs cannot be mixed accidentally.

Validate AI evidence references against actual event IDs belonging to the investigated run.

---

# 12. REST API

Implement, test, and document the following endpoints, refining the structure if necessary:

- GET `/api/health`
- GET `/api/simulations/scenarios`
- POST `/api/simulations/run`
- GET `/api/simulations`
- GET `/api/simulations/{runId}`
- GET `/api/simulations/{runId}/events`
- GET `/api/simulations/{runId}/orders`
- POST `/api/investigations/{runId}`
- GET `/api/investigations/{runId}`
- POST `/api/verification/{runId}`

Requirements:
- Request validation.
- Meaningful HTTP status codes.
- DTOs.
- Consistent error responses.
- Appropriate pagination.
- Graceful handling of missing resources.
- No leaked secrets or internal stack traces.
- Straightforward frontend integration.

Document request and response examples.

---

# 13. META AI INVESTIGATION ENGINE

Implement AI investigation as a real product feature.

The backend should:

1. Retrieve the stored event trace for a simulation.
2. Construct a concise, bounded request containing actual events.
3. Send the request to the configured Meta model.
4. Validate the response.
5. Validate all referenced event IDs.
6. Persist the investigation result.
7. Return the result to the frontend.

Ask the model to produce:

- Root-cause hypothesis.
- Causal explanation.
- Evidence event IDs.
- Direct evidence versus inference.
- Alternative explanations.
- Missing information.
- Uncertainty.
- Recommended correction.
- Verification plan.

Prefer structured JSON output or schema-constrained output if supported by the selected API.

Validate the response before displaying it.

Do not allow the model to invent event IDs and present them as actual evidence.

Do not let the model decide whether verification passed.

## Provider architecture

Use a replaceable provider abstraction.

The configured provider and model should be changeable through environment variables without modifying source code.

Verify the official Meta API contract before implementing the HTTP client.

Do not assume previously mentioned model names, API endpoints, or pricing are correct without verification.

## Error handling

Handle:
- Missing API key.
- Invalid credentials.
- Unsupported models.
- Rate limits.
- Timeouts.
- Malformed responses.
- External API failures.

Use bounded timeouts and controlled retry behavior.

Never log secrets or authorization headers.

Do not repeatedly retry expensive API requests without a clear reason.

The application must still allow simulation and verification when Meta AI is unavailable.

If integration is blocked by missing documentation or credentials, implement the remaining independent functionality and document the exact blocker.

Do not fake successful AI integration.

---

# 14. REPAIR RECOMMENDATIONS

Implement a repair-recommendation workflow.

For the duplicate-order scenario, Meta AI should be able to recommend appropriate idempotency protection and explain why it should prevent duplicate processing.

Show:
- Current behavior.
- Proposed correction.
- Expected outcome.
- Risks and assumptions.
- Verification steps.

Where practical, implement a deterministic repaired simulation mode.

This is a controlled simulation, not a claim that production code has been modified.

Do not automatically edit external repositories, execute arbitrary model-generated code, or run shell commands generated by the AI.

If a code patch is proposed, treat it as a suggestion requiring review.

---

# 15. DETERMINISTIC VERIFICATION ENGINE

Implement verification using Java and executable checks.

For the duplicate-order scenario, verify:
- Number of orders created.
- Number of unique logical orders.
- Duplicate count.
- Workflow status.
- Whether idempotency prevents the duplicate.

Persist:
- Verification ID.
- Run ID.
- Test name.
- Expected value.
- Actual value.
- Pass/fail.
- Execution timestamp.
- Failure explanation.

Return actual verification results to the frontend.

Meta AI may explain a failed verification but must never fabricate a passing result.

Distinguish clearly between:
- AI recommendation generated.
- Correction applied to the simulator.
- Verification not yet run.
- Verification passed.
- Verification failed.

---

# 16. META API KEY CONFIGURATION AND CREDIT MANAGEMENT

The application must support configuring a Meta API key at testing time without changing application source code.

Do not hardcode API keys.

Use environment variables or a Git-ignored local `.env` file.

A possible configuration is:

`META_API_KEY=your_key_here`

Use the exact variable names required by the implemented configuration.

The backend may load environment variables through Spring Boot configuration or a suitable local configuration mechanism.

Requirements:
- `.env` must be Git-ignored.
- `.env.example` must contain placeholders only.
- Never commit actual credentials.
- Never place the key in React or a `VITE_*` variable.
- Never expose it through API responses.
- Never print it in logs.
- Never include it in the final report.
- Never place the secret in screenshots or public documentation.

If the key is not configured, provide a clear explanation in the UI.

When the key is supplied, verify the configuration with one controlled request before conducting additional tests.

Do not consume credits during normal automated tests.

Use mocked model responses for automated tests.

Only run real model calls during explicit integration testing.

## Switching accounts

The project must support changing the configured API key without source-code changes.

Do not hardcode two accounts or automatically switch between keys.

If the user's account credits are exhausted, the user can configure the teammate's key, subject to the applicable credit rules.

The application should report errors accurately rather than claiming that credits remain available.

Use conservative, bounded API calls to avoid unnecessary credit consumption.

Do not assume the two credit allocations can be combined automatically or that all API requests qualify for the credits.

---

# 17. FRONTEND IMPLEMENTATION

Implement the full application using React, TypeScript, and Vite.

Organize the frontend into reusable components and clearly separated responsibilities.

Suggested structure:

frontend/
  src/
    api/
    components/
    hooks/
    layouts/
    pages/
    types/
    styles/
    utils/

Adjust as needed.

The frontend must connect to the real backend.

Requirements:
- Typed API requests and responses.
- Functional navigation.
- Functional buttons.
- Real simulation execution.
- Real event timeline.
- Actual AI investigation results.
- Actual verification results.
- Persisted investigation history.
- Clear loading, empty, error, and success states.
- No fake metrics.
- No fabricated AI output.
- No broken routes.
- No API keys in frontend code.
- Responsive laptop layout.
- Successful production build.

The investigation workspace should receive the most design attention.

---

# 18. LOCAL DEVELOPMENT EXPERIENCE

Create:
- `README.md`
- `.env.example`
- `.gitignore`
- `docker-compose.yml` if useful
- `scripts/start-backend.ps1`
- `scripts/start-frontend.ps1`
- `scripts/run-tests.ps1`

The README must contain exact instructions for:

1. Prerequisites.
2. Starting PostgreSQL.
3. Configuring environment variables.
4. Running backend tests.
5. Starting the backend.
6. Installing frontend dependencies.
7. Starting the frontend.
8. Running the complete demonstration.
9. Configuring Meta AI.
10. Switching API keys safely.
11. Troubleshooting common errors.

Do not assume Docker or PostgreSQL is running without checking.

Do not create scripts that delete user data.

Keep local secrets and build artifacts out of Git.

---

# 19. TESTING REQUIREMENTS

Implement backend tests for at least:

1. Normal order creates exactly one order.
2. Duplicate-order scenario reproduces the duplicate.
3. Payment failure creates no completed order.
4. Idempotent retry prevents duplication.
5. Event ordering is correct.
6. Event histories are isolated.
7. Invalid scenarios are rejected.
8. Unknown run IDs return appropriate errors.
9. Evidence references are validated.
10. Malformed AI responses are handled.
11. Missing API key is handled correctly.
12. Verification matches actual simulation data.
13. AI calls are mocked in automated tests.
14. API validation and error handling work.

Add frontend tests for critical interactions where practical.

Run:

Backend:

`mvn clean verify`

Frontend:

`npm run build`

Also run configured frontend tests and linting.

When commands fail:
1. Inspect the error.
2. Determine the cause.
3. Fix the implementation.
4. Rerun the relevant command.
5. Document any remaining failure.

Do not claim success without actually executing the command and checking its output.

---

# 20. UI QUALITY ASSURANCE

Do not consider the frontend complete merely because the code compiles.

If browser access, screenshots, or visual-inspection tools are available:

1. Start the backend and frontend.
2. Open the application.
3. Inspect the overview dashboard.
4. Inspect the investigation workspace.
5. Inspect verification.
6. Inspect history.
7. Test the primary interactions.
8. Check typical laptop dimensions.
9. Check alignment, typography, contrast, overflow, and spacing.
10. Fix visible issues and inspect again.

Verify the complete workflow:

- Start a simulation.
- Open the resulting run.
- Inspect the timeline.
- Run AI investigation.
- Follow an evidence reference.
- Review the repair recommendation.
- Run verification.
- View the result in history.

If browser-based visual inspection is unavailable, conduct a code-level review and explicitly state that the live UI was not visually verified.

Do not claim to have used Figma or inspected the live application unless it actually happened.

---

# 21. GIT AND SECURITY

Check Git availability and repository status.

If the directory is not a repository, initialize Git there.

Preserve existing commits and remotes.

Do not push to a remote repository without explicit authorization.

Do not stage or commit secrets.

Review:
- Input validation.
- API key handling.
- External API error handling.
- AI response validation.
- Event-reference validation.
- Database constraints.
- Exception responses.
- Logging.
- Dependency configuration.

Document any unresolved security concerns.

---

# 22. IMPLEMENTATION ORDER

Work through all phases in one substantial execution.

## Phase A: Environment and repository inspection

Inspect D:, the existing project, tool versions, and dependencies.

## Phase B: Backend

Implement the domain model, migrations, simulator, persistence, APIs, and tests.

## Phase C: Design system and frontend

Establish the visual system and implement the complete application interface.

## Phase D: Frontend/backend integration

Connect the screens to the actual backend and test the primary workflow.

## Phase E: Meta AI integration

Verify the official API contract and implement the real integration.

## Phase F: Verification and quality

Run tests, build the frontend, fix errors, and inspect the UI where possible.

## Phase G: Documentation

Write the README, architecture documentation, API documentation, test report, and remaining-work report.

Do not stop after producing a plan.

Do not stop after generating empty classes or placeholder endpoints.

Do not stop after creating the UI shell.

Continue until the application is working as fully as the environment permits.

---

# 23. REQUIRED DOCUMENTATION

Create these files:

- `README.md`
- `REPORT.md`
- `docs/ARCHITECTURE.md`
- `docs/API.md`
- `docs/TESTING.md`
- `docs/REMAINING_WORK.md`
- `docs/DESIGN_SYSTEM.md`

## REPORT.md

The final report must include:

### 1. Executive summary
What was actually built.

### 2. Environment
Java, Maven, Node.js, npm, Git, PostgreSQL, Docker, and relevant restrictions.

### 3. Architecture
Frontend, backend, database, simulator, AI integration, and verification engine.

### 4. Feature inventory
Features complete, partial, unimplemented, and unverified.

### 5. File inventory
Important files and their purposes.

### 6. API summary
Endpoints, payloads, and error behavior.

### 7. Database
Entities, relationships, migrations, constraints, and indexes.

### 8. Meta API integration
- Whether the real integration is implemented.
- The verified API contract.
- Required configuration.
- Whether credentials were available.
- Whether a real authenticated request was tested.
- Any remaining integration work.

Never include actual secrets.

### 9. Test report
For every important command:
- Exact command.
- Pass/fail result.
- Test count where available.
- Errors encountered.
- Fixes made.

Distinguish automated tests from manual checks.

### 10. Demonstration guide
Exact steps for running all scenarios, AI investigation, repair recommendations, and verification.

### 11. UI quality
Design system, completed screens, responsive behavior, and whether live visual inspection occurred.

### 12. Known limitations
Actual bugs, blockers, and incomplete features.

### 13. Security review
Secret handling, validation, AI response validation, and outstanding risks.

### 14. Credit management
API timeouts, bounded requests, retry policy, and methods to prevent unnecessary model calls.

### 15. Remaining work
Prioritize:
- P0: Must fix before the demonstration.
- P1: Important improvements.
- P2: Optional enhancements.

Explain why each item matters and identify the relevant files or modules.

### 16. Exact next steps
A prioritized checklist for completing the project.

### 17. Final readiness verdict
Answer:
- Can the application run locally?
- Does the backend compile?
- Do backend tests pass?
- Does the frontend build?
- Does the frontend connect to the backend?
- Does the simulator work?
- Does Meta AI work with real credentials?
- Was the live UI visually inspected?
- What remains before submission?

If something has not been verified, state "Not verified."

---

# 24. FINAL PRODUCT QUALITY GATE

Before declaring TRACEBACK complete, evaluate the actual user experience.

Ask:

- Can a first-time user understand the product quickly?
- Can the user launch a simulation without using the terminal?
- Is the failure sequence easy to understand?
- Can the user inspect the evidence behind the AI's diagnosis?
- Are AI hypotheses clearly distinguished from established facts?
- Can the user see the proposed correction?
- Can the user run verification and see the actual result?
- Does the application handle empty data and API failures gracefully?
- Does investigation history persist?
- Are there broken buttons, fake statistics, or misleading success states?
- Does the UI look cohesive and professional?
- Can the complete demonstration run reliably?

Improve the weakest parts before finishing.

If the browser or visual-inspection tools are available, inspect the running application and fix visible issues rather than merely documenting them.

If an external service prevents completion, continue implementing independent functionality and document the precise blocker.

The goal is a credible, polished product that engineers and hackathon judges can understand and evaluate—not just a codebase that compiles.

---

# 25. FINAL EXECUTION RULES

- Follow this master prompt throughout implementation.
- Reread the relevant instructions whenever you get stuck.
- Inspect actual files and errors before attempting fixes.
- Build as much of the real application as possible in this execution.
- Do not stop after planning.
- Do not stop after creating the backend skeleton.
- Do not stop after creating the frontend skeleton.
- Do not claim an integration works without verifying it.
- Do not fabricate AI output, metrics, test results, or UI inspection.
- Do not consume credits during ordinary automated tests.
- Do not expose credentials.
- Do not overengineer the system.
- Do not introduce unnecessary infrastructure.
- Do not destroy existing work.
- Do not ask for confirmation after every phase.
- Make reasonable decisions and continue.
- Keep the README and reports synchronized with the implementation.
- Preserve meaningful progress if interrupted.

If context or execution time becomes limited, save progress and update `REPORT.md` with the precise next steps.

---

# 26. FINAL RESPONSE REQUIREMENTS

After completing the implementation, provide a concise summary containing:

1. Project path.
2. Features actually implemented.
3. UI screens completed.
4. Backend test results.
5. Frontend build results.
6. Whether the real Meta API integration was verified.
7. Whether visual inspection was performed.
8. Remaining blockers.
9. Location of `REPORT.md`.
10. The single most important next action.

Begin by inspecting the environment and `D:\TRACEBACK`.

Then implement the application, test it, fix what you can, inspect the UI where possible, and write the final report.

**Build the strongest practical version of TRACEBACK that you can. The master prompt is the specification: follow it, revisit it when stuck, verify your work, and never misrepresent what has been completed.**