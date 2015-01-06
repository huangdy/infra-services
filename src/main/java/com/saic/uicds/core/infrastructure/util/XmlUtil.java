package com.saic.uicds.core.infrastructure.util;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;

public class XmlUtil {

    public static final XmlOptions normal = new XmlOptions().setSavePrettyPrint()
        .setLoadStripWhitespace();

    public static final XmlOptions innerOnly = new XmlOptions().setSavePrettyPrint().setSaveInner();

    public static String getDOMString(Document doc) {

        String s = null;
        TransformerFactory tfactory = TransformerFactory.newInstance();
        try {
            Transformer xform = tfactory.newTransformer();
            Source src = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            xform.transform(src, result);
            s = writer.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s;
    }

    public static String getTextFromAny(XmlObject object) {
        XmlCursor c = object.newCursor();
        String text = c.getTextValue();
        c.dispose();
        return text;
    }

    public static final void substitute(XmlObject parentObject,
                                        String subNamespace,
                                        String subTypeName,
                                        SchemaType subSchemaType,
                                        XmlObject theObject) {

        XmlObject subObject =
            parentObject.substitute(new QName(subNamespace, subTypeName), subSchemaType);
        if (subObject != parentObject) {
            subObject.set(theObject);
        }
    }

}
