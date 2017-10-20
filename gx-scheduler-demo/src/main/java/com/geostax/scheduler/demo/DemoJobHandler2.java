package com.geostax.scheduler.demo;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.geostax.scheduler.core.executor.ITaskHandler;
import com.geostax.scheduler.core.executor.TaskHandler;
import com.geostax.scheduler.core.log.ScheduleTaskLogger;
import com.geostax.scheduler.core.model.ReturnT;

/**
 * 任务Handler的一个Demo（Bean模式）
 *
 * 开发步骤： 1、继承 “IJobHandler” ； 2、装配到Spring，例如加 “@Service” 注解； 3、加 “@JobHander”
 * 注解，注解value值为新增任务生成的JobKey的值;多个JobKey用逗号分割; 4、执行日志：需要通过 "XxlJobLogger.log"
 * 打印执行日志；
 *
 * @author xuxueli 2015-12-19 19:43:36
 */
@TaskHandler(value = "demoJobHandler2")
@Service
public class DemoJobHandler2 implements ITaskHandler {

	@Override
	public ReturnT<String> execute(String... params) throws Exception {
		ScheduleTaskLogger.log(">>>>>> 触发 DemoJobHandler2");
		return ReturnT.SUCCESS;
	}

	public static void main(String[] args) throws Exception {

	}

}
