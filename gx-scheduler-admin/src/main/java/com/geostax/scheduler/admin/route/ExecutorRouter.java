package com.geostax.scheduler.admin.route;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geostax.scheduler.admin.model.ScheduleTaskLog;
import com.geostax.scheduler.core.executor.IExecutorService;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.model.TriggerParam;
import com.geostax.scheduler.core.rpc.netcom.NetComClientProxy;

/**
 * Created by xuxueli on 17/3/10.
 */
public abstract class ExecutorRouter {
    protected static Logger logger = LoggerFactory.getLogger(ExecutorRouter.class);

    /**
     * route run
     *
     * @param triggerParam
     * @param addressList
     * @return
     */
    public abstract ReturnT<String> routeRun(TriggerParam triggerParam, ArrayList<String> addressList, ScheduleTaskLog jobLog);

    /**
     * run executor
     * @param triggerParam
     * @param address
     * @return
     */
    protected static ReturnT<String> runExecutor(TriggerParam triggerParam, String address){
        ReturnT<String> runResult = null;
        try {
        	IExecutorService executorBiz = (IExecutorService) new NetComClientProxy(IExecutorService.class, address).getObject();
            runResult = executorBiz.run(triggerParam);
        } catch (Exception e) {
            logger.error("", e);
            runResult = new ReturnT<String>(ReturnT.FAIL_CODE, ""+e );
        }

        StringBuffer runResultSB = new StringBuffer("触发调度：");
        runResultSB.append("<br>address：").append(address);
        runResultSB.append("<br>code：").append(runResult.getCode());
        runResultSB.append("<br>msg：").append(runResult.getMsg());

        runResult.setMsg(runResultSB.toString());
        return runResult;
    }

}
