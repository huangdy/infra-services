package com.saic.uicds.core.infrastructure.service;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.directoryServiceData.WorkProductTypeListType;

import com.saic.precis.x2009.x06.structures.WorkProductIdentificationDocument;
import com.saic.uicds.core.infrastructure.dao.ProductSubscriptionByIDDAO;
import com.saic.uicds.core.infrastructure.dao.ProductSubscriptionByTypeDAO;
import com.saic.uicds.core.infrastructure.exceptions.EmptySubscriberNameException;
import com.saic.uicds.core.infrastructure.exceptions.InvalidProductIDException;
import com.saic.uicds.core.infrastructure.exceptions.InvalidXpathException;
import com.saic.uicds.core.infrastructure.exceptions.NullSubscriberException;
import com.saic.uicds.core.infrastructure.messages.ProductChangeNotificationMessage;
import com.saic.uicds.core.infrastructure.model.ProductSubscriptionByID;
import com.saic.uicds.core.infrastructure.model.ProductSubscriptionByType;
import com.saic.uicds.core.infrastructure.service.impl.PubSubServiceImpl;

public class PubSubServiceTest {

    private static final String SECOND_NOTIFICATION_SERVICE_NAME = "SecondNotificationService";
    private static final String SHELTER_PRODUCT_TYPE = "Shelter";
    private static final String PRODUCT_TYPE = "Incident";
    private static final String PRODUCT_ID = "ID";
    private static final int TYPE_SUB_ID = 1;
    private static final int ID_SUB_ID = 2;
    Logger log = LoggerFactory.getLogger(PubSubServiceTest.class);

    public void setPubSubService(PubSubServiceImpl pss) {

        pubsubService = pss;
    }

    class SubscriptionInfo {
        Integer prodIdSubId;
        Integer prodTypeSubId;

        public SubscriptionInfo() {

            prodIdSubId = 0;
            prodTypeSubId = 0;
        }

        public Integer getProdIdSubId() {

            return prodIdSubId;
        }

        public void setProdIdSubId(Integer id) {

            prodIdSubId = id;
        }

        public Integer getProdTypeSubId() {

            return prodTypeSubId;
        }

        public void setProdTypeSubId(Integer id) {

            prodTypeSubId = id;
        }
    }

    class NotificationTest
        implements PubSubNotificationService {

        public String newVersionProductID;
        public Integer newVersionSubscriptionId;
        private int newVersionNotificationCount;

        public Integer InvalidXpathSubscriptionId;
        private int invalidXpathNotificationCount;

        public String serviceName = NotificationService.NOTIFICATION_SERVICE_NAME;

        public NotificationTest() {

            newVersionProductID = "";
            newVersionSubscriptionId = 0;
            newVersionNotificationCount = 0;

            InvalidXpathSubscriptionId = 0;
            invalidXpathNotificationCount = 0;
        }

        @Override
        public void InvalidXpathNotification(Integer subscriptionId, String errorMessage) {

            InvalidXpathSubscriptionId = subscriptionId;
            invalidXpathNotificationCount++;
        }

        public int getInvalidXpathNotificationCount() {

            return invalidXpathNotificationCount;
        }

        public void setInvalidXpathNotificationCount(int count) {

            invalidXpathNotificationCount = count;
        }

        public void setServiceName(String name) {

            serviceName = name;
        }

        @Override
        public String getServiceName() {

            return serviceName;
        }

        @Override
        public void newWorkProductVersion(String workProductID, Integer subscriptionId) {

            log.debug("NotificationTest.newWorkProductVersion() called");
            newVersionProductID = workProductID;
            newVersionSubscriptionId = subscriptionId;
            newVersionNotificationCount++;
            log.debug("===>this=" + this + "  newVersionNotificationCount="
                + newVersionNotificationCount);
        }

        public int getNewVersionNotificationCount() {

            return newVersionNotificationCount;
        }

        public void setNewVersionNotificationCount(int count) {

            newVersionNotificationCount = count;
        }

        @Override
        public void workProductDeleted(ProductChangeNotificationMessage changedMessage,
            Integer subscriptionId) {

            log.debug("NotificationTest.workProductDeleted() called");

        }

    }

