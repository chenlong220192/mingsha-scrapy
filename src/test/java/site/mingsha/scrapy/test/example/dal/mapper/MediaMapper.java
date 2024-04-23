package site.mingsha.scrapy.test.example.dal.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import site.mingsha.scrapy.test.example.dal.model.GoodsDO;
import site.mingsha.scrapy.test.example.dal.model.MediaDO;

/**
 *
 */
@Mapper
public interface MediaMapper {

    List<MediaDO> findByGoodsId(@Param("goodsId") Long goodsId);

    void insert(@Param("mediaDOList") List<MediaDO> mediaDOList);

}
