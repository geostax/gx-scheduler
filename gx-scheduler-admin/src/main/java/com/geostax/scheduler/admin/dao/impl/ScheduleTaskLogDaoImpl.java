package com.geostax.scheduler.admin.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.geostax.scheduler.admin.dao.IScheduleTaskLogDao;
import com.geostax.scheduler.admin.model.ScheduleTaskLog;

/**
 * job log
 * @author xuxueli 2016-1-12 18:03:06
 */
@Repository
public class ScheduleTaskLogDaoImpl implements IScheduleTaskLogDao {
	
	@Resource
	public SqlSessionTemplate sqlSessionTemplate;

	@Override
	public List<ScheduleTaskLog> pageList(int offset, int pagesize, int taskGroup, int jobId, Date triggerTimeStart, Date triggerTimeEnd) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("offset", offset);
		params.put("pagesize", pagesize);
		params.put("jobGroup", taskGroup);
		params.put("jobId", jobId);
		params.put("triggerTimeStart", triggerTimeStart);
		params.put("triggerTimeEnd", triggerTimeEnd);
		
		return sqlSessionTemplate.selectList("ScheduleTaskLogMapper.pageList", params);
	}

	@Override
	public int pageListCount(int offset, int pagesize, int taskGroup, int taskId, Date triggerTimeStart, Date triggerTimeEnd) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("offset", offset);
		params.put("pagesize", pagesize);
		params.put("jobGroup", taskGroup);
		params.put("jobId", taskId);
		params.put("triggerTimeStart", triggerTimeStart);
		params.put("triggerTimeEnd", triggerTimeEnd);
		
		return sqlSessionTemplate.selectOne("ScheduleTaskLogMapper.pageListCount", params);
	}

	@Override
	public ScheduleTaskLog load(int id) {
		return sqlSessionTemplate.selectOne("ScheduleTaskLogMapper.load", id);
	}

	@Override
	public int save(ScheduleTaskLog taskLog) {
		return sqlSessionTemplate.insert("ScheduleTaskLogMapper.save", taskLog);
	}

	@Override
	public int updateTriggerInfo(ScheduleTaskLog taskLog) {
		if (taskLog.getTriggerMsg()!=null && taskLog.getTriggerMsg().length()>2000) {
			taskLog.setTriggerMsg(taskLog.getTriggerMsg().substring(0, 2000));
		}
		return sqlSessionTemplate.update("ScheduleTaskLogMapper.updateTriggerInfo", taskLog);
	}

	@Override
	public int updateHandleInfo(ScheduleTaskLog taskLog) {
		if (taskLog.getHandleMsg()!=null && taskLog.getHandleMsg().length()>2000) {
			taskLog.setHandleMsg(taskLog.getHandleMsg().substring(0, 2000));
		}
		return sqlSessionTemplate.update("ScheduleTaskLogMapper.updateHandleInfo", taskLog);
	}

	@Override
	public int delete(int jobId) {
		return sqlSessionTemplate.delete("ScheduleTaskLogMapper.delete", jobId);
	}

	@Override
	public int triggerCountByHandleCode(int handleCode) {
		return sqlSessionTemplate.selectOne("ScheduleTaskLogMapper.triggerCountByHandleCode", handleCode);
	}

	@Override
	public List<Map<String, Object>> triggerCountByDay(Date from, Date to, int handleCode) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("from", from);
		params.put("to", to);
		params.put("handleCode", handleCode);
		return sqlSessionTemplate.selectList("ScheduleTaskLogMapper.triggerCountByDay", params);
	}

	@Override
	public int clearLog(int taskGroup, int taskId, Date clearBeforeTime, int clearBeforeNum) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("taskGroup", taskGroup);
		params.put("taskId", taskId);
		params.put("clearBeforeTime", clearBeforeTime);
		params.put("clearBeforeNum", clearBeforeNum);
		return sqlSessionTemplate.delete("ScheduleTaskLogMapper.clearLog", params);
	}

}
