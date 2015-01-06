package com.saic.uicds.core.infrastructure.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.saic.uicds.core.infrastructure.util.LdapUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/test/resources/contexts/test-LdapUtilContext.xml", })
public class LdapUtilTest {

    @Autowired
    LdapUtil ldapUtil;

    private static String[] groups = new String[] { "uicds-admin", "uicds-users", "group1" };

    @Test
    public void testGroupContainsMember() {

        for (String group : groups) {
            List<String> members = ldapUtil.getGroupMembers(group);
            if (members.size() > 0) {
                System.out.println("Group: " + group + " contains");
            }
            for (String member : members) {
                System.out.println("\tMember: " + member);
            }
            System.out.println();
        }
    }
}
