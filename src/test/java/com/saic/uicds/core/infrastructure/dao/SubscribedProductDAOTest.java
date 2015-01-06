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

public class SubscribedProductDAOTest extends AbstractJpaTests {

    @Autowired
    RegisteredServiceDAO registeredServiceDAO;

    @Autowired
    SubscribedProductDAO subscribedProductDAO;

    @Autowired
    ConfigurationService cfgService;

    @Test
    public void testConfigurationServiceCreation() {
        assertNotNull(cfgService);
    }

    @Test
    public void testRegisteredServiceDAOCreation() {
        assertNotNull(registeredServiceDAO);
    }

    @Test
    public void testSubscribedProductDAOCreation() {
        assertNotNull(subscribedProductDAO);
    }

    @Test
    public void testFindByProductType() {
        String coreName = cfgService.getHostName();
        String serviceName = "AlertService";

        RegisteredService service = new RegisteredService();
        service.setCoreName(coreName);
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

        Set<SubscribedProduct> subscribedProductList = subscribedProductDAO.findByProductType("Alert");
        assertEquals(1, subscribedProductList.size());

        subscribedProductList = subscribedProductDAO.findByProductType("Incident");
        assertEquals(1, subscribedProductList.size());

        RegisteredService service1 = new RegisteredService();
        service1.setCoreName(coreName);
        service.setURN(cfgService.getServiceNameURN(serviceName));
        service1.setServiceName(serviceName);
        service1.setServiceType(RegisteredService.SERVICE_TYPE.UICDS);

        Set<PublishedProduct> publishedProducts1 = new HashSet<PublishedProduct>();
        publishedProducts1.add(new PublishedProduct("MapViewContext"));
        service1.setPublishedProducts(publishedProducts1);

        Set<SubscribedProduct> subscribedProducts1 = new HashSet<SubscribedProduct>();
        subscribedProducts1.add(new SubscribedProduct("MapViewContext"));
        subscribedProducts1.add(new SubscribedProduct("Incident"));
        service1.setSubscribedProducts(subscribedProducts1);

        registeredServiceDAO.makePersistent(service1);

        subscribedProductList = subscribedProductDAO.findByProductType("Incident");

        assertEquals(1, subscribedProductList.size());

        registeredServiceDAO.makeTransient(service);

        subscribedProductList = subscribedProductDAO.findByProductType("Alert");
        assertEquals(0, subscribedProductList.size());

        subscribedProductList = subscribedProductDAO.findByProductType("MapViewContext");
        assertEquals(1, subscribedProductList.size());

        subscribedProductList = subscribedProductDAO.findByProductType("Incident");
        assertEquals(1, subscribedProductList.size());

    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml" };
    }
}
