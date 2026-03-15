---
description: Reverse engineer ALL features currently implemented in the repository and convert them into Spec-Kit specifications.
---


## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).?\

You are performing a complete feature discovery of an existing software system.

Your goal is to reverse engineer ALL features currently implemented in the repository and convert them into Spec-Kit specifications.

This process must be exhaustive and capable of working on extremely large codebases.

This is NOT a new feature specification task.

The system already exists and must be analyzed.

---

# Objectives

1. Discover every feature implemented in the system

2. Map code to system capabilities

3. Organize capabilities into coherent features

4. Generate a specification for each feature

5. Track progress with a feature extraction task list

6. Continue until the system is fully mapped


---

# Critical Rules

You MUST:

• Document only features that actually exist
• Infer behavior from tests, endpoints, UI flows, and services
• Prefer observable system behavior over implementation details
• Avoid mentioning languages, frameworks, or libraries

You MUST NOT:

• Invent new functionality
• Speculate about future capabilities
• Describe implementation details

Tests are considered authoritative evidence of system behavior.

---

# Branch & Directory Convention

Each extracted feature MUST be created using the `create-new-feature.sh` script
to ensure consistent branch naming and directory structure.

## How to create each feature

For each discovered feature, run:

```bash
bash .specify/scripts/bash/create-new-feature.sh --json --short-name '<short-name>' '<feature description>'
```

This script will:
1. Auto-detect the next available branch number (e.g., `001`, `002`, ...)
2. Create a git branch named `###-<short-name>` (e.g., `001-game-lifecycle`)
3. Create a spec directory at `specs/###-<short-name>/`
4. Copy the spec template into `specs/###-<short-name>/spec.md`
5. Output JSON with `BRANCH_NAME`, `SPEC_FILE`, and `FEATURE_NUM`

**IMPORTANT**: After creating the branch and spec file for a feature, you MUST
switch back to the original extraction branch before creating the next feature:

```bash
git checkout <extraction-branch>
```

The extraction branch is the branch you started on (the branch active when the
command was invoked). All feature branches should be created from this base branch.

## Directory structure produced

```
specs/
  001-game-lifecycle/
    spec.md
  002-game-board/
    spec.md
  003-human-player-input/
    spec.md
  ...
```

## Feature Inventory location

The master `FEATURE-INVENTORY.md` should be placed in `specs/FEATURE-INVENTORY.md`.

Do NOT create features under `.specify/features/` — use the `specs/` directory
as established by the `create-new-feature.sh` script.

---

# Phase 1 — System Indexing

First build a SYSTEM FEATURE MAP.

Analyze:

• README and documentation
• directory structure
• API routes
• controllers/handlers
• CLI commands
• UI routes/pages
• services/modules
• message queues or event handlers
• background workers
• database entities
• integration points
• test suites

From this build a list of **candidate capabilities**.

Example:

Authentication
User accounts
Project management
Search
Notifications
Billing
Admin tools
Data export

These are NOT yet final features.

---

# Phase 2 — Capability Mapping

For each capability identify:

• user actors
• workflows
• entry points (UI/API/CLI)
• related modules
• related tests

Then group related capabilities into FEATURES.

Example:

Capability cluster:

login endpoint
password reset
session validation
logout endpoint

Feature:

User Authentication

---

# Phase 3 — Feature Inventory

Create a master feature inventory at `specs/FEATURE-INVENTORY.md`.

Each entry must include:

Feature Name
Short Name (2–4 words, lowercase, hyphen-separated)
Branch Name (###-short-name, as created by `create-new-feature.sh`)
Evidence (files/modules/tests)
Confidence level

Example:

Feature: User Authentication
Short Name: user-auth
Branch: 001-user-auth
Evidence:

- /api/auth/*

- login tests

- session middleware
    Confidence: High


---

# Phase 4 — Feature Extraction Task List

If more than 10 features are discovered, generate a task list:

Feature Extraction Tasks

[ ] 001-user-auth
[ ] 002-account-management
[ ] 003-project-creation
[ ] 004-project-collaboration
[ ] 005-notifications
[ ] 006-search
[ ] 007-billing
[ ] 008-api-access
[ ] 009-admin-controls
[ ] 010-data-import
[ ] 011-data-export
[ ] 012-reporting

This task list becomes the authoritative progress tracker.

As specs are generated tasks must be marked complete.

---

# Phase 5 — Specification Generation

For each feature:

1. Run `create-new-feature.sh` to create the branch and spec file (see Branch & Directory Convention above)
2. Switch back to the extraction branch
3. Write the spec content into the created `specs/###-short-name/spec.md` file

Each spec must use the same structure as `.specify/templates/spec-template.md` and include:

• Feature Overview
• User Scenarios
• Functional Requirements
• Success Criteria
• Key Entities (if applicable)
• Assumptions
• Edge Cases

Requirements must describe **observable behavior**.

Bad:

"The system stores passwords in a hashed format."

Good:

"Users can securely authenticate using their account credentials."

---

# Phase 6 — Evidence Driven Requirements

Requirements must be derived from evidence such as:

• endpoints
• workflows
• tests
• UI interactions
• CLI commands

If behavior is unclear use:

[NEEDS CLARIFICATION: explanation]

Limit to 3 per feature.

---

# Phase 7 — Large Repository Strategy

If the repository is too large to analyze at once:

1. Analyze directory-by-directory

2. Build a partial feature map

3. Merge maps

4. Continue until the repository is fully covered


Track coverage:

Repository Coverage

[x] auth/
[x] accounts/
[x] projects/
[x] notifications/
[ ] billing/
[ ] analytics/

Do not stop until coverage reaches 100%.

---

# Phase 8 — Output Format

First output:

1. System Feature Map

2. Feature Inventory

3. Feature Extraction Task List


Then begin generating specs sequentially.

Each spec should start with:

Feature:

Then include the full specification.
