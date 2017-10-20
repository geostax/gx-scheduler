package com.geostax.scheduler.admin.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.geostax.scheduler.admin.dao.ISheduleTaskRegistryDao;
import com.geostax.scheduler.admin.model.ScheduleTaskRegistry;

/**
 * Created by xuxueli on 16/9/30.
 */
@Repository
public class ScheduleTaskRegistryDaoImpl implements ISheduleTaskRegistryDao {

    @Resource
    public SqlSessionTemplate sqlSessionTemplate;

    @Override
    public int removeDead(int timeout) {
        return sqlSessionTemplate.delete("ScheduleTaskRegistryMapper.removeDead", timeout);
    }

    @Override
    public List<ScheduleTaskRegistry> findAll(int timeout) {
        return sqlSessionTemplate.selectList("ScheduleTaskRegistryMapper.findAll", timeout);
    }

    @Override
    public int registryUpdate(String registryGroup, String registryKey, String registryValue) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("registryGroup", registryGroup);
        params.put("registryKey", registryKey);
        params.put("registryValue", registryValue);

        return sqlSessionTemplate.update("ScheduleTaskRegistryMapper.registryUpdate", params);
    }

    @Override
    public int registrySave(String registryGroup, String registryKey, String registryValue) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("registryGroup", registryGroup);
        params.put("registryKey", registryKey);
        params.put("registryValue", registryValue);

        return sqlSessionTemplate.update("ScheduleTaskRegistryMapper.registrySave", params);
    }

}
