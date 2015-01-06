package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.ProductSubscriptionByType;

public interface ProductSubscriptionByTypeDAO extends
        GenericDAO<ProductSubscriptionByType, Integer> {

    public List<ProductSubscriptionByType> findByProductType(String productType);

    public List<ProductSubscriptionByType> findByInterestGroupIdAndProductType(String interestGroupID,
            String productType);

    public List<ProductSubscriptionByType> findByXPath(String xPath);

    public List<ProductSubscriptionByType> findBySubscriberName(String subscriberName);

    public List<ProductSubscriptionByType> findBySubscriptionId(Integer subscriptionId);

}
