package com.geostax.scheduler.admin.dao;

import java.util.List;

import com.geostax.scheduler.admin.model.ScheduleTaskInfo;


/**
 * job info
 * @author xuxueli 2016-1-12 18:03:45
 */
public interface IScheduleTaskInfoDao {

	public List<ScheduleTaskInfo> pageList(int offset, int pagesize, int taskGroup, String executorHandler);
	public int pageListCount(int offset, int pagesize, int taskGroup, String executorHandler);
	
	public int save(ScheduleTaskInfo info);

	public ScheduleTaskInfo loadById(int id);
	
	public int update(ScheduleTaskInfo item);
	
	public int delete(int id);

	public List<ScheduleTaskInfo> getTasksByGroup(String taskGroup);

	public int findAllCount();

}
