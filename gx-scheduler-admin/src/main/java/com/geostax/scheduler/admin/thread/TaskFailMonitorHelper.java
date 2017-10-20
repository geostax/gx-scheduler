package com.geostax.scheduler.admin.thread;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geostax.scheduler.admin.GxDynamicScheduler;
import com.geostax.scheduler.admin.model.ScheduleTaskGroup;
import com.geostax.scheduler.admin.model.ScheduleTaskInfo;
import com.geostax.scheduler.admin.model.ScheduleTaskLog;
import com.geostax.scheduler.admin.util.MailUtil;
import com.geostax.scheduler.core.model.ReturnT;

/**
 * job monitor instance
 * 
 * @author xuxueli 2015-9-1 18:05:56
 */
public class TaskFailMonitorHelper {
	private static Logger logger = LoggerFactory.getLogger(TaskFailMonitorHelper.class);

	private static TaskFailMonitorHelper instance = new TaskFailMonitorHelper();

	public static TaskFailMonitorHelper getInstance() {
		return instance;
	}

	private LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>(0xfff8);

	private Thread monitorThread;
	private boolean toStop = false;

	public void start() {
		monitorThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!toStop) {
					try {
						logger.debug(">>>>>>>>>>> job monitor beat ... ");
						Integer jobLogId = TaskFailMonitorHelper.instance.queue.take();
						if (jobLogId != null && jobLogId > 0) {
							logger.debug(">>>>>>>>>>> job monitor heat success, JobLogId:{}", jobLogId);
							ScheduleTaskLog log = GxDynamicScheduler.scheduleTaskLogDao.load(jobLogId);
							if (log != null) {
								if (ReturnT.SUCCESS_CODE == log.getTriggerCode() && log.getHandleCode() == 0) {
									// running
									try {
										TimeUnit.SECONDS.sleep(10);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									TaskFailMonitorHelper.monitor(jobLogId);
								}
								if (ReturnT.SUCCESS_CODE == log.getTriggerCode()
										&& ReturnT.SUCCESS_CODE == log.getHandleCode()) {
									// pass
								}
								if (ReturnT.FAIL_CODE == log.getTriggerCode()
										|| ReturnT.FAIL_CODE == log.getHandleCode()) {
									ScheduleTaskInfo info = GxDynamicScheduler.scheduleTaskInfoDao
											.loadById(log.getTaskId());
									if (info != null && info.getAlarmEmail() != null
											&& info.getAlarmEmail().trim().length() > 0) {

										Set<String> emailSet = new HashSet<String>(
												Arrays.asList(info.getAlarmEmail().split(",")));
										for (String email : emailSet) {
											String title = "《调度监控报警》(任务调度中心XXL-JOB)";
											ScheduleTaskGroup group = GxDynamicScheduler.scheduleTaskGroupDao
													.load(Integer.valueOf(info.getTaskGroup()));
											String content = MessageFormat.format("任务调度失败, 执行器名称:{0}, 任务描述:{1}.",
													group != null ? group.getTitle() : "null", info.getTaskDesc());
											MailUtil.sendMail(email, title, content, false, null);
										}
									}
								}
							}
						}
					} catch (Exception e) {
						logger.error("job monitor error:{}", e);
					}
				}
			}
		});
		monitorThread.setDaemon(true);
		monitorThread.start();
	}

	public void toStop() {
		toStop = true;
		// monitorThread.interrupt();
	}

	// producer
	public static void monitor(int jobLogId) {
		getInstance().queue.offer(jobLogId);
	}

}
