# 前端检查记录 / Frontend Review — 2026-09-30

更新 / Updated: 2026-10-01（返回结算与发货表单复验 / checkout return and shipment form checks）。

本记录只描述 `micro-server-own-web-vben/apps/web-antd` 的商城业务界面。代码检查、模拟 API 浏览器回归与真实后端联调是不同证据；本轮没有向真实业务库写入数据。

This record covers the marketplace UI in `micro-server-own-web-vben/apps/web-antd` only. Source inspection, browser tests with mocked APIs, and real backend integration are different evidence levels. This round did not write to a real business database.

## 本轮修复与补充 / Delivered in this round

| 范围 / Area | 结果 / Result |
| --- | --- |
| 商城表单 / Marketplace forms | 为依赖 `@finish` 的 Ant Design Vue 表单绑定对应 `model`，恢复查询与写入按钮的提交事件。System role-grant form also rejects empty/non-positive IDs and reports the result. |
| 结算 / Checkout | 切换地址或优惠券后重新试算，清除旧报价并阻止在新报价完成前下单；订单命令携带所选券。Address/coupon changes invalidate stale quotations before order submission. |
| 券包 / Coupon wallet | 增加买家券包和已知商家模板 ID 领券入口；无效 ID 不发送请求，失败重试复用原幂等键；从结算跳转领券后可返回原购物车项。Adds a wallet, stable retry keys, and a return-to-checkout path. |
| 商家发货 / Shipment | 空物流信息不再发起写请求；结果和待发货列表加载错误有页面反馈。Blank shipment details no longer produce a write request, and the page reports shipment/list errors. |
| 会话 / Session | 损坏或不完整的浏览器缓存令牌不再使登录页加载崩溃。Malformed or incomplete saved tokens no longer crash startup. |

商家券领取需要运营方或商家提供已知模板 ID；后端当前没有面向买家的“可领券活动列表”接口。结算 UI 当前一次选择一张已领取且可用的券；服务端仍负责校验门槛、归属和占券。领券、试算和下单都不是客户端自行计算最终金额。

Claiming a store coupon requires a known template ID supplied by an operator or merchant; there is no buyer-facing claimable-offer listing API yet. Checkout currently selects one owned, available coupon at a time. The server remains responsible for eligibility, ownership, quotation, and reservation.

## 验证边界 / Verification boundary

- `vue-tsc --noEmit --skipLibCheck` 通过；`marketplace-*.test.ts` 共 4 个文件、9 个测试通过；Vite production build 通过。Type checking, 9 focused tests, and the production build passed.
- 本地 Chromium + 模拟 API：空模板 ID、领取失败后同键重试、券包读取、地址/券切换后的报价、下单请求内容、返回结算仍保留购物车项；另验证商品搜索，以及商家发货空输入不请求、有效输入成功。无未捕获页面异常或 Vite error overlay。Local browser checks covered those interactions without uncaught page errors.
- 390px 手机冷启动券包页未发生页面级横向溢出；券表自身允许横向滚动。未提供已提交的视觉基线，因此视觉回归结论为不确定。The 390px wallet page had no page-level horizontal overflow; there is no committed screenshot baseline, so visual regression remains inconclusive.
- 不代表 Nacos、MySQL、Neo4j、真实支付/物流、真实登录与权限、渠道对账已验收。It does **not** establish acceptance for real databases, authentication, payments, logistics, or reconciliation.

## 仍缺的前端业务 / Remaining frontend work

1. 买家可发现和领取的活动列表需后端公开查询契约；当前只能凭已知模板 ID 领店铺券。A public claimable-offer catalog requires a backend read API.
2. 买家收藏与评价、商家评价回复、个人资料与会话管理仍缺商城业务入口；这些后端接口已经存在。Buyer favorites/reviews, merchant replies, and profile/session screens remain unconnected despite existing backend APIs.
3. 商家售后审核、收退货与换货发货已有订单服务接口但无商家前端页面；买家物流轨迹与售后关闭接口也未在当前页面形成可操作入口。Merchant after-sales handling and buyer logistics/after-sales actions need UI integration.
4. 系统账号/角色搜索、现有授权展示与撤销接口尚未完整提供；当前仅能用已审核 ID 做追加授权。System account/role search, current-grant display, and revocation need explicit backend contracts.
5. 真实支付、提现付款、外部物流与端到端消息通知需要渠道、业务政策及隔离集成环境，不能把模拟页当作生产闭环。Real payment/payout, external logistics, and messaging need provider contracts and isolated integration acceptance.
