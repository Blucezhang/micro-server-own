package com.own.product.review.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.own.face.trade.TradeException;
import com.own.product.review.moderation.domain.ReviewProhibitedTerm;
import com.own.product.review.moderation.dto.ReviewProhibitedTermCommand;
import com.own.product.review.moderation.dto.ReviewProhibitedTermView;
import com.own.product.review.moderation.repository.ReviewProhibitedTermRepository;
import com.own.product.review.moderation.service.ReviewProhibitedTermService;
import java.util.Collections;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class ReviewProhibitedTermServiceTest {
    @Test
    public void systemCanCreateAndDeactivateNormalizedTerm() {
        ReviewProhibitedTermRepository repository = mock(ReviewProhibitedTermRepository.class);
        when(repository.save(any(ReviewProhibitedTerm.class))).thenAnswer(invocation -> {
            ReviewProhibitedTerm value = (ReviewProhibitedTerm) invocation.getArguments()[0];
            if (value.getId() == null) ReflectionTestUtils.setField(value, "id", 7L);
            return value;
        });
        ReviewProhibitedTermService service = new ReviewProhibitedTermService(repository);
        ReviewProhibitedTermCommand create = new ReviewProhibitedTermCommand(); create.setTerm("  Blocked Phrase ");
        ReviewProhibitedTermView created = service.create(9L, create);
        assertEquals("blocked phrase", created.getTerm()); assertTrue(created.isActive());
        ReviewProhibitedTerm stored = new ReviewProhibitedTerm("blocked phrase", 9L); ReflectionTestUtils.setField(stored, "id", 7L);
        when(repository.findById(7L)).thenReturn(stored);
        ReviewProhibitedTermCommand update = new ReviewProhibitedTermCommand(); update.setActive(false);
        assertEquals(false, service.update(10L, 7L, update).isActive());
    }

    @Test(expected = TradeException.class)
    public void managedActiveTermBlocksReviewAndReplyContent() {
        ReviewProhibitedTermRepository repository = mock(ReviewProhibitedTermRepository.class);
        when(repository.findByActiveTrueOrderByIdAsc()).thenReturn(Collections.singletonList(new ReviewProhibitedTerm("managed block", 1L)));
        new ReviewContentPolicy("", repository).requireAllowed("contains MANAGED BLOCK content");
    }
}
