# AI Usage

The course allows and expects the use of AI assistants. This document describes
how I used them while building the project. It is about my development process,
not about the AI feature inside the application (see ADR 0003 for that) ![ADR-0003](adr/0003_ai_integration.md).

## Tools

| Tool               | Used for                                                                                                       |
|--------------------|----------------------------------------------------------------------------------------------------------------|
| Claude (claude.ai) | planning, domain model review, ADR drafts, wireframe instructions, reviewing my Java code                      |
| Figma AI agent     | creating wireframe frames and high-fidelity screens from my prompts, organizing the Screens page into sections |
| GitHub Copilot     | automatic review of pull requests                                                                              |

## Log

### Domain model (2026-10-11 – 2026-10-12)
- **What I used AI for:** I drew the domain model in draw.io myself and asked Claude to review it against the requirements of the specification after each version.
- **Prompts that mattered:**
    - "Review my domain model against the specification: one-to-many, many-to-many with an association entity, optional relation."
- **What I changed or rejected:**
  - The reviews found relationship lines attached to the wrong classes, typos in names and missing fields. I fixed them myself in draw.io, over several rounds.
  - Claude suggested two dependency types (finish-to-start and start-to-start). I kept only finish-to-start, because it is enough for the critical path and keeps the calculation simpler.
  - Claude suggested a projectRole field on ProjectMember and an assignee on Subtask. I left both out to keep the model simpler. The role is already on the user (ADMIN, PROJECT_LEAD, MEMBER).
  - Claude suggested estimates in hours. I changed them to working days on 2026-10-16, because the critical path and the prototype both work in days (see the update in ADR 0001).

### Architecture decision records (2026-10-12)
...

### Wireframes (2026-10-12)

**What I used AI for:**
I wrote prompts for Figma's AI agent to turn my wireframe sketches into frames. I formed each prompt as a detailed step-by-step instructions for Figma: exact positions, sizes and greyscale colours for every element. I gave these instructions to Figma's AI agent, which created the frames [Project detail, Task form, Dependency editor].
I built the Project list frame myself by following the instructions.

**Prompts that mattered:**

```text
Can you make a wireframe for the Project list screen? If you are unclear on something, ask me before you start. Also, I want to point out this is a student university project, but I am explicitly allowed by professor's official instructions to use generative AI for the project. 

Use greyscale only, and follow these instructions:
1. Colors (for now, only use greyscale, no other colours):
Background: #FFFFFF
Light areas: #F5F5F5 / #EEEEEE
Lines and borders: #E0E0E0
Placeholder bars: #BDBDBD
Button and strong borders: #9E9E9E
Secondary text: #757575
2. Frame
Select the frame Project list: W 1440, H 1024, fill #FFFFFF.
2. Top bar
Bar: rectangle X 0, Y 0, W 1440, H 64, fill #EEEEEE. Add a stroke #E0E0E0, 1 px, and in the stroke settings choose Bottom only.
Logo placeholder: rectangle X 40, Y 18, W 120, H 28, fill #BDBDBD.
Navigation: text “Projects” at X 200, Y 21, and “My tasks” at X 300, Y 21, both 16 px. Select both and use Align vertical centers.
Language switch: rectangle X 1220, Y 16, W 80, H 32, no fill, stroke #9E9E9E, corner radius 4. Centre the text “HR | EN” (14 px) inside it.
Avatar: circle (O) X 1364, Y 14, W 36, H 36, fill #BDBDBD.
3. Title and button
Title: text “Projects”, 28 px, at X 40, Y 100.
Button: rectangle X 1240, Y 100, W 160, H 40, fill #9E9E9E, radius 
5. Centre the text “+ New project” (14 px, white) on it.
6. Search and filter
Search field: rectangle X 40, Y 172, W 1180, H 40, no fill, stroke #9E9E9E, radius 4. Text “Search projects” (14 px,  #757575) at X 52, vertically centred.
Status filter: rectangle X 1240, Y 172, W 160, H 40, same style. Text “Status ▾” inside.
5. Table
The table spans X 40 to 1400, with these column starts:
Column	Text starts at X
Name	56
Lead	600
Status	860
Due date	1080
Actions (⋯)	1360
Header row: rectangle X 40, Y 236, W 1360, H 44, fill #F5F5F5. Texts “Name”, “Lead”, “Status”, “Due date” (14 px) at the X positions above, vertically centred.
First data row (Y 280, height 56):
Name placeholder: rectangle X 56, W 300, H 12, fill #BDBDBD, vertically centred in the row.
Lead placeholder: X 600, W 160, H 12, same style.
Status chip: rectangle X 860, W 80, H 24, no fill, stroke #9E9E9E, radius 12.
Due date placeholder: X 1080, W 100, H 12.
Text “⋯” at X 1360.
Divider line (L) at the bottom of the row: X 40 to 1400, Y 336, #E0E0E0.
Repeat the row: select everything in the first row and group it (Cmd + G), name it Row, then duplicate with Cmd + D and move each copy 56 px lower. You need five rows: Y 280, 336, 392, 448 and 504.
Vary the widths of the name and lead bars a little in each row so it looks like real data.
Table border: rectangle X 40, Y 236, W 1360, H 324, no fill, stroke #E0E0E0.
6. Pagination
Text: “Showing 1–5 of 12” (14 px, #757575) at X 40, Y 584.
Page buttons: five squares of 32 × 32, no fill, stroke #9E9E9E, radius 4, starting at X 1208, Y 580, 8 px apart (X 1208, 1248, 1288, 1328, 1368). Put the texts ‹, 1, 2, 3, › inside them. Give “1” a darker stroke (#616161) to mark the current page.
```

