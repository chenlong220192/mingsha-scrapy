package cyy.scrapy.test.example.model;

import java.io.Serializable;

public class GoodsVO implements Serializable {

    private String serial;
    /**
     * 搜索词
     */
    private String source;
    /**
     * 搜索词
     */
    private String keyword;

    /* ------------------------- */
    /**
     * 商铺链接
     */
    private String shopUrl;
    /**
     * 商铺名称
     */
    private String shopName;
    /**
     * 商品链接
     */
    private String goodsUrl;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 售卖数量描述
     */
    private String saleDesc;
    /**
     * 收货数量描述
     */
    private String takeDeliveryDesc;
    /**
     * 评价描述
     */
    private String evaluateDesc;
    /**
     * 价格
     */
    private String price;

    public static GoodsVO newInstance() {
        return new GoodsVO();
    }

    public String getSerial() {
        return serial;
    }

    public GoodsVO setSerial(String serial) {
        this.serial = serial;
        return this;
    }

    public String getSource() {
        return source;
    }

    public GoodsVO setSource(String source) {
        this.source = source;
        return this;
    }

    public String getKeyword() {
        return keyword;
    }

    public GoodsVO setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public GoodsVO setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
        return this;
    }

    public String getShopName() {
        return shopName;
    }

    public GoodsVO setShopName(String shopName) {
        this.shopName = shopName;
        return this;
    }

    public String getGoodsUrl() {
        return goodsUrl;
    }

    public GoodsVO setGoodsUrl(String goodsUrl) {
        this.goodsUrl = goodsUrl;
        return this;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public GoodsVO setGoodsName(String goodsName) {
        this.goodsName = goodsName;
        return this;
    }

    public String getSaleDesc() {
        return saleDesc;
    }

    public GoodsVO setSaleDesc(String saleDesc) {
        this.saleDesc = saleDesc;
        return this;
    }

    public String getTakeDeliveryDesc() {
        return takeDeliveryDesc;
    }

    public GoodsVO setTakeDeliveryDesc(String takeDeliveryDesc) {
        this.takeDeliveryDesc = takeDeliveryDesc;
        return this;
    }

    public String getEvaluateDesc() {
        return evaluateDesc;
    }

    public GoodsVO setEvaluateDesc(String evaluateDesc) {
        this.evaluateDesc = evaluateDesc;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public GoodsVO setPrice(String price) {
        this.price = price;
        return this;
    }
}
