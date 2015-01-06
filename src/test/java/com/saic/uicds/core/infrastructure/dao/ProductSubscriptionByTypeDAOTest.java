package com.saic.uicds.core.infrastructure.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.saic.uicds.core.infrastructure.model.NamespaceMap;
import com.saic.uicds.core.infrastructure.model.ProductSubscriptionByType;

//@Ignore
public class ProductSubscriptionByTypeDAOTest extends AbstractJpaTests {
    @Autowired
    ProductSubscriptionByTypeDAO productSubscriptionByTypeDAO;

    Set<NamespaceMap> namespacemap = new HashSet<NamespaceMap>();
    private final String incidentID = "1";

    @Test
    public void testProductSubscriptionByTypeDAOCreation() {
        assertNotNull(productSubscriptionByTypeDAO);
    }

    @Test
    public void testFindByProductType() {

        String productType = "Alert";

        ProductSubscriptionByType subscription = new ProductSubscriptionByType(productType, null,
            "AlertService", "x", 100, namespacemap);

        productSubscriptionByTypeDAO.makePersistent(subscription);

        List<ProductSubscriptionByType> subscriptionList = productSubscriptionByTypeDAO.findByProductType(productType);
        assertEquals(1, subscriptionList.size());

        productSubscriptionByTypeDAO.makeTransient(subscription);
        subscriptionList = productSubscriptionByTypeDAO.findByProductType(productType);
        assertEquals(0, subscriptionList.size());

    }

    @Test
    public void testFindByIncidentIdAndProductType() {

        String productType = "Alert";

        ProductSubscriptionByType subscription = new ProductSubscriptionByType(productType,
            incidentID, "AlertService", "x", 100, namespacemap);
        productSubscriptionByTypeDAO.makePersistent(subscription);

        subscription = new ProductSubscriptionByType(productType, null, "AlertService", "x", 100,
        		namespacemap);

        productSubscriptionByTypeDAO.makePersistent(subscription);

        List<ProductSubscriptionByType> subscriptionList = productSubscriptionByTypeDAO.findByInterestGroupIdAndProductType(
            incidentID, productType);
        assertEquals(1, subscriptionList.size());

        productSubscriptionByTypeDAO.makeTransient(subscription);
        subscriptionList = productSubscriptionByTypeDAO.findByProductType(productType);
        assertEquals(1, subscriptionList.size());

    }

    @Test
    public void testFindBySubscriptionId() {

        Integer subscriptionId = 100;
        String productType = "Alert";

        ProductSubscriptionByType subscription = new ProductSubscriptionByType(productType, null,
            "x", "AlertService", subscriptionId, namespacemap);
        productSubscriptionByTypeDAO.makePersistent(subscription);

        List<ProductSubscriptionByType> subscriptionList = productSubscriptionByTypeDAO.findBySubscriptionId(subscriptionId);
        assertEquals(1, subscriptionList.size());

        productSubscriptionByTypeDAO.makeTransient(subscription);
        subscriptionList = productSubscriptionByTypeDAO.findBySubscriptionId(subscriptionId);
        assertEquals(0, subscriptionList.size());

    }

    @Test
    public void testFindByXPath() {

        String xPath = "xyz";
        String productType = "Alert";

        ProductSubscriptionByType subscription = new ProductSubscriptionByType(productType, null,
            xPath, "AlertService", 100, namespacemap);
        productSubscriptionByTypeDAO.makePersistent(subscription);

        List<ProductSubscriptionByType> subscriptionList = productSubscriptionByTypeDAO.findByXPath(xPath);
        assertEquals(1, subscriptionList.size());

        productSubscriptionByTypeDAO.makeTransient(subscription);
        subscriptionList = productSubscriptionByTypeDAO.findByXPath(xPath);
        assertEquals(0, subscriptionList.size());

    }

    @Test
    public void testFindBySubsriberName() {

        String productType = "Alert";

        String subscriberName = "AlertService";

        ProductSubscriptionByType subscription = new ProductSubscriptionByType(productType, null,
            "x", subscriberName, 100, namespacemap);
        productSubscriptionByTypeDAO.makePersistent(subscription);

        List<ProductSubscriptionByType> subscriptionList = productSubscriptionByTypeDAO.findBySubscriberName(subscriberName);
        assertEquals(1, subscriptionList.size());

        productSubscriptionByTypeDAO.makeTransient(subscription);
        subscriptionList = productSubscriptionByTypeDAO.findBySubscriberName(subscriberName);
        assertEquals(0, subscriptionList.size());

    }

   // @Ignore
    @Test
    public void testFindByNamespaceMap() {

        String xPath = "xyz";
        String prefix = "al";
        String namespace = "http://localhost:8080/uicds/AlertService";
        NamespaceMap small = new NamespaceMap();
       
      //  small.setId(123);
        small.setPrefix(prefix);
        small.setUri(namespace);
        namespacemap.add(small);
        
        String productType = "Alert";

        List<ProductSubscriptionByType> subscriptionList = productSubscriptionByTypeDAO.findAll();
        assertEquals(0, subscriptionList.size());

        ProductSubscriptionByType subscription = new ProductSubscriptionByType(productType, null,
            xPath, "AlertService", 100, namespacemap);
        productSubscriptionByTypeDAO.makePersistent(subscription);

        subscriptionList = productSubscriptionByTypeDAO.findAll();
        assertEquals(1, subscriptionList.size());

        // to be done. code not right
 //       assertTrue(subscriptionList.get(0).getNamespacemap().contains("http://localhost:8080/uicds/AlertService"));

        productSubscriptionByTypeDAO.makeTransient(subscription);
        subscriptionList = productSubscriptionByTypeDAO.findAll();
        assertEquals(0, subscriptionList.size());
	
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml" };
    }

}
