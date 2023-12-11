package com.firstone.greenjangteo.product.domain.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long productId;
    private String category;
    private int level;
}
