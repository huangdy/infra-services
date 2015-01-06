/**
 * 
 */
package com.saic.uicds.core.infrastructure.service;

import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.junit.Ignore;
import org.junit.Test;
import org.oasisOpen.docs.wsn.b2.QueryExpressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessageChannel;
import org.springframework.test.jpa.AbstractJpaTests;
import org.uicds.resourceProfileService.CreateProfileRequestDocument;
import org.uicds.resourceProfileService.Interest;
import org.uicds.resourceProfileService.ResourceProfile;
import org.uicds.resourceProfileService.ResourceProfileListType;
import org.uicds.resourceProfileService.ResourceProfile.Interests;
import org.uicds.resourceProfileService.ResourceProfile.ResourceTyping;

import com.saic.precis.x2009.x06.base.CodespaceValueType;
import com.saic.precis.x2009.x06.base.IdentifierType;
import com.saic.precis.x2009.x06.base.NamespaceMapItemType;
import com.saic.precis.x2009.x06.base.NamespaceMapType;
import com.saic.uicds.core.infrastructure.dao.ResourceProfileDAO;
import com.saic.uicds.core.infrastructure.model.CodeSpaceValueType;
import com.saic.uicds.core.infrastructure.model.InterestElement;
import com.saic.uicds.core.infrastructure.model.ResourceProfileModel;
import com.saic.uicds.core.infrastructure.service.impl.ConfigurationServiceImpl;
import com.saic.uicds.core.infrastructure.service.impl.ResourceProfileServiceImpl;
import com.saic.uicds.core.infrastructure.util.ResourceProfileUtil;

/**
 * @author Andre Bonner
 * 
 */

//@Ignore
public class ResourceProfileServiceTest extends AbstractJpaTests {

    Logger log = LoggerFactory.getLogger(ResourceProfileServiceTest.class);

    ResourceProfileServiceImpl profileService = new ResourceProfileServiceImpl();

    // No need to mock this object's behavior since it just returns constants
    ConfigurationService configurationService = new ConfigurationServiceImpl();

    // Mock objects
    NotificationService notificationService;
    ResourceProfileDAO profileDAO;
    MessageChannel profileNotificationChannel;

    ResourceProfileModel profile;
    ResourceProfileModel profile2;

    static String coreName = "core1.saic.com";
    static String personFullName = "Joe";
    static String personFullName2 = "Bob";
    static public String personEntityID = "joe@" + coreName;
    static public String personEntityID2 = "bob@" + coreName;
    static public IdentifierType profileID1;

    static String notificationURI = "http://" + coreName + "/" + personEntityID + "/notification";
    static public String incidentID = "some-uuid";
    static String incidentName = "fire";
    static String workProductID = "workProductID";
    static String OLD_DESCRIPTION = "OLD DESCRIPTION";
    static String NEW_DESCRIPTION = "NEW DESCRIPTION";
    static String DESCRIPTION2 = "OTHER DESCRIPTION";

    static String WSN_XPATH_DIALECT = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    static String WSN_SIMPLE_TOPIC = "http://docs.oasis-open.org/wsn/2004/06/TopicExpression/Simple";

    @Override
    public void onSetUp() {

        profileID1 = IdentifierType.Factory.newInstance();
        profileID1.setStringValue(personEntityID);
        profileID1.setLabel(personFullName);

        if (profileDAO == null) {
            profileDAO = org.easymock.EasyMock.createMock(ResourceProfileDAO.class);
            profileService.setResourceProfileDAO(profileDAO);
        }

        if (profile == null) {
            profile = new ResourceProfileModel();
            profile.setIdentifier(personEntityID);
            profile.setLabel(personFullName);
            profile.setDescription(OLD_DESCRIPTION);
            InterestElement i = new InterestElement();
            i.setMessageContent("Interest");
            profile.addInterest(i);
            
            HashSet<CodeSpaceValueType> cvts = new HashSet<CodeSpaceValueType>();   
            CodeSpaceValueType cvt1 = new CodeSpaceValueType();
            cvt1.setCodeSpace("c1");
            cvt1.setLabel("l1");
            cvt1.setValue("v1");

            cvts.add(cvt1);

            CodeSpaceValueType cvt2 = new CodeSpaceValueType();
            cvt2.setCodeSpace("c2");
            cvt2.setLabel("l2");
            cvt2.setValue("v2");
            cvts.add(cvt2);
            profile.setCvts(cvts);
            
         //   profile.addResourceTyping("c1,l1", "v1");
         //   profile.addResourceTyping("c1,l2", "v2");
        }

        if (profile2 == null) {
            profile2 = new ResourceProfileModel();
            profile2.setIdentifier(personEntityID2);
            profile2.setLabel(personFullName2);
            profile2.setDescription(DESCRIPTION2);
        }
    }

