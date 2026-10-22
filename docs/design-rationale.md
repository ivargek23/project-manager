# Design rationale

Figma file: https://www.figma.com/design/J8fzbXuHwKIUMOLCqgbaSi/Project-and-task-management?node-id=1-3&t=kmX0lGVZhGUCmcCe-1
PDF export: `docs/prototype.pdf`

## Starting point

As a first step, I started with greyscale wireframes of the main screens (page *Wireframes* in the Figma file).

After that I made the color variables and text styles, and then the six components (button, input, select, status chip, table row, flash message) by hand, with variants and auto layout.
Later I added two more: an icon button (edit and delete, with a hover state) and a confirmation dialog.

I did not start from a UI kit.

The high-fidelity screens were generated with Figma's AI from prompts I wrote for each screen.

Because the components already existed, I instructed the AI to use them, and to use the colour variables and text styles.

After generating, I checked every screen and fixed what was wrong, for example the table header that didn't line up with the columns, the estimate in the critical path card and the dependency column, which I changed from names to a count.

## Layout decisions

**Navigation**

Every screen has the same top bar: app name, "Projects" and "My tasks", the language switch and the user avatar. 
The language switch is always visible because the app has to work in Croatian and English, and the choice should be one click away.

**Project list**

A table, because users compare projects by lead, status and due date, and a table is the easiest way to compare rows. 
Search and the status filter are above the table, and "New project" is in the top right corner, where the main action of a page usually is.
The last column has edit and delete icons, so a project can be changed or removed directly from the list.

**Delete confirmation**

Deleting a project also deletes its tasks, dependencies and meeting notes, so it has to be confirmed. The dialog names the project and says what will be deleted with it. "Delete" uses the danger colour, and "Cancel" is the secondary button, so the safe choice doesn't look like the main action.

**Empty state**

When there are no projects, the page shows an icon, a short explanation and a "Create project" button in the middle of the page.
It repeats the "New project" action from the header on purpose, because a new user looks at the middle of an empty page first.

**Task form**

One column, 640 px wide, on a white card. One column is easier to read and to fill in than two, and it works the same way on mobile. 
Errors are shown in three places at once: a flash message at the top, a red border on the field and the message directly under the field. The messages are the real ones that go into `messages.properties`.

**Project detail**

A header with the most important numbers (lead, dates, progress, critical path) and tabs under it: Tasks, Dependencies, Schedule, Metrics and Meeting notes. Each of the three advanced requirements has its own tab, so the page doesn't get too long and the user always knows where to find something.

**Meeting notes (AI feature)**

The notes are on the left and the suggested tasks on the right, so the user can compare them side by side. Each suggestion:
- can be edited (title, assignee, estimate) before it is accepted,
- shows the sentence from the notes it came from, so the user can check it,
- has its own "Accept" and "Dismiss" buttons, because nothing can be added to the plan without the project lead clicking it.

## Responsive behaviour (1440 → 390)

**Table → cards**

A table with five columns doesn't fit into 390 px, so on mobile every project is a card. In the app this is a CSS breakpoint over the same Thymeleaf data, not a separate page.

**Full-width buttons, Save on top**

They are easier to tap, and the main action is closer to the thumb.

**Shorter breadcrumb**

On mobile it only shows the way back ("‹ Website Redesign").

**Labels with Fill width**

Croatian labels are usually longer than English ones (for example "Procijenjeno trajanje (radni dani)"), so the labels wrap instead of overflowing.

## Rejected alternatives

| Alternative                                       | Why I didn't use it                                                                                                                                      |
|---------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Names of predecessors in the "Deps" column        | Doesn't scale. A task can have five or more predecessors and the row would break. The table shows a count, and the full list is on the Dependencies tab. |
| White text on strong colours in the status chips  | Harder to reach good contrast, and it looks too loud in a table with many rows. I used dark text on a light tint.                                        |
| An "Open" variant of the select with a drawn list | In the app this is a native `<select>`, and the browser draws the open list itself.                                                                      |
| Two-column task form                              |  Harder to scan, and it would need a different layout on mobile anyway.                                                                                  |
| Separate page for the AI feature                  |  The notes belong to a project, so they are a tab of the project, not a page of their own.                                                               |

