package com.saic.uicds.core.infrastructure.dao;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.ResourceProfileModel;

public interface ResourceProfileDAO extends GenericDAO<ResourceProfileModel, String> {

    public ResourceProfileModel findByLabel(String label);
    
    public ResourceProfileModel findByIdentifier(String identifier);

}