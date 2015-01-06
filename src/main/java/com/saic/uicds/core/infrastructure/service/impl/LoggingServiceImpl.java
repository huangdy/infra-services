package com.saic.uicds.core.infrastructure.service.impl;

/**
 * @author B. Orledge
 * @ssdd
 */

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.xml.XMLLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.uicds.coreConfig.CoreConfigListType;
import org.uicds.coreConfig.CoreConfigType;
import org.uicds.directoryServiceData.WorkProductTypeListType;
import org.uicds.loggingService.GetLogByHostnameRequestDocument;
import org.uicds.loggingService.GetLogByHostnameResponseDocument;
import org.uicds.loggingService.GetLogByHostnameResponseDocument.GetLogByHostnameResponse;
import org.uicds.loggingService.GetLogByLoggerRequestDocument;
import org.uicds.loggingService.GetLogByLoggerResponseDocument;
import org.uicds.loggingService.GetLogByLoggerResponseDocument.GetLogByLoggerResponse;
import org.uicds.loggingService.LogLevelType;
import org.uicds.loggingService.LogRequestDocument;
import org.uicds.loggingService.LogResponseDocument;
import org.uicds.loggingService.LogResponseDocument.LogResponse;
import org.uicds.loggingService.LogResponseType;
import org.uicds.loggingService.LogType;

import com.saic.uicds.core.infrastructure.dao.LoggerDAO;
import com.saic.uicds.core.infrastructure.model.Log;
import com.saic.uicds.core.infrastructure.rule.RuleEngine;
import com.saic.uicds.core.infrastructure.service.CommunicationsService;
import com.saic.uicds.core.infrastructure.service.ConfigurationService;
import com.saic.uicds.core.infrastructure.service.DirectoryService;
import com.saic.uicds.core.infrastructure.service.LoggingService;
import com.saic.uicds.core.infrastructure.status.ComponentLogMonitor;
import com.saic.uicds.core.infrastructure.status.Status;
import com.saic.uicds.core.infrastructure.status.StatusEvent;
import com.saic.uicds.core.infrastructure.status.StatusEventListener;
import com.saic.uicds.core.infrastructure.util.ServiceNamespaces;

