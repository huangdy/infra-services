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

public class PublishedProductDAOTest extends AbstractJpaTests {
    @Autowired
    RegisteredServiceDAO registeredServiceDAO;

    @Autowired
    PublishedProductDAO publishedProductDAO;

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
    public void testPublishedProductDAOCreation() {
        assertNotNull(publishedProductDAO);
    }

    @Test
    public void testFindByProductType() {
        String coreName = cfgService.getCoreName();

        String serviceName = "Alert";

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

        Set<PublishedProduct> publishedProductList = publishedProductDAO.findByProductType("Alert");
        assertEquals(1, publishedProductList.size());

        String svcName = null;

        for (PublishedProduct product : publishedProductList) {
            svcName = product.getPublisher().getServiceName();
            break;
        }

        assertTrue(svcName.equals(serviceName));

        registeredServiceDAO.makeTransient(service);

        publishedProductList = publishedProductDAO.findByProductType("Alert");
        assertEquals(0, publishedProductList.size());
    }

    @Test
    public void testFindAll() {
        String coreName = cfgService.getCoreName();
        String serviceName = "Alert";

        RegisteredService service = new RegisteredService();
        service.setCoreName(coreName);
        service.setURN(cfgService.getServiceNameURN(serviceName));
        service.setServiceName(serviceName);
        service.setServiceType(RegisteredService.SERVICE_TYPE.UICDS);

        Set<PublishedProduct> publishedProducts = new HashSet<PublishedProduct>();
        publishedProducts.add(new PublishedProduct("Alert"));
        publishedProducts.add(new PublishedProduct("MapViewContext"));
        publishedProducts.add(new PublishedProduct("LayerViewContext"));
        service.setPublishedProducts(publishedProducts);

        Set<SubscribedProduct> subscribedProducts = new HashSet<SubscribedProduct>();
        subscribedProducts.add(new SubscribedProduct("Alert"));
        subscribedProducts.add(new SubscribedProduct("Incident"));
        service.setSubscribedProducts(subscribedProducts);

        registeredServiceDAO.makePersistent(service);

        Set<PublishedProduct> publishedProductList = publishedProductDAO.findAllPublishedProducts();
        assertEquals(3, publishedProductList.size());

        registeredServiceDAO.makeTransient(service);

        publishedProductList = publishedProductDAO.findAllPublishedProducts();
        assertEquals(0, publishedProductList.size());
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml" };
    }
}
