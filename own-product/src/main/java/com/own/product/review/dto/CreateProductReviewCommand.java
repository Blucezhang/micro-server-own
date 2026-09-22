package com.own.product.review.dto;

public class CreateProductReviewCommand {
    private Integer rating; private String content;
    public Integer getRating() { return rating; } public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
}
