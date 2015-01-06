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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The ResourceProfile data model. UICDS resource profiles contain resource typing information and
 * interest expressions for notifications that can be applied to resource instances. The resource
 * profiles generally represent a role that a potential resource instance will fulfill with respect
 * to a particular instantiation of an Interest Group (i.e. incident). See
 * {@link com.saic.uicds.core.endpoint.ResourceProfileServiceEndpoint}
 * 
 * @ssdd
 */

@Entity
@Table(name = "RESOURCE_PROFILE")
public class ResourceProfileModel implements Serializable {

    private static final long serialVersionUID = 3631735818429898973L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "IDENTIFIER", unique = true)
    @Field(index = Index.TOKENIZED)
    private String identifier;

    @Column(name = "LABEL")
    @Field(index = Index.TOKENIZED)
    private String label;

    @Column(name = "DESCRIPTION")
    private String description;

    // key = codespace,label value =value>
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Set<CodeSpaceValueType> cvts = new HashSet<CodeSpaceValueType>();
    
    @OneToMany(targetEntity = InterestElement.class, cascade = CascadeType.ALL)
    @Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @LazyCollection(value = LazyCollectionOption.FALSE)
    private Set<InterestElement> interests = new HashSet<InterestElement>();

    /**
     * Gets the id.
     * 
     * @return the id
     * @ssdd
     */
    public Integer getId() {
        return id;
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
     * Gets the identifier.
     * 
     * @return the identifier
     * @ssdd
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier.
     * 
     * @param identifier the new identifier
     * @ssdd
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the label.
     * 
     * @return the label
     * @ssdd
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     * 
     * @param label the new label
     * @ssdd
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     * @ssdd
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * 
     * @param description the new description
     * @ssdd
     */
    public void setDescription(String description) {
        this.description = description;
    }

  
    
    /**
     * Gets the interests.
     * 
     * @return the interests
     * @ssdd
     */
    public Set<InterestElement> getInterests() {
        return interests;
    }

    /**
     * Adds the interest.
     * 
     * @param interest the interest
     * 
     * @return true, if successful
     * @ssdd
     */
    public boolean addInterest(InterestElement interest) {
        return this.interests.add(interest);
    }

    /**
     * Removes the interest.
     * 
     * @param interest the interest
     * 
     * @return true, if successful
     * @ssdd
     */
    public boolean removeInterest(InterestElement interest) {
        return this.interests.remove(interest);
    }

    /**
     * Sets the interests.
     * 
     * @param interests the new interests
     * @ssdd
     */
    public void setInterests(Set<InterestElement> interests) {
        this.interests = interests;
    }

    public String toString() {
        String profile = "";

        profile += label + "\n";
        profile += description + "\n";
        for (InterestElement intEle : interests) {
            profile += intEle.getMessageContent() + "\n";
            profile += intEle.getTopicExpression() + "\n";
            for (InterestNamespaceType ns : intEle.getNamespaces()) {
                profile += ns.getPrefix() + "\n";
                profile += ns.getUri() + "\n";
            }
        }

        return profile;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((interests == null) ? 0 : interests.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceProfileModel other = (ResourceProfileModel) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (interests == null) {
            if (other.interests != null)
                return false;
        } else if (!interests.equals(other.interests))
            return false;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        return true;
    }

	public Set<CodeSpaceValueType> getCvts() {
		return cvts;
	}

	public void setCvts(Set<CodeSpaceValueType> cvts) {
		this.cvts = cvts;
	}

	public void addCVT(String cs, String label, String value)
	{
		CodeSpaceValueType cvt=new CodeSpaceValueType();
		cvt.setCodeSpace(cs);
		cvt.setLabel(label);
		cvt.setValue(value);
		cvts.add(cvt);
	}
}
