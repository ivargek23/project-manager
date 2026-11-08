package hr.algebra.project_manager.dependency.repository;

final class GraphSqlQueries {

    private GraphSqlQueries() {
    }

    // language=PostgreSQL
    static final String IS_REACHABLE = """
        WITH RECURSIVE reachable(task_id) AS (
            SELECT td.successor_id
            FROM task_dependency td
            WHERE td.project_id = :projectId
              AND td.predecessor_id = :fromTaskId
              AND td.id <> :excludedDependencyId
            UNION
            SELECT td.successor_id
            FROM task_dependency td
            JOIN reachable r ON td.predecessor_id = r.task_id
            WHERE td.project_id = :projectId
              AND td.id <> :excludedDependencyId
        )
        SELECT EXISTS (SELECT 1 FROM reachable WHERE task_id = :toTaskId)
        """;

    // language=PostgreSQL
    static final String PATH_TITLES = """
        WITH RECURSIVE walk(task_id, path) AS (
            SELECT td.successor_id, ARRAY[td.predecessor_id, td.successor_id]
            FROM task_dependency td
            WHERE td.project_id = :projectId
              AND td.predecessor_id = :fromTaskId
              AND td.id <> :excludedDependencyId
            UNION ALL
            SELECT td.successor_id, w.path || td.successor_id
            FROM walk w
            JOIN task_dependency td ON td.predecessor_id = w.task_id
            WHERE td.project_id = :projectId
              AND td.id <> :excludedDependencyId
              AND w.task_id <> :toTaskId
              AND NOT td.successor_id = ANY (w.path)
        ),
        shortest AS (
            SELECT path
            FROM walk
            WHERE task_id = :toTaskId
            ORDER BY array_length(path, 1)
            LIMIT 1
        )
        SELECT t.title
        FROM shortest s
        CROSS JOIN LATERAL unnest(s.path) WITH ORDINALITY AS p(task_id, position)
        JOIN task t ON t.id = p.task_id
        ORDER BY p.position
        """;

    static final String NODES = """
        SELECT NEW hr.algebra.project_manager.dependency.repository.NodeView(t.id, t.title, t.estimateDays, t.status)
        FROM Task t
        WHERE t.project.id = :projectId
        ORDER BY t.id
    """;

    static final String EDGES = """
        SELECT NEW hr.algebra.project_manager.dependency.repository.EdgeView(td.predecessor.id, td.successor.id, td.type, td.lagDays)
        FROM TaskDependency td
        WHERE td.projectId = :projectId
        ORDER BY td.id
    """;
}
