/**
 * 
 */
package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jpa.AbstractJpaTests;

import com.saic.uicds.core.infrastructure.dao.hb.InterestGroupDAOHibernate;
import com.saic.uicds.core.infrastructure.messages.InterestGroupStateNotificationMessage;
import com.saic.uicds.core.infrastructure.model.InterestGroup;

/**
 * @author roger
 * 
 */
public class InterestGroupDAOTest
    extends AbstractJpaTests {

    private Logger log = LoggerFactory.getLogger(InterestGroupDAOTest.class);

    @Autowired
    InterestGroupDAOHibernate interestGroupDAO;

    private final static String GROUP_TYPE = "Incident";

    private final static String GROUP1_GROUP_ID = "incident1";
    private final static String GROUP1_NAME = "name1";
    private final static String GROUP1_OWNER = "owner1";
    private final static String GROUP1_DESC = "group1 description";

    private final static String GROUP2_GROUP_ID = "incident2";
    private final static String GROUP2_NAME = "name2";
    private final static String GROUP2_OWNER = "owner2";
    private final static String GROUP2_DESC = "group2 description";

    private final static String GROUP3_GROUP_ID = "incident3";
    private final static String GROUP3_NAME = "name3";
    private final static String GROUP3_DESC = "group3 description";

    private final static String BIG_DESC = "Incident used in CreateIncident Test Media outlets are reporting that a person was shot and killed at the East Point MARTA station on E. Main St. and Cambridge Ave. DC Fire EMS is reporting a power outage in the area of Kenilworth Ave NE and Nannie Helen Burroughs Ave NE. Reports indicate that police have taken three suspects into custody following a shooting in the area of 2nd Ave and E 99th St. There were unconfirmed reports that a man was holding a woman hostage in the area of S 72nd Ave and Grover St. DC Fire EMS reported a power outage in the area of Kenilworth Ave NE and Nannie Helen Burroughs Ave NE. MARTA is reporting that the East Point station on E. Main St. and Cambridge Ave has reopened and trains service has resumed.  DC Fire EMS reported a power outage in the area of Kenilworth Ave NE and Nannie Helen Burroughs Ave NE.";

    @Override
    protected String[] getConfigLocations() {

        return new String[] {
            "classpath:contexts/applicationContext-infra.xml",
            "classpath:contexts/test-dataSrcContext.xml" };
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {

    }

    @Test
    public void testFindByInterestGroup() {

        InterestGroup ig = interestGroupDAO.findByInterestGroup(GROUP1_GROUP_ID);
        assertNull("found IG is not null", ig);

        InterestGroup interestGroup1 = addInterestGroup1();
        assertNotNull("interest group 1 is null", interestGroup1);

        findAndCheckInterestGroup1();

        InterestGroup interestGroup2 = addInterestGroup2();
        assertNotNull("interest group 2 is null", interestGroup2);

        findAndCheckInterestGroup1();
        findAndCheckInterestGroup2();

        interestGroupDAO.makeTransient(interestGroup1);

        ig = interestGroupDAO.findByInterestGroup(GROUP1_GROUP_ID);
        assertNull("found IG 1 is not null", ig);

        interestGroupDAO.delete(GROUP2_GROUP_ID, false);
        ig = interestGroupDAO.findByInterestGroup(GROUP2_GROUP_ID);
        assertFalse("IG 2 as not closed", ig.isActive());
        assertNotNull("IG 2 was not closed but removed instead", ig);

        interestGroupDAO.delete(GROUP2_GROUP_ID, true);

        ig = interestGroupDAO.findByInterestGroup(GROUP2_GROUP_ID);
        assertNull("found IG 2 is not null", ig);
    }

    @Test
    public void testFindByOwner() {

        List<InterestGroup> list = interestGroupDAO.findByOwningCore(GROUP1_OWNER);
        assertNotNull("found IG is not null", list);
        assertEquals("wrong number of IG", 0, list.size());

        list = interestGroupDAO.findByOwningCore(GROUP2_OWNER);
        assertNotNull("found IG is not null", list);
        assertEquals("wrong number of IG", 0, list.size());

        InterestGroup interestGroup1 = addInterestGroup1();
        assertNotNull("interest group 1 is null", interestGroup1);

        InterestGroup interestGroup2 = addInterestGroup2();
        assertNotNull("interest group 2 is null", interestGroup2);

        InterestGroup interestGroup3 = addInterestGroup3();
        assertNotNull("interest group 3 is null", interestGroup2);

        list = interestGroupDAO.findByOwningCore(GROUP2_OWNER);
        assertNotNull("found IG is not null", list);
        assertEquals("wrong number of IG", 2, list.size());

        list = interestGroupDAO.findAll();
        assertNotNull("found IG is not null", list);
        assertEquals("wrong number of IG", 3, list.size());

        interestGroupDAO.makeTransient(interestGroup2);

        list = interestGroupDAO.findByOwningCore(GROUP2_OWNER);
        assertNotNull("found IG is not null", list);
        assertEquals("wrong number of IG", 1, list.size());

        list = interestGroupDAO.findByOwningCore(GROUP1_OWNER);
        assertNotNull("found IG is not null", list);
        assertEquals("wrong number of IG", 1, list.size());

        interestGroupDAO.makeTransient(interestGroup3);

        list = interestGroupDAO.findByOwningCore(GROUP2_OWNER);
        assertNotNull("found IG is not null", list);
        assertEquals("wrong number of IG", 0, list.size());

        interestGroupDAO.makeTransient(interestGroup1);

        list = interestGroupDAO.findByOwningCore(GROUP1_OWNER);
        assertNotNull("found IG is not null", list);
        assertEquals("wrong number of IG", 0, list.size());
    }

    @Test
    public void testLargeDescription() {

        InterestGroup interestGroup = addInterestGroup1WithBigDescription();

        interestGroup = interestGroupDAO.makePersistent(interestGroup);

        assertEquals("wrong description", BIG_DESC, interestGroup.getDescription());

        List<InterestGroup> list = interestGroupDAO.findByOwningCore(GROUP1_OWNER);
        assertEquals("wrong size of list", 1, list.size());
        assertEquals("wrong description", BIG_DESC, list.get(0).getDescription());
    }

    private void findAndCheckInterestGroup2() {

        InterestGroup ig;
        ig = interestGroupDAO.findByInterestGroup(GROUP2_GROUP_ID);
        assertNotNull("found IG is null", ig);
        assertEquals("wrong id", GROUP2_GROUP_ID, ig.getInterestGroupID());
    }

    private void findAndCheckInterestGroup1() {

        InterestGroup ig;
        ig = interestGroupDAO.findByInterestGroup(GROUP1_GROUP_ID);
        assertNotNull("found IG is null", ig);
        assertEquals("wrong id", GROUP1_GROUP_ID, ig.getInterestGroupID());
    }

    private InterestGroup addInterestGroup1() {

        InterestGroup interestGroup = new InterestGroup();
        interestGroup.setInterestGroupID(GROUP1_GROUP_ID);
        interestGroup.setInterestGroupType(GROUP_TYPE);
        interestGroup.setInterestGroupSubtype(GROUP_TYPE);
        interestGroup.setDescription(GROUP1_DESC);
        interestGroup.setName(GROUP1_NAME);
        interestGroup.setOwningCore(GROUP1_OWNER);
        interestGroup.setSharingStatus(InterestGroupStateNotificationMessage.SharingStatus.None.toString());
        interestGroup = interestGroupDAO.makePersistent(interestGroup);
        return interestGroup;
    }

    private InterestGroup addInterestGroup2() {

        InterestGroup interestGroup = new InterestGroup();
        interestGroup.setInterestGroupID(GROUP2_GROUP_ID);
        interestGroup.setInterestGroupType(GROUP_TYPE);
        interestGroup.setInterestGroupSubtype(GROUP_TYPE);
        interestGroup.setDescription(GROUP2_DESC);
        interestGroup.setName(GROUP2_NAME);
        interestGroup.setOwningCore(GROUP2_OWNER);
        interestGroup.setSharingStatus(InterestGroupStateNotificationMessage.SharingStatus.None.toString());
        interestGroup = interestGroupDAO.makePersistent(interestGroup);
        return interestGroup;
    }

    private InterestGroup addInterestGroup3() {

        InterestGroup interestGroup = new InterestGroup();
        interestGroup.setInterestGroupID(GROUP3_GROUP_ID);
        interestGroup.setInterestGroupType(GROUP_TYPE);
        interestGroup.setInterestGroupSubtype(GROUP_TYPE);
        interestGroup.setDescription(GROUP3_DESC);
        interestGroup.setName(GROUP3_NAME);
        interestGroup.setOwningCore(GROUP2_OWNER);
        interestGroup.setSharingStatus(InterestGroupStateNotificationMessage.SharingStatus.None.toString());
        interestGroup = interestGroupDAO.makePersistent(interestGroup);
        return interestGroup;
    }

    private InterestGroup addInterestGroup1WithBigDescription() {

        InterestGroup interestGroup = new InterestGroup();
        interestGroup.setInterestGroupID(GROUP1_GROUP_ID);
        interestGroup.setInterestGroupType(GROUP_TYPE);
        interestGroup.setInterestGroupSubtype(GROUP_TYPE);
        interestGroup.setDescription(BIG_DESC);
        interestGroup.setName(GROUP1_NAME);
        interestGroup.setOwningCore(GROUP1_OWNER);
        interestGroup.setSharingStatus(InterestGroupStateNotificationMessage.SharingStatus.None.toString());
        interestGroup = interestGroupDAO.makePersistent(interestGroup);
        return interestGroup;
    }

}
