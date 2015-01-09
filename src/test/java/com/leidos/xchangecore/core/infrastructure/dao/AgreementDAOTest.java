package com.leidos.xchangecore.core.infrastructure.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.leidos.xchangecore.core.infrastructure.dao.AgreementDAO;
import com.leidos.xchangecore.core.infrastructure.model.Agreement;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "file:src/main/resources/contexts/applicationContext-dataSrc.xml",
    "file:src/main/resources/contexts/mssql-dataSrcContext.xml",
    "file:src/test/resources/contexts/test-AgreementDAOContext.xml"
})
@TransactionConfiguration(transactionManager = "transactionManager")
@Transactional
public class AgreementDAOTest {

    @Autowired
    AgreementDAO dao;

    @Test
    public void testListAgreeemnt() {

        List<Agreement> agreements = dao.findAll();
        for (Agreement agreement : agreements) {
            System.out.println(agreement);
        }
    }

    @Test
    public void testParseCore() {

        String remoteCoreValue = "uicds@gv56bz1.corp.leidos.com?groups=group12, group13&ids=user4";
        String localCoreValue = "uicds@gv56bz1.corp.leidos.com?ids=admin";
        Agreement agreement = new Agreement();
        agreement.setLocalValue(localCoreValue);
        agreement.setRemoteValue(remoteCoreValue);
        agreement = dao.makePersistent(agreement);
        System.out.println(agreement.toString());
    }

}