public class LoggingServiceImpl implements LoggingService, ServiceNamespaces, InitializingBean,
        StatusEventListener {

    Logger logger = LoggerFactory.getLogger(LoggingServiceImpl.class);

    private LoggerDAO loggerDAO;

    private DirectoryService directoryService;

    private CommunicationsService communicationsService;

    private ConfigurationService configurationService;

    private Map<String, ComponentLogMonitor> loggers = new HashMap<String, ComponentLogMonitor>();

    private Map<String, Status> componentStatusMap = new HashMap<String, Status>();
    private Map<String, StatusEvent> componentStatusEventMap = new HashMap<String, StatusEvent>();

    private Map<String, Boolean> componentTimeOut = new HashMap<String, Boolean>();

    public Map<String, Boolean> getComponentTimeOut() {
        return componentTimeOut;
    }

    public void setComponentTimeOut(Map<String, Boolean> componentTimeOut) {
        this.componentTimeOut = componentTimeOut;
    }

    private Map<String, List<String>> componentLogHistory = new HashMap<String, List<String>>();

    private Map<String, org.apache.log4j.Logger> componentLogOutputs = new HashMap<String, org.apache.log4j.Logger>();

    public RuleEngine ruleEngine;
    
    private boolean sendHistory=false;

    public boolean isSendHistory() {
        return sendHistory;
    }

    public void setSendHistory(boolean sendHistory) {
        this.sendHistory = sendHistory;
    }

    private List<String> registeredJidList = new ArrayList<String>();

    public void setRegisteredJidList(List<String> registeredJidList) {
        for (String jid : registeredJidList) {
            this.registeredJidList.add(jid);
        }
    }

    private List<String> localCoreJids=new ArrayList<String>();

    private String localCoreJid;

    public String getLocalCoreJid() {
        return localCoreJid;
    }

    public List<String> getLocalCoreJids() {
        return localCoreJids;
    }

    Status normalStatus = new Status("NORMAL", "NORMAL");

    private String consoleResource;

    private File eacUsersFile;

    public Map<String, List<String>> getComponentLogHistory() {
        return componentLogHistory;
    }

    public void setComponentLogHistory(Map<String, List<String>> componentLogHistory) {
        this.componentLogHistory = componentLogHistory;
    }

    public File getEacUsersFile() {
        return eacUsersFile;
    }

    public void setEacUsersFile(File eacUsersFile) {
        this.eacUsersFile = eacUsersFile;
    }

    public String getConsoleResource() {
        return consoleResource;
    }

    public void setConsoleResource(String consoleResource) {
        this.consoleResource = consoleResource;
    }

    public boolean isLogToFile() {
        return logToFile;
    }

    public void setLogToFile(boolean logToFile) {
        this.logToFile = logToFile;
    }

    private Map<String, Status> coreStatusMap = new HashMap<String, Status>();

    private boolean logToFile = false;

    public void setLocalCoreJid(String localCoreJid) {
        List<String> jids=this.localCoreJids;
        this.localCoreJids.add(localCoreJid);
        this.localCoreJid=localCoreJid;
        this.directoryService.setLocalCoreJid(localCoreJid);
        this.directoryService.setConsoleResource(getConsoleResource());
        initializeStatus();
        sendComponentXmppMessage(jids);
        notifyOtherCores();
        if(this.localCoreJids.size()>1)
            sharedCoreUpdateToConsole();
    }

    private void sharedCoreUpdateToConsole() {
        String xmppMessage = "Shared-CoreStatus:"; 
        if (getCommunicationsService() != null) {
            String jid=getConfigurationService().getCoreName()+"/"+getConsoleResource();
            getCommunicationsService().sendXMPPMessage(xmppMessage, "message", "message",
                    jid);
        }
    }

    private void sendComponentXmppMessage(List<String> jids) {
        for(String compId:componentStatusMap.keySet()){
            StatusEvent event=componentStatusEventMap.get(compId);
            if(event!=null){
                Status status=componentStatusMap.get(compId);
                String xmppMessage = "CompenentId: [" + event.getComponentId() + "]\n";
                xmppMessage += "Status: [" + status.getCategory() + "]\n";
                xmppMessage += "TimeStamp: [" + event.getTimestamp() + "]\n";
                xmppMessage += "LogMessage:[" + event.getMessage() + "]\n";
                if (getCommunicationsService() != null) {
                    for(String localCoreJid:jids)
                        getCommunicationsService().sendXMPPMessage(xmppMessage, "message", "message",
                                localCoreJid);
                }
            }
        }
    }

    private void initializeStatus() {
        initializeComponentStatus();
        initializeCoreStatus();
        directoryService.setOverallCoreStatus("NORMAL");
    }

    private void initializeComponentStatus() {
        for (String componentId : componentStatusMap.keySet()) {
            componentStatusMap.put(componentId, normalStatus);
        }
    }

    private void notifyOtherCores() {
        if (directoryService != null) {
            CoreConfigListType cores = directoryService.getCoreList();
            CoreConfigType[] coreTypes = cores.getCoreArray();
            String localCoreName = getConfigurationService().getCoreName();
            for (CoreConfigType core : coreTypes) {
                String online = core.getOnlineStatus().toString();
                if (online.equalsIgnoreCase("online")) {
                    String coreJid = core.getName();
                    if (!coreJid.equalsIgnoreCase(localCoreName)) {
                        sendCoreStatus(coreJid, online);
                    }
                }
            }
        }
    }

    private void sendCoreStatus(String coreName, String coreStatus) {
        if (coreStatus.equalsIgnoreCase("online")) {
            String jid = this.directoryService.getCoreName();
            String message = "Remote-CoreStatus: [" + jid + "]\n";
            String status = getCurrentCoreStatus(coreName).getCategory();
            message += "Status: [" + status + "]";
            String resource = getConsoleResource();
            if (resource != null) {
                getCommunicationsService().sendXMPPMessage(message, "message", "message",
                        coreName + "/" + resource);
            }
        }
    }

    public List<String> getRegisteredJidList() {
        return registeredJidList;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (String key : loggers.keySet()) {
            ComponentLogMonitor monitor = loggers.get(key);
            monitor.addListener(this); // add StatusListener to LogMonitor to handleStatus
            componentTimeOut.put(key, true);
            componentLogHistory.put(key, new ArrayList<String>());
        }
        ruleEngine.addListener(this); // add StatusListener to RulesEngine to handleStatus
        initializeCoreStatus();
        buildRegisteredUsers();
    }

    private void initializeCoreStatus() {
        if (this.directoryService != null) {
            CoreConfigListType cores = directoryService.getCoreList();
            CoreConfigType[] coreTypes = cores.getCoreArray();
            String localCoreName = getConfigurationService().getCoreName();
            for (CoreConfigType core : coreTypes) {
                String coreJid = core.getName();
                if (!coreJid.equalsIgnoreCase(localCoreName)) {
                    coreStatusMap.put(coreJid, normalStatus);
                }
            }
        }
    }

    public void buildRegisteredUsers() {
        File file = getEacUsersFile();
        if (file != null) {
            if (file.exists()) {
                try {
                    FileInputStream fis = null;
                    BufferedInputStream bis = null;
                    DataInputStream dis = null;
                    try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    dis = new DataInputStream(bis);
                    while (dis.available() != 0) {
                        @SuppressWarnings("deprecation")
                        String jid = dis.readLine();
                        jid = jid.trim();
                        if (jid.contains("@"))
                            this.registeredJidList.add(jid);
                    }
                    } finally {
                        try {
                            fis.close();
                            bis.close();
                            dis.close();
                        } catch (IOException ignored) {
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error reading config file:" + file.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public List<Status> getComponentStatus() {
        List<Status> results = new ArrayList<Status>();
        for (Status status : componentStatusMap.values()) {
            results.add(status);
        }
        return results;
    }

    @Override
    public Status getComponentStatus(String id) {
        return componentStatusMap.get(id);
    }

    public Map<String, Status> getComponentStatusMap() {
        return componentStatusMap;
    }

    /** {@inheritDoc} */
    @Override
    public LoggerDAO getDao() {

        return this.loggerDAO;
    }

    /**
     * Gets the log by hostname.
     * 
     * @param request the request
     * 
     * @return the log by hostname
     * @ssdd
     */
    @Override
    public GetLogByHostnameResponseDocument getLogByHostname(GetLogByHostnameRequestDocument request) {

        String hostname;
        int numOfRecords = 0;
        GetLogByHostnameResponseDocument response = GetLogByHostnameResponseDocument.Factory
                .newInstance();

        if (request.getGetLogByHostnameRequest().getHostname() != null) {
            hostname = request.getGetLogByHostnameRequest().getHostname();
            Log log = new Log();
            log.setHostname(request.getGetLogByHostnameRequest().getHostname());
            List<Log> listOfLog = loggerDAO.findByExample(log);

            GetLogByHostnameResponse getlogByHostnameResponse = response
                    .addNewGetLogByHostnameResponse();
            getlogByHostnameResponse.setHostname(hostname);
            // Log[] logArray = (Log[]) listOfLog.toArray(new Log[0]);
            LogLevelType.Enum logLevelTypeEnum = LogLevelType.INFO;

            ListIterator<?> litr = listOfLog.listIterator();
            while (litr.hasNext()) {
                Object element = litr.next();
                Log logRecord = (Log) element;
                logger.debug("id=" + logRecord.getId());
                LogType logType = getlogByHostnameResponse.addNewLogRecord();
                logType.setLogger(logRecord.getLogger());
                logType.setHostname(logRecord.getHostname());

                if (logRecord.getLoggingType().equalsIgnoreCase("Info"))
                    logLevelTypeEnum = LogLevelType.INFO;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Error"))
                    logLevelTypeEnum = LogLevelType.ERROR;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Warn"))
                    logLevelTypeEnum = LogLevelType.WARN;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Debug"))
                    logLevelTypeEnum = LogLevelType.DEBUG;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Fatal"))
                    logLevelTypeEnum = LogLevelType.FATAL;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Trace"))
                    logLevelTypeEnum = LogLevelType.TRACE;

                logType.setType(logLevelTypeEnum);
                Date date = logRecord.getTimestamp();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                logType.setTimestamp(cal);
                logType.setMessage(logRecord.getMessage());
                numOfRecords++;
            }
            getlogByHostnameResponse.setNumberOfRecords(numOfRecords);
            return response;
        } else
            return null;
    }

    /**
     * Gets the log by logger.
     * 
     * @param request the request
     * 
     * @return the log by logger
     * @ssdd
     */
    @Override
    public GetLogByLoggerResponseDocument getLogByLogger(GetLogByLoggerRequestDocument request) {

        String loggerStr;
        int numOfRecords = 0;
        GetLogByLoggerResponseDocument response = GetLogByLoggerResponseDocument.Factory
                .newInstance();

        if (request.getGetLogByLoggerRequest().getLogger() != null) {
            loggerStr = request.getGetLogByLoggerRequest().getLogger();
            Log log = new Log();
            log.setLogger(request.getGetLogByLoggerRequest().getLogger());
            List<Log> listOfLog = loggerDAO.findByExample(log);

            GetLogByLoggerResponse getlogByLoggerResponse = response.addNewGetLogByLoggerResponse();
            getlogByLoggerResponse.setLogger(loggerStr);
            // Log[] logArray = (Log[]) listOfLog.toArray(new Log[0]);
            LogLevelType.Enum logLevelTypeEnum = LogLevelType.INFO;

            ListIterator<?> litr = listOfLog.listIterator();
            while (litr.hasNext()) {
                Object element = litr.next();
                Log logRecord = (Log) element;
                logger.debug("id=" + logRecord.getId());
                LogType logType = getlogByLoggerResponse.addNewLogRecord();
                logType.setLogger(logRecord.getLogger());
                logType.setHostname(logRecord.getHostname());

                if (logRecord.getLoggingType().equalsIgnoreCase("Info"))
                    logLevelTypeEnum = LogLevelType.INFO;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Error"))
                    logLevelTypeEnum = LogLevelType.ERROR;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Warn"))
                    logLevelTypeEnum = LogLevelType.WARN;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Debug"))
                    logLevelTypeEnum = LogLevelType.DEBUG;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Fatal"))
                    logLevelTypeEnum = LogLevelType.FATAL;
                else if (logRecord.getLoggingType().equalsIgnoreCase("Trace"))
                    logLevelTypeEnum = LogLevelType.TRACE;

                logType.setType(logLevelTypeEnum);
                Date date = logRecord.getTimestamp();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                logType.setTimestamp(cal);
                logType.setMessage(logRecord.getMessage());
                numOfRecords++;
            }
            getlogByLoggerResponse.setNumberOfRecords(numOfRecords);
            return response;
        } else
            return null;

    }

    public Map<String, ComponentLogMonitor> getLoggers() {
        return loggers;
    }

    public RuleEngine getRuleEngine() {
        return ruleEngine;
    }

    @Override
    public void handleStatusEvent(StatusEvent event) {
        while(isSendHistory()){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Status compOldStatus = getComponentStatus(event.getComponentId()); // get latest status of
                                                                           // component
        Status compNewStatus = ruleEngine.getStatus(event, compOldStatus); // apply rules and get
                                                                           // new status

        String componentId = event.getComponentId();

        ComponentLogMonitor monitor=loggers.get(componentId);
        if(monitor!=null){
            Date thresholdTimeStamp = monitor.getThresholdTimeStamp();
            Date componentTimeStamp = event.getTimestamp();
            if (componentTimeStamp != null) {
                if (componentTimeStamp.before(thresholdTimeStamp)) {
                } else {
                    handleNewEvents(compOldStatus, compNewStatus, event);
                }
            }
            if(!event.getEvent().equals("TIMEOUT"))
                appendToHistory(compNewStatus, event);
        }
    }

    private void handleNewEvents(Status compOldStatus, Status compNewStatus, StatusEvent event) {
        String componentId = event.getComponentId();
        boolean skipTimeout = componentTimeOut.get(componentId);
        //skip first timeOut event before first logEvent after threshold Timestamp
        if (event.getEvent().equalsIgnoreCase("TIMEOUT")) {
            if (!skipTimeout) {
                sendStatusMessage(compNewStatus, event); 
                if (coreStatusChanged(compOldStatus, compNewStatus)) {
                    sendStatusToCores(compNewStatus, event);
                }
            }
        } else {
            if (skipTimeout) {
                componentTimeOut.put(componentId, false);
                sendStatusToCores(compNewStatus, event);
            }
            sendStatusMessage(compNewStatus, event); 
            if (coreStatusChanged(compOldStatus, compNewStatus)) {
                sendStatusToCores(compNewStatus, event);
            }
        }

    }

    private void appendToHistory(Status status, StatusEvent event) {
        String xmppMessage = "ComponentId-History: [" + event.getComponentId() + "]\n";
        xmppMessage += "Status: [" + status.getCategory() + "]\n";
        xmppMessage += "TimeStamp: [" + event.getTimestamp() + "]\n";
        xmppMessage += "LogMessage:[" + event.getMessage() + "]\n";
        List<String> history = componentLogHistory.get(event.getComponentId());
        if (history == null)
            history = new ArrayList<String>();
        history.add(xmppMessage);
        for(String localCoreJid:localCoreJids)
            communicationsService.sendXMPPMessage(xmppMessage,"message", "message", localCoreJid);
    }

    private boolean coreStatusChanged(Status compOldStatus, Status compNewStatus) {
        if (!compOldStatus.equals(compNewStatus)) {
            return true;
        }
        return false;
    }

    private void sendStatusToCores(Status status, StatusEvent event) {
        if (directoryService != null) {
            CoreConfigListType cores = directoryService.getCoreList();
            CoreConfigType[] coreTypes = cores.getCoreArray();
            String localCoreName = getConfigurationService().getCoreName();
            ;
            for (CoreConfigType core : coreTypes) {
                String online = core.getOnlineStatus().toString();
                if (online.equalsIgnoreCase("online")) {
                    String coreJid = core.getName();
                    if (!coreJid.equalsIgnoreCase(localCoreName)) {
                        sendCoreStatusXmppMessage(status, event, localCoreName, coreJid);
                    }
                }
            }
        }
    }

    private void sendCoreStatusXmppMessage(Status status, StatusEvent event, String localCoreJid,
            String coreJid) {
        String coreEventMessage = getCoreEventMessage();
        if (!coreEventMessage.equals("")) {
            Status currentCoreStatus = getCurrentCoreStatus(coreJid);
            StatusEvent coreStatusEvent = new StatusEvent("CORE", event.getEvent(),
                    coreEventMessage, event.getTimestamp());
            Status newCoreStatus = ruleEngine.getCoreStatus(coreStatusEvent, currentCoreStatus);
            if (newCoreStatus != null) {
                coreStatusMap.put(coreJid, newCoreStatus);
                if (communicationsService != null) {
                    String message = "Remote-CoreStatus: [" + localCoreJid + "]\n";
                    message += "Status: [" + newCoreStatus.getCategory() + "]";
                    String resource = getConsoleResource();
                    if (resource != null) {
                        getCommunicationsService().sendXMPPMessage(message, "message", "message",
                                coreJid + "/" + resource);
                    }
                }
                directoryService.setOverallCoreStatus(newCoreStatus.getCategory());
            }
        }
    }

    private Status getCurrentCoreStatus(String coreJid) {
        if (coreStatusMap.get(coreJid) == null) {
            coreStatusMap.put(coreJid, normalStatus);
        }
        return coreStatusMap.get(coreJid);
    }

    /**
     * Log request.
     * 
     * @param request the request
     * 
     * @return the log response document
     * @ssdd
     */
    public LogResponseDocument logRequest(LogRequestDocument request) {

        LogResponseDocument response = LogResponseDocument.Factory.newInstance();
        if (request.getLogRequest().getLogger() != null
                && request.getLogRequest().getHostname() != null
                && request.getLogRequest().getType() != null) {

            Log log = new Log();
            log.setLogger(request.getLogRequest().getLogger());
            log.setTimestamp(request.getLogRequest().getTimestamp().getTime());
            log.setHostname(request.getLogRequest().getHostname());
            log.setLoggingType(request.getLogRequest().getType().toString());
            log.setMessage(request.getLogRequest().getMessage());

            // Log persistedLog = loggerDAO.logRequest(log);
            loggerDAO.makePersistent(log);
            LogResponse logResponse = response.addNewLogResponse();
            logResponse.setResponse(LogResponseType.SUCCESS);

        } else {
            logger.error("Problem with Logging in the request");
            if (request.getLogRequest().getLogger() == null)
                logger.error(" no logger");
            if (request.getLogRequest().getHostname() == null)
                logger.error(" no hostname");
            if (request.getLogRequest().getHostname() == null)
                logger.error(" no log level");
        }
        return response;
    }

    @Override
    public void sendStatusMessage(Status status, StatusEvent event) {
        if (componentStatusMap == null)
            componentStatusMap = new HashMap<String, Status>();
        componentStatusMap.put(event.getComponentId(), status); // update component status
        componentStatusEventMap.put(event.getComponentId(), event); // update component statusevent
        if (isLogToFile()) {
            logEventToFile(status, event);
        } else {
            sendComponentXmppMessage(status, event);
        }
    }

    private String getCoreEventMessage() {
        String message = "";
        for (String component : componentStatusMap.keySet()) {
            message += component + ":" + componentStatusMap.get(component).getCategory() + " ";
        }
        return message;
    }

    private void sendComponentXmppMessage(Status status, StatusEvent event) {
        String xmppMessage = "CompenentId: [" + event.getComponentId() + "]\n";
        xmppMessage += "Status: [" + status.getCategory() + "]\n";
        xmppMessage += "TimeStamp: [" + event.getTimestamp() + "]\n";
        xmppMessage += "LogMessage:[" + event.getMessage() + "]\n";
        if (getCommunicationsService() != null) {
            for(String localCoreJid:localCoreJids)
                getCommunicationsService().sendXMPPMessage(xmppMessage, "message", "message",
                        localCoreJid);
            for (String jid : registeredJidList) {
                getCommunicationsService().sendXMPPMessage(xmppMessage, "message", "message", jid);
            }
        }
    }

    private void logEventToFile(Status status, StatusEvent event) {
        org.apache.log4j.Logger logger = componentLogOutputs.get(event.getComponentId());
        if (logger == null) {
            logger = createLogger(event.getComponentId());
        }
        org.apache.log4j.spi.LoggingEvent logEvent = new org.apache.log4j.spi.LoggingEvent();
        logEvent.setLogger(logger);
        logEvent.setLevel(Level.toLevel(event.getEvent()));
        logEvent.setMessage("[Component=" + event.getComponentId() + "]\n[Time="
                + event.getTimestamp() + "]\n[Status:" + status.getCategory() + "-"
                + status.getName() + "]\n[Message=" + event.getMessage() + "]");
        logger.callAppenders(logEvent);
    }

    private org.apache.log4j.Logger createLogger(String componentId) {
        // create output logFiles for component
        File file = new File("src/test/resources/loggingService/outputLogs/" + componentId
                + "_output.log");
        try {
            file.delete();
            file.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        FileAppender fileAppender = null;
        try {
            fileAppender = new FileAppender(new XMLLayout(), file.getAbsolutePath(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileAppender.activateOptions();
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(file.getAbsolutePath());
        logger.addAppender(fileAppender);
        componentLogOutputs.put(componentId, logger);
        return logger;
    }

    public void setComponentStatusMap(Map<String, Status> componentStatusMap) {
        this.componentStatusMap.putAll(componentStatusMap);
    }

    /** {@inheritDoc} */
    @Override
    public void setDao(LoggerDAO dao) {

        this.loggerDAO = dao;
    }

    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    public void setLoggers(Map<String, ComponentLogMonitor> loggers) {
        this.loggers.putAll(loggers);
    }

    public void setRuleEngine(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    /** {@inheritDoc} */
    public void systemInitializedHandler(String messgae) {

        WorkProductTypeListType typeList = WorkProductTypeListType.Factory.newInstance();
        directoryService.registerUICDSService(NS_LoggingService, LOGGING_SERVICE_NAME, typeList,
                typeList);
    }

    @Override
    public void updatePresence(Status status) {
        // TODO Auto-generated method stub
    }

    public CommunicationsService getCommunicationsService() {
        return this.communicationsService;
    }

    public void setCommunicationsService(CommunicationsService service) {
        this.communicationsService = service;
    }

    public void unRegisteredJidList(List<String> jidList) {
        for (String jid : jidList) {
            if (registeredJidList.contains(jid)) {
                registeredJidList.remove(jid);
            }
        }
    }

    public void updateEacUsersFile() {
        File file = getEacUsersFile();
        if (file != null) {
            if (file.exists()) {
                String path = file.getAbsolutePath();
                try {
                    file.delete();
                    File newFile = new File(path);
                    FileWriter fstream = new FileWriter(newFile, true);
                    BufferedWriter out = new BufferedWriter(fstream);
                    try {
                            for (String jid : this.registeredJidList) {
                                    out.write(jid + "\n");
                            }
                    } finally {
                    	if (out != null) out.close();
                    	if (fstream != null) fstream.close();
                    }
                } catch (Exception e) {// Catch exception if any
                    logger.error("Error updating file (" + file.getAbsolutePath() + "): "
                            + e.getMessage());
                }
            }
        }
    }

    public void sendAcknowledgementToJids(List<String> jidList) {
        String coreName = null;
        if (directoryService != null)
            coreName = directoryService.getCoreName();
        if (communicationsService != null) {
            for (String jid : jidList) {
                String xmppMessage = "Welcome, " + jid
                        + "! You are registered to receive Health and Status notification from "
                        + coreName;
                communicationsService.sendXMPPMessage(xmppMessage, "message", "message", jid);
            }
        }
    }

    public Map<String,List<String>> updateComponentHistory() {
        setSendHistory(true);
        return componentLogHistory;
    }

    public void unRegisterLocalJid(String connectionJid) {
        if(localCoreJids.contains(connectionJid))
            localCoreJids.remove(connectionJid);
    }

}