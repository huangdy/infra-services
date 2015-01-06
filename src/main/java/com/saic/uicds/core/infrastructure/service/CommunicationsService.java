package com.saic.uicds.core.infrastructure.service;

import com.saic.uicds.core.infrastructure.exceptions.EmptyCoreNameListException;
import com.saic.uicds.core.infrastructure.exceptions.LocalCoreNotOnlineException;
import com.saic.uicds.core.infrastructure.exceptions.NoShareAgreementException;
import com.saic.uicds.core.infrastructure.exceptions.NoShareRuleInAgreementException;
import com.saic.uicds.core.infrastructure.exceptions.RemoteCoreUnavailableException;
import com.saic.uicds.core.infrastructure.exceptions.RemoteCoreUnknownException;
import com.saic.uicds.core.infrastructure.messages.Core2CoreMessage;

/**
 * The CommunicationsService interface provides the mechanism for UICDS services to send
 * messages to a remote core.  The messages are handled by the CommunicationsService
 * at the receiver and dispatched to the correct message handler based on message type.
 * 
 * @author Aruna Hau
 * @since 1.0
 * @ssdd
 * 
 */
public interface CommunicationsService {

    public static final String COMMUNICATIONS_SERVICE_NAME = "CommunicationsService";

    public static final String UICDSExplicitAddressScheme = "uicds:user";
    public static final String UICDSCoreAddressScheme = "uicds:core";
    public static final String XMPPAddressScheme = "xmpp";

    public enum CORE2CORE_MESSAGE_TYPE {
        RESOURCE_MESSAGE, BROADCAST_MESSAGE, XMPP_MESSAGE
    }

    /**
     * Sends a message to a remote core. 
     * 
     * @param message The string message
     * @param messageType The type of message
     * @param hostName JID of the receiving core (e.g. uicds@uicds-test1.saic.com)
     * @throws LocalCoreNotOnlineException
     * @throws NoShareRuleInAgreementException
     * @throws NoShareAgreementException
     * @throws EmptyCoreNameListException 
     * @ssdd
     */
    public void sendMessage(String message, CORE2CORE_MESSAGE_TYPE messageType, String hostName)
        throws IllegalArgumentException, RemoteCoreUnknownException,
        RemoteCoreUnavailableException, LocalCoreNotOnlineException, NoShareAgreementException,
        NoShareRuleInAgreementException;

    /**
     * Sends an XMPP message to a specific JID
     * 
     * @param body Standard XMPP Message body text
     * @param xhtml XHTML version of the body (maybe null)
     * @param xml XML version of the body (maybe null);
     */
    public void sendXMPPMessage(String body, String xhtml, String xml, String jid);
    
    /**
     * Handles notifications of a Core to Core message received from a remote core and 
     * dispatches to the correct handler based on message type.
     * 
     * @param message
     * @see Core2CoreMessage
     * @ssdd
     */
    public void core2CoreMessageNotificationHandler(Core2CoreMessage message);

}
