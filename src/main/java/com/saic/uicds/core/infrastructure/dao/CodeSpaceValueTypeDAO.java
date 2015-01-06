package com.saic.uicds.core.infrastructure.dao;

import java.util.Set;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.CodeSpaceValueType;

public interface CodeSpaceValueTypeDAO extends GenericDAO<CodeSpaceValueType, Integer> {

    public Set<CodeSpaceValueType> findAllCodeSpaceValueTypes();
    
}
