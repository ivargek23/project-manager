# AI Usage

The course allows and expects the use of AI assistants. This document describes
how I used them while building the project. It is about my development process,
not about the AI feature inside the application (see ADR 0003 for that) ![ADR-0003](./0003_ai_integration.md).

## Tools

| Tool | Used for |
|---|---|
| Claude (claude.ai) | planning, domain model review, ADR drafts, wireframe instructions |
| Figma AI agent (www.figma.com) | creating wireframe frames from my instructions |

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

## What I did without AI

### 1. Domain model
I developed and then drew in draw.io the domain model myself, without AI assistance.

### 2. Architecture decision records

I envisioned the architecture decision records myself, and wrote them in markdown. I used AI to review them and suggest improvements. Then I drew the diagrams in draw.io myself, without AI assistance.

## Reflection