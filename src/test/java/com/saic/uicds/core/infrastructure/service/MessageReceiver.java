package com.saic.uicds.core.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.saic.uicds.core.infrastructure.messages.ProductChangeNotificationMessage;
import com.saic.uicds.core.infrastructure.messages.ProfileNotificationMessage;

//import org.springframework.integration.annotation.Subscriber;
//import org.springframework.stereotype.Component;

//@Component

public class MessageReceiver {

    Logger log = LoggerFactory.getLogger(MessageReceiver.class);

    public boolean gotIt = false;

    public String name;

    public void setName(String n) {

        name = n;
    }

    private int gotIncidentWPCount = 0;
    private int gotAlertWPCount = 0;
    private int gotMapWPCount = 0;
    private int gotICSWPCount = 0;

    public int getGotIncidentWPCount() {

        return gotIncidentWPCount;
    }

    public void setGotIncidentWPCount(int gotIncidentWPCount) {

        this.gotIncidentWPCount = gotIncidentWPCount;
    }

    public int getGotAlertWPCount() {

        return gotAlertWPCount;
    }

    public void setGotAlertWPCount(int gotAlertWPCount) {

        this.gotAlertWPCount = gotAlertWPCount;
    }

    public int getGotMapWPCount() {

        return gotMapWPCount;
    }

    public void setGotMapWPCount(int gotMapWPCount) {

        this.gotMapWPCount = gotMapWPCount;
    }

    public int getGotICSWPCount() {

        return gotICSWPCount;
    }

    public void setGotICSWPCount(int gotICSWPCount) {

        this.gotICSWPCount = gotICSWPCount;
    }

    // @Subscriber(channel="incidentStateChannel")
    public void handleProductChange(ProfileNotificationMessage notification) {

        if (log.isDebugEnabled()) {
            log.debug(name + " GOT IT: " + notification.getUserID());
        }
        gotIt = true;
    }

}
