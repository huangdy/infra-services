package com.saic.uicds.core.infrastructure.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The ProductSubscriptionByType data model.
 * 
 * @ssdd
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "PRODUCT_SUBSCRIPTION_BY_TYPE")
public class ProductSubscriptionByType
    implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PRODUCT_TYPE")
    @Field(index = Index.TOKENIZED)
    private String productType;

    @Column(name = "INTEREST_GROUP_ID")
    @Field(index = Index.TOKENIZED)
    private String interestGroupID;

    @Column(name = "XPATH")
    @Field(index = Index.TOKENIZED)
    private String xPath;

    @Column(name = "SUBSCRIBER_NAME")
    @Field(index = Index.TOKENIZED)
    private String subscriberName;

    // Randomly generated subscription ID
    @Column(name = "SUBSCRIPTION_ID")
    @Field(index = Index.TOKENIZED)
    private Integer subscriptionId;

    // nameSpaceMap
    // key: prefix
    // value: namespace. //also named as URI

     @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
     @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
     private Set<NamespaceMap> namespacemap = new HashSet<NamespaceMap>();

    public ProductSubscriptionByType() {

    }

    /**
     * Instantiates a new product subscription by type.
     * 
     * @param productType the product type
     * @param interestGroupID the interest group id
     * @param xPath the x path
     * @param subscriberName the subscriber name
     * @param subscriptionId the subscription id
     * @param namespaceMap the namespace map
     * @ssdd
     */
    public ProductSubscriptionByType(String productType, String interestGroupID, String xPath,
        String subscriberName, Integer subscriptionId, Set<NamespaceMap> namespaceMap) {

        setProductType(productType);
        setInterestGroupID(interestGroupID);
        setXPath(xPath);
        setSubscriberName(subscriberName);
        setSubscriptionId(subscriptionId);
        setNamespacemap(namespaceMap);
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
     * Sets the product type.
     * 
     * @param type the new product type
     * @ssdd
     */
    public void setProductType(String type) {

        this.productType = type;
    }

    /**
     * Gets the product type.
     * 
     * @return the product type
     * @ssdd
     */
    public String getProductType() {

        return this.productType;
    }

    /**
     * Sets the interest group id.
     * 
     * @param interestGroupID the new interest group id
     * @ssdd
     */
    public void setInterestGroupID(String interestGroupID) {

        this.interestGroupID = interestGroupID;
    }

    /**
     * Gets the interest group id.
     * 
     * @return the interest group id
     * @ssdd
     */
    public String getInterestGroupID() {

        return this.interestGroupID;
    }

    /**
     * Sets the x path.
     * 
     * @param xPath the new x path
     * @ssdd
     */
    public void setXPath(String xPath) {

        this.xPath = xPath;
    }

    /**
     * Gets the x path.
     * 
     * @return the x path
     * @ssdd
     */
    public String getXPath() {

        return this.xPath;
    }

    /**
     * Sets the subscriber name.
     * 
     * @param subscriberName the new subscriber name
     * @ssdd
     */
    public void setSubscriberName(String subscriberName) {

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


    public Map<String, String> getNamespaceMap() {

        HashMap<String, String> map = new HashMap<String, String>();
        for (NamespaceMap namespace : namespacemap) {
            map.put(namespace.getPrefix(), namespace.getUri());
        }
        return map;
    }

	public Set<NamespaceMap> getNamespacemap() {
		return this.namespacemap;
	}

	public void setNamespacemap(Set<NamespaceMap> namespacemap) {
		this.namespacemap = namespacemap;
	}
}
