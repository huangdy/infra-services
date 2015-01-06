package com.saic.uicds.core.infrastructure.service;

import java.util.List;

import org.uicds.loggingService.GetLogByHostnameRequestDocument;
import org.uicds.loggingService.GetLogByHostnameResponseDocument;
import org.uicds.loggingService.GetLogByLoggerRequestDocument;
import org.uicds.loggingService.GetLogByLoggerResponseDocument;
import org.uicds.loggingService.LogRequestDocument;
import org.uicds.loggingService.LogResponseDocument;

import com.saic.uicds.core.infrastructure.dao.LoggerDAO;
import com.saic.uicds.core.infrastructure.status.Status;
import com.saic.uicds.core.infrastructure.status.StatusEvent;

/**
 * The Logging Service manages the persistence and retrieval of UICDS logs.
 * 
 * @author B. Orledge
 * @ssdd
 * 
 */
public interface LoggingService {

    public static final String LOGGING_SERVICE_NAME = "LoggingService";

    /**
     * Log the information including from which host, who is the logger, what is the category, and
     * the message.
     * 
     * @param request
     * @ssdd
     */
    public LogResponseDocument logRequest(LogRequestDocument request);

    /**
     * Retrieve the log by host name
     * 
     * @param request
     * @ssdd
     */
    public GetLogByHostnameResponseDocument getLogByHostname(GetLogByHostnameRequestDocument request);

    /**
     * Return the log by the log specified
     * 
     * @param request
     * @ssdd
     */
    public GetLogByLoggerResponseDocument getLogByLogger(GetLogByLoggerRequestDocument request);

    /**
     * Getter accessor for data layer
     */
    public LoggerDAO getDao();
    
    public Status getComponentStatus(String id);
    
    public List<Status> getComponentStatus();
    
    public void sendStatusMessage(Status status, StatusEvent event);
    
    public void updatePresence(Status status);

    /**
     * Setter accessor for data layer
     * 
     * @param dao
     */
    public void setDao(LoggerDAO dao);

    /**
     * SystemIntialized Message Handler
     * 
     * @param message SystemInitialized message
     * @see applicationContext
     * @ssdd
     */
    public void systemInitializedHandler(String messgae);

}