package io.github._3xhaust.root_server.domain.tag.service;

import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.tag.entity.GarageSaleTag;
import io.github._3xhaust.root_server.domain.tag.entity.ProductTag;
import io.github._3xhaust.root_server.domain.tag.entity.Tag;
import io.github._3xhaust.root_server.domain.tag.repository.GarageSaleTagRepository;
import io.github._3xhaust.root_server.domain.tag.repository.ProductTagRepository;
import io.github._3xhaust.root_server.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final GarageSaleTagRepository garageSaleTagRepository;

    public List<String> getProductTags(Long productId) {
        return productTagRepository.findTagNamesByProductId(productId);
    }

    public List<String> getGarageSaleTags(Long garageSaleId) {
        return garageSaleTagRepository.findTagNamesByGarageSaleId(garageSaleId);
    }

    @Transactional
    public void addTagsToProduct(Product product, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;

        List<Tag> tags = getOrCreateTags(tagNames, "PRODUCT");
        for (Tag tag : tags) {
            ProductTag productTag = ProductTag.builder()
                    .product(product)
                    .tag(tag)
                    .build();
            productTagRepository.save(productTag);
        }
    }

    @Transactional
    public void updateProductTags(Product product, List<String> tagNames) {
        productTagRepository.deleteByProductId(product.getId());
        addTagsToProduct(product, tagNames);
    }

    @Transactional
    public void addTagsToGarageSale(GarageSale garageSale, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;

        List<Tag> tags = getOrCreateTags(tagNames, "GARAGE_SALE");
        for (Tag tag : tags) {
            GarageSaleTag garageSaleTag = GarageSaleTag.builder()
                    .garageSale(garageSale)
                    .tag(tag)
                    .build();
            garageSaleTagRepository.save(garageSaleTag);
        }
    }

    @Transactional
    public void updateGarageSaleTags(GarageSale garageSale, List<String> tagNames) {
        garageSaleTagRepository.deleteByGarageSaleId(garageSale.getId());
        addTagsToGarageSale(garageSale, tagNames);
    }

    private List<Tag> getOrCreateTags(List<String> tagNames, String category) {
        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .name(tagName)
                            .category(category)
                            .build()));
            tags.add(tag);
        }
        return tags;
    }

    public List<String> generateTagsFromContent(String title, String description, String body) {
        return new ArrayList<>();
    }
}