**What I changed or decided myself:**
- I renamed and grouped the layers (Top bar, Table header, Row, Pagination...) so that parts can be reused across screens.
- After the first review, I changed the frame background to white, made the top bar lighter, aligned the navigation items and added the language switch and avatar.
- On the task form, I moved the dropdown arrows from the labels into the input fields and fixed the overlapping frames.
- I kept the decisions that matter for the application, such as putting the dependency editor in its own tab instead of in the task form, and showing the cycle error directly under the form instead of in a pop-up, because they match how the cycle check works (see ADR 0002).

**What I learned:**
How to work with frames, rectangles, text, grouping and alignment in Figma, and why wireframes stay in greyscale: they settle the layout and navigation before any colours are chosen.
Also, as I have never used Figma before, this was the first time I used Figma's AI agent as well and the more I used it, the better, more detailed and precise my prompts became. I learned that the AI agent is not perfect, and that I need to check the results carefully and correct them myself.

### High-fidelity prototype (2026-10-13 – 2026-10-16)

**What I used AI for:** I generated the high-fidelity screens with Figma's AI agent, from a prompt I wrote for each screen. Claude explained the Figma steps (variables, text styles, components with variants, auto layout, prototype connections) one at a time, and calculated the contrast ratios for my colour pairs.

**What I did myself:**

The colour variables, the text styles and the six components (button, input, select, status chip, table row, flash message), with variants and auto layout.
I built the components before generating the screens on purpose, so that the AI would use them instead of drawing its own buttons and inputs. This mostly worked.

**Prompts that mattered:**

Project list:

text
`Desktop web app screen for a project management tool. Top navigation bar with app name "Planner", links "Projects" and "My tasks", language switch "HR | EN" and user avatar on the right. Page title "Projects" with a primary button "New project". Search field and a status filter dropdown above a data table. Table columns: Name, Lead, Status, Due date, Tasks. Eight rows with realistic project names. Pagination below the table. Clean, light, minimal, plenty of white space.
`
Meeting notes (AI feature screen):

text
`Fill this area with an AI meeting-notes feature, in two columns. Left column: the meeting notes text area, a character counter "2,431 / 10,000", "7 of 10 analyses left today" and a button "Extract tasks". Right column: "Suggested tasks", the line "AI suggestions — nothing is added to the plan until you accept it", and four suggestion cards with an editable title, assignee select, estimate, a quote from the notes, and "Accept" / "Dismiss". First card already accepted, third card with an assignee error "No project member matches "Marko"."
`

Similar prompts were used for the empty state, the task form, the project detail and the two mobile screens.

**What I changed after the AI:**

| Screen | Problem | What I did |
|-----|-----|-----|-----|

