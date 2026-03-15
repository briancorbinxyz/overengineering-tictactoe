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

# Directory Convention

Extraction documents **existing** features — it does NOT create new branches.
Use `create-new-feature.sh` only when specifying *new* features via `/speckit.specify`.

## How to organize extracted features

For each discovered feature, create the directory and spec file directly:

```
specs/###-<short-name>/spec.md
```

Where `###` is a zero-padded sequential number (001, 002, ...). Before assigning
numbers, check the `specs/` directory for existing entries and continue from the
next available number (e.g., if `specs/005-*` exists, start at `006`). The short
name is 2–4 lowercase words, hyphen-separated.

Do NOT:
• Run `create-new-feature.sh` (that creates branches — unnecessary for extraction)
• Create git branches for extracted features
• Place specs under `.specify/features/`

## Directory structure produced

```
specs/
  FEATURE-INVENTORY.md
  001-game-lifecycle/
    spec.md
  002-game-board/
    spec.md
  003-human-player-input/
    spec.md
  ...
```

## Feature Inventory location

The master `FEATURE-INVENTORY.md` MUST be placed at `specs/FEATURE-INVENTORY.md`.

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
Spec Directory (###-short-name)
Evidence (files/modules/tests)
Confidence level

Example:

Feature: User Authentication
Short Name: user-auth
Directory: 001-user-auth
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

For each feature, create the directory and write the spec file directly:

1. Create `specs/###-short-name/` directory
2. Write the spec content into `specs/###-short-name/spec.md`

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
