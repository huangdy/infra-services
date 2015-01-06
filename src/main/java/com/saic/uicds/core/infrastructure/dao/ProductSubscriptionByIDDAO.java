package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.ProductSubscriptionByID;

public interface ProductSubscriptionByIDDAO extends GenericDAO<ProductSubscriptionByID, Integer> {

    public List<ProductSubscriptionByID> findByProductID(String productId);

    public List<ProductSubscriptionByID> findBySubscriptionId(Integer subscriptionId);

    public List<ProductSubscriptionByID> findBySubscriberName(String subscriberName);
}
