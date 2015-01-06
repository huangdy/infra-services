package com.saic.uicds.core.infrastructure.dao.hb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.saic.uicds.core.dao.hb.GenericHibernateDAO;
import com.saic.uicds.core.infrastructure.dao.ShareRuleDAO;
import com.saic.uicds.core.infrastructure.model.ShareRule;

public class ShareRuleHibernateDAO extends GenericHibernateDAO<ShareRule, Integer>
        implements ShareRuleDAO {

    public Set<ShareRule> findAllShareRules() {

        List<ShareRule> shareRuleList = findAll();
        Set<ShareRule> shareRules = new HashSet<ShareRule>(shareRuleList);
        /*
        Set<ShareRule> shareRules = new HashSet<ShareRule>();
        for (ShareRule shareRule : shareRuleList) {
            shareRules.add(shareRule);
        }
        */
        
        return shareRules;
    }

}
