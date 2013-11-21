package com.sun.facelets.tag;

import com.sun.facelets.FaceletTestCase;
import com.sun.facelets.FaceletViewHandler;

public class TagAttributeTest extends FaceletTestCase {

    public void testToStringDevMode() {

        Location location = new Location("location", 1, 1);
        TagAttribute tagAttribute = new TagAttribute(location, "ns", "localName", "qName", "value");

        assertEquals("location @1,1 qName=\"value\"", tagAttribute.toString());
    }

    public void testToStringProdMode() {
        this.servletContext.setInitParameter(FaceletViewHandler.PARAM_DEVELOPMENT, "false");

        Location location = new Location("location", 1, 1);
        TagAttribute tagAttribute = new TagAttribute(location, "ns", "localName", "qName", "value");

        assertEquals("no debugging information available", tagAttribute.toString());
    }
}
