package com.geostax.scheduler.admin.taskbean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.geostax.scheduler.admin.GxDynamicScheduler;
import com.geostax.scheduler.admin.enums.ExecutorFailStrategyEnum;
import com.geostax.scheduler.admin.model.ScheduleTaskGroup;
import com.geostax.scheduler.admin.model.ScheduleTaskInfo;
import com.geostax.scheduler.admin.model.ScheduleTaskLog;
import com.geostax.scheduler.admin.route.ExecutorRouteStrategyEnum;
import com.geostax.scheduler.admin.thread.TaskFailMonitorHelper;
import com.geostax.scheduler.admin.thread.TaskRegistryMonitorHelper;
import com.geostax.scheduler.core.enums.ExecutorBlockStrategyEnum;
import com.geostax.scheduler.core.enums.RegistryConfig;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.model.TriggerParam;

/**
 * http job bean “@DisallowConcurrentExecution” diable concurrent, thread size
 * can not be only one, better given more
 * 
 * @author xuxueli 2015-12-17 18:20:34
 */
// @DisallowConcurrentExecution
public class RemoteHttpJobBean extends QuartzJobBean {
	private static Logger logger = LoggerFactory.getLogger(RemoteHttpJobBean.class);

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

		// load job
		JobKey jobKey = context.getTrigger().getJobKey();
		Integer jobId = Integer.valueOf(jobKey.getName());
		ScheduleTaskInfo jobInfo = GxDynamicScheduler.scheduleTaskInfoDao.loadById(jobId);

		// log part-1
		ScheduleTaskLog jobLog = new ScheduleTaskLog();
		jobLog.setTaskGroup(jobInfo.getTaskGroup());
		jobLog.setTaskId(jobInfo.getId());
		GxDynamicScheduler.scheduleTaskLogDao.save(jobLog);
		logger.debug(">>>>>>>>>>> xxl-job trigger start, jobId:{}", jobLog.getId());

		// log part-2 param
		// jobLog.setExecutorAddress(executorAddress);
		jobLog.setExecutorHandler(jobInfo.getExecutorHandler());
		jobLog.setExecutorParam(jobInfo.getExecutorParam());
		jobLog.setTriggerTime(new Date());

		// trigger request
		TriggerParam triggerParam = new TriggerParam();
		triggerParam.setTaskId(jobInfo.getId());
		triggerParam.setExecutorHandler(jobInfo.getExecutorHandler());
		triggerParam.setExecutorParams(jobInfo.getExecutorParam());
		triggerParam.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
		triggerParam.setLogId(jobLog.getId());
		triggerParam.setLogDateTim(jobLog.getTriggerTime().getTime());

		// do trigger
		ReturnT<String> triggerResult = doTrigger(triggerParam, jobInfo, jobLog);

		// fail retry
		if (triggerResult.getCode() == ReturnT.FAIL_CODE && ExecutorFailStrategyEnum
				.match(jobInfo.getExecutorFailStrategy(), null) == ExecutorFailStrategyEnum.FAIL_RETRY) {
			ReturnT<String> retryTriggerResult = doTrigger(triggerParam, jobInfo, jobLog);

			triggerResult.setCode(retryTriggerResult.getCode());
			triggerResult.setMsg(triggerResult.getMsg()
					+ "<br><br><span style=\"color:#F39C12;\" > >>>>>>>>>>>失败重试<<<<<<<<<<< </span><br><br>"
					+ retryTriggerResult.getMsg());
		}

		// log part-2
		jobLog.setTriggerCode(triggerResult.getCode());
		jobLog.setTriggerMsg(triggerResult.getMsg());
		GxDynamicScheduler.scheduleTaskLogDao.updateTriggerInfo(jobLog);

		// monitor triger
		TaskFailMonitorHelper.monitor(jobLog.getId());
		logger.debug(">>>>>>>>>>> xxl-job trigger end, jobId:{}", jobLog.getId());
	}

	public ReturnT<String> doTrigger(TriggerParam triggerParam, ScheduleTaskInfo jobInfo, ScheduleTaskLog jobLog) {
		StringBuffer triggerSb = new StringBuffer();

		// exerutor address list
		ArrayList<String> addressList = null;
		ScheduleTaskGroup group = GxDynamicScheduler.scheduleTaskGroupDao.load(jobInfo.getTaskGroup());
		if (group.getAddressType() == 0) {
			triggerSb.append("注册方式：自动注册");
			addressList = (ArrayList<String>) TaskRegistryMonitorHelper
					.discover(RegistryConfig.RegistType.EXECUTOR.name(), group.getAppName());
		} else {
			triggerSb.append("注册方式：手动录入");
			if (StringUtils.isNotBlank(group.getAddressList())) {
				addressList = new ArrayList<String>(Arrays.asList(group.getAddressList().split(",")));
			}
		}
		triggerSb.append("<br>阻塞处理策略：").append(ExecutorBlockStrategyEnum
				.match(jobInfo.getExecutorBlockStrategy(), ExecutorBlockStrategyEnum.SERIAL_EXECUTION).getTitle());
		triggerSb.append("<br>失败处理策略：").append(ExecutorFailStrategyEnum
				.match(jobInfo.getExecutorBlockStrategy(), ExecutorFailStrategyEnum.FAIL_ALARM).getTitle());
		triggerSb.append("<br>地址列表：").append(addressList != null ? addressList.toString() : "");
		if (CollectionUtils.isEmpty(addressList)) {
			triggerSb.append("<br>----------------------<br>").append("调度失败：").append("执行器地址为空");
			return new ReturnT<String>(ReturnT.FAIL_CODE, triggerSb.toString());
		}

		// executor route strategy
		ExecutorRouteStrategyEnum executorRouteStrategyEnum = ExecutorRouteStrategyEnum
				.match(jobInfo.getExecutorRouteStrategy(), null);
		if (executorRouteStrategyEnum == null) {
			triggerSb.append("<br>----------------------<br>").append("调度失败：").append("执行器路由策略为空");
			return new ReturnT<String>(ReturnT.FAIL_CODE, triggerSb.toString());
		}
		triggerSb.append("<br>路由策略：")
				.append(executorRouteStrategyEnum.name() + "-" + executorRouteStrategyEnum.getTitle());

		// route run / trigger remote executor
		ReturnT<String> routeRunResult = executorRouteStrategyEnum.getRouter().routeRun(triggerParam, addressList,
				jobLog);
		triggerSb.append("<br>----------------------<br>").append(routeRunResult.getMsg());
		return new ReturnT<String>(routeRunResult.getCode(), triggerSb.toString());

	}

}