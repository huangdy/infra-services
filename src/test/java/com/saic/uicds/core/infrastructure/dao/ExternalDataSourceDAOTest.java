package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.saic.uicds.core.infrastructure.model.ExternalDataSourceConfig;
import com.saic.uicds.core.infrastructure.service.ConfigurationService;

//@Ignore
public class ExternalDataSourceDAOTest extends AbstractJpaTests {
    @Autowired
    ExternalDataSourceConfigDAO externalDataSourceDAO;

    @Autowired
    ConfigurationService cfgService;

    @Test
    public void testConfigurationServiceCreation() {
        assertNotNull(cfgService);
    }

    @Test
    public void testExternalDataSourceConfigDAOCreation() {
        assertNotNull(externalDataSourceDAO);
    }

    @Test
    public void testFindByURN() {
        String urn = "http://uicds.dctd.saic.com/IAPService";

        ExternalDataSourceConfig dataSource = new ExternalDataSourceConfig(urn,
            cfgService.getCoreName());
        externalDataSourceDAO.makePersistent(dataSource);

        List<ExternalDataSourceConfig> dataSourceList = externalDataSourceDAO.findByUrn(urn);
        assertEquals(1, dataSourceList.size());

        externalDataSourceDAO.makeTransient(dataSource);
        dataSourceList = externalDataSourceDAO.findByUrn(urn);
        assertEquals(0, dataSourceList.size());

    }

    @Test
    public void testFindByCoreName() {
        String coreName = cfgService.getCoreName();

        ExternalDataSourceConfig dataSource = new ExternalDataSourceConfig(
            "http://uicds.dctd.saic.com/AlertSource", coreName);
        externalDataSourceDAO.makePersistent(dataSource);

        List<ExternalDataSourceConfig> dataSourceList = externalDataSourceDAO.findByCoreName(coreName);
        assertEquals(1, dataSourceList.size());

        externalDataSourceDAO.makeTransient(dataSource);
        dataSourceList = externalDataSourceDAO.findByCoreName(coreName);
        assertEquals(0, dataSourceList.size());

    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml" };
    }
}
