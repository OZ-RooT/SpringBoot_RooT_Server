package io.github._3xhaust.root_server.domain.garagesale.repository;

import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSaleImage;
import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSaleImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GarageSaleImageRepository extends JpaRepository<GarageSaleImage, GarageSaleImageId> {
    @Modifying
    @Query("DELETE FROM GarageSaleImage gi WHERE gi.garageSale.id = :garageSaleId")
    void deleteByGarageSaleId(@Param("garageSaleId") Long garageSaleId);

    GarageSaleImage findByGarageSaleIdAndImageId(Long garageSaleId, Long imageId);
}
