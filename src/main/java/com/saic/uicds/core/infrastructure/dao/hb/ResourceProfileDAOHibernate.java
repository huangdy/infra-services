/**
 * 
 */
package com.saic.uicds.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import com.saic.uicds.core.dao.hb.GenericHibernateDAO;
import com.saic.uicds.core.infrastructure.dao.ResourceProfileDAO;
import com.saic.uicds.core.infrastructure.model.ResourceProfileModel;

public class ResourceProfileDAOHibernate extends GenericHibernateDAO<ResourceProfileModel, String>
        implements ResourceProfileDAO {

	@Override
    public ResourceProfileModel findByIdentifier(String identifier) {
        List<ResourceProfileModel> results = findByCriteria(Restrictions.eq("identifier", identifier));
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }

	@Override
	public ResourceProfileModel findByLabel(String label) {
        List<ResourceProfileModel> results = findByCriteria(Restrictions.eq("label", label));
        if (!results.isEmpty()) {
            return results.get(0);
        }
		return null;
	}

}