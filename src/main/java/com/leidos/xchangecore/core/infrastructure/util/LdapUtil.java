/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leidos.xchangecore.core.infrastructure.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vmuser
 */
public class LdapUtil {

    private static Logger logger = LoggerFactory.getLogger(LdapUtil.class);
    private static Hashtable<String, String> env = new Hashtable<String, String>();

    public static String GroupName_UICDS_USERS = "uicds-users";

    // ldap connection parameters
    private static String S_SecurityPrincipal = "cn=\"Directory Manager\"";
    private static String S_ConnectionUrl = "ldap://localhost:389/dc=uicds,dc=us";

    // some useful regex
    private static Pattern commonNamePattern = Pattern.compile("cn=([^,]+)");

    static {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        // intialize connection string
        env.put(Context.PROVIDER_URL, S_ConnectionUrl);

        // initialize lookup access (default Directory Manager)
        env.put(Context.SECURITY_PRINCIPAL, S_SecurityPrincipal);
    }

    public LdapUtil() {

    }

    public String[] getCNLocation(String cn) {

        logger.debug("getCNLocation: Looking up lat/lon for cn=" + cn);

        String[] locationArray = new String[] {
            "", ""
        };

        try {
            // Create initial context
            DirContext ctx = new InitialDirContext(env);

            String searchFilter = "(cn=" + cn + ")";

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrsFilter = {
                "geoLatitude", "geoLongitude"
            };
            searchControls.setReturningAttributes(attrsFilter);

            NamingEnumeration<SearchResult> results = ctx.search("", searchFilter, searchControls);

            SearchResult searchResult;

            String latitude;
            String longitude;

            if (results.hasMoreElements()) {
                searchResult = results.nextElement();
                Attribute members = searchResult.getAttributes().get("geoLatitude");
                if (members != null) {
                    latitude = (String) members.get();
                    locationArray[0] = latitude;
                }
                members = searchResult.getAttributes().get("geoLongitude");
                if (members != null) {
                    longitude = (String) members.get();
                    locationArray[1] = longitude;
                }
            }
            logger.debug("getCNLocation: [lat/lon]: [" + locationArray[0] + "/" + locationArray[1] +
                         "]");
            // Close the context when we're done
            ctx.close();
        } catch (Exception e) {
            logger.error("getCNLocation: " + e.getMessage());
        }

        return locationArray;
    }

    public ArrayList<String> getGroupMembers(String group) {

        logger.debug("Looking up members of group: " + group);

        ArrayList<String> membersArray = new ArrayList<String>();

        try {
            // Create initial context
            DirContext ctx = new InitialDirContext(env);

            String searchFilter = "(cn=" + group + ")";

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrsFilter = {
                "uniqueMember"
            };
            searchControls.setReturningAttributes(attrsFilter);

            NamingEnumeration<SearchResult> results = ctx.search("", searchFilter, searchControls);

            SearchResult searchResult;

            Matcher matcher;
            while (results.hasMoreElements()) {
                searchResult = results.nextElement();
                Attribute members = searchResult.getAttributes().get("uniqueMember");
                for (int i = 0; i < members.size(); i++) {
                    matcher = LdapUtil.commonNamePattern.matcher(members.get(i).toString());
                    if (matcher.find()) {
                        membersArray.add(matcher.group(1));
                    }
                }
            }

            // Close the context when we're done
            ctx.close();
        } catch (Exception e) {
            logger.error("getGroupMembers: " + e.getMessage());
        }

        return membersArray;

    }

    public List<String> getMembersForUicdsUsersGroup() {

        return getGroupMembers(GroupName_UICDS_USERS);
    }

    public Boolean groupContainsMember(String group, String member) {

        logger.debug("Does group " + group + " contain member " + member);

        try {
            // Create initial context
            DirContext ctx = new InitialDirContext(env);

            String searchFilter = "(&(uniqueMember=cn=" + member +
                                  ",dc=uicds,dc=us)(objectClass=groupOfUniqueNames))";

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> results = ctx.search("", searchFilter, searchControls);

            // Close the context when we're done
            ctx.close();

            if (results.hasMoreElements()) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            logger.error("groupContainsMember: " + e.getMessage());
            return false;
        }
    }

    public void setPassword(String password) {

        LdapUtil.env.put(Context.SECURITY_CREDENTIALS, password);
    }
}
