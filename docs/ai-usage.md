# AI Usage

The course allows and expects the use of AI assistants. This document describes
how I used them while building the project. It is about my development process,
not about the AI feature inside the application (see ADR 0003 for that) ![ADR-0003](./0003_ai_integration.md).

## Tools

| Tool | Used for |
|---|---|
| Claude (claude.ai) | planning, domain model review, ADR drafts, wireframe instructions |
| Figma AI agent (www.figma.com) | Generating the high-fidelity screens from my prompts |

## Log

### Domain model (2026-10-11 – 2026-10-12)
- **What I used AI for:** ...
- **Prompts that mattered:** ...
- **What I changed or rejected:** ...

### Architecture decision records (2026-10-12)
...

### Wireframes (2026-10-12)

**What I used AI for:**
I wrote prompts for Figma's AI agent to turn my wireframe sketches into frames. I formed each prompt as a detailed step-by-step instructions for Figma: exact positions, sizes and greyscale colours for every element. I gave these instructions to Figma's AI agent, which created the frames [Project detail, Task form, Dependency editor].
I built the Project list frame myself by following the instructions.

**Prompts that mattered:**

***Project list***

> Desktop web app screen for a project management tool. Top navigation bar with app name "Planner", links "Projects" and "My tasks", language switch "HR | EN" and user avatar on the right. Page title "Projects" with a primary button "New project". Search field and a status filter dropdown above a data table. Table columns: Name, Lead, Status, Due date, Tasks. Eight rows with realistic project names. Pagination below the table. Clean, light, minimal, plenty of white space.


***Meeting notes (AI feature screen)***

> Fill this area with an AI meeting-notes feature, in two columns. Left column: the meeting notes text area, a character counter "2,431 / 10,000", "7 of 10 analyses left today" and a button "Extract tasks". Right column: "Suggested tasks", the line "AI suggestions — nothing is added to the plan until you accept it", and four suggestion cards with an editable title, assignee select, estimate, a quote from the notes, and "Accept" / "Dismiss". First card already accepted, third card with an assignee error "No project member matches "Marko"."

Similar prompts were used for the empty state, the task form, the project detail and the two mobile screens.

**What I changed or decided myself:**
- I renamed and grouped the layers (Top bar, Table header, Row, Pagination...) so that parts can be reused across screens.
- After the first review, I changed the frame background to white, made the top bar lighter, aligned the navigation items and added the language switch and avatar.
- On the task form, I moved the dropdown arrows from the labels into the input fields and fixed the overlapping frames.
- I kept the decisions that matter for the application, such as putting the dependency editor in its own tab instead of in the task form, and showing the cycle error directly under the form instead of in a pop-up, because they match how the cycle check works (see ADR 0002).

**What I learned:**
How to work with frames, rectangles, text, grouping and alignment in Figma, and why wireframes stay in greyscale: they settle the layout and navigation before any colours are chosen.
Also, as I have never used Figma before, this was the first time I used Figma's AI agent as well and the more I used it, the better, more detailed and precise my prompts became. I learned that the AI agent is not perfect, and that I need to check the results carefully and correct them myself.

## What I did without AI

### 1. Domain model
I developed and then drew in draw.io the domain model myself, without AI assistance.

### 2. Architecture decision records

I envisioned the architecture decision records myself, and wrote them in markdown. I used AI to review them and suggest improvements. Then I drew the diagrams in draw.io myself, without AI assistance.

### 3. Low-fidelity wireframes of all main screens.

### 4. Colour variables and text styles.

### 5. The six components (button, input, select, status chip, table row, flash message) with variants and auto layout


## Reflection