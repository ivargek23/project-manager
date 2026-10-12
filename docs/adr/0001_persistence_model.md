# ADR 0001: Persistence model

## Status

Accepted (2026-10-12)

## Context

Tasks within a project depend on each other: Task B cannot start until Task A is finished. A dependency carries its own data, such as a lag in days and the time it was created, and users must be able to create, edit and delete individual dependencies through both the screens and the REST API. 

The application must reject dependency cycles and compute a critical path over these dependencies. Additionally, one of the requirements is that the graph calculations do not load the entire project on every request. The specification also requires a many-to-many relation with an association entity carrying attributes.

There were two main questions to answer prior to implementing the persistence model: how a dependency is stored, and how the graph is read back for computation of the critical path.

## Decision

### 1. Task dependencies as an entity
A dependency is modelled as its own entity, `TaskDependency`, rather than as a `@ManyToMany` collection on `Task`.

This entity has the following attributes: `id`, `predecessor`, `successor`, `lagDays`, `createdAt`.
Both `predecessor` and `successor` have a `@ManyToOne` relation to `Task`, with a fetch type of `LAZY`, so loading a dependency doesn’t automatically load both tasks. Task itself doesn’t have lists of its dependencies; they are only read through `TaskDependencyRepository`, so loading a task never accidentally loads part of the graph.

The `task_dependency` table also stores `project_id`, and there are database-enforced constraints:
    - `UNIQUE (predecessor_id, successor_id)` to prevent duplicate dependencies
    - `CHECK (predecessor_id != successor_id)` to prevent self-dependencies
    - composite foreign key `(predecessor_id, project_id)` and `(successor_id, project_id)` referencing `task (id, project_id)` to ensure that both tasks belong to the same project.

`ProjectMember` follows the same reasoning for the Project ↔ AppUser many-to-many relation, because it carries the `allocationPercent` and `joinedAt` attributes.

### 2. Projections for graph reads
Entities are used for CRUD on screens and through the REST API. Graph computations never load entities; they read only the fields they need:

**Cycle check:** a native recursive query in `DependencyGraphRepository` returns a boolean (yes/no) answer, so no rows are loaded into memory.

**Critical path:** two queries per project return Java records, `TaskNode(id, durationDays, status)` and `Edge(predecessorId, successorId, lagDays)`.

## Alternatives rejected

1. A plain `@ManyToMany` relation on `Task`. The *under the hood* join table cannot hold additional attributes such as `lagDays` or `createdAt`, and a link has no identity of its own, so it cannot be addressed as `/dependencies/{id}` or edited individually. Hibernate also tends to rewrite the whole collection when one element changes, and mapping the collection could lead to accidental lazy loading of the graph.

2. Dependency IDs stored in an array or JSON column on `task`. For example, task 20 would have a column `depends_on BIGINT[]` with the value `{12, 15}`, meaning it waits for tasks 12 and 15. This is compact and needs no extra table, but it was rejected for these reasons:
- No referential integrity. PostgreSQL cannot put a foreign key on the elements of an array or a JSON document. If task 12 is deleted, `{12, 15}` silently keeps pointing to a task that no longer exists, and nothing prevents storing the task's own ID or the ID of a task from another project.
- No proper place for attributes. The lag would have to go into a parallel array or into JSON objects such as `[{"id": 12, "lag": 2}]`, which the database can neither type-check nor constrain.
- Inefficient queries in one direction. "Which tasks does task 20 wait for?" is easy, but "which tasks wait for task 12?" has to search inside the array of every task (`WHERE 12 = ANY(depends_on)`), which requires a special GIN index. The recursive cycle check would also have to unpack arrays at every step.
- Edits conflict with unrelated changes. Adding or removing one dependency rewrites the whole task row, so it clashes (version conflict, `HTTP 409`) with someone editing the same task's title or estimate at the same moment.
- Not a JPA relation. Hibernate would map the column as a plain value, not an association, so it could not be navigated or fetched as a relation and would not satisfy the course requirement for a many-to-many with an association entity.

3. Loading `Task` entities for graph computations, with or without a fetch join or entity graph. Every field is loaded, including descriptions, and every entity is tracked by the persistence context. The cost grows with the size of the project on each request, which contradicts the topic's main constraint.

## Consequences

### Gained
1. Data integrity is enforced by the database, not only by the service layer, so even a bug in the code cannot create a duplicate, self-referencing or cross-project dependency. This way, the service and the database protect the data together: the service checks for cycles, and the database catches duplicates, self-links and links between different projects, even if the service has a bug.

2. Each dependency is a resource of its own, which maps cleanly onto the REST API.

3. Graph reads transfer only IDs and numbers, with no lazy loading, no N+1 queries and no persistence-context overhead.

### Accepted costs

1. More code: an extra entity, repository and mapping, and two read models (entities and projections) that must both be kept in line with the schema.

2. `project_id` in `task_dependency` is redundant data. The service sets it from the predecessor task, and the composite foreign keys reject any inconsistency.

3. The recursive query is native SQL written for PostgreSQL, so it is verified with Testcontainers against a real PostgreSQL instance rather than H2.