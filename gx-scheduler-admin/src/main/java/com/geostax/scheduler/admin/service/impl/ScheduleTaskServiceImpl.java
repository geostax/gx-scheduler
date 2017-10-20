package com.geostax.scheduler.admin.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.geostax.scheduler.admin.GxDynamicScheduler;
import com.geostax.scheduler.admin.dao.IScheduleTaskGroupDao;
import com.geostax.scheduler.admin.dao.IScheduleTaskInfoDao;
import com.geostax.scheduler.admin.dao.IScheduleTaskLogDao;
import com.geostax.scheduler.admin.enums.ExecutorFailStrategyEnum;
import com.geostax.scheduler.admin.model.ScheduleTaskGroup;
import com.geostax.scheduler.admin.model.ScheduleTaskInfo;
import com.geostax.scheduler.admin.route.ExecutorRouteStrategyEnum;
import com.geostax.scheduler.admin.service.IScheduleTaskService;
import com.geostax.scheduler.admin.thread.TaskRegistryMonitorHelper;
import com.geostax.scheduler.core.enums.ExecutorBlockStrategyEnum;
import com.geostax.scheduler.core.enums.RegistryConfig;
import com.geostax.scheduler.core.model.ReturnT;

/**
 * core job action for xxl-job
 * 
 * @author xuxueli 2016-5-28 15:30:33
 */
@Service
public class ScheduleTaskServiceImpl implements IScheduleTaskService {
	private static Logger logger = LoggerFactory.getLogger(ScheduleTaskServiceImpl.class);

	@Resource
	private IScheduleTaskGroupDao xxlJobGroupDao;
	@Resource
	private IScheduleTaskInfoDao xxlJobInfoDao;
	@Resource
	public IScheduleTaskLogDao xxlJobLogDao;

	@Override
	public Map<String, Object> pageList(int start, int length, int taskGroup, String executorHandler,
			String filterTime) {

		// page list
		List<ScheduleTaskInfo> list = xxlJobInfoDao.pageList(start, length, taskGroup, executorHandler);
		int list_count = xxlJobInfoDao.pageListCount(start, length, taskGroup, executorHandler);

		// fill job info
		if (list != null && list.size() > 0) {
			for (ScheduleTaskInfo jobInfo : list) {
				GxDynamicScheduler.fillJobInfo(jobInfo);
			}
		}

		// package result
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("recordsTotal", list_count); // 总记录数
		maps.put("recordsFiltered", list_count); // 过滤后的总记录数
		maps.put("data", list); // 分页列表
		return maps;
	}

