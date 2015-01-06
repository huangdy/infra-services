package com.saic.uicds.core.infrastructure.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;
import org.uicds.agreementService.AgreementType.ShareRules;

import com.saic.uicds.core.infrastructure.model.CodeSpaceValueType;
import com.saic.uicds.core.infrastructure.model.NamespaceMap;
import com.saic.uicds.core.infrastructure.model.ResourceInstanceModel;
import com.saic.uicds.core.infrastructure.model.ShareRule;

/**
 * 
 * @author roger
 * 
 */

// @Ignore
public class ResourceInstanceDAOTest
    extends AbstractJpaTests {

    private static final String RESOURCE_ID = "resourceID";

    private static final String PROFILE = "profile";

    private static final String ENDPOINT = "endpoint";

    private static final String DESCRIPTION = "Description";

    private static final String OWNING_CORE = "owningCore";

    private static final String LOCAL_RESOURCE_ID = "localResourceID";

    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";

    private static final String LABEL1 = "label1";
    private static final String LABEL2 = "label2";

    private static final String CODESPACE1 = "codespace1";
    private static final String CODESPACE2 = "codespace2";

    private static final String RESOURCE_LABEL = "resource_label";

    private Logger log = LoggerFactory.getLogger(ResourceInstanceDAOTest.class);

    static final private String ID1 = "IC1";
    static final private String ID2 = "IC2";
    static final private String ID3 = "IC3";

    @Autowired
    ResourceInstanceDAO resourceInstanceDAO;

    @Test
    public void testFindByEntityId() {

        log.debug("Creating sample Instance");
        ResourceInstanceModel resourceInstance = buildSampleInstance(ID1);
        log.debug(resourceInstance.toString());

        log.debug("Persisting sample Instance");
        ResourceInstanceModel result = resourceInstanceDAO.makePersistent(resourceInstance);

        assertNotNull(result);
        assertEquals(resourceInstance, result);
        assertNotNull(resourceInstance.getIdentifier());
        String identifier = resourceInstance.getIdentifier();
        log.debug("ID: " + identifier);

        log.debug("Retrieving sample Instance");
        ResourceInstanceModel result2 = resourceInstanceDAO.findByIdentifier(identifier);

        assertNotNull(result2);
        assertEquals(resourceInstance, result2);
        assertNotNull(result2.getIdentifier());
        log.debug(result2.toString());
    }

    @Test
    public void testFindAll() {

        createSeveralResourceInstances();

        List<ResourceInstanceModel> list = resourceInstanceDAO.findAll();
        assertNotNull(list);
        assertEquals(2, list.size());

        checkCreatedResourceInstances(list);
    }

    @Test
    public void testRemove() {

        createSeveralResourceInstances();

        ResourceInstanceModel resourceInstance = resourceInstanceDAO.findByIdentifier(ID2);
        assertNotNull("Resource instance not found", resourceInstance);
        assertEquals("found wrong instance", ID2, resourceInstance.getIdentifier());
        resourceInstanceDAO.makeTransient(resourceInstance);

        List<ResourceInstanceModel> list = resourceInstanceDAO.findAll();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("wrong instance left in list", ID1, list.get(0).getIdentifier());

    }

    private void createSeveralResourceInstances() {

        ResourceInstanceModel resourceInstance = new ResourceInstanceModel();
        resourceInstance = buildSampleInstance(ID1);
        ResourceInstanceModel result = resourceInstanceDAO.makePersistent(resourceInstance);
        assertNotNull(result);

        ResourceInstanceModel resourceInstance2 = new ResourceInstanceModel();
        resourceInstance2 = buildSampleInstance(ID2);
        ResourceInstanceModel result2 = resourceInstanceDAO.makePersistent(resourceInstance2);
        assertNotNull(result2);
    }

    private void checkCreatedResourceInstances(List<ResourceInstanceModel> list) {

        boolean foundID1 = false;
        boolean foundID2 = false;
        for (ResourceInstanceModel ri : list) {
            if (ri.getIdentifier().equals(ID1)) {
                foundID1 = true;
            } else if (ri.getIdentifier().equals(ID2)) {
                foundID2 = true;
            }
            assertEquals("wrong description", DESCRIPTION, ri.getDescription());
            assertEquals("wrong resource label", RESOURCE_LABEL, ri.getLabel());
            assertEquals("wrong local id", LOCAL_RESOURCE_ID, ri.getLocalResourceID());
            assertNotNull("null endpoints", ri.getEndpoints());
            assertTrue("no endpoints", ri.getEndpoints().size() > 0);
            String endpoint = ri.getEndpoints().iterator().next();
            assertEquals("wrong endpoint", ENDPOINT, endpoint);
            assertEquals("wrong resource id", RESOURCE_ID, ri.getResourceID());
            assertEquals("wrong owning core", OWNING_CORE, ri.getOwningCore());

            assertNotNull("null profile set", ri.getProfiles());
            assertTrue("no profiles in set", ri.getProfiles().size() > 0);
            String profile = ri.getProfiles().iterator().next();
            assertEquals("wrong profile", PROFILE, profile);

            assertNotNull("null csvt set", ri.getCvts());
            assertTrue("no csvt", ri.getCvts().size() > 0);
            //CodeSpaceValueType csvt = ri.getCvts().iterator().next();
            //assertNotNull("null csvt", csvt);
            if (ri.getIdentifier().equals(ID1)) {
            	
            	CodeSpaceValueType csvt =findCVTByCodespace(ri.getCvts(), CODESPACE1 );
            	assertNotNull("null csvt", csvt);           	
                assertEquals("wrong csvt codespace", CODESPACE1, csvt.getCodeSpace());
                assertEquals("wrong csvt label", LABEL1, csvt.getLabel());
                assertEquals("wrong csvt value", VALUE1, csvt.getValue());
            } else if (ri.getIdentifier().equals(ID2)) {
            	CodeSpaceValueType csvt =findCVTByCodespace(ri.getCvts(), CODESPACE2 );
            	assertNotNull("null csvt", csvt);
                assertEquals("wrong csvt codespace", CODESPACE2, csvt.getCodeSpace());
                assertEquals("wrong csvt label", LABEL2, csvt.getLabel());
                assertEquals("wrong csvt value", VALUE2, csvt.getValue());
            }

        }
        assertTrue("not all instances found", foundID1 && foundID2);
    }

    private CodeSpaceValueType findCVTByCodespace(Set<CodeSpaceValueType> cvts, String codespace)
    {
    	 CodeSpaceValueType obj=null;
    	 for (CodeSpaceValueType cvt : cvts) 
    	 {
             if(cvt.getCodeSpace().equalsIgnoreCase(codespace))
             {
            	 return cvt;
             }
         }
    	
    	
    	return obj;
    }
    
    
    @Override
    protected String[] getConfigLocations() {

        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml" };
    }

    private ResourceInstanceModel buildSampleInstance(String id) {

        ResourceInstanceModel model = new ResourceInstanceModel();
        model.setDescription(DESCRIPTION);
        HashSet<String> endpoints = new HashSet<String>();
        endpoints.add(ENDPOINT);
        model.setEndpoints(endpoints);
        model.setIdentifier(id);
        // we are using set now, make up some 2 cvts for testing./////////////////////////////////
        HashSet<CodeSpaceValueType> cvts = new HashSet<CodeSpaceValueType>();

        CodeSpaceValueType cvt1 = new CodeSpaceValueType();

        cvt1.setCodeSpace(CODESPACE1);
        cvt1.setLabel(LABEL1);
        cvt1.setValue(VALUE1);
        // cvt1.setShareRule(shareRule1);
        cvts.add(cvt1);

        CodeSpaceValueType cvt2 = new CodeSpaceValueType();
        cvt2.setCodeSpace(CODESPACE2);
        cvt2.setLabel(LABEL2);
        cvt2.setValue(VALUE2);
        // cvt2.setShareRule(shareRule1);
        cvts.add(cvt2);

        model.setCvts(cvts);

        // ////////////////////////////////////////////////
        model.setLabel(RESOURCE_LABEL);
        model.setLocalResourceID(LOCAL_RESOURCE_ID);
        model.setOwningCore(OWNING_CORE);
        HashSet<String> profiles = new HashSet<String>();
        profiles.add(PROFILE);
        model.setProfiles(profiles);
        model.setResourceID(RESOURCE_ID);

        return model;
    }

}
