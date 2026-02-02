package io.github._3xhaust.root_server.domain.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecommendationScore<T> {
    private T item;
    private double score;
}
