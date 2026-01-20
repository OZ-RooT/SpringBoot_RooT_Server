package io.github._3xhaust.root_server.domain.garagesale.repository;

import io.github._3xhaust.root_server.domain.garagesale.entity.FavoriteGarageSale;
import io.github._3xhaust.root_server.domain.garagesale.entity.FavoriteGarageSaleId;
import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteGarageSaleRepository extends JpaRepository<FavoriteGarageSale, FavoriteGarageSaleId> {
    @Query("SELECT f.garageSale FROM FavoriteGarageSale f WHERE f.user.id = :userId")
    List<GarageSale> findGarageSalesByUserId(@Param("userId") Long userId);

    Optional<FavoriteGarageSale> findByUserIdAndGarageSaleId(Long userId, Long garageSaleId);

    boolean existsByUserIdAndGarageSaleId(Long userId, Long garageSaleId);

    void deleteByUserIdAndGarageSaleId(Long userId, Long garageSaleId);

    @Query("SELECT COUNT(f) FROM FavoriteGarageSale f WHERE f.garageSale.id = :garageSaleId")
    Long countByGarageSaleId(@Param("garageSaleId") Long garageSaleId);
}

