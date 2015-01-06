/**
 * 
 */
package com.saic.uicds.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.saic.uicds.core.dao.hb.GenericHibernateDAO;
import com.saic.uicds.core.infrastructure.dao.ResourceInstanceDAO;
import com.saic.uicds.core.infrastructure.model.ResourceInstanceModel;

public class ResourceInstanceDAOHibernate extends
        GenericHibernateDAO<ResourceInstanceModel, String> implements ResourceInstanceDAO {

	@Override
    public ResourceInstanceModel findByIdentifier(String identifier) {
        List<ResourceInstanceModel> results = findByCriteria(Restrictions.eq("identifier", identifier));
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

    @Override
    public ResourceInstanceModel findByLabel(String label) {
        List<ResourceInstanceModel> results = findByCriteria(Restrictions.eq("label", label));
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

    
    public ResourceInstanceModel findByResourceID(String resourceID) {
        List<ResourceInstanceModel> results = findByCriteria(Restrictions.eq("resourceID", resourceID));
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }
}