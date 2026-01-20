package io.github._3xhaust.root_server.domain.tag.repository;

import io.github._3xhaust.root_server.domain.tag.entity.GarageSaleTag;
import io.github._3xhaust.root_server.domain.tag.entity.GarageSaleTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface GarageSaleTagRepository extends JpaRepository<GarageSaleTag, GarageSaleTagId> {

    @Query("SELECT gst FROM GarageSaleTag gst JOIN FETCH gst.tag WHERE gst.garageSale.id = :garageSaleId")
    List<GarageSaleTag> findByGarageSaleIdWithTag(@Param("garageSaleId") Long garageSaleId);

    void deleteByGarageSaleId(Long garageSaleId);

    @Query("SELECT gst.tag.name FROM GarageSaleTag gst WHERE gst.garageSale.id = :garageSaleId")
    List<String> findTagNamesByGarageSaleId(@Param("garageSaleId") Long garageSaleId);

    @Query("SELECT DISTINCT gst.tag.name FROM GarageSaleTag gst " +
           "WHERE gst.garageSale.id IN " +
           "(SELECT f.garageSale.id FROM io.github._3xhaust.root_server.domain.garagesale.entity.FavoriteGarageSale f " +
           "WHERE f.user.id = :userId)")
    List<String> findTagNamesByUserId(@Param("userId") Long userId);

    @Query("SELECT gst.tag.name FROM GarageSaleTag gst")
    List<String> findAllTagNames();

    @Query("SELECT gst.tag.name FROM GarageSaleTag gst " +
           "WHERE gst.garageSale.createdAt >= :after")
    List<String> findTagNamesByCreatedAtAfter(@Param("after") Instant after);
}

