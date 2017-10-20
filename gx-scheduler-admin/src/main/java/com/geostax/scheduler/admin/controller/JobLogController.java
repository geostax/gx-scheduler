package com.geostax.scheduler.admin.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.geostax.scheduler.admin.dao.IScheduleTaskGroupDao;
import com.geostax.scheduler.admin.dao.IScheduleTaskInfoDao;
import com.geostax.scheduler.admin.dao.IScheduleTaskLogDao;
import com.geostax.scheduler.admin.model.ScheduleTaskGroup;
import com.geostax.scheduler.admin.model.ScheduleTaskInfo;
import com.geostax.scheduler.admin.model.ScheduleTaskLog;
import com.geostax.scheduler.core.executor.IExecutorService;
import com.geostax.scheduler.core.model.LogResult;
import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.rpc.netcom.NetComClientProxy;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/joblog")
public class JobLogController {

    @Resource
	private IScheduleTaskGroupDao xxlJobGroupDao;
	@Resource
	private IScheduleTaskInfoDao ScheduleTaskInfoDao;
	@Resource
	public IScheduleTaskLogDao ScheduleTaskLogDao;
	
	@RequestMapping
	public String index(Model model, @RequestParam(required = false, defaultValue = "0") Integer jobId) {

		// 执行器列表
		List<ScheduleTaskGroup> jobGroupList =  xxlJobGroupDao.findAll();
		model.addAttribute("JobGroupList", jobGroupList);

		// 任务
		if (jobId > 0) {
			ScheduleTaskInfo jobInfo = ScheduleTaskInfoDao.loadById(jobId);
			model.addAttribute("jobInfo", jobInfo);
		}

		return "joblog/joblog.index";
	}

