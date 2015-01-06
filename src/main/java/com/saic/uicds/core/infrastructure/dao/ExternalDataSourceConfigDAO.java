package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.ExternalDataSourceConfig;

public interface ExternalDataSourceConfigDAO extends GenericDAO<ExternalDataSourceConfig, Integer> {

    public List<ExternalDataSourceConfig> findByUrn(String urn);

    public List<ExternalDataSourceConfig> findByCoreName(String coreName);
}
