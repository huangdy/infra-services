package com.saic.uicds.core.infrastructure.dao.hb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.saic.uicds.core.dao.hb.GenericHibernateDAO;
import com.saic.uicds.core.infrastructure.dao.CodeSpaceValueTypeDAO;
import com.saic.uicds.core.infrastructure.model.CodeSpaceValueType;

public class CodeSpaceValueTypeHibernateDAO extends GenericHibernateDAO<CodeSpaceValueType, Integer>
        implements CodeSpaceValueTypeDAO {

    public Set<CodeSpaceValueType> findAllCodeSpaceValueTypes() {

        List<CodeSpaceValueType> allCodeTypes = findAll();

        //just direct convert list into set, no loop needed as below needed. FLi changed on 11/16/2011
        Set<CodeSpaceValueType> foundCodeTypes = new HashSet<CodeSpaceValueType>(allCodeTypes);
        
        /* old code, keep this as reference.
        Set<CodeSpaceValueType> foundCodeTypes = new HashSet<CodeSpaceValueType>();
        for (CodeSpaceValueType type : allCodeTypes) {
            foundCodeTypes.add(type);
        }
        */
        return foundCodeTypes;
    }

}
