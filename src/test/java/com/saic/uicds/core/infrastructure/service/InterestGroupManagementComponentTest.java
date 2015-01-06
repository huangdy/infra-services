/**
 * 
 */
package com.saic.uicds.core.infrastructure.service;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.uicds.agreementService.AgreementListType;
import org.uicds.agreementService.AgreementType;
import org.uicds.agreementService.AgreementType.ShareRules;
import org.uicds.agreementService.AgreementType.ShareRules.ShareRule;
import org.uicds.agreementService.ConditionType;
import org.uicds.coreConfig.CoreConfigType;
import org.uicds.coreConfig.CoreStatusType;

import com.saic.precis.x2009.x06.base.CodespaceValueType;
import com.saic.uicds.core.infrastructure.dao.InterestGroupDAO;
import com.saic.uicds.core.infrastructure.exceptions.InvalidInterestGroupIDException;
import com.saic.uicds.core.infrastructure.exceptions.LocalCoreNotOnlineException;
import com.saic.uicds.core.infrastructure.exceptions.NoShareAgreementException;
import com.saic.uicds.core.infrastructure.exceptions.NoShareRuleInAgreementException;
import com.saic.uicds.core.infrastructure.exceptions.RemoteCoreUnavailableException;
import com.saic.uicds.core.infrastructure.exceptions.RemoteCoreUnknownException;
import com.saic.uicds.core.infrastructure.exceptions.XMPPComponentException;
import com.saic.uicds.core.infrastructure.model.InterestGroup;
import com.saic.uicds.core.infrastructure.service.impl.InterestGroupManagementComponentImpl;
import com.saic.uicds.core.infrastructure.util.AgreementTestUtils;

/**
 * NotificationServiceTest
 * 
 * @author roger, initial revision
 * 
 */
