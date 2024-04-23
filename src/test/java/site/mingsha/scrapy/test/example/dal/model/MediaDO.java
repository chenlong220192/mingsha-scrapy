package site.mingsha.scrapy.test.example.dal.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class MediaDO implements Serializable {

    private Long   id;
    private Long   goodsId;
    private String url;
    private String localUrl;
    private Date   gmtCreated;
    private Date   gmtModified;

    public Long getId() {
        return id;
    }

    public MediaDO setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public MediaDO setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public MediaDO setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public Date getGmtCreated() {
        return gmtCreated;
    }

    public MediaDO setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
        return this;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public MediaDO setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
        return this;
    }

    @Override
    public String toString() {
        return "MediaDO{" +
                "id=" + id +
                ", goodsId=" + goodsId +
                ", url='" + url + '\'' +
                ", localUrl='" + localUrl + '\'' +
                ", gmtCreated=" + gmtCreated +
                ", gmtModified=" + gmtModified +
                '}';
    }
}
