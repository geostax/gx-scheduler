package com.geostax.scheduler.admin.dao.impl;

import java.util.List;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.geostax.scheduler.admin.dao.IScheduleTaskGroupDao;
import com.geostax.scheduler.admin.model.ScheduleTaskGroup;

/**
 * Created by xuxueli on 16/9/30.
 */

@Repository
public class ScheduleTaskGroupDaoImpl implements IScheduleTaskGroupDao {

    @Resource
    public SqlSessionTemplate sqlSessionTemplate;

    @Override
    public List<ScheduleTaskGroup> findAll() {
        return sqlSessionTemplate.selectList("ScheduleTaskGroupMapper.findAll");
    }

    @Override
    public int save(ScheduleTaskGroup ScheduleTaskGroup) {
        return sqlSessionTemplate.update("ScheduleTaskGroupMapper.save", ScheduleTaskGroup);
    }

    @Override
    public int update(ScheduleTaskGroup ScheduleTaskGroup) {
        return sqlSessionTemplate.update("ScheduleTaskGroupMapper.update", ScheduleTaskGroup);
    }

    @Override
    public int remove(int id) {
        return sqlSessionTemplate.delete("ScheduleTaskGroupMapper.remove", id);
    }

    @Override
    public ScheduleTaskGroup load(int id) {
        return sqlSessionTemplate.selectOne("ScheduleTaskGroupMapper.load", id);
    }


}
