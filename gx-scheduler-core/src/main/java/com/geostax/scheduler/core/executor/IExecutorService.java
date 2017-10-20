package com.geostax.scheduler.core.executor;

import com.geostax.scheduler.core.model.LogResult;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.model.TriggerParam;

public interface IExecutorService {

	/**
	 * beat
	 * 
	 * @return
	 */
	public ReturnT<String> beat();

	/**
	 * idle beat
	 *
	 * @param jobId
	 * @return
	 */
	public ReturnT<String> idleBeat(int jobId);

	/**
	 * kill
	 * 
	 * @param jobId
	 * @return
	 */
	public ReturnT<String> kill(int jobId);

	/**
	 * log
	 * 
	 * @param logDateTim
	 * @param logId
	 * @param fromLineNum
	 * @return
	 */
	public ReturnT<LogResult> log(long logDateTim, int logId, int fromLineNum);

	/**
	 * run
	 * 
	 * @param triggerParam
	 * @return
	 */
	public ReturnT<String> run(TriggerParam triggerParam);
}
