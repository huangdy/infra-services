package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.Notification;

public interface NotificationDAO extends GenericDAO<Notification, String> {

    public Notification findByEntityId(String entityID);
    public List<Notification> findBySubscriptionId(Integer SubID);
    
  //FLi modified on 11/29/2011
    public int findMsgCountByEntityId(String entityID);

}