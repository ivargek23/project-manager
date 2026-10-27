# Permission matrix

## Legend

| Symbol | Permission                      |
|:------:|---------------------------------|
|   🌐   | Global (any project)            |
|   📂   | Own project only                |
|   👤   | Only tasks assigned to the user |
|   🚫   | Forbidden (403)                 |

| Action                               | ADMIN | PROJECT_LEAD | MEMBER |
|--------------------------------------|:-----:|:------------:|:------:|
| Manage user accounts & global roles  | 🌐    | 🚫           | 🚫     |
| Create a project                     | 🌐    | 🌐           | 🚫     |
| View project (+ tasks & subtasks)    | 🌐    | 📂           | 📂     |
| Edit or delete a project             | 🌐    | 📂           | 🚫     |
| Reassign the project lead            | 🌐    | 🚫           | 🚫     |
| Add or remove project members        | 🚫    | 📂           | 🚫     |
| Create/edit/delete a task            | 🚫    | 📂           | 🚫     |
| Update status and progress of a task | 🚫    | 📂           | 👤     |
| Create/edit/delete a subtask         | 🚫    | 📂           |   👤   |
| Add/edit/remove a dependency         | 🚫    | 📂           | 🚫     |
| View schedule and critical path      | 🌐    | 📂           | 📂     |
| View burndown & workload charts      | 🌐    | 📂           | 📂     |
| Run AI extraction on meeting notes   | 🚫    | 📂           | 🚫     |
| Accept/reject an AI candidate task   | 🚫    |      📂      |   🚫   |

## Notes

> **PROJECT_LEAD** is a global role that allows creating projects. 📂 indicates the user is either the lead or a member of that specific project, so leading one project does not mean having access to other projects.

> **ADMIN** manages both users and projects, but does not work inside the plan (tasks, dependencies, AI). The plan belongs to the project lead.

> Every cell of the matrix is covered by a parametrized test that has two possible outcomes: *success* or *403*.







