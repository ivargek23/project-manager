package hr.algebra.project_manager.dependency.repository;

import hr.algebra.project_manager.dependency.domain.TaskDependency;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GraphQueryRepository extends Repository<TaskDependency, Long> {

    /** Graph nodes of one project: four column per task */
    @Query("""
        SELECT NEW hr.algebra.project_manager.dependency.repository.NodeView(t.id, t.title, t.estimateDays, t.status)
        FROM Task t
        WHERE t.project.id = :projectId
        ORDER BY t.id
    """)
    List<NodeView> findNodes(@Param("projectId") Long projectId);

    /** Graph edges of one project
     * d.predecessor.id reads the FK column
     */
    @Query("""
        SELECT NEW hr.algebra.project_manager.dependency.repository.EdgeView(td.predecessor.id, td.successor.id, td.type, td.lagDays)
        FROM TaskDependency td
        WHERE td.projectId = :projectId
        ORDER BY td.id
    """)
    List<EdgeView> findEdges(@Param("projectId") Long projectId);
}
