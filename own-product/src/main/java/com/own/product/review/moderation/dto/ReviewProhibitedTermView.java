package com.own.product.review.moderation.dto;

import com.own.product.review.moderation.domain.ReviewProhibitedTerm;
import java.util.Date;

/** Operational view intentionally omits internal operator identity fields. */
public class ReviewProhibitedTermView {
    private final Long id;
    private final String term;
    private final boolean active;
    private final Date createdAt;
    private final Date updatedAt;
    public ReviewProhibitedTermView(ReviewProhibitedTerm term) {
        this.id = term.getId(); this.term = term.getNormalizedTerm(); this.active = term.isActive();
        this.createdAt = term.getCreatedAt(); this.updatedAt = term.getUpdatedAt();
    }
    public Long getId() { return id; }
    public String getTerm() { return term; }
    public boolean isActive() { return active; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
}
