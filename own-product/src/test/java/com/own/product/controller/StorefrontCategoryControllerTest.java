package com.own.product.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.own.face.trade.TradeException;
import com.own.face.util.Resp;
import com.own.product.dao.CategoryDao;
import com.own.product.domain.Category;
import com.own.product.dto.PublicCategoryView;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class StorefrontCategoryControllerTest {
    @Test
    @SuppressWarnings("unchecked")
    public void returnsSortedDisplayOnlyCategoriesForValidatedLevel() {
        CategoryDao dao = mock(CategoryDao.class);
        Category later = category(2L, "Tea", "1"); later.setRemark("internal");
        Category first = category(1L, "Coffee", "1");
        when(dao.queryCategoryByLevel("1")).thenReturn(Arrays.asList(later, first));
        Resp response = new StorefrontCategoryController(dao).list("1");
        List<PublicCategoryView> result = (List<PublicCategoryView>) response.getData();
        assertEquals(Long.valueOf(1L), result.get(0).getId());
        assertEquals(3, PublicCategoryView.class.getDeclaredFields().length);
    }

    @Test
    public void rejectsUnexpectedCategoryLevel() {
        try { new StorefrontCategoryController(mock(CategoryDao.class)).list("1 OR 1=1"); fail("level must be validated"); }
        catch (TradeException expected) { assertEquals(422, expected.getStatus()); }
    }

    private Category category(Long id, String name, String level) { Category value = new Category(); value.setId(id); value.setName(name); value.setLevel(level); return value; }
}
