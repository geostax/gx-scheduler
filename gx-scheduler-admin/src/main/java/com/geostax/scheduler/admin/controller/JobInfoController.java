package com.geostax.scheduler.admin.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.geostax.scheduler.admin.dao.IScheduleTaskGroupDao;
import com.geostax.scheduler.admin.enums.ExecutorFailStrategyEnum;
import com.geostax.scheduler.admin.model.ScheduleTaskGroup;
import com.geostax.scheduler.admin.model.ScheduleTaskInfo;
import com.geostax.scheduler.admin.route.ExecutorRouteStrategyEnum;
import com.geostax.scheduler.admin.service.IScheduleTaskService;
import com.geostax.scheduler.core.enums.ExecutorBlockStrategyEnum;
import com.geostax.scheduler.core.model.ReturnT;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {

    @Resource
	private IScheduleTaskGroupDao xxlJobGroupDao;

	@Resource
	private IScheduleTaskService xxlJobService;
	
	@RequestMapping
	public String index(Model model) {

		// 枚举-字典
		model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	// 路由策略-列表							// Glue类型-字典
		model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	// 阻塞处理策略-字典
		model.addAttribute("ExecutorFailStrategyEnum", ExecutorFailStrategyEnum.values());		// 失败处理策略-字典

		// 任务组
		List<ScheduleTaskGroup> jobGroupList =  xxlJobGroupDao.findAll();
		model.addAttribute("JobGroupList", jobGroupList);
		return "jobinfo/jobinfo.index";
	}
	
	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,  
			@RequestParam(required = false, defaultValue = "10") int length,
			int taskGroup, String executorHandler, String filterTime) {
		return xxlJobService.pageList(start, length, taskGroup, executorHandler, filterTime);
	}
	
	@RequestMapping("/add")
	@ResponseBody
	public ReturnT<String> add(ScheduleTaskInfo jobInfo) {
		return xxlJobService.add(jobInfo);
	}
	
	@RequestMapping("/reschedule")
	@ResponseBody
	public ReturnT<String> reschedule(ScheduleTaskInfo jobInfo) {
		return xxlJobService.reschedule(jobInfo);
	}
	
	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id) {
		return xxlJobService.remove(id);
	}
	
	@RequestMapping("/pause")
	@ResponseBody
	public ReturnT<String> pause(int id) {
		return xxlJobService.pause(id);
	}
	
	@RequestMapping("/resume")
	@ResponseBody
	public ReturnT<String> resume(int id) {
		return xxlJobService.resume(id);
	}
	
	@RequestMapping("/trigger")
	@ResponseBody
	public ReturnT<String> triggerJob(int id) {
		return xxlJobService.triggerJob(id);
	}
	
}