	@Override
	public ReturnT<String> add(ScheduleTaskInfo taskInfo) {
		// valid
		ScheduleTaskGroup group = xxlJobGroupDao.load(taskInfo.getTaskGroup());
		if (group == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请选择“执行器”");
		}
		if (!CronExpression.isValidExpression(taskInfo.getTaskCron())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入格式正确的“Cron”");
		}
		if (StringUtils.isBlank(taskInfo.getTaskDesc())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入“任务描述”");
		}
		if (StringUtils.isBlank(taskInfo.getAuthor())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入“负责人”");
		}
		if (ExecutorRouteStrategyEnum.match(taskInfo.getExecutorRouteStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "路由策略非法");
		}
		if (ExecutorBlockStrategyEnum.match(taskInfo.getExecutorBlockStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "阻塞处理策略非法");
		}
		if (ExecutorFailStrategyEnum.match(taskInfo.getExecutorFailStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "失败处理策略非法");
		}

		// childJobKey valid
		if (StringUtils.isNotBlank(taskInfo.getChildJobKey())) {
			String[] childJobKeys = taskInfo.getChildJobKey().split(",");
			for (String childJobKeyItem : childJobKeys) {
				String[] childJobKeyArr = childJobKeyItem.split("_");
				if (childJobKeyArr.length != 2) {
					return new ReturnT<String>(ReturnT.FAIL_CODE,
							MessageFormat.format("子任务Key({0})格式错误", childJobKeyItem));
				}
				ScheduleTaskInfo childJobInfo = xxlJobInfoDao.loadById(Integer.valueOf(childJobKeyArr[1]));
				if (childJobInfo == null) {
					return new ReturnT<String>(ReturnT.FAIL_CODE,
							MessageFormat.format("子任务Key({0})无效", childJobKeyItem));
				}
			}
		}

		// add in db
		xxlJobInfoDao.save(taskInfo);
		if (taskInfo.getId() < 1) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "新增任务失败");
		}

		// add in quartz
		String qz_group = String.valueOf(taskInfo.getTaskGroup());
		String qz_name = String.valueOf(taskInfo.getId());
		try {
			GxDynamicScheduler.addJob(qz_name, qz_group, taskInfo.getTaskCron());
			// GxDynamicScheduler.pauseJob(qz_name, qz_group);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			logger.error("", e);
			try {
				xxlJobInfoDao.delete(taskInfo.getId());
				GxDynamicScheduler.removeJob(qz_name, qz_group);
			} catch (SchedulerException e1) {
				logger.error("", e1);
			}
			return new ReturnT<String>(ReturnT.FAIL_CODE, "新增任务失败:" + e.getMessage());
		}
	}

	@Override
	public ReturnT<String> reschedule(ScheduleTaskInfo taskInfo) {

		// valid
		if (!CronExpression.isValidExpression(taskInfo.getTaskCron())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入格式正确的“Cron”");
		}
		if (StringUtils.isBlank(taskInfo.getTaskDesc())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入“任务描述”");
		}
		if (StringUtils.isBlank(taskInfo.getAuthor())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "请输入“负责人”");
		}
		if (ExecutorRouteStrategyEnum.match(taskInfo.getExecutorRouteStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "路由策略非法");
		}
		if (ExecutorBlockStrategyEnum.match(taskInfo.getExecutorBlockStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "阻塞处理策略非法");
		}
		if (ExecutorFailStrategyEnum.match(taskInfo.getExecutorFailStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "失败处理策略非法");
		}

		// childJobKey valid
		if (StringUtils.isNotBlank(taskInfo.getChildJobKey())) {
			String[] childJobKeys = taskInfo.getChildJobKey().split(",");
			for (String childJobKeyItem : childJobKeys) {
				String[] childJobKeyArr = childJobKeyItem.split("_");
				if (childJobKeyArr.length != 2) {
					return new ReturnT<String>(ReturnT.FAIL_CODE,
							MessageFormat.format("子任务Key({0})格式错误", childJobKeyItem));
				}
				ScheduleTaskInfo childJobInfo = xxlJobInfoDao.loadById(Integer.valueOf(childJobKeyArr[1]));
				if (childJobInfo == null) {
					return new ReturnT<String>(ReturnT.FAIL_CODE,
							MessageFormat.format("子任务Key({0})无效", childJobKeyItem));
				}
			}
		}

		// stage job info
		ScheduleTaskInfo exists_jobInfo = xxlJobInfoDao.loadById(taskInfo.getId());
		if (exists_jobInfo == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "参数异常");
		}
		// String old_cron = exists_jobInfo.getJobCron();

		exists_jobInfo.setTaskCron(taskInfo.getTaskCron());
		exists_jobInfo.setTaskDesc(taskInfo.getTaskDesc());
		exists_jobInfo.setAuthor(taskInfo.getAuthor());
		exists_jobInfo.setAlarmEmail(taskInfo.getAlarmEmail());
		exists_jobInfo.setExecutorRouteStrategy(taskInfo.getExecutorRouteStrategy());
		exists_jobInfo.setExecutorHandler(taskInfo.getExecutorHandler());
		exists_jobInfo.setExecutorParam(taskInfo.getExecutorParam());
		exists_jobInfo.setExecutorBlockStrategy(taskInfo.getExecutorBlockStrategy());
		exists_jobInfo.setExecutorFailStrategy(taskInfo.getExecutorFailStrategy());
		exists_jobInfo.setChildJobKey(taskInfo.getChildJobKey());
		xxlJobInfoDao.update(exists_jobInfo);

		// fresh quartz
		String qz_group = String.valueOf(exists_jobInfo.getTaskGroup());
		String qz_name = String.valueOf(exists_jobInfo.getId());
		try {
			boolean ret = GxDynamicScheduler.rescheduleJob(qz_group, qz_name, exists_jobInfo.getTaskCron());
			return ret ? ReturnT.SUCCESS : ReturnT.FAIL;
		} catch (SchedulerException e) {
			logger.error("", e);
		}

		return ReturnT.FAIL;
	}

	@Override
	public ReturnT<String> remove(int id) {
		ScheduleTaskInfo xxlJobInfo = xxlJobInfoDao.loadById(id);
		String group = String.valueOf(xxlJobInfo.getTaskGroup());
		String name = String.valueOf(xxlJobInfo.getId());

		try {
			GxDynamicScheduler.removeJob(name, group);
			xxlJobInfoDao.delete(id);
			xxlJobLogDao.delete(id);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return ReturnT.FAIL;
	}

	@Override
	public ReturnT<String> pause(int id) {
		ScheduleTaskInfo xxlJobInfo = xxlJobInfoDao.loadById(id);
		String group = String.valueOf(xxlJobInfo.getTaskGroup());
		String name = String.valueOf(xxlJobInfo.getId());

		try {
			boolean ret = GxDynamicScheduler.pauseJob(name, group); // jobStatus
																	// do not
																	// store
			return ret ? ReturnT.SUCCESS : ReturnT.FAIL;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return ReturnT.FAIL;
		}
	}

	@Override
	public ReturnT<String> resume(int id) {
		ScheduleTaskInfo xxlJobInfo = xxlJobInfoDao.loadById(id);
		String group = String.valueOf(xxlJobInfo.getTaskGroup());
		String name = String.valueOf(xxlJobInfo.getId());

		try {
			boolean ret = GxDynamicScheduler.resumeJob(name, group);
			return ret ? ReturnT.SUCCESS : ReturnT.FAIL;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return ReturnT.FAIL;
		}
	}

	@Override
	public ReturnT<String> triggerJob(int id) {
		ScheduleTaskInfo xxlJobInfo = xxlJobInfoDao.loadById(id);
		String group = String.valueOf(xxlJobInfo.getTaskGroup());
		String name = String.valueOf(xxlJobInfo.getId());

		try {
			GxDynamicScheduler.triggerJob(name, group);
			return ReturnT.SUCCESS;
		} catch (SchedulerException e) {
			e.printStackTrace();
			return ReturnT.FAIL;
		}
	}

	@Override
	public Map<String, Object> dashboardInfo() {

		int jobInfoCount = xxlJobInfoDao.findAllCount();
		int jobLogCount = xxlJobLogDao.triggerCountByHandleCode(-1);
		int jobLogSuccessCount = xxlJobLogDao.triggerCountByHandleCode(ReturnT.SUCCESS_CODE);

		// executor count
		Set<String> executerAddressSet = new HashSet<String>();
		List<ScheduleTaskGroup> groupList = xxlJobGroupDao.findAll();
		if (CollectionUtils.isNotEmpty(groupList)) {
			for (ScheduleTaskGroup group : groupList) {
				List<String> registryList = null;
				if (group.getAddressType() == 0) {
					registryList = TaskRegistryMonitorHelper.discover(RegistryConfig.RegistType.EXECUTOR.name(),
							group.getAppName());
				} else {
					if (StringUtils.isNotBlank(group.getAddressList())) {
						registryList = Arrays.asList(group.getAddressList().split(","));
					}
				}
				if (CollectionUtils.isNotEmpty(registryList)) {
					executerAddressSet.addAll(registryList);
				}
			}
		}
		int executorCount = executerAddressSet.size();

		Map<String, Object> dashboardMap = new HashMap<String, Object>();
		dashboardMap.put("jobInfoCount", jobInfoCount);
		dashboardMap.put("jobLogCount", jobLogCount);
		dashboardMap.put("jobLogSuccessCount", jobLogSuccessCount);
		dashboardMap.put("executorCount", executorCount);
		return dashboardMap;
	}

	@Override
	public ReturnT<Map<String, Object>> triggerChartDate() {
		Date from = DateUtils.addDays(new Date(), -30);
		Date to = new Date();

		List<String> triggerDayList = new ArrayList<String>();
		List<Integer> triggerDayCountSucList = new ArrayList<Integer>();
		List<Integer> triggerDayCountFailList = new ArrayList<Integer>();
		int triggerCountSucTotal = 0;
		int triggerCountFailTotal = 0;

		List<Map<String, Object>> triggerCountMapAll = xxlJobLogDao.triggerCountByDay(from, to, -1);
		List<Map<String, Object>> triggerCountMapSuc = xxlJobLogDao.triggerCountByDay(from, to, ReturnT.SUCCESS_CODE);
		if (CollectionUtils.isNotEmpty(triggerCountMapAll)) {
			for (Map<String, Object> item : triggerCountMapAll) {
				String day = String.valueOf(item.get("triggerDay"));
				int dayAllCount = Integer.valueOf(String.valueOf(item.get("triggerCount")));
				int daySucCount = 0;
				int dayFailCount = dayAllCount - daySucCount;

				if (CollectionUtils.isNotEmpty(triggerCountMapSuc)) {
					for (Map<String, Object> sucItem : triggerCountMapSuc) {
						String daySuc = String.valueOf(sucItem.get("triggerDay"));
						if (day.equals(daySuc)) {
							daySucCount = Integer.valueOf(String.valueOf(sucItem.get("triggerCount")));
							dayFailCount = dayAllCount - daySucCount;
						}
					}
				}

				triggerDayList.add(day);
				triggerDayCountSucList.add(daySucCount);
				triggerDayCountFailList.add(dayFailCount);
				triggerCountSucTotal += daySucCount;
				triggerCountFailTotal += dayFailCount;
			}
		} else {
			for (int i = 4; i > -1; i--) {
				triggerDayList.add(FastDateFormat.getInstance("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -i)));
				triggerDayCountSucList.add(0);
				triggerDayCountFailList.add(0);
			}
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("triggerDayList", triggerDayList);
		result.put("triggerDayCountSucList", triggerDayCountSucList);
		result.put("triggerDayCountFailList", triggerDayCountFailList);
		result.put("triggerCountSucTotal", triggerCountSucTotal);
		result.put("triggerCountFailTotal", triggerCountFailTotal);
		return new ReturnT<Map<String, Object>>(result);
	}

}
