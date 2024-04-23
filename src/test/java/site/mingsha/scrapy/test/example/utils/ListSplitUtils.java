package site.mingsha.scrapy.test.example.utils;

import java.util.List;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

public class ListSplitUtils {

    /**
     * 集合分组（指定每个批次的容量）
     *
     * @param list 待处理集合
     * @param limit 批次容量
     * @return
     */
    public static <T> List<List<T>> splitByCondition(List<T> list, int limit) {
        List<List<T>> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        // 总数
        int size = list.size();
        // 批数
        int part = size / limit + (size % limit == 0 ? 0 : 1);
        // 拆分
        for (int i = 0; i < part; i++) {
            result.add(list.subList(i * limit, (i + 1) * limit <= size ? (i + 1) * limit : size));
        }
        return result;
    }

    /**
     * 集合分组（指定总批次）
     *
     * @param list 待处理集合
     * @param part 批次数
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitWithPart(List<T> list, int part) {
        List<List<T>> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        // 总数
        int size = list.size();
        // 容量
        int limit = size / part;
        // 余数
        int tail = size % part;
        // 拆分
        for (int i = 0; i < part; i++) {
            result.add(list.subList(i * limit, (i + 1) < part ? (i + 1) * limit : size));
        }
        return result;
    }

}