public class InterestGroupManagementComponentTest
    extends Assert {

    Logger log = LoggerFactory.getLogger(this.getClass());

    InterestGroupManagementComponentImpl interestGroupManager = new InterestGroupManagementComponentImpl();

    AgreementService agreementService;
    DirectoryService directoryService;
    InterestGroupDAO interestGroupDAO;
    ConfigurationService configurationService;
    MessageChannel interestGroupStateNotificationChannel;

    private static final boolean ONLINE = true;
    private static final boolean OFFLINE = false;
    private static final boolean RETURN_NULL = true;
    private static final boolean RETURN_NOT_NULL = false;

    public void addShareRule(ShareRules shareRules, String codespace, String value) {

        ShareRule shareRule = shareRules.addNewShareRule();

        shareRule.addNewWorkProducts().addNewType().setStringValue(AgreementTestUtils.INCIDENT_WPTYPE);
        shareRule.setEnabled(true);
        shareRule.setId(AgreementTestUtils.SHARE_RULE1_ID);
        ConditionType condition = shareRule.addNewCondition();
        CodespaceValueType interestGroup = condition.addNewInterestGroup();
        interestGroup.setCodespace(codespace);
        interestGroup.setStringValue(value);
    }

    @Before
    public void setup() {

        agreementService = EasyMock.createMock(AgreementService.class);
        interestGroupManager.setAgreementService(agreementService);

        directoryService = EasyMock.createMock(DirectoryService.class);
        interestGroupManager.setDirectoryService(directoryService);

        interestGroupDAO = EasyMock.createMock(InterestGroupDAO.class);
        interestGroupManager.setInterestGroupDAO(interestGroupDAO);

        configurationService = org.easymock.EasyMock.createMock(ConfigurationService.class);
        // interestGroupManager.setConfigurationService(configurationService);

        interestGroupStateNotificationChannel = EasyMock.createMock(MessageChannel.class);
        interestGroupManager.setInterestGroupStateNotificationChannel(interestGroupStateNotificationChannel);
    }

    private void setupConfigurationServiceLocalAndRemoteCoreNameRequest(String remoteCoreName) {

        EasyMock.reset(configurationService);

        EasyMock.expect(configurationService.getCoreName()).andReturn(AgreementTestUtils.LOCAL_CORE1);
        if (remoteCoreName != null) {
            EasyMock.expect(configurationService.getCoreName()).andReturn(remoteCoreName);
        }

        EasyMock.replay(configurationService);

    }

    private void setupEmptyAgreement() {

        AgreementListType agreementList = AgreementListType.Factory.newInstance();

        EasyMock.expect(agreementService.getAgreementList()).andReturn(agreementList);

        EasyMock.replay(agreementService);
    }

    private void setupInterestGroupDAOFindByInterestGroup(boolean callMakePersistent) {

        EasyMock.reset(interestGroupDAO);

        InterestGroup interestGroup = new InterestGroup();
        interestGroup.setInterestGroupID(AgreementTestUtils.INTEREST_GROUP_ID);
        interestGroup.setInterestGroupType(AgreementTestUtils.INCIDENT_WPTYPE);
        interestGroup.setInterestGroupSubtype(AgreementTestUtils.INCIDENT_TYPE);
        EasyMock.expect(interestGroupDAO.findByInterestGroup(EasyMock.isA(String.class))).andReturn(interestGroup);

        if (callMakePersistent) {
            EasyMock.expect(interestGroupDAO.makePersistent(EasyMock.isA(InterestGroup.class))).andReturn(interestGroup);
        }

        EasyMock.replay(interestGroupDAO);

    }

    private void setupLocalAndRemoteCoreOnline(String remoteCoreName,
                                               boolean online,
                                               boolean nullReturn) {

        EasyMock.reset(directoryService);

        CoreConfigType localCoreConfig = CoreConfigType.Factory.newInstance();
        localCoreConfig.setLocalCore(true);
        localCoreConfig.setName(AgreementTestUtils.LOCAL_CORE1);
        localCoreConfig.setOnlineStatus(CoreStatusType.ONLINE);

        EasyMock.expect(directoryService.getCoreConfig(EasyMock.isA(String.class))).andReturn(localCoreConfig);

        if (remoteCoreName != null) {
            CoreConfigType remoteCoreConfig = CoreConfigType.Factory.newInstance();
            remoteCoreConfig.setLocalCore(true);
            remoteCoreConfig.setName(remoteCoreName);
            if (online) {
                remoteCoreConfig.setOnlineStatus(CoreStatusType.ONLINE);
            } else {
                remoteCoreConfig.setOnlineStatus(CoreStatusType.OFFLINE);
            }

            if (nullReturn) {
                EasyMock.expect(directoryService.getCoreConfig(EasyMock.isA(String.class))).andReturn(null);
            } else {
                EasyMock.expect(directoryService.getCoreConfig(EasyMock.isA(String.class))).andReturn(remoteCoreConfig);

            }

        }

        EasyMock.replay(directoryService);

    }

    private void setupManualAgreement() {

        EasyMock.reset(agreementService);

        AgreementType agreement = AgreementTestUtils.createAgreement();
        ShareRules shareRules = agreement.addNewShareRules();
        shareRules.setEnabled(true);
        ShareRule shareRule = shareRules.addNewShareRule();

        shareRule.addNewWorkProducts().addNewType().setStringValue(AgreementTestUtils.INCIDENT_WPTYPE);
        shareRule.setEnabled(true);
        shareRule.setId(AgreementTestUtils.SHARE_RULE1_ID);
        ConditionType condition = shareRule.addNewCondition();
        CodespaceValueType interestGroup = condition.addNewInterestGroup();
        interestGroup.setCodespace(InterestGroupManagementComponent.MANUAL_CODE_SPACE);
        interestGroup.setStringValue(Boolean.TRUE.toString());

        AgreementListType agreementList = AgreementListType.Factory.newInstance();
        agreementList.addNewAgreement().set(agreement);

        EasyMock.expect(agreementService.getAgreementList()).andReturn(agreementList);

        EasyMock.replay(agreementService);
    }

    private void setupMessageChannelSend() {

        EasyMock.reset(interestGroupStateNotificationChannel);

        EasyMock.expect(interestGroupStateNotificationChannel.send(EasyMock.isA(Message.class))).andReturn(true);

        EasyMock.replay(interestGroupStateNotificationChannel);

    }

    private void setupOneRuleAgreement() {

        AgreementType agreement = AgreementTestUtils.createAgreement();
        ShareRules shareRules = agreement.addNewShareRules();
        shareRules.setEnabled(true);
        addShareRule(shareRules,
                     InterestGroupManagementComponent.CodeSpace +
                         AgreementTestUtils.INCIDENT_WPTYPE,
                     AgreementTestUtils.INCIDENT_TYPE);

        AgreementListType agreementList = AgreementListType.Factory.newInstance();
        agreementList.addNewAgreement().set(agreement);

        EasyMock.expect(agreementService.getAgreementList()).andReturn(agreementList);

        EasyMock.replay(agreementService);

    }

    private void setupOneRuleAgreementNoMatch() {

        AgreementType agreement = AgreementTestUtils.createAgreement();
        ShareRules shareRules = agreement.addNewShareRules();
        shareRules.setEnabled(true);
        addShareRule(shareRules, InterestGroupManagementComponent.CodeSpace +
                                 AgreementTestUtils.INCIDENT_WPTYPE, "NOT_MATCH");

        AgreementListType agreementList = AgreementListType.Factory.newInstance();
        agreementList.addNewAgreement().set(agreement);

        EasyMock.expect(agreementService.getAgreementList()).andReturn(agreementList);

        EasyMock.replay(agreementService);

    }

    @Test(expected = RemoteCoreUnavailableException.class)
    public void testCoreOffline() throws InvalidInterestGroupIDException,
        LocalCoreNotOnlineException, RemoteCoreUnavailableException, RemoteCoreUnknownException,
        XMPPComponentException, NoShareAgreementException, NoShareRuleInAgreementException {

        setupOneRuleAgreement();
        setupInterestGroupDAOFindByInterestGroup(true);
        setupConfigurationServiceLocalAndRemoteCoreNameRequest(AgreementTestUtils.REMOTE_CORE1);
        setupLocalAndRemoteCoreOnline(AgreementTestUtils.REMOTE_CORE1, OFFLINE, RETURN_NOT_NULL);
        setupMessageChannelSend();

        interestGroupManager.shareInterestGroup(AgreementTestUtils.INTEREST_GROUP_ID,
                                                AgreementTestUtils.REMOTE_CORE1,
                                                "detailed info",
                                                false);

        EasyMock.verify(agreementService, interestGroupDAO, interestGroupStateNotificationChannel);
    }

    @Test
    public void testMatchingRule() throws InvalidInterestGroupIDException,
        LocalCoreNotOnlineException, RemoteCoreUnavailableException, RemoteCoreUnknownException,
        XMPPComponentException, NoShareAgreementException, NoShareRuleInAgreementException {

        setupOneRuleAgreement();
        setupInterestGroupDAOFindByInterestGroup(true);
        setupConfigurationServiceLocalAndRemoteCoreNameRequest(AgreementTestUtils.REMOTE_CORE1);
        setupLocalAndRemoteCoreOnline(AgreementTestUtils.REMOTE_CORE1, ONLINE, RETURN_NOT_NULL);
        setupMessageChannelSend();

        interestGroupManager.shareInterestGroup(AgreementTestUtils.INTEREST_GROUP_ID,
                                                AgreementTestUtils.REMOTE_CORE1,
                                                "detailed info",
                                                false);

        EasyMock.verify(agreementService, interestGroupDAO, interestGroupStateNotificationChannel);
    }

    @Test(expected = NoShareRuleInAgreementException.class)
    public void testNoMatchingRule() throws InvalidInterestGroupIDException,
        LocalCoreNotOnlineException, RemoteCoreUnavailableException, RemoteCoreUnknownException,
        XMPPComponentException, NoShareAgreementException, NoShareRuleInAgreementException {

        setupOneRuleAgreementNoMatch();
        setupInterestGroupDAOFindByInterestGroup(true);
        setupConfigurationServiceLocalAndRemoteCoreNameRequest(AgreementTestUtils.REMOTE_CORE1);
        setupLocalAndRemoteCoreOnline(AgreementTestUtils.REMOTE_CORE1, ONLINE, RETURN_NOT_NULL);
        setupMessageChannelSend();

        interestGroupManager.shareInterestGroup(AgreementTestUtils.INTEREST_GROUP_ID,
                                                AgreementTestUtils.REMOTE_CORE1,
                                                "detailed info",
                                                false);

        EasyMock.verify(agreementService, interestGroupDAO, interestGroupStateNotificationChannel);
    }

    @Test
    public void testServiceCreate() {

        assertNotNull(interestGroupManager);
    }

    @Test
    public void testShareInterestManual() throws InvalidInterestGroupIDException,
        LocalCoreNotOnlineException, RemoteCoreUnavailableException, RemoteCoreUnknownException,
        XMPPComponentException, NoShareAgreementException, NoShareRuleInAgreementException {

        setupManualAgreement();
        setupInterestGroupDAOFindByInterestGroup(true);
        setupConfigurationServiceLocalAndRemoteCoreNameRequest(AgreementTestUtils.REMOTE_CORE1);
        setupLocalAndRemoteCoreOnline(AgreementTestUtils.REMOTE_CORE1, true, false);
        setupMessageChannelSend();

        interestGroupManager.shareInterestGroup(AgreementTestUtils.INTEREST_GROUP_ID,
                                                AgreementTestUtils.REMOTE_CORE1,
                                                "detailed info",
                                                false);

        EasyMock.verify(agreementService, interestGroupDAO, interestGroupStateNotificationChannel);
    }

    @Test(expected = RemoteCoreUnknownException.class)
    public void testWithNoAgreement() throws InvalidInterestGroupIDException,
        LocalCoreNotOnlineException, RemoteCoreUnavailableException, RemoteCoreUnknownException,
        XMPPComponentException, NoShareAgreementException, NoShareRuleInAgreementException {

        setupEmptyAgreement();
        setupInterestGroupDAOFindByInterestGroup(true);
        setupConfigurationServiceLocalAndRemoteCoreNameRequest(null);
        setupLocalAndRemoteCoreOnline(AgreementTestUtils.UKNOWN_REMOTE_CORE, OFFLINE, RETURN_NULL);
        setupMessageChannelSend();

        interestGroupManager.shareInterestGroup(AgreementTestUtils.INTEREST_GROUP_ID,
                                                AgreementTestUtils.UKNOWN_REMOTE_CORE,
                                                "detailed info",
                                                false);

        EasyMock.verify(agreementService, interestGroupDAO, interestGroupStateNotificationChannel);

    }
}
