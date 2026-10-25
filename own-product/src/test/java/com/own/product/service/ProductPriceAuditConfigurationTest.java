package com.own.product.service;

import com.own.product.config.ProductPriceAuditProperties;
import com.own.product.dao.ProductPriceChangeEventDao;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductPriceAuditConfigurationTest {
    @Test
    void realPackagedDefaultsBindAndInvalidLimitsFail() throws Exception {
        var environment = new MockEnvironment();
        new YamlPropertySourceLoader().load("product-runtime", new ClassPathResource("product-runtime.yaml"))
                .forEach(source -> environment.getPropertySources().addLast(source));
        assertEquals(new ProductPriceAuditProperties(50, 30000), Binder.get(environment)
                .bind("own.product.price-audit", Bindable.of(ProductPriceAuditProperties.class)).get());
        assertThrows(IllegalArgumentException.class, () -> new ProductPriceAuditProperties(0, 30000));
        assertThrows(IllegalArgumentException.class, () -> new ProductPriceAuditProperties(51, 30000));
        assertThrows(IllegalArgumentException.class, () -> new ProductPriceAuditProperties(1, 999));
        assertThrows(IllegalArgumentException.class, () -> new ProductPriceAuditProperties(1, 86400001));
    }

    @Test
    void scheduledAndExplicitScansBothRespectTheConfiguredBatchSize() {
        var dao = mock(ProductPriceChangeEventDao.class);
        when(dao.findPendingProjections(3)).thenReturn(List.of());
        var projector = new ProductPriceAuditProjector(mock(ProductPriceAuditSqlWriter.class),
                mock(ProductUpdateGraphTransactionService.class), dao, new ProductPriceAuditProperties(3, 5000));
        assertEquals(0, projector.recoverPendingProjections());
        assertEquals(0, projector.recoverPendingProjections(Integer.MAX_VALUE));
        verify(dao, times(2)).findPendingProjections(3);
        verifyNoMoreInteractions(dao);
    }
}
