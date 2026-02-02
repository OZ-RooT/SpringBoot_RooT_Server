package io.github._3xhaust.root_server.domain.history.repository;

import io.github._3xhaust.root_server.domain.history.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    @Query("SELECT DISTINCT sh.garageSaleId FROM SearchHistory sh " +
           "WHERE sh.user.id = :userId AND sh.garageSaleId IS NOT NULL " +
           "AND sh.createdAt >= :since " +
           "ORDER BY sh.createdAt DESC")
    List<Long> findRecentGarageSaleIdsByUserId(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("SELECT DISTINCT sh.productId FROM SearchHistory sh " +
           "WHERE sh.user.id = :userId AND sh.productId IS NOT NULL " +
           "AND sh.createdAt >= :since " +
           "ORDER BY sh.createdAt DESC")
    List<Long> findRecentProductIdsByUserId(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("SELECT DISTINCT sh.keyword FROM SearchHistory sh " +
           "WHERE sh.user.id = :userId " +
           "AND sh.createdAt >= :since " +
           "ORDER BY sh.createdAt DESC")
    List<String> findRecentKeywordsByUserId(@Param("userId") Long userId, @Param("since") Instant since);
}
