package com.saic.uicds.core.infrastructure.dao;

import java.util.Set;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.RegisteredService;

public interface RegisteredServiceDAO extends GenericDAO<RegisteredService, Integer> {

    public Set<RegisteredService> findByUrn(String urn);

    public Set<RegisteredService> findByServiceName(String serviceName);

    public Set<RegisteredService> findByServiceType(RegisteredService.SERVICE_TYPE serviceType);

    public Set<RegisteredService> findByCoreName(String coreName);

    public Set<RegisteredService> findByServiceTypeAndCoreName(
            RegisteredService.SERVICE_TYPE serviceType, String coreName);

    public Set<RegisteredService> findByUrnAndCoreName(String urn, String coreName);

    public Set<RegisteredService> findByServiceNameAndCoreName(String serviceName, String coreName);

    public Set<RegisteredService> findAllRegisteredServices();

}
