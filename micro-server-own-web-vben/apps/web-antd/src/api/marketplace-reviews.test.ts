import { describe, expect, it, vi } from 'vitest';

const { requests } = vi.hoisted(() => ({ requests: [] as Record<string, any>[] }));
vi.mock('./marketplace-request', () => ({
  marketplaceRequest: vi.fn(async (config: Record<string, any>) => { requests.push(config); return {}; }),
}));

import { buyerApi, catalogApi, merchantApi } from './marketplace';

describe('favorite and review API contracts', () => {
  it('reads and changes buyer favorites on the product route', async () => {
    await buyerApi.favorites();
    await buyerApi.addFavorite(12, 'add-12');
    await buyerApi.removeFavorite(12, 'remove-12');
    expect(requests.slice(-3)).toMatchObject([
      { method: 'GET', url: '/product/api/v1/favorites' },
      { headers: { 'Idempotency-Key': 'add-12' }, method: 'POST', url: '/product/api/v1/favorites/12' },
      { headers: { 'Idempotency-Key': 'remove-12' }, method: 'DELETE', url: '/product/api/v1/favorites/12' },
    ]);
  });

  it('pages public reviews and preserves explicit write retry keys', async () => {
    await catalogApi.reviews(12, 2, 10);
    await buyerApi.createReview(12, { content: 'Good', rating: 5 }, 'review-12');
    await buyerApi.reportReview(12, 3, 'Spam', 'report-3');
    await merchantApi.replyReview(3, 'Thanks', 'reply-3');
    expect(requests.slice(-4)).toMatchObject([
      { method: 'GET', params: { page: 2, size: 10 }, url: '/product/api/v1/products/12/reviews' },
      { data: { content: 'Good', rating: 5 }, headers: { 'Idempotency-Key': 'review-12' }, method: 'POST', url: '/product/api/v1/products/12/reviews' },
      { data: { reason: 'Spam' }, headers: { 'Idempotency-Key': 'report-3' }, method: 'POST', url: '/product/api/v1/products/12/reviews/3/reports' },
      { data: { content: 'Thanks' }, headers: { 'Idempotency-Key': 'reply-3' }, method: 'POST', url: '/product/api/v1/merchant/reviews/3/reply' },
    ]);
  });
});
