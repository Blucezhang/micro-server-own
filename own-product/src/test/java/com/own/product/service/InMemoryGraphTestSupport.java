package com.own.product.service;

import com.own.product.dao.ProductDao;
import com.own.product.dao.ProductPriceChangeEventDao;
import com.own.product.domain.Product;
import com.own.product.domain.ProductPriceChangeEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class InMemoryGraphTestSupport {

    private InMemoryGraphTestSupport() {}

    private static Object objectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "toString" -> "IsolatedGraphRepository@" + Integer.toHexString(System.identityHashCode(proxy));
            default -> throw new UnsupportedOperationException(method.getName());
        };
    }

    public static final class GraphState {
        private final Map<Long, Product> products = new ConcurrentHashMap<Long, Product>();
        private final Map<String, ProductPriceChangeEvent> eventsByKey = new ConcurrentHashMap<String, ProductPriceChangeEvent>();
        private final ConcurrentHashMap<Long, ReentrantLock> productLocks = new ConcurrentHashMap<Long, ReentrantLock>();
        private final ThreadLocal<TxContext> currentTx = new ThreadLocal<TxContext>();
        private final AtomicLong eventIdSeq = new AtomicLong(100L);
        private final AtomicBoolean failNextProductSave = new AtomicBoolean(false);
        private final AtomicBoolean failNextInitialEventSave = new AtomicBoolean(false);
        private final AtomicBoolean failNextMarkProjectedSave = new AtomicBoolean(false);
        private volatile Consumer<Long> afterLockHook;

        public void putProduct(Product product) {
            products.put(product.getId(), copyProduct(product));
        }

        public Product getProduct(Long id) {
            Product stored = products.get(id);
            return stored == null ? null : copyProduct(stored);
        }

        public ProductPriceChangeEvent getEvent(String eventKey) {
            ProductPriceChangeEvent stored = eventsByKey.get(eventKey);
            return stored == null ? null : copyEvent(stored);
        }

        public List<ProductPriceChangeEvent> allEvents() {
            List<ProductPriceChangeEvent> list = new ArrayList<ProductPriceChangeEvent>();
            for (ProductPriceChangeEvent e : eventsByKey.values()) {
                list.add(copyEvent(e));
            }
            return list;
        }

        public void clear() {
            products.clear();
            eventsByKey.clear();
            failNextProductSave.set(false);
            failNextInitialEventSave.set(false);
            failNextMarkProjectedSave.set(false);
            afterLockHook = null;
        }

        public void setFailNextProductSave(boolean value) { failNextProductSave.set(value); }
        public void setFailNextInitialEventSave(boolean value) { failNextInitialEventSave.set(value); }
        public void setFailNextMarkProjectedSave(boolean value) { failNextMarkProjectedSave.set(value); }
        public void setAfterLockHook(Consumer<Long> hook) { this.afterLockHook = hook; }
        public boolean isGraphTxActive() { return currentTx.get() != null; }

        private void requireGraphTxAndNoJpaTx() {
            if (currentTx.get() == null) {
                throw new IllegalStateException("Expected active neo4jTransactionManager transaction");
            }
            if (TransactionSynchronizationManager.isActualTransactionActive()
                    && !"neo4jTransactionManager".equals(currentTx.get().managerName)) {
                throw new IllegalStateException("Unexpected non-Neo4j transaction active");
            }
        }

        public ProductDao createProductDao() {
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if (method.getDeclaringClass() == Object.class) return objectMethod(proxy, method, args);
                    String name = method.getName();
                    if ("lockProductById".equals(name)) {
                        requireGraphTxAndNoJpaTx();
                        Long id = (Long) args[0];
                        ReentrantLock lock = productLocks.computeIfAbsent(id, k -> new ReentrantLock());
                        lock.lock();
                        currentTx.get().acquiredLocks.add(lock);
                        Consumer<Long> hook = afterLockHook;
                        if (hook != null) {
                            hook.accept(id);
                        }
                        Product p = products.get(id);
                        if (p == null) {
                            return null;
                        }
                        long nextRev = (p.getPriceWriteRevision() == null ? 0L : p.getPriceWriteRevision()) + 1L;
                        p.setPriceWriteRevision(nextRev);
                        return copyProduct(p);
                    }
                    if ("save".equals(name)) {
                        requireGraphTxAndNoJpaTx();
                        if (failNextProductSave.compareAndSet(true, false)) {
                            throw new RuntimeException("Simulated Neo4j product save failure");
                        }
                        Product p = copyProduct((Product) args[0]);
                        products.put(p.getId(), p);
                        return copyProduct(p);
                    }
                    if ("queryProductById".equals(name)) {
                        return getProduct((Long) args[0]);
                    }
                    if ("queryProduct".equals(name)) {
                        List<Product> list = new ArrayList<Product>();
                        for (Product p : products.values()) {
                            list.add(copyProduct(p));
                        }
                        return list;
                    }
                    throw new UnsupportedOperationException("Unsupported ProductDao method: " + name);
                }
            };
            return (ProductDao) Proxy.newProxyInstance(
                    ProductDao.class.getClassLoader(), new Class<?>[] {ProductDao.class}, handler);
        }

        public ProductPriceChangeEventDao createEventDao() {
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if (method.getDeclaringClass() == Object.class) return objectMethod(proxy, method, args);
                    String name = method.getName();
                    if ("findByEventKey".equals(name)) {
                        requireGraphTxAndNoJpaTx();
                        return getEvent((String) args[0]);
                    }
                    if ("save".equals(name)) {
                        requireGraphTxAndNoJpaTx();
                        ProductPriceChangeEvent event = copyEvent((ProductPriceChangeEvent) args[0]);
                        boolean existed = eventsByKey.containsKey(event.getEventKey());
                        if (!existed && failNextInitialEventSave.compareAndSet(true, false)) {
                            throw new RuntimeException("Simulated Neo4j initial event write failure");
                        }
                        if (existed && Boolean.TRUE.equals(event.getProjected())
                                && failNextMarkProjectedSave.compareAndSet(true, false)) {
                            throw new RuntimeException("Simulated Neo4j markProjected confirmation failure");
                        }
                        if (event.getId() == null) {
                            event.setId(eventIdSeq.incrementAndGet());
                        }
                        eventsByKey.put(event.getEventKey(), event);
                        return copyEvent(event);
                    }
                    if ("findPendingProjections".equals(name)) {
                        int limit = (Integer) args[0];
                        List<ProductPriceChangeEvent> pending = new ArrayList<ProductPriceChangeEvent>();
                        for (ProductPriceChangeEvent e : eventsByKey.values()) {
                            if (Boolean.TRUE.equals(e.getPriceChanged()) && !Boolean.TRUE.equals(e.getProjected())) {
                                pending.add(copyEvent(e));
                            }
                        }
                        pending.sort(Comparator.comparing(ProductPriceChangeEvent::getChangedAtEpochSecond)
                                .thenComparing(ProductPriceChangeEvent::getId));
                        return pending.subList(0, Math.min(limit, pending.size()));
                    }
                    throw new UnsupportedOperationException("Unsupported EventDao method: " + name);
                }
            };
            return (ProductPriceChangeEventDao) Proxy.newProxyInstance(
                    ProductPriceChangeEventDao.class.getClassLoader(),
                    new Class<?>[] {ProductPriceChangeEventDao.class},
                    handler);
        }
    }

    private static final class TxContext {
        private final String managerName = "neo4jTransactionManager";
        private final Map<Long, Product> productSnapshot = new LinkedHashMap<Long, Product>();
        private final Map<String, ProductPriceChangeEvent> eventSnapshot = new LinkedHashMap<String, ProductPriceChangeEvent>();
        private final List<ReentrantLock> acquiredLocks = new ArrayList<ReentrantLock>();
    }

    public static final class GraphTransactionManager extends AbstractPlatformTransactionManager {
        private final GraphState state;

        public GraphTransactionManager(GraphState state) {
            this.state = state;
        }

        @Override
        protected Object doGetTransaction() throws TransactionException {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
            TxContext ctx = new TxContext();
            for (Map.Entry<Long, Product> entry : state.products.entrySet()) {
                ctx.productSnapshot.put(entry.getKey(), copyProduct(entry.getValue()));
            }
            for (Map.Entry<String, ProductPriceChangeEvent> entry : state.eventsByKey.entrySet()) {
                ctx.eventSnapshot.put(entry.getKey(), copyEvent(entry.getValue()));
            }
            state.currentTx.set(ctx);
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
            // Changes are already in state maps; locks release in cleanup.
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
            TxContext ctx = state.currentTx.get();
            if (ctx != null) {
                state.products.clear();
                state.products.putAll(ctx.productSnapshot);
                state.eventsByKey.clear();
                state.eventsByKey.putAll(ctx.eventSnapshot);
            }
        }

        @Override
        protected void doCleanupAfterCompletion(Object transaction) {
            TxContext ctx = state.currentTx.get();
            state.currentTx.remove();
            if (ctx != null) {
                for (int i = ctx.acquiredLocks.size() - 1; i >= 0; i--) {
                    ReentrantLock lock = ctx.acquiredLocks.get(i);
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        }
    }

    public static Product copyProduct(Product src) {
        Product copy = new Product();
        copy.setId(src.getId());
        copy.setName(src.getName());
        copy.setOriginalPrice(src.getOriginalPrice());
        copy.setPromotionPrice(src.getPromotionPrice());
        copy.setStocksNum(src.getStocksNum());
        copy.setSalesNum(src.getSalesNum());
        copy.setCategoryId(src.getCategoryId());
        copy.setBrandId(src.getBrandId());
        copy.setPartyId(src.getPartyId());
        copy.setContent(src.getContent());
        copy.setSkuCode(src.getSkuCode());
        copy.setSpecification(src.getSpecification());
        copy.setSaleStatus(src.getSaleStatus());
        copy.setPriceWriteRevision(src.getPriceWriteRevision());
        return copy;
    }

    public static ProductPriceChangeEvent copyEvent(ProductPriceChangeEvent src) {
        ProductPriceChangeEvent copy = new ProductPriceChangeEvent(
                src.getEventKey(),
                src.getRequestFingerprint(),
                src.getProductId(),
                src.getMerchantId(),
                src.getOriginalPriceBefore(),
                src.getOriginalPriceAfter(),
                src.getPromotionPriceBefore(),
                src.getPromotionPriceAfter(),
                src.getReason(),
                src.getChangedAtEpochSecond(),
                src.getProductSnapshot(),
                src.getPriceChanged(),
                src.getProjected());
        copy.setId(src.getId());
        copy.setProjectedAtEpochSecond(src.getProjectedAtEpochSecond());
        return copy;
    }
}
