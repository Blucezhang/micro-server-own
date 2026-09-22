package com.own.product.review.service;

import com.own.face.trade.TradeException;
import com.own.product.review.moderation.domain.ReviewProhibitedTerm;
import com.own.product.review.moderation.repository.ReviewProhibitedTermRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Small deterministic first-line moderation rule; reports and SYSTEM moderation remain authoritative. */
@Component
public class ReviewContentPolicy {
    private final List<String> prohibitedTerms = new ArrayList<String>();
    private final ReviewProhibitedTermRepository repository;

    @Autowired
    public ReviewContentPolicy(@Value("${trade.review.prohibited-terms:}") String configuredTerms,
                               ReviewProhibitedTermRepository repository) {
        this.repository = repository;
        if (configuredTerms == null) return;
        for (String term : configuredTerms.split(",")) {
            String normalized = term == null ? "" : term.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) prohibitedTerms.add(normalized);
        }
    }

    /** Focused-test and non-JPA compatibility constructor. */
    public ReviewContentPolicy(String configuredTerms) { this(configuredTerms, null); }

    public void requireAllowed(String content) {
        String normalized = content == null ? "" : content.toLowerCase(Locale.ROOT);
        for (String term : allTerms()) if (normalized.contains(term)) throw TradeException.unprocessable("review content contains a prohibited term");
    }

    private Set<String> allTerms() {
        Set<String> terms = new LinkedHashSet<String>(prohibitedTerms);
        if (repository != null) {
            List<ReviewProhibitedTerm> managed = repository.findByActiveTrueOrderByIdAsc();
            if (managed != null) for (ReviewProhibitedTerm term : managed) {
                if (term != null && term.getNormalizedTerm() != null && !term.getNormalizedTerm().trim().isEmpty()) {
                    terms.add(term.getNormalizedTerm().trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return terms;
    }
}
