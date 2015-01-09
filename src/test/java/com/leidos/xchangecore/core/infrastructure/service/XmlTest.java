package com.leidos.xchangecore.core.infrastructure.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.jpa.AbstractJpaTests;

import com.leidos.xchangecore.core.infrastructure.exceptions.InvalidXpathException;
import com.leidos.xchangecore.core.infrastructure.util.DocumentUtil;
import com.saic.precis.x2009.x06.base.PropertiesType;

public class XmlTest extends AbstractJpaTests {

    Logger log = LoggerFactory.getLogger(XmlTest.class);

    private static Map<String, String> namespaceMap = new HashMap<String, String>();
    {
        namespaceMap.put("cap", "urn:oasis:names:tc:emergency:cap:1.1");
        namespaceMap.put("inc", "http://uicds.org/incident");
        namespaceMap.put("de", "urn:oasis:names:tc:emergency:EDXL:DE:1.0");
        namespaceMap.put("nc", "http://niem.gov/niem/niem-core/2.0");
        namespaceMap.put("s", "http://niem.gov/niem/structures/2.0");
    }

    @Test
    public void testDocumentUtil() {

        try {
            FileInputStream fis =
                new FileInputStream(new File("src/test/resources/workproduct/IncidentSample.xml"));
            int size = fis.available();
            byte[] content = new byte[size];
            for (int i = 0; i < size;)
                i += fis.read(content, i, size - i);
            XmlObject xmlContent = null;
            try {
                xmlContent = XmlObject.Factory.parse(new String(content));
            } catch (Throwable e) {
                log.error("Paring error: " + e.getMessage());
                return;
            }
            String expression = "/inc:incident/nc:ActivityName";
            try {
                log.debug(expression +
                          (DocumentUtil.exist(expression, xmlContent, namespaceMap) ? " EXISTED" : " NOT EXISTED"));
            } catch (InvalidXpathException e) {
                log.error("Invalid xpath expression");
            }
            expression = "/inc:incident/cap:alert";
            try {
                log.debug(expression +
                          (DocumentUtil.exist(expression, xmlContent, namespaceMap) ? " EXISTED" : " NOT EXISTED"));
            } catch (Exception e) {
                log.error("Invalid xpath expression");
            }
            expression = "/inc:incident/cap:alert/cap:info[cap:category='CBRNE']";
            try {
                log.debug(expression +
                          (DocumentUtil.exist(expression, xmlContent, namespaceMap) ? " EXISTED" : " NOT EXISTED"));
            } catch (Exception e) {
                log.error("Invalid xpath expression");
            }
        } catch (Exception e) {
            log.error("Excecption caught: " + e.getMessage());
        }
    }

    @Test
    public void testWorkProductProperties() {

        PropertiesType prop = PropertiesType.Factory.newInstance();
        prop.addNewCreated().setDateValue(new Date());
        log.debug("Properties: " + prop.toString());
    }
}
