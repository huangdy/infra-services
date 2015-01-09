/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leidos.xchangecore.core.infrastructure.util;

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
 *
 * @author vmuser
 */

@Entity
@Table(name="EXTENDED_METADATA")
public class ExtendedMetadata implements Serializable {
    
        @SuppressWarnings("unused")
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
        @Column(name = "CODESPACE")
    @Field(index = Index.TOKENIZED)    
    private String codespace;
        
        @Column(name = "CODE")
    @Field(index = Index.TOKENIZED)
        private String code;
        
        @Column(name = "LABEL")
    @Field(index = Index.TOKENIZED)
        private String label;
 
        @Column(name = "VALUE")
    @Field(index = Index.TOKENIZED)
        private String value;
   
    
    public String getCodespace() {
        return codespace;
    }

    public void setCodespace(String codespace) {
        this.codespace = codespace;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
   
    
    public void ExtendedMetadata() {
    
        this.codespace="";
        this.code="";
        this.label="";
        this.value="";
}
}

