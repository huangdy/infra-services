package com.saic.uicds.core.infrastructure.dao;

import java.util.Set;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.PublishedProduct;

public interface PublishedProductDAO extends GenericDAO<PublishedProduct, Integer> {

    public Set<PublishedProduct> findByProductType(String productType);

    public Set<PublishedProduct> findAllPublishedProducts();
}
