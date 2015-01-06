package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.UserRole;

public interface UserRoleDAO extends GenericDAO<UserRole, String> {

    public List<UserRole> findUsersByRole(String roleRefID);

}