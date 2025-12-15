package io.github._3xhaust.root_server.domain.tag.repository;

import io.github._3xhaust.root_server.domain.tag.entity.ProductTag;
import io.github._3xhaust.root_server.domain.tag.entity.ProductTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductTagRepository extends JpaRepository<ProductTag, ProductTagId> {

    @Query("SELECT pt FROM ProductTag pt JOIN FETCH pt.tag WHERE pt.product.id = :productId")
    List<ProductTag> findByProductIdWithTag(@Param("productId") Long productId);

    void deleteByProductId(Long productId);

    @Query("SELECT pt.tag.name FROM ProductTag pt WHERE pt.product.id = :productId")
    List<String> findTagNamesByProductId(@Param("productId") Long productId);
}

