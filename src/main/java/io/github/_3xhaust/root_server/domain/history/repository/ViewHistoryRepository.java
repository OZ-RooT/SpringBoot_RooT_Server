package io.github._3xhaust.root_server.domain.history.repository;

import io.github._3xhaust.root_server.domain.history.entity.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {
    @Query("SELECT DISTINCT vh.garageSaleId FROM ViewHistory vh " +
           "WHERE vh.user.id = :userId AND vh.garageSaleId IS NOT NULL " +
           "AND vh.createdAt >= :since " +
           "ORDER BY vh.createdAt DESC")
    List<Long> findRecentGarageSaleIdsByUserId(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("SELECT DISTINCT vh.productId FROM ViewHistory vh " +
           "WHERE vh.user.id = :userId AND vh.productId IS NOT NULL " +
           "AND vh.createdAt >= :since " +
           "ORDER BY vh.createdAt DESC")
    List<Long> findRecentProductIdsByUserId(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("SELECT COUNT(DISTINCT vh.user.id) FROM ViewHistory vh " +
           "WHERE vh.garageSaleId = :garageSaleId")
    Long countViewersByGarageSaleId(@Param("garageSaleId") Long garageSaleId);

    @Query("SELECT COUNT(DISTINCT vh.user.id) FROM ViewHistory vh " +
           "WHERE vh.productId = :productId")
    Long countViewersByProductId(@Param("productId") Long productId);
}
