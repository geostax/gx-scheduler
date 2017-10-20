package com.geostax.scheduler.admin.dao;

import java.util.List;

import com.geostax.scheduler.admin.model.ScheduleTaskRegistry;

/**
 * Created by xuxueli on 16/9/30.
 */
public interface ISheduleTaskRegistryDao {
    public int removeDead(int timeout);

    public List<ScheduleTaskRegistry> findAll(int timeout);

    public int registryUpdate(String registryGroup, String registryKey, String registryValue);

    public int registrySave(String registryGroup, String registryKey, String registryValue);

}
