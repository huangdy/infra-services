package com.saic.uicds.core.infrastructure.model;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XmlObjectUserType Data Model
 * 
 * @author Christopher Lakey
 * @created September 07, 2009
 */
@SuppressWarnings("unchecked")
public class XmlBeansUserType implements UserType {

    // This constant is referenced by Hibernate @Type annotations
    public final static String NAME = "com.saic.uicds.core.infrastructure.model.XmlBeansUserType";

    private static final int[] sqlTypes = new int[] { Types.CLOB };

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Object assemble(Serializable value, Object owner) throws HibernateException {
        return value;
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public boolean equals(Object arg0, Object arg1) throws HibernateException {
        if (arg0 == arg1)
            return true;
        if (arg0 == null || arg1 == null)
            return false;
        return arg0.equals(arg1);
    }

    public int hashCode(Object arg0) throws HibernateException {
        return arg0.hashCode();
    }

    public boolean isMutable() {
        return false;
    }

    public Object nullSafeGet(ResultSet rset, String[] names, Object owner)
        throws HibernateException, SQLException {
        Object result = null;
        String value = rset.getString(names[0]);
        if (value != null && (value.length() > 0)) {
            try {
                result = XmlObject.Factory.parse(value);
            } catch (XmlException e) {
                logger.error("Error parsing xml", e);
            }
        }
        return result;
    }

    public void nullSafeSet(PreparedStatement stmt, Object value, int index)
        throws HibernateException, SQLException {
        if (value instanceof XmlObject) {
            XmlObject xmlObject = (XmlObject) value;
            stmt.setString(index, xmlObject.xmlText());
        } else {
            stmt.setString(index, "");
        }
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    public Class returnedClass() {
        return XmlObject.class;
    }

    public int[] sqlTypes() {
        return sqlTypes;
    }

}