## Accessibility and contrast (WCAG AA)

Ratios calculated with the WCAG 2.x formula. Body text needs 4.5:1, borders that identify an input need 3:1 (WCAG 1.4.11).

| Text/element                                      |  Background                    | Ratio | Result                                   |
|---------------------------------------------------|--------------------------------|-------|------------------------------------------|
| text/primary                                      | #1F2328	surface/page #FFFFFF    | 	15.80	 | pass                                     |
| text/primary                                      | #1F2328	surface/subtle #F6F8FA  | 	14.84	 | pass                                     |
| text/secondary                                    | #57606A	surface/page #FFFFFF    | 	6.39	  | pass                                     |
| text/secondary                                    | #57606A	surface/subtle #F6F8FA  | 	6.00	  | pass                                     |
| text/on-primary                                   | #FFFFFF	primary/default #0969DA | 	5.19  | 	pass                                     |
| text/on-primary                                   | #FFFFFF	primary/hover #0550AE   | 	7.59  | 	pass                                     |
| primary/default #0969DA (links, secondary button) | #FFFFFF                        | 	5.19  | 	pass                                     |
| primary/default #0969DA                           | 	#F6F8FA	                        | 4.88  | 	pass                                     |
|  danger/default #CF222E (error messages)	          | #FFFFFF                        | 	5.36  | 	pass                                     |
| Chip "To do" #424A53	                              | #EAEEF2                        | 	7.71  | 	pass                                     |
| Chip "In progress" #0550AE	                        | #DDF4FF                        | 	6.68  | 	pass                                     |
| Chip "Blocked" #A40E26	                            | #FFEBE9                        | 	6.86  | 	pass                                     |
| Chip "Done" #116329	                               | #DAFBE1                        | 	6.64  | 	pass                                     |
| Disabled button text  #FFFFFF                     | 	primary/disabled #8C959F       | 	3.04  | 	exempt (WCAG excludes disabled controls) |
| Input border border/default #D0D7DE	               | #FFFFFF                        | 	1.45  | 	fail → see below                         |

**Input border**

The light border `border/default` is fine for table lines, because they are only decoration. But an input or a select needs a border with at least 3:1, otherwise people with weaker eyesight can't see where the field is. I added a separate variable `border/input (#818B98)`, which has 3.45:1 on white and 3.24:1 on `surface/subtle`, and used it for the fields in Input and Select. Table lines still use `border/default`.

Other things:
- Status chips and flash messages never rely on colour alone: chips have text, and flash messages have an icon (check/alert) and text
- The edit and delete icons have no visible text, so in the app each one has an `aria-label` with the project name (for example "Delete project Website Redesign")
- Icons are from Lucide, the same set I use in the application, so the prototype and the app match

## Planned deviations in the implementation

- The inputs in the suggestion cards (Meeting notes) have no visible labels in the prototype, because the layout is clear enough. In the template they get visually hidden labels (or `aria-label`: "Task title", "Assignee", "Estimate"), so screen readers can read them.
- The Description field is a plain frame in Figma, not a component. In the app it is a `<textarea>` with the same styles as the input.
- The mapping between Figma frames and Thymeleaf templates, with all deviations, is in the README.

## Changes after implementation

The first version of the prototype (`docs/prototype.pdf`, exported on 16 Oct 2026) was made before the templates. While building the project list (issue #19), I found two problems and updated the prototype:

| Change                                                                        | Why                                                                                                                                                                                             |
|-------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Edit and delete icons in every row of the project list (frames 01 and 01m)    | Deleting was only possible from the edit page, so the user had to open a form just to remove a project.                                                                                         |
| Custom confirmation dialog (new frame 01c) instead of the browser's `confirm` | The browser window can't be styled, looks different in every browser, and doesn't match the rest of the app. The custom dialog shows which project will be deleted and what is deleted with it. |

The first version is still in the Git history, in commit `b7aa5be`.
