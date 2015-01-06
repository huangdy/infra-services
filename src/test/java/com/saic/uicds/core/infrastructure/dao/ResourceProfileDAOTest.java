package com.saic.uicds.core.infrastructure.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.Descriptor.Iterator;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.saic.uicds.core.infrastructure.model.CodeSpaceValueType;
import com.saic.uicds.core.infrastructure.model.InterestElement;
import com.saic.uicds.core.infrastructure.model.InterestNamespaceType;
import com.saic.uicds.core.infrastructure.model.ResourceProfileModel;

/**
 * 
 * @author Andre Bonner
 * 
 */

//@Ignore
public class ResourceProfileDAOTest extends AbstractJpaTests {

    private Logger log = LoggerFactory.getLogger(ResourceProfileDAOTest.class);

    @Autowired
    ResourceProfileDAO profileDAO;

    static String MESSAGE_CONTENT = "//de:EDXLDistribution/de:contentObject/de:xmlContent/de:embeddedXMLContent/inc:incident[nc:Activitiy CategoryText='CBRNE']";

    @Test
    public void testFindByEntityId() {
        String profileID1;
        ResourceProfileModel profile = new ResourceProfileModel();

        log.debug("Creating sample Profile");
        profile = buildSampleProfile();
        log.debug(profile.toString());

        log.debug("Persisting sample Profile");
        ResourceProfileModel result = profileDAO.makePersistent(profile);
        assertNotNull(result);
        assertEquals(profile, result);
        assertNotNull(profile.getIdentifier());
        profileID1 = profile.getIdentifier();
        log.debug("ID: " + profileID1);

        log.debug("Retrieving sample Profile");
        ResourceProfileModel result2 = profileDAO.findByIdentifier(profileID1);

        assertNotNull(result2);
        assertEquals(profile, result2);
        assertNotNull(result2.getIdentifier());
        assertNotNull(result2.getInterests());
        for (InterestElement interest : result2.getInterests()) {
            assertNotNull(interest);
            assertEquals(MESSAGE_CONTENT, interest.getMessageContent());
        }

  //      assertNotNull(result2.getResourceTyping());
        assertNotNull(result2.getCvts());
      
  //      Map<String, String> types = result2.getResourceTyping();
  //      assertTrue(types.size() == 2);
  //      assertTrue(types.get("c1,l1").equals("v1"));
  //      assertTrue(types.get("c1,l2").equals("v2"));
        Set<CodeSpaceValueType> cvts = result2.getCvts();
        assertTrue(cvts.size() == 2);
        
     // Iterating over the elements in the set
        java.util.Iterator<CodeSpaceValueType> it = cvts.iterator();
        int n=1;
        while (it.hasNext()) {
            // Get each cvt and verify it
        	CodeSpaceValueType obj = (CodeSpaceValueType)it.next();
        	
        	if(n==1)
        	{
	        	assertTrue(obj.getCodeSpace().equals("c1"));
	        	assertTrue(obj.getLabel().equals("l1"));
	        	assertTrue(obj.getValue().equals("v1"));
        	}
        	
        	if(n==2)
        	{
	        	assertTrue(obj.getCodeSpace().equals("c2"));
	        	assertTrue(obj.getLabel().equals("l2"));
	        	assertTrue(obj.getValue().equals("v2"));
        	}
        	
        	n++;
        }
        
        log.debug("Interest size: " + result2.getInterests().size());
        log.debug(result2.toString());
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-ED-dataSrcContext.xml" };
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {

    }

    private ResourceProfileModel buildSampleProfile() {

        ResourceProfileModel element = new ResourceProfileModel();
        element.setIdentifier("IncidentCommander@core1.saic.com");
        // interests
        Set<InterestElement> sampleInterests = new HashSet<InterestElement>();
        InterestElement interest = new InterestElement();
        interest.setMessageContent(MESSAGE_CONTENT);
        interest.setTopicExpression("Incident");
        InterestNamespaceType nsType = new InterestNamespaceType();
        nsType.setPrefix("inc");
        nsType.setUri("hhtp://uicds.org/incident");
        Set<InterestNamespaceType> nsTypes = new HashSet<InterestNamespaceType>();
        nsTypes.add(nsType);
        interest.setNamespaces(nsTypes);
        sampleInterests.add(interest);
        element.setInterests(sampleInterests);

   //     HashMap<String, String> typeMap = new HashMap<String, String>();
   //     typeMap.put("c1,l1", "v1");
   //     typeMap.put("c1,l2", "v2");
   //     element.setResourceTyping(typeMap);
        
        //use CodeSpaceValueType, not map<string, string>
        HashSet<CodeSpaceValueType> cvts = new HashSet<CodeSpaceValueType>();   
        CodeSpaceValueType cvt1 = new CodeSpaceValueType();
        cvt1.setCodeSpace("c1");
        cvt1.setLabel("l1");
        cvt1.setValue("v1");

        cvts.add(cvt1);

        CodeSpaceValueType cvt2 = new CodeSpaceValueType();
        cvt2.setCodeSpace("c2");
        cvt2.setLabel("l2");
        cvt2.setValue("v2");
        cvts.add(cvt2);
        element.setCvts(cvts);
        
        return element;
    }

}
