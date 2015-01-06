package com.saic.uicds.core.infrastructure.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The ProductSubscriptionByID data model.
 * 
 * @ssdd
 */
@Entity
@Table(name = "PRODUCT_SUBSCRIPTION_BY_ID")
public class ProductSubscriptionByID {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PRODUCT_ID")
    @Field(index = Index.TOKENIZED)
    private String productId;

    @Column(name = "SUBSCRIBER_NAME")
    @Field(index = Index.TOKENIZED)
    private String subscriberName;

    // Randomly generated subscription ID
    @Column(name = "SUBSCRIPTION_ID")
    @Field(index = Index.TOKENIZED)
    private Integer subscriptionId;

    public ProductSubscriptionByID() {
    }

    /**
     * Instantiates a new product subscription by id.
     * 
     * @param productId the product id
     * @param subscriberName the subscriber name
     * @param subscriptionId the subscription id
     * @ssdd
     */
    public ProductSubscriptionByID(String productId, String subscriberName, Integer subscriptionId) {
        setProductId(productId);
        setSubscriber(subscriberName);
        setSubscriptionId(subscriptionId);
    }

    /**
     * Sets the id.
     * 
     * @param id the new id
     * @ssdd
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the id.
     * 
     * @return the id
     * @ssdd
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Sets the product id.
     * 
     * @param productId the new product id
     * @ssdd
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Gets the product id.
     * 
     * @return the product id
     * @ssdd
     */
    public String getProductId() {
        return this.productId;
    }

    /**
     * Sets the subscriber.
     * 
     * @param subscriberName the new subscriber
     * @ssdd
     */
    public void setSubscriber(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    /**
     * Gets the subscriber name.
     * 
     * @return the subscriber name
     * @ssdd
     */
    public String getSubscriberName() {
        return this.subscriberName;
    }

    /**
     * Sets the subscription id.
     * 
     * @param subscriptionId the new subscription id
     * @ssdd
     */
    public void setSubscriptionId(Integer subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Gets the subscription id.
     * 
     * @return the subscription id
     * @ssdd
     */
    public Integer getSubscriptionId() {
        return this.subscriptionId;
    }
}
