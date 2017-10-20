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
public class ExecutorRouteBusyover extends ExecutorRouter {

	public String route(int jobId, ArrayList<String> addressList) {
		return addressList.get(0);
	}

	@Override
	public ReturnT<String> routeRun(TriggerParam triggerParam, ArrayList<String> addressList, ScheduleTaskLog jobLog) {

		StringBuffer idleBeatResultSB = new StringBuffer();
		for (String address : addressList) {
			// beat
			ReturnT<String> idleBeatResult = null;
			try {
				IExecutorService executorBiz = (IExecutorService) new NetComClientProxy(IExecutorService.class, address)
						.getObject();
				idleBeatResult = executorBiz.idleBeat(triggerParam.getTaskId());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				idleBeatResult = new ReturnT<String>(ReturnT.FAIL_CODE, "" + e);
			}
			idleBeatResultSB.append("<br>----------------------<br>").append("空闲检测：").append("<br>address：")
					.append(address).append("<br>code：").append(idleBeatResult.getCode()).append("<br>msg：")
					.append(idleBeatResult.getMsg());

			// beat success
			if (idleBeatResult.getCode() == ReturnT.SUCCESS_CODE) {
				jobLog.setExecutorAddress(address);

				ReturnT<String> runResult = runExecutor(triggerParam, address);
				idleBeatResultSB.append("<br>----------------------<br>").append(runResult.getMsg());

				return new ReturnT<String>(runResult.getCode(), idleBeatResultSB.toString());
			}
		}

		return new ReturnT<String>(ReturnT.FAIL_CODE, idleBeatResultSB.toString());
	}
}
