package com.geostax.scheduler.admin.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.geostax.scheduler.admin.model.ScheduleTaskLog;

/**
 * job log
 * @author xuxueli 2016-1-12 18:03:06
 */
public interface IScheduleTaskLogDao {
	
	public List<ScheduleTaskLog> pageList(int offset, int pagesize, int taskGroup, int jobId, Date triggerTimeStart, Date triggerTimeEnd);
	public int pageListCount(int offset, int pagesize, int taskGroup, int taskId, Date triggerTimeStart, Date triggerTimeEnd);
	
	public ScheduleTaskLog load(int id);

	public int save(ScheduleTaskLog ScheduleTaskLog);

	public int updateTriggerInfo(ScheduleTaskLog ScheduleTaskLog);

	public int updateHandleInfo(ScheduleTaskLog ScheduleTaskLog);
	
	public int delete(int jobId);

	public int triggerCountByHandleCode(int handleCode);

	public List<Map<String, Object>> triggerCountByDay(Date from, Date to, int handleCode);

	public int clearLog(int taskGroup, int taskId, Date clearBeforeTime, int clearBeforeNum);

}
