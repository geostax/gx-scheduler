package com.geostax.scheduler.core.executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.geostax.scheduler.core.rpc.netcom.NetComServerFactory;
import com.geostax.scheduler.core.thread.ExecutorRegistryThread;
import com.geostax.scheduler.core.thread.TaskThread;
import com.geostax.scheduler.core.thread.TriggerCallbackThread;
import com.geostax.scheduler.core.util.AdminApiUtil;

/**
 * 
 * 
 * @author Created by Phil XIAO on 2016/3/2 21:14.
 *
 */
public class ScheduleTaskExecutor implements ApplicationContextAware, ApplicationListener {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleTaskExecutor.class);

	private String ip;
	private int port = 9999;
	private String appName;
	private String adminAddresses;
	public static String logPath;

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setAdminAddresses(String adminAddresses) {
		this.adminAddresses = adminAddresses;
	}

	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}

	/**
	 * Job Server
	 */
	private NetComServerFactory serverFactory = new NetComServerFactory();

	public void start() throws Exception {
		// admin api util init
		AdminApiUtil.init(adminAddresses);

		// executor start
		NetComServerFactory.putService(IExecutorService.class, new ExecutorServiceImpl());
		serverFactory.start(port, ip, appName);

		// trigger callback thread start
		TriggerCallbackThread.getInstance().start();
	}

	public void destroy() {
		// 1、executor registry thread stop
		ExecutorRegistryThread.getInstance().toStop();

		// 2、executor stop
		serverFactory.destroy();

		// 3、job thread repository destory
		if (TaskThreadRepository.size() > 0) {
			for (Map.Entry<Integer, TaskThread> item : TaskThreadRepository.entrySet()) {
				TaskThread jobThread = item.getValue();
				jobThread.toStop("Web容器销毁终止");
				jobThread.interrupt();

			}
			TaskThreadRepository.clear();
		}

		// 4、trigger callback thread stop
		TriggerCallbackThread.getInstance().toStop();
	}

	/**
	 * init job handler
	 */
	public static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ScheduleTaskExecutor.applicationContext = applicationContext;

		// init job handler action
		Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(TaskHandler.class);

		if (serviceBeanMap != null && serviceBeanMap.size() > 0) {
			for (Object serviceBean : serviceBeanMap.values()) {
				if (serviceBean instanceof ITaskHandler) {
					String name = serviceBean.getClass().getAnnotation(TaskHandler.class).value();
					ITaskHandler handler = (ITaskHandler) serviceBean;
					registTaskHandler(name, handler);
				}
			}
		}
	}

	/**
	 * destory task executor
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		if (applicationEvent instanceof ContextClosedEvent) {
			// TODO
		}
	}

	/**
	 * Schedule task repository
	 */
	private static ConcurrentHashMap<String, ITaskHandler> TaskHandlerRepository = new ConcurrentHashMap<String, ITaskHandler>();

	public static ITaskHandler registTaskHandler(String name, ITaskHandler handler) {
		logger.info("Gx-Scheduler register scheduleTask success, name:{}, task:{}", name, handler);
		return TaskHandlerRepository.put(name, handler);
	}

	public static ITaskHandler loadTaskHandler(String name) {
		return TaskHandlerRepository.get(name);
	}

	/**
	 * task thread repository
	 */
	private static ConcurrentHashMap<Integer, TaskThread> TaskThreadRepository = new ConcurrentHashMap<Integer, TaskThread>();

	public static TaskThread registTaskThread(int jobId, ITaskHandler handler, String removeOldReason) {
		TaskThread newJobThread = new TaskThread(jobId, handler);
		newJobThread.start();
		logger.info(">>>>>>>>>>> xxl-job regist JobThread success, jobId:{}, handler:{}",
				new Object[] { jobId, handler });

		TaskThread oldJobThread = TaskThreadRepository.put(jobId, newJobThread);
		// putIfAbsent | oh my god, map's put method return the old value!!!
		if (oldJobThread != null) {
			oldJobThread.toStop(removeOldReason);
			oldJobThread.interrupt();
		}

		return newJobThread;
	}

	public static void removeTaskThread(int jobId, String removeOldReason) {
		TaskThread oldJobThread = TaskThreadRepository.remove(jobId);
		if (oldJobThread != null) {
			oldJobThread.toStop(removeOldReason);
			oldJobThread.interrupt();
		}
	}

	public static TaskThread loadTaskThread(int jobId) {
		TaskThread jobThread = TaskThreadRepository.get(jobId);
		return jobThread;
	}

}
