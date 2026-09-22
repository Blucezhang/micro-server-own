package com.own.product.controller;

import com.own.face.trade.TradeException;
import com.own.face.util.Resp;
import com.own.product.dao.CategoryDao;
import com.own.product.domain.Category;
import com.own.product.dto.PublicCategoryView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Versioned, safe category read model for storefront filters. */
@RestController
@RequestMapping("/product/api/v1/categories")
public class StorefrontCategoryController {
    private final CategoryDao categories;

    public StorefrontCategoryController(CategoryDao categories) { this.categories = categories; }

    @GetMapping
    public Resp list(@RequestParam(defaultValue = "1") String level) {
        if (level == null || !level.matches("[1-9]")) throw TradeException.unprocessable("level must be an integer between 1 and 9");
        List<Category> source = categories.queryCategoryByLevel(level);
        List<PublicCategoryView> result = new ArrayList<PublicCategoryView>();
        if (source != null) for (Category category : source) if (category != null) result.add(new PublicCategoryView(category));
        Collections.sort(result, new Comparator<PublicCategoryView>() {
            @Override public int compare(PublicCategoryView left, PublicCategoryView right) {
                Long a = left.getId(), b = right.getId();
                if (a == null && b == null) return 0; if (a == null) return 1; if (b == null) return -1;
                return a.compareTo(b);
            }
        });
        return new Resp(result);
    }
}
