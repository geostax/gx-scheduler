package com.geostax.scheduler.admin.dao.impl;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.geostax.scheduler.admin.dao.IScheduleTaskInfoDao;
import com.geostax.scheduler.admin.model.ScheduleTaskInfo;

/**
 * job info
 * 
 * @author xuxueli 2016-1-12 18:03:45
 */
@Repository
public class ScheduleTaskInfoDaoImpl implements IScheduleTaskInfoDao {

	@Resource
	public SqlSessionTemplate sqlSessionTemplate;

	@Override
	public List<ScheduleTaskInfo> pageList(int offset, int pagesize, int taskGroup, String executorHandler) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("offset", offset);
		params.put("pagesize", pagesize);
		params.put("taskGroup", taskGroup);
		params.put("executorHandler", executorHandler);
		return sqlSessionTemplate.selectList("ScheduleTaskInfoMapper.pageList", params);
	}

	@Override
	public int pageListCount(int offset, int pagesize, int taskGroup, String executorHandler) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("offset", offset);
		params.put("pagesize", pagesize);
		params.put("taskGroup", taskGroup);
		params.put("executorHandler", executorHandler);

		return sqlSessionTemplate.selectOne("ScheduleTaskInfoMapper.pageListCount", params);
	}

	@Override
	public int save(ScheduleTaskInfo info) {
		return sqlSessionTemplate.insert("ScheduleTaskInfoMapper.save", info);
	}

	@Override
	public ScheduleTaskInfo loadById(int id) {
		return sqlSessionTemplate.selectOne("ScheduleTaskInfoMapper.loadById", id);
	}

	@Override
	public int update(ScheduleTaskInfo item) {
		return sqlSessionTemplate.update("ScheduleTaskInfoMapper.update", item);
	}

	@Override
	public int delete(int id) {
		return sqlSessionTemplate.update("ScheduleTaskInfoMapper.delete", id);
	}

	@Override
	public List<ScheduleTaskInfo> getTasksByGroup(String taskGroup) {
		return sqlSessionTemplate.selectList("ScheduleTaskInfoMapper.getJobsByGroup", taskGroup);
	}

	@Override
	public int findAllCount() {
		return sqlSessionTemplate.selectOne("ScheduleTaskInfoMapper.findAllCount");
	}

}
