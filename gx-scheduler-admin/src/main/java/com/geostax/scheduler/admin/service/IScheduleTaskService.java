package com.geostax.scheduler.admin.service;


import java.util.Map;

import com.geostax.scheduler.admin.model.ScheduleTaskInfo;
import com.geostax.scheduler.core.model.ReturnT;

/**
 * core job action for xxl-job
 * 
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface IScheduleTaskService {
	
	public Map<String, Object> pageList(int start, int length, int taskGroup, String executorHandler, String filterTime);
	
	public ReturnT<String> add(ScheduleTaskInfo taskInfo);
	
	public ReturnT<String> reschedule(ScheduleTaskInfo taskInfo);
	
	public ReturnT<String> remove(int id);
	
	public ReturnT<String> pause(int id);
	
	public ReturnT<String> resume(int id);
	
	public ReturnT<String> triggerJob(int id);

	public Map<String,Object> dashboardInfo();

	public ReturnT<Map<String,Object>> triggerChartDate();

}
