package io.github._3xhaust.root_server.domain.tag.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GarageSaleTagId implements Serializable {
    private Long garageSaleId;
    private Long tagId;
}

