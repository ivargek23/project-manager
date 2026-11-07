package hr.algebra.project_manager.dependency.repository;

final class TaskDependencyQueries {
    private TaskDependencyQueries() {}

    static final String FIND_ROWS = """
        SELECT NEW hr.algebra.project_manager.dependency.repository.DependencyRow(
                d.id, p.id, p.title, s.id, s.title, d.lagDays)
        FROM TaskDependency d
        JOIN d.predecessor p
        JOIN d.successor s
        WHERE d.projectId = :projectId
        ORDER BY s.title, p.title
        """;

    static final String FIND_ROW = """
        SELECT NEW hr.algebra.project_manager.dependency.repository.DependencyRow(
                d.id, p.id, p.title, s.id, s.title, d.lagDays)
        FROM TaskDependency d
        JOIN d.predecessor p
        JOIN d.successor s
        WHERE d.projectId = :projectId AND d.id = :id
        """;
}
