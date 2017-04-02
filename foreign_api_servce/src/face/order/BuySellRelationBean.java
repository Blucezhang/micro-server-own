package face.order;


public class BuySellRelationBean {
	
	private Long buyerSellerId;
	private BuyerBean buyers;
	private SellerBean sellers;
	
	public Long getBuyerSellerId() {
		return buyerSellerId;
	}
	public void setBuyerSellerId(Long buyerSellerId) {
		this.buyerSellerId = buyerSellerId;
	}
	public BuyerBean getBuyers() {
		return buyers;
	}
	public void setBuyers(BuyerBean buyers) {
		this.buyers = buyers;
	}
	public SellerBean getSellers() {
		return sellers;
	}
	public void setSellers(SellerBean sellers) {
		this.sellers = sellers;
	}

}
