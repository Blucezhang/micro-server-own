-- Prerequisite before deploying own-product single-source price update:
-- Execute once against Neo4j 3.5 to enforce uniqueness of ProductPriceChangeEvent.eventKey.
CREATE CONSTRAINT ON (e:ProductPriceChangeEvent) ASSERT e.eventKey IS UNIQUE;
