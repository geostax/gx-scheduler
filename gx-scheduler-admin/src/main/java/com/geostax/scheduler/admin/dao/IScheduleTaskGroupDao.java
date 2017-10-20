package com.geostax.scheduler.admin.dao;

import java.util.List;

import com.geostax.scheduler.admin.model.ScheduleTaskGroup;

/**
 * Created by xuxueli on 16/9/30.
 */
public interface IScheduleTaskGroupDao {

    public List<ScheduleTaskGroup> findAll();

    public int save(ScheduleTaskGroup ScheduleTaskGroup);

    public int update(ScheduleTaskGroup ScheduleTaskGroup);

    public int remove(int id);

    public ScheduleTaskGroup load(int id);
}
