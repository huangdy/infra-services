package com.saic.uicds.core.infrastructure.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

/**
 * The InterestNamespaceType data model.
 * 
 * @ssdd
 */
@Entity
@Table(name = "INTEREST_NAMESPACE_TYPE")
public class InterestNamespaceType implements Serializable {

    private static final long serialVersionUID = 6157253001335701160L;

    @Id
    @Column(name = "INTEREST_NS_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PREFIX")
    @Field(index = Index.TOKENIZED)
    private String prefix;

    @Column(name = "URI")
    @Field(index = Index.TOKENIZED)
    private String uri;

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
     * Gets the prefix.
     * 
     * @return the prefix
     * @ssdd
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix.
     * 
     * @param prefix the new prefix
     * @ssdd
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the uri.
     * 
     * @return the uri
     * @ssdd
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the uri.
     * 
     * @param uri the new uri
     * @ssdd
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
        InterestNamespaceType other = (InterestNamespaceType) obj;
        if (prefix == null) {
            if (other.prefix != null)
                return false;
        } else if (!prefix.equals(other.prefix))
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        return true;
    }
}
