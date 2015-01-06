package com.saic.uicds.core.infrastructure.service.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.directoryServiceData.WorkProductTypeListType;

import com.saic.uicds.core.infrastructure.service.DirectoryService;
import com.saic.uicds.core.infrastructure.service.SearchService;

/**
 * The SearchService Interface implementation.
 * 
 * @ssdd
 */
public class SearchServiceImpl implements SearchService {

    /** The log. */
    Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    /** The em. */
    @PersistenceContext
    EntityManager em;

    private DirectoryService directoryService;

    /**
     * Sets the directory service.
     * 
     * @param directoryService the new directory service
     */
    public void setDirectoryService(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    /**
     * Find entities.
     * 
     * @param queryString the query string
     * @param classes the classes
     * 
     * @return the list
     * @ssdd
     */
    @Override
    public List<?> findEntities(String queryString, Class<?>... classes) {
        List<?> results = null;

        try {
            FullTextEntityManager textEm = Search.getFullTextEntityManager(em);
            QueryParser parser = new QueryParser("name", new StopAnalyzer());
            org.apache.lucene.search.Query luceneQuery = parser.parse(queryString);
            FullTextQuery fullTextQuery = textEm.createFullTextQuery(luceneQuery, classes);
            results = fullTextQuery.getResultList();

            int count = 0;
            for (Object o : results) {
                log.debug("Result: " + o);
                ++count;
            }
            log.debug("matches: " + count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    public void systemInitializedHandler(String messgae) {
        WorkProductTypeListType typeList = WorkProductTypeListType.Factory.newInstance();
        directoryService.registerUICDSService("http://uicds.dctd.saic.com/searchService",
            SEARCH_SERVICE_NAME, typeList, typeList);
    }
}
