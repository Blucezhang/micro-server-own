package com.own.product.review.moderation.service;

import com.own.face.trade.TradeException;
import com.own.product.review.moderation.domain.ReviewProhibitedTerm;
import com.own.product.review.moderation.dto.ReviewProhibitedTermCommand;
import com.own.product.review.moderation.dto.ReviewProhibitedTermView;
import com.own.product.review.moderation.repository.ReviewProhibitedTermRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewProhibitedTermService {
    private final ReviewProhibitedTermRepository repository;
    public ReviewProhibitedTermService(ReviewProhibitedTermRepository repository) { this.repository = repository; }

    @Transactional
    public ReviewProhibitedTermView create(Long systemUserId, ReviewProhibitedTermCommand command) {
        String term = normalize(command == null ? null : command.getTerm());
        if (repository.findByNormalizedTerm(term) != null) throw TradeException.conflict("review prohibited term already exists");
        try { return view(repository.save(new ReviewProhibitedTerm(term, systemUserId))); }
        catch (DataIntegrityViolationException exception) { throw TradeException.conflict("review prohibited term already exists"); }
    }

    @Transactional
    public ReviewProhibitedTermView update(Long systemUserId, Long id, ReviewProhibitedTermCommand command) {
        ReviewProhibitedTerm current = id == null ? null : repository.findById(id).orElse(null);
        if (current == null) throw TradeException.notFound("review prohibited term was not found");
        if (command == null || command.getActive() == null) throw TradeException.unprocessable("active is required");
        String term = command.getTerm() == null ? current.getNormalizedTerm() : normalize(command.getTerm());
        ReviewProhibitedTerm duplicate = repository.findByNormalizedTerm(term);
        if (duplicate != null && !current.getId().equals(duplicate.getId())) throw TradeException.conflict("review prohibited term already exists");
        current.update(term, command.getActive().booleanValue(), systemUserId);
        try { return view(repository.save(current)); }
        catch (DataIntegrityViolationException exception) { throw TradeException.conflict("review prohibited term already exists"); }
    }

    public Map<String, Object> page(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw TradeException.unprocessable("page must be nonnegative and size must be 1..100");
        Page<ReviewProhibitedTerm> result = repository.findAllByOrderByIdDesc(PageRequest.of(page, size));
        List<ReviewProhibitedTermView> items = new ArrayList<ReviewProhibitedTermView>();
        for (ReviewProhibitedTerm term : result.getContent()) items.add(view(term));
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("total", result.getTotalElements()); response.put("page", page); response.put("size", size); response.put("items", items);
        return response;
    }

    public ReviewProhibitedTermView view(ReviewProhibitedTerm term) { return new ReviewProhibitedTermView(term); }

    private String normalize(String source) {
        String term = source == null ? "" : source.trim().toLowerCase(Locale.ROOT);
        if (term.isEmpty() || term.length() > 100) throw TradeException.unprocessable("term must be 1..100 characters");
        return term;
    }
}
