package com.geostax.scheduler.core.executor;

import com.geostax.scheduler.core.model.ReturnT;

/**
 * annotation for task handler
 * A task handler can be triggered by Quartz scheduler to achieve a user-specified task
 * 
 * @author Phil XIAO 2016-5-17 21:06:49
 */
public interface ITaskHandler {

	/**
	 * job handler
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public abstract ReturnT<String> execute(String... params) throws Exception;
	
}
