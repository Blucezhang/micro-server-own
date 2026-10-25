package com.own.product.dao;

import com.own.product.domain.ProductPriceChangeEvent;
import java.util.List;
import org.springframework.data.neo4j.repository.query.Query;

public interface ProductPriceChangeEventDao extends BaseDao<ProductPriceChangeEvent> {

    @Query("MATCH (e:ProductPriceChangeEvent) WHERE e.eventKey = $0 RETURN e LIMIT 1")
    ProductPriceChangeEvent findByEventKey(String eventKey);

    @Query("MATCH (e:ProductPriceChangeEvent) WHERE e.priceChanged = true AND e.projected = false RETURN e ORDER BY e.changedAtEpochSecond ASC, id(e) ASC LIMIT $0")
    List<ProductPriceChangeEvent> findPendingProjections(int limit);
}