    @Test
    public void testCreateProfile() {

        // Create a profile
        ResourceProfile response = createProfile();
        // System.out.println(response);

        // Check response
        checkInitialProfile(response);
        checkCreateProfile();
    }

    private void checkCreateProfile() {
        org.easymock.EasyMock.verify(profileDAO);
    }

    private ResourceProfile createProfile() {

        // reset the profileDAO object from the last usage
        org.easymock.EasyMock.reset(profileDAO);

        // expect two calls to this
        org.easymock.EasyMock.expect(
            profileDAO.makePersistent(org.easymock.EasyMock.isA(ResourceProfileModel.class))).andReturn(
            profile);
        // org.easymock.EasyMock.expect(
        // profileDAO.makePersistent(org.easymock.EasyMock.isA(ResourceProfileModel.class)))
        // .andReturn(profile);

        org.easymock.EasyMock.expect(
            profileDAO.findByIdentifier(org.easymock.EasyMock.isA(String.class))).andReturn(profile).anyTimes();

        org.easymock.EasyMock.replay(profileDAO);

        // Invoke the test
        CreateProfileRequestDocument request = getProfileCreateRequest(true);

        ResourceProfileModel profileModel = profileService.createProfile(request.getCreateProfileRequest().getProfile());

        ResourceProfile response = ResourceProfileUtil.copyProperties(profileModel);

        return response;
    }

    @Test
    public void testGetProfile() {
        ResourceProfileModel profileModel = getProfile();

        ResourceProfile profile = ResourceProfileUtil.copyProperties(profileModel);
        checkInitialProfile(profile);
    }

    private ResourceProfileModel getProfile() {
        org.easymock.EasyMock.reset(profileDAO);
        org.easymock.EasyMock.expect(
            profileDAO.findByIdentifier(org.easymock.EasyMock.isA(String.class))).andReturn(profile).anyTimes();

        org.easymock.EasyMock.replay(profileDAO);

        ResourceProfileModel profileModel = profileService.getProfile(profileID1);
        ResourceProfile p = ResourceProfileUtil.copyProperties(profileModel);
        assertNotNull(p);
        org.easymock.EasyMock.verify(profileDAO);
        return profileModel;
    }

    private ResourceProfileModel getProfileCopy() {
        ResourceProfileModel newProfile = new ResourceProfileModel();
        newProfile.setDescription(profile.getDescription());
        newProfile.setId(profile.getId());
        newProfile.setInterests(profile.getInterests());
        newProfile.setIdentifier(profile.getIdentifier());
        newProfile.setCvts(profile.getCvts());
        return newProfile;
    }

    @Test
    public void testUpdateProfile() {
        ResourceProfileModel profileModel = getProfileCopy();
        assertNotNull(profileModel);

        profileModel.setDescription(NEW_DESCRIPTION);

        org.easymock.EasyMock.reset(profileDAO);
        org.easymock.EasyMock.expect(
            profileDAO.findByIdentifier(org.easymock.EasyMock.isA(String.class))).andReturn(profile).anyTimes();

        ResourceProfile profileUpdate = ResourceProfileUtil.copyProperties(profileModel);

        org.easymock.EasyMock.expect(
            profileDAO.makePersistent(org.easymock.EasyMock.isA(ResourceProfileModel.class))).andReturn(
            profileModel);

        assertNotSame(profile, profileModel);

        org.easymock.EasyMock.replay(profileDAO);

        ResourceProfileModel newProfileModel = profileService.updateProfile(profileUpdate);
        assertNotNull(newProfileModel);

        org.easymock.EasyMock.verify(profileDAO);

        ResourceProfile newProfile = ResourceProfileUtil.copyProperties(newProfileModel);
        checkUpdatedProfile(newProfile);
    }

