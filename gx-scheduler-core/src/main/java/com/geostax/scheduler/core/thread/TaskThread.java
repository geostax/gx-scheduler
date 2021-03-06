package com.geostax.scheduler.core.thread;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geostax.scheduler.core.executor.ITaskHandler;
import com.geostax.scheduler.core.executor.ScheduleTaskExecutor;
import com.geostax.scheduler.core.log.ScheduleTaskFileAppender;
import com.geostax.scheduler.core.log.ScheduleTaskLogger;
import com.geostax.scheduler.core.model.HandleCallbackParam;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.model.TriggerParam;

/**
 * handler thread
 * 
 * @author xuxueli 2016-1-16 19:52:47
 */
public class TaskThread extends Thread {
	private static Logger logger = LoggerFactory.getLogger(TaskThread.class);

	private int jobId;
	private ITaskHandler handler;
	private LinkedBlockingQueue<TriggerParam> triggerQueue;
	private ConcurrentHashSet<Integer> triggerLogIdSet; // avoid repeat trigger
														// for the same
														// TRIGGER_LOG_ID

	private boolean toStop = false;
	private String stopReason;

	private boolean running = false; // if running job
	private int idleTimes = 0; // idel times

	public TaskThread(int jobId, ITaskHandler handler) {
		this.jobId = jobId;
		this.handler = handler;
		this.triggerQueue = new LinkedBlockingQueue<TriggerParam>();
		this.triggerLogIdSet = new ConcurrentHashSet<Integer>();
	}

	public ITaskHandler getHandler() {
		return handler;
	}

	/**
	 * new trigger to queue
	 *
	 * @param triggerParam
	 * @return
	 */
	public ReturnT<String> pushTriggerQueue(TriggerParam triggerParam) {
		// avoid repeat
		if (triggerLogIdSet.contains(triggerParam.getLogId())) {
			logger.debug("repeate trigger job, logId:{}", triggerParam.getLogId());
			return new ReturnT<String>(ReturnT.FAIL_CODE, "repeate trigger job, logId:" + triggerParam.getLogId());
		}

		triggerLogIdSet.add(triggerParam.getLogId());
		triggerQueue.add(triggerParam);
		return ReturnT.SUCCESS;
	}

	/**
	 * kill job thread
	 *
	 * @param stopReason
	 */
	public void toStop(String stopReason) {
		/**
		 * Thread.interrupt只支持终止线程的阻塞状态(wait、join、sleep)，
		 * 在阻塞出抛出InterruptedException异常,但是并不会终止运行的线程本身；
		 * 所以需要注意，此处彻底销毁本线程，需要通过共享变量方式；
		 */
		this.toStop = true;
		this.stopReason = stopReason;
	}

	/**
	 * is running job
	 * 
	 * @return
	 */
	public boolean isRunningOrHasQueue() {
		return running || triggerQueue.size() > 0;
	}

	@Override
	public void run() {
		while (!toStop) {
			running = false;
			idleTimes++;
			try {
				// to check toStop signal, we need cycle, so wo cannot use
				// queue.take(), instand of poll(timeout)
				TriggerParam triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
				if (triggerParam != null) {
					running = true;
					idleTimes = 0;
					triggerLogIdSet.remove(triggerParam.getLogId());

					// parse param
					String[] handlerParams = (triggerParam.getExecutorParams() != null
							&& triggerParam.getExecutorParams().trim().length() > 0)
									? (String[]) (Arrays.asList(triggerParam.getExecutorParams().split(",")).toArray())
									: null;

					// handle task
					ReturnT<String> executeResult = null;
					try {
						// log filename: yyyy-MM-dd/9999.log
						String logFileName = ScheduleTaskFileAppender
								.makeLogFileName(new Date(triggerParam.getLogDateTim()), triggerParam.getLogId());

						ScheduleTaskFileAppender.contextHolder.set(logFileName);
						ScheduleTaskLogger
								.log("<br>----------- xxl-job job execute start -----------<br>----------- Params:"
										+ Arrays.toString(handlerParams));

						executeResult = handler.execute(handlerParams);
						if (executeResult == null) {
							executeResult = ReturnT.FAIL;
						}

						ScheduleTaskLogger
								.log("<br>----------- xxl-job job execute end(finish) -----------<br>----------- ReturnT:"
										+ executeResult);
					} catch (Exception e) {
						if (toStop) {
							ScheduleTaskLogger.log("<br>----------- JobThread toStop, stopReason:" + stopReason);
						}

						StringWriter stringWriter = new StringWriter();
						e.printStackTrace(new PrintWriter(stringWriter));
						String errorMsg = stringWriter.toString();
						executeResult = new ReturnT<String>(ReturnT.FAIL_CODE, errorMsg);

						ScheduleTaskLogger.log("<br>----------- JobThread Exception:" + errorMsg
								+ "<br>----------- xxl-job job execute end(error) -----------");
					}

					// callback handler info
					if (!toStop) {
						// commonm
						TriggerCallbackThread
								.pushCallBack(new HandleCallbackParam(triggerParam.getLogId(), executeResult));
					} else {
						// is killed
						ReturnT<String> stopResult = new ReturnT<String>(ReturnT.FAIL_CODE,
								stopReason + " [业务运行中，被强制终止]");
						TriggerCallbackThread
								.pushCallBack(new HandleCallbackParam(triggerParam.getLogId(), stopResult));
					}
				} else {
					if (idleTimes > 30) {
						ScheduleTaskExecutor.removeTaskThread(jobId, "excutor idel times over limit.");
					}
				}
			} catch (Throwable e) {
				if (toStop) {
					ScheduleTaskLogger.log("<br>----------- xxl-job toStop, stopReason:" + stopReason);
				}

				StringWriter stringWriter = new StringWriter();
				e.printStackTrace(new PrintWriter(stringWriter));
				String errorMsg = stringWriter.toString();
				ScheduleTaskLogger.log("----------- xxl-job JobThread Exception:" + errorMsg);
			}
		}

		// callback trigger request in queue
		while (triggerQueue != null && triggerQueue.size() > 0) {
			TriggerParam triggerParam = triggerQueue.poll();
			if (triggerParam != null) {
				// is killed
				ReturnT<String> stopResult = new ReturnT<String>(ReturnT.FAIL_CODE, stopReason + " [任务尚未执行，在调度队列中被终止]");
				TriggerCallbackThread.pushCallBack(new HandleCallbackParam(triggerParam.getLogId(), stopResult));
			}
		}

		logger.info(">>>>>>>>>>>> xxl-job JobThread stoped, hashCode:{}", Thread.currentThread());
	}
}
