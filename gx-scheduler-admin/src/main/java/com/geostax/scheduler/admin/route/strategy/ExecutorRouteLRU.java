package com.geostax.scheduler.admin.route.strategy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.geostax.scheduler.admin.model.ScheduleTaskLog;
import com.geostax.scheduler.admin.route.ExecutorRouter;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.model.TriggerParam;

/**
 * 单个JOB对应的每个执行器，最久为使用的优先被选举
 *      a、LFU(Least Frequently Used)：最不经常使用，频率/次数
 *      b(*)、LRU(Least Recently Used)：最近最久未使用，时间
 *
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteLRU extends ExecutorRouter {

    private static ConcurrentHashMap<Integer, LinkedHashMap<String, String>> jobLRUMap = new ConcurrentHashMap<Integer, LinkedHashMap<String, String>>();
    private static long CACHE_VALID_TIME = 0;

    public String route(int jobId, ArrayList<String> addressList) {

        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLRUMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000*60*60*24;
        }

        // init lru
        LinkedHashMap<String, String> lruItem = jobLRUMap.get(jobId);
        if (lruItem == null) {
            /**
             * LinkedHashMap
             *      a、accessOrder：ture=访问顺序排序（get/put时排序）；false=插入顺序排期；
             *      b、removeEldestEntry：新增元素时将会调用，返回true时会删除最老元素；可封装LinkedHashMap并重写该方法，比如定义最大容量，超出是返回true即可实现固定长度的LRU算法；
             */
            lruItem = new LinkedHashMap<>(16, 0.75f, true);
            jobLRUMap.put(jobId, lruItem);
        }

        // put
        for (String address: addressList) {
            if (!lruItem.containsKey(address)) {
                lruItem.put(address, address);
            }
        }

        // load
        String eldestKey = lruItem.entrySet().iterator().next().getKey();
        String eldestValue = lruItem.get(eldestKey);
        return eldestValue;
    }


    @Override
    public ReturnT<String> routeRun(TriggerParam triggerParam, ArrayList<String> addressList, ScheduleTaskLog jobLog) {

        // address
        String address = route(triggerParam.getTaskId(), addressList);
        jobLog.setExecutorAddress(address);

        // run executor
        ReturnT<String> runResult = runExecutor(triggerParam, address);
        runResult.setMsg("<br>----------------------<br>" + runResult.getMsg());

        return runResult;
    }

}
