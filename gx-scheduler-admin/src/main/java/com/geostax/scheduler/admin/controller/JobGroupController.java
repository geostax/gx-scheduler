package com.geostax.scheduler.admin.controller;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.geostax.scheduler.admin.dao.IScheduleTaskGroupDao;
import com.geostax.scheduler.admin.dao.IScheduleTaskInfoDao;
import com.geostax.scheduler.admin.model.ScheduleTaskGroup;
import com.geostax.scheduler.admin.thread.TaskRegistryMonitorHelper;
import com.geostax.scheduler.core.enums.RegistryConfig;
import com.geostax.scheduler.core.model.ReturnT;

/**
 * job group controller
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

    @Resource
	private IScheduleTaskGroupDao xxlJobGroupDao;
	@Resource
	private IScheduleTaskInfoDao xxlJobInfoDao;
	
	@RequestMapping
	public String index(Model model) {

		// job group (executor)
		List<ScheduleTaskGroup> list = xxlJobGroupDao.findAll();

		if (CollectionUtils.isNotEmpty(list)) {
			for (ScheduleTaskGroup group: list) {
				List<String> registryList = null;
				if (group.getAddressType() == 0) {
					registryList = TaskRegistryMonitorHelper.discover(RegistryConfig.RegistType.EXECUTOR.name(), group.getAppName());
				} else {
					if (StringUtils.isNotBlank(group.getAddressList())) {
						registryList = Arrays.asList(group.getAddressList().split(","));
					}
				}
				group.setRegistryList(registryList);
			}
		}

		model.addAttribute("list", list);
		return "jobgroup/jobgroup.index";
	}

	@RequestMapping("/save")
	@ResponseBody
	public ReturnT<String> save(ScheduleTaskGroup xxlJobGroup){

		// valid
		if (xxlJobGroup.getAppName()==null || StringUtils.isBlank(xxlJobGroup.getAppName())) {
			return new ReturnT<String>(500, "请输入AppName");
		}
		if (xxlJobGroup.getAppName().length()>64) {
			return new ReturnT<String>(500, "AppName长度限制为4~64");
		}
		if (xxlJobGroup.getTitle()==null || StringUtils.isBlank(xxlJobGroup.getTitle())) {
			return new ReturnT<String>(500, "请输入名称");
		}
		if (xxlJobGroup.getAddressType()!=0) {
			if (StringUtils.isBlank(xxlJobGroup.getAddressList())) {
				return new ReturnT<String>(500, "手动录入注册方式，机器地址不可为空");
			}
			String[] addresss = xxlJobGroup.getAddressList().split(",");
			for (String item: addresss) {
				if (StringUtils.isBlank(item)) {
					return new ReturnT<String>(500, "机器地址非法");
				}
			}
		}

		int ret = xxlJobGroupDao.save(xxlJobGroup);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(ScheduleTaskGroup xxlJobGroup){
		// valid
		if (xxlJobGroup.getAppName()==null || StringUtils.isBlank(xxlJobGroup.getAppName())) {
			return new ReturnT<String>(500, "请输入AppName");
		}
		if (xxlJobGroup.getAppName().length()>64) {
			return new ReturnT<String>(500, "AppName长度限制为4~64");
		}
		if (xxlJobGroup.getTitle()==null || StringUtils.isBlank(xxlJobGroup.getTitle())) {
			return new ReturnT<String>(500, "请输入名称");
		}
		if (xxlJobGroup.getAddressType()!=0) {
			if (StringUtils.isBlank(xxlJobGroup.getAddressList())) {
				return new ReturnT<String>(500, "手动录入注册方式，机器地址不可为空");
			}
			String[] addresss = xxlJobGroup.getAddressList().split(",");
			for (String item: addresss) {
				if (StringUtils.isBlank(item)) {
					return new ReturnT<String>(500, "机器地址非法");
				}
			}
		}

		int ret = xxlJobGroupDao.update(xxlJobGroup);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id){

		// valid
		int count = xxlJobInfoDao.pageListCount(0, 10, id, null);
		if (count > 0) {
			return new ReturnT<String>(500, "该分组使用中, 不可删除");
		}

		List<ScheduleTaskGroup> allList = xxlJobGroupDao.findAll();
		if (allList.size() == 1) {
			return new ReturnT<String>(500, "删除失败, 系统需要至少预留一个默认分组");
		}

		int ret = xxlJobGroupDao.remove(id);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

}