	@RequestMapping("/getJobsByGroup")
	@ResponseBody
	public ReturnT<List<ScheduleTaskInfo>> listJobByGroup(String taskGroup){
		List<ScheduleTaskInfo> list = ScheduleTaskInfoDao.getTasksByGroup(taskGroup);
		return new ReturnT<List<ScheduleTaskInfo>>(list);
	}
	
	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,  
			@RequestParam(required = false, defaultValue = "10") int length,
			int jobGroup, int jobId, String filterTime) {
		
		// parse param
		Date triggerTimeStart = null;
		Date triggerTimeEnd = null;
		if (StringUtils.isNotBlank(filterTime)) {
			String[] temp = filterTime.split(" - ");
			if (temp!=null && temp.length == 2) {
				try {
					triggerTimeStart = DateUtils.parseDate(temp[0], new String[]{"yyyy-MM-dd HH:mm:ss"});
					triggerTimeEnd = DateUtils.parseDate(temp[1], new String[]{"yyyy-MM-dd HH:mm:ss"});
				} catch (ParseException e) {	}
			}
		}
		
		// page query
		List<ScheduleTaskLog> list = ScheduleTaskLogDao.pageList(start, length, jobGroup, jobId, triggerTimeStart, triggerTimeEnd);
		int list_count = ScheduleTaskLogDao.pageListCount(start, length, jobGroup, jobId, triggerTimeStart, triggerTimeEnd);
		
		// package result
		Map<String, Object> maps = new HashMap<String, Object>();
	    maps.put("recordsTotal", list_count);		// 总记录数
	    maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
	    maps.put("data", list);  					// 分页列表
		return maps;
	}

	@RequestMapping("/logDetailPage")
	public String logDetailPage(int id, Model model){

		// base check
		ReturnT<String> logStatue = ReturnT.SUCCESS;
		ScheduleTaskLog jobLog = ScheduleTaskLogDao.load(id);
		if (jobLog == null) {
            throw new RuntimeException("抱歉，日志ID非法.");
		}

        model.addAttribute("triggerCode", jobLog.getTriggerCode());
        model.addAttribute("handleCode", jobLog.getHandleCode());
        model.addAttribute("executorAddress", jobLog.getExecutorAddress());
        model.addAttribute("triggerTime", jobLog.getTriggerTime().getTime());
        model.addAttribute("logId", jobLog.getId());
		return "joblog/joblog.detail";
	}

	@RequestMapping("/logDetailCat")
	@ResponseBody
	public ReturnT<LogResult> logDetailCat(String executorAddress, long triggerTime, int logId, int fromLineNum){
		try {
			IExecutorService IExecutorService = (IExecutorService) new NetComClientProxy(IExecutorService.class, executorAddress).getObject();
			ReturnT<LogResult> logResult = IExecutorService.log(triggerTime, logId, fromLineNum);

			// is end
            if (logResult.getContent()!=null && logResult.getContent().getFromLineNum() > logResult.getContent().getToLineNum()) {
                ScheduleTaskLog jobLog = ScheduleTaskLogDao.load(logId);
                if (jobLog.getHandleCode() > 0) {
                    logResult.getContent().setEnd(true);
                }
            }

			return logResult;
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnT<LogResult>(ReturnT.FAIL_CODE, e.getMessage());
		}
	}

	@RequestMapping("/logKill")
	@ResponseBody
	public ReturnT<String> logKill(int id){
		// base check
		ScheduleTaskLog log = ScheduleTaskLogDao.load(id);
		ScheduleTaskInfo jobInfo = ScheduleTaskInfoDao.loadById(log.getTaskId());
		if (jobInfo==null) {
			return new ReturnT<String>(500, "参数异常");
		}
		if (ReturnT.SUCCESS_CODE != log.getTriggerCode()) {
			return new ReturnT<String>(500, "调度失败，无法终止日志");
		}

		// request of kill
		IExecutorService IExecutorService = null;
		try {
			IExecutorService = (IExecutorService) new NetComClientProxy(IExecutorService.class, log.getExecutorAddress()).getObject();
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnT<String>(500, e.getMessage());
		}
		ReturnT<String> runResult = IExecutorService.kill(jobInfo.getId());

		if (ReturnT.SUCCESS_CODE == runResult.getCode()) {
			log.setHandleCode(ReturnT.FAIL_CODE);
			log.setHandleMsg("人为操作主动终止:" + (runResult.getMsg()!=null?runResult.getMsg():""));
			log.setHandleTime(new Date());
			ScheduleTaskLogDao.updateHandleInfo(log);
			return new ReturnT<String>(runResult.getMsg());
		} else {
			return new ReturnT<String>(500, runResult.getMsg());
		}
	}

	@RequestMapping("/clearLog")
	@ResponseBody
	public ReturnT<String> clearLog(int jobGroup, int jobId, int type){

		Date clearBeforeTime = null;
		int clearBeforeNum = 0;
		if (type == 1) {
			clearBeforeTime = DateUtils.addMonths(new Date(), -1);	// 清理一个月之前日志数据
		} else if (type == 2) {
			clearBeforeTime = DateUtils.addMonths(new Date(), -3);	// 清理三个月之前日志数据
		} else if (type == 3) {
			clearBeforeTime = DateUtils.addMonths(new Date(), -6);	// 清理六个月之前日志数据
		} else if (type == 4) {
			clearBeforeTime = DateUtils.addYears(new Date(), -1);	// 清理一年之前日志数据
		} else if (type == 5) {
			clearBeforeNum = 1000;		// 清理一千条以前日志数据
		} else if (type == 6) {
			clearBeforeNum = 10000;		// 清理一万条以前日志数据
		} else if (type == 7) {
			clearBeforeNum = 30000;		// 清理三万条以前日志数据
		} else if (type == 8) {
			clearBeforeNum = 100000;	// 清理十万条以前日志数据
		} else if (type == 9) {
			clearBeforeNum = 0;			// 清理所用日志数据
		} else {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "清理类型参数异常");
		}

		ScheduleTaskLogDao.clearLog(jobGroup, jobId, clearBeforeTime, clearBeforeNum);
		return ReturnT.SUCCESS;
	}

}
