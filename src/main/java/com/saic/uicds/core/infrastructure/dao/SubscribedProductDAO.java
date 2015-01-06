package com.saic.uicds.core.infrastructure.dao;

import java.util.Set;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.SubscribedProduct;

public interface SubscribedProductDAO extends GenericDAO<SubscribedProduct, Integer> {

    public Set<SubscribedProduct> findByProductType(String productType);

    public Set<SubscribedProduct> findAllSubscribedProducts();

}
