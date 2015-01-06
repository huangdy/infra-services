package com.saic.uicds.core.infrastructure.dao;

import java.util.Set;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.ShareRule;

public interface ShareRuleDAO extends GenericDAO<ShareRule, Integer> {

    public Set<ShareRule> findAllShareRules();
    
}
