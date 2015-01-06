package com.saic.uicds.core.infrastructure.dao;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.saic.uicds.core.infrastructure.model.PublishedProduct;
import com.saic.uicds.core.infrastructure.model.RegisteredService;
import com.saic.uicds.core.infrastructure.model.SubscribedProduct;
import com.saic.uicds.core.infrastructure.service.ConfigurationService;

public class RegisteredServiceDAOTest extends AbstractJpaTests {

    @Autowired
    RegisteredServiceDAO registeredServiceDAO;

    @Autowired
    ConfigurationService cfgService;

    @Test
    public void testRegisteredServiceDAOCreation() {
        assertNotNull(registeredServiceDAO);
    }

    @Test
    public void testConfigurationServiceCreation() {
        assertNotNull(cfgService);
    }

    @Test
    public void testFindByCoreName() {
        String coreName = cfgService.getCoreName();

        RegisteredService service = new RegisteredService();
        service.setCoreName(coreName);
        service.setServiceName("AlertService");
        service.setServiceType(RegisteredService.SERVICE_TYPE.UICDS);
        service.setURN(cfgService.getServiceNameURN("AlertService"));

        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();
        publishedProducts.add(new PublishedProduct("Alert"));
        service.setPublishedProducts(publishedProducts);

        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        subscribedProducts.add(new SubscribedProduct("Alert"));
        subscribedProducts.add(new SubscribedProduct("Incident"));
        service.setSubscribedProducts(subscribedProducts);

        registeredServiceDAO.makePersistent(service);

        Set<RegisteredService> serviceList = registeredServiceDAO.findByCoreName(coreName);
        assertEquals(1, serviceList.size());

        registeredServiceDAO.makeTransient(service);

        serviceList = registeredServiceDAO.findByCoreName(coreName);
        assertEquals(0, serviceList.size());
    }

    @Test
    public void testFindByURN() {
        String urn = cfgService.getServiceNameURN("AlertService");

        RegisteredService service = new RegisteredService();
        service.setCoreName(cfgService.getCoreName());
        service.setURN(urn);
        service.setServiceName("AlertService");
        service.setServiceType(RegisteredService.SERVICE_TYPE.UICDS);

        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();
        publishedProducts.add(new PublishedProduct("Alert"));
        service.setPublishedProducts(publishedProducts);

        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        subscribedProducts.add(new SubscribedProduct("Alert"));
        subscribedProducts.add(new SubscribedProduct("Incident"));
        service.setSubscribedProducts(subscribedProducts);

        registeredServiceDAO.makePersistent(service);

        Set<RegisteredService> serviceList = registeredServiceDAO.findByUrn(urn);
        assertEquals(1, serviceList.size());

        registeredServiceDAO.makeTransient(service);

        serviceList = registeredServiceDAO.findByUrn(urn);
        assertEquals(0, serviceList.size());
    }

    @Test
    public void testFindByServiceName() {
        String serviceName = "AlertService";

        RegisteredService service = new RegisteredService();
        service.setCoreName(cfgService.getCoreName());
        service.setURN(cfgService.getServiceNameURN(serviceName));
        service.setServiceName(serviceName);
        service.setServiceType(RegisteredService.SERVICE_TYPE.UICDS);

        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();
        publishedProducts.add(new PublishedProduct("Alert"));
        service.setPublishedProducts(publishedProducts);

        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        subscribedProducts.add(new SubscribedProduct("Alert"));
        subscribedProducts.add(new SubscribedProduct("Incident"));
        service.setSubscribedProducts(subscribedProducts);

        registeredServiceDAO.makePersistent(service);

        Set<RegisteredService> serviceList = registeredServiceDAO.findByServiceName(serviceName);
        assertEquals(1, serviceList.size());

        registeredServiceDAO.makeTransient(service);

        serviceList = registeredServiceDAO.findByServiceName(serviceName);
        assertEquals(0, serviceList.size());
    }

    @Test
    public void testFindByServiceType() {
        RegisteredService.SERVICE_TYPE serviceType = RegisteredService.SERVICE_TYPE.EXTERNAL;

        RegisteredService service = new RegisteredService();
        service.setCoreName(cfgService.getCoreName());
        service.setURN(cfgService.getServiceNameURN("AlertService"));
        service.setServiceName("AlertService");
        service.setServiceType(serviceType);

        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();
        publishedProducts.add(new PublishedProduct("Alert"));
        service.setPublishedProducts(publishedProducts);

        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        subscribedProducts.add(new SubscribedProduct("Alert"));
        subscribedProducts.add(new SubscribedProduct("Incident"));
        service.setSubscribedProducts(subscribedProducts);

        registeredServiceDAO.makePersistent(service);

        Set<RegisteredService> serviceList = registeredServiceDAO.findByServiceType(serviceType);
        assertEquals(1, serviceList.size());

        registeredServiceDAO.makeTransient(service);

        serviceList = registeredServiceDAO.findByServiceType(serviceType);
        assertEquals(0, serviceList.size());
    }

