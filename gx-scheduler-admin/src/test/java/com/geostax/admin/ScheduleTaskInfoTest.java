package com.geostax.admin;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.geostax.scheduler.admin.dao.IScheduleTaskInfoDao;
import com.geostax.scheduler.admin.model.ScheduleTaskInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:applicationcontext-*.xml")
public class ScheduleTaskInfoTest {
	
	@Resource
	private IScheduleTaskInfoDao ScheduleTaskInfoDao;
	
	@Test
	public void pageList(){
		List<ScheduleTaskInfo> list = ScheduleTaskInfoDao.pageList(0, 20, 0, null);
		int list_count = ScheduleTaskInfoDao.pageListCount(0, 20, 0, null);
		
		System.out.println(list);
		System.out.println(list_count);
	}
	
	@Test
	public void save_load(){
		ScheduleTaskInfo info = new ScheduleTaskInfo();
		info.setTaskGroup(1);
		info.setTaskCron("jobCron");
		int count = ScheduleTaskInfoDao.save(info);
		System.out.println(count);
		System.out.println(info.getId());

		ScheduleTaskInfo item = ScheduleTaskInfoDao.loadById(2);
		System.out.println(item);
	}
	
	@Test
	public void update(){
		ScheduleTaskInfo item = ScheduleTaskInfoDao.loadById(2);
		
		item.setTaskCron("jobCron2");
		ScheduleTaskInfoDao.update(item);
	}
	
}
