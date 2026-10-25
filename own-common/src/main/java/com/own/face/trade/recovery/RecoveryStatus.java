package com.own.face.trade.recovery;

import java.util.Set;

/** Persisted/API protocol values: not operator-tunable configuration. */
public final class RecoveryStatus {
    public static final String NONE = "NONE";
    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";
    public static final String RECONCILIATION_REQUIRED = "RECONCILIATION_REQUIRED";
    public static final Set<String> QUERYABLE = Set.of(PENDING, COMPLETED, RECONCILIATION_REQUIRED);

    private RecoveryStatus() { }
}
