/**
 * 
 */
package com.saic.uicds.core.infrastructure.service;

import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.jpa.AbstractJpaTests;
import org.uicds.resourceInstanceService.ResourceInstance;
import org.uicds.resourceInstanceService.ResourceInstanceListType;
import org.uicds.resourceProfileService.Interest;
import org.uicds.resourceProfileService.ResourceProfile;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import com.saic.precis.x2009.x06.base.IdentifierType;
import com.saic.uicds.core.infrastructure.dao.ResourceInstanceDAO;
import com.saic.uicds.core.infrastructure.exceptions.EmptySubscriberNameException;
import com.saic.uicds.core.infrastructure.exceptions.InvalidProductIDException;
import com.saic.uicds.core.infrastructure.exceptions.InvalidProductTypeException;
import com.saic.uicds.core.infrastructure.exceptions.NullSubscriberException;
import com.saic.uicds.core.infrastructure.exceptions.ResourceInstanceDoesNotExist;
import com.saic.uicds.core.infrastructure.exceptions.ResourceProfileDoesNotExist;
import com.saic.uicds.core.infrastructure.model.ResourceInstanceModel;
import com.saic.uicds.core.infrastructure.model.ResourceProfileModel;
import com.saic.uicds.core.infrastructure.service.impl.ConfigurationServiceImpl;
import com.saic.uicds.core.infrastructure.service.impl.ResourceInstanceServiceImpl;
import com.saic.uicds.core.infrastructure.util.NotificationUtils;
import com.saic.uicds.core.infrastructure.util.ResourceProfileUtil;
import com.saic.uicds.core.infrastructure.util.ServiceNamespaces;
import com.saic.uicds.core.infrastructure.util.XmlUtil;

/**
 * @author roger
 * 
 */
