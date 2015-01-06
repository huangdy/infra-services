package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.Log;

public interface LoggerDAO extends GenericDAO<Log, Integer> {

    Log logRequest(Log log);

    List<Log> findByHostname(String hostname);

    List<Log> findByLogger(String logger);
}
