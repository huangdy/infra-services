package com.saic.uicds.core.infrastructure.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import com.saic.uicds.core.infrastructure.model.WorkProduct;
import com.saic.uicds.core.infrastructure.util.XmlUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "file:src/main/resources/contexts/applicationContext-dataSrc.xml",
    "file:src/main/resources/contexts/mssql-dataSrcContext.xml",
    "file:src/test/resources/contexts/test-WorkProductDAOContext.xml"
})
@TransactionConfiguration(transactionManager = "transactionManager")
@Transactional
public class WorkProductDAOTest {

    @Autowired
    WorkProductDAO productDAO;

    @Test
    public void testAll() {

        List<WorkProduct> productList = productDAO.findAll();
        for (WorkProduct product : productList) {
            System.out.println("+++++++++++++++++++++++++++++++++++++++");
            System.out.println(product.getMetadata());
            List<WorkProduct> products = productDAO.findAllClosedVersionOfProduct(product.getProductID());
            for (WorkProduct p : products) {
                System.out.println("----- closed version: " + p.getMetadata() + "\n-----");
            }
            System.out.println("+++++++++++++++++++++++++++++++++++++++\n");
        }
    }

    @Test
    public void testFindByInterestGroup() {

        String igID = "IG-9f1d846c-6f3f-40e3-96ef-1dd12ab75103";
        List<WorkProduct> productList = productDAO.findByInterestGroup(igID);
        for (WorkProduct product : productList) {
            System.out.println(product.getMetadata());
        }
    }

    @Test
    public void testFindByProductType() {

        String productType = "Incident";
        List<WorkProduct> products = productDAO.findByProductType(productType);
        for (WorkProduct product : products) {
            System.out.println("findByProductType(" + productType + "): " + product.getMetadata());
        }
    }

    @Test
    public void testFindDocBySearchCriteria() throws Exception {

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("req.remoteUser", new String[] {
            "admin", ""
        });
        params.put("full", new String[] {
            "true", ""
        });
        params.put("productType", new String[] {
            "incident", ""
        });
        // params.put("productType", new String[] { "alert", "" });
        // params.put("startIndex", new String[] { "1", "" });
        // params.put("count", new String[] { "3", "" });
        params.put("bbox", new String[] {
            "-120,30,-80,50", ""
        });
        // params.put("productVersion", new String[] { "1", "" });
        // params.put("productID", new String[] { "Alert-afcb54aa-ea1b-41e2-8824-92134a11aead", "" });
        params.put("format", new String[] {
            "rss", ""
        });
        Document doc = productDAO.findDocsBySearchCriteria(params);
        if (doc != null) {
            System.out.println("Found:\n" + XmlUtil.getDOMString(doc));
        }
    }
}
