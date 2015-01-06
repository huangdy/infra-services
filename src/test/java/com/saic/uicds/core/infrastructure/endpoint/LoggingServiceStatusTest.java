package com.saic.uicds.core.infrastructure.endpoint;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.jpa.AbstractJpaTests;

import com.saic.uicds.core.infrastructure.rule.RuleEngine;
import com.saic.uicds.core.infrastructure.service.impl.LoggingServiceImpl;
import com.saic.uicds.core.infrastructure.status.ComponentLogMonitor;
import com.saic.uicds.core.infrastructure.status.Status;
import com.saic.uicds.core.infrastructure.status.StatusEvent;

public class LoggingServiceStatusTest extends AbstractJpaTests {

    Logger log = LoggerFactory.getLogger(this.getClass());

    LoggingServiceImpl loggingService = new LoggingServiceImpl();

    @Override
    public void onSetUp() {
        loggingService = new LoggingServiceImpl();
        loggingService.setLogToFile(true);
    }

    @Test
    public void testHandleStatusEvent() throws Exception {
        Map<String, Status> componentStatusMap = new HashMap<String, Status>();
        Map<String, ComponentLogMonitor> loggers = new HashMap<String, ComponentLogMonitor>();
        File rulesFile = new File("src/test/resources/loggingService/rules.xml");
        RuleEngine ruleEngine = new RuleEngine();

        ComponentLogMonitor monitor = new ComponentLogMonitor();
        loggers.put("tomcat", monitor);
        componentStatusMap.put("tomcat", new Status("NORMAL", "NORMAL"));
        ruleEngine.initializeRules(rulesFile);

        loggingService.setEacUsersFile(new File("C:/UICDS/ServerApps/uicds/config/eac_users.ini"));
        loggingService.setComponentStatusMap(componentStatusMap);
        loggingService.setLoggers(loggers);
        loggingService.setRuleEngine(ruleEngine);
        loggingService.afterPropertiesSet();
        loggingService.setLogToFile(false);
        // logmonitor notifies new event and updates component state.
        Date date=new Date();
        monitor.notifyOnEvent(new StatusEvent("tomcat", "ERROR", "Exception in tomcat", date));
        Status expected = new Status("ERROR", "ERROR");
        Status actual = loggingService.getComponentStatus("tomcat");
        assertStatusEquals(expected, actual);
    }

    @Test
    public void testRuleEngineOperation() throws Exception {
        Map<String, Status> componentStatusMap = new HashMap<String, Status>();
        Map<String, Boolean> componentTimeOut = new HashMap<String, Boolean>();
        componentStatusMap.put("tomcat", new Status("NORMAL", "NORMAL"));
        componentStatusMap.put("openfire", new Status("ERROR", "ERROR"));
        File rulesFile = new File("src/test/resources/loggingService/rules.xml");
        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.initializeRules(rulesFile);

        ComponentLogMonitor monitor = new ComponentLogMonitor();
        Map<String, ComponentLogMonitor> loggers = new HashMap<String, ComponentLogMonitor>();
        loggers.put("tomcat", monitor);
        componentTimeOut.put("tomcat", true);
        monitor = new ComponentLogMonitor();
        loggers.put("openfire", monitor);
        componentTimeOut.put("openfire", true);
        
        loggingService.setComponentStatusMap(componentStatusMap);
        loggingService.setRuleEngine(ruleEngine);
        loggingService.setLoggers(loggers);
        loggingService.setComponentTimeOut(componentTimeOut);
        loggingService.setLogToFile(false);

        // loggingservice handles event by applying ruleengine and updates component state 
        Date date=new Date();
        StatusEvent tomcatEvent = new StatusEvent("tomcat", "ERROR", "Exception in tomcat", date);
        loggingService.handleStatusEvent(tomcatEvent); // Rule: beginStatus=NORMAL, endStatus=ERROR, & containsAny
        Status expectedTomcatStatus = new Status("ERROR", "ERROR"); 
        Status actualTomcatStatus = loggingService.getComponentStatus("tomcat");
        assertStatusEquals(expectedTomcatStatus, actualTomcatStatus);

        date=new Date();
        StatusEvent openfireEvent = new StatusEvent("openfire", "WARN", "openfire log message",
                date);
        loggingService.handleStatusEvent(openfireEvent);
        Status expectedOpenfireStatus = new Status("CAUTIOUS", "ERROR"); // Rule: beginStatus=ERROR, endStatus=CAUTIOUS, & containsNone
        Status actualOpenfireStatus = loggingService.getComponentStatus("openfire");
        assertStatusEquals(expectedOpenfireStatus, actualOpenfireStatus);
    }

    private void assertStatusEquals(Status expected, Status actual) {
        assertEquals(expected.getCategory(), actual.getCategory());
        assertEquals(expected.getName(), actual.getName());
    }

}
