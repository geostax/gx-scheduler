package com.geostax.scheduler.admin.controller;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.geostax.scheduler.admin.GxDynamicScheduler;
import com.geostax.scheduler.admin.controller.annotation.PermessionLimit;
import com.geostax.scheduler.admin.dao.IScheduleTaskGroupDao;
import com.geostax.scheduler.admin.dao.IScheduleTaskInfoDao;
import com.geostax.scheduler.admin.dao.IScheduleTaskLogDao;
import com.geostax.scheduler.admin.dao.ISheduleTaskRegistryDao;
import com.geostax.scheduler.admin.model.ScheduleTaskInfo;
import com.geostax.scheduler.admin.model.ScheduleTaskLog;
import com.geostax.scheduler.core.model.HandleCallbackParam;
import com.geostax.scheduler.core.model.RegistryParam;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.util.AdminApiUtil;

/**
 * Created by xuxueli on 17/5/10.
 */
@Controller
public class JobApiController {
    private static Logger logger = LoggerFactory.getLogger(JobApiController.class);

    @Resource
	private IScheduleTaskGroupDao xxlJobGroupDao;
	@Resource
	private IScheduleTaskInfoDao xxlJobInfoDao;
	@Resource
	public IScheduleTaskLogDao xxlJobLogDao;
	@Resource
	public ISheduleTaskRegistryDao xxlJobRegistryDao;


    @RequestMapping(value= AdminApiUtil.CALLBACK, method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> callback(@RequestBody List<HandleCallbackParam> callbackParamList){

        for (HandleCallbackParam handleCallbackParam: callbackParamList) {
            ReturnT<String> callbackResult = callback(handleCallbackParam);
            logger.info("JobApiController.callback {}, handleCallbackParam={}, callbackResult={}",
                    (callbackResult.getCode()==ReturnT.SUCCESS_CODE?"success":"fail"), handleCallbackParam, callbackResult);
        }

        return ReturnT.SUCCESS;
    }

    private ReturnT<String> callback(HandleCallbackParam handleCallbackParam) {
        // valid log item
        ScheduleTaskLog log = xxlJobLogDao.load(handleCallbackParam.getLogId());
        if (log == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "log item not found.");
        }

        // trigger success, to trigger child job, and avoid repeat trigger child job
        String childTriggerMsg = null;
        if (ReturnT.SUCCESS_CODE==handleCallbackParam.getExecuteResult().getCode() && ReturnT.SUCCESS_CODE!=log.getHandleCode()) {
        	ScheduleTaskInfo xxlJobInfo = xxlJobInfoDao.loadById(log.getTaskId());
            if (xxlJobInfo!=null && StringUtils.isNotBlank(xxlJobInfo.getChildJobKey())) {
                childTriggerMsg = "<hr>";
                String[] childJobKeys = xxlJobInfo.getChildJobKey().split(",");
                for (int i = 0; i < childJobKeys.length; i++) {
                    String[] jobKeyArr = childJobKeys[i].split("_");
                    if (jobKeyArr!=null && jobKeyArr.length==2) {
                    	ScheduleTaskInfo childJobInfo = xxlJobInfoDao.loadById(Integer.valueOf(jobKeyArr[1]));
                        if (childJobInfo!=null) {
                            try {
                                boolean ret = GxDynamicScheduler.triggerJob(String.valueOf(childJobInfo.getId()), String.valueOf(childJobInfo.getTaskGroup()));

                                // add msg
                                childTriggerMsg += MessageFormat.format("<br> {0}/{1} 触发子任务成功, 子任务Key: {2}, status: {3}, 子任务描述: {4}",
                                        (i+1), childJobKeys.length, childJobKeys[i], ret, childJobInfo.getTaskDesc());
                            } catch (SchedulerException e) {
                                logger.error("", e);
                            }
                        } else {
                            childTriggerMsg += MessageFormat.format("<br> {0}/{1} 触发子任务失败, 子任务xxlJobInfo不存在, 子任务Key: {2}",
                                    (i+1), childJobKeys.length, childJobKeys[i]);
                        }
                    } else {
                        childTriggerMsg += MessageFormat.format("<br> {0}/{1} 触发子任务失败, 子任务Key格式错误, 子任务Key: {2}",
                                (i+1), childJobKeys.length, childJobKeys[i]);
                    }
                }

            }
        }

        // handle msg
        StringBuffer handleMsg = new StringBuffer();
        if (log.getHandleMsg()!=null) {
            handleMsg.append(log.getHandleMsg()).append("<br>");
        }
        if (handleCallbackParam.getExecuteResult().getMsg() != null) {
            handleMsg.append(handleCallbackParam.getExecuteResult().getMsg());
        }
        if (childTriggerMsg !=null) {
            handleMsg.append("<br>子任务触发备注：").append(childTriggerMsg);
        }

        // success, save log
        log.setHandleTime(new Date());
        log.setHandleCode(handleCallbackParam.getExecuteResult().getCode());
        log.setHandleMsg(handleMsg.toString());
        xxlJobLogDao.updateHandleInfo(log);

        return ReturnT.SUCCESS;
    }


    @RequestMapping(value=AdminApiUtil.REGISTRY, method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> registry(@RequestBody RegistryParam registryParam){
        int ret = xxlJobRegistryDao.registryUpdate(registryParam.getRegistGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
        if (ret < 1) {
            xxlJobRegistryDao.registrySave(registryParam.getRegistGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
        }
        return ReturnT.SUCCESS;
    }

}
