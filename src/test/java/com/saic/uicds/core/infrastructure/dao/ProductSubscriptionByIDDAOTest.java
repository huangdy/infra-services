package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.saic.uicds.core.infrastructure.model.ProductSubscriptionByID;

public class ProductSubscriptionByIDDAOTest extends AbstractJpaTests {
    @Autowired
    ProductSubscriptionByIDDAO productSubscriptionByIDDAO;

    @Test
    public void testProductSubscriptionByIDDAOCreation() {
        assertNotNull(productSubscriptionByIDDAO);
    }

    @Test
    public void testFindByProductID() {

        String productId = "Product_01";

        // TBD: this constructor seems to ignore productType completely ???
        ProductSubscriptionByID subscription = new ProductSubscriptionByID(productId,
            "AlertService", 100);
        // Why this is needed???
        // subscription.setProductType(productType);

        productSubscriptionByIDDAO.makePersistent(subscription);

        List<ProductSubscriptionByID> subscriptionList = productSubscriptionByIDDAO.findByProductID(productId);
        assertEquals(1, subscriptionList.size());

        productSubscriptionByIDDAO.makeTransient(subscription);
        subscriptionList = productSubscriptionByIDDAO.findByProductID(productId);
        assertEquals(0, subscriptionList.size());

    }

    @Test
    public void testFindBySubscriptionId() {

        Integer subscriptionId = 100;

        ProductSubscriptionByID subscription = new ProductSubscriptionByID("Product_01",
            "AlertService", subscriptionId);
        productSubscriptionByIDDAO.makePersistent(subscription);

        List<ProductSubscriptionByID> subscriptionList = productSubscriptionByIDDAO.findBySubscriptionId(subscriptionId);
        assertEquals(1, subscriptionList.size());

        productSubscriptionByIDDAO.makeTransient(subscription);
        subscriptionList = productSubscriptionByIDDAO.findBySubscriptionId(subscriptionId);
        assertEquals(0, subscriptionList.size());

    }

    @Test
    public void testFindBySubsriberName() {

        String subscriberName = "Alert";

        ProductSubscriptionByID subscription = new ProductSubscriptionByID("Product_01",
            subscriberName, 100);
        productSubscriptionByIDDAO.makePersistent(subscription);

        List<ProductSubscriptionByID> subscriptionList = productSubscriptionByIDDAO.findBySubscriberName(subscriberName);
        assertEquals(1, subscriptionList.size());

        productSubscriptionByIDDAO.makeTransient(subscription);
        subscriptionList = productSubscriptionByIDDAO.findBySubscriberName(subscriberName);
        assertEquals(0, subscriptionList.size());

    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml" };
    }

}
