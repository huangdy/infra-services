package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.UserInterestGroup;

public interface UserInterestGroupDAO extends GenericDAO<UserInterestGroup, String> {

    public void addUser(String user, String interestGroup);

    // public UserInterestGroup findByUser(String user);

    public List<String> getInterestGroupList(String user);

    public boolean isEligible(String user, String interestGroupID);

    public void removeUser(String user, String interestGoup);
    
    public int removeInterestGroup(String igID);
}
