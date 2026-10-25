package com.own.product.controller;

import com.own.product.dao.ProductDao;
import com.own.product.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RetiredLegacyProductWriteTest {
    @Test
    void legacyWritesHaveNoHandlerWhileReadRemainsAvailable() throws Exception {
        ProductDao products = mock(ProductDao.class);
        ProductController controller = new ProductController();
        ReflectionTestUtils.setField(controller, "productDao", products);
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(put("/product/addProduct").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/product/10").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/product/10")).andExpect(status().isNotFound());
        Product product = new Product(); product.setId(10L); product.setName("test");
        when(products.queryProductById(10L)).thenReturn(product);
        mvc.perform(get("/product/Product/10")).andExpect(status().isOk());
        verify(products, never()).save(any());
        verify(products, never()).deleteProduct(any());
    }
}