| Screen | Problem | What I did |
|-----|-----|-----|-----|
| Project list|Table header didn't line up with the columns |	Made the header a "Header" variant of my Table row component|
|Project detail |Critical path said 34 days, but the tasks in the table add up to 32|Changed it to 32|
|Project detail |	"Deps" column showed names of tasks, which overflowed the column |	Changed it to a count, the names are on the Dependencies tab|
|Task form|	Estimate placeholder was "0", which is not a valid value	|Changed it to "e.g. 3"|
|Task form (errors) |	Estimate showed "3" with the error "must be between 1 and 100"|	Changed the value to "0"|
|Meeting notes |	The error card said "Marko", but Marko was not in the notes| Added Marko to the notes, so the case is realistic|
|Meeting notes |	The accepted suggestion still looked editable|	Set its inputs to 50 % opacity|
|Mobile form |	Cancel button was cut off|	Set the frame height to Hug|
|All |	Input borders had only 1.45:1 contrast (found when Claude calculated the ratios) | Added border/input (#818B98, 3.45:1)|

### Persistence layer (2026-10-16 – 2026-10-19)

**GitHub Copilot review of the entities pull request (#56):**

- It found an import of Spring's scheduling Task instead of my own Task entity, and missing AppUser imports. Both were real errors, and I fixed them.
- It reported "invalid sequence names with spaces" in V2__seed_data.sql. This was a false positive, and V2 was already applied, so I didn't change it. An applied Flyway migration is never edited; a fix would go into a new migration.

### Project screens, issue #19 (2026-10-19 – 2026-10-22)

**How I worked:** I asked Claude to lead me through the issue when I would get stuck, to break the task down to steps and lead me one step at a time. I wrote the Java code myself and sent it to Claude for review. I did not use the generated zip files Claude offered with complete classes, because I want to be able to explain every class at the defence.

**What I wrote myself:** `ProjectService`, `ProjectCommand`, `ProjectDetails`, `InvalidLeadException`, `ProjectForm`, the `@ValidDateRange` validator, `ProjectMapper`, the repository queries and the HR/EN validation messages.

**What Claude wrote, at my request:**

- parts of Thymeleaf templates based on my instruction and `app.css` file based on the colors I provided him based on Figma design.

### Prototype update (2026-10-21 – 2026-10-22)

**What I used AI for:** I asked Figma's AI agent to organize the Screens page, and it grouped the frames into sections (project lists, project details, task forms, meeting notes, mobile).

**What I changed:**

- Added the icon button and dialog components, the delete confirmation frame (01c), and edit and delete icons in the project list rows and mobile cards.
- The review found buttons that had switched to the wrong variant, a missing gap in the task form with errors and a thin line above the table header. I fixed them before exporting the new `docs/prototype.pdf`.

### Task and subtask screens, issue #20 (2026-10-22 – 2026-10-23)

**How I worked:**
The same way as for #19: I asked Claude to break the task into steps, solved one at a time, and I sent my code for review after each step.

**What I wrote myself:**
`TaskForm`, `TaskCommand`, `TaskMapper`, `TaskService` (create, update, delete, the assignee check and the subtask methods), the queries in `ProjectMemberRepository`, `SubtaskForm`, `SubtaskRow`, and most of `TaskPageController`.

**What Claude wrote, at my request:**
- `task/form.html`, the edit and delete icons in the task table on the project detail page, and the CSS for the subtasks.

**What Claude's reviews found in my code:**
- `estimateDays` was a `LocalDate` instead of an `Integer`, and the message keys were missing the curly braces, so the user would have seen the key instead of the text.
- The create, update and delete methods had no `@Transactional`, so they ran in the class-level read-only transaction and PostgreSQL would have rejected the inserts.
- `createTask` and `updateTask` ignored the description, due date and assignee.
- `setSubtaskDone` was called without the project id, and one subtask URL was `/subtask` instead of `/subtasks`.

**What I changed or rejected:**
- I wanted to put `@Builder` on `Task`. Claude explained that a class-level builder would bypass the constructor and the fields without setters, and ignore the field initializers unless they are marked `@Builder.Default`. I kept the constructor.
- I wanted task editing in a pop-up instead of a page. I kept the page, because the subtasks live on the edit page, a long form in a dialog is cramped on mobile, and the validation errors work with the normal post-redirect-get flow.
- An AI review of the controller suggested `@ModelAttribute` methods, `UriComponentsBuilder` and `@ExceptionHandler`. I didn't use them: `@ModelAttribute` methods run on every request, including the ones that only redirect, and an `@ExceptionHandler` can't show the form again with the user's input and the error under the field.
- The task names didn't look clickable, so I asked for names in tables to use the link colour.
- I started a rule that the due date can't be in the past, but postponed it after Claude pointed out it would block editing overdue tasks unless the check runs only when the date changes.
- Two of Claude's controller tests had bugs: one sent the estimate parameter twice, so Spring used the first, valid value; and a new helper method had the same name as an existing one. Both were fixed.

## What I did without AI

### 1. Domain model
I developed and then drew in draw.io the domain model myself, without AI assistance.

### 2. Architecture decision records

I envisioned the architecture decision records myself, and wrote them in markdown. I used AI to review them and suggest improvements. Then I drew the diagrams in draw.io myself, without AI assistance.

### 3. Java code of the project screens

The service, the form, the validator, the mapper and the repository queries for issue #19 are written by me. Claude only reviewed them.

## Reflection