    ProductSubscriptionByIDDAO productSubscriptionByIDDAO;
    ProductSubscriptionByTypeDAO productSubscriptionByTypeDAO;
    WorkProductService workProductService;
    PubSubServiceImpl pubsubService;
    NotificationTest notificationTest = new NotificationTest();
    SubscriptionInfo subscriptionInfo = new SubscriptionInfo();

    public void setup() {

        pubsubService = new PubSubServiceImpl();

        if (workProductService == null) {
            workProductService = EasyMock.createMock(WorkProductService.class);
            assertNotNull(workProductService);
            pubsubService.setWorkProductService(workProductService);
        }

        if (productSubscriptionByIDDAO == null) {
            productSubscriptionByIDDAO = EasyMock.createMock(ProductSubscriptionByIDDAO.class);
            pubsubService.setProductSubscriptionByIDDAO(productSubscriptionByIDDAO);
        }

        if (productSubscriptionByTypeDAO == null) {
            productSubscriptionByTypeDAO = EasyMock.createMock(ProductSubscriptionByTypeDAO.class);
            pubsubService.setProductSubscriptionByTypeDAO(productSubscriptionByTypeDAO);
        }

    }

    @Test
    public void testPubSubServiceCreation() {

        if (log.isDebugEnabled()) {
            log.debug("testPubSubServiceCreation starts");
        }
        setup();
        assertNotNull(pubsubService);
        assertNotNull(workProductService);
    }

    @Test
    public void testSubscribeInvalidWorkProductID() {

        boolean caughtInvalidProductIDException = false;
        String prodId = PRODUCT_ID;

        setup();

        if (log.isDebugEnabled()) {
            log.debug("testSubscribeInvalidWorkProductID starts");
            log.debug("==>notificationTest=" + notificationTest);
        }

        org.easymock.EasyMock.expect(
            workProductService.isExisted(org.easymock.EasyMock.isA(String.class))).andReturn(false);
        org.easymock.EasyMock.replay(workProductService);

        try {
            subscriptionInfo.prodIdSubId = pubsubService.subscribeWorkProductID(prodId,
                notificationTest);
        } catch (InvalidProductIDException e) {
            if (log.isDebugEnabled()) {
                log.debug("Caught " + e.toString() + " as expected");
            }
            caughtInvalidProductIDException = true;
        } catch (NullSubscriberException e) {
            log.error("", e);
        } catch (EmptySubscriberNameException e) {
            log.error("", e);
        }

        assertTrue(caughtInvalidProductIDException);
        org.easymock.EasyMock.verify(workProductService);

    }

