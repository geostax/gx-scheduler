package com.geostax.scheduler.core.executor;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geostax.scheduler.core.enums.ExecutorBlockStrategyEnum;
import com.geostax.scheduler.core.log.ScheduleTaskFileAppender;
import com.geostax.scheduler.core.model.LogResult;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.model.TriggerParam;
import com.geostax.scheduler.core.thread.TaskThread;

public class ExecutorServiceImpl implements IExecutorService {

	private static Logger logger = LoggerFactory.getLogger(ExecutorServiceImpl.class);

	@Override
	public ReturnT<String> beat() {
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> idleBeat(int taskId) {

		// isRunningOrHasQueue
		boolean isRunningOrHasQueue = false;
		TaskThread TaskThread = ScheduleTaskExecutor.loadTaskThread(taskId);
		if (TaskThread != null && TaskThread.isRunningOrHasQueue()) {
			isRunningOrHasQueue = true;
		}

		if (isRunningOrHasQueue) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "job thread is running or has trigger queue.");
		}
		return ReturnT.SUCCESS;
	}

	@Override
	public ReturnT<String> kill(int taskId) {
		// kill handlerThread, and create new one
		TaskThread TaskThread = ScheduleTaskExecutor.loadTaskThread(taskId);
		if (TaskThread != null) {
			ScheduleTaskExecutor.removeTaskThread(taskId, "人工手动终止");
			return ReturnT.SUCCESS;
		}

		return new ReturnT<String>(ReturnT.SUCCESS_CODE, "Task thread aleady killed.");
	}

	@Override
	public ReturnT<LogResult> log(long logDateTim, int logId, int fromLineNum) {
		String logFileName = ScheduleTaskFileAppender.makeLogFileName(new Date(logDateTim), logId);
		LogResult logResult = ScheduleTaskFileAppender.readLog(logFileName, fromLineNum);
		return new ReturnT<LogResult>(logResult);
	}

	@Override
	public ReturnT<String> run(TriggerParam triggerParam) {
		// load old：jobHandler + TaskThread
		TaskThread taskThread = ScheduleTaskExecutor.loadTaskThread(triggerParam.getTaskId());
		ITaskHandler taskHandler = taskThread != null ? taskThread.getHandler() : null;
		String removeOldReason = null;

		// valid old TaskThread
		if (taskThread != null && taskHandler != null && taskThread.getHandler() != taskHandler) {
			// change handler, need kill old thread
			removeOldReason = "更新TaskHandler或更换任务模式,终止旧任务线程";

			taskThread = null;
			taskHandler = null;
		}

		// valid handler
		if (taskHandler == null) {
			taskHandler = ScheduleTaskExecutor.loadTaskHandler(triggerParam.getExecutorHandler());
			if (taskHandler == null) {
				return new ReturnT<String>(ReturnT.FAIL_CODE,
						"task handler [" + triggerParam.getExecutorHandler() + "] not found.");
			}
		}

		// executor block strategy
		if (taskThread != null) {
			ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum
					.match(triggerParam.getExecutorBlockStrategy(), null);
			if (ExecutorBlockStrategyEnum.DISCARD_LATER == blockStrategy) {
				// discard when running
				if (taskThread.isRunningOrHasQueue()) {
					return new ReturnT<String>(ReturnT.FAIL_CODE,
							"阻塞处理策略-生效：" + ExecutorBlockStrategyEnum.DISCARD_LATER.getTitle());
				}
			} else if (ExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy) {
				// kill running TaskThread
				if (taskThread.isRunningOrHasQueue()) {
					removeOldReason = "阻塞处理策略-生效：" + ExecutorBlockStrategyEnum.COVER_EARLY.getTitle();

					taskThread = null;
				}
			} else {
				// just queue trigger
			}
		}

		// replace thread (new or exists invalid)
		if (taskThread == null) {
			taskThread = ScheduleTaskExecutor.registTaskThread(triggerParam.getTaskId(), taskHandler, removeOldReason);
		}

		// push data to queue
		ReturnT<String> pushResult = taskThread.pushTriggerQueue(triggerParam);
		return pushResult;
	}

}
