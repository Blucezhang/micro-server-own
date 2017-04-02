package promotion.dao.domain;

/**
 * 赠品促销
 */
public class PromotionalGifts extends BasePromotion {
	
	private static final long serialVersionUID = -4103188992239047743L;
	
	private String giftContent;//存储序列化的赠品信息

	public String getGiftContent() {
		return giftContent;
	}

	public void setGiftContent(String giftContent) {
		this.giftContent = giftContent;
	}
	
	public void processBussines(){
		System.out.println("处理商品促销业务...");
	}
	
}
