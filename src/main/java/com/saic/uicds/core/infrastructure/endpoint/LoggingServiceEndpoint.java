package com.saic.uicds.core.infrastructure.endpoint;

import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.uicds.loggingService.GetLogByHostnameRequestDocument;
import org.uicds.loggingService.GetLogByHostnameResponseDocument;
import org.uicds.loggingService.GetLogByLoggerRequestDocument;
import org.uicds.loggingService.GetLogByLoggerResponseDocument;
import org.uicds.loggingService.LogRequestDocument;
import org.uicds.loggingService.LogResponseDocument;

import com.saic.uicds.core.infrastructure.rule.RuleEngine;
import com.saic.uicds.core.infrastructure.service.impl.LoggingServiceImpl;
import com.saic.uicds.core.infrastructure.status.ComponentLogMonitor;
import com.saic.uicds.core.infrastructure.status.Status;
import com.saic.uicds.core.infrastructure.util.ServiceNamespaces;

/**
 * This service provides Apache log4j functionality to allow the client to log
 * any type of information into the log file. This web service is configured
 * based on the configure file (log4j.properties).
 * <p>
 * The LogType is defined as the following data structure:
 * <p>
 * <img src="doc-files/LogType.png"/> <BR>
 * 
 * @author Brian Borledge
 * @see <a href="../../wsdl/LoggingService.wsdl">Appendix: LoggingService.wsdl</a>
 * @see <a href="../../services/Logging/0.1/LoggingService.xsd">Appendix:
 *      LoggingService.xsd</a>
 * @idd
 */
@Component
@Endpoint
public class LoggingServiceEndpoint implements ServiceNamespaces {

	@Autowired
	private LoggingServiceImpl loggingService;

	Logger log = LoggerFactory.getLogger(LoggingServiceEndpoint.class);

	public void afterPropertiesSet() throws Exception {
		loggingService.afterPropertiesSet();
	}

	public List<Status> getComponentStatus() {
		return loggingService.getComponentStatus();
	}

	public Status getComponentStatus(String id) {
		return loggingService.getComponentStatus(id);
	}

	/**
	 * Get log by the hostname.
	 * 
	 * @param GetLogByHostnameRequestDocument
	 * 
	 * @return GetLogByHostnameResponseDocument
	 * @see <a href="../../services/Logging/0.1/LoggingService.xsd">Appendix:
	 *      LoggingService.xsd</a>
	 * @idd
	 */
	@PayloadRoot(namespace = NS_LoggingService, localPart = "GetLogByHostnameRequest")
	public GetLogByHostnameResponseDocument getLogByHostname(
			GetLogByHostnameRequestDocument request)
			throws DatatypeConfigurationException {

		return loggingService.getLogByHostname(request);
	}

	/**
	 * Get log by the logger.
	 * 
	 * @param GetLogByLoggerRequestDocument
	 * 
	 * @return GetLogByLoggerResponseDocument
	 * @see <a href="../../services/Logging/0.1/LoggingService.xsd">Appendix:
	 *      LoggingService.xsd</a>
	 * @idd
	 */
	@PayloadRoot(namespace = NS_LoggingService, localPart = "GetLogByLoggerRequest")
	public GetLogByLoggerResponseDocument getLogByLogger(
			GetLogByLoggerRequestDocument request)
			throws DatatypeConfigurationException {

		return loggingService.getLogByLogger(request);
	}

	public Map<String, ComponentLogMonitor> getLoggers() {
		return loggingService.getLoggers();
	}

	/**
	 * Request to log the information.
	 * 
	 * @param LogRequestDocument
	 * 
	 * @return LogResponseDocument
	 * @see <a href="../../services/Logging/0.1/LoggingService.xsd">Appendix:
	 *      LoggingService.xsd</a>
	 * @idd
	 */
	@PayloadRoot(namespace = NS_LoggingService, localPart = "LogRequest")
	public LogResponseDocument log(LogRequestDocument request)
			throws DatatypeConfigurationException {

		return loggingService.logRequest(request);
	}

	public void setComponentStatus(Map<String, Status> componentStatus) {
		loggingService.setComponentStatusMap(componentStatus);
	}

	public void setLoggers(Map<String, ComponentLogMonitor> loggers) {
		loggingService.setLoggers(loggers);
	}

	public void setRuleEngine(RuleEngine ruleEngine) {
		loggingService.setRuleEngine(ruleEngine);
	}

}
