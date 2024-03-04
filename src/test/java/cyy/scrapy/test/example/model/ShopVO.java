package cyy.scrapy.test.example.model;

import java.io.Serializable;

public class ShopVO implements Serializable {

    /**
     * 搜索词
     */
    private String source = "taobao";
    /**
     * 搜索词
     */
    private String keyword;

    /* ------------------------- */

    /**
     * 店铺链接
     */
    private String shopUrl;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺ID
     */
    private String shopId;
    /**
     * 店铺等级
     */
    private String shopLevel;
    /**
     * 店铺类型 taobao or tmall
     */
    private String shopType;

    public static ShopVO newInstance() {
        return new ShopVO();
    }

    public String getSource() {
        return source;
    }

    public ShopVO setSource(String source) {
        this.source = source;
        return this;
    }

    public String getKeyword() {
        return keyword;
    }

    public ShopVO setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public ShopVO setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
        return this;
    }

    public String getShopName() {
        return shopName;
    }

    public ShopVO setShopName(String shopName) {
        this.shopName = shopName;
        return this;
    }

    public String getShopId() {
        return shopId;
    }

    public ShopVO setShopId(String shopId) {
        this.shopId = shopId;
        return this;
    }

    public String getShopLevel() {
        return shopLevel;
    }

    public ShopVO setShopLevel(String shopLevel) {
        this.shopLevel = shopLevel;
        return this;
    }

    public String getShopType() {
        return shopType;
    }

    public ShopVO setShopType(String shopType) {
        this.shopType = shopType;
        return this;
    }
}
