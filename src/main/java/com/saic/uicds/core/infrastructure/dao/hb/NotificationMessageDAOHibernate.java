/**
 * 
 */
package com.saic.uicds.core.infrastructure.dao.hb;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.dao.hb.GenericHibernateDAO;
import com.saic.uicds.core.infrastructure.model.NotificationMessage;

public class NotificationMessageDAOHibernate extends GenericHibernateDAO<NotificationMessage, String> implements
GenericDAO<NotificationMessage, String> {

  
    static NotificationMessageDAOHibernate instance = null;

    public NotificationMessageDAOHibernate() {
        if (instance == null) {
            instance = this;
        }
    }

    public static NotificationMessageDAOHibernate getInstance() {
        return instance;
    }

   
}