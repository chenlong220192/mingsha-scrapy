package cyy.scrapy.test.example.dal.mapper;

import cyy.scrapy.test.example.dal.model.GoodsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 */
@Mapper
public interface GoodsMapper {

    List<GoodsDO> findAll();

    List<GoodsDO> findAllShopUrlIsEmpty(@Param("serial") String serial);

    GoodsDO findById(@Param("id") Long id);

    List<GoodsDO> findBySerial(@Param("serial") String serial);

    int countByKeyword(@Param("serial") String serial, @Param("keyword") String keyword);

    void insert(@Param("goodsDOList") List<GoodsDO> goodsDOList);

    void update(GoodsDO goodsDO);

}
