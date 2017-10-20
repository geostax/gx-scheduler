package com.geostax.scheduler.admin.model;

import java.util.Date;

/**
 * xxl-job info
 * @author xuxueli  2016-1-12 18:25:49
 */
public class ScheduleTaskInfo {
	
	private int id;				// 主键ID	    (JobKey.name)
	
	private int taskGroup;		// 执行器主键ID	(JobKey.group)
	private String taskCron;		// 任务执行CRON表达式 【base on quartz】
	private String taskDesc;
	
	private Date addTime;
	private Date updateTime;
	
	private String author;		// 负责人
	private String alarmEmail;	// 报警邮件

	private String executorRouteStrategy;	// 执行器路由策略
	private String executorHandler;		    // 执行器，任务Handler名称
	private String executorParam;		    // 执行器，任务参数
	private String executorBlockStrategy;	// 阻塞处理策略
	private String executorFailStrategy;	// 失败处理策略
	
	private String childJobKey;		// 子任务Key
	
	// copy from quartz
	private String taskStatus;		// 任务状态 【base on quartz】

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTaskGroup() {
		return taskGroup;
	}

	public void setTaskGroup(int taskGroup) {
		this.taskGroup = taskGroup;
	}

	public String getTaskCron() {
		return taskCron;
	}

	public void setTaskCron(String taskCron) {
		this.taskCron = taskCron;
	}

	public String getTaskDesc() {
		return taskDesc;
	}

	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAlarmEmail() {
		return alarmEmail;
	}

	public void setAlarmEmail(String alarmEmail) {
		this.alarmEmail = alarmEmail;
	}

    public String getExecutorRouteStrategy() {
        return executorRouteStrategy;
    }

    public void setExecutorRouteStrategy(String executorRouteStrategy) {
        this.executorRouteStrategy = executorRouteStrategy;
    }

    public String getExecutorHandler() {
		return executorHandler;
	}

	public void setExecutorHandler(String executorHandler) {
		this.executorHandler = executorHandler;
	}

	public String getExecutorParam() {
		return executorParam;
	}

	public void setExecutorParam(String executorParam) {
		this.executorParam = executorParam;
	}

	public String getExecutorBlockStrategy() {
		return executorBlockStrategy;
	}

	public void setExecutorBlockStrategy(String executorBlockStrategy) {
		this.executorBlockStrategy = executorBlockStrategy;
	}

	public String getExecutorFailStrategy() {
		return executorFailStrategy;
	}

	public void setExecutorFailStrategy(String executorFailStrategy) {
		this.executorFailStrategy = executorFailStrategy;
	}


	public String getChildJobKey() {
		return childJobKey;
	}

	public void setChildJobKey(String childJobKey) {
		this.childJobKey = childJobKey;
	}

}
