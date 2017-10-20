package com.geostax.scheduler.admin.route.strategy;

import java.util.ArrayList;

import com.geostax.scheduler.admin.model.ScheduleTaskLog;
import com.geostax.scheduler.admin.route.ExecutorRouter;
import com.geostax.scheduler.core.executor.IExecutorService;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.model.TriggerParam;
import com.geostax.scheduler.core.rpc.netcom.NetComClientProxy;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFailover extends ExecutorRouter {

    public String route(int jobId, ArrayList<String> addressList) {
        return addressList.get(0);
    }

    @Override
    public ReturnT<String> routeRun(TriggerParam triggerParam, ArrayList<String> addressList, ScheduleTaskLog jobLog) {

        StringBuffer beatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ReturnT<String> beatResult = null;
            try {
            	IExecutorService executorBiz = (IExecutorService) new NetComClientProxy(IExecutorService.class, address).getObject();
                beatResult = executorBiz.beat();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                beatResult = new ReturnT<String>(ReturnT.FAIL_CODE, ""+e );
            }
            beatResultSB.append("<br>----------------------<br>")
                    .append("心跳检测：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(beatResult.getCode())
                    .append("<br>msg：").append(beatResult.getMsg());

            // beat success
            if (beatResult.getCode() == ReturnT.SUCCESS_CODE) {
                jobLog.setExecutorAddress(address);

                ReturnT<String> runResult = runExecutor(triggerParam, address);
                beatResultSB.append("<br>----------------------<br>").append(runResult.getMsg());

                return new ReturnT<String>(runResult.getCode(), beatResultSB.toString());
            }
        }
        return new ReturnT<String>(ReturnT.FAIL_CODE, beatResultSB.toString());

    }
}