    @Test
    public void testGetList() {
        ResourceProfileListType list;
        checkProfileListSize(0);

        // reset the profileDAO object from the last usage
        org.easymock.EasyMock.reset(profileDAO);

        // setup the new expectations for the profileDAO object
        ArrayList<ResourceProfileModel> profiles = new ArrayList<ResourceProfileModel>();
        profiles.add(profile);
        profiles.add(profile2);
        org.easymock.EasyMock.expect(profileDAO.findAll()).andReturn(profiles);
        org.easymock.EasyMock.replay(profileDAO);

        // Invoke the test
        list = profileService.getProfileList("");

        // Check results
        assertNotNull(list);
        assertFalse(list.isNil());
        assertTrue("expected 2 but size is: " + list.sizeOfResourceProfileArray(),
            list.sizeOfResourceProfileArray() == 2);
        org.easymock.EasyMock.verify(profileDAO);
    }

    private void checkProfileListSize(int size) {
        // reset the profileDAO object from the last usage
        org.easymock.EasyMock.reset(profileDAO);

        // setup the new expectations for the profileDAO object
        org.easymock.EasyMock.expect(profileDAO.findAll()).andReturn(null);
        org.easymock.EasyMock.replay(profileDAO);

        ResourceProfileListType list = profileService.getProfileList("");
        assertFalse(list.isNil());
        assertTrue("size is: " + list.sizeOfResourceProfileArray(),
            list.sizeOfResourceProfileArray() == size);

        org.easymock.EasyMock.verify(profileDAO);
    }

    static public CreateProfileRequestDocument getProfileCreateRequest(boolean addTopicQuery) {
        CreateProfileRequestDocument request = CreateProfileRequestDocument.Factory.newInstance();

        ResourceProfile profile = request.addNewCreateProfileRequest().addNewProfile();
        profile.addNewID().setStringValue(personEntityID);

        Interests interests = profile.addNewInterests();
        Interest interest = interests.addNewInterest();

        QName topicExpression = new QName("Incident");
        interest.setTopicExpression(topicExpression.toString());

        if (addTopicQuery) {
            QueryExpressionType query = interest.addNewMessageContent();
            query.setDialect(WSN_XPATH_DIALECT);
            XmlCursor queryCursor = query.newCursor();
            queryCursor.toNextToken();
            queryCursor.toNextToken();
            queryCursor.insertChars("//stuff");
            queryCursor.dispose();

            NamespaceMapType map = interest.addNewNamespaceMap();
            NamespaceMapItemType item = map.addNewItem();
            item.setPrefix("de");
            item.setURI("urn:oasis:names:tc:emergency:EDXL:DE:1.0");

            item = map.addNewItem();
            item.setPrefix("cap");
            item.setURI("urn:oasis:names:tc:emergency:cap:1.1");
        }

        ResourceTyping types = profile.addNewResourceTyping();
        CodespaceValueType c1 = types.addNewType();
        c1.setCodespace("c1");
        c1.setLabel("l1");
        c1.setStringValue("v1");

        CodespaceValueType c2 = types.addNewType();
        c2.setCodespace("c1");
        c2.setLabel("l2");
        c2.setStringValue("v2");

        // log.debug(request);
        return request;
    }

    static public void checkInitialProfile(ResourceProfile profile) {
        assertNotNull(profile);
        assertEquals(personEntityID, profile.getID().getStringValue());
    }

    static public void checkUpdatedProfile(ResourceProfile profile) {
        assertNotNull(profile);
        assertEquals(personEntityID, profile.getID().getStringValue());
        assertEquals(NEW_DESCRIPTION, profile.getDescription());
    }

}
