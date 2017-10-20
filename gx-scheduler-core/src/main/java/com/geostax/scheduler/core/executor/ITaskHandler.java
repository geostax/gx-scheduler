package com.geostax.scheduler.core.executor;

import com.geostax.scheduler.core.model.ReturnT;

public interface ITaskHandler {

	/**
	 * job handler
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public abstract ReturnT<String> execute(String... params) throws Exception;
	
}
