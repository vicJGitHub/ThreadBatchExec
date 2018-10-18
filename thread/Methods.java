package com.hywa.pricepublish.common.utils.thread;

import com.hywa.pricepublish.common.utils.SpringContext;
import com.hywa.pricepublish.dao.entity.AvgPriceStatistics;
import com.hywa.pricepublish.dao.mapper.AvgPriceStatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 作为线程执行的特定方法(将函数作为参数传入的特定类)
 * //参数[0]默认为list对象,后续参数作为自定义参数类型
 */
@Component
@Slf4j
public class Methods {

    public void avgPriceStatisticsByUpdateBatch(Object... objs) {
        AvgPriceStatisticsMapper avgPriceStatisticsMapper = (AvgPriceStatisticsMapper) SpringContext.getBean("avgPriceStatisticsMapper");
        avgPriceStatisticsMapper.updateBatch((List<AvgPriceStatistics>) objs[0]);
    }

    public void avgPriceStatisticsByInsertBatch(Object... objs) {
        AvgPriceStatisticsMapper avgPriceStatisticsMapper = (AvgPriceStatisticsMapper) SpringContext.getBean("avgPriceStatisticsMapper");
        avgPriceStatisticsMapper.insertBatch((List<AvgPriceStatistics>) objs[0]);
    }

    public void thread(Object... objs) {
        for (Object o : (List<?>)objs[0]) {
            System.out.println("线程名称:" + Thread.currentThread().getName() + ":" + o);
        }
    }
}