    @Test
    public void testFindByServiceTypeAndCoreName() {
        RegisteredService.SERVICE_TYPE serviceType = RegisteredService.SERVICE_TYPE.EXTERNAL;

        RegisteredService service = new RegisteredService();
        service.setCoreName(cfgService.getCoreName());
        service.setURN(cfgService.getServiceNameURN("AlertService"));
        service.setServiceName("AlertService");
        service.setServiceType(serviceType);

        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();
        publishedProducts.add(new PublishedProduct("Alert"));
        service.setPublishedProducts(publishedProducts);

        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        subscribedProducts.add(new SubscribedProduct("Alert"));
        subscribedProducts.add(new SubscribedProduct("Incident"));
        service.setSubscribedProducts(subscribedProducts);

        registeredServiceDAO.makePersistent(service);

        Set<RegisteredService> serviceList = registeredServiceDAO.findByServiceTypeAndCoreName(
            serviceType, cfgService.getCoreName().concat("_BAD"));
        assertEquals(0, serviceList.size());

        serviceList = registeredServiceDAO.findByServiceTypeAndCoreName(
            RegisteredService.SERVICE_TYPE.UICDS, cfgService.getCoreName());
        assertEquals(0, serviceList.size());

        serviceList = registeredServiceDAO.findByServiceTypeAndCoreName(serviceType,
            cfgService.getCoreName());
        assertEquals(1, serviceList.size());

        registeredServiceDAO.makeTransient(service);

        serviceList = registeredServiceDAO.findByServiceType(serviceType);
        assertEquals(0, serviceList.size());
    }

    @Test
    public void testFindByUrnAndCoreName() {
        String alertUrn = cfgService.getServiceNameURN("AlertService");
        RegisteredService.SERVICE_TYPE serviceType = RegisteredService.SERVICE_TYPE.EXTERNAL;

        RegisteredService service = new RegisteredService();
        service.setCoreName(cfgService.getCoreName());
        service.setURN(alertUrn);
        service.setServiceName("AlertService");
        service.setServiceType(serviceType);

        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();
        publishedProducts.add(new PublishedProduct("Alert"));
        service.setPublishedProducts(publishedProducts);

        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        subscribedProducts.add(new SubscribedProduct("Alert"));
        subscribedProducts.add(new SubscribedProduct("Incident"));
        service.setSubscribedProducts(subscribedProducts);

        registeredServiceDAO.makePersistent(service);

        String wrongUrn = cfgService.getServiceNameURN("MapService");
        Set<RegisteredService> serviceList = registeredServiceDAO.findByUrnAndCoreName(wrongUrn,
            cfgService.getCoreName());
        assertEquals(0, serviceList.size());

        serviceList = registeredServiceDAO.findByUrnAndCoreName(alertUrn,
            cfgService.getCoreName().concat("_BAD"));
        assertEquals(0, serviceList.size());

        serviceList = registeredServiceDAO.findByUrnAndCoreName(alertUrn, cfgService.getCoreName());
        assertEquals(1, serviceList.size());

        registeredServiceDAO.makeTransient(service);

        serviceList = registeredServiceDAO.findByServiceType(serviceType);
        assertEquals(0, serviceList.size());
    }

    @Test
    public void testFindByServiceNameAndCoreName() {
        RegisteredService.SERVICE_TYPE serviceType = RegisteredService.SERVICE_TYPE.EXTERNAL;
        String alertServiceName = "AlertService";
        String alertUrn = cfgService.getServiceNameURN(alertServiceName);

        RegisteredService service = new RegisteredService();
        service.setCoreName(cfgService.getCoreName());
        service.setURN(alertUrn);
        service.setServiceName(alertServiceName);
        service.setServiceType(serviceType);

        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();
        publishedProducts.add(new PublishedProduct("Alert"));
        service.setPublishedProducts(publishedProducts);

        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        subscribedProducts.add(new SubscribedProduct("Alert"));
        subscribedProducts.add(new SubscribedProduct("Incident"));
        service.setSubscribedProducts(subscribedProducts);

        registeredServiceDAO.makePersistent(service);

        String wrongServiceName = "BroadcastService";
        Set<RegisteredService> serviceList = registeredServiceDAO.findByServiceNameAndCoreName(
            wrongServiceName, cfgService.getCoreName().concat("_BAD"));
        assertEquals(0, serviceList.size());

        serviceList = registeredServiceDAO.findByServiceNameAndCoreName(wrongServiceName,
            cfgService.getCoreName());
        assertEquals(0, serviceList.size());

        serviceList = registeredServiceDAO.findByServiceNameAndCoreName(alertServiceName,
            cfgService.getCoreName());
        assertEquals(1, serviceList.size());

        registeredServiceDAO.makeTransient(service);

        serviceList = registeredServiceDAO.findByServiceType(serviceType);
        assertEquals(0, serviceList.size());
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml" };
    }
}
