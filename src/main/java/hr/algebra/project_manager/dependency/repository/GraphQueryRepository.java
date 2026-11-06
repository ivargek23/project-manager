package hr.algebra.project_manager.dependency.repository;

import hr.algebra.project_manager.dependency.domain.TaskDependency;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GraphQueryRepository extends Repository<TaskDependency, Long> {

    @NativeQuery(GraphSqlQueries.IS_REACHABLE)
    boolean isReachable(@Param("projectId") Long projectId,
                        @Param("fromTaskId") Long fromTaskId,
                        @Param("toTaskId") Long toTaskId,
                        @Param("excludedDependencyId") Long excludedDependencyId);

    default boolean isReachable(Long projectId, Long fromTaskId, Long toTaskId) {
        return isReachable(projectId, fromTaskId, toTaskId, -1L);
    }

    @NativeQuery(GraphSqlQueries.PATH_TITLES)
    List<String> findPathTitles(@Param("projectId") Long projectId,
                                @Param("fromTaskId") Long fromTaskId,
                                @Param("toTaskId") Long toTaskId,
                                @Param("excludedDependencyId") Long excludedDependencyId);

    default List<String> findPathTitles(Long projectId, Long fromTaskId, Long toTaskId) {
        return findPathTitles(projectId, fromTaskId, toTaskId, -1L);
    }

    @Query(GraphSqlQueries.NODES)
    List<NodeView> findNodes(@Param("projectId") Long projectId);

    @Query(GraphSqlQueries.EDGES)
    List<EdgeView> findEdges(@Param("projectId") Long projectId);
}
