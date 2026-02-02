package io.github._3xhaust.root_server.domain.tag.repository;

import io.github._3xhaust.root_server.domain.tag.entity.ProductTag;
import io.github._3xhaust.root_server.domain.tag.entity.ProductTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ProductTagRepository extends JpaRepository<ProductTag, ProductTagId> {

    @Query("SELECT pt FROM ProductTag pt JOIN FETCH pt.tag WHERE pt.product.id = :productId")
    List<ProductTag> findByProductIdWithTag(@Param("productId") Long productId);

    void deleteByProductId(Long productId);

    @Query("SELECT pt.tag.name FROM ProductTag pt WHERE pt.product.id = :productId")
    List<String> findTagNamesByProductId(@Param("productId") Long productId);

    @Query("SELECT DISTINCT pt.tag.name FROM ProductTag pt " +
           "WHERE pt.product.id IN " +
           "(SELECT f.product.id FROM io.github._3xhaust.root_server.domain.product.entity.FavoriteUsedItem f " +
           "WHERE f.user.id = :userId)")
    List<String> findTagNamesByUserId(@Param("userId") Long userId);

    @Query("SELECT pt.tag.name FROM ProductTag pt")
    List<String> findAllTagNames();

    @Query("SELECT pt.tag.name FROM ProductTag pt " +
           "WHERE pt.product.createdAt >= :after")
    List<String> findTagNamesByCreatedAtAfter(@Param("after") Instant after);
}

