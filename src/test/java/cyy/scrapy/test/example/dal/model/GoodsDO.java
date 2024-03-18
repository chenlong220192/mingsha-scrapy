package cyy.scrapy.test.example.dal.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class GoodsDO implements Serializable {

    private Long   id;
    private String serial;
    private String source;
    private String keyword;
    private String url;
    private String name;
    private String price;
    private String saleDesc;
    private String takeDeliveryDesc;

    private String evaluateDesc;
    private String shopUrl;
    private String shopName;
    private Date   gmtCreated;
    private Date   gmtModified;

    public Long getId() {
        return id;
    }

    public GoodsDO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getSerial() {
        return serial;
    }

    public GoodsDO setSerial(String serial) {
        this.serial = serial;
        return this;
    }

    public String getSource() {
        return source;
    }

    public GoodsDO setSource(String source) {
        this.source = source;
        return this;
    }

    public String getKeyword() {
        return keyword;
    }

    public GoodsDO setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public GoodsDO setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getName() {
        return name;
    }

    public GoodsDO setName(String name) {
        this.name = name;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public GoodsDO setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getSaleDesc() {
        return saleDesc;
    }

    public GoodsDO setSaleDesc(String saleDesc) {
        this.saleDesc = saleDesc;
        return this;
    }

    public String getTakeDeliveryDesc() {
        return takeDeliveryDesc;
    }

    public GoodsDO setTakeDeliveryDesc(String takeDeliveryDesc) {
        this.takeDeliveryDesc = takeDeliveryDesc;
        return this;
    }

    public String getEvaluateDesc() {
        return evaluateDesc;
    }

    public GoodsDO setEvaluateDesc(String evaluateDesc) {
        this.evaluateDesc = evaluateDesc;
        return this;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public GoodsDO setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
        return this;
    }

    public String getShopName() {
        return shopName;
    }

    public GoodsDO setShopName(String shopName) {
        this.shopName = shopName;
        return this;
    }

    public Date getGmtCreated() {
        return gmtCreated;
    }

    public GoodsDO setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
        return this;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public GoodsDO setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
        return this;
    }

    @Override
    public String toString() {
        return "GoodsDO{" +
                "id=" + id +
                ", serial='" + serial + '\'' +
                ", source='" + source + '\'' +
                ", keyword='" + keyword + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", saleDesc='" + saleDesc + '\'' +
                ", takeDeliveryDesc='" + takeDeliveryDesc + '\'' +
                ", evaluateDesc='" + evaluateDesc + '\'' +
                ", shopUrl='" + shopUrl + '\'' +
                ", shopName='" + shopName + '\'' +
                ", gmtCreated=" + gmtCreated +
                ", gmtModified=" + gmtModified +
                '}';
    }
}
