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
    private String shopUrl;
    private String shopName;
    private Date   gmtCreated;
    private Date   gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSaleDesc() {
        return saleDesc;
    }

    public void setSaleDesc(String saleDesc) {
        this.saleDesc = saleDesc;
    }

    public String getTakeDeliveryDesc() {
        return takeDeliveryDesc;
    }

    public void setTakeDeliveryDesc(String takeDeliveryDesc) {
        this.takeDeliveryDesc = takeDeliveryDesc;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public Date getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
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
                ", shopUrl='" + shopUrl + '\'' +
                ", shopName='" + shopName + '\'' +
                ", gmtCreated=" + gmtCreated +
                ", gmtModified=" + gmtModified +
                '}';
    }
}
