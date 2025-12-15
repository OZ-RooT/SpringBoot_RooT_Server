package io.github._3xhaust.root_server.domain.tag.repository;

import io.github._3xhaust.root_server.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    List<Tag> findByNameIn(List<String> names);
    List<Tag> findByCategory(String category);
    boolean existsByName(String name);
}

