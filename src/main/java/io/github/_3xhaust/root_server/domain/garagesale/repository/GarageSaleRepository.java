package io.github._3xhaust.root_server.domain.garagesale.repository;

import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarageSaleRepository extends JpaRepository<GarageSale, Long> {

    @Query("SELECT g FROM GarageSale g WHERE g.owner.id = :ownerId")
    Page<GarageSale> findByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query(value = """
        SELECT * FROM garage_sales g 
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(g.latitude)) 
        * cos(radians(g.longitude) - radians(:lng)) 
        + sin(radians(:lat)) * sin(radians(g.latitude)))) <= :radius
        """, nativeQuery = true)
    List<GarageSale> findNearbyGarageSales(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radius") Double radiusKm
    );
}

