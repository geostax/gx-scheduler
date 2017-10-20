package com.geostax.scheduler.admin.route.strategy;

import java.util.ArrayList;

import com.geostax.scheduler.admin.model.ScheduleTaskLog;
import com.geostax.scheduler.admin.route.ExecutorRouter;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.model.TriggerParam;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFirst extends ExecutorRouter {

    public String route(int jobId, ArrayList<String> addressList) {
        return addressList.get(0);
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
