package com.saic.uicds.core.infrastructure.service;

import gov.ucore.ucore.x20.DigestDocument;
import gov.ucore.ucore.x20.IdentifierType;
import gov.ucore.ucore.x20.ThingType;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;

public class TestString {

    @Test
    public void addIdentifierToDigestEvent() {

        String digestText = "<Digest xmlns=\"http://ucore.gov/ucore/2.0\"><DigestMetadata/><Event id=\"Entity001\"><Descriptor>Some description</Descriptor><What code=\"Equipment\" /><Identifier ns:label=\"InterestGroupID\" xmlns:ns=\"http://ucore.gov/ucore/2.0\">IG-1234567890</Identifier></Event></Digest>";
        DigestDocument digest;
        try {
            digest = DigestDocument.Factory.parse(digestText);
            ThingType thing = digest.getDigest().getThingAbstractArray(0);
            if (thing != null && thing instanceof ThingType && thing.sizeOfIdentifierArray() == 0) {
                IdentifierType id = thing.addNewIdentifier();
                id.addNewLabel().setStringValue("InterestGroupID");
                id.setStringValue("IG-1234567890");
                thing.setIdentifierArray(0, id);

            }
            System.out.println("Digest:\n" + digest.xmlText());
        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getFirst(String ids) {

        if (ids.length() > 0) {
            int index = ids.indexOf(" ");
            return index == -1 ? ids
                              : ids.substring(0, index);
        } else {
            return ids;
        }
    }

    @Test
    public void testString() {

        String ids = "IG-123";
        System.out.println("id: " + getFirst(ids));
        ids = "";
        System.out.println("id: " + getFirst(ids));
        ids = "IG-123 IG-345 IG-234";
        System.out.println("id: " + getFirst(ids));
    }
}
