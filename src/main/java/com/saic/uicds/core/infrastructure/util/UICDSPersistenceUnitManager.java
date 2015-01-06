package com.saic.uicds.core.infrastructure.util;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

public class UICDSPersistenceUnitManager extends DefaultPersistenceUnitManager {

    static Logger log = LoggerFactory.getLogger(UICDSPersistenceUnitManager.class);

    protected void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        try {
            super.postProcessPersistenceUnitInfo(pui);

            pui.addJarFileUrl(pui.getPersistenceUnitRootUrl());
            log.debug("\n\n\npostProcessPersistenceUnitInfo: - adding persistence unit root="
                + pui.getPersistenceUnitRootUrl());

            MutablePersistenceUnitInfo oldPui = getPersistenceUnitInfo(pui.getPersistenceUnitName());
            log.debug("postProcessPersistenceUnitInfo: - get pui for persistence unit name="
                + pui.getPersistenceUnitName());

            if (oldPui != null) {
                List<URL> urls = oldPui.getJarFileUrls();
                for (URL url : urls) {
                    pui.addJarFileUrl(url);
                    log.debug("postProcessPersistenceUnitInfo: adding jar with url:" + url);

                }
            }

            log.debug("===> URLs in pui:");
            List<URL> urls = pui.getJarFileUrls();
            for (URL url : urls) {
                log.debug("             url:" + url);

            }

        } catch (Throwable e) {
            log.debug("postProcessPersistenceUnitInfo: exception caught:" + e.getMessage());
            e.printStackTrace();
        }

        log.debug("postProcessPersistenceUnitInfo - leaving\n\n\n");
    }
}