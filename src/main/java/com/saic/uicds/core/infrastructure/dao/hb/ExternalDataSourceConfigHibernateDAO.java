package com.saic.uicds.core.infrastructure.dao.hb;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.saic.uicds.core.dao.hb.GenericHibernateDAO;
import com.saic.uicds.core.infrastructure.dao.ExternalDataSourceConfigDAO;
import com.saic.uicds.core.infrastructure.model.ExternalDataSourceConfig;

public class ExternalDataSourceConfigHibernateDAO extends
		GenericHibernateDAO<ExternalDataSourceConfig, Integer> implements
		ExternalDataSourceConfigDAO {

	public List<ExternalDataSourceConfig> findByUrn(String urn) {
		Criterion criterion = Restrictions.eq("urn", urn);
		List<ExternalDataSourceConfig> externalDataSources = findByCriteria(criterion);
		return externalDataSources;
	}

	public List<ExternalDataSourceConfig> findByCoreName(String coreName) {
		Criterion criterion = Restrictions.eq("coreName", coreName);
		List<ExternalDataSourceConfig> externalDataSources = findByCriteria(criterion);
		return externalDataSources;
	}

}
