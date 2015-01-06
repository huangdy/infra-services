/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.saic.uicds.core.infrastructure.service;

import com.saic.uicds.core.infrastructure.dao.UserInterestGroupDAO;
import com.saic.uicds.core.infrastructure.messages.DisseminationManagerMessage;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vmuser
 */
public class DisseminationManagerComponent {


     /** The logger. */
    Logger logger = LoggerFactory.getLogger(DisseminationManagerComponent.class);


    
    private UserInterestGroupDAO userInterestGroupDAO;

    public UserInterestGroupDAO getUserInterestGroupDAO() {
    
        return userInterestGroupDAO;
    }


    public void setUserInterestGroupDAO(UserInterestGroupDAO userInterestGroupDAO) {
    
        this.userInterestGroupDAO = userInterestGroupDAO;
    }

    
    /**
     *
     * handle message on DissMgr channel for add/remove users and groups
     * message origin from AutoShare.
     */
    public void disseminationManagerMessageHandler(DisseminationManagerMessage message) {
        logger.debug("Received Dissemination Manager Message\n"
                + "\tIGID: " + message.getInterestGroupID() + "\n"
                + "\tADD: " + printArrayList(message.getJidsToAdd()) + "\n"
                + "\tREMOVE: " + printArrayList(message.getJidsToRemove()) + "\n");

        String igid = message.getInterestGroupID();
        // make sure there is an IG id first...
        if (igid != null) {

            // process the jids to add
            if (! message.getJidsToAdd().isEmpty() ) {
                for (String jid : message.getJidsToAdd()) {
                    logger.debug("Adding user " + jid + " to IG " + igid);
                    this.addJID(igid, jid);
                }
            }

            // process the jids to remove
            if (! message.getJidsToRemove().isEmpty() ) {
                for (String jid : message.getJidsToRemove()) {
                    logger.debug("Removing user " + jid + " from IG " + igid);
                    this.removeJID(igid, jid);
                }
            }

            // TODO: process groups add/remove
            // TODO: dynamic lookup?  move to queue manager?
        } else {
            logger.info("No interest group ID was found");
        }
    }


    // add a single JID to the specified interest group
    public void addJID(String igid, String jid) {
        
        logger.debug("addJID: jid: " + jid + ", IGID: " + igid);
        getUserInterestGroupDAO().addUser(jid, igid);
        
        /*
        ArrayList value = manager.get(igid);
        // if the interest group is already in the map, add the jid to its jid list
        if (value != null) {
            value.add(jid);
        }
        // otherwise, create a new entry
        else {
            ArrayList jids = new ArrayList();
            jids.add(jid);
            manager.put(igid, jids);
            // notificationService.addUser(igid, jid);
        }
        */
    }


    // remove a single JID from the specified interest group
    public void removeJID(String igid, String jid) {
        
        logger.debug("removeJID: jid" + jid + ", IGID: " +igid);
        getUserInterestGroupDAO().removeUser(jid, igid);
        
        /*
        ArrayList value = manager.get(igid);
        // if the igid exists, try to remove the jid
        if (value != null) {
            value.remove(jid);
            // if the jid list is empty, remove the igid from the dissemination manager
            if (value.isEmpty()) {
                manager.remove(igid);
                // notificationService.RemoveUser(igid, jid);
            }
        }
        */
    }


    /**
     * System initialized handler.
     *
     * @param message the message
     */
     public void systemInitializedHandler(String message) {
        // No need to do anything yet
        logger.info("Dissemination Manager - initialized");

        // TODO: check database (disseminationManagerDAO)for existing map on startup and recreate manager list
    }

    // utility for printing array lists
    private String printArrayList(ArrayList<String> list) {
        String listString = "";
        for (String s : list)
        {
            listString += s + " ";
        }
        return listString;
    }
}
