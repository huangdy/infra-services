/**
 * 
 */
package com.leidos.xchangecore.core.infrastructure.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.easymock.Capture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.uicds.resourceProfileService.Interest;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import com.leidos.xchangecore.core.infrastructure.dao.NotificationDAO;
import com.leidos.xchangecore.core.infrastructure.exceptions.EmptySubscriberNameException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductIDException;
import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidProductTypeException;
import com.leidos.xchangecore.core.infrastructure.exceptions.NullSubscriberException;
import com.leidos.xchangecore.core.infrastructure.model.Notification;
import com.leidos.xchangecore.core.infrastructure.model.NotificationSubscription;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.CommunicationsService;
import com.leidos.xchangecore.core.infrastructure.service.ConfigurationService;
import com.leidos.xchangecore.core.infrastructure.service.NotificationService;
import com.leidos.xchangecore.core.infrastructure.service.PubSubNotificationService;
import com.leidos.xchangecore.core.infrastructure.service.PubSubService;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;
import com.leidos.xchangecore.core.infrastructure.service.impl.NotificationServiceImpl;
import com.leidos.xchangecore.core.infrastructure.util.NotificationUtils;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;

/**
 * NotificationServiceTest
 * 
 * @author roger, initial revision
 * 
 */
public class NotificationServiceTest
    extends Assert {

    private static final String INCIDENT_WP_TYPE = "Incident";

    NotificationServiceImpl notificationService = new NotificationServiceImpl();

    PubSubService pubSubService;
    ConfigurationService configurationService;

    NotificationDAO notificationDAO;

    CommunicationsService communicationsService;
    WorkProductService workProductService;

    static final String entityID = "joe@core.saic.com";
    static final String entityID1 = "roberta@core.saic.com";

    static final String baseURL = "http://localhost/uicds/ws";
    static final String serviceName = "urn:uicds:NotificationService";

    static final String xpContext = "/incident[@msgType=Alert]";

    private EndpointReferenceType endpoint;

    static final String WSN_XPATH_DIALECT = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    static final String WSN_SIMPLE_TOPIC = "http://docs.oasis-open.org/wsn/2004/06/TopicExpression/Simple";
    static final String PROFILE_SERVICE_NS = "http://uicds.org/ProfileService/";

    @Before
    public void setup() {

        // log.debug("\nCall: \n onSetUp\n");
        pubSubService = org.easymock.EasyMock.createMock(PubSubService.class);
        notificationService.setPubSubService(pubSubService);

        configurationService = org.easymock.EasyMock.createMock(ConfigurationService.class);
        notificationService.setConfigurationService(configurationService);

        notificationDAO = org.easymock.EasyMock.createMock(NotificationDAO.class);
        notificationService.setNotificationDAO(notificationDAO);

        communicationsService = org.easymock.EasyMock.createMock(CommunicationsService.class);
        notificationService.setCommunicationsService(communicationsService);

        workProductService = org.easymock.EasyMock.createMock(WorkProductService.class);
        notificationService.setWorkProductService(workProductService);
    }

    @Test
    public void testProfileServiceCreate() {

        // log.debug("\nTest: \n ProfileServiceCreate\n");
        assertNotNull(notificationService);
    }

    // Will be called by the Profile service for each profile that is created
    @Test
    public void testCreatePullpoint() {

        // logger.debug("\nTest: \nCreatePullpoint\n");
        resetMocks();

        createPullPoint();

        String expectedAddress = baseURL + "/" + entityID;
        assertNotNull(endpoint.getAddress());
        assertEquals(expectedAddress, endpoint.getAddress().getStringValue());

        // Make sure the URL is valid
        try {
            URL url = new URL(endpoint.getAddress().getStringValue());
            assertNotNull(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testChangeEndpoint() {

        // logger.debug("\nTest: \nCreatePullpoint\n");
        resetMocks();

        createPullPoint();
        changePullPoint(entityID);
        changePullPoint(entityID1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubscribe() {

        createPullPoint();

        Interest interest = Interest.Factory.newInstance();
        interest.addNewMessageContent().setDialect(WSN_SIMPLE_TOPIC);
        interest.setTopicExpression(INCIDENT_WP_TYPE);
        FilterType filter = null;
        try {
            filter = NotificationUtils.getFilterFromInterest(interest);
        } catch (XmlException e2) {
            fail("error creating filter " + e2.getMessage());
        }

        resetMocks();

        Integer subscriptionID = new Integer(1);

        try {
            org.easymock.EasyMock.expect(
                pubSubService.subscribeInterestGroupIdAndWorkProductType(
                    org.easymock.EasyMock.isA(String.class),
                    org.easymock.EasyMock.isA(String.class),
                    org.easymock.EasyMock.isA(String.class), org.easymock.EasyMock.isA(Map.class),
                    org.easymock.EasyMock.isA(PubSubNotificationService.class))).andReturn(
                subscriptionID);
        } catch (NullSubscriberException e1) {
            fail("null subscriber");
        } catch (EmptySubscriberNameException e1) {
            fail("empty subscriber");
        }

        org.easymock.EasyMock.expect(notificationDAO.findByEntityId((entityID))).andReturn(null);

        Notification notification = new Notification();
        notification.setEntityID(entityID);
        org.easymock.EasyMock.expect(notificationDAO.makePersistent(notification)).andReturn(
            notification);

        org.easymock.EasyMock.replay(pubSubService, notificationDAO);

        try {
            notificationService.subscribe(endpoint, filter);
        } catch (InvalidProductTypeException e) {
            fail("invalid product type");
        } catch (NullSubscriberException e) {
            fail("null subscriber");
        } catch (EmptySubscriberNameException e) {
            fail("empty subscriber");
        } catch (InvalidProductIDException e) {
            fail("invalid product id");
        }

        org.easymock.EasyMock.verify(pubSubService, notificationDAO);

        destroyPullPoint(true);
    }

    // @Test
    // public void testNewProductVersion() {
    // WorkProduct workProduct =
    // getWorkProductFromFile("src/test/resources/workproduct/WorkProduct1.xml");
    // assertNotNull(workProduct);
    // org.easymock.EasyMock.expect(workProductService.getProduct(isA(String.class))).andReturn(workProduct);
    //
    // List<Notification> notifications = new ArrayList<Notification>();
    // Notification notification = new Notification();
    // notification.setEntityID(entityID);
    // NotificationSubscription sub = new NotificationSubscription();
    // sub.setId(1);
    // sub.setNotification(notification);
    // sub.setSubscriptionID(1);
    // notification.addSubscription(sub);
    // notifications.add(notification);
    // org.easymock.EasyMock.expect(notificationDAO.findBySubscriptionId(EasyMock.isA(Integer.class))).andReturn(notifications);
    //
    // org.easymock.EasyMock.replay(workProductService, notificationDAO);
    //
    // notificationService.newWorkProductVersion("id", 1);
    //
    // assertEquals("no notification",1,notification.getMessages().size());
    // }
    //
    // @Test
    // public void testNewProductVersionToXMPP() {
    // WorkProduct workProduct =
    // getWorkProductFromFile("src/test/resources/workproduct/WorkProduct1.xml");
    // assertNotNull(workProduct);
    // org.easymock.EasyMock.expect(workProductService.getProduct(isA(String.class))).andReturn(workProduct);
    //
    // List<Notification> notifications = new ArrayList<Notification>();
    // Notification notification = new Notification();
    // notification.setEntityID(entityID);
    // notification.setEndpointURL("xmpp:" + entityID);
    // notification.setEndpointWS(true);
    // NotificationSubscription sub = new NotificationSubscription();
    // sub.setId(1);
    // sub.setNotification(notification);
    // sub.setSubscriptionID(1);
    // notification.addSubscription(sub);
    // notifications.add(notification);
    // org.easymock.EasyMock.expect(notificationDAO.findBySubscriptionId(EasyMock.isA(Integer.class))).andReturn(notifications);
    //
    // Capture<String> body = new Capture<String>();
    // Capture<String> html = new Capture<String>();
    // Capture<String> message = new Capture<String>();
    // Capture<String> jid = new Capture<String>();
    // communicationsService.sendXMPPMessage(and(isA(String.class),capture(body)),
    // and((String)isNull(),capture(html)), and(isA(String.class),capture(message)),
    // and(isA(String.class),capture(jid)));
    //
    // org.easymock.EasyMock.replay(workProductService, notificationDAO, communicationsService);
    //
    // notificationService.newWorkProductVersion("id", 1);
    //
    // assertNotNull("no body",body.getValue());
    // // Don't have anything to put in an HTML version yet. Will fill with
    // // URL to work product once we have access to the config file to create
    // // base URL to search controller provided work product. Should be
    // // able to access the base url by getting the config.xml from the ConfigurationService.
    // assertNull("has html",html.getValue());
    // assertNotNull("no message",message.getValue());
    // assertNotNull("no jid",jid.getValue());
    //
    // assertEquals("wrong jid",entityID,jid.getValue());
    // assertTrue("wrong body",body.getValue().startsWith("UICDS Incident work product"));
    // assertTrue("wrong message",message.getValue().startsWith("<str:WorkProduct"));
    // }
    //
    // @Test
    // public void testNewProductVersionToXMPP2() {
    // WorkProduct workProduct =
    // getWorkProductFromFile("src/test/resources/workproduct/WorkProduct2.xml");
    // assertNotNull(workProduct);
    // org.easymock.EasyMock.expect(workProductService.getProduct(isA(String.class))).andReturn(workProduct);
    //
    // List<Notification> notifications = new ArrayList<Notification>();
    // Notification notification = new Notification();
    // notification.setEntityID(entityID);
    // notification.setEndpointURL("xmpp:" + entityID);
    // notification.setEndpointWS(true);
    // NotificationSubscription sub = new NotificationSubscription();
    // sub.setId(1);
    // sub.setNotification(notification);
    // sub.setSubscriptionID(1);
    // notification.addSubscription(sub);
    // notifications.add(notification);
    // org.easymock.EasyMock.expect(notificationDAO.findBySubscriptionId(EasyMock.isA(Integer.class))).andReturn(notifications);
    //
    // Capture<String> body = new Capture<String>();
    // Capture<String> html = new Capture<String>();
    // Capture<String> message = new Capture<String>();
    // Capture<String> jid = new Capture<String>();
    // communicationsService.sendXMPPMessage(and(isA(String.class),capture(body)),
    // and((String)isNull(),capture(html)), and(isA(String.class),capture(message)),
    // and(isA(String.class),capture(jid)));
    //
    // org.easymock.EasyMock.replay(workProductService, notificationDAO, communicationsService);
    //
    // notificationService.newWorkProductVersion("id", 1);
    //
    // assertNotNull("no body",body.getValue());
    // // Don't have anything to put in an HTML version yet. Will fill with
    // // URL to work product once we have access to the config file to create
    // // base URL to search controller provided work product. Should be
    // // able to access the base url by getting the config.xml from the ConfigurationService.
    // assertNull("has html",html.getValue());
    // assertNotNull("no message",message.getValue());
    // assertNotNull("no jid",jid.getValue());
    //
    // assertEquals("wrong jid",entityID,jid.getValue());
    // assertTrue("wrong body",body.getValue().startsWith("UICDS MapViewContext work product"));
    // assertTrue("wrong message",message.getValue().startsWith("<str:WorkProduct"));
    // }

    private WorkProduct getWorkProductFromFile(String fileName) {

        File file = new File(fileName);
        assertNotNull(file);
        assertTrue(file.exists());
        WorkProductDocument doc = null;
        try {
            doc = WorkProductDocument.Factory.parse(file);
        } catch (XmlException e) {
            fail("XML parsing " + fileName);
        } catch (IOException e) {
            fail("IO parsing " + fileName);
        }

        WorkProduct workProduct = WorkProductHelper.toModel(doc.getWorkProduct());
        return workProduct;
    }

    private void resetMocks() {

        org.easymock.EasyMock.reset(configurationService, notificationDAO, pubSubService);
    }

    private void createPullPoint() {

        resetMocks();

        org.easymock.EasyMock.expect(configurationService.getWebServiceBaseURL()).andReturn(baseURL);
        org.easymock.EasyMock.expect(
            configurationService.getServiceNameURN(NotificationService.NOTIFICATION_SERVICE_NAME)).andReturn(
            serviceName);
        org.easymock.EasyMock.expect(notificationDAO.findByEntityId((entityID))).andReturn(null);
        org.easymock.EasyMock.expect(
            notificationDAO.makePersistent(org.easymock.EasyMock.isA(Notification.class))).andReturn(
            null);

        org.easymock.EasyMock.replay(configurationService, notificationDAO);

        endpoint = notificationService.createPullPoint(entityID);
        assertNotNull(endpoint);

        org.easymock.EasyMock.verify(configurationService, notificationDAO);

        resetMocks();
    }

    private void changePullPoint(String id) {

        resetMocks();

        org.easymock.EasyMock.expect(notificationDAO.findByEntityId((entityID))).andReturn(null);

        Capture<Notification> notification = new Capture<Notification>();
        org.easymock.EasyMock.expect(
            notificationDAO.makePersistent(org.easymock.EasyMock.and(
                org.easymock.EasyMock.isA(Notification.class),
                org.easymock.EasyMock.capture(notification)))).andReturn(null);

        org.easymock.EasyMock.replay(configurationService, notificationDAO);

        String endpointAddress = CommunicationsService.XMPPAddressScheme + ":" + id;
        notificationService.updateEndpoint(entityID, endpointAddress, true);

        String newEndpoint = notification.getValue().getEndpointURL();
        assertNotNull("new endpoint is null", newEndpoint);
        assertFalse("new endpoint is empty", newEndpoint.isEmpty());
        assertEquals(endpointAddress, newEndpoint);

        // Make sure the URL is valid
        try {
            URI uri = new URI(notification.getValue().getEndpointURL());
            assertNotNull(uri);
            assertEquals("wrong id", id, uri.getSchemeSpecificPart());
            // System.out.println(uri.getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            fail("URI invalid: " + e.getMessage());
        }

        org.easymock.EasyMock.verify(configurationService, notificationDAO);

        resetMocks();
    }

    private void destroyPullPoint(boolean expectUnsubscribe) {

        resetMocks();

        Notification notification = new Notification();
        notification.setEntityID(entityID);
        NotificationSubscription subscription = new NotificationSubscription();
        Integer subscriptionID = new Integer(1);
        subscription.setSubscriptionID(subscriptionID);
        notification.addSubscription(subscription);
        org.easymock.EasyMock.expect(notificationDAO.findByEntityId((entityID))).andReturn(
            notification);
        // org.easymock.EasyMock.expect(notificationDAO.findByEntityId((entityID))).andReturn(notification);
        // notificationDAO.makeTransient(org.easymock.EasyMock.isA(Notification.class));
        pubSubService.unsubscribeBySubscriptionID(subscriptionID);

        notificationDAO.makeTransient(org.easymock.EasyMock.isA(Notification.class));

        org.easymock.EasyMock.replay(notificationDAO, pubSubService);

        notificationService.destroyPullPoint(entityID);

        org.easymock.EasyMock.verify(notificationDAO, pubSubService);

        resetMocks();
    }
    //
    // public void testSubscribeWithXPath() {
    // // SubscribeResponse response = SubscribeResponse.Factory.newInstance();
    // // response.addNewSubscriptionReference();
    // //
    // // endpoint = notificationService.createPullPoint(entityID);
    // // assertNotNull(endpoint);
    // // assertNotNull(endpoint.getAddress());
    // //
    // // FilterType filter = FilterType.Factory.newInstance();
    // // XmlCursor filterCursor = filter.newCursor();
    // // filterCursor.toNextToken();
    // //
    // // // Set the work product type as the simple topic expression
    // // TopicExpressionDocument topicDoc = TopicExpressionDocument.Factory.newInstance();
    // // TopicExpressionType topic = topicDoc.addNewTopicExpression();
    // // topic.setDialect(WSN_SIMPLE_TOPIC);
    // // XmlCursor topicCursor = topic.newCursor();
    // // topicCursor.toNextToken();
    // // topicCursor.toNextToken();
    // // topicCursor.insertChars("/" + IncidentManagementService.Type);
    // //
    // // topicCursor.toStartDoc();
    // // topicCursor.toNextToken();
    // // topicCursor.copyXml(filterCursor);
    // //
    // // topicCursor.dispose();
    // //
    // // String xPath = "//stuff";
    // // MessageContentDocument contentDoc = MessageContentDocument.Factory.newInstance();
    // // QueryExpressionType query = contentDoc.addNewMessageContent();
    // // query.setDialect(WSN_XPATH_DIALECT);
    // // XmlCursor contentCursor = query.newCursor();
    // // contentCursor.toNextToken();
    // // contentCursor.toNextToken();
    // // contentCursor.insertChars(xPath);
    // //
    // // contentCursor.toStartDoc();
    // // contentCursor.toNextToken();
    // // contentCursor.copyXml(filterCursor);
    // //
    // // QName qname = new QName(PROFILE_SERVICE_NS, "NamespaceMap");
    // // filterCursor.beginElement(qname);
    // // filterCursor.beginElement(new QName(PROFILE_SERVICE_NS, "item"));
    // // filterCursor.insertElementWithText(new QName(PROFILE_SERVICE_NS, "prefix"), "de");
    // // filterCursor.insertElementWithText(new QName(PROFILE_SERVICE_NS, "uri"),
    // // "urn:oasis:names:tc:emergency:EDXL:DE:1.0");
    // // filterCursor.toNextToken();
    // // filterCursor.beginElement(new QName(PROFILE_SERVICE_NS, "item"));
    // // filterCursor.insertElementWithText(new QName(PROFILE_SERVICE_NS, "prefix"), "cap");
    // // filterCursor.insertElementWithText(new QName(PROFILE_SERVICE_NS, "uri"),
    // // "urn:oasis:names:tc:emergency:cap:1.1");
    // //
    // // contentCursor.dispose();
    // // filterCursor.dispose();
    // //
    // // HashMap<String, String> namespaceMap = new HashMap<String, String>();
    // // try {
    // // org.easymock.EasyMock.expect(
    // // pubSubService.subscribeWorkProductType(org.easymock.EasyMock.isA(String.class),
    // // org.easymock.EasyMock.startsWith(xPath), org.easymock.EasyMock
    // // .isA(namespaceMap.getClass()), org.easymock.EasyMock
    // // .isA(PubSubNotificationService.class))).andReturn(
    // // subscriptionID);
    // // } catch (InvalidProductTypeException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (NullSubscriberException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (EmptySubscriberNameException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // }
    // // org.easymock.EasyMock.replay(pubSubService);
    // // response = notificationService.subscribe(endpoint, filter);
    // // assertNotNull(response);
    // // assertNotNull(response.getSubscriptionReference());
    // // assertNotNull(response.getSubscriptionReference().getAddress());
    // // // assertEquals(endpoint.getAddress(), response.getSubscriptionReference().getAddress());
    // // org.easymock.EasyMock.verify(pubSubService);
    // }
    //
    // // Will be called by the Profile service for each interest
    // public void testSubscribeWorkProductID() {
    // // log.debug("\nTest: \n Subscribe Work Product ID\n");
    // // String workProduct = "1";
    // // try {
    // // org.easymock.EasyMock.expect(
    // // pubSubService.subscribeWorkProductID(org.easymock.EasyMock.isA(String.class),
    // // org.easymock.EasyMock.isA(PubSubNotificationService.class))).andReturn(
    // // subscriptionID);
    // // } catch (InvalidProductIDException e) {
    // // e.printStackTrace();
    // // } catch (NullSubscriberException e) {
    // // e.printStackTrace();
    // // } catch (EmptySubscriberNameException e) {
    // // e.printStackTrace();
    // // }
    // // org.easymock.EasyMock.replay(pubSubService);
    // // try {
    // // subscriptionID = notificationService.subscribeWorkProductID(workProduct, entityID);
    // // } catch (InvalidProductIDException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (NullSubscriberException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (EmptySubscriberNameException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // }
    // // org.easymock.EasyMock.verify(pubSubService);
    // // log.debug("SubID: " + subscriptionID + " EntityID: " + entityID);
    // }
    //
    // // Will be called by the Profile service for each interest by XPath expression
    // public void testSubscribeWorkProductType() {
    // // log.debug("\nTest: \n Subscribe Work Product Type\n");
    // // HashMap<String, String> namespaceMap = new HashMap<String, String>();
    // //
    // // try {
    // // org.easymock.EasyMock.expect(
    // // pubSubService.subscribeWorkProductType(org.easymock.EasyMock.isA(String.class),
    // // org.easymock.EasyMock.isA(String.class), org.easymock.EasyMock
    // // .isA(namespaceMap.getClass()), org.easymock.EasyMock
    // // .isA(PubSubNotificationService.class))).andReturn(
    // // subscriptionID);
    // // } catch (InvalidProductTypeException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (NullSubscriberException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (EmptySubscriberNameException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // }
    // // org.easymock.EasyMock.replay(pubSubService);
    // // try {
    // // HashMap<String, String> nsMap = new HashMap<String, String>();
    // // nsMap.put("urn", "urn:oasis:names:tc:emergency:cap:1.1");
    // // nsMap.put("inc", "http://uicds.org/incident");
    // // nsMap.put("de", "urn:oasis:names:tc:emergency:EDXL:DE:1.0");
    // // subscriptionID = notificationService.subscribeWorkProductType(AlertService.Type,
    // // xpContext, nsMap, entityID);
    // // } catch (InvalidProductTypeException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (NullSubscriberException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // } catch (EmptySubscriberNameException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // }
    // // org.easymock.EasyMock.verify(pubSubService);
    // // log.debug("SubID:" + subscriptionID + " EntityID:" + entityID + " Xpath:"
    // // + xpContext);
    // }
    //
    // // Will be called by pubsub when a subscription is met
    // public void testNewWorkProduct() {
    // // log.debug("\nTest: \n New Work Product\n");
    // // assertNotNull(subscriptionID);
    // // // log.debug("Subscribe ID: " + subscriptionID);
    // // notificationService.newWorkProductVersion("1", subscriptionID);
    // // notificationService.newWorkProductVersion("2", subscriptionID);
    // // notificationService.newWorkProductVersion("3", subscriptionID);
    // // assertNotNull(subscriptionID1);
    // // // log.debug("Subscribe ID: " + subscriptionID1);
    // // notificationService.newWorkProductVersion("1", subscriptionID1);
    // }
    //
    // // Will be called by the NotificationEndpoint when a client calls GetMessages on their pull
    // // point
    // public void testGetMessage() {
    // // log.debug("\nTest: \n Get Message\n");
    // // log.debug("\n1st call for GetMessages For entityID: " + entityID + "\n");
    // // NotificationMessageHolderType[] messages = notificationService.getMessages(entityID, 1);
    // // // log.debug("Number of messages waiting: " + messages.length);
    // // int tot = messages.length;
    // // // for (int i = 0; i < tot; i++) {
    // // // log.debug("Message " + i + ":" + messages[i].toString());
    // // // }
    // // assertNotNull(messages);
    // // assertEquals(3, messages.length);
    // //
    // // // should be no messages left for this entityid
    // // NotificationMessageHolderType[] numMessages = notificationService.getMessages(entityID,
    // // 1);
    // // // log.debug("\n2nd call for GetMessages For entityID: " + entityID + "\n");
    // // // log.debug("Number of messages waiting: " + numMessages.length);
    // // int tot1 = numMessages.length;
    // // // for (int j = 0; j < tot1; j++) {
    // // // log.debug("Message " + j + ":" + messages[j].toString());
    // // // }
    // // assertNotNull(numMessages);
    // // assertEquals(0, numMessages.length);
    //
    // }
    // This test should test adding a couple of messages, make sure we get the same number of
    // messages
    // back, and that there are zero messages when a second getMessagesDocument is called

    // private FilterType createFilter() {
    // FilterType filter = FilterType.Factory.newInstance();
    // XmlCursor filterCursor = filter.newCursor();
    // filterCursor.toNextToken();
    //
    // // Set the work product type as the simple topic expression
    // TopicExpressionDocument topicDoc = TopicExpressionDocument.Factory.newInstance();
    // TopicExpressionType topic = topicDoc.addNewTopicExpression();
    // topic.setDialect(WSN_SIMPLE_TOPIC);
    // XmlCursor topicCursor = topic.newCursor();
    // topicCursor.toNextToken();
    // topicCursor.toNextToken();
    // topicCursor.insertChars("/" + INCIDENT_WP_TYPE);
    //
    // filterCursor.dispose();
    // System.out.println(filter);
    // return filter;
    // }

}
