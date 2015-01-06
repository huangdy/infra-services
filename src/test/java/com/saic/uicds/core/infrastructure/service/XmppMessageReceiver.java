package com.saic.uicds.core.infrastructure.service;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import com.saic.uicds.core.infrastructure.messages.ShareInterestGroupMessage;

public class XmppMessageReceiver {

    Logger log = LoggerFactory.getLogger(XmppMessageReceiver.class);

    private String lastMethod = null;

    public String getLastMethod() {
        return lastMethod;
    }

    public void shareIncidentHandler(ShareInterestGroupMessage message) {
        log.info("shareIncidentHandler called: igID=" + message.getInterestGroupID()
            + " remoteCore=" + message.getRemoteCore());
    }

}
