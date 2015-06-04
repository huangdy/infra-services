package com.leidos.xchangecore.core.infrastructure.util;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath*.*/CoreProperties.xml", "file:src/test/resources/contexts/test-LdapUtilContext.xml"
})
public class LdapUtilTest {

    @Autowired
    LdapUtil ldapUtil;

    @Test
    public void testListOfMembers() {

        List<String> members = ldapUtil.listOfMembers();
        for (String member : members) {
            System.out.println("Member: " + member);
        }
    }

}