public class ResourceInstanceServiceTest
    extends AbstractJpaTests {

    Logger log = LoggerFactory.getLogger(this.getClass());

    ResourceInstanceServiceImpl resourceInstanceService = new ResourceInstanceServiceImpl();

    // No need to mock this object's behavior since it just returns constants
    ConfigurationService configurationService = new ConfigurationServiceImpl();

    // Mock objects
    NotificationService notificationService;
    ResourceProfileService resourceProfileService;
    ResourceInstanceDAO resourceInstanceDAO;

    // Test variables
    final static String RESOURCE_ID1 = "resourceID1";
    final static String RESOURCE_ID2 = "resourceID2";
    final static String RESOURCE_ID3 = "resourceID3";
    final static String UNKNOWN_RESOURCE_ID1 = "unknownResourceID1";
    final static String PROFILE_ID1 = "profileID1";
    final static String ENDPOINT_UPDATE = "http://endpoint";
    IdentifierType resourceID1 = IdentifierType.Factory.newInstance();
    IdentifierType resourceID2 = IdentifierType.Factory.newInstance();
    IdentifierType resourceID3 = IdentifierType.Factory.newInstance();
    IdentifierType profileID1 = IdentifierType.Factory.newInstance();
    IdentifierType localResourceID1 = IdentifierType.Factory.newInstance();
    Integer intID = 1;
    Integer msgCount1 = new Integer(1);
    Integer msgCount2 = new Integer(2);
    Integer msgCount3 = new Integer(3);

    EndpointReferenceType endpoint = null;
    ResourceProfile resourceProfile = null;
    ResourceProfileModel resourceProfileModel = null;

    @Override
    public void onSetUp() {

        resourceID1.setStringValue(RESOURCE_ID1);
        resourceID1.setLabel(RESOURCE_ID1);
        resourceID2.setStringValue(RESOURCE_ID2);
        resourceID2.setLabel(RESOURCE_ID2);
        resourceID3.setStringValue(RESOURCE_ID3);
        resourceID3.setLabel(RESOURCE_ID3);
        profileID1.setStringValue(PROFILE_ID1);
        profileID1.setLabel(PROFILE_ID1);
        localResourceID1.setStringValue("");

        resourceInstanceService.setConfigurationService(configurationService);

        if (resourceInstanceDAO == null) {
            resourceInstanceDAO = org.easymock.EasyMock.createMock(ResourceInstanceDAO.class);
            resourceInstanceService.setResourceInstanceDAO(resourceInstanceDAO);
        }
        if (notificationService == null) {
            notificationService = org.easymock.EasyMock.createMock(NotificationService.class);
            resourceInstanceService.setNotificationService(notificationService);
        }
        if (resourceProfileService == null) {
            resourceProfileService = org.easymock.EasyMock.createMock(ResourceProfileService.class);
            resourceInstanceService.setResourceProfileService(resourceProfileService);
        }
        if (endpoint == null) {
            endpoint = NotificationUtils.createEndpoint(resourceID1.getStringValue());
        }
        if (resourceProfile == null) {
            resourceProfile = ResourceProfile.Factory.newInstance();
            resourceProfile.addNewID().setStringValue(profileID1.getStringValue());
            Interest i1 = resourceProfile.addNewInterests().addNewInterest();
            QName topicExpression = new QName("Incident");
            i1.setTopicExpression(topicExpression.toString());
            // resourceProfile.setID(new BigInteger(intID));
        }
        if (resourceProfileModel == null) {
            resourceProfileModel = ResourceProfileUtil.copyProperties(resourceProfile, new Integer(
                intID));
        }

    }

    @Test
    public void testCreateResourceInstance() {

        ResourceInstanceModel ris = createResourceInstance(resourceID1);
        assertNotNull(ris);
    }

    private ResourceInstanceModel createResourceInstance(IdentifierType resourceID) {

        org.easymock.EasyMock.reset(notificationService);
        assertNotNull(endpoint);
        org.easymock.EasyMock.expect(
            notificationService.createPullPoint(resourceID.getStringValue())).andReturn(endpoint);
 //       org.easymock.EasyMock.expect(
 //           notificationService.findMsgCountByEntityId(resourceID.getStringValue())).andReturn(
 //           msgCount1);
        org.easymock.EasyMock.replay(notificationService);

        ResourceInstanceModel resourceModel = resourceInstanceService.createResourceInstance(resourceID);
        HashSet<String> profiles = new HashSet<String>();
        profiles.add(profileID1.getStringValue());
        resourceModel.setProfiles(profiles);

        resourceModel.setLocalResourceID(localResourceID1.getStringValue());

        assertNotNull(resourceModel);
        assertEquals(resourceID.getStringValue(), resourceModel.getResourceID());
        assertEquals(1, resourceModel.getEndpoints().size());
        assertTrue(resourceModel.getEndpoints().contains(endpoint.getAddress().getStringValue()));
        org.easymock.EasyMock.verify(notificationService);

        return resourceModel;
    }

    @Test
    public void testApplyProfile() {

        ResourceInstanceModel resourceModel = createResourceInstance(resourceID1);
        // make sure no profiles have been applied to this resource
        HashSet<String> profiles = new HashSet<String>();
        resourceModel.setProfiles(profiles);

        org.easymock.EasyMock.reset(resourceProfileService, notificationService);

        org.easymock.EasyMock.expect(resourceProfileService.getProfile(profileID1)).andReturn(
            resourceProfileModel);
        try {
            notificationService.subscribe(org.easymock.EasyMock.isA(EndpointReferenceType.class),
                org.easymock.EasyMock.isA(FilterType.class));
        } catch (InvalidProductTypeException e1) {
            fail("invalid product type");
        } catch (NullSubscriberException e1) {
            fail("null subscriber");
        } catch (EmptySubscriberNameException e1) {
            fail("empty subscriber name");
        } catch (InvalidProductIDException e1) {
            fail("invalid product id");
        }

        org.easymock.EasyMock.replay(resourceProfileService, notificationService);

        try {
            resourceInstanceService.applyProfile(resourceModel, profileID1);
        } catch (ResourceProfileDoesNotExist e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        org.easymock.EasyMock.verify(resourceProfileService, notificationService);
    }

    @Test
    public void testRegisterNoPersistedResourceInstance() {

        registerSingleResourceInstance();
    }

    @Test
    public void testUnregisterPersistedResourceInstance() {

   //     org.easymock.EasyMock.expect(
   //         notificationService.findMsgCountByEntityId(resourceID1.getStringValue())).andReturn(
   //         msgCount1);

        ResourceInstanceModel resourceModel = registerSingleResourceInstance();

        org.easymock.EasyMock.reset(resourceInstanceDAO, notificationService);

        org.easymock.EasyMock.expect(
            resourceInstanceDAO.findByIdentifier(org.easymock.EasyMock.isA(String.class))).andReturn(
            resourceModel);
        resourceInstanceDAO.makeTransient(org.easymock.EasyMock.isA(ResourceInstanceModel.class));
        org.easymock.EasyMock.expect(
            notificationService.destroyPullPoint(resourceModel.getResourceID())).andReturn(true);

        org.easymock.EasyMock.replay(resourceInstanceDAO, notificationService);

        IdentifierType id = IdentifierType.Factory.newInstance();
        id.setStringValue(resourceModel.getIdentifier());
        try {
            IdentifierType removedID = resourceInstanceService.unregister(id);
            assertNotNull("resource instance model null after unregister", removedID);
            assertEquals("wrong resource instance removed", removedID.getStringValue(),
                RESOURCE_ID1);
        } catch (ResourceInstanceDoesNotExist e) {
            fail(e.getMessage());
        }

        org.easymock.EasyMock.verify(resourceInstanceDAO, notificationService);
    }

    @Test(expected = com.saic.uicds.core.infrastructure.exceptions.ResourceInstanceDoesNotExist.class)
    public void testUnregisterNoPersistedResourceInstance() {

     //   org.easymock.EasyMock.expect(
     //       notificationService.findMsgCountByEntityId(resourceID1.getStringValue())).andReturn(
     //       msgCount1);

        ResourceInstanceModel resourceModel = registerSingleResourceInstance();
        assertNotNull(resourceModel);

        org.easymock.EasyMock.reset(resourceInstanceDAO);
        org.easymock.EasyMock.expect(
            resourceInstanceDAO.findByIdentifier(org.easymock.EasyMock.isA(String.class))).andReturn(
            null);

        org.easymock.EasyMock.replay(resourceInstanceDAO);

        IdentifierType id = IdentifierType.Factory.newInstance();
        id.setStringValue(UNKNOWN_RESOURCE_ID1);
        try {
            resourceInstanceService.unregister(id);
            fail("expected exception");
        } catch (ResourceInstanceDoesNotExist e) {
            ;
        }

        org.easymock.EasyMock.verify(resourceInstanceDAO);

    }

    private ResourceInstanceModel registerSingleResourceInstance() {

        ResourceInstanceModel resourceModel = createResourceInstance(resourceID1);

        org.easymock.EasyMock.reset(resourceInstanceDAO, resourceProfileService,
            notificationService);

        org.easymock.EasyMock.expect(
            resourceInstanceDAO.findByIdentifier(org.easymock.EasyMock.isA(String.class))).andReturn(
            null);
        org.easymock.EasyMock.expect(
            notificationService.createPullPoint(resourceID1.getStringValue())).andReturn(endpoint);

    //    org.easymock.EasyMock.expect(
    //        notificationService.findMsgCountByEntityId(resourceID1.getStringValue())).andReturn(
    //        msgCount1);

        org.easymock.EasyMock.expect(resourceProfileService.getProfile(profileID1)).andReturn(
            resourceProfileModel);
        try {
            notificationService.subscribe(org.easymock.EasyMock.isA(EndpointReferenceType.class),
                org.easymock.EasyMock.isA(FilterType.class));
        } catch (InvalidProductTypeException e1) {
            fail("invalid product type");
        } catch (NullSubscriberException e1) {
            fail("null subscriber");
        } catch (EmptySubscriberNameException e1) {
            fail("empty subscriber name");
        } catch (InvalidProductIDException e1) {
            fail("invalid product id");
        }
        org.easymock.EasyMock.expect(
            resourceInstanceDAO.makePersistent(org.easymock.EasyMock.isA(ResourceInstanceModel.class))).andReturn(
            resourceModel);

        org.easymock.EasyMock.replay(resourceInstanceDAO, resourceProfileService,
            notificationService);

        try {
            resourceInstanceService.register(resourceID1, localResourceID1, profileID1);
        } catch (ResourceProfileDoesNotExist e) {
            fail(e.getMessage());
        }

        org.easymock.EasyMock.verify(resourceInstanceDAO, resourceProfileService,
            notificationService);

        org.easymock.EasyMock.reset(resourceInstanceDAO, resourceProfileService,
            notificationService);

        return resourceModel;
    }

    @Test
    public void testRegisterPersistedResourceInstance() {

        ResourceInstanceModel resourceModel = createResourceInstance(resourceID1);

        org.easymock.EasyMock.reset(resourceInstanceDAO, resourceProfileService,
            notificationService);

        org.easymock.EasyMock.expect(
            resourceInstanceDAO.findByIdentifier(org.easymock.EasyMock.isA(String.class))).andReturn(
            resourceModel);
        org.easymock.EasyMock.expect(resourceProfileService.getProfile(profileID1)).andReturn(
            resourceProfileModel);

        // org.easymock.EasyMock.expect(notificationService.findMsgCountByEntityId(resourceID1.getStringValue())).andReturn(msgCount);

        org.easymock.EasyMock.replay(resourceInstanceDAO, resourceProfileService,
            notificationService);

        try {
            resourceInstanceService.register(resourceID1, localResourceID1, profileID1);
        } catch (ResourceProfileDoesNotExist e) {
            fail(e.getMessage());
        }

        org.easymock.EasyMock.verify(resourceInstanceDAO, resourceProfileService,
            notificationService);
    }

    @Test(expected = com.saic.uicds.core.infrastructure.exceptions.ResourceProfileDoesNotExist.class)
    public void testRegisterNoResourceProfile() {

        org.easymock.EasyMock.reset(resourceInstanceDAO, resourceProfileService,
            notificationService);
        org.easymock.EasyMock.expect(
            resourceInstanceDAO.findByIdentifier(org.easymock.EasyMock.isA(String.class))).andReturn(
            null);
        org.easymock.EasyMock.expect(
            notificationService.createPullPoint(resourceID1.getStringValue())).andReturn(endpoint);
        // org.easymock.EasyMock.expect(notificationService.findMsgCountByEntityId(resourceID1.getStringValue())).andReturn(msgCount);

        org.easymock.EasyMock.expect(resourceProfileService.getProfile(profileID1)).andReturn(null);

   //     org.easymock.EasyMock.expect(
   //         notificationService.findMsgCountByEntityId(resourceID1.getStringValue())).andReturn(
   //         msgCount1);

        org.easymock.EasyMock.replay(resourceInstanceDAO, resourceProfileService,
            notificationService);

        try {
            resourceInstanceService.register(resourceID1, localResourceID1, profileID1);
        } catch (ResourceProfileDoesNotExist e) {

        }

        org.easymock.EasyMock.verify(resourceInstanceDAO, resourceProfileService,
            notificationService);
    }

    @Test
    public void testGetListOneResource() {

        ArrayList<ResourceInstanceModel> list = new ArrayList<ResourceInstanceModel>();
        list.add(new ResourceInstanceModel());

        org.easymock.EasyMock.reset(resourceInstanceDAO);
        org.easymock.EasyMock.expect(resourceInstanceDAO.findAll()).andReturn(list);

        org.easymock.EasyMock.replay(resourceInstanceDAO);

  //      org.easymock.EasyMock.expect(
  //          notificationService.findMsgCountByEntityId(resourceID1.getStringValue())).andReturn(
  //          msgCount1);

        ResourceInstanceListType resourceList = resourceInstanceService.getResourceInstanceList("");
        assertNotNull(resourceList);
        assertEquals(1, resourceList.sizeOfResourceInstanceArray());

        org.easymock.EasyMock.verify(resourceInstanceDAO);
    }

    // Capture<Document> capture = new Capture<Document>();
    // processor.process(and(isA(Document.class), capture(capture)));
    // replay(processor);
    //
    // Document captured = capture.getValue();

    @Test
    public void testGetListMultipleResources() {

        ArrayList<ResourceInstanceModel> list = new ArrayList<ResourceInstanceModel>();
        list.add(createResourceInstance(resourceID1));
        list.add(createResourceInstance(resourceID2));
        list.add(createResourceInstance(resourceID3));

        org.easymock.EasyMock.reset(resourceInstanceDAO, notificationService);
        org.easymock.EasyMock.expect(resourceInstanceDAO.findAll()).andReturn(list);

        org.easymock.EasyMock.expect(
            notificationService.findMsgCountByEntityId(resourceID1.getStringValue())).andReturn(
            msgCount1);
        org.easymock.EasyMock.expect(
            notificationService.findMsgCountByEntityId(resourceID2.getStringValue())).andReturn(
            msgCount2);
        org.easymock.EasyMock.expect(
            notificationService.findMsgCountByEntityId(resourceID3.getStringValue())).andReturn(
            msgCount3);

        org.easymock.EasyMock.replay(resourceInstanceDAO, notificationService);

        ResourceInstanceListType resourceList = resourceInstanceService.getResourceInstanceList("");
        assertNotNull(resourceList);
        assertEquals(3, resourceList.sizeOfResourceInstanceArray());

        checkNotificationCounts(resourceList);

        org.easymock.EasyMock.verify(resourceInstanceDAO);
    }

    private void checkNotificationCounts(ResourceInstanceListType resourceList) {

        for (ResourceInstance ri : resourceList.getResourceInstanceArray()) {
            assertNotNull("no endpoints element", ri.getEndpoints());
            assertEquals("no endpoints", 1, ri.getEndpoints().sizeOfEndpointArray());
            EndpointReferenceType ep = ri.getEndpoints().getEndpointArray(0);
            XmlObject attr = ep.selectAttribute(ServiceNamespaces.NS_NotificationService,
                "notificationCount");
            assertNotNull("no attr", attr);
            
            
            if (resourceID1.getStringValue().equals(ri.getID().getStringValue())) {
                assertEquals("wrong # for 1", msgCount1.toString(), XmlUtil.getTextFromAny(attr));
            } else if (resourceID2.getStringValue().equals(ri.getID().getStringValue())) {
                assertEquals("wrong # for 2", msgCount2.toString(), XmlUtil.getTextFromAny(attr));
            } else if (resourceID3.getStringValue().equals(ri.getID().getStringValue())) {
                assertEquals("wrong # for 3", msgCount3.toString(), XmlUtil.getTextFromAny(attr));
            }
            
        }

    }

    @Test
    public void testUpdateEndpoint() {

        ResourceInstanceModel resourceModel = createResourceInstance(resourceID1);

        org.easymock.EasyMock.reset(resourceInstanceDAO, notificationService);
        org.easymock.EasyMock.expect(
            resourceInstanceDAO.findByIdentifier(org.easymock.EasyMock.isA(String.class))).andReturn(
            resourceModel);

        notificationService.updateEndpoint(resourceID1.getStringValue(), ENDPOINT_UPDATE, true);

        org.easymock.EasyMock.replay(resourceInstanceDAO, notificationService);

        assertTrue(resourceInstanceService.updateEndpoint(resourceID1, ENDPOINT_UPDATE, true));
        assertEquals(1, resourceModel.getEndpoints().size());
        assertTrue(resourceModel.getEndpoints().contains(ENDPOINT_UPDATE));
        org.easymock.EasyMock.verify(resourceInstanceDAO, notificationService);

    }
}
