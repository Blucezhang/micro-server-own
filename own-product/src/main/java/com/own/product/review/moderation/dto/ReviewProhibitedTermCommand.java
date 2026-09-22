package com.own.product.review.moderation.dto;

public class ReviewProhibitedTermCommand {
    private String term;
    private Boolean active;
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
