package com.saic.uicds.core.infrastructure.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.saic.uicds.core.infrastructure.model.Notification;
import com.saic.uicds.core.infrastructure.model.NotificationSubscription;

//@Ignore
public class NotificationDAOTest
    extends AbstractJpaTests {

    private Logger log = LoggerFactory.getLogger(NotificationDAOTest.class);
    private String sampleEntityID = "testEntID";
    private String sampleEndpoint = "//saic.com/tests/";
    private String secondEndpoint = "xmpp:user@core";
    private Integer sampleSubID = 33;

    @Autowired
    NotificationDAO notificationDAO;

    @Override
    protected String[] getConfigLocations() {

        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-dataSrcContext.xml" };
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {

    }

    @Test
    public void testFindWithNullEntityID() {

        Notification notification = new Notification();

        notification = buildSampleNotification(sampleEntityID, sampleSubID);

        Notification result = notificationDAO.findByEntityId(null);
        assertNull(result);

        // don't clear messages and delete the notification
        notificationDAO.makeTransient(notification);

        Notification list = notificationDAO.findByEntityId(sampleEntityID);
        assertNull("notification is still found", list);
    }

    @Test
    public void testFindByEntityIdWithOneEntity() {

        Notification notification = new Notification();

        log.debug("Creating sample Notification");
        notification = buildSampleNotification(sampleEntityID, sampleSubID);

        log.debug("Retrieving sample Notification by EntityID");
        Notification result = notificationDAO.findByEntityId(sampleEntityID);

        checkFindByEntityIdResult(notification, result);

        log.debug(result.toString());

        // clear messages and delete the notification
        result.clearMessages();
        notificationDAO.makePersistent(result);
        /*
        notificationDAO.makeTransient(result);

        Notification list = notificationDAO.findByEntityId(sampleEntityID);
        assertNull("notification is still found",list);
        */
    }

    @Test
    public void testFindByEntityIdWithOneMultipleEntities() {

        Notification notification = buildSampleNotification(sampleEntityID, sampleSubID);
        assertNotNull("notification is null", notification);

        int lastValue = 6;
        String entityID = sampleEntityID + Integer.toString(lastValue - 2);
        Notification checkedNotification = null;

        for (int i = 1; i < 6; i++) {
            Notification notif = new Notification();
            notif = buildSampleNotification(sampleEntityID + Integer.toString(i), sampleSubID + i);
            if (entityID.equals(notif.getEntityID())) {
                checkedNotification = notif;
            }
        }

        Notification result = notificationDAO.findByEntityId(entityID);

        List<Notification> notifs = notificationDAO.findAll();
        // Using lastValue because for loop starts with 1
        assertTrue("Wrong number of Notifications", notifs.size() == lastValue);

        checkFindByEntityIdResult(checkedNotification, result);

        log.debug(result.toString());
    }

    @Test
    public void testOrdering() {

        Notification notification = new Notification();
        notification = buildSampleNotification(sampleEntityID, sampleSubID);
        checkMessageOrder(notification.getMessagesAsString());
        notificationDAO.makeTransient(notification);
    }

    @Test
    public void testMixedOrdering() {

        Notification notification = new Notification();
        notification = buildMixedSampleNotification(sampleEntityID, sampleSubID);
        checkMessageOrder(notification.getMessagesAsString());
        notificationDAO.makeTransient(notification);
    }

    @Test
    public void testChangeEndpoint() {

        log.debug("Creating sample Notification");
        Notification notification = buildSampleNotification(sampleEntityID, sampleSubID);

        log.debug("Retrieving sample Notification by EntityID");
        Notification result = notificationDAO.findByEntityId(sampleEntityID);
        assertNotNull("first result not found", result);

        result.setEndpointURL(secondEndpoint);
        result.setEndpointWS(true);

        Notification newResult = notificationDAO.makePersistent(result);
        assertNotNull("modified result null", newResult);
        assertEquals("not same result", result, newResult);
        assertEquals("wrong endpoint", secondEndpoint, newResult.getEndpointURL());
        assertTrue("not WS", newResult.isEndpointWS());

        Notification secondFindResult = notificationDAO.findByEntityId(sampleEntityID);
        assertNotNull("not found after update endpoint", secondFindResult);
    }

    private void checkFindByEntityIdResult(Notification notification, Notification result) {

        assertNotNull(result);
        assertEquals(notification, result);
        assertNotNull(result.getEntityID());
        assertEquals(result.getEntityID(), notification.getEntityID());
        assertNotNull(result.getEndpointURL());
        assertEquals(result.getEndpointURL(), notification.getEndpointURL());
        assertNotNull(result.getSubscriptions());
        assertEquals(result.getSubscriptions().size(), 1);
        assertEquals(result.getSubscriptions().size(), notification.getSubscriptions().size());
        assertEquals(result.getMessages().size(), notification.getMessages().size());
    }

    private Notification buildSampleNotification(String entityID, Integer subID) {

        Notification notification = new Notification();
        notification.setEntityID(entityID);
        notification.setEndpointURL(sampleEndpoint);
        addNewMessage("1", subID, notification);
        addNewMessage("2", subID, notification);
        addNewMessage("3", subID, notification);
        addNewMessage("4", subID, notification);
        addNewMessage("5", subID, notification);
        addNewMessage("6", subID, notification);
        addNewMessage("7", subID, notification);
        addNewMessage("8", subID, notification);

        Notification result = notificationDAO.makePersistent(notification);
        assertNotNull(result);
        assertEquals(notification, result);
        assertNotNull(notification.getEntityID());
        assertEquals(8, result.getMessages().size());
        assertEquals(1, result.getSubscriptions().size());
        entityID = notification.getEntityID();

        return notification;
    }

    private Notification buildMixedSampleNotification(String entityID, int subID) {

        Notification notification = new Notification();
        notification.setEntityID(entityID);
        notification.setEndpointURL(sampleEndpoint);
        addNewMessage("1", subID, notification);
        addNewMessage("2", subID, notification);
        addNewMessage("3", subID, notification);
        addNewMessage("4", subID + 1, notification);
        addNewMessage("5", subID + 1, notification);
        addNewMessage("6", subID + 2, notification);
        addNewMessage("7", subID + 3, notification);
        addNewMessage("8", subID + 1, notification);
        addNewMessage("9", subID + 2, notification);
        addNewMessage("10", subID + 2, notification);
        addNewMessage("11", subID + 2, notification);
        addNewMessage("12", subID + 6, notification);
        addNewMessage("13", subID + 7, notification);
        addNewMessage("14", subID + 6, notification);
        addNewMessage("15", subID, notification);
        addNewMessage("16", subID + 7, notification);
        addNewMessage("17", subID + 2, notification);
        addNewMessage("18", subID, notification);

        Notification result = notificationDAO.makePersistent(notification);
        assertNotNull(result);
        assertEquals(notification, result);
        assertNotNull(notification.getEntityID());
        assertEquals(18, result.getMessages().size());
        assertEquals(6, result.getSubscriptions().size());
        entityID = notification.getEntityID();

        return notification;
    }

    private void addNewMessage(String message, Integer subID, Notification notification) {

        Set<NotificationSubscription> subscriptions = notification.getSubscriptions();
        Iterator<NotificationSubscription> itr = subscriptions.iterator();
        boolean foundSub = false;
        while (itr.hasNext()) {
            NotificationSubscription sub = itr.next();
            if (sub.getSubscriptionID() == subID) {
                foundSub = true;
            }
        }
        if (!foundSub) {
            NotificationSubscription subscription = new NotificationSubscription();
            subscription.setSubscriptionID(subID);
            notification.addSubscription(subscription);
            subscriptions.add(subscription);
        }
        notification.addMessage(subID, "WorkProductID" + notification.getMessages().size(), message);
        // Notification result = notificationDAO.makePersistent(notification);
        // assertNotNull(result);
    }

    private void checkMessageOrder(Set<String> messages) {

        Iterator<String> itr = messages.iterator();
        int index = 1;
        while (itr.hasNext()) {
            int value = Integer.parseInt(itr.next());
            assertTrue("wrong index: looking for " + index + " got " + value, index == value);
            index++;
        }
    }

    @Test
    public void testfindBySubscriptionId() {

        log.debug("Creating mixed Notification");
        Notification notification = new Notification();
        notification = buildMixedSampleNotification(sampleEntityID, sampleSubID);

        checkMessageOrder(notification.getMessagesAsString());

        log.debug("Retrieving sample Notification by subid");
        List<Notification> results = notificationDAO.findBySubscriptionId(sampleSubID);
        assertNotNull("notification found", results);

        Iterator<Notification> it = results.iterator();
        while (it.hasNext()) {
            Notification obj = (Notification) it.next();
            checkMessageOrder(obj.getMessagesAsString());
        }

    }

}