    @Test
    public void testSubscribeValidWorkProductID() {

        ProductChangeNotificationMessage changedMessage = new ProductChangeNotificationMessage();
        WorkProductIdentificationDocument identification = createWorkProductIdentification();
        changedMessage.setIdentification(identification);

        setup();

        if (log.isDebugEnabled()) {
            log.debug("testSubscribeValidWorkProductID starts");
            log.debug("==>notificationTest=" + notificationTest);
        }

        // TEST subscribing by work product ID

        org.easymock.EasyMock.expect(
            workProductService.isExisted(org.easymock.EasyMock.isA(String.class))).andReturn(true);

        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        ProductSubscriptionByID subscriptionByID = createSubscriptionByID(ID_SUB_ID,
            notificationTest.getServiceName(), PRODUCT_ID);
        EasyMock.expect(
            productSubscriptionByIDDAO.makePersistent(EasyMock.isA(ProductSubscriptionByID.class))).andReturn(
            subscriptionByID);

        EasyMock.replay(workProductService, productSubscriptionByIDDAO);

        try {
            subscriptionInfo.prodIdSubId = pubsubService.subscribeWorkProductID(
                changedMessage.getProductID(), notificationTest);
            subscriptionByID.setSubscriptionId(subscriptionInfo.prodIdSubId);
        } catch (InvalidProductIDException e) {
            log.error("Unexpectedly Caught Exception", e);
        } catch (NullSubscriberException e) {
            log.error("", e);
        } catch (EmptySubscriberNameException e) {
            log.error("", e);
        }

        // Make sure work product service verified the work product existed and that the
        // subscription was persisted
        org.easymock.EasyMock.verify(workProductService, productSubscriptionByIDDAO);

        // Make sure that the subscriber receives a notification for the correct work product
        assertEquals(1, notificationTest.newVersionNotificationCount);
        assertEquals(changedMessage.getProductID(), notificationTest.newVersionProductID);
        assertEquals(subscriptionInfo.prodIdSubId, notificationTest.newVersionSubscriptionId);

        // Set the subscription info for further use
        subscriptionByID.setSubscriptionId(subscriptionInfo.prodIdSubId);

        // TEST that a notification is sent when the work product with the subscribed to ID is
        // updated
        EasyMock.reset(workProductService, productSubscriptionByIDDAO);

        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        List<ProductSubscriptionByID> subscriptionsByID = createFindByIDResponse(subscriptionByID);
        EasyMock.expect(productSubscriptionByIDDAO.findByProductID(PRODUCT_ID)).andReturn(
            subscriptionsByID);

        List<ProductSubscriptionByType> subscriptionsByType = new ArrayList<ProductSubscriptionByType>();
        EasyMock.expect(productSubscriptionByTypeDAO.findByProductType(SHELTER_PRODUCT_TYPE)).andReturn(
            subscriptionsByType);

        changedMessage.getIdentification().getWorkProductIdentification().addNewType().setStringValue(
            SHELTER_PRODUCT_TYPE);

        EasyMock.replay(workProductService, productSubscriptionByIDDAO,
            productSubscriptionByTypeDAO);
        pubsubService.publishWorkProduct(changedMessage);

        // Make sure we got a notification for the changed work product
        assertEquals(1, notificationTest.newVersionNotificationCount);
        assertEquals(changedMessage.getProductID(), notificationTest.newVersionProductID);
        assertEquals(subscriptionInfo.prodIdSubId, notificationTest.newVersionSubscriptionId);

        // TEST unsubscribing
        EasyMock.reset(workProductService, productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        EasyMock.expect(
            productSubscriptionByIDDAO.findBySubscriptionId(EasyMock.isA(Integer.class))).andReturn(
            subscriptionsByID);

        EasyMock.expect(
            productSubscriptionByTypeDAO.findBySubscriptionId(EasyMock.isA(Integer.class))).andReturn(
            subscriptionsByType);

        productSubscriptionByIDDAO.makeTransient(EasyMock.isA(ProductSubscriptionByID.class));

        EasyMock.replay(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        pubsubService.unsubscribeBySubscriptionID(subscriptionInfo.prodIdSubId);

        EasyMock.verify(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        // TEST Make sure a new publication does not make a notification

        EasyMock.reset(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        // clear before before checking next test
        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        subscriptionsByID.clear();
        EasyMock.expect(productSubscriptionByIDDAO.findByProductID(PRODUCT_ID)).andReturn(
            subscriptionsByID);

        EasyMock.expect(productSubscriptionByTypeDAO.findByProductType(SHELTER_PRODUCT_TYPE)).andReturn(
            subscriptionsByType);

        EasyMock.replay(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        pubsubService.publishWorkProduct(changedMessage);

        // should not receive any notification
        assertEquals(0, notificationTest.newVersionNotificationCount);
    }

    private List<ProductSubscriptionByID> createFindByIDResponse(
        ProductSubscriptionByID subscriptionByID) {

        List<ProductSubscriptionByID> subscriptionsByID = new ArrayList<ProductSubscriptionByID>();
        subscriptionsByID.add(subscriptionByID);
        return subscriptionsByID;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubscribeValidWorkProductType() throws InvalidXpathException {

        WorkProductIdentificationDocument identification = createWorkProductIdentification();
        String productType = PRODUCT_TYPE;
        identification.getWorkProductIdentification().addNewType().setStringValue(productType);
        ProductChangeNotificationMessage changedMessage = new ProductChangeNotificationMessage(
            identification, null, null);
        String xPath = "";

        setup();

        // TEST subscribing by type.
        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        List<String> productIdList = new ArrayList<String>();
        productIdList.add(PRODUCT_ID);

        HashMap<String, String> namespaceMap = new HashMap<String, String>();

        ProductSubscriptionByType subscriptionByType = subscribeByWorkProductType(productType,
            xPath, productIdList, namespaceMap, notificationTest);

        // See comment on subscribeInterestGroupIdAndWorkProductType method
        // assertEquals(1, notificationTest.newVersionNotificationCount);
        // assertEquals(PRODUCT_ID, notificationTest.newVersionProductID);
        // assertEquals(subscriptionInfo.prodTypeSubId, notificationTest.newVersionSubscriptionId);

        subscriptionByType.setSubscriptionId(subscriptionInfo.prodTypeSubId);

        // TEST getting a notification
        org.easymock.EasyMock.reset(workProductService, productSubscriptionByTypeDAO,
            productSubscriptionByIDDAO);

        // clear before before checking next test
        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        List<ProductSubscriptionByID> subscriptionsByID = new ArrayList<ProductSubscriptionByID>();
        EasyMock.expect(productSubscriptionByIDDAO.findByProductID(EasyMock.isA(String.class))).andReturn(
            subscriptionsByID);

        List<ProductSubscriptionByType> subscriptionsByType = new ArrayList<ProductSubscriptionByType>();
        subscriptionsByType.add(subscriptionByType);
        EasyMock.expect(productSubscriptionByTypeDAO.findByProductType(EasyMock.isA(String.class))).andReturn(
            subscriptionsByType);

        org.easymock.EasyMock.replay(workProductService, productSubscriptionByTypeDAO,
            productSubscriptionByIDDAO);

        pubsubService.publishWorkProduct(changedMessage);

        log.debug("checking notificationTest=" + notificationTest
            + "  newVersionNotificationCount:" + notificationTest.newVersionNotificationCount);
        assertEquals(1, notificationTest.newVersionNotificationCount);
        assertEquals(PRODUCT_ID, notificationTest.newVersionProductID);
        assertEquals(subscriptionInfo.prodTypeSubId, notificationTest.newVersionSubscriptionId);

        // TEST unsubscribing

        org.easymock.EasyMock.reset(workProductService, productSubscriptionByTypeDAO,
            productSubscriptionByIDDAO);

        EasyMock.expect(
            productSubscriptionByIDDAO.findBySubscriptionId(EasyMock.isA(Integer.class))).andReturn(
            subscriptionsByID);

        EasyMock.expect(
            productSubscriptionByTypeDAO.findBySubscriptionId(EasyMock.isA(Integer.class))).andReturn(
            subscriptionsByType);

        productSubscriptionByTypeDAO.makeTransient(EasyMock.isA(ProductSubscriptionByType.class));

        EasyMock.replay(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        pubsubService.unsubscribeBySubscriptionID(subscriptionInfo.prodTypeSubId);

        // TEST that notifications are no longer received
        EasyMock.reset(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        // clear before before checking next test
        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        EasyMock.expect(productSubscriptionByIDDAO.findByProductID(EasyMock.isA(String.class))).andReturn(
            subscriptionsByID);

        subscriptionsByType.clear();
        EasyMock.expect(productSubscriptionByTypeDAO.findByProductType(EasyMock.isA(String.class))).andReturn(
            subscriptionsByType);

        EasyMock.replay(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        pubsubService.publishWorkProduct(changedMessage);

        // should not receive any notification
        assertEquals(0, notificationTest.newVersionNotificationCount);
    }

    @Test
    public void testTwoSubscribersSameWorkProductType() throws InvalidXpathException {

        WorkProductIdentificationDocument identification = createWorkProductIdentification();
        String productType = PRODUCT_TYPE;
        identification.getWorkProductIdentification().addNewType().setStringValue(productType);
        ProductChangeNotificationMessage changedMessage = new ProductChangeNotificationMessage(
            identification, null, null);
        String xPath = "";

        setup();

        // TEST subscribing by type.
        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        List<String> productIdList = new ArrayList<String>();
        productIdList.add(PRODUCT_ID);

        HashMap<String, String> namespaceMap = new HashMap<String, String>();

        ProductSubscriptionByType subscriptionByType = subscribeByWorkProductType(productType,
            xPath, productIdList, namespaceMap, notificationTest);

        // See comment on subscribeInterestGroupIdAndWorkProductType method
        // assertEquals(1, notificationTest.newVersionNotificationCount);
        // assertEquals(PRODUCT_ID, notificationTest.newVersionProductID);
        // assertEquals(subscriptionInfo.prodTypeSubId, notificationTest.newVersionSubscriptionId);

        subscriptionByType.setSubscriptionId(subscriptionInfo.prodTypeSubId);
        Integer firstSubscriptionID = subscriptionInfo.prodTypeSubId;

        org.easymock.EasyMock.reset(workProductService, productSubscriptionByTypeDAO,
            productSubscriptionByIDDAO);

        NotificationTest secondNotificationTest = createSecondsNotificationTest();

        ProductSubscriptionByType secondSubscriptionByType = subscribeByWorkProductType(
            productType, xPath, productIdList, namespaceMap, secondNotificationTest);

        // See comment on subscribeInterestGroupIdAndWorkProductType method
        // assertEquals(1, secondNotificationTest.newVersionNotificationCount);
        // assertEquals(PRODUCT_ID, secondNotificationTest.newVersionProductID);
        // assertEquals(subscriptionInfo.prodTypeSubId,
        // secondNotificationTest.newVersionSubscriptionId);

        secondSubscriptionByType.setSubscriptionId(subscriptionInfo.prodTypeSubId);
        Integer secondSubscriptionID = subscriptionInfo.prodTypeSubId;

        // TEST getting a notification
        org.easymock.EasyMock.reset(workProductService, productSubscriptionByTypeDAO,
            productSubscriptionByIDDAO);

        // clear before before checking next test
        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        secondNotificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, secondNotificationTest.newVersionNotificationCount);

        List<ProductSubscriptionByID> subscriptionsByID = new ArrayList<ProductSubscriptionByID>();
        EasyMock.expect(productSubscriptionByIDDAO.findByProductID(EasyMock.isA(String.class))).andReturn(
            subscriptionsByID);

        // The subscription DAO should have two items (one for each subscriber)
        List<ProductSubscriptionByType> subscriptionsByType = new ArrayList<ProductSubscriptionByType>();
        subscriptionsByType.add(subscriptionByType);
        subscriptionsByType.add(secondSubscriptionByType);
        EasyMock.expect(productSubscriptionByTypeDAO.findByProductType(EasyMock.isA(String.class))).andReturn(
            subscriptionsByType);

        org.easymock.EasyMock.replay(workProductService, productSubscriptionByTypeDAO,
            productSubscriptionByIDDAO);

        pubsubService.publishWorkProduct(changedMessage);

        log.debug("checking notificationTest=" + notificationTest
            + "  newVersionNotificationCount:" + notificationTest.newVersionNotificationCount);
        assertEquals(1, notificationTest.newVersionNotificationCount);
        assertEquals(PRODUCT_ID, notificationTest.newVersionProductID);
        assertEquals(firstSubscriptionID, notificationTest.newVersionSubscriptionId);

        log.debug("checking notificationTest=" + secondNotificationTest
            + "  newVersionNotificationCount:" + secondNotificationTest.newVersionNotificationCount);
        assertEquals(1, secondNotificationTest.newVersionNotificationCount);
        assertEquals(PRODUCT_ID, secondNotificationTest.newVersionProductID);
        assertEquals(secondSubscriptionID, secondNotificationTest.newVersionSubscriptionId);
    }

    private NotificationTest createSecondsNotificationTest() {

        NotificationTest secondNotificationTest = new NotificationTest();
        secondNotificationTest.setServiceName(SECOND_NOTIFICATION_SERVICE_NAME);
        return secondNotificationTest;
    }

    private ProductSubscriptionByType subscribeByWorkProductType(String productType, String xPath,
        List<String> productIdList, HashMap<String, String> namespaceMap,
        NotificationTest testNotificationService) throws InvalidXpathException {

        // org.easymock.EasyMock.expect(
        // workProductService.getProductIDListByTypeAndXQuery(
        // org.easymock.EasyMock.isA(String.class), org.easymock.EasyMock.isA(String.class),
        // org.easymock.EasyMock.isA(namespaceMap.getClass()))).andReturn(productIdList).andThrow(
        // new InvalidXpathException("InvalidXPath: "));

        ProductSubscriptionByType subscriptionByType = createSubscriptionByPRODUCT_TYPE(
            TYPE_SUB_ID, testNotificationService.getServiceName(), productType);

        EasyMock.expect(
            productSubscriptionByTypeDAO.makePersistent(EasyMock.isA(ProductSubscriptionByType.class))).andReturn(
            subscriptionByType);

        org.easymock.EasyMock.replay(workProductService, productSubscriptionByTypeDAO);

        try {

            subscriptionInfo.prodTypeSubId = pubsubService.subscribeWorkProductType(productType,
                xPath, namespaceMap, testNotificationService);
        } catch (NullSubscriberException e) {
            log.error("", e);
        } catch (EmptySubscriberNameException e) {
            log.error("", e);
        }
        return subscriptionByType;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubscribeIncidentIDAndValidWorkProductType() throws InvalidXpathException {

        String productType = PRODUCT_TYPE;
        String prodId = PRODUCT_ID;
        String incidentID = "1";
        String xPath = "";

        setup();

        WorkProductTypeListType productTypeList = WorkProductTypeListType.Factory.newInstance();

        if (log.isDebugEnabled()) {
            log.debug("testSubscribeValidWorkProductType starts");
            log.debug("==>notificationTest=" + notificationTest);
        }

        productTypeList.addProductType(productType);

        List<String> prodIdList = new ArrayList<String>();
        prodIdList.add(prodId);

        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        HashMap<String, String> namespaceMap = new HashMap<String, String>();

        org.easymock.EasyMock.expect(
            workProductService.getProductIDListByTypeAndXQuery(
                org.easymock.EasyMock.isA(String.class), org.easymock.EasyMock.isA(String.class),
                org.easymock.EasyMock.isA(namespaceMap.getClass()))).andReturn(prodIdList).andThrow(
            new InvalidXpathException("Invalid XPath: "));

        ProductSubscriptionByType subscriptionByType = createSubscriptionByPRODUCT_TYPE(
            TYPE_SUB_ID, notificationTest.getServiceName(), PRODUCT_TYPE);
        EasyMock.expect(
            productSubscriptionByTypeDAO.makePersistent(EasyMock.isA(ProductSubscriptionByType.class))).andReturn(
            subscriptionByType);

        org.easymock.EasyMock.replay(workProductService, productSubscriptionByTypeDAO);

        try {
            subscriptionInfo.prodTypeSubId = pubsubService.subscribeInterestGroupIdAndWorkProductType(
                productType, incidentID, xPath, namespaceMap, notificationTest);
            subscriptionByType.setSubscriptionId(subscriptionInfo.prodTypeSubId);
        } catch (NullSubscriberException e) {
            log.error("", e);
        } catch (EmptySubscriberNameException e) {
            log.error("", e);
        }

        // See comment on subscribeInterestGroupIdAndWorkProductType method
        // assertEquals(1, notificationTest.newVersionNotificationCount);
        // assertEquals(prodId, notificationTest.newVersionProductID);
        // assertEquals(subscriptionInfo.prodTypeSubId, notificationTest.newVersionSubscriptionId);

        // TEST notification

        // clear before before checking next test
        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        WorkProductIdentificationDocument identification = WorkProductIdentificationDocument.Factory.newInstance();
        identification.addNewWorkProductIdentification().addNewIdentifier().setStringValue(prodId);
        identification.getWorkProductIdentification().addNewType().setStringValue(productType);
        ProductChangeNotificationMessage changedMessage = new ProductChangeNotificationMessage(
            identification, null, null);

        // set the InterestGroupID to "1"
        changedMessage.setInterestGroupID("1");

        org.easymock.EasyMock.reset(workProductService, productSubscriptionByTypeDAO,
            productSubscriptionByIDDAO);

        // clear before before checking next test
        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        List<ProductSubscriptionByID> subscriptionsByID = new ArrayList<ProductSubscriptionByID>();
        EasyMock.expect(productSubscriptionByIDDAO.findByProductID(EasyMock.isA(String.class))).andReturn(
            subscriptionsByID);

        List<ProductSubscriptionByType> subscriptionsByType = createFindByProductTypeResponse(subscriptionByType);
        EasyMock.expect(productSubscriptionByTypeDAO.findByProductType(EasyMock.isA(String.class))).andReturn(
            subscriptionsByType);

        org.easymock.EasyMock.replay(workProductService, productSubscriptionByTypeDAO,
            productSubscriptionByIDDAO);

        pubsubService.publishWorkProduct(changedMessage);

        log.debug("checking notificationTest=" + notificationTest
            + "  newVersionNotificationCount:" + notificationTest.newVersionNotificationCount);
        assertEquals(1, notificationTest.newVersionNotificationCount);
        assertEquals(prodId, notificationTest.newVersionProductID);
        assertEquals(subscriptionInfo.prodTypeSubId, notificationTest.newVersionSubscriptionId);

        // TEST unsubscribe

        org.easymock.EasyMock.reset(workProductService, productSubscriptionByTypeDAO,
            productSubscriptionByIDDAO);

        EasyMock.expect(
            productSubscriptionByIDDAO.findBySubscriptionId(EasyMock.isA(Integer.class))).andReturn(
            subscriptionsByID);

        EasyMock.expect(
            productSubscriptionByTypeDAO.findBySubscriptionId(EasyMock.isA(Integer.class))).andReturn(
            subscriptionsByType);

        productSubscriptionByTypeDAO.makeTransient(EasyMock.isA(ProductSubscriptionByType.class));

        EasyMock.replay(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        pubsubService.unsubscribeBySubscriptionID(subscriptionInfo.prodTypeSubId);

        // TEST no notifications when no subscriptions
        EasyMock.reset(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        // clear before before checking next test
        notificationTest.setNewVersionNotificationCount(0);
        assertEquals(0, notificationTest.newVersionNotificationCount);

        EasyMock.expect(productSubscriptionByIDDAO.findByProductID(EasyMock.isA(String.class))).andReturn(
            subscriptionsByID);

        subscriptionsByType.clear();
        EasyMock.expect(productSubscriptionByTypeDAO.findByProductType(EasyMock.isA(String.class))).andReturn(
            subscriptionsByType);

        EasyMock.replay(productSubscriptionByIDDAO, productSubscriptionByTypeDAO);

        pubsubService.publishWorkProduct(changedMessage);

        // should not receive any notification
        assertEquals(0, notificationTest.newVersionNotificationCount);
    }

    @Test
    public void testGetSubscriptionListType() {

        setup();

        ProductSubscriptionByType subscriptionByType = createSubscriptionByPRODUCT_TYPE(
            TYPE_SUB_ID, notificationTest.getServiceName(), PRODUCT_TYPE);
        List<ProductSubscriptionByType> subscriptionsByType = createFindByProductTypeResponse(subscriptionByType);
        EasyMock.expect(
            productSubscriptionByTypeDAO.findBySubscriberName(EasyMock.isA(String.class))).andReturn(
            subscriptionsByType);

        List<ProductSubscriptionByID> subscriptionsByID = new ArrayList<ProductSubscriptionByID>();
        EasyMock.expect(productSubscriptionByIDDAO.findBySubscriberName(EasyMock.isA(String.class))).andReturn(
            subscriptionsByID);

        EasyMock.replay(productSubscriptionByTypeDAO, productSubscriptionByIDDAO);

        List<Integer> subscriptions = pubsubService.getSubscriptionsByServiceName(notificationTest.getServiceName());
        assertNotNull("response is null", subscriptions);
        assertEquals("no subscription", 1, subscriptions.size());
        assertTrue("no TYPE ID", subscriptions.contains(TYPE_SUB_ID));
    }

    @Test
    public void testGetSubscriptionListID() {

        setup();

        List<ProductSubscriptionByType> subscriptionsByType = new ArrayList<ProductSubscriptionByType>();
        EasyMock.expect(
            productSubscriptionByTypeDAO.findBySubscriberName(EasyMock.isA(String.class))).andReturn(
            subscriptionsByType);

        ProductSubscriptionByID subscriptionByID = createSubscriptionByID(ID_SUB_ID,
            notificationTest.getServiceName(), PRODUCT_ID);
        List<ProductSubscriptionByID> subscriptionsByID = createFindByIDResponse(subscriptionByID);
        EasyMock.expect(productSubscriptionByIDDAO.findBySubscriberName(EasyMock.isA(String.class))).andReturn(
            subscriptionsByID);

        EasyMock.replay(productSubscriptionByTypeDAO, productSubscriptionByIDDAO);

        List<Integer> subscriptions = pubsubService.getSubscriptionsByServiceName(notificationTest.getServiceName());
        assertNotNull("response is null", subscriptions);
        assertEquals("no subscription", 1, subscriptions.size());
        assertTrue("no ID ID", subscriptions.contains(ID_SUB_ID));
    }

    @Test
    public void testGetSubscriptionListTypeAndID() {

        setup();

        ProductSubscriptionByType subscriptionByType = createSubscriptionByPRODUCT_TYPE(
            TYPE_SUB_ID, notificationTest.getServiceName(), PRODUCT_TYPE);
        List<ProductSubscriptionByType> subscriptionsByType = createFindByProductTypeResponse(subscriptionByType);
        EasyMock.expect(
            productSubscriptionByTypeDAO.findBySubscriberName(EasyMock.isA(String.class))).andReturn(
            subscriptionsByType);

        NotificationTest secondNotificationTest = createSecondsNotificationTest();

        ProductSubscriptionByID subscriptionByID = createSubscriptionByID(ID_SUB_ID,
            notificationTest.getServiceName(), PRODUCT_ID);
        List<ProductSubscriptionByID> subscriptionsByID = createFindByIDResponse(subscriptionByID);
        EasyMock.expect(productSubscriptionByIDDAO.findBySubscriberName(EasyMock.isA(String.class))).andReturn(
            subscriptionsByID);

        EasyMock.replay(productSubscriptionByTypeDAO, productSubscriptionByIDDAO);

        List<Integer> subscriptions = pubsubService.getSubscriptionsByServiceName(notificationTest.getServiceName());
        assertNotNull("response is null", subscriptions);
        assertEquals("no subscription", 2, subscriptions.size());
        assertTrue("no TYPE ID", subscriptions.contains(TYPE_SUB_ID));
        assertTrue("no ID ID", subscriptions.contains(ID_SUB_ID));
    }

    @Test
    public void testGetSubscriptionListTypeAndIDMultipleServices() {

        setup();

        ProductSubscriptionByType subscriptionByType = createSubscriptionByPRODUCT_TYPE(
            TYPE_SUB_ID, notificationTest.getServiceName(), PRODUCT_TYPE);

        List<ProductSubscriptionByType> subscriptionsByType = createFindByProductTypeResponse(subscriptionByType);

        EasyMock.expect(
            productSubscriptionByTypeDAO.findBySubscriberName(EasyMock.isA(String.class))).andReturn(
            subscriptionsByType);

        ProductSubscriptionByID subscriptionByID = createSubscriptionByID(ID_SUB_ID,
            notificationTest.getServiceName(), PRODUCT_ID);
        List<ProductSubscriptionByID> subscriptionsByID = createFindByIDResponse(subscriptionByID);
        EasyMock.expect(productSubscriptionByIDDAO.findBySubscriberName(EasyMock.isA(String.class))).andReturn(
            subscriptionsByID);

        EasyMock.replay(productSubscriptionByTypeDAO, productSubscriptionByIDDAO);

        List<Integer> subscriptions = pubsubService.getSubscriptionsByServiceName(notificationTest.getServiceName());
        assertNotNull("response is null", subscriptions);
        assertEquals("no subscription", 2, subscriptions.size());
        assertTrue("no TYPE ID", subscriptions.contains(TYPE_SUB_ID));
        assertTrue("no ID ID", subscriptions.contains(ID_SUB_ID));
    }

    private List<ProductSubscriptionByType> createFindByProductTypeResponse(
        ProductSubscriptionByType subscriptionByType) {

        return addSubscriptionToProductTypeResponse(null, subscriptionByType);
    }

    private List<ProductSubscriptionByType> addSubscriptionToProductTypeResponse(
        List<ProductSubscriptionByType> subscriptionsByType,
        ProductSubscriptionByType subscriptionByType) {

        if (subscriptionsByType == null) {
            subscriptionsByType = new ArrayList<ProductSubscriptionByType>();
        }
        subscriptionsByType.add(subscriptionByType);
        return subscriptionsByType;
    }

    private ProductSubscriptionByType createSubscriptionByPRODUCT_TYPE(Integer subscriptionId,
        String serviceName, String productType) {

        ProductSubscriptionByType subscriptionByType = new ProductSubscriptionByType();
        subscriptionByType.setSubscriptionId(subscriptionId);
        subscriptionByType.setProductType(productType);
        subscriptionByType.setSubscriberName(serviceName);
        return subscriptionByType;
    }

    private ProductSubscriptionByID createSubscriptionByID(Integer subscriptionId,
        String serviceName, String productID) {

        ProductSubscriptionByID subscriptionByID = new ProductSubscriptionByID();
        subscriptionByID.setSubscriptionId(subscriptionId);
        subscriptionByID.setProductId(productID);
        subscriptionByID.setSubscriber(serviceName);
        return subscriptionByID;
    }

    private WorkProductIdentificationDocument createWorkProductIdentification() {

        WorkProductIdentificationDocument identification = WorkProductIdentificationDocument.Factory.newInstance();
        identification.addNewWorkProductIdentification().addNewIdentifier().setStringValue(
            PRODUCT_ID);
        return identification;
    }

}
