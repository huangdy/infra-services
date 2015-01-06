package com.saic.uicds.core.infrastructure.dao;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.ResourceInstanceModel;

public interface ResourceInstanceDAO extends GenericDAO<ResourceInstanceModel, String> {

    public ResourceInstanceModel findByLabel(String label);
    
    public ResourceInstanceModel findByIdentifier(String identifier);

}