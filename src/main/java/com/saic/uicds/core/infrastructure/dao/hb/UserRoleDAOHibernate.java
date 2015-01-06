/**
 * 
 */
package com.saic.uicds.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.saic.uicds.core.dao.hb.GenericHibernateDAO;
import com.saic.uicds.core.infrastructure.dao.UserRoleDAO;
import com.saic.uicds.core.infrastructure.model.UserRole;

public class UserRoleDAOHibernate extends GenericHibernateDAO<UserRole, String> implements
        UserRoleDAO {

    @Override
    public List<UserRole> findUsersByRole(String roleRefID) {
        return findByCriteria(Restrictions.eq("roleRefId", roleRefID));
    }

}