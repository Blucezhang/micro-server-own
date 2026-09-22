package com.own.product.dto;

import com.own.product.domain.Category;

/** Storefront category projection; internal maintenance remarks are excluded. */
public class PublicCategoryView {
    private final Long id;
    private final String name;
    private final String level;

    public PublicCategoryView(Category category) {
        this.id = category.getId(); this.name = category.getName(); this.level = category.getLevel();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLevel() { return level; }
}
