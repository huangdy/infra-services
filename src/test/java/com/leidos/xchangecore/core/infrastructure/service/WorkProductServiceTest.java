package com.leidos.xchangecore.core.infrastructure.service;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.leidos.xchangecore.core.infrastructure.dao.WorkProductDAO;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "file:src/main/resources/contexts/applicationContext-infra.xml",
    "file:src/main/resources/contexts/applicationContext-dataSrc.xml",
    "file:src/main/resources/contexts/mssql-dataSrcContext.xml",
    "file:src/test/resources/contexts/test-WorkProductDAOContext.xml"
})
@TransactionConfiguration(transactionManager = "transactionManager")
@Transactional
public class WorkProductServiceTest {

    @Autowired
    WorkProductService productService;
    @Autowired
    WorkProductDAO productDAO;

    @Test
    public void testProductUpdate() {

        List<WorkProduct> productList = productDAO.findAll();
        for (WorkProduct p : productList) {
            WorkProduct product = productService.getProduct(p.getProductID());
            System.out.println("before publish: " + product.getMetadata());
            product.setUpdatedDate(new Date());
            productService.publishProduct(product);
            product = productDAO.findByProductID(p.getProductID());
            System.out.println("After publish: " + product.getMetadata());
        }
    }
}
