package com.geostax.scheduler.admin.route;

import com.geostax.scheduler.admin.route.strategy.ExecutorRouteBusyover;
import com.geostax.scheduler.admin.route.strategy.ExecutorRouteConsistentHash;
import com.geostax.scheduler.admin.route.strategy.ExecutorRouteFailover;
import com.geostax.scheduler.admin.route.strategy.ExecutorRouteFirst;
import com.geostax.scheduler.admin.route.strategy.ExecutorRouteLFU;
import com.geostax.scheduler.admin.route.strategy.ExecutorRouteLRU;
import com.geostax.scheduler.admin.route.strategy.ExecutorRouteLast;
import com.geostax.scheduler.admin.route.strategy.ExecutorRouteRandom;
import com.geostax.scheduler.admin.route.strategy.ExecutorRouteRound;

/**
 * Created by xuxueli on 17/3/10.
 */
public enum ExecutorRouteStrategyEnum {

    FIRST("第一个", new ExecutorRouteFirst()),
    LAST("最后一个", new ExecutorRouteLast()),
    ROUND("轮询", new ExecutorRouteRound()),
    RANDOM("随机", new ExecutorRouteRandom()),
    CONSISTENT_HASH("一致性HASH", new ExecutorRouteConsistentHash()),
    LEAST_FREQUENTLY_USED("最不经常使用", new ExecutorRouteLFU()),
    LEAST_RECENTLY_USED("最近最久未使用", new ExecutorRouteLRU()),
    FAILOVER("故障转移", new ExecutorRouteFailover()),
    BUSYOVER("忙碌转移", new ExecutorRouteBusyover());

    ExecutorRouteStrategyEnum(String title, ExecutorRouter router) {
        this.title = title;
        this.router = router;
    }

    private String title;
    private ExecutorRouter router;

    public String getTitle() {
        return title;
    }
    public ExecutorRouter getRouter() {
        return router;
    }

    public static ExecutorRouteStrategyEnum match(String name, ExecutorRouteStrategyEnum defaultItem){
        if (name != null) {
            for (ExecutorRouteStrategyEnum item: ExecutorRouteStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }

}
