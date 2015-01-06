package com.saic.uicds.core.infrastructure.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType;
import org.oasisOpen.docs.wsn.b2.NotificationMessageHolderType.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.jpa.AbstractJpaTests;
import org.uicds.notificationService.GetCurrentMessageRequestDocument;
import org.uicds.notificationService.GetCurrentMessageResponseDocument;
import org.uicds.notificationService.GetMessagesRequestDocument;
import org.uicds.notificationService.GetMessagesResponseDocument;

import com.saic.uicds.core.infrastructure.endpoint.NotificationServiceEndpoint;
import com.saic.uicds.core.infrastructure.service.NotificationService;

public class NotificationServiceEndpointTest extends AbstractJpaTests {

    Logger log = LoggerFactory.getLogger(getClass());

    NotificationServiceEndpoint notificationServiceEndpoint = new NotificationServiceEndpoint();

    NotificationService notificationService;

    private static final String getMessagesRequestString = "<wsn:GetMessagesRequest xmlns:wsn=\"http://uicds.org/NotificationService\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" >"
            + "<wsn:MaximumNumber>1</wsn:MaximumNumber> "
            + "<wsa:To>http://localhost:8080/uicds/ws/joe@core1.saic.com</wsa:To> "
            + "</wsn:GetMessagesRequest>";
    private static final String getCurrentALERTMessagesRequestString = "<wsn:GetCurrentMessageRequest xmlns:wsn=\"http://uicds.org/NotificationService\""
            + "xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\" >"
            + "<wsnt:Topic>alert/*</wsnt:Topic>" + "</wsn:GetCurrentMessageRequest>";

    private static final String notifyRequstString = "<ns:NotifyRequest xmlns:ns=\"http://uicds.org/NotificationService\" "
            + "xmlns:p=\"http://docs.oasis-open.org/wsn/b-2\" "
            + "xmlns:tns=\"http://www.w3.org/2005/08/addressing\"> "
            + "<ns:NotificationMessage> <p:Message>NewMessage </p:Message> </ns:NotificationMessage> "
            + "<tns:To>IncidentCommander@core1.saic.com</tns:To> " + "</ns:NotifyRequest>";

    // private static final String getMessagesRequestString =
    // "<wsn:GetMessages xmlns:wsn=\"http://docs.oasis-open.org/wsn/b-2\"  >"
    // + "<wsn:MaximumNumber>1</wsn:MaximumNumber> "
    // +
    // "<wsa:To xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://localhost:8080/uicds/ws/joe@core1.saic.com</wsa:To> "
    // + "</wsn:GetMessages> ";

    // private static final String getMessagesRequestString =
    // "<b:GetMessages xmlns:b=\"http://docs.oasis-open.org/wsn/b-2\">"
    // + "<b:MaximumNumber>1</b:MaximumNumber>"
    // +
    // "<add:To xmlns:add=\"http://www.w3.org/2005/08/addressing\">http://localhost:8080/uicds/ws/joe@core1.saic.com</add:To>"
    // + "</b:GetMessages>";

    @Override
    public void onSetUp() {
        notificationService = org.easymock.EasyMock.createMock(NotificationService.class);
        notificationServiceEndpoint.setNotificationService(notificationService);
    }

    // @Test
    // public void testNotify() {
    // try {
    // NotifyRequestDocument request = NotifyRequestDocument.Factory.parse(notifyRequstString);
    // log.debug("request: " + request);
    // NotificationMessageHolderType[] holder = request.getNotifyRequest()
    // .getNotificationMessageArray();
    //
    // String entityID = "joe@core1.saic.com";
    // int num = 1;
    // // notificationService.notify(entityID, holder);
    // org.easymock.EasyMock.expectLastCall();
    //
    // org.easymock.EasyMock.replay(notificationService);
    //
    // notificationServiceEndpoint.Notify(request);
    //
    // org.easymock.EasyMock.verify(notificationService);
    //
    // } catch (XmlException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

    @Test
    public void testGetMessages() {
        try {
            GetMessagesRequestDocument request = GetMessagesRequestDocument.Factory
                    .parse(getMessagesRequestString);
            log.debug("request: " + request);

            String entityID = "joe@core1.saic.com";
            int num = 1;
            org.easymock.EasyMock.expect(notificationService.getMessages(entityID, num)).andReturn(
                    getMockMessages());
            org.easymock.EasyMock.replay(notificationService);

            GetMessagesResponseDocument response = notificationServiceEndpoint.GetMessages(request);

            if (log.isDebugEnabled()) {
                log.debug(response.toString());
            }

            org.easymock.EasyMock.verify(notificationService);

        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testGetCurrentMessageRequest() throws Exception {
        assertNotNull(notificationServiceEndpoint);

        GetCurrentMessageRequestDocument doc = GetCurrentMessageRequestDocument.Factory
                .newInstance();
        doc.addNewGetCurrentMessageRequest().addNewTopic().setDialect("alert/*");

        assertNotNull(doc);
        GetCurrentMessageResponseDocument response = notificationServiceEndpoint
                .GetCurrentMessage(doc);
        log.debug("testGetCurrentMessage");
    }

    public NotificationMessageHolderType[] getMockMessages() {
        NotificationMessageHolderType[] response = null;

        ArrayList<NotificationMessageHolderType> messages = new ArrayList<NotificationMessageHolderType>();
        List<String> messageList = new ArrayList<String>();
        messageList.add("1");
        messageList.add("2");

        for (String message : messageList) {
            NotificationMessageHolderType t = NotificationMessageHolderType.Factory.newInstance();
            Message m = t.addNewMessage();
            XmlCursor xc = m.newCursor();
            xc.toNextToken();
            xc.insertElementWithText("WorkProduct", message);
            xc.dispose();
            if (log.isDebugEnabled()) {
                log.debug(t.toString());
            }
            messages.add(t);
        }

        response = new NotificationMessageHolderType[messages.size()];
        response = messages.toArray(response);
        // String[] stringArray = (String[])nfhList.toArray(new String[nfhList.size()]);

        // m.set(xmlobject);
        //		
        //		
        // messages.setNotificationMessage(notificationMessage);

        return response;
    }
}